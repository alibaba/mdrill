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
 * ---------------------------
 * XYAnnotationBoundsInfo.java
 * ---------------------------
 * (C) Copyright 2009, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 10-Mar-2009 : Version 1 (DG);
 *
 */

package org.jfree.chart.annotations;

import org.jfree.data.Range;

/**
 * An interface that supplies information about the bounds of the annotation.
 *
 * @since 1.0.13
 */
public interface XYAnnotationBoundsInfo {

    /**
     * Returns a flag that determines whether or not the annotation's
     * bounds should be taken into account for auto-range calculations on
     * the axes that the annotation is plotted against.
     *
     * @return A boolean.
     */
    public boolean getIncludeInDataBounds();

    /**
     * Returns the range of x-values (in data space) that the annotation
     * uses.
     *
     * @return The x-range.
     */
    public Range getXRange();

    /**
     * Returns the range of y-values (in data space) that the annotation
     * uses.
     *
     * @return The y-range.
     */
    public Range getYRange();

}
