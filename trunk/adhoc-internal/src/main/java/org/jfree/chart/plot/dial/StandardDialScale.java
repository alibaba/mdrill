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
 * ----------------------
 * StandardDialScale.java
 * ----------------------
 * (C) Copyright 2006-2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 03-Nov-2006 : Version 1 (DG);
 * 17-Nov-2006 : Added flags for tick label visibility (DG);
 * 24-Oct-2007 : Added tick label formatter (DG);
 * 19-Nov-2007 : Added some missing accessor methods (DG);
 * 27-Feb-2009 : Fixed bug 2617557: tickLabelPaint ignored (DG);
 *
 */

package org.jfree.chart.plot.dial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.jfree.io.SerialUtilities;
import org.jfree.text.TextUtilities;
import org.jfree.ui.TextAnchor;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A scale for a {@link DialPlot}.
 *
 * @since 1.0.7
 */
public class StandardDialScale extends AbstractDialLayer implements DialScale,
        Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    static final long serialVersionUID = 3715644629665918516L;

    /** The minimum data value for the scale. */
    private double lowerBound;

    /** The maximum data value for the scale. */
    private double upperBound;

    /**
     * The start angle for the scale display, in degrees (using the same
     * encoding as Arc2D).
     */
    private double startAngle;

    /** The extent of the scale display. */
    private double extent;

    /**
     * The factor (in the range 0.0 to 1.0) that determines the outside limit
     * of the tick marks.
     */
    private double tickRadius;

    /**
     * The increment (in data units) between major tick marks.
     */
    private double majorTickIncrement;

    /**
     * The factor that is subtracted from the tickRadius to determine the
     * inner point of the major ticks.
     */
    private double majorTickLength;

    /**
     * The paint to use for major tick marks.  This field is transient because
     * it requires special handling for serialization.
     */
    private transient Paint majorTickPaint;

    /**
     * The stroke to use for major tick marks.  This field is transient because
     * it requires special handling for serialization.
     */
    private transient Stroke majorTickStroke;

    /**
     * The number of minor ticks between each major tick.
     */
    private int minorTickCount;

    /**
     * The factor that is subtracted from the tickRadius to determine the
     * inner point of the minor ticks.
     */
    private double minorTickLength;

    /**
     * The paint to use for minor tick marks.  This field is transient because
     * it requires special handling for serialization.
     */
    private transient Paint minorTickPaint;

    /**
     * The stroke to use for minor tick marks.  This field is transient because
     * it requires special handling for serialization.
     */
    private transient Stroke minorTickStroke;

    /**
     * The tick label offset.
     */
    private double tickLabelOffset;

    /**
     * The tick label font.
     */
    private Font tickLabelFont;

    /**
     * A flag that controls whether or not the tick labels are
     * displayed.
     */
    private boolean tickLabelsVisible;

    /**
     * The number formatter for the tick labels.
     */
    private NumberFormat tickLabelFormatter;

    /**
     * A flag that controls whether or not the first tick label is
     * displayed.
     */
    private boolean firstTickLabelVisible;

    /**
     * The tick label paint.  This field is transient because it requires
     * special handling for serialization.
     */
    private transient Paint tickLabelPaint;

    /**
     * Creates a new instance of DialScale.
     */
    public StandardDialScale() {
        this(0.0, 100.0, 175, -170, 10.0, 4);
    }

    /**
     * Creates a new instance.
     *
     * @param lowerBound  the lower bound of the scale.
     * @param upperBound  the upper bound of the scale.
     * @param startAngle  the start angle (in degrees, using the same
     *     orientation as Java's <code>Arc2D</code> class).
     * @param extent  the extent (in degrees, counter-clockwise).
     * @param majorTickIncrement  the interval between major tick marks
     * @param minorTickCount  the number of minor ticks between major tick
     *          marks.
     */
    public StandardDialScale(double lowerBound, double upperBound,
            double startAngle, double extent, double majorTickIncrement,
            int minorTickCount) {
        this.startAngle = startAngle;
        this.extent = extent;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.tickRadius = 0.70;
        this.tickLabelsVisible = true;
        this.tickLabelFormatter = new DecimalFormat("0.0");
        this.firstTickLabelVisible = true;
        this.tickLabelFont = new Font("Dialog", Font.BOLD, 16);
        this.tickLabelPaint = Color.blue;
        this.tickLabelOffset = 0.10;
        this.majorTickIncrement = majorTickIncrement;
        this.majorTickLength = 0.04;
        this.majorTickPaint = Color.black;
        this.majorTickStroke = new BasicStroke(3.0f);
        this.minorTickCount = minorTickCount;
        this.minorTickLength = 0.02;
        this.minorTickPaint = Color.black;
        this.minorTickStroke = new BasicStroke(1.0f);
    }

    /**
     * Returns the lower bound for the scale.
     *
     * @return The lower bound for the scale.
     *
     * @see #setLowerBound(double)
     *
     * @since 1.0.8
     */
    public double getLowerBound() {
        return this.lowerBound;
    }

    /**
     * Sets the lower bound for the scale and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param lower  the lower bound.
     *
     * @see #getLowerBound()
     *
     * @since 1.0.8
     */
    public void setLowerBound(double lower) {
        this.lowerBound = lower;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the upper bound for the scale.
     *
     * @return The upper bound for the scale.
     *
     * @see #setUpperBound(double)
     *
     * @since 1.0.8
     */
    public double getUpperBound() {
        return this.upperBound;
    }

    /**
     * Sets the upper bound for the scale and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param upper  the upper bound.
     *
     * @see #getUpperBound()
     *
     * @since 1.0.8
     */
    public void setUpperBound(double upper) {
        this.upperBound = upper;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the start angle for the scale (in degrees using the same
     * orientation as Java's <code>Arc2D</code> class).
     *
     * @return The start angle.
     *
     * @see #setStartAngle(double)
     */
    public double getStartAngle() {
        return this.startAngle;
    }

    /**
     * Sets the start angle for the scale and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param angle  the angle (in degrees).
     *
     * @see #getStartAngle()
     */
    public void setStartAngle(double angle) {
        this.startAngle = angle;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the extent.
     *
     * @return The extent.
     *
     * @see #setExtent(double)
     */
    public double getExtent() {
        return this.extent;
    }

    /**
     * Sets the extent and sends a {@link DialLayerChangeEvent} to all
     * registered listeners.
     *
     * @param extent  the extent.
     *
     * @see #getExtent()
     */
    public void setExtent(double extent) {
        this.extent = extent;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the radius (as a percentage of the maximum space available) of
     * the outer limit of the tick marks.
     *
     * @return The tick radius.
     *
     * @see #setTickRadius(double)
     */
    public double getTickRadius() {
        return this.tickRadius;
    }

    /**
     * Sets the tick radius and sends a {@link DialLayerChangeEvent} to all
     * registered listeners.
     *
     * @param radius  the radius.
     *
     * @see #getTickRadius()
     */
    public void setTickRadius(double radius) {
        if (radius <= 0.0) {
            throw new IllegalArgumentException(
                    "The 'radius' must be positive.");
        }
        this.tickRadius = radius;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the increment (in data units) between major tick labels.
     *
     * @return The increment between major tick labels.
     *
     * @see #setMajorTickIncrement(double)
     */
    public double getMajorTickIncrement() {
        return this.majorTickIncrement;
    }

    /**
     * Sets the increment (in data units) between major tick labels and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param increment  the increment.
     *
     * @see #getMajorTickIncrement()
     */
    public void setMajorTickIncrement(double increment) {
        if (increment <= 0.0) {
            throw new IllegalArgumentException(
                    "The 'increment' must be positive.");
        }
        this.majorTickIncrement = increment;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the length factor for the major tick marks.  The value is
     * subtracted from the tick radius to determine the inner starting point
     * for the tick marks.
     *
     * @return The length factor.
     *
     * @see #setMajorTickLength(double)
     */
    public double getMajorTickLength() {
        return this.majorTickLength;
    }

    /**
     * Sets the length factor for the major tick marks and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param length  the length.
     *
     * @see #getMajorTickLength()
     */
    public void setMajorTickLength(double length) {
        if (length < 0.0) {
            throw new IllegalArgumentException("Negative 'length' argument.");
        }
        this.majorTickLength = length;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the major tick paint.
     *
     * @return The major tick paint (never <code>null</code>).
     *
     * @see #setMajorTickPaint(Paint)
     */
    public Paint getMajorTickPaint() {
        return this.majorTickPaint;
    }

    /**
     * Sets the major tick paint and sends a {@link DialLayerChangeEvent} to
     * all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getMajorTickPaint()
     */
    public void setMajorTickPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.majorTickPaint = paint;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the stroke used to draw the major tick marks.
     *
     * @return The stroke (never <code>null</code>).
     *
     * @see #setMajorTickStroke(Stroke)
     */
    public Stroke getMajorTickStroke() {
        return this.majorTickStroke;
    }

    /**
     * Sets the stroke used to draw the major tick marks and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     *
     * @see #getMajorTickStroke()
     */
    public void setMajorTickStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.majorTickStroke = stroke;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the number of minor tick marks between major tick marks.
     *
     * @return The number of minor tick marks between major tick marks.
     *
     * @see #setMinorTickCount(int)
     */
    public int getMinorTickCount() {
        return this.minorTickCount;
    }

    /**
     * Sets the number of minor tick marks between major tick marks and sends
     * a {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param count  the count.
     *
     * @see #getMinorTickCount()
     */
    public void setMinorTickCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException(
                    "The 'count' cannot be negative.");
        }
        this.minorTickCount = count;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the length factor for the minor tick marks.  The value is
     * subtracted from the tick radius to determine the inner starting point
     * for the tick marks.
     *
     * @return The length factor.
     *
     * @see #setMinorTickLength(double)
     */
    public double getMinorTickLength() {
        return this.minorTickLength;
    }

    /**
     * Sets the length factor for the minor tick marks and sends
     * a {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param length  the length.
     *
     * @see #getMinorTickLength()
     */
    public void setMinorTickLength(double length) {
        if (length < 0.0) {
            throw new IllegalArgumentException("Negative 'length' argument.");
        }
        this.minorTickLength = length;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the paint used to draw the minor tick marks.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setMinorTickPaint(Paint)
     */
    public Paint getMinorTickPaint() {
        return this.minorTickPaint;
    }

    /**
     * Sets the paint used to draw the minor tick marks and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getMinorTickPaint()
     */
    public void setMinorTickPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.minorTickPaint = paint;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the stroke used to draw the minor tick marks.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setMinorTickStroke(Stroke)
     *
     * @since 1.0.8
     */
    public Stroke getMinorTickStroke() {
        return this.minorTickStroke;
    }

    /**
     * Sets the stroke used to draw the minor tick marks and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     *
     * @see #getMinorTickStroke()
     *
     * @since 1.0.8
     */
    public void setMinorTickStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.minorTickStroke = stroke;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the tick label offset.
     *
     * @return The tick label offset.
     *
     * @see #setTickLabelOffset(double)
     */
    public double getTickLabelOffset() {
        return this.tickLabelOffset;
    }

    /**
     * Sets the tick label offset and sends a {@link DialLayerChangeEvent} to
     * all registered listeners.
     *
     * @param offset  the offset.
     *
     * @see #getTickLabelOffset()
     */
    public void setTickLabelOffset(double offset) {
        this.tickLabelOffset = offset;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the font used to draw the tick labels.
     *
     * @return The font (never <code>null</code>).
     *
     * @see #setTickLabelFont(Font)
     */
    public Font getTickLabelFont() {
        return this.tickLabelFont;
    }

    /**
     * Sets the font used to display the tick labels and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param font  the font (<code>null</code> not permitted).
     *
     * @see #getTickLabelFont()
     */
    public void setTickLabelFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }
        this.tickLabelFont = font;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the paint used to draw the tick labels.
     *
     * @return The paint (<code>null</code> not permitted).
     *
     * @see #setTickLabelPaint(Paint)
     */
    public Paint getTickLabelPaint() {
        return this.tickLabelPaint;
    }

    /**
     * Sets the paint used to draw the tick labels and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     */
    public void setTickLabelPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.tickLabelPaint = paint;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns <code>true</code> if the tick labels should be displayed,
     * and <code>false</code> otherwise.
     *
     * @return A boolean.
     *
     * @see #setTickLabelsVisible(boolean)
     */
    public boolean getTickLabelsVisible() {
        return this.tickLabelsVisible;
    }

    /**
     * Sets the flag that controls whether or not the tick labels are
     * displayed, and sends a {@link DialLayerChangeEvent} to all registered
     * listeners.
     *
     * @param visible  the new flag value.
     *
     * @see #getTickLabelsVisible()
     */
    public void setTickLabelsVisible(boolean visible) {
        this.tickLabelsVisible = visible;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the number formatter used to convert the tick label values to
     * strings.
     *
     * @return The formatter (never <code>null</code>).
     *
     * @see #setTickLabelFormatter(NumberFormat)
     */
    public NumberFormat getTickLabelFormatter() {
        return this.tickLabelFormatter;
    }

    /**
     * Sets the number formatter used to convert the tick label values to
     * strings, and sends a {@link DialLayerChangeEvent} to all registered
     * listeners.
     *
     * @param formatter  the formatter (<code>null</code> not permitted).
     *
     * @see #getTickLabelFormatter()
     */
    public void setTickLabelFormatter(NumberFormat formatter) {
        if (formatter == null) {
            throw new IllegalArgumentException("Null 'formatter' argument.");
        }
        this.tickLabelFormatter = formatter;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns a flag that controls whether or not the first tick label is
     * visible.
     *
     * @return A boolean.
     *
     * @see #setFirstTickLabelVisible(boolean)
     */
    public boolean getFirstTickLabelVisible() {
        return this.firstTickLabelVisible;
    }

    /**
     * Sets a flag that controls whether or not the first tick label is
     * visible, and sends a {@link DialLayerChangeEvent} to all registered
     * listeners.
     *
     * @param visible  the new flag value.
     *
     * @see #getFirstTickLabelVisible()
     */
    public void setFirstTickLabelVisible(boolean visible) {
        this.firstTickLabelVisible = visible;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns <code>true</code> to indicate that this layer should be
     * clipped within the dial window.
     *
     * @return <code>true</code>.
     */
    public boolean isClippedToWindow() {
        return true;
    }

    /**
     * Draws the scale on the dial plot.
     *
     * @param g2  the graphics target (<code>null</code> not permitted).
     * @param plot  the dial plot (<code>null</code> not permitted).
     * @param frame  the reference frame that is used to construct the
     *     geometry of the plot (<code>null</code> not permitted).
     * @param view  the visible part of the plot (<code>null</code> not
     *     permitted).
     */
    public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame,
            Rectangle2D view) {

        Rectangle2D arcRect = DialPlot.rectangleByRadius(frame,
                this.tickRadius, this.tickRadius);
        Rectangle2D arcRectMajor = DialPlot.rectangleByRadius(frame,
                this.tickRadius - this.majorTickLength,
                this.tickRadius - this.majorTickLength);
        Rectangle2D arcRectMinor = arcRect;
        if (this.minorTickCount > 0 && this.minorTickLength > 0.0) {
            arcRectMinor = DialPlot.rectangleByRadius(frame,
                    this.tickRadius - this.minorTickLength,
                    this.tickRadius - this.minorTickLength);
        }
        Rectangle2D arcRectForLabels = DialPlot.rectangleByRadius(frame,
                this.tickRadius - this.tickLabelOffset,
                this.tickRadius - this.tickLabelOffset);

        boolean firstLabel = true;

        Arc2D arc = new Arc2D.Double();
        Line2D workingLine = new Line2D.Double();
        for (double v = this.lowerBound; v <= this.upperBound;
                v += this.majorTickIncrement) {
            arc.setArc(arcRect, this.startAngle, valueToAngle(v)
                    - this.startAngle, Arc2D.OPEN);
            Point2D pt0 = arc.getEndPoint();
            arc.setArc(arcRectMajor, this.startAngle, valueToAngle(v)
                    - this.startAngle, Arc2D.OPEN);
            Point2D pt1 = arc.getEndPoint();
            g2.setPaint(this.majorTickPaint);
            g2.setStroke(this.majorTickStroke);
            workingLine.setLine(pt0, pt1);
            g2.draw(workingLine);
            arc.setArc(arcRectForLabels, this.startAngle, valueToAngle(v)
                    - this.startAngle, Arc2D.OPEN);
            Point2D pt2 = arc.getEndPoint();

            if (this.tickLabelsVisible) {
                if (!firstLabel || this.firstTickLabelVisible) {
                    g2.setFont(this.tickLabelFont);
                    g2.setPaint(this.tickLabelPaint);
                    TextUtilities.drawAlignedString(
                            this.tickLabelFormatter.format(v), g2,
                            (float) pt2.getX(), (float) pt2.getY(),
                            TextAnchor.CENTER);
                }
            }
            firstLabel = false;

            // now do the minor tick marks
            if (this.minorTickCount > 0 && this.minorTickLength > 0.0) {
                double minorTickIncrement = this.majorTickIncrement
                        / (this.minorTickCount + 1);
                for (int i = 0; i < this.minorTickCount; i++) {
                    double vv = v + ((i + 1) * minorTickIncrement);
                    if (vv >= this.upperBound) {
                        break;
                    }
                    double angle = valueToAngle(vv);

                    arc.setArc(arcRect, this.startAngle, angle
                            - this.startAngle, Arc2D.OPEN);
                    pt0 = arc.getEndPoint();
                    arc.setArc(arcRectMinor, this.startAngle, angle
                            - this.startAngle, Arc2D.OPEN);
                    Point2D pt3 = arc.getEndPoint();
                    g2.setStroke(this.minorTickStroke);
                    g2.setPaint(this.minorTickPaint);
                    workingLine.setLine(pt0, pt3);
                    g2.draw(workingLine);
                }
            }

        }
    }

    /**
     * Converts a data value to an angle against this scale.
     *
     * @param value  the data value.
     *
     * @return The angle (in degrees, using the same specification as Java's
     *     Arc2D class).
     *
     * @see #angleToValue(double)
     */
    public double valueToAngle(double value) {
        double range = this.upperBound - this.lowerBound;
        double unit = this.extent / range;
        return this.startAngle + unit * (value - this.lowerBound);
    }

    /**
     * Converts the given angle to a data value, based on this scale.
     *
     * @param angle  the angle.
     *
     * @return The data value.
     *
     * @see #valueToAngle(double)
     */
    public double angleToValue(double angle) {
        return Double.NaN;  // FIXME
    }

    /**
     * Tests this <code>StandardDialScale</code> for equality with an arbitrary
     * object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StandardDialScale)) {
            return false;
        }
        StandardDialScale that = (StandardDialScale) obj;
        if (this.lowerBound != that.lowerBound) {
            return false;
        }
        if (this.upperBound != that.upperBound) {
            return false;
        }
        if (this.startAngle != that.startAngle) {
            return false;
        }
        if (this.extent != that.extent) {
            return false;
        }
        if (this.tickRadius != that.tickRadius) {
            return false;
        }
        if (this.majorTickIncrement != that.majorTickIncrement) {
            return false;
        }
        if (this.majorTickLength != that.majorTickLength) {
            return false;
        }
        if (!PaintUtilities.equal(this.majorTickPaint, that.majorTickPaint)) {
            return false;
        }
        if (!this.majorTickStroke.equals(that.majorTickStroke)) {
            return false;
        }
        if (this.minorTickCount != that.minorTickCount) {
            return false;
        }
        if (this.minorTickLength != that.minorTickLength) {
            return false;
        }
        if (!PaintUtilities.equal(this.minorTickPaint, that.minorTickPaint)) {
            return false;
        }
        if (!this.minorTickStroke.equals(that.minorTickStroke)) {
            return false;
        }
        if (this.tickLabelsVisible != that.tickLabelsVisible) {
            return false;
        }
        if (this.tickLabelOffset != that.tickLabelOffset) {
            return false;
        }
        if (!this.tickLabelFont.equals(that.tickLabelFont)) {
            return false;
        }
        if (!PaintUtilities.equal(this.tickLabelPaint, that.tickLabelPaint)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code.
     */
    public int hashCode() {
        int result = 193;
        // lowerBound
        long temp = Double.doubleToLongBits(this.lowerBound);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        // upperBound
        temp = Double.doubleToLongBits(this.upperBound);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        // startAngle
        temp = Double.doubleToLongBits(this.startAngle);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        // extent
        temp = Double.doubleToLongBits(this.extent);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        // tickRadius
        temp = Double.doubleToLongBits(this.tickRadius);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        // majorTickIncrement
        // majorTickLength
        // majorTickPaint
        // majorTickStroke
        // minorTickCount
        // minorTickLength
        // minorTickPaint
        // minorTickStroke
        // tickLabelOffset
        // tickLabelFont
        // tickLabelsVisible
        // tickLabelFormatter
        // firstTickLabelsVisible
        return result;
    }

    /**
     * Returns a clone of this instance.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if this instance is not cloneable.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
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
        SerialUtilities.writePaint(this.majorTickPaint, stream);
        SerialUtilities.writeStroke(this.majorTickStroke, stream);
        SerialUtilities.writePaint(this.minorTickPaint, stream);
        SerialUtilities.writeStroke(this.minorTickStroke, stream);
        SerialUtilities.writePaint(this.tickLabelPaint, stream);
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
        this.majorTickPaint = SerialUtilities.readPaint(stream);
        this.majorTickStroke = SerialUtilities.readStroke(stream);
        this.minorTickPaint = SerialUtilities.readPaint(stream);
        this.minorTickStroke = SerialUtilities.readStroke(stream);
        this.tickLabelPaint = SerialUtilities.readPaint(stream);
    }

}
