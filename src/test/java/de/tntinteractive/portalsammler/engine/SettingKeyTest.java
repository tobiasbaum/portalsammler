package de.tntinteractive.portalsammler.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.tntinteractive.portalsammler.engine.SettingKey;


public class SettingKeyTest {

    @Test
    public void testHashCode() {
        final SettingKey a1 = new SettingKey("a");
        final SettingKey a2 = new SettingKey("a");
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    public void testEquals() {
        final SettingKey a1 = new SettingKey("a");
        final SettingKey a2 = new SettingKey("a");
        final SettingKey b = new SettingKey("b");

        assertTrue(a1.equals(a2));
        assertFalse(a1.equals(b));
        assertFalse(a2.equals(b));
    }

}
