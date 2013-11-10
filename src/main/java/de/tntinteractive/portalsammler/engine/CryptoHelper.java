package de.tntinteractive.portalsammler.engine;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithRandom;

public final class CryptoHelper {

    private CryptoHelper() {
    }

    public static byte[] generateKey(SecureRandom srand) throws NoSuchAlgorithmException {
        final byte[] key = new byte[256 / 8];
        srand.nextBytes(key);
        return key;
    }

    public static CipherInputStream createAesDecryptStream(InputStream is, byte[] key, SecureRandom srand) {
        final PaddedBufferedBlockCipher cipher = initAes(key, srand, false);
        return new CipherInputStream(is, cipher);
    }

    public static CipherOutputStream createAesEncryptStream(OutputStream os, byte[] key, SecureRandom srand) {
        final PaddedBufferedBlockCipher cipher = initAes(key, srand, true);
        return new CipherOutputStream(os, cipher);
    }

    private static PaddedBufferedBlockCipher initAes(byte[] key, SecureRandom srand, boolean forEncryption) {
        final CipherParameters cipherParams = new ParametersWithRandom(new KeyParameter(key), srand);

        final AESFastEngine aes = new AESFastEngine();
        final PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(aes));
        cipher.init(forEncryption, cipherParams);
        return cipher;
    }

    public static byte[] keyFromString(String pw) {
        return Base64.decodeBase64(pw);
    }

    public static String keyToString(byte[] key) {
        return Base64.encodeBase64String(key);
    }

}
