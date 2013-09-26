/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
 * 
 * Project Info:  http://www.jfree.org/jcommon/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 * 
 * --------------------
 * DrawStringPanel.java
 * --------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: DrawStringPanel.java,v 1.6 2007/11/02 17:50:35 taqua Exp $
 *
 * Changes
 * -------
 * 10-Jun-2003 : Version 1;
 * 30-Sep-2004 : Moved drawRotatedString() from RefineryUtilities --> TextUtilities (DG);
 *
 */
package org.jfree.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

import org.jfree.text.TextUtilities;
import org.jfree.ui.TextAnchor;

/**
 * A panel used by the {@link DrawStringDemo} class.
 * 
 * @author David Gilbert
 */
public class DrawStringPanel extends JPanel {

    /** The preferred size for the panel. */
    private static final Dimension PREFERRED_SIZE = new Dimension(500, 300);

    /** Is the text rotated. */
    private boolean rotate;

    /** The text to display. */
    private String text = "Hello World";

    /** The text anchor. */
    private TextAnchor anchor = TextAnchor.TOP_LEFT;

    /** The rotation anchor. */
    private TextAnchor rotationAnchor = TextAnchor.TOP_LEFT;

    /** The font. */
    private Font font = new Font("Serif", Font.PLAIN, 12);

    /** The rotation angle. */
    private double angle;

    /**
     * Creates a new panel.
     *
     * @param text  the text.
     * @param rotate  a flag that controls whether or not the text is rotated.
     */
    public DrawStringPanel(final String text, final boolean rotate) {
        this.text = text;
        this.rotate = rotate;
    }

    /**
     * Returns the preferred size for the panel.
     *
     * @return The preferred size.
     */
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }

    /**
     * Sets the text anchor.
     *
     * @param anchor  the text anchor.
     */
    public void setAnchor(final TextAnchor anchor) {
        this.anchor = anchor;
    }

    /**
     * Sets the rotation anchor.
     *
     * @param anchor  the rotation anchor.
     */
    public void setRotationAnchor(final TextAnchor anchor) {
        this.rotationAnchor = anchor;
    }

    /**
     * Sets the rotation angle.
     *
     * @param angle  the rotation angle.
     */
    public void setAngle(final double angle) {
        this.angle = angle;
    }

    /**
     * Returns the font.
     *
     * @return The font.
     */
    public Font getFont() {
        return this.font;
    }

    /**
     * Sets the font.
     *
     * @param font  the font.
     */
    public void setFont(final Font font) {
        this.font = font;
    }

    /**
     * Paints the panel.
     *
     * @param g  the graphics device.
     */
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g;

        final Dimension size = getSize();
        final Insets insets = getInsets();
        final Rectangle2D available = new Rectangle2D.Double(
            insets.left, insets.top,
            size.getWidth() - insets.left - insets.right,
            size.getHeight() - insets.top - insets.bottom
        );

        final double x = available.getCenterX();
        final double y = available.getCenterY();

        final Line2D line1 = new Line2D.Double(x - 2.0, y + 2.0, x + 2.0, y - 2.0);
        final Line2D line2 = new Line2D.Double(x - 2.0, y - 2.0, x + 2.0, y + 2.0);
        g2.setPaint(Color.red);
        g2.draw(line1);
        g2.draw(line2);

        g2.setFont(this.font);
        g2.setPaint(Color.black);
        if (this.rotate) {
            TextUtilities.drawRotatedString(
                this.text, g2, (float) x, (float) y,
                this.anchor, this.angle, this.rotationAnchor
            );
        }
        else {
            TextUtilities.drawAlignedString(this.text, g2, (float) x, (float) y, this.anchor);
        }

    }

}
