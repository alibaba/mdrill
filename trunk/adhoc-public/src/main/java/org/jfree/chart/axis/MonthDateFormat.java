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
 * --------------------
 * MonthDateFormat.java
 * --------------------
 * (C) Copyright 2005-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 10-May-2005 : Version 1 (DG);
 *
 */

package org.jfree.chart.axis;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.jfree.data.time.Month;

/**
 * A formatter that formats dates to show the initial letter(s) of the month
 * name and, as an option, the year for the first or last month of each year.
 */
public class MonthDateFormat extends DateFormat {

    /** The symbols used for the months. */
    private String[] months;

    /** Flags that control which months will have the year appended. */
    private boolean[] showYear;

    /** The year formatter. */
    private DateFormat yearFormatter;

    /**
     * Creates a new instance for the default time zone.
     */
    public MonthDateFormat() {
        this(TimeZone.getDefault());
    }

    /**
     * Creates a new instance for the specified time zone.
     *
     * @param zone  the time zone (<code>null</code> not permitted).
     */
    public MonthDateFormat(TimeZone zone) {
        this(zone, Locale.getDefault(), 1, true, false);
    }

    /**
     * Creates a new instance for the specified time zone.
     *
     * @param locale  the locale used to obtain the month
     *                names (<code>null</code> not permitted).
     */
    public MonthDateFormat(Locale locale) {
        this(TimeZone.getDefault(), locale, 1, true, false);
    }

    /**
     * Creates a new instance for the specified time zone.
     *
     * @param zone  the time zone (<code>null</code> not permitted).
     * @param chars  the maximum number of characters to use from the month
     *               names (that are obtained from the date symbols of the
     *               default locale).  If this value is <= 0, the entire
     *               month name is used in each case.
     */
    public MonthDateFormat(TimeZone zone, int chars) {
        this(zone, Locale.getDefault(), chars, true, false);
    }

    /**
     * Creates a new instance for the specified time zone.
     *
     * @param locale  the locale (<code>null</code> not permitted).
     * @param chars  the maximum number of characters to use from the month
     *               names (that are obtained from the date symbols of the
     *               default locale).  If this value is <= 0, the entire
     *               month name is used in each case.
     */
    public MonthDateFormat(Locale locale, int chars) {
        this(TimeZone.getDefault(), locale, chars, true, false);
    }

    /**
     * Creates a new formatter.
     *
     * @param zone  the time zone used to extract the month and year from dates
     *              passed to this formatter (<code>null</code> not permitted).
     * @param locale  the locale used to determine the month names
     *                (<code>null</code> not permitted).
     * @param chars  the maximum number of characters to use from the month
     *               names, or zero to indicate that the entire month name
     *               should be used.
     * @param showYearForJan  a flag that controls whether or not the year is
     *                        appended to the symbol for the first month of
     *                        each year.
     * @param showYearForDec  a flag that controls whether or not the year is
     *                        appended to the symbol for the last month of
     *                        each year.
     */
    public MonthDateFormat(TimeZone zone, Locale locale, int chars,
                           boolean showYearForJan, boolean showYearForDec) {
        this(zone, locale, chars, new boolean[] {showYearForJan, false, false,
            false, false, false, false, false, false, false, false, false,
            showYearForDec}, new SimpleDateFormat("yy"));
    }

    /**
     * Creates a new formatter.
     *
     * @param zone  the time zone used to extract the month and year from dates
     *              passed to this formatter (<code>null</code> not permitted).
     * @param locale  the locale used to determine the month names
     *                (<code>null</code> not permitted).
     * @param chars  the maximum number of characters to use from the month
     *               names, or zero to indicate that the entire month name
     *               should be used.
     * @param showYear  an array of flags that control whether or not the
     *                  year is displayed for a particular month.
     * @param yearFormatter  the year formatter.
     */
    public MonthDateFormat(TimeZone zone, Locale locale, int chars,
                           boolean[] showYear, DateFormat yearFormatter) {
        if (locale == null) {
            throw new IllegalArgumentException("Null 'locale' argument.");
        }
        DateFormatSymbols dfs = new DateFormatSymbols(locale);
        String[] monthsFromLocale = dfs.getMonths();
        this.months = new String[12];
        for (int i = 0; i < 12; i++) {
            if (chars > 0) {
                this.months[i] = monthsFromLocale[i].substring(0,
                        Math.min(chars, monthsFromLocale[i].length()));
            }
            else {
                this.months[i] = monthsFromLocale[i];
            }
        }
        this.calendar = new GregorianCalendar(zone);
        this.showYear = showYear;
        this.yearFormatter = yearFormatter;

        // the following is never used, but it seems that DateFormat requires
        // it to be non-null.  It isn't well covered in the spec, refer to
        // bug parade 5061189 for more info.
        this.numberFormat = NumberFormat.getNumberInstance();
    }

