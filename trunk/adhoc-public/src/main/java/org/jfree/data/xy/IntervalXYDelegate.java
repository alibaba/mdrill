/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
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
 * -----------------------
 * IntervalXYDelegate.java
 * -----------------------
 * (C) Copyright 2004-2009, by Andreas Schroeder and Contributors.
 *
 * Original Author:  Andreas Schroeder;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 31-Mar-2004 : Version 1 (AS);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 18-Aug-2004 : Moved from org.jfree.data --> org.jfree.data.xy (DG);
 * 04-Nov-2004 : Added argument check for setIntervalWidth() method (DG);
 * 17-Nov-2004 : New methods to reflect changes in DomainInfo (DG);
 * 11-Jan-2005 : Removed deprecated methods in preparation for the 1.0.0
 *               release (DG);
 * 21-Feb-2005 : Made public and added equals() method (DG);
 * 06-Oct-2005 : Implemented DatasetChangeListener to recalculate
 *               autoIntervalWidth (DG);
 * 02-Feb-2007 : Removed author tags all over JFreeChart sources (DG);
 * 06-Mar-2009 : Implemented hashCode() (DG);
 *
 */

package org.jfree.data.xy;

import java.io.Serializable;

import org.jfree.chart.HashUtilities;
import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A delegate that handles the specification or automatic calculation of the
 * interval surrounding the x-values in a dataset.  This is used to extend
 * a regular {@link XYDataset} to support the {@link IntervalXYDataset}
 * interface.
 * <p>
 * The decorator pattern was not used because of the several possibly
 * implemented interfaces of the decorated instance (e.g.
 * {@link TableXYDataset}, {@link RangeInfo}, {@link DomainInfo} etc.).
 * <p>
 * The width can be set manually or calculated automatically. The switch
 * autoWidth allows to determine which behavior is used. The auto width
 * calculation tries to find the smallest gap between two x-values in the
 * dataset.  If there is only one item in the series, the auto width
 * calculation fails and falls back on the manually set interval width (which
 * is itself defaulted to 1.0).
 */
