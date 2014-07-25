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
 * ----------------------
 * XIntervalDataItem.java
 * ----------------------
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

import org.jfree.data.ComparableObjectItem;

/**
 * An item representing data in the form (x, x-low, x-high, y).
 *
 * @since 1.0.3
 */
public class XIntervalDataItem extends ComparableObjectItem {

    /**
     * Creates a new instance of <code>XIntervalDataItem</code>.
     *
     * @param x  the x-value.
     * @param xLow  the lower bound of the x-interval.
     * @param xHigh  the upper bound of the x-interval.
     * @param y  the y-value.
     */
    public XIntervalDataItem(double x, double xLow, double xHigh, double y) {
        super(new Double(x), new YWithXInterval(y, xLow, xHigh));
    }

    /**
     * Returns the x-value.
     *
     * @return The x-value (never <code>null</code>).
     */
    public Number getX() {
        return (Number) getComparable();
    }

    /**
     * Returns the y-value.
     *
     * @return The y-value.
     */
    public double getYValue() {
        YWithXInterval interval = (YWithXInterval) getObject();
        if (interval != null) {
            return interval.getY();
        }
        else {
            return Double.NaN;
        }
    }

    /**
     * Returns the lower bound of the x-interval.
     *
     * @return The lower bound of the x-interval.
     */
    public double getXLowValue() {
        YWithXInterval interval = (YWithXInterval) getObject();
        if (interval != null) {
            return interval.getXLow();
        }
        else {
            return Double.NaN;
        }
    }

    /**
     * Returns the upper bound of the x-interval.
     *
     * @return The upper bound of the x-interval.
     */
    public double getXHighValue() {
        YWithXInterval interval = (YWithXInterval) getObject();
        if (interval != null) {
            return interval.getXHigh();
        }
        else {
            return Double.NaN;
        }
    }

}
