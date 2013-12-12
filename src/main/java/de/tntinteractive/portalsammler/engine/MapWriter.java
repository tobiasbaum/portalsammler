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

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

public final class MapWriter implements Closeable {

    private final Writer writer;

    public MapWriter(final Writer writer) {
        this.writer = writer;
    }

    public static MapWriter createFor(final OutputStream buffer) {
        try {
            return new MapWriter(new OutputStreamWriter(buffer, "UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            throw new ShouldNotHappenException(e);
        }
    }

    public void write(final String id, final Map<String, String> map) throws IOException {
        this.writeLine(id);
        for (final Entry<String, String> e : map.entrySet()) {
            this.writer.write(' ');
            this.writeLine(e.getKey());
            this.writer.write(' ');
            this.writeLine(e.getValue());
        }
        this.writer.write(".\n");
    }

    private void writeLine(final String data) throws IOException {
        if (data.contains("\n") || data.contains("\r")) {
            throw new IllegalArgumentException("data with line break: " + data);
        }
        this.writer.write(data);
        this.writer.write('\n');
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }

}
