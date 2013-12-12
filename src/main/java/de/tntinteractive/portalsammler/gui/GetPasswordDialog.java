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
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.Timer;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public final class GetPasswordDialog extends JDialog {

    private static final long serialVersionUID = 8141959875687165896L;

    private final JPasswordField passwordField;
    private String password;

    private final Webcam webcam;
    private Timer timer;


    public GetPasswordDialog(final String storeIdentifier) {
        this.setTitle("Passwortprüfung");
        this.setModal(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.webcam = Webcam.getDefault();

        final JComponent webcamPanel;
        if (this.webcam != null) {
            webcamPanel = new WebcamPanel(this.webcam);
            this.timer = new Timer(200, new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    GetPasswordDialog.this.tryScan();
                }
            });
            this.timer.start();
        } else {
            webcamPanel = new JLabel("Keine Webcam gefunden!");
        }

        final PanelBuilder fields = new PanelBuilder(new FormLayout(
                "p, 4dlu, fill:p:grow", "p"));
        fields.addLabel("&Passwort für " + storeIdentifier);
        this.passwordField = new JPasswordField();
        fields.add(this.passwordField, CC.xy(3, 1));

        final ButtonBarBuilder bbb = new ButtonBarBuilder();
        bbb.addGlue();
        final JButton okButton = this.createOkButton();
        bbb.addButton(okButton, this.createCancelButton());
        this.getRootPane().setDefaultButton(okButton);

        final PanelBuilder panelBuilder = new PanelBuilder(new FormLayout(
                "4dlu, fill:p:grow, 4dlu", "4dlu, p, 4dlu, p, 4dlu, p, 4dlu"));
        panelBuilder.add(fields.build(), CC.xy(2, 2));
        panelBuilder.add(webcamPanel, CC.xy(2, 4));
        panelBuilder.add(bbb.build(), CC.xy(2, 6));
        this.setContentPane(panelBuilder.getPanel());

        this.pack();
        this.setLocationRelativeTo(this.getOwner());
    }

    private JButton createOkButton() {
        final JButton ret = new JButton("OK");
        ret.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                GetPasswordDialog.this.password = String.valueOf(GetPasswordDialog.this.passwordField.getPassword());
                GetPasswordDialog.this.close();
            }
        });
        return ret;
    }

    private JButton createCancelButton() {
        final JButton ret = new JButton("Abbrechen");
        ret.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                GetPasswordDialog.this.close();
            }
        });
        return ret;
    }

    private void tryScan() {
        final QRCodeReader r = new QRCodeReader();
        final BufferedImage image = this.webcam.getImage();
        final LuminanceSource source = new BufferedImageLuminanceSource(image);
        final BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            final Result res = r.decode(bitmap);
            this.password = res.getText();
            this.close();
        } catch (final NotFoundException e) {
        } catch (final ChecksumException e) {
        } catch (final FormatException e) {
        }
    }

    public String getPassword() {
        return this.password;
    }

    private void close() {
        if (this.timer != null) {
            this.timer.stop();
        }
        GetPasswordDialog.this.setVisible(false);

        if (this.webcam != null) {
            //Webcam noch eine Zeit offen lassen, damit sie gesperrt bleibt
            final Timer delayedClose = new Timer(5000, new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    GetPasswordDialog.this.webcam.close();
                }
            });
            delayedClose.setRepeats(false);
            delayedClose.start();
        }
    }
}
