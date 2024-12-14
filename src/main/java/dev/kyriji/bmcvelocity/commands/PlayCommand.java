package dev.kyriji.bmcvelocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.wiji.bigminecraftapi.BigMinecraftAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class PlayCommand implements SimpleCommand {

	@Override
	public void execute(final Invocation invocation) {
		CommandSource source = invocation.source();
		String[] args = invocation.arguments();

		if(!(source instanceof Player player)) {
			source.sendMessage(Component.text("Only players can use this command.").color(TextColor.color(255, 0, 0)));
			return;
		}

		if (args.length == 0) {
			source.sendMessage(Component.text("Usage: /play <game>").color(TextColor.color(255, 0, 0)));
			return;
		}

		String game = args[0];
		BigMinecraftAPI.getNetworkManager().queuePlayer(player.getUniqueId(), game);
	}
}
