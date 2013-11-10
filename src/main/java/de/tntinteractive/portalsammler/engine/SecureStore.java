package de.tntinteractive.portalsammler.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;

public class SecureStore {

    private final StorageLayer storage;
    private final SecureRandom srand;
    private final byte[] key;

    private final Settings settings = new Settings();
    private int settingSalt;

    private final DocumentIndex index = new DocumentIndex();
    private int indexSalt;


    private SecureStore(StorageLayer storage, SecureRandom srand, byte[] key) {
        this.storage = storage;
        this.srand = srand;
        this.key = key;
    }

    public static SecureStore createEmpty(StorageLayer storage, SecureRandom srand, byte[] key) {
        final SecureStore ret = new SecureStore(storage, srand, key);
        ret.settingSalt = ret.srand.nextInt();
        return ret;
    }

    public static SecureStore readFrom(final StorageLayer storage, SecureRandom srand, byte[] key)
        throws IOException, GeneralSecurityException {

        final SecureStore ret = new SecureStore(storage, srand, key);

        readSettings(storage, srand, key, ret);
        readIndex(storage, srand, key, ret);
        return ret;
    }

    private static void readSettings(final StorageLayer storage, SecureRandom srand, byte[] key, final SecureStore ret)
            throws IOException {
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

    private static void readIndex(final StorageLayer storage, SecureRandom srand, byte[] key, final SecureStore ret)
            throws IOException {
        final InputStream stream = storage.openInputStream("index");
        try {
            final Pair<Integer, MapReader> saltAndReader = createMapReader(stream, srand, key);
            ret.indexSalt = saltAndReader.getLeft();
            final MapReader r = saltAndReader.getRight();

            Pair<String, Map<String, String>> p;
            while ((p = r.readNext()) != null) {
                ret.index.putDocument(DocumentInfo.parse(p.getLeft()), p.getRight());
            }

            r.close();
        } finally {
            stream.close();
        }
    }

    private static Pair<Integer, MapReader> createMapReader(InputStream stream, SecureRandom srand, byte[] key)
        throws IOException {

        final CipherInputStream cipher = CryptoHelper.createAesDecryptStream(stream, key, srand);
        final int salt = readInt(cipher);

        final InflaterInputStream inflate = new InflaterInputStream(cipher);
        return Pair.of(salt, MapReader.createFrom(inflate));
    }

    public Settings getSettings() {
        return this.settings;
    }

    public void storeDocument(DocumentInfo metadata, byte[] content) throws IOException {
        System.out.println("Storing " + metadata.getKeywords());
        final File file = new File("C:\\Temp\\secureStore\\", metadata.getKeywords() + ".pdf");
        FileUtils.writeByteArrayToFile(file, content);
        this.index.putDocument(metadata, Collections.singletonMap("f", file.toString()));
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

    private MapWriter createMapWriterAndWriteSalt(final OutputStream out, int salt) throws IOException {
        final CipherOutputStream cipher = CryptoHelper.createAesEncryptStream(out, this.key, this.srand);
        writeInt(cipher, salt);

        final DeflaterOutputStream deflate = new DeflaterOutputStream(cipher);
        final MapWriter w = MapWriter.createFor(deflate);
        return w;
    }

    private static int readInt(InputStream stream) throws IOException {
        final int ch1 = stream.read();
        final int ch2 = stream.read();
        final int ch3 = stream.read();
        final int ch4 = stream.read();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    private static void writeInt(OutputStream stream, int v) throws IOException {
        stream.write((v >>> 24) & 0xFF);
        stream.write((v >>> 16) & 0xFF);
        stream.write((v >>>  8) & 0xFF);
        stream.write((v >>>  0) & 0xFF);
    }

    public DocumentIndex getIndex() {
        return this.index;
    }

}
