package de.tntinteractive.portalsammler.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class StubStorage implements StorageLayer {

    private boolean exists;
    private final Map<String, byte[]> files = new HashMap<String, byte[]>();

    public StubStorage(boolean exists) {
        this.exists = exists;
    }

    public StubStorage() {
        this(true);
    }

    @Override
    public InputStream openInputStream(String name) throws IOException {
        if (!this.exists) {
            throw new FileNotFoundException("directory does not exist");
        }
        final byte[] data = this.files.get(name);
        if (data == null) {
            throw new FileNotFoundException("unknown file " + name);
        }
        return new ByteArrayInputStream(data);
    }

    @Override
    public OutputStream openOutputStream(final String name) throws IOException {
        if (!this.exists) {
            throw new FileNotFoundException("directory does not exist");
        }
        return new OutputStream() {

            private boolean closed;
            private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            @Override
            public void write(int b) throws IOException {
                if (this.closed) {
                    throw new IOException("already closed");
                }
                this.buffer.write(b);
            }

            @Override
            public void close() throws IOException {
                if (!this.closed) {
                    this.closed = true;
                    this.buffer.close();
                    StubStorage.this.files.put(name, this.buffer.toByteArray());
                }
            }

        };
    }

    @Override
    public boolean exists() {
        return this.exists;
    }

    @Override
    public void create() {
        this.exists = true;
    }

    @Override
    public boolean fileExists(String name) {
        return this.exists && this.files.containsKey(name);
    }

}
