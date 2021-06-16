package com.rafaelsms.blockprotection.friends.commands;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DeleteCommand implements CommandExecutor {

	private final BlockProtectionPlugin plugin;

	public DeleteCommand(BlockProtectionPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (sender instanceof Player) {
			// Check argument length
			if (args.length != 1) {
				Lang.FRIENDS_FRIEND_DELETE_COMMAND_HELP.sendMessage(plugin, sender);
				return true;
			}

			// Get player id by name
			UUID friend = plugin.getServer().getPlayerUniqueId(args[0]);
			// Check if UUID is null
			if (friend == null) {
				sender.sendMessage(Lang.parseLegacyText(
						Lang.FRIENDS_FRIEND_NOT_FOUND.toString(plugin).formatted(args[0])));
				return true;
			}

			// Add to the database
			if (plugin.getFriendsDatabase().removeFriend(((Player) sender).getUniqueId(), friend)) {
				sender.sendMessage(Lang.parseLegacyText(
						Lang.FRIENDS_FRIEND_DELETE_SUCCESS.toString(plugin).formatted(args[0])));
				return true;
			} else {
				Lang.FRIENDS_DATABASE_FAILURE.sendMessage(plugin, sender);
				return true;
			}
		} else {
			Lang.FRIENDS_CANT_BE_CONSOLE.sendMessage(plugin, sender);
			return true;
		}
	}
}