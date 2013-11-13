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
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

public class PDFViewer extends JFrame {

    private static final long serialVersionUID = 843311728636015602L;

    private final PDFFile pdfFile;
    private int currentPage;
    private final JLabel posLabel;
    private final JLabel contentLabel;

    public PDFViewer(String title, PDFFile pdfFile) {
        super(title);

        this.pdfFile = pdfFile;
        this.currentPage = 1;

        this.setLayout(new BorderLayout());

        this.contentLabel = new JLabel();
        this.contentLabel.setVerticalAlignment(JLabel.TOP);
        final JScrollPane scrollPane = new JScrollPane(this.contentLabel);
        this.add(scrollPane, BorderLayout.CENTER);

        this.posLabel = new JLabel();
        final JButton backwardButton = new JButton("<");
        backwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PDFViewer.this.backward();
            }
        });
        final JButton forwardButton = new JButton(">");
        forwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PDFViewer.this.forward();
            }
        });

        final JPanel navigationPanel = new JPanel();
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.LINE_AXIS));
        navigationPanel.add(backwardButton);
        navigationPanel.add(this.posLabel);
        navigationPanel.add(forwardButton);

        this.showCurrentPage();
        this.add(navigationPanel, BorderLayout.SOUTH);

        this.pack();
    }

    private void backward() {
        if (this.currentPage > 1) {
            this.currentPage--;
            this.showCurrentPage();
        }
    }

    private void forward() {
        if (this.currentPage < this.pdfFile.getNumPages()) {
            this.currentPage++;
            this.showCurrentPage();
        }
    }

    private void showCurrentPage() {
        this.posLabel.setText(this.currentPage + " / " + this.pdfFile.getNumPages());

        final PDFPage page = this.pdfFile.getPage(this.currentPage);

        final Rectangle2D r2d = page.getBBox();
        double width = r2d.getWidth();
        double height = r2d.getHeight();
        width /= 72.0;
        height /= 72.0;
        final int res = Toolkit.getDefaultToolkit().getScreenResolution();
        width *= res;
        height *= res;

        final Image image = page.getImage((int) width,(int) height, r2d, null, true, true);
        this.contentLabel.setIcon(new ImageIcon(image));

        this.validate();
    }

}