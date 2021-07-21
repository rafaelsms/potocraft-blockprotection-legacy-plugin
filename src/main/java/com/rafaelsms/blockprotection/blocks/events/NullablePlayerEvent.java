package com.rafaelsms.blockprotection.blocks.events;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface NullablePlayerEvent {

    @Nullable Player getPlayer();
    @Nullable UUID getPlayerUUID();

}
