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

}
