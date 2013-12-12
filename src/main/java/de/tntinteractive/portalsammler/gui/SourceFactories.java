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

import de.tntinteractive.portalsammler.engine.SettingKey;
import de.tntinteractive.portalsammler.sources.DocumentSourceFactory;
import de.tntinteractive.portalsammler.sources.HanVBFactoryV1;
import de.tntinteractive.portalsammler.sources.IngDibaFactoryV1;
import de.tntinteractive.portalsammler.sources.MlpFactoryV1;

public final class SourceFactories {

    public static final SettingKey TYPE = new SettingKey("type");

    private SourceFactories() {
    }

    public static DocumentSourceFactory[] getFactories() {
        return new DocumentSourceFactory[] {
            new IngDibaFactoryV1(),
            new HanVBFactoryV1(),
            new MlpFactoryV1()
        };
    }

    public static DocumentSourceFactory getByName(final String name) {
        for (final DocumentSourceFactory f : getFactories()) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Quellentyp " + name + " ist unbekannt.");
    }

}
