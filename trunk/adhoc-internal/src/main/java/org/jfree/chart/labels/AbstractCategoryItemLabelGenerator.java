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
 * ---------------------------------------
 * AbstractCategoryItemLabelGenerator.java
 * ---------------------------------------
 * (C) Copyright 2005-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 11-May-2004 : Version 1, distilled from StandardCategoryLabelGenerator (DG);
 * 31-Jan-2005 : Added methods to return row and column labels (DG);
 * 17-May-2005 : Added percentage to item array (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 03-May-2006 : Added new constructor (DG);
 * 23-Nov-2007 : Implemented hashCode() (DG);
 *
 */

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;

import org.jfree.chart.HashUtilities;
import org.jfree.data.DataUtilities;
import org.jfree.data.category.CategoryDataset;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A base class that can be used to create a label or tooltip generator that
 * can be assigned to a
 * {@link org.jfree.chart.renderer.category.CategoryItemRenderer}.
 */
public abstract class AbstractCategoryItemLabelGenerator
        implements PublicCloneable, Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -7108591260223293197L;

    /**
     * The label format string used by a <code>MessageFormat</code> object to
     * combine the standard items:  {0} = series name, {1} = category,
     * {2} = value, {3} = value as a percentage of the column total.
     */
    private String labelFormat;

    /** The string used to represent a null value. */
    private String nullValueString;

    /**
     * A number formatter used to preformat the value before it is passed to
     * the MessageFormat object.
     */
    private NumberFormat numberFormat;

    /**
     * A date formatter used to preformat the value before it is passed to the
     * MessageFormat object.
     */
    private DateFormat dateFormat;

    /**
     * A number formatter used to preformat the percentage value before it is
     * passed to the MessageFormat object.
     */
    private NumberFormat percentFormat;

    /**
     * Creates a label generator with the specified number formatter.
     *
     * @param labelFormat  the label format string (<code>null</code> not
     *                     permitted).
     * @param formatter  the number formatter (<code>null</code> not permitted).
     */
    protected AbstractCategoryItemLabelGenerator(String labelFormat,
                                                 NumberFormat formatter) {
        this(labelFormat, formatter, NumberFormat.getPercentInstance());
    }

    /**
     * Creates a label generator with the specified number formatter.
     *
     * @param labelFormat  the label format string (<code>null</code> not
     *                     permitted).
     * @param formatter  the number formatter (<code>null</code> not permitted).
     * @param percentFormatter  the percent formatter (<code>null</code> not
     *     permitted).
     *
     * @since 1.0.2
     */
    protected AbstractCategoryItemLabelGenerator(String labelFormat,
            NumberFormat formatter, NumberFormat percentFormatter) {
        if (labelFormat == null) {
            throw new IllegalArgumentException("Null 'labelFormat' argument.");
        }
        if (formatter == null) {
            throw new IllegalArgumentException("Null 'formatter' argument.");
        }
        if (percentFormatter == null) {
            throw new IllegalArgumentException(
                    "Null 'percentFormatter' argument.");
        }
        this.labelFormat = labelFormat;
        this.numberFormat = formatter;
        this.percentFormat = percentFormatter;
        this.dateFormat = null;
        this.nullValueString = "-";
    }

    /**
     * Creates a label generator with the specified date formatter.
     *
     * @param labelFormat  the label format string (<code>null</code> not
     *                     permitted).
     * @param formatter  the date formatter (<code>null</code> not permitted).
     */
    protected AbstractCategoryItemLabelGenerator(String labelFormat,
                                                 DateFormat formatter) {
        if (labelFormat == null) {
            throw new IllegalArgumentException("Null 'labelFormat' argument.");
        }
        if (formatter == null) {
            throw new IllegalArgumentException("Null 'formatter' argument.");
        }
        this.labelFormat = labelFormat;
        this.numberFormat = null;
        this.percentFormat = NumberFormat.getPercentInstance();
        this.dateFormat = formatter;
        this.nullValueString = "-";
    }

    /**
     * Generates a label for the specified row.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param row  the row index (zero-based).
     *
     * @return The label.
     */
    public String generateRowLabel(CategoryDataset dataset, int row) {
        return dataset.getRowKey(row).toString();
    }

    /**
     * Generates a label for the specified row.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param column  the column index (zero-based).
     *
     * @return The label.
     */
    public String generateColumnLabel(CategoryDataset dataset, int column) {
        return dataset.getColumnKey(column).toString();
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
     * @return The number formatter (possibly <code>null</code>).
     */
    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }

    /**
     * Returns the date formatter.
     *
     * @return The date formatter (possibly <code>null</code>).
     */
    public DateFormat getDateFormat() {
        return this.dateFormat;
    }

    /**
     * Generates a for the specified item.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The label (possibly <code>null</code>).
     */
    protected String generateLabelString(CategoryDataset dataset,
                                         int row, int column) {
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
        String result = null;
        Object[] items = createItemArray(dataset, row, column);
        result = MessageFormat.format(this.labelFormat, items);
        return result;

    }

    /**
     * Creates the array of items that can be passed to the
     * {@link MessageFormat} class for creating labels.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The items (never <code>null</code>).
     */
    protected Object[] createItemArray(CategoryDataset dataset,
                                       int row, int column) {
        Object[] result = new Object[4];
        result[0] = dataset.getRowKey(row).toString();
        result[1] = dataset.getColumnKey(column).toString();
        Number value = dataset.getValue(row, column);
        if (value != null) {
            if (this.numberFormat != null) {
                result[2] = this.numberFormat.format(value);
            }
            else if (this.dateFormat != null) {
                result[2] = this.dateFormat.format(value);
            }
        }
        else {
            result[2] = this.nullValueString;
        }
        if (value != null) {
            double total = DataUtilities.calculateColumnTotal(dataset, column);
            double percent = value.doubleValue() / total;
            result[3] = this.percentFormat.format(percent);
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
        if (!(obj instanceof AbstractCategoryItemLabelGenerator)) {
            return false;
        }

        AbstractCategoryItemLabelGenerator that
            = (AbstractCategoryItemLabelGenerator) obj;
        if (!this.labelFormat.equals(that.labelFormat)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.dateFormat, that.dateFormat)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.numberFormat, that.numberFormat)) {
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
        result = HashUtilities.hashCode(result, this.nullValueString);
        result = HashUtilities.hashCode(result, this.dateFormat);
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
        AbstractCategoryItemLabelGenerator clone
            = (AbstractCategoryItemLabelGenerator) super.clone();
        if (this.numberFormat != null) {
            clone.numberFormat = (NumberFormat) this.numberFormat.clone();
        }
        if (this.dateFormat != null) {
            clone.dateFormat = (DateFormat) this.dateFormat.clone();
        }
        return clone;
    }

}
