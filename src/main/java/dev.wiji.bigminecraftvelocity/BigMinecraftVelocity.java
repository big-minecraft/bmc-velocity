package dev.wiji.bigminecraftvelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.wiji.bigminecraftapi.BigMinecraftAPI;
import dev.wiji.bigminecraftapi.redis.RedisListener;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.logging.Logger;

@Plugin(id = "bmc-velocity", name = "BigMinecraftVelocity", version = "1.0",
		url = "https://github.com/kyriji", description = "A connector plugin for BigMinecraft", authors = {"wiji, Kyro"})
public class BigMinecraftVelocity {

	public static ProxyServer INSTANCE;
	public final Logger logger;

	@Inject
	public BigMinecraftVelocity(ProxyServer server, Logger logger) {
		INSTANCE = server;
		this.logger = logger;
	}

	@Subscribe
	public void onProxyInitialize(ProxyInitializeEvent event) {
		System.out.println("BigMinecraftVelocity loaded");

		INSTANCE.getEventManager().register(this, new EventHandler());

		new Thread(() -> {
			registerServers();

			BigMinecraftAPI.init();

			new RedisListener("instance-registered") {
				@Override
				public void onMessage(String message) {
					registerServers();
				}
			};
		}).start();
	}

	public void registerServers() {
		BigMinecraftAPI.getRedisManager().getInstances().forEach(instance -> {
			if (INSTANCE.getServer(instance.getName()).isPresent()) return;

			InetSocketAddress address = new InetSocketAddress(instance.getIp(), 25565);
			INSTANCE.registerServer(new ServerInfo(instance.getName(), address));
		});
	}
}