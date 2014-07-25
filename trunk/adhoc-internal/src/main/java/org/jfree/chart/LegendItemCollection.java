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
 * LegendItemCollection.java
 * -------------------------
 * (C) Copyright 2002-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 07-Feb-2002 : Version 1 (DG);
 * 24-Sep-2002 : Added get(int) and getItemCount() methods (DG);
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 18-Apr-2005 : Added equals() method and implemented Cloneable and
 *               Serializable (DG);
 * 23-Apr-2008 : Fixed clone() method (DG);
 *
 */

package org.jfree.chart;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.jfree.util.ObjectUtilities;

/**
 * A collection of legend items.
 */
public class LegendItemCollection implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 1365215565589815953L;

    /** Storage for the legend items. */
    private List items;

    /**
     * Constructs a new legend item collection, initially empty.
     */
    public LegendItemCollection() {
        this.items = new java.util.ArrayList();
    }

    /**
     * Adds a legend item to the collection.
     *
     * @param item  the item to add.
     */
    public void add(LegendItem item) {
        this.items.add(item);
    }

    /**
     * Adds the legend items from another collection to this collection.
     *
     * @param collection  the other collection.
     */
    public void addAll(LegendItemCollection collection) {
        this.items.addAll(collection.items);
    }

    /**
     * Returns a legend item from the collection.
     *
     * @param index  the legend item index (zero-based).
     *
     * @return The legend item.
     */
    public LegendItem get(int index) {
        return (LegendItem) this.items.get(index);
    }

    /**
     * Returns the number of legend items in the collection.
     *
     * @return The item count.
     */
    public int getItemCount() {
        return this.items.size();
    }

    /**
     * Returns an iterator that provides access to all the legend items.
     *
     * @return An iterator.
     */
    public Iterator iterator() {
        return this.items.iterator();
    }

    /**
     * Tests this collection for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LegendItemCollection)) {
            return false;
        }
        LegendItemCollection that = (LegendItemCollection) obj;
        if (!this.items.equals(that.items)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a clone of the collection.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if an item in the collection is not
     *         cloneable.
     */
    public Object clone() throws CloneNotSupportedException {
        LegendItemCollection clone = (LegendItemCollection) super.clone();
        clone.items = (List) ObjectUtilities.deepClone(this.items);
        return clone;
    }

}
