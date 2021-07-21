package com.rafaelsms.blockprotection;

public enum Permission {

    PROTECT("blockprotection.protect"),
    DEBUG("blockprotection.debug"),
    PROTECTION_OVERRIDE("blockprotection.protection_override"),
    ;

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    @Override
    public String toString() {
        return permission;
    }
}
