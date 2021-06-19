package com.rafaelsms.blockprotection.util;

import org.bukkit.block.Block;

import java.util.Objects;
import java.util.UUID;

public class BlockKey {

    private final UUID world;
    private final int x;
    private final int y;
    private final int z;

    private BlockKey(Block block) {
        this.world = block.getWorld().getUID();
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
    }

    public static BlockKey fromBlock(Block block) {
        return new BlockKey(block);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockKey blockKey = (BlockKey) o;
        return x == blockKey.x && y == blockKey.y && z == blockKey.z && Objects.equals(world, blockKey.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }
}
