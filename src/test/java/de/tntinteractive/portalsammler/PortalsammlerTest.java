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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.SecureRandom;

import org.junit.Test;

import de.tntinteractive.portalsammler.engine.CryptoHelper;
import de.tntinteractive.portalsammler.engine.FailAllGui;
import de.tntinteractive.portalsammler.engine.InsecureRandom;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SourceSettings;
import de.tntinteractive.portalsammler.engine.StorageLayer;
import de.tntinteractive.portalsammler.engine.StubStorage;
import de.tntinteractive.portalsammler.engine.UserInteraction;

public final class PortalsammlerTest {

    @Test
    public void testCreationOfStore() throws Exception {
        final StubStorage stubStorage = new StubStorage(false);
        final ValueWrapper<Boolean> shown = ValueWrapper.create(Boolean.FALSE);
        final UserInteraction stubGui = new FailAllGui() {
            @Override
            public void showGeneratedPassword(final String key) {
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
            public String askForPassword(final StorageLayer storeDirectory) {
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
            public String askForPassword(final StorageLayer storeDirectory) {
                return null;
            }
        };
        final SecureStore store = Portalsammler.openOrCreateStore(stubStorage, stubGui);

        assertNull(store);
    }

}
