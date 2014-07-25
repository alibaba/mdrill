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
 * -------------------
 * XYAreaRenderer.java
 * -------------------
 * (C) Copyright 2002-2008, by Hari and Contributors.
 *
 * Original Author:  Hari (ourhari@hotmail.com);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Richard Atkinson;
 *                   Christian W. Zuckschwerdt;
 *
 * Changes:
 * --------
 * 03-Apr-2002 : Version 1, contributed by Hari.  This class is based on the
 *               StandardXYItemRenderer class (DG);
 * 09-Apr-2002 : Removed the translated zero from the drawItem method -
 *               overridden the initialise() method to calculate it (DG);
 * 30-May-2002 : Added tool tip generator to constructor to match super
 *               class (DG);
 * 25-Jun-2002 : Removed unnecessary local variable (DG);
 * 05-Aug-2002 : Small modification to drawItem method to support URLs for HTML
 *               image maps (RA);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 07-Nov-2002 : Renamed AreaXYItemRenderer --> XYAreaRenderer (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 01-May-2003 : Modified drawItem() method signature (DG);
 * 27-Jul-2003 : Made line and polygon properties protected rather than
 *               private (RA);
 * 30-Jul-2003 : Modified entity constructor (CZ);
 * 20-Aug-2003 : Implemented Cloneable and PublicCloneable (DG);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 07-Oct-2003 : Added renderer state (DG);
 * 08-Dec-2003 : Modified hotspot for chart entity (DG);
 * 10-Feb-2004 : Changed the drawItem() method to make cut-and-paste overriding
 *               easier.  Also moved state class into this class (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState.  Renamed
 *               XYToolTipGenerator --> XYItemLabelGenerator (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 11-Nov-2004 : Now uses ShapeUtilities to translate shapes (DG);
 * 19-Jan-2005 : Now accesses primitives only from dataset (DG);
 * 21-Mar-2005 : Override getLegendItem() and equals() methods (DG);
 * 20-Apr-2005 : Use generators for legend tooltips and URLs (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 06-Feb-2007 : Fixed bug 1086307, crosshairs with multiple axes (DG);
 * 14-Feb-2007 : Fixed bug in clone() (DG);
 * 20-Apr-2007 : Updated getLegendItem() for renderer change (DG);
 * 04-May-2007 : Set processVisibleItemsOnly flag to false (DG);
 * 17-May-2007 : Set datasetIndex and seriesIndex in getLegendItem() (DG);
 * 18-May-2007 : Set dataset and seriesKey for LegendItem (DG);
 * 17-Jun-2008 : Apply legend font and paint attributes (DG);
 * 31-Dec-2008 : Fix for bug 2471906 - dashed outlines performance issue (DG);
 *
 */

package org.jfree.chart.renderer.xy;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;

/**
 * Area item renderer for an {@link XYPlot}.  This class can draw (a) shapes at
 * each point, or (b) lines between points, or (c) both shapes and lines,
 * or (d) filled areas, or (e) filled areas and shapes. The example shown here
 * is generated by the <code>XYAreaRendererDemo1.java</code> program included
 * in the JFreeChart demo collection:
 * <br><br>
 * <img src="../../../../../images/XYAreaRendererSample.png"
 * alt="XYAreaRendererSample.png" />
 */
public class XYAreaRenderer extends AbstractXYItemRenderer
        implements XYItemRenderer, PublicCloneable {

    /** For serialization. */
    private static final long serialVersionUID = -4481971353973876747L;

    /**
     * A state object used by this renderer.
     */
    static class XYAreaRendererState extends XYItemRendererState {

        /** Working storage for the area under one series. */
        public Polygon area;

        /** Working line that can be recycled. */
        public Line2D line;

        /**
         * Creates a new state.
         *
         * @param info  the plot rendering info.
         */
        public XYAreaRendererState(PlotRenderingInfo info) {
            super(info);
            this.area = new Polygon();
            this.line = new Line2D.Double();
        }

    }

    /** Useful constant for specifying the type of rendering (shapes only). */
    public static final int SHAPES = 1;

    /** Useful constant for specifying the type of rendering (lines only). */
    public static final int LINES = 2;

    /**
     * Useful constant for specifying the type of rendering (shapes and lines).
     */
    public static final int SHAPES_AND_LINES = 3;

    /** Useful constant for specifying the type of rendering (area only). */
    public static final int AREA = 4;

    /**
     * Useful constant for specifying the type of rendering (area and shapes).
     */
    public static final int AREA_AND_SHAPES = 5;

    /** A flag indicating whether or not shapes are drawn at each XY point. */
    private boolean plotShapes;

    /** A flag indicating whether or not lines are drawn between XY points. */
    private boolean plotLines;

    /** A flag indicating whether or not Area are drawn at each XY point. */
    private boolean plotArea;

    /** A flag that controls whether or not the outline is shown. */
    private boolean showOutline;

    /**
     * The shape used to represent an area in each legend item (this should
     * never be <code>null</code>).
     */
    private transient Shape legendArea;

    /**
     * Constructs a new renderer.
     */
    public XYAreaRenderer() {
        this(AREA);
    }

    /**
     * Constructs a new renderer.
     *
     * @param type  the type of the renderer.
     */
    public XYAreaRenderer(int type) {
        this(type, null, null);
    }

    /**
     * Constructs a new renderer.  To specify the type of renderer, use one of
     * the constants: <code>SHAPES</code>, <code>LINES</code>,
     * <code>SHAPES_AND_LINES</code>, <code>AREA</code> or
     * <code>AREA_AND_SHAPES</code>.
     *
     * @param type  the type of renderer.
     * @param toolTipGenerator  the tool tip generator to use
     *                          (<code>null</code> permitted).
     * @param urlGenerator  the URL generator (<code>null</code> permitted).
     */
    public XYAreaRenderer(int type, XYToolTipGenerator toolTipGenerator,
                          XYURLGenerator urlGenerator) {

        super();
        setBaseToolTipGenerator(toolTipGenerator);
        setURLGenerator(urlGenerator);

        if (type == SHAPES) {
            this.plotShapes = true;
        }
        if (type == LINES) {
            this.plotLines = true;
        }
        if (type == SHAPES_AND_LINES) {
            this.plotShapes = true;
            this.plotLines = true;
        }
        if (type == AREA) {
            this.plotArea = true;
        }
        if (type == AREA_AND_SHAPES) {
            this.plotArea = true;
            this.plotShapes = true;
        }
        this.showOutline = false;
        GeneralPath area = new GeneralPath();
        area.moveTo(0.0f, -4.0f);
        area.lineTo(3.0f, -2.0f);
        area.lineTo(4.0f, 4.0f);
        area.lineTo(-4.0f, 4.0f);
        area.lineTo(-3.0f, -2.0f);
        area.closePath();
        this.legendArea = area;

    }

    /**
     * Returns true if shapes are being plotted by the renderer.
     *
     * @return <code>true</code> if shapes are being plotted by the renderer.
     */
    public boolean getPlotShapes() {
        return this.plotShapes;
    }

    /**
     * Returns true if lines are being plotted by the renderer.
     *
     * @return <code>true</code> if lines are being plotted by the renderer.
     */
    public boolean getPlotLines() {
        return this.plotLines;
    }

    /**
     * Returns true if Area is being plotted by the renderer.
     *
     * @return <code>true</code> if Area is being plotted by the renderer.
     */
    public boolean getPlotArea() {
        return this.plotArea;
    }

    /**
     * Returns a flag that controls whether or not outlines of the areas are
     * drawn.
     *
     * @return The flag.
     *
     * @see #setOutline(boolean)
     */
    public boolean isOutline() {
        return this.showOutline;
    }

    /**
     * Sets a flag that controls whether or not outlines of the areas are drawn
     * and sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param show  the flag.
     *
     * @see #isOutline()
     */
    public void setOutline(boolean show) {
        this.showOutline = show;
        fireChangeEvent();
    }

    /**
     * Returns the shape used to represent an area in the legend.
     *
     * @return The legend area (never <code>null</code>).
     */
    public Shape getLegendArea() {
        return this.legendArea;
    }

    /**
     * Sets the shape used as an area in each legend item and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param area  the area (<code>null</code> not permitted).
     */
    public void setLegendArea(Shape area) {
        if (area == null) {
            throw new IllegalArgumentException("Null 'area' argument.");
        }
        this.legendArea = area;
        fireChangeEvent();
    }

    /**
     * Initialises the renderer and returns a state object that should be
     * passed to all subsequent calls to the drawItem() method.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area inside the axes.
     * @param plot  the plot.
     * @param data  the data.
     * @param info  an optional info collection object to return data back to
     *              the caller.
     *
     * @return A state object for use by the renderer.
     */
    public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea,
            XYPlot plot, XYDataset data, PlotRenderingInfo info) {
        XYAreaRendererState state = new XYAreaRendererState(info);

        // in the rendering process, there is special handling for item
        // zero, so we can't support processing of visible data items only
        state.setProcessVisibleItemsOnly(false);
        return state;
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
        XYPlot xyplot = getPlot();
        if (xyplot != null) {
            XYDataset dataset = xyplot.getDataset(datasetIndex);
            if (dataset != null) {
                XYSeriesLabelGenerator lg = getLegendItemLabelGenerator();
                String label = lg.generateLabel(dataset, series);
                String description = label;
                String toolTipText = null;
                if (getLegendItemToolTipGenerator() != null) {
                    toolTipText = getLegendItemToolTipGenerator().generateLabel(
                            dataset, series);
                }
                String urlText = null;
                if (getLegendItemURLGenerator() != null) {
                    urlText = getLegendItemURLGenerator().generateLabel(
                            dataset, series);
                }
                Paint paint = lookupSeriesPaint(series);
                result = new LegendItem(label, description, toolTipText,
                        urlText, this.legendArea, paint);
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
        return result;
    }

    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the data is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color information
     *              etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        (<code>null</code> permitted).
     * @param pass  the pass index.
     */
    public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) {

        if (!getItemVisible(series, item)) {
            return;
        }
        XYAreaRendererState areaState = (XYAreaRendererState) state;

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(y1)) {
            y1 = 0.0;
        }
        double transX1 = domainAxis.valueToJava2D(x1, dataArea,
                plot.getDomainAxisEdge());
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea,
                plot.getRangeAxisEdge());

        // get the previous point and the next point so we can calculate a
        // "hot spot" for the area (used by the chart entity)...
        int itemCount = dataset.getItemCount(series);
        double x0 = dataset.getXValue(series, Math.max(item - 1, 0));
        double y0 = dataset.getYValue(series, Math.max(item - 1, 0));
        if (Double.isNaN(y0)) {
            y0 = 0.0;
        }
        double transX0 = domainAxis.valueToJava2D(x0, dataArea,
                plot.getDomainAxisEdge());
        double transY0 = rangeAxis.valueToJava2D(y0, dataArea,
                plot.getRangeAxisEdge());

        double x2 = dataset.getXValue(series, Math.min(item + 1,
                itemCount - 1));
        double y2 = dataset.getYValue(series, Math.min(item + 1,
                itemCount - 1));
        if (Double.isNaN(y2)) {
            y2 = 0.0;
        }
        double transX2 = domainAxis.valueToJava2D(x2, dataArea,
                plot.getDomainAxisEdge());
        double transY2 = rangeAxis.valueToJava2D(y2, dataArea,
                plot.getRangeAxisEdge());

        double transZero = rangeAxis.valueToJava2D(0.0, dataArea,
                plot.getRangeAxisEdge());
        Polygon hotspot = null;
        if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
            hotspot = new Polygon();
            hotspot.addPoint((int) transZero,
                    (int) ((transX0 + transX1) / 2.0));
            hotspot.addPoint((int) ((transY0 + transY1) / 2.0),
                    (int) ((transX0 + transX1) / 2.0));
            hotspot.addPoint((int) transY1, (int) transX1);
            hotspot.addPoint((int) ((transY1 + transY2) / 2.0),
                    (int) ((transX1 + transX2) / 2.0));
            hotspot.addPoint((int) transZero,
                    (int) ((transX1 + transX2) / 2.0));
        }
        else {  // vertical orientation
            hotspot = new Polygon();
            hotspot.addPoint((int) ((transX0 + transX1) / 2.0),
                    (int) transZero);
            hotspot.addPoint((int) ((transX0 + transX1) / 2.0),
                    (int) ((transY0 + transY1) / 2.0));
            hotspot.addPoint((int) transX1, (int) transY1);
            hotspot.addPoint((int) ((transX1 + transX2) / 2.0),
                    (int) ((transY1 + transY2) / 2.0));
            hotspot.addPoint((int) ((transX1 + transX2) / 2.0),
                    (int) transZero);
        }

        if (item == 0) {  // create a new area polygon for the series
            areaState.area = new Polygon();
            // the first point is (x, 0)
            double zero = rangeAxis.valueToJava2D(0.0, dataArea,
                    plot.getRangeAxisEdge());
            if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                areaState.area.addPoint((int) transX1, (int) zero);
            }
            else if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
                areaState.area.addPoint((int) zero, (int) transX1);
            }
        }

        // Add each point to Area (x, y)
        if (plot.getOrientation() == PlotOrientation.VERTICAL) {
            areaState.area.addPoint((int) transX1, (int) transY1);
        }
        else if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
            areaState.area.addPoint((int) transY1, (int) transX1);
        }

        PlotOrientation orientation = plot.getOrientation();
        Paint paint = getItemPaint(series, item);
        Stroke stroke = getItemStroke(series, item);
        g2.setPaint(paint);
        g2.setStroke(stroke);

        Shape shape = null;
        if (getPlotShapes()) {
            shape = getItemShape(series, item);
            if (orientation == PlotOrientation.VERTICAL) {
                shape = ShapeUtilities.createTranslatedShape(shape, transX1,
                        transY1);
            }
            else if (orientation == PlotOrientation.HORIZONTAL) {
                shape = ShapeUtilities.createTranslatedShape(shape, transY1,
                        transX1);
            }
            g2.draw(shape);
        }

        if (getPlotLines()) {
            if (item > 0) {
                if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                    areaState.line.setLine(transX0, transY0, transX1, transY1);
                }
                else if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
                    areaState.line.setLine(transY0, transX0, transY1, transX1);
                }
                g2.draw(areaState.line);
            }
        }

        // Check if the item is the last item for the series.
        // and number of items > 0.  We can't draw an area for a single point.
        if (getPlotArea() && item > 0 && item == (itemCount - 1)) {

            if (orientation == PlotOrientation.VERTICAL) {
                // Add the last point (x,0)
                areaState.area.addPoint((int) transX1, (int) transZero);
            }
            else if (orientation == PlotOrientation.HORIZONTAL) {
                // Add the last point (x,0)
                areaState.area.addPoint((int) transZero, (int) transX1);
            }

            g2.fill(areaState.area);

            // draw an outline around the Area.
            if (isOutline()) {
                Shape area = areaState.area;

                // Java2D has some issues drawing dashed lines around "large"
                // geometrical shapes - for example, see bug 6620013 in the
                // Java bug database.  So, we'll check if the outline is
                // dashed and, if it is, do our own clipping before drawing
                // the outline...
                Stroke outlineStroke = lookupSeriesOutlineStroke(series);
                if (outlineStroke instanceof BasicStroke) {
                    BasicStroke bs = (BasicStroke) outlineStroke;
                    if (bs.getDashArray() != null) {
                        Area poly = new Area(areaState.area);
                        // we make the clip region slightly larger than the
                        // dataArea so that the clipped edges don't show lines
                        // on the chart
                        Area clip = new Area(new Rectangle2D.Double(
                                dataArea.getX() - 5.0, dataArea.getY() - 5.0,
                                dataArea.getWidth() + 10.0,
                                dataArea.getHeight() + 10.0));
                        poly.intersect(clip);
                        area = poly;
                    }
                } // end of workaround

                g2.setStroke(outlineStroke);
                g2.setPaint(lookupSeriesOutlinePaint(series));
                g2.draw(area);
            }
        }

        int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
        int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
        updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex,
                rangeAxisIndex, transX1, transY1, orientation);

        // collect entity and tool tip information...
        EntityCollection entities = state.getEntityCollection();
        if (entities != null && hotspot != null) {
            addEntity(entities, hotspot, dataset, series, item, 0.0, 0.0);
        }

    }

    /**
     * Returns a clone of the renderer.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  if the renderer cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        XYAreaRenderer clone = (XYAreaRenderer) super.clone();
        clone.legendArea = ShapeUtilities.clone(this.legendArea);
        return clone;
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
        if (!(obj instanceof XYAreaRenderer)) {
            return false;
        }
        XYAreaRenderer that = (XYAreaRenderer) obj;
        if (this.plotArea != that.plotArea) {
            return false;
        }
        if (this.plotLines != that.plotLines) {
            return false;
        }
        if (this.plotShapes != that.plotShapes) {
            return false;
        }
        if (this.showOutline != that.showOutline) {
            return false;
        }
        if (!ShapeUtilities.equal(this.legendArea, that.legendArea)) {
            return false;
        }
        return true;
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
        this.legendArea = SerialUtilities.readShape(stream);
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
        SerialUtilities.writeShape(this.legendArea, stream);
    }
}
