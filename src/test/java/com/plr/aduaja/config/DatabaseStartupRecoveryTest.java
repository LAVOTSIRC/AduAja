package com.plr.aduaja.config;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseStartupRecoveryTest {

	@Test
	void detectsH2CorruptionByErrorCode() {
		SQLException exception = new SQLException("File corrupted while reading record", "90030", 90030);

		assertTrue(DatabaseStartupRecovery.isCorruptionError(exception));
	}

	@Test
	void ignoresUnrelatedSqlException() {
		SQLException exception = new SQLException("Connection refused", "08001", 0);

		assertFalse(DatabaseStartupRecovery.isCorruptionError(exception));
	}
}

