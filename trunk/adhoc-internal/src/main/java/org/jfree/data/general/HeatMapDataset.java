/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
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
 * HeatMapDataset.java
 * -------------------
 * (C) Copyright 2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 28-Jan-2009 : Version 1 (DG);
 *
 */

package org.jfree.data.general;

/**
 * A dataset that represents a rectangular grid of (x, y, z) values.  The x
 * and y values appear at regular intervals in the dataset, while the z-values
 * can take any value (including <code>null</code> for unknown values).
 *
 * @since 1.0.13
 */
public interface HeatMapDataset {

    /**
     * Returns the number of x values across the width of the dataset.  The
     * values are evenly spaced between {@link #getMinimumXValue()} and
     * {@link #getMaximumXValue()}.
     * 
     * @return The number of x-values (always > 0).
     */
    public int getXSampleCount();

    /**
     * Returns the number of y values (or samples) for the dataset.  The
     * values are evenly spaced between {@link #getMinimumYValue()} and
     * {@link #getMaximumYValue()}.
     *
     * @return The number of y-values (always > 0).
     */
    public int getYSampleCount();

    /**
     * Returns the lowest x-value represented in this dataset.  A requirement
     * of this interface is that this method must never return infinite or
     * Double.NAN values.
     *
     * @return The lowest x-value represented in this dataset.
     */
    public double getMinimumXValue();

    /**
     * Returns the highest x-value represented in this dataset.  A requirement
     * of this interface is that this method must never return infinite or
     * Double.NAN values.
     *
     * @return The highest x-value represented in this dataset.
     */
    public double getMaximumXValue();

    /**
     * Returns the lowest y-value represented in this dataset.  A requirement
     * of this interface is that this method must never return infinite or
     * Double.NAN values.
     *
     * @return The lowest y-value represented in this dataset.
     */
    public double getMinimumYValue();

    /**
     * Returns the highest y-value represented in this dataset.  A requirement
     * of this interface is that this method must never return infinite or
     * Double.NAN values.
     *
     * @return The highest y-value represented in this dataset.
     */
    public double getMaximumYValue();

    /**
     * A convenience method that returns the x-value for the given index.
     * 
     * @param xIndex  the xIndex.
     * 
     * @return The x-value.
     */
    public double getXValue(int xIndex);

    /**
     * A convenience method that returns the y-value for the given index.
     * 
     * @param yIndex  the yIndex.
     * 
     * @return The y-value.
     */
    public double getYValue(int yIndex);

    /**
     * Returns the z-value at the specified sample position in the dataset.
     * For a missing or unknown value, this method should return Double.NAN.
     *
     * @param xIndex  the position of the x sample in the dataset.
     * @param yIndex  the position of the y sample in the dataset.
     *
     * @return The z-value.
     */
    public double getZValue(int xIndex, int yIndex);

    /**
     * Returns the z-value at the specified sample position in the dataset.
     * This method can return <code>null</code> to indicate a missing/unknown
     * value.
     * <br><br>
     * Bear in mind that the class implementing this interface may
     * store its data using primitives rather than objects, so calling this
     * method may require a new <code>Number</code> object to be allocated...
     * for this reason, it is generally preferable to use the
     * {@link #getZValue(int, int)} method unless you *know* that the dataset
     * implementation stores the z-values using objects.
     *
     * @param xIndex  the position of the x sample in the dataset.
     * @param yIndex  the position of the y sample in the dataset.
     *
     * @return The z-value (possibly <code>null</code>).
     */
    public Number getZ(int xIndex, int yIndex);

}
