/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
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
 * WindNeedle.java
 * ---------------
 * (C) Copyright 2002-2008, by the Australian Antarctic Division and
 *                          Contributors.
 *
 * Original Author:  Bryan Scott (for the Australian Antarctic Division);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes:
 * --------
 * 25-Sep-2002 : Version 1, contributed by Bryan Scott (DG);
 * 09-Sep-2003 : Added equals() method (DG);
 * 22-Nov-2007 : Implemented hashCode() (DG)
 *
 */

package org.jfree.chart.needle;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/**
 * A needle that indicates wind direction, for use with the
 * {@link org.jfree.chart.plot.CompassPlot} class.
 */
public class WindNeedle extends ArrowNeedle
                                implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -2861061368907167834L;

    /**
     * Default constructor.
     */
    public WindNeedle() {
        super(false);  // isArrowAtTop
    }

    /**
     * Draws the needle.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param rotate  the rotation point.
     * @param angle  the angle.
     */
    protected void drawNeedle(Graphics2D g2, Rectangle2D plotArea,
                              Point2D rotate, double angle) {

        super.drawNeedle(g2, plotArea, rotate, angle);
        if ((rotate != null) && (plotArea != null)) {

            int spacing = getSize() * 3;
            Rectangle2D newArea = new Rectangle2D.Double();

            Point2D newRotate = rotate;
            newArea.setRect(plotArea.getMinX() - spacing, plotArea.getMinY(),
                    plotArea.getWidth(), plotArea.getHeight());
            super.drawNeedle(g2, newArea, newRotate, angle);

            newArea.setRect(plotArea.getMinX() + spacing,
                    plotArea.getMinY(), plotArea.getWidth(),
                    plotArea.getHeight());
            super.drawNeedle(g2, newArea, newRotate, angle);

        }
    }

    /**
     * Tests another object for equality with this object.
     *
     * @param object  the object to test.
     *
     * @return A boolean.
     */
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (super.equals(object) && object instanceof WindNeedle) {
            return true;
        }
        return false;
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code.
     */
    public int hashCode() {
        return super.hashCode();
    }

}

