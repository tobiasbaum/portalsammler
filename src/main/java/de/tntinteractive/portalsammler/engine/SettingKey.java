package de.tntinteractive.portalsammler.engine;

import java.io.Serializable;

/**
 * Schlüssel für Einstellungen zu einer Quelle.
 */
public class SettingKey implements Serializable {

    private static final long serialVersionUID = 7565991322115885250L;

    private final String key;

    public SettingKey(String key) {
        this.key = key;
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public boolean equals(Object o) {
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
