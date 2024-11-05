package dev.kyriji.bmcvelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.kyriji.bmcvelocity.listeners.InitialConnectListener;
import dev.kyriji.bmcvelocity.listeners.PlayerListener;
import dev.wiji.bigminecraftapi.BigMinecraftAPI;
import dev.wiji.bigminecraftapi.redis.RedisListener;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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

		INSTANCE.getEventManager().register(this, new InitialConnectListener());
		INSTANCE.getEventManager().register(this, new PlayerListener());

		new Thread(() -> {
			registerServers();

			BigMinecraftAPI.init();

			new RedisListener("instance-changed") {
				@Override
				public void onMessage(String message) {
					registerServers();
				}
			};
		}).start();
	}

	public void registerServers() {
		List<RegisteredServer> servers = new ArrayList<>(BigMinecraftVelocity.INSTANCE.getAllServers());

		BigMinecraftAPI.getRedisManager().getInstances().forEach(instance -> {
			if (INSTANCE.getServer(instance.getName()).isPresent()) {
				servers.remove(INSTANCE.getServer(instance.getName()).get());
				return;
			}

			InetSocketAddress address = new InetSocketAddress(instance.getIp(), 25565);
			INSTANCE.registerServer(new ServerInfo(instance.getName(), address));
		});

		servers.forEach(server -> INSTANCE.unregisterServer(server.getServerInfo()));
	}
}