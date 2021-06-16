package com.rafaelsms.blockprotection.blocks.events;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PlayerUUIDEvent {

	@Nullable UUID getPlayerUUID();

}
