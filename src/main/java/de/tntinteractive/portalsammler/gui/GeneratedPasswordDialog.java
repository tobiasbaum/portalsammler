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

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class GeneratedPasswordDialog extends JDialog {

    private static final long serialVersionUID = -1276429595586985556L;

    public GeneratedPasswordDialog(final UserInteraction gui, String key) {
        this.setTitle("Neues Passwort generiert");
        this.setModal(true);

        final JLabel text = new JLabel(
                "<html><body>" +
                "Es wurde ein neues Passwort generiert:<br/>" +
                "<br/>" +
                "<b><center>" + key.replace("<", "&lt;") + "</center></b>" +
        		"<br/>" +
        		"Mit diesem Passwort sind alle gespeicherten Daten geschützt. Wenn es verloren<br/>" +
        		" geht, ist kein Zugriff auf die gespeicherten Daten mehr möglich.</br>" +
        		"<br/>" +
        		"Der unten sichtbare QR-Code enthält das Passwort in codierter Form. Sie können ihn<br/>" +
        		" ausdrucken und über eine Webcam o.ä. einlesen, damit sie sich das Passwort nicht<br/>" +
        		" merken und wiederholt eintippen müssen. Stellen Sie aber sicher, dass der QR-Code<br/>" +
        		" getrennt von Ihrem Computer gelagert wird, um einen gleichzeitigen Zugriff z.B. durch<br/>" +
        		" Einbrecher auszuschließen.<br/>" +
                "</body></html>");

        final Image qrImage = this.createQrImage(gui, key);
        final JLabel qrCode;
        if (qrImage != null) {
            qrCode = new JLabel(new ImageIcon(qrImage));
        } else {
            qrCode = new JLabel("Erstellung des QR-Codes nicht möglich!!");
        }

        final JButton printButton = new JButton("Passwort und QR-Code drucken");
        printButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GeneratedPasswordDialog.this.print(gui);
            }
        });

        final JButton closeButton = new JButton("Schließen");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GeneratedPasswordDialog.this.setVisible(false);
            }
        });

        final PanelBuilder panelBuilder = new PanelBuilder(new FormLayout(
                "4dlu, fill:p:grow, 4dlu", "4dlu, p, 4dlu, p, 4dlu, p, 4dlu, p, 4dlu"));
        panelBuilder.add(text, CC.xy(2, 2));
        panelBuilder.add(qrCode, CC.xy(2, 4));
        panelBuilder.add(printButton, CC.xy(2, 6));
        panelBuilder.add(closeButton, CC.xy(2, 8));
        this.setContentPane(panelBuilder.getPanel());
        this.setBackground(Color.WHITE);

        this.pack();
        this.setLocationRelativeTo(this.getOwner());
    }

    protected void print(UserInteraction gui) {
        final PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat page, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return NO_SUCH_PAGE;
                }
                final Graphics2D g2d = (Graphics2D) graphics;
                final Container pane = GeneratedPasswordDialog.this.getContentPane();
                g2d.translate(
                        page.getImageableX() + (page.getImageableWidth() - pane.getWidth()) / 2,
                        page.getImageableY() + (page.getImageableHeight() - pane.getHeight()) / 2);
                pane.paint(g2d);
                return PAGE_EXISTS;
            }
        });
        if (job.printDialog()) {
            try {
                job.print();
            } catch (final PrinterException e) {
                gui.showError(e);
            }
        }
    }

    private BufferedImage createQrImage(UserInteraction gui, String key) {
        try {
            final QRCodeWriter w = new QRCodeWriter();
            final BitMatrix m = w.encode(key, BarcodeFormat.QR_CODE, 200, 200);
            return MatrixToImageWriter.toBufferedImage(m);
        } catch (final WriterException e) {
            gui.showError(e);
            return null;
        }
    }

}
