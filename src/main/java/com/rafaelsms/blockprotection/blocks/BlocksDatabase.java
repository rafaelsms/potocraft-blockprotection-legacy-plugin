package com.rafaelsms.blockprotection.blocks;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Config;
import com.rafaelsms.blockprotection.util.*;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("FieldCanBeLocal")
public class BlocksDatabase extends Database {

    private final ProtectionRadius placeRadius;
    private final ProtectionRadius breakRadius;
    private final ProtectionRadius interactRadius;
    private final ProtectionRadius updateRadius;

    private final ProtectionRadius searchRadius;
    private final int neededCountToProtect;

    private final int daysProtected;

    public BlocksDatabase(BlockProtectionPlugin plugin) throws SQLException {
        super(plugin);

        // Get configuration
        breakRadius = new ProtectionRadius(Config.PROTECTION_PROTECTION_BREAK_RADIUS.getInt());
        placeRadius = new ProtectionRadius(Config.PROTECTION_PROTECTION_PLACE_RADIUS.getInt());
        interactRadius = new ProtectionRadius(Config.PROTECTION_PROTECTION_INTERACT_RADIUS.getInt());
        updateRadius = new ProtectionRadius(Config.PROTECTION_PROTECTION_UPDATE_RADIUS.getInt());

        searchRadius = new ProtectionRadius(Config.PROTECTION_PROTECTION_SEARCH_RADIUS.getInt());
        neededCountToProtect = Config.PROTECTION_PROTECTION_BLOCK_COUNT.getInt();

        daysProtected = Config.PROTECTION_DAYS_PROTECTED.getInt();

        // Check limits
        if (placeRadius.getBlockRadius() < 2 * breakRadius.getBlockRadius()) {
            plugin.getLogger().warning("Can't set place radius less than 2 times break radius or blocks will collide");
        }
        if (interactRadius.getBlockRadius() > breakRadius.getBlockRadius()) {
            plugin.getLogger().warning("Can't set interact radius less than break radius or people can't break");
        }

        // Create blocks table
        try (Connection connection = getConnection()) {
            final String SQL_CREATE_TABLE = """
                    CREATE TABLE IF NOT EXISTS `blocks` (
                      `world` binary(16) NOT NULL,
                      `chunkX` int NOT NULL,
                      `chunkZ` int NOT NULL,
                      `x` int NOT NULL,
                      `y` int NOT NULL,
                      `z` int NOT NULL,
                      `temporaryBlock` tinyint(1) NOT NULL DEFAULT TRUE,
                      `lastModification` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      `owner` binary(16) NOT NULL,
                      PRIMARY KEY (`world`,`x`,`y`,`z`),
                      KEY `chunkIndex` (`world`,`chunkX`,`chunkZ`)
                    ) ;
                    """;
            connection.prepareStatement(SQL_CREATE_TABLE).execute();
        }
    }

    private void setLocation(PreparedStatement statement, Location location) throws SQLException {
        // Check if location's world is null
        if (!location.isWorldLoaded()) {
            throw new SQLException("Location's world is null");
        }
        assert location.getWorld() != null;

        // World id
        statement.setString(1, location.getWorld().getUID().toString());
        // Chunk coordinates
        statement.setInt(2, location.getChunk().getX());
        statement.setInt(3, location.getChunk().getZ());
        // Block coordinates
        statement.setInt(4, location.getBlockX());
        statement.setInt(5, location.getBlockY());
        statement.setInt(6, location.getBlockZ());
    }

    private void setLocation(PreparedStatement statement, Location location, ProtectionRadius radius)
            throws SQLException {
        // Check if location's world is null
        if (!location.isWorldLoaded()) {
            throw new SQLException("Location's world is null");
        }
        assert location.getWorld() != null;

        // world's UUID
        statement.setString(1, location.getWorld().getUID().toString());
        // chunkX
        statement.setInt(2, location.getChunk().getX() - radius.getChunkRadius());
        statement.setInt(3, location.getChunk().getX() + radius.getChunkRadius());
        // chunkZ
        statement.setInt(4, location.getChunk().getZ() - radius.getChunkRadius());
        statement.setInt(5, location.getChunk().getZ() + radius.getChunkRadius());
        // x
        statement.setInt(6, location.getBlockX() - radius.getBlockRadius());
        statement.setInt(7, location.getBlockX() + radius.getBlockRadius());
        // y
        statement.setInt(8, location.getBlockY() - radius.getBlockRadius());
        statement.setInt(9, location.getBlockY() + radius.getBlockRadius());
        // z
        statement.setInt(10, location.getBlockZ() - radius.getBlockRadius());
        statement.setInt(11, location.getBlockZ() + radius.getBlockRadius());
    }

