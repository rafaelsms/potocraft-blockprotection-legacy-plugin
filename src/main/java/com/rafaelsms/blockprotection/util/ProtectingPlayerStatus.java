package com.rafaelsms.blockprotection.util;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Lang;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ProtectingPlayerStatus {

	private final int PROTECTION_DURATION_TICKS;
	private final int CHARGING_DURATION_TICKS;

	private final BlockProtectionPlugin plugin;
	private final Player player;

	private final BossBar protectingBar;
	private final BossBar chargingBar;

	private PlayerState state = PlayerState.NONE;
	private int remainingTicks = 0;
	private BukkitTask counterTask = null;

	public ProtectingPlayerStatus(BlockProtectionPlugin plugin, Player player) {
		this.plugin = plugin;
		this.player = player;

		// Get from configuration
		this.PROTECTION_DURATION_TICKS = plugin.getConfig().getInt("config.protection.protection_duration_tick");
		this.CHARGING_DURATION_TICKS = plugin.getConfig().getInt("config.protection.charging_duration_tick");

		// Start bars
		protectingBar = BossBar.bossBar(
				Lang.PROTECTION_BLOCK_PROTECTING_TITLE.toComponent(plugin),
				BossBar.MAX_PROGRESS,
				BossBar.Color.GREEN,
				BossBar.Overlay.PROGRESS
		);
		chargingBar = BossBar.bossBar(
				Lang.PROTECTION_BLOCK_CHARGING_TITLE.toComponent(plugin),
				BossBar.MIN_PROGRESS,
				BossBar.Color.RED,
				BossBar.Overlay.PROGRESS
		);
	}

	private void cancelTask() {
		// Filter null counterTasks
		if (this.counterTask == null) {
			return;
		}
		// Cancel and make it null
		this.counterTask.cancel();
		this.counterTask = null;
	}

	private void setState(PlayerState state) {
		// Cancel task
		cancelTask();

		// If we are changing states
		if (this.state != state) {
			// Hide bar
			if (this.state == PlayerState.PROTECTING) {
				// Reset to their initial state
				chargingBar.progress(BossBar.MIN_PROGRESS);
				protectingBar.progress(BossBar.MAX_PROGRESS);
				// Hide bar
				player.hideBossBar(protectingBar);
			} else if (this.state == PlayerState.CHARGING) {
				// Reset to their initial state
				chargingBar.progress(BossBar.MIN_PROGRESS);
				protectingBar.progress(BossBar.MAX_PROGRESS);
				// Hide bar
				player.hideBossBar(chargingBar);
			}
		}

		// Update to new state
		this.state = state;

		// Check if new state requires bar
		if (this.state == PlayerState.PROTECTING) {
			// Reset to their initial state
			chargingBar.progress(BossBar.MIN_PROGRESS);
			protectingBar.progress(BossBar.MAX_PROGRESS);
			// Reset remaining ticks
			this.remainingTicks = PROTECTION_DURATION_TICKS;

			// Show protecting bar
			player.showBossBar(protectingBar);
		} else if (this.state == PlayerState.CHARGING) {
			// Reset to their initial state
			chargingBar.progress(BossBar.MIN_PROGRESS);
			protectingBar.progress(BossBar.MAX_PROGRESS);
			// Reset remaining ticks
			this.remainingTicks = CHARGING_DURATION_TICKS;

			// Show charging bar
			player.showBossBar(chargingBar);
		}

		// Don't stark task for NONE
		if (this.state == PlayerState.NONE) {
			return;
		}

		// Start task again
		counterTask = plugin.getServer().getScheduler().runTaskTimer(
				plugin,
				() -> {
					// Decrease remaining ticks
					remainingTicks--;

					// Check if we need to stop protecting
					if (remainingTicks <= 0) {

						// Check which action we need to do when we are over
						if (this.state == PlayerState.PROTECTING) {
							setState(PlayerState.NONE);
						} else if (this.state == PlayerState.CHARGING) {
							setState(PlayerState.PROTECTING);
						} else if (this.state == PlayerState.NONE) {
							cancelTask();
						}

					} else {
						// Set progress
						switch (this.state) {
							case PROTECTING -> protectingBar
									                   .progress((float) remainingTicks / PROTECTION_DURATION_TICKS);
							case CHARGING -> chargingBar
									                 .progress(1.0f - (float) remainingTicks / CHARGING_DURATION_TICKS);
							case NONE -> {
								// Shouldn't happen, hide bar
								player.hideBossBar(protectingBar);
								player.hideBossBar(chargingBar);
								// Cancel counter
								cancelTask();
							}
						}
					}

				},
				1,
				1
		);
	}

	public void forceCancel() {
		switch (state) {
			// Cancel any stat other than none
			case CHARGING, PROTECTING -> setState(PlayerState.NONE);
			case NONE -> {
			}
		}
	}

	public void sneak(boolean sneaking) {
		switch (state) {
			// In case of none, start charging
			case NONE -> {
				if (sneaking) {
					setState(PlayerState.CHARGING);
				}
			}
			// In case of charging, cancel
			case CHARGING -> {
				if (!sneaking) {
					setState(PlayerState.NONE);
				}
			}
			// Maintain protecting state
			case PROTECTING -> {
			}
		}
	}

	public void place() {
		switch (state) {
			// Cancel if charging, maintain if none
			case NONE, CHARGING -> setState(PlayerState.NONE);
			// Restart state protecting
			case PROTECTING -> setState(PlayerState.PROTECTING);
		}
	}

	public void destroy() {
		setState(PlayerState.NONE);
	}

	public boolean isProtecting() {
		return state == PlayerState.PROTECTING;
	}
}
