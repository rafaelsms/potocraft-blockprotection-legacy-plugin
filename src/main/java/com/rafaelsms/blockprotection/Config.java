package com.rafaelsms.blockprotection;

import java.util.List;

public enum Config {

    // Database
    DATABASE_URL("config.database.url"),
    DATABASE_USER("config.database.user"),
    DATABASE_PASSWORD("config.database.password"),
    DATABASE_POOL_SIZE("config.database.pool_size"),

    // Protection
    PROTECTION_DAYS_PROTECTED("config.protection.protected_for_days"),
    PROTECTION_MINIMUM_HEIGHT("config.protection.minimum_height_to_protect"),
    PROTECTION_PROTECTION_BREAK_RADIUS("config.protection.protection_break_radius"),
    PROTECTION_PROTECTION_PLACE_RADIUS("config.protection.protection_place_radius"),
    PROTECTION_PROTECTION_INTERACT_RADIUS("config.protection.protection_interact_radius"),
    PROTECTION_PROTECTION_UPDATE_RADIUS("config.protection.protection_update_time_radius"),
    PROTECTION_PROTECTION_SEARCH_RADIUS("config.protection.protection_search_radius"),
    PROTECTION_PROTECTION_BLOCK_COUNT("config.protection.protection_block_count"),
    PROTECTION_PROTECTED_WORLDS("config.protection.protected_worlds"),
    // Materials
    PROTECTION_DEBUG_ITEM("config.protection.materials.debug_item"),
    PROTECTION_MATERIALS_DENIED_INTERACTION("config.protection.materials.denied_interaction"),
    PROTECTION_MATERIALS_PROTECTED("config.protection.materials.protected_materials"),

    ;

    private static BlockProtectionPlugin plugin;

    private final String configurationPath;

    Config(String configurationPath) {
        this.configurationPath = configurationPath;
    }

    public String getString() {
        return plugin.getConfig().getString(configurationPath);
    }

    public int getInt() {
        return plugin.getConfig().getInt(configurationPath);
    }

    public double getDouble() {
        return plugin.getConfig().getDouble(configurationPath);
    }

    public boolean getBoolean() {
        return plugin.getConfig().getBoolean(configurationPath);
    }

    public List<String> getStringList() {
        return plugin.getConfig().getStringList(configurationPath);
    }

    @Override
    public String toString() {
        return configurationPath;
    }

    public static void setPlugin(BlockProtectionPlugin plugin) {
        Config.plugin = plugin;
    }
}
