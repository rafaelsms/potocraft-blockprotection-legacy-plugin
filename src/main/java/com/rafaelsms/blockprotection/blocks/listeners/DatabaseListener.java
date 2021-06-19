package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Lang;
import com.rafaelsms.blockprotection.blocks.events.ProtectedBreakEvent;
import com.rafaelsms.blockprotection.blocks.events.ProtectedPlaceEvent;
import com.rafaelsms.blockprotection.util.Listener;
import com.rafaelsms.blockprotection.util.ProtectionQuery;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class DatabaseListener extends Listener {

    public DatabaseListener(BlockProtectionPlugin plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onProtectedPlace(ProtectedPlaceEvent event) {
        ProtectionQuery blockQuery = plugin.getBlocksDatabase().insertBlock(
                event.getBlock().getLocation(),
                event.getPlayer().getUniqueId(),
                plugin.getBlocksDatabase().getUpdateRadius(),
                plugin.getBlocksDatabase().getSearchRadius(),
                plugin.getBlocksDatabase().getNeededCountToProtect());
        switch (blockQuery.getResult()) {
            case PROTECTED -> Lang.PROTECTION_BLOCK_PROTECTING.sendActionBar(event.getPlayer());
            case NOT_PROTECTED -> Lang.PROTECTION_BLOCK_NOT_PROTECTING.sendActionBar(event.getPlayer());
            case DATABASE_FAILURE -> Lang.PROTECTION_DATABASE_FAILURE.sendActionBar(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onProtectedBreak(ProtectedBreakEvent event) {
        plugin.getBlocksDatabase().deleteBlock(event.getBlock().getLocation());
    }
}