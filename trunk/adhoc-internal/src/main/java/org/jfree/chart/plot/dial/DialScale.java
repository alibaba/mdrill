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
 * --------------
 * DialScale.java
 * --------------
 * (C) Copyright 2006-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 03-Nov-2006 : Version 1 (DG);
 * 17-Oct-2007 : Made this an extension of the DialLayer interface (DG);
 *
 */

package org.jfree.chart.plot.dial;

/**
 * A dial scale is a specialised layer that has the ability to convert data
 * values into angles.
 *
 * @since 1.0.7
 */
public interface DialScale extends DialLayer {

    /**
     * Converts a data value to an angle (in degrees, using the same
     * specification as Java's Arc2D class).
     *
     * @param value  the data value.
     *
     * @return The angle in degrees.
     *
     * @see #angleToValue(double)
     */
    public double valueToAngle(double value);

    /**
     * Converts an angle (in degrees) to a data value.
     *
     * @param angle  the angle (in degrees).
     *
     * @return The data value.
     *
     * @see #valueToAngle(double)
     */
    public double angleToValue(double angle);

}
