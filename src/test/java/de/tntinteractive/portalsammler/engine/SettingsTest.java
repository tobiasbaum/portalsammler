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
