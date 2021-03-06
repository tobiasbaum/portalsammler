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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.InflaterOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

public final class SecureStore {

    private StorageLayer storage;
    private final SecureRandom srand;
    private final byte[] key;
    private final int sizeLimit = 1024 * 1024;

    private final Settings settings = new Settings();
    private int settingSalt;

    private final DocumentIndex index = new DocumentIndex();
    private int indexSalt;

    private ByteArrayOutputStream currentOutputBuffer;
    private int currentFileIndex;


    private SecureStore(final StorageLayer storage, final SecureRandom srand, final byte[] key) {
        this.storage = storage;
        this.srand = srand;
        this.key = key;
    }

    public static SecureStore createEmpty(final StorageLayer storage, final SecureRandom srand, final byte[] key) {
        final SecureStore ret = new SecureStore(storage, srand, key);
        ret.settingSalt = ret.srand.nextInt();
        ret.currentOutputBuffer = new ByteArrayOutputStream();
        ret.currentFileIndex = 1;
        return ret;
    }

    public static SecureStore readFrom(final StorageLayer storage, final SecureRandom srand, final byte[] key)
        throws IOException, GeneralSecurityException {

        final SecureStore ret = new SecureStore(storage, srand, key);

        readSettings(storage, srand, key, ret);
        readIndex(storage, srand, key, ret);
        ret.setCurrentFileIndexToHighestKnown();
        ret.currentOutputBuffer = new ByteArrayOutputStream();
        if (storage.fileExists(ret.getCurrentFilename())) {
            ret.currentOutputBuffer.write(ret.readAndDecrypt(ret.getCurrentFilename()));
        } else {
            writeInt(ret.currentOutputBuffer, srand.nextInt());
        }
        ret.checkCurrentBufferSize();
        return ret;
    }

    private void setCurrentFileIndexToHighestKnown() {
        this.currentFileIndex = 1;
        do {
            this.currentFileIndex++;
        } while (this.storage.fileExists(this.getCurrentFilename()));
        this.currentFileIndex--;
    }

    private static void readSettings(final StorageLayer storage, final SecureRandom srand, final byte[] key,
            final SecureStore ret) throws IOException {
        final InputStream stream = storage.openInputStream("meta");
        try {
            final Pair<Integer, MapReader> saltAndReader = createMapReader(stream, srand, key);
            ret.settingSalt = saltAndReader.getLeft();
            final MapReader r = saltAndReader.getRight();

            Pair<String, Map<String, String>> p;
            while ((p = r.readNext()) != null) {
                final SourceSettings s = new SourceSettings(p.getRight());
                ret.getSettings().putSettings(p.getLeft(), s);
            }

            r.close();
        } finally {
            stream.close();
        }
    }

    private static void readIndex(final StorageLayer storage, final SecureRandom srand, final byte[] key,
            final SecureStore ret) throws IOException {
        final InputStream stream = storage.openInputStream("index");
        try {
            final Pair<Integer, MapReader> saltAndReader = createMapReader(stream, srand, key);
            ret.indexSalt = saltAndReader.getLeft();
            final MapReader r = saltAndReader.getRight();

            Pair<String, Map<String, String>> p;
            while ((p = r.readNext()) != null) {
                final DocumentInfo di = DocumentInfo.parse(p.getLeft());
                ret.index.putDocument(di, p.getRight());
            }

            r.close();
        } finally {
            stream.close();
        }
    }

    private static Pair<Integer, MapReader> createMapReader(final InputStream stream, final SecureRandom srand,
            final byte[] key) throws IOException {

        final InputStream cipher = CryptoHelper.createAesDecryptStream(stream, key, srand);
        final int salt = readInt(cipher);

        final InflaterInputStream inflate = new InflaterInputStream(cipher);
        return Pair.of(salt, MapReader.createFrom(inflate));
    }

    public Settings getSettings() {
        return this.settings;
    }

    public boolean containsDocument(final DocumentInfo info) {
        return this.index.getAllDocuments().contains(info);
    }

    public void storeDocument(final DocumentInfo metadata, final byte[] content) throws IOException {
        final Map<String, String> fileOffset = this.saveContent(content);
        fileOffset.put("u", "u");
        this.index.putDocument(metadata, fileOffset);
    }

    private Map<String, String> saveContent(final byte[] content) throws IOException {
        this.checkCurrentBufferSize();

        final int offset = this.currentOutputBuffer.size();
        final byte[] compressedContent = this.compress(content);
        this.currentOutputBuffer.write(compressedContent);

        final Map<String, String> fileOffset = new HashMap<String, String>();
        fileOffset.put("f", this.getCurrentFilename());
        fileOffset.put("o", Integer.toString(offset));
        fileOffset.put("s", Integer.toString(compressedContent.length));
        this.saveCurrentBuffer();

        return fileOffset;
    }

    private void checkCurrentBufferSize() throws IOException {
        if (this.currentOutputBuffer.size() > this.sizeLimit) {
            this.startNewOutputBuffer();
        }
    }

    private void saveCurrentBuffer() throws IOException {
        this.encryptAndWrite(this.key, this.currentOutputBuffer.toByteArray(), this.getCurrentFilename());
    }

    private void encryptAndWrite(final byte[] encryptionKey, final byte[] data, final String filename)
        throws IOException {

        final OutputStream out = this.storage.openOutputStream(filename);
        try {
            final OutputStream cipher = CryptoHelper.createAesEncryptStream(out, encryptionKey, this.srand);
            cipher.write(data);
            cipher.close();
        } finally {
            out.close();
        }
    }

