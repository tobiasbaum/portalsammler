package de.tntinteractive.portalsammler.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

public class PDFViewer extends JFrame
{
    private final Image image;

    public PDFViewer(String title, PDFFile pdfFile)
    {
        super(title);

        final int numpages = pdfFile.getNumPages();

        final PDFPage page = pdfFile.getPage(1);

        final Rectangle2D r2d = page.getBBox();

        double width = r2d.getWidth();
        double height = r2d.getHeight();
        width /= 72.0;
        height /= 72.0;
        final int res = Toolkit.getDefaultToolkit().getScreenResolution();
        width *= res;
        height *= res;

        this.image = page.getImage((int) width,(int) height, r2d, null, true, true);

        final JLabel label = new JLabel(new ImageIcon(this.image));
        label.setVerticalAlignment(JLabel.TOP);

        this.setContentPane(new JScrollPane(label));

        this.pack();
    }

}