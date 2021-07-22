package com.rafaelsms.blockprotection.blocks.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class DoorListener implements Listener {

    private final static int SOUND_RADIUS_LOCATION = 16;

    private boolean doorConditions(PlayerInteractEvent event) {
        return // Ignore denied events
                event.useInteractedBlock() != Event.Result.DENY &&
                        // Ignore action not being right click
                        event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                        // Ignore player sneaking
                        !event.getPlayer().isSneaking() &&
                        // Ignore block being null
                        event.getClickedBlock() != null &&
                        // Ignore material other than iron door/trap door
                        (event.getClickedBlock().getType() == Material.IRON_DOOR ||
                                event.getClickedBlock().getType() == Material.IRON_TRAPDOOR) &&
                        // Ignore block not being instance of door
                        (event.getClickedBlock().getBlockData() instanceof Door ||
                                event.getClickedBlock().getBlockData() instanceof TrapDoor);
    }

    private Sound getSoundFromDoor(Openable door) {
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
            if (blockData instanceof Door door) {
                door.setOpen(!door.isOpen());
                clickedBlock.setBlockData(door);
            } else if (blockData instanceof TrapDoor trapDoor) {
                trapDoor.setOpen(!trapDoor.isOpen());
                clickedBlock.setBlockData(trapDoor);
            }

            // Send door sound to anyone nearby
            double radiusSquared = SOUND_RADIUS_LOCATION * SOUND_RADIUS_LOCATION;
            for (Player player : clickedBlock.getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(clickedBlock.getLocation()) <= radiusSquared) {
                    player.playSound(
                            clickedBlock.getLocation(),
                            getSoundFromDoor((Openable) blockData),
                            SoundCategory.PLAYERS,
                            1.0f,
                            1.0f
                    );
                }
            }
        }
    }
}
