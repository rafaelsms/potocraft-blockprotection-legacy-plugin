package com.rafaelsms.blockprotection.util;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.zaxxer.hikari.HikariDataSource;

public abstract class Database {

    private static HikariDataSource dataSource;

    protected final BlockProtectionPlugin plugin;

    public Database(BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    protected HikariDataSource getDataSource() {
        return dataSource;
    }

    public static void setDataSource(HikariDataSource dataSource) {
        Database.dataSource = dataSource;
    }
}
