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
package de.tntinteractive.portalsammler.sources;

import java.util.Arrays;
import java.util.List;

import de.tntinteractive.portalsammler.engine.SettingKey;


public final class IngDibaFactoryV1 extends DocumentSourceFactory {

    public IngDibaFactoryV1() {
        super("ING-DiBa V1");
    }

    @Override
    public List<SettingKey> getNeededSettings() {
        return Arrays.asList(
                IngDibaSourceV1.USER,
                IngDibaSourceV1.PASSWORD,
                IngDibaSourceV1.CODE);
    }

    @Override
    public DocumentSource create(final String id) {
        return new IngDibaSourceV1(id);
    }

}
