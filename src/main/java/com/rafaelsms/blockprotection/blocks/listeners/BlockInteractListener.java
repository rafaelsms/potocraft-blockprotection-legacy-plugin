package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.blocks.events.AttemptInteractEvent;
import com.rafaelsms.blockprotection.blocks.events.InteractEvent;
import com.rafaelsms.blockprotection.util.Listener;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockInteractListener extends Listener {

	public BlockInteractListener(BlockProtectionPlugin plugin) {
		super(plugin);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	private void onInteraction(PlayerInteractEvent event) {
		// Let's ignore when no block is involved
		Block clickedBlock = event.getClickedBlock();
		if (event.useInteractedBlock() == Event.Result.DENY
				    || clickedBlock == null) {
			return;
		}

		// Check if interaction happened with air
		if (event.getAction().equals(Action.LEFT_CLICK_AIR) ||
				    event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
			event.setUseInteractedBlock(Event.Result.DENY);
			return;
		}

		AttemptInteractEvent interactEvent = new AttemptInteractEvent(event.getPlayer(), clickedBlock);
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
		if (event.useInteractedBlock() == Event.Result.DENY
				    || clickedBlock == null) {
			return;
		}

		// Check if interaction happened with air
		if (event.getAction().equals(Action.LEFT_CLICK_AIR) ||
				    event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
			return;
		}

		// Invoke interaction
		InteractEvent interactEvent = new InteractEvent(event.getPlayer(), clickedBlock);
		plugin.getServer().getPluginManager().callEvent(interactEvent);
	}
}