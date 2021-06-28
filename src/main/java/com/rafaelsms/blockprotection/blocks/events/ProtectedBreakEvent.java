package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

public class ProtectedBreakEvent extends BlockEvent {

    private static final HandlerList handlers = new HandlerList();

    public ProtectedBreakEvent(@NotNull Block block) {
        super(block);
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
