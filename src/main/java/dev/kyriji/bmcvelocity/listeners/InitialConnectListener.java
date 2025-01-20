package dev.kyriji.bmcvelocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.kyriji.bmcvelocity.BigMinecraftVelocity;
import dev.wiji.bigminecraftapi.BigMinecraftAPI;
import dev.wiji.bigminecraftapi.controllers.RedisListener;
import dev.wiji.bigminecraftapi.enums.RedisChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.*;

public class InitialConnectListener {

	public InitialConnectListener() {

		new Thread(() -> {
			BigMinecraftAPI.getRedisManager().addListener(new RedisListener(RedisChannel.INITIAL_INSTANCE_RESPONSE) {
				@Override
				public void onMessage(String message) {
					String[] parts = message.split(":");
					UUID player = UUID.fromString(parts[0]);
					String server = parts[1];

					if (initialServers.containsKey(player)) return;
					initialServers.put(player, server);

					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							initialServers.remove(player);
						}
					}, 5000);
				}
			});
		}).start();
	}

	private final Map<UUID, String> initialServers = new HashMap<>();

	@Subscribe
	public void onPreConnect(PreLoginEvent event) {
		if(event.getUniqueId() == null) return;
		if(BigMinecraftAPI.getNetworkManager().isPlayerConnected(event.getUniqueId())) {
			event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("You are already connected to the network.").color(TextColor.color(255, 0, 0))));
			return;
		}

		System.out.println("PreLoginEvent: " + event.getUniqueId().toString());
		BigMinecraftAPI.getRedisManager().publish(RedisChannel.REQUEST_INITIAL_INSTANCE, event.getUniqueId().toString());
		System.out.println("Instance servers: " + BigMinecraftAPI.getNetworkManager().getInstances().toString());
	}

	@Subscribe
	public void onInitialServerChoose(PlayerChooseInitialServerEvent event) {
		UUID player = event.getPlayer().getUniqueId();

		String serverString = initialServers.get(player);

		System.out.println("Server string: " + serverString);

		if (serverString == null) return;

		initialServers.remove(player);

		System.out.println(BigMinecraftVelocity.INSTANCE.getAllServers().toString());

		Optional<RegisteredServer> optionalServer = BigMinecraftVelocity.INSTANCE.getServer(serverString);
		optionalServer.ifPresent(event::setInitialServer);
	}
}
