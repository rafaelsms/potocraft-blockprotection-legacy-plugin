package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlaceEvent extends BlockEvent implements NotNullPlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final @NotNull Player player;

    public PlaceEvent(@NotNull Block block, @NotNull Player player) {
        super(block);
        this.player = player;
    }

    @Override
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
