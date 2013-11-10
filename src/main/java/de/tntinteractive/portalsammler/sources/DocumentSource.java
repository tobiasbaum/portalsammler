package de.tntinteractive.portalsammler.sources;

import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SourceSettings;

public abstract class DocumentSource {

    public abstract void poll(SourceSettings settings, SecureStore store) throws Exception;

}
