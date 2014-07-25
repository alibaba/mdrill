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
 * TableXYDataset.java
 * -------------------
 * (C) Copyright 2000-2008, by Richard Atkinson and Contributors.
 *
 * Original Author:  Richard Atkinson;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 22-Sep-2003 : Changed to be an interface.  Previous functionality moved to
 *               DefaultTableXYDataset;
 * 16-Feb-2004 : Updated Javadocs (DG);
 *
 */

package org.jfree.data.xy;


/**
 * A dataset containing one or more data series containing (x, y) data items,
 * where all series in the dataset share the same set of x-values.  This is a
 * restricted form of the {@link XYDataset} interface (which allows independent
 * x-values between series). This is used primarily by the
 * {@link org.jfree.chart.renderer.xy.StackedXYAreaRenderer}.
 */
public interface TableXYDataset extends XYDataset {

    /**
     * Returns the number of items every series.
     *
     * @return The item count.
     */
    public int getItemCount();

}
