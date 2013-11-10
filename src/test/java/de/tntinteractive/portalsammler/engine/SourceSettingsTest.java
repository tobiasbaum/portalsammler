package de.tntinteractive.portalsammler.engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tntinteractive.portalsammler.engine.SettingKey;
import de.tntinteractive.portalsammler.engine.SourceSettings;

public class SourceSettingsTest {

    @Test
    public void testStoreAndRetrieveValue() {
        final SourceSettings s = new SourceSettings();
        final SettingKey key = new SettingKey("a");
        s.set(key, "123");
        assertEquals("123", s.get(key));
    }

    @Test
    public void testStoreAndRetrieveTwoValues() {
        final SourceSettings s = new SourceSettings();
        final SettingKey key1 = new SettingKey("a");
        final SettingKey key2 = new SettingKey("b");
        s.set(key1, "123");
        s.set(key2, "abc");
        assertEquals("123", s.get(key1));
        assertEquals("abc", s.get(key2));
    }

    @Test
    public void testSetMultipleTimes() {
        final SourceSettings s = new SourceSettings();
        final SettingKey key = new SettingKey("a");
        s.set(key, "123");
        s.set(key, "456");
        assertEquals("456", s.get(key));
    }

}
