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
 * -----------------
 * DateTickUnit.java
 * -----------------
 * (C) Copyright 2000-2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Chris Boek;
 *
 * Changes
 * -------
 * 08-Nov-2002 : Moved to new package com.jrefinery.chart.axis (DG);
 * 27-Nov-2002 : Added IllegalArgumentException to getMillisecondCount()
 *               method (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 12-Nov-2003 : Added roll fields that can improve the labelling on segmented
 *               date axes (DG);
 * 03-Dec-2003 : DateFormat constructor argument is now filled with an default
 *               if null (TM);
 * 07-Dec-2003 : Fixed bug (null pointer exception) in constructor (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 21-Mar-2007 : Added toString() for debugging (DG);
 * 04-Apr-2007 : Added new methods addToDate(Date, TimeZone) and rollDate(Date,
 *               TimeZone) (CB);
 * 09-Jun-2008 : Deprecated addToDate(Date) (DG);
 * 09-Jan-2009 : Replaced the unit and rollUnit fields with an enumerated
 *               type (DG);
 *
 */

package org.jfree.chart.axis;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.jfree.util.ObjectUtilities;

/**
 * A tick unit for use by subclasses of {@link DateAxis}.  Instances of this
 * class are immutable.
 */
public class DateTickUnit extends TickUnit implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -7289292157229621901L;

    /**
     * The units.
     *
     * @since 1.0.13
     */
    private DateTickUnitType unitType;

    /** The unit count. */
    private int count;

    /**
     * The roll unit type.
     *
     * @since 1.0.13
     */
    private DateTickUnitType rollUnitType;

    /** The roll count. */
    private int rollCount;

    /** The date formatter. */
    private DateFormat formatter;

    /**
     * Creates a new date tick unit.
     *
     * @param unitType  the unit type (<code>null</code> not permitted).
     * @param multiple  the multiple (of the unit type, must be > 0).
     *
     * @since 1.0.13
     */
    public DateTickUnit(DateTickUnitType unitType, int multiple) {
        this(unitType, multiple, DateFormat.getDateInstance(DateFormat.SHORT));
    }

    /**
     * Creates a new date tick unit.
     *
     * @param unitType  the unit type (<code>null</code> not permitted).
     * @param multiple  the multiple (of the unit type, must be > 0).
     * @param formatter  the date formatter (<code>null</code> not permitted).
     *
     * @since 1.0.13
     */
    public DateTickUnit(DateTickUnitType unitType, int multiple,
            DateFormat formatter) {
        this(unitType, multiple, unitType, multiple, formatter);
    }

    /**
     * Creates a new unit.
     *
     * @param unitType  the unit.
     * @param multiple  the multiple.
     * @param rollUnitType  the roll unit.
     * @param rollMultiple  the roll multiple.
     * @param formatter  the date formatter (<code>null</code> not permitted).
     *
     * @since 1.0.13
     */
    public DateTickUnit(DateTickUnitType unitType, int multiple,
            DateTickUnitType rollUnitType, int rollMultiple,
            DateFormat formatter) {
        super(DateTickUnit.getMillisecondCount(unitType, multiple));
        if (formatter == null) {
            throw new IllegalArgumentException("Null 'formatter' argument.");
        }
        if (multiple <= 0) {
            throw new IllegalArgumentException("Requires 'multiple' > 0.");
        }
        if (rollMultiple <= 0) {
            throw new IllegalArgumentException("Requires 'rollMultiple' > 0.");
        }
        this.unitType = unitType;
        this.count = multiple;
        this.rollUnitType = rollUnitType;
        this.rollCount = rollMultiple;
        this.formatter = formatter;

        // populate deprecated fields
        this.unit = unitTypeToInt(unitType);
        this.rollUnit = unitTypeToInt(rollUnitType);
    }

    /**
     * Returns the unit type.
     *
     * @return The unit type (never <code>null</code>).
     *
     * @since 1.0.13
     */
    public DateTickUnitType getUnitType() {
        return this.unitType;
    }

    /**
     * Returns the unit multiple.
     *
     * @return The unit multiple (always > 0).
     */
    public int getMultiple() {
        return this.count;
    }

    /**
     * Returns the roll unit type.
     *
     * @return The roll unit type (never <code>null</code>).
     *
     * @since 1.0.13
     */
    public DateTickUnitType getRollUnitType() {
        return this.rollUnitType;
    }

    /**
     * Returns the roll unit multiple.
     *
     * @return The roll unit multiple.
     *
     * @since 1.0.13
     */
    public int getRollMultiple() {
        return this.rollCount;
    }

    /**
     * Formats a value.
     *
     * @param milliseconds  date in milliseconds since 01-01-1970.
     *
     * @return The formatted date.
     */
    public String valueToString(double milliseconds) {
        return this.formatter.format(new Date((long) milliseconds));
    }

    /**
     * Formats a date using the tick unit's formatter.
     *
     * @param date  the date.
     *
     * @return The formatted date.
     */
    public String dateToString(Date date) {
        return this.formatter.format(date);
    }

    /**
     * Calculates a new date by adding this unit to the base date.
     *
     * @param base  the base date.
     * @param zone  the time zone for the date calculation.
     *
     * @return A new date one unit after the base date.
     *
     * @since 1.0.6
     */
    public Date addToDate(Date base, TimeZone zone) {
        // as far as I know, the Locale for the calendar only affects week
        // number calculations, and since DateTickUnit doesn't do week
        // arithmetic, the default locale (whatever it is) should be fine
        // here...
        Calendar calendar = Calendar.getInstance(zone);
        calendar.setTime(base);
        calendar.add(this.unitType.getCalendarField(), this.count);
        return calendar.getTime();
    }

    /**
     * Rolls the date forward by the amount specified by the roll unit and
     * count.
     *
     * @param base  the base date.

     * @return The rolled date.
     *
     * @see #rollDate(Date, TimeZone)
     */
    public Date rollDate(Date base) {
        return rollDate(base, TimeZone.getDefault());
    }

    /**
     * Rolls the date forward by the amount specified by the roll unit and
     * count.
     *
     * @param base  the base date.
     * @param zone  the time zone.
     *
     * @return The rolled date.
     *
     * @since 1.0.6
     */
    public Date rollDate(Date base, TimeZone zone) {
        // as far as I know, the Locale for the calendar only affects week
        // number calculations, and since DateTickUnit doesn't do week
        // arithmetic, the default locale (whatever it is) should be fine
        // here...
        Calendar calendar = Calendar.getInstance(zone);
        calendar.setTime(base);
        calendar.add(this.rollUnitType.getCalendarField(), this.rollCount);
        return calendar.getTime();
    }

    /**
     * Returns a field code that can be used with the <code>Calendar</code>
     * class.
     *
     * @return The field code.
     */
    public int getCalendarField() {
        return this.unitType.getCalendarField();
    }

    /**
     * Returns the (approximate) number of milliseconds for the given unit and
     * unit count.
     * <P>
     * This value is an approximation some of the time (e.g. months are
     * assumed to have 31 days) but this shouldn't matter.
     *
     * @param unit  the unit.
     * @param count  the unit count.
     *
     * @return The number of milliseconds.
     *
     * @since 1.0.13
     */
    private static long getMillisecondCount(DateTickUnitType unit, int count) {

        if (unit.equals(DateTickUnitType.YEAR)) {
            return (365L * 24L * 60L * 60L * 1000L) * count;
        }
        else if (unit.equals(DateTickUnitType.MONTH)) {
            return (31L * 24L * 60L * 60L * 1000L) * count;
        }
        else if (unit.equals(DateTickUnitType.DAY)) {
            return (24L * 60L * 60L * 1000L) * count;
        }
        else if (unit.equals(DateTickUnitType.HOUR)) {
            return (60L * 60L * 1000L) * count;
        }
        else if (unit.equals(DateTickUnitType.MINUTE)) {
            return (60L * 1000L) * count;
        }
        else if (unit.equals(DateTickUnitType.SECOND)) {
            return 1000L * count;
        }
        else if (unit.equals(DateTickUnitType.MILLISECOND)) {
            return count;
        }
        else {
            throw new IllegalArgumentException("The 'unit' argument has a " +
            		"value that is not recognised.");
        }

    }

    /**
     * A utility method that is used internally to convert the old unit
     * constants into the corresponding enumerated value.
     *
     * @param unit  the unit specified using the deprecated integer codes.
     *
     * @return The unit type.
     *
     * @since 1.0.13
     */
    private static DateTickUnitType intToUnitType(int unit) {
        switch (unit) {
            case YEAR: return DateTickUnitType.YEAR;
            case MONTH: return DateTickUnitType.MONTH;
            case DAY: return DateTickUnitType.DAY;
            case HOUR: return DateTickUnitType.HOUR;
            case MINUTE: return DateTickUnitType.MINUTE;
            case SECOND: return DateTickUnitType.SECOND;
            case MILLISECOND: return DateTickUnitType.MILLISECOND;
            default: throw new IllegalArgumentException(
                    "Unrecognised 'unit' value " + unit + ".");
        }
    }

    /**
     * Converts a unit type to the corresponding deprecated integer constant.
     *
     * @param unitType  the unit type (<code>null</code> not permitted).
     *
     * @return The int code.
     *
     * @since 1.0.13
     */
    private static int unitTypeToInt(DateTickUnitType unitType) {
        if (unitType == null) {
            throw new IllegalArgumentException("Null 'unitType' argument.");
        }
        if (unitType.equals(DateTickUnitType.YEAR)) {
            return YEAR;
        }
        else if (unitType.equals(DateTickUnitType.MONTH)) {
            return MONTH;
        }
        else if (unitType.equals(DateTickUnitType.DAY)) {
            return DAY;
        }
        else if (unitType.equals(DateTickUnitType.HOUR)) {
            return HOUR;
        }
        else if (unitType.equals(DateTickUnitType.MINUTE)) {
            return MINUTE;
        }
        else if (unitType.equals(DateTickUnitType.SECOND)) {
            return SECOND;
        }
        else if (unitType.equals(DateTickUnitType.MILLISECOND)) {
            return MILLISECOND;
        }
        else {
            throw new IllegalArgumentException(
                    "The 'unitType' is not recognised");
        }
    }

    /**
     * A utility method to put a default in place if a null formatter is
     * supplied.
     *
     * @param formatter  the formatter (<code>null</code> permitted).
     *
     * @return The formatter if it is not null, otherwise a default.
     */
    private static DateFormat notNull(DateFormat formatter) {
        if (formatter == null) {
            return DateFormat.getDateInstance(DateFormat.SHORT);
        }
        else {
            return formatter;
        }
    }

    /**
     * Tests this unit for equality with another object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DateTickUnit)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        DateTickUnit that = (DateTickUnit) obj;
        if (!(this.unitType.equals(that.unitType))) {
            return false;
        }
        if (this.count != that.count) {
            return false;
        }
        if (!ObjectUtilities.equal(this.formatter, that.formatter)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code for this object.
     *
     * @return A hash code.
     */
    public int hashCode() {
        int result = 19;
        result = 37 * result + this.unitType.hashCode();
        result = 37 * result + this.count;
        result = 37 * result + this.formatter.hashCode();
        return result;
    }

    /**
     * Returns a string representation of this instance, primarily used for
     * debugging purposes.
     *
     * @return A string representation of this instance.
     */
    public String toString() {
        return "DateTickUnit[" + this.unitType.toString() + ", "
                + this.count + "]";
    }

    /**
     * A constant for years.
     *
     * @deprecated As of version 1.0.13, use {@link DateTickUnitType} instead.
     */
    public static final int YEAR = 0;

    /**
     * A constant for months.
     *
     * @deprecated As of version 1.0.13, use {@link DateTickUnitType} instead.
     */
    public static final int MONTH = 1;

    /**
     * A constant for days.
     *
     * @deprecated As of version 1.0.13, use {@link DateTickUnitType} instead.
     */
    public static final int DAY = 2;

    /**
     * A constant for hours.
     *
     * @deprecated As of version 1.0.13, use {@link DateTickUnitType} instead.
     */
    public static final int HOUR = 3;

    /**
     * A constant for minutes.
     *
     * @deprecated As of version 1.0.13, use {@link DateTickUnitType} instead.
     */
    public static final int MINUTE = 4;

    /**
     * A constant for seconds.
     *
     * @deprecated As of version 1.0.13, use {@link DateTickUnitType} instead.
     */
    public static final int SECOND = 5;

    /**
     * A constant for milliseconds.
     *
     * @deprecated As of version 1.0.13, use {@link DateTickUnitType} instead.
     */
    public static final int MILLISECOND = 6;

    /**
     * The unit.
     *
     * @deprecated As of version 1.0.13, use the unitType field.
     */
    private int unit;

    /**
     * The roll unit.
     *
     * @deprecated As of version 1.0.13, use the rollUnitType field.
     */
    private int rollUnit;

    /**
     * Creates a new date tick unit.  You can specify the units using one of
     * the constants YEAR, MONTH, DAY, HOUR, MINUTE, SECOND or MILLISECOND.
     * In addition, you can specify a unit count, and a date format.
     *
     * @param unit  the unit.
     * @param count  the unit count.
     * @param formatter  the date formatter (defaults to DateFormat.SHORT).
     *
     * @deprecated As of version 1.0.13, use {@link #DateTickUnit(
     *     DateTickUnitType, int, DateFormat)}.
     */
    public DateTickUnit(int unit, int count, DateFormat formatter) {
        this(unit, count, unit, count, formatter);
    }

    /**
     * Creates a new date tick unit.  The dates will be formatted using a
     * SHORT format for the default locale.
     *
     * @param unit  the unit.
     * @param count  the unit count.
     *
     * @deprecated As of version 1.0.13, use {@link #DateTickUnit(
     *     DateTickUnitType, int)}.
     */
    public DateTickUnit(int unit, int count) {
        this(unit, count, null);
    }

    /**
     * Creates a new unit.
     *
     * @param unit  the unit.
     * @param count  the count.
     * @param rollUnit  the roll unit.
     * @param rollCount  the roll count.
     * @param formatter  the date formatter (defaults to DateFormat.SHORT).
     *
     * @deprecated As of version 1.0.13, use {@link #DateTickUnit(
     *     DateTickUnitType, int, DateTickUnitType, int, DateFormat)}.
     */
    public DateTickUnit(int unit, int count, int rollUnit, int rollCount,
                        DateFormat formatter) {
        this(intToUnitType(unit), count, intToUnitType(rollUnit), rollCount,
                notNull(formatter));
    }

    /**
     * Returns the date unit.  This will be one of the constants
     * <code>YEAR</code>, <code>MONTH</code>, <code>DAY</code>,
     * <code>HOUR</code>, <code>MINUTE</code>, <code>SECOND</code> or
     * <code>MILLISECOND</code>, defined by this class.  Note that these
     * constants do NOT correspond to those defined in Java's
     * <code>Calendar</code> class.
     *
     * @return The date unit.
     *
     * @deprecated As of 1.0.13, use the getUnitType() method.
     */
    public int getUnit() {
        return this.unit;
    }

    /**
     * Returns the unit count.
     *
     * @return The unit count.
     *
     * @deprecated As of version 1.0.13, use {@link #getMultiple()}.
     */
    public int getCount() {
        return this.count;
    }

    /**
     * Returns the roll unit.  This is the amount by which the tick advances if
     * it is "hidden" when displayed on a segmented date axis.  Typically the
     * roll will be smaller than the regular tick unit (for example, a 7 day
     * tick unit might use a 1 day roll).
     *
     * @return The roll unit.
     *
     * @deprecated As of version 1.0.13, use {@link #getRollUnitType()}.
     */
    public int getRollUnit() {
        return this.rollUnit;
    }

    /**
     * Returns the roll count.
     *
     * @return The roll count.
     *
     * @deprecated As of version 1.0.13, use the {@link #getRollMultiple()}
     *
     */
    public int getRollCount() {
        return this.rollCount;
    }

    /**
     * Calculates a new date by adding this unit to the base date, with
     * calculations performed in the default timezone and locale.
     *
     * @param base  the base date.
     *
     * @return A new date one unit after the base date.
     *
     * @see #addToDate(Date, TimeZone)
     *
     * @deprecated As of JFreeChart 1.0.10, this method is deprecated - you
     *     should use {@link #addToDate(Date, TimeZone)} instead.
     */
    public Date addToDate(Date base) {
        return addToDate(base, TimeZone.getDefault());
    }

}
