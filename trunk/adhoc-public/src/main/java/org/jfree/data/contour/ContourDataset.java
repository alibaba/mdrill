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
 * -------------------
 * ContourDataset.java
 * -------------------
 * (C) Copyright 2002-2008, by David M. O'Donnell and Contributors.
 *
 * Original Author:  David M. O'Donnell;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes (from 23-Jan-2003)
 * --------------------------
 * 23-Jan-2003 : Added standard header (DG);
 * 17-Jan-2004 : Added methods from DefaultContourDataset that are referenced
 *               by ContourPlot.  See bug 741048 (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 31-Jan-2007 : Deprecated (DG);
 *
 */

package org.jfree.data.contour;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYZDataset;

/**
 * The interface through which JFreeChart obtains data in the form of (x, y, z)
 * items - used for XY and XYZ plots.
 *
 * @deprecated This interface is no longer supported (as of version 1.0.4).
 *     If you are creating contour plots, please try to use {@link XYPlot} and
 *     {@link XYBlockRenderer}.
 */
public interface ContourDataset extends XYZDataset {

    /**
     * Returns the smallest Z data value.
     *
     * @return The minimum Z value.
     */
    public double getMinZValue();

    /**
     * Returns the largest Z data value.
     *
     * @return The maximum Z value.
     */
    public double getMaxZValue();

    /**
     * Returns the array of Numbers representing the x data values.
     *
     * @return The array of x values.
     */
    public Number[] getXValues();

    /**
     * Returns the array of Numbers representing the y data values.
     *
     * @return The array of y values.
     */
    public Number[] getYValues();

    /**
     * Returns the array of Numbers representing the z data values.
     *
     * @return The array of z values.
     */
    public Number[] getZValues();

    /**
     * Returns an int array contain the index into the x values.
     *
     * @return The X values.
     */
    public int[] indexX();

    /**
     * Returns the index of the xvalues.
     *
     * @return The x values.
     */
    public int[] getXIndices();

    /**
     * Returns the maximum z-value within visible region of plot.
     *
     * @param x  the x-value.
     * @param y  the y-value.
     *
     * @return The maximum z-value.
     */
    public Range getZValueRange(Range x, Range y);

    /**
     * Returns true if axis are dates.
     *
     * @param axisNumber  the axis where 0-x, 1-y, and 2-z.
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean isDateAxis(int axisNumber);

}
