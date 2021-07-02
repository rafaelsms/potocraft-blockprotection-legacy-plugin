package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.blocks.BlocksDatabase;
import com.rafaelsms.blockprotection.blocks.events.AttemptBreakEvent;
import com.rafaelsms.blockprotection.blocks.events.BreakEvent;
import com.rafaelsms.blockprotection.util.ProtectionQuery;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import java.util.List;
import java.util.stream.Collectors;

public record BlockBreakListener(BlockProtectionPlugin plugin) implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onEntityExplodeBlocks(EntityExplodeEvent event) {
        List<Location> blockListLocations = event.blockList().stream()
                .map(Block::getLocation)
                .collect(Collectors.toList());

        int maxX = event.getLocation().getBlockX();
        int minX = event.getLocation().getBlockX();
        int maxY = event.getLocation().getBlockY();
        int minY = event.getLocation().getBlockY();
        int maxZ = event.getLocation().getBlockZ();
        int minZ = event.getLocation().getBlockZ();

        for (Location location : blockListLocations) {
            // Check X
            int blockX = location.getBlockX();
            if (blockX > maxX) {
                maxX = blockX;
            } else if (blockX < minX) {
                minX = blockX;
            }
            // Check Y
            int blockY = location.getBlockY();
            if (blockY > maxY) {
                maxY = blockY;
            } else if (blockY < minY) {
                minY = blockY;
            }
            // Check Z
            int blockZ = location.getBlockZ();
            if (blockZ > maxZ) {
                maxZ = blockZ;
            } else if (blockZ < minZ) {
                minZ = blockZ;
            }
        }

        Location lowerCorner = new Location(event.getLocation().getWorld(), minX, minY, minZ);
        Location higherCorner = new Location(event.getLocation().getWorld(), maxX, maxY, maxZ);

        // Find blocking blocks nearby (single SQL query)
        BlocksDatabase database = plugin.getBlocksDatabase();
        ProtectionQuery protectionQuery = database.isThereBlockingBlocksNearby(lowerCorner, higherCorner,
                database.getBreakRadius());
        // If it is protected, don't destroy any block
        if (protectionQuery.isProtected()) {
            event.blockList().clear();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onSpreadBlock(BlockSpreadEvent event) {
        // Check just when a block disappears
        if (!event.getBlock().isEmpty() && !event.getNewState().getType().isSolid()) {
            AttemptBreakEvent breakEvent = new AttemptBreakEvent(event.getBlock(), null);
            plugin.getServer().getPluginManager().callEvent(breakEvent);

            // Check if event was cancelled
            if (breakEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onBreakDoor(EntityBreakDoorEvent event) {
        AttemptBreakEvent breakEvent = new AttemptBreakEvent(event.getBlock(), null);
        plugin.getServer().getPluginManager().callEvent(breakEvent);

        // Check if event was cancelled
        if (breakEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onBurn(BlockBurnEvent event) {
        AttemptBreakEvent breakEvent = new AttemptBreakEvent(event.getBlock(), null);
        plugin.getServer().getPluginManager().callEvent(breakEvent);

        // Check if event was cancelled
        if (breakEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onFadeMonitor(BlockFadeEvent event) {
        BreakEvent breakEvent = new BreakEvent(event.getBlock());
        plugin.getServer().getPluginManager().callEvent(breakEvent);
    }

    @SuppressWarnings("DuplicatedCode")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onChangeToNothing(EntityChangeBlockEvent event) {
        // Check if block changed at all
        if (event.getBlock().getType() == event.getTo()) {
            return;
        }

        // Ignore when block is not solid and is not going to be
        if (!event.getBlock().getType().isSolid() && !event.getTo().isSolid()) {
            return;
        }

        // Check if it is a farm
        if (event.getBlock().getType() == Material.FARMLAND) {
            return;
        }

        // Check if entity is a falling block or a primed TNT
        if (event.getEntityType() == EntityType.FALLING_BLOCK ||
                event.getEntityType() == EntityType.PRIMED_TNT ||
                event.getEntityType() == EntityType.VILLAGER ||
                event.getEntityType() == EntityType.BEE ||
                event.getEntityType() == EntityType.TURTLE) {
            // allow these entities
            return;
        }

        // Check if entity is a player
        Player player = null;
        if (event.getEntityType() == EntityType.PLAYER) {
            player = (Player) event.getEntity();
        }

        // Check on block place or break
        if (!event.getTo().isSolid()) {
            AttemptBreakEvent breakEvent = new AttemptBreakEvent(event.getBlock(), player);
            plugin.getServer().getPluginManager().callEvent(breakEvent);

            // Check if event was cancelled
            if (breakEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onBucketFill(PlayerBucketFillEvent event) {
        AttemptBreakEvent breakEvent = new AttemptBreakEvent(event.getBlock(), event.getPlayer());
        plugin.getServer().getPluginManager().callEvent(breakEvent);

        // Check if event was cancelled
        if (breakEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onDamage(BlockDamageEvent event) {
        AttemptBreakEvent breakEvent = new AttemptBreakEvent(event.getBlock(), event.getPlayer());
        plugin.getServer().getPluginManager().callEvent(breakEvent);

        // Check if it was cancelled
        if (breakEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onBreak(BlockBreakEvent event) {
        AttemptBreakEvent breakEvent = new AttemptBreakEvent(event.getBlock(), event.getPlayer());
        plugin.getServer().getPluginManager().callEvent(breakEvent);

        // Check if it was cancelled
        if (breakEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBreakMonitor(BlockBreakEvent event) {
        BreakEvent breakEvent = new BreakEvent(event.getBlock());
        plugin.getServer().getPluginManager().callEvent(breakEvent);
    }
}
