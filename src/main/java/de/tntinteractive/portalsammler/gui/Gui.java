package de.tntinteractive.portalsammler.gui;

import javax.swing.JOptionPane;

import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.StorageLayer;

public class Gui implements UserInteraction {

    @Override
    public String askForPassword(StorageLayer storeDirectory) {
        return JOptionPane.showInputDialog("Enter password for " + storeDirectory);
    }

    @Override
    public void showGeneratedPassword(String key) {
        JOptionPane.showInputDialog("New password generated:", key);
    }

    @Override
    public void showError(Throwable e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
    }

    @Override
    public void showMainGui(SecureStore store) {
        new MainDialog(this, store).setVisible(true);
    }

    public void showConfigGui(SecureStore store) {
        new SourceConfigDialog(this, store).setVisible(true);
    }

}
