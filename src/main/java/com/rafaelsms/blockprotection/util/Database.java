package com.rafaelsms.blockprotection.util;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Database {

	private final HikariDataSource dataSource;
	protected final BlockProtectionPlugin plugin;

	public Database(BlockProtectionPlugin plugin, HikariDataSource dataSource) {
		this.plugin = plugin;
		this.dataSource = dataSource;
	}

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
}