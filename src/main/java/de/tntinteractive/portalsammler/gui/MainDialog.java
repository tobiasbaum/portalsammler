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

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.tntinteractive.portalsammler.engine.DocumentFilter;
import de.tntinteractive.portalsammler.engine.DocumentFilterParser;
import de.tntinteractive.portalsammler.engine.DocumentInfo;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.ShouldNotHappenException;
import de.tntinteractive.portalsammler.engine.SourceSettings;
import de.tntinteractive.portalsammler.sources.DocumentSourceFactory;

public class MainDialog extends JFrame {

    private static final long serialVersionUID = -2309663260423505246L;

    private final Gui gui;
    private final SecureStore store;
    private final JTable table;
    private final JTextField filterField;
    private DocumentFilter currentFilter = DocumentFilter.NO_FILTER;

    public MainDialog(Gui gui, SecureStore store) {
        this.setTitle("Portalsammler");
        this.setLayout(new BorderLayout());
        this.setSize(800, 600);
        this.gui = gui;
        this.store = store;

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.filterField = new JTextField();
        this.filterField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainDialog.this.filter();
            }
        });

        final PanelBuilder fpb = new PanelBuilder(new FormLayout(
                "right:pref, 4dlu, fill:pref:grow", "p"));
        fpb.addLabel("&Filter", CC.xy(1, 1));
        fpb.add(this.filterField, CC.xy(3, 1));

        this.table = new JTable();
        this.table.setRowSelectionAllowed(true);
        this.add(new JScrollPane(this.table), BorderLayout.CENTER);
        this.fillTable();
        this.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                final JTable table =(JTable) me.getSource();
                if (me.getClickCount() == 2) {
                    final Point p = me.getPoint();
                    final int row = table.rowAtPoint(p);
                    MainDialog.this.openDocument(row);
                }
            }
        });

        final ButtonBarBuilder bbb = new ButtonBarBuilder();
        bbb.addButton(this.createConfigButton(), this.createPollButton());

        final PanelBuilder builder = new PanelBuilder(new FormLayout(
                "4dlu, fill:pref:grow, 4dlu",
                "4dlu, p, 4dlu, fill:50dlu:grow, 4dlu, p, 4dlu"));
        builder.add(fpb.getPanel(), CC.xy(2, 2));
        builder.add(new JScrollPane(this.table), CC.xy(2, 4));
        builder.add(bbb.getPanel(), CC.xy(2, 6));

        this.setContentPane(builder.getPanel());
    }

    private void filter() {
        this.currentFilter = DocumentFilterParser.parse(this.filterField.getText());
        this.fillTable();
    }

    private void openDocument(int row) {
        try {
            final DocumentInfo di = ((DocumentTableModel) this.table.getModel()).data.get(row);
            final byte[] content = this.store.getDocument(di);
            this.gui.showDocument(di, content);
        } catch (final IOException e) {
            this.gui.showError(e);
        }
    }

    private static class DocumentTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 8074878200701440324L;

        private final List<DocumentInfo> data;

        public DocumentTableModel(Collection<DocumentInfo> documents) {
            this.data = new ArrayList<DocumentInfo>(documents);
        }

        @Override
        public int getRowCount() {
            return this.data.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
            case 0:
                return "Datum";
            case 1:
                return "Quelle";
            case 2:
                return "Stichworte";
            default:
                throw new ShouldNotHappenException("invalid index " + columnIndex);
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            final DocumentInfo di = this.data.get(rowIndex);
            switch (columnIndex) {
            case 0:
                return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(di.getDate());
            case 1:
                return di.getSourceId();
            case 2:
                return di.getKeywords();
            default:
                throw new ShouldNotHappenException("invalid index " + columnIndex);
            }
        }

    }

    private void fillTable() {
        final Collection<DocumentInfo> allDocuments = this.store.getIndex().getAllDocuments();
        final List<DocumentInfo> filteredDocuments = new ArrayList<DocumentInfo>();
        for (final DocumentInfo d : allDocuments) {
            if (this.currentFilter.shallShow(d)) {
                filteredDocuments.add(d);
            }
        }
        this.table.setModel(new DocumentTableModel(filteredDocuments));
    }

    private JButton createConfigButton() {
        final JButton button = new JButton("Quellen konfigurieren...");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainDialog.this.gui.showConfigGui(MainDialog.this.store);
            }
        });
        return button;
    }

    private JButton createPollButton() {
        final JButton button = new JButton("Abrufen");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainDialog.this.poll(MainDialog.this.gui);
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
        this.fillTable();
    }

}
