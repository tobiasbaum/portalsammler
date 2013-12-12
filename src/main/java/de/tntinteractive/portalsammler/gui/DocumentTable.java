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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.io.FileUtils;

import de.tntinteractive.portalsammler.engine.DocumentFilter;
import de.tntinteractive.portalsammler.engine.DocumentInfo;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.ShouldNotHappenException;

final class DocumentTable {

    private final Gui gui;
    private final JTable table;

    private DocumentFilter currentFilter = DocumentFilter.NO_FILTER;

    private SecureStore store;

    public DocumentTable(final Gui gui, final SecureStore store) {
        this.gui = gui;
        this.store = store;

        this.table = new JTable();
        this.table.setRowSelectionAllowed(true);
        this.refreshContents();
        this.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent me) {
                final int r = DocumentTable.this.table.rowAtPoint(me.getPoint());
                if (!DocumentTable.this.table.getSelectionModel().isSelectedIndex(r)) {
                    if (r >= 0 && r < DocumentTable.this.table.getRowCount()) {
                        DocumentTable.this.table.setRowSelectionInterval(r, r);
                    } else {
                        DocumentTable.this.table.clearSelection();
                    }
                }

                if (me.isPopupTrigger()) {
                    DocumentTable.this.showPopup(me);
                } else if (me.getClickCount() == 2) {
                    DocumentTable.this.openSelectedRows();
                }
            }
            @Override
            public void mouseReleased(final MouseEvent me) {
                if (me.isPopupTrigger()) {
                    DocumentTable.this.showPopup(me);
                }
            }
        });
        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        this.table.getColumnModel().getColumn(0).setPreferredWidth(120);
        this.table.getColumnModel().getColumn(1).setPreferredWidth(80);
        this.table.getColumnModel().getColumn(2).setPreferredWidth(100);
        this.table.getColumnModel().getColumn(3).setPreferredWidth(500);
    }

    private void showPopup(final MouseEvent ev) {
        final JMenuItem open = new JMenuItem("Anzeigen");
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DocumentTable.this.openSelectedRows();
            }
        });

        final JMenuItem export = new JMenuItem("Exportieren");
        export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DocumentTable.this.exportSelectedRows();
            }
        });

        final JPopupMenu menu = new JPopupMenu();
        menu.add(open);
        menu.add(export);
        menu.show(ev.getComponent(), ev.getX(), ev.getY());
    }

    private static class DocumentTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 8074878200701440324L;

        private final SecureStore store;
        private final List<DocumentInfo> data;

        public DocumentTableModel(final SecureStore store, final Collection<DocumentInfo> documents) {
            this.store = store;
            this.data = new ArrayList<DocumentInfo>(documents);
        }

        @Override
        public int getRowCount() {
            return this.data.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(final int columnIndex) {
            switch (columnIndex) {
            case 0:
                return "Datum";
            case 1:
                return "ungelesen?";
            case 2:
                return "Quelle";
            case 3:
                return "Stichworte";
            default:
                throw new ShouldNotHappenException("invalid index " + columnIndex);
            }
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            final DocumentInfo di = this.data.get(rowIndex);
            switch (columnIndex) {
            case 0:
                return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(di.getDate());
            case 1:
                return this.store.isRead(di) ? "" : "X";
            case 2:
                return di.getSourceId();
            case 3:
                return di.getKeywords();
            default:
                throw new ShouldNotHappenException("invalid index " + columnIndex);
            }
        }

    }

    void refreshContents() {
        final int[] widths = this.saveColumnWidths();

        final Collection<DocumentInfo> allDocuments = this.store.getIndex().getAllDocuments();
        final List<DocumentInfo> filteredDocuments = new ArrayList<DocumentInfo>();
        for (final DocumentInfo d : allDocuments) {
            if (this.currentFilter.shallShow(d)) {
                filteredDocuments.add(d);
            }
        }
        this.table.setModel(new DocumentTableModel(this.store, filteredDocuments));
        this.restoreColumnWidths(widths);
    }

    private int[] saveColumnWidths() {
        final int[] widths = new int[this.table.getColumnCount()];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = this.table.getColumnModel().getColumn(i).getPreferredWidth();
        }
        return widths;
    }

    private void restoreColumnWidths(final int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            this.table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    void openSelectedRows() {
        for (final int row : this.table.getSelectedRows()) {
            this.openDocument(row);
        }
        this.refreshContents();
    }

    protected void exportSelectedRows() {
        final JFileChooser chooser = new JFileChooser();
        for (final int row : this.table.getSelectedRows()) {
            final boolean cancel = this.exportDocument(chooser, row);
            if (cancel) {
                break;
            }
        }
        this.refreshContents();
    }

    private void openDocument(final int row) {
        try {
            final DocumentInfo di = this.getDocumentInRow(row);
            final byte[] content = this.store.getDocument(di);
            this.store.markAsRead(di);
            this.gui.showDocument(di, content);
        } catch (final IOException e) {
            this.gui.showError(e);
        }
    }

    private boolean exportDocument(final JFileChooser chooser, final int row) {
        try {
            final DocumentInfo di = this.getDocumentInRow(row);
            final byte[] content = this.store.getDocument(di);

            chooser.setSelectedFile(new File(this.makeFilenameFor(di)));
            final int result = chooser.showSaveDialog(this.table);
            if (result != JFileChooser.APPROVE_OPTION) {
                return true;
            }

            FileUtils.writeByteArrayToFile(chooser.getSelectedFile(), content);
            this.store.markAsRead(di);
            return false;
        } catch (final IOException e) {
            this.gui.showError(e);
            return false;
        }
    }

    private String makeFilenameFor(final DocumentInfo di) {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return this.makeValidFilename(di.getSourceId()
                    + ", " + format.format(di.getDate())
                    + ", " + this.cutToSize(di.getKeywords(), 30))
                + "." + di.getFormat().getExtension();
    }

    private String cutToSize(final String s, final int i) {
        if (s.length() > i) {
            return s.substring(0, i);
        } else {
            return s;
        }
    }

    private String makeValidFilename(final String string) {
        return string.replaceAll("[|:<>\\/\n\r\t]+", " ");
    }

    private DocumentInfo getDocumentInRow(final int row) {
        return ((DocumentTableModel) this.table.getModel()).data.get(row);
    }

    public JScrollPane createWrappedPanel() {
        return new JScrollPane(this.table);
    }

    public void changeFilter(final DocumentFilter newFilter) {
        this.currentFilter = newFilter;
        this.refreshContents();
    }

    public void setStore(final SecureStore newStore) {
        this.store = newStore;
        this.refreshContents();
    }

    SecureStore getStore() {
        return this.store;
    }

}
