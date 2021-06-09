package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
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
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.PortalCreateEvent;

public class BlockPlaceListener implements Listener {

	private final BlockProtectionPlugin plugin;

	public BlockPlaceListener(BlockProtectionPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPortalCreation(PortalCreateEvent event) {
		// Ignore events other than nether generating portal
		if (event.getReason() != PortalCreateEvent.CreateReason.NETHER_PAIR) {
			return;
		}

		for (BlockState block : event.getBlocks()) {
			AttemptPlaceEvent placeEvent = new AttemptPlaceEvent(null, block.getBlock());
			plugin.getServer().getPluginManager().callEvent(placeEvent);

			// Check if event was cancelled
			if (placeEvent.isCancelled()) {
				event.setCancelled(true);
				// We can return on any block
				return;
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onChangeToAnything(EntityChangeBlockEvent event) {
		// Check if block changed at all
		if (event.getBlock().getType() == event.getTo()) {
			return;
		}

		// Check if it is a farm
		if (event.getBlock().getType() == Material.FARMLAND) {
			return;
		}

		// Check if entity is a falling block or a primed TNT
		if (event.getEntityType() == EntityType.FALLING_BLOCK ||
				    event.getEntityType() == EntityType.PRIMED_TNT) {
			// allow these entities
			return;
		}

		if (!event.getTo().isEmpty()) {
			AttemptPlaceEvent placeEvent = new AttemptPlaceEvent(null, event.getBlock());
			plugin.getServer().getPluginManager().callEvent(placeEvent);

			// Check if event was cancelled
			if (placeEvent.isCancelled()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onCanBuildBlock(BlockCanBuildEvent event) {
		AttemptPlaceEvent placeEvent = new AttemptPlaceEvent(event.getPlayer(), event.getBlock());
		plugin.getServer().getPluginManager().callEvent(placeEvent);

		// Check if event was cancelled
		if (placeEvent.isCancelled()) {
			event.setBuildable(false);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlace(BlockPlaceEvent event) {
		AttemptPlaceEvent placeEvent = new AttemptPlaceEvent(event.getPlayer(), event.getBlock());
		plugin.getServer().getPluginManager().callEvent(placeEvent);

		// Check if event was cancelled
		if (placeEvent.isCancelled()) {
			event.setCancelled(false);
			event.setBuild(false);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onMultiPlace(BlockMultiPlaceEvent event) {
		// Do the same as place
		onPlace(event);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlaceMonitor(BlockPlaceEvent event) {
		PlaceEvent placeEvent = new PlaceEvent(event.getPlayer(), event.getBlock());
		plugin.getServer().getPluginManager().callEvent(placeEvent);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onMultiPlaceMonitor(BlockMultiPlaceEvent event) {
		// Do the same as place
		onPlaceMonitor(event);
	}
}