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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SettingKey;
import de.tntinteractive.portalsammler.engine.Settings;
import de.tntinteractive.portalsammler.engine.SourceSettings;
import de.tntinteractive.portalsammler.sources.DocumentSourceFactory;

public class SourceConfigDialog extends JDialog {

    private static final long serialVersionUID = -5863672998709207556L;

    private final Gui gui;
    private final SecureStore store;
    private final Settings workingCopy;

    private final JComboBox<String> idCombo;
    private final JPanel settingPanel;

    private JButton removeButton;


    public SourceConfigDialog(final Gui gui, final SecureStore store) {
        this.setTitle("Quellen-Konfiguration");
        this.setModal(true);
        this.setMinimumSize(new Dimension(500, 350));

        this.gui = gui;
        this.store = store;
        this.workingCopy = this.store.getSettings().deepClone();

        this.idCombo = new JComboBox<String>();
        this.idCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SourceConfigDialog.this.updateSettingPanel();
            }
        });

        final PanelBuilder upb = new PanelBuilder(new FormLayout(
                "fill:p:grow, 4dlu, p, 4dlu, p", "p"));
        upb.add(this.idCombo, CC.xy(1, 1));
        upb.add(this.createNewButton(), CC.xy(3, 1));
        upb.add(this.createRemoveButton(), CC.xy(5, 1));

        this.settingPanel = new JPanel();

        final ButtonBarBuilder bbb = new ButtonBarBuilder();
        bbb.addGlue();
        bbb.addButton(this.createOkButton(), this.createCancelButton());

        this.updateIdCombo();

        final PanelBuilder builder = new PanelBuilder(new FormLayout(
                "4dlu, fill:p:grow, 4dlu",
                "4dlu, p, 4dlu, fill:p:grow, 4dlu, p, 4dlu"));
        builder.add(upb.getPanel(), CC.xy(2, 2));
        builder.add(this.settingPanel, CC.xy(2, 4));
        builder.add(bbb.getPanel(), CC.xy(2, 6));

        this.setContentPane(builder.getPanel());
        this.pack();
        this.setLocationRelativeTo(this.getOwner());
    }

    private JButton createOkButton() {
        final JButton button = new JButton("OK");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SourceConfigDialog.this.handleOk();
            }
        });
        return button;
    }

    private void handleOk() {
        this.store.getSettings().takeFrom(this.workingCopy);
        try {
            this.store.writeMetadata();
        } catch (final IOException e) {
            this.gui.showError(e);
        } catch (final GeneralSecurityException e) {
            this.gui.showError(e);
        }
        this.setVisible(false);
    }

    private JButton createCancelButton() {
        final JButton button = new JButton("Abbrechen");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SourceConfigDialog.this.setVisible(false);
            }
        });
        return button;
    }

    private void updateSettingPanel() {
        this.settingPanel.removeAll();
        if (!this.idCombo.isEnabled()) {
            return;
        }
        final String id = (String) this.idCombo.getSelectedItem();
        if (id == null) {
            return;
        }
        final SourceSettings settings = this.workingCopy.getSettings(id);
        final String type = settings.get(SourceFactories.TYPE, this.gui);
        final DocumentSourceFactory factory = SourceFactories.getByName(type);
        final DefaultFormBuilder formBuilder = new DefaultFormBuilder(
                new FormLayout("right:p, 4dlu, fill:p:grow"), this.settingPanel);
        for (final SettingKey key : factory.getNeededSettings()) {
            final JLabel label = new JLabel(key.getKeyString());
            final String value = settings.getOrCreate(key);
            final JTextField input = new JTextField(value, 30);
            input.setName(key.getKeyString());
            input.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void removeUpdate(final DocumentEvent e) {
                    SourceConfigDialog.this.handleSettingChanged(settings, input);
                }
                @Override
                public void insertUpdate(final DocumentEvent e) {
                    SourceConfigDialog.this.handleSettingChanged(settings, input);
                }
                @Override
                public void changedUpdate(final DocumentEvent e) {
                    SourceConfigDialog.this.handleSettingChanged(settings, input);
                }
            });

            formBuilder.append(label, input);
        }

        this.settingPanel.validate();
        this.settingPanel.repaint();
    }

    private void handleSettingChanged(final SourceSettings settings, final JTextField input) {
        final String text = input.getText();
        final String key = input.getName();
        settings.set(new SettingKey(key), text);
    }

    private void updateIdCombo() {
        this.idCombo.removeAllItems();
        this.idCombo.setEnabled(true);
        this.removeButton.setEnabled(true);
        final Set<String> allIds = this.workingCopy.getAllSettingIds();
        for (final String id : allIds) {
            this.idCombo.addItem(id);
        }
        if (allIds.isEmpty()) {
            this.idCombo.setEnabled(false);
            this.removeButton.setEnabled(false);
            this.idCombo.addItem("Noch keine Quellen konfiguriert");
        }
    }

    private void updateIdCombo(final String idToSelect) {
        this.updateIdCombo();
        this.idCombo.setSelectedItem(idToSelect);
    }

    private JButton createNewButton() {
        final JButton button = new JButton("Neue Quelle hinzufügen...");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SourceConfigDialog.this.newConfiguration();
            }
        });
        return button;
    }

    private void newConfiguration() {
        final DocumentSourceFactory factory = (DocumentSourceFactory) JOptionPane.showInputDialog(
                this,
                "Bitte wählen Sie die Art der Quelle",
                "Art der Quelle",
                JOptionPane.QUESTION_MESSAGE,
                null,
                SourceFactories.getFactories(),
                null);
        if (factory == null) {
            return;
        }
        String id = factory.getName();
        while (true) {
            id = JOptionPane.showInputDialog(this, "Bitte geben Sie eine ID für die Quelle an", id);
            if (id == null) {
                return;
            }

            final SourceSettings existingSettings = this.workingCopy.getSettings(id);
            if (existingSettings == null) {
                break;
            }
            JOptionPane.showMessageDialog(this, "Die ID " + id + " wurde bereits für eine andere Quelle vergeben.",
                    "Doppelte ID", JOptionPane.ERROR_MESSAGE);
        }

        final SourceSettings settings = new SourceSettings();
        settings.set(SourceFactories.TYPE, factory.getName());
        this.workingCopy.putSettings(id, settings);
        this.updateIdCombo(id);
    }

    private JButton createRemoveButton() {
        assert this.removeButton == null;
        this.removeButton = new JButton("Quelle löschen");
        this.removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SourceConfigDialog.this.removeConfiguration();
            }
        });
        return this.removeButton;
    }

    private void removeConfiguration() {
        final String id = (String) this.idCombo.getSelectedItem();
        if (id == null) {
            return;
        }
        this.workingCopy.removeSettings(id);
        this.updateIdCombo();
    }

}
