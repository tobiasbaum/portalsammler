package de.tntinteractive.portalsammler.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import de.tntinteractive.portalsammler.engine.MapReader;

public class MapReaderTest {

    private static MapReader createReader(String input) throws Exception {
        return MapReader.createFrom(new ByteArrayInputStream(input.getBytes("UTF-8")));
    }

    @Test
    public void testReadEmptyMap() throws Exception {
        final String input =
                "test123\n" +
                ".";
        final MapReader r = createReader(input);
        final Pair<String, Map<String, String>> p = r.readNext();
        assertEquals("test123", p.getLeft());
        assertEquals(Collections.emptyMap(), p.getRight());
        assertNull(r.readNext());
    }

    @Test
    public void testSingleEntryMap() throws Exception {
        final String input =
                "test123\n" +
                " e1\n" +
                " w1\n" +
                ".";
        final MapReader r = createReader(input);
        final Pair<String, Map<String, String>> p = r.readNext();
        assertEquals("test123", p.getLeft());
        assertEquals(Collections.singletonMap("e1", "w1"), p.getRight());
        assertNull(r.readNext());
    }

    @Test
    public void testTwoEntryMap() throws Exception {
        final String input =
                "test123\n" +
                " e1\n" +
                " w1\n" +
                " e2\n" +
                " w2\n" +
                ".";
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
                "test123\n" +
                " ä\n" +
                " ö\n" +
                ".\n" +
                "test456\n" +
                " ü\n" +
                " ß\n" +
                ".";
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
