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
 * DefaultPieDataset.java
 * ----------------------
 * (C) Copyright 2001-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Sam (oldman);
 *
 * Changes
 * -------
 * 17-Nov-2001 : Version 1 (DG);
 * 22-Jan-2002 : Removed legend methods from dataset implementations (DG);
 * 07-Apr-2002 : Modified implementation to guarantee data sequence to remain
 *               in the order categories are added (oldman);
 * 23-Oct-2002 : Added getCategory(int) method and getItemCount() method, in
 *               line with changes to the PieDataset interface (DG);
 * 04-Feb-2003 : Changed underlying data storage to DefaultKeyedValues (DG);
 * 04-Mar-2003 : Inserted DefaultKeyedValuesDataset class into hierarchy (DG);
 * 24-Apr-2003 : Switched places with DefaultKeyedValuesDataset (DG);
 * 18-Aug-2003 : Implemented Cloneable (DG);
 * 03-Mar-2005 : Implemented PublicCloneable (DG);
 * 29-Jun-2005 : Added remove() method (DG);
 * ------------- JFREECHART 1.0.0 ---------------------------------------------
 * 31-Jul-2006 : Added a clear() method to clear all values from the
 *               dataset (DG);
 * 28-Sep-2006 : Added sortByKeys() and sortByValues() methods (DG);
 * 30-Apr-2007 : Added new insertValues() methods (DG);
 *
 */

package org.jfree.data.general;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.UnknownKeyException;
import org.jfree.util.PublicCloneable;
import org.jfree.util.SortOrder;

/**
 * A default implementation of the {@link PieDataset} interface.
 */
