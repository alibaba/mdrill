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
 * YInterval.java
 * --------------
 * (C) Copyright 2006-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 20-Oct-2006 : Version 1 (DG);
 *
 */

package org.jfree.data.xy;

import java.io.Serializable;

/**
 * A y-interval.  This class is used internally by the
 * {@link YIntervalDataItem} class.
 *
 * @since 1.0.3
 */
public class YInterval implements Serializable {

    /** The y-value. */
    private double y;

    /** The lower bound of the y-interval. */
    private double yLow;

    /** The upper bound of the y-interval. */
    private double yHigh;

    /**
     * Creates a new instance of <code>YInterval</code>.
     *
     * @param y  the y-value.
     * @param yLow  the lower bound of the y-interval.
     * @param yHigh  the upper bound of the y-interval.
     */
    public YInterval(double y, double yLow, double yHigh) {
        this.y = y;
        this.yLow = yLow;
        this.yHigh = yHigh;
    }

    /**
     * Returns the y-value.
     *
     * @return The y-value.
     */
    public double getY() {
        return this.y;
    }

    /**
     * Returns the lower bound of the y-interval.
     *
     * @return The lower bound of the y-interval.
     */
    public double getYLow() {
        return this.yLow;
    }

    /**
     * Returns the upper bound of the y-interval.
     *
     * @return The upper bound of the y-interval.
     */
    public double getYHigh() {
        return this.yHigh;
    }

    /**
     * Tests this instance for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof YInterval)) {
            return false;
        }
        YInterval that = (YInterval) obj;
        if (this.y != that.y) {
            return false;
        }
        if (this.yLow != that.yLow) {
            return false;
        }
        if (this.yHigh != that.yHigh) {
            return false;
        }
        return true;
    }

}
