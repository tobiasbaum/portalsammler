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

public class MapReader implements Closeable {

    private final BufferedReader reader;

    private MapReader(BufferedReader reader) {
        this.reader = reader;
    }

    public static MapReader createFrom(InputStream in) {
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

    private static String lineToValue(String line) {
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