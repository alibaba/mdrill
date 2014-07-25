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
 * ---------------------
 * LegendItemEntity.java
 * ---------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 05-Jun-2003 : Version 1 (DG);
 * 20-May-2004 : Added equals() method and implemented Cloneable and
 *               Serializable (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 18-May-2007 : Added dataset and seriesKey fields (DG);
 *
 */

package org.jfree.chart.entity;

import java.awt.Shape;
import java.io.Serializable;

import org.jfree.data.general.Dataset;
import org.jfree.util.ObjectUtilities;

/**
 * An entity that represents an item within a legend.
 */
public class LegendItemEntity extends ChartEntity
                              implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -7435683933545666702L;

    /**
     * The dataset.
     *
     * @since 1.0.6
     */
    private Dataset dataset;

    /**
     * The series key.
     *
     * @since 1.0.6
     */
    private Comparable seriesKey;

    /** The series index. */
    private int seriesIndex;

    /**
     * Creates a legend item entity.
     *
     * @param area  the area.
     */
    public LegendItemEntity(Shape area) {
        super(area);
    }

    /**
     * Returns a reference to the dataset that this legend item is derived
     * from.
     *
     * @return The dataset.
     *
     * @since 1.0.6
     *
     * @see #setDataset(Dataset)
     */
    public Dataset getDataset() {
        return this.dataset;
    }

    /**
     * Sets a reference to the dataset that this legend item is derived from.
     *
     * @param dataset  the dataset.
     *
     * @since 1.0.6
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * Returns the series key that identifies the legend item.
     *
     * @return The series key.
     *
     * @since 1.0.6
     *
     * @see #setSeriesKey(Comparable)
     */
    public Comparable getSeriesKey() {
        return this.seriesKey;
    }

    /**
     * Sets the key for the series.
     *
     * @param key  the key.
     *
     * @since 1.0.6
     *
     * @see #getSeriesKey()
     */
    public void setSeriesKey(Comparable key) {
        this.seriesKey = key;
    }

    /**
     * Returns the series index.
     *
     * @return The series index.
     *
     * @see #setSeriesIndex(int)
     *
     * @deprecated As of 1.0.6, use the {@link #getSeriesKey()} method.
     */
    public int getSeriesIndex() {
        return this.seriesIndex;
    }

    /**
     * Sets the series index.
     *
     * @param index  the series index.
     *
     * @see #getSeriesIndex()
     *
     * @deprecated As of 1.0.6, use the {@link #setSeriesKey(Comparable)}
     *         method.
     */
    public void setSeriesIndex(int index) {
        this.seriesIndex = index;
    }

    /**
     * Tests this object for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LegendItemEntity)) {
            return false;
        }
        LegendItemEntity that = (LegendItemEntity) obj;
        if (!ObjectUtilities.equal(this.seriesKey, that.seriesKey)) {
            return false;
        }
        if (this.seriesIndex != that.seriesIndex) {
            return false;
        }
        if (!ObjectUtilities.equal(this.dataset, that.dataset)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns a clone of the entity.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if there is a problem cloning the
     *         object.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Returns a string representing this object (useful for debugging
     * purposes).
     *
     * @return A string (never <code>null</code>).
     */
    public String toString() {
        return "LegendItemEntity: seriesKey=" + this.seriesKey
                + ", dataset=" + this.dataset;
    }

}
