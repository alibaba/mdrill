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
 * ------------------
 * BarRenderer3D.java
 * ------------------
 * (C) Copyright 2001-2009, by Serge V. Grachov and Contributors.
 *
 * Original Author:  Serge V. Grachov;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Tin Luu;
 *                   Milo Simpson;
 *                   Richard Atkinson;
 *                   Rich Unger;
 *                   Christian W. Zuckschwerdt;
 *
 * Changes
 * -------
 * 31-Oct-2001 : First version, contributed by Serge V. Grachov (DG);
 * 15-Nov-2001 : Modified to allow for null data values (DG);
 * 13-Dec-2001 : Added tooltips (DG);
 * 16-Jan-2002 : Added fix for single category or single series datasets,
 *               pointed out by Taoufik Romdhane (DG);
 * 24-May-2002 : Incorporated tooltips into chart entities (DG);
 * 11-Jun-2002 : Added check for (permitted) null info object, bug and fix
 *               reported by David Basten.  Also updated Javadocs. (DG);
 * 19-Jun-2002 : Added code to draw labels on bars (TL);
 * 26-Jun-2002 : Added bar clipping to avoid PRExceptions (DG);
 * 05-Aug-2002 : Small modification to drawCategoryItem method to support URLs
 *               for HTML image maps (RA);
 * 06-Aug-2002 : Value labels now use number formatter, thanks to Milo
 *               Simpson (DG);
 * 08-Aug-2002 : Applied fixed in bug id 592218 (DG);
 * 20-Sep-2002 : Added fix for categoryPaint by Rich Unger, and fixed errors
 *               reported by Checkstyle (DG);
 * 24-Oct-2002 : Amendments for changes in CategoryDataset interface and
 *               CategoryToolTipGenerator interface (DG);
 * 05-Nov-2002 : Replaced references to CategoryDataset with TableDataset (DG);
 * 06-Nov-2002 : Moved to the com.jrefinery.chart.renderer package (DG);
 * 28-Jan-2003 : Added an attribute to control the shading of the left and
 *               bottom walls in the plot background (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 10-Apr-2003 : Removed category paint usage (DG);
 * 13-May-2003 : Renamed VerticalBarRenderer3D --> BarRenderer3D and merged with
 *               HorizontalBarRenderer3D (DG);
 * 30-Jul-2003 : Modified entity constructor (CZ);
 * 19-Aug-2003 : Implemented Cloneable and PublicCloneable (DG);
 * 07-Oct-2003 : Added renderer state (DG);
 * 08-Oct-2003 : Removed clipping (replaced with flag in CategoryPlot to
 *               control order in which the data items are processed) (DG);
 * 20-Oct-2003 : Fixed bug (outline stroke not being used for bar
 *               outlines) (DG);
 * 21-Oct-2003 : Bar width moved into CategoryItemRendererState (DG);
 * 24-Nov-2003 : Fixed bug 846324 (item labels not showing) (DG);
 * 27-Nov-2003 : Added code to respect maxBarWidth setting (DG);
 * 02-Feb-2004 : Fixed bug where 'drawBarOutline' flag is not respected (DG);
 * 10-Feb-2004 : Small change to drawItem() method to make cut-and-paste
 *               overriding easier (DG);
 * 04-Oct-2004 : Fixed bug with item label positioning when plot alignment is
 *               horizontal (DG);
 * 05-Nov-2004 : Modified drawItem() signature (DG);
 * 20-Apr-2005 : Renamed CategoryLabelGenerator
 *               --> CategoryItemLabelGenerator (DG);
 * 25-Apr-2005 : Override initialise() method to fix bug 1189642 (DG);
 * 09-Jun-2005 : Use addEntityItem from super class (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 07-Dec-2006 : Implemented equals() override (DG);
 * 17-Jan-2007 : Fixed bug in drawDomainGridline() method (DG);
 * 03-Apr-2007 : Fixed bugs in drawBackground() method (DG);
 * 16-Oct-2007 : Fixed bug in range marker drawing (DG);
 * 19-Mar-2009 : Override for drawRangeLine() method (DG);
 *
 */

package org.jfree.chart.renderer.category;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.Effect3D;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.text.TextUtilities;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A renderer for bars with a 3D effect, for use with the
 * {@link CategoryPlot} class.  The example shown here is generated
 * by the <code>BarChart3DDemo1.java</code> program included in the JFreeChart
 * Demo Collection:
 * <br><br>
 * <img src="../../../../../images/BarRenderer3DSample.png"
 * alt="BarRenderer3DSample.png" />
 */
public class BarRenderer3D extends BarRenderer
        implements Effect3D, Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 7686976503536003636L;

    /** The default x-offset for the 3D effect. */
    public static final double DEFAULT_X_OFFSET = 12.0;

    /** The default y-offset for the 3D effect. */
    public static final double DEFAULT_Y_OFFSET = 8.0;

    /** The default wall paint. */
    public static final Paint DEFAULT_WALL_PAINT = new Color(0xDD, 0xDD, 0xDD);

    /** The size of x-offset for the 3D effect. */
    private double xOffset;

    /** The size of y-offset for the 3D effect. */
    private double yOffset;

    /** The paint used to shade the left and lower 3D wall. */
    private transient Paint wallPaint;

    /**
     * Default constructor, creates a renderer with a default '3D effect'.
     */
    public BarRenderer3D() {
        this(DEFAULT_X_OFFSET, DEFAULT_Y_OFFSET);
    }

    /**
     * Constructs a new renderer with the specified '3D effect'.
     *
     * @param xOffset  the x-offset for the 3D effect.
     * @param yOffset  the y-offset for the 3D effect.
     */
    public BarRenderer3D(double xOffset, double yOffset) {

        super();
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.wallPaint = DEFAULT_WALL_PAINT;
        // set the default item label positions
        ItemLabelPosition p1 = new ItemLabelPosition(ItemLabelAnchor.INSIDE12,
                TextAnchor.TOP_CENTER);
        setBasePositiveItemLabelPosition(p1);
        ItemLabelPosition p2 = new ItemLabelPosition(ItemLabelAnchor.INSIDE12,
                TextAnchor.TOP_CENTER);
        setBaseNegativeItemLabelPosition(p2);

    }

    /**
     * Returns the x-offset for the 3D effect.
     *
     * @return The 3D effect.
     *
     * @see #getYOffset()
     */
    public double getXOffset() {
        return this.xOffset;
    }

    /**
     * Returns the y-offset for the 3D effect.
     *
     * @return The 3D effect.
     */
    public double getYOffset() {
        return this.yOffset;
    }

    /**
     * Returns the paint used to highlight the left and bottom wall in the plot
     * background.
     *
     * @return The paint.
     *
     * @see #setWallPaint(Paint)
     */
    public Paint getWallPaint() {
        return this.wallPaint;
    }

    /**
     * Sets the paint used to hightlight the left and bottom walls in the plot
     * background, and sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getWallPaint()
     */
    public void setWallPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.wallPaint = paint;
        fireChangeEvent();
    }


    /**
     * Initialises the renderer and returns a state object that will be passed
     * to subsequent calls to the drawItem method.  This method gets called
     * once at the start of the process of drawing a chart.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area in which the data is to be plotted.
     * @param plot  the plot.
     * @param rendererIndex  the renderer index.
     * @param info  collects chart rendering information for return to caller.
     *
     * @return The renderer state.
     */
    public CategoryItemRendererState initialise(Graphics2D g2,
                                                Rectangle2D dataArea,
                                                CategoryPlot plot,
                                                int rendererIndex,
                                                PlotRenderingInfo info) {

        Rectangle2D adjusted = new Rectangle2D.Double(dataArea.getX(),
                dataArea.getY() + getYOffset(), dataArea.getWidth()
                - getXOffset(), dataArea.getHeight() - getYOffset());
        CategoryItemRendererState state = super.initialise(g2, adjusted, plot,
                rendererIndex, info);
        return state;

    }

    /**
     * Draws the background for the plot.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param dataArea  the area inside the axes.
     */
    public void drawBackground(Graphics2D g2, CategoryPlot plot,
                               Rectangle2D dataArea) {

        float x0 = (float) dataArea.getX();
        float x1 = x0 + (float) Math.abs(this.xOffset);
        float x3 = (float) dataArea.getMaxX();
        float x2 = x3 - (float) Math.abs(this.xOffset);

        float y0 = (float) dataArea.getMaxY();
        float y1 = y0 - (float) Math.abs(this.yOffset);
        float y3 = (float) dataArea.getMinY();
        float y2 = y3 + (float) Math.abs(this.yOffset);

        GeneralPath clip = new GeneralPath();
        clip.moveTo(x0, y0);
        clip.lineTo(x0, y2);
        clip.lineTo(x1, y3);
        clip.lineTo(x3, y3);
        clip.lineTo(x3, y1);
        clip.lineTo(x2, y0);
        clip.closePath();

        Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                plot.getBackgroundAlpha()));

        // fill background...
        Paint backgroundPaint = plot.getBackgroundPaint();
        if (backgroundPaint != null) {
            g2.setPaint(backgroundPaint);
            g2.fill(clip);
        }

        GeneralPath leftWall = new GeneralPath();
        leftWall.moveTo(x0, y0);
        leftWall.lineTo(x0, y2);
        leftWall.lineTo(x1, y3);
        leftWall.lineTo(x1, y1);
        leftWall.closePath();
        g2.setPaint(getWallPaint());
        g2.fill(leftWall);

        GeneralPath bottomWall = new GeneralPath();
        bottomWall.moveTo(x0, y0);
        bottomWall.lineTo(x1, y1);
        bottomWall.lineTo(x3, y1);
        bottomWall.lineTo(x2, y0);
        bottomWall.closePath();
        g2.setPaint(getWallPaint());
        g2.fill(bottomWall);

        // highlight the background corners...
        g2.setPaint(Color.lightGray);
        Line2D corner = new Line2D.Double(x0, y0, x1, y1);
        g2.draw(corner);
        corner.setLine(x1, y1, x1, y3);
        g2.draw(corner);
        corner.setLine(x1, y1, x3, y1);
        g2.draw(corner);

        // draw background image, if there is one...
        Image backgroundImage = plot.getBackgroundImage();
        if (backgroundImage != null) {
            Rectangle2D adjusted = new Rectangle2D.Double(dataArea.getX()
                    + getXOffset(), dataArea.getY(),
                    dataArea.getWidth() - getXOffset(),
                    dataArea.getHeight() - getYOffset());
            plot.drawBackgroundImage(g2, adjusted);
        }

        g2.setComposite(originalComposite);

    }

    /**
     * Draws the outline for the plot.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param dataArea  the area inside the axes.
     */
    public void drawOutline(Graphics2D g2, CategoryPlot plot,
                            Rectangle2D dataArea) {

        float x0 = (float) dataArea.getX();
        float x1 = x0 + (float) Math.abs(this.xOffset);
        float x3 = (float) dataArea.getMaxX();
        float x2 = x3 - (float) Math.abs(this.xOffset);

        float y0 = (float) dataArea.getMaxY();
        float y1 = y0 - (float) Math.abs(this.yOffset);
        float y3 = (float) dataArea.getMinY();
        float y2 = y3 + (float) Math.abs(this.yOffset);

        GeneralPath clip = new GeneralPath();
        clip.moveTo(x0, y0);
        clip.lineTo(x0, y2);
        clip.lineTo(x1, y3);
        clip.lineTo(x3, y3);
        clip.lineTo(x3, y1);
        clip.lineTo(x2, y0);
        clip.closePath();

        // put an outline around the data area...
        Stroke outlineStroke = plot.getOutlineStroke();
        Paint outlinePaint = plot.getOutlinePaint();
        if ((outlineStroke != null) && (outlinePaint != null)) {
            g2.setStroke(outlineStroke);
            g2.setPaint(outlinePaint);
            g2.draw(clip);
        }

    }

    /**
     * Draws a grid line against the domain axis.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param dataArea  the area for plotting data (not yet adjusted for any
     *                  3D effect).
     * @param value  the Java2D value at which the grid line should be drawn.
     *
     */
    public void drawDomainGridline(Graphics2D g2,
                                   CategoryPlot plot,
                                   Rectangle2D dataArea,
                                   double value) {

        Line2D line1 = null;
        Line2D line2 = null;
        PlotOrientation orientation = plot.getOrientation();
        if (orientation == PlotOrientation.HORIZONTAL) {
            double y0 = value;
            double y1 = value - getYOffset();
            double x0 = dataArea.getMinX();
            double x1 = x0 + getXOffset();
            double x2 = dataArea.getMaxX();
            line1 = new Line2D.Double(x0, y0, x1, y1);
            line2 = new Line2D.Double(x1, y1, x2, y1);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            double x0 = value;
            double x1 = value + getXOffset();
            double y0 = dataArea.getMaxY();
            double y1 = y0 - getYOffset();
            double y2 = dataArea.getMinY();
            line1 = new Line2D.Double(x0, y0, x1, y1);
            line2 = new Line2D.Double(x1, y1, x1, y2);
        }
        Paint paint = plot.getDomainGridlinePaint();
        Stroke stroke = plot.getDomainGridlineStroke();
        g2.setPaint(paint != null ? paint : Plot.DEFAULT_OUTLINE_PAINT);
        g2.setStroke(stroke != null ? stroke : Plot.DEFAULT_OUTLINE_STROKE);
        g2.draw(line1);
        g2.draw(line2);

    }

    /**
     * Draws a grid line against the range axis.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the value axis.
     * @param dataArea  the area for plotting data (not yet adjusted for any
     *                  3D effect).
     * @param value  the value at which the grid line should be drawn.
     *
     */
    public void drawRangeGridline(Graphics2D g2, CategoryPlot plot,
            ValueAxis axis, Rectangle2D dataArea, double value) {

        Range range = axis.getRange();

        if (!range.contains(value)) {
            return;
        }

        Rectangle2D adjusted = new Rectangle2D.Double(dataArea.getX(),
                dataArea.getY() + getYOffset(), dataArea.getWidth()
                - getXOffset(), dataArea.getHeight() - getYOffset());

        Line2D line1 = null;
        Line2D line2 = null;
        PlotOrientation orientation = plot.getOrientation();
        if (orientation == PlotOrientation.HORIZONTAL) {
            double x0 = axis.valueToJava2D(value, adjusted,
                    plot.getRangeAxisEdge());
            double x1 = x0 + getXOffset();
            double y0 = dataArea.getMaxY();
            double y1 = y0 - getYOffset();
            double y2 = dataArea.getMinY();
            line1 = new Line2D.Double(x0, y0, x1, y1);
            line2 = new Line2D.Double(x1, y1, x1, y2);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            double y0 = axis.valueToJava2D(value, adjusted,
                    plot.getRangeAxisEdge());
            double y1 = y0 - getYOffset();
            double x0 = dataArea.getMinX();
            double x1 = x0 + getXOffset();
            double x2 = dataArea.getMaxX();
            line1 = new Line2D.Double(x0, y0, x1, y1);
            line2 = new Line2D.Double(x1, y1, x2, y1);
        }
        Paint paint = plot.getRangeGridlinePaint();
        Stroke stroke = plot.getRangeGridlineStroke();
        g2.setPaint(paint != null ? paint : Plot.DEFAULT_OUTLINE_PAINT);
        g2.setStroke(stroke != null ? stroke : Plot.DEFAULT_OUTLINE_STROKE);
        g2.draw(line1);
        g2.draw(line2);

    }

    /**
     * Draws a line perpendicular to the range axis.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the value axis.
     * @param dataArea  the area for plotting data (not yet adjusted for any 3D
     *                  effect).
     * @param value  the value at which the grid line should be drawn.
     * @param paint  the paint.
     * @param stroke  the stroke.
     *
     * @see #drawRangeGridline
     *
     * @since 1.0.13
     */
    public void drawRangeLine(Graphics2D g2, CategoryPlot plot, ValueAxis axis,
            Rectangle2D dataArea, double value, Paint paint, Stroke stroke) {

        Range range = axis.getRange();
        if (!range.contains(value)) {
            return;
        }

        Rectangle2D adjusted = new Rectangle2D.Double(dataArea.getX(),
                dataArea.getY() + getYOffset(), dataArea.getWidth()
                - getXOffset(), dataArea.getHeight() - getYOffset());

        Line2D line1 = null;
        Line2D line2 = null;
        PlotOrientation orientation = plot.getOrientation();
        if (orientation == PlotOrientation.HORIZONTAL) {
            double x0 = axis.valueToJava2D(value, adjusted,
                    plot.getRangeAxisEdge());
            double x1 = x0 + getXOffset();
            double y0 = dataArea.getMaxY();
            double y1 = y0 - getYOffset();
            double y2 = dataArea.getMinY();
            line1 = new Line2D.Double(x0, y0, x1, y1);
            line2 = new Line2D.Double(x1, y1, x1, y2);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            double y0 = axis.valueToJava2D(value, adjusted,
                    plot.getRangeAxisEdge());
            double y1 = y0 - getYOffset();
            double x0 = dataArea.getMinX();
            double x1 = x0 + getXOffset();
            double x2 = dataArea.getMaxX();
            line1 = new Line2D.Double(x0, y0, x1, y1);
            line2 = new Line2D.Double(x1, y1, x2, y1);
        }
        g2.setPaint(paint);
        g2.setStroke(stroke);
        g2.draw(line1);
        g2.draw(line2);

    }

    /**
     * Draws a range marker.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the value axis.
     * @param marker  the marker.
     * @param dataArea  the area for plotting data (not including 3D effect).
     */
    public void drawRangeMarker(Graphics2D g2,
                                CategoryPlot plot,
                                ValueAxis axis,
                                Marker marker,
                                Rectangle2D dataArea) {


        Rectangle2D adjusted = new Rectangle2D.Double(dataArea.getX(),
                dataArea.getY() + getYOffset(), dataArea.getWidth()
                - getXOffset(), dataArea.getHeight() - getYOffset());
        if (marker instanceof ValueMarker) {
            ValueMarker vm = (ValueMarker) marker;
            double value = vm.getValue();
            Range range = axis.getRange();
            if (!range.contains(value)) {
                return;
            }

            GeneralPath path = null;
            PlotOrientation orientation = plot.getOrientation();
            if (orientation == PlotOrientation.HORIZONTAL) {
                float x = (float) axis.valueToJava2D(value, adjusted,
                        plot.getRangeAxisEdge());
                float y = (float) adjusted.getMaxY();
                path = new GeneralPath();
                path.moveTo(x, y);
                path.lineTo((float) (x + getXOffset()),
                        y - (float) getYOffset());
                path.lineTo((float) (x + getXOffset()),
                        (float) (adjusted.getMinY() - getYOffset()));
                path.lineTo(x, (float) adjusted.getMinY());
                path.closePath();
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                float y = (float) axis.valueToJava2D(value, adjusted,
                        plot.getRangeAxisEdge());
                float x = (float) dataArea.getX();
                path = new GeneralPath();
                path.moveTo(x, y);
                path.lineTo(x + (float) this.xOffset, y - (float) this.yOffset);
                path.lineTo((float) (adjusted.getMaxX() + this.xOffset),
                        y - (float) this.yOffset);
                path.lineTo((float) (adjusted.getMaxX()), y);
                path.closePath();
            }
            g2.setPaint(marker.getPaint());
            g2.fill(path);
            g2.setPaint(marker.getOutlinePaint());
            g2.draw(path);

            String label = marker.getLabel();
            RectangleAnchor anchor = marker.getLabelAnchor();
            if (label != null) {
                Font labelFont = marker.getLabelFont();
                g2.setFont(labelFont);
                g2.setPaint(marker.getLabelPaint());
                Point2D coordinates = calculateRangeMarkerTextAnchorPoint(
                        g2, orientation, dataArea, path.getBounds2D(),
                        marker.getLabelOffset(), LengthAdjustmentType.EXPAND,
                        anchor);
                TextUtilities.drawAlignedString(label, g2,
                        (float) coordinates.getX(), (float) coordinates.getY(),
                        marker.getLabelTextAnchor());
            }

        }
        else {
            super.drawRangeMarker(g2, plot, axis, marker, adjusted);
            // TODO: draw the interval marker with a 3D effect
        }
    }

    /**
     * Draws a 3D bar to represent one data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area for plotting the data.
     * @param plot  the plot.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     * @param pass  the pass index.
     */
    public void drawItem(Graphics2D g2,
                         CategoryItemRendererState state,
                         Rectangle2D dataArea,
                         CategoryPlot plot,
                         CategoryAxis domainAxis,
                         ValueAxis rangeAxis,
                         CategoryDataset dataset,
                         int row,
                         int column,
                         int pass) {

        // check the value we are plotting...
        Number dataValue = dataset.getValue(row, column);
        if (dataValue == null) {
            return;
        }

        double value = dataValue.doubleValue();

        Rectangle2D adjusted = new Rectangle2D.Double(dataArea.getX(),
                dataArea.getY() + getYOffset(),
                dataArea.getWidth() - getXOffset(),
                dataArea.getHeight() - getYOffset());

        PlotOrientation orientation = plot.getOrientation();

        double barW0 = calculateBarW0(plot, orientation, adjusted, domainAxis,
                state, row, column);
        double[] barL0L1 = calculateBarL0L1(value);
        if (barL0L1 == null) {
            return;  // the bar is not visible
        }

        RectangleEdge edge = plot.getRangeAxisEdge();
        double transL0 = rangeAxis.valueToJava2D(barL0L1[0], adjusted, edge);
        double transL1 = rangeAxis.valueToJava2D(barL0L1[1], adjusted, edge);
        double barL0 = Math.min(transL0, transL1);
        double barLength = Math.abs(transL1 - transL0);

        // draw the bar...
        Rectangle2D bar = null;
        if (orientation == PlotOrientation.HORIZONTAL) {
            bar = new Rectangle2D.Double(barL0, barW0, barLength,
                    state.getBarWidth());
        }
        else {
            bar = new Rectangle2D.Double(barW0, barL0, state.getBarWidth(),
                    barLength);
        }
        Paint itemPaint = getItemPaint(row, column);
        g2.setPaint(itemPaint);
        g2.fill(bar);

        double x0 = bar.getMinX();
        double x1 = x0 + getXOffset();
        double x2 = bar.getMaxX();
        double x3 = x2 + getXOffset();

        double y0 = bar.getMinY() - getYOffset();
        double y1 = bar.getMinY();
        double y2 = bar.getMaxY() - getYOffset();
        double y3 = bar.getMaxY();

        GeneralPath bar3dRight = null;
        GeneralPath bar3dTop = null;
        if (barLength > 0.0) {
            bar3dRight = new GeneralPath();
            bar3dRight.moveTo((float) x2, (float) y3);
            bar3dRight.lineTo((float) x2, (float) y1);
            bar3dRight.lineTo((float) x3, (float) y0);
            bar3dRight.lineTo((float) x3, (float) y2);
            bar3dRight.closePath();

            if (itemPaint instanceof Color) {
                g2.setPaint(((Color) itemPaint).darker());
            }
            g2.fill(bar3dRight);
        }

        bar3dTop = new GeneralPath();
        bar3dTop.moveTo((float) x0, (float) y1);
        bar3dTop.lineTo((float) x1, (float) y0);
        bar3dTop.lineTo((float) x3, (float) y0);
        bar3dTop.lineTo((float) x2, (float) y1);
        bar3dTop.closePath();
        g2.fill(bar3dTop);

        if (isDrawBarOutline()
                && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
            g2.setStroke(getItemOutlineStroke(row, column));
            g2.setPaint(getItemOutlinePaint(row, column));
            g2.draw(bar);
            if (bar3dRight != null) {
                g2.draw(bar3dRight);
            }
            if (bar3dTop != null) {
                g2.draw(bar3dTop);
            }
        }

        CategoryItemLabelGenerator generator
            = getItemLabelGenerator(row, column);
        if (generator != null && isItemLabelVisible(row, column)) {
            drawItemLabel(g2, dataset, row, column, plot, generator, bar,
                    (value < 0.0));
        }

        // add an item entity, if this information is being collected
        EntityCollection entities = state.getEntityCollection();
        if (entities != null) {
            GeneralPath barOutline = new GeneralPath();
            barOutline.moveTo((float) x0, (float) y3);
            barOutline.lineTo((float) x0, (float) y1);
            barOutline.lineTo((float) x1, (float) y0);
            barOutline.lineTo((float) x3, (float) y0);
            barOutline.lineTo((float) x3, (float) y2);
            barOutline.lineTo((float) x2, (float) y3);
            barOutline.closePath();
            addItemEntity(entities, dataset, row, column, barOutline);
        }

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
        if (!(obj instanceof BarRenderer3D)) {
            return false;
        }
        BarRenderer3D that = (BarRenderer3D) obj;
        if (this.xOffset != that.xOffset) {
            return false;
        }
        if (this.yOffset != that.yOffset) {
            return false;
        }
        if (!PaintUtilities.equal(this.wallPaint, that.wallPaint)) {
            return false;
        }
        return super.equals(obj);
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
        SerialUtilities.writePaint(this.wallPaint, stream);
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
        this.wallPaint = SerialUtilities.readPaint(stream);
    }

}
