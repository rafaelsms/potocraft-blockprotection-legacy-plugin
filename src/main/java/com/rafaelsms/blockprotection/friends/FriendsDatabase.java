package com.rafaelsms.blockprotection.friends;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.util.Database;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class FriendsDatabase extends Database {

	public FriendsDatabase(BlockProtectionPlugin plugin, HikariDataSource dataSource) throws SQLException {
		super(plugin, dataSource);
		// Setup our database
		setupSchema();
	}

	private synchronized void setupSchema() throws SQLException {
		try (Connection connection = getConnection()) {
			final String SQL_CREATE_TABLE = """
			CREATE TABLE IF NOT EXISTS `blockprotection`.`friends` (
			  `player` BINARY(16) NOT NULL,
			  `friend` BINARY(16) NOT NULL,
			  PRIMARY KEY (`player`, `friend`),
			  INDEX `friendsIndex` (`friend` ASC));
			""";
			PreparedStatement statement = connection.prepareStatement(SQL_CREATE_TABLE);
			statement.execute();
		}
	}

	/**
	 * Retrieves the friends of a player.
	 *
	 * @param player player to search its friends
	 * @return a unmodifiable set of friends or null if query failed
	 */
	public synchronized Set<OfflinePlayer> listFriends(UUID player) {
		try (Connection connection = getConnection()) {
			final String SQL_SELECT_FRIENDS = """
			SELECT
			    BIN_TO_UUID(`friends`.`friend`)
			FROM `blockprotection`.`friends`
			WHERE
			    `friends`.`player` = UUID_TO_BIN(?);
			""";
			PreparedStatement statement = connection.prepareStatement(SQL_SELECT_FRIENDS);

			statement.setString(1, player.toString());
			ResultSet result = statement.executeQuery();

			// For every result, populate the set
			HashSet<OfflinePlayer> friends = new HashSet<>();
			while (result.next()) {
				OfflinePlayer friend = plugin.getServer().getOfflinePlayer(UUID.fromString(result.getString(1)));
				friends.add(friend);
			}

			// Return unmodifiable set
			return Collections.unmodifiableSet(friends);
		} catch (SQLException exception) {
			// Warn console and print error
			plugin.getLogger().warning("Failed to search friends: %d".formatted(exception.getErrorCode()));
			exception.printStackTrace();
			return null;
		}
	}

	public synchronized boolean addFriend(UUID player, UUID friend) {
		try (Connection connection = getConnection()) {
			final String SQL_INSERT_FRIEND = """
			INSERT INTO `blockprotection`.`friends`
			   (`player`, `friend`)
			VALUES
			   (UUID_TO_BIN(?), UUID_TO_BIN(?));
			""";
			PreparedStatement statement = connection.prepareStatement(SQL_INSERT_FRIEND);

			statement.setString(1, player.toString());
			statement.setString(2, friend.toString());

			statement.execute();
			return true;
		} catch (SQLException exception) {
			plugin.getLogger().warning("Failed to add friend: %d".formatted(exception.getErrorCode()));
			exception.printStackTrace();
			return false;
		}
	}

	public synchronized boolean removeFriend(UUID player, UUID friend) {
		try (Connection connection = getConnection()) {
			final String SQL_DELETE_FRIEND = """
			DELETE FROM `blockprotection`.`friends`
			WHERE
			    `friends`.`player` = UUID_TO_BIN(?) AND
			    `friends`.`friend` = UUID_TO_BIN(?);
			""";
			PreparedStatement statement = connection.prepareStatement(SQL_DELETE_FRIEND);

			statement.setString(1, player.toString());
			statement.setString(2, friend.toString());

			statement.execute();
			return true;
		} catch (SQLException exception) {
			plugin.getLogger().warning("Failed to add friend: %d".formatted(exception.getErrorCode()));
			exception.printStackTrace();
			return false;
		}
	}
}