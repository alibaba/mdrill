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
 * ------------------
 * XYTaskDataset.java
 * ------------------
 * (C) Copyright 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 17-Sep-2008 : Version 1 (DG);
 *
 */

package org.jfree.data.gantt;

import java.util.Date;

import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.IntervalXYDataset;

/**
 * A dataset implementation that wraps a {@link TaskSeriesCollection} and
 * presents it as an {@link IntervalXYDataset}, allowing a set of tasks to
 * be displayed using an {@link XYBarRenderer} (and usually a
 * {@link SymbolAxis}).  This is a very specialised dataset implementation
 * ---before using it, you should take some time to understand the use-cases
 * that it is designed for.
 *
 * @since 1.0.11
 */
public class XYTaskDataset extends AbstractXYDataset
        implements IntervalXYDataset, DatasetChangeListener {

    /** The underlying tasks. */
    private TaskSeriesCollection underlying;

    /** The series interval width (typically 0.0 < w <= 1.0). */
    private double seriesWidth;

    /** A flag that controls whether or not the data values are transposed. */
    private boolean transposed;

    /**
     * Creates a new dataset based on the supplied collection of tasks.
     *
     * @param tasks  the underlying dataset (<code>null</code> not permitted).
     */
    public XYTaskDataset(TaskSeriesCollection tasks) {
        if (tasks == null) {
            throw new IllegalArgumentException("Null 'tasks' argument.");
        }
        this.underlying = tasks;
        this.seriesWidth = 0.8;
        this.underlying.addChangeListener(this);
    }

    /**
     * Returns the underlying task series collection that was supplied to the
     * constructor.
     *
     * @return The underlying collection (never <code>null</code>).
     */
    public TaskSeriesCollection getTasks() {
        return this.underlying;
    }

    /**
     * Returns the width of the interval for each series this dataset.
     *
     * @return The width of the series interval.
     *
     * @see #setSeriesWidth(double)
     */
    public double getSeriesWidth() {
        return this.seriesWidth;
    }

    /**
     * Sets the series interval width and sends a {@link DatasetChangeEvent} to
     * all registered listeners.
     *
     * @param w  the width.
     *
     * @see #getSeriesWidth()
     */
    public void setSeriesWidth(double w) {
        if (w <= 0.0) {
            throw new IllegalArgumentException("Requires 'w' > 0.0.");
        }
        this.seriesWidth = w;
        fireDatasetChanged();
    }

    /**
     * Returns a flag that indicates whether or not the dataset is transposed.
     * The default is <code>false</code> which means the x-values are integers
     * corresponding to the series indices, and the y-values are millisecond
     * values corresponding to the task date/time intervals.  If the flag
     * is set to <code>true</code>, the x and y-values are reversed.
     *
     * @return The flag.
     *
     * @see #setTransposed(boolean)
     */
    public boolean isTransposed() {
        return this.transposed;
    }

    /**
     * Sets the flag that controls whether or not the dataset is transposed
     * and sends a {@link DatasetChangeEvent} to all registered listeners.
     *
     * @param transposed  the new flag value.
     *
     * @see #isTransposed()
     */
    public void setTransposed(boolean transposed) {
        this.transposed = transposed;
        fireDatasetChanged();
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
     * Returns the name of a series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The name of a series.
     */
    public Comparable getSeriesKey(int series) {
        return this.underlying.getSeriesKey(series);
    }

    /**
     * Returns the number of items (tasks) in the specified series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The item count.
     */
    public int getItemCount(int series) {
        return this.underlying.getSeries(series).getItemCount();
    }

    /**
     * Returns the x-value (as a double primitive) for an item within a series.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */
    public double getXValue(int series, int item) {
        if (!this.transposed) {
            return getSeriesValue(series);
        }
        else {
            return getItemValue(series, item);
        }
    }

    /**
     * Returns the starting date/time for the specified item (task) in the
     * given series, measured in milliseconds since 1-Jan-1970 (as in
     * java.util.Date).
     *
     * @param series  the series index.
     * @param item  the item (or task) index.
     *
     * @return The start date/time.
     */
    public double getStartXValue(int series, int item) {
        if (!this.transposed) {
            return getSeriesStartValue(series);
        }
        else {
            return getItemStartValue(series, item);
        }
    }

    /**
     * Returns the ending date/time for the specified item (task) in the
     * given series, measured in milliseconds since 1-Jan-1970 (as in
     * java.util.Date).
     *
     * @param series  the series index.
     * @param item  the item (or task) index.
     *
     * @return The end date/time.
     */
    public double getEndXValue(int series, int item) {
        if (!this.transposed) {
            return getSeriesEndValue(series);
        }
        else {
            return getItemEndValue(series, item);
        }
    }

    /**
     * Returns the x-value for the specified series.
     *
     * @param series  the series index.
     * @param item  the item index.
     *
     * @return The x-value (in milliseconds).
     */
    public Number getX(int series, int item) {
        return new Double(getXValue(series, item));
    }

    /**
     * Returns the starting date/time for the specified item (task) in the
     * given series, measured in milliseconds since 1-Jan-1970 (as in
     * java.util.Date).
     *
     * @param series  the series index.
     * @param item  the item (or task) index.
     *
     * @return The start date/time.
     */
    public Number getStartX(int series, int item) {
        return new Double(getStartXValue(series, item));
    }

    /**
     * Returns the ending date/time for the specified item (task) in the
     * given series, measured in milliseconds since 1-Jan-1970 (as in
     * java.util.Date).
     *
     * @param series  the series index.
     * @param item  the item (or task) index.
     *
     * @return The end date/time.
     */
    public Number getEndX(int series, int item) {
        return new Double(getEndXValue(series, item));
    }

    /**
     * Returns the y-value (as a double primitive) for an item within a series.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */
    public double getYValue(int series, int item) {
        if (!this.transposed) {
            return getItemValue(series, item);
        }
        else {
            return getSeriesValue(series);
        }
    }

    /**
     * Returns the starting value of the y-interval for an item in the
     * given series.
     *
     * @param series  the series index.
     * @param item  the item (or task) index.
     *
     * @return The y-interval start.
     */
    public double getStartYValue(int series, int item) {
        if (!this.transposed) {
            return getItemStartValue(series, item);
        }
        else {
            return getSeriesStartValue(series);
        }
    }

    /**
     * Returns the ending value of the y-interval for an item in the
     * given series.
     *
     * @param series  the series index.
     * @param item  the item (or task) index.
     *
     * @return The y-interval end.
     */
    public double getEndYValue(int series, int item) {
        if (!this.transposed) {
            return getItemEndValue(series, item);
        }
        else {
            return getSeriesEndValue(series);
        }
    }

    /**
     * Returns the y-value for the specified series/item.  In this
     * implementation, we return the series index as the y-value (this means
     * that every item in the series has a constant integer value).
     *
     * @param series  the series index.
     * @param item  the item index.
     *
     * @return The y-value.
     */
    public Number getY(int series, int item) {
        return new Double(getYValue(series, item));
    }

    /**
     * Returns the starting value of the y-interval for an item in the
     * given series.
     *
     * @param series  the series index.
     * @param item  the item (or task) index.
     *
     * @return The y-interval start.
     */
    public Number getStartY(int series, int item) {
        return new Double(getStartYValue(series, item));
    }

    /**
     * Returns the ending value of the y-interval for an item in the
     * given series.
     *
     * @param series  the series index.
     * @param item  the item (or task) index.
     *
     * @return The y-interval end.
     */
    public Number getEndY(int series, int item) {
        return new Double(getEndYValue(series, item));
    }

    private double getSeriesValue(int series) {
        return series;
    }

    private double getSeriesStartValue(int series) {
        return series - this.seriesWidth / 2.0;
    }

    private double getSeriesEndValue(int series) {
        return series + this.seriesWidth / 2.0;
    }

    private double getItemValue(int series, int item) {
        TaskSeries s = this.underlying.getSeries(series);
        Task t = s.get(item);
        TimePeriod duration = t.getDuration();
        Date start = duration.getStart();
        Date end = duration.getEnd();
        return (start.getTime() + end.getTime()) / 2.0;
    }

    private double getItemStartValue(int series, int item) {
        TaskSeries s = this.underlying.getSeries(series);
        Task t = s.get(item);
        TimePeriod duration = t.getDuration();
        Date start = duration.getStart();
        return start.getTime();
    }

    private double getItemEndValue(int series, int item) {
        TaskSeries s = this.underlying.getSeries(series);
        Task t = s.get(item);
        TimePeriod duration = t.getDuration();
        Date end = duration.getEnd();
        return end.getTime();
    }


    /**
     * Receives a change event from the underlying dataset and responds by
     * firing a change event for this dataset.
     *
     * @param event  the event.
     */
    public void datasetChanged(DatasetChangeEvent event) {
        fireDatasetChanged();
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
        if (!(obj instanceof XYTaskDataset)) {
            return false;
        }
        XYTaskDataset that = (XYTaskDataset) obj;
        if (this.seriesWidth != that.seriesWidth) {
            return false;
        }
        if (this.transposed != that.transposed) {
            return false;
        }
        if (!this.underlying.equals(that.underlying)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a clone of this dataset.
     *
     * @return A clone of this dataset.
     *
     * @throws CloneNotSupportedException if there is a problem cloning.
     */
    public Object clone() throws CloneNotSupportedException {
        XYTaskDataset clone = (XYTaskDataset) super.clone();
        clone.underlying = (TaskSeriesCollection) this.underlying.clone();
        return clone;
    }

}
