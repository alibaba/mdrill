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
 * ------------------
 * SeriesDataset.java
 * ------------------
 * (C) Copyright 2000-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 17-Nov-2001 : Version 1 (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 19-May-2005 : Changed getSeriesName() --> getSeriesKey() and added indexOf()
 *               method (DG);
 *
 */

package org.jfree.data.general;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.IntervalXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

/**
 * The interface for a dataset consisting of one or many series of data.
 *
 * @see CategoryDataset
 * @see IntervalXYDataset
 * @see IntervalXYZDataset
 * @see XYDataset
 * @see XYZDataset
 */
public interface SeriesDataset extends Dataset {

    /**
     * Returns the number of series in the dataset.
     *
     * @return The series count.
     */
    public int getSeriesCount();

    /**
     * Returns the key for a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     *
     * @return The key for the series.
     */
    public Comparable getSeriesKey(int series);

    /**
     * Returns the index of the series with the specified key, or -1 if there
     * is no such series in the dataset.
     *
     * @param seriesKey  the series key (<code>null</code> permitted).
     *
     * @return The index, or -1.
     */
    public int indexOf(Comparable seriesKey);

}
