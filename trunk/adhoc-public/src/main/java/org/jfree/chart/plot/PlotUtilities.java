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
 * PlotUtilities.java
 * ------------------
 * (C) Copyright 2007, 2008, by Sergei Ivanov and Contributors.
 *
 * Original Author:  Sergei Ivanov;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 26-Sep-2007 : Version 1, contributed by Sergei Ivanov (see patch
 *               1772932) (DG);
 *
 */

package org.jfree.chart.plot;

import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;

/**
 * Some utility methods related to the plot classes.
 *
 * @since 1.0.7
 */
public class PlotUtilities {

    /**
     * Returns <code>true</code> if all the datasets belonging to the specified
     * plot are empty or <code>null</code>, and <code>false</code> otherwise.
     *
     * @param plot  the plot (<code>null</code> permitted).
     *
     * @return A boolean.
     *
     * @since 1.0.7
     */
    public static boolean isEmptyOrNull(XYPlot plot) {
        if (plot != null) {
            for (int i = 0, n = plot.getDatasetCount(); i < n; i++) {
                final XYDataset dataset = plot.getDataset(i);
                if (!DatasetUtilities.isEmptyOrNull(dataset)) {
                    return false;
                }
            }
        }
        return true;
    }

}
