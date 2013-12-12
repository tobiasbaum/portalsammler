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
package de.tntinteractive.portalsammler.gui;

import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SettingKey;
import de.tntinteractive.portalsammler.engine.StorageLayer;

public interface UserInteraction {

    /**
     * Fragt das Passwort für den übergebenen Store beim Benutzer ab.
     * Liefert das abgefragte Passwort (in String-Darstellung) oder null, wenn der Benutzer abgebrochen hat.
     */
    public abstract String askForPassword(StorageLayer storeDirectory);

    /**
     * Zeigt ein neu erzeugtes Passwort und gibt dem Benutzer die Chance, es zu notieren.
     */
    public abstract void showGeneratedPassword(String key);

    /**
     * Zeigt eine Fehlermeldung.
     */
    public abstract void showError(Throwable e);

    /**
     * Zeigt die Hauptansicht.
     */
    public abstract void showMainGui(SecureStore store);

    /**
     * Fragt eine Einstellung beim Benutzer ab.
     * Liefert das Ergebnis, oder null, wenn der Benutzer abgebrochen hat.
     */
    public abstract String askForSetting(SettingKey key);

}
