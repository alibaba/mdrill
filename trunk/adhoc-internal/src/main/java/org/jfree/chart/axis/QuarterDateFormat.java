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
 * ----------------------
 * QuarterDateFormat.java
 * ----------------------
 * (C) Copyright 2005-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 01-Mar-2005 : Version 1 (DG);
 * 10-May-2005 : Added equals() method, and implemented Cloneable and
 *               Serializable (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 08-Jun-2007 : Added Greek symbols, and support for reversing the date - see
 *               patch 1577221 (DG);
 *
 */

package org.jfree.chart.axis;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * A formatter that formats dates to show the year and quarter (for example,
 * '2004 IV' for the last quarter of 2004.
 */
public class QuarterDateFormat extends DateFormat
        implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -6738465248529797176L;

    /** Symbols for regular quarters. */
    public static final String[] REGULAR_QUARTERS = new String[] {"1", "2",
            "3", "4"};

    /** Symbols for roman numbered quarters. */
    public static final String[] ROMAN_QUARTERS  = new String[] {"I", "II",
            "III", "IV"};

    /**
     * Symbols for greek numbered quarters.
     *
     * @since 1.0.6
     */
    public static final String[] GREEK_QUARTERS = new String[] {"\u0391",
            "\u0392", "\u0393", "\u0394"};

    /** The strings. */
    private String[] quarters = REGULAR_QUARTERS;

    /** A flag that controls whether the quarter or the year goes first. */
    private boolean quarterFirst;

    /**
     * Creates a new instance for the default time zone.
     */
    public QuarterDateFormat() {
        this(TimeZone.getDefault());
    }

    /**
     * Creates a new instance for the specified time zone.
     *
     * @param zone  the time zone (<code>null</code> not permitted).
     */
    public QuarterDateFormat(TimeZone zone) {
        this(zone, REGULAR_QUARTERS);
    }

    /**
     * Creates a new instance for the specified time zone.
     *
     * @param zone  the time zone (<code>null</code> not permitted).
     * @param quarterSymbols  the quarter symbols.
     */
    public QuarterDateFormat(TimeZone zone, String[] quarterSymbols) {
        this(zone, quarterSymbols, false);
    }

    /**
     * Creates a new instance for the specified time zone.
     *
     * @param zone  the time zone (<code>null</code> not permitted).
     * @param quarterSymbols  the quarter symbols.
     * @param quarterFirst  a flag that controls whether the quarter or the
     *         year is displayed first.
     *
     * @since 1.0.6
     */
    public QuarterDateFormat(TimeZone zone, String[] quarterSymbols,
            boolean quarterFirst) {
        if (zone == null) {
            throw new IllegalArgumentException("Null 'zone' argument.");
        }
        this.calendar = new GregorianCalendar(zone);
        this.quarters = quarterSymbols;
        this.quarterFirst = quarterFirst;

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
        int year = this.calendar.get(Calendar.YEAR);
        int month = this.calendar.get(Calendar.MONTH);
        int quarter = month / 3;
        if (this.quarterFirst) {
            toAppendTo.append(this.quarters[quarter]);
            toAppendTo.append(" ");
            toAppendTo.append(year);
        }
        else {
            toAppendTo.append(year);
            toAppendTo.append(" ");
            toAppendTo.append(this.quarters[quarter]);
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
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof QuarterDateFormat)) {
            return false;
        }
        QuarterDateFormat that = (QuarterDateFormat) obj;
        if (!Arrays.equals(this.quarters, that.quarters)) {
            return false;
        }
        if (this.quarterFirst != that.quarterFirst) {
            return false;
        }
        return super.equals(obj);
    }

}
