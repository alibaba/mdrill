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
 * ---------------------------
 * DefaultCategoryDataset.java
 * ---------------------------
 * (C) Copyright 2002-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 21-Jan-2003 : Added standard header, and renamed DefaultCategoryDataset (DG);
 * 13-Mar-2003 : Inserted DefaultKeyedValues2DDataset into class hierarchy (DG);
 * 06-Oct-2003 : Added incrementValue() method (DG);
 * 05-Apr-2004 : Added clear() method (DG);
 * 18-Aug-2004 : Moved from org.jfree.data --> org.jfree.data.category (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 26-Feb-2007 : Updated API docs (DG);
 * 08-Mar-2007 : Implemented clone() (DG);
 * 09-May-2008 : Implemented PublicCloneable (DG);
 *
 */

package org.jfree.data.category;

import java.io.Serializable;
import java.util.List;

import org.jfree.data.DefaultKeyedValues2D;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.util.PublicCloneable;

/**
 * A default implementation of the {@link CategoryDataset} interface.
 */
public class DefaultCategoryDataset extends AbstractDataset
        implements CategoryDataset, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -8168173757291644622L;

    /** A storage structure for the data. */
    private DefaultKeyedValues2D data;

    /**
     * Creates a new (empty) dataset.
     */
    public DefaultCategoryDataset() {
        this.data = new DefaultKeyedValues2D();
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return The row count.
     *
     * @see #getColumnCount()
     */
    public int getRowCount() {
        return this.data.getRowCount();
    }

    /**
     * Returns the number of columns in the table.
     *
     * @return The column count.
     *
     * @see #getRowCount()
     */
    public int getColumnCount() {
        return this.data.getColumnCount();
    }

    /**
     * Returns a value from the table.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The value (possibly <code>null</code>).
     *
     * @see #addValue(Number, Comparable, Comparable)
     * @see #removeValue(Comparable, Comparable)
     */
    public Number getValue(int row, int column) {
        return this.data.getValue(row, column);
    }

    /**
     * Returns the key for the specified row.
     *
     * @param row  the row index (zero-based).
     *
     * @return The row key.
     *
     * @see #getRowIndex(Comparable)
     * @see #getRowKeys()
     * @see #getColumnKey(int)
     */
    public Comparable getRowKey(int row) {
        return this.data.getRowKey(row);
    }

    /**
     * Returns the row index for a given key.
     *
     * @param key  the row key (<code>null</code> not permitted).
     *
     * @return The row index.
     *
     * @see #getRowKey(int)
     */
    public int getRowIndex(Comparable key) {
        // defer null argument check
        return this.data.getRowIndex(key);
    }

    /**
     * Returns the row keys.
     *
     * @return The keys.
     *
     * @see #getRowKey(int)
     */
    public List getRowKeys() {
        return this.data.getRowKeys();
    }

    /**
     * Returns a column key.
     *
     * @param column  the column index (zero-based).
     *
     * @return The column key.
     *
     * @see #getColumnIndex(Comparable)
     */
    public Comparable getColumnKey(int column) {
        return this.data.getColumnKey(column);
    }

    /**
     * Returns the column index for a given key.
     *
     * @param key  the column key (<code>null</code> not permitted).
     *
     * @return The column index.
     *
     * @see #getColumnKey(int)
     */
    public int getColumnIndex(Comparable key) {
        // defer null argument check
        return this.data.getColumnIndex(key);
    }

    /**
     * Returns the column keys.
     *
     * @return The keys.
     *
     * @see #getColumnKey(int)
     */
    public List getColumnKeys() {
        return this.data.getColumnKeys();
    }

    /**
     * Returns the value for a pair of keys.
     *
     * @param rowKey  the row key (<code>null</code> not permitted).
     * @param columnKey  the column key (<code>null</code> not permitted).
     *
     * @return The value (possibly <code>null</code>).
     *
     * @throws UnknownKeyException if either key is not defined in the dataset.
     *
     * @see #addValue(Number, Comparable, Comparable)
     */
    public Number getValue(Comparable rowKey, Comparable columnKey) {
        return this.data.getValue(rowKey, columnKey);
    }

    /**
     * Adds a value to the table.  Performs the same function as setValue().
     *
     * @param value  the value.
     * @param rowKey  the row key.
     * @param columnKey  the column key.
     *
     * @see #getValue(Comparable, Comparable)
     * @see #removeValue(Comparable, Comparable)
     */
    public void addValue(Number value, Comparable rowKey,
                         Comparable columnKey) {
        this.data.addValue(value, rowKey, columnKey);
        fireDatasetChanged();
    }

    /**
     * Adds a value to the table.
     *
     * @param value  the value.
     * @param rowKey  the row key.
     * @param columnKey  the column key.
     *
     * @see #getValue(Comparable, Comparable)
     */
    public void addValue(double value, Comparable rowKey,
                         Comparable columnKey) {
        addValue(new Double(value), rowKey, columnKey);
    }

    /**
     * Adds or updates a value in the table and sends a
     * {@link DatasetChangeEvent} to all registered listeners.
     *
     * @param value  the value (<code>null</code> permitted).
     * @param rowKey  the row key (<code>null</code> not permitted).
     * @param columnKey  the column key (<code>null</code> not permitted).
     *
     * @see #getValue(Comparable, Comparable)
     */
    public void setValue(Number value, Comparable rowKey,
                         Comparable columnKey) {
        this.data.setValue(value, rowKey, columnKey);
        fireDatasetChanged();
    }

    /**
     * Adds or updates a value in the table and sends a
     * {@link DatasetChangeEvent} to all registered listeners.
     *
     * @param value  the value.
     * @param rowKey  the row key (<code>null</code> not permitted).
     * @param columnKey  the column key (<code>null</code> not permitted).
     *
     * @see #getValue(Comparable, Comparable)
     */
    public void setValue(double value, Comparable rowKey,
                         Comparable columnKey) {
        setValue(new Double(value), rowKey, columnKey);
    }

    /**
     * Adds the specified value to an existing value in the dataset (if the
     * existing value is <code>null</code>, it is treated as if it were 0.0).
     *
     * @param value  the value.
     * @param rowKey  the row key (<code>null</code> not permitted).
     * @param columnKey  the column key (<code>null</code> not permitted).
     *
     * @throws UnknownKeyException if either key is not defined in the dataset.
     */
    public void incrementValue(double value,
                               Comparable rowKey,
                               Comparable columnKey) {
        double existing = 0.0;
        Number n = getValue(rowKey, columnKey);
        if (n != null) {
            existing = n.doubleValue();
        }
        setValue(existing + value, rowKey, columnKey);
    }

    /**
     * Removes a value from the dataset and sends a {@link DatasetChangeEvent}
     * to all registered listeners.
     *
     * @param rowKey  the row key.
     * @param columnKey  the column key.
     *
     * @see #addValue(Number, Comparable, Comparable)
     */
    public void removeValue(Comparable rowKey, Comparable columnKey) {
        this.data.removeValue(rowKey, columnKey);
        fireDatasetChanged();
    }

    /**
     * Removes a row from the dataset and sends a {@link DatasetChangeEvent}
     * to all registered listeners.
     *
     * @param rowIndex  the row index.
     *
     * @see #removeColumn(int)
     */
    public void removeRow(int rowIndex) {
        this.data.removeRow(rowIndex);
        fireDatasetChanged();
    }

    /**
     * Removes a row from the dataset and sends a {@link DatasetChangeEvent}
     * to all registered listeners.
     *
     * @param rowKey  the row key.
     *
     * @see #removeColumn(Comparable)
     */
    public void removeRow(Comparable rowKey) {
        this.data.removeRow(rowKey);
        fireDatasetChanged();
    }

    /**
     * Removes a column from the dataset and sends a {@link DatasetChangeEvent}
     * to all registered listeners.
     *
     * @param columnIndex  the column index.
     *
     * @see #removeRow(int)
     */
    public void removeColumn(int columnIndex) {
        this.data.removeColumn(columnIndex);
        fireDatasetChanged();
    }

    /**
     * Removes a column from the dataset and sends a {@link DatasetChangeEvent}
     * to all registered listeners.
     *
     * @param columnKey  the column key (<code>null</code> not permitted).
     *
     * @see #removeRow(Comparable)
     *
     * @throws UnknownKeyException if <code>columnKey</code> is not defined
     *         in the dataset.
     */
    public void removeColumn(Comparable columnKey) {
        this.data.removeColumn(columnKey);
        fireDatasetChanged();
    }

    /**
     * Clears all data from the dataset and sends a {@link DatasetChangeEvent}
     * to all registered listeners.
     */
    public void clear() {
        this.data.clear();
        fireDatasetChanged();
    }

    /**
     * Tests this dataset for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CategoryDataset)) {
            return false;
        }
        CategoryDataset that = (CategoryDataset) obj;
        if (!getRowKeys().equals(that.getRowKeys())) {
            return false;
        }
        if (!getColumnKeys().equals(that.getColumnKeys())) {
            return false;
        }
        int rowCount = getRowCount();
        int colCount = getColumnCount();
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                Number v1 = getValue(r, c);
                Number v2 = that.getValue(r, c);
                if (v1 == null) {
                    if (v2 != null) {
                        return false;
                    }
                }
                else if (!v1.equals(v2)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a hash code for the dataset.
     *
     * @return A hash code.
     */
    public int hashCode() {
        return this.data.hashCode();
    }

    /**
     * Returns a clone of the dataset.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if there is a problem cloning the
     *         dataset.
     */
    public Object clone() throws CloneNotSupportedException {
        DefaultCategoryDataset clone = (DefaultCategoryDataset) super.clone();
        clone.data = (DefaultKeyedValues2D) this.data.clone();
        return clone;
    }

}
