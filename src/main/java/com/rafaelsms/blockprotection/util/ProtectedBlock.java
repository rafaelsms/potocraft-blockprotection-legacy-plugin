package com.rafaelsms.blockprotection.util;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;

import java.util.UUID;

@SuppressWarnings("unused")
public class ProtectedBlock {

	private final UUID owner;
	private final UUID world;
	private final int x;
	private final int y;
	private final int z;

	public ProtectedBlock(UUID owner, UUID world, int x, int y, int z) {
		this.owner = owner;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public UUID getOwner() {
		return owner;
	}

	public UUID getWorld() {
		return world;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public static String toString(int x, int y, int z) {
		return "x,y,z = (%d, %d, %d)".formatted(x, y, z);
	}

	public String toString(BlockProtectionPlugin plugin) {
		return "owner = %s, %s".formatted(
				String.valueOf(plugin.getServer().getOfflinePlayer(owner).getName()), toString(x, y, z));
	}
}
