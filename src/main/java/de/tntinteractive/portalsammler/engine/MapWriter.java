package de.tntinteractive.portalsammler.engine;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

public class MapWriter implements Closeable {

    private final Writer writer;

    public MapWriter(Writer writer) {
        this.writer = writer;
    }

    public static MapWriter createFor(OutputStream buffer) {
        try {
            return new MapWriter(new OutputStreamWriter(buffer, "UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            throw new ShouldNotHappenException(e);
        }
    }

    public void write(String id, Map<String, String> map) throws IOException {
        this.writeLine(id);
        for (final Entry<String, String> e : map.entrySet()) {
            this.writer.write(' ');
            this.writeLine(e.getKey());
            this.writer.write(' ');
            this.writeLine(e.getValue());
        }
        this.writer.write(".\n");
    }

    private void writeLine(String data) throws IOException {
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
