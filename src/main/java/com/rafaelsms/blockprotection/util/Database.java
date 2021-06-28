package com.rafaelsms.blockprotection.util;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Database {

    private static HikariDataSource dataSource;

    protected final BlockProtectionPlugin plugin;

    public Database(BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void setDataSource(HikariDataSource dataSource) {
        Database.dataSource = dataSource;
    }
}
