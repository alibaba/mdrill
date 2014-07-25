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
 * -------------
 * ColorBar.java
 * -------------
 * (C) Copyright 2002-2008, by David M. O'Donnell and Contributors.
 *
 * Original Author:  David M. O'Donnell;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 26-Nov-2002 : Version 1 contributed by David M. O'Donnell (DG);
 * 14-Jan-2003 : Changed autoRangeMinimumSize from Number --> double (DG);
 * 17-Jan-2003 : Moved plot classes to separate package (DG);
 * 20-Jan-2003 : Removed unnecessary constructors (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 09-Jul-2003 : Changed ColorBar from extending axis classes to enclosing
 *               them (DG);
 * 05-Aug-2003 : Applied changes in bug report 780298 (DG);
 * 14-Aug-2003 : Implemented Cloneable (DG);
 * 08-Sep-2003 : Changed ValueAxis API (DG);
 * 21-Jan-2004 : Update for renamed method in ValueAxis (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 31-Jan-2007 : Deprecated (DG);
 *
 */

package org.jfree.chart.axis;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.plot.ColorPalette;
import org.jfree.chart.plot.ContourPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.RainbowPalette;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.ui.RectangleEdge;

/**
 * A color bar.
 *
 * @deprecated This class is no longer supported (as of version 1.0.4).  If
 *     you are creating contour plots, please try to use {@link XYPlot} and
 *     {@link XYBlockRenderer}.
 */
public class ColorBar implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -2101776212647268103L;

    /** The default color bar thickness. */
    public static final int DEFAULT_COLORBAR_THICKNESS = 0;

    /** The default color bar thickness percentage. */
    public static final double DEFAULT_COLORBAR_THICKNESS_PERCENT = 0.10;

    /** The default outer gap. */
    public static final int DEFAULT_OUTERGAP = 2;

    /** The axis. */
    private ValueAxis axis;

    /** The color bar thickness. */
    private int colorBarThickness = DEFAULT_COLORBAR_THICKNESS;

    /**
     * The color bar thickness as a percentage of the height of the data area.
     */
    private double colorBarThicknessPercent
            = DEFAULT_COLORBAR_THICKNESS_PERCENT;

    /** The color palette. */
    private ColorPalette colorPalette = null;

    /** The color bar length. */
    private int colorBarLength = 0; // default make height of plotArea

    /** The amount of blank space around the colorbar. */
    private int outerGap;

    /**
     * Constructs a horizontal colorbar axis, using default values where
     * necessary.
     *
     * @param label  the axis label.
     */
    public ColorBar(String label) {

        NumberAxis a = new NumberAxis(label);
        a.setAutoRangeIncludesZero(false);
        this.axis = a;
        this.axis.setLowerMargin(0.0);
        this.axis.setUpperMargin(0.0);

        this.colorPalette = new RainbowPalette();
        this.colorBarThickness = DEFAULT_COLORBAR_THICKNESS;
        this.colorBarThicknessPercent = DEFAULT_COLORBAR_THICKNESS_PERCENT;
        this.outerGap = DEFAULT_OUTERGAP;
        this.colorPalette.setMinZ(this.axis.getRange().getLowerBound());
        this.colorPalette.setMaxZ(this.axis.getRange().getUpperBound());

    }

    /**
     * Configures the color bar.
     *
     * @param plot  the plot.
     */
    public void configure(ContourPlot plot) {
        double minZ = plot.getDataset().getMinZValue();
        double maxZ = plot.getDataset().getMaxZValue();
        setMinimumValue(minZ);
        setMaximumValue(maxZ);
    }

    /**
     * Returns the axis.
     *
     * @return The axis.
     */
    public ValueAxis getAxis() {
        return this.axis;
    }

    /**
     * Sets the axis.
     *
     * @param axis  the axis.
     */
    public void setAxis(ValueAxis axis) {
        this.axis = axis;
    }

    /**
     * Rescales the axis to ensure that all data are visible.
     */
    public void autoAdjustRange() {
        this.axis.autoAdjustRange();
        this.colorPalette.setMinZ(this.axis.getLowerBound());
        this.colorPalette.setMaxZ(this.axis.getUpperBound());
    }

    /**
     * Draws the plot on a Java 2D graphics device (such as the screen or a
     * printer).
     *
     * @param g2  the graphics device.
     * @param cursor  the cursor.
     * @param plotArea  the area within which the chart should be drawn.
     * @param dataArea  the area within which the plot should be drawn (a
     *                  subset of the drawArea).
     * @param reservedArea  the reserved area.
     * @param edge  the color bar location.
     *
     * @return The new cursor location.
     */
    public double draw(Graphics2D g2, double cursor,
                       Rectangle2D plotArea, Rectangle2D dataArea,
                       Rectangle2D reservedArea, RectangleEdge edge) {


        Rectangle2D colorBarArea = null;

        double thickness = calculateBarThickness(dataArea, edge);
        if (this.colorBarThickness > 0) {
            thickness = this.colorBarThickness;  // allow fixed thickness
        }

        double length = 0.0;
        if (RectangleEdge.isLeftOrRight(edge)) {
            length = dataArea.getHeight();
        }
        else {
            length = dataArea.getWidth();
        }

        if (this.colorBarLength > 0) {
            length = this.colorBarLength;
        }

        if (edge == RectangleEdge.BOTTOM) {
            colorBarArea = new Rectangle2D.Double(dataArea.getX(),
                    plotArea.getMaxY() + this.outerGap, length, thickness);
        }
        else if (edge == RectangleEdge.TOP) {
            colorBarArea = new Rectangle2D.Double(dataArea.getX(),
                    reservedArea.getMinY() + this.outerGap, length, thickness);
        }
        else if (edge == RectangleEdge.LEFT) {
            colorBarArea = new Rectangle2D.Double(plotArea.getX() - thickness
                    - this.outerGap, dataArea.getMinY(), thickness, length);
        }
        else if (edge == RectangleEdge.RIGHT) {
            colorBarArea = new Rectangle2D.Double(plotArea.getMaxX()
                    + this.outerGap, dataArea.getMinY(), thickness, length);
        }

        // update, but dont draw tick marks (needed for stepped colors)
        this.axis.refreshTicks(g2, new AxisState(), colorBarArea, edge);

        drawColorBar(g2, colorBarArea, edge);

        AxisState state = null;
        if (edge == RectangleEdge.TOP) {
            cursor = colorBarArea.getMinY();
            state = this.axis.draw(g2, cursor, reservedArea, colorBarArea,
                    RectangleEdge.TOP, null);
        }
        else if (edge == RectangleEdge.BOTTOM) {
            cursor = colorBarArea.getMaxY();
            state = this.axis.draw(g2, cursor, reservedArea, colorBarArea,
                    RectangleEdge.BOTTOM, null);
        }
        else if (edge == RectangleEdge.LEFT) {
            cursor = colorBarArea.getMinX();
            state = this.axis.draw(g2, cursor, reservedArea, colorBarArea,
                    RectangleEdge.LEFT, null);
        }
        else if (edge == RectangleEdge.RIGHT) {
            cursor = colorBarArea.getMaxX();
            state = this.axis.draw(g2, cursor, reservedArea, colorBarArea,
                    RectangleEdge.RIGHT, null);
        }
        return state.getCursor();

    }

    /**
     * Draws the plot on a Java 2D graphics device (such as the screen or a
     * printer).
     *
     * @param g2  the graphics device.
     * @param colorBarArea  the area within which the axis should be drawn.
     * @param edge  the location.
     */
    public void drawColorBar(Graphics2D g2, Rectangle2D colorBarArea,
                             RectangleEdge edge) {

        Object antiAlias = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_OFF);

        // setTickValues was missing from ColorPalette v. 0.96
        //colorPalette.setTickValues(this.axis.getTicks());

        Stroke strokeSaved = g2.getStroke();
        g2.setStroke(new BasicStroke(1.0f));

        if (RectangleEdge.isTopOrBottom(edge)) {
            double y1 = colorBarArea.getY();
            double y2 = colorBarArea.getMaxY();
            double xx = colorBarArea.getX();
            Line2D line = new Line2D.Double();
            while (xx <= colorBarArea.getMaxX()) {
                double value = this.axis.java2DToValue(xx, colorBarArea, edge);
                line.setLine(xx, y1, xx, y2);
                g2.setPaint(getPaint(value));
                g2.draw(line);
                xx += 1;
            }
        }
        else {
            double y1 = colorBarArea.getX();
            double y2 = colorBarArea.getMaxX();
            double xx = colorBarArea.getY();
            Line2D line = new Line2D.Double();
            while (xx <= colorBarArea.getMaxY()) {
                double value = this.axis.java2DToValue(xx, colorBarArea, edge);
                line.setLine(y1, xx, y2, xx);
                g2.setPaint(getPaint(value));
                g2.draw(line);
                xx += 1;
            }
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias);
        g2.setStroke(strokeSaved);

    }

    /**
     * Returns the color palette.
     *
     * @return The color palette.
     */
    public ColorPalette getColorPalette() {
        return this.colorPalette;
    }

    /**
     * Returns the Paint associated with a value.
     *
     * @param value  the value.
     *
     * @return The paint.
     */
    public Paint getPaint(double value) {
        return this.colorPalette.getPaint(value);
    }

    /**
     * Sets the color palette.
     *
     * @param palette  the new palette.
     */
    public void setColorPalette(ColorPalette palette) {
        this.colorPalette = palette;
    }

    /**
     * Sets the maximum value.
     *
     * @param value  the maximum value.
     */
    public void setMaximumValue(double value) {
        this.colorPalette.setMaxZ(value);
        this.axis.setUpperBound(value);
    }

    /**
     * Sets the minimum value.
     *
     * @param value  the minimum value.
     */
    public void setMinimumValue(double value) {
        this.colorPalette.setMinZ(value);
        this.axis.setLowerBound(value);
    }

    /**
     * Reserves the space required to draw the color bar.
     *
     * @param g2  the graphics device.
     * @param plot  the plot that the axis belongs to.
     * @param plotArea  the area within which the plot should be drawn.
     * @param dataArea  the data area.
     * @param edge  the axis location.
     * @param space  the space already reserved.
     *
     * @return The space required to draw the axis in the specified plot area.
     */
    public AxisSpace reserveSpace(Graphics2D g2, Plot plot,
                                  Rectangle2D plotArea,
                                  Rectangle2D dataArea, RectangleEdge edge,
                                  AxisSpace space) {

        AxisSpace result = this.axis.reserveSpace(g2, plot, plotArea, edge,
                space);
        double thickness = calculateBarThickness(dataArea, edge);
        result.add(thickness + 2 * this.outerGap, edge);
        return result;

    }

    /**
     * Calculates the bar thickness.
     *
     * @param plotArea  the plot area.
     * @param edge  the location.
     *
     * @return The thickness.
     */
    private double calculateBarThickness(Rectangle2D plotArea,
                                         RectangleEdge edge) {
        double result = 0.0;
        if (RectangleEdge.isLeftOrRight(edge)) {
            result = plotArea.getWidth() * this.colorBarThicknessPercent;
        }
        else {
            result = plotArea.getHeight() * this.colorBarThicknessPercent;
        }
        return result;
    }

    /**
     * Returns a clone of the object.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if some component of the color bar
     *         does not support cloning.
     */
    public Object clone() throws CloneNotSupportedException {

        ColorBar clone = (ColorBar) super.clone();
        clone.axis = (ValueAxis) this.axis.clone();
        return clone;

    }

    /**
     * Tests this object for equality with another.
     *
     * @param obj  the object to test against.
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ColorBar)) {
            return false;
        }
        ColorBar that = (ColorBar) obj;
        if (!this.axis.equals(that.axis)) {
            return false;
        }
        if (this.colorBarThickness != that.colorBarThickness) {
            return false;
        }
        if (this.colorBarThicknessPercent != that.colorBarThicknessPercent) {
            return false;
        }
        if (!this.colorPalette.equals(that.colorPalette)) {
            return false;
        }
        if (this.colorBarLength != that.colorBarLength) {
            return false;
        }
        if (this.outerGap != that.outerGap) {
            return false;
        }
        return true;

    }

    /**
     * Returns a hash code for this object.
     *
     * @return A hash code.
     */
    public int hashCode() {
        return this.axis.hashCode();
    }

}
