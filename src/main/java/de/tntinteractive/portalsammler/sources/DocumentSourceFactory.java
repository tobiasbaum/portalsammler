package de.tntinteractive.portalsammler.sources;

import java.util.List;

import de.tntinteractive.portalsammler.engine.SettingKey;

public abstract class DocumentSourceFactory {

    private final String name;

    public DocumentSourceFactory(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public abstract List<SettingKey> getNeededSettings();

    public abstract DocumentSource create(String id);

}
