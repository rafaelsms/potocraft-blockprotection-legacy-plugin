package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlaceEvent extends BlockEvent implements PlayerUUIDEvent {

    private static final HandlerList handlers = new HandlerList();

    private final @Nullable Player player;

    /**
     * A block place event that may or may not be protected.
     * (This is a shortcut for every block place or change caused by players or entities).
     *
     * @param player user placing the block
     * @param block  block being placed
     */
    public PlaceEvent(@NotNull Block block, @Nullable Player player) {
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

    public static HandlerList getHandlerList() {
        return handlers;
    }
}