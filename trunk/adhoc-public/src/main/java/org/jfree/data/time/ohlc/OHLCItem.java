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
 * -------------
 * OHLCItem.java
 * -------------
 * (C) Copyright 2006, 2007, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 04-Dec-2006 : Version 1 (DG);
 *
 */

package org.jfree.data.time.ohlc;

import org.jfree.data.ComparableObjectItem;
import org.jfree.data.time.RegularTimePeriod;

/**
 * An item representing data in the form (period, open, high, low, close).
 *
 * @since 1.0.4
 */
public class OHLCItem extends ComparableObjectItem {

    /**
     * Creates a new instance of <code>OHLCItem</code>.
     *
     * @param period  the time period.
     * @param open  the open-value.
     * @param high  the high-value.
     * @param low  the low-value.
     * @param close  the close-value.
     */
    public OHLCItem(RegularTimePeriod period, double open, double high,
            double low, double close) {
        super(period, new OHLC(open, high, low, close));
    }

    /**
     * Returns the period.
     *
     * @return The period (never <code>null</code>).
     */
    public RegularTimePeriod getPeriod() {
        return (RegularTimePeriod) getComparable();
    }

    /**
     * Returns the y-value.
     *
     * @return The y-value.
     */
    public double getYValue() {
        return getCloseValue();
    }

    /**
     * Returns the open value.
     *
     * @return The open value.
     */
    public double getOpenValue() {
        OHLC ohlc = (OHLC) getObject();
        if (ohlc != null) {
            return ohlc.getOpen();
        }
        else {
            return Double.NaN;
        }
    }

    /**
     * Returns the high value.
     *
     * @return The high value.
     */
    public double getHighValue() {
        OHLC ohlc = (OHLC) getObject();
        if (ohlc != null) {
            return ohlc.getHigh();
        }
        else {
            return Double.NaN;
        }
    }

    /**
     * Returns the low value.
     *
     * @return The low value.
     */
    public double getLowValue() {
        OHLC ohlc = (OHLC) getObject();
        if (ohlc != null) {
            return ohlc.getLow();
        }
        else {
            return Double.NaN;
        }
    }

    /**
     * Returns the close value.
     *
     * @return The close value.
     */
    public double getCloseValue() {
        OHLC ohlc = (OHLC) getObject();
        if (ohlc != null) {
            return ohlc.getClose();
        }
        else {
            return Double.NaN;
        }
    }

}
