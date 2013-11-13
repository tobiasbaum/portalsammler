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

import org.junit.Test;

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
