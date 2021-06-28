package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AttemptPlaceEvent extends BlockEvent implements Cancellable, PlayerUUIDEvent {

    private static final HandlerList handlers = new HandlerList();

    private final @Nullable Player player;

    private boolean cancelled = false;

    /**
     * Event that represents a block break event.
     * (This is a shortcut for every block place or change caused by players or entities).
     *
     * @param player user that attempted to place the block
     * @param block  block being placed
     */
    public AttemptPlaceEvent(@NotNull Block block, @Nullable Player player) {
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
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
