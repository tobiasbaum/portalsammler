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

    public static byte[] generateKey(final SecureRandom srand) throws NoSuchAlgorithmException {
        final byte[] key = new byte[256 / 8];
        srand.nextBytes(key);
        return key;
    }

    public static CipherInputStream createAesDecryptStream(final InputStream is, final byte[] key,
            final SecureRandom srand) {
        final PaddedBufferedBlockCipher cipher = initAes(key, srand, false);
        return new CipherInputStream(is, cipher);
    }

    public static CipherOutputStream createAesEncryptStream(final OutputStream os, final byte[] key,
            final SecureRandom srand) {
        final PaddedBufferedBlockCipher cipher = initAes(key, srand, true);
        return new CipherOutputStream(os, cipher);
    }

    private static PaddedBufferedBlockCipher initAes(final byte[] key, final SecureRandom srand,
            final boolean forEncryption) {
        final CipherParameters cipherParams = new ParametersWithRandom(new KeyParameter(key), srand);

        final AESFastEngine aes = new AESFastEngine();
        final PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(aes));
        cipher.init(forEncryption, cipherParams);
        return cipher;
    }

    public static byte[] keyFromString(final String pw) {
        return Base64.decodeBase64(pw);
    }

    public static String keyToString(final byte[] key) {
        return Base64.encodeBase64String(key);
    }

}
