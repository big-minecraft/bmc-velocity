package dev.kyriji.bmcvelocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerPing;
import dev.wiji.bigminecraftapi.BigMinecraftAPI;
import dev.wiji.bigminecraftapi.redis.RedisListener;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class PingListener {

	private int playerCount = 0;

	public PingListener() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Map<UUID, String> players = BigMinecraftAPI.getRedisManager().getPlayers();
				if (players != null) playerCount = players.size();
			}
		}, 0, 5000);
	}

	@Subscribe
	public void onConnect(ProxyPingEvent event) {
		ServerPing.Builder builder = event.getPing().asBuilder();
		builder.onlinePlayers(playerCount);

		event.setPing(builder.build());
	}
}
