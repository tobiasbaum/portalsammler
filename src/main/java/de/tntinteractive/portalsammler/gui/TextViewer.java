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

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class TextViewer extends JFrame {

    private static final long serialVersionUID = -159615766685799753L;


    public TextViewer(final String title, final String text) {
        super(title);

        final JTextArea contentArea = new JTextArea(text);
        contentArea.setEditable(false);

        final PanelBuilder builder = new PanelBuilder(new FormLayout(
                "4dlu, fill:350dlu:grow, 4dlu",
                "4dlu, fill:300dlu:grow, 4dlu"));
        builder.add(new JScrollPane(contentArea), CC.xy(2, 2));
        this.setContentPane(builder.getPanel());

        this.pack();
    }

}
