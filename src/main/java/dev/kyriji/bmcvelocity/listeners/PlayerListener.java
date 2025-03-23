package dev.kyriji.bmcvelocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.kyriji.bigminecraftapi.BigMinecraftAPI;
import dev.kyriji.bigminecraftapi.enums.RedisChannel;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class PlayerListener {

	@Subscribe
	public void onConnect(LoginEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		String name = player.getUsername();

		String proxyIP = getHostname();
		String payload = playerId.toString() + ":" + name + ":" + proxyIP;

		BigMinecraftAPI.getRedisManager().publish(RedisChannel.PROXY_CONNECT, payload);
	}

	@Subscribe
	public void onDisconnect(DisconnectEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		String name = player.getUsername();

		String proxyIP = getHostname();
		String payload = playerId.toString() + ":" + name + ":" + proxyIP;

		BigMinecraftAPI.getRedisManager().publish(RedisChannel.PROXY_DISCONNECT, payload);
	}

	@Subscribe
	public void onInstanceSwitch(ServerConnectedEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		String name = player.getUsername();

		RegisteredServer server = event.getServer();
		String serverIP = server.getServerInfo().getAddress().getHostName();

		String payload = playerId.toString() + ":" + name + ":" + serverIP;

		BigMinecraftAPI.getRedisManager().publish(RedisChannel.INSTANCE_SWITCH, payload);
	}

	public String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch(UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
}
