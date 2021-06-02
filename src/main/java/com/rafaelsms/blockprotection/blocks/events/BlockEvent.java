package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

abstract class BlockEvent extends Event {

	protected final @NotNull Block block;

	BlockEvent(@NotNull Block block) {
		this.block = block;
	}

	public @NotNull Block getBlock() {
		return block;
	}
}