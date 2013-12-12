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

import org.junit.Test;


public final class SettingKeyTest {

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
