package de.tntinteractive.portalsammler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.SecureRandom;

import org.junit.Test;

import de.tntinteractive.portalsammler.engine.CryptoHelper;
import de.tntinteractive.portalsammler.engine.InsecureRandom;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SourceSettings;
import de.tntinteractive.portalsammler.engine.StorageLayer;
import de.tntinteractive.portalsammler.engine.StubStorage;
import de.tntinteractive.portalsammler.gui.UserInteraction;

public class PortalsammlerTest {

    private static class FailAllGui implements UserInteraction {

        @Override
        public String askForPassword(StorageLayer storeDirectory) {
            fail("askForPassword not expected");
            return null;
        }

        @Override
        public void showGeneratedPassword(String key) {
            fail("showGeneratedPassword not expected");
        }

        @Override
        public void showError(Throwable e) {
            fail("showError not expected");
        }

        @Override
        public void showMainGui(SecureStore store) {
            fail("showMainGui not expected");
        }

    }

    @Test
    public void testCreationOfStore() throws Exception {
        final StubStorage stubStorage = new StubStorage(false);
        final ValueWrapper<Boolean> shown = ValueWrapper.create(Boolean.FALSE);
        final UserInteraction stubGui = new FailAllGui() {
            @Override
            public void showGeneratedPassword(String key) {
                shown.set(Boolean.TRUE);
            }
        };
        final SecureStore store = Portalsammler.openOrCreateStore(stubStorage, stubGui);

        assertTrue(shown.get());
        assertNotNull(store);
        assertTrue(stubStorage.exists());
    }

    @Test
    public void testOpeningOfStore() throws Exception {
        final StubStorage stubStorage = new StubStorage(true);
        final SecureRandom srand = new InsecureRandom();
        final byte[] key = CryptoHelper.generateKey(srand);
        final SecureStore testdata = SecureStore.createEmpty(stubStorage, srand, key);
        testdata.getSettings().putSettings("asdf", new SourceSettings());
        testdata.writeMetadata();

        final UserInteraction stubGui = new FailAllGui() {
            @Override
            public String askForPassword(StorageLayer storeDirectory) {
                return CryptoHelper.keyToString(key);
            }
        };
        final SecureStore store = Portalsammler.openOrCreateStore(stubStorage, stubGui);

        assertNotNull(store);
        assertNotNull(store.getSettings().getSettings("asdf"));
    }

    @Test
    public void testOpeningIsCancelled() throws Exception {
        final StubStorage stubStorage = new StubStorage(true);
        final UserInteraction stubGui = new FailAllGui() {
            @Override
            public String askForPassword(StorageLayer storeDirectory) {
                return null;
            }
        };
        final SecureStore store = Portalsammler.openOrCreateStore(stubStorage, stubGui);

        assertNull(store);
    }

}
