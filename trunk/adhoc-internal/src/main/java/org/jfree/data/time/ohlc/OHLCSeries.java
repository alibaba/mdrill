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
 * ---------------
 * OHLCSeries.java
 * ---------------
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
import org.jfree.data.ComparableObjectSeries;
import org.jfree.data.time.RegularTimePeriod;

/**
 * A list of (RegularTimePeriod, open, high, low, close) data items.
 *
 * @since 1.0.4
 *
 * @see OHLCSeriesCollection
 */
public class OHLCSeries extends ComparableObjectSeries {

    /**
     * Creates a new empty series.  By default, items added to the series will
     * be sorted into ascending order by period, and duplicate periods will
     * not be allowed.
     *
     * @param key  the series key (<code>null</code> not permitted).
     */
    public OHLCSeries(Comparable key) {
        super(key, true, false);
    }

    /**
     * Returns the time period for the specified item.
     *
     * @param index  the item index.
     *
     * @return The time period.
     */
    public RegularTimePeriod getPeriod(int index) {
        final OHLCItem item = (OHLCItem) getDataItem(index);
        return item.getPeriod();
    }

    /**
     * Returns the data item at the specified index.
     *
     * @param index  the item index.
     *
     * @return The data item.
     */
    public ComparableObjectItem getDataItem(int index) {
        return super.getDataItem(index);
    }

    /**
     * Adds a data item to the series.
     *
     * @param period  the period.
     * @param open  the open-value.
     * @param high  the high-value.
     * @param low  the low-value.
     * @param close  the close-value.
     */
    public void add(RegularTimePeriod period, double open, double high,
            double low, double close) {
        if (getItemCount() > 0) {
            OHLCItem item0 = (OHLCItem) this.getDataItem(0);
            if (!period.getClass().equals(item0.getPeriod().getClass())) {
                throw new IllegalArgumentException(
                        "Can't mix RegularTimePeriod class types.");
            }
        }
        super.add(new OHLCItem(period, open, high, low, close), true);
    }

}
