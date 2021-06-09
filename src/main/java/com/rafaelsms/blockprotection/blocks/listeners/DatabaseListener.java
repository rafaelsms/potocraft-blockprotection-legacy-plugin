package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Lang;
import com.rafaelsms.blockprotection.blocks.events.ProtectedBreakEvent;
import com.rafaelsms.blockprotection.blocks.events.ProtectedPlaceEvent;
import com.rafaelsms.blockprotection.util.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class DatabaseListener extends Listener {

	public DatabaseListener(BlockProtectionPlugin plugin) {
		super(plugin);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onProtectedPlace(ProtectedPlaceEvent event) {
		if (!plugin.getBlocksDatabase().insertBlock(event.getBlock().getLocation(), event.getUniqueId())) {
			Lang.PROTECTION_DATABASE_FAILURE.sendActionBar(plugin, event.getPlayer());
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onProtectedBreak(ProtectedBreakEvent event) {
		plugin.getBlocksDatabase().deleteBlock(event.getBlock().getLocation());
	}
}