public class DefaultPieDataset extends AbstractDataset
        implements PieDataset, Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 2904745139106540618L;

    /** Storage for the data. */
    private DefaultKeyedValues data;

    /**
     * Constructs a new dataset, initially empty.
     */
    public DefaultPieDataset() {
        this.data = new DefaultKeyedValues();
    }

    /**
     * Creates a new dataset by copying data from a {@link KeyedValues}
     * instance.
     *
     * @param data  the data (<code>null</code> not permitted).
     */
    public DefaultPieDataset(KeyedValues data) {
        if (data == null) {
            throw new IllegalArgumentException("Null 'data' argument.");
        }
        this.data = new DefaultKeyedValues();
        for (int i = 0; i < data.getItemCount(); i++) {
            this.data.addValue(data.getKey(i), data.getValue(i));
        }
    }

    /**
     * Returns the number of items in the dataset.
     *
     * @return The item count.
     */
    public int getItemCount() {
        return this.data.getItemCount();
    }

    /**
     * Returns the categories in the dataset.  The returned list is
     * unmodifiable.
     *
     * @return The categories in the dataset.
     */
    public List getKeys() {
        return Collections.unmodifiableList(this.data.getKeys());
    }

    /**
     * Returns the key for the specified item, or <code>null</code>.
     *
     * @param item  the item index (in the range <code>0</code> to
     *     <code>getItemCount() - 1</code>).
     *
     * @return The key, or <code>null</code>.
     *
     * @throws IndexOutOfBoundsException if <code>item</code> is not in the
     *     specified range.
     */
    public Comparable getKey(int item) {
        return this.data.getKey(item);
    }

    /**
     * Returns the index for a key, or -1 if the key is not recognised.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The index, or <code>-1</code> if the key is unrecognised.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     */
    public int getIndex(Comparable key) {
        return this.data.getIndex(key);
    }

    /**
     * Returns a value.
     *
     * @param item  the value index.
     *
     * @return The value (possibly <code>null</code>).
     */
    public Number getValue(int item) {

        Number result = null;
        if (getItemCount() > item) {
            result = this.data.getValue(item);
        }
        return result;

    }

    /**
     * Returns the data value associated with a key.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The value (possibly <code>null</code>).
     *
     * @throws UnknownKeyException if the key is not recognised.
     */
    public Number getValue(Comparable key) {
        if (key == null) {
            throw new IllegalArgumentException("Null 'key' argument.");
        }
        return this.data.getValue(key);
    }

    /**
     * Sets the data value for a key and sends a {@link DatasetChangeEvent} to
     * all registered listeners.
     *
     * @param key  the key (<code>null</code> not permitted).
     * @param value  the value.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     */
    public void setValue(Comparable key, Number value) {
        this.data.setValue(key, value);
        fireDatasetChanged();
    }

    /**
     * Sets the data value for a key and sends a {@link DatasetChangeEvent} to
     * all registered listeners.
     *
     * @param key  the key (<code>null</code> not permitted).
     * @param value  the value.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     */
    public void setValue(Comparable key, double value) {
        setValue(key, new Double(value));
    }

    /**
     * Inserts a new value at the specified position in the dataset or, if
     * there is an existing item with the specified key, updates the value
     * for that item and moves it to the specified position.  After the change
     * is made, this methods sends a {@link DatasetChangeEvent} to all
     * registered listeners.
     *
     * @param position  the position (in the range 0 to getItemCount()).
     * @param key  the key (<code>null</code> not permitted).
     * @param value  the value (<code>null</code> permitted).
     *
     * @since 1.0.6
     */
    public void insertValue(int position, Comparable key, double value) {
        insertValue(position, key, new Double(value));
    }

    /**
     * Inserts a new value at the specified position in the dataset or, if
     * there is an existing item with the specified key, updates the value
     * for that item and moves it to the specified position.  After the change
     * is made, this methods sends a {@link DatasetChangeEvent} to all
     * registered listeners.
     *
     * @param position  the position (in the range 0 to getItemCount()).
     * @param key  the key (<code>null</code> not permitted).
     * @param value  the value (<code>null</code> permitted).
     *
     * @since 1.0.6
     */
    public void insertValue(int position, Comparable key, Number value) {
        this.data.insertValue(position, key, value);
        fireDatasetChanged();
    }

    /**
     * Removes an item from the dataset and sends a {@link DatasetChangeEvent}
     * to all registered listeners.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     */
    public void remove(Comparable key) {
        this.data.removeValue(key);
        fireDatasetChanged();
    }

    /**
     * Clears all data from this dataset and sends a {@link DatasetChangeEvent}
     * to all registered listeners (unless the dataset was already empty).
     *
     * @since 1.0.2
     */
    public void clear() {
        if (getItemCount() > 0) {
            this.data.clear();
            fireDatasetChanged();
        }
    }

    /**
     * Sorts the dataset's items by key and sends a {@link DatasetChangeEvent}
     * to all registered listeners.
     *
     * @param order  the sort order (<code>null</code> not permitted).
     *
     * @since 1.0.3
     */
    public void sortByKeys(SortOrder order) {
        this.data.sortByKeys(order);
        fireDatasetChanged();
    }

    /**
     * Sorts the dataset's items by value and sends a {@link DatasetChangeEvent}
     * to all registered listeners.
     *
     * @param order  the sort order (<code>null</code> not permitted).
     *
     * @since 1.0.3
     */
    public void sortByValues(SortOrder order) {
        this.data.sortByValues(order);
        fireDatasetChanged();
    }

    /**
     * Tests if this object is equal to another.
     *
     * @param obj  the other object.
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof PieDataset)) {
            return false;
        }
        PieDataset that = (PieDataset) obj;
        int count = getItemCount();
        if (that.getItemCount() != count) {
            return false;
        }

        for (int i = 0; i < count; i++) {
            Comparable k1 = getKey(i);
            Comparable k2 = that.getKey(i);
            if (!k1.equals(k2)) {
                return false;
            }

            Number v1 = getValue(i);
            Number v2 = that.getValue(i);
            if (v1 == null) {
                if (v2 != null) {
                    return false;
                }
            }
            else {
                if (!v1.equals(v2)) {
                    return false;
                }
            }
        }
        return true;

    }

    /**
     * Returns a hash code.
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
     * @throws CloneNotSupportedException This class will not throw this
     *         exception, but subclasses (if any) might.
     */
    public Object clone() throws CloneNotSupportedException {
        DefaultPieDataset clone = (DefaultPieDataset) super.clone();
        clone.data = (DefaultKeyedValues) this.data.clone();
        return clone;
    }

}
