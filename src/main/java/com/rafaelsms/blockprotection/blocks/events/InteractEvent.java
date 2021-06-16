package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class InteractEvent extends BlockEvent implements PlayerUUIDEvent {

	private static final HandlerList handlers = new HandlerList();

	private final @Nullable Player player;

	public InteractEvent(@NotNull Block block, @Nullable Player player) {
		super(block);
		this.player = player;
	}

	public @Nullable Player getPlayer() {
		return player;
	}

	@Override
	public @Nullable UUID getPlayerUUID() {
		return player != null ? player.getUniqueId() : null;
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