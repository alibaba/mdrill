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
 * ----------------
 * KeyedValues.java
 * ----------------
 * (C) Copyright 2002-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 23-Oct-2002 : Version 1 (DG);
 * 12-Jan-2005 : Updated Javadocs to specify new behaviour when key
 *               is not recognised (DG);
 * ------------- JFREECHART 1.0.0 ---------------------------------------------
 * 02-May-2006 : Updated API docs (DG);
 *
 */

package org.jfree.data;

import java.util.List;

/**
 * An ordered list of (key, value) items where the keys are unique and
 * non-<code>null</code>.
 *
 * @see Values
 * @see DefaultKeyedValues
 */
public interface KeyedValues extends Values {

    /**
     * Returns the key associated with the item at a given position.  Note
     * that some implementations allow re-ordering of the data items, so the
     * result may be transient.
     *
     * @param index  the item index (in the range <code>0</code> to
     *     <code>getItemCount() - 1</code>).
     *
     * @return The key (never <code>null</code>).
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is not in the
     *     specified range.
     */
    public Comparable getKey(int index);

    /**
     * Returns the index for a given key.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The index, or <code>-1</code> if the key is unrecognised.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     */
    public int getIndex(Comparable key);

    /**
     * Returns the keys for the values in the collection.  Note that you can
     * access the values in this collection by key or by index.  For this
     * reason, the key order is important - this method should return the keys
     * in order.  The returned list may be unmodifiable.
     *
     * @return The keys (never <code>null</code>).
     */
    public List getKeys();

    /**
     * Returns the value for a given key.
     *
     * @param key  the key.
     *
     * @return The value (possibly <code>null</code>).
     *
     * @throws UnknownKeyException if the key is not recognised.
     */
    public Number getValue(Comparable key);

}
