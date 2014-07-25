/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2006, by Object Refinery Limited and Contributors.
 * 
 * Project Info:  http://www.jfree.org/jcommon/index.html
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
 * SerialDate.java
 * ---------------
 * (C) Copyright 2001-2006, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: SerialDate.java,v 1.8 2006/08/29 13:44:16 mungady Exp $
 *
 * Changes (from 11-Oct-2001)
 * --------------------------
 * 11-Oct-2001 : Re-organised the class and moved it to new package 
 *               com.jrefinery.date (DG);
 * 05-Nov-2001 : Added a getDescription() method, and eliminated NotableDate 
 *               class (DG);
 * 12-Nov-2001 : IBD requires setDescription() method, now that NotableDate 
 *               class is gone (DG);  Changed getPreviousDayOfWeek(), 
 *               getFollowingDayOfWeek() and getNearestDayOfWeek() to correct 
 *               bugs (DG);
 * 05-Dec-2001 : Fixed bug in SpreadsheetDate class (DG);
 * 29-May-2002 : Moved the month constants into a separate interface 
 *               (MonthConstants) (DG);
 * 27-Aug-2002 : Fixed bug in addMonths() method, thanks to N???levka Petr (DG);
 * 03-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 13-Mar-2003 : Implemented Serializable (DG);
 * 29-May-2003 : Fixed bug in addMonths method (DG);
 * 04-Sep-2003 : Implemented Comparable.  Updated the isInRange javadocs (DG);
 * 05-Jan-2005 : Fixed bug in addYears() method (1096282) (DG);
 * 
 */

package org.jfree.date;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *  An abstract class that defines our requirements for manipulating dates,
 *  without tying down a particular implementation.
 *  <P>
 *  Requirement 1 : match at least what Excel does for dates;
 *  Requirement 2 : the date represented by the class is immutable;
 *  <P>
 *  Why not just use java.util.Date?  We will, when it makes sense.  At times,
 *  java.util.Date can be *too* precise - it represents an instant in time,
 *  accurate to 1/1000th of a second (with the date itself depending on the
 *  time-zone).  Sometimes we just want to represent a particular day (e.g. 21
 *  January 2015) without concerning ourselves about the time of day, or the
 *  time-zone, or anything else.  That's what we've defined SerialDate for.
 *  <P>
 *  You can call getInstance() to get a concrete subclass of SerialDate,
 *  without worrying about the exact implementation.
 *
 * @author David Gilbert
 */