    public ProtectionRadius getBreakRadius() {
        return breakRadius;
    }

    public ProtectionRadius getPlaceRadius() {
        return placeRadius;
    }

    public ProtectionRadius getInteractRadius() {
        return interactRadius;
    }

    public ProtectionRadius getUpdateRadius() {
        return updateRadius;
    }

    public ProtectionRadius getSearchRadius() {
        return searchRadius;
    }

    public int getNeededCountToProtect() {
        return neededCountToProtect;
    }

    public ProtectedBlockDate getBlockData(@NotNull Location location) {
        try (Connection connection = getConnection()) {
            final String SQL_SELECT_OWNER = """
                    SELECT
                        BIN_TO_UUID(`blocks`.`owner`),
                        `blocks`.`lastModification`,
                        `blocks`.`temporaryBlock`
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
            setLocation(statement, location);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(result.getString(1)));
                LocalDateTime dateTime = result.getTimestamp(2).toLocalDateTime();
                boolean temporaryBlock = result.getBoolean(3);
                return new ProtectedBlockDate(offlinePlayer, dateTime, temporaryBlock);
            } else {
                return new ProtectedBlockDate();
            }

        } catch (SQLException exception) {
            plugin.getLogger().warning("Single block query failed: %d".formatted(exception.getErrorCode()));
            exception.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public List<ProtectedBlock> getDistinctOwnersProtectedBlocks(@NotNull Location location, ProtectionRadius radius) {
        try (Connection connection = getConnection()) {
            final String SQL_SELECT_DISTINCT_BLOCKS = """
                    SELECT DISTINCT
                        BIN_TO_UUID(`blocks`.`owner`),
                        MAX(`blocks`.`x`),
                        MAX(`blocks`.`y`),
                        MAX(`blocks`.`z`)
                    FROM `blockprotection`.`blocks`
                    WHERE
                        `blocks`.`owner` IN (
                            SELECT DISTINCT
                                `blocks`.`owner`
                            FROM `blockprotection`.`blocks`
                            WHERE
                                `blocks`.`world` = UUID_TO_BIN(?) AND
                                `blocks`.`chunkX` BETWEEN ? AND ? AND
                                `blocks`.`chunkZ` BETWEEN ? AND ?
                        ) AND
                        `blocks`.`world` = UUID_TO_BIN(?) AND
                        `blocks`.`chunkX` BETWEEN ? AND ? AND
                        `blocks`.`chunkZ` BETWEEN ? AND ? AND
                        `blocks`.`x` BETWEEN ? AND ? AND
                        `blocks`.`y` BETWEEN ? AND ? AND
                        `blocks`.`z` BETWEEN ? AND ? AND
                        `blocks`.`temporaryBlock` = FALSE AND
                        `blocks`.`lastModification` >= (NOW() - INTERVAL ? DAY)
                    GROUP BY `blocks`.`owner`;
                    """;
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_DISTINCT_BLOCKS);
            // Check if location's world is null
            if (!location.isWorldLoaded()) {
                return null;
            }
            assert location.getWorld() != null;
            // Set variables for inner SELECT
            // World uuid
            statement.setString(1, location.getWorld().getUID().toString());
            // Chunk X
            statement.setInt(2, location.getChunk().getX() - radius.getChunkRadius());
            statement.setInt(3, location.getChunk().getX() + radius.getChunkRadius());
            // Chunk Z
            statement.setInt(4, location.getChunk().getZ() - radius.getChunkRadius());
            statement.setInt(5, location.getChunk().getZ() + radius.getChunkRadius());

            // Set variables for outer SELECT
            // World uuid again
            statement.setString(6, location.getWorld().getUID().toString());
            // Chunk X range
            statement.setInt(7, location.getChunk().getX() - radius.getChunkRadius());
            statement.setInt(8, location.getChunk().getX() + radius.getChunkRadius());
            // Chunk Y range
            statement.setInt(9, location.getChunk().getZ() - radius.getChunkRadius());
            statement.setInt(10, location.getChunk().getZ() + radius.getChunkRadius());
            // X
            statement.setInt(11, location.getBlockX() - radius.getBlockRadius());
            statement.setInt(12, location.getBlockX() + radius.getBlockRadius());
            // Y
            statement.setInt(13, location.getBlockY() - radius.getBlockRadius());
            statement.setInt(14, location.getBlockY() + radius.getBlockRadius());
            // Z
            statement.setInt(15, location.getBlockZ() - radius.getBlockRadius());
            statement.setInt(16, location.getBlockZ() + radius.getBlockRadius());
            // Set last modification
            statement.setInt(17, daysProtected);

            ResultSet result = statement.executeQuery();
            final ArrayList<ProtectedBlock> protectedBlocks = new ArrayList<>();

            // Now we search
            while (result.next()) {
                ProtectedBlock protectedBlock = new ProtectedBlock(
                        UUID.fromString(result.getString(1)),
                        location.getWorld().getUID(),
                        result.getInt(2),
                        result.getInt(3),
                        result.getInt(4)
                );

                // Add it to the list
                protectedBlocks.add(protectedBlock);
            }

            // Close ResultSet
            result.close();

            // Return unmodifiable list
            return Collections.unmodifiableList(protectedBlocks);
        } catch (SQLException exception) {
            plugin.getLogger().warning("Failed to select nearby blocks: %d".formatted(exception.getErrorCode()));
            exception.printStackTrace();
            return null;
        }
    }

    public ProtectionQuery isThereBlockingBlocksNearby(@NotNull Location location, ProtectionRadius radius) {
        try (Connection connection = getConnection()) {
            final String SQL_QUERY_BLOCKS_NO_USER = """
                    SELECT
                        BIN_TO_UUID(`blocks`.`owner`)
                    FROM `blockprotection`.`blocks`
                    WHERE
                        `blocks`.`world` = UUID_TO_BIN(?) AND
                        `blocks`.`chunkX` BETWEEN ? AND ? AND
                        `blocks`.`chunkZ` BETWEEN ? AND ? AND
                        `blocks`.`x` BETWEEN ? AND ? AND
                        `blocks`.`y` BETWEEN ? AND ? AND
                        `blocks`.`z` BETWEEN ? AND ? AND
                        `blocks`.`temporaryBlock` = FALSE AND
                        `blocks`.`lastModification` >= (NOW() - INTERVAL ? DAY)
                    LIMIT 1;
                    """;
            PreparedStatement statement = connection.prepareStatement(SQL_QUERY_BLOCKS_NO_USER);
            setLocation(statement, location, radius);
            statement.setInt(12, daysProtected);

            ResultSet result = statement.executeQuery();
            // Return owner or not protected
            if (result.next()) {
                return new ProtectionQuery(UUID.fromString(result.getString(1)));
            } else {
                return new ProtectionQuery(ProtectionQuery.Result.NOT_PROTECTED);
            }
        } catch (SQLException exception) {
            plugin.getLogger().warning("Failed to select nearby blocks: %d".formatted(exception.getErrorCode()));
            exception.printStackTrace();
            return new ProtectionQuery(ProtectionQuery.Result.DATABASE_FAILURE);
        }
    }

    public ProtectionQuery isThereBlockingBlocksNearby(@NotNull Location location, @Nullable UUID player,
                                                       ProtectionRadius radius) {
        // Check if player is null
        if (player == null) {
            return isThereBlockingBlocksNearby(location, radius);
        }

        try (Connection connection = getConnection()) {
            final String SQL_QUERY_BLOCKS_USER = """
                    SELECT
                        BIN_TO_UUID(`blocks`.`owner`)
                    FROM `blockprotection`.`blocks`
                    WHERE
                        `blocks`.`world` = UUID_TO_BIN(?) AND
                        `blocks`.`chunkX` BETWEEN ? AND ? AND
                        `blocks`.`chunkZ` BETWEEN ? AND ? AND
                        `blocks`.`x` BETWEEN ? AND ? AND
                        `blocks`.`y` BETWEEN ? AND ? AND
                        `blocks`.`z` BETWEEN ? AND ? AND
                        `blocks`.`temporaryBlock` = FALSE AND
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
            setLocation(statement, location, radius);
            // time interval
            statement.setInt(12, daysProtected);
            // player
            statement.setString(13, player.toString());
            statement.setString(14, player.toString());

            ResultSet result = statement.executeQuery();
            // Return owner or not protected
            if (result.next()) {
                return new ProtectionQuery(UUID.fromString(result.getString(1)));
            } else {
                return new ProtectionQuery(ProtectionQuery.Result.NOT_PROTECTED);
            }
        } catch (SQLException exception) {
            plugin.getLogger().warning(
                    "Failed to select nearby blocks with player: %d".formatted(exception.getErrorCode()));
            exception.printStackTrace();
            return new ProtectionQuery(ProtectionQuery.Result.DATABASE_FAILURE);
        }
    }

