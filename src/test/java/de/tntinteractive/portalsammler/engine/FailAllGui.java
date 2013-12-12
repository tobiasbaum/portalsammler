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

import static org.junit.Assert.fail;

public class FailAllGui implements UserInteraction {

    @Override
    public String askForPassword(final StorageLayer storeDirectory) {
        fail("askForPassword not expected");
        return null;
    }

    @Override
    public void showGeneratedPassword(final String key) {
        fail("showGeneratedPassword not expected");
    }

    @Override
    public void showError(final Throwable e) {
        fail("showError not expected");
    }

    @Override
    public void showMainGui(final SecureStore store) {
        fail("showMainGui not expected");
    }

    @Override
    public String askForSetting(final SettingKey key) {
        fail("askForSetting not expected");
        return null;
    }

}
