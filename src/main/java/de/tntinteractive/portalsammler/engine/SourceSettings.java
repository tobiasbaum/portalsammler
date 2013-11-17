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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.tntinteractive.portalsammler.gui.UserInteraction;

/**
 * Verwaltet die Einstellungen zu einer Quelle.
 */
public class SourceSettings implements Serializable {

    private static final long serialVersionUID = 3724264529057649734L;

    private final Map<SettingKey, String> values = new LinkedHashMap<SettingKey, String>();

    public SourceSettings() {
    }

    public SourceSettings(Map<String, String> content) {
        for (final Entry<String, String> e : content.entrySet()) {
            this.values.put(new SettingKey(e.getKey()), e.getValue());
        }
    }

    public String get(SettingKey key, UserInteraction gui) {
        final String ret = this.values.get(key);
        if (ret == null || ret.isEmpty()) {
            return gui.askForSetting(key);
        }
        return ret;
    }

    public String getOrCreate(SettingKey key) {
        final String ret = this.values.get(key);
        if (ret == null) {
            this.values.put(key, "");
            return "";
        }
        return ret;
    }

    public void set(SettingKey key, String value) {
        this.values.put(key, value);
    }

    public Map<String, String> toStringMap() {
        final LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();
        for (final Entry<SettingKey, String> e : this.values.entrySet()) {
            ret.put(e.getKey().getKeyString(), e.getValue());
        }
        return ret;
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SourceSettings)) {
            return false;
        }
        final SourceSettings s = (SourceSettings) o;
        return this.values.equals(s.values);
    }

    public SourceSettings deepClone() {
        final SourceSettings s = new SourceSettings();
        s.values.putAll(this.values);
        return s;
    }

}
