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

}
