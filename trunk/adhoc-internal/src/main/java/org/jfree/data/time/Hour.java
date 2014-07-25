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
 * ---------
 * Hour.java
 * ---------
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
 * 14-Feb-2002 : Fixed bug in Hour(Date) constructor (DG);
 * 26-Feb-2002 : Changed getStart(), getMiddle() and getEnd() methods to
 *               evaluate with reference to a particular time zone (DG);
 * 15-Mar-2002 : Changed API (DG);
 * 16-Apr-2002 : Fixed small time zone bug in constructor (DG);
 * 10-Sep-2002 : Added getSerialIndex() method (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 10-Jan-2003 : Changed base class and method names (DG);
 * 13-Mar-2003 : Moved to com.jrefinery.data.time package, and implemented
 *               Serializable (DG);
 * 21-Oct-2003 : Added hashCode() method, and new constructor for
 *               convenience (DG);
 * 30-Sep-2004 : Replaced getTime().getTime() with getTimeInMillis() (DG);
 * 04-Nov-2004 : Reverted change of 30-Sep-2004, because it won't work for
 *               JDK 1.3 (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 05-Oct-2006 : Updated API docs (DG);
 * 06-Oct-2006 : Refactored to cache first and last millisecond values (DG);
 * 04-Apr-2007 : In Hour(Date, TimeZone), peg milliseconds using specified
 *               time zone (DG);
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
 * Represents an hour in a specific day.  This class is immutable, which is a
 * requirement for all {@link RegularTimePeriod} subclasses.
 */
public class Hour extends RegularTimePeriod implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -835471579831937652L;

    /** Useful constant for the first hour in the day. */
    public static final int FIRST_HOUR_IN_DAY = 0;

    /** Useful constant for the last hour in the day. */
    public static final int LAST_HOUR_IN_DAY = 23;

    /** The day. */
    private Day day;

    /** The hour. */
    private byte hour;

    /** The first millisecond. */
    private long firstMillisecond;

    /** The last millisecond. */
    private long lastMillisecond;

    /**
     * Constructs a new Hour, based on the system date/time.
     */
    public Hour() {
        this(new Date());
    }

    /**
     * Constructs a new Hour.
     *
     * @param hour  the hour (in the range 0 to 23).
     * @param day  the day (<code>null</code> not permitted).
     */
    public Hour(int hour, Day day) {
        if (day == null) {
            throw new IllegalArgumentException("Null 'day' argument.");
        }
        this.hour = (byte) hour;
        this.day = day;
        peg(Calendar.getInstance());
    }

    /**
     * Creates a new hour.
     *
     * @param hour  the hour (0-23).
     * @param day  the day (1-31).
     * @param month  the month (1-12).
     * @param year  the year (1900-9999).
     */
    public Hour(int hour, int day, int month, int year) {
        this(hour, new Day(day, month, year));
    }

    /**
     * Constructs a new instance, based on the supplied date/time and
     * the default time zone.
     *
     * @param time  the date-time (<code>null</code> not permitted).
     *
     * @see #Hour(Date, TimeZone)
     */
    public Hour(Date time) {
        // defer argument checking...
        this(time, TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * Constructs a new instance, based on the supplied date/time evaluated
     * in the specified time zone.
     *
     * @param time  the date-time (<code>null</code> not permitted).
     * @param zone  the time zone (<code>null</code> not permitted).
     *
     * @deprecated As of 1.0.13, use the constructor that specifies the locale
     *     also.
     */
    public Hour(Date time, TimeZone zone) {
        this(time, zone, Locale.getDefault());
    }

    /**
     * Constructs a new instance, based on the supplied date/time evaluated
     * in the specified time zone.
     *
     * @param time  the date-time (<code>null</code> not permitted).
     * @param zone  the time zone (<code>null</code> not permitted).
     * @param locale  the locale (<code>null</code> not permitted).
     *
     * @since 1.0.13
     */
    public Hour(Date time, TimeZone zone, Locale locale) {
        if (time == null) {
            throw new IllegalArgumentException("Null 'time' argument.");
        }
        if (zone == null) {
            throw new IllegalArgumentException("Null 'zone' argument.");
        }
        if (locale == null) {
            throw new IllegalArgumentException("Null 'locale' argument.");
        }
        Calendar calendar = Calendar.getInstance(zone, locale);
        calendar.setTime(time);
        this.hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        this.day = new Day(time, zone, locale);
        peg(calendar);
    }

    /**
     * Returns the hour.
     *
     * @return The hour (0 <= hour <= 23).
     */
    public int getHour() {
        return this.hour;
    }

    /**
     * Returns the day in which this hour falls.
     *
     * @return The day.
     */
    public Day getDay() {
        return this.day;
    }

    /**
     * Returns the year in which this hour falls.
     *
     * @return The year.
     */
    public int getYear() {
        return this.day.getYear();
    }

    /**
     * Returns the month in which this hour falls.
     *
     * @return The month.
     */
    public int getMonth() {
        return this.day.getMonth();
    }

    /**
     * Returns the day-of-the-month in which this hour falls.
     *
     * @return The day-of-the-month.
     */
    public int getDayOfMonth() {
        return this.day.getDayOfMonth();
    }

    /**
     * Returns the first millisecond of the hour.  This will be determined
     * relative to the time zone specified in the constructor, or in the
     * calendar instance passed in the most recent call to the
     * {@link #peg(Calendar)} method.
     *
     * @return The first millisecond of the hour.
     *
     * @see #getLastMillisecond()
     */
    public long getFirstMillisecond() {
        return this.firstMillisecond;
    }

    /**
     * Returns the last millisecond of the hour.  This will be
     * determined relative to the time zone specified in the constructor, or
     * in the calendar instance passed in the most recent call to the
     * {@link #peg(Calendar)} method.
     *
     * @return The last millisecond of the hour.
     *
     * @see #getFirstMillisecond()
     */
    public long getLastMillisecond() {
        return this.lastMillisecond;
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
        this.lastMillisecond = getLastMillisecond(calendar);
    }

    /**
     * Returns the hour preceding this one.
     *
     * @return The hour preceding this one.
     */
    public RegularTimePeriod previous() {
        Hour result;
        if (this.hour != FIRST_HOUR_IN_DAY) {
            result = new Hour(this.hour - 1, this.day);
        }
        else { // we are at the first hour in the day...
            Day prevDay = (Day) this.day.previous();
            if (prevDay != null) {
                result = new Hour(LAST_HOUR_IN_DAY, prevDay);
            }
            else {
                result = null;
            }
        }
        return result;
    }

    /**
     * Returns the hour following this one.
     *
     * @return The hour following this one.
     */
    public RegularTimePeriod next() {
        Hour result;
        if (this.hour != LAST_HOUR_IN_DAY) {
            result = new Hour(this.hour + 1, this.day);
        }
        else { // we are at the last hour in the day...
            Day nextDay = (Day) this.day.next();
            if (nextDay != null) {
                result = new Hour(FIRST_HOUR_IN_DAY, nextDay);
            }
            else {
                result = null;
            }
        }
        return result;
    }

    /**
     * Returns a serial index number for the hour.
     *
     * @return The serial index number.
     */
    public long getSerialIndex() {
        return this.day.getSerialIndex() * 24L + this.hour;
    }

    /**
     * Returns the first millisecond of the hour.
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
        int dom = this.day.getDayOfMonth();
        calendar.set(year, month, dom, this.hour, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        //return calendar.getTimeInMillis();  // this won't work for JDK 1.3
        return calendar.getTime().getTime();
    }

    /**
     * Returns the last millisecond of the hour.
     *
     * @param calendar  the calendar/timezone (<code>null</code> not permitted).
     *
     * @return The last millisecond.
     *
     * @throws NullPointerException if <code>calendar</code> is
     *     <code>null</code>.
     */
    public long getLastMillisecond(Calendar calendar) {
        int year = this.day.getYear();
        int month = this.day.getMonth() - 1;
        int dom = this.day.getDayOfMonth();
        calendar.set(year, month, dom, this.hour, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        //return calendar.getTimeInMillis();  // this won't work for JDK 1.3
        return calendar.getTime().getTime();
    }

    /**
     * Tests the equality of this object against an arbitrary Object.
     * <P>
     * This method will return true ONLY if the object is an Hour object
     * representing the same hour as this instance.
     *
     * @param obj  the object to compare (<code>null</code> permitted).
     *
     * @return <code>true</code> if the hour and day value of the object
     *      is the same as this.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Hour)) {
            return false;
        }
        Hour that = (Hour) obj;
        if (this.hour != that.hour) {
            return false;
        }
        if (!this.day.equals(that.day)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a string representation of this instance, for debugging
     * purposes.
     *
     * @return A string.
     */
    public String toString() {
    	return "[" + this.hour + "," + getDayOfMonth() + "/" + getMonth() + "/"
    	        + getYear() + "]";
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
        result = 37 * result + this.hour;
        result = 37 * result + this.day.hashCode();
        return result;
    }

    /**
     * Returns an integer indicating the order of this Hour object relative to
     * the specified object:
     *
     * negative == before, zero == same, positive == after.
     *
     * @param o1  the object to compare.
     *
     * @return negative == before, zero == same, positive == after.
     */
    public int compareTo(Object o1) {
        int result;

        // CASE 1 : Comparing to another Hour object
        // -----------------------------------------
        if (o1 instanceof Hour) {
            Hour h = (Hour) o1;
            result = getDay().compareTo(h.getDay());
            if (result == 0) {
                result = this.hour - h.getHour();
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
     * Creates an Hour instance by parsing a string.  The string is assumed to
     * be in the format "YYYY-MM-DD HH", perhaps with leading or trailing
     * whitespace.
     *
     * @param s  the hour string to parse.
     *
     * @return <code>null</code> if the string is not parseable, the hour
     *         otherwise.
     */
    public static Hour parseHour(String s) {
        Hour result = null;
        s = s.trim();

        String daystr = s.substring(0, Math.min(10, s.length()));
        Day day = Day.parseDay(daystr);
        if (day != null) {
            String hourstr = s.substring(
                Math.min(daystr.length() + 1, s.length()), s.length()
            );
            hourstr = hourstr.trim();
            int hour = Integer.parseInt(hourstr);
            // if the hour is 0 - 23 then create an hour
            if ((hour >= FIRST_HOUR_IN_DAY) && (hour <= LAST_HOUR_IN_DAY)) {
                result = new Hour(hour, day);
            }
        }

        return result;
    }

}
