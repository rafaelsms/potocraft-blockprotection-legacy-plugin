package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Lang;
import com.rafaelsms.blockprotection.blocks.events.ProtectedBreakEvent;
import com.rafaelsms.blockprotection.blocks.events.ProtectedPlaceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DatabaseListener implements Listener {

    private final BlockProtectionPlugin plugin;

    public DatabaseListener(BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onProtectedPlace(ProtectedPlaceEvent event) {
        boolean inserted = plugin.getBlocksDatabase().insertBlock(
                event.getBlock().getLocation(),
                event.getPlayer().getUniqueId(),
                plugin.getBlocksDatabase().getUpdateTimeRadius(),
                plugin.getBlocksDatabase().getSearchRadiusForTemporary(),
                plugin.getBlocksDatabase().getNeededNearbyCountToProtect());
        if (!inserted) {
            Lang.PROTECTION_DATABASE_FAILURE.sendActionBar(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onProtectedBreak(ProtectedBreakEvent event) {
        plugin.getBlocksDatabase().deleteBlock(event.getBlock().getLocation());
    }
}
