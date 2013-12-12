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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StubStorage implements StorageLayer {

    private boolean exists;
    private final Map<String, byte[]> files = new HashMap<String, byte[]>();

    public StubStorage(final boolean exists) {
        this.exists = exists;
    }

    public StubStorage() {
        this(true);
    }

    @Override
    public InputStream openInputStream(final String name) throws IOException {
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
            public void write(final int b) throws IOException {
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
    public boolean fileExists(final String name) {
        return this.exists && this.files.containsKey(name);
    }

    @Override
    public List<String> getAllFiles() {
        return new ArrayList<String>(this.files.keySet());
    }

    @Override
    public void delete(final String name) throws IOException {
        if (!this.files.containsKey(name)) {
            throw new IOException();
        }
        this.files.remove(name);
    }

    @Override
    public void rename(final String oldName, final String newName) throws IOException {
        if (!this.files.containsKey(oldName)) {
            throw new IOException();
        }
        final byte[] bs = this.files.get(oldName);
        this.files.remove(oldName);
        this.files.put(newName, bs);
    }

}
