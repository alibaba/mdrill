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
 * --------------------
 * PowerFunction2D.java
 * --------------------
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
 * A function of the form y = a * x ^ b.
 */
public class PowerFunction2D implements Function2D {

    /** The 'a' coefficient. */
    private double a;

    /** The 'b' coefficient. */
    private double b;

    /**
     * Creates a new power function.
     *
     * @param a  the 'a' coefficient.
     * @param b  the 'b' coefficient.
     */
    public PowerFunction2D(double a, double b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Returns the value of the function for a given input ('x').
     *
     * @param x  the x-value.
     *
     * @return The value.
     */
    public double getValue(double x) {
        return this.a * Math.pow(x, this.b);
    }

}
