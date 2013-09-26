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
 * -------------------
 * TextBlockPanel.java
 * -------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: TextBlockPanel.java,v 1.4 2007/11/02 17:50:35 taqua Exp $
 *
 * Changes
 * -------
 * 07-Jan-2004 : Version 1;
 *
 */

package org.jfree.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

import org.jfree.text.G2TextMeasurer;
import org.jfree.text.TextBlock;
import org.jfree.text.TextBlockAnchor;
import org.jfree.text.TextUtilities;

/**
 * A panel used by the {@link DrawStringDemo} class.
 *
 * @author David Gilbert
 */
public class TextBlockPanel extends JPanel {

    /** The preferred size for the panel. */
    private static final Dimension PREFERRED_SIZE = new Dimension(500, 300);

    /** The text to display. */
    private String text;
    
    /** The font to use. */
    private Font font;

    /**
     * Creates a new panel.
     *
     * @param text  the text.
     * @param font  the font.
     */
    public TextBlockPanel(final String text, final Font font) {
        this.text = text;
        this.font = font;
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
        final Rectangle2D available = new Rectangle2D.Double(insets.left, insets.top,
                                      size.getWidth() - insets.left - insets.right,
                                      size.getHeight() - insets.top - insets.bottom);

        final double x = available.getX();
        final double y = available.getY();
        final float width = (float) available.getWidth();
        final TextBlock block = TextUtilities.createTextBlock(
            this.text, this.font, Color.black, width, new G2TextMeasurer(g2)
        );
        g2.setPaint(Color.black);
        block.draw(g2, (float) x, (float) y, TextBlockAnchor.TOP_LEFT, 0.0f, 0.0f, 0.0);

    }

}
