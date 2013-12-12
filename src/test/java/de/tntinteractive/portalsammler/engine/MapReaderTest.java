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
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public final class MapReaderTest {

    private static MapReader createReader(final String input) throws Exception {
        return MapReader.createFrom(new ByteArrayInputStream(input.getBytes("UTF-8")));
    }

    @Test
    public void testReadEmptyMap() throws Exception {
        final String input =
                "test123\n"
                + ".";
        final MapReader r = createReader(input);
        final Pair<String, Map<String, String>> p = r.readNext();
        assertEquals("test123", p.getLeft());
        assertEquals(Collections.emptyMap(), p.getRight());
        assertNull(r.readNext());
    }

    @Test
    public void testSingleEntryMap() throws Exception {
        final String input =
                "test123\n"
                + " e1\n"
                + " w1\n"
                + ".";
        final MapReader r = createReader(input);
        final Pair<String, Map<String, String>> p = r.readNext();
        assertEquals("test123", p.getLeft());
        assertEquals(Collections.singletonMap("e1", "w1"), p.getRight());
        assertNull(r.readNext());
    }

    @Test
    public void testTwoEntryMap() throws Exception {
        final String input =
                "test123\n"
                + " e1\n"
                + " w1\n"
                + " e2\n"
                + " w2\n"
                + ".";
        final MapReader r = createReader(input);
        final Pair<String, Map<String, String>> p = r.readNext();
        assertEquals("test123", p.getLeft());
        final Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("e1", "w1");
        map.put("e2", "w2");
        assertEquals(map, p.getRight());
        assertNull(r.readNext());
    }

    @Test
    public void testMultipleMaps() throws Exception {
        final String input =
                "test123\n"
                + " ä\n"
                + " ö\n"
                + ".\n"
                + "test456\n"
                + " ü\n"
                + " ß\n"
                + ".";
        final MapReader r = createReader(input);
        final Pair<String, Map<String, String>> p1 = r.readNext();
        assertEquals("test123", p1.getLeft());
        assertEquals(Collections.singletonMap("ä", "ö"), p1.getRight());
        final Pair<String, Map<String, String>> p2 = r.readNext();
        assertEquals("test456", p2.getLeft());
        assertEquals(Collections.singletonMap("ü", "ß"), p2.getRight());
        assertNull(r.readNext());
    }


}
