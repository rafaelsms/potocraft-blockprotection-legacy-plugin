package com.rafaelsms.blockprotection.util;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProtectedBlockDate {

    private final OfflinePlayer offlinePlayer;
    private final LocalDateTime dateTime;
    private final boolean validBlock;
    private final boolean temporaryBlock;

    public ProtectedBlockDate(@Nullable OfflinePlayer offlinePlayer, @NotNull LocalDateTime dateTime,
                              boolean validBlock, boolean temporaryBlock) {
        this.offlinePlayer = offlinePlayer;
        this.dateTime = dateTime;
        this.validBlock = validBlock;
        this.temporaryBlock = temporaryBlock;
    }

    public ProtectedBlockDate() {
        this.offlinePlayer = null;
        this.dateTime = null;
        this.validBlock = false;
        this.temporaryBlock = true;
    }

    public boolean isNotRegistered() {
        return dateTime == null;
    }

    public @Nullable OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    public @NotNull String printOfflinePlayer() {
        return offlinePlayer != null && offlinePlayer.getName() != null ? offlinePlayer.getName() : "null";
    }

    public @NotNull String printDate() {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) : "null";
    }

    public boolean isTemporaryBlock() {
        return temporaryBlock;
    }

    public boolean isValid() {
        return validBlock;
    }
}
