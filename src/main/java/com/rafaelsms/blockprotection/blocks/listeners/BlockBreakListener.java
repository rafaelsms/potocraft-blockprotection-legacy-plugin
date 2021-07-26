package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.blocks.events.AttemptBreakEvent;
import com.rafaelsms.blockprotection.blocks.events.AttemptMultiBreakEvent;
import com.rafaelsms.blockprotection.blocks.events.BreakEvent;
import com.rafaelsms.blockprotection.blocks.events.MultiBreakEvent;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public record BlockBreakListener(BlockProtectionPlugin plugin) implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onEntityExplodeBlocks(EntityExplodeEvent event) {
        // Ignore explosives
        if (event.getEntityType() == EntityType.PRIMED_TNT ||
                    event.getEntityType() == EntityType.MINECART_TNT ||
                    event.getEntityType() == EntityType.ENDER_CRYSTAL) {
            return;
        }

        AttemptMultiBreakEvent breakEvent = new AttemptMultiBreakEvent(event.blockList());
        plugin.getServer().getPluginManager().callEvent(breakEvent);

        // Check if event was cancelled
        if (breakEvent.isCancelled()) {
            // Clear list instead of cancelling it
            event.blockList().clear();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onBreakDoor(EntityBreakDoorEvent event) {
        AttemptBreakEvent breakEvent = new AttemptBreakEvent(event.getBlock());
        plugin.getServer().getPluginManager().callEvent(breakEvent);

        // Check if event was cancelled
        if (breakEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onBurn(BlockBurnEvent event) {
        AttemptBreakEvent breakEvent = new AttemptBreakEvent(event.getBlock());
        plugin.getServer().getPluginManager().callEvent(breakEvent);

        // Check if event was cancelled
        if (breakEvent.isCancelled()) {
            event.setCancelled(true);
        }
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onFadeMonitor(BlockFadeEvent event) {
        BreakEvent breakEvent = new BreakEvent(event.getBlock());
        plugin.getServer().getPluginManager().callEvent(breakEvent);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityExplodeBlocksMonitor(EntityExplodeEvent event) {
        // Ignore empty block list
        if (event.blockList().isEmpty()) {
            return;
        }

        MultiBreakEvent breakEvent = new MultiBreakEvent(event.blockList());
        plugin.getServer().getPluginManager().callEvent(breakEvent);
    }
}
