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

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

public class Settings {

    private final LinkedHashMap<String, SourceSettings> settings = new LinkedHashMap<String, SourceSettings>();

    public Set<String> getAllSettingIds() {
        return this.settings.keySet();
    }

    public void putSettings(String id, SourceSettings settings) {
        this.settings.put(id, settings);
    }

    public SourceSettings getSettings(String id) {
        return this.settings.get(id);
    }

    public Settings deepClone() {
        final Settings s = new Settings();
        s.takeFrom(this);
        return s;
    }

    public void takeFrom(Settings newSettings) {
        this.settings.clear();
        for (final Entry<String, SourceSettings> e : newSettings.settings.entrySet()) {
            this.putSettings(e.getKey(), e.getValue().deepClone());
        }
    }

    public int getSize() {
        return this.settings.size();
    }

}
