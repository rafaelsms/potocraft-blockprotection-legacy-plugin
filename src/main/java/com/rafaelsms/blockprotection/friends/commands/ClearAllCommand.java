package com.rafaelsms.blockprotection.friends.commands;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Lang;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClearAllCommand implements CommandExecutor {

    private final BlockProtectionPlugin plugin;

    public ClearAllCommand(BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            if (plugin.getFriendsDatabase().removeAllFriends(((Player) sender).getUniqueId())) {
                Lang.FRIENDS_DELETE_ALL_SUCCESS.sendMessage(sender);
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
