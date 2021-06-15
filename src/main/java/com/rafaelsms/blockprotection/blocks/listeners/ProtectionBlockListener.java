package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Config;
import com.rafaelsms.blockprotection.Lang;
import com.rafaelsms.blockprotection.Permission;
import com.rafaelsms.blockprotection.blocks.events.*;
import com.rafaelsms.blockprotection.util.Listener;
import com.rafaelsms.blockprotection.util.ProtectedBlock;
import com.rafaelsms.blockprotection.util.ProtectedBlockOwner;
import com.rafaelsms.blockprotection.util.ProtectionQuery;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
		plugin.getLogger().info("%d materials are going to be protected".formatted(this.protectedMaterials.size()));
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

	private void sendPlayerMessage(Player player, ProtectionQuery result) {
		// Check if there is a player
		if (player == null) {
			return;
		}

		// Check if there is a owner
		if (result.getOwner() != null) {
			OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(result.getOwner());

			// Check if owner has a name on the server
			if (offlinePlayer.getName() != null) {
				String blockProtectedMessage = Lang.PROTECTION_NEARBY_BLOCKS_OWNED.toString(plugin)
						                               .replaceAll("\\{PLAYER}", offlinePlayer.getName());
				player.sendActionBar(Lang.parseLegacyText(blockProtectedMessage));
				return;
			}
		}

		// Otherwise, send default message
		Lang.PROTECTION_NEARBY_BLOCKS.sendActionBar(plugin, player);
	}

	@EventHandler(ignoreCancelled = false)
	private void onDebugInteract(AttemptInteractEvent event) {
		// Check main item in hand
		ItemStack mainHand = event.getEvent().getItem();
		if (mainHand == null || mainHand.getType() != debugItem) {
			return;
		}

		// Check if there is a player
		Player player = event.getPlayer();
		if (player == null || !player.hasPermission(Permission.DEBUG.toString())) {
			return;
		}

		Action action = event.getEvent().getAction();

		// On left click, get single block data
		if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
			// Get block data from database
			ProtectedBlockOwner blockData = plugin.getBlocksDatabase().getBlockData(event.getBlock().getLocation());
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

		// On right click, mimic attempt place event and check nearby blocks
		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
			List<ProtectedBlock> protectedBlocks = plugin.getBlocksDatabase().getDistinctOwnersProtectedBlocks(
					event.getBlock().getLocation(), plugin.getBlocksDatabase().getPlaceRadius());

			if (protectedBlocks.isEmpty()) {
				Lang.PROTECTION_DEBUG_LIST_EMPTY.sendMessage(plugin, player);
				return;
			}

			String blockString = ProtectedBlock.toString(
					event.getBlock().getX(),
					event.getBlock().getY(),
					event.getBlock().getZ()
			);
			player.sendMessage(Lang.parseLegacyText(Lang.PROTECTION_DEBUG_LIST_TITLE.toString(plugin)
					                                        .formatted(blockString)));
			for (ProtectedBlock protectedBlock : protectedBlocks) {
				player.sendMessage(Lang.parseLegacyText(Lang.PROTECTION_DEBUG_LIST_TEXT.toString(plugin)
						                                        .formatted(protectedBlock.toString(plugin))));
			}
		}

		// Prevent any accidental change to the block
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	private void onAttemptInteract(AttemptInteractEvent event) {
		Block block = event.getBlock();

		// Avoid any check when not in a protected environment
		// Interactions shouldn't warn the player
		if (shouldIgnore(block, null)) {
			return;
		}

		// Check if material is allowed to interact
		boolean isInventoryHolder = event.getBlock().getState() instanceof InventoryHolder;
		if (materialsAllowedInteraction.contains(block.getType()) && !isInventoryHolder) {
			// Ignore event (allow interaction)
			return;
		}

		// Check if is inventory holder
		if (isInventoryHolder && event.getPlayer() != null) {
			// Get line of sight of player
			for (Block next : event.getPlayer().getLineOfSight(null, 5)) {
				// Skip empty or liquid
				if (next.isEmpty() || next.isLiquid()) {
					continue;
				}

				// Check if next block is the inventory holder
				if (next.getBlockKey() != block.getBlockKey()) {
					// If it doesn't, cancel the event and return
					event.setCancelled(true);
					return;
				}
			}

			// If it wasn't cancelled, allow
			return;
		}

		// Ignore admin permission to override block interaction
		if (event.getPlayer() != null && event.getPlayer().hasPermission(Permission.PROTECTION_OVERRIDE.toString())) {
			return;
		}

		// Check if there are protected blocks nearby
		ProtectionQuery result = plugin.getBlocksDatabase().isThereBlockingBlocksNearby(
				block.getLocation(), event.getUniqueId(), plugin.getBlocksDatabase().getInteractRadius()
		);

		// Check if it is protected
		if (result.isProtected()) {
			// Cancel the event
			event.setCancelled(true);
			// Send player message
			sendPlayerMessage(event.getPlayer(), result);
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
		ProtectionQuery result = plugin.getBlocksDatabase().isThereBlockingBlocksNearby(
				block.getLocation(), event.getUniqueId(), plugin.getBlocksDatabase().getBreakRadius());

		// Check if it is protected
		if (result.isProtected()) {
			// Cancel the event
			event.setCancelled(true);
			// Send player message
			sendPlayerMessage(event.getPlayer(), result);
		}
		// If there isn't, allow break
	}

	@EventHandler(ignoreCancelled = true)
	private void onAttemptPlace(AttemptPlaceEvent event) {
		Block block = event.getBlock();
		// Avoid any check when not in a protected environment
		if (shouldIgnore(block, event.getPlayer())) {
			return;
		}

		// Check if there are protected blocks nearby
		ProtectionQuery result = plugin.getBlocksDatabase().isThereBlockingBlocksNearby(
				block.getLocation(), event.getUniqueId(), plugin.getBlocksDatabase().getPlaceRadius()
		);

		// Check if it is protected
		if (result.isProtected()) {
			// Cancel the event
			event.setCancelled(true);
			// Send player message
			sendPlayerMessage(event.getPlayer(), result);
		}
		// If there isn't, allow place
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