package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ProtectedPlaceEvent extends BlockEvent implements UserEvent {

	private static final HandlerList handlers = new HandlerList();

	private final @NotNull Player player;

	public ProtectedPlaceEvent(@NotNull Player player, @NotNull Block block) {
		super(block);
		this.player = player;
	}

	@Override
	public @NotNull Player getPlayer() {
		return player;
	}

	@Override
	public @NotNull UUID getUniqueId() {
		return player.getUniqueId();
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}