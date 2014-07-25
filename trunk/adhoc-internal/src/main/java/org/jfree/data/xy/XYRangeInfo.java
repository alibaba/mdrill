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
 * ----------------
 * XYRangeInfo.java
 * ----------------
 * (C) Copyright 2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 27-Mar-2009 : Version 1 (DG);
 *
 */

package org.jfree.data.xy;

import java.util.List;
import org.jfree.data.Range;

/**
 * An interface that can (optionally) be implemented by a dataset to assist in
 * determining the minimum and maximum y-values.
 *
 * @since 1.0.13
 */
public interface XYRangeInfo {

    /**
     * Returns the range of the values in this dataset's range.
     *
     * @param visibleSeriesKeys  the keys of the visible series.
     * @param xRange  the x-range (<code>null</code> not permitted).
     * @param includeInterval  a flag that determines whether or not the
     *                         y-interval is taken into account.
     *
     * @return The range (or <code>null</code> if the dataset contains no
     *     values).
     */
    public Range getRangeBounds(List visibleSeriesKeys, Range xRange,
            boolean includeInterval);

}
