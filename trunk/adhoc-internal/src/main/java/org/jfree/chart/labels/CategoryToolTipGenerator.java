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
 * -----------------------------
 * CategoryToolTipGenerator.java
 * -----------------------------
 * (C) Copyright 2001-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 15-Apr-2004 : Separated tool tip method from CategoryItemLabelGenerator
 *               interface (DG);
 *
 */

package org.jfree.chart.labels;

import org.jfree.data.category.CategoryDataset;

/**
 * A <i>category tool tip generator</i> is an object that can be assigned to a
 * {@link org.jfree.chart.renderer.category.CategoryItemRenderer} and that
 * assumes responsibility for creating text items to be used as tooltips for the
 * items in a {@link org.jfree.chart.plot.CategoryPlot}.
 * <p>
 * To assist with cloning charts, classes that implement this interface should
 * also implement the <code>org.jfree.util.PublicCloneable</code> interface (in
 * JCommon).
 */
public interface CategoryToolTipGenerator {

    /**
     * Generates the tool tip text for an item in a dataset.  Note: in the
     * current dataset implementation, each row is a series, and each column
     * contains values for a particular category.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The tooltip text (possibly <code>null</code>).
     */
    public String generateToolTip(CategoryDataset dataset, int row, int column);

}
