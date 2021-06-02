package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public abstract class CancellableBlockEvent extends BlockEvent implements Cancellable {

	// State of the event
	private boolean cancelled = false;

	CancellableBlockEvent(@NotNull Block block) {
		super(block);
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}