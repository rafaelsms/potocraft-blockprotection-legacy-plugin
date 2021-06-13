package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.blocks.events.AttemptBreakEvent;
import com.rafaelsms.blockprotection.blocks.events.BreakEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

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

			AttemptBreakEvent breakEvent = new AttemptBreakEvent(null, block);
			plugin.getServer().getPluginManager().callEvent(breakEvent);

			// Check if event was cancelled
			if (breakEvent.isCancelled()) {
				// Remove the block and continue searching
				iterator.remove();
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	private void onBreakDoor(EntityBreakDoorEvent event) {
		AttemptBreakEvent breakEvent = new AttemptBreakEvent(null, event.getBlock());
		plugin.getServer().getPluginManager().callEvent(breakEvent);

		// Check if event was cancelled
		if (breakEvent.isCancelled()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	private void onBurn(BlockBurnEvent event) {
		AttemptBreakEvent breakEvent = new AttemptBreakEvent(null, event.getBlock());
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

		// Check if it is a farm
		if (event.getBlock().getType() == Material.FARMLAND) {
			return;
		}

		// Check if entity is a falling block or a primed TNT
		if (event.getEntityType() == EntityType.FALLING_BLOCK || event.getEntityType() == EntityType.PRIMED_TNT) {
			// allow these entities
			return;
		}

		// Check on block place or break
		if (event.getTo().isEmpty()) {
			AttemptBreakEvent breakEvent = new AttemptBreakEvent(null, event.getBlock());
			plugin.getServer().getPluginManager().callEvent(breakEvent);

			// Check if event was cancelled
			if (breakEvent.isCancelled()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	private void onDamage(BlockDamageEvent event) {
		AttemptBreakEvent breakEvent = new AttemptBreakEvent(event.getPlayer(), event.getBlock());
		plugin.getServer().getPluginManager().callEvent(breakEvent);

		// Check if it was cancelled
		if (breakEvent.isCancelled()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	private void onBreak(BlockBreakEvent event) {
		AttemptBreakEvent breakEvent = new AttemptBreakEvent(event.getPlayer(), event.getBlock());
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