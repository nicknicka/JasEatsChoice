package com.xx.jaseatschoicejava.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class XhsSessionStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void isolatesSessionsByUser() {
        XhsSessionStore store = new XhsSessionStore(tempDir.toString());

        store.write("user-a", "a1=1; web_session=a");
        store.write("user-b", "a1=2; web_session=b");

        assertEquals("a1=1; web_session=a", store.read("user-a"));
        assertEquals("a1=2; web_session=b", store.read("user-b"));
        assertTrue(store.exists("user-a"));
        assertTrue(store.exists("user-b"));
    }

    @Test
    void deleteRemovesOnlyRequestedUser() {
        XhsSessionStore store = new XhsSessionStore(tempDir.toString());
        store.write("user-a", "a1=1; web_session=a");
        store.write("user-b", "a1=2; web_session=b");

        store.delete("user-a");

        assertEquals("", store.read("user-a"));
        assertFalse(store.exists("user-a"));
        assertEquals("a1=2; web_session=b", store.read("user-b"));
        assertTrue(store.exists("user-b"));
    }

    @Test
    void notExpiredByDefault() {
        XhsSessionStore store = new XhsSessionStore(tempDir.toString());
        store.write("user-a", "a1=1; web_session=a");

        assertFalse(store.isExpired("user-a"));
    }

    @Test
    void writeAfterMarkExpiredClearsExpiredFlag() {
        XhsSessionStore store = new XhsSessionStore(tempDir.toString());
        store.write("user-a", "a1=1; web_session=a");

        store.markExpired("user-a");
        assertTrue(store.isExpired("user-a"));

        store.write("user-a", "a1=2; web_session=b");
        assertFalse(store.isExpired("user-a"));
        assertEquals("a1=2; web_session=b", store.read("user-a"));
    }

    @Test
    void deleteClearsExpiredFlag() {
        XhsSessionStore store = new XhsSessionStore(tempDir.toString());
        store.write("user-a", "a1=1; web_session=a");
        store.markExpired("user-a");

        store.delete("user-a");

        assertFalse(store.isExpired("user-a"));
        assertFalse(store.exists("user-a"));
    }

    @Test
    void markExpiredDoesNotAffectOtherUsers() {
        XhsSessionStore store = new XhsSessionStore(tempDir.toString());
        store.write("user-a", "a1=1; web_session=a");
        store.write("user-b", "a1=2; web_session=b");

        store.markExpired("user-a");

        assertTrue(store.isExpired("user-a"));
        assertFalse(store.isExpired("user-b"));
        assertEquals("a1=2; web_session=b", store.read("user-b"));
    }

    @Test
    void updatedAtIsNullWhenUnbound() {
        XhsSessionStore store = new XhsSessionStore(tempDir.toString());

        assertNull(store.updatedAt("user-a"));
        assertFalse(store.exists("user-a"));
    }

    @Test
    void unconfiguredDirectoryReadsEmpty() {
        XhsSessionStore store = new XhsSessionStore("");

        assertEquals("", store.read("user-a"));
        assertFalse(store.exists("user-a"));
        assertNull(store.updatedAt("user-a"));
    }
}
