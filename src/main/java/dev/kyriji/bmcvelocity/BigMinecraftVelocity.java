package dev.kyriji.bmcvelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.kyriji.bigminecraftapi.BigMinecraftAPI;
import dev.kyriji.bigminecraftapi.controllers.RedisListener;
import dev.kyriji.bigminecraftapi.enums.RedisChannel;
import dev.kyriji.bmcvelocity.commands.PlayCommand;
import dev.kyriji.bmcvelocity.listeners.InitialConnectListener;
import dev.kyriji.bmcvelocity.listeners.PingListener;
import dev.kyriji.bmcvelocity.listeners.PlayerListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
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

		BigMinecraftAPI.init();

		INSTANCE.getEventManager().register(this, new InitialConnectListener());
		INSTANCE.getEventManager().register(this, new PlayerListener());
		INSTANCE.getEventManager().register(this, new PingListener());

		INSTANCE.getCommandManager().register(INSTANCE.getCommandManager().metaBuilder("play").build(), new PlayCommand());

		new Thread(() -> {
			registerServers();

			BigMinecraftAPI.getRedisManager().addListener(new RedisListener(RedisChannel.INSTANCE_MODIFIED) {
				@Override
				public void onMessage(String message) {
					registerServers();
				}
			});

			BigMinecraftAPI.getRedisManager().addListener(new RedisListener(RedisChannel.QUEUE_RESPONSE) {
				@Override
				public void onMessage(String message) {
					String[] parts = message.split(":");
					UUID uuid = UUID.fromString(parts[0]);
					String server = parts[1];

					RegisteredServer registeredServer = INSTANCE.getServer(server).orElse(null);
					Player player = INSTANCE.getPlayer(uuid).orElse(null);
					if(player == null) return;

					if(registeredServer == null) {
						player.sendMessage(Component.text("Game does not exist or is currently unavailable.")
								.color(TextColor.color(255, 0, 0)));

						return;
					}

					player.createConnectionRequest(registeredServer).fireAndForget();
				}
			});
		}).start();
	}

	public void registerServers() {
		List<RegisteredServer> servers = new ArrayList<>(BigMinecraftVelocity.INSTANCE.getAllServers());

		System.out.println("Attempting to register servers");
		BigMinecraftAPI.getNetworkManager().getInstances().forEach(instance -> {
			System.out.println("Registering server: " + instance.getName());
			if (INSTANCE.getServer(instance.getName()).isPresent()) {
				System.out.println("Server already exists: " + instance.getName());
				servers.remove(INSTANCE.getServer(instance.getName()).get());
				return;
			}

			System.out.println("Server does not exist: " + instance.getName());
			InetSocketAddress address = new InetSocketAddress(instance.getIp(), 25565);
			INSTANCE.registerServer(new ServerInfo(instance.getName(), address));
		});

		servers.forEach(server -> INSTANCE.unregisterServer(server.getServerInfo()));
	}
}