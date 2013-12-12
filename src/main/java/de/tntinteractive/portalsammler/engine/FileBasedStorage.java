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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FileBasedStorage implements StorageLayer {

    private final File dir;

    public FileBasedStorage(final String directory) {
        this.dir = new File(directory);
    }

    @Override
    public InputStream openInputStream(final String name) throws IOException {
        return new FileInputStream(new File(this.dir, name));
    }

    @Override
    public OutputStream openOutputStream(final String name) throws IOException {
        return new FileOutputStream(new File(this.dir, name));
    }

    @Override
    public boolean exists() {
        return this.dir.exists();
    }

    @Override
    public void create() throws IOException {
        if (!this.dir.mkdir()) {
            throw new IOException("Directory could not be created: " + this.dir);
        }
    }

    @Override
    public String toString() {
        return this.dir.toString();
    }

    @Override
    public boolean fileExists(final String name) {
        return new File(this.dir, name).exists();
    }

    @Override
    public List<String> getAllFiles() {
        final String[] list = this.dir.list();
        if (list == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(list);
    }

    @Override
    public void delete(final String name) throws IOException {
        final boolean success = new File(this.dir, name).delete();
        if (!success) {
            throw new IOException(name + " konnte nicht gelöscht werden!");
        }
    }

    @Override
    public void rename(final String oldName, final String newName) throws IOException {
        final boolean success = new File(this.dir, oldName).renameTo(new File(this.dir, newName));
        if (!success) {
            throw new IOException(oldName + " konnte nicht in " + newName + " umbenannt werden!");
        }
    }

}
