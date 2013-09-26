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
 * -------------------------
 * XYDifferenceRenderer.java
 * -------------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Richard West, Advanced Micro Devices, Inc. (major rewrite
 *                   of difference drawing algorithm);
 *
 * Changes:
 * --------
 * 30-Apr-2003 : Version 1 (DG);
 * 30-Jul-2003 : Modified entity constructor (CZ);
 * 20-Aug-2003 : Implemented Cloneable and PublicCloneable (DG);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 09-Feb-2004 : Updated to support horizontal plot orientation (DG);
 * 10-Feb-2004 : Added default constructor, setter methods and updated
 *               Javadocs (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState (DG);
 * 30-Mar-2004 : Fixed bug in getNegativePaint() method (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 25-Aug-2004 : Fixed a bug preventing the use of crosshairs (DG);
 * 11-Nov-2004 : Now uses ShapeUtilities to translate shapes (DG);
 * 19-Jan-2005 : Now accesses only primitive values from dataset (DG);
 * 22-Feb-2005 : Override getLegendItem(int, int) to return "line" items (DG);
 * 13-Apr-2005 : Fixed shape positioning bug (id = 1182062) (DG);
 * 20-Apr-2005 : Use generators for legend tooltips and URLs (DG);
 * 04-May-2005 : Override equals() method, renamed get/setPlotShapes() -->
 *               get/setShapesVisible (DG);
 * 09-Jun-2005 : Updated equals() to handle GradientPaint (DG);
 * 16-Jun-2005 : Fix bug (1221021) affecting stroke used for each series (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 24-Jan-2007 : Added flag to allow rounding of x-coordinates, and fixed
 *               bug in clone() (DG);
 * 05-Feb-2007 : Added an extra call to updateCrosshairValues() in
 *               drawItemPass1(), to fix bug 1564967 (DG);
 * 06-Feb-2007 : Fixed bug 1086307, crosshairs with multiple axes (DG);
 * 08-Mar-2007 : Fixed entity generation (DG);
 * 20-Apr-2007 : Updated getLegendItem() for renderer change (DG);
 * 23-Apr-2007 : Rewrite of difference drawing algorithm to allow use of
 *               series with disjoint x-values (RW);
 * 04-May-2007 : Set processVisibleItemsOnly flag to false (DG);
 * 17-May-2007 : Set datasetIndex and seriesIndex in getLegendItem() (DG);
 * 18-May-2007 : Set dataset and seriesKey for LegendItem (DG);
 * 05-Nov-2007 : Draw item labels if visible (RW);
 * 17-Jun-2008 : Apply legend shape, font and paint attributes (DG);
 *
 */

package org.jfree.chart.renderer.xy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.LinkedList;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;

/**
 * A renderer for an {@link XYPlot} that highlights the differences between two
 * series.  The example shown here is generated by the
 * <code>DifferenceChartDemo1.java</code> program included in the JFreeChart
 * demo collection:
 * <br><br>
 * <img src="../../../../../images/XYDifferenceRendererSample.png"
 * alt="XYDifferenceRendererSample.png" />
 */
public class XYDifferenceRenderer extends AbstractXYItemRenderer
        implements XYItemRenderer, PublicCloneable {

    /** For serialization. */
    private static final long serialVersionUID = -8447915602375584857L;

    /** The paint used to highlight positive differences (y(0) > y(1)). */
    private transient Paint positivePaint;

    /** The paint used to highlight negative differences (y(0) < y(1)). */
    private transient Paint negativePaint;

    /** Display shapes at each point? */
    private boolean shapesVisible;

    /** The shape to display in the legend item. */
    private transient Shape legendLine;

    /**
     * This flag controls whether or not the x-coordinates (in Java2D space)
     * are rounded to integers.  When set to true, this can avoid the vertical
     * striping that anti-aliasing can generate.  However, the rounding may not
     * be appropriate for output in high resolution formats (for example,
     * vector graphics formats such as SVG and PDF).
     *
     * @since 1.0.4
     */
    private boolean roundXCoordinates;

    /**
     * Creates a new renderer with default attributes.
     */
    public XYDifferenceRenderer() {
        this(Color.green, Color.red, false);
    }

    /**
     * Creates a new renderer.
     *
     * @param positivePaint  the highlight color for positive differences
     *                       (<code>null</code> not permitted).
     * @param negativePaint  the highlight color for negative differences
     *                       (<code>null</code> not permitted).
     * @param shapes  draw shapes?
     */
    public XYDifferenceRenderer(Paint positivePaint, Paint negativePaint,
                                boolean shapes) {
        if (positivePaint == null) {
            throw new IllegalArgumentException(
                    "Null 'positivePaint' argument.");
        }
        if (negativePaint == null) {
            throw new IllegalArgumentException(
                    "Null 'negativePaint' argument.");
        }
        this.positivePaint = positivePaint;
        this.negativePaint = negativePaint;
        this.shapesVisible = shapes;
        this.legendLine = new Line2D.Double(-7.0, 0.0, 7.0, 0.0);
        this.roundXCoordinates = false;
    }

    /**
     * Returns the paint used to highlight positive differences.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setPositivePaint(Paint)
     */
    public Paint getPositivePaint() {
        return this.positivePaint;
    }

    /**
     * Sets the paint used to highlight positive differences and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getPositivePaint()
     */
    public void setPositivePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.positivePaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the paint used to highlight negative differences.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setNegativePaint(Paint)
     */
    public Paint getNegativePaint() {
        return this.negativePaint;
    }

    /**
     * Sets the paint used to highlight negative differences.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getNegativePaint()
     */
    public void setNegativePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.negativePaint = paint;
        notifyListeners(new RendererChangeEvent(this));
    }

    /**
     * Returns a flag that controls whether or not shapes are drawn for each
     * data value.
     *
     * @return A boolean.
     *
     * @see #setShapesVisible(boolean)
     */
    public boolean getShapesVisible() {
        return this.shapesVisible;
    }

    /**
     * Sets a flag that controls whether or not shapes are drawn for each
     * data value, and sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param flag  the flag.
     *
     * @see #getShapesVisible()
     */
    public void setShapesVisible(boolean flag) {
        this.shapesVisible = flag;
        fireChangeEvent();
    }

    /**
     * Returns the shape used to represent a line in the legend.
     *
     * @return The legend line (never <code>null</code>).
     *
     * @see #setLegendLine(Shape)
     */
    public Shape getLegendLine() {
        return this.legendLine;
    }

    /**
     * Sets the shape used as a line in each legend item and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param line  the line (<code>null</code> not permitted).
     *
     * @see #getLegendLine()
     */
    public void setLegendLine(Shape line) {
        if (line == null) {
            throw new IllegalArgumentException("Null 'line' argument.");
        }
        this.legendLine = line;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not the x-coordinates (in
     * Java2D space) are rounded to integer values.
     *
     * @return The flag.
     *
     * @since 1.0.4
     *
     * @see #setRoundXCoordinates(boolean)
     */
    public boolean getRoundXCoordinates() {
        return this.roundXCoordinates;
    }

    /**
     * Sets the flag that controls whether or not the x-coordinates (in
     * Java2D space) are rounded to integer values, and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param round  the new flag value.
     *
     * @since 1.0.4
     *
     * @see #getRoundXCoordinates()
     */
    public void setRoundXCoordinates(boolean round) {
        this.roundXCoordinates = round;
        fireChangeEvent();
    }

    /**
     * Initialises the renderer and returns a state object that should be
     * passed to subsequent calls to the drawItem() method.  This method will
     * be called before the first item is rendered, giving the renderer an
     * opportunity to initialise any state information it wants to maintain.
     * The renderer can do nothing if it chooses.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area inside the axes.
     * @param plot  the plot.
     * @param data  the data.
     * @param info  an optional info collection object to return data back to
     *              the caller.
     *
     * @return A state object.
     */
    public XYItemRendererState initialise(Graphics2D g2,
                                          Rectangle2D dataArea,
                                          XYPlot plot,
                                          XYDataset data,
                                          PlotRenderingInfo info) {

        XYItemRendererState state = super.initialise(g2, dataArea, plot, data,
                info);
        state.setProcessVisibleItemsOnly(false);
        return state;

    }

    /**
     * Returns <code>2</code>, the number of passes required by the renderer.
     * The {@link XYPlot} will run through the dataset this number of times.
     *
     * @return The number of passes required by the renderer.
     */
    public int getPassCount() {
        return 2;
    }

    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the data is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain (horizontal) axis.
     * @param rangeAxis  the range (vertical) axis.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        (<code>null</code> permitted).
     * @param pass  the pass index.
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

        if (pass == 0) {
            drawItemPass0(g2, dataArea, info, plot, domainAxis, rangeAxis,
                    dataset, series, item, crosshairState);
        }
        else if (pass == 1) {
            drawItemPass1(g2, dataArea, info, plot, domainAxis, rangeAxis,
                    dataset, series, item, crosshairState);
        }

    }

    /**
     * Draws the visual representation of a single data item, first pass.
     *
     * @param x_graphics  the graphics device.
     * @param x_dataArea  the area within which the data is being drawn.
     * @param x_info  collects information about the drawing.
     * @param x_plot  the plot (can be used to obtain standard color
     *                information etc).
     * @param x_domainAxis  the domain (horizontal) axis.
     * @param x_rangeAxis  the range (vertical) axis.
     * @param x_dataset  the dataset.
     * @param x_series  the series index (zero-based).
     * @param x_item  the item index (zero-based).
     * @param x_crosshairState  crosshair information for the plot
     *                          (<code>null</code> permitted).
     */
    protected void drawItemPass0(Graphics2D x_graphics,
                                 Rectangle2D x_dataArea,
                                 PlotRenderingInfo x_info,
                                 XYPlot x_plot,
                                 ValueAxis x_domainAxis,
                                 ValueAxis x_rangeAxis,
                                 XYDataset x_dataset,
                                 int x_series,
                                 int x_item,
                                 CrosshairState x_crosshairState) {

        if (!((0 == x_series) && (0 == x_item))) {
            return;
        }

        boolean b_impliedZeroSubtrahend = (1 == x_dataset.getSeriesCount());

        // check if either series is a degenerate case (i.e. less than 2 points)
        if (isEitherSeriesDegenerate(x_dataset, b_impliedZeroSubtrahend)) {
            return;
        }

        // check if series are disjoint (i.e. domain-spans do not overlap)
        if (!b_impliedZeroSubtrahend && areSeriesDisjoint(x_dataset)) {
            return;
        }

        // polygon definitions
        LinkedList l_minuendXs    = new LinkedList();
        LinkedList l_minuendYs    = new LinkedList();
        LinkedList l_subtrahendXs = new LinkedList();
        LinkedList l_subtrahendYs = new LinkedList();
        LinkedList l_polygonXs    = new LinkedList();
        LinkedList l_polygonYs    = new LinkedList();

        // state
        int l_minuendItem      = 0;
        int l_minuendItemCount = x_dataset.getItemCount(0);
        Double l_minuendCurX   = null;
        Double l_minuendNextX  = null;
        Double l_minuendCurY   = null;
        Double l_minuendNextY  = null;
        double l_minuendMaxY   = Double.NEGATIVE_INFINITY;
        double l_minuendMinY   = Double.POSITIVE_INFINITY;

        int l_subtrahendItem      = 0;
        int l_subtrahendItemCount = 0; // actual value set below
        Double l_subtrahendCurX   = null;
        Double l_subtrahendNextX  = null;
        Double l_subtrahendCurY   = null;
        Double l_subtrahendNextY  = null;
        double l_subtrahendMaxY   = Double.NEGATIVE_INFINITY;
        double l_subtrahendMinY   = Double.POSITIVE_INFINITY;

        // if a subtrahend is not specified, assume it is zero
        if (b_impliedZeroSubtrahend) {
            l_subtrahendItem      = 0;
            l_subtrahendItemCount = 2;
            l_subtrahendCurX      = new Double(x_dataset.getXValue(0, 0));
            l_subtrahendNextX     = new Double(x_dataset.getXValue(0,
                    (l_minuendItemCount - 1)));
            l_subtrahendCurY      = new Double(0.0);
            l_subtrahendNextY     = new Double(0.0);
            l_subtrahendMaxY      = 0.0;
            l_subtrahendMinY      = 0.0;

            l_subtrahendXs.add(l_subtrahendCurX);
            l_subtrahendYs.add(l_subtrahendCurY);
        }
        else {
            l_subtrahendItemCount = x_dataset.getItemCount(1);
        }

        boolean b_minuendDone           = false;
        boolean b_minuendAdvanced       = true;
        boolean b_minuendAtIntersect    = false;
        boolean b_minuendFastForward    = false;
        boolean b_subtrahendDone        = false;
        boolean b_subtrahendAdvanced    = true;
        boolean b_subtrahendAtIntersect = false;
        boolean b_subtrahendFastForward = false;
        boolean b_colinear              = false;

        boolean b_positive;

        // coordinate pairs
        double l_x1 = 0.0, l_y1 = 0.0; // current minuend point
        double l_x2 = 0.0, l_y2 = 0.0; // next minuend point
        double l_x3 = 0.0, l_y3 = 0.0; // current subtrahend point
        double l_x4 = 0.0, l_y4 = 0.0; // next subtrahend point

        // fast-forward through leading tails
        boolean b_fastForwardDone = false;
        while (!b_fastForwardDone) {
            // get the x and y coordinates
            l_x1 = x_dataset.getXValue(0, l_minuendItem);
            l_y1 = x_dataset.getYValue(0, l_minuendItem);
            l_x2 = x_dataset.getXValue(0, l_minuendItem + 1);
            l_y2 = x_dataset.getYValue(0, l_minuendItem + 1);

            l_minuendCurX  = new Double(l_x1);
            l_minuendCurY  = new Double(l_y1);
            l_minuendNextX = new Double(l_x2);
            l_minuendNextY = new Double(l_y2);

            if (b_impliedZeroSubtrahend) {
                l_x3 = l_subtrahendCurX.doubleValue();
                l_y3 = l_subtrahendCurY.doubleValue();
                l_x4 = l_subtrahendNextX.doubleValue();
                l_y4 = l_subtrahendNextY.doubleValue();
            }
            else {
                l_x3 = x_dataset.getXValue(1, l_subtrahendItem);
                l_y3 = x_dataset.getYValue(1, l_subtrahendItem);
                l_x4 = x_dataset.getXValue(1, l_subtrahendItem + 1);
                l_y4 = x_dataset.getYValue(1, l_subtrahendItem + 1);

                l_subtrahendCurX  = new Double(l_x3);
                l_subtrahendCurY  = new Double(l_y3);
                l_subtrahendNextX = new Double(l_x4);
                l_subtrahendNextY = new Double(l_y4);
            }

            if (l_x2 <= l_x3) {
                // minuend needs to be fast forwarded
                l_minuendItem++;
                b_minuendFastForward = true;
                continue;
            }

            if (l_x4 <= l_x1) {
                // subtrahend needs to be fast forwarded
                l_subtrahendItem++;
                b_subtrahendFastForward = true;
                continue;
            }

            // check if initial polygon needs to be clipped
            if ((l_x3 < l_x1) && (l_x1 < l_x4)) {
                // project onto subtrahend
                double l_slope   = (l_y4 - l_y3) / (l_x4 - l_x3);
                l_subtrahendCurX = l_minuendCurX;
                l_subtrahendCurY = new Double((l_slope * l_x1)
                        + (l_y3 - (l_slope * l_x3)));

                l_subtrahendXs.add(l_subtrahendCurX);
                l_subtrahendYs.add(l_subtrahendCurY);
            }

            if ((l_x1 < l_x3) && (l_x3 < l_x2)) {
                // project onto minuend
                double l_slope = (l_y2 - l_y1) / (l_x2 - l_x1);
                l_minuendCurX  = l_subtrahendCurX;
                l_minuendCurY  = new Double((l_slope * l_x3)
                        + (l_y1 - (l_slope * l_x1)));

                l_minuendXs.add(l_minuendCurX);
                l_minuendYs.add(l_minuendCurY);
            }

            l_minuendMaxY    = l_minuendCurY.doubleValue();
            l_minuendMinY    = l_minuendCurY.doubleValue();
            l_subtrahendMaxY = l_subtrahendCurY.doubleValue();
            l_subtrahendMinY = l_subtrahendCurY.doubleValue();

            b_fastForwardDone = true;
        }

        // start of algorithm
        while (!b_minuendDone && !b_subtrahendDone) {
            if (!b_minuendDone && !b_minuendFastForward && b_minuendAdvanced) {
                l_x1 = x_dataset.getXValue(0, l_minuendItem);
                l_y1 = x_dataset.getYValue(0, l_minuendItem);
                l_minuendCurX = new Double(l_x1);
                l_minuendCurY = new Double(l_y1);

                if (!b_minuendAtIntersect) {
                    l_minuendXs.add(l_minuendCurX);
                    l_minuendYs.add(l_minuendCurY);
                }

                l_minuendMaxY = Math.max(l_minuendMaxY, l_y1);
                l_minuendMinY = Math.min(l_minuendMinY, l_y1);

                l_x2 = x_dataset.getXValue(0, l_minuendItem + 1);
                l_y2 = x_dataset.getYValue(0, l_minuendItem + 1);
                l_minuendNextX = new Double(l_x2);
                l_minuendNextY = new Double(l_y2);
            }

            // never updated the subtrahend if it is implied to be zero
            if (!b_impliedZeroSubtrahend && !b_subtrahendDone
                    && !b_subtrahendFastForward && b_subtrahendAdvanced) {
                l_x3 = x_dataset.getXValue(1, l_subtrahendItem);
                l_y3 = x_dataset.getYValue(1, l_subtrahendItem);
                l_subtrahendCurX = new Double(l_x3);
                l_subtrahendCurY = new Double(l_y3);

                if (!b_subtrahendAtIntersect) {
                    l_subtrahendXs.add(l_subtrahendCurX);
                    l_subtrahendYs.add(l_subtrahendCurY);
                }

                l_subtrahendMaxY = Math.max(l_subtrahendMaxY, l_y3);
                l_subtrahendMinY = Math.min(l_subtrahendMinY, l_y3);

                l_x4 = x_dataset.getXValue(1, l_subtrahendItem + 1);
                l_y4 = x_dataset.getYValue(1, l_subtrahendItem + 1);
                l_subtrahendNextX = new Double(l_x4);
                l_subtrahendNextY = new Double(l_y4);
            }

            // deassert b_*FastForward (only matters for 1st time through loop)
            b_minuendFastForward    = false;
            b_subtrahendFastForward = false;

            Double l_intersectX = null;
            Double l_intersectY = null;
            boolean b_intersect = false;

            b_minuendAtIntersect    = false;
            b_subtrahendAtIntersect = false;

            // check for intersect
            if ((l_x2 == l_x4) && (l_y2 == l_y4)) {
                // check if line segments are colinear
                if ((l_x1 == l_x3) && (l_y1 == l_y3)) {
                    b_colinear = true;
                }
                else {
                    // the intersect is at the next point for both the minuend
                    // and subtrahend
                    l_intersectX = new Double(l_x2);
                    l_intersectY = new Double(l_y2);

                    b_intersect             = true;
                    b_minuendAtIntersect    = true;
                    b_subtrahendAtIntersect = true;
                 }
            }
            else {
                // compute common denominator
                double l_denominator = ((l_y4 - l_y3) * (l_x2 - l_x1))
                        - ((l_x4 - l_x3) * (l_y2 - l_y1));

                // compute common deltas
                double l_deltaY = l_y1 - l_y3;
                double l_deltaX = l_x1 - l_x3;

                // compute numerators
                double l_numeratorA = ((l_x4 - l_x3) * l_deltaY)
                        - ((l_y4 - l_y3) * l_deltaX);
                double l_numeratorB = ((l_x2 - l_x1) * l_deltaY)
                        - ((l_y2 - l_y1) * l_deltaX);

                // check if line segments are colinear
                if ((0 == l_numeratorA) && (0 == l_numeratorB)
                        && (0 == l_denominator)) {
                    b_colinear = true;
                }
                else {
                    // check if previously colinear
                    if (b_colinear) {
                        // clear colinear points and flag
                        l_minuendXs.clear();
                        l_minuendYs.clear();
                        l_subtrahendXs.clear();
                        l_subtrahendYs.clear();
                        l_polygonXs.clear();
                        l_polygonYs.clear();

                        b_colinear = false;

                        // set new starting point for the polygon
                        boolean b_useMinuend = ((l_x3 <= l_x1)
                                && (l_x1 <= l_x4));
                        l_polygonXs.add(b_useMinuend ? l_minuendCurX
                                : l_subtrahendCurX);
                        l_polygonYs.add(b_useMinuend ? l_minuendCurY
                                : l_subtrahendCurY);
                    }

                    // compute slope components
                    double l_slopeA = l_numeratorA / l_denominator;
                    double l_slopeB = l_numeratorB / l_denominator;

                    // check if the line segments intersect
                    if ((0 < l_slopeA) && (l_slopeA <= 1) && (0 < l_slopeB)
                            && (l_slopeB <= 1)) {
                        // compute the point of intersection
                        double l_xi = l_x1 + (l_slopeA * (l_x2 - l_x1));
                        double l_yi = l_y1 + (l_slopeA * (l_y2 - l_y1));

                        l_intersectX            = new Double(l_xi);
                        l_intersectY            = new Double(l_yi);
                        b_intersect             = true;
                        b_minuendAtIntersect    = ((l_xi == l_x2)
                                && (l_yi == l_y2));
                        b_subtrahendAtIntersect = ((l_xi == l_x4)
                                && (l_yi == l_y4));

                        // advance minuend and subtrahend to intesect
                        l_minuendCurX    = l_intersectX;
                        l_minuendCurY    = l_intersectY;
                        l_subtrahendCurX = l_intersectX;
                        l_subtrahendCurY = l_intersectY;
                    }
                }
            }

            if (b_intersect) {
                // create the polygon
                // add the minuend's points to polygon
                l_polygonXs.addAll(l_minuendXs);
                l_polygonYs.addAll(l_minuendYs);

                // add intersection point to the polygon
                l_polygonXs.add(l_intersectX);
                l_polygonYs.add(l_intersectY);

                // add the subtrahend's points to the polygon in reverse
                Collections.reverse(l_subtrahendXs);
                Collections.reverse(l_subtrahendYs);
                l_polygonXs.addAll(l_subtrahendXs);
                l_polygonYs.addAll(l_subtrahendYs);

                // create an actual polygon
                b_positive = (l_subtrahendMaxY <= l_minuendMaxY)
                        && (l_subtrahendMinY <= l_minuendMinY);
                createPolygon(x_graphics, x_dataArea, x_plot, x_domainAxis,
                        x_rangeAxis, b_positive, l_polygonXs, l_polygonYs);

                // clear the point vectors
                l_minuendXs.clear();
                l_minuendYs.clear();
                l_subtrahendXs.clear();
                l_subtrahendYs.clear();
                l_polygonXs.clear();
                l_polygonYs.clear();

                // set the maxY and minY values to intersect y-value
                double l_y       = l_intersectY.doubleValue();
                l_minuendMaxY    = l_y;
                l_subtrahendMaxY = l_y;
                l_minuendMinY    = l_y;
                l_subtrahendMinY = l_y;

                // add interection point to new polygon
                l_polygonXs.add(l_intersectX);
                l_polygonYs.add(l_intersectY);
            }

            // advance the minuend if needed
            if (l_x2 <= l_x4) {
                l_minuendItem++;
                b_minuendAdvanced = true;
            }
            else {
                b_minuendAdvanced = false;
            }

            // advance the subtrahend if needed
            if (l_x4 <= l_x2) {
                l_subtrahendItem++;
                b_subtrahendAdvanced = true;
            }
            else {
                b_subtrahendAdvanced = false;
            }

            b_minuendDone    = (l_minuendItem == (l_minuendItemCount - 1));
            b_subtrahendDone = (l_subtrahendItem == (l_subtrahendItemCount
                    - 1));
        }

        // check if the final polygon needs to be clipped
        if (b_minuendDone && (l_x3 < l_x2) && (l_x2 < l_x4)) {
            // project onto subtrahend
            double l_slope    = (l_y4 - l_y3) / (l_x4 - l_x3);
            l_subtrahendNextX = l_minuendNextX;
            l_subtrahendNextY = new Double((l_slope * l_x2)
                    + (l_y3 - (l_slope * l_x3)));
        }

        if (b_subtrahendDone && (l_x1 < l_x4) && (l_x4 < l_x2)) {
            // project onto minuend
            double l_slope = (l_y2 - l_y1) / (l_x2 - l_x1);
            l_minuendNextX = l_subtrahendNextX;
            l_minuendNextY = new Double((l_slope * l_x4)
                    + (l_y1 - (l_slope * l_x1)));
        }

        // consider last point of minuend and subtrahend for determining
        // positivity
        l_minuendMaxY    = Math.max(l_minuendMaxY,
                l_minuendNextY.doubleValue());
        l_subtrahendMaxY = Math.max(l_subtrahendMaxY,
                l_subtrahendNextY.doubleValue());
        l_minuendMinY    = Math.min(l_minuendMinY,
                l_minuendNextY.doubleValue());
        l_subtrahendMinY = Math.min(l_subtrahendMinY,
                l_subtrahendNextY.doubleValue());

        // add the last point of the minuned and subtrahend
        l_minuendXs.add(l_minuendNextX);
        l_minuendYs.add(l_minuendNextY);
        l_subtrahendXs.add(l_subtrahendNextX);
        l_subtrahendYs.add(l_subtrahendNextY);

        // create the polygon
        // add the minuend's points to polygon
        l_polygonXs.addAll(l_minuendXs);
        l_polygonYs.addAll(l_minuendYs);

        // add the subtrahend's points to the polygon in reverse
        Collections.reverse(l_subtrahendXs);
        Collections.reverse(l_subtrahendYs);
        l_polygonXs.addAll(l_subtrahendXs);
        l_polygonYs.addAll(l_subtrahendYs);

        // create an actual polygon
        b_positive = (l_subtrahendMaxY <= l_minuendMaxY)
                && (l_subtrahendMinY <= l_minuendMinY);
        createPolygon(x_graphics, x_dataArea, x_plot, x_domainAxis,
                x_rangeAxis, b_positive, l_polygonXs, l_polygonYs);
    }

    /**
     * Draws the visual representation of a single data item, second pass.  In
     * the second pass, the renderer draws the lines and shapes for the
     * individual points in the two series.
     *
     * @param x_graphics  the graphics device.
     * @param x_dataArea  the area within which the data is being drawn.
     * @param x_info  collects information about the drawing.
     * @param x_plot  the plot (can be used to obtain standard color
     *         information etc).
     * @param x_domainAxis  the domain (horizontal) axis.
     * @param x_rangeAxis  the range (vertical) axis.
     * @param x_dataset  the dataset.
     * @param x_series  the series index (zero-based).
     * @param x_item  the item index (zero-based).
     * @param x_crosshairState  crosshair information for the plot
     *                          (<code>null</code> permitted).
     */
    protected void drawItemPass1(Graphics2D x_graphics,
                                 Rectangle2D x_dataArea,
                                 PlotRenderingInfo x_info,
                                 XYPlot x_plot,
                                 ValueAxis x_domainAxis,
                                 ValueAxis x_rangeAxis,
                                 XYDataset x_dataset,
                                 int x_series,
                                 int x_item,
                                 CrosshairState x_crosshairState) {

        Shape l_entityArea = null;
        EntityCollection l_entities = null;
        if (null != x_info) {
            l_entities = x_info.getOwner().getEntityCollection();
        }

        Paint l_seriesPaint   = getItemPaint(x_series, x_item);
        Stroke l_seriesStroke = getItemStroke(x_series, x_item);
        x_graphics.setPaint(l_seriesPaint);
        x_graphics.setStroke(l_seriesStroke);

        PlotOrientation l_orientation      = x_plot.getOrientation();
        RectangleEdge l_domainAxisLocation = x_plot.getDomainAxisEdge();
        RectangleEdge l_rangeAxisLocation  = x_plot.getRangeAxisEdge();

        double l_x0 = x_dataset.getXValue(x_series, x_item);
        double l_y0 = x_dataset.getYValue(x_series, x_item);
        double l_x1 = x_domainAxis.valueToJava2D(l_x0, x_dataArea,
                l_domainAxisLocation);
        double l_y1 = x_rangeAxis.valueToJava2D(l_y0, x_dataArea,
                l_rangeAxisLocation);

        if (getShapesVisible()) {
            Shape l_shape = getItemShape(x_series, x_item);
            if (l_orientation == PlotOrientation.HORIZONTAL) {
                l_shape = ShapeUtilities.createTranslatedShape(l_shape,
                        l_y1, l_x1);
            }
            else {
                l_shape = ShapeUtilities.createTranslatedShape(l_shape,
                        l_x1, l_y1);
            }
            if (l_shape.intersects(x_dataArea)) {
                x_graphics.setPaint(getItemPaint(x_series, x_item));
                x_graphics.fill(l_shape);
            }
            l_entityArea = l_shape;
        }

        // add an entity for the item...
        if (null != l_entities) {
            if (null == l_entityArea) {
                l_entityArea = new Rectangle2D.Double((l_x1 - 2), (l_y1 - 2),
                        4, 4);
            }
            String l_tip = null;
            XYToolTipGenerator l_tipGenerator = getToolTipGenerator(x_series,
                    x_item);
            if (null != l_tipGenerator) {
                l_tip = l_tipGenerator.generateToolTip(x_dataset, x_series,
                        x_item);
            }
            String l_url = null;
            XYURLGenerator l_urlGenerator = getURLGenerator();
            if (null != l_urlGenerator) {
                l_url = l_urlGenerator.generateURL(x_dataset, x_series,
                        x_item);
            }
            XYItemEntity l_entity = new XYItemEntity(l_entityArea, x_dataset,
                    x_series, x_item, l_tip, l_url);
            l_entities.add(l_entity);
        }

        // draw the item label if there is one...
        if (isItemLabelVisible(x_series, x_item)) {
            drawItemLabel(x_graphics, l_orientation, x_dataset, x_series,
                          x_item, l_x1, l_y1, (l_y1 < 0.0));
        }

        int l_domainAxisIndex = x_plot.getDomainAxisIndex(x_domainAxis);
        int l_rangeAxisIndex  = x_plot.getRangeAxisIndex(x_rangeAxis);
        updateCrosshairValues(x_crosshairState, l_x0, l_y0, l_domainAxisIndex,
                              l_rangeAxisIndex, l_x1, l_y1, l_orientation);

        if (0 == x_item) {
            return;
        }

        double l_x2 = x_domainAxis.valueToJava2D(x_dataset.getXValue(x_series,
                (x_item - 1)), x_dataArea, l_domainAxisLocation);
        double l_y2 = x_rangeAxis.valueToJava2D(x_dataset.getYValue(x_series,
                (x_item - 1)), x_dataArea, l_rangeAxisLocation);

        Line2D l_line = null;
        if (PlotOrientation.HORIZONTAL == l_orientation) {
            l_line = new Line2D.Double(l_y1, l_x1, l_y2, l_x2);
        }
        else if (PlotOrientation.VERTICAL == l_orientation) {
            l_line = new Line2D.Double(l_x1, l_y1, l_x2, l_y2);
        }

        if ((null != l_line) && l_line.intersects(x_dataArea)) {
            x_graphics.setPaint(getItemPaint(x_series, x_item));
            x_graphics.setStroke(getItemStroke(x_series, x_item));
            x_graphics.draw(l_line);
        }
    }

    /**
     * Determines if a dataset is degenerate.  A degenerate dataset is a
     * dataset where either series has less than two (2) points.
     *
     * @param x_dataset  the dataset.
     * @param x_impliedZeroSubtrahend  if false, do not check the subtrahend
     *
     * @return true if the dataset is degenerate.
     */
    private boolean isEitherSeriesDegenerate(XYDataset x_dataset,
            boolean x_impliedZeroSubtrahend) {

        if (x_impliedZeroSubtrahend) {
            return (x_dataset.getItemCount(0) < 2);
        }

        return ((x_dataset.getItemCount(0) < 2)
                || (x_dataset.getItemCount(1) < 2));
    }

    /**
     * Determines if the two (2) series are disjoint.
     * Disjoint series do not overlap in the domain space.
     *
     * @param x_dataset  the dataset.
     *
     * @return true if the dataset is degenerate.
     */
    private boolean areSeriesDisjoint(XYDataset x_dataset) {

        int l_minuendItemCount = x_dataset.getItemCount(0);
        double l_minuendFirst  = x_dataset.getXValue(0, 0);
        double l_minuendLast   = x_dataset.getXValue(0, l_minuendItemCount - 1);

        int l_subtrahendItemCount = x_dataset.getItemCount(1);
        double l_subtrahendFirst  = x_dataset.getXValue(1, 0);
        double l_subtrahendLast   = x_dataset.getXValue(1,
                l_subtrahendItemCount - 1);

        return ((l_minuendLast < l_subtrahendFirst)
                || (l_subtrahendLast < l_minuendFirst));
    }

    /**
     * Draws the visual representation of a polygon
     *
     * @param x_graphics  the graphics device.
     * @param x_dataArea  the area within which the data is being drawn.
     * @param x_plot  the plot (can be used to obtain standard color
     *                information etc).
     * @param x_domainAxis  the domain (horizontal) axis.
     * @param x_rangeAxis  the range (vertical) axis.
     * @param x_positive  indicates if the polygon is positive (true) or
     *                    negative (false).
     * @param x_xValues  a linked list of the x values (expects values to be
     *                   of type Double).
     * @param x_yValues  a linked list of the y values (expects values to be
     *                   of type Double).
     */
    private void createPolygon (Graphics2D x_graphics,
                                Rectangle2D x_dataArea,
                                XYPlot x_plot,
                                ValueAxis x_domainAxis,
                                ValueAxis x_rangeAxis,
                                boolean x_positive,
                                LinkedList x_xValues,
                                LinkedList x_yValues) {

        PlotOrientation l_orientation      = x_plot.getOrientation();
        RectangleEdge l_domainAxisLocation = x_plot.getDomainAxisEdge();
        RectangleEdge l_rangeAxisLocation  = x_plot.getRangeAxisEdge();

        Object[] l_xValues = x_xValues.toArray();
        Object[] l_yValues = x_yValues.toArray();

        GeneralPath l_path = new GeneralPath();

        if (PlotOrientation.VERTICAL == l_orientation) {
            double l_x = x_domainAxis.valueToJava2D((
                    (Double) l_xValues[0]).doubleValue(), x_dataArea,
                    l_domainAxisLocation);
            if (this.roundXCoordinates) {
                l_x = Math.rint(l_x);
            }

            double l_y = x_rangeAxis.valueToJava2D((
                    (Double) l_yValues[0]).doubleValue(), x_dataArea,
                    l_rangeAxisLocation);

            l_path.moveTo((float) l_x, (float) l_y);
            for (int i = 1; i < l_xValues.length; i++) {
                l_x = x_domainAxis.valueToJava2D((
                        (Double) l_xValues[i]).doubleValue(), x_dataArea,
                        l_domainAxisLocation);
                if (this.roundXCoordinates) {
                    l_x = Math.rint(l_x);
                }

                l_y = x_rangeAxis.valueToJava2D((
                        (Double) l_yValues[i]).doubleValue(), x_dataArea,
                        l_rangeAxisLocation);
                l_path.lineTo((float) l_x, (float) l_y);
            }
            l_path.closePath();
        }
        else {
            double l_x = x_domainAxis.valueToJava2D((
                    (Double) l_xValues[0]).doubleValue(), x_dataArea,
                    l_domainAxisLocation);
            if (this.roundXCoordinates) {
                l_x = Math.rint(l_x);
            }

            double l_y = x_rangeAxis.valueToJava2D((
                    (Double) l_yValues[0]).doubleValue(), x_dataArea,
                    l_rangeAxisLocation);

            l_path.moveTo((float) l_y, (float) l_x);
            for (int i = 1; i < l_xValues.length; i++) {
                l_x = x_domainAxis.valueToJava2D((
                        (Double) l_xValues[i]).doubleValue(), x_dataArea,
                        l_domainAxisLocation);
                if (this.roundXCoordinates) {
                    l_x = Math.rint(l_x);
                }

                l_y = x_rangeAxis.valueToJava2D((
                        (Double) l_yValues[i]).doubleValue(), x_dataArea,
                        l_rangeAxisLocation);
                l_path.lineTo((float) l_y, (float) l_x);
            }
            l_path.closePath();
        }

        if (l_path.intersects(x_dataArea)) {
            x_graphics.setPaint(x_positive ? getPositivePaint()
                    : getNegativePaint());
            x_graphics.fill(l_path);
        }
    }

    /**
     * Returns a default legend item for the specified series.  Subclasses
     * should override this method to generate customised items.
     *
     * @param datasetIndex  the dataset index (zero-based).
     * @param series  the series index (zero-based).
     *
     * @return A legend item for the series.
     */
    public LegendItem getLegendItem(int datasetIndex, int series) {
        LegendItem result = null;
        XYPlot p = getPlot();
        if (p != null) {
            XYDataset dataset = p.getDataset(datasetIndex);
            if (dataset != null) {
                if (getItemVisible(series, 0)) {
                    String label = getLegendItemLabelGenerator().generateLabel(
                            dataset, series);
                    String description = label;
                    String toolTipText = null;
                    if (getLegendItemToolTipGenerator() != null) {
                        toolTipText
                            = getLegendItemToolTipGenerator().generateLabel(
                                    dataset, series);
                    }
                    String urlText = null;
                    if (getLegendItemURLGenerator() != null) {
                        urlText = getLegendItemURLGenerator().generateLabel(
                                dataset, series);
                    }
                    Paint paint = lookupSeriesPaint(series);
                    Stroke stroke = lookupSeriesStroke(series);
                    Shape line = getLegendLine();
                    result = new LegendItem(label, description,
                            toolTipText, urlText, line, stroke, paint);
                    result.setLabelFont(lookupLegendTextFont(series));
                    Paint labelPaint = lookupLegendTextPaint(series);
                    if (labelPaint != null) {
                        result.setLabelPaint(labelPaint);
                    }
                    result.setDataset(dataset);
                    result.setDatasetIndex(datasetIndex);
                    result.setSeriesKey(dataset.getSeriesKey(series));
                    result.setSeriesIndex(series);
                }
            }

        }

        return result;

    }

    /**
     * Tests this renderer for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYDifferenceRenderer)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        XYDifferenceRenderer that = (XYDifferenceRenderer) obj;
        if (!PaintUtilities.equal(this.positivePaint, that.positivePaint)) {
            return false;
        }
        if (!PaintUtilities.equal(this.negativePaint, that.negativePaint)) {
            return false;
        }
        if (this.shapesVisible != that.shapesVisible) {
            return false;
        }
        if (!ShapeUtilities.equal(this.legendLine, that.legendLine)) {
            return false;
        }
        if (this.roundXCoordinates != that.roundXCoordinates) {
            return false;
        }
        return true;
    }

    /**
     * Returns a clone of the renderer.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  if the renderer cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        XYDifferenceRenderer clone = (XYDifferenceRenderer) super.clone();
        clone.legendLine = ShapeUtilities.clone(this.legendLine);
        return clone;
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint(this.positivePaint, stream);
        SerialUtilities.writePaint(this.negativePaint, stream);
        SerialUtilities.writeShape(this.legendLine, stream);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.positivePaint = SerialUtilities.readPaint(stream);
        this.negativePaint = SerialUtilities.readPaint(stream);
        this.legendLine = SerialUtilities.readShape(stream);
    }

}
