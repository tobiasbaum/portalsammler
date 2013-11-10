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

import de.tntinteractive.portalsammler.engine.MapWriter;

public class MapWriterTest {

    private static MapWriter createWriter(OutputStream buffer) {
        return MapWriter.createFor(buffer);
    }

    private static String getData(ByteArrayOutputStream buffer) throws Exception {
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
                "asdf\n" +
                " x\n" +
                " y\n" +
                ".\n" +
                "jklö\n" +
                " 123\n" +
                " 456\n" +
                " a\n" +
                " b\n" +
                ".\n",
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
