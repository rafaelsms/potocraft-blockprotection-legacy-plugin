package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AttemptMultiPlaceEvent extends Event implements Cancellable, NullablePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final @NotNull List<Block> blocks;
    private final @Nullable Player player;

    private boolean cancelled = false;

    public AttemptMultiPlaceEvent(@NotNull Collection<Block> blocks) {
        this.blocks = List.copyOf(blocks);
        this.player = null;
    }

    public AttemptMultiPlaceEvent(@NotNull Collection<Block> blocks, @Nullable Player player) {
        this.blocks = List.copyOf(blocks);
        this.player = player;
    }

    public @NotNull List<Block> getBlocks() {
        return blocks;
    }

    @Override
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
