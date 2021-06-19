package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Config;
import com.rafaelsms.blockprotection.blocks.events.AttemptPlaceEvent;
import com.rafaelsms.blockprotection.blocks.events.PlaceEvent;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.PortalCreateEvent;

public class BlockPlaceListener implements Listener {

    private final BlockProtectionPlugin plugin;

    private final boolean stopFireSpread;

    public BlockPlaceListener(BlockProtectionPlugin plugin) {
        this.plugin = plugin;
        // Get configuration
        stopFireSpread = Config.PROTECTION_STOP_FIRE_SPREAD.getBoolean();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onPortalCreation(PortalCreateEvent event) {
        // Ignore events other than nether generating portal
        if (event.getReason() != PortalCreateEvent.CreateReason.NETHER_PAIR) {
            return;
        }

        for (BlockState block : event.getBlocks()) {
            AttemptPlaceEvent placeEvent = new AttemptPlaceEvent(block.getBlock(), null);
            plugin.getServer().getPluginManager().callEvent(placeEvent);

            // Check if event was cancelled
            if (placeEvent.isCancelled()) {
                event.setCancelled(true);
                // We can return on any block
                return;
            }
        }
    }

    @SuppressWarnings("DuplicatedCode")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onChangeToAnything(EntityChangeBlockEvent event) {
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

        AttemptPlaceEvent placeEvent = new AttemptPlaceEvent(event.getBlock(), null);
        plugin.getServer().getPluginManager().callEvent(placeEvent);

        // Check if event was cancelled
        if (placeEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onSpreadBlock(BlockSpreadEvent event) {
        // Check if we need to skip fire altogether
        if (stopFireSpread && event.getSource().getType() == Material.FIRE) {
            event.getSource().setType(Material.AIR);
            event.setCancelled(true);
        }

        // Check just when a block appears
        if (event.getBlock().isEmpty() && event.getNewState().getType().isSolid()) {
            AttemptPlaceEvent placeEvent = new AttemptPlaceEvent(event.getBlock(), null);
            plugin.getServer().getPluginManager().callEvent(placeEvent);

            // Check if event was cancelled
            if (placeEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onCanBuildBlock(BlockCanBuildEvent event) {
        AttemptPlaceEvent placeEvent = new AttemptPlaceEvent(event.getBlock(), event.getPlayer());
        plugin.getServer().getPluginManager().callEvent(placeEvent);

        // Check if event was cancelled
        if (placeEvent.isCancelled()) {
            event.setBuildable(false);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onPlace(BlockPlaceEvent event) {
        AttemptPlaceEvent placeEvent = new AttemptPlaceEvent(event.getBlock(), event.getPlayer());
        plugin.getServer().getPluginManager().callEvent(placeEvent);

        // Check if event was cancelled
        if (placeEvent.isCancelled()) {
            event.setCancelled(false);
            event.setBuild(false);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onMultiPlace(BlockMultiPlaceEvent event) {
        // Do the same as place
        onPlace(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlaceMonitor(BlockPlaceEvent event) {
        PlaceEvent placeEvent = new PlaceEvent(event.getBlock(), event.getPlayer());
        plugin.getServer().getPluginManager().callEvent(placeEvent);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onMultiPlaceMonitor(BlockMultiPlaceEvent event) {
        // Do the same as place
        onPlaceMonitor(event);
    }
}