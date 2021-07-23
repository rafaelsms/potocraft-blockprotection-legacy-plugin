package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class MultiBreakEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final @NotNull List<Block> blocks;

    public MultiBreakEvent(@NotNull Collection<Block> blocks) {
        this.blocks = List.copyOf(blocks);
    }

    public @NotNull List<Block> getBlocks() {
        return blocks;
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
