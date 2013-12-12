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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface StorageLayer {

    public abstract InputStream openInputStream(String name) throws IOException;

    public abstract OutputStream openOutputStream(String name) throws IOException;

    public abstract boolean exists();

    public abstract void create() throws IOException;

    public abstract boolean fileExists(String name);

    public abstract List<String> getAllFiles();

    public abstract void delete(String name) throws IOException;

    public abstract void rename(String oldName, String newName) throws IOException;

}
