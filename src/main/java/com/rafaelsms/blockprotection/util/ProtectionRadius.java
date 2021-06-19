package com.rafaelsms.blockprotection.util;

public class ProtectionRadius {

    private final int blockRadius;
    private final int chunkRadius;

    public ProtectionRadius(int blockRadius) {
        this.blockRadius = blockRadius;
        chunkRadius = ((int) Math.ceil(blockRadius / 16.0)) + 1;
    }

    public int getBlockRadius() {
        return blockRadius;
    }

    public int getChunkRadius() {
        return chunkRadius;
    }
}
