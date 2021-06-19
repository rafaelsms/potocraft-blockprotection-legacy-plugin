package com.rafaelsms.blockprotection.friends.commands;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Lang;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
                Lang.FRIENDS_FRIEND_DELETE_COMMAND_HELP.sendMessage(sender);
                return true;
            }

            // Get player id by name
            @SuppressWarnings("deprecation") OfflinePlayer friend = plugin.getServer().getOfflinePlayer(args[0]);
            // Check if UUID is null
            if (!friend.hasPlayedBefore() && !friend.isOnline()) {
                sender.sendMessage(Lang.FRIENDS_FRIEND_NOT_FOUND.toColoredString().formatted(args[0]));
                return true;
            }

            // Add to the database
            if (plugin.getFriendsDatabase().removeFriend(((Player) sender).getUniqueId(), friend.getUniqueId())) {
                sender.sendMessage(Lang.FRIENDS_FRIEND_DELETE_SUCCESS.toColoredString().formatted(args[0]));
                return true;
            } else {
                Lang.FRIENDS_DATABASE_FAILURE.sendMessage(sender);
                return true;
            }
        } else {
            Lang.FRIENDS_CANT_BE_CONSOLE.sendMessage(sender);
            return true;
        }
    }
}