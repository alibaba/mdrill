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
 * ---------------------
 * SubseriesDataset.java
 * ---------------------
 * (C) Copyright 2001-2009, by Bill Kelemen and Contributors.
 *
 * Original Author:  Bill Kelemen;
 * Contributor(s):   Sylvain Vieujot;
 *                   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 06-Dec-2001 : Version 1 (BK);
 * 05-Feb-2002 : Added SignalsDataset (and small change to HighLowDataset
 *               interface) as requested by Sylvain Vieujot (DG);
 * 28-Feb-2002 : Fixed bug: missing map[series] in IntervalXYDataset and
 *               SignalsDataset methods (BK);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 06-May-2004 : Now extends AbstractIntervalXYDataset (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 29-Nov-2005 : Removed SignalsDataset (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags from all over JFreeChart sources (DG);
 * 04-Feb-2009 : Deprecated, this class won't be supported in version
 *               1.2.0 (DG);
 *
 */

package org.jfree.data.general;

import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;

/**
 * This class will create a dataset with one or more series from another
 * {@link SeriesDataset}.
 *
 * @deprecated As of version 1.0.13.  This class will be removed from
 *     JFreeChart 1.2.0 onwards.  Anyone needing this facility will need to
 *     maintain it outside of JFreeChart.
 */
public class SubSeriesDataset extends AbstractIntervalXYDataset
        implements OHLCDataset, IntervalXYDataset, CombinationDataset {

    /** The parent dataset. */
    private SeriesDataset parent = null;

    /** Storage for map. */
    private int[] map;  // maps our series into our parent's

    /**
     * Creates a SubSeriesDataset using one or more series from
     * <code>parent</code>.  The series to use are passed as an array of int.
     *
     * @param parent  underlying dataset
     * @param map  int[] of series from parent to include in this Dataset
     */
    public SubSeriesDataset(SeriesDataset parent, int[] map) {
        this.parent = parent;
        this.map = map;
    }

    /**
     * Creates a SubSeriesDataset using one series from <code>parent</code>.
     * The series to is passed as an int.
     *
     * @param parent  underlying dataset
     * @param series  series from parent to include in this Dataset
     */
    public SubSeriesDataset(SeriesDataset parent, int series) {
        this(parent, new int[] {series});
    }

    ///////////////////////////////////////////////////////////////////////////
    // From HighLowDataset
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the high-value for the specified series and item.
     * <p>
     * Note: throws <code>ClassCastException</code> if the series if not from a
     * {@link OHLCDataset}.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The high-value for the specified series and item.
     */
    public Number getHigh(int series, int item) {
        return ((OHLCDataset) this.parent).getHigh(this.map[series], item);
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
     * <p>
     * Note: throws <code>ClassCastException</code> if the series if not from a
     * {@link OHLCDataset}.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The low-value for the specified series and item.
     */
    public Number getLow(int series, int item) {
        return ((OHLCDataset) this.parent).getLow(this.map[series], item);
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
     * <p>
     * Note: throws <code>ClassCastException</code> if the series if not from a
     * {@link OHLCDataset}.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The open-value for the specified series and item.
     */
    public Number getOpen(int series, int item) {
        return ((OHLCDataset) this.parent).getOpen(this.map[series], item);
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
     * <p>
     * Note: throws <code>ClassCastException</code> if the series if not from a
     * {@link OHLCDataset}.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The close-value for the specified series and item.
     */
    public Number getClose(int series, int item) {
        return ((OHLCDataset) this.parent).getClose(this.map[series], item);
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
     * Returns the volume.
     * <p>
     * Note: throws <code>ClassCastException</code> if the series if not from a
     * {@link OHLCDataset}.
     *
     * @param series  the series (zero based index).
     * @param item  the item (zero based index).
     *
     * @return The volume.
     */
    public Number getVolume(int series, int item) {
        return ((OHLCDataset) this.parent).getVolume(this.map[series], item);
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
    // From XYDataset
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the X-value for the specified series and item.
     * <p>
     * Note: throws <code>ClassCastException</code> if the series if not from a
     * {@link XYDataset}.
     *
     * @param series  the index of the series of interest (zero-based);
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The X-value for the specified series and item.
     */
    public Number getX(int series, int item) {
        return ((XYDataset) this.parent).getX(this.map[series], item);
    }

    /**
     * Returns the Y-value for the specified series and item.
     * <p>
     * Note: throws <code>ClassCastException</code> if the series if not from a
     * {@link XYDataset}.
     *
     * @param series  the index of the series of interest (zero-based).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The Y-value for the specified series and item.
     */
    public Number getY(int series, int item) {
        return ((XYDataset) this.parent).getY(this.map[series], item);
    }

    /**
     * Returns the number of items in a series.
     * <p>
     * Note: throws <code>ClassCastException</code> if the series if not from a
     * {@link XYDataset}.
     *
     * @param series  the index of the series of interest (zero-based).
     *
     * @return The number of items in a series.
     */
    public int getItemCount(int series) {
        return ((XYDataset) this.parent).getItemCount(this.map[series]);
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
        return this.map.length;
    }

    /**
     * Returns the key for a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The name of a series.
     */
    public Comparable getSeriesKey(int series) {
        return this.parent.getSeriesKey(this.map[series]);
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
     * @return The starting X value for the specified series and item.
     */
    public Number getStartX(int series, int item) {
        if (this.parent instanceof IntervalXYDataset) {
            return ((IntervalXYDataset) this.parent).getStartX(
                this.map[series], item
            );
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
     * @return The ending X value for the specified series and item.
     */
    public Number getEndX(int series, int item) {
        if (this.parent instanceof IntervalXYDataset) {
            return ((IntervalXYDataset) this.parent).getEndX(
                this.map[series], item
            );
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
        if (this.parent instanceof IntervalXYDataset) {
            return ((IntervalXYDataset) this.parent).getStartY(
                this.map[series], item
            );
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
    public Number getEndY(int series,  int item) {
        if (this.parent instanceof IntervalXYDataset) {
            return ((IntervalXYDataset) this.parent).getEndY(
                this.map[series], item
            );
        }
        else {
            return getY(series, item);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // New methods from CombinationDataset
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the parent Dataset of this combination.
     *
     * @return The parent Dataset of this combination.
     */
    public SeriesDataset getParent() {
        return this.parent;
    }

    /**
     * Returns a map or indirect indexing form our series into parent's series.
     *
     * @return A map or indirect indexing form our series into parent's series.
     */
    public int[] getMap() {
        return (int[]) this.map.clone();
    }

}
