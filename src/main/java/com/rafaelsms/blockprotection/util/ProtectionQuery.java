package com.rafaelsms.blockprotection.util;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ProtectionQuery {

	private final Result result;

	private UUID owner = null;

	public ProtectionQuery(Result result) {
		this.result = result;
	}

	public ProtectionQuery(UUID owner) {
		this.result = Result.PROTECTED;
		this.owner = owner;
	}

	public boolean isProtected() {
		return result == Result.PROTECTED;
	}

	public @Nullable UUID getOwner() {
		return owner;
	}

	public enum Result {
		PROTECTED,
		NOT_PROTECTED,
		DATABASE_FAILURE
	}
}