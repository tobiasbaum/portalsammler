package de.tntinteractive.portalsammler.sources;

import java.util.Arrays;
import java.util.List;

import de.tntinteractive.portalsammler.engine.SettingKey;

public class HanVBFactoryV1 extends DocumentSourceFactory {

    public HanVBFactoryV1() {
        super("Hannoversche Volksbank V1");
    }

    @Override
    public List<SettingKey> getNeededSettings() {
        return Arrays.asList(
                HanVBSourceV1.USER,
                HanVBSourceV1.PASSWORD);
    }

    @Override
    public DocumentSource create(String id) {
        return new HanVBSourceV1(id);
    }

}
