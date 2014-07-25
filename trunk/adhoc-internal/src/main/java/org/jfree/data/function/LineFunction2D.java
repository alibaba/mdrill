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
 * LineFunction2D.java
 * -------------------
 * (C) Copyright 2002-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 01-Oct-2002 : Version 1 (DG);
 *
 */

package org.jfree.data.function;

/**
 * A function in the form y = a + bx.
 */
public class LineFunction2D implements Function2D {

    /** The intercept. */
    private double a;

    /** The slope of the line. */
    private double b;

    /**
     * Constructs a new line function.
     *
     * @param a  the intercept.
     * @param b  the slope.
     */
    public LineFunction2D(double a, double b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Returns the function value.
     *
     * @param x  the x-value.
     *
     * @return The value.
     */
    public double getValue(double x) {
        return this.a + this.b * x;
    }

}
