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

import java.io.Serializable;

/**
 * Schlüssel für Einstellungen zu einer Quelle.
 */
public final class SettingKey implements Serializable {

    private static final long serialVersionUID = 7565991322115885250L;

    private final String key;

    public SettingKey(final String key) {
        this.key = key;
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof SettingKey)) {
            return false;
        }
        return this.key.equals(((SettingKey) o).key);
    }

    public String getKeyString() {
        return this.key;
    }

    @Override
    public String toString() {
        return this.key;
    }

}
