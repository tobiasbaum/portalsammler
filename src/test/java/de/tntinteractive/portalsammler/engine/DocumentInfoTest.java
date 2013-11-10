package de.tntinteractive.portalsammler.engine;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

public class DocumentInfoTest {

    @Test
    public void testAddKeywords() {
        final DocumentInfo i = DocumentInfo.create("quelle");
        i.addKeywords(" asdf  \njklö 123 ");
        i.addKeywords("456");
        i.addKeywords("78\r9");

        assertEquals("asdf jklö 123 456 78 9", i.getKeywords());
    }

    @Test
    public void testAsStringAndParse() {
        checkAsStringAndParse("", 0, "");
        final DocumentInfo i = DocumentInfo.create("quelle");
        i.addKeywords(" asdf  \njklö 123 ");
        i.addKeywords("456");
        i.addKeywords("78\r9");

        assertEquals("asdf jklö 123 456 78 9", i.getKeywords());
    }

    private static void checkAsStringAndParse(String source, long date, String keywords) {
        final DocumentInfo d1 = DocumentInfo.create(source);
        d1.setDate(new Date(date));
        d1.addKeywords(keywords);
        final String serialized = d1.asString();
        final DocumentInfo d2 = DocumentInfo.parse(serialized);
        assertEquals(source, d2.getSourceId());
        assertEquals(date, d2.getDate().getTime());
        assertEquals(keywords, d2.getKeywords());
    }

}
