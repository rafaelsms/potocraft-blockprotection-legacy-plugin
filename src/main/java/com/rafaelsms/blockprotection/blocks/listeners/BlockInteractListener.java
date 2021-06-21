package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.blocks.events.AttemptInteractEvent;
import com.rafaelsms.blockprotection.blocks.events.InteractEvent;
import com.rafaelsms.blockprotection.util.Listener;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockInteractListener extends Listener {

    public BlockInteractListener(BlockProtectionPlugin plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onInteraction(PlayerInteractEvent event) {
        // Let's ignore when no block is involved
        Block clickedBlock = event.getClickedBlock();
        if (event.useInteractedBlock() == Event.Result.DENY || clickedBlock == null) {
            return;
        }

        AttemptInteractEvent interactEvent = new AttemptInteractEvent(clickedBlock, event.getPlayer());
        plugin.getServer().getPluginManager().callEvent(interactEvent);

        // Check if event was cancelled
        if (interactEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onInteractionMonitor(PlayerInteractEvent event) {
        // Check if there isn't a clicked block
        Block clickedBlock = event.getClickedBlock();
        if (event.useInteractedBlock() == Event.Result.DENY || clickedBlock == null) {
            return;
        }

        // Invoke interaction
        InteractEvent interactEvent = new InteractEvent(clickedBlock, event.getPlayer());
        plugin.getServer().getPluginManager().callEvent(interactEvent);
    }
}