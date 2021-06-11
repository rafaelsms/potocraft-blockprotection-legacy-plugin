package com.rafaelsms.blockprotection.blocks;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Config;
import com.rafaelsms.blockprotection.util.Database;
import com.rafaelsms.blockprotection.util.ProtectedBlock;
import com.rafaelsms.blockprotection.util.ProtectionResult;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class BlocksDatabase extends Database {

	private final int protectionRadius;
	private final int protectionChunkRadius;
	private final int daysProtected;

	public BlocksDatabase(BlockProtectionPlugin plugin, HikariDataSource dataSource) throws SQLException {
		super(plugin, dataSource);

		// Get configuration
		protectionRadius = plugin.getConfig().getInt(Config.PROTECTION_PROTECTION_RADIUS.toString());
		protectionChunkRadius = ((int) Math.ceil(protectionRadius / 16.0)) + 1;
		daysProtected = plugin.getConfig().getInt(Config.PROTECTION_DAYS_PROTECTED.toString());

		// Create blocks table
		try (Connection connection = getConnection()) {
			final String SQL_CREATE_TABLE = """
					CREATE TABLE IF NOT EXISTS `blockprotection`.`blocks` (
					  `world` BINARY(16) NOT NULL,
					  `chunkX` INT NOT NULL,
					  `chunkZ` INT NOT NULL,
					  `x` INT NOT NULL,
					  `y` INT NOT NULL,
					  `z` INT NOT NULL,
					  `lastModification` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
					  `owner` BINARY(16) NOT NULL,
					  PRIMARY KEY (`world`, `x`, `y`, `z`),
					  INDEX `chunkIndex` (`chunkX` ASC, `chunkZ` ASC, `world` ASC));
					""";
			connection.prepareStatement(SQL_CREATE_TABLE).execute();
		}
	}

	public ProtectedBlock getBlockData(@NotNull Location location) {
		try (Connection connection = getConnection()) {
			final String SQL_SELECT_OWNER = """
					SELECT
					    BIN_TO_UUID(`blocks`.`owner`),
					    `blocks`.`lastModification`
					FROM `blockprotection`.`blocks`
					WHERE
					    `blocks`.`world` = UUID_TO_BIN(?) AND
					    `blocks`.`chunkX` = ? AND
					    `blocks`.`chunkZ` = ? AND
					    `blocks`.`x` = ? AND
					    `blocks`.`y` = ? AND
					    `blocks`.`z` = ?;
					""";
			PreparedStatement statement = connection.prepareStatement(SQL_SELECT_OWNER);
			// world
			statement.setString(1, location.getWorld().getUID().toString());
			// chunk
			statement.setInt(2, location.getChunk().getX());
			statement.setInt(3, location.getChunk().getZ());
			// coordinates
			statement.setInt(4, location.getBlockX());
			statement.setInt(5, location.getBlockY());
			statement.setInt(6, location.getBlockZ());

			ResultSet result = statement.executeQuery();

			if (result.next()) {
				OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(result.getString(1)));
				LocalDateTime dateTime = result.getTimestamp(2).toLocalDateTime();
				return new ProtectedBlock(offlinePlayer, dateTime);
			} else {
				return new ProtectedBlock(null, null);
			}

		} catch (SQLException exception) {
			plugin.getLogger().warning("Single block query failed: %d".formatted(exception.getErrorCode()));
			exception.printStackTrace();
			return null;
		}
	}

	public ProtectionResult isThereBlockingBlocksNearby(@NotNull Location location) {
		try (Connection connection = getConnection()) {
			final String SQL_QUERY_BLOCKS_NO_USER = """
					SELECT
					    `blocks`.`owner`
					FROM `blockprotection`.`blocks`
					WHERE
					    `blocks`.`world` = UUID_TO_BIN(?) AND
					    `blocks`.`chunkX` BETWEEN ? AND ? AND
					    `blocks`.`chunkZ` BETWEEN ? AND ? AND
					    `blocks`.`x` BETWEEN ? AND ? AND
					    `blocks`.`y` BETWEEN ? AND ? AND
					    `blocks`.`z` BETWEEN ? AND ? AND
					    `blocks`.`lastModification` >= (NOW() - INTERVAL ? DAY)
					LIMIT 1;
					""";
			PreparedStatement statement = connection.prepareStatement(SQL_QUERY_BLOCKS_NO_USER);

			// Prepare arguments:
			// - World coordinates
			// world's UUID
			statement.setString(1, location.getWorld().getUID().toString());
			// chunkX
			statement.setInt(2, location.getChunk().getX() - protectionChunkRadius);
			statement.setInt(3, location.getChunk().getX() + protectionChunkRadius);
			// chunkZ
			statement.setInt(4, location.getChunk().getZ() - protectionChunkRadius);
			statement.setInt(5, location.getChunk().getZ() + protectionChunkRadius);
			// x
			statement.setInt(6, location.getBlockX() - protectionRadius);
			statement.setInt(7, location.getBlockX() + protectionRadius);
			// y
			statement.setInt(8, location.getBlockY() - protectionRadius);
			statement.setInt(9, location.getBlockY() + protectionRadius);
			// z
			statement.setInt(10, location.getBlockZ() - protectionRadius);
			statement.setInt(11, location.getBlockZ() + protectionRadius);
			// time interval
			statement.setInt(12, daysProtected);

			ResultSet result = statement.executeQuery();
			return result.next() ? ProtectionResult.PROTECTED : ProtectionResult.NOT_PROTECTED;
		} catch (SQLException exception) {
			plugin.getLogger().warning("Failed to select nearby blocks: %d".formatted(exception.getErrorCode()));
			exception.printStackTrace();
			return ProtectionResult.DATABASE_FAILURE;
		}
	}

	public ProtectionResult isThereBlockingBlocksNearby(@NotNull Location location, @Nullable UUID player) {
		// Check if player is null
		if (player == null) {
			return isThereBlockingBlocksNearby(location);
		}

		try (Connection connection = getConnection()) {
			final String SQL_QUERY_BLOCKS_USER = """
					SELECT
					    `blocks`.`owner`
					FROM `blockprotection`.`blocks`
					WHERE
					    `blocks`.`world` = UUID_TO_BIN(?) AND
					    `blocks`.`chunkX` BETWEEN ? AND ? AND
					    `blocks`.`chunkZ` BETWEEN ? AND ? AND
					    `blocks`.`x` BETWEEN ? AND ? AND
					    `blocks`.`y` BETWEEN ? AND ? AND
					    `blocks`.`z` BETWEEN ? AND ? AND
					    `blocks`.`lastModification` >= (NOW() - INTERVAL ? DAY) AND
					    `blocks`.`owner` != UUID_TO_BIN(?) AND
					    UUID_TO_BIN(?) NOT IN (
					        SELECT
					            `friends`.`friend`
					        FROM `blockprotection`.`friends`
					        WHERE
					            `friends`.`player` = `blocks`.`owner`
					    )
					LIMIT 1;
					""";
			PreparedStatement statement = connection.prepareStatement(SQL_QUERY_BLOCKS_USER);

			// Prepare arguments:
			// - World coordinates
			// world's UUID
			statement.setString(1, location.getWorld().getUID().toString());
			// chunkX
			statement.setInt(2, location.getChunk().getX() - protectionChunkRadius);
			statement.setInt(3, location.getChunk().getX() + protectionChunkRadius);
			// chunkZ
			statement.setInt(4, location.getChunk().getZ() - protectionChunkRadius);
			statement.setInt(5, location.getChunk().getZ() + protectionChunkRadius);
			// x
			statement.setInt(6, location.getBlockX() - protectionRadius);
			statement.setInt(7, location.getBlockX() + protectionRadius);
			// y
			statement.setInt(8, location.getBlockY() - protectionRadius);
			statement.setInt(9, location.getBlockY() + protectionRadius);
			// z
			statement.setInt(10, location.getBlockZ() - protectionRadius);
			statement.setInt(11, location.getBlockZ() + protectionRadius);
			// time interval
			statement.setInt(12, daysProtected);
			// player
			statement.setString(13, player.toString());
			statement.setString(14, player.toString());

			ResultSet result = statement.executeQuery();
			return result.next() ? ProtectionResult.PROTECTED : ProtectionResult.NOT_PROTECTED;
		} catch (SQLException exception) {
			plugin.getLogger().warning("Failed to select nearby blocks with player: %d".formatted(
					exception.getErrorCode()));
			exception.printStackTrace();
			return ProtectionResult.DATABASE_FAILURE;
		}
	}

	public boolean insertBlock(Location location, UUID owner) {
		try (Connection connection = getConnection()) {
			final String SQL_INSERT_BLOCK = """
					INSERT INTO `blockprotection`.`blocks`
					(`world`,
					`chunkX`, `chunkZ`,
					`x`, `y`, `z`,
					`owner`)
					VALUES
					(UUID_TO_BIN(?),
					?, ?,
					?, ?, ?,
					UUID_TO_BIN(?)) ON DUPLICATE KEY UPDATE
					`owner` = UUID_TO_BIN(?);
					""";
			PreparedStatement statement = connection.prepareStatement(SQL_INSERT_BLOCK);
			// World id
			statement.setString(1, location.getWorld().getUID().toString());
			// Chunk coordinates
			statement.setInt(2, location.getChunk().getX());
			statement.setInt(3, location.getChunk().getZ());
			// Block coordinates
			statement.setInt(4, location.getBlockX());
			statement.setInt(5, location.getBlockY());
			statement.setInt(6, location.getBlockZ());
			// Owner
			statement.setString(7, owner.toString());
			statement.setString(8, owner.toString());
			statement.execute();
			return true;
		} catch (SQLException exception) {
			plugin.getLogger().severe("Failed to insert block on database: %d".formatted(exception.getErrorCode()));
			exception.printStackTrace();
			return false;
		}
	}

	public boolean deleteBlock(Location location) {
		try (Connection connection = getConnection()) {
			final String SQL_DELETE_BLOCK = """
					DELETE IGNORE FROM `blockprotection`.`blocks`
					WHERE
					    `world` = UUID_TO_BIN(?) AND
					    `chunkX` = ? AND
					    `chunkZ` = ? AND
					    `x` = ? AND
					    `y` = ? AND
					    `z` = ?;
					""";
			PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BLOCK);
			// World id
			statement.setString(1, location.getWorld().getUID().toString());
			// Chunk coordinates
			statement.setInt(2, location.getChunk().getX());
			statement.setInt(3, location.getChunk().getZ());
			// Block coordinates
			statement.setInt(4, location.getBlockX());
			statement.setInt(5, location.getBlockY());
			statement.setInt(6, location.getBlockZ());
			statement.execute();
			return true;
		} catch (SQLException exception) {
			plugin.getLogger().severe("Failed to delete block: %d".formatted(exception.getErrorCode()));
			exception.printStackTrace();
			return false;
		}
	}

}