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
 * ----------------------------
 * ContourToolTipGenerator.java
 * ----------------------------
 * (C) Copyright 2002-2008, by David M. O'Donnell and Contributors.
 *
 * Original Author:  David M. O'Donnell;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 23-Jan-2003 : Added standard header (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Deprecated (DG);
 *
 */

package org.jfree.chart.labels;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.contour.ContourDataset;

/**
 * Interface for a tooltip generator for plots that use data from a
 * {@link ContourDataset}.
 *
 * @deprecated This interface is no longer supported (as of version 1.0.4).
 *     If you are creating contour plots, please try to use {@link XYPlot} and
 *     {@link XYBlockRenderer}.
 */
public interface ContourToolTipGenerator {

    /**
     * Generates a tooltip text item for a particular item within a series.
     *
     * @param dataset  the dataset.
     * @param item  the item index (zero-based).
     *
     * @return The tooltip text.
     */
    public String generateToolTip(ContourDataset dataset, int item);

}
