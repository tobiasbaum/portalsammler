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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

public final class MapReader implements Closeable {

    private final BufferedReader reader;

    private MapReader(final BufferedReader reader) {
        this.reader = reader;
    }

    public static MapReader createFrom(final InputStream in) {
        try {
            return new MapReader(new BufferedReader(new InputStreamReader(in, "UTF-8")));
        } catch (final UnsupportedEncodingException e) {
            throw new ShouldNotHappenException(e);
        }
    }

    public Pair<String, Map<String, String>> readNext() throws IOException {
        final String id = this.reader.readLine();
        if (id == null) {
            return null;
        }
        final Map<String, String> map = new LinkedHashMap<String, String>();
        while (true) {
            final String firstLine = this.reader.readLine();
            if (firstLine.equals(".")) {
                break;
            }
            final String secondLine = this.reader.readLine();
            map.put(lineToValue(firstLine), lineToValue(secondLine));
        }
        return Pair.of(id, map);
    }

    private static String lineToValue(final String line) {
        if (!line.startsWith(" ")) {
            throw new ShouldNotHappenException("invalid line: " + line);
        }
        return line.substring(1);
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }

}
