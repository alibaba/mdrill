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
 * -----------------
 * MatrixSeries.java
 * -----------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Zhitao Wang;
 *
 * Changes
 * -------
 * 10-Jul-2003 : Version 1 contributed by Barak Naveh (DG);
 * 10-Feb-2004 : Fixed Checkstyle complaints (DG);
 * 21-May-2004 : Fixed bug 940188 - problem in getItemColumn() and
 *               getItemRow() (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 27-Nov-2006 : Fixed bug in equals() method (DG);
 * 02-Feb-2007 : Removed author tags all over JFreeChart sources (DG);
 *
 */

package org.jfree.data.xy;

import java.io.Serializable;

import org.jfree.data.general.Series;

/**
 * Represents a dense matrix M[i,j] where each Mij item of the matrix has a
 * value (default is 0).
 */
public class MatrixSeries extends Series implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 7934188527308315704L;

    /** Series matrix values */
    protected double[][] data;

    /**
     * Constructs a new matrix series.
     * <p>
     * By default, all matrix items are initialzed to 0.
     * </p>
     *
     * @param name  series name (<code>null</code> not permitted).
     * @param rows  the number of rows.
     * @param columns  the number of columns.
     */
    public MatrixSeries(String name, int rows, int columns) {
        super(name);
        this.data = new double[rows][columns];
        zeroAll();
    }

    /**
     * Returns the number of columns in this matrix series.
     *
     * @return The number of columns in this matrix series.
     */
    public int getColumnsCount() {
        return this.data[0].length;
    }


    /**
     * Return the matrix item at the specified index.  Note that this method
     * creates a new <code>Double</code> instance every time it is called.
     *
     * @param itemIndex item index.
     *
     * @return The matrix item at the specified index.
     *
     * @see #get(int, int)
     */
    public Number getItem(int itemIndex) {
        int i = getItemRow(itemIndex);
        int j = getItemColumn(itemIndex);

        Number n = new Double(get(i, j));

        return n;
    }


    /**
     * Returns the column of the specified item.
     *
     * @param itemIndex the index of the item.
     *
     * @return The column of the specified item.
     */
    public int getItemColumn(int itemIndex) {
        //assert itemIndex >= 0 && itemIndex < getItemCount();
        return itemIndex % getColumnsCount();
    }


    /**
     * Returns the number of items in the series.
     *
     * @return The item count.
     */
    public int getItemCount() {
        return getRowCount() * getColumnsCount();
    }


    /**
     * Returns the row of the specified item.
     *
     * @param itemIndex the index of the item.
     *
     * @return The row of the specified item.
     */
    public int getItemRow(int itemIndex) {
        //assert itemIndex >= 0 && itemIndex < getItemCount();
        return itemIndex / getColumnsCount();
    }


    /**
     * Returns the number of rows in this matrix series.
     *
     * @return The number of rows in this matrix series.
     */
    public int getRowCount() {
        return this.data.length;
    }


    /**
     * Returns the value of the specified item in this matrix series.
     *
     * @param i the row of the item.
     * @param j the column of the item.
     *
     * @return The value of the specified item in this matrix series.
     *
     * @see #getItem(int)
     * @see #update(int, int, double)
     */
    public double get(int i, int j) {
        return this.data[i][j];
    }


    /**
     * Updates the value of the specified item in this matrix series.
     *
     * @param i the row of the item.
     * @param j the column of the item.
     * @param mij the new value for the item.
     *
     * @see #get(int, int)
     */
    public void update(int i, int j, double mij) {
        this.data[i][j] = mij;
        fireSeriesChanged();
    }


    /**
     * Sets all matrix values to zero and sends a
     * {@link org.jfree.data.general.SeriesChangeEvent} to all registered
     * listeners.
     */
    public void zeroAll() {
        int rows = getRowCount();
        int columns = getColumnsCount();

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                this.data[row][column] = 0.0;
            }
        }
        fireSeriesChanged();
    }

    /**
     * Tests this object instance for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MatrixSeries)) {
            return false;
        }
        MatrixSeries that = (MatrixSeries) obj;
        if (!(getRowCount() == that.getRowCount())) {
            return false;
        }
        if (!(getColumnsCount() == that.getColumnsCount())) {
            return false;
        }
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnsCount(); c++) {
                if (get(r, c) != that.get(r, c)) {
                    return false;
                }
            }
        }
        return super.equals(obj);
    }

}
