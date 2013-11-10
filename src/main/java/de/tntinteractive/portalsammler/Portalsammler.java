package de.tntinteractive.portalsammler;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import de.tntinteractive.portalsammler.engine.CryptoHelper;
import de.tntinteractive.portalsammler.engine.FileBasedStorage;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.StorageLayer;
import de.tntinteractive.portalsammler.gui.Gui;
import de.tntinteractive.portalsammler.gui.UserInteraction;


public class Portalsammler {

    public static void main(String[] args) throws Exception {
        final UserInteraction gui = new Gui();

        try {
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    gui.showError(e);
                }
            });

            final StorageLayer storeDirectory = determineStoreDirectory(args);
            final SecureStore store = openOrCreateStore(storeDirectory, gui);
            if (store == null) {
                return;
            }

            gui.showMainGui(store);
            //TODO sperren des Verzeichnisses
    //        if (isLocked(storeDirectory)) {
    //
    //            return;
    //        }

        } catch (final Exception e) {
            gui.showError(e);
        }
//        ingDiba.poll(store.getSettings(ingDiba.getId()), store);
    }

    static SecureStore openOrCreateStore(StorageLayer storeDirectory, UserInteraction gui) throws IOException, GeneralSecurityException {
        final SecureRandom srand = new SecureRandom();
        if (storeDirectory.exists()) {
            final byte[] key = CryptoHelper.keyFromString(gui.askForPassword(storeDirectory));
            if (key == null) {
                return null;
            }
            return SecureStore.readFrom(storeDirectory, srand, key);
        } else {
            storeDirectory.create();
            final byte[] key = CryptoHelper.generateKey(srand);
            gui.showGeneratedPassword(CryptoHelper.keyToString(key));
            final SecureStore ret = SecureStore.createEmpty(storeDirectory, srand, key);
            ret.writeMetadata();
            return ret;
        }
    }

    private static StorageLayer determineStoreDirectory(String[] args) {
        return new FileBasedStorage(args[0]);
    }

}
