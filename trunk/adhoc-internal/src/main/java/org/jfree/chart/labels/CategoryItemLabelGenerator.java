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
 * -------------------------------
 * CategoryItemLabelGenerator.java
 * -------------------------------
 * (C) Copyright 2001-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 13-Dec-2001 : Version 1 (DG);
 * 16-Jan-2002 : Completed Javadocs (DG);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 24-Oct-2002 : Method now specifies category index (DG);
 * 05-Nov-2002 : Replaced reference to CategoryDataset with TableDataset (DG);
 * 21-Jan-2003 : TableDataset merged with CategoryDataset (DG);
 * 10-Apr-2003 : Changed CategoryDataset --> KeyedValues2DDataset (DG);
 * 01-May-2003 : Added generateValueLabel() method (with a plan to renaming
 *               this interface to reflect its more general use) (DG);
 * 09-Jun-2003 : Renamed CategoryToolTipGenerator
 *               --> CategoryItemLabelGenerator (DG);
 * 13-Aug-2003 : Added clone() method (DG);
 * 12-Feb-2004 : Removed clone() method (DG);
 * 15-Apr-2004 : Moved generateToolTip() method into CategoryToolTipGenerator
 *               interface (DG);
 * 11-May-2004 : Renamed CategoryItemLabelGenerator
 *               --> CategoryLabelGenerator (DG);
 * 31-Jan-2005 : Added generateRowLabel() and generateColumnLabel()
 *               methods (DG);
 * 20-Apr-2005 : Reverted name change of 11-May-2004 (DG);
 *
 */

package org.jfree.chart.labels;

import org.jfree.data.category.CategoryDataset;

/**
 * A <i>category item label generator</i> is an object that can be assigned to a
 * {@link org.jfree.chart.renderer.category.CategoryItemRenderer} and that
 * assumes responsibility for creating text items to be used as labels for the
 * items in a {@link org.jfree.chart.plot.CategoryPlot}.
 * <p>
 * To assist with cloning charts, classes that implement this interface should
 * also implement the {@link org.jfree.util.PublicCloneable} interface.
 */
public interface CategoryItemLabelGenerator {

    /**
     * Generates a label for the specified row.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param row  the row index (zero-based).
     *
     * @return The label.
     */
    public String generateRowLabel(CategoryDataset dataset, int row);

    /**
     * Generates a label for the specified row.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param column  the column index (zero-based).
     *
     * @return The label.
     */
    public String generateColumnLabel(CategoryDataset dataset, int column);

    /**
     * Generates a label for the specified item. The label is typically a
     * formatted version of the data value, but any text can be used.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The label (possibly <code>null</code>).
     */
    public String generateLabel(CategoryDataset dataset, int row, int column);

}
