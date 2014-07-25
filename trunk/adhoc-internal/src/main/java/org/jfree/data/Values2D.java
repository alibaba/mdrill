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
 * -------------
 * Values2D.java
 * -------------
 * (C) Copyright 2002-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 28-Oct-2002 : Version 1 (DG);
 *
 */

package org.jfree.data;

/**
 * A general purpose interface that can be used to access a table of values.
 */
public interface Values2D {

    /**
     * Returns the number of rows in the table.
     *
     * @return The row count.
     */
    public int getRowCount();

    /**
     * Returns the number of columns in the table.
     *
     * @return The column count.
     */
    public int getColumnCount();

    /**
     * Returns a value from the table.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The value (possibly <code>null</code>).
     *
     * @throws IndexOutOfBoundsException if the <code>row</code>
     *         or <code>column</code> is out of bounds.
     */
    public Number getValue(int row, int column);

}
