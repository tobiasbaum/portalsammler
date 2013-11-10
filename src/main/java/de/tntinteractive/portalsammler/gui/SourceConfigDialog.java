package de.tntinteractive.portalsammler.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SettingKey;
import de.tntinteractive.portalsammler.engine.Settings;
import de.tntinteractive.portalsammler.engine.SourceSettings;
import de.tntinteractive.portalsammler.sources.DocumentSourceFactory;

public class SourceConfigDialog extends JDialog {

    private final Gui gui;
    private final SecureStore store;
    private final Settings workingCopy;

    private final JComboBox<String> idCombo;
    private final JPanel settingPanel;


    public SourceConfigDialog(Gui gui, SecureStore store) {
        this.setTitle("Quellen-Konfiguration");
        this.setLayout(new BorderLayout());
        this.setSize(400, 400);
        this.setModal(true);

        this.gui = gui;
        this.store = store;
        this.workingCopy = this.store.getSettings().deepClone();

        final JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.LINE_AXIS));

        this.idCombo = new JComboBox<String>();
        this.idCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SourceConfigDialog.this.updateSettingPanel();
            }
        });
        upperPanel.add(this.idCombo);
        upperPanel.add(this.createNewButton());
        this.add(upperPanel, BorderLayout.NORTH);

        this.settingPanel = new JPanel();
        this.settingPanel.setLayout(new GridBagLayout());
        this.add(this.settingPanel, BorderLayout.CENTER);

        final JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.LINE_AXIS));
        lowerPanel.add(this.createOkButton());
        lowerPanel.add(this.createCancelButton());
        this.add(lowerPanel, BorderLayout.SOUTH);

        this.updateIdCombo();
    }

    private JButton createOkButton() {
        final JButton button = new JButton("OK");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            public void actionPerformed(ActionEvent e) {
                SourceConfigDialog.this.setVisible(false);
            }
        });
        return button;
    }

    private void updateSettingPanel() {
        this.settingPanel.removeAll();
        final String id = (String) this.idCombo.getSelectedItem();
        if (id == null) {
            return;
        }
        final SourceSettings settings = this.workingCopy.getSettings(id);
        final String type = settings.get(SourceFactories.TYPE);
        final DocumentSourceFactory factory = SourceFactories.getByName(type);
        int i = 0;
        for (final SettingKey key : factory.getNeededSettings()) {
            final JLabel label = new JLabel(key.getKeyString());
            final String value = settings.getOrCreate(key);
            final JTextField input = new JTextField(value, 30);
            input.setName(key.getKeyString());
            input.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void removeUpdate(DocumentEvent e) {
                    SourceConfigDialog.this.handleSettingChanged(settings, input);
                }
                @Override
                public void insertUpdate(DocumentEvent e) {
                    SourceConfigDialog.this.handleSettingChanged(settings, input);
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    SourceConfigDialog.this.handleSettingChanged(settings, input);
                }
            });

            this.settingPanel.add(label, gbc(0, i));
            this.settingPanel.add(input, gbc(1, i));
            i++;
        }
    }

    private void handleSettingChanged(SourceSettings settings, JTextField input) {
        final String text = input.getText();
        final String key = input.getName();
        settings.set(new SettingKey(key), text);
    }

    private static GridBagConstraints gbc(int x, int y) {
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 1;
        return gbc;
    }

    private void updateIdCombo() {
        this.idCombo.removeAllItems();
        for (final String id : this.workingCopy.getAllSettingIds()) {
            this.idCombo.addItem(id);
        }
    }

    private void updateIdCombo(String idToSelect) {
        this.updateIdCombo();
        this.idCombo.setSelectedItem(idToSelect);
    }

    private JButton createNewButton() {
        final JButton button = new JButton("Neue Quelle hinzufügen...");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            JOptionPane.showMessageDialog(this, "Die ID " + id + " wurde bereits für eine andere Quelle vergeben.", "Doppelte ID", JOptionPane.ERROR_MESSAGE);
        }

        final SourceSettings settings = new SourceSettings();
        settings.set(SourceFactories.TYPE, factory.getName());
        this.workingCopy.putSettings(id, settings);
        this.updateIdCombo(id);
    }

}
