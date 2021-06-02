package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlaceEvent extends BlockEvent implements UserEvent {

	private static final HandlerList handlers = new HandlerList();

	private final @Nullable Player player;

	/**
	 * A block place event that may or may not be protected.
	 * (This is a shortcut for every block place or change caused by players or entities).
	 *
	 * @param player user placing the block
	 * @param block  block being placed
	 */
	public PlaceEvent(@Nullable Player player, @NotNull Block block) {
		super(block);
		this.player = player;
	}

	@Override
	public @Nullable Player getPlayer() {
		return player;
	}

	@Override
	public @Nullable UUID getUniqueId() {
		return player == null ? null : player.getUniqueId();
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}