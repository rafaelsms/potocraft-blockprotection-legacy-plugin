package com.rafaelsms.blockprotection.blocks.listeners;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.blockprotection.util.Listener;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class DoorListener extends Listener {

    private final static int RADIUS_LOCATION = 16;

    public DoorListener(BlockProtectionPlugin plugin) {
        super(plugin);
    }

    private boolean doorConditions(PlayerInteractEvent event) {
        // Check if we can interact with the block
        if (event.useInteractedBlock() == Event.Result.DENY) {
            return false;
        }

        // Check if we right clicked
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return false;
        }

        // Check if player is sneaking
        if (event.getPlayer().isSneaking()) {
            return false;
        }

        // Check if clicked block is null
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return false;
        }

        // Check if block is a door
        if (clickedBlock.getType() == Material.IRON_DOOR || clickedBlock.getType() == Material.IRON_TRAPDOOR) {
            BlockData blockData = clickedBlock.getBlockData();

            // Check if it is a door
            return blockData instanceof Door;
        }
        return false;
    }

    private Sound getSoundFromDoor(Door door) {
        if (door.getMaterial() == Material.IRON_TRAPDOOR) {
            if (door.isOpen()) {
                return Sound.BLOCK_IRON_TRAPDOOR_OPEN;
            }
            return Sound.BLOCK_IRON_TRAPDOOR_CLOSE;
        } else {
            if (door.isOpen()) {
                return Sound.BLOCK_IRON_DOOR_OPEN;
            }
            return Sound.BLOCK_IRON_DOOR_CLOSE;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onInteract(PlayerInteractEvent event) {
        if (doorConditions(event)) {
            // For both hands, deny using the item in hand
            event.setUseItemInHand(Event.Result.DENY);

            if (event.getHand() == EquipmentSlot.HAND) {
                // For the main hand, allow it explicitly
                event.setUseInteractedBlock(Event.Result.ALLOW);
            } else {
                // For the offhand, cancel the event entirely
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onInteractMonitor(PlayerInteractEvent event) {
        if (doorConditions(event)) {

            // Check if we are not interacting with our main hand (or we will open and close the door at the same time)
            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }

            Block clickedBlock = event.getClickedBlock();
            @SuppressWarnings("ConstantConditions") BlockData blockData = clickedBlock.getBlockData();

            // Source: https://github.com/JEFF-Media-GbR/Doors-Reloaded/blob/master/src/main/java/de/jeff_media/doorsreloaded/listeners/DoorListener.java#L54
            // Update the door to set its new state
            Door door = (Door) blockData;
            door.setOpen(!door.isOpen());
            clickedBlock.setBlockData(door);

            // Send door sound to anyone nearby
            double radiusSquared = RADIUS_LOCATION * RADIUS_LOCATION;
            for (Player player : clickedBlock.getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(clickedBlock.getLocation()) <= radiusSquared) {
                    player.playSound(
                            clickedBlock.getLocation(),
                            getSoundFromDoor(door),
                            SoundCategory.PLAYERS,
                            1.0f,
                            1.0f
                    );
                }
            }
        }
    }
}