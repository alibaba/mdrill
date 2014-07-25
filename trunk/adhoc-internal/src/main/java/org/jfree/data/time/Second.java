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
 * -----------
 * Second.java
 * -----------
 * (C) Copyright 2001-2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 11-Oct-2001 : Version 1 (DG);
 * 18-Dec-2001 : Changed order of parameters in constructor (DG);
 * 19-Dec-2001 : Added a new constructor as suggested by Paul English (DG);
 * 14-Feb-2002 : Fixed bug in Second(Date) constructor, and changed start of
 *               range to zero from one (DG);
 * 26-Feb-2002 : Changed getStart(), getMiddle() and getEnd() methods to
 *               evaluate with reference to a particular time zone (DG);
 * 13-Mar-2002 : Added parseSecond() method (DG);
 * 10-Sep-2002 : Added getSerialIndex() method (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 10-Jan-2003 : Changed base class and method names (DG);
 * 05-Mar-2003 : Fixed bug in getLastMillisecond() picked up in JUnit
 *               tests (DG);
 * 13-Mar-2003 : Moved to com.jrefinery.data.time package and implemented
 *               Serializable (DG);
 * 21-Oct-2003 : Added hashCode() method (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 05-Oct-2006 : Updated API docs (DG);
 * 06-Oct-2006 : Refactored to cache first and last millisecond values (DG);
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
 * Represents a second in a particular day.  This class is immutable, which is
 * a requirement for all {@link RegularTimePeriod} subclasses.
 */
public class Second extends RegularTimePeriod implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -6536564190712383466L;

    /** Useful constant for the first second in a minute. */
    public static final int FIRST_SECOND_IN_MINUTE = 0;

    /** Useful constant for the last second in a minute. */
    public static final int LAST_SECOND_IN_MINUTE = 59;

    /** The day. */
    private Day day;

    /** The hour of the day. */
    private byte hour;

    /** The minute. */
    private byte minute;

    /** The second. */
    private byte second;

    /**
     * The first millisecond.  We don't store the last millisecond, because it
     * is always firstMillisecond + 999L.
     */
    private long firstMillisecond;

    /**
     * Constructs a new Second, based on the system date/time.
     */
    public Second() {
        this(new Date());
    }

    /**
     * Constructs a new Second.
     *
     * @param second  the second (0 to 24*60*60-1).
     * @param minute  the minute (<code>null</code> not permitted).
     */
    public Second(int second, Minute minute) {
        if (minute == null) {
            throw new IllegalArgumentException("Null 'minute' argument.");
        }
        this.day = minute.getDay();
        this.hour = (byte) minute.getHourValue();
        this.minute = (byte) minute.getMinute();
        this.second = (byte) second;
        peg(Calendar.getInstance());
    }

    /**
     * Creates a new second.
     *
     * @param second  the second (0-59).
     * @param minute  the minute (0-59).
     * @param hour  the hour (0-23).
     * @param day  the day (1-31).
     * @param month  the month (1-12).
     * @param year  the year (1900-9999).
     */
    public Second(int second, int minute, int hour,
                  int day, int month, int year) {
        this(second, new Minute(minute, hour, day, month, year));
    }

    /**
     * Constructs a new instance from the specified date/time and the default
     * time zone..
     *
     * @param time  the time (<code>null</code> not permitted).
     *
     * @see #Second(Date, TimeZone)
     */
    public Second(Date time) {
        this(time, TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * Creates a new second based on the supplied time and time zone.
     *
     * @param time  the instant in time.
     * @param zone  the time zone.
     *
     * @deprecated As of 1.0.13, use the constructor that specifies the locale
     *     also.
     */
    public Second(Date time, TimeZone zone) {
        this(time, zone, Locale.getDefault());
    }

    /**
     * Creates a new second based on the supplied time and time zone.
     *
     * @param time  the time (<code>null</code> not permitted).
     * @param zone  the time zone (<code>null</code> not permitted).
     * @param locale  the locale (<code>null</code> not permitted).
     *
     * @since 1.0.13
     */
    public Second(Date time, TimeZone zone, Locale locale) {
        Calendar calendar = Calendar.getInstance(zone, locale);
        calendar.setTime(time);
        this.second = (byte) calendar.get(Calendar.SECOND);
        this.minute = (byte) calendar.get(Calendar.MINUTE);
        this.hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        this.day = new Day(time, zone, locale);
        peg(calendar);
    }

    /**
     * Returns the second within the minute.
     *
     * @return The second (0 - 59).
     */
    public int getSecond() {
        return this.second;
    }

    /**
     * Returns the minute.
     *
     * @return The minute (never <code>null</code>).
     */
    public Minute getMinute() {
        return new Minute(this.minute, new Hour(this.hour, this.day));
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
        return this.firstMillisecond + 999L;
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
     * Returns the second preceding this one.
     *
     * @return The second preceding this one.
     */
    public RegularTimePeriod previous() {
        Second result = null;
        if (this.second != FIRST_SECOND_IN_MINUTE) {
            result = new Second(this.second - 1, getMinute());
        }
        else {
            Minute previous = (Minute) getMinute().previous();
            if (previous != null) {
                result = new Second(LAST_SECOND_IN_MINUTE, previous);
            }
        }
        return result;
    }

    /**
     * Returns the second following this one.
     *
     * @return The second following this one.
     */
    public RegularTimePeriod next() {
        Second result = null;
        if (this.second != LAST_SECOND_IN_MINUTE) {
            result = new Second(this.second + 1, getMinute());
        }
        else {
            Minute next = (Minute) getMinute().next();
            if (next != null) {
                result = new Second(FIRST_SECOND_IN_MINUTE, next);
            }
        }
        return result;
    }

    /**
     * Returns a serial index number for the minute.
     *
     * @return The serial index number.
     */
    public long getSerialIndex() {
        long hourIndex = this.day.getSerialIndex() * 24L + this.hour;
        long minuteIndex = hourIndex * 60L + this.minute;
        return minuteIndex * 60L + this.second;
    }

    /**
     * Returns the first millisecond of the minute.
     *
     * @param calendar  the calendar/timezone (<code>null</code> not permitted).
     *
     * @return The first millisecond.
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
        calendar.set(Calendar.MILLISECOND, 0);
        //return calendar.getTimeInMillis();  // this won't work for JDK 1.3
        return calendar.getTime().getTime();
    }

    /**
     * Returns the last millisecond of the second.
     *
     * @param calendar  the calendar/timezone (<code>null</code> not permitted).
     *
     * @return The last millisecond.
     *
     * @throws NullPointerException if <code>calendar</code> is
     *     <code>null</code>.
     */
    public long getLastMillisecond(Calendar calendar) {
        return getFirstMillisecond(calendar) + 999L;
    }

    /**
     * Tests the equality of this object against an arbitrary Object.
     * <P>
     * This method will return true ONLY if the object is a Second object
     * representing the same second as this instance.
     *
     * @param obj  the object to compare (<code>null</code> permitted).
     *
     * @return <code>true</code> if second and minute of this and the object
     *         are the same.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Second)) {
            return false;
        }
        Second that = (Second) obj;
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
     * @return A hash code.
     */
    public int hashCode() {
        int result = 17;
        result = 37 * result + this.second;
        result = 37 * result + this.minute;
        result = 37 * result + this.hour;
        result = 37 * result + this.day.hashCode();
        return result;
    }

    /**
     * Returns an integer indicating the order of this Second object relative
     * to the specified
     * object: negative == before, zero == same, positive == after.
     *
     * @param o1  the object to compare.
     *
     * @return negative == before, zero == same, positive == after.
     */
    public int compareTo(Object o1) {
        int result;

        // CASE 1 : Comparing to another Second object
        // -------------------------------------------
        if (o1 instanceof Second) {
            Second s = (Second) o1;
            if (this.firstMillisecond < s.firstMillisecond) {
                return -1;
            }
            else if (this.firstMillisecond > s.firstMillisecond) {
                return 1;
            }
            else {
                return 0;
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
     * Creates a new instance by parsing a string.  The string is assumed to
     * be in the format "YYYY-MM-DD HH:MM:SS", perhaps with leading or trailing
     * whitespace.
     *
     * @param s  the string to parse.
     *
     * @return The second, or <code>null</code> if the string is not parseable.
     */
    public static Second parseSecond(String s) {
        Second result = null;
        s = s.trim();
        String daystr = s.substring(0, Math.min(10, s.length()));
        Day day = Day.parseDay(daystr);
        if (day != null) {
            String hmsstr = s.substring(Math.min(daystr.length() + 1,
                    s.length()), s.length());
            hmsstr = hmsstr.trim();

            int l = hmsstr.length();
            String hourstr = hmsstr.substring(0, Math.min(2, l));
            String minstr = hmsstr.substring(Math.min(3, l), Math.min(5, l));
            String secstr = hmsstr.substring(Math.min(6, l), Math.min(8, l));
            int hour = Integer.parseInt(hourstr);

            if ((hour >= 0) && (hour <= 23)) {

                int minute = Integer.parseInt(minstr);
                if ((minute >= 0) && (minute <= 59)) {

                    Minute m = new Minute(minute, new Hour(hour, day));
                    int second = Integer.parseInt(secstr);
                    if ((second >= 0) && (second <= 59)) {
                        result = new Second(second, m);
                    }
                }
            }
        }
        return result;
    }

}
