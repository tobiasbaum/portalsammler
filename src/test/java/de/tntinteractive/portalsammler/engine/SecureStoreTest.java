/*
    Copyright (C) 2013  Tobias Baum <tbaum at tntinteractive.de>

    This file is a part of Portalsammler.

    Portalsammler is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Portalsammler is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Portalsammler.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tntinteractive.portalsammler.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.SecureRandom;
import java.util.Arrays;

import org.junit.Test;


public class SecureStoreTest {

    private static final SettingKey USER = new SettingKey("user");
    private static final SettingKey PASSWORD = new SettingKey("password");

    @Test
    public void testReadAndWriteSourceSettings() throws Exception {
        final SecureRandom srand = new InsecureRandom();
        final byte[] key = CryptoHelper.generateKey(srand);
        final StorageLayer stubStorage = new StubStorage();

        final SecureStore s1 = SecureStore.createEmpty(stubStorage, srand, key);
        final SourceSettings settings1 = new SourceSettings();
        settings1.set(USER, "123");
        settings1.set(PASSWORD, "abc");
        s1.getSettings().putSettings("diba", settings1);
        final SourceSettings settings2 = new SourceSettings();
        settings2.set(USER, "456");
        settings2.set(PASSWORD, "789");
        s1.getSettings().putSettings("hanvb", settings2);

        s1.writeMetadata();

        final SecureStore s2 = SecureStore.readFrom(stubStorage, srand, key);

        assertEquals(settings1, s2.getSettings().getSettings("diba"));
        assertEquals(settings2, s2.getSettings().getSettings("hanvb"));
    }

    @Test
    public void testStoreDocument() throws Exception {
        final SecureRandom srand = new InsecureRandom();
        final byte[] key = CryptoHelper.generateKey(srand);
        final StorageLayer stubStorage = new StubStorage();

        final SecureStore s = SecureStore.createEmpty(stubStorage, srand, key);

        final DocumentInfo metadata = DocumentInfo.create("id1", DocumentFormat.PDF);
        final byte[] content = new byte[] {1, 2, 3, 4, 5};
        s.storeDocument(metadata, content);

        assertTrue(s.containsDocument(metadata));
        assertTrue(Arrays.equals(content, s.getDocument(metadata)));
        assertFalse(s.isRead(metadata));
    }

    @Test
    public void testStoreTwoDocuments() throws Exception {
        final SecureRandom srand = new InsecureRandom();
        final byte[] key = CryptoHelper.generateKey(srand);
        final StorageLayer stubStorage = new StubStorage();

        final SecureStore s = SecureStore.createEmpty(stubStorage, srand, key);

        final DocumentInfo metadata1 = DocumentInfo.create("id1", DocumentFormat.PDF);
        final byte[] content1 = new byte[] {1, 2, 3, 4, 5};
        s.storeDocument(metadata1, content1);

        final DocumentInfo metadata2 = DocumentInfo.create("id2", DocumentFormat.PDF);
        final byte[] content2 = new byte[] {7, 8, 9, 10};
        s.storeDocument(metadata2, content2);

        final DocumentInfo metadata3 = DocumentInfo.create("id3", DocumentFormat.PDF);

        assertTrue(s.containsDocument(metadata1));
        assertTrue(Arrays.equals(content1, s.getDocument(metadata1)));

        assertTrue(s.containsDocument(metadata2));
        assertTrue(Arrays.equals(content2, s.getDocument(metadata2)));

        assertFalse(s.containsDocument(metadata3));
    }

    @Test
    public void testStoreTwoDocumentsWithReopen() throws Exception {
        final SecureRandom srand = new InsecureRandom();
        final byte[] key = CryptoHelper.generateKey(srand);
        final StorageLayer stubStorage = new StubStorage();

        final SecureStore s1 = SecureStore.createEmpty(stubStorage, srand, key);

        final DocumentInfo metadata1 = DocumentInfo.create("id1", DocumentFormat.PDF);
        final byte[] content1 = new byte[] {1, 2, 3, 4, 5};
        s1.storeDocument(metadata1, content1);
        s1.writeMetadata();

        final SecureStore s2 = SecureStore.readFrom(stubStorage, srand, key);

        final DocumentInfo metadata2 = DocumentInfo.create("id2", DocumentFormat.PDF);
        final byte[] content2 = new byte[] {7, 8, 9, 10};
        s2.storeDocument(metadata2, content2);

        assertTrue(s2.containsDocument(metadata1));
        assertTrue(Arrays.equals(content1, s2.getDocument(metadata1)));

        assertTrue(s2.containsDocument(metadata2));
        assertTrue(Arrays.equals(content2, s2.getDocument(metadata2)));
    }

    @Test
    public void testMarkAsRead() throws Exception {
        final SecureRandom srand = new InsecureRandom();
        final byte[] key = CryptoHelper.generateKey(srand);
        final SecureStore s = SecureStore.createEmpty(new StubStorage(), srand, key);

        final DocumentInfo metadata = DocumentInfo.create("id1", DocumentFormat.PDF);
        s.storeDocument(metadata, new byte[] {1, 2, 3, 4, 5});

        s.markAsRead(metadata);
        assertTrue(s.isRead(metadata));
    }


    @Test
    public void testStoreReadAndUnreadIsKeptAfterReopen() throws Exception {
        final SecureRandom srand = new InsecureRandom();
        final byte[] key = CryptoHelper.generateKey(srand);
        final StorageLayer stubStorage = new StubStorage();

        final SecureStore s1 = SecureStore.createEmpty(stubStorage, srand, key);

        final DocumentInfo metadata1 = DocumentInfo.create("id1", DocumentFormat.PDF);
        s1.storeDocument(metadata1, new byte[] {1, 2, 3, 4, 5});
        s1.writeMetadata();
        final DocumentInfo metadata2 = DocumentInfo.create("id2", DocumentFormat.PDF);
        s1.storeDocument(metadata2, new byte[] {7, 8, 9, 10});
        s1.markAsRead(metadata2);

        final SecureStore s2 = SecureStore.readFrom(stubStorage, srand, key);

        assertFalse(s2.isRead(metadata1));
        assertTrue(s2.isRead(metadata2));
    }

    @Test
    public void testRecrypt() throws Exception {
        final SecureRandom srand = new InsecureRandom();
        final byte[] key1 = CryptoHelper.generateKey(srand);
        final StorageLayer stubStorage = new StubStorage();

        final SecureStore s1 = SecureStore.createEmpty(stubStorage, srand, key1);
        final DocumentInfo metadata1 = DocumentInfo.create("id1", DocumentFormat.PDF);
        s1.storeDocument(metadata1, new byte[] {1, 2, 3, 4, 5});
        s1.writeMetadata();

        final byte[] key2 = CryptoHelper.generateKey(srand);
        final SecureStore s2 = s1.recrypt(key2);
        assertTrue(Arrays.equals(new byte[] {1, 2, 3, 4, 5}, s2.getDocument(metadata1)));
    }
}
