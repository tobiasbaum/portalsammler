/*
    Copyright (C) 2013  Tobias Baum <tbaum at tntinteractive.de>

    This file is a part of Portalsammler.

    Portalsammler is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Portalsammler is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Portalsammler.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tntinteractive.portalsammler.gui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.sun.pdfview.PDFFile;

import de.tntinteractive.portalsammler.engine.DocumentInfo;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SettingKey;
import de.tntinteractive.portalsammler.engine.ShouldNotHappenException;
import de.tntinteractive.portalsammler.engine.StorageLayer;

public class Gui implements UserInteraction {

    @Override
    public String askForPassword(StorageLayer storeDirectory) {
        final GetPasswordDialog dlg = new GetPasswordDialog(storeDirectory.toString());
        dlg.setVisible(true);
        return dlg.getPassword();
    }

    @Override
    public void showGeneratedPassword(String key) {
        new GeneratedPasswordDialog(this, key).setVisible(true);
    }

    @Override
    public void showError(final Throwable e) {
        e.printStackTrace();
        final Runnable msg = new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            msg.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(msg);
            } catch (final InvocationTargetException e1) {
                e1.printStackTrace();
            } catch (final InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void showMainGui(SecureStore store) {
        if (store.getSettings().getAllSettingIds().isEmpty()) {
            this.showConfigGui(store);
        }
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
            new TextViewer(metadata.getKeywords(), new String(content, "UTF-8")).setVisible(true);
            break;
        default:
            throw new ShouldNotHappenException(metadata.getFormat().toString());
        }

    }

    @Override
    public String askForSetting(SettingKey key) {
        return JOptionPane.showInputDialog("Bitte geben Sie den Wert für einen fehlenden Parameter ein: " + key.getKeyString());
    }
}
