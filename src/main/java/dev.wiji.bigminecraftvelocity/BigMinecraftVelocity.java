package dev.wiji.bigminecraftvelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.wiji.bigminecraftapi.BigMinecraftAPI;
import dev.wiji.bigminecraftapi.redis.RedisListener;

import java.util.UUID;
import java.util.logging.Logger;


@Plugin(id = "bmc-velocity", name = "BigMinecraftVelocity", version = "1.0",
		url = "https://github.com/kyriji", description = "A connector plugin for BigMinecraft", authors = {"wiji, Kyro"})
public class BigMinecraftVelocity {

	public final ProxyServer server;
	public final Logger logger;
	public final UUID serverID;

	@Inject
	public BigMinecraftVelocity(ProxyServer server, Logger logger) {
		this.server = server;
		this.logger = logger;

		this.serverID = UUID.randomUUID();

		logger.info("Hello there! I made my first plugin with Velocity.");

		new Thread(() -> {
			BigMinecraftAPI.init();
			BigMinecraftAPI.getRedisManager().publish("proxy", "Server " + serverID + " has connected!");

			new RedisListener("proxy") {
				@Override
				public void onMessage(String message) {
					System.out.println("Received PROXY message: " + message);
				}
			};
		}).start();

	}
}