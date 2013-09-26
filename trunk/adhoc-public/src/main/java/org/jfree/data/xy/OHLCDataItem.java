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
 * -----------------
 * OHLCDataItem.java
 * -----------------
 * (C) Copyright 2003-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 03-Dec-2003 : Version 1 (DG);
 * 29-Apr-2005 : Added equals() method (DG);
 *
 */

package org.jfree.data.xy;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a single (open-high-low-close) data item in
 * an {@link DefaultOHLCDataset}.  This data item is commonly used
 * to summarise the trading activity of a financial commodity for
 * a fixed period (most often one day).
 */
public class OHLCDataItem implements Comparable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 7753817154401169901L;

    /** The date. */
    private Date date;

    /** The open value. */
    private Number open;

    /** The high value. */
    private Number high;

    /** The low value. */
    private Number low;

    /** The close value. */
    private Number close;

    /** The trading volume (number of shares, contracts or whatever). */
    private Number volume;

    /**
     * Creates a new item.
     *
     * @param date  the date (<code>null</code> not permitted).
     * @param open  the open value.
     * @param high  the high value.
     * @param low  the low value.
     * @param close  the close value.
     * @param volume  the volume.
     */
    public OHLCDataItem(Date date,
                        double open,
                        double high,
                        double low,
                        double close,
                        double volume) {
        if (date == null) {
            throw new IllegalArgumentException("Null 'date' argument.");
        }
        this.date = date;
        this.open = new Double(open);
        this.high = new Double(high);
        this.low = new Double(low);
        this.close = new Double(close);
        this.volume = new Double(volume);
    }

    /**
     * Returns the date that the data item relates to.
     *
     * @return The date (never <code>null</code>).
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Returns the open value.
     *
     * @return The open value.
     */
    public Number getOpen() {
        return this.open;
    }

    /**
     * Returns the high value.
     *
     * @return The high value.
     */
    public Number getHigh() {
        return this.high;
    }

    /**
     * Returns the low value.
     *
     * @return The low value.
     */
    public Number getLow() {
        return this.low;
    }

    /**
     * Returns the close value.
     *
     * @return The close value.
     */
    public Number getClose() {
        return this.close;
    }

    /**
     * Returns the volume.
     *
     * @return The volume.
     */
    public Number getVolume() {
        return this.volume;
    }

    /**
     * Checks this instance for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OHLCDataItem)) {
            return false;
        }
        OHLCDataItem that = (OHLCDataItem) obj;
        if (!this.date.equals(that.date)) {
            return false;
        }
        if (!this.high.equals(that.high)) {
            return false;
        }
        if (!this.low.equals(that.low)) {
            return false;
        }
        if (!this.open.equals(that.open)) {
            return false;
        }
        if (!this.close.equals(that.close)) {
            return false;
        }
        return true;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param object  the object to compare to.
     *
     * @return A negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     */
    public int compareTo(Object object) {
        if (object instanceof OHLCDataItem) {
            OHLCDataItem item = (OHLCDataItem) object;
            return this.date.compareTo(item.date);
        }
        else {
            throw new ClassCastException("OHLCDataItem.compareTo().");
        }
    }

}
