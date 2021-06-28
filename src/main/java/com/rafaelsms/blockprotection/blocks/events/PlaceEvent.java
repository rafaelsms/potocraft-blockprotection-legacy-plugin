package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlaceEvent extends BlockEvent implements PlayerUUIDEvent {

    private static final HandlerList handlers = new HandlerList();

    private final @NotNull Player player;

    /**
     * A block place event that may or may not be protected.
     * (This is a shortcut for every block place or change caused by players or entities).
     *
     * @param player user placing the block
     * @param block  block being placed
     */
    public PlaceEvent(@NotNull Block block, @NotNull Player player) {
        super(block);
        this.player = player;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull UUID getPlayerUUID() {
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
