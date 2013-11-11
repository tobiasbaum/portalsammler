package de.tntinteractive.portalsammler.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StorageLayer {

    public abstract InputStream openInputStream(String name) throws IOException;

    public abstract OutputStream openOutputStream(String name) throws IOException;

    public abstract boolean exists();

    public abstract void create() throws IOException;

    public abstract boolean fileExists(String name);

}
