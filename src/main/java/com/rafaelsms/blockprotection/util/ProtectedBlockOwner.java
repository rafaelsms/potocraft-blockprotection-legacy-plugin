package com.rafaelsms.blockprotection.util;

import org.bukkit.OfflinePlayer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProtectedBlockOwner {

	private final OfflinePlayer offlinePlayer;
	private final LocalDateTime dateTime;

	public ProtectedBlockOwner(OfflinePlayer offlinePlayer, LocalDateTime dateTime) {
		this.offlinePlayer = offlinePlayer;
		this.dateTime = dateTime;
	}

	public OfflinePlayer getOfflinePlayer() {
		return offlinePlayer;
	}

	public String printDate() {
		return dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
	}
}