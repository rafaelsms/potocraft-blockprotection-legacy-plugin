package com.rafaelsms.blockprotection.util;

import com.rafaelsms.blockprotection.BlockProtectionPlugin;

public abstract class Listener implements org.bukkit.event.Listener {

    protected final BlockProtectionPlugin plugin;

    public Listener(BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }
}
