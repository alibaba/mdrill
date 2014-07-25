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
 * ----------------
 * Millisecond.java
 * ----------------
 * (C) Copyright 2001-2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 11-Oct-2001 : Version 1 (DG);
 * 19-Dec-2001 : Added new constructors as suggested by Paul English (DG);
 * 26-Feb-2002 : Added new getStart() and getEnd() methods (DG);
 * 29-Mar-2002 : Fixed bug in getStart(), getEnd() and compareTo() methods (DG);
 * 10-Sep-2002 : Added getSerialIndex() method (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 10-Jan-2003 : Changed base class and method names (DG);
 * 13-Mar-2003 : Moved to com.jrefinery.data.time package and implemented
 *               Serializable (DG);
 * 21-Oct-2003 : Added hashCode() method (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 05-Oct-2006 : Updated API docs (DG);
 * 06-Oct-2006 : Refactored to cache first and last millisecond values (DG);
 * 04-Apr-2007 : In Millisecond(Date, TimeZone), peg milliseconds to the
 *               specified zone (DG);
 * 06-Jun-2008 : Added handling for general RegularTimePeriod in compareTo()
 *               method:
 *               see http://www.jfree.org/phpBB2/viewtopic.php?t=24805 (DG);
 * 16-Sep-2008 : Deprecated DEFAULT_TIME_ZONE (DG);
 * 02-Mar-2009 : Added new constructor with Locale (DG);
 *
 */

package org.jfree.data.time;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Represents a millisecond.  This class is immutable, which is a requirement
 * for all {@link RegularTimePeriod} subclasses.
 */
public class Millisecond extends RegularTimePeriod implements Serializable {

    /** For serialization. */
    static final long serialVersionUID = -5316836467277638485L;

    /** A constant for the first millisecond in a second. */
    public static final int FIRST_MILLISECOND_IN_SECOND = 0;

    /** A constant for the last millisecond in a second. */
    public static final int LAST_MILLISECOND_IN_SECOND = 999;

    /** The day. */
    private Day day;

    /** The hour in the day. */
    private byte hour;

    /** The minute. */
    private byte minute;

    /** The second. */
    private byte second;

    /** The millisecond. */
    private int millisecond;

    /**
     * The pegged millisecond.
     */
    private long firstMillisecond;

    /**
     * Constructs a millisecond based on the current system time.
     */
    public Millisecond() {
        this(new Date());
    }

    /**
     * Constructs a millisecond.
     *
     * @param millisecond  the millisecond (0-999).
     * @param second  the second.
     */
    public Millisecond(int millisecond, Second second) {
        this.millisecond = millisecond;
        this.second = (byte) second.getSecond();
        this.minute = (byte) second.getMinute().getMinute();
        this.hour = (byte) second.getMinute().getHourValue();
        this.day = second.getMinute().getDay();
        peg(Calendar.getInstance());
    }

    /**
     * Creates a new millisecond.
     *
     * @param millisecond  the millisecond (0-999).
     * @param second  the second (0-59).
     * @param minute  the minute (0-59).
     * @param hour  the hour (0-23).
     * @param day  the day (1-31).
     * @param month  the month (1-12).
     * @param year  the year (1900-9999).
     */
    public Millisecond(int millisecond, int second, int minute, int hour,
                       int day, int month, int year) {

        this(millisecond, new Second(second, minute, hour, day, month, year));

    }

    /**
     * Constructs a new millisecond using the default time zone.
     *
     * @param time  the time.
     *
     * @see #Millisecond(Date, TimeZone)
     */
    public Millisecond(Date time) {
        this(time, TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * Creates a millisecond.
     *
     * @param time  the instant in time.
     * @param zone  the time zone.
     *
     * @deprecated As of 1.0.13, use the constructor that specifies the locale
     *     also.
     */
    public Millisecond(Date time, TimeZone zone) {
        this(time, zone, Locale.getDefault());
    }

    /**
     * Creates a millisecond.
     *
     * @param time  the date-time (<code>null</code> not permitted).
     * @param zone  the time zone (<code>null</code> not permitted).
     * @param locale  the locale (<code>null</code> not permitted).
     *
     * @since 1.0.13
     */
    public Millisecond(Date time, TimeZone zone, Locale locale) {
        Calendar calendar = Calendar.getInstance(zone, locale);
        calendar.setTime(time);
        this.millisecond = calendar.get(Calendar.MILLISECOND);
        this.second = (byte) calendar.get(Calendar.SECOND);
        this.minute = (byte) calendar.get(Calendar.MINUTE);
        this.hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        this.day = new Day(time, zone, locale);
        peg(calendar);
    }

    /**
     * Returns the second.
     *
     * @return The second.
     */
    public Second getSecond() {
        return new Second(this.second, this.minute, this.hour,
                this.day.getDayOfMonth(), this.day.getMonth(),
                this.day.getYear());
    }

    /**
     * Returns the millisecond.
     *
     * @return The millisecond.
     */
    public long getMillisecond() {
        return this.millisecond;
    }

    /**
     * Returns the first millisecond of the second.  This will be determined
     * relative to the time zone specified in the constructor, or in the
     * calendar instance passed in the most recent call to the
     * {@link #peg(Calendar)} method.
     *
     * @return The first millisecond of the second.
     *
     * @see #getLastMillisecond()
     */
    public long getFirstMillisecond() {
        return this.firstMillisecond;
    }

    /**
     * Returns the last millisecond of the second.  This will be
     * determined relative to the time zone specified in the constructor, or
     * in the calendar instance passed in the most recent call to the
     * {@link #peg(Calendar)} method.
     *
     * @return The last millisecond of the second.
     *
     * @see #getFirstMillisecond()
     */
    public long getLastMillisecond() {
        return this.firstMillisecond;
    }

    /**
     * Recalculates the start date/time and end date/time for this time period
     * relative to the supplied calendar (which incorporates a time zone).
     *
     * @param calendar  the calendar (<code>null</code> not permitted).
     *
     * @since 1.0.3
     */
    public void peg(Calendar calendar) {
        this.firstMillisecond = getFirstMillisecond(calendar);
    }

    /**
     * Returns the millisecond preceding this one.
     *
     * @return The millisecond preceding this one.
     */
    public RegularTimePeriod previous() {
        RegularTimePeriod result = null;
        if (this.millisecond != FIRST_MILLISECOND_IN_SECOND) {
            result = new Millisecond(this.millisecond - 1, getSecond());
        }
        else {
            Second previous = (Second) getSecond().previous();
            if (previous != null) {
                result = new Millisecond(LAST_MILLISECOND_IN_SECOND, previous);
            }
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
        if (this.millisecond != LAST_MILLISECOND_IN_SECOND) {
            result = new Millisecond(this.millisecond + 1, getSecond());
        }
        else {
            Second next = (Second) getSecond().next();
            if (next != null) {
                result = new Millisecond(FIRST_MILLISECOND_IN_SECOND, next);
            }
        }
        return result;
    }

    /**
     * Returns a serial index number for the millisecond.
     *
     * @return The serial index number.
     */
    public long getSerialIndex() {
        long hourIndex = this.day.getSerialIndex() * 24L + this.hour;
        long minuteIndex = hourIndex * 60L + this.minute;
        long secondIndex = minuteIndex * 60L + this.second;
        return secondIndex * 1000L + this.millisecond;
    }

    /**
     * Tests the equality of this object against an arbitrary Object.
     * <P>
     * This method will return true ONLY if the object is a Millisecond object
     * representing the same millisecond as this instance.
     *
     * @param obj  the object to compare
     *
     * @return <code>true</code> if milliseconds and seconds of this and object
     *      are the same.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Millisecond)) {
            return false;
        }
        Millisecond that = (Millisecond) obj;
        if (this.millisecond != that.millisecond) {
            return false;
        }
        if (this.second != that.second) {
            return false;
        }
        if (this.minute != that.minute) {
            return false;
        }
        if (this.hour != that.hour) {
            return false;
        }
        if (!this.day.equals(that.day)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code for this object instance.  The approach described by
     * Joshua Bloch in "Effective Java" has been used here:
     * <p>
     * <code>http://developer.java.sun.com/developer/Books/effectivejava
     * /Chapter3.pdf</code>
     *
     * @return A hashcode.
     */
    public int hashCode() {
        int result = 17;
        result = 37 * result + this.millisecond;
        result = 37 * result + getSecond().hashCode();
        return result;
    }

    /**
     * Returns an integer indicating the order of this Millisecond object
     * relative to the specified object:
     *
     * negative == before, zero == same, positive == after.
     *
     * @param obj  the object to compare
     *
     * @return negative == before, zero == same, positive == after.
     */
    public int compareTo(Object obj) {
        int result;
        long difference;

        // CASE 1 : Comparing to another Second object
        // -------------------------------------------
        if (obj instanceof Millisecond) {
            Millisecond ms = (Millisecond) obj;
            difference = getFirstMillisecond() - ms.getFirstMillisecond();
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
        else if (obj instanceof RegularTimePeriod) {
            RegularTimePeriod rtp = (RegularTimePeriod) obj;
            final long thisVal = this.getFirstMillisecond();
            final long anotherVal = rtp.getFirstMillisecond();
            result = (thisVal < anotherVal ? -1
                    : (thisVal == anotherVal ? 0 : 1));
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
     * @param calendar  the calendar (<code>null</code> not permitted).
     *
     * @return The first millisecond of the time period.
     *
     * @throws NullPointerException if <code>calendar</code> is
     *     <code>null</code>.
     */
    public long getFirstMillisecond(Calendar calendar) {
        int year = this.day.getYear();
        int month = this.day.getMonth() - 1;
        int day = this.day.getDayOfMonth();
        calendar.clear();
        calendar.set(year, month, day, this.hour, this.minute, this.second);
        calendar.set(Calendar.MILLISECOND, this.millisecond);
        //return calendar.getTimeInMillis();  // this won't work for JDK 1.3
        return calendar.getTime().getTime();
    }

    /**
     * Returns the last millisecond of the time period.
     *
     * @param calendar  the calendar (<code>null</code> not permitted).
     *
     * @return The last millisecond of the time period.
     *
     * @throws NullPointerException if <code>calendar</code> is
     *     <code>null</code>.
     */
    public long getLastMillisecond(Calendar calendar) {
        return getFirstMillisecond(calendar);
    }

}
