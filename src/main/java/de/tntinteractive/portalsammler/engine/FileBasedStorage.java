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

}
