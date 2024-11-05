package dev.kyriji.bmcvelocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.kyriji.bmcvelocity.BigMinecraftVelocity;
import dev.wiji.bigminecraftapi.BigMinecraftAPI;
import dev.wiji.bigminecraftapi.redis.RedisListener;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class PlayerListener {

	@Subscribe
	public void onConnect(LoginEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		String name = player.getUsername();

		String proxyIP = getHostname();
		String payload = playerId.toString() + ":" + name + ":" + proxyIP;

		BigMinecraftAPI.getRedisManager().publish("proxy-connect", payload);
	}

	@Subscribe
	public void onDisconnect(DisconnectEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		String name = player.getUsername();

		String proxyIP = getHostname();
		String payload = playerId.toString() + ":" + name + ":" + proxyIP;

		BigMinecraftAPI.getRedisManager().publish("proxy-disconnect", payload);
	}

	@Subscribe
	public void onInstanceSwitch(ServerConnectedEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		String name = player.getUsername();

		RegisteredServer server = event.getServer();
		String serverUid = server.getServerInfo().getName();

		String payload = playerId.toString() + ":" + name + ":" + serverUid;

		BigMinecraftAPI.getRedisManager().publish("server-switch", payload);
	}

	public String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch(UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
}
