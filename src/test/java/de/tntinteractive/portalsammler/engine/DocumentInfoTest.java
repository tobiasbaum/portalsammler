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

import java.util.Date;

import org.junit.Test;

public final class DocumentInfoTest {

    @Test
    public void testAddKeywords() {
        final DocumentInfo i = DocumentInfo.create("quelle", DocumentFormat.PDF);
        i.addKeywords(" asdf  \njklö 123 ");
        i.addKeywords("456");
        i.addKeywords("78\r9");

        assertEquals("asdf jklö 123 456 78 9", i.getKeywords());
    }

    @Test
    public void testAsStringAndParse() {
        checkAsStringAndParse("", 0, "");
        final DocumentInfo i = DocumentInfo.create("quelle", DocumentFormat.PDF);
        i.addKeywords(" asdf  \njklö 123 ");
        i.addKeywords("456");
        i.addKeywords("78\r9");

        assertEquals("asdf jklö 123 456 78 9", i.getKeywords());
    }

    private static void checkAsStringAndParse(final String source, final long date, final String keywords) {
        final DocumentInfo d1 = DocumentInfo.create(source, DocumentFormat.PDF);
        d1.setDate(new Date(date));
        d1.addKeywords(keywords);
        final String serialized = d1.asString();
        final DocumentInfo d2 = DocumentInfo.parse(serialized);
        assertEquals(source, d2.getSourceId());
        assertEquals(date, d2.getDate().getTime());
        assertEquals(keywords, d2.getKeywords());
    }

}
