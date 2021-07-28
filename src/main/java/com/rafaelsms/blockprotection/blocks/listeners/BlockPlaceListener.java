package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Config;
import com.rafaelsms.blockprotection.blocks.events.AttemptMultiPlaceEvent;
import com.rafaelsms.blockprotection.blocks.events.AttemptPlaceEvent;
import com.rafaelsms.blockprotection.blocks.events.PlaceEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.world.PortalCreateEvent;

import java.util.List;
import java.util.stream.Collectors;

public record BlockPlaceListener(BlockProtectionPlugin plugin) implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onPortalCreation(PortalCreateEvent event) {
        // Ignore events other than nether generating portal
        if (event.getReason() != PortalCreateEvent.CreateReason.NETHER_PAIR) {
            return;
        }

        List<Block> blocks = event.getBlocks().stream()
                                     .map(BlockState::getBlock)
                                     .collect(Collectors.toList());

        AttemptMultiPlaceEvent multiPlaceEvent = new AttemptMultiPlaceEvent(blocks);
        plugin.getServer().getPluginManager().callEvent(multiPlaceEvent);

        // Check if event was cancelled
        if (multiPlaceEvent.isCancelled()) {
            event.setCancelled(true);
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

        // Check if entity is a player
        Player player = null;
        if (event.getEntityType() == EntityType.PLAYER) {
            player = (Player) event.getEntity();
        }

        AttemptPlaceEvent placeEvent = new AttemptPlaceEvent(event.getBlock(), player);
        plugin.getServer().getPluginManager().callEvent(placeEvent);

        // Check if event was cancelled
        if (placeEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onSpreadBlock(BlockSpreadEvent event) {
        // Check if we need to skip fire altogether
        if (event.getSource().getType() == Material.FIRE && Config.PROTECTION_PREVENT_FIRE_SPREAD.getBoolean()) {
            event.getSource().setType(Material.AIR);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onBucketEmpty(PlayerBucketEmptyEvent event) {
        AttemptPlaceEvent placeEvent = new AttemptPlaceEvent(event.getBlock(), event.getPlayer());
        plugin.getServer().getPluginManager().callEvent(placeEvent);

        // Check if event was cancelled
        if (placeEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onPlace(BlockPlaceEvent event) {
        AttemptPlaceEvent placeEvent = new AttemptPlaceEvent(event.getBlock(), event.getPlayer());
        plugin.getServer().getPluginManager().callEvent(placeEvent);

        // Check if event was cancelled
        if (placeEvent.isCancelled()) {
            event.setCancelled(true);
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
