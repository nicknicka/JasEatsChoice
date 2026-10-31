package com.xx.jaseatschoicejava.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AiRuntimeKeyStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void writesAndReadsRestrictedRuntimeKeyFile() throws IOException {
        Path keyFile = tempDir.resolve("runtime/ai-active-key");
        AiRuntimeKeyStore store = new AiRuntimeKeyStore(keyFile.toString());

        store.writeKey(" runtime-key ");

        assertEquals("runtime-key", store.readKey());
        Set<PosixFilePermission> filePermissions = Files.getPosixFilePermissions(keyFile);
        Set<PosixFilePermission> directoryPermissions = Files.getPosixFilePermissions(keyFile.getParent());
        assertEquals(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE), filePermissions);
        assertEquals(Set.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE), directoryPermissions);
        try (var files = Files.list(keyFile.getParent())) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void missingOrUnconfiguredFileFallsBackToEmptyValue() throws IOException {
        assertEquals("", new AiRuntimeKeyStore("").readKey());
        assertEquals("", new AiRuntimeKeyStore(
            tempDir.resolve("missing/ai-active-key").toString()).readKey());
        assertFalse(Files.exists(tempDir.resolve("missing")));
    }

    @Test
    void deleteRemovesRuntimeKeyOnly() throws IOException {
        Path keyFile = tempDir.resolve("ai-active-key");
        AiRuntimeKeyStore store = new AiRuntimeKeyStore(keyFile.toString());
        store.writeKey("runtime-key");

        store.deleteKey();

        assertFalse(Files.exists(keyFile));
        assertTrue(Files.exists(keyFile.getParent()));
        assertEquals("", store.readKey());
        assertDoesNotThrow(store::deleteKey);
    }

    @Test
    void writeFailureDoesNotLeaveTemporaryFiles() throws IOException {
        Path keyFile = tempDir.resolve("ai-active-key");
        AiRuntimeKeyStore store = new AiRuntimeKeyStore(keyFile.toString());
        store.writeKey("old-key");
        Files.delete(keyFile);
        Files.createDirectory(keyFile);

        assertThrows(IllegalStateException.class, () -> store.writeKey("new-key"));

        try (var files = Files.list(tempDir)) {
            assertEquals(1, files.count());
        }
        assertTrue(Files.isDirectory(keyFile));
    }
}
