package com.rafaelsms.blockprotection;

import com.rafaelsms.blockprotection.blocks.BlocksDatabase;
import com.rafaelsms.blockprotection.blocks.listeners.*;
import com.rafaelsms.blockprotection.friends.FriendsDatabase;
import com.rafaelsms.blockprotection.friends.commands.AddCommand;
import com.rafaelsms.blockprotection.friends.commands.DeleteCommand;
import com.rafaelsms.blockprotection.friends.commands.ListCommand;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("ConstantConditions")
public class BlockProtectionPlugin extends JavaPlugin {

	// Database managers
	private HikariDataSource dataSource;
	private BlocksDatabase blocksDatabase;
	private FriendsDatabase friendsDatabase;

	// Listener
	private BlockBreakListener blockBreakListener;
	private BlockInteractListener blockInteractListener;
	private BlockPistonListener blockPistonListener;
	private BlockPlaceListener blockPlaceListener;
	private DatabaseListener databaseListener;
	private PlayerProtectionListener playerProtectionListener;
	private ProtectionBlockListener protectionBlockListener;
	private DoorListener doorListener;

	private ListCommand listCommand;
	private AddCommand addCommand;
	private DeleteCommand deleteCommand;

	@Override
	public void onEnable() {
		// Copy default configuration
		saveDefaultConfig();

		// Initialize database
		try {

			HikariConfig config = new HikariConfig();
			// Get from configuration
			config.setJdbcUrl(getConfig().getString(Config.DATABASE_URL.toString()));
			config.setUsername(getConfig().getString(Config.DATABASE_USER.toString()));
			config.setPassword(getConfig().getString(Config.DATABASE_PASSWORD.toString()));

			// Additional configurations
			config.addDataSourceProperty("rewriteBatchedStatements", "true");
			config.addDataSourceProperty("cacheServerConfiguration", "true");
			config.addDataSourceProperty("useServerPrepStmts", "true");
			config.addDataSourceProperty("cachePrepStmts", "true");
			config.addDataSourceProperty("prepStmtCacheSize", "250");
			config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
			config.setMaximumPoolSize(getConfig().getInt(Config.DATABASE_POOL_SIZE.toString()));
			dataSource = new HikariDataSource(config);

			blocksDatabase = new BlocksDatabase(this, dataSource);
			friendsDatabase = new FriendsDatabase(this, dataSource);
		} catch (Exception exception) {
			getLogger().severe("Couldn't initialize MySQL database: %s".formatted(exception.getMessage()));
			exception.printStackTrace();
			getServer().shutdown();
			return;
		}

		// Initialize listeners
		blockBreakListener = new BlockBreakListener(this);
		blockInteractListener = new BlockInteractListener(this);
		blockPistonListener = new BlockPistonListener(this);
		blockPlaceListener = new BlockPlaceListener(this);
		databaseListener = new DatabaseListener(this);
		playerProtectionListener = new PlayerProtectionListener(this);
		protectionBlockListener = new ProtectionBlockListener(this);
		doorListener = new DoorListener(this);

		// Register listeners
		getServer().getPluginManager().registerEvents(blockBreakListener, this);
		getServer().getPluginManager().registerEvents(blockInteractListener, this);
		getServer().getPluginManager().registerEvents(blockPistonListener, this);
		getServer().getPluginManager().registerEvents(blockPlaceListener, this);
		getServer().getPluginManager().registerEvents(databaseListener, this);
		getServer().getPluginManager().registerEvents(playerProtectionListener, this);
		getServer().getPluginManager().registerEvents(protectionBlockListener, this);
		getServer().getPluginManager().registerEvents(doorListener, this);

		// Initialize commands
		listCommand = new ListCommand(this);
		addCommand = new AddCommand(this);
		deleteCommand = new DeleteCommand(this);

		// Set executors for commands
		getServer().getPluginCommand("friends").setExecutor(listCommand);
		getServer().getPluginCommand("addfriend").setExecutor(addCommand);
		getServer().getPluginCommand("delfriend").setExecutor(deleteCommand);

		getLogger().info("BlockProtection enabled!");
	}

	@Override
	public void onDisable() {
		// Unregister listeners
		HandlerList.unregisterAll(this);

		// Stop our command executors
		listCommand = null;
		addCommand = null;
		deleteCommand = null;

		// Delete listeners
		blockBreakListener = null;
		blockInteractListener = null;
		blockPistonListener = null;
		blockPlaceListener = null;
		databaseListener = null;
		protectionBlockListener = null;
		playerProtectionListener = null;
		doorListener = null;

		// Disable database managers
		friendsDatabase = null;
		blocksDatabase = null;

		getLogger().info("BlockProtection disabled!");
	}

	public BlocksDatabase getBlocksDatabase() {
		return blocksDatabase;
	}

	public FriendsDatabase getFriendsDatabase() {
		return friendsDatabase;
	}

	public PlayerProtectionListener getPlayerProtectionListener() {
		return playerProtectionListener;
	}
}