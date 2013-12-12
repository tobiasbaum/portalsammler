package de.tntinteractive.portalsammler.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class SettingsTest {

    @Test
    public void testAddSetting() {
        final Settings s = new Settings();
        final SourceSettings set = new SourceSettings();
        s.putSettings("asdf", set);
        assertSame(set, s.getSettings("asdf"));
    }

    @Test
    public void testRemoveSetting() {
        final Settings s = new Settings();
        final SourceSettings set = new SourceSettings();
        s.putSettings("asdf", set);
        s.removeSettings("asdf");
        assertNull(s.getSettings("asdf"));
    }

    @Test
    public void testDeepCloneReallyDoesCopy() {
        final Settings s = new Settings();
        final SettingKey key = new SettingKey("lkj");
        final SourceSettings set = new SourceSettings();
        set.set(key, "123");
        s.putSettings("asdf", set);
        final Settings clone = s.deepClone();
        assertNotSame(set, clone.getSettings("asdf"));
        assertEquals("123", clone.getSettings("asdf").get(key, null));
    }

}