    public boolean insertBlock(Location location, UUID owner, ProtectionRadius updateRadius,
                               ProtectionRadius searchRadius, int blockCountToProtect) {
        try (Connection connection = getConnection()) {
            final String SQL_INSERT_BLOCK = """
                    INSERT INTO `blockprotection`.`blocks` (
                        `world`,
                        `chunkX`, `chunkZ`,
                        `x`, `y`, `z`,
                        `owner`,
                        `temporaryBlock`
                    ) VALUES (
                        UUID_TO_BIN(?),
                        ?, ?,
                        ?, ?, ?,
                        UUID_TO_BIN(?),
                        TRUE
                    ) ON DUPLICATE KEY UPDATE `owner` = UUID_TO_BIN(?);
                    """;
            PreparedStatement insertStatement = connection.prepareStatement(SQL_INSERT_BLOCK);
            setLocation(insertStatement, location);
            // Owner
            insertStatement.setString(7, owner.toString());
            insertStatement.setString(8, owner.toString());
            insertStatement.execute();

            if (searchRadius != null && searchRadius.getBlockRadius() > 0) {
                // Asynchronously search and protect nearby blocks
                CompletableFuture.runAsync(() -> updateNearbyTemporaryBlocks(location, owner,
                        searchRadius, blockCountToProtect));
            }

            // Check if we should update the time of protection of nearby blocks
            if (updateRadius != null && updateRadius.getBlockRadius() > 0) {
                // Asynchronously update near blocks to new date
                CompletableFuture.runAsync(() -> updateNearbyBlocksTime(location, owner, updateRadius));
            }

            return true;
        } catch (SQLException exception) {
            plugin.getLogger().severe("Failed to insert block on database: %d".formatted(exception.getErrorCode()));
            exception.printStackTrace();
            return false;
        }
    }

