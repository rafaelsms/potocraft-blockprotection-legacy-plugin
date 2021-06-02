package com.rafaelsms.blockprotection;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public enum Lang {

	// Block place/break/interaction
	PROTECTION_NEARBY_BLOCKS("config.lang.protection.nearby_blocks"),
	// Block place
	PROTECTION_UNDER_MINIMUM_HEIGHT("config.lang.protection.block_under_minimum_height"),
	PROTECTION_UNPROTECTED_WORLD("config.lang.protection.unprotected_world"),
	PROTECTION_BLOCK_WONT_BE_PROTECTED("config.lang.protection.unprotected_material_placed"),
	PROTECTION_DATABASE_FAILURE("config.lang.protection.database_insert_failure"),
	// Debug
	PROTECTION_DEBUG_DATABASE_FAILURE("config.lang.protection.debug.database_failure"),
	PROTECTION_DEBUG_NO_BLOCK("config.lang.protection.debug.no_block"),
	PROTECTION_DEBUG_TEXT("config.lang.protection.debug.debug_text"),

	// Friends
	FRIENDS_CANT_BE_CONSOLE("config.lang.friends.cant_be_console"),
	FRIENDS_NO_FRIENDS("config.lang.friends.no_friends"),
	FRIENDS_DATABASE_FAILURE("config.lang.friends.database_failure"),
	FRIENDS_FRIEND_NOT_FOUND("config.lang.friends.friend_not_found"),

	FRIENDS_FRIEND_LIST_INIT("config.lang.friends.friend_list.list_init"),
	FRIENDS_FRIEND_LIST_ITEM("config.lang.friends.friend_list.list_item"),
	FRIENDS_FRIEND_LIST_UNKNOWN("config.lang.friends.friend_list.unknown_name"),

	FRIENDS_FRIEND_ADD_COMMAND_HELP("config.lang.friends.friend_add.command_help"),
	FRIENDS_FRIEND_ADD_SUCCESS("config.lang.friends.friend_add.friend_added"),
	FRIENDS_FRIEND_ADD_ALREADY("config.lang.friends.friend_add.already_friends"),

	FRIENDS_FRIEND_DELETE_COMMAND_HELP("config.lang.friends.friend_delete.command_help"),
	FRIENDS_FRIEND_DELETE_SUCCESS("config.lang.friends.friend_delete.friend_deleted"),

	;

	private final String configurationPath;

	Lang(String configurationPath) {
		this.configurationPath = configurationPath;
	}

	public void sendActionBar(BlockProtectionPlugin plugin, @Nullable Player player) {
		// Check if there is a player to send a bar to
		if (player == null) {
			return;
		}
		player.sendActionBar(parseLegacyText(plugin.getConfig().getString(configurationPath)));
	}

	public void sendMessage(BlockProtectionPlugin plugin, CommandSender sender) {
		// Check if there is a player to send a message to
		if (sender == null) {
			return;
		}
		sender.sendMessage(parseLegacyText(plugin.getConfig().getString(configurationPath)));
	}

	public String toString(BlockProtectionPlugin plugin) {
		return plugin.getConfig().getString(configurationPath);
	}

	public static TextComponent parseLegacyText(String message) {
		// Get the message and parse all "&" commands
		return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
	}
}