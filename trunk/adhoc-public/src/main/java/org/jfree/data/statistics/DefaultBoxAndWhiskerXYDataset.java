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
 * ----------------------------------
 * DefaultBoxAndWhiskerXYDataset.java
 * ----------------------------------
 * (C) Copyright 2003-2008, by David Browning and Contributors.
 *
 * Original Author:  David Browning (for Australian Institute of Marine
 *                   Science);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 05-Aug-2003 : Version 1, contributed by David Browning (DG);
 * 08-Aug-2003 : Minor changes to comments (DB)
 *               Allow average to be null  - average is a perculiar AIMS
 *               requirement which probably should be stripped out and overlaid
 *               if required...
 *               Added a number of methods to allow the max and min non-outlier
 *               and non-farout values to be calculated
 * 12-Aug-2003   Changed the getYValue to return the highest outlier value
 *               Added getters and setters for outlier and farout coefficients
 * 27-Aug-2003 : Renamed DefaultBoxAndWhiskerDataset
 *               --> DefaultBoxAndWhiskerXYDataset (DG);
 * 06-May-2004 : Now extends AbstractXYDataset (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 18-Nov-2004 : Updated for changes in RangeInfo interface (DG);
 * 11-Jan-2005 : Removed deprecated code in preparation for the 1.0.0
 *               release (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags from all over JFreeChart sources (DG);
 * 12-Nov-2007 : Implemented equals() and clone() (DG);
 *
 */

package org.jfree.data.statistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.util.ObjectUtilities;

/**
 * A simple implementation of the {@link BoxAndWhiskerXYDataset} interface.
 * This dataset implementation can hold only one series.
 */
public class DefaultBoxAndWhiskerXYDataset extends AbstractXYDataset
            implements BoxAndWhiskerXYDataset, RangeInfo {

    /** The series key. */
    private Comparable seriesKey;

    /** Storage for the dates. */
    private List dates;

    /** Storage for the box and whisker statistics. */
    private List items;

    /** The minimum range value. */
    private Number minimumRangeValue;

    /** The maximum range value. */
    private Number maximumRangeValue;

    /** The range of values. */
    private Range rangeBounds;

    /**
     * The coefficient used to calculate outliers. Tukey's default value is
     * 1.5 (see EDA) Any value which is greater than Q3 + (interquartile range
     * * outlier coefficient) is considered to be an outlier.  Can be altered
     * if the data is particularly skewed.
     */
    private double outlierCoefficient = 1.5;

    /**
     * The coefficient used to calculate farouts. Tukey's default value is 2
     * (see EDA) Any value which is greater than Q3 + (interquartile range *
     * farout coefficient) is considered to be a farout.  Can be altered if the
     * data is particularly skewed.
     */
    private double faroutCoefficient = 2.0;

    /**
     * Constructs a new box and whisker dataset.
     * <p>
     * The current implementation allows only one series in the dataset.
     * This may be extended in a future version.
     *
     * @param seriesKey  the key for the series.
     */
    public DefaultBoxAndWhiskerXYDataset(Comparable seriesKey) {
        this.seriesKey = seriesKey;
        this.dates = new ArrayList();
        this.items = new ArrayList();
        this.minimumRangeValue = null;
        this.maximumRangeValue = null;
        this.rangeBounds = null;
    }

    /**
     * Returns the value used as the outlier coefficient. The outlier
     * coefficient gives an indication of the degree of certainty in an
     * unskewed distribution.  Increasing the coefficient increases the number
     * of values included. Currently only used to ensure farout coefficient is
     * greater than the outlier coefficient
     *
     * @return A <code>double</code> representing the value used to calculate
     *         outliers.
     *
     * @see #setOutlierCoefficient(double)
     */
    public double getOutlierCoefficient() {
        return this.outlierCoefficient;
    }

    /**
     * Sets the value used as the outlier coefficient
     *
     * @param outlierCoefficient  being a <code>double</code> representing the
     *                            value used to calculate outliers.
     *
     * @see #getOutlierCoefficient()
     */
    public void setOutlierCoefficient(double outlierCoefficient) {
        this.outlierCoefficient = outlierCoefficient;
    }

    /**
     * Returns the value used as the farout coefficient. The farout coefficient
     * allows the calculation of which values will be off the graph.
     *
     * @return A <code>double</code> representing the value used to calculate
     *         farouts.
     *
     * @see #setFaroutCoefficient(double)
     */
    public double getFaroutCoefficient() {
        return this.faroutCoefficient;
    }

    /**
     * Sets the value used as the farouts coefficient. The farout coefficient
     * must b greater than the outlier coefficient.
     *
     * @param faroutCoefficient being a <code>double</code> representing the
     *                          value used to calculate farouts.
     *
     * @see #getFaroutCoefficient()
     */
    public void setFaroutCoefficient(double faroutCoefficient) {

        if (faroutCoefficient > getOutlierCoefficient()) {
            this.faroutCoefficient = faroutCoefficient;
        }
        else {
            throw new IllegalArgumentException("Farout value must be greater "
                + "than the outlier value, which is currently set at: ("
                + getOutlierCoefficient() + ")");
        }
    }

    /**
     * Returns the number of series in the dataset.
     * <p>
     * This implementation only allows one series.
     *
     * @return The number of series.
     */
    public int getSeriesCount() {
        return 1;
    }

    /**
     * Returns the number of items in the specified series.
     *
     * @param series  the index (zero-based) of the series.
     *
     * @return The number of items in the specified series.
     */
    public int getItemCount(int series) {
        return this.dates.size();
    }

    /**
     * Adds an item to the dataset and sends a {@link DatasetChangeEvent} to
     * all registered listeners.
     *
     * @param date  the date (<code>null</code> not permitted).
     * @param item  the item (<code>null</code> not permitted).
     */
    public void add(Date date, BoxAndWhiskerItem item) {
        this.dates.add(date);
        this.items.add(item);
        if (this.minimumRangeValue == null) {
            this.minimumRangeValue = item.getMinRegularValue();
        }
        else {
            if (item.getMinRegularValue().doubleValue()
                    < this.minimumRangeValue.doubleValue()) {
                this.minimumRangeValue = item.getMinRegularValue();
            }
        }
        if (this.maximumRangeValue == null) {
            this.maximumRangeValue = item.getMaxRegularValue();
        }
        else {
            if (item.getMaxRegularValue().doubleValue()
                    > this.maximumRangeValue.doubleValue()) {
                this.maximumRangeValue = item.getMaxRegularValue();
            }
        }
        this.rangeBounds = new Range(this.minimumRangeValue.doubleValue(),
                this.maximumRangeValue.doubleValue());
        fireDatasetChanged();
    }

    /**
     * Returns the name of the series stored in this dataset.
     *
     * @param i  the index of the series. Currently ignored.
     *
     * @return The name of this series.
     */
    public Comparable getSeriesKey(int i) {
        return this.seriesKey;
    }

    /**
     * Return an item from within the dataset.
     *
     * @param series  the series index (ignored, since this dataset contains
     *                only one series).
     * @param item  the item within the series (zero-based index)
     *
     * @return The item.
     */
    public BoxAndWhiskerItem getItem(int series, int item) {
        return (BoxAndWhiskerItem) this.items.get(item);
    }

    /**
     * Returns the x-value for one item in a series.
     * <p>
     * The value returned is a Long object generated from the underlying Date
     * object.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The x-value.
     */
    public Number getX(int series, int item) {
        return new Long(((Date) this.dates.get(item)).getTime());
    }

    /**
     * Returns the x-value for one item in a series, as a Date.
     * <p>
     * This method is provided for convenience only.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The x-value as a Date.
     */
    public Date getXDate(int series, int item) {
        return (Date) this.dates.get(item);
    }

    /**
     * Returns the y-value for one item in a series.
     * <p>
     * This method (from the XYDataset interface) is mapped to the
     * getMeanValue() method.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The y-value.
     */
    public Number getY(int series, int item) {
        return getMeanValue(series, item);
    }

    /**
     * Returns the mean for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The mean for the specified series and item.
     */
    public Number getMeanValue(int series, int item) {
        Number result = null;
        BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
        if (stats != null) {
            result = stats.getMean();
        }
        return result;
    }

    /**
     * Returns the median-value for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The median-value for the specified series and item.
     */
    public Number getMedianValue(int series, int item) {
        Number result = null;
        BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
        if (stats != null) {
            result = stats.getMedian();
        }
        return result;
    }

    /**
     * Returns the Q1 median-value for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The Q1 median-value for the specified series and item.
     */
    public Number getQ1Value(int series, int item) {
        Number result = null;
        BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
        if (stats != null) {
            result = stats.getQ1();
        }
        return result;
    }

    /**
     * Returns the Q3 median-value for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The Q3 median-value for the specified series and item.
     */
    public Number getQ3Value(int series, int item) {
        Number result = null;
        BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
        if (stats != null) {
            result = stats.getQ3();
        }
        return result;
    }

    /**
     * Returns the min-value for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The min-value for the specified series and item.
     */
    public Number getMinRegularValue(int series, int item) {
        Number result = null;
        BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
        if (stats != null) {
            result = stats.getMinRegularValue();
        }
        return result;
    }

    /**
     * Returns the max-value for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The max-value for the specified series and item.
     */
    public Number getMaxRegularValue(int series, int item) {
        Number result = null;
        BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
        if (stats != null) {
            result = stats.getMaxRegularValue();
        }
        return result;
    }

    /**
     * Returns the minimum value which is not a farout.
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return A <code>Number</code> representing the maximum non-farout value.
     */
    public Number getMinOutlier(int series, int item) {
        Number result = null;
        BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
        if (stats != null) {
            result = stats.getMinOutlier();
        }
        return result;
    }

    /**
     * Returns the maximum value which is not a farout, ie Q3 + (interquartile
     * range * farout coefficient).
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return A <code>Number</code> representing the maximum non-farout value.
     */
    public Number getMaxOutlier(int series, int item) {
        Number result = null;
        BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
        if (stats != null) {
            result = stats.getMaxOutlier();
        }
        return result;
    }

    /**
     * Returns an array of outliers for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The array of outliers for the specified series and item.
     */
    public List getOutliers(int series, int item) {
        List result = null;
        BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
        if (stats != null) {
            result = stats.getOutliers();
        }
        return result;
    }

    /**
     * Returns the minimum y-value in the dataset.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         y-interval is taken into account.
     *
     * @return The minimum value.
     */
    public double getRangeLowerBound(boolean includeInterval) {
        double result = Double.NaN;
        if (this.minimumRangeValue != null) {
            result = this.minimumRangeValue.doubleValue();
        }
        return result;
    }

    /**
     * Returns the maximum y-value in the dataset.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         y-interval is taken into account.
     *
     * @return The maximum value.
     */
    public double getRangeUpperBound(boolean includeInterval) {
        double result = Double.NaN;
        if (this.maximumRangeValue != null) {
            result = this.maximumRangeValue.doubleValue();
        }
        return result;
    }

    /**
     * Returns the range of the values in this dataset's range.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         y-interval is taken into account.
     *
     * @return The range.
     */
    public Range getRangeBounds(boolean includeInterval) {
        return this.rangeBounds;
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
        if (!(obj instanceof DefaultBoxAndWhiskerXYDataset)) {
            return false;
        }
        DefaultBoxAndWhiskerXYDataset that
                = (DefaultBoxAndWhiskerXYDataset) obj;
        if (!ObjectUtilities.equal(this.seriesKey, that.seriesKey)) {
            return false;
        }
        if (!this.dates.equals(that.dates)) {
            return false;
        }
        if (!this.items.equals(that.items)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a clone of the plot.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  if the cloning is not supported.
     */
    public Object clone() throws CloneNotSupportedException {
        DefaultBoxAndWhiskerXYDataset clone
                = (DefaultBoxAndWhiskerXYDataset) super.clone();
        clone.dates = new java.util.ArrayList(this.dates);
        clone.items = new java.util.ArrayList(this.items);
        return clone;
    }

}
