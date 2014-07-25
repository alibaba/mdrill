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
 * XYBarDataset.java
 * -----------------
 * (C) Copyright 2004-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 02-Mar-2004 : Version 1 (DG);
 * 05-May-2004 : Now extends AbstractIntervalXYDataset (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 25-Jan-2007 : Added some accessor methods, plus new equals() and clone()
 *               overrides (DG);
 * 30-Jan-2007 : Added method overrides to prevent unnecessary object
 *               creation (DG);
 * 22-Apr-2008 : Implemented PublicCloneable (DG);
 *
 */

package org.jfree.data.xy;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.util.PublicCloneable;

/**
 * A dataset wrapper class that converts a standard {@link XYDataset} into an
 * {@link IntervalXYDataset} suitable for use in creating XY bar charts.
 */
public class XYBarDataset extends AbstractIntervalXYDataset
        implements IntervalXYDataset, DatasetChangeListener, PublicCloneable {

    /** The underlying dataset. */
    private XYDataset underlying;

    /** The bar width. */
    private double barWidth;

    /**
     * Creates a new dataset.
     *
     * @param underlying  the underlying dataset (<code>null</code> not
     *     permitted).
     * @param barWidth  the width of the bars.
     */
    public XYBarDataset(XYDataset underlying, double barWidth) {
        this.underlying = underlying;
        this.underlying.addChangeListener(this);
        this.barWidth = barWidth;
    }

    /**
     * Returns the underlying dataset that was specified via the constructor.
     *
     * @return The underlying dataset (never <code>null</code>).
     *
     * @since 1.0.4
     */
    public XYDataset getUnderlyingDataset() {
        return this.underlying;
    }

    /**
     * Returns the bar width.
     *
     * @return The bar width.
     *
     * @see #setBarWidth(double)
     * @since 1.0.4
     */
    public double getBarWidth() {
        return this.barWidth;
    }

    /**
     * Sets the bar width and sends a {@link DatasetChangeEvent} to all
     * registered listeners.
     *
     * @param barWidth  the bar width.
     *
     * @see #getBarWidth()
     * @since 1.0.4
     */
    public void setBarWidth(double barWidth) {
        this.barWidth = barWidth;
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    /**
     * Returns the number of series in the dataset.
     *
     * @return The series count.
     */
    public int getSeriesCount() {
        return this.underlying.getSeriesCount();
    }

    /**
     * Returns the key for a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     *
     * @return The series key.
     */
    public Comparable getSeriesKey(int series) {
        return this.underlying.getSeriesKey(series);
    }

    /**
     * Returns the number of items in a series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The item count.
     */
    public int getItemCount(int series) {
        return this.underlying.getItemCount(series);
    }

    /**
     * Returns the x-value for an item within a series.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The x-value.
     *
     * @see #getXValue(int, int)
     */
    public Number getX(int series, int item) {
        return this.underlying.getX(series, item);
    }

    /**
     * Returns the x-value (as a double primitive) for an item within a series.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     *
     * @see #getX(int, int)
     */
    public double getXValue(int series, int item) {
        return this.underlying.getXValue(series, item);
    }

    /**
     * Returns the y-value for an item within a series.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The y-value (possibly <code>null</code>).
     *
     * @see #getYValue(int, int)
     */
    public Number getY(int series, int item) {
        return this.underlying.getY(series, item);
    }

    /**
     * Returns the y-value (as a double primitive) for an item within a series.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     *
     * @see #getY(int, int)
     */
    public double getYValue(int series, int item) {
        return this.underlying.getYValue(series, item);
    }

    /**
     * Returns the starting X value for the specified series and item.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */
    public Number getStartX(int series, int item) {
        Number result = null;
        Number xnum = this.underlying.getX(series, item);
        if (xnum != null) {
             result = new Double(xnum.doubleValue() - this.barWidth / 2.0);
        }
        return result;
    }

    /**
     * Returns the starting x-value (as a double primitive) for an item within
     * a series.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     *
     * @see #getXValue(int, int)
     */
    public double getStartXValue(int series, int item) {
        return getXValue(series, item) - this.barWidth / 2.0;
    }

    /**
     * Returns the ending X value for the specified series and item.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */
    public Number getEndX(int series, int item) {
        Number result = null;
        Number xnum = this.underlying.getX(series, item);
        if (xnum != null) {
             result = new Double(xnum.doubleValue() + this.barWidth / 2.0);
        }
        return result;
    }

    /**
     * Returns the ending x-value (as a double primitive) for an item within
     * a series.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     *
     * @see #getXValue(int, int)
     */
    public double getEndXValue(int series, int item) {
        return getXValue(series, item) + this.barWidth / 2.0;
    }

    /**
     * Returns the starting Y value for the specified series and item.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */
    public Number getStartY(int series, int item) {
        return this.underlying.getY(series, item);
    }

    /**
     * Returns the starting y-value (as a double primitive) for an item within
     * a series.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     *
     * @see #getYValue(int, int)
     */
    public double getStartYValue(int series, int item) {
        return getYValue(series, item);
    }

    /**
     * Returns the ending Y value for the specified series and item.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */
    public Number getEndY(int series, int item) {
        return this.underlying.getY(series, item);
    }

    /**
     * Returns the ending y-value (as a double primitive) for an item within
     * a series.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     *
     * @see #getYValue(int, int)
     */
    public double getEndYValue(int series, int item) {
        return getYValue(series, item);
    }

    /**
     * Receives notification of an dataset change event.
     *
     * @param event  information about the event.
     */
    public void datasetChanged(DatasetChangeEvent event) {
        notifyListeners(event);
    }

    /**
     * Tests this dataset for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYBarDataset)) {
            return false;
        }
        XYBarDataset that = (XYBarDataset) obj;
        if (!this.underlying.equals(that.underlying)) {
            return false;
        }
        if (this.barWidth != that.barWidth) {
            return false;
        }
        return true;
    }

    /**
     * Returns an independent copy of the dataset.  Note that:
     * <ul>
     * <li>the underlying dataset is only cloned if it implements the
     * {@link PublicCloneable} interface;</li>
     * <li>the listeners registered with this dataset are not carried over to
     * the cloned dataset.</li>
     * </ul>
     *
     * @return An independent copy of the dataset.
     *
     * @throws CloneNotSupportedException if the dataset cannot be cloned for
     *         any reason.
     */
    public Object clone() throws CloneNotSupportedException {
        XYBarDataset clone = (XYBarDataset) super.clone();
        if (this.underlying instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable) this.underlying;
            clone.underlying = (XYDataset) pc.clone();
        }
        return clone;
    }

}
