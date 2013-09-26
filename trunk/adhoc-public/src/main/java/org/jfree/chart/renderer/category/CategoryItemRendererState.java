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
 * ------------------------------
 * CategoryItemRendererState.java
 * ------------------------------
 * (C) Copyright 2003-2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Peter Kolb (patch 2497611);
 *
 * Changes (since 20-Oct-2003):
 * ----------------------------
 * 20-Oct-2003 : Added series running total (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 01-Dec-2006 : Updated API docs (DG);
 * 26-Jun-2008 : Added CrosshairState (DG);
 * 14-Jan-2009 : Added visibleSeries[] array (PK);
 * 04-Feb-2009 : Added getVisibleSeriesArray() method (DG);
 *
 */

package org.jfree.chart.renderer.category;

import org.jfree.chart.plot.CategoryCrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.renderer.RendererState;

/**
 * An object that retains temporary state information for a
 * {@link CategoryItemRenderer}.
 */
public class CategoryItemRendererState extends RendererState {

    /** The bar width. */
    private double barWidth;

    /** The series running total. */
    private double seriesRunningTotal;

    /** The array with the indices of the visible series.*/
    private int[] visibleSeries;

    /**
     * State information for crosshairs in the plot (this is updated by the
     * renderer, but may be passed to several renderers in one chart).
     *
     * @since 1.0.11
     */
    private CategoryCrosshairState crosshairState;

    /**
     * Creates a new object for recording temporary state information for a
     * renderer.
     *
     * @param info  the plot rendering info (<code>null</code> permitted).
     */
    public CategoryItemRendererState(PlotRenderingInfo info) {
        super(info);
        this.barWidth = 0.0;
        this.seriesRunningTotal = 0.0;
    }

    /**
     * Returns the bar width.
     *
     * @return The bar width.
     *
     * @see #setBarWidth(double)
     */
    public double getBarWidth() {
        return this.barWidth;
    }

    /**
     * Sets the bar width.  The renderer calculates this value and stores it
     * here - it is not intended that users can manually set the bar width.
     *
     * @param width  the width.
     *
     * @see #getBarWidth()
     */
    public void setBarWidth(double width) {
        this.barWidth = width;
    }

    /**
     * Returns the series running total.
     *
     * @return The running total.
     *
     * @see #setSeriesRunningTotal(double)
     */
    public double getSeriesRunningTotal() {
        return this.seriesRunningTotal;
    }

    /**
     * Sets the series running total (this method is intended for the use of
     * the renderer only).
     *
     * @param total  the new total.
     *
     * @see #getSeriesRunningTotal()
     */
    void setSeriesRunningTotal(double total) {
        this.seriesRunningTotal = total;
    }

    /**
     * Returns the crosshair state, if any.
     *
     * @return The crosshair state (possibly <code>null</code>).
     *
     * @since 1.0.11
     *
     * @see #setCrosshairState(CategoryCrosshairState)
     */
    public CategoryCrosshairState getCrosshairState() {
        return this.crosshairState;
    }

    /**
     * Sets the crosshair state.
     *
     * @param state  the new state (<code>null</code> permitted).
     *
     * @since 1.0.11
     *
     * @see #getCrosshairState()
     */
    public void setCrosshairState(CategoryCrosshairState state) {
        this.crosshairState = state;
    }

    /**
     * Returns the index of the row relative to the visible rows.  If no
     * visible rows have been specified, the original row index is returned.
     * If the row index is not included in the array of visible rows,
     * -1 is returned.
     *
     * @param rowIndex  the row index.
     *
     * @return The new row index or -1.
     *
     * @since 1.0.13
     */
    public int getVisibleSeriesIndex(int rowIndex) {
    	if (this.visibleSeries == null) {
    	    return rowIndex;
    	}
		int index = -1;
		for (int vRow = 0; vRow < this.visibleSeries.length ; vRow++){
			if (this.visibleSeries[vRow] == rowIndex) {
				index = vRow;
				break;
			}
		}
		return index;
    }

    /**
     * Returns the number of visible series or -1 if no visible series have
     * been specified.
     *
     * @return The number or -1.
     *
     * @since 1.0.13
     */
    public int getVisibleSeriesCount() {
    	if (this.visibleSeries == null) {
    	    return -1;
    	}
    	return this.visibleSeries.length;
    }

    /**
     * Returns a copy of the visible series array.
     * 
     * @return The visible series array (possibly <code>null</code>).
     * 
     * @since 1.0.13
     */
    public int[] getVisibleSeriesArray() {
        if (this.visibleSeries == null) {
            return null;
        }
        int[] result = new int[this.visibleSeries.length];
        System.arraycopy(this.visibleSeries, 0, result, 0,
                this.visibleSeries.length);
        return result;
    }

    /**
     * Sets an array with the indices of the visible rows.
     *
     * @param visibleSeries the array (<code>null</code> permitted).
     *
     * @since 1.0.13
     */
    public void setVisibleSeriesArray(int[] visibleSeries) {
        this.visibleSeries = visibleSeries;
    }

}
