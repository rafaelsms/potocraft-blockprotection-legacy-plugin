package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface NotNullPlayerEvent {
    
    @NotNull Player getPlayer();
    @NotNull UUID getPlayerUUID();

}
