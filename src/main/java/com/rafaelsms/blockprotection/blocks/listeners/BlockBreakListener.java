package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.blocks.events.AttemptBreakEvent;
import com.rafaelsms.blockprotection.blocks.events.BreakEvent;
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

import java.util.Iterator;

public class BlockBreakListener implements Listener {

    private final BlockProtectionPlugin plugin;

    public BlockBreakListener(BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onEntityExplodeBlocks(EntityExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();

            AttemptBreakEvent breakEvent = new AttemptBreakEvent(block, null);
            plugin.getServer().getPluginManager().callEvent(breakEvent);

            // Check if event was cancelled
            if (breakEvent.isCancelled()) {
                // Remove the block and continue searching
                iterator.remove();
            }
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
