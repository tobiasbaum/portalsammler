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

public class FileBasedStorage implements StorageLayer {

    private final File dir;

    public FileBasedStorage(String directory) {
        this.dir = new File(directory);
    }

    @Override
    public InputStream openInputStream(String name) throws IOException {
        return new FileInputStream(new File(this.dir, name));
    }

    @Override
    public OutputStream openOutputStream(String name) throws IOException {
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
    public boolean fileExists(String name) {
        return new File(this.dir, name).exists();
    }

}
