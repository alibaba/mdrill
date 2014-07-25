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
 * ----------------------------------
 * AbstractPieItemLabelGenerator.java
 * ----------------------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 09-Nov-2004 : Version 1, draws out code from StandardPieItemLabelGenerator
 *               and StandardPieToolTipGenerator (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 03-May-2006 : Fixed bug 1480978, a problem in the clone() method (DG);
 * 23-Nov-2007 : Implemented hashCode() (DG);
 *
 */

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.NumberFormat;

import org.jfree.chart.HashUtilities;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.PieDataset;

/**
 * A base class used for generating pie chart item labels.
 */
public class AbstractPieItemLabelGenerator implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 7347703325267846275L;

    /** The label format string. */
    private String labelFormat;

    /** A number formatter for the value. */
    private NumberFormat numberFormat;

    /** A number formatter for the percentage. */
    private NumberFormat percentFormat;

    /**
     * Creates an item label generator using the specified number formatters.
     *
     * @param labelFormat  the label format string (<code>null</code> not
     *                     permitted).
     * @param numberFormat  the format object for the values (<code>null</code>
     *                      not permitted).
     * @param percentFormat  the format object for the percentages
     *                       (<code>null</code> not permitted).
     */
    protected AbstractPieItemLabelGenerator(String labelFormat,
                                            NumberFormat numberFormat,
                                            NumberFormat percentFormat) {

        if (labelFormat == null) {
            throw new IllegalArgumentException("Null 'labelFormat' argument.");
        }
        if (numberFormat == null) {
            throw new IllegalArgumentException("Null 'numberFormat' argument.");
        }
        if (percentFormat == null) {
            throw new IllegalArgumentException(
                    "Null 'percentFormat' argument.");
        }
        this.labelFormat = labelFormat;
        this.numberFormat = numberFormat;
        this.percentFormat = percentFormat;

    }

    /**
     * Returns the label format string.
     *
     * @return The label format string (never <code>null</code>).
     */
    public String getLabelFormat() {
        return this.labelFormat;
    }

    /**
     * Returns the number formatter.
     *
     * @return The formatter (never <code>null</code>).
     */
    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }

    /**
     * Returns the percent formatter.
     *
     * @return The formatter (never <code>null</code>).
     */
    public NumberFormat getPercentFormat() {
        return this.percentFormat;
    }

    /**
     * Creates the array of items that can be passed to the
     * {@link MessageFormat} class for creating labels.  The returned array
     * contains four values:
     * <ul>
     * <li>result[0] = the section key converted to a <code>String</code>;</li>
     * <li>result[1] = the formatted data value;</li>
     * <li>result[2] = the formatted percentage (of the total);</li>
     * <li>result[3] = the formatted total value.</li>
     * </ul>
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The items (never <code>null</code>).
     */
    protected Object[] createItemArray(PieDataset dataset, Comparable key) {
        Object[] result = new Object[4];
        double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
        result[0] = key.toString();
        Number value = dataset.getValue(key);
        if (value != null) {
            result[1] = this.numberFormat.format(value);
        }
        else {
            result[1] = "null";
        }
        double percent = 0.0;
        if (value != null) {
            double v = value.doubleValue();
            if (v > 0.0) {
                percent = v / total;
            }
        }
        result[2] = this.percentFormat.format(percent);
        result[3] = this.numberFormat.format(total);
        return result;
    }

    /**
     * Generates a label for a pie section.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param key  the section key (<code>null</code> not permitted).
     *
     * @return The label (possibly <code>null</code>).
     */
    protected String generateSectionLabel(PieDataset dataset, Comparable key) {
        String result = null;
        if (dataset != null) {
            Object[] items = createItemArray(dataset, key);
            result = MessageFormat.format(this.labelFormat, items);
        }
        return result;
    }

    /**
     * Tests the generator for equality with an arbitrary object.
     *
     * @param obj  the object to test against (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AbstractPieItemLabelGenerator)) {
            return false;
        }

        AbstractPieItemLabelGenerator that
                = (AbstractPieItemLabelGenerator) obj;
        if (!this.labelFormat.equals(that.labelFormat)) {
            return false;
        }
        if (!this.numberFormat.equals(that.numberFormat)) {
            return false;
        }
        if (!this.percentFormat.equals(that.percentFormat)) {
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
        result = HashUtilities.hashCode(result, this.labelFormat);
        result = HashUtilities.hashCode(result, this.numberFormat);
        result = HashUtilities.hashCode(result, this.percentFormat);
        return result;
    }

    /**
     * Returns an independent copy of the generator.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  should not happen.
     */
    public Object clone() throws CloneNotSupportedException {
        AbstractPieItemLabelGenerator clone
                = (AbstractPieItemLabelGenerator) super.clone();
        if (this.numberFormat != null) {
            clone.numberFormat = (NumberFormat) this.numberFormat.clone();
        }
        if (this.percentFormat != null) {
            clone.percentFormat = (NumberFormat) this.percentFormat.clone();
        }
        return clone;
    }

}