public class IntervalXYDelegate implements DatasetChangeListener,
        DomainInfo, Serializable, Cloneable, PublicCloneable {

    /** For serialization. */
    private static final long serialVersionUID = -685166711639592857L;

    /**
     * The dataset to enhance.
     */
    private XYDataset dataset;

    /**
     * A flag to indicate whether the width should be calculated automatically.
     */
    private boolean autoWidth;

    /**
     * A value between 0.0 and 1.0 that indicates the position of the x-value
     * within the interval.
     */
    private double intervalPositionFactor;

    /**
     * The fixed interval width (defaults to 1.0).
     */
    private double fixedIntervalWidth;

    /**
     * The automatically calculated interval width.
     */
    private double autoIntervalWidth;

    /**
     * Creates a new delegate that.
     *
     * @param dataset  the underlying dataset (<code>null</code> not permitted).
     */
    public IntervalXYDelegate(XYDataset dataset) {
        this(dataset, true);
    }

    /**
     * Creates a new delegate for the specified dataset.
     *
     * @param dataset  the underlying dataset (<code>null</code> not permitted).
     * @param autoWidth  a flag that controls whether the interval width is
     *                   calculated automatically.
     */
    public IntervalXYDelegate(XYDataset dataset, boolean autoWidth) {
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
        this.dataset = dataset;
        this.autoWidth = autoWidth;
        this.intervalPositionFactor = 0.5;
        this.autoIntervalWidth = Double.POSITIVE_INFINITY;
        this.fixedIntervalWidth = 1.0;
    }

    /**
     * Returns <code>true</code> if the interval width is automatically
     * calculated, and <code>false</code> otherwise.
     *
     * @return A boolean.
     */
    public boolean isAutoWidth() {
        return this.autoWidth;
    }

    /**
     * Sets the flag that indicates whether the interval width is automatically
     * calculated.  If the flag is set to <code>true</code>, the interval is
     * recalculated.
     * <p>
     * Note: recalculating the interval amounts to changing the data values
     * represented by the dataset.  The calling dataset must fire an
     * appropriate {@link DatasetChangeEvent}.
     *
     * @param b  a boolean.
     */
    public void setAutoWidth(boolean b) {
        this.autoWidth = b;
        if (b) {
            this.autoIntervalWidth = recalculateInterval();
        }
    }

    /**
     * Returns the interval position factor.
     *
     * @return The interval position factor.
     */
    public double getIntervalPositionFactor() {
        return this.intervalPositionFactor;
    }

    /**
     * Sets the interval position factor.  This controls how the interval is
     * aligned to the x-value.  For a value of 0.5, the interval is aligned
     * with the x-value in the center.  For a value of 0.0, the interval is
     * aligned with the x-value at the lower end of the interval, and for a
     * value of 1.0, the interval is aligned with the x-value at the upper
     * end of the interval.
     * <br><br>
     * Note that changing the interval position factor amounts to changing the
     * data values represented by the dataset.  Therefore, the dataset that is
     * using this delegate is responsible for generating the
     * appropriate {@link DatasetChangeEvent}.
     *
     * @param d  the new interval position factor (in the range
     *           <code>0.0</code> to <code>1.0</code> inclusive).
     */
    public void setIntervalPositionFactor(double d) {
        if (d < 0.0 || 1.0 < d) {
            throw new IllegalArgumentException(
                    "Argument 'd' outside valid range.");
        }
        this.intervalPositionFactor = d;
    }

    /**
     * Returns the fixed interval width.
     *
     * @return The fixed interval width.
     */
    public double getFixedIntervalWidth() {
        return this.fixedIntervalWidth;
    }

    /**
     * Sets the fixed interval width and, as a side effect, sets the
     * <code>autoWidth</code> flag to <code>false</code>.
     * <br><br>
     * Note that changing the interval width amounts to changing the data
     * values represented by the dataset.  Therefore, the dataset
     * that is using this delegate is responsible for generating the
     * appropriate {@link DatasetChangeEvent}.
     *
     * @param w  the width (negative values not permitted).
     */
    public void setFixedIntervalWidth(double w) {
        if (w < 0.0) {
            throw new IllegalArgumentException("Negative 'w' argument.");
        }
        this.fixedIntervalWidth = w;
        this.autoWidth = false;
    }

    /**
     * Returns the interval width.  This method will return either the
     * auto calculated interval width or the manually specified interval
     * width, depending on the {@link #isAutoWidth()} result.
     *
     * @return The interval width to use.
     */
    public double getIntervalWidth() {
        if (isAutoWidth() && !Double.isInfinite(this.autoIntervalWidth)) {
            // everything is fine: autoWidth is on, and an autoIntervalWidth
            // was set.
            return this.autoIntervalWidth;
        }
        else {
            // either autoWidth is off or autoIntervalWidth was not set.
            return this.fixedIntervalWidth;
        }
    }

    /**
     * Returns the start value of the x-interval for an item within a series.
     *
     * @param series  the series index.
     * @param item  the item index.
     *
     * @return The start value of the x-interval (possibly <code>null</code>).
     *
     * @see #getStartXValue(int, int)
     */
    public Number getStartX(int series, int item) {
        Number startX = null;
        Number x = this.dataset.getX(series, item);
        if (x != null) {
            startX = new Double(x.doubleValue()
                     - (getIntervalPositionFactor() * getIntervalWidth()));
        }
        return startX;
    }

    /**
     * Returns the start value of the x-interval for an item within a series.
     *
     * @param series  the series index.
     * @param item  the item index.
     *
     * @return The start value of the x-interval.
     *
     * @see #getStartX(int, int)
     */
    public double getStartXValue(int series, int item) {
        return this.dataset.getXValue(series, item)
                - getIntervalPositionFactor() * getIntervalWidth();
    }

    /**
     * Returns the end value of the x-interval for an item within a series.
     *
     * @param series  the series index.
     * @param item  the item index.
     *
     * @return The end value of the x-interval (possibly <code>null</code>).
     *
     * @see #getEndXValue(int, int)
     */
    public Number getEndX(int series, int item) {
        Number endX = null;
        Number x = this.dataset.getX(series, item);
        if (x != null) {
            endX = new Double(x.doubleValue()
                + ((1.0 - getIntervalPositionFactor()) * getIntervalWidth()));
        }
        return endX;
    }

    /**
     * Returns the end value of the x-interval for an item within a series.
     *
     * @param series  the series index.
     * @param item  the item index.
     *
     * @return The end value of the x-interval.
     *
     * @see #getEndX(int, int)
     */
    public double getEndXValue(int series, int item) {
        return this.dataset.getXValue(series, item)
                + (1.0 - getIntervalPositionFactor()) * getIntervalWidth();
    }

    /**
     * Returns the minimum x-value in the dataset.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         x-interval is taken into account.
     *
     * @return The minimum value.
     */
    public double getDomainLowerBound(boolean includeInterval) {
        double result = Double.NaN;
        Range r = getDomainBounds(includeInterval);
        if (r != null) {
            result = r.getLowerBound();
        }
        return result;
    }

    /**
     * Returns the maximum x-value in the dataset.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         x-interval is taken into account.
     *
     * @return The maximum value.
     */
    public double getDomainUpperBound(boolean includeInterval) {
        double result = Double.NaN;
        Range r = getDomainBounds(includeInterval);
        if (r != null) {
            result = r.getUpperBound();
        }
        return result;
    }

    /**
     * Returns the range of the values in the dataset's domain, including
     * or excluding the interval around each x-value as specified.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         x-interval should be taken into account.
     *
     * @return The range.
     */
    public Range getDomainBounds(boolean includeInterval) {
        // first get the range without the interval, then expand it for the
        // interval width
        Range range = DatasetUtilities.findDomainBounds(this.dataset, false);
        if (includeInterval && range != null) {
            double lowerAdj = getIntervalWidth() * getIntervalPositionFactor();
            double upperAdj = getIntervalWidth() - lowerAdj;
            range = new Range(range.getLowerBound() - lowerAdj,
                range.getUpperBound() + upperAdj);
        }
        return range;
    }

    /**
     * Handles events from the dataset by recalculating the interval if
     * necessary.
     *
     * @param e  the event.
     */
    public void datasetChanged(DatasetChangeEvent e) {
        // TODO: by coding the event with some information about what changed
        // in the dataset, we could make the recalculation of the interval
        // more efficient in some cases (for instance, if the change is
        // just an update to a y-value, then the x-interval doesn't need
        // updating)...
        if (this.autoWidth) {
            this.autoIntervalWidth = recalculateInterval();
        }
    }

    /**
     * Recalculate the minimum width "from scratch".
     *
     * @return The minimum width.
     */
    private double recalculateInterval() {
        double result = Double.POSITIVE_INFINITY;
        int seriesCount = this.dataset.getSeriesCount();
        for (int series = 0; series < seriesCount; series++) {
            result = Math.min(result, calculateIntervalForSeries(series));
        }
        return result;
    }

    /**
     * Calculates the interval width for a given series.
     *
     * @param series  the series index.
     *
     * @return The interval width.
     */
    private double calculateIntervalForSeries(int series) {
        double result = Double.POSITIVE_INFINITY;
        int itemCount = this.dataset.getItemCount(series);
        if (itemCount > 1) {
            double prev = this.dataset.getXValue(series, 0);
            for (int item = 1; item < itemCount; item++) {
                double x = this.dataset.getXValue(series, item);
                result = Math.min(result, x - prev);
                prev = x;
            }
        }
        return result;
    }

    /**
     * Tests the delegate for equality with an arbitrary object.  The
     * equality test considers two delegates to be equal if they would
     * calculate the same intervals for any given dataset (for this reason, the
     * dataset itself is NOT included in the equality test, because it is just
     * a reference back to the current 'owner' of the delegate).
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof IntervalXYDelegate)) {
            return false;
        }
        IntervalXYDelegate that = (IntervalXYDelegate) obj;
        if (this.autoWidth != that.autoWidth) {
            return false;
        }
        if (this.intervalPositionFactor != that.intervalPositionFactor) {
            return false;
        }
        if (this.fixedIntervalWidth != that.fixedIntervalWidth) {
            return false;
        }
        return true;
    }

    /**
     * @return A clone of this delegate.
     *
     * @throws CloneNotSupportedException if the object cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code.
     */
    public int hashCode() {
        int hash = 5;
        hash = HashUtilities.hashCode(hash, this.autoWidth);
        hash = HashUtilities.hashCode(hash, this.intervalPositionFactor);
        hash = HashUtilities.hashCode(hash, this.fixedIntervalWidth);
        return hash;
    }

}
