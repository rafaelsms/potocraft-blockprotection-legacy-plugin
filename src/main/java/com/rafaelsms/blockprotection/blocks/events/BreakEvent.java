package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BreakEvent extends BlockEvent {

	private static final HandlerList handlers = new HandlerList();

	/**
	 * A block place event that may or may not be protected.
	 * (This is a shortcut for every block break or change caused by players or entities).
	 *
	 * @param block block being broken
	 */
	public BreakEvent(@NotNull Block block) {
		super(block);
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}