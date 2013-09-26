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
 * ------------------------
 * XYItemRendererState.java
 * ------------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Ulrich Voigt;
 *                   Greg Darke;
 *
 * Changes:
 * --------
 * 07-Oct-2003 : Version 1 (DG);
 * 27-Jan-2004 : Added workingLine attribute (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 04-May-2007 : Added processVisibleItemsOnly flag (DG);
 * 09-Jul-2008 : Added start/endSeriesPass() methods - see patch 1997549 by
 *               Ulrich Voigt (DG);
 * 19-Sep-2008 : Added first and last item indices, based on patch by Greg
 *               Darke (DG);
 *
 */

package org.jfree.chart.renderer.xy;

import java.awt.geom.Line2D;

import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.RendererState;
import org.jfree.data.xy.XYDataset;

/**
 * The state for an {@link XYItemRenderer}.
 */
public class XYItemRendererState extends RendererState {

    /**
     * The first item in the series that will be displayed.
     *
     * @since 1.0.11
     */
    private int firstItemIndex;

    /**
     * The last item in the current series that will be displayed.
     *
     * @since 1.0.11
     */
    private int lastItemIndex;

    /**
     * A line object that the renderer can reuse to save instantiating a lot
     * of objects.
     */
    public Line2D workingLine;

    /**
     * A flag that controls whether the plot should pass ALL data items to the
     * renderer, or just the items that will be visible.
     *
     * @since 1.0.6
     */
    private boolean processVisibleItemsOnly;

    /**
     * Creates a new state.
     *
     * @param info  the plot rendering info.
     */
    public XYItemRendererState(PlotRenderingInfo info) {
        super(info);
        this.workingLine = new Line2D.Double();
        this.processVisibleItemsOnly = true;
    }

    /**
     * Returns the flag that controls whether the plot passes all data
     * items in each series to the renderer, or just the visible items.  The
     * default value is <code>true</code>.
     *
     * @return A boolean.
     *
     * @since 1.0.6
     *
     * @see #setProcessVisibleItemsOnly(boolean)
     */
    public boolean getProcessVisibleItemsOnly() {
        return this.processVisibleItemsOnly;
    }

    /**
     * Sets the flag that controls whether the plot passes all data
     * items in each series to the renderer, or just the visible items.
     *
     * @param flag  the new flag value.
     *
     * @since 1.0.6
     */
    public void setProcessVisibleItemsOnly(boolean flag) {
        this.processVisibleItemsOnly = flag;
    }

    /**
     * Returns the first item index (this is updated with each call to
     * {@link #startSeriesPass(XYDataset, int, int, int, int, int)}.
     *
     * @return The first item index.
     *
     * @since 1.0.11
     */
    public int getFirstItemIndex() {
        return this.firstItemIndex;
    }

    /**
     * Returns the last item index (this is updated with each call to
     * {@link #startSeriesPass(XYDataset, int, int, int, int, int)}.
     *
     * @return The last item index.
     *
     * @since 1.0.11
     */
    public int getLastItemIndex() {
        return this.lastItemIndex;
    }

    /**
     * This method is called by the {@link XYPlot} when it starts a pass
     * through the (visible) items in a series.  The default implementation
     * records the first and last item indices - override this method to
     * implement additional specialised behaviour.
     *
     * @param dataset  the dataset.
     * @param series  the series index.
     * @param firstItem  the index of the first item in the series.
     * @param lastItem  the index of the last item in the series.
     * @param pass  the pass index.
     * @param passCount  the number of passes.
     *
     * @see #endSeriesPass(XYDataset, int, int, int, int, int)
     *
     * @since 1.0.11
     */
    public void startSeriesPass(XYDataset dataset, int series, int firstItem,
            int lastItem, int pass, int passCount) {
        this.firstItemIndex = firstItem;
        this.lastItemIndex = lastItem;
    }

    /**
     * This method is called by the {@link XYPlot} when it ends a pass
     * through the (visible) items in a series.  The default implementation
     * does nothing, but you can override this method to implement specialised
     * behaviour.
     *
     * @param dataset  the dataset.
     * @param series  the series index.
     * @param firstItem  the index of the first item in the series.
     * @param lastItem  the index of the last item in the series.
     * @param pass  the pass index.
     * @param passCount  the number of passes.
     *
     * @see #startSeriesPass(XYDataset, int, int, int, int, int)
     *
     * @since 1.0.11
     */
    public void endSeriesPass(XYDataset dataset, int series, int firstItem,
            int lastItem, int pass, int passCount) {
        // do nothing...this is just a hook for subclasses
    }

}
