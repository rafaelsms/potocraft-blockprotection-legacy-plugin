package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

public class ProtectedPlaceEvent extends BlockEvent {

	private static final HandlerList handlers = new HandlerList();

	private final @NotNull Player player;

	public ProtectedPlaceEvent(@NotNull Block block, @NotNull Player player) {
		super(block);
		this.player = player;
	}

	public @NotNull Player getPlayer() {
		return player;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	@SuppressWarnings("unused")
	public static HandlerList getHandlerList() {
		return handlers;
	}
}