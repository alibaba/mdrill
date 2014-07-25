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
 * XYZDataset.java
 * ---------------
 * (C) Copyright 2001-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 31-Oct-2001 : Initial version (DG);
 * 05-May-2004 : Added getZ() method;
 * 15-Jul-2004 : Switched getZ() and getZValue() methods (DG);
 * 18-Aug-2004 : Moved from org.jfree.data --> org.jfree.data.xy (DG);
 *
 */

package org.jfree.data.xy;

/**
 * The interface through which JFreeChart obtains data in the form of (x, y, z)
 * items - used for XY and XYZ plots.
 */
public interface XYZDataset extends XYDataset {

    /**
     * Returns the z-value for the specified series and item.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The z-value (possibly <code>null</code>).
     */
    public Number getZ(int series, int item);

    /**
     * Returns the z-value (as a double primitive) for an item within a series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The z-value.
     */
    public double getZValue(int series, int item);

}