    private void updateNearbyTemporaryBlocks(Location location, UUID owner,
                                             ProtectionRadius searchRadius, int blockCountToProtect) {
        try (Connection connection = getConnection()) {
            final String SQL_COUNT_NEARBY_BLOCKS = """
                    SELECT
                        COUNT(*)
                    FROM `blockprotection`.`blocks`
                    WHERE
                        `blocks`.`world` = UUID_TO_BIN(?) AND
                        `blocks`.`chunkX` BETWEEN ? AND ? AND
                        `blocks`.`chunkZ` BETWEEN ? AND ? AND
                        `blocks`.`x` BETWEEN ? AND ? AND
                        `blocks`.`y` BETWEEN ? AND ? AND
                        `blocks`.`z` BETWEEN ? AND ? AND
                        (
                            `blocks`.`owner` = UUID_TO_BIN(?) OR
                            UUID_TO_BIN(?) IN (
                                SELECT
                                    `friends`.`player`
                                FROM `blockprotection`.`friends`
                                WHERE
                                    `friends`.`friend` = `blocks`.`owner`
                            )
                        );
                    """;
            PreparedStatement countStatement = connection.prepareStatement(SQL_COUNT_NEARBY_BLOCKS);
            setLocation(countStatement, location, searchRadius);
            // Owner
            countStatement.setString(12, owner.toString());
            countStatement.setString(13, owner.toString());
            ResultSet countResult = countStatement.executeQuery();
            int nearbyBlocks;
            if (countResult.next()) {
                nearbyBlocks = countResult.getInt(1);
            } else {
                nearbyBlocks = 0;
            }

            // Exit if count result is less than needed
            if (nearbyBlocks < blockCountToProtect) {
                return;
            }

            final String SQL_UPDATE_NEARBY_BLOCKS = """
                    UPDATE
                        `blockprotection`.`blocks`
                    SET
                        `blocks`.`temporaryBlock` = FALSE
                    WHERE
                        `blocks`.`world` = UUID_TO_BIN(?) AND
                        `blocks`.`chunkX` BETWEEN ? AND ? AND
                        `blocks`.`chunkZ` BETWEEN ? AND ? AND
                        `blocks`.`x` BETWEEN ? AND ? AND
                        `blocks`.`y` BETWEEN ? AND ? AND
                        `blocks`.`z` BETWEEN ? AND ? AND
                        (
                            `blocks`.`owner` = UUID_TO_BIN(?) OR
                            UUID_TO_BIN(?) IN (
                                SELECT
                                    `friends`.`player`
                                FROM `blockprotection`.`friends`
                                WHERE
                                    `friends`.`friend` = `blocks`.`owner`
                            )
                        );
                    """;
            PreparedStatement updateStatement = connection.prepareStatement(SQL_UPDATE_NEARBY_BLOCKS);
            setLocation(updateStatement, location, searchRadius);
            // Owner
            updateStatement.setString(12, owner.toString());
            updateStatement.setString(13, owner.toString());
            updateStatement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().warning("Failed to protect nearby blocks: %d".formatted(exception.getErrorCode()));
            exception.printStackTrace();
        }
    }

