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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.tuple.Pair;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.tntinteractive.portalsammler.engine.CryptoHelper;
import de.tntinteractive.portalsammler.engine.DocumentFilterParser;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.Settings;
import de.tntinteractive.portalsammler.engine.SourceSettings;
import de.tntinteractive.portalsammler.sources.DocumentSourceFactory;

public final class MainDialog extends JFrame {

    private static final long serialVersionUID = -2309663260423505246L;

    private final Gui gui;
    private final JTextField filterField;
    private final JButton pollButton;
    private final DocumentTable table;


    public MainDialog(final Gui gui, final SecureStore store) {
        this.setTitle("Portalsammler");
        this.setSize(800, 600);
        this.gui = gui;
        this.table = new DocumentTable(gui, store);

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.filterField = new JTextField();
        this.filterField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainDialog.this.filter();
            }
        });

        final PanelBuilder fpb = new PanelBuilder(new FormLayout(
                "right:pref, 4dlu, fill:pref:grow", "p"));
        fpb.addLabel("&Filter", CC.xy(1, 1));
        fpb.add(this.filterField, CC.xy(3, 1));

        final ButtonBarBuilder bbb = new ButtonBarBuilder();
        this.pollButton = this.createPollButton();
        bbb.addButton(this.pollButton, this.createConfigButton());

        final PanelBuilder builder = new PanelBuilder(new FormLayout(
                "4dlu, fill:pref:grow, 4dlu",
                "4dlu, p, 4dlu, fill:50dlu:grow, 4dlu, p, 4dlu"));
        builder.add(fpb.getPanel(), CC.xy(2, 2));
        builder.add(this.table.createWrappedPanel(), CC.xy(2, 4));
        builder.add(bbb.getPanel(), CC.xy(2, 6));

        this.setContentPane(builder.getPanel());
        this.setLocationRelativeTo(this.getOwner());
    }

    private void filter() {
        this.table.changeFilter(DocumentFilterParser.parse(this.filterField.getText()));
    }

    private JButton createConfigButton() {
        final JButton button = new JButton("Konfiguration...");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JPopupMenu menu = MainDialog.this.createConfigMenu();
                final JButton source = (JButton) e.getSource();
                menu.show(source, 0, source.getHeight());
            }
        });
        return button;
    }

    private JPopupMenu createConfigMenu() {
        final JPopupMenu menu = new JPopupMenu();

        final JMenuItem sourceConfig = new JMenuItem("Quellen verwalten...");
        sourceConfig.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainDialog.this.gui.showConfigGui(MainDialog.this.getStore());
            }
        });
        menu.add(sourceConfig);

        final JMenuItem changePassword = new JMenuItem("Neues Passwort...");
        changePassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    MainDialog.this.changePassword();
                } catch (final GeneralSecurityException ex) {
                    MainDialog.this.gui.showError(ex);
                } catch (final IOException ex) {
                    MainDialog.this.gui.showError(ex);
                }
            }
        });
        menu.add(changePassword);

        return menu;
    }

    private JButton createPollButton() {
        final JButton button = new JButton("Dokumente abrufen");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainDialog.this.poll(MainDialog.this.gui);
            }
        });
        return button;
    }

    private void poll(final Gui gui) {

        this.pollButton.setEnabled(false);

        final Settings settings = this.getStore().getSettings().deepClone();
        final ProgressMonitor progress = new ProgressMonitor(
                this, "Sammle Daten aus den Quell-Portalen...", "...", 0, settings.getSize());
        progress.setMillisToDecideToPopup(0);
        progress.setMillisToPopup(0);
        progress.setProgress(0);

        final SwingWorker<String, String> task = new SwingWorker<String, String>() {

            @Override
            protected String doInBackground() throws Exception {
                final StringBuilder summary = new StringBuilder();
                int cnt = 0;
                for (final String id : settings.getAllSettingIds()) {
                    if (this.isCancelled()) {
                        break;
                    }
                    cnt++;
                    this.publish(cnt + ": " + id);
                    final Pair<Integer, Integer> counts = MainDialog.this.pollSingleSource(settings, id);
                    summary.append(id).append(": ");
                    if (counts != null) {
                        summary.append(counts.getLeft()).append(" neu, ")
                            .append(counts.getRight()).append(" schon bekannt\n");
                    } else {
                        summary.append("Fehler!\n");
                    }
                    this.setProgress(cnt);
                }
                MainDialog.this.getStore().writeMetadata();
                return summary.toString();
            }

            @Override
            protected void process(final List<String> ids) {
                progress.setNote(ids.get(ids.size() - 1));
            }

            @Override
            public void done() {
                MainDialog.this.pollButton.setEnabled(true);
                MainDialog.this.table.refreshContents();
                try {
                    final String summary = this.get();
                    JOptionPane.showMessageDialog(MainDialog.this, summary, "Abruf-Zusammenfassung",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (final Exception e) {
                    gui.showError(e);
                }
            }

        };

        task.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    progress.setProgress((Integer) evt.getNewValue());
                }
                if (progress.isCanceled()) {
                    task.cancel(true);
                }
            }
        });

        task.execute();
    }

    private Pair<Integer, Integer> pollSingleSource(final Settings settings, final String id) {
        final SourceSettings s = settings.getSettings(id);
        final DocumentSourceFactory factory = SourceFactories.getByName(s.get(SourceFactories.TYPE, this.gui));
        try {
            return factory.create(id).poll(s, this.gui, this.getStore());
        } catch (final Exception e) {
            this.gui.showError(e);
            return null;
        }
    }

    private void changePassword() throws GeneralSecurityException, IOException {
        final SecureRandom srand = new SecureRandom();
        final byte[] key = CryptoHelper.generateKey(srand);
        this.gui.showGeneratedPassword(CryptoHelper.keyToString(key));

        while (true) {
            final String enteredPw = this.gui.askForPassword(this.getStore().getDirectory());
            if (enteredPw == null) {
                //Abbruch durch den Benutzer
                return;
            }
            if (Arrays.equals(key, CryptoHelper.keyFromString(enteredPw))) {
                //alles OK => ändern kann abgeschlossen werden
                break;
            }
            JOptionPane.showMessageDialog(this, "Das neue Passwort wurde nicht korrekt eingegeben.",
                    "Falsches Passwort", JOptionPane.ERROR_MESSAGE, null);
        }

        this.table.setStore(this.getStore().recrypt(key));
    }

    private SecureStore getStore() {
        return this.table.getStore();
    }

}
