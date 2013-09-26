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
 * ---------
 * Week.java
 * ---------
 * (C) Copyright 2001-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Aimin Han;
 *
 * Changes
 * -------
 * 11-Oct-2001 : Version 1 (DG);
 * 18-Dec-2001 : Changed order of parameters in constructor (DG);
 * 19-Dec-2001 : Added a new constructor as suggested by Paul English (DG);
 * 29-Jan-2002 : Worked on the parseWeek() method (DG);
 * 13-Feb-2002 : Fixed bug in Week(Date) constructor (DG);
 * 26-Feb-2002 : Changed getStart(), getMiddle() and getEnd() methods to
 *               evaluate with reference to a particular time zone (DG);
 * 05-Apr-2002 : Reinstated this class to the JCommon library (DG);
 * 24-Jun-2002 : Removed unnecessary main method (DG);
 * 10-Sep-2002 : Added getSerialIndex() method (DG);
 * 06-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 18-Oct-2002 : Changed to observe 52 or 53 weeks per year, consistent with
 *               GregorianCalendar. Thanks to Aimin Han for the code (DG);
 * 02-Jan-2003 : Removed debug code (DG);
 * 13-Mar-2003 : Moved to com.jrefinery.data.time package, and implemented
 *               Serializable (DG);
 * 21-Oct-2003 : Added hashCode() method (DG);
 * 24-May-2004 : Modified getFirstMillisecond() and getLastMillisecond() to
 *               take account of firstDayOfWeek setting in Java's Calendar
 *               class (DG);
 * 30-Sep-2004 : Replaced getTime().getTime() with getTimeInMillis() (DG);
 * 04-Nov-2004 : Reverted change of 30-Sep-2004, because it won't work for
 *               JDK 1.3 (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 06-Mar-2006 : Fix for bug 1448828, incorrect calculation of week and year
 *               for the first few days of some years (DG);
 * 05-Oct-2006 : Updated API docs (DG);
 * 06-Oct-2006 : Refactored to cache first and last millisecond values (DG);
 * 09-Jan-2007 : Fixed bug in next() (DG);
 * 28-Aug-2007 : Added new constructor to avoid problem in creating new
 *               instances (DG);
 * 19-Dec-2007 : Fixed bug in deprecated constructor (DG);
 * 16-Sep-2008 : Deprecated DEFAULT_TIME_ZONE (DG);
 *
 */

package org.jfree.data.time;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A calendar week.  All years are considered to have 53 weeks, numbered from 1
 * to 53, although in many cases the 53rd week is empty.  Most of the time, the
 * 1st week of the year *begins* in the previous calendar year, but it always
 * finishes in the current year (this behaviour matches the workings of the
 * <code>GregorianCalendar</code> class).
 * <P>
 * This class is immutable, which is a requirement for all
 * {@link RegularTimePeriod} subclasses.
 */
public class Week extends RegularTimePeriod implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 1856387786939865061L;

    /** Constant for the first week in the year. */
    public static final int FIRST_WEEK_IN_YEAR = 1;

    /** Constant for the last week in the year. */
    public static final int LAST_WEEK_IN_YEAR = 53;

    /** The year in which the week falls. */
    private short year;

    /** The week (1-53). */
    private byte week;

    /** The first millisecond. */
    private long firstMillisecond;

    /** The last millisecond. */
    private long lastMillisecond;

    /**
     * Creates a new time period for the week in which the current system
     * date/time falls.
     */
    public Week() {
        this(new Date());
    }

    /**
     * Creates a time period representing the week in the specified year.
     *
     * @param week  the week (1 to 53).
     * @param year  the year (1900 to 9999).
     */
    public Week(int week, int year) {
        if ((week < FIRST_WEEK_IN_YEAR) && (week > LAST_WEEK_IN_YEAR)) {
            throw new IllegalArgumentException(
                    "The 'week' argument must be in the range 1 - 53.");
        }
        this.week = (byte) week;
        this.year = (short) year;
        peg(Calendar.getInstance());
    }

    /**
     * Creates a time period representing the week in the specified year.
     *
     * @param week  the week (1 to 53).
     * @param year  the year (1900 to 9999).
     */
    public Week(int week, Year year) {
        if ((week < FIRST_WEEK_IN_YEAR) && (week > LAST_WEEK_IN_YEAR)) {
            throw new IllegalArgumentException(
                    "The 'week' argument must be in the range 1 - 53.");
        }
        this.week = (byte) week;
        this.year = (short) year.getYear();
        peg(Calendar.getInstance());
   }

    /**
     * Creates a time period for the week in which the specified date/time
     * falls, using the default time zone and locale (the locale can affect the
     * day-of-the-week that marks the beginning of the week, as well as the
     * minimal number of days in the first week of the year).
     *
     * @param time  the time (<code>null</code> not permitted).
     *
     * @see #Week(Date, TimeZone, Locale)
     */
    public Week(Date time) {
        // defer argument checking...
        this(time, TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * Creates a time period for the week in which the specified date/time
     * falls, calculated relative to the specified time zone.
     *
     * @param time  the date/time (<code>null</code> not permitted).
     * @param zone  the time zone (<code>null</code> not permitted).
     *
     * @deprecated As of 1.0.7, use {@link #Week(Date, TimeZone, Locale)}.
     */
    public Week(Date time, TimeZone zone) {
        // defer argument checking...
        this(time, zone, Locale.getDefault());
    }

    /**
     * Creates a time period for the week in which the specified date/time
     * falls, calculated relative to the specified time zone.
     *
     * @param time  the date/time (<code>null</code> not permitted).
     * @param zone  the time zone (<code>null</code> not permitted).
     * @param locale  the locale (<code>null</code> not permitted).
     *
     * @since 1.0.7
     */
    public Week(Date time, TimeZone zone, Locale locale) {
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

        // sometimes the last few days of the year are considered to fall in
        // the *first* week of the following year.  Refer to the Javadocs for
        // GregorianCalendar.
        int tempWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        if (tempWeek == 1
                && calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
            this.week = 1;
            this.year = (short) (calendar.get(Calendar.YEAR) + 1);
        }
        else {
            this.week = (byte) Math.min(tempWeek, LAST_WEEK_IN_YEAR);
            int yyyy = calendar.get(Calendar.YEAR);
            // alternatively, sometimes the first few days of the year are
            // considered to fall in the *last* week of the previous year...
            if (calendar.get(Calendar.MONTH) == Calendar.JANUARY
                    && this.week >= 52) {
                yyyy--;
            }
            this.year = (short) yyyy;
        }
        peg(calendar);
    }

    /**
     * Returns the year in which the week falls.
     *
     * @return The year (never <code>null</code>).
     */
    public Year getYear() {
        return new Year(this.year);
    }

    /**
     * Returns the year in which the week falls, as an integer value.
     *
     * @return The year.
     */
    public int getYearValue() {
        return this.year;
    }

    /**
     * Returns the week.
     *
     * @return The week.
     */
    public int getWeek() {
        return this.week;
    }

    /**
     * Returns the first millisecond of the week.  This will be determined
     * relative to the time zone specified in the constructor, or in the
     * calendar instance passed in the most recent call to the
     * {@link #peg(Calendar)} method.
     *
     * @return The first millisecond of the week.
     *
     * @see #getLastMillisecond()
     */
    public long getFirstMillisecond() {
        return this.firstMillisecond;
    }

    /**
     * Returns the last millisecond of the week.  This will be
     * determined relative to the time zone specified in the constructor, or
     * in the calendar instance passed in the most recent call to the
     * {@link #peg(Calendar)} method.
     *
     * @return The last millisecond of the week.
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
     * Returns the week preceding this one.  This method will return
     * <code>null</code> for some lower limit on the range of weeks (currently
     * week 1, 1900).  For week 1 of any year, the previous week is always week
     * 53, but week 53 may not contain any days (you should check for this).
     *
     * @return The preceding week (possibly <code>null</code>).
     */
    public RegularTimePeriod previous() {

        Week result;
        if (this.week != FIRST_WEEK_IN_YEAR) {
            result = new Week(this.week - 1, this.year);
        }
        else {
            // we need to work out if the previous year has 52 or 53 weeks...
            if (this.year > 1900) {
                int yy = this.year - 1;
                Calendar prevYearCalendar = Calendar.getInstance();
                prevYearCalendar.set(yy, Calendar.DECEMBER, 31);
                result = new Week(prevYearCalendar.getActualMaximum(
                        Calendar.WEEK_OF_YEAR), yy);
            }
            else {
                result = null;
            }
        }
        return result;

    }

    /**
     * Returns the week following this one.  This method will return
     * <code>null</code> for some upper limit on the range of weeks (currently
     * week 53, 9999).  For week 52 of any year, the following week is always
     * week 53, but week 53 may not contain any days (you should check for
     * this).
     *
     * @return The following week (possibly <code>null</code>).
     */
    public RegularTimePeriod next() {

        Week result;
        if (this.week < 52) {
            result = new Week(this.week + 1, this.year);
        }
        else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(this.year, Calendar.DECEMBER, 31);
            int actualMaxWeek
                = calendar.getActualMaximum(Calendar.WEEK_OF_YEAR);
            if (this.week < actualMaxWeek) {
                result = new Week(this.week + 1, this.year);
            }
            else {
                if (this.year < 9999) {
                    result = new Week(FIRST_WEEK_IN_YEAR, this.year + 1);
                }
                else {
                    result = null;
                }
            }
        }
        return result;

    }

    /**
     * Returns a serial index number for the week.
     *
     * @return The serial index number.
     */
    public long getSerialIndex() {
        return this.year * 53L + this.week;
    }

    /**
     * Returns the first millisecond of the week, evaluated using the supplied
     * calendar (which determines the time zone).
     *
     * @param calendar  the calendar (<code>null</code> not permitted).
     *
     * @return The first millisecond of the week.
     *
     * @throws NullPointerException if <code>calendar</code> is
     *     <code>null</code>.
     */
    public long getFirstMillisecond(Calendar calendar) {
        Calendar c = (Calendar) calendar.clone();
        c.clear();
        c.set(Calendar.YEAR, this.year);
        c.set(Calendar.WEEK_OF_YEAR, this.week);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        //return c.getTimeInMillis();  // this won't work for JDK 1.3
        return c.getTime().getTime();
    }

    /**
     * Returns the last millisecond of the week, evaluated using the supplied
     * calendar (which determines the time zone).
     *
     * @param calendar  the calendar (<code>null</code> not permitted).
     *
     * @return The last millisecond of the week.
     *
     * @throws NullPointerException if <code>calendar</code> is
     *     <code>null</code>.
     */
    public long getLastMillisecond(Calendar calendar) {
        Calendar c = (Calendar) calendar.clone();
        c.clear();
        c.set(Calendar.YEAR, this.year);
        c.set(Calendar.WEEK_OF_YEAR, this.week + 1);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        //return c.getTimeInMillis();  // this won't work for JDK 1.3
        return c.getTime().getTime() - 1;
    }

    /**
     * Returns a string representing the week (e.g. "Week 9, 2002").
     *
     * TODO: look at internationalisation.
     *
     * @return A string representing the week.
     */
    public String toString() {
        return "Week " + this.week + ", " + this.year;
    }

    /**
     * Tests the equality of this Week object to an arbitrary object.  Returns
     * true if the target is a Week instance representing the same week as this
     * object.  In all other cases, returns false.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return <code>true</code> if week and year of this and object are the
     *         same.
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Week)) {
            return false;
        }
        Week that = (Week) obj;
        if (this.week != that.week) {
            return false;
        }
        if (this.year != that.year) {
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
        result = 37 * result + this.week;
        result = 37 * result + this.year;
        return result;
    }

    /**
     * Returns an integer indicating the order of this Week object relative to
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

        // CASE 1 : Comparing to another Week object
        // --------------------------------------------
        if (o1 instanceof Week) {
            Week w = (Week) o1;
            result = this.year - w.getYear().getYear();
            if (result == 0) {
                result = this.week - w.getWeek();
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
     * Parses the string argument as a week.
     * <P>
     * This method is required to accept the format "YYYY-Wnn".  It will also
     * accept "Wnn-YYYY". Anything else, at the moment, is a bonus.
     *
     * @param s  string to parse.
     *
     * @return <code>null</code> if the string is not parseable, the week
     *         otherwise.
     */
    public static Week parseWeek(String s) {

        Week result = null;
        if (s != null) {

            // trim whitespace from either end of the string
            s = s.trim();

            int i = Week.findSeparator(s);
            if (i != -1) {
                String s1 = s.substring(0, i).trim();
                String s2 = s.substring(i + 1, s.length()).trim();

                Year y = Week.evaluateAsYear(s1);
                int w;
                if (y != null) {
                    w = Week.stringToWeek(s2);
                    if (w == -1) {
                        throw new TimePeriodFormatException(
                                "Can't evaluate the week.");
                    }
                    result = new Week(w, y);
                }
                else {
                    y = Week.evaluateAsYear(s2);
                    if (y != null) {
                        w = Week.stringToWeek(s1);
                        if (w == -1) {
                            throw new TimePeriodFormatException(
                                    "Can't evaluate the week.");
                        }
                        result = new Week(w, y);
                    }
                    else {
                        throw new TimePeriodFormatException(
                                "Can't evaluate the year.");
                    }
                }

            }
            else {
                throw new TimePeriodFormatException(
                        "Could not find separator.");
            }

        }
        return result;

    }

    /**
     * Finds the first occurrence of ' ', '-', ',' or '.'
     *
     * @param s  the string to parse.
     *
     * @return <code>-1</code> if none of the characters was found, the
     *      index of the first occurrence otherwise.
     */
    private static int findSeparator(String s) {

        int result = s.indexOf('-');
        if (result == -1) {
            result = s.indexOf(',');
        }
        if (result == -1) {
            result = s.indexOf(' ');
        }
        if (result == -1) {
            result = s.indexOf('.');
        }
        return result;
    }

    /**
     * Creates a year from a string, or returns null (format exceptions
     * suppressed).
     *
     * @param s  string to parse.
     *
     * @return <code>null</code> if the string is not parseable, the year
     *         otherwise.
     */
    private static Year evaluateAsYear(String s) {

        Year result = null;
        try {
            result = Year.parseYear(s);
        }
        catch (TimePeriodFormatException e) {
            // suppress
        }
        return result;

    }

    /**
     * Converts a string to a week.
     *
     * @param s  the string to parse.
     * @return <code>-1</code> if the string does not contain a week number,
     *         the number of the week otherwise.
     */
    private static int stringToWeek(String s) {

        int result = -1;
        s = s.replace('W', ' ');
        s = s.trim();
        try {
            result = Integer.parseInt(s);
            if ((result < 1) || (result > LAST_WEEK_IN_YEAR)) {
                result = -1;
            }
        }
        catch (NumberFormatException e) {
            // suppress
        }
        return result;

    }

}
