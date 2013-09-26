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
 * CategoryCrosshairState.java
 * ---------------------------
 * (C) Copyright 2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 26-Jun-2008 : Version 1 (DG);
 *
 */

package org.jfree.chart.plot;

import java.awt.geom.Point2D;

import org.jfree.chart.renderer.category.CategoryItemRenderer;

/**
 * Represents state information for the crosshairs in a {@link CategoryPlot}.
 * An instance of this class is created at the start of the rendering process,
 * and updated as each data item is rendered.  At the end of the rendering
 * process, this class holds the row key, column key and value for the
 * crosshair location.
 *
 * @since 1.0.11
 */
public class CategoryCrosshairState extends CrosshairState {

    /**
     * The row key for the crosshair point.
     */
    private Comparable rowKey;

    /**
     * The column key for the crosshair point.
     */
    private Comparable columnKey;

    /**
     * Creates a new instance.
     */
    public CategoryCrosshairState() {
        this.rowKey = null;
        this.columnKey = null;
    }

    /**
     * Returns the row key.
     *
     * @return The row key.
     */
    public Comparable getRowKey() {
        return this.rowKey;
    }

    /**
     * Sets the row key.
     *
     * @param key  the row key.
     */
    public void setRowKey(Comparable key) {
        this.rowKey = key;
    }

    /**
     * Returns the column key.
     *
     * @return The column key.
     */
    public Comparable getColumnKey() {
        return this.columnKey;
    }

    /**
     * Sets the column key.
     *
     * @param key  the key.
     */
    public void setColumnKey(Comparable key) {
        this.columnKey = key;
    }

    /**
     * Evaluates a data point from a {@link CategoryItemRenderer} and if it is
     * the closest to the anchor point it becomes the new crosshair point.
     *
     * @param rowKey  the row key.
     * @param columnKey  the column key.
     * @param value  y coordinate (measured against the range axis).
     * @param datasetIndex  the dataset index for this point.
     * @param transX  x translated into Java2D space.
     * @param transY  y translated into Java2D space.
     * @param orientation  the plot orientation.
     */
    public void updateCrosshairPoint(Comparable rowKey, Comparable columnKey,
            double value, int datasetIndex, double transX, double transY,
            PlotOrientation orientation) {

        Point2D anchor = getAnchor();
        if (anchor != null) {
            double xx = anchor.getX();
            double yy = anchor.getY();
            if (orientation == PlotOrientation.HORIZONTAL) {
                double temp = yy;
                yy = xx;
                xx = temp;
            }
            double d = (transX - xx) * (transX - xx)
                    + (transY - yy) * (transY - yy);

            if (d < getCrosshairDistance()) {
                this.rowKey = rowKey;
                this.columnKey = columnKey;
                setCrosshairY(value);
                setDatasetIndex(datasetIndex);
                setCrosshairDistance(d);
            }
        }

    }

    /**
     * Updates only the crosshair row and column keys (this is for the case
     * where the range crosshair does NOT lock onto the nearest data value).
     *
     * @param rowKey  the row key.
     * @param columnKey  the column key.
     * @param datasetIndex  the dataset axis index.
     * @param transX  the translated x-value.
     * @param orientation  the plot orientation.
     */
    public void updateCrosshairX(Comparable rowKey, Comparable columnKey,
            int datasetIndex, double transX, PlotOrientation orientation) {

        Point2D anchor = getAnchor();
        if (anchor != null) {
            double anchorX = anchor.getX();
            if (orientation == PlotOrientation.HORIZONTAL) {
                anchorX = anchor.getY();
            }
            double d = Math.abs(transX - anchorX);
            if (d < getCrosshairDistance()) {
                this.rowKey = rowKey;
                this.columnKey = columnKey;
                setDatasetIndex(datasetIndex);
                setCrosshairDistance(d);
            }
        }

    }

}
