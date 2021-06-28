package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.blocks.events.BreakEvent;
import com.rafaelsms.blockprotection.util.Listener;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class BlockPistonListener extends Listener {

    public BlockPistonListener(BlockProtectionPlugin plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onExtendMonitor(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            BreakEvent breakEvent = new BreakEvent(block);
            plugin.getServer().getPluginManager().callEvent(breakEvent);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onRetractMonitor(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            BreakEvent breakEvent = new BreakEvent(block);
            plugin.getServer().getPluginManager().callEvent(breakEvent);
        }
    }
}
