package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.Config;
import com.rafaelsms.blockprotection.Lang;
import com.rafaelsms.blockprotection.Permission;
import com.rafaelsms.blockprotection.blocks.events.*;
import com.rafaelsms.blockprotection.util.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ProtectionBlockListener implements Listener {

    private final BlockProtectionPlugin plugin;

    // Configuration
    private final Set<UUID> protectedWorlds;

    private final Set<Material> materialsHandAllowed;
    private final Set<Material> materialsBlockDenied;
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
        this.plugin = plugin;

        // Get configuration
        minimumProtectedHeight = Config.PROTECTION_MINIMUM_HEIGHT.getInt();
        debugItem = Material.valueOf(Config.PROTECTION_DEBUG_ITEM.getString());

        // Check which worlds exists
        HashSet<UUID> protectedWorlds = new HashSet<>();
        for (String worldName : Config.PROTECTION_PROTECTED_WORLDS.getStringList()) {
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

        // Check interaction materials
        this.materialsBlockDenied = getMaterialsFromList(
                Config.PROTECTION_MATERIALS_BLOCKS_DENIED_INTERACTION.getStringList());
        plugin.getLogger().info(
                "%d block materials that cannot be interacted with".formatted(this.materialsBlockDenied.size()));

        this.materialsHandAllowed = getMaterialsFromList(
                Config.PROTECTION_MATERIALS_HAND_ALLOWED_INTERACTION.getStringList());
        plugin.getLogger().info(
                "%d hand materials that can be interacted by anyone".formatted(this.materialsHandAllowed.size()));

        // Check protected materials
        this.protectedMaterials = getMaterialsFromList(Config.PROTECTION_MATERIALS_PROTECTED.getStringList());
        plugin.getLogger().info("%d materials are going to be protected".formatted(this.protectedMaterials.size()));
    }

    private Set<Material> getMaterialsFromList(List<String> materialsNames) {
        HashSet<Material> materials = new HashSet<>();
        for (String material : materialsNames) {
            try {
                materials.add(Material.valueOf(material));
            } catch (Exception exception) {
                plugin.getLogger().info("Couldn't recognize material: %s".formatted(material));
            }
        }
        return Collections.unmodifiableSet(materials);
    }

    private boolean shouldIgnore(Block block, @Nullable Player player) {
        // Check if is protected world first
        // (otherwise we will get a "under protection height" warning at a unprotected world)
        if (!protectedWorlds.contains(block.getWorld().getUID())) {
            Lang.PROTECTION_UNPROTECTED_WORLD.sendActionBar(player);
            return true;
        }

        // Check height
        if (block.getY() < minimumProtectedHeight) {
            Lang.PROTECTION_UNDER_MINIMUM_HEIGHT.sendActionBar(player);
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
                BaseComponent[] blockProtectedMessage = TextComponent.fromLegacyText(
                        Lang.PROTECTION_NEARBY_BLOCKS_OWNED.toColoredString()
                                .replaceAll("\\{PLAYER}", offlinePlayer.getName()));
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, blockProtectedMessage);
                return;
            }
        }

        // Otherwise, send default message
        Lang.PROTECTION_NEARBY_BLOCKS.sendActionBar(player);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    private void onDebugInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        // Filter null blocks
        if (block == null) {
            return;
        }

        // Check item in main hand
        ItemStack mainHand = event.getItem();
        if (mainHand == null || mainHand.getType() != debugItem) {
            return;
        }

        // Check if player has permission
        if (!player.hasPermission(Permission.DEBUG.toString())) {
            return;
        }

        Action action = event.getAction();

        // On left click, get single block data
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            // Get block data from database
            ProtectedBlockDate blockData = plugin.getBlocksDatabase().getBlockData(block.getLocation());
            if (blockData != null) {
                if (blockData.isNotRegistered()) {
                    Lang.PROTECTION_DEBUG_NO_BLOCK.sendMessage(player);
                } else {
                    player.sendMessage(Lang.PROTECTION_DEBUG_TEXT.toColoredString().formatted(
                            blockData.printOfflinePlayer(),
                            blockData.printDate(),
                            blockData.isTemporaryBlock(),
                            blockData.isValid()
                    ));
                }
            } else {
                Lang.PROTECTION_DEBUG_DATABASE_FAILURE.sendMessage(player);
            }
        }

        // On right click, mimic attempt place event and check nearby blocks
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            List<ProtectedBlock> protectedBlocks = plugin.getBlocksDatabase().getDistinctOwnersProtectedBlocks(
                    block.getLocation(), plugin.getBlocksDatabase().getPlaceRadius());

            if (protectedBlocks.isEmpty()) {
                Lang.PROTECTION_DEBUG_LIST_EMPTY.sendMessage(player);
                return;
            }

            String blockString = ProtectedBlock.toString(block.getX(), block.getY(), block.getZ());
            player.sendMessage(Lang.PROTECTION_DEBUG_LIST_TITLE.toColoredString().formatted(blockString));
            for (ProtectedBlock protectedBlock : protectedBlocks) {
                player.sendMessage(Lang.PROTECTION_DEBUG_LIST_TEXT.toColoredString()
                        .formatted(protectedBlock.toString(plugin)));
            }
        }

        // Prevent any accidental change to the block
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
    }

    @EventHandler(ignoreCancelled = true)
    private void onAttemptBlockInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        // Ignore when interacting with no blocks
        if (block == null) {
            return;
        }

        // Avoid any check when not in a protected environment
        if (shouldIgnore(block, null)) {
            return;
        }

        // Ignore materials other than what is denied
        if (!materialsBlockDenied.contains(block.getType())) {
            return;
        }

        // Ignore admin permission to override block interaction
        if (player.hasPermission(Permission.PROTECTION_OVERRIDE.toString())) {
            return;
        }

        // Since this includes a denied material, check permissions
        ProtectionQuery result = plugin.getBlocksDatabase().isThereBlockingBlocksNearby(
                block.getLocation(), event.getPlayer().getUniqueId(), plugin.getBlocksDatabase().getInteractRadius()
        );

        // Check if it is protected
        if (result.isProtected()) {
            // Cancel the event
            event.setUseInteractedBlock(Event.Result.DENY);
            // Send player message
            sendPlayerMessage(player, result);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onAttemptItemInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Ignore when interacting with blocks
        if (event.getClickedBlock() != null) {
            return;
        }

        // Avoid any check when not in a protected environment
        if (shouldIgnore(event.getPlayer().getLocation().getBlock(), null)) {
            return;
        }

        // Ignore allowed interactions
        if (event.getItem() != null && materialsHandAllowed.contains(event.getItem().getType())) {
            return;
        }

        // Ignore admin permission to override block interaction
        if (player.hasPermission(Permission.PROTECTION_OVERRIDE.toString())) {
            return;
        }

        // Since this includes a denied material, check permissions
        ProtectionQuery result = plugin.getBlocksDatabase().isThereBlockingBlocksNearby(
                event.getPlayer().getLocation(), event.getPlayer().getUniqueId(),
                plugin.getBlocksDatabase().getInteractRadius()
        );

        // Check if it is protected
        if (result.isProtected()) {
            // Cancel the event
            event.setUseItemInHand(Event.Result.DENY);
            // Send player message
            sendPlayerMessage(player, result);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onOpenInventoryHolder(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        // Ignore when interacting with no blocks
        if (block == null) {
            return;
        }

        // Avoid any check when not in a protected environment
        if (shouldIgnore(block, null)) {
            return;
        }

        // Check if is inventory holder
        if (block.getState() instanceof InventoryHolder) {
            // Get line of sight of player
            for (Block next : player.getLineOfSight(null, 5)) {
                // Skip empty or liquid
                if (next.isEmpty() || next.isLiquid()) {
                    continue;
                }

                // Check if next block is the inventory holder
                if (!BlockKey.fromBlock(block).equals(BlockKey.fromBlock(next))) {
                    // If it doesn't, cancel the event and return
                    event.setCancelled(true);
                    event.setUseInteractedBlock(Event.Result.DENY);
                    event.setUseItemInHand(Event.Result.DENY);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onTeleport(PlayerTeleportEvent event) {
        // Filter chorus fruit
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT &&
                event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        // Ignore null destinations
        if (event.getTo() == null) {
            return;
        }

        // Check if new location is near protected area
        ProtectionQuery result = plugin.getBlocksDatabase().isThereBlockingBlocksNearby(
                event.getTo(), event.getPlayer().getUniqueId(), plugin.getBlocksDatabase().getPlaceRadius());

        // Check if it is protected
        if (result.isProtected()) {
            // Cancel the event
            event.setCancelled(true);
            // Send player message
            sendPlayerMessage(event.getPlayer(), result);
        }
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
                block.getLocation(), event.getPlayerUUID(), plugin.getBlocksDatabase().getBreakRadius());

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
                block.getLocation(), event.getPlayerUUID(), plugin.getBlocksDatabase().getPlaceRadius()
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

        // Check place material
        if (!protectedMaterials.contains(block.getType())) {
            // Show to player that this block can't be protected
            Lang.PROTECTION_BLOCK_NOT_PROTECTED_MATERIAL.sendActionBar(player);
            return;
        }

        // Call protected event
        ProtectedPlaceEvent protectedPlaceEvent = new ProtectedPlaceEvent(block, player);
        plugin.getServer().getPluginManager().callEvent(protectedPlaceEvent);
    }

}
