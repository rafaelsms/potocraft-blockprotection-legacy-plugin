package com.rafaelsms.blockprotection;

import java.util.List;

public enum Config {

    // Database
    DATABASE_URL("config.database.url"),
    DATABASE_USER("config.database.user"),
    DATABASE_PASSWORD("config.database.password"),
    DATABASE_POOL_SIZE("config.database.pool_size"),
    DATABASE_CONNECTION_TIMEOUT("config.database.connection_timeout_ms"),
    DATABASE_LEAK_DETECTION_TIMEOUT("config.database.leak_detection_timeout_ms"),

    // Protection
    PROTECTION_DAYS_PROTECTED("config.protection.protected_for_days"),
    PROTECTION_MINIMUM_HEIGHT("config.protection.minimum_height_to_protect"),

    // Protect radius
    PROTECTION_BREAK_RADIUS("config.protection.protection_radius.break_radius"),
    PROTECTION_PLACE_RADIUS("config.protection.protection_radius.place_radius"),
    PROTECTION_INTERACT_RADIUS("config.protection.protection_radius.interact_radius"),

    // Update on place protection radius
    PROTECTION_UPDATE_TIME_RADIUS("config.protection.update_protection_radius.update_time_radius"),

    PROTECTION_SEARCH_TEMPORARY_RADIUS("config.protection.update_protection_radius.search_temporary_radius"),
    PROTECTION_BLOCK_COUNT_TO_PROTECT("config.protection.update_protection_radius.block_count_to_protect_temporary"),

    // Options
    PROTECTION_ALLOW_HAND_OPENING_IRON_DOOR("config.protection.options.allow_hand_opening_iron_doors"),
    PROTECTION_PREVENT_PLAYER_EXPLOSIONS("config.protection.options.prevent_player_explosion"),
    PROTECTION_PREVENT_HOSTILES_EXPLOSIONS("config.protection.options.prevent_hostiles_explosions"),
    PROTECTION_EXPLOSION_RADIUS_MULTIPLIER("config.protection.options.explosion_radius_multiplier"),
    PROTECTION_PREVENT_FIRE_SPREAD("config.protection.options.prevent_fire_spread"),
    PROTECTION_PREVENT_LAVA_SPREAD("config.protection.options.prevent_lava_spread"),
    PROTECTION_PREVENT_VEHICLE_ENTER("config.protection.options.prevent_vehicle_enter"),
    PROTECTION_CHECK_PATH_TO_INVENTORY_HOLDER("config.protection.options.check_open_inventory_holder"),
    PROTECTION_PREVENT_CHORUS_FRUIT_TELEPORT("config.protection.options.prevent_chorus_fruit_teleport"),
    PROTECTION_PREVENT_ENDERPEARL_TELEPORT("config.protection.options.prevent_enderpearl_teleport"),
    PROTECTION_PREVENT_BED_ENTER("config.protection.options.prevent_bed_enter"),

    // Worlds
    PROTECTION_PROTECTED_WORLDS("config.protection.protected_worlds"),

    // Materials
    PROTECTION_DEBUG_ITEM("config.protection.materials.debug_item"),
    PROTECTION_MATERIALS_BLOCKS_DENIED_INTERACTION("config.protection.materials.denied_interaction_block"),
    PROTECTION_MATERIALS_HAND_ALLOWED_INTERACTION("config.protection.materials.allowed_interaction_hand"),
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
