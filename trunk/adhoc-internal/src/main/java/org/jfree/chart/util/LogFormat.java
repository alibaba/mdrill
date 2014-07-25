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
 * --------------
 * LogFormat.java
 * --------------
 * (C) Copyright 2007-2009, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 02-Aug-2007 : Version 1 (DG);
 * 19-Feb-2008 : Implemented equals() and clone(), and added new powerLabel
 *               attribute as per Feature Request 1886036 (DG);
 * 14-Jan-2009 : Added default constructor, and accessor methods for
 *               exponent formatter (DG);
 *
 */

package org.jfree.chart.util;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * A number formatter for logarithmic values.  This formatter does not support
 * parsing.
 *
 * @since 1.0.7
 */
public class LogFormat extends NumberFormat {

    /** The log base value. */
    private double base;

    /** The natural logarithm of the base value. */
    private double baseLog;

    /** The label for the log base (for example, "e"). */
    private String baseLabel;

    /**
     * The label for the power symbol.
     *
     * @since 1.0.10
     */
    private String powerLabel;

    /** A flag that controls whether or not the base is shown. */
    private boolean showBase;

    /** The number formatter for the exponent. */
    private NumberFormat formatter = new DecimalFormat("0.0#");

    /**
     * Creates a new instance using base 10.
     *
     * @since 1.0.13
     */
    public LogFormat() {
        this(10.0, "10", true);
    }

    /**
     * Creates a new instance.
     *
     * @param base  the base.
     * @param baseLabel  the base label (<code>null</code> not permitted).
     * @param showBase  a flag that controls whether or not the base value is
     *                  shown.
     */
    public LogFormat(double base, String baseLabel, boolean showBase) {
        this(base, baseLabel, "^", showBase);
    }

    /**
     * Creates a new instance.
     *
     * @param base  the base.
     * @param baseLabel  the base label (<code>null</code> not permitted).
     * @param powerLabel  the power label (<code>null</code> not permitted).
     * @param showBase  a flag that controls whether or not the base value is
     *                  shown.
     *
     * @since 1.0.10
     */
    public LogFormat(double base, String baseLabel, String powerLabel,
            boolean showBase) {
        if (baseLabel == null) {
            throw new IllegalArgumentException("Null 'baseLabel' argument.");
        }
        if (powerLabel == null) {
            throw new IllegalArgumentException("Null 'powerLabel' argument.");
        }
        this.base = base;
        this.baseLog = Math.log(this.base);
        this.baseLabel = baseLabel;
        this.showBase = showBase;
        this.powerLabel = powerLabel;
    }

    /**
     * Returns the number format used for the exponent.
     *
     * @return The number format (never <code>null</code>).
     *
     * @since 1.0.13.
     */
    public NumberFormat getExponentFormat() {
        return (NumberFormat) this.formatter.clone();
    }

    /**
     * Sets the number format used for the exponent.
     *
     * @param format  the formatter (<code>null</code> not permitted).
     *
     * @since 1.0.13
     */
    public void setExponentFormat(NumberFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("Null 'format' argument.");
        }
        this.formatter = format;
    }

    /**
     * Calculates the log of a given value.
     *
     * @param value  the value.
     *
     * @return The log of the value.
     */
    private double calculateLog(double value) {
        return Math.log(value) / this.baseLog;
    }

    /**
     * Returns a formatted representation of the specified number.
     *
     * @param number  the number.
     * @param toAppendTo  the string buffer to append to.
     * @param pos  the position.
     *
     * @return A string buffer containing the formatted value.
     */
    public StringBuffer format(double number, StringBuffer toAppendTo,
            FieldPosition pos) {
        StringBuffer result = new StringBuffer();
        if (this.showBase) {
            result.append(this.baseLabel);
            result.append(this.powerLabel);
        }
        result.append(this.formatter.format(calculateLog(number)));
        return result;
    }

    /**
     * Formats the specified number as a hexadecimal string.  The decimal
     * fraction is ignored.
     *
     * @param number  the number to format.
     * @param toAppendTo  the buffer to append to (ignored here).
     * @param pos  the field position (ignored here).
     *
     * @return The string buffer.
     */
    public StringBuffer format(long number, StringBuffer toAppendTo,
            FieldPosition pos) {
        StringBuffer result = new StringBuffer();
        if (this.showBase) {
            result.append(this.baseLabel);
            result.append("^");
        }
        result.append(this.formatter.format(calculateLog(number)));
        return result;
    }

    /**
     * Parsing is not implemented, so this method always returns
     * <code>null</code>.
     *
     * @param source  ignored.
     * @param parsePosition  ignored.
     *
     * @return Always <code>null</code>.
     */
    public Number parse (String source, ParsePosition parsePosition) {
        return null; // don't bother with parsing
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
        if (!(obj instanceof LogFormat)) {
            return false;
        }
        LogFormat that = (LogFormat) obj;
        if (this.base != that.base) {
            return false;
        }
        if (!this.baseLabel.equals(that.baseLabel)) {
            return false;
        }
        if (this.baseLog != that.baseLog) {
            return false;
        }
        if (this.showBase != that.showBase) {
            return false;
        }
        if (!this.formatter.equals(that.formatter)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns a clone of this instance.
     *
     * @return A clone.
     */
    public Object clone() {
        LogFormat clone = (LogFormat) super.clone();
        clone.formatter = (NumberFormat) this.formatter.clone();
        return clone;
    }

}
