package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Config;
import com.rafaelsms.blockprotection.Lang;
import com.rafaelsms.blockprotection.Permission;
import com.rafaelsms.blockprotection.blocks.events.*;
import com.rafaelsms.blockprotection.util.Listener;
import com.rafaelsms.blockprotection.util.ProtectedBlock;
import com.rafaelsms.blockprotection.util.ProtectionResult;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProtectionBlockListener extends Listener {

	// Configuration
	private final Set<UUID> protectedWorlds;
	private final Set<Material> materialsAllowedInteraction;
	private final Set<Material> protectedMaterials;
	private final int minimumProtectedHeight;
	private final Material debugItem;

	/*
	 * This listener will get all of our Minecraft events and make into two
	 * (Attempt[Break/Place] which is cancellable and [Break/Place], which
	 * is not).
	 *
	 * We need to get all of those events and check if we need to deny the
	 * event (on attempt to break/place), check if we can make a protected
	 * block or a temporary one (on break/place).
	 */

	public ProtectionBlockListener(BlockProtectionPlugin plugin) {
		super(plugin);

		// Get configuration
		minimumProtectedHeight = plugin.getConfig().getInt(Config.PROTECTION_MINIMUM_HEIGHT.toString());
		debugItem = Material.valueOf(plugin.getConfig().getString(Config.PROTECTION_DEBUG_ITEM.toString()));

		// Check which worlds exists
		HashSet<UUID> protectedWorlds = new HashSet<>();
		for (String worldName : plugin.getConfig().getStringList(Config.PROTECTION_PROTECTED_WORLDS.toString())) {
			World world = plugin.getServer().getWorld(worldName);

			// Check if world exists
			if (world != null) {
				protectedWorlds.add(world.getUID());
				plugin.getLogger().info("World \"%s\" will be protected".formatted(world.getName()));
			} else {
				throw new IllegalArgumentException("World \"%s\" was not found and WILL NOT be protected".formatted(
						worldName));
			}
		}
		this.protectedWorlds = Collections.unmodifiableSet(protectedWorlds);

		// Check allowed materials
		HashSet<Material> materialsAllowedInteraction = new HashSet<>();
		for (String material : plugin.getConfig()
				                       .getStringList(Config.PROTECTION_MATERIALS_ALLOWED_INTERACTION.toString())) {
			materialsAllowedInteraction.add(Material.valueOf(material));
		}
		this.materialsAllowedInteraction = Collections.unmodifiableSet(materialsAllowedInteraction);
		plugin.getLogger().info("%d materials are allowed to be interactable by anyone".formatted(
				this.materialsAllowedInteraction.size()));

		// Check protected materials
		HashSet<Material> protectedMaterials = new HashSet<>();
		for (String material : plugin.getConfig().getStringList(Config.PROTECTION_MATERIALS_PROTECTED.toString())) {
			protectedMaterials.add(Material.valueOf(material));
		}
		this.protectedMaterials = Collections.unmodifiableSet(protectedMaterials);
		plugin.getLogger().info("%d materials are going to be protected".formatted(
				this.protectedMaterials.size()));
	}

	private boolean shouldIgnore(Block block, @Nullable Player player) {
		// Check if is protected world first
		// (otherwise we will get a "under protection height" warning at a unprotected world)
		if (!protectedWorlds.contains(block.getWorld().getUID())) {
			Lang.PROTECTION_UNPROTECTED_WORLD.sendActionBar(plugin, player);
			return true;
		}

		// Check height
		if (block.getY() < minimumProtectedHeight) {
			Lang.PROTECTION_UNDER_MINIMUM_HEIGHT.sendActionBar(plugin, player);
			return true;
		}

		return false;
	}

	@EventHandler(ignoreCancelled = false)
	private void onDebugInteract(AttemptInteractEvent event) {
		Player player = event.getPlayer();
		if (player == null || !player.hasPermission(Permission.DEBUG.toString())) {
			return;
		}

		// Check main item in hand
		ItemStack mainHand = player.getInventory().getItemInMainHand();
		if (mainHand.getType() != debugItem) {
			return;
		}

		// Get block data from database
		ProtectedBlock blockData = plugin.getBlocksDatabase().getBlockData(event.getBlock().getLocation());
		if (blockData != null) {
			if (blockData.getOfflinePlayer() != null) {
				player.sendMessage(Lang.parseLegacyText(Lang.PROTECTION_DEBUG_TEXT.toString(plugin).formatted(
						blockData.getOfflinePlayer().getName(),
						blockData.printDate()
				)));
			} else {
				Lang.PROTECTION_DEBUG_NO_BLOCK.sendMessage(plugin, player);
			}
		} else {
			Lang.PROTECTION_DEBUG_DATABASE_FAILURE.sendMessage(plugin, player);
		}
	}

	@EventHandler(ignoreCancelled = true)
	private void onAttemptInteract(AttemptInteractEvent event) {
		Block block = event.getBlock();

		// Avoid any check when not in a protected environment
		// Interactions shouldn't warn the player
		if (shouldIgnore(block, null)) {
			return;
		}

		// Ignore admin permission to override block interaction
		if (event.getPlayer() != null && event.getPlayer().hasPermission(Permission.PROTECTION_OVERRIDE.toString())) {
			return;
		}

		// Check if material is allowed to interact
		if (materialsAllowedInteraction.contains(block.getType())) {
			// Ignore event (allow interaction)
			return;
		}

		// Check if there are protected blocks nearby
		ProtectionResult result = plugin.getBlocksDatabase().isThereBlockingBlocksNearby(
				block.getLocation(), event.getUniqueId(), plugin.getBlocksDatabase().getInteractRadius()
		);

		if (result.isProtected()) {
			Lang.PROTECTION_NEARBY_BLOCKS.sendActionBar(plugin, event.getPlayer());
			event.setCancelled(true);
		}
		// If there isn't, allow interaction
	}

	@EventHandler(ignoreCancelled = true)
	private void onAttemptBreak(AttemptBreakEvent event) {
		Block block = event.getBlock();

		// Avoid any check when not in a protected environment
		if (shouldIgnore(block, null)) {
			return;
		}

		// Ignore admin permission to override block break
		if (event.getPlayer() != null && event.getPlayer().hasPermission(Permission.PROTECTION_OVERRIDE.toString())) {
			return;
		}

		// Check if there are protected blocks nearby
		ProtectionResult result = plugin.getBlocksDatabase().isThereBlockingBlocksNearby(
				block.getLocation(), event.getUniqueId(), plugin.getBlocksDatabase().getBreakRadius()
		);

		// If there is a blocking block nearby, cancel
		if (result.isProtected()) {
			Lang.PROTECTION_NEARBY_BLOCKS.sendActionBar(plugin, event.getPlayer());
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	private void onAttemptPlace(AttemptPlaceEvent event) {
		Block block = event.getBlock();
		// Avoid any check when not in a protected environment
		if (shouldIgnore(block, event.getPlayer())) {
			return;
		}

		// Check if there are protected blocks nearby
		ProtectionResult result = plugin.getBlocksDatabase().isThereBlockingBlocksNearby(
				block.getLocation(), event.getUniqueId(), plugin.getBlocksDatabase().getPlaceRadius()
		);

		// If there is a blocking block nearby, cancel
		if (result.isProtected()) {
			Lang.PROTECTION_NEARBY_BLOCKS.sendActionBar(plugin, event.getPlayer());
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	private void onBreak(BreakEvent event) {
		// Avoid any check when not in a protected environment
		if (shouldIgnore(event.getBlock(), null)) {
			return;
		}

		// Call protected event
		ProtectedBreakEvent protectedBreakEvent = new ProtectedBreakEvent(event.getBlock());
		plugin.getServer().getPluginManager().callEvent(protectedBreakEvent);
	}

	@EventHandler(ignoreCancelled = true)
	private void onPlace(PlaceEvent event) {
		// Avoid any check when not in a protected environment
		Player player = event.getPlayer();
		Block block = event.getBlock();
		if (shouldIgnore(block, player)) {
			return;
		}

		// Ignore events without a player
		if (player == null) {
			return;
		}

		// Check place material
		if (protectedMaterials.contains(block.getType())) {
			// Call protected event
			ProtectedPlaceEvent protectedPlaceEvent = new ProtectedPlaceEvent(player, block);
			plugin.getServer().getPluginManager().callEvent(protectedPlaceEvent);
		} else {
			// Show to player that this block can't be protected
			Lang.PROTECTION_BLOCK_WONT_BE_PROTECTED.sendActionBar(plugin, player);
		}
	}

}