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
 * --------------------
 * CombinedDataset.java
 * --------------------
 * (C) Copyright 2001-2009, by Bill Kelemen and Contributors.
 *
 * Original Author:  Bill Kelemen;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 06-Dec-2001 : Version 1 (BK);
 * 27-Dec-2001 : Fixed bug in getChildPosition method (BK);
 * 29-Dec-2001 : Fixed bug in getChildPosition method with complex
 *               CombinePlot (BK);
 * 05-Feb-2002 : Small addition to the interface HighLowDataset, as requested
 *               by Sylvain Vieujot (DG);
 * 14-Feb-2002 : Added bug fix for IntervalXYDataset methods, submitted by
 *               Gyula Kun-Szabo (DG);
 * 11-Jun-2002 : Updated for change in event constructor (DG);
 * 04-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 06-May-2004 : Now extends AbstractIntervalXYDataset and added other methods
 *               that return double primitives (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags from all over JFreeChart sources (DG);
 * 04-Feb-2009 : Deprecated the class (DG);
 * 
 */

package org.jfree.data.general;

import java.util.List;

import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;

/**
 * This class can combine instances of {@link XYDataset}, {@link OHLCDataset}
 * and {@link IntervalXYDataset} together exposing the union of all the series
 * under one dataset.
 *
 * @deprecated As of version 1.0.13.  This class will be removed from
 *     JFreeChart 1.2.0 onwards.  Anyone needing this facility will need to
 *     maintain it outside of JFreeChart.
 */
public class CombinedDataset extends AbstractIntervalXYDataset
        implements XYDataset, OHLCDataset, IntervalXYDataset,
        CombinationDataset {

    /** Storage for the datasets we combine. */
    private List datasetInfo = new java.util.ArrayList();

    /**
     * Default constructor for an empty combination.
     */
    public CombinedDataset() {
        super();
    }

    /**
     * Creates a CombinedDataset initialized with an array of SeriesDatasets.
     *
     * @param data  array of SeriesDataset that contains the SeriesDatasets to
     *              combine.
     */
    public CombinedDataset(SeriesDataset[] data) {
        add(data);
    }

    /**
     * Adds one SeriesDataset to the combination. Listeners are notified of the
     * change.
     *
     * @param data  the SeriesDataset to add.
     */
    public void add(SeriesDataset data) {
        fastAdd(data);
        DatasetChangeEvent event = new DatasetChangeEvent(this, this);
        notifyListeners(event);
    }

    /**
     * Adds an array of SeriesDataset's to the combination. Listeners are
     * notified of the change.
     *
     * @param data  array of SeriesDataset to add
     */
    public void add(SeriesDataset[] data) {

        for (int i = 0; i < data.length; i++) {
            fastAdd(data[i]);
        }
        DatasetChangeEvent event = new DatasetChangeEvent(this, this);
        notifyListeners(event);

    }

    /**
     * Adds one series from a SeriesDataset to the combination. Listeners are
     * notified of the change.
     *
     * @param data  the SeriesDataset where series is contained
     * @param series  series to add
     */
    public void add(SeriesDataset data, int series) {
        add(new SubSeriesDataset(data, series));
    }

    /**
     * Fast add of a SeriesDataset. Does not notify listeners of the change.
     *
     * @param data  SeriesDataset to add
     */
    private void fastAdd(SeriesDataset data) {
        for (int i = 0; i < data.getSeriesCount(); i++) {
            this.datasetInfo.add(new DatasetInfo(data, i));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // From SeriesDataset
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the number of series in the dataset.
     *
     * @return The number of series in the dataset.
     */
    public int getSeriesCount() {
        return this.datasetInfo.size();
    }

    /**
     * Returns the key for a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The key for a series.
     */
    public Comparable getSeriesKey(int series) {
        DatasetInfo di = getDatasetInfo(series);
        return di.data.getSeriesKey(di.series);
    }

    ///////////////////////////////////////////////////////////////////////////
    // From XYDataset
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the X-value for the specified series and item.
     * <P>
     * Note:  throws <code>ClassCastException</code> if the series is not from
     * a {@link XYDataset}.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The X-value for the specified series and item.
     */
    public Number getX(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        return ((XYDataset) di.data).getX(di.series, item);
    }

    /**
     * Returns the Y-value for the specified series and item.
     * <P>
     * Note:  throws <code>ClassCastException</code> if the series is not from
     * a {@link XYDataset}.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The Y-value for the specified series and item.
     */
    public Number getY(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        return ((XYDataset) di.data).getY(di.series, item);
    }

    /**
     * Returns the number of items in a series.
     * <P>
     * Note:  throws <code>ClassCastException</code> if the series is not from
     * a {@link XYDataset}.
     *
     * @param series  the index of the series of interest (zero-based).
     *
     * @return The number of items in a series.
     */
    public int getItemCount(int series) {
        DatasetInfo di = getDatasetInfo(series);
        return ((XYDataset) di.data).getItemCount(di.series);
    }

    ///////////////////////////////////////////////////////////////////////////
    // From HighLowDataset
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the high-value for the specified series and item.
     * <P>
     * Note:  throws <code>ClassCastException</code> if the series is not from a
     * {@link OHLCDataset}.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The high-value for the specified series and item.
     */
    public Number getHigh(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        return ((OHLCDataset) di.data).getHigh(di.series, item);
    }

    /**
     * Returns the high-value (as a double primitive) for an item within a
     * series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The high-value.
     */
    public double getHighValue(int series, int item) {
        double result = Double.NaN;
        Number high = getHigh(series, item);
        if (high != null) {
            result = high.doubleValue();
        }
        return result;
    }

    /**
     * Returns the low-value for the specified series and item.
     * <P>
     * Note:  throws <code>ClassCastException</code> if the series is not from a
     * {@link OHLCDataset}.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The low-value for the specified series and item.
     */
    public Number getLow(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        return ((OHLCDataset) di.data).getLow(di.series, item);
    }

    /**
     * Returns the low-value (as a double primitive) for an item within a
     * series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The low-value.
     */
    public double getLowValue(int series, int item) {
        double result = Double.NaN;
        Number low = getLow(series, item);
        if (low != null) {
            result = low.doubleValue();
        }
        return result;
    }

    /**
     * Returns the open-value for the specified series and item.
     * <P>
     * Note:  throws <code>ClassCastException</code> if the series is not from a
     * {@link OHLCDataset}.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The open-value for the specified series and item.
     */
    public Number getOpen(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        return ((OHLCDataset) di.data).getOpen(di.series, item);
    }

    /**
     * Returns the open-value (as a double primitive) for an item within a
     * series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The open-value.
     */
    public double getOpenValue(int series, int item) {
        double result = Double.NaN;
        Number open = getOpen(series, item);
        if (open != null) {
            result = open.doubleValue();
        }
        return result;
    }

    /**
     * Returns the close-value for the specified series and item.
     * <P>
     * Note:  throws <code>ClassCastException</code> if the series is not from a
     * {@link OHLCDataset}.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The close-value for the specified series and item.
     */
    public Number getClose(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        return ((OHLCDataset) di.data).getClose(di.series, item);
    }

    /**
     * Returns the close-value (as a double primitive) for an item within a
     * series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The close-value.
     */
    public double getCloseValue(int series, int item) {
        double result = Double.NaN;
        Number close = getClose(series, item);
        if (close != null) {
            result = close.doubleValue();
        }
        return result;
    }

    /**
     * Returns the volume value for the specified series and item.
     * <P>
     * Note:  throws <code>ClassCastException</code> if the series is not from a
     * {@link OHLCDataset}.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The volume value for the specified series and item.
     */
    public Number getVolume(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        return ((OHLCDataset) di.data).getVolume(di.series, item);
    }

    /**
     * Returns the volume-value (as a double primitive) for an item within a
     * series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The volume-value.
     */
    public double getVolumeValue(int series, int item) {
        double result = Double.NaN;
        Number volume = getVolume(series, item);
        if (volume != null) {
            result = volume.doubleValue();
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////
    // From IntervalXYDataset
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the starting X value for the specified series and item.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The value.
     */
    public Number getStartX(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        if (di.data instanceof IntervalXYDataset) {
            return ((IntervalXYDataset) di.data).getStartX(di.series, item);
        }
        else {
            return getX(series, item);
        }
    }

    /**
     * Returns the ending X value for the specified series and item.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The value.
     */
    public Number getEndX(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        if (di.data instanceof IntervalXYDataset) {
            return ((IntervalXYDataset) di.data).getEndX(di.series, item);
        }
        else {
            return getX(series, item);
        }
    }

    /**
     * Returns the starting Y value for the specified series and item.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The starting Y value for the specified series and item.
     */
    public Number getStartY(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        if (di.data instanceof IntervalXYDataset) {
            return ((IntervalXYDataset) di.data).getStartY(di.series, item);
        }
        else {
            return getY(series, item);
        }
    }

    /**
     * Returns the ending Y value for the specified series and item.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The ending Y value for the specified series and item.
     */
    public Number getEndY(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        if (di.data instanceof IntervalXYDataset) {
            return ((IntervalXYDataset) di.data).getEndY(di.series, item);
        }
        else {
            return getY(series, item);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // New methods from CombinationDataset
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the parent Dataset of this combination. If there is more than
     * one parent, or a child is found that is not a CombinationDataset, then
     * returns <code>null</code>.
     *
     * @return The parent Dataset of this combination or <code>null</code>.
     */
    public SeriesDataset getParent() {

        SeriesDataset parent = null;
        for (int i = 0; i < this.datasetInfo.size(); i++) {
            SeriesDataset child = getDatasetInfo(i).data;
            if (child instanceof CombinationDataset) {
                SeriesDataset childParent
                    = ((CombinationDataset) child).getParent();
                if (parent == null) {
                    parent = childParent;
                }
                else if (parent != childParent) {
                    return null;
                }
            }
            else {
                return null;
            }
        }
        return parent;

    }

    /**
     * Returns a map or indirect indexing form our series into parent's series.
     * Prior to calling this method, the client should check getParent() to make
     * sure the CombinationDataset uses the same parent. If not, the map
     * returned by this method will be invalid or null.
     *
     * @return A map or indirect indexing form our series into parent's series.
     *
     * @see #getParent()
     */
    public int[] getMap() {

        int[] map = null;
        for (int i = 0; i < this.datasetInfo.size(); i++) {
            SeriesDataset child = getDatasetInfo(i).data;
            if (child instanceof CombinationDataset) {
                int[] childMap = ((CombinationDataset) child).getMap();
                if (childMap == null) {
                    return null;
                }
                map = joinMap(map, childMap);
            }
            else {
                return null;
            }
        }
        return map;
    }

    ///////////////////////////////////////////////////////////////////////////
    // New Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the child position.
     *
     * @param child  the child dataset.
     *
     * @return The position.
     */
    public int getChildPosition(Dataset child) {

        int n = 0;
        for (int i = 0; i < this.datasetInfo.size(); i++) {
            SeriesDataset childDataset = getDatasetInfo(i).data;
            if (childDataset instanceof CombinedDataset) {
                int m = ((CombinedDataset) childDataset)
                    .getChildPosition(child);
                if (m >= 0) {
                    return n + m;
                }
                n++;
            }
            else {
                if (child == childDataset) {
                    return n;
                }
                n++;
            }
        }
        return -1;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the DatasetInfo object associated with the series.
     *
     * @param series  the index of the series.
     *
     * @return The DatasetInfo object associated with the series.
     */
    private DatasetInfo getDatasetInfo(int series) {
        return (DatasetInfo) this.datasetInfo.get(series);
    }

    /**
     * Joins two map arrays (int[]) together.
     *
     * @param a  the first array.
     * @param b  the second array.
     *
     * @return A copy of { a[], b[] }.
     */
    private int[] joinMap(int[] a, int[] b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        int[] result = new int[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Private class to store as pairs (SeriesDataset, series) for all combined
     * series.
     */
    private class DatasetInfo {

        /** The dataset. */
        private SeriesDataset data;

        /** The series. */
        private int series;

        /**
         * Creates a new dataset info record.
         *
         * @param data  the dataset.
         * @param series  the series.
         */
        DatasetInfo(SeriesDataset data, int series) {
            this.data = data;
            this.series = series;
        }
    }

}
