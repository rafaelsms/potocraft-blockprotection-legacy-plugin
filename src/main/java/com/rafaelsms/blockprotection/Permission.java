package com.rafaelsms.blockprotection;

public enum Permission {

	DEBUG("blockprotection.debug"),
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