public abstract class SerialDate implements Comparable, 
                                            Serializable, 
                                            MonthConstants {

    /** For serialization. */
    private static final long serialVersionUID = -293716040467423637L;
    
    /** Date format symbols. */
    public static final DateFormatSymbols
        DATE_FORMAT_SYMBOLS = new SimpleDateFormat().getDateFormatSymbols();

    /** The serial number for 1 January 1900. */
    public static final int SERIAL_LOWER_BOUND = 2;

    /** The serial number for 31 December 9999. */
    public static final int SERIAL_UPPER_BOUND = 2958465;

    /** The lowest year value supported by this date format. */
    public static final int MINIMUM_YEAR_SUPPORTED = 1900;

    /** The highest year value supported by this date format. */
    public static final int MAXIMUM_YEAR_SUPPORTED = 9999;

    /** Useful constant for Monday. Equivalent to java.util.Calendar.MONDAY. */
    public static final int MONDAY = Calendar.MONDAY;

    /** 
     * Useful constant for Tuesday. Equivalent to java.util.Calendar.TUESDAY. 
     */
    public static final int TUESDAY = Calendar.TUESDAY;

    /** 
     * Useful constant for Wednesday. Equivalent to 
     * java.util.Calendar.WEDNESDAY. 
     */
    public static final int WEDNESDAY = Calendar.WEDNESDAY;

    /** 
     * Useful constant for Thrusday. Equivalent to java.util.Calendar.THURSDAY. 
     */
    public static final int THURSDAY = Calendar.THURSDAY;

    /** Useful constant for Friday. Equivalent to java.util.Calendar.FRIDAY. */
    public static final int FRIDAY = Calendar.FRIDAY;

    /** 
     * Useful constant for Saturday. Equivalent to java.util.Calendar.SATURDAY.
     */
    public static final int SATURDAY = Calendar.SATURDAY;

    /** Useful constant for Sunday. Equivalent to java.util.Calendar.SUNDAY. */
    public static final int SUNDAY = Calendar.SUNDAY;

    /** The number of days in each month in non leap years. */
    static final int[] LAST_DAY_OF_MONTH =
        {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    /** The number of days in a (non-leap) year up to the end of each month. */
    static final int[] AGGREGATE_DAYS_TO_END_OF_MONTH =
        {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365};

    /** The number of days in a year up to the end of the preceding month. */
    static final int[] AGGREGATE_DAYS_TO_END_OF_PRECEDING_MONTH =
        {0, 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365};

    /** The number of days in a leap year up to the end of each month. */
    static final int[] LEAP_YEAR_AGGREGATE_DAYS_TO_END_OF_MONTH =
        {0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366};

    /** 
     * The number of days in a leap year up to the end of the preceding month. 
     */
    static final int[] 
        LEAP_YEAR_AGGREGATE_DAYS_TO_END_OF_PRECEDING_MONTH =
            {0, 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366};

    /** A useful constant for referring to the first week in a month. */
    public static final int FIRST_WEEK_IN_MONTH = 1;

    /** A useful constant for referring to the second week in a month. */
    public static final int SECOND_WEEK_IN_MONTH = 2;

    /** A useful constant for referring to the third week in a month. */
    public static final int THIRD_WEEK_IN_MONTH = 3;

    /** A useful constant for referring to the fourth week in a month. */
    public static final int FOURTH_WEEK_IN_MONTH = 4;

    /** A useful constant for referring to the last week in a month. */
    public static final int LAST_WEEK_IN_MONTH = 0;

    /** Useful range constant. */
    public static final int INCLUDE_NONE = 0;

    /** Useful range constant. */
    public static final int INCLUDE_FIRST = 1;

    /** Useful range constant. */
    public static final int INCLUDE_SECOND = 2;

    /** Useful range constant. */
    public static final int INCLUDE_BOTH = 3;

    /** 
     * Useful constant for specifying a day of the week relative to a fixed 
     * date. 
     */
    public static final int PRECEDING = -1;

    /** 
     * Useful constant for specifying a day of the week relative to a fixed 
     * date. 
     */
    public static final int NEAREST = 0;

    /** 
     * Useful constant for specifying a day of the week relative to a fixed 
     * date. 
     */
    public static final int FOLLOWING = 1;

    /** A description for the date. */
    private String description;

    /**
     * Default constructor.
     */
    protected SerialDate() {
    }

    /**
     * Returns <code>true</code> if the supplied integer code represents a 
     * valid day-of-the-week, and <code>false</code> otherwise.
     *
     * @param code  the code being checked for validity.
     *
     * @return <code>true</code> if the supplied integer code represents a 
     *         valid day-of-the-week, and <code>false</code> otherwise.
     */
    public static boolean isValidWeekdayCode(final int code) {

        switch(code) {
            case SUNDAY: 
            case MONDAY: 
            case TUESDAY: 
            case WEDNESDAY: 
            case THURSDAY: 
            case FRIDAY: 
            case SATURDAY: 
                return true;
            default: 
                return false;
        }

    }

    /**
     * Converts the supplied string to a day of the week.
     *
     * @param s  a string representing the day of the week.
     *
     * @return <code>-1</code> if the string is not convertable, the day of 
     *         the week otherwise.
     */
    public static int stringToWeekdayCode(String s) {

        final String[] shortWeekdayNames 
            = DATE_FORMAT_SYMBOLS.getShortWeekdays();
        final String[] weekDayNames = DATE_FORMAT_SYMBOLS.getWeekdays();

        int result = -1;
        s = s.trim();
        for (int i = 0; i < weekDayNames.length; i++) {
            if (s.equals(shortWeekdayNames[i])) {
                result = i;
                break;
            }
            if (s.equals(weekDayNames[i])) {
                result = i;
                break;
            }
        }
        return result;

    }

    /**
     * Returns a string representing the supplied day-of-the-week.
     * <P>
     * Need to find a better approach.
     *
     * @param weekday  the day of the week.
     *
     * @return a string representing the supplied day-of-the-week.
     */
    public static String weekdayCodeToString(final int weekday) {

        final String[] weekdays = DATE_FORMAT_SYMBOLS.getWeekdays();
        return weekdays[weekday];

    }

    /**
     * Returns an array of month names.
     *
     * @return an array of month names.
     */
    public static String[] getMonths() {

        return getMonths(false);

    }

    /**
     * Returns an array of month names.
     *
     * @param shortened  a flag indicating that shortened month names should 
     *                   be returned.
     *
     * @return an array of month names.
     */
    public static String[] getMonths(final boolean shortened) {

        if (shortened) {
            return DATE_FORMAT_SYMBOLS.getShortMonths();
        }
        else {
            return DATE_FORMAT_SYMBOLS.getMonths();
        }

    }

    /**
     * Returns true if the supplied integer code represents a valid month.
     *
     * @param code  the code being checked for validity.
     *
     * @return <code>true</code> if the supplied integer code represents a 
     *         valid month.
     */
    public static boolean isValidMonthCode(final int code) {

        switch(code) {
            case JANUARY: 
            case FEBRUARY: 
            case MARCH: 
            case APRIL: 
            case MAY: 
            case JUNE: 
            case JULY: 
            case AUGUST: 
            case SEPTEMBER: 
            case OCTOBER: 
            case NOVEMBER: 
            case DECEMBER: 
                return true;
            default: 
                return false;
        }

    }

    /**
     * Returns the quarter for the specified month.
     *
     * @param code  the month code (1-12).
     *
     * @return the quarter that the month belongs to.
     */
    public static int monthCodeToQuarter(final int code) {

        switch(code) {
            case JANUARY: 
            case FEBRUARY: 
            case MARCH: return 1;
            case APRIL: 
            case MAY: 
            case JUNE: return 2;
            case JULY: 
            case AUGUST: 
            case SEPTEMBER: return 3;
            case OCTOBER: 
            case NOVEMBER: 
            case DECEMBER: return 4;
            default: throw new IllegalArgumentException(
                "SerialDate.monthCodeToQuarter: invalid month code.");
        }

    }

    /**
     * Returns a string representing the supplied month.
     * <P>
     * The string returned is the long form of the month name taken from the 
     * default locale.
     *
     * @param month  the month.
     *
     * @return a string representing the supplied month.
     */
    public static String monthCodeToString(final int month) {

        return monthCodeToString(month, false);

    }

    /**
     * Returns a string representing the supplied month.
     * <P>
     * The string returned is the long or short form of the month name taken 
     * from the default locale.
     *
     * @param month  the month.
     * @param shortened  if <code>true</code> return the abbreviation of the 
     *                   month.
     *
     * @return a string representing the supplied month.
     */
    public static String monthCodeToString(final int month, 
                                           final boolean shortened) {

        // check arguments...
        if (!isValidMonthCode(month)) {
            throw new IllegalArgumentException(
                "SerialDate.monthCodeToString: month outside valid range.");
        }

        final String[] months;

        if (shortened) {
            months = DATE_FORMAT_SYMBOLS.getShortMonths();
        }
        else {
            months = DATE_FORMAT_SYMBOLS.getMonths();
        }

        return months[month - 1];

    }

    /**
     * Converts a string to a month code.
     * <P>
     * This method will return one of the constants JANUARY, FEBRUARY, ..., 
     * DECEMBER that corresponds to the string.  If the string is not 
     * recognised, this method returns -1.
     *
     * @param s  the string to parse.
     *
     * @return <code>-1</code> if the string is not parseable, the month of the
     *         year otherwise.
     */
    public static int stringToMonthCode(String s) {

        final String[] shortMonthNames = DATE_FORMAT_SYMBOLS.getShortMonths();
        final String[] monthNames = DATE_FORMAT_SYMBOLS.getMonths();

        int result = -1;
        s = s.trim();

        // first try parsing the string as an integer (1-12)...
        try {
            result = Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            // suppress
        }

        // now search through the month names...
        if ((result < 1) || (result > 12)) {
            for (int i = 0; i < monthNames.length; i++) {
                if (s.equals(shortMonthNames[i])) {
                    result = i + 1;
                    break;
                }
                if (s.equals(monthNames[i])) {
                    result = i + 1;
                    break;
                }
            }
        }

        return result;

    }

    /**
     * Returns true if the supplied integer code represents a valid 
     * week-in-the-month, and false otherwise.
     *
     * @param code  the code being checked for validity.
     * @return <code>true</code> if the supplied integer code represents a 
     *         valid week-in-the-month.
     */
    public static boolean isValidWeekInMonthCode(final int code) {

        switch(code) {
            case FIRST_WEEK_IN_MONTH: 
            case SECOND_WEEK_IN_MONTH: 
            case THIRD_WEEK_IN_MONTH: 
            case FOURTH_WEEK_IN_MONTH: 
            case LAST_WEEK_IN_MONTH: return true;
            default: return false;
        }

    }

    /**
     * Determines whether or not the specified year is a leap year.
     *
     * @param yyyy  the year (in the range 1900 to 9999).
     *
     * @return <code>true</code> if the specified year is a leap year.
     */
    public static boolean isLeapYear(final int yyyy) {

        if ((yyyy % 4) != 0) {
            return false;
        }
        else if ((yyyy % 400) == 0) {
            return true;
        }
        else if ((yyyy % 100) == 0) {
            return false;
        }
        else {
            return true;
        }

    }

    /**
     * Returns the number of leap years from 1900 to the specified year 
     * INCLUSIVE.
     * <P>
     * Note that 1900 is not a leap year.
     *
     * @param yyyy  the year (in the range 1900 to 9999).
     *
     * @return the number of leap years from 1900 to the specified year.
     */
    public static int leapYearCount(final int yyyy) {

        final int leap4 = (yyyy - 1896) / 4;
        final int leap100 = (yyyy - 1800) / 100;
        final int leap400 = (yyyy - 1600) / 400;
        return leap4 - leap100 + leap400;

    }

    /**
     * Returns the number of the last day of the month, taking into account 
     * leap years.
     *
     * @param month  the month.
     * @param yyyy  the year (in the range 1900 to 9999).
     *
     * @return the number of the last day of the month.
     */
    public static int lastDayOfMonth(final int month, final int yyyy) {

        final int result = LAST_DAY_OF_MONTH[month];
        if (month != FEBRUARY) {
            return result;
        }
        else if (isLeapYear(yyyy)) {
            return result + 1;
        }
        else {
            return result;
        }

    }

    /**
     * Creates a new date by adding the specified number of days to the base 
     * date.
     *
     * @param days  the number of days to add (can be negative).
     * @param base  the base date.
     *
     * @return a new date.
     */
    public static SerialDate addDays(final int days, final SerialDate base) {

        final int serialDayNumber = base.toSerial() + days;
        return SerialDate.createInstance(serialDayNumber);

    }

    /**
     * Creates a new date by adding the specified number of months to the base 
     * date.
     * <P>
     * If the base date is close to the end of the month, the day on the result
     * may be adjusted slightly:  31 May + 1 month = 30 June.
     *
     * @param months  the number of months to add (can be negative).
     * @param base  the base date.
     *
     * @return a new date.
     */
    public static SerialDate addMonths(final int months, 
                                       final SerialDate base) {

        final int yy = (12 * base.getYYYY() + base.getMonth() + months - 1) 
                       / 12;
        final int mm = (12 * base.getYYYY() + base.getMonth() + months - 1) 
                       % 12 + 1;
        final int dd = Math.min(
            base.getDayOfMonth(), SerialDate.lastDayOfMonth(mm, yy)
        );
        return SerialDate.createInstance(dd, mm, yy);

    }

    /**
     * Creates a new date by adding the specified number of years to the base 
     * date.
     *
     * @param years  the number of years to add (can be negative).
     * @param base  the base date.
     *
     * @return A new date.
     */
    public static SerialDate addYears(final int years, final SerialDate base) {

        final int baseY = base.getYYYY();
        final int baseM = base.getMonth();
        final int baseD = base.getDayOfMonth();

        final int targetY = baseY + years;
        final int targetD = Math.min(
            baseD, SerialDate.lastDayOfMonth(baseM, targetY)
        );

        return SerialDate.createInstance(targetD, baseM, targetY);

    }

    /**
     * Returns the latest date that falls on the specified day-of-the-week and 
     * is BEFORE the base date.
     *
     * @param targetWeekday  a code for the target day-of-the-week.
     * @param base  the base date.
     *
     * @return the latest date that falls on the specified day-of-the-week and 
     *         is BEFORE the base date.
     */
    public static SerialDate getPreviousDayOfWeek(final int targetWeekday, 
                                                  final SerialDate base) {

        // check arguments...
        if (!SerialDate.isValidWeekdayCode(targetWeekday)) {
            throw new IllegalArgumentException(
                "Invalid day-of-the-week code."
            );
        }

        // find the date...
        final int adjust;
        final int baseDOW = base.getDayOfWeek();
        if (baseDOW > targetWeekday) {
            adjust = Math.min(0, targetWeekday - baseDOW);
        }
        else {
            adjust = -7 + Math.max(0, targetWeekday - baseDOW);
        }

        return SerialDate.addDays(adjust, base);

    }

    /**
     * Returns the earliest date that falls on the specified day-of-the-week
     * and is AFTER the base date.
     *
     * @param targetWeekday  a code for the target day-of-the-week.
     * @param base  the base date.
     *
     * @return the earliest date that falls on the specified day-of-the-week 
     *         and is AFTER the base date.
     */
    public static SerialDate getFollowingDayOfWeek(final int targetWeekday, 
                                                   final SerialDate base) {

        // check arguments...
        if (!SerialDate.isValidWeekdayCode(targetWeekday)) {
            throw new IllegalArgumentException(
                "Invalid day-of-the-week code."
            );
        }

        // find the date...
        final int adjust;
        final int baseDOW = base.getDayOfWeek();
        if (baseDOW > targetWeekday) {
            adjust = 7 + Math.min(0, targetWeekday - baseDOW);
        }
        else {
            adjust = Math.max(0, targetWeekday - baseDOW);
        }

        return SerialDate.addDays(adjust, base);
    }

    /**
     * Returns the date that falls on the specified day-of-the-week and is
     * CLOSEST to the base date.
     *
     * @param targetDOW  a code for the target day-of-the-week.
     * @param base  the base date.
     *
     * @return the date that falls on the specified day-of-the-week and is 
     *         CLOSEST to the base date.
     */
    public static SerialDate getNearestDayOfWeek(final int targetDOW,  
                                                 final SerialDate base) {

        // check arguments...
        if (!SerialDate.isValidWeekdayCode(targetDOW)) {
            throw new IllegalArgumentException(
                "Invalid day-of-the-week code."
            );
        }

        // find the date...
        final int baseDOW = base.getDayOfWeek();
        int adjust = -Math.abs(targetDOW - baseDOW);
        if (adjust >= 4) {
            adjust = 7 - adjust;
        }
        if (adjust <= -4) {
            adjust = 7 + adjust;
        }
        return SerialDate.addDays(adjust, base);

    }

    /**
     * Rolls the date forward to the last day of the month.
     *
     * @param base  the base date.
     *
     * @return a new serial date.
     */
    public SerialDate getEndOfCurrentMonth(final SerialDate base) {
        final int last = SerialDate.lastDayOfMonth(
            base.getMonth(), base.getYYYY()
        );
        return SerialDate.createInstance(last, base.getMonth(), base.getYYYY());
    }

    /**
     * Returns a string corresponding to the week-in-the-month code.
     * <P>
     * Need to find a better approach.
     *
     * @param count  an integer code representing the week-in-the-month.
     *
     * @return a string corresponding to the week-in-the-month code.
     */
    public static String weekInMonthToString(final int count) {

        switch (count) {
            case SerialDate.FIRST_WEEK_IN_MONTH : return "First";
            case SerialDate.SECOND_WEEK_IN_MONTH : return "Second";
            case SerialDate.THIRD_WEEK_IN_MONTH : return "Third";
            case SerialDate.FOURTH_WEEK_IN_MONTH : return "Fourth";
            case SerialDate.LAST_WEEK_IN_MONTH : return "Last";
            default :
                return "SerialDate.weekInMonthToString(): invalid code.";
        }

    }

    /**
     * Returns a string representing the supplied 'relative'.
     * <P>
     * Need to find a better approach.
     *
     * @param relative  a constant representing the 'relative'.
     *
     * @return a string representing the supplied 'relative'.
     */
    public static String relativeToString(final int relative) {

        switch (relative) {
            case SerialDate.PRECEDING : return "Preceding";
            case SerialDate.NEAREST : return "Nearest";
            case SerialDate.FOLLOWING : return "Following";
            default : return "ERROR : Relative To String";
        }

    }

    /**
     * Factory method that returns an instance of some concrete subclass of 
     * {@link SerialDate}.
     *
     * @param day  the day (1-31).
     * @param month  the month (1-12).
     * @param yyyy  the year (in the range 1900 to 9999).
     *
     * @return An instance of {@link SerialDate}.
     */
    public static SerialDate createInstance(final int day, final int month, 
                                            final int yyyy) {
        return new SpreadsheetDate(day, month, yyyy);
    }

    /**
     * Factory method that returns an instance of some concrete subclass of 
     * {@link SerialDate}.
     *
     * @param serial  the serial number for the day (1 January 1900 = 2).
     *
     * @return a instance of SerialDate.
     */
    public static SerialDate createInstance(final int serial) {
        return new SpreadsheetDate(serial);
    }

    /**
     * Factory method that returns an instance of a subclass of SerialDate.
     *
     * @param date  A Java date object.
     *
     * @return a instance of SerialDate.
     */
    public static SerialDate createInstance(final java.util.Date date) {

        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return new SpreadsheetDate(calendar.get(Calendar.DATE),
                                   calendar.get(Calendar.MONTH) + 1,
                                   calendar.get(Calendar.YEAR));

    }

    /**
     * Returns the serial number for the date, where 1 January 1900 = 2 (this
     * corresponds, almost, to the numbering system used in Microsoft Excel for
     * Windows and Lotus 1-2-3).
     *
     * @return the serial number for the date.
     */
    public abstract int toSerial();

    /**
     * Returns a java.util.Date.  Since java.util.Date has more precision than
     * SerialDate, we need to define a convention for the 'time of day'.
     *
     * @return this as <code>java.util.Date</code>.
     */
    public abstract java.util.Date toDate();

    /**
     * Returns the description that is attached to the date.  It is not 
     * required that a date have a description, but for some applications it 
     * is useful.
     *
     * @return The description (possibly <code>null</code>).
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description for the date.
     *
     * @param description  the description for this date (<code>null</code> 
     *                     permitted).
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Converts the date to a string.
     *
     * @return  a string representation of the date.
     */
    public String toString() {
        return getDayOfMonth() + "-" + SerialDate.monthCodeToString(getMonth())
                               + "-" + getYYYY();
    }

    /**
     * Returns the year (assume a valid range of 1900 to 9999).
     *
     * @return the year.
     */
    public abstract int getYYYY();

    /**
     * Returns the month (January = 1, February = 2, March = 3).
     *
     * @return the month of the year.
     */
    public abstract int getMonth();

    /**
     * Returns the day of the month.
     *
     * @return the day of the month.
     */
    public abstract int getDayOfMonth();

    /**
     * Returns the day of the week.
     *
     * @return the day of the week.
     */
    public abstract int getDayOfWeek();

    /**
     * Returns the difference (in days) between this date and the specified 
     * 'other' date.
     * <P>
     * The result is positive if this date is after the 'other' date and
     * negative if it is before the 'other' date.
     *
     * @param other  the date being compared to.
     *
     * @return the difference between this and the other date.
     */
    public abstract int compare(SerialDate other);

    /**
     * Returns true if this SerialDate represents the same date as the 
     * specified SerialDate.
     *
     * @param other  the date being compared to.
     *
     * @return <code>true</code> if this SerialDate represents the same date as 
     *         the specified SerialDate.
     */
    public abstract boolean isOn(SerialDate other);

    /**
     * Returns true if this SerialDate represents an earlier date compared to
     * the specified SerialDate.
     *
     * @param other  The date being compared to.
     *
     * @return <code>true</code> if this SerialDate represents an earlier date 
     *         compared to the specified SerialDate.
     */
    public abstract boolean isBefore(SerialDate other);

    /**
     * Returns true if this SerialDate represents the same date as the 
     * specified SerialDate.
     *
     * @param other  the date being compared to.
     *
     * @return <code>true<code> if this SerialDate represents the same date
     *         as the specified SerialDate.
     */
    public abstract boolean isOnOrBefore(SerialDate other);

    /**
     * Returns true if this SerialDate represents the same date as the 
     * specified SerialDate.
     *
     * @param other  the date being compared to.
     *
     * @return <code>true</code> if this SerialDate represents the same date
     *         as the specified SerialDate.
     */
    public abstract boolean isAfter(SerialDate other);

    /**
     * Returns true if this SerialDate represents the same date as the 
     * specified SerialDate.
     *
     * @param other  the date being compared to.
     *
     * @return <code>true</code> if this SerialDate represents the same date
     *         as the specified SerialDate.
     */
    public abstract boolean isOnOrAfter(SerialDate other);

    /**
     * Returns <code>true</code> if this {@link SerialDate} is within the 
     * specified range (INCLUSIVE).  The date order of d1 and d2 is not 
     * important.
     *
     * @param d1  a boundary date for the range.
     * @param d2  the other boundary date for the range.
     *
     * @return A boolean.
     */
    public abstract boolean isInRange(SerialDate d1, SerialDate d2);

    /**
     * Returns <code>true</code> if this {@link SerialDate} is within the 
     * specified range (caller specifies whether or not the end-points are 
     * included).  The date order of d1 and d2 is not important.
     *
     * @param d1  a boundary date for the range.
     * @param d2  the other boundary date for the range.
     * @param include  a code that controls whether or not the start and end 
     *                 dates are included in the range.
     *
     * @return A boolean.
     */
    public abstract boolean isInRange(SerialDate d1, SerialDate d2, 
                                      int include);

    /**
     * Returns the latest date that falls on the specified day-of-the-week and
     * is BEFORE this date.
     *
     * @param targetDOW  a code for the target day-of-the-week.
     *
     * @return the latest date that falls on the specified day-of-the-week and
     *         is BEFORE this date.
     */
    public SerialDate getPreviousDayOfWeek(final int targetDOW) {
        return getPreviousDayOfWeek(targetDOW, this);
    }

    /**
     * Returns the earliest date that falls on the specified day-of-the-week
     * and is AFTER this date.
     *
     * @param targetDOW  a code for the target day-of-the-week.
     *
     * @return the earliest date that falls on the specified day-of-the-week
     *         and is AFTER this date.
     */
    public SerialDate getFollowingDayOfWeek(final int targetDOW) {
        return getFollowingDayOfWeek(targetDOW, this);
    }

    /**
     * Returns the nearest date that falls on the specified day-of-the-week.
     *
     * @param targetDOW  a code for the target day-of-the-week.
     *
     * @return the nearest date that falls on the specified day-of-the-week.
     */
    public SerialDate getNearestDayOfWeek(final int targetDOW) {
        return getNearestDayOfWeek(targetDOW, this);
    }

}
