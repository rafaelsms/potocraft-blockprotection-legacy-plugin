package com.rafaelsms.blockprotection;

import com.rafaelsms.blockprotection.blocks.BlocksDatabase;
import com.rafaelsms.blockprotection.blocks.listeners.*;
import com.rafaelsms.blockprotection.friends.FriendsDatabase;
import com.rafaelsms.blockprotection.friends.commands.AddCommand;
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
    private DatabaseListener databaseListener;
    private ProtectionBlockListener protectionBlockListener;
    private DoorListener doorListener;

    private ListCommand listCommand;
    private AddCommand addCommand;
    private DeleteCommand deleteCommand;

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

            // Additional configurations
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setMaximumPoolSize(Config.DATABASE_POOL_SIZE.getInt());
            dataSource = new HikariDataSource(config);
            Database.setDataSource(dataSource);

            blocksDatabase = new BlocksDatabase(this);
            friendsDatabase = new FriendsDatabase(this);

            // Initialize listeners
            blockBreakListener = new BlockBreakListener(this);
            blockPistonListener = new BlockPistonListener(this);
            blockPlaceListener = new BlockPlaceListener(this);
            databaseListener = new DatabaseListener(this);
            protectionBlockListener = new ProtectionBlockListener(this);
            doorListener = new DoorListener(this);

            // Register listeners
            getServer().getPluginManager().registerEvents(blockBreakListener, this);
            getServer().getPluginManager().registerEvents(blockPistonListener, this);
            getServer().getPluginManager().registerEvents(blockPlaceListener, this);
            getServer().getPluginManager().registerEvents(databaseListener, this);
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

        // Stop our command executors
        listCommand = null;
        addCommand = null;
        deleteCommand = null;

        // Delete listeners
        blockBreakListener = null;
        blockPistonListener = null;
        blockPlaceListener = null;
        databaseListener = null;
        protectionBlockListener = null;
        doorListener = null;

        // Disable database managers
        friendsDatabase = null;
        blocksDatabase = null;

        Database.setDataSource(null);
        dataSource = null;

        // Erase plugin from Lang and Config
        Lang.setPlugin(null);

        getLogger().info("BlockProtection disabled!");
    }

    public BlocksDatabase getBlocksDatabase() {
        return blocksDatabase;
    }

    public FriendsDatabase getFriendsDatabase() {
        return friendsDatabase;
    }
}
