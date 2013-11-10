package de.tntinteractive.portalsammler.sources;

import java.util.Arrays;
import java.util.List;

import de.tntinteractive.portalsammler.engine.SettingKey;


public class IngDibaFactoryV1 extends DocumentSourceFactory {

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
    public DocumentSource create(String id) {
        return new IngDibaSourceV1(id);
    }

}
