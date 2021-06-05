package com.rafaelsms.blockprotection;

public enum Config {

	// Database
	DATABASE_URL("config.database.url"),
	DATABASE_USER("config.database.user"),
	DATABASE_PASSWORD("config.database.password"),
	DATABASE_POOL_SIZE("config.database.pool_size"),

	// Protection
	PROTECTION_DAYS_PROTECTED("config.protection.protected_for_days"),
	PROTECTION_MINIMUM_HEIGHT("config.protection.minimum_height_to_protect"),
	PROTECTION_PROTECTION_RADIUS("config.protection.protection_radius"),
	PROTECTION_PROTECTED_WORLDS("config.protection.protected_worlds"),
	// Materials
	PROTECTION_DEBUG_ITEM("config.protection.materials.debug_item"),
	PROTECTION_MATERIALS_ALLOWED_INTERACTION("config.protection.materials.allowed_interaction"),
	PROTECTION_MATERIALS_PROTECTED("config.protection.materials.protected_materials"),

	;

	private final String configurationPath;

	Config(String configurationPath) {
		this.configurationPath = configurationPath;
	}

	@Override
	public String toString() {
		return configurationPath;
	}
}