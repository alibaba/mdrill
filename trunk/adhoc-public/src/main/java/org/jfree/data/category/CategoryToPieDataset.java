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
 * -------------------------
 * CategoryToPieDataset.java
 * -------------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Christian W. Zuckschwerdt;
 *
 * Changes
 * -------
 * 23-Jan-2003 : Version 1 (DG);
 * 30-Jul-2003 : Pass through DatasetChangeEvent (CZ);
 * 29-Jan-2004 : Replaced 'extract' int with TableOrder (DG);
 * 11-Jan-2005 : Removed deprecated code in preparation for the 1.0.0
 *               release (DG);
 * ------------- JFREECHART 1.0.0 RELEASED ------------------------------------
 * 26-Jul-2006 : Added serialVersionUID, changed constructor to allow null
 *               for source, and added getSource(), getExtractType() and
 *               getExtractIndex() methods - see feature request 1477915 (DG);
 *
 */

package org.jfree.data.category;

import java.util.Collections;
import java.util.List;

import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.PieDataset;
import org.jfree.util.TableOrder;

/**
 * A {@link PieDataset} implementation that obtains its data from one row or
 * column of a {@link CategoryDataset}.
 */
public class CategoryToPieDataset extends AbstractDataset
        implements PieDataset, DatasetChangeListener {

    /** For serialization. */
    static final long serialVersionUID = 5516396319762189617L;

    /** The source. */
    private CategoryDataset source;

    /** The extract type. */
    private TableOrder extract;

    /** The row or column index. */
    private int index;

    /**
     * An adaptor class that converts any {@link CategoryDataset} into a
     * {@link PieDataset}, by taking the values from a single row or column.
     * <p>
     * If <code>source</code> is <code>null</code>, the created dataset will
     * be empty.
     *
     * @param source  the source dataset (<code>null</code> permitted).
     * @param extract  extract data from rows or columns? (<code>null</code>
     *                 not permitted).
     * @param index  the row or column index.
     */
    public CategoryToPieDataset(CategoryDataset source,
                                TableOrder extract,
                                int index) {
        if (extract == null) {
            throw new IllegalArgumentException("Null 'extract' argument.");
        }
        this.source = source;
        if (this.source != null) {
            this.source.addChangeListener(this);
        }
        this.extract = extract;
        this.index = index;
    }

    /**
     * Returns the underlying dataset.
     *
     * @return The underlying dataset (possibly <code>null</code>).
     *
     * @since 1.0.2
     */
    public CategoryDataset getUnderlyingDataset() {
        return this.source;
    }

    /**
     * Returns the extract type, which determines whether data is read from
     * one row or one column of the underlying dataset.
     *
     * @return The extract type.
     *
     * @since 1.0.2
     */
    public TableOrder getExtractType() {
        return this.extract;
    }

    /**
     * Returns the index of the row or column from which to extract the data.
     *
     * @return The extract index.
     *
     * @since 1.0.2
     */
    public int getExtractIndex() {
        return this.index;
    }

    /**
     * Returns the number of items (values) in the collection.  If the
     * underlying dataset is <code>null</code>, this method returns zero.
     *
     * @return The item count.
     */
    public int getItemCount() {
        int result = 0;
        if (this.source != null) {
            if (this.extract == TableOrder.BY_ROW) {
                result = this.source.getColumnCount();
            }
            else if (this.extract == TableOrder.BY_COLUMN) {
                result = this.source.getRowCount();
            }
        }
        return result;
    }

    /**
     * Returns a value from the dataset.
     *
     * @param item  the item index (zero-based).
     *
     * @return The value (possibly <code>null</code>).
     *
     * @throws IndexOutOfBoundsException if <code>item</code> is not in the
     *     range <code>0</code> to <code>getItemCount() - 1</code>.
     */
    public Number getValue(int item) {
        Number result = null;
        if (item < 0 || item >= getItemCount()) {
            // this will include the case where the underlying dataset is null
            throw new IndexOutOfBoundsException(
                    "The 'item' index is out of bounds.");
        }
        if (this.extract == TableOrder.BY_ROW) {
            result = this.source.getValue(this.index, item);
        }
        else if (this.extract == TableOrder.BY_COLUMN) {
            result = this.source.getValue(item, this.index);
        }
        return result;
    }

    /**
     * Returns the key at the specified index.
     *
     * @param index  the item index (in the range <code>0</code> to
     *     <code>getItemCount() - 1</code>).
     *
     * @return The key.
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is not in the
     *     specified range.
     */
    public Comparable getKey(int index) {
        Comparable result = null;
        if (index < 0 || index >= getItemCount()) {
            // this includes the case where the underlying dataset is null
            throw new IndexOutOfBoundsException("Invalid 'index': " + index);
        }
        if (this.extract == TableOrder.BY_ROW) {
            result = this.source.getColumnKey(index);
        }
        else if (this.extract == TableOrder.BY_COLUMN) {
            result = this.source.getRowKey(index);
        }
        return result;
    }

    /**
     * Returns the index for a given key, or <code>-1</code> if there is no
     * such key.
     *
     * @param key  the key.
     *
     * @return The index for the key, or <code>-1</code>.
     */
    public int getIndex(Comparable key) {
        int result = -1;
        if (this.source != null) {
            if (this.extract == TableOrder.BY_ROW) {
                result = this.source.getColumnIndex(key);
            }
            else if (this.extract == TableOrder.BY_COLUMN) {
                result = this.source.getRowIndex(key);
            }
        }
        return result;
    }

    /**
     * Returns the keys for the dataset.
     * <p>
     * If the underlying dataset is <code>null</code>, this method returns an
     * empty list.
     *
     * @return The keys.
     */
    public List getKeys() {
        List result = Collections.EMPTY_LIST;
        if (this.source != null) {
            if (this.extract == TableOrder.BY_ROW) {
                result = this.source.getColumnKeys();
            }
            else if (this.extract == TableOrder.BY_COLUMN) {
                result = this.source.getRowKeys();
            }
        }
        return result;
    }

    /**
     * Returns the value for a given key.  If the key is not recognised, the
     * method should return <code>null</code> (but note that <code>null</code>
     * can be associated with a valid key also).
     *
     * @param key  the key.
     *
     * @return The value (possibly <code>null</code>).
     */
    public Number getValue(Comparable key) {
        Number result = null;
        int keyIndex = getIndex(key);
        if (keyIndex != -1) {
            if (this.extract == TableOrder.BY_ROW) {
                result = this.source.getValue(this.index, keyIndex);
            }
            else if (this.extract == TableOrder.BY_COLUMN) {
                result = this.source.getValue(keyIndex, this.index);
            }
        }
        return result;
    }

    /**
     * Sends a {@link DatasetChangeEvent} to all registered listeners, with
     * this (not the underlying) dataset as the source.
     *
     * @param event  the event (ignored, a new event with this dataset as the
     *     source is sent to the listeners).
     */
    public void datasetChanged(DatasetChangeEvent event) {
        fireDatasetChanged();
    }

    /**
     * Tests this dataset for equality with an arbitrary object, returning
     * <code>true</code> if <code>obj</code> is a dataset containing the same
     * keys and values in the same order as this dataset.
     *
     * @param obj  the object to test (<code>null</code> permitted).
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

}
