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
 * ---------------
 * ArrowPanel.java
 * ---------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: ArrowPanel.java,v 1.6 2007/11/02 17:50:36 taqua Exp $
 *
 * Changes
 * -------
 * 25-Sep-2002 : Version 1 (DG);
 * 13-Oct-2002 : Added Javadocs (DG);
 *
 */

package org.jfree.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

/**
 * A basic panel that displays a small up or down arrow.
 *
 * @author David Gilbert
 */
public class ArrowPanel extends JPanel {

    /** A constant for the up arrow. */
    public static final int UP = 0;

    /** A constant for the down arrow. */
    public static final int DOWN = 1;

    /** The arrow type. */
    private int type = UP;

    /** The available area. */
    private Rectangle2D available = new Rectangle2D.Float();

    /**
     * Creates a new arrow panel.
     *
     * @param type  the arrow type.
     */
    public ArrowPanel(final int type) {
        this.type = type;
        setPreferredSize(new Dimension(14, 9));
    }

    /**
     * Paints the arrow panel.
     *
     * @param g  the graphics device for drawing on.
     */
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g;

        // first determine the size of the drawing area...
        final Dimension size = getSize();
        final Insets insets = getInsets();
        this.available.setRect(insets.left, insets.top,
                               size.getWidth() - insets.left - insets.right,
                               size.getHeight() - insets.top - insets.bottom);
        g2.translate(insets.left, insets.top);
        g2.fill(getArrow(this.type));

    }

    /**
     * Returns a shape for the arrow.
     *
     * @param t  the arrow type.
     *
     * @return the arrow shape.
     */
    private Shape getArrow(final int t) {
        switch (t) {
            case UP : return getUpArrow();
            case DOWN : return getDownArrow();
            default : return getUpArrow();
        }
    }

    /**
     * Returns an up arrow.
     *
     * @return an up arrow.
     */
    private Shape getUpArrow() {
        final Polygon result = new Polygon();
        result.addPoint(7, 2);
        result.addPoint(2, 7);
        result.addPoint(12, 7);
        return result;
    }

    /**
     * Returns a down arrow.
     *
     * @return a down arrow.
     */
    private Shape getDownArrow() {
        final Polygon result = new Polygon();
        result.addPoint(7, 7);
        result.addPoint(2, 2);
        result.addPoint(12, 2);
        return result;
    }

}
