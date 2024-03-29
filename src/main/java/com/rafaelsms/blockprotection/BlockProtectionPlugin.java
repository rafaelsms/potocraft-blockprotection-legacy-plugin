package com.rafaelsms.blockprotection;

import com.rafaelsms.blockprotection.blocks.BlocksDatabase;
import com.rafaelsms.blockprotection.blocks.listeners.*;
import com.rafaelsms.blockprotection.friends.FriendsDatabase;
import com.rafaelsms.blockprotection.friends.commands.AddCommand;
import com.rafaelsms.blockprotection.friends.commands.ClearAllCommand;
import com.rafaelsms.blockprotection.friends.commands.DeleteCommand;
import com.rafaelsms.blockprotection.friends.commands.ListCommand;
import com.rafaelsms.blockprotection.util.Database;
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
    private BlockPistonListener blockPistonListener;
    private BlockPlaceListener blockPlaceListener;
    private ProtectionBlockListener protectionBlockListener;
    // Optional listener
    private DoorListener doorListener = null;

    private ListCommand listCommand;
    private AddCommand addCommand;
    private DeleteCommand deleteCommand;
    private ClearAllCommand clearAllCommand;

    @Override
    public void onEnable() {
        try {
            // Copy default configuration
            saveDefaultConfig();

            // Set plugin for Lang and Config
            Lang.setPlugin(this);
            Config.setPlugin(this);

            // Initialize database
            HikariConfig config = new HikariConfig();
            // Get from configuration
            config.setJdbcUrl(Config.DATABASE_URL.getString());
            config.setUsername(Config.DATABASE_USER.getString());
            config.setPassword(Config.DATABASE_PASSWORD.getString());
            config.setConnectionTimeout(Config.DATABASE_CONNECTION_TIMEOUT.getInt());
            config.setMaximumPoolSize(Config.DATABASE_POOL_SIZE.getInt());
            config.setMinimumIdle(Config.DATABASE_POOL_SIZE.getInt() / 4);
            config.setLeakDetectionThreshold(Config.DATABASE_LEAK_DETECTION_TIMEOUT.getInt());

            // Additional configurations
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            dataSource = new HikariDataSource(config);
            Database.setDataSource(dataSource);

            blocksDatabase = new BlocksDatabase(this);
            friendsDatabase = new FriendsDatabase(this);

            // Initialize listeners
            blockBreakListener = new BlockBreakListener(this);
            blockPistonListener = new BlockPistonListener(this);
            blockPlaceListener = new BlockPlaceListener(this);
            protectionBlockListener = new ProtectionBlockListener(this);

            // Register listeners
            getServer().getPluginManager().registerEvents(blockBreakListener, this);
            getServer().getPluginManager().registerEvents(blockPistonListener, this);
            getServer().getPluginManager().registerEvents(blockPlaceListener, this);
            getServer().getPluginManager().registerEvents(protectionBlockListener, this);

            // Conditionally check door listener
            if (Config.PROTECTION_ALLOW_HAND_OPENING_IRON_DOOR.getBoolean()) {
                doorListener = new DoorListener();
                getServer().getPluginManager().registerEvents(doorListener, this);
            }

            // Initialize commands
            listCommand = new ListCommand(this);
            addCommand = new AddCommand(this);
            deleteCommand = new DeleteCommand(this);
            clearAllCommand = new ClearAllCommand(this);

            // Set executors for commands
            getServer().getPluginCommand("friends").setExecutor(listCommand);
            getServer().getPluginCommand("addfriend").setExecutor(addCommand);
            getServer().getPluginCommand("delfriend").setExecutor(deleteCommand);
            getServer().getPluginCommand("clearfriends").setExecutor(clearAllCommand);

        } catch (Exception exception) {
            getLogger().severe("Couldn't initialize Block protection: %s".formatted(exception.getMessage()));
            exception.printStackTrace();
            // Force shutdown
            getServer().shutdown();
            return;
        }

        getLogger().info("BlockProtection enabled!");
    }

    @Override
    public void onDisable() {
        // Unregister listeners
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        // Stop our command executors
        listCommand = null;
        addCommand = null;
        deleteCommand = null;
        clearAllCommand = null;

        // Delete listeners
        blockBreakListener = null;
        blockPistonListener = null;
        blockPlaceListener = null;
        protectionBlockListener = null;
        doorListener = null;

        // Disable database managers
        friendsDatabase = null;
        blocksDatabase = null;

        Database.setDataSource(null);
        dataSource.close();
        dataSource = null;

        // Erase plugin from Lang and Config
        Lang.setPlugin(null);
        Config.setPlugin(null);

        getLogger().info("BlockProtection disabled!");
    }

    public BlocksDatabase getBlocksDatabase() {
        return blocksDatabase;
    }

    public FriendsDatabase getFriendsDatabase() {
        return friendsDatabase;
    }
}
