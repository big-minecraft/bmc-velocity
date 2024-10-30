package dev.kyriji.bmcvelocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.wiji.bigminecraftapi.BigMinecraftAPI;
import dev.wiji.bigminecraftapi.redis.RedisListener;

import java.util.*;

public class EventHandler {

	public EventHandler() {

		new Thread(() -> {
			new RedisListener("initial-server-response") {
				@Override
				public void onMessage(String message) {
					String[] parts = message.split(" ");
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
			};
		}).start();
	}

	private final Map<UUID, String> initialServers = new HashMap<>();

	@Subscribe
	public void onPreConnect(PreLoginEvent event) {
		if(event.getUniqueId() == null) return;

		System.out.println("PreLoginEvent: " + event.getUniqueId().toString());
		BigMinecraftAPI.getRedisManager().publish("request-initial-server", event.getUniqueId().toString());
		System.out.println("Instance servers: " + BigMinecraftAPI.getRedisManager().getInstances().toString());
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
