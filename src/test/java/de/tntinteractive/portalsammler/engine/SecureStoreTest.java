package de.tntinteractive.portalsammler.engine;

import static org.junit.Assert.assertEquals;

import java.security.SecureRandom;

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

}