    private void startNewOutputBuffer() throws IOException {
        this.currentOutputBuffer = new ByteArrayOutputStream();
        writeInt(this.currentOutputBuffer, this.srand.nextInt());
        this.currentFileIndex++;
    }

    private String getCurrentFilename() {
        return String.format("data.%08d", this.currentFileIndex);
    }

    private byte[] compress(final byte[] content) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final DeflaterOutputStream deflate = new DeflaterOutputStream(buffer);
        deflate.write(content);
        deflate.close();
        return buffer.toByteArray();
    }

    public byte[] getDocument(final DocumentInfo metadata) throws IOException {
        final Map<String, String> pointer = this.index.getFilePosition(metadata);
        final byte[] buffer = this.readAndDecrypt(pointer.get("f"));
        final int offset = Integer.parseInt(pointer.get("o"));
        final int size = Integer.parseInt(pointer.get("s"));

        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        final InflaterOutputStream inflate = new InflaterOutputStream(result);
        inflate.write(buffer, offset, size);
        inflate.close();

        return result.toByteArray();
    }

    private byte[] readAndDecrypt(final String filename) throws IOException {
        final InputStream input = this.storage.openInputStream(filename);
        try {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final InputStream cipher = CryptoHelper.createAesDecryptStream(input, this.key, this.srand);
            IOUtils.copy(cipher, buffer);
            return buffer.toByteArray();
        } finally {
            input.close();
        }
    }

    /**
     * Schreibt alle Metadaten und ersetzt dadurch die zuletzt geschriebenen.
     */
    public void writeMetadata() throws IOException, GeneralSecurityException {
        this.writeSettings();
        this.writeIndex();
    }

    private void writeSettings() throws IOException {
        final OutputStream out = this.storage.openOutputStream("meta");
        try {
            final MapWriter w = this.createMapWriterAndWriteSalt(out, this.settingSalt);
            for (final String id : this.getSettings().getAllSettingIds()) {
                w.write(id, this.getSettings().getSettings(id).toStringMap());
            }
            w.close();
        } finally {
            out.close();
        }
    }

    private void writeIndex() throws IOException {
        final OutputStream out = this.storage.openOutputStream("index");
        try {
            final MapWriter w = this.createMapWriterAndWriteSalt(out, this.indexSalt);
            for (final DocumentInfo id : this.index.getAllDocuments()) {
                w.write(id.asString(), this.index.getFilePosition(id));
            }
            w.close();
        } finally {
            out.close();
        }
    }

    private MapWriter createMapWriterAndWriteSalt(final OutputStream out, final int salt) throws IOException {
        final OutputStream cipher = CryptoHelper.createAesEncryptStream(out, this.key, this.srand);
        writeInt(cipher, salt);

        final DeflaterOutputStream deflate = new DeflaterOutputStream(cipher);
        final MapWriter w = MapWriter.createFor(deflate);
        return w;
    }

    private static int readInt(final InputStream stream) throws IOException {
        final int ch1 = stream.read();
        final int ch2 = stream.read();
        final int ch3 = stream.read();
        final int ch4 = stream.read();
        return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
    }

    private static void writeInt(final OutputStream stream, final int v) throws IOException {
        stream.write((v >>> 24) & 0xFF);
        stream.write((v >>> 16) & 0xFF);
        stream.write((v >>>  8) & 0xFF);
        stream.write((v >>>  0) & 0xFF);
    }

    public DocumentIndex getIndex() {
        return this.index;
    }

    public boolean isRead(final DocumentInfo di) {
        final Map<String, String> fileInfo = this.index.getFilePosition(di);
        return fileInfo == null || fileInfo.get("u") == null;
    }

    public void markAsRead(final DocumentInfo di) {
        final Map<String, String> fileInfo = this.index.getFilePosition(di);
        if (fileInfo != null) {
            fileInfo.remove("u");
        }
    }

    public StorageLayer getDirectory() {
        return this.storage;
    }

    /**
     * Verschlüsselt alle Daten neu mit dem übergebenen Schlüssel und löscht die alten Dateien.
     * Dieser Store darf im Anschluss nicht mehr genutzt werden.
     * Es wird ein neuer Store mit dem neuen Schlüssel erzeugt und zurückgeliefert.
     */
    public SecureStore recrypt(final byte[] newKey) throws IOException, GeneralSecurityException {
        this.createNewFiles(newKey);
        this.removeOldFiles();
        this.renameNewFiles();

        final SecureStore newStore = readFrom(this.storage, this.srand, newKey);
        this.storage = null;
        return newStore;
    }

    private void createNewFiles(final byte[] newKey) throws IOException {
        for (final String file : this.storage.getAllFiles()) {
            final byte[] data = this.readAndDecrypt(file);
            this.encryptAndWrite(newKey, data, file + ".tmp");
        }
    }

    private void removeOldFiles() throws IOException {
        for (final String file : this.storage.getAllFiles()) {
            if (!file.endsWith(".tmp")) {
                this.storage.delete(file);
            }
        }
    }

    private void renameNewFiles() throws IOException {
        for (final String file : this.storage.getAllFiles()) {
            if (file.endsWith(".tmp")) {
                this.storage.rename(file, file.substring(0, file.length() - 4));
            }
        }
    }

}
