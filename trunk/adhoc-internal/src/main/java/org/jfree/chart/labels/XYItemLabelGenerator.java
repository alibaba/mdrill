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
 * -------------------------
 * XYItemLabelGenerator.java
 * -------------------------
 * (C) Copyright 2001-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 13-Dec-2001 : Version 1 (DG);
 * 16-Jan-2002 : Completed Javadocs (DG);
 * 13-Jun-2002 : Correction to Javadoc comments (DG);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 25-Feb-2004 : Renamed XYToolTipGenerator --> XYItemLabelGenerator (DG);
 * 15-Apr-2004 : Moved the generateToolTip() method to a separate
 *               interface (DG);
 * 11-May-2004 : Renamed XYItemLabelGenerator --> XYLabelGenerator (DG);
 * 20-Apr-2005 : Reverted name change of 11-May-2004 (DG);
 *
 */

package org.jfree.chart.labels;

import org.jfree.data.xy.XYDataset;

/**
 * Interface for a label generator for plots that use data from an
 * {@link XYDataset}.
 */
public interface XYItemLabelGenerator {

    /**
     * Generates a label for the specified item. The label is typically a
     * formatted version of the data value, but any text can be used.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The label (possibly <code>null</code>).
     */
    public String generateLabel(XYDataset dataset, int series, int item);

}
