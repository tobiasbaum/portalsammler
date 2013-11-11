package de.tntinteractive.portalsammler.gui;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.swing.JOptionPane;

import com.sun.pdfview.PDFFile;

import de.tntinteractive.portalsammler.engine.DocumentInfo;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.ShouldNotHappenException;
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

    public void showDocument(DocumentInfo metadata, byte[] content) throws IOException {
        switch (metadata.getFormat()) {
        case PDF:
            final PDFFile pdfFile = new PDFFile(ByteBuffer.wrap(content));
            new PDFViewer(metadata.getKeywords(), pdfFile).setVisible(true);
            break;
        case TEXT:
            JOptionPane.showMessageDialog(null, new String(content, "UTF-8"));
            break;
        default:
            throw new ShouldNotHappenException(metadata.getFormat().toString());
        }

    }
}