    private void updateNearbyBlocksTime(Location location, UUID owner, ProtectionRadius radius) {
        try (Connection connection = getConnection()) {
            final String SQL_UPDATE_TIME = """
                    UPDATE `blockprotection`.`blocks`
                    SET
                        `blocks`.`lastModification` = NOW()
                    WHERE
                        `blocks`.`world` = UUID_TO_BIN(?) AND
                        `blocks`.`chunkX` BETWEEN ? AND ? AND
                        `blocks`.`chunkZ` BETWEEN ? AND ? AND
                        `blocks`.`x` BETWEEN ? AND ? AND
                        `blocks`.`y` BETWEEN ? AND ? AND
                        `blocks`.`z` BETWEEN ? AND ? AND
                        `blocks`.`lastModification` >= (NOW() - INTERVAL ? DAY) AND (
                            `blocks`.`owner` = UUID_TO_BIN(?) OR
                            UUID_TO_BIN(?) IN (
                                SELECT
                                    `friends`.`friend`
                                FROM `blockprotection`.`friends`
                                WHERE
                                    `friends`.`player` = `blocks`.`owner`
                            )
                        );
                    """;
            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_TIME);
            setLocation(statement, location, radius);
            // Set time and owner
            statement.setInt(12, daysProtected);
            statement.setString(13, owner.toString());
            statement.setString(14, owner.toString());
            // Execute
            statement.execute();
        } catch (SQLException exception) {
            plugin.getLogger().warning("Failed to update blocks: %d".formatted(exception.getErrorCode()));
            exception.printStackTrace();
        }
    }

    @SuppressWarnings("UnusedReturnValue")
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
            setLocation(statement, location);
            statement.execute();
            return true;
        } catch (SQLException exception) {
            plugin.getLogger().severe("Failed to delete block: %d".formatted(exception.getErrorCode()));
            exception.printStackTrace();
            return false;
        }
    }

}
