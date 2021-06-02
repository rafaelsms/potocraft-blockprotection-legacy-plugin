package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AttemptPlaceEvent extends CancellableBlockEvent implements UserEvent {

	private static final HandlerList handlers = new HandlerList();

	private final @Nullable Player player;

	/**
	 * Event that represents a block break event.
	 * (This is a shortcut for every block place or change caused by players or entities).
	 *
	 * @param player  user that attempted to place the block
	 * @param block block being placed
	 */
	public AttemptPlaceEvent(@Nullable Player player, @NotNull Block block) {
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