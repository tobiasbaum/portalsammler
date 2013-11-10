package de.tntinteractive.portalsammler.gui;

import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.StorageLayer;

public interface UserInteraction {

    public abstract String askForPassword(StorageLayer storeDirectory);

    public abstract void showGeneratedPassword(String key);

    public abstract void showError(Throwable e);

    public abstract void showMainGui(SecureStore store);

}
