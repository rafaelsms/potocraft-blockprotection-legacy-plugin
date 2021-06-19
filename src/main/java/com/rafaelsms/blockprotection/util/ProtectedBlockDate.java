package com.rafaelsms.blockprotection.util;

import org.bukkit.OfflinePlayer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProtectedBlockDate {

    private final OfflinePlayer offlinePlayer;
    private final LocalDateTime dateTime;
    private final boolean temporaryBlock;

    public ProtectedBlockDate(OfflinePlayer offlinePlayer, LocalDateTime dateTime, boolean temporaryBlock) {
        this.offlinePlayer = offlinePlayer;
        this.dateTime = dateTime;
        this.temporaryBlock = temporaryBlock;
    }

    public ProtectedBlockDate() {
        this.offlinePlayer = null;
        this.dateTime = null;
        this.temporaryBlock = true;
    }

    public boolean isNull() {
        return offlinePlayer == null || dateTime == null;
    }

    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    public String printDate() {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) : "null";
    }

    public boolean isTemporaryBlock() {
        return temporaryBlock;
    }
}