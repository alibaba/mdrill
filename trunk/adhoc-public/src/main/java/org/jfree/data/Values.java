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
 * -----------
 * Values.java
 * -----------
 * (C) Copyright 2001-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 08-Nov-2001 : Version 1 (DG);
 * 23-Oct-2002 : Renamed getValueCount --> getItemCount (DG);#
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-May-2006 : Updated API docs (DG);
 *
 */

package org.jfree.data;

/**
 * An interface through which (single-dimension) data values can be accessed.
 */
public interface Values {

    /**
     * Returns the number of items (values) in the collection.
     *
     * @return The item count (possibly zero).
     */
    public int getItemCount();

    /**
     * Returns the value with the specified index.
     *
     * @param index  the item index (in the range <code>0</code> to
     *     <code>getItemCount() - 1</code>).
     *
     * @return The value (possibly <code>null</code>).
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is not in the
     *     specified range.
     */
    public Number getValue(int index);

}
