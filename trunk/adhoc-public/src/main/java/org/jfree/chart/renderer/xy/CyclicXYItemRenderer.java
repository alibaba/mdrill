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
 * CyclicXYItemRenderer.java
 * ---------------------------
 * (C) Copyright 2003-2008, by Nicolas Brodu and Contributors.
 *
 * Original Author:  Nicolas Brodu;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 19-Nov-2003 : Initial import to JFreeChart from the JSynoptic project (NB);
 * 23-Dec-2003 : Added missing Javadocs (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * ------------- JFREECHART 1.0.0 ---------------------------------------------
 * 06-Jul-2006 : Modified to call only dataset methods that return double
 *               primitives (DG);
 *
 */

package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.CyclicNumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;

/**
 * The Cyclic XY item renderer is specially designed to handle cyclic axis.
 * While the standard renderer would draw a line across the plot when a cycling
 * occurs, the cyclic renderer splits the line at each cycle end instead. This
 * is done by interpolating new points at cycle boundary. Thus, correct
 * appearance is restored.
 *
 * The Cyclic XY item renderer works exactly like a standard XY item renderer
 * with non-cyclic axis.
 */
public class CyclicXYItemRenderer extends StandardXYItemRenderer
                                  implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 4035912243303764892L;

    /**
     * Default constructor.
     */
    public CyclicXYItemRenderer() {
        super();
    }

    /**
     * Creates a new renderer.
     *
     * @param type  the renderer type.
     */
    public CyclicXYItemRenderer(int type) {
        super(type);
    }

    /**
     * Creates a new renderer.
     *
     * @param type  the renderer type.
     * @param labelGenerator  the tooltip generator.
     */
    public CyclicXYItemRenderer(int type, XYToolTipGenerator labelGenerator) {
        super(type, labelGenerator);
    }

    /**
     * Creates a new renderer.
     *
     * @param type  the renderer type.
     * @param labelGenerator  the tooltip generator.
     * @param urlGenerator  the url generator.
     */
    public CyclicXYItemRenderer(int type,
                                XYToolTipGenerator labelGenerator,
                                XYURLGenerator urlGenerator) {
        super(type, labelGenerator, urlGenerator);
    }


    /**
     * Draws the visual representation of a single data item.
     * When using cyclic axis, do not draw a line from right to left when
     * cycling as would a standard XY item renderer, but instead draw a line
     * from the previous point to the cycle bound in the last cycle, and a line
     * from the cycle bound to current point in the current cycle.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the data area.
     * @param info  the plot rendering info.
     * @param plot  the plot.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index.
     * @param item  the item index.
     * @param crosshairState  crosshair information for the plot
     *                        (<code>null</code> permitted).
     * @param pass  the current pass index.
     */
    public void drawItem(Graphics2D g2,
                         XYItemRendererState state,
                         Rectangle2D dataArea,
                         PlotRenderingInfo info,
                         XYPlot plot,
                         ValueAxis domainAxis,
                         ValueAxis rangeAxis,
                         XYDataset dataset,
                         int series,
                         int item,
                         CrosshairState crosshairState,
                         int pass) {

        if ((!getPlotLines()) || ((!(domainAxis instanceof CyclicNumberAxis))
                && (!(rangeAxis instanceof CyclicNumberAxis))) || (item <= 0)) {
            super.drawItem(g2, state, dataArea, info, plot, domainAxis,
                    rangeAxis, dataset, series, item, crosshairState, pass);
            return;
        }

        // get the previous data point...
        double xn = dataset.getXValue(series, item - 1);
        double yn = dataset.getYValue(series, item - 1);
        // If null, don't draw line => then delegate to parent
        if (Double.isNaN(yn)) {
            super.drawItem(g2, state, dataArea, info, plot, domainAxis,
                    rangeAxis, dataset, series, item, crosshairState, pass);
            return;
        }
        double[] x = new double[2];
        double[] y = new double[2];
        x[0] = xn;
        y[0] = yn;

        // get the data point...
        xn = dataset.getXValue(series, item);
        yn = dataset.getYValue(series, item);
        // If null, don't draw line at all
        if (Double.isNaN(yn)) {
            return;
        }
        x[1] = xn;
        y[1] = yn;

        // Now split the segment as needed
        double xcycleBound = Double.NaN;
        double ycycleBound = Double.NaN;
        boolean xBoundMapping = false, yBoundMapping = false;
        CyclicNumberAxis cnax = null, cnay = null;

        if (domainAxis instanceof CyclicNumberAxis) {
            cnax = (CyclicNumberAxis) domainAxis;
            xcycleBound = cnax.getCycleBound();
            xBoundMapping = cnax.isBoundMappedToLastCycle();
            // If the segment must be splitted, insert a new point
            // Strict test forces to have real segments (not 2 equal points)
            // and avoids division by 0
            if ((x[0] != x[1])
                    && ((xcycleBound >= x[0])
                    && (xcycleBound <= x[1])
                    || (xcycleBound >= x[1])
                    && (xcycleBound <= x[0]))) {
                double[] nx = new double[3];
                double[] ny = new double[3];
                nx[0] = x[0]; nx[2] = x[1]; ny[0] = y[0]; ny[2] = y[1];
                nx[1] = xcycleBound;
                ny[1] = (y[1] - y[0]) * (xcycleBound - x[0])
                        / (x[1] - x[0]) + y[0];
                x = nx; y = ny;
            }
        }

        if (rangeAxis instanceof CyclicNumberAxis) {
            cnay = (CyclicNumberAxis) rangeAxis;
            ycycleBound = cnay.getCycleBound();
            yBoundMapping = cnay.isBoundMappedToLastCycle();
            // The split may occur in either x splitted segments, if any, but
            // not in both
            if ((y[0] != y[1]) && ((ycycleBound >= y[0])
                    && (ycycleBound <= y[1])
                    || (ycycleBound >= y[1]) && (ycycleBound <= y[0]))) {
                double[] nx = new double[x.length + 1];
                double[] ny = new double[y.length + 1];
                nx[0] = x[0]; nx[2] = x[1]; ny[0] = y[0]; ny[2] = y[1];
                ny[1] = ycycleBound;
                nx[1] = (x[1] - x[0]) * (ycycleBound - y[0])
                        / (y[1] - y[0]) + x[0];
                if (x.length == 3) {
                    nx[3] = x[2]; ny[3] = y[2];
                }
                x = nx; y = ny;
            }
            else if ((x.length == 3) && (y[1] != y[2]) && ((ycycleBound >= y[1])
                    && (ycycleBound <= y[2])
                    || (ycycleBound >= y[2]) && (ycycleBound <= y[1]))) {
                double[] nx = new double[4];
                double[] ny = new double[4];
                nx[0] = x[0]; nx[1] = x[1]; nx[3] = x[2];
                ny[0] = y[0]; ny[1] = y[1]; ny[3] = y[2];
                ny[2] = ycycleBound;
                nx[2] = (x[2] - x[1]) * (ycycleBound - y[1])
                        / (y[2] - y[1]) + x[1];
                x = nx; y = ny;
            }
        }

        // If the line is not wrapping, then parent is OK
        if (x.length == 2) {
            super.drawItem(g2, state, dataArea, info, plot, domainAxis,
                    rangeAxis, dataset, series, item, crosshairState, pass);
            return;
        }

        OverwriteDataSet newset = new OverwriteDataSet(x, y, dataset);

        if (cnax != null) {
            if (xcycleBound == x[0]) {
                cnax.setBoundMappedToLastCycle(x[1] <= xcycleBound);
            }
            if (xcycleBound == x[1]) {
                cnax.setBoundMappedToLastCycle(x[0] <= xcycleBound);
            }
        }
        if (cnay != null) {
            if (ycycleBound == y[0]) {
                cnay.setBoundMappedToLastCycle(y[1] <= ycycleBound);
            }
            if (ycycleBound == y[1]) {
                cnay.setBoundMappedToLastCycle(y[0] <= ycycleBound);
            }
        }
        super.drawItem(
            g2, state, dataArea, info, plot, domainAxis, rangeAxis,
            newset, series, 1, crosshairState, pass
        );

        if (cnax != null) {
            if (xcycleBound == x[1]) {
                cnax.setBoundMappedToLastCycle(x[2] <= xcycleBound);
            }
            if (xcycleBound == x[2]) {
                cnax.setBoundMappedToLastCycle(x[1] <= xcycleBound);
            }
        }
        if (cnay != null) {
            if (ycycleBound == y[1]) {
                cnay.setBoundMappedToLastCycle(y[2] <= ycycleBound);
            }
            if (ycycleBound == y[2]) {
                cnay.setBoundMappedToLastCycle(y[1] <= ycycleBound);
            }
        }
        super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis,
                newset, series, 2, crosshairState, pass);

        if (x.length == 4) {
            if (cnax != null) {
                if (xcycleBound == x[2]) {
                    cnax.setBoundMappedToLastCycle(x[3] <= xcycleBound);
                }
                if (xcycleBound == x[3]) {
                    cnax.setBoundMappedToLastCycle(x[2] <= xcycleBound);
                }
            }
            if (cnay != null) {
                if (ycycleBound == y[2]) {
                    cnay.setBoundMappedToLastCycle(y[3] <= ycycleBound);
                }
                if (ycycleBound == y[3]) {
                    cnay.setBoundMappedToLastCycle(y[2] <= ycycleBound);
                }
            }
            super.drawItem(g2, state, dataArea, info, plot, domainAxis,
                    rangeAxis, newset, series, 3, crosshairState, pass);
        }

        if (cnax != null) {
            cnax.setBoundMappedToLastCycle(xBoundMapping);
        }
        if (cnay != null) {
            cnay.setBoundMappedToLastCycle(yBoundMapping);
        }
    }

    /**
     * A dataset to hold the interpolated points when drawing new lines.
     */
    protected static class OverwriteDataSet implements XYDataset {

        /** The delegate dataset. */
        protected XYDataset delegateSet;

        /** Storage for the x and y values. */
        Double[] x, y;

        /**
         * Creates a new dataset.
         *
         * @param x  the x values.
         * @param y  the y values.
         * @param delegateSet  the dataset.
         */
        public OverwriteDataSet(double [] x, double[] y,
                                XYDataset delegateSet) {
            this.delegateSet = delegateSet;
            this.x = new Double[x.length]; this.y = new Double[y.length];
            for (int i = 0; i < x.length; ++i) {
                this.x[i] = new Double(x[i]);
                this.y[i] = new Double(y[i]);
            }
        }

        /**
         * Returns the order of the domain (X) values.
         *
         * @return The domain order.
         */
        public DomainOrder getDomainOrder() {
            return DomainOrder.NONE;
        }

        /**
         * Returns the number of items for the given series.
         *
         * @param series  the series index (zero-based).
         *
         * @return The item count.
         */
        public int getItemCount(int series) {
            return this.x.length;
        }

        /**
         * Returns the x-value.
         *
         * @param series  the series index (zero-based).
         * @param item  the item index (zero-based).
         *
         * @return The x-value.
         */
        public Number getX(int series, int item) {
            return this.x[item];
        }

        /**
         * Returns the x-value (as a double primitive) for an item within a
         * series.
         *
         * @param series  the series (zero-based index).
         * @param item  the item (zero-based index).
         *
         * @return The x-value.
         */
        public double getXValue(int series, int item) {
            double result = Double.NaN;
            Number x = getX(series, item);
            if (x != null) {
                result = x.doubleValue();
            }
            return result;
        }

        /**
         * Returns the y-value.
         *
         * @param series  the series index (zero-based).
         * @param item  the item index (zero-based).
         *
         * @return The y-value.
         */
        public Number getY(int series, int item) {
            return this.y[item];
        }

        /**
         * Returns the y-value (as a double primitive) for an item within a
         * series.
         *
         * @param series  the series (zero-based index).
         * @param item  the item (zero-based index).
         *
         * @return The y-value.
         */
        public double getYValue(int series, int item) {
            double result = Double.NaN;
            Number y = getY(series, item);
            if (y != null) {
                result = y.doubleValue();
            }
            return result;
        }

        /**
         * Returns the number of series in the dataset.
         *
         * @return The series count.
         */
        public int getSeriesCount() {
            return this.delegateSet.getSeriesCount();
        }

        /**
         * Returns the name of the given series.
         *
         * @param series  the series index (zero-based).
         *
         * @return The series name.
         */
        public Comparable getSeriesKey(int series) {
            return this.delegateSet.getSeriesKey(series);
        }

        /**
         * Returns the index of the named series, or -1.
         *
         * @param seriesName  the series name.
         *
         * @return The index.
         */
        public int indexOf(Comparable seriesName) {
            return this.delegateSet.indexOf(seriesName);
        }

        /**
         * Does nothing.
         *
         * @param listener  ignored.
         */
        public void addChangeListener(DatasetChangeListener listener) {
            // unused in parent
        }

        /**
         * Does nothing.
         *
         * @param listener  ignored.
         */
        public void removeChangeListener(DatasetChangeListener listener) {
            // unused in parent
        }

        /**
         * Returns the dataset group.
         *
         * @return The dataset group.
         */
        public DatasetGroup getGroup() {
            // unused but must return something, so while we are at it...
            return this.delegateSet.getGroup();
        }

        /**
         * Does nothing.
         *
         * @param group  ignored.
         */
        public void setGroup(DatasetGroup group) {
            // unused in parent
        }

    }

}


