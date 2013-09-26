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
 * NormalizedMatrixSeries.java
 * ---------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 10-Jul-2003 : Version 1 contributed by Barak Naveh (DG);
 * 02-Feb-2007 : Removed author tags all over JFreeChart sources (DG);
 *
 */

package org.jfree.data.xy;

/**
 * Represents a dense normalized matrix M[i,j] where each Mij item of the
 * matrix has a value (default is 0). When a matrix item is observed using
 * <code>getItem</code> method, it is normalized, that is, divided by the
 * total sum of all items. It can be also be scaled by setting a scale factor.
 */
public class NormalizedMatrixSeries extends MatrixSeries {

    /** The default scale factor. */
    public static final double DEFAULT_SCALE_FACTOR = 1.0;

    /**
     * A factor that multiplies each item in this series when observed using
     * getItem method.
     */
    private double m_scaleFactor = DEFAULT_SCALE_FACTOR;

    /** The sum of all items in this matrix */
    private double m_totalSum;

    /**
     * Constructor for NormalizedMatrixSeries.
     *
     * @param name  the series name.
     * @param rows  the number of rows.
     * @param columns  the number of columns.
     */
    public NormalizedMatrixSeries(String name, int rows, int columns) {
        super(name, rows, columns);

        /*
         * we assum super is always initialized to all-zero matrix, so the
         * total sum should be 0 upon initialization. However, we set it to
         * Double.MIN_VALUE to get the same effect and yet avoid division by 0
         * upon initialization.
         */
        this.m_totalSum = Double.MIN_VALUE;
    }

    /**
     * Returns an item.
     *
     * @param itemIndex  the index.
     *
     * @return The value.
     *
     * @see org.jfree.data.xy.MatrixSeries#getItem(int)
     */
    public Number getItem(int itemIndex) {
        int i = getItemRow(itemIndex);
        int j = getItemColumn(itemIndex);

        double mij = get(i, j) * this.m_scaleFactor;
        Number n = new Double(mij / this.m_totalSum);

        return n;
    }

    /**
     * Sets the factor that multiplies each item in this series when observed
     * using getItem mehtod.
     *
     * @param factor new factor to set.
     *
     * @see #DEFAULT_SCALE_FACTOR
     */
    public void setScaleFactor(double factor) {
        this.m_scaleFactor = factor;
        // FIXME: this should generate a series change event
    }


    /**
     * Returns the factor that multiplies each item in this series when
     * observed using getItem mehtod.
     *
     * @return The factor
     */
    public double getScaleFactor() {
        return this.m_scaleFactor;
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
        this.m_totalSum -= get(i, j);
        this.m_totalSum += mij;

        super.update(i, j, mij);
    }

    /**
     * @see org.jfree.data.xy.MatrixSeries#zeroAll()
     */
    public void zeroAll() {
        this.m_totalSum = 0;
        super.zeroAll();
    }
}
