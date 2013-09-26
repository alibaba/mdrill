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
 * ------------------
 * KeyedObject2D.java
 * ------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 05-Feb-2003 : Version 1 (DG);
 * 01-Mar-2004 : Added equals() and clone() methods and implemented
 *               Serializable (DG);
 * 03-Oct-2007 : Updated getObject() to handle modified behaviour in
 *               KeyedObjects class, added clear() method (DG);
 *
 */

package org.jfree.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A data structure that stores zero, one or many objects, where each object is
 * associated with two keys (a 'row' key and a 'column' key).
 */
public class KeyedObjects2D implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -1015873563138522374L;

    /** The row keys. */
    private List rowKeys;

    /** The column keys. */
    private List columnKeys;

    /** The row data. */
    private List rows;

    /**
     * Creates a new instance (initially empty).
     */
    public KeyedObjects2D() {
        this.rowKeys = new java.util.ArrayList();
        this.columnKeys = new java.util.ArrayList();
        this.rows = new java.util.ArrayList();
    }

    /**
     * Returns the row count.
     *
     * @return The row count.
     *
     * @see #getColumnCount()
     */
    public int getRowCount() {
        return this.rowKeys.size();
    }

    /**
     * Returns the column count.
     *
     * @return The column count.
     *
     * @see #getRowCount()
     */
    public int getColumnCount() {
        return this.columnKeys.size();
    }

    /**
     * Returns the object for a given row and column.
     *
     * @param row  the row index (in the range 0 to getRowCount() - 1).
     * @param column  the column index (in the range 0 to getColumnCount() - 1).
     *
     * @return The object (possibly <code>null</code>).
     *
     * @see #getObject(Comparable, Comparable)
     */
    public Object getObject(int row, int column) {
        Object result = null;
        KeyedObjects rowData = (KeyedObjects) this.rows.get(row);
        if (rowData != null) {
            Comparable columnKey = (Comparable) this.columnKeys.get(column);
            if (columnKey != null) {
                int index = rowData.getIndex(columnKey);
                if (index >= 0) {
                    result = rowData.getObject(columnKey);
                }
            }
        }
        return result;
    }

    /**
     * Returns the key for a given row.
     *
     * @param row  the row index (zero based).
     *
     * @return The row index.
     *
     * @see #getRowIndex(Comparable)
     */
    public Comparable getRowKey(int row) {
        return (Comparable) this.rowKeys.get(row);
    }

    /**
     * Returns the row index for a given key, or <code>-1</code> if the key
     * is not recognised.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The row index.
     *
     * @see #getRowKey(int)
     */
    public int getRowIndex(Comparable key) {
        if (key == null) {
            throw new IllegalArgumentException("Null 'key' argument.");
        }
        return this.rowKeys.indexOf(key);
    }

    /**
     * Returns the row keys.
     *
     * @return The row keys (never <code>null</code>).
     *
     * @see #getRowKeys()
     */
    public List getRowKeys() {
        return Collections.unmodifiableList(this.rowKeys);
    }

    /**
     * Returns the key for a given column.
     *
     * @param column  the column.
     *
     * @return The key.
     *
     * @see #getColumnIndex(Comparable)
     */
    public Comparable getColumnKey(int column) {
        return (Comparable) this.columnKeys.get(column);
    }

    /**
     * Returns the column index for a given key, or <code>-1</code> if the key
     * is not recognised.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The column index.
     *
     * @see #getColumnKey(int)
     */
    public int getColumnIndex(Comparable key) {
        if (key == null) {
            throw new IllegalArgumentException("Null 'key' argument.");
        }
        return this.columnKeys.indexOf(key);
    }

    /**
     * Returns the column keys.
     *
     * @return The column keys (never <code>null</code>).
     *
     * @see #getRowKeys()
     */
    public List getColumnKeys() {
        return Collections.unmodifiableList(this.columnKeys);
    }

    /**
     * Returns the object for the given row and column keys.
     *
     * @param rowKey  the row key (<code>null</code> not permitted).
     * @param columnKey  the column key (<code>null</code> not permitted).
     *
     * @return The object (possibly <code>null</code>).
     *
     * @throws IllegalArgumentException if <code>rowKey<code> or
     *         <code>columnKey</code> is <code>null</code>.
     * @throws UnknownKeyException if <code>rowKey</code> or
     *         <code>columnKey</code> is not recognised.
     */
    public Object getObject(Comparable rowKey, Comparable columnKey) {
        if (rowKey == null) {
            throw new IllegalArgumentException("Null 'rowKey' argument.");
        }
        if (columnKey == null) {
            throw new IllegalArgumentException("Null 'columnKey' argument.");
        }
        int row = this.rowKeys.indexOf(rowKey);
        if (row < 0) {
            throw new UnknownKeyException("Row key (" + rowKey
                    + ") not recognised.");
        }
        int column = this.columnKeys.indexOf(columnKey);
        if (column < 0) {
            throw new UnknownKeyException("Column key (" + columnKey
                    + ") not recognised.");
        }
        KeyedObjects rowData = (KeyedObjects) this.rows.get(row);
        int index = rowData.getIndex(columnKey);
        if (index >= 0) {
            return rowData.getObject(index);
        }
        else {
            return null;
        }
    }

    /**
     * Adds an object to the table.  Performs the same function as setObject().
     *
     * @param object  the object.
     * @param rowKey  the row key (<code>null</code> not permitted).
     * @param columnKey  the column key (<code>null</code> not permitted).
     */
    public void addObject(Object object, Comparable rowKey,
            Comparable columnKey) {
        setObject(object, rowKey, columnKey);
    }

    /**
     * Adds or updates an object.
     *
     * @param object  the object.
     * @param rowKey  the row key (<code>null</code> not permitted).
     * @param columnKey  the column key (<code>null</code> not permitted).
     */
    public void setObject(Object object, Comparable rowKey,
            Comparable columnKey) {

        if (rowKey == null) {
            throw new IllegalArgumentException("Null 'rowKey' argument.");
        }
        if (columnKey == null) {
            throw new IllegalArgumentException("Null 'columnKey' argument.");
        }
        KeyedObjects row;
        int rowIndex = this.rowKeys.indexOf(rowKey);
        if (rowIndex >= 0) {
            row = (KeyedObjects) this.rows.get(rowIndex);
        }
        else {
            this.rowKeys.add(rowKey);
            row = new KeyedObjects();
            this.rows.add(row);
        }
        row.setObject(columnKey, object);
        int columnIndex = this.columnKeys.indexOf(columnKey);
        if (columnIndex < 0) {
            this.columnKeys.add(columnKey);
        }

    }

    /**
     * Removes an object from the table by setting it to <code>null</code>.  If
     * all the objects in the specified row and/or column are now
     * <code>null</code>, the row and/or column is removed from the table.
     *
     * @param rowKey  the row key (<code>null</code> not permitted).
     * @param columnKey  the column key (<code>null</code> not permitted).
     *
     * @see #addObject(Object, Comparable, Comparable)
     */
    public void removeObject(Comparable rowKey, Comparable columnKey) {
        int rowIndex = getRowIndex(rowKey);
        if (rowIndex < 0) {
            throw new UnknownKeyException("Row key (" + rowKey
                    + ") not recognised.");
        }
        int columnIndex = getColumnIndex(columnKey);
        if (columnIndex < 0) {
            throw new UnknownKeyException("Column key (" + columnKey
                    + ") not recognised.");
        }
        setObject(null, rowKey, columnKey);

        // 1. check whether the row is now empty.
        boolean allNull = true;
        KeyedObjects row = (KeyedObjects) this.rows.get(rowIndex);

        for (int item = 0, itemCount = row.getItemCount(); item < itemCount;
             item++) {
            if (row.getObject(item) != null) {
                allNull = false;
                break;
            }
        }

        if (allNull) {
            this.rowKeys.remove(rowIndex);
            this.rows.remove(rowIndex);
        }

        // 2. check whether the column is now empty.
        allNull = true;

        for (int item = 0, itemCount = this.rows.size(); item < itemCount;
             item++) {
            row = (KeyedObjects) this.rows.get(item);
            int colIndex = row.getIndex(columnKey);
            if (colIndex >= 0 && row.getObject(colIndex) != null) {
                allNull = false;
                break;
            }
        }

        if (allNull) {
            for (int item = 0, itemCount = this.rows.size(); item < itemCount;
                 item++) {
                row = (KeyedObjects) this.rows.get(item);
                int colIndex = row.getIndex(columnKey);
                if (colIndex >= 0) {
                    row.removeValue(colIndex);
                }
            }
            this.columnKeys.remove(columnKey);
        }
    }

    /**
     * Removes an entire row from the table.
     *
     * @param rowIndex  the row index.
     *
     * @see #removeColumn(int)
     */
    public void removeRow(int rowIndex) {
        this.rowKeys.remove(rowIndex);
        this.rows.remove(rowIndex);
    }

    /**
     * Removes an entire row from the table.
     *
     * @param rowKey  the row key (<code>null</code> not permitted).
     *
     * @throws UnknownKeyException if <code>rowKey</code> is not recognised.
     *
     * @see #removeColumn(Comparable)
     */
    public void removeRow(Comparable rowKey) {
        int index = getRowIndex(rowKey);
        if (index < 0) {
            throw new UnknownKeyException("Row key (" + rowKey
                    + ") not recognised.");
        }
        removeRow(index);
    }

    /**
     * Removes an entire column from the table.
     *
     * @param columnIndex  the column index.
     *
     * @see #removeRow(int)
     */
    public void removeColumn(int columnIndex) {
        Comparable columnKey = getColumnKey(columnIndex);
        removeColumn(columnKey);
    }

    /**
     * Removes an entire column from the table.
     *
     * @param columnKey  the column key (<code>null</code> not permitted).
     *
     * @throws UnknownKeyException if <code>rowKey</code> is not recognised.
     *
     * @see #removeRow(Comparable)
     */
    public void removeColumn(Comparable columnKey) {
        int index = getColumnIndex(columnKey);
        if (index < 0) {
            throw new UnknownKeyException("Column key (" + columnKey
                    + ") not recognised.");
        }
        Iterator iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            KeyedObjects rowData = (KeyedObjects) iterator.next();
            int i = rowData.getIndex(columnKey);
            if (i >= 0) {
                rowData.removeValue(i);
            }
        }
        this.columnKeys.remove(columnKey);
    }

    /**
     * Clears all the data and associated keys.
     *
     * @since 1.0.7
     */
    public void clear() {
        this.rowKeys.clear();
        this.columnKeys.clear();
        this.rows.clear();
    }

    /**
     * Tests this object for equality with an arbitrary object.
     *
     * @param obj  the object to test (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof KeyedObjects2D)) {
            return false;
        }

        KeyedObjects2D that = (KeyedObjects2D) obj;
        if (!getRowKeys().equals(that.getRowKeys())) {
            return false;
        }
        if (!getColumnKeys().equals(that.getColumnKeys())) {
            return false;
        }
        int rowCount = getRowCount();
        if (rowCount != that.getRowCount()) {
            return false;
        }
        int colCount = getColumnCount();
        if (colCount != that.getColumnCount()) {
            return false;
        }
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                Object v1 = getObject(r, c);
                Object v2 = that.getObject(r, c);
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
        }
        return true;
    }

    /**
     * Returns a hashcode for this object.
     *
     * @return A hashcode.
     */
    public int hashCode() {
        int result;
        result = this.rowKeys.hashCode();
        result = 29 * result + this.columnKeys.hashCode();
        result = 29 * result + this.rows.hashCode();
        return result;
    }

    /**
     * Returns a clone.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  this class will not throw this
     *         exception, but subclasses (if any) might.
     */
    public Object clone() throws CloneNotSupportedException {
        KeyedObjects2D clone = (KeyedObjects2D) super.clone();
        clone.columnKeys = new java.util.ArrayList(this.columnKeys);
        clone.rowKeys = new java.util.ArrayList(this.rowKeys);
        clone.rows = new java.util.ArrayList(this.rows.size());
        Iterator iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            KeyedObjects row = (KeyedObjects) iterator.next();
            clone.rows.add(row.clone());
        }
        return clone;
    }

}
