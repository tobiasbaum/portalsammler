package de.tntinteractive.portalsammler.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import de.tntinteractive.portalsammler.engine.DocumentInfo;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SourceSettings;
import de.tntinteractive.portalsammler.sources.DocumentSourceFactory;

public class MainDialog extends JFrame {

    private final SecureStore store;
    private final JTable table;

    public MainDialog(Gui gui, SecureStore store) {
        this.setTitle("Portalsammler");
        this.setLayout(new BorderLayout());
        this.setSize(800, 600);
        this.store = store;;

        final JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.LINE_AXIS));
        filterPanel.add(new JLabel("Filter"));
        filterPanel.add(new JTextField());
        this.add(filterPanel, BorderLayout.NORTH);

        this.table = new JTable();
        this.add(new JScrollPane(this.table), BorderLayout.CENTER);
        this.fillTable(store.getIndex().getAllDocuments());

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(this.createConfigButton(gui));
        buttonPanel.add(this.createPollButton(gui));
        this.add(buttonPanel, BorderLayout.SOUTH);

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void fillTable(Collection<DocumentInfo> documents) {
        final Object[][] data = new Object[documents.size()][3];
        int i = 0;
        for (final DocumentInfo d : documents) {
            data[i][0] = d.getDate();
            data[i][1] = d.getSourceId();
            data[i][2] = d.getKeywords();
            i++;
        }
        final TableModel dataModel = new DefaultTableModel(data, new String[] {"Datum", "Quelle", "Stichworte"});
        this.table.setModel(dataModel);
    }

    private JButton createConfigButton(final Gui gui) {
        final JButton button = new JButton("Quellen konfigurieren...");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.showConfigGui(MainDialog.this.store);
            }
        });
        return button;
    }

    private JButton createPollButton(final Gui gui) {
        final JButton button = new JButton("Abrufen");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainDialog.this.poll(gui);
            }
        });
        return button;
    }

    private void poll(final Gui gui) {
        for (final String id : this.store.getSettings().getAllSettingIds()) {
            final SourceSettings s = this.store.getSettings().getSettings(id);
            final DocumentSourceFactory factory = SourceFactories.getByName(s.get(SourceFactories.TYPE));
            try {
                factory.create(id).poll(s, this.store);
            } catch (final Exception e) {
                gui.showError(e);
            }
        }

        try {
            this.store.writeMetadata();
        } catch (final IOException e) {
            gui.showError(e);
        } catch (final GeneralSecurityException e) {
            gui.showError(e);
        }
        this.fillTable(this.store.getIndex().getAllDocuments());
    }

}
