package com.rafaelsms.blockprotection.util;

public enum ProtectionResult {

	PROTECTED,
	NOT_PROTECTED,
	DATABASE_FAILURE;

	public boolean isProtected() {
		return this == PROTECTED;
	}

}