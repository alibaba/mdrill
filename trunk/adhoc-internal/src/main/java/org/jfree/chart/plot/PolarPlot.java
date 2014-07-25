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
 * --------------
 * PolarPlot.java
 * --------------
 * (C) Copyright 2004-2008, by Solution Engineering, Inc. and Contributors.
 *
 * Original Author:  Daniel Bridenbecker, Solution Engineering, Inc.;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Martin Hoeller (patch 1871902);
 *
 * Changes
 * -------
 * 19-Jan-2004 : Version 1, contributed by DB with minor changes by DG (DG);
 * 07-Apr-2004 : Changed text bounds calculation (DG);
 * 05-May-2005 : Updated draw() method parameters (DG);
 * 09-Jun-2005 : Fixed getDataRange() and equals() methods (DG);
 * 25-Oct-2005 : Implemented Zoomable (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 07-Feb-2007 : Fixed bug 1599761, data value less than axis minimum (DG);
 * 21-Mar-2007 : Fixed serialization bug (DG);
 * 24-Sep-2007 : Implemented new zooming methods (DG);
 * 17-Feb-2007 : Added angle tick unit attribute (see patch 1871902 by
 *               Martin Hoeller) (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.renderer.PolarItemRenderer;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;

/**
 * Plots data that is in (theta, radius) pairs where
 * theta equal to zero is due north and increases clockwise.
 */
public class PolarPlot extends Plot implements ValueAxisPlot, Zoomable,
        RendererChangeListener, Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 3794383185924179525L;

    /** The default margin. */
    private static final int MARGIN = 20;

    /** The annotation margin. */
    private static final double ANNOTATION_MARGIN = 7.0;

    /**
     * The default angle tick unit size.
     *
     * @since 1.0.10
     */
    public static final double DEFAULT_ANGLE_TICK_UNIT_SIZE = 45.0;

    /** The default grid line stroke. */
    public static final Stroke DEFAULT_GRIDLINE_STROKE = new BasicStroke(
            0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
            0.0f, new float[]{2.0f, 2.0f}, 0.0f);

    /** The default grid line paint. */
    public static final Paint DEFAULT_GRIDLINE_PAINT = Color.gray;

    /** The resourceBundle for the localization. */
    protected static ResourceBundle localizationResources
            = ResourceBundleWrapper.getBundle(
                    "org.jfree.chart.plot.LocalizationBundle");

    /** The angles that are marked with gridlines. */
    private List angleTicks;

    /** The axis (used for the y-values). */
    private ValueAxis axis;

    /** The dataset. */
    private XYDataset dataset;

    /**
     * Object responsible for drawing the visual representation of each point
     * on the plot.
     */
    private PolarItemRenderer renderer;

    /**
     * The tick unit that controls the spacing between the angular grid lines.
     *
     * @since 1.0.10
     */
    private TickUnit angleTickUnit;

    /** A flag that controls whether or not the angle labels are visible. */
    private boolean angleLabelsVisible = true;

    /** The font used to display the angle labels - never null. */
    private Font angleLabelFont = new Font("SansSerif", Font.PLAIN, 12);

    /** The paint used to display the angle labels. */
    private transient Paint angleLabelPaint = Color.black;

    /** A flag that controls whether the angular grid-lines are visible. */
    private boolean angleGridlinesVisible;

    /** The stroke used to draw the angular grid-lines. */
    private transient Stroke angleGridlineStroke;

    /** The paint used to draw the angular grid-lines. */
    private transient Paint angleGridlinePaint;

    /** A flag that controls whether the radius grid-lines are visible. */
    private boolean radiusGridlinesVisible;

    /** The stroke used to draw the radius grid-lines. */
    private transient Stroke radiusGridlineStroke;

    /** The paint used to draw the radius grid-lines. */
    private transient Paint radiusGridlinePaint;

    /** The annotations for the plot. */
    private List cornerTextItems = new ArrayList();

    /**
     * Default constructor.
     */
    public PolarPlot() {
        this(null, null, null);
    }

   /**
     * Creates a new plot.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     * @param radiusAxis  the radius axis (<code>null</code> permitted).
     * @param renderer  the renderer (<code>null</code> permitted).
     */
    public PolarPlot(XYDataset dataset,
                     ValueAxis radiusAxis,
                     PolarItemRenderer renderer) {

        super();

        this.dataset = dataset;
        if (this.dataset != null) {
            this.dataset.addChangeListener(this);
        }
        this.angleTickUnit = new NumberTickUnit(DEFAULT_ANGLE_TICK_UNIT_SIZE);

        this.axis = radiusAxis;
        if (this.axis != null) {
            this.axis.setPlot(this);
            this.axis.addChangeListener(this);
        }

        this.renderer = renderer;
        if (this.renderer != null) {
            this.renderer.setPlot(this);
            this.renderer.addChangeListener(this);
        }

        this.angleGridlinesVisible = true;
        this.angleGridlineStroke = DEFAULT_GRIDLINE_STROKE;
        this.angleGridlinePaint = DEFAULT_GRIDLINE_PAINT;

        this.radiusGridlinesVisible = true;
        this.radiusGridlineStroke = DEFAULT_GRIDLINE_STROKE;
        this.radiusGridlinePaint = DEFAULT_GRIDLINE_PAINT;
    }

    /**
     * Add text to be displayed in the lower right hand corner and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param text  the text to display (<code>null</code> not permitted).
     *
     * @see #removeCornerTextItem(String)
     */
    public void addCornerTextItem(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Null 'text' argument.");
        }
        this.cornerTextItems.add(text);
        fireChangeEvent();
    }

    /**
     * Remove the given text from the list of corner text items and
     * sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param text  the text to remove (<code>null</code> ignored).
     *
     * @see #addCornerTextItem(String)
     */
    public void removeCornerTextItem(String text) {
        boolean removed = this.cornerTextItems.remove(text);
        if (removed) {
            fireChangeEvent();
        }
    }

    /**
     * Clear the list of corner text items and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     *
     * @see #addCornerTextItem(String)
     * @see #removeCornerTextItem(String)
     */
    public void clearCornerTextItems() {
        if (this.cornerTextItems.size() > 0) {
            this.cornerTextItems.clear();
            fireChangeEvent();
        }
    }

    /**
     * Returns the plot type as a string.
     *
     * @return A short string describing the type of plot.
     */
    public String getPlotType() {
       return PolarPlot.localizationResources.getString("Polar_Plot");
    }

    /**
     * Returns the axis for the plot.
     *
     * @return The radius axis (possibly <code>null</code>).
     *
     * @see #setAxis(ValueAxis)
     */
    public ValueAxis getAxis() {
        return this.axis;
    }

    /**
     * Sets the axis for the plot and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param axis  the new axis (<code>null</code> permitted).
     */
    public void setAxis(ValueAxis axis) {
        if (axis != null) {
            axis.setPlot(this);
        }

        // plot is likely registered as a listener with the existing axis...
        if (this.axis != null) {
            this.axis.removeChangeListener(this);
        }

        this.axis = axis;
        if (this.axis != null) {
            this.axis.configure();
            this.axis.addChangeListener(this);
        }
        fireChangeEvent();
    }

    /**
     * Returns the primary dataset for the plot.
     *
     * @return The primary dataset (possibly <code>null</code>).
     *
     * @see #setDataset(XYDataset)
     */
    public XYDataset getDataset() {
        return this.dataset;
    }

    /**
     * Sets the dataset for the plot, replacing the existing dataset if there
     * is one.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @see #getDataset()
     */
    public void setDataset(XYDataset dataset) {
        // if there is an existing dataset, remove the plot from the list of
        // change listeners...
        XYDataset existing = this.dataset;
        if (existing != null) {
            existing.removeChangeListener(this);
        }

        // set the new m_Dataset, and register the chart as a change listener...
        this.dataset = dataset;
        if (this.dataset != null) {
            setDatasetGroup(this.dataset.getGroup());
            this.dataset.addChangeListener(this);
        }

        // send a m_Dataset change event to self...
        DatasetChangeEvent event = new DatasetChangeEvent(this, this.dataset);
        datasetChanged(event);
    }

    /**
     * Returns the item renderer.
     *
     * @return The renderer (possibly <code>null</code>).
     *
     * @see #setRenderer(PolarItemRenderer)
     */
    public PolarItemRenderer getRenderer() {
        return this.renderer;
    }

    /**
     * Sets the item renderer, and notifies all listeners of a change to the
     * plot.
     * <P>
     * If the renderer is set to <code>null</code>, no chart will be drawn.
     *
     * @param renderer  the new renderer (<code>null</code> permitted).
     *
     * @see #getRenderer()
     */
    public void setRenderer(PolarItemRenderer renderer) {
        if (this.renderer != null) {
            this.renderer.removeChangeListener(this);
        }

        this.renderer = renderer;
        if (this.renderer != null) {
            this.renderer.setPlot(this);
        }
        fireChangeEvent();
    }

    /**
     * Returns the tick unit that controls the spacing of the angular grid
     * lines.
     *
     * @return The tick unit (never <code>null</code>).
     *
     * @since 1.0.10
     */
    public TickUnit getAngleTickUnit() {
        return this.angleTickUnit;
    }

    /**
     * Sets the tick unit that controls the spacing of the angular grid
     * lines, and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param unit  the tick unit (<code>null</code> not permitted).
     *
     * @since 1.0.10
     */
    public void setAngleTickUnit(TickUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Null 'unit' argument.");
        }
        this.angleTickUnit = unit;
        fireChangeEvent();
    }

    /**
     * Returns a flag that controls whether or not the angle labels are visible.
     *
     * @return A boolean.
     *
     * @see #setAngleLabelsVisible(boolean)
     */
    public boolean isAngleLabelsVisible() {
        return this.angleLabelsVisible;
    }

    /**
     * Sets the flag that controls whether or not the angle labels are visible,
     * and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param visible  the flag.
     *
     * @see #isAngleLabelsVisible()
     */
    public void setAngleLabelsVisible(boolean visible) {
        if (this.angleLabelsVisible != visible) {
            this.angleLabelsVisible = visible;
            fireChangeEvent();
        }
    }

    /**
     * Returns the font used to display the angle labels.
     *
     * @return A font (never <code>null</code>).
     *
     * @see #setAngleLabelFont(Font)
     */
    public Font getAngleLabelFont() {
        return this.angleLabelFont;
    }

    /**
     * Sets the font used to display the angle labels and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param font  the font (<code>null</code> not permitted).
     *
     * @see #getAngleLabelFont()
     */
    public void setAngleLabelFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }
        this.angleLabelFont = font;
        fireChangeEvent();
    }

    /**
     * Returns the paint used to display the angle labels.
     *
     * @return A paint (never <code>null</code>).
     *
     * @see #setAngleLabelPaint(Paint)
     */
    public Paint getAngleLabelPaint() {
        return this.angleLabelPaint;
    }

    /**
     * Sets the paint used to display the angle labels and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     */
    public void setAngleLabelPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.angleLabelPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns <code>true</code> if the angular gridlines are visible, and
     * <code>false<code> otherwise.
     *
     * @return <code>true</code> or <code>false</code>.
     *
     * @see #setAngleGridlinesVisible(boolean)
     */
    public boolean isAngleGridlinesVisible() {
        return this.angleGridlinesVisible;
    }

    /**
     * Sets the flag that controls whether or not the angular grid-lines are
     * visible.
     * <p>
     * If the flag value is changed, a {@link PlotChangeEvent} is sent to all
     * registered listeners.
     *
     * @param visible  the new value of the flag.
     *
     * @see #isAngleGridlinesVisible()
     */
    public void setAngleGridlinesVisible(boolean visible) {
        if (this.angleGridlinesVisible != visible) {
            this.angleGridlinesVisible = visible;
            fireChangeEvent();
        }
    }

    /**
     * Returns the stroke for the grid-lines (if any) plotted against the
     * angular axis.
     *
     * @return The stroke (possibly <code>null</code>).
     *
     * @see #setAngleGridlineStroke(Stroke)
     */
    public Stroke getAngleGridlineStroke() {
        return this.angleGridlineStroke;
    }

    /**
     * Sets the stroke for the grid lines plotted against the angular axis and
     * sends a {@link PlotChangeEvent} to all registered listeners.
     * <p>
     * If you set this to <code>null</code>, no grid lines will be drawn.
     *
     * @param stroke  the stroke (<code>null</code> permitted).
     *
     * @see #getAngleGridlineStroke()
     */
    public void setAngleGridlineStroke(Stroke stroke) {
        this.angleGridlineStroke = stroke;
        fireChangeEvent();
    }

    /**
     * Returns the paint for the grid lines (if any) plotted against the
     * angular axis.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setAngleGridlinePaint(Paint)
     */
    public Paint getAngleGridlinePaint() {
        return this.angleGridlinePaint;
    }

    /**
     * Sets the paint for the grid lines plotted against the angular axis.
     * <p>
     * If you set this to <code>null</code>, no grid lines will be drawn.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getAngleGridlinePaint()
     */
    public void setAngleGridlinePaint(Paint paint) {
        this.angleGridlinePaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns <code>true</code> if the radius axis grid is visible, and
     * <code>false<code> otherwise.
     *
     * @return <code>true</code> or <code>false</code>.
     *
     * @see #setRadiusGridlinesVisible(boolean)
     */
    public boolean isRadiusGridlinesVisible() {
        return this.radiusGridlinesVisible;
    }

    /**
     * Sets the flag that controls whether or not the radius axis grid lines
     * are visible.
     * <p>
     * If the flag value is changed, a {@link PlotChangeEvent} is sent to all
     * registered listeners.
     *
     * @param visible  the new value of the flag.
     *
     * @see #isRadiusGridlinesVisible()
     */
    public void setRadiusGridlinesVisible(boolean visible) {
        if (this.radiusGridlinesVisible != visible) {
            this.radiusGridlinesVisible = visible;
            fireChangeEvent();
        }
    }

    /**
     * Returns the stroke for the grid lines (if any) plotted against the
     * radius axis.
     *
     * @return The stroke (possibly <code>null</code>).
     *
     * @see #setRadiusGridlineStroke(Stroke)
     */
    public Stroke getRadiusGridlineStroke() {
        return this.radiusGridlineStroke;
    }

    /**
     * Sets the stroke for the grid lines plotted against the radius axis and
     * sends a {@link PlotChangeEvent} to all registered listeners.
     * <p>
     * If you set this to <code>null</code>, no grid lines will be drawn.
     *
     * @param stroke  the stroke (<code>null</code> permitted).
     *
     * @see #getRadiusGridlineStroke()
     */
    public void setRadiusGridlineStroke(Stroke stroke) {
        this.radiusGridlineStroke = stroke;
        fireChangeEvent();
    }

    /**
     * Returns the paint for the grid lines (if any) plotted against the radius
     * axis.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setRadiusGridlinePaint(Paint)
     */
    public Paint getRadiusGridlinePaint() {
        return this.radiusGridlinePaint;
    }

    /**
     * Sets the paint for the grid lines plotted against the radius axis and
     * sends a {@link PlotChangeEvent} to all registered listeners.
     * <p>
     * If you set this to <code>null</code>, no grid lines will be drawn.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getRadiusGridlinePaint()
     */
    public void setRadiusGridlinePaint(Paint paint) {
        this.radiusGridlinePaint = paint;
        fireChangeEvent();
    }

    /**
     * Generates a list of tick values for the angular tick marks.
     *
     * @return A list of {@link NumberTick} instances.
     *
     * @since 1.0.10
     */
    protected List refreshAngleTicks() {
        List ticks = new ArrayList();
        for (double currentTickVal = 0.0; currentTickVal < 360.0;
                currentTickVal += this.angleTickUnit.getSize()) {
            NumberTick tick = new NumberTick(new Double(currentTickVal),
                this.angleTickUnit.valueToString(currentTickVal),
                TextAnchor.CENTER, TextAnchor.CENTER, 0.0);
            ticks.add(tick);
        }
        return ticks;
    }

    /**
     * Draws the plot on a Java 2D graphics device (such as the screen or a
     * printer).
     * <P>
     * This plot relies on a {@link PolarItemRenderer} to draw each
     * item in the plot.  This allows the visual representation of the data to
     * be changed easily.
     * <P>
     * The optional info argument collects information about the rendering of
     * the plot (dimensions, tooltip information etc).  Just pass in
     * <code>null</code> if you do not need this information.
     *
     * @param g2  the graphics device.
     * @param area  the area within which the plot (including axes and
     *              labels) should be drawn.
     * @param anchor  the anchor point (<code>null</code> permitted).
     * @param parentState  ignored.
     * @param info  collects chart drawing information (<code>null</code>
     *              permitted).
     */
    public void draw(Graphics2D g2,
                     Rectangle2D area,
                     Point2D anchor,
                     PlotState parentState,
                     PlotRenderingInfo info) {

        // if the plot area is too small, just return...
        boolean b1 = (area.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
        boolean b2 = (area.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
        if (b1 || b2) {
            return;
        }

        // record the plot area...
        if (info != null) {
            info.setPlotArea(area);
        }

        // adjust the drawing area for the plot insets (if any)...
        RectangleInsets insets = getInsets();
        insets.trim(area);

        Rectangle2D dataArea = area;
        if (info != null) {
            info.setDataArea(dataArea);
        }

        // draw the plot background and axes...
        drawBackground(g2, dataArea);
        double h = Math.min(dataArea.getWidth() / 2.0,
                dataArea.getHeight() / 2.0) - MARGIN;
        Rectangle2D quadrant = new Rectangle2D.Double(dataArea.getCenterX(),
                dataArea.getCenterY(), h, h);
        AxisState state = drawAxis(g2, area, quadrant);
        if (this.renderer != null) {
            Shape originalClip = g2.getClip();
            Composite originalComposite = g2.getComposite();

            g2.clip(dataArea);
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, getForegroundAlpha()));

            this.angleTicks = refreshAngleTicks();
            drawGridlines(g2, dataArea, this.angleTicks, state.getTicks());

            // draw...
            render(g2, dataArea, info);

            g2.setClip(originalClip);
            g2.setComposite(originalComposite);
        }
        drawOutline(g2, dataArea);
        drawCornerTextItems(g2, dataArea);
    }

    /**
     * Draws the corner text items.
     *
     * @param g2  the drawing surface.
     * @param area  the area.
     */
    protected void drawCornerTextItems(Graphics2D g2, Rectangle2D area) {
        if (this.cornerTextItems.isEmpty()) {
            return;
        }

        g2.setColor(Color.black);
        double width = 0.0;
        double height = 0.0;
        for (Iterator it = this.cornerTextItems.iterator(); it.hasNext();) {
            String msg = (String) it.next();
            FontMetrics fm = g2.getFontMetrics();
            Rectangle2D bounds = TextUtilities.getTextBounds(msg, g2, fm);
            width = Math.max(width, bounds.getWidth());
            height += bounds.getHeight();
        }

        double xadj = ANNOTATION_MARGIN * 2.0;
        double yadj = ANNOTATION_MARGIN;
        width += xadj;
        height += yadj;

        double x = area.getMaxX() - width;
        double y = area.getMaxY() - height;
        g2.drawRect((int) x, (int) y, (int) width, (int) height);
        x += ANNOTATION_MARGIN;
        for (Iterator it = this.cornerTextItems.iterator(); it.hasNext();) {
            String msg = (String) it.next();
            Rectangle2D bounds = TextUtilities.getTextBounds(msg, g2,
                    g2.getFontMetrics());
            y += bounds.getHeight();
            g2.drawString(msg, (int) x, (int) y);
        }
    }

    /**
     * A utility method for drawing the axes.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param dataArea  the data area.
     *
     * @return A map containing the axis states.
     */
    protected AxisState drawAxis(Graphics2D g2, Rectangle2D plotArea,
                                 Rectangle2D dataArea) {
        return this.axis.draw(g2, dataArea.getMinY(), plotArea, dataArea,
                RectangleEdge.TOP, null);
    }

    /**
     * Draws a representation of the data within the dataArea region, using the
     * current m_Renderer.
     *
     * @param g2  the graphics device.
     * @param dataArea  the region in which the data is to be drawn.
     * @param info  an optional object for collection dimension
     *              information (<code>null</code> permitted).
     */
    protected void render(Graphics2D g2,
                       Rectangle2D dataArea,
                       PlotRenderingInfo info) {

        // now get the data and plot it (the visual representation will depend
        // on the m_Renderer that has been set)...
        if (!DatasetUtilities.isEmptyOrNull(this.dataset)) {
            int seriesCount = this.dataset.getSeriesCount();
            for (int series = 0; series < seriesCount; series++) {
                this.renderer.drawSeries(g2, dataArea, info, this,
                        this.dataset, series);
            }
        }
        else {
            drawNoDataMessage(g2, dataArea);
        }
    }

    /**
     * Draws the gridlines for the plot, if they are visible.
     *
     * @param g2  the graphics device.
     * @param dataArea  the data area.
     * @param angularTicks  the ticks for the angular axis.
     * @param radialTicks  the ticks for the radial axis.
     */
    protected void drawGridlines(Graphics2D g2, Rectangle2D dataArea,
                                 List angularTicks, List radialTicks) {

        // no renderer, no gridlines...
        if (this.renderer == null) {
            return;
        }

        // draw the domain grid lines, if any...
        if (isAngleGridlinesVisible()) {
            Stroke gridStroke = getAngleGridlineStroke();
            Paint gridPaint = getAngleGridlinePaint();
            if ((gridStroke != null) && (gridPaint != null)) {
                this.renderer.drawAngularGridLines(g2, this, angularTicks,
                        dataArea);
            }
        }

        // draw the radius grid lines, if any...
        if (isRadiusGridlinesVisible()) {
            Stroke gridStroke = getRadiusGridlineStroke();
            Paint gridPaint = getRadiusGridlinePaint();
            if ((gridStroke != null) && (gridPaint != null)) {
                this.renderer.drawRadialGridLines(g2, this, this.axis,
                        radialTicks, dataArea);
            }
        }
    }

    /**
     * Zooms the axis ranges by the specified percentage about the anchor point.
     *
     * @param percent  the amount of the zoom.
     */
    public void zoom(double percent) {
        if (percent > 0.0) {
            double radius = getMaxRadius();
            double scaledRadius = radius * percent;
            this.axis.setUpperBound(scaledRadius);
            getAxis().setAutoRange(false);
        }
        else {
            getAxis().setAutoRange(true);
        }
    }

    /**
     * Returns the range for the specified axis.
     *
     * @param axis  the axis.
     *
     * @return The range.
     */
    public Range getDataRange(ValueAxis axis) {
        Range result = null;
        if (this.dataset != null) {
            result = Range.combine(result,
                    DatasetUtilities.findRangeBounds(this.dataset));
        }
        return result;
    }

    /**
     * Receives notification of a change to the plot's m_Dataset.
     * <P>
     * The axis ranges are updated if necessary.
     *
     * @param event  information about the event (not used here).
     */
    public void datasetChanged(DatasetChangeEvent event) {

        if (this.axis != null) {
            this.axis.configure();
        }

        if (getParent() != null) {
            getParent().datasetChanged(event);
        }
        else {
            super.datasetChanged(event);
        }
    }

    /**
     * Notifies all registered listeners of a property change.
     * <P>
     * One source of property change events is the plot's m_Renderer.
     *
     * @param event  information about the property change.
     */
    public void rendererChanged(RendererChangeEvent event) {
        fireChangeEvent();
    }

    /**
     * Returns the number of series in the dataset for this plot.  If the
     * dataset is <code>null</code>, the method returns 0.
     *
     * @return The series count.
     */
    public int getSeriesCount() {
        int result = 0;

        if (this.dataset != null) {
            result = this.dataset.getSeriesCount();
        }
        return result;
    }

    /**
     * Returns the legend items for the plot.  Each legend item is generated by
     * the plot's m_Renderer, since the m_Renderer is responsible for the visual
     * representation of the data.
     *
     * @return The legend items.
     */
    public LegendItemCollection getLegendItems() {
        LegendItemCollection result = new LegendItemCollection();

        // get the legend items for the main m_Dataset...
        if (this.dataset != null) {
            if (this.renderer != null) {
                int seriesCount = this.dataset.getSeriesCount();
                for (int i = 0; i < seriesCount; i++) {
                    LegendItem item = this.renderer.getLegendItem(i);
                    result.add(item);
                }
            }
        }
        return result;
    }

    /**
     * Tests this plot for equality with another object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PolarPlot)) {
            return false;
        }
        PolarPlot that = (PolarPlot) obj;
        if (!ObjectUtilities.equal(this.axis, that.axis)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.renderer, that.renderer)) {
            return false;
        }
        if (!this.angleTickUnit.equals(that.angleTickUnit)) {
            return false;
        }
        if (this.angleGridlinesVisible != that.angleGridlinesVisible) {
            return false;
        }
        if (this.angleLabelsVisible != that.angleLabelsVisible) {
            return false;
        }
        if (!this.angleLabelFont.equals(that.angleLabelFont)) {
            return false;
        }
        if (!PaintUtilities.equal(this.angleLabelPaint, that.angleLabelPaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.angleGridlineStroke,
                that.angleGridlineStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(
            this.angleGridlinePaint, that.angleGridlinePaint
        )) {
            return false;
        }
        if (this.radiusGridlinesVisible != that.radiusGridlinesVisible) {
            return false;
        }
        if (!ObjectUtilities.equal(this.radiusGridlineStroke,
                that.radiusGridlineStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.radiusGridlinePaint,
                that.radiusGridlinePaint)) {
            return false;
        }
        if (!this.cornerTextItems.equals(that.cornerTextItems)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns a clone of the plot.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  this can occur if some component of
     *         the plot cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {

        PolarPlot clone = (PolarPlot) super.clone();
        if (this.axis != null) {
            clone.axis = (ValueAxis) ObjectUtilities.clone(this.axis);
            clone.axis.setPlot(clone);
            clone.axis.addChangeListener(clone);
        }

        if (clone.dataset != null) {
            clone.dataset.addChangeListener(clone);
        }

        if (this.renderer != null) {
            clone.renderer
                = (PolarItemRenderer) ObjectUtilities.clone(this.renderer);
        }

        clone.cornerTextItems = new ArrayList(this.cornerTextItems);

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
        SerialUtilities.writeStroke(this.angleGridlineStroke, stream);
        SerialUtilities.writePaint(this.angleGridlinePaint, stream);
        SerialUtilities.writeStroke(this.radiusGridlineStroke, stream);
        SerialUtilities.writePaint(this.radiusGridlinePaint, stream);
        SerialUtilities.writePaint(this.angleLabelPaint, stream);
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
        this.angleGridlineStroke = SerialUtilities.readStroke(stream);
        this.angleGridlinePaint = SerialUtilities.readPaint(stream);
        this.radiusGridlineStroke = SerialUtilities.readStroke(stream);
        this.radiusGridlinePaint = SerialUtilities.readPaint(stream);
        this.angleLabelPaint = SerialUtilities.readPaint(stream);

        if (this.axis != null) {
            this.axis.setPlot(this);
            this.axis.addChangeListener(this);
        }

        if (this.dataset != null) {
            this.dataset.addChangeListener(this);
        }
    }

    /**
     * This method is required by the {@link Zoomable} interface, but since
     * the plot does not have any domain axes, it does nothing.
     *
     * @param factor  the zoom factor.
     * @param state  the plot state.
     * @param source  the source point (in Java2D coordinates).
     */
    public void zoomDomainAxes(double factor, PlotRenderingInfo state,
                               Point2D source) {
        // do nothing
    }

    /**
     * This method is required by the {@link Zoomable} interface, but since
     * the plot does not have any domain axes, it does nothing.
     *
     * @param factor  the zoom factor.
     * @param state  the plot state.
     * @param source  the source point (in Java2D coordinates).
     * @param useAnchor  use source point as zoom anchor?
     *
     * @since 1.0.7
     */
    public void zoomDomainAxes(double factor, PlotRenderingInfo state,
                               Point2D source, boolean useAnchor) {
        // do nothing
    }

    /**
     * This method is required by the {@link Zoomable} interface, but since
     * the plot does not have any domain axes, it does nothing.
     *
     * @param lowerPercent  the new lower bound.
     * @param upperPercent  the new upper bound.
     * @param state  the plot state.
     * @param source  the source point (in Java2D coordinates).
     */
    public void zoomDomainAxes(double lowerPercent, double upperPercent,
                               PlotRenderingInfo state, Point2D source) {
        // do nothing
    }

    /**
     * Multiplies the range on the range axis/axes by the specified factor.
     *
     * @param factor  the zoom factor.
     * @param state  the plot state.
     * @param source  the source point (in Java2D coordinates).
     */
    public void zoomRangeAxes(double factor, PlotRenderingInfo state,
                              Point2D source) {
        zoom(factor);
    }

    /**
     * Multiplies the range on the range axis by the specified factor.
     *
     * @param factor  the zoom factor.
     * @param info  the plot rendering info.
     * @param source  the source point (in Java2D space).
     * @param useAnchor  use source point as zoom anchor?
     *
     * @see #zoomDomainAxes(double, PlotRenderingInfo, Point2D, boolean)
     *
     * @since 1.0.7
     */
    public void zoomRangeAxes(double factor, PlotRenderingInfo info,
                              Point2D source, boolean useAnchor) {

        if (useAnchor) {
            // get the source coordinate - this plot has always a VERTICAL
            // orientation
            double sourceX = source.getX();
            double anchorX = this.axis.java2DToValue(sourceX,
                    info.getDataArea(), RectangleEdge.BOTTOM);
            this.axis.resizeRange(factor, anchorX);
        }
        else {
            this.axis.resizeRange(factor);
        }

    }

    /**
     * Zooms in on the range axes.
     *
     * @param lowerPercent  the new lower bound.
     * @param upperPercent  the new upper bound.
     * @param state  the plot state.
     * @param source  the source point (in Java2D coordinates).
     */
    public void zoomRangeAxes(double lowerPercent, double upperPercent,
                              PlotRenderingInfo state, Point2D source) {
        zoom((upperPercent + lowerPercent) / 2.0);
    }

    /**
     * Returns <code>false</code> always.
     *
     * @return <code>false</code> always.
     */
    public boolean isDomainZoomable() {
        return false;
    }

    /**
     * Returns <code>true</code> to indicate that the range axis is zoomable.
     *
     * @return <code>true</code>.
     */
    public boolean isRangeZoomable() {
        return true;
    }

    /**
     * Returns the orientation of the plot.
     *
     * @return The orientation.
     */
    public PlotOrientation getOrientation() {
        return PlotOrientation.HORIZONTAL;
    }

    /**
     * Returns the upper bound of the radius axis.
     *
     * @return The upper bound.
     */
    public double getMaxRadius() {
        return this.axis.getUpperBound();
    }

    /**
     * Translates a (theta, radius) pair into Java2D coordinates.  If
     * <code>radius</code> is less than the lower bound of the axis, then
     * this method returns the centre point.
     *
     * @param angleDegrees  the angle in degrees.
     * @param radius  the radius.
     * @param dataArea  the data area.
     *
     * @return A point in Java2D space.
     */
    public Point translateValueThetaRadiusToJava2D(double angleDegrees,
                                                   double radius,
                                                   Rectangle2D dataArea) {

        double radians = Math.toRadians(angleDegrees - 90.0);

        double minx = dataArea.getMinX() + MARGIN;
        double maxx = dataArea.getMaxX() - MARGIN;
        double miny = dataArea.getMinY() + MARGIN;
        double maxy = dataArea.getMaxY() - MARGIN;

        double lengthX = maxx - minx;
        double lengthY = maxy - miny;
        double length = Math.min(lengthX, lengthY);

        double midX = minx + lengthX / 2.0;
        double midY = miny + lengthY / 2.0;

        double axisMin = this.axis.getLowerBound();
        double axisMax =  getMaxRadius();
        double adjustedRadius = Math.max(radius, axisMin);

        double xv = length / 2.0 * Math.cos(radians);
        double yv = length / 2.0 * Math.sin(radians);

        float x = (float) (midX + (xv * (adjustedRadius - axisMin)
                / (axisMax - axisMin)));
        float y = (float) (midY + (yv * (adjustedRadius - axisMin)
                / (axisMax - axisMin)));

        int ix = Math.round(x);
        int iy = Math.round(y);

        Point p = new Point(ix, iy);
        return p;

    }

}
