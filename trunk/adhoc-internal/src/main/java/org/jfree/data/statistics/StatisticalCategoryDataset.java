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
 * -------------------------------
 * StatisticalCategoryDataset.java
 * -------------------------------
 * (C) Copyright 2002-2009, by Pascal Collet and Contributors.
 *
 * Original Author:  Pascal Collet;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 21-Aug-2002 : Version 1, contributed by Pascal Collet (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 24-Oct-2002 : Amendments in line with changes to the CategoryDataset
 *               interface (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags from all over JFreeChart sources (DG);
 *
 */

package org.jfree.data.statistics;

import org.jfree.data.category.CategoryDataset;

/**
 * A category dataset that defines a mean and standard deviation value for
 * each item.
 */
public interface StatisticalCategoryDataset extends CategoryDataset {

    /**
     * Returns the mean value for an item.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The mean value (possibly <code>null</code>).
     */
    public Number getMeanValue(int row, int column);

    /**
     * Returns the mean value for an item.
     *
     * @param rowKey  the row key.
     * @param columnKey  the columnKey.
     *
     * @return The mean value (possibly <code>null</code>).
     */
    public Number getMeanValue(Comparable rowKey, Comparable columnKey);

    /**
     * Returns the standard deviation value for an item.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The standard deviation (possibly <code>null</code>).
     */
    public Number getStdDevValue(int row, int column);

    /**
     * Returns the standard deviation value for an item.
     *
     * @param rowKey  the row key.
     * @param columnKey  the columnKey.
     *
     * @return The standard deviation (possibly <code>null</code>).
     */
    public Number getStdDevValue(Comparable rowKey, Comparable columnKey);

}

