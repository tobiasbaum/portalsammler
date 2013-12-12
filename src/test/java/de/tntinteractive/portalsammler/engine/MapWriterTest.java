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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public final class MapWriterTest {

    private static MapWriter createWriter(final OutputStream buffer) {
        return MapWriter.createFor(buffer);
    }

    private static String getData(final ByteArrayOutputStream buffer) throws Exception {
        return buffer.toString("UTF-8");
    }

    @Test
    public void testSimpleWrite() throws Exception {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final MapWriter w = createWriter(buffer);
        w.write("asdf", Collections.singletonMap("x", "y"));
        final Map<String, String> map2 = new LinkedHashMap<String, String>();
        map2.put("123", "456");
        map2.put("a", "b");
        w.write("jklö", map2);
        w.close();

        assertEquals(
                "asdf\n"
                + " x\n"
                + " y\n"
                + ".\n"
                + "jklö\n"
                + " 123\n"
                + " 456\n"
                + " a\n"
                + " b\n"
                + ".\n",
                getData(buffer));
    }

    @Test
    public void testLineBreaksCanNotBeWritten() throws Exception {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final MapWriter w = createWriter(buffer);
        try {
            w.write("a\nb", Collections.<String, String>emptyMap());
            fail();
        } catch (final IllegalArgumentException e) {
            assertTrue("invalid message: " + e.getMessage(), e.getMessage().contains("a\nb"));
        }
    }

}
