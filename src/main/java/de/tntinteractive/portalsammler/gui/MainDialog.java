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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.tntinteractive.portalsammler.engine.DocumentFilter;
import de.tntinteractive.portalsammler.engine.DocumentFilterParser;
import de.tntinteractive.portalsammler.engine.DocumentInfo;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.Settings;
import de.tntinteractive.portalsammler.engine.ShouldNotHappenException;
import de.tntinteractive.portalsammler.engine.SourceSettings;
import de.tntinteractive.portalsammler.sources.DocumentSourceFactory;

public class MainDialog extends JFrame {

    private static final long serialVersionUID = -2309663260423505246L;

    private final Gui gui;
    private final SecureStore store;
    private final JTable table;
    private final JTextField filterField;
    private final JButton pollButton;

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
                final int r = MainDialog.this.table.rowAtPoint(me.getPoint());
                if (!MainDialog.this.table.getSelectionModel().isSelectedIndex(r)) {
                    if (r >= 0 && r < MainDialog.this.table.getRowCount()) {
                        MainDialog.this.table.setRowSelectionInterval(r, r);
                    } else {
                        MainDialog.this.table.clearSelection();
                    }
                }

                if (me.isPopupTrigger()) {
                    MainDialog.this.showPopup(me);
                } else if (me.getClickCount() == 2) {
                    MainDialog.this.openSelectedRows();
                }
            }
            @Override
            public void mouseReleased(MouseEvent me) {
                if (me.isPopupTrigger()) {
                    MainDialog.this.showPopup(me);
                }
            }
        });
        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        this.table.getColumnModel().getColumn(0).setPreferredWidth(120);
        this.table.getColumnModel().getColumn(1).setPreferredWidth(80);
        this.table.getColumnModel().getColumn(2).setPreferredWidth(100);
        this.table.getColumnModel().getColumn(3).setPreferredWidth(500);

        final ButtonBarBuilder bbb = new ButtonBarBuilder();
        this.pollButton = this.createPollButton();
        bbb.addButton(this.createConfigButton(), this.pollButton);

        final PanelBuilder builder = new PanelBuilder(new FormLayout(
                "4dlu, fill:pref:grow, 4dlu",
                "4dlu, p, 4dlu, fill:50dlu:grow, 4dlu, p, 4dlu"));
        builder.add(fpb.getPanel(), CC.xy(2, 2));
        builder.add(new JScrollPane(this.table), CC.xy(2, 4));
        builder.add(bbb.getPanel(), CC.xy(2, 6));

        this.setContentPane(builder.getPanel());
        this.setLocationRelativeTo(this.getOwner());
    }

    private void showPopup(MouseEvent ev) {
        final JMenuItem open = new JMenuItem("Anzeigen");
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainDialog.this.openSelectedRows();
            }
        });

        final JMenuItem export = new JMenuItem("Exportieren");
        export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainDialog.this.exportSelectedRows();
            }
        });

        final JPopupMenu menu = new JPopupMenu();
        menu.add(open);
        menu.add(export);
        menu.show(ev.getComponent(), ev.getX(), ev.getY());
    }

    protected void openSelectedRows() {
        for (final int row : this.table.getSelectedRows()) {
            this.openDocument(row);
        }
        this.filter();
    }

    protected void exportSelectedRows() {
        final JFileChooser chooser = new JFileChooser();
        for (final int row : this.table.getSelectedRows()) {
            final boolean cancel = this.exportDocument(chooser, row);
            if (cancel) {
                break;
            }
        }
        this.filter();
    }

    private void filter() {
        this.currentFilter = DocumentFilterParser.parse(this.filterField.getText());
        this.fillTable();
    }

    private void openDocument(int row) {
        try {
            final DocumentInfo di = this.getDocumentInRow(row);
            final byte[] content = this.store.getDocument(di);
            this.store.markAsRead(di);
            this.gui.showDocument(di, content);
        } catch (final IOException e) {
            this.gui.showError(e);
        }
    }

    private boolean exportDocument(JFileChooser chooser, int row) {
        try {
            final DocumentInfo di = this.getDocumentInRow(row);
            final byte[] content = this.store.getDocument(di);

            chooser.setSelectedFile(new File(this.makeFilenameFor(di)));
            final int result = chooser.showSaveDialog(this);
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

    private String makeFilenameFor(DocumentInfo di) {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return this.makeValidFilename(di.getSourceId()
                    + ", " + format.format(di.getDate())
                    + ", " + this.cutToSize(di.getKeywords(), 30))
                + "." + di.getFormat().getExtension();
    }

    private String cutToSize(String s, int i) {
        if (s.length() > i) {
            return s.substring(0, i);
        } else {
            return s;
        }
    }

    private String makeValidFilename(String string) {
        return string.replaceAll("[|:<>\\/\n\r\t]+", " ");
    }

    private DocumentInfo getDocumentInRow(int row) {
        return ((DocumentTableModel) this.table.getModel()).data.get(row);
    }

    private static class DocumentTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 8074878200701440324L;

        private final SecureStore store;
        private final List<DocumentInfo> data;

        public DocumentTableModel(SecureStore store, Collection<DocumentInfo> documents) {
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
        public String getColumnName(int columnIndex) {
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
        public Object getValueAt(int rowIndex, int columnIndex) {
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

    private void fillTable() {
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

    private void restoreColumnWidths(int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            this.table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
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

        this.pollButton.setEnabled(false);

        final Settings settings = this.store.getSettings().deepClone();
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
                MainDialog.this.store.writeMetadata();
                return summary.toString();
            }

            @Override
            protected void process(List<String> ids) {
                progress.setNote(ids.get(ids.size() - 1));
            }

            @Override
            public void done() {
                MainDialog.this.pollButton.setEnabled(true);
                MainDialog.this.fillTable();
                try {
                    final String summary = this.get();
                    JOptionPane.showMessageDialog(MainDialog.this, summary, "Abruf-Zusammenfassung", JOptionPane.INFORMATION_MESSAGE);
                } catch (final Exception e) {
                    gui.showError(e);
                }
            }

        };

        task.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
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

    private Pair<Integer, Integer> pollSingleSource(Settings settings, String id) {
        final SourceSettings s = settings.getSettings(id);
        final DocumentSourceFactory factory = SourceFactories.getByName(s.get(SourceFactories.TYPE, this.gui));
        try {
            return factory.create(id).poll(s, this.gui, this.store);
        } catch (final Exception e) {
            this.gui.showError(e);
            return null;
        }
    }

}
