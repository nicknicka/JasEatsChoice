package com.xx.jaseatschoicejava.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSecretStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void directoryStoreWritesAndReadsRestrictedFilesByKey() throws IOException {
        Path directory = tempDir.resolve("secrets");
        FileSecretStore store = FileSecretStore.directory(directory.toString());

        store.write("user/1", " first-secret ");
        store.write("user/2", "second-secret");

        assertEquals("first-secret", store.read("user/1"));
        assertEquals("second-secret", store.read("user/2"));
        assertEquals(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE), Files.getPosixFilePermissions(directory));
        try (var files = Files.list(directory)) {
            assertEquals(2, files.count());
        }
    }

    @Test
    void directoryStoreDeletesOnlyRequestedKey() throws IOException {
        FileSecretStore store = FileSecretStore.directory(tempDir.toString());
        store.write("first", "first-secret");
        store.write("second", "second-secret");

        store.delete("first");

        assertEquals("", store.read("first"));
        assertEquals("second-secret", store.read("second"));
        assertTrue(Files.exists(tempDir));
    }

    @Test
    void fileStoreKeepsTheConfiguredPath() throws IOException {
        Path secretFile = tempDir.resolve("runtime/ai-active-key");
        FileSecretStore store = FileSecretStore.file(secretFile.toString());

        store.write("secret", "runtime-key");

        assertEquals("runtime-key", store.read("secret"));
        assertTrue(Files.isRegularFile(secretFile));
        assertEquals(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
            Files.getPosixFilePermissions(secretFile));
    }

    @Test
    void missingOrUnconfiguredValueFallsBackToEmpty() {
        assertEquals("", FileSecretStore.file("").read("secret"));
        assertEquals("", FileSecretStore.directory("").read("key"));
        assertEquals("", FileSecretStore.file(
            tempDir.resolve("missing/ai-active-key").toString()).read("secret"));
        assertFalse(Files.exists(tempDir.resolve("missing")));
    }

    @Test
    void atomicWriteNeverExposesPartialContent() throws Exception {
        Path secretFile = tempDir.resolve("active-key");
        FileSecretStore store = FileSecretStore.file(secretFile.toString());
        String oldValue = "old-" + "a".repeat(200_000);
        String newValue = "new-" + "b".repeat(200_000);
        store.write("secret", oldValue);

        AtomicBoolean writing = new AtomicBoolean(true);
        Thread writer = new Thread(() -> {
            for (int index = 0; index < 20; index++) {
                store.write("secret", index % 2 == 0 ? newValue : oldValue);
            }
            writing.set(false);
        });
        writer.start();
        try {
            while (writing.get()) {
                String value = store.read("secret");
                assertTrue(value.equals(oldValue) || value.equals(newValue));
            }
        } finally {
            writer.join();
        }
        String finalValue = store.read("secret");
        assertTrue(finalValue.equals(oldValue) || finalValue.equals(newValue));
    }

    @Test
    void secretContentIsNotWrittenToStandardStreams() throws IOException {
        FileSecretStore store = FileSecretStore.directory(tempDir.toString());
        String secret = "sensitive-session-cookie";
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        PrintStream originalErrors = System.err;
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(errors, true, StandardCharsets.UTF_8));
            store.write("user/1", secret);
            assertEquals(secret, store.read("user/1"));
        } finally {
            System.setOut(originalOutput);
            System.setErr(originalErrors);
        }
        assertFalse(output.toString(StandardCharsets.UTF_8).contains(secret));
        assertFalse(errors.toString(StandardCharsets.UTF_8).contains(secret));
    }

    @Test
    void writeFailureDoesNotLeaveTemporaryFiles() throws IOException {
        Path directory = tempDir.resolve("secrets");
        FileSecretStore store = FileSecretStore.directory(directory.toString());
        store.write("key", "old-secret");
        Path secretFile;
        try (var files = Files.list(directory)) {
            secretFile = files.findFirst().orElseThrow();
        }
        Files.delete(secretFile);
        Files.createDirectory(secretFile);

        assertThrows(IllegalStateException.class, () -> store.write("key", "new-secret"));

        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
        assertTrue(Files.isDirectory(secretFile));
    }
}
