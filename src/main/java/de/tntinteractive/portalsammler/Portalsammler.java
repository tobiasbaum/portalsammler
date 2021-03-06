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
package de.tntinteractive.portalsammler;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import de.tntinteractive.portalsammler.engine.CryptoHelper;
import de.tntinteractive.portalsammler.engine.FileBasedStorage;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.StorageLayer;
import de.tntinteractive.portalsammler.engine.UserInteraction;
import de.tntinteractive.portalsammler.gui.Gui;


public final class Portalsammler {

    private Portalsammler() {
    }

    public static void main(final String[] args) throws Exception {
        final UserInteraction gui = new Gui();

        try {
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(final Thread t, final Throwable e) {
                    gui.showError(e);
                }
            });

            final StorageLayer storeDirectory = determineStoreDirectory(args);
            final SecureStore store = openOrCreateStore(storeDirectory, gui);
            if (store == null) {
                System.exit(1);
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
            System.exit(2);
        }
    }

    static SecureStore openOrCreateStore(final StorageLayer storeDirectory, final UserInteraction gui)
        throws IOException, GeneralSecurityException {

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

    private static StorageLayer determineStoreDirectory(final String[] args) {
        String directory;
        if (args.length > 0) {
            directory = args[0];
        } else {
            directory = new File(System.getProperty("user.home"), ".portalsammler").toString();
        }
        return new FileBasedStorage(directory);
    }

}
