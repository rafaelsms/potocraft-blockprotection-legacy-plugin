package com.rafaelsms.blockprotection.blocks;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Config;
import com.rafaelsms.blockprotection.Lang;
import com.rafaelsms.blockprotection.util.*;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BlocksDatabase extends Database {

    private final ProtectionRadius placeRadius;
    private final ProtectionRadius breakRadius;
    private final ProtectionRadius interactRadius;

    private final ProtectionRadius placeUpdateTimeRadius;
    private final ProtectionRadius placeSearchRadiusForTemporary;
    private final int placeNeededNearbyCountToProtect;

    private final ProtectionRadius deletePortalRadius;

    private final int daysProtected;

    public BlocksDatabase(BlockProtectionPlugin plugin) throws SQLException {
        super(plugin);

        // Get configuration
        breakRadius = new ProtectionRadius(Config.PROTECTION_PROTECTION_BREAK_RADIUS.getInt());
        placeRadius = new ProtectionRadius(Config.PROTECTION_PROTECTION_PLACE_RADIUS.getInt());
        interactRadius = new ProtectionRadius(Config.PROTECTION_PROTECTION_INTERACT_RADIUS.getInt());

        placeUpdateTimeRadius = new ProtectionRadius(Config.PROTECTION_PROTECTION_PLACE_UPDATE_TIME_RADIUS.getInt());
        placeSearchRadiusForTemporary = new ProtectionRadius(
                Config.PROTECTION_PROTECTION_PLACE_SEARCH_TEMPORARY_RADIUS.getInt());
        placeNeededNearbyCountToProtect = Config.PROTECTION_PROTECTION_PLACE_BLOCK_COUNT_TO_PROTECT.getInt();

        deletePortalRadius = new ProtectionRadius(breakRadius.getBlockRadius() + 1);

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

    private void setLocation(PreparedStatement statement, Location location, int offset) throws SQLException {
        // Check if location's world is null
        if (!location.isWorldLoaded()) {
            throw new SQLException("Location's world is null");
        }
        assert location.getWorld() != null;

        // World id
        statement.setString(offset + 1, location.getWorld().getUID().toString());
        // Chunk coordinates
        statement.setInt(offset + 2, location.getChunk().getX());
        statement.setInt(offset + 3, location.getChunk().getZ());
        // Block coordinates
        statement.setInt(offset + 4, location.getBlockX());
        statement.setInt(offset + 5, location.getBlockY());
        statement.setInt(offset + 6, location.getBlockZ());
    }

    private void setLocation(PreparedStatement statement, Location location, ProtectionRadius radius, int offset)
            throws SQLException {
        // Check if location's world is null
        if (!location.isWorldLoaded()) {
            throw new SQLException("Location's world is null");
        }
        assert location.getWorld() != null;

        // world's UUID
        statement.setString(offset + 1, location.getWorld().getUID().toString());
        // chunkX
        statement.setInt(offset + 2, location.getChunk().getX() - radius.getChunkRadius());
        statement.setInt(offset + 3, location.getChunk().getX() + radius.getChunkRadius());
        // chunkZ
        statement.setInt(offset + 4, location.getChunk().getZ() - radius.getChunkRadius());
        statement.setInt(offset + 5, location.getChunk().getZ() + radius.getChunkRadius());
        // x
        statement.setInt(offset + 6, location.getBlockX() - radius.getBlockRadius());
        statement.setInt(offset + 7, location.getBlockX() + radius.getBlockRadius());
        // y
        statement.setInt(offset + 8, location.getBlockY() - radius.getBlockRadius());
        statement.setInt(offset + 9, location.getBlockY() + radius.getBlockRadius());
        // z
        statement.setInt(offset + 10, location.getBlockZ() - radius.getBlockRadius());
        statement.setInt(offset + 11, location.getBlockZ() + radius.getBlockRadius());
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

    public ProtectionRadius getPortalDeleteRadius() {
        return deletePortalRadius;
    }

    public ProtectedBlockDate getBlockData(@NotNull Location location) {
        try (Connection connection = getConnection()) {
            final String SQL_SELECT_OWNER = """
                    SELECT
                        BIN_TO_UUID(`blocks`.`owner`),
                        `blocks`.`lastModification`,
                        (`blocks`.`lastModification` >= (NOW() - INTERVAL ? DAY)) AS `validBlock`,
                        `blocks`.`temporaryBlock`
                    FROM `blocks`
                    WHERE
                        `blocks`.`world` = UUID_TO_BIN(?) AND
                        `blocks`.`chunkX` = ? AND
                        `blocks`.`chunkZ` = ? AND
                        `blocks`.`x` = ? AND
                        `blocks`.`y` = ? AND
                        `blocks`.`z` = ?;
                    """;
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_OWNER);
            statement.setInt(1, daysProtected);
            setLocation(statement, location, 1);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(result.getString(1)));
                LocalDateTime dateTime = result.getTimestamp(2).toLocalDateTime();
                boolean validBlock = result.getBoolean(3);
                boolean temporaryBlock = result.getBoolean(4);
                return new ProtectedBlockDate(offlinePlayer, dateTime, validBlock, temporaryBlock);
            } else {
                return new ProtectedBlockDate();
            }

        } catch (SQLException exception) {
            plugin.getLogger().warning("Single block query failed: %s".formatted(exception.getMessage()));
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
                    FROM `blocks`
                    WHERE
                        `blocks`.`owner` IN (
                            SELECT DISTINCT
                                `blocks`.`owner`
                            FROM `blocks`
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
            plugin.getLogger().warning("Failed to select nearby blocks: %s".formatted(exception.getMessage()));
            exception.printStackTrace();
            return null;
        }
    }

    public ProtectionQuery isThereBlockingBlocksNearby(@NotNull Location location, ProtectionRadius radius) {
        try (Connection connection = getConnection()) {
            final String SQL_QUERY_BLOCKS_NO_USER = """
                    SELECT
                        BIN_TO_UUID(`blocks`.`owner`)
                    FROM `blocks`
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
            setLocation(statement, location, radius, 0);
            statement.setInt(12, daysProtected);

            ResultSet result = statement.executeQuery();
            // Return owner or not protected
            if (result.next()) {
                return new ProtectionQuery(UUID.fromString(result.getString(1)));
            } else {
                return new ProtectionQuery(ProtectionQuery.Result.NOT_PROTECTED);
            }
        } catch (SQLException exception) {
            plugin.getLogger().warning("Failed to select nearby blocks: %s".formatted(exception.getMessage()));
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
                    FROM `blocks`
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
                            FROM `friends`
                            WHERE
                                `friends`.`player` = `blocks`.`owner`
                        )
                    LIMIT 1;
                    """;
            PreparedStatement statement = connection.prepareStatement(SQL_QUERY_BLOCKS_USER);
            setLocation(statement, location, radius, 0);
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
                    "Failed to select nearby blocks with player: %s".formatted(exception.getMessage()));
            exception.printStackTrace();
            return new ProtectionQuery(ProtectionQuery.Result.DATABASE_FAILURE);
        }
    }

    public ProtectionQuery isThereBlockingBlocksNearby(World world, Collection<Location> blockLocations,
                                                       ProtectionRadius radius) {
        Integer maxX = null;
        Integer minX = null;
        Integer maxY = null;
        Integer minY = null;
        Integer maxZ = null;
        Integer minZ = null;

        for (Location location : blockLocations) {
            // Check X
            int blockX = location.getBlockX();
            if (maxX == null || blockX > maxX) {
                maxX = blockX;
            }
            if (minX == null || blockX < minX) {
                minX = blockX;
            }
            // Check Y
            int blockY = location.getBlockY();
            if (maxY == null || blockY > maxY) {
                maxY = blockY;
            }
            if (minY == null || blockY < minY) {
                minY = blockY;
            }
            // Check Z
            int blockZ = location.getBlockZ();
            if (maxZ == null || blockZ > maxZ) {
                maxZ = blockZ;
            }
            if (minZ == null || blockZ < minZ) {
                minZ = blockZ;
            }
        }

        // Check if we have at least one block to look up
        if (minX == null) {
            // Fail (wrong arguments)
            return new ProtectionQuery(ProtectionQuery.Result.DATABASE_FAILURE);
        }
        Location lowerCorner = new Location(world, minX, minY, minZ);
        Location higherCorner = new Location(world, maxX, maxY, maxZ);
        return isThereBlockingBlocksNearby(higherCorner, lowerCorner, radius);
    }

    public ProtectionQuery isThereBlockingBlocksNearby(@NotNull Location lowerCorner,
                                                       @NotNull Location higherCorner,
                                                       ProtectionRadius radius) {
        // Check if world is the same
        //noinspection ConstantConditions
        if (!lowerCorner.isWorldLoaded() || !higherCorner.isWorldLoaded() ||
                    !Objects.equals(lowerCorner.getWorld().getUID(), higherCorner.getWorld().getUID())) {
            plugin.getLogger().warning("Bad world arguments for protection check.");
            return new ProtectionQuery(ProtectionQuery.Result.DATABASE_FAILURE);
        }

        try (Connection connection = getConnection()) {
            final String SQL_QUERY_BLOCKS_USER = """
                    SELECT
                        BIN_TO_UUID(`blocks`.`owner`)
                    FROM `blocks`
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
            PreparedStatement statement = connection.prepareStatement(SQL_QUERY_BLOCKS_USER);
            // Set world
            statement.setString(1, higherCorner.getWorld().getUID().toString());
            // Set chunks from lower corner - radius to higher corner + radius
            statement.setInt(2, lowerCorner.getChunk().getX() - radius.getChunkRadius());
            statement.setInt(3, higherCorner.getChunk().getX() + radius.getChunkRadius());
            statement.setInt(4, lowerCorner.getChunk().getZ() - radius.getChunkRadius());
            statement.setInt(5, higherCorner.getChunk().getZ() + radius.getChunkRadius());
            // Set coordinates the same way
            statement.setInt(6, lowerCorner.getBlockX() - radius.getBlockRadius());
            statement.setInt(7, higherCorner.getBlockX() + radius.getBlockRadius());
            statement.setInt(8, lowerCorner.getBlockY() - radius.getBlockRadius());
            statement.setInt(9, higherCorner.getBlockY() + radius.getBlockRadius());
            statement.setInt(10, lowerCorner.getBlockZ() - radius.getBlockRadius());
            statement.setInt(11, higherCorner.getBlockZ() + radius.getBlockRadius());
            // time interval
            statement.setInt(12, daysProtected);

            ResultSet result = statement.executeQuery();
            // Return owner or not protected
            if (result.next()) {
                return new ProtectionQuery(UUID.fromString(result.getString(1)));
            } else {
                return new ProtectionQuery(ProtectionQuery.Result.NOT_PROTECTED);
            }
        } catch (SQLException exception) {
            plugin.getLogger().warning(
                    "Failed to select nearby blocks from corners: %s".formatted(exception.getMessage()));
            exception.printStackTrace();
            return new ProtectionQuery(ProtectionQuery.Result.DATABASE_FAILURE);
        }
    }

    public void insertBlockAsync(Location location, UUID owner, CompletableFuture<Void> future) {
        insertBlockAsync(
                location, owner,
                placeUpdateTimeRadius, placeSearchRadiusForTemporary, placeNeededNearbyCountToProtect,
                future
        );
    }

    public void insertBlockAsync(Location location, UUID owner, ProtectionRadius updateRadius,
                                 ProtectionRadius searchRadius, int blockCountToProtect,
                                 CompletableFuture<Void> future) {
        plugin.getServer().getScheduler().runTaskAsynchronously(
                plugin,
                () -> insertBlock(location, owner, updateRadius, searchRadius, blockCountToProtect, future)
        );
    }

    public void insertBlock(Location location, UUID owner, ProtectionRadius updateRadius,
                            ProtectionRadius searchRadius, int blockCountToProtect,
                            CompletableFuture<Void> future) {
        try (Connection connection = getConnection()) {

            final String SQL_INSERT_BLOCK = """
                    INSERT INTO `blocks` (
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
            insertStatement.closeOnCompletion();
            setLocation(insertStatement, location, 0);
            // Owner
            insertStatement.setString(7, owner.toString());
            insertStatement.setString(8, owner.toString());
            insertStatement.execute();

            // Search and update temporary blocks nearby
            if (searchRadius != null && searchRadius.getBlockRadius() > 0) {
                updateNearbyTemporaryBlocks(connection,
                        location, owner, searchRadius, blockCountToProtect);
            }

            // Search and update all blocks for time
            if (updateRadius != null && updateRadius.getBlockRadius() > 0) {
                updateNearbyBlocksTimeAndOwner(connection, location, owner, updateRadius);
            }

            future.complete(null);
        } catch (SQLException exception) {
            plugin.getLogger().severe("Failed to insert block on database: %s".formatted(exception.getMessage()));
            exception.printStackTrace();
            future.completeExceptionally(new Exception(Lang.PROTECTION_DATABASE_FAILURE.toString()));
        }
    }

    private void updateNearbyTemporaryBlocks(Connection connection,
                                             Location location, UUID owner,
                                             ProtectionRadius searchRadius, int blockCountToProtect)
            throws SQLException {
        final String SQL_COUNT_NEARBY_BLOCKS = """
                SELECT
                    COUNT(*)
                FROM `blocks`
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
                            FROM `friends`
                            WHERE
                                `friends`.`friend` = `blocks`.`owner`
                        )
                    );
                """;
        PreparedStatement countStatement = connection.prepareStatement(SQL_COUNT_NEARBY_BLOCKS);
        countStatement.closeOnCompletion();
        setLocation(countStatement, location, searchRadius, 0);
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
                    `blocks`
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
                            FROM `friends`
                            WHERE
                                `friends`.`friend` = `blocks`.`owner`
                        )
                    );
                """;
        PreparedStatement updateStatement = connection.prepareStatement(SQL_UPDATE_NEARBY_BLOCKS);
        updateStatement.closeOnCompletion();
        setLocation(updateStatement, location, searchRadius, 0);
        // Owner
        updateStatement.setString(12, owner.toString());
        updateStatement.setString(13, owner.toString());
        updateStatement.executeUpdate();
    }

    private void updateNearbyBlocksTimeAndOwner(Connection connection,
                                                Location location, UUID owner, ProtectionRadius radius)
            throws SQLException {
        final String SQL_UPDATE_TIME = """
                UPDATE `blocks`
                SET
                    `blocks`.`lastModification` = NOW(),
                    `blocks`.`owner` = UUID_TO_BIN(?)
                WHERE
                    `blocks`.`world` = UUID_TO_BIN(?) AND
                    `blocks`.`chunkX` BETWEEN ? AND ? AND
                    `blocks`.`chunkZ` BETWEEN ? AND ? AND
                    `blocks`.`x` BETWEEN ? AND ? AND
                    `blocks`.`y` BETWEEN ? AND ? AND
                    `blocks`.`z` BETWEEN ? AND ? AND (
                        (
                            `blocks`.`owner` = UUID_TO_BIN(?) OR
                            UUID_TO_BIN(?) IN (
                                SELECT
                                    `friends`.`friend`
                                FROM `friends`
                                WHERE
                                    `friends`.`player` = `blocks`.`owner`
                            )
                        ) OR (
                            `blocks`.`lastModification` < (NOW() - INTERVAL ? DAY) AND
                            `blocks`.`temporaryBlock` = FALSE
                        )
                    );
                """;
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_TIME);
        statement.closeOnCompletion();
        // Set owner
        statement.setString(1, owner.toString());
        // Set location
        setLocation(statement, location, radius, 1);
        // Set owner
        statement.setString(13, owner.toString());
        statement.setString(14, owner.toString());
        // Set time
        statement.setInt(15, daysProtected);
        // Execute
        statement.execute();
    }

    public void deleteBlockAsync(Location location, CompletableFuture<Void> future) {
        plugin.getServer().getScheduler().runTaskAsynchronously(
                plugin,
                () -> deleteBlock(location, future)
        );
    }

    public void deleteBlock(Location location, CompletableFuture<Void> future) {
        try (Connection connection = getConnection()) {
            final String SQL_DELETE_BLOCK = """
                    DELETE IGNORE FROM `blocks`
                    WHERE
                        `world` = UUID_TO_BIN(?) AND
                        `chunkX` = ? AND
                        `chunkZ` = ? AND
                        `x` = ? AND
                        `y` = ? AND
                        `z` = ?;
                    """;
            PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BLOCK);
            setLocation(statement, location, 0);
            statement.execute();
            future.complete(null);
        } catch (SQLException exception) {
            plugin.getLogger().severe("Failed to delete block: %s".formatted(exception.getMessage()));
            exception.printStackTrace();
            future.completeExceptionally(new Exception(Lang.PROTECTION_DATABASE_FAILURE.toString()));
        }
    }

    public void deleteBlocksAsync(List<Location> locations, CompletableFuture<Void> future) {
        plugin.getServer().getScheduler().runTaskAsynchronously(
                plugin,
                () -> deleteBlocks(locations, future)
        );
    }

    public void deleteBlocks(List<Location> locations, CompletableFuture<Void> future) {
        try (Connection connection = getConnection()) {
            final String SQL_DELETE_BLOCK = """
                    DELETE IGNORE FROM `blocks`
                    WHERE
                        `world` = UUID_TO_BIN(?) AND
                        `chunkX` = ? AND
                        `chunkZ` = ? AND
                        `x` = ? AND
                        `y` = ? AND
                        `z` = ?;
                    """;
            PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BLOCK);
            for (Location location : locations) {
                setLocation(statement, location, 0);
                statement.addBatch();
            }
            statement.executeBatch();
            future.complete(null);
        } catch (SQLException exception) {
            plugin.getLogger().severe("Failed to delete block: %s".formatted(exception.getMessage()));
            exception.printStackTrace();
            future.completeExceptionally(new Exception(Lang.PROTECTION_DATABASE_FAILURE.toString()));
        }
    }

    public void deleteNearbyBlocksAsync(Location location, ProtectionRadius radius, CompletableFuture<Void> future) {
        plugin.getServer().getScheduler().runTaskAsynchronously(
                plugin,
                () -> deleteNearbyBlocks(location, radius, future)
        );
    }

    public void deleteNearbyBlocks(Location location, ProtectionRadius radius, CompletableFuture<Void> future) {
        try (Connection connection = getConnection()) {
            final String SQL_DELETE_RADIUS = """
                    DELETE IGNORE FROM `blocks`
                    WHERE
                        `world` = UUID_TO_BIN(?) AND
                        `chunkX` BETWEEN ? AND ? AND
                        `chunkZ` BETWEEN ? AND ? AND
                        `x` BETWEEN ? AND ? AND
                        `y` BETWEEN ? AND ? AND
                        `z` BETWEEN ? AND ?;
                    """;
            PreparedStatement statement = connection.prepareStatement(SQL_DELETE_RADIUS);
            setLocation(statement, location, radius, 0);
            statement.execute();
            future.complete(null);
        } catch (SQLException exception) {
            plugin.getLogger().severe("Failed to delete block: %s".formatted(exception.getMessage()));
            exception.printStackTrace();
            future.completeExceptionally(new Exception(Lang.PROTECTION_DATABASE_FAILURE.toString()));
        }
    }

}
