package com.rafaelsms.blockprotection;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public enum Lang {

    // Block place/break/interaction
    PROTECTION_NEARBY_BLOCKS("config.lang.protection.nearby_blocks"),
    PROTECTION_NEARBY_BLOCKS_OWNED("config.lang.protection.nearby_blocks_owned_by"),
    // Block place
    PROTECTION_UNDER_MINIMUM_HEIGHT("config.lang.protection.block_under_minimum_height"),
    PROTECTION_UNPROTECTED_WORLD("config.lang.protection.unprotected_world"),
    PROTECTION_BLOCK_NOT_PROTECTED_MATERIAL("config.lang.protection.unprotected_material_placed"),
    PROTECTION_BLOCK_PROTECTING("config.lang.protection.protecting"),
    PROTECTION_BLOCK_NOT_PROTECTING("config.lang.protection.not_protecing"),
    PROTECTION_DATABASE_FAILURE("config.lang.protection.database_insert_failure"),
    // Debug
    PROTECTION_DEBUG_DATABASE_FAILURE("config.lang.protection.debug.database_failure"),
    PROTECTION_DEBUG_NO_BLOCK("config.lang.protection.debug.no_block"),
    PROTECTION_DEBUG_TEXT("config.lang.protection.debug.debug_text"),
    PROTECTION_DEBUG_LIST_TITLE("config.lang.protection.debug.debug_list.title"),
    PROTECTION_DEBUG_LIST_TEXT("config.lang.protection.debug.debug_list.text"),
    PROTECTION_DEBUG_LIST_EMPTY("config.lang.protection.debug.debug_list.empty"),

    // Friends
    FRIENDS_CANT_BE_CONSOLE("config.lang.friends.cant_be_console"),
    FRIENDS_NO_FRIENDS("config.lang.friends.no_friends"),
    FRIENDS_DATABASE_FAILURE("config.lang.friends.database_failure"),
    FRIENDS_FRIEND_NOT_FOUND("config.lang.friends.friend_not_found"),

    FRIENDS_FRIEND_LIST_INIT("config.lang.friends.friend_list.list_init"),
    FRIENDS_FRIEND_LIST_ITEM("config.lang.friends.friend_list.list_item"),
    FRIENDS_FRIEND_LIST_UNKNOWN("config.lang.friends.friend_list.unknown_name"),
    FRIENDS_FRIEND_LIST_NOT_PLAYED_BEFORE("config.lang.friends.friend_list.has_not_played"),

    FRIENDS_FRIEND_ADD_COMMAND_HELP("config.lang.friends.friend_add.command_help"),
    FRIENDS_FRIEND_ADD_SUCCESS("config.lang.friends.friend_add.friend_added"),
    FRIENDS_FRIEND_ADD_ALREADY("config.lang.friends.friend_add.already_friends"),
    FRIENDS_FRIEND_ADD_CANNOT_BE_YOU("config.lang.friends.friend_add.friend_can_not_be_you"),

    FRIENDS_FRIEND_DELETE_COMMAND_HELP("config.lang.friends.friend_delete.command_help"),
    FRIENDS_FRIEND_DELETE_SUCCESS("config.lang.friends.friend_delete.friend_deleted"),

    ;

    private static BlockProtectionPlugin plugin;

    private final String configurationPath;

    Lang(String configurationPath) {
        this.configurationPath = configurationPath;
    }

    public BaseComponent[] toComponent() {
        return parseLegacyText(this);
    }

    public void sendActionBar(@Nullable Player player) {
        // Check if there is a player to send a bar to
        if (player == null) {
            return;
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, toComponent());
    }

    public void sendMessage(CommandSender sender) {
        // Check if there is a player to send a message to
        if (sender == null) {
            return;
        }
        sender.spigot().sendMessage(toComponent());
    }

    public String toColoredString() {
        return ChatColor.translateAlternateColorCodes('&', toString());
    }

    @Override
    public String toString() {
        return plugin.getConfig().getString(configurationPath);
    }

    public static BaseComponent[] parseLegacyText(Lang lang) {
        // Get the message and parse all "&" commands
        return TextComponent.fromLegacyText(lang.toColoredString());
    }

    public static void setPlugin(BlockProtectionPlugin plugin) {
        Lang.plugin = plugin;
    }
}