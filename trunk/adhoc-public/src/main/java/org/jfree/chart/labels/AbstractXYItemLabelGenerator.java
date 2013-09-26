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
 * ---------------------------------
 * AbstractXYItemLabelGenerator.java
 * ---------------------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 27-Feb-2004 : Version 1 (DG);
 * 12-May-2004 : Moved default tool tip format to
 *               StandardXYToolTipGenerator (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 08-Oct-2004 : Modified createItemArray() method to handle null values (DG);
 * 10-Jan-2005 : Updated createItemArray() to use x, y primitives if
 *               possible (DG);
 * ------------- JFREECHART 1.0.x --------------------------------------------
 * 26-Jan-2006 : Minor API doc update (DG);
 * 25-Jan-2007 : Added new constructor and fixed bug in clone() method (DG);
 * 16-Oct-2007 : Removed redundant code (DG);
 * 23-Nov-2007 : Implemented hashCode() (DG);
 * 26-May-2008 : Added accessor methods for nullYString and updated equals()
 *               method (DG);
 *
 */

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.jfree.chart.HashUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.ObjectUtilities;

/**
 * A base class for creating item label generators.
 */
public class AbstractXYItemLabelGenerator implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 5869744396278660636L;

    /** The item label format string. */
    private String formatString;

    /** A number formatter for the x value. */
    private NumberFormat xFormat;

    /** A date formatter for the x value. */
    private DateFormat xDateFormat;

    /** A formatter for the y value. */
    private NumberFormat yFormat;

    /** A date formatter for the y value. */
    private DateFormat yDateFormat;

    /** The string used to represent 'null' for the y-value. */
    private String nullYString = "null";

    /**
     * Creates an item label generator using default number formatters.
     */
    protected AbstractXYItemLabelGenerator() {
        this("{2}", NumberFormat.getNumberInstance(),
                NumberFormat.getNumberInstance());
    }

    /**
     * Creates an item label generator using the specified number formatters.
     *
     * @param formatString  the item label format string (<code>null</code>
     *                      not permitted).
     * @param xFormat  the format object for the x values (<code>null</code>
     *                 not permitted).
     * @param yFormat  the format object for the y values (<code>null</code>
     *                 not permitted).
     */
    protected AbstractXYItemLabelGenerator(String formatString,
                                           NumberFormat xFormat,
                                           NumberFormat yFormat) {

        if (formatString == null) {
            throw new IllegalArgumentException("Null 'formatString' argument.");
        }
        if (xFormat == null) {
            throw new IllegalArgumentException("Null 'xFormat' argument.");
        }
        if (yFormat == null) {
            throw new IllegalArgumentException("Null 'yFormat' argument.");
        }
        this.formatString = formatString;
        this.xFormat = xFormat;
        this.yFormat = yFormat;

    }

    /**
     * Creates an item label generator using the specified number formatters.
     *
     * @param formatString  the item label format string (<code>null</code>
     *                      not permitted).
     * @param xFormat  the format object for the x values (<code>null</code>
     *                 permitted).
     * @param yFormat  the format object for the y values (<code>null</code>
     *                 not permitted).
     */
    protected AbstractXYItemLabelGenerator(String formatString,
                                           DateFormat xFormat,
                                           NumberFormat yFormat) {

        this(formatString, NumberFormat.getInstance(), yFormat);
        this.xDateFormat = xFormat;

    }

    /**
     * Creates an item label generator using the specified formatters (a
     * number formatter for the x-values and a date formatter for the
     * y-values).
     *
     * @param formatString  the item label format string (<code>null</code>
     *                      not permitted).
     * @param xFormat  the format object for the x values (<code>null</code>
     *                 permitted).
     * @param yFormat  the format object for the y values (<code>null</code>
     *                 not permitted).
     *
     * @since 1.0.4
     */
    protected AbstractXYItemLabelGenerator(String formatString,
            NumberFormat xFormat, DateFormat yFormat) {

        this(formatString, xFormat, NumberFormat.getInstance());
        this.yDateFormat = yFormat;
    }

    /**
     * Creates an item label generator using the specified number formatters.
     *
     * @param formatString  the item label format string (<code>null</code>
     *                      not permitted).
     * @param xFormat  the format object for the x values (<code>null</code>
     *                 permitted).
     * @param yFormat  the format object for the y values (<code>null</code>
     *                 not permitted).
     */
    protected AbstractXYItemLabelGenerator(String formatString,
                                           DateFormat xFormat,
                                           DateFormat yFormat) {

        this(formatString, NumberFormat.getInstance(),
                NumberFormat.getInstance());
        this.xDateFormat = xFormat;
        this.yDateFormat = yFormat;

    }

    /**
     * Returns the format string (this controls the overall structure of the
     * label).
     *
     * @return The format string (never <code>null</code>).
     */
    public String getFormatString() {
        return this.formatString;
    }

    /**
     * Returns the number formatter for the x-values.
     *
     * @return The number formatter (possibly <code>null</code>).
     */
    public NumberFormat getXFormat() {
        return this.xFormat;
    }

    /**
     * Returns the date formatter for the x-values.
     *
     * @return The date formatter (possibly <code>null</code>).
     */
    public DateFormat getXDateFormat() {
        return this.xDateFormat;
    }

    /**
     * Returns the number formatter for the y-values.
     *
     * @return The number formatter (possibly <code>null</code>).
     */
    public NumberFormat getYFormat() {
        return this.yFormat;
    }

    /**
     * Returns the date formatter for the y-values.
     *
     * @return The date formatter (possibly <code>null</code>).
     */
    public DateFormat getYDateFormat() {
        return this.yDateFormat;
    }

    /**
     * Generates a label string for an item in the dataset.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The label (possibly <code>null</code>).
     */
    public String generateLabelString(XYDataset dataset, int series, int item) {
        String result = null;
        Object[] items = createItemArray(dataset, series, item);
        result = MessageFormat.format(this.formatString, items);
        return result;
    }

    /**
     * Returns the string representing a null value.
     *
     * @return The string representing a null value.
     *
     * @since 1.0.10
     */
    public String getNullYString() {
        return this.nullYString;
    }

    /**
     * Creates the array of items that can be passed to the
     * {@link MessageFormat} class for creating labels.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return An array of three items from the dataset formatted as
     *         <code>String</code> objects (never <code>null</code>).
     */
    protected Object[] createItemArray(XYDataset dataset, int series,
                                       int item) {
        Object[] result = new Object[3];
        result[0] = dataset.getSeriesKey(series).toString();

        double x = dataset.getXValue(series, item);
        if (this.xDateFormat != null) {
            result[1] = this.xDateFormat.format(new Date((long) x));
        }
        else {
            result[1] = this.xFormat.format(x);
        }

        double y = dataset.getYValue(series, item);
        if (Double.isNaN(y) && dataset.getY(series, item) == null) {
            result[2] = this.nullYString;
        }
        else {
            if (this.yDateFormat != null) {
                result[2] = this.yDateFormat.format(new Date((long) y));
            }
            else {
                result[2] = this.yFormat.format(y);
            }
        }
        return result;
    }

    /**
     * Tests this object for equality with an arbitrary object.
     *
     * @param obj  the other object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AbstractXYItemLabelGenerator)) {
            return false;
        }
        AbstractXYItemLabelGenerator that = (AbstractXYItemLabelGenerator) obj;
        if (!this.formatString.equals(that.formatString)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.xFormat, that.xFormat)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.xDateFormat, that.xDateFormat)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.yFormat, that.yFormat)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.yDateFormat, that.yDateFormat)) {
            return false;
        }
        if (!this.nullYString.equals(that.nullYString)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code.
     */
    public int hashCode() {
        int result = 127;
        result = HashUtilities.hashCode(result, this.formatString);
        result = HashUtilities.hashCode(result, this.xFormat);
        result = HashUtilities.hashCode(result, this.xDateFormat);
        result = HashUtilities.hashCode(result, this.yFormat);
        result = HashUtilities.hashCode(result, this.yDateFormat);
        return result;
    }

    /**
     * Returns an independent copy of the generator.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if cloning is not supported.
     */
    public Object clone() throws CloneNotSupportedException {
        AbstractXYItemLabelGenerator clone
                = (AbstractXYItemLabelGenerator) super.clone();
        if (this.xFormat != null) {
            clone.xFormat = (NumberFormat) this.xFormat.clone();
        }
        if (this.yFormat != null) {
            clone.yFormat = (NumberFormat) this.yFormat.clone();
        }
        if (this.xDateFormat != null) {
            clone.xDateFormat = (DateFormat) this.xDateFormat.clone();
        }
        if (this.yDateFormat != null) {
            clone.yDateFormat = (DateFormat) this.yDateFormat.clone();
        }
        return clone;
    }

}
