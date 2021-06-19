package com.rafaelsms.blockprotection.friends.commands;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Lang;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ListCommand implements CommandExecutor {

    private final BlockProtectionPlugin plugin;

    public ListCommand(BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            // Get from database
            Set<OfflinePlayer> friends = plugin.getFriendsDatabase().listFriends(((Player) sender).getUniqueId());

            // Check if is valid
            if (friends == null) {
                Lang.FRIENDS_DATABASE_FAILURE.sendMessage(sender);
                return true;
            }

            // Check if there is none
            if (friends.isEmpty()) {
                Lang.FRIENDS_NO_FRIENDS.sendMessage(sender);
                return true;
            }

            boolean notPlayedBefore = false;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Lang.FRIENDS_FRIEND_LIST_INIT.toColoredString());

            // Iterate through all friends
            for (OfflinePlayer friend : friends) {
                // Get friend name
                if (friend.getName() != null) {
                    // Add warning before the name
                    if (!friend.hasPlayedBefore()) {
                        stringBuilder.append('*');
                        notPlayedBefore = true;
                    }

                    stringBuilder.append(Lang.FRIENDS_FRIEND_LIST_ITEM.toColoredString().formatted(friend.getName()));
                } else {
                    stringBuilder.append(Lang.FRIENDS_FRIEND_LIST_UNKNOWN.toColoredString());
                }
            }

            // Send built message
            sender.sendMessage(stringBuilder.toString());
            if (notPlayedBefore) {
                Lang.FRIENDS_FRIEND_LIST_NOT_PLAYED_BEFORE.sendMessage(sender);
            }
            return true;
        } else {
            Lang.FRIENDS_CANT_BE_CONSOLE.sendMessage(sender);
            return true;
        }
    }
}