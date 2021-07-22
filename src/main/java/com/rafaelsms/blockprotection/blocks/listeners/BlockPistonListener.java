package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;
import java.util.stream.Collectors;

public record BlockPistonListener(BlockProtectionPlugin plugin) implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onExtendMonitor(BlockPistonExtendEvent event) {
        List<Location> locations = event.getBlocks().stream()
                                           .map(Block::getLocation)
                                           .collect(Collectors.toList());
        plugin.getBlocksDatabase().deleteBlocksAsync(locations);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onRetractMonitor(BlockPistonRetractEvent event) {
        List<Location> locations = event.getBlocks().stream()
                                           .map(Block::getLocation)
                                           .collect(Collectors.toList());
        plugin.getBlocksDatabase().deleteBlocksAsync(locations);
    }
}
