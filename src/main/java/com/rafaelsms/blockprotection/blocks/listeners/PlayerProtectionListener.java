package com.rafaelsms.blockprotection.blocks.listeners;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.blocks.events.ProtectedPlaceEvent;
import com.rafaelsms.blockprotection.util.ProtectingPlayerStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.HashMap;
import java.util.UUID;

public class PlayerProtectionListener implements Listener {

	private final HashMap<UUID, ProtectingPlayerStatus> protecting = new HashMap<>();

	private final BlockProtectionPlugin plugin;

	public PlayerProtectionListener(BlockProtectionPlugin plugin) {
		this.plugin = plugin;
	}

	private ProtectingPlayerStatus getOrCreate(Player player) {
		ProtectingPlayerStatus playerStatus = protecting.getOrDefault(player.getUniqueId(), null);

		// Create player status if it doesn't exists
		if (playerStatus == null) {
			playerStatus = new ProtectingPlayerStatus(plugin, player);
			protecting.put(player.getUniqueId(), playerStatus);
		}

		return playerStatus;
	}

	public boolean isProtecting(Player player) {
		return getOrCreate(player).isProtecting();
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onQuit(PlayerQuitEvent event) {
		ProtectingPlayerStatus playerStatus = protecting.remove(event.getPlayer().getUniqueId());
		if (playerStatus != null) {
			playerStatus.destroy();
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onPlayerSneak(PlayerToggleSneakEvent event) {
		getOrCreate(event.getPlayer()).sneak(event.isSneaking());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onProtectedPlace(ProtectedPlaceEvent event) {
		getOrCreate(event.getPlayer()).place();
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onPlayerSprint(PlayerToggleSprintEvent event) {
		if (event.isSprinting()) {
			getOrCreate(event.getPlayer()).forceCancel();
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onPlayerTeleport(PlayerTeleportEvent event) {
		getOrCreate(event.getPlayer()).forceCancel();
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onPlayerDeath(PlayerDeathEvent event) {
		getOrCreate(event.getEntity()).forceCancel();
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onPlayerElytra(PlayerElytraBoostEvent event) {
		getOrCreate(event.getPlayer()).forceCancel();
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onPlayerAtPortal(PlayerPortalEvent event) {
		getOrCreate(event.getPlayer()).forceCancel();
	}
}
