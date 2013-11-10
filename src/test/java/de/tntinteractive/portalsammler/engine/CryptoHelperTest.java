package de.tntinteractive.portalsammler.engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CryptoHelperTest {

    @Test
    public void testKeyEncoding() {
        checkKeyEncoding("");
        checkKeyEncoding("asdf");
        checkKeyEncoding("12345690asdfasdfasdfasdf");
    }

    private static void checkKeyEncoding(String string) {
        assertEquals(string, CryptoHelper.keyToString(CryptoHelper.keyFromString(string)));
    }

}
