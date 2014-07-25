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
 * -----------------------
 * DefaultKeyedValues.java
 * -----------------------
 * (C) Copyright 2002-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Thomas Morgner;
 *
 * Changes:
 * --------
 * 31-Oct-2002 : Version 1 (DG);
 * 11-Feb-2003 : Fixed bug in getValue(key) method for unrecognised key (DG);
 * 05-Mar-2003 : Added methods to sort stored data 'by key' or 'by value' (DG);
 * 13-Mar-2003 : Implemented Serializable (DG);
 * 08-Apr-2003 : Modified removeValue(Comparable) method to fix bug 717049 (DG);
 * 18-Aug-2003 : Implemented Cloneable (DG);
 * 27-Aug-2003 : Moved SortOrder from org.jfree.data --> org.jfree.util (DG);
 * 09-Feb-2004 : Modified getIndex() method - see bug report 893256 (DG);
 * 15-Sep-2004 : Updated clone() method and added PublicCloneable
 *               interface (DG);
 * 25-Nov-2004 : Small update to the clone() implementation (DG);
 * 24-Feb-2005 : Added methods addValue(Comparable, double) and
 *               setValue(Comparable, double) for convenience (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 31-Jul-2006 : Added a clear() method (DG);
 * 01-Aug-2006 : Added argument check to getIndex() method (DG);
 * 30-Apr-2007 : Added insertValue() methods (DG);
 * 31-Oct-2007 : Performance improvements by using separate lists for keys and
 *               values (TM);
 * 21-Nov-2007 : Fixed bug in removeValue() method from previous patch (DG);
 *
 */

package org.jfree.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.jfree.util.PublicCloneable;
import org.jfree.util.SortOrder;

/**
 * An ordered list of (key, value) items.  This class provides a default
 * implementation of the {@link KeyedValues} interface.
 */
public class DefaultKeyedValues implements KeyedValues, Cloneable,
        PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 8468154364608194797L;

    /** Storage for the keys. */
    private ArrayList keys;

    /** Storage for the values. */
    private ArrayList values;

    /**
     * Contains (key, Integer) mappings, where the Integer is the index for
     * the key in the list.
     */
    private HashMap indexMap;

  /**
     * Creates a new collection (initially empty).
     */
    public DefaultKeyedValues() {
        this.keys = new ArrayList();
        this.values = new ArrayList();
        this.indexMap = new HashMap();
    }

    /**
     * Returns the number of items (values) in the collection.
     *
     * @return The item count.
     */
    public int getItemCount() {
        return this.indexMap.size();
    }

    /**
     * Returns a value.
     *
     * @param item  the item of interest (zero-based index).
     *
     * @return The value (possibly <code>null</code>).
     *
     * @throws IndexOutOfBoundsException if <code>item</code> is out of bounds.
     */
    public Number getValue(int item) {
        return (Number) this.values.get(item);
    }

    /**
     * Returns a key.
     *
     * @param index  the item index (zero-based).
     *
     * @return The row key.
     *
     * @throws IndexOutOfBoundsException if <code>item</code> is out of bounds.
     */
    public Comparable getKey(int index) {
        return (Comparable) this.keys.get(index);
    }

    /**
     * Returns the index for a given key.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The index, or <code>-1</code> if the key is not recognised.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     */
    public int getIndex(Comparable key) {
        if (key == null) {
            throw new IllegalArgumentException("Null 'key' argument.");
        }
        final Integer i = (Integer) this.indexMap.get(key);
        if (i == null) {
            return -1;  // key not found
        }
        return i.intValue();
    }

    /**
     * Returns the keys for the values in the collection.
     *
     * @return The keys (never <code>null</code>).
     */
    public List getKeys() {
        return (List) this.keys.clone();
    }

    /**
     * Returns the value for a given key.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The value (possibly <code>null</code>).
     *
     * @throws UnknownKeyException if the key is not recognised.
     *
     * @see #getValue(int)
     */
    public Number getValue(Comparable key) {
        int index = getIndex(key);
        if (index < 0) {
            throw new UnknownKeyException("Key not found: " + key);
        }
        return getValue(index);
    }

    /**
     * Updates an existing value, or adds a new value to the collection.
     *
     * @param key  the key (<code>null</code> not permitted).
     * @param value  the value.
     *
     * @see #addValue(Comparable, Number)
     */
    public void addValue(Comparable key, double value) {
        addValue(key, new Double(value));
    }

    /**
     * Adds a new value to the collection, or updates an existing value.
     * This method passes control directly to the
     * {@link #setValue(Comparable, Number)} method.
     *
     * @param key  the key (<code>null</code> not permitted).
     * @param value  the value (<code>null</code> permitted).
     */
    public void addValue(Comparable key, Number value) {
        setValue(key, value);
    }

    /**
     * Updates an existing value, or adds a new value to the collection.
     *
     * @param key  the key (<code>null</code> not permitted).
     * @param value  the value.
     */
    public void setValue(Comparable key, double value) {
        setValue(key, new Double(value));
    }

    /**
     * Updates an existing value, or adds a new value to the collection.
     *
     * @param key  the key (<code>null</code> not permitted).
     * @param value  the value (<code>null</code> permitted).
     */
    public void setValue(Comparable key, Number value) {
        if (key == null) {
            throw new IllegalArgumentException("Null 'key' argument.");
        }
        int keyIndex = getIndex(key);
        if (keyIndex >= 0) {
            this.keys.set(keyIndex, key);
            this.values.set(keyIndex, value);
        }
        else {
            this.keys.add(key);
            this.values.add(value);
            this.indexMap.put(key, new Integer(this.keys.size() - 1));
        }
    }

    /**
     * Inserts a new value at the specified position in the dataset or, if
     * there is an existing item with the specified key, updates the value
     * for that item and moves it to the specified position.
     *
     * @param position  the position (in the range 0 to getItemCount()).
     * @param key  the key (<code>null</code> not permitted).
     * @param value  the value.
     *
     * @since 1.0.6
     */
    public void insertValue(int position, Comparable key, double value) {
        insertValue(position, key, new Double(value));
    }

    /**
     * Inserts a new value at the specified position in the dataset or, if
     * there is an existing item with the specified key, updates the value
     * for that item and moves it to the specified position.
     *
     * @param position  the position (in the range 0 to getItemCount()).
     * @param key  the key (<code>null</code> not permitted).
     * @param value  the value (<code>null</code> permitted).
     *
     * @since 1.0.6
     */
    public void insertValue(int position, Comparable key, Number value) {
        if (position < 0 || position > getItemCount()) {
            throw new IllegalArgumentException("'position' out of bounds.");
        }
        if (key == null) {
            throw new IllegalArgumentException("Null 'key' argument.");
        }
        int pos = getIndex(key);
        if (pos == position) {
            this.keys.set(pos, key);
            this.values.set(pos, value);
        }
        else {
            if (pos >= 0) {
                this.keys.remove(pos);
                this.values.remove(pos);
            }

            this.keys.add(position, key);
            this.values.add(position, value);
            rebuildIndex();
        }
    }

    /**
     * Rebuilds the key to indexed-position mapping after an positioned insert
     * or a remove operation.
     */
    private void rebuildIndex () {
        this.indexMap.clear();
        for (int i = 0; i < this.keys.size(); i++) {
            final Object key = this.keys.get(i);
            this.indexMap.put(key, new Integer(i));
        }
    }

    /**
     * Removes a value from the collection.
     *
     * @param index  the index of the item to remove (in the range
     *     <code>0</code> to <code>getItemCount() - 1</code>).
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is not within
     *     the specified range.
     */
    public void removeValue(int index) {
        this.keys.remove(index);
        this.values.remove(index);
        rebuildIndex();
    }

    /**
     * Removes a value from the collection.
     *
     * @param key  the item key (<code>null</code> not permitted).
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     * @throws UnknownKeyException if <code>key</code> is not recognised.
     */
    public void removeValue(Comparable key) {
        int index = getIndex(key);
        if (index < 0) {
            throw new UnknownKeyException("The key (" + key
                    + ") is not recognised.");
        }
        removeValue(index);
    }

    /**
     * Clears all values from the collection.
     *
     * @since 1.0.2
     */
    public void clear() {
        this.keys.clear();
        this.values.clear();
        this.indexMap.clear();
    }

    /**
     * Sorts the items in the list by key.
     *
     * @param order  the sort order (<code>null</code> not permitted).
     */
    public void sortByKeys(SortOrder order) {
        final int size = this.keys.size();
        final DefaultKeyedValue[] data = new DefaultKeyedValue[size];

        for (int i = 0; i < size; i++) {
            data[i] = new DefaultKeyedValue((Comparable) this.keys.get(i),
                    (Number) this.values.get(i));
        }

        Comparator comparator = new KeyedValueComparator(
                KeyedValueComparatorType.BY_KEY, order);
        Arrays.sort(data, comparator);
        clear();

        for (int i = 0; i < data.length; i++) {
            final DefaultKeyedValue value = data[i];
            addValue(value.getKey(), value.getValue());
        }
    }

    /**
     * Sorts the items in the list by value.  If the list contains
     * <code>null</code> values, they will sort to the end of the list,
     * irrespective of the sort order.
     *
     * @param order  the sort order (<code>null</code> not permitted).
     */
    public void sortByValues(SortOrder order) {
        final int size = this.keys.size();
        final DefaultKeyedValue[] data = new DefaultKeyedValue[size];
        for (int i = 0; i < size; i++) {
            data[i] = new DefaultKeyedValue((Comparable) this.keys.get(i),
                    (Number) this.values.get(i));
        }

        Comparator comparator = new KeyedValueComparator(
                KeyedValueComparatorType.BY_VALUE, order);
        Arrays.sort(data, comparator);

        clear();
        for (int i = 0; i < data.length; i++) {
            final DefaultKeyedValue value = data[i];
            addValue(value.getKey(), value.getValue());
        }
    }

    /**
     * Tests if this object is equal to another.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof KeyedValues)) {
            return false;
        }

        KeyedValues that = (KeyedValues) obj;
        int count = getItemCount();
        if (count != that.getItemCount()) {
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
        return (this.keys != null ? this.keys.hashCode() : 0);
    }

    /**
     * Returns a clone.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  this class will not throw this
     *         exception, but subclasses might.
     */
    public Object clone() throws CloneNotSupportedException {
        DefaultKeyedValues clone = (DefaultKeyedValues) super.clone();
        clone.keys = (ArrayList) this.keys.clone();
        clone.values = (ArrayList) this.values.clone();
        clone.indexMap = (HashMap) this.indexMap.clone();
        return clone;
    }

}
