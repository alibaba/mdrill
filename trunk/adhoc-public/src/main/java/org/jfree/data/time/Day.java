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
 * --------
 * Day.java
 * --------
 * (C) Copyright 2001-2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 11-Oct-2001 : Version 1 (DG);
 * 15-Nov-2001 : Updated Javadoc comments (DG);
 * 04-Dec-2001 : Added static method to parse a string into a Day object (DG);
 * 19-Dec-2001 : Added new constructor as suggested by Paul English (DG);
 * 29-Jan-2002 : Changed getDay() method to getSerialDate() (DG);
 * 26-Feb-2002 : Changed getStart(), getMiddle() and getEnd() methods to
 *               evaluate with reference to a particular time zone (DG);
 * 19-Mar-2002 : Changed the API for the TimePeriod classes (DG);
 * 29-May-2002 : Fixed bug in equals method (DG);
 * 24-Jun-2002 : Removed unnecessary imports (DG);
 * 10-Sep-2002 : Added getSerialIndex() method (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 10-Jan-2003 : Changed base class and method names (DG);
 * 13-Mar-2003 : Moved to com.jrefinery.data.time package, and implemented
 *               Serializable (DG);
 * 21-Oct-2003 : Added hashCode() method (DG);
 * 30-Sep-2004 : Replaced getTime().getTime() with getTimeInMillis() (DG);
 * 04-Nov-2004 : Reverted change of 30-Sep-2004, because it won't work for
 *               JDK 1.3 (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 05-Oct-2006 : Updated API docs (DG);
 * 06-Oct-2006 : Refactored to cache first and last millisecond values (DG);
 * 16-Sep-2008 : Deprecated DEFAULT_TIME_ZONE (DG);
 * 02-Mar-2009 : Added new constructor with Locale (DG);
 *
 */

package org.jfree.data.time;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.jfree.date.SerialDate;

/**
 * Represents a single day in the range 1-Jan-1900 to 31-Dec-9999.  This class
 * is immutable, which is a requirement for all {@link RegularTimePeriod}
 * subclasses.
 */
public class Day extends RegularTimePeriod implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -7082667380758962755L;

    /** A standard date formatter. */
    protected static final DateFormat DATE_FORMAT
        = new SimpleDateFormat("yyyy-MM-dd");

    /** A date formatter for the default locale. */
    protected static final DateFormat
        DATE_FORMAT_SHORT = DateFormat.getDateInstance(DateFormat.SHORT);

    /** A date formatter for the default locale. */
    protected static final DateFormat
        DATE_FORMAT_MEDIUM = DateFormat.getDateInstance(DateFormat.MEDIUM);

    /** A date formatter for the default locale. */
    protected static final DateFormat
        DATE_FORMAT_LONG = DateFormat.getDateInstance(DateFormat.LONG);

    /** The day (uses SerialDate for convenience). */
    private SerialDate serialDate;

    /** The first millisecond. */
    private long firstMillisecond;

    /** The last millisecond. */
    private long lastMillisecond;

    /**
     * Creates a new instance, derived from the system date/time (and assuming
     * the default timezone).
     */
    public Day() {
        this(new Date());
    }

    /**
     * Constructs a new one day time period.
     *
     * @param day  the day-of-the-month.
     * @param month  the month (1 to 12).
     * @param year  the year (1900 <= year <= 9999).
     */
    public Day(int day, int month, int year) {
        this.serialDate = SerialDate.createInstance(day, month, year);
        peg(Calendar.getInstance());
    }

    /**
     * Constructs a new one day time period.
     *
     * @param serialDate  the day (<code>null</code> not permitted).
     */
    public Day(SerialDate serialDate) {
        if (serialDate == null) {
            throw new IllegalArgumentException("Null 'serialDate' argument.");
        }
        this.serialDate = serialDate;
        peg(Calendar.getInstance());
    }

    /**
     * Constructs a new instance, based on a particular date/time and the
     * default time zone.
     *
     * @param time  the time (<code>null</code> not permitted).
     *
     * @see #Day(Date, TimeZone)
     */
    public Day(Date time) {
        // defer argument checking...
        this(time, TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * Constructs a new instance, based on a particular date/time and time zone.
     *
     * @param time  the date/time.
     * @param zone  the time zone.
     *
     * @deprecated As of 1.0.13, use the constructor that specifies the locale
     *     also.
     */
    public Day(Date time, TimeZone zone) {
        this(time, zone, Locale.getDefault());
    }

    /**
     * Constructs a new instance, based on a particular date/time and time zone.
     *
     * @param time  the date/time (<code>null</code> not permitted).
     * @param zone  the time zone (<code>null</code> not permitted).
     * @param locale  the locale (<code>null</code> not permitted).
     */
    public Day(Date time, TimeZone zone, Locale locale) {
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
        int d = calendar.get(Calendar.DAY_OF_MONTH);
        int m = calendar.get(Calendar.MONTH) + 1;
        int y = calendar.get(Calendar.YEAR);
        this.serialDate = SerialDate.createInstance(d, m, y);
        peg(calendar);
    }

    /**
     * Returns the day as a {@link SerialDate}.  Note: the reference that is
     * returned should be an instance of an immutable {@link SerialDate}
     * (otherwise the caller could use the reference to alter the state of
     * this <code>Day</code> instance, and <code>Day</code> is supposed
     * to be immutable).
     *
     * @return The day as a {@link SerialDate}.
     */
    public SerialDate getSerialDate() {
        return this.serialDate;
    }

    /**
     * Returns the year.
     *
     * @return The year.
     */
    public int getYear() {
        return this.serialDate.getYYYY();
    }

    /**
     * Returns the month.
     *
     * @return The month.
     */
    public int getMonth() {
        return this.serialDate.getMonth();
    }

    /**
     * Returns the day of the month.
     *
     * @return The day of the month.
     */
    public int getDayOfMonth() {
        return this.serialDate.getDayOfMonth();
    }

    /**
     * Returns the first millisecond of the day.  This will be determined
     * relative to the time zone specified in the constructor, or in the
     * calendar instance passed in the most recent call to the
     * {@link #peg(Calendar)} method.
     *
     * @return The first millisecond of the day.
     *
     * @see #getLastMillisecond()
     */
    public long getFirstMillisecond() {
        return this.firstMillisecond;
    }

    /**
     * Returns the last millisecond of the day.  This will be
     * determined relative to the time zone specified in the constructor, or
     * in the calendar instance passed in the most recent call to the
     * {@link #peg(Calendar)} method.
     *
     * @return The last millisecond of the day.
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
     * Returns the day preceding this one.
     *
     * @return The day preceding this one.
     */
    public RegularTimePeriod previous() {
        Day result;
        int serial = this.serialDate.toSerial();
        if (serial > SerialDate.SERIAL_LOWER_BOUND) {
            SerialDate yesterday = SerialDate.createInstance(serial - 1);
            return new Day(yesterday);
        }
        else {
            result = null;
        }
        return result;
    }

    /**
     * Returns the day following this one, or <code>null</code> if some limit
     * has been reached.
     *
     * @return The day following this one, or <code>null</code> if some limit
     *         has been reached.
     */
    public RegularTimePeriod next() {
        Day result;
        int serial = this.serialDate.toSerial();
        if (serial < SerialDate.SERIAL_UPPER_BOUND) {
            SerialDate tomorrow = SerialDate.createInstance(serial + 1);
            return new Day(tomorrow);
        }
        else {
            result = null;
        }
        return result;
    }

    /**
     * Returns a serial index number for the day.
     *
     * @return The serial index number.
     */
    public long getSerialIndex() {
        return this.serialDate.toSerial();
    }

    /**
     * Returns the first millisecond of the day, evaluated using the supplied
     * calendar (which determines the time zone).
     *
     * @param calendar  calendar to use (<code>null</code> not permitted).
     *
     * @return The start of the day as milliseconds since 01-01-1970.
     *
     * @throws NullPointerException if <code>calendar</code> is
     *     <code>null</code>.
     */
    public long getFirstMillisecond(Calendar calendar) {
        int year = this.serialDate.getYYYY();
        int month = this.serialDate.getMonth();
        int day = this.serialDate.getDayOfMonth();
        calendar.clear();
        calendar.set(year, month - 1, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        //return calendar.getTimeInMillis();  // this won't work for JDK 1.3
        return calendar.getTime().getTime();
    }

    /**
     * Returns the last millisecond of the day, evaluated using the supplied
     * calendar (which determines the time zone).
     *
     * @param calendar  calendar to use (<code>null</code> not permitted).
     *
     * @return The end of the day as milliseconds since 01-01-1970.
     *
     * @throws NullPointerException if <code>calendar</code> is
     *     <code>null</code>.
     */
    public long getLastMillisecond(Calendar calendar) {
        int year = this.serialDate.getYYYY();
        int month = this.serialDate.getMonth();
        int day = this.serialDate.getDayOfMonth();
        calendar.clear();
        calendar.set(year, month - 1, day, 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        //return calendar.getTimeInMillis();  // this won't work for JDK 1.3
        return calendar.getTime().getTime();
    }

    /**
     * Tests the equality of this Day object to an arbitrary object.  Returns
     * true if the target is a Day instance or a SerialDate instance
     * representing the same day as this object. In all other cases,
     * returns false.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A flag indicating whether or not an object is equal to this day.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Day)) {
            return false;
        }
        Day that = (Day) obj;
        if (!this.serialDate.equals(that.getSerialDate())) {
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
        return this.serialDate.hashCode();
    }

    /**
     * Returns an integer indicating the order of this Day object relative to
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

        // CASE 1 : Comparing to another Day object
        // ----------------------------------------
        if (o1 instanceof Day) {
            Day d = (Day) o1;
            result = -d.getSerialDate().compare(this.serialDate);
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
     * Returns a string representing the day.
     *
     * @return A string representing the day.
     */
    public String toString() {
        return this.serialDate.toString();
    }

    /**
     * Parses the string argument as a day.
     * <P>
     * This method is required to recognise YYYY-MM-DD as a valid format.
     * Anything else, for now, is a bonus.
     *
     * @param s  the date string to parse.
     *
     * @return <code>null</code> if the string does not contain any parseable
     *      string, the day otherwise.
     */
    public static Day parseDay(String s) {
        try {
            return new Day (Day.DATE_FORMAT.parse(s));
        }
        catch (ParseException e1) {
            try {
                return new Day (Day.DATE_FORMAT_SHORT.parse(s));
            }
            catch (ParseException e2) {
              // ignore
            }
        }
        return null;
    }

}
