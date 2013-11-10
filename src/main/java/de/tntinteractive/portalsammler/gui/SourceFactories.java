package de.tntinteractive.portalsammler.gui;

import de.tntinteractive.portalsammler.engine.SettingKey;
import de.tntinteractive.portalsammler.sources.DocumentSourceFactory;
import de.tntinteractive.portalsammler.sources.IngDibaFactoryV1;

public class SourceFactories {

    public static final SettingKey TYPE = new SettingKey("type");

    public static DocumentSourceFactory[] getFactories() {
        return new DocumentSourceFactory[] {
                new IngDibaFactoryV1()
        };
    }

    public static DocumentSourceFactory getByName(String name) {
        for (final DocumentSourceFactory f : getFactories()) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Quellentyp " + name + " ist unbekannt.");
    }

}
