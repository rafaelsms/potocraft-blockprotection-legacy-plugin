package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class AttemptMultiBreakEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final @NotNull List<Block> blocks;

    private boolean cancelled = false;

    public AttemptMultiBreakEvent(@NotNull Collection<Block> blocks) {
        this.blocks = List.copyOf(blocks);
    }

    public @NotNull List<Block> getBlocks() {
        return blocks;
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
