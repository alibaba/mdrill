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
 * ---------------------
 * FixedMillisecond.java
 * ---------------------
 * (C) Copyright 2002-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 19-Mar-2002 : Version 1, based on original Millisecond implementation (DG);
 * 24-Jun-2002 : Removed unnecessary imports (DG);
 * 10-Sep-2002 : Added getSerialIndex() method (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 13-Mar-2003 : Moved to com.jrefinery.data.time package and implemented
 *               Serializable (DG);
 * 21-Oct-2003 : Added hashCode() method (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 06-Oct-2006 : Added peg() method (DG);
 * 28-May-2008 : Fixed immutability problem (DG);
 *
 */

package org.jfree.data.time;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Wrapper for a <code>java.util.Date</code> object that allows it to be used
 * as a {@link RegularTimePeriod}.  This class is immutable, which is a
 * requirement for all {@link RegularTimePeriod} subclasses.
 */
public class FixedMillisecond extends RegularTimePeriod
        implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 7867521484545646931L;

    /** The millisecond. */
    private long time;

    /**
     * Constructs a millisecond based on the current system time.
     */
    public FixedMillisecond() {
        this(new Date());
    }

    /**
     * Constructs a millisecond.
     *
     * @param millisecond  the millisecond (same encoding as java.util.Date).
     */
    public FixedMillisecond(long millisecond) {
        this(new Date(millisecond));
    }

    /**
     * Constructs a millisecond.
     *
     * @param time  the time.
     */
    public FixedMillisecond(Date time) {
        this.time = time.getTime();
    }

    /**
     * Returns the date/time.
     *
     * @return The date/time.
     */
    public Date getTime() {
        return new Date(this.time);
    }

    /**
     * This method is overridden to do nothing.
     *
     * @param calendar  ignored
     *
     * @since 1.0.3
     */
    public void peg(Calendar calendar) {
        // nothing to do
    }

    /**
     * Returns the millisecond preceding this one.
     *
     * @return The millisecond preceding this one.
     */
    public RegularTimePeriod previous() {
        RegularTimePeriod result = null;
        long t = this.time;
        if (t != Long.MIN_VALUE) {
            result = new FixedMillisecond(t - 1);
        }
        return result;
    }

    /**
     * Returns the millisecond following this one.
     *
     * @return The millisecond following this one.
     */
    public RegularTimePeriod next() {
        RegularTimePeriod result = null;
        long t = this.time;
        if (t != Long.MAX_VALUE) {
            result = new FixedMillisecond(t + 1);
        }
        return result;
    }

    /**
     * Tests the equality of this object against an arbitrary Object.
     *
     * @param object  the object to compare
     *
     * @return A boolean.
     */
    public boolean equals(Object object) {
        if (object instanceof FixedMillisecond) {
            FixedMillisecond m = (FixedMillisecond) object;
            return this.time == m.getFirstMillisecond();
        }
        else {
            return false;
        }

    }

    /**
     * Returns a hash code for this object instance.
     *
     * @return A hash code.
     */
    public int hashCode() {
        return (int) this.time;
    }

    /**
     * Returns an integer indicating the order of this Millisecond object
     * relative to the specified
     * object: negative == before, zero == same, positive == after.
     *
     * @param o1    the object to compare.
     *
     * @return negative == before, zero == same, positive == after.
     */
    public int compareTo(Object o1) {

        int result;
        long difference;

        // CASE 1 : Comparing to another Second object
        // -------------------------------------------
        if (o1 instanceof FixedMillisecond) {
            FixedMillisecond t1 = (FixedMillisecond) o1;
            difference = this.time - t1.time;
            if (difference > 0) {
                result = 1;
            }
            else {
                if (difference < 0) {
                   result = -1;
                }
                else {
                    result = 0;
                }
            }
        }

        // CASE 2 : Comparing to another TimePeriod object
        // -----------------------------------------------
        else if (o1 instanceof RegularTimePeriod) {
            // more difficult case - evaluate later...
            result = 0;
        }

        // CASE 3 : Comparing to a non-TimePeriod object
        // ---------------------------------------------
        else {
            // consider time periods to be ordered after general objects
            result = 1;
        }

        return result;

    }

    /**
     * Returns the first millisecond of the time period.
     *
     * @return The first millisecond of the time period.
     */
    public long getFirstMillisecond() {
        return this.time;
    }


    /**
     * Returns the first millisecond of the time period.
     *
     * @param calendar  the calendar.
     *
     * @return The first millisecond of the time period.
     */
    public long getFirstMillisecond(Calendar calendar) {
        return this.time;
    }

    /**
     * Returns the last millisecond of the time period.
     *
     * @return The last millisecond of the time period.
     */
    public long getLastMillisecond() {
        return this.time;
    }

    /**
     * Returns the last millisecond of the time period.
     *
     * @param calendar  the calendar.
     *
     * @return The last millisecond of the time period.
     */
    public long getLastMillisecond(Calendar calendar) {
        return this.time;
    }

    /**
     * Returns the millisecond closest to the middle of the time period.
     *
     * @return The millisecond closest to the middle of the time period.
     */
    public long getMiddleMillisecond() {
        return this.time;
    }

    /**
     * Returns the millisecond closest to the middle of the time period.
     *
     * @param calendar  the calendar.
     *
     * @return The millisecond closest to the middle of the time period.
     */
    public long getMiddleMillisecond(Calendar calendar) {
        return this.time;
    }

    /**
     * Returns a serial index number for the millisecond.
     *
     * @return The serial index number.
     */
    public long getSerialIndex() {
        return this.time;
    }

}