    /**
     * Formats the given date.
     *
     * @param date  the date.
     * @param toAppendTo  the string buffer.
     * @param fieldPosition  the field position.
     *
     * @return The formatted date.
     */
    public StringBuffer format(Date date, StringBuffer toAppendTo,
                               FieldPosition fieldPosition) {
        this.calendar.setTime(date);
        int month = this.calendar.get(Calendar.MONTH);
        toAppendTo.append(this.months[month]);
        if (this.showYear[month]) {
            toAppendTo.append(this.yearFormatter.format(date));
        }
        return toAppendTo;
    }

    /**
     * Parses the given string (not implemented).
     *
     * @param source  the date string.
     * @param pos  the parse position.
     *
     * @return <code>null</code>, as this method has not been implemented.
     */
    public Date parse(String source, ParsePosition pos) {
        return null;
    }

    /**
     * Tests this formatter for equality with an arbitrary object.
     *
     * @param obj  the object.
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MonthDateFormat)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        MonthDateFormat that = (MonthDateFormat) obj;
        if (!Arrays.equals(this.months, that.months)) {
            return false;
        }
        if (!Arrays.equals(this.showYear, that.showYear)) {
            return false;
        }
        if (!this.yearFormatter.equals(that.yearFormatter)) {
            return false;
        }
        return true;
    }

    /**
     * Some test code.
     *
     * @param args  ignored.
     */
    public static void main(String[] args) {
        MonthDateFormat mdf = new MonthDateFormat(Locale.UK, 2);
        System.out.println("UK:");
        System.out.println(mdf.format(new Month(1, 2005).getStart()));
        System.out.println(mdf.format(new Month(2, 2005).getStart()));
        System.out.println(mdf.format(new Month(3, 2005).getStart()));
        System.out.println(mdf.format(new Month(4, 2005).getStart()));
        System.out.println(mdf.format(new Month(5, 2005).getStart()));
        System.out.println(mdf.format(new Month(6, 2005).getStart()));
        System.out.println(mdf.format(new Month(7, 2005).getStart()));
        System.out.println(mdf.format(new Month(8, 2005).getStart()));
        System.out.println(mdf.format(new Month(9, 2005).getStart()));
        System.out.println(mdf.format(new Month(10, 2005).getStart()));
        System.out.println(mdf.format(new Month(11, 2005).getStart()));
        System.out.println(mdf.format(new Month(12, 2005).getStart()));
        System.out.println();

        mdf = new MonthDateFormat(Locale.GERMANY, 2);
        System.out.println("GERMANY:");
        System.out.println(mdf.format(new Month(1, 2005).getStart()));
        System.out.println(mdf.format(new Month(2, 2005).getStart()));
        System.out.println(mdf.format(new Month(3, 2005).getStart()));
        System.out.println(mdf.format(new Month(4, 2005).getStart()));
        System.out.println(mdf.format(new Month(5, 2005).getStart()));
        System.out.println(mdf.format(new Month(6, 2005).getStart()));
        System.out.println(mdf.format(new Month(7, 2005).getStart()));
        System.out.println(mdf.format(new Month(8, 2005).getStart()));
        System.out.println(mdf.format(new Month(9, 2005).getStart()));
        System.out.println(mdf.format(new Month(10, 2005).getStart()));
        System.out.println(mdf.format(new Month(11, 2005).getStart()));
        System.out.println(mdf.format(new Month(12, 2005).getStart()));
        System.out.println();

        mdf = new MonthDateFormat(Locale.FRANCE, 2);
        System.out.println("FRANCE:");
        System.out.println(mdf.format(new Month(1, 2005).getStart()));
        System.out.println(mdf.format(new Month(2, 2005).getStart()));
        System.out.println(mdf.format(new Month(3, 2005).getStart()));
        System.out.println(mdf.format(new Month(4, 2005).getStart()));
        System.out.println(mdf.format(new Month(5, 2005).getStart()));
        System.out.println(mdf.format(new Month(6, 2005).getStart()));
        System.out.println(mdf.format(new Month(7, 2005).getStart()));
        System.out.println(mdf.format(new Month(8, 2005).getStart()));
        System.out.println(mdf.format(new Month(9, 2005).getStart()));
        System.out.println(mdf.format(new Month(10, 2005).getStart()));
        System.out.println(mdf.format(new Month(11, 2005).getStart()));
        System.out.println(mdf.format(new Month(12, 2005).getStart()));
        System.out.println();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        sdf.setNumberFormat(null);
    }
}
