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
 * ---------
 * Axis.java
 * ---------
 * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Bill Kelemen;
 *                   Nicolas Brodu;
 *                   Peter Kolb (patches 1934255 and 2603321);
 *                   Andrew Mickish (patch 1870189);
 *
 * Changes
 * -------
 * 21-Aug-2001 : Added standard header, fixed DOS encoding problem (DG);
 * 18-Sep-2001 : Updated header (DG);
 * 07-Nov-2001 : Allow null axis labels (DG);
 *             : Added default font values (DG);
 * 13-Nov-2001 : Modified the setPlot() method to check compatibility between
 *               the axis and the plot (DG);
 * 30-Nov-2001 : Changed default font from "Arial" --> "SansSerif" (DG);
 * 06-Dec-2001 : Allow null in setPlot() method (BK);
 * 06-Mar-2002 : Added AxisConstants interface (DG);
 * 23-Apr-2002 : Added a visible property.  Moved drawVerticalString to
 *               RefineryUtilities.  Added fixedDimension property for use in
 *               combined plots (DG);
 * 25-Jun-2002 : Removed unnecessary imports (DG);
 * 05-Sep-2002 : Added attribute for tick mark paint (DG);
 * 18-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 07-Nov-2002 : Added attributes to control the inside and outside length of
 *               the tick marks (DG);
 * 08-Nov-2002 : Moved to new package com.jrefinery.chart.axis (DG);
 * 18-Nov-2002 : Added axis location to refreshTicks() parameters (DG);
 * 15-Jan-2003 : Removed monolithic constructor (DG);
 * 17-Jan-2003 : Moved plot classes to separate package (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 03-Jul-2003 : Modified reserveSpace method (DG);
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 11-Sep-2003 : Took care of listeners while cloning (NB);
 * 29-Oct-2003 : Added workaround for font alignment in PDF output (DG);
 * 06-Nov-2003 : Modified refreshTicks() signature (DG);
 * 06-Jan-2004 : Added axis line attributes (DG);
 * 16-Mar-2004 : Added plot state to draw() method (DG);
 * 07-Apr-2004 : Modified text bounds calculation (DG);
 * 18-May-2004 : Eliminated AxisConstants.java (DG);
 * 30-Sep-2004 : Moved drawRotatedString() from RefineryUtilities -->
 *               TextUtilities (DG);
 * 04-Oct-2004 : Modified getLabelEnclosure() method to treat an empty String
 *               the same way as a null string - see bug 1026521 (DG);
 * 21-Apr-2005 : Replaced Insets with RectangleInsets (DG);
 * 26-Apr-2005 : Removed LOGGER (DG);
 * 01-Jun-2005 : Added hasListener() method for unit testing (DG);
 * 08-Jun-2005 : Fixed equals() method to handle GradientPaint (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 22-Aug-2006 : API doc updates (DG);
 * 06-Jun-2008 : Added setTickLabelInsets(RectangleInsets, boolean) (DG);
 * 25-Sep-2008 : Added minor tick support, see patch 1934255 by Peter Kolb (DG);
 * 26-Sep-2008 : Added fireChangeEvent() method (DG);
 * 19-Mar-2009 : Added entity support - see patch 2603321 by Peter Kolb (DG);
 *
 */

package org.jfree.chart.axis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import javax.swing.event.EventListenerList;

import org.jfree.chart.entity.AxisEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.io.SerialUtilities;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;

/**
 * The base class for all axes in JFreeChart.  Subclasses are divided into
 * those that display values ({@link ValueAxis}) and those that display
 * categories ({@link CategoryAxis}).
 */
public abstract class Axis implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 7719289504573298271L;

    /** The default axis visibility. */
    public static final boolean DEFAULT_AXIS_VISIBLE = true;

    /** The default axis label font. */
    public static final Font DEFAULT_AXIS_LABEL_FONT = new Font(
            "SansSerif", Font.PLAIN, 12);

    /** The default axis label paint. */
    public static final Paint DEFAULT_AXIS_LABEL_PAINT = Color.black;

    /** The default axis label insets. */
    public static final RectangleInsets DEFAULT_AXIS_LABEL_INSETS
            = new RectangleInsets(3.0, 3.0, 3.0, 3.0);

    /** The default axis line paint. */
    public static final Paint DEFAULT_AXIS_LINE_PAINT = Color.gray;

    /** The default axis line stroke. */
    public static final Stroke DEFAULT_AXIS_LINE_STROKE = new BasicStroke(1.0f);

    /** The default tick labels visibility. */
    public static final boolean DEFAULT_TICK_LABELS_VISIBLE = true;

    /** The default tick label font. */
    public static final Font DEFAULT_TICK_LABEL_FONT = new Font("SansSerif",
            Font.PLAIN, 10);

    /** The default tick label paint. */
    public static final Paint DEFAULT_TICK_LABEL_PAINT = Color.black;

    /** The default tick label insets. */
    public static final RectangleInsets DEFAULT_TICK_LABEL_INSETS
            = new RectangleInsets(2.0, 4.0, 2.0, 4.0);

    /** The default tick marks visible. */
    public static final boolean DEFAULT_TICK_MARKS_VISIBLE = true;

    /** The default tick stroke. */
    public static final Stroke DEFAULT_TICK_MARK_STROKE = new BasicStroke(1);

    /** The default tick paint. */
    public static final Paint DEFAULT_TICK_MARK_PAINT = Color.gray;

    /** The default tick mark inside length. */
    public static final float DEFAULT_TICK_MARK_INSIDE_LENGTH = 0.0f;

    /** The default tick mark outside length. */
    public static final float DEFAULT_TICK_MARK_OUTSIDE_LENGTH = 2.0f;

    /** A flag indicating whether or not the axis is visible. */
    private boolean visible;

    /** The label for the axis. */
    private String label;

    /** The font for displaying the axis label. */
    private Font labelFont;

    /** The paint for drawing the axis label. */
    private transient Paint labelPaint;

    /** The insets for the axis label. */
    private RectangleInsets labelInsets;

    /** The label angle. */
    private double labelAngle;

    /** A flag that controls whether or not the axis line is visible. */
    private boolean axisLineVisible;

    /** The stroke used for the axis line. */
    private transient Stroke axisLineStroke;

    /** The paint used for the axis line. */
    private transient Paint axisLinePaint;

    /**
     * A flag that indicates whether or not tick labels are visible for the
     * axis.
     */
    private boolean tickLabelsVisible;

    /** The font used to display the tick labels. */
    private Font tickLabelFont;

    /** The color used to display the tick labels. */
    private transient Paint tickLabelPaint;

    /** The blank space around each tick label. */
    private RectangleInsets tickLabelInsets;

    /**
     * A flag that indicates whether or not major tick marks are visible for
     * the axis.
     */
    private boolean tickMarksVisible;

    /**
     * The length of the major tick mark inside the data area (zero
     * permitted).
     */
    private float tickMarkInsideLength;

    /**
     * The length of the major tick mark outside the data area (zero
     * permitted).
     */
    private float tickMarkOutsideLength;

    /**
     * A flag that indicates whether or not minor tick marks are visible for the
     * axis.
     *
     * @since 1.0.12
     */
    private boolean minorTickMarksVisible;

    /**
     * The length of the minor tick mark inside the data area (zero permitted).
     *
     * @since 1.0.12
     */
    private float minorTickMarkInsideLength;

    /**
     * The length of the minor tick mark outside the data area (zero permitted).
     *
     * @since 1.0.12
     */
    private float minorTickMarkOutsideLength;

    /** The stroke used to draw tick marks. */
    private transient Stroke tickMarkStroke;

    /** The paint used to draw tick marks. */
    private transient Paint tickMarkPaint;

    /** The fixed (horizontal or vertical) dimension for the axis. */
    private double fixedDimension;

    /**
     * A reference back to the plot that the axis is assigned to (can be
     * <code>null</code>).
     */
    private transient Plot plot;

    /** Storage for registered listeners. */
    private transient EventListenerList listenerList;

    /**
     * Constructs an axis, using default values where necessary.
     *
     * @param label  the axis label (<code>null</code> permitted).
     */
    protected Axis(String label) {

        this.label = label;
        this.visible = DEFAULT_AXIS_VISIBLE;
        this.labelFont = DEFAULT_AXIS_LABEL_FONT;
        this.labelPaint = DEFAULT_AXIS_LABEL_PAINT;
        this.labelInsets = DEFAULT_AXIS_LABEL_INSETS;
        this.labelAngle = 0.0;

        this.axisLineVisible = true;
        this.axisLinePaint = DEFAULT_AXIS_LINE_PAINT;
        this.axisLineStroke = DEFAULT_AXIS_LINE_STROKE;

        this.tickLabelsVisible = DEFAULT_TICK_LABELS_VISIBLE;
        this.tickLabelFont = DEFAULT_TICK_LABEL_FONT;
        this.tickLabelPaint = DEFAULT_TICK_LABEL_PAINT;
        this.tickLabelInsets = DEFAULT_TICK_LABEL_INSETS;

        this.tickMarksVisible = DEFAULT_TICK_MARKS_VISIBLE;
        this.tickMarkStroke = DEFAULT_TICK_MARK_STROKE;
        this.tickMarkPaint = DEFAULT_TICK_MARK_PAINT;
        this.tickMarkInsideLength = DEFAULT_TICK_MARK_INSIDE_LENGTH;
        this.tickMarkOutsideLength = DEFAULT_TICK_MARK_OUTSIDE_LENGTH;

        this.minorTickMarksVisible = false;
        this.minorTickMarkInsideLength = 0.0f;
        this.minorTickMarkOutsideLength = 2.0f;

        this.plot = null;

        this.listenerList = new EventListenerList();

    }

    /**
     * Returns <code>true</code> if the axis is visible, and
     * <code>false</code> otherwise.
     *
     * @return A boolean.
     *
     * @see #setVisible(boolean)
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Sets a flag that controls whether or not the axis is visible and sends
     * an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param flag  the flag.
     *
     * @see #isVisible()
     */
    public void setVisible(boolean flag) {
        if (flag != this.visible) {
            this.visible = flag;
            fireChangeEvent();
        }
    }

    /**
     * Returns the label for the axis.
     *
     * @return The label for the axis (<code>null</code> possible).
     *
     * @see #getLabelFont()
     * @see #getLabelPaint()
     * @see #setLabel(String)
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Sets the label for the axis and sends an {@link AxisChangeEvent} to all
     * registered listeners.
     *
     * @param label  the new label (<code>null</code> permitted).
     *
     * @see #getLabel()
     * @see #setLabelFont(Font)
     * @see #setLabelPaint(Paint)
     */
    public void setLabel(String label) {

        String existing = this.label;
        if (existing != null) {
            if (!existing.equals(label)) {
                this.label = label;
                fireChangeEvent();
            }
        }
        else {
            if (label != null) {
                this.label = label;
                fireChangeEvent();
            }
        }

    }

    /**
     * Returns the font for the axis label.
     *
     * @return The font (never <code>null</code>).
     *
     * @see #setLabelFont(Font)
     */
    public Font getLabelFont() {
        return this.labelFont;
    }

    /**
     * Sets the font for the axis label and sends an {@link AxisChangeEvent}
     * to all registered listeners.
     *
     * @param font  the font (<code>null</code> not permitted).
     *
     * @see #getLabelFont()
     */
    public void setLabelFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }
        if (!this.labelFont.equals(font)) {
            this.labelFont = font;
            fireChangeEvent();
        }
    }

    /**
     * Returns the color/shade used to draw the axis label.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setLabelPaint(Paint)
     */
    public Paint getLabelPaint() {
        return this.labelPaint;
    }

    /**
     * Sets the paint used to draw the axis label and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getLabelPaint()
     */
    public void setLabelPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.labelPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the insets for the label (that is, the amount of blank space
     * that should be left around the label).
     *
     * @return The label insets (never <code>null</code>).
     *
     * @see #setLabelInsets(RectangleInsets)
     */
    public RectangleInsets getLabelInsets() {
        return this.labelInsets;
    }

    /**
     * Sets the insets for the axis label, and sends an {@link AxisChangeEvent}
     * to all registered listeners.
     *
     * @param insets  the insets (<code>null</code> not permitted).
     *
     * @see #getLabelInsets()
     */
    public void setLabelInsets(RectangleInsets insets) {
        setLabelInsets(insets, true);
    }

    /**
     * Sets the insets for the axis label, and sends an {@link AxisChangeEvent}
     * to all registered listeners.
     *
     * @param insets  the insets (<code>null</code> not permitted).
     * @param notify  notify listeners?
     *
     * @since 1.0.10
     */
    public void setLabelInsets(RectangleInsets insets, boolean notify) {
        if (insets == null) {
            throw new IllegalArgumentException("Null 'insets' argument.");
        }
        if (!insets.equals(this.labelInsets)) {
            this.labelInsets = insets;
            if (notify) {
                fireChangeEvent();
            }
        }
    }

    /**
     * Returns the angle of the axis label.
     *
     * @return The angle (in radians).
     *
     * @see #setLabelAngle(double)
     */
    public double getLabelAngle() {
        return this.labelAngle;
    }

    /**
     * Sets the angle for the label and sends an {@link AxisChangeEvent} to all
     * registered listeners.
     *
     * @param angle  the angle (in radians).
     *
     * @see #getLabelAngle()
     */
    public void setLabelAngle(double angle) {
        this.labelAngle = angle;
        fireChangeEvent();
    }

    /**
     * A flag that controls whether or not the axis line is drawn.
     *
     * @return A boolean.
     *
     * @see #getAxisLinePaint()
     * @see #getAxisLineStroke()
     * @see #setAxisLineVisible(boolean)
     */
    public boolean isAxisLineVisible() {
        return this.axisLineVisible;
    }

    /**
     * Sets a flag that controls whether or not the axis line is visible and
     * sends an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param visible  the flag.
     *
     * @see #isAxisLineVisible()
     * @see #setAxisLinePaint(Paint)
     * @see #setAxisLineStroke(Stroke)
     */
    public void setAxisLineVisible(boolean visible) {
        this.axisLineVisible = visible;
        fireChangeEvent();
    }

    /**
     * Returns the paint used to draw the axis line.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setAxisLinePaint(Paint)
     */
    public Paint getAxisLinePaint() {
        return this.axisLinePaint;
    }

    /**
     * Sets the paint used to draw the axis line and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getAxisLinePaint()
     */
    public void setAxisLinePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.axisLinePaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the stroke used to draw the axis line.
     *
     * @return The stroke (never <code>null</code>).
     *
     * @see #setAxisLineStroke(Stroke)
     */
    public Stroke getAxisLineStroke() {
        return this.axisLineStroke;
    }

    /**
     * Sets the stroke used to draw the axis line and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     *
     * @see #getAxisLineStroke()
     */
    public void setAxisLineStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.axisLineStroke = stroke;
        fireChangeEvent();
    }

    /**
     * Returns a flag indicating whether or not the tick labels are visible.
     *
     * @return The flag.
     *
     * @see #getTickLabelFont()
     * @see #getTickLabelPaint()
     * @see #setTickLabelsVisible(boolean)
     */
    public boolean isTickLabelsVisible() {
        return this.tickLabelsVisible;
    }

    /**
     * Sets the flag that determines whether or not the tick labels are
     * visible and sends an {@link AxisChangeEvent} to all registered
     * listeners.
     *
     * @param flag  the flag.
     *
     * @see #isTickLabelsVisible()
     * @see #setTickLabelFont(Font)
     * @see #setTickLabelPaint(Paint)
     */
    public void setTickLabelsVisible(boolean flag) {

        if (flag != this.tickLabelsVisible) {
            this.tickLabelsVisible = flag;
            fireChangeEvent();
        }

    }

    /**
     * Returns the flag that indicates whether or not the minor tick marks are
     * showing.
     *
     * @return The flag that indicates whether or not the minor tick marks are
     *         showing.
     *
     * @see #setMinorTickMarksVisible(boolean)
     *
     * @since 1.0.12
     */
    public boolean isMinorTickMarksVisible() {
        return this.minorTickMarksVisible;
    }

    /**
     * Sets the flag that indicates whether or not the minor tick marks are showing
     * and sends an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param flag  the flag.
     *
     * @see #isMinorTickMarksVisible()
     *
     * @since 1.0.12
     */
    public void setMinorTickMarksVisible(boolean flag) {
        if (flag != this.minorTickMarksVisible) {
            this.minorTickMarksVisible = flag;
            fireChangeEvent();
        }
    }

    /**
     * Returns the font used for the tick labels (if showing).
     *
     * @return The font (never <code>null</code>).
     *
     * @see #setTickLabelFont(Font)
     */
    public Font getTickLabelFont() {
        return this.tickLabelFont;
    }

    /**
     * Sets the font for the tick labels and sends an {@link AxisChangeEvent}
     * to all registered listeners.
     *
     * @param font  the font (<code>null</code> not allowed).
     *
     * @see #getTickLabelFont()
     */
    public void setTickLabelFont(Font font) {

        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }

        if (!this.tickLabelFont.equals(font)) {
            this.tickLabelFont = font;
            fireChangeEvent();
        }

    }

    /**
     * Returns the color/shade used for the tick labels.
     *
     * @return The paint used for the tick labels.
     *
     * @see #setTickLabelPaint(Paint)
     */
    public Paint getTickLabelPaint() {
        return this.tickLabelPaint;
    }

    /**
     * Sets the paint used to draw tick labels (if they are showing) and
     * sends an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getTickLabelPaint()
     */
    public void setTickLabelPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.tickLabelPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the insets for the tick labels.
     *
     * @return The insets (never <code>null</code>).
     *
     * @see #setTickLabelInsets(RectangleInsets)
     */
    public RectangleInsets getTickLabelInsets() {
        return this.tickLabelInsets;
    }

    /**
     * Sets the insets for the tick labels and sends an {@link AxisChangeEvent}
     * to all registered listeners.
     *
     * @param insets  the insets (<code>null</code> not permitted).
     *
     * @see #getTickLabelInsets()
     */
    public void setTickLabelInsets(RectangleInsets insets) {
        if (insets == null) {
            throw new IllegalArgumentException("Null 'insets' argument.");
        }
        if (!this.tickLabelInsets.equals(insets)) {
            this.tickLabelInsets = insets;
            fireChangeEvent();
        }
    }

    /**
     * Returns the flag that indicates whether or not the tick marks are
     * showing.
     *
     * @return The flag that indicates whether or not the tick marks are
     *         showing.
     *
     * @see #setTickMarksVisible(boolean)
     */
    public boolean isTickMarksVisible() {
        return this.tickMarksVisible;
    }

    /**
     * Sets the flag that indicates whether or not the tick marks are showing
     * and sends an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param flag  the flag.
     *
     * @see #isTickMarksVisible()
     */
    public void setTickMarksVisible(boolean flag) {
        if (flag != this.tickMarksVisible) {
            this.tickMarksVisible = flag;
            fireChangeEvent();
        }
    }

    /**
     * Returns the inside length of the tick marks.
     *
     * @return The length.
     *
     * @see #getTickMarkOutsideLength()
     * @see #setTickMarkInsideLength(float)
     */
    public float getTickMarkInsideLength() {
        return this.tickMarkInsideLength;
    }

    /**
     * Sets the inside length of the tick marks and sends
     * an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param length  the new length.
     *
     * @see #getTickMarkInsideLength()
     */
    public void setTickMarkInsideLength(float length) {
        this.tickMarkInsideLength = length;
        fireChangeEvent();
    }

    /**
     * Returns the outside length of the tick marks.
     *
     * @return The length.
     *
     * @see #getTickMarkInsideLength()
     * @see #setTickMarkOutsideLength(float)
     */
    public float getTickMarkOutsideLength() {
        return this.tickMarkOutsideLength;
    }

    /**
     * Sets the outside length of the tick marks and sends
     * an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param length  the new length.
     *
     * @see #getTickMarkInsideLength()
     */
    public void setTickMarkOutsideLength(float length) {
        this.tickMarkOutsideLength = length;
        fireChangeEvent();
    }

    /**
     * Returns the stroke used to draw tick marks.
     *
     * @return The stroke (never <code>null</code>).
     *
     * @see #setTickMarkStroke(Stroke)
     */
    public Stroke getTickMarkStroke() {
        return this.tickMarkStroke;
    }

    /**
     * Sets the stroke used to draw tick marks and sends
     * an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     *
     * @see #getTickMarkStroke()
     */
    public void setTickMarkStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        if (!this.tickMarkStroke.equals(stroke)) {
            this.tickMarkStroke = stroke;
            fireChangeEvent();
        }
    }

    /**
     * Returns the paint used to draw tick marks (if they are showing).
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setTickMarkPaint(Paint)
     */
    public Paint getTickMarkPaint() {
        return this.tickMarkPaint;
    }

    /**
     * Sets the paint used to draw tick marks and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getTickMarkPaint()
     */
    public void setTickMarkPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.tickMarkPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the inside length of the minor tick marks.
     *
     * @return The length.
     *
     * @see #getMinorTickMarkOutsideLength()
     * @see #setMinorTickMarkInsideLength(float)
     *
     * @since 1.0.12
     */
    public float getMinorTickMarkInsideLength() {
        return this.minorTickMarkInsideLength;
    }

    /**
     * Sets the inside length of the minor tick marks and sends
     * an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param length  the new length.
     *
     * @see #getMinorTickMarkInsideLength()
     *
     * @since 1.0.12
     */
    public void setMinorTickMarkInsideLength(float length) {
        this.minorTickMarkInsideLength = length;
        fireChangeEvent();
    }

    /**
     * Returns the outside length of the minor tick marks.
     *
     * @return The length.
     *
     * @see #getMinorTickMarkInsideLength()
     * @see #setMinorTickMarkOutsideLength(float)
     *
     * @since 1.0.12
     */
    public float getMinorTickMarkOutsideLength() {
        return this.minorTickMarkOutsideLength;
    }

    /**
     * Sets the outside length of the minor tick marks and sends
     * an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param length  the new length.
     *
     * @see #getMinorTickMarkInsideLength()
     *
     * @since 1.0.12
     */
    public void setMinorTickMarkOutsideLength(float length) {
        this.minorTickMarkOutsideLength = length;
        fireChangeEvent();
    }

    /**
     * Returns the plot that the axis is assigned to.  This method will return
     * <code>null</code> if the axis is not currently assigned to a plot.
     *
     * @return The plot that the axis is assigned to (possibly
     *         <code>null</code>).
     *
     * @see #setPlot(Plot)
     */
    public Plot getPlot() {
        return this.plot;
    }

    /**
     * Sets a reference to the plot that the axis is assigned to.
     * <P>
     * This method is used internally, you shouldn't need to call it yourself.
     *
     * @param plot  the plot.
     *
     * @see #getPlot()
     */
    public void setPlot(Plot plot) {
        this.plot = plot;
        configure();
    }

    /**
     * Returns the fixed dimension for the axis.
     *
     * @return The fixed dimension.
     *
     * @see #setFixedDimension(double)
     */
    public double getFixedDimension() {
        return this.fixedDimension;
    }

    /**
     * Sets the fixed dimension for the axis.
     * <P>
     * This is used when combining more than one plot on a chart.  In this case,
     * there may be several axes that need to have the same height or width so
     * that they are aligned.  This method is used to fix a dimension for the
     * axis (the context determines whether the dimension is horizontal or
     * vertical).
     *
     * @param dimension  the fixed dimension.
     *
     * @see #getFixedDimension()
     */
    public void setFixedDimension(double dimension) {
        this.fixedDimension = dimension;
    }

    /**
     * Configures the axis to work with the current plot.  Override this method
     * to perform any special processing (such as auto-rescaling).
     */
    public abstract void configure();

    /**
     * Estimates the space (height or width) required to draw the axis.
     *
     * @param g2  the graphics device.
     * @param plot  the plot that the axis belongs to.
     * @param plotArea  the area within which the plot (including axes) should
     *                  be drawn.
     * @param edge  the axis location.
     * @param space  space already reserved.
     *
     * @return The space required to draw the axis (including pre-reserved
     *         space).
     */
    public abstract AxisSpace reserveSpace(Graphics2D g2, Plot plot,
                                           Rectangle2D plotArea,
                                           RectangleEdge edge,
                                           AxisSpace space);

    /**
     * Draws the axis on a Java 2D graphics device (such as the screen or a
     * printer).
     *
     * @param g2  the graphics device (<code>null</code> not permitted).
     * @param cursor  the cursor location (determines where to draw the axis).
     * @param plotArea  the area within which the axes and plot should be drawn.
     * @param dataArea  the area within which the data should be drawn.
     * @param edge  the axis location (<code>null</code> not permitted).
     * @param plotState  collects information about the plot
     *                   (<code>null</code> permitted).
     *
     * @return The axis state (never <code>null</code>).
     */
    public abstract AxisState draw(Graphics2D g2,
                                   double cursor,
                                   Rectangle2D plotArea,
                                   Rectangle2D dataArea,
                                   RectangleEdge edge,
                                   PlotRenderingInfo plotState);

    /**
     * Calculates the positions of the ticks for the axis, storing the results
     * in the tick list (ready for drawing).
     *
     * @param g2  the graphics device.
     * @param state  the axis state.
     * @param dataArea  the area inside the axes.
     * @param edge  the edge on which the axis is located.
     *
     * @return The list of ticks.
     */
    public abstract List refreshTicks(Graphics2D g2, AxisState state,
            Rectangle2D dataArea, RectangleEdge edge);

    /**
     * Created an entity for the axis.
     *
     * @param cursor  the initial cursor value.
     * @param state  the axis state after completion of the drawing with a
     *     possibly updated cursor position.
     * @param dataArea  the data area.
     * @param edge  the edge.
     * @param plotState  the PlotRenderingInfo from which a reference to the
     *     entity collection can be obtained.
     *
     * @since 1.0.13
     */
	protected void createAndAddEntity(double cursor, AxisState state,
            Rectangle2D dataArea, RectangleEdge edge,
            PlotRenderingInfo plotState){

		if (plotState == null || plotState.getOwner() == null) {
            return;  // no need to create entity if we can´t save it anyways...
        }
		Rectangle2D hotspot = null;
		if (edge.equals(RectangleEdge.TOP)){
			hotspot = new Rectangle2D.Double(dataArea.getX(),
                    state.getCursor(), dataArea.getWidth(),
                    cursor - state.getCursor());
		}
		else if(edge.equals(RectangleEdge.BOTTOM)) {
			hotspot = new Rectangle2D.Double(dataArea.getX(), cursor,
                    dataArea.getWidth(), state.getCursor() - cursor);
		}
		else if(edge.equals(RectangleEdge.LEFT)) {
			hotspot = new Rectangle2D.Double(state.getCursor(),
                    dataArea.getY(), cursor - state.getCursor(),
                    dataArea.getHeight());
		}
		else if(edge.equals(RectangleEdge.RIGHT)){
			hotspot = new Rectangle2D.Double(cursor, dataArea.getY(),
                    state.getCursor() - cursor, dataArea.getHeight());
		}
		EntityCollection e = plotState.getOwner().getEntityCollection();
		if (e != null) {
            e.add(new AxisEntity(hotspot, this));
        }
	}

    /**
     * Registers an object for notification of changes to the axis.
     *
     * @param listener  the object that is being registered.
     *
     * @see #removeChangeListener(AxisChangeListener)
     */
    public void addChangeListener(AxisChangeListener listener) {
        this.listenerList.add(AxisChangeListener.class, listener);
    }

    /**
     * Deregisters an object for notification of changes to the axis.
     *
     * @param listener  the object to deregister.
     *
     * @see #addChangeListener(AxisChangeListener)
     */
    public void removeChangeListener(AxisChangeListener listener) {
        this.listenerList.remove(AxisChangeListener.class, listener);
    }

    /**
     * Returns <code>true</code> if the specified object is registered with
     * the dataset as a listener.  Most applications won't need to call this
     * method, it exists mainly for use by unit testing code.
     *
     * @param listener  the listener.
     *
     * @return A boolean.
     */
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return list.contains(listener);
    }

    /**
     * Notifies all registered listeners that the axis has changed.
     * The AxisChangeEvent provides information about the change.
     *
     * @param event  information about the change to the axis.
     */
    protected void notifyListeners(AxisChangeEvent event) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == AxisChangeListener.class) {
                ((AxisChangeListener) listeners[i + 1]).axisChanged(event);
            }
        }
    }

    /**
     * Sends an {@link AxisChangeEvent} to all registered listeners.
     *
     * @since 1.0.12
     */
    protected void fireChangeEvent() {
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns a rectangle that encloses the axis label.  This is typically
     * used for layout purposes (it gives the maximum dimensions of the label).
     *
     * @param g2  the graphics device.
     * @param edge  the edge of the plot area along which the axis is measuring.
     *
     * @return The enclosing rectangle.
     */
    protected Rectangle2D getLabelEnclosure(Graphics2D g2, RectangleEdge edge) {

        Rectangle2D result = new Rectangle2D.Double();
        String axisLabel = getLabel();
        if (axisLabel != null && !axisLabel.equals("")) {
            FontMetrics fm = g2.getFontMetrics(getLabelFont());
            Rectangle2D bounds = TextUtilities.getTextBounds(axisLabel, g2, fm);
            RectangleInsets insets = getLabelInsets();
            bounds = insets.createOutsetRectangle(bounds);
            double angle = getLabelAngle();
            if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) {
                angle = angle - Math.PI / 2.0;
            }
            double x = bounds.getCenterX();
            double y = bounds.getCenterY();
            AffineTransform transformer
                = AffineTransform.getRotateInstance(angle, x, y);
            Shape labelBounds = transformer.createTransformedShape(bounds);
            result = labelBounds.getBounds2D();
        }

        return result;

    }

    /**
     * Draws the axis label.
     *
     * @param label  the label text.
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param dataArea  the area inside the axes.
     * @param edge  the location of the axis.
     * @param state  the axis state (<code>null</code> not permitted).
     *
     * @return Information about the axis.
     */
    protected AxisState drawLabel(String label, Graphics2D g2,
            Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge,
            AxisState state) {

        // it is unlikely that 'state' will be null, but check anyway...
        if (state == null) {
            throw new IllegalArgumentException("Null 'state' argument.");
        }

        if ((label == null) || (label.equals(""))) {
            return state;
        }

        Font font = getLabelFont();
        RectangleInsets insets = getLabelInsets();
        g2.setFont(font);
        g2.setPaint(getLabelPaint());
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D labelBounds = TextUtilities.getTextBounds(label, g2, fm);

        if (edge == RectangleEdge.TOP) {
            AffineTransform t = AffineTransform.getRotateInstance(
                    getLabelAngle(), labelBounds.getCenterX(),
                    labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = dataArea.getCenterX();
            double labely = state.getCursor() - insets.getBottom()
                            - labelBounds.getHeight() / 2.0;
            TextUtilities.drawRotatedString(label, g2, (float) labelx,
                    (float) labely, TextAnchor.CENTER, getLabelAngle(),
                    TextAnchor.CENTER);
            state.cursorUp(insets.getTop() + labelBounds.getHeight()
                    + insets.getBottom());
        }
        else if (edge == RectangleEdge.BOTTOM) {
            AffineTransform t = AffineTransform.getRotateInstance(
                    getLabelAngle(), labelBounds.getCenterX(),
                    labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = dataArea.getCenterX();
            double labely = state.getCursor()
                            + insets.getTop() + labelBounds.getHeight() / 2.0;
            TextUtilities.drawRotatedString(label, g2, (float) labelx,
                    (float) labely, TextAnchor.CENTER, getLabelAngle(),
                    TextAnchor.CENTER);
            state.cursorDown(insets.getTop() + labelBounds.getHeight()
                    + insets.getBottom());
        }
        else if (edge == RectangleEdge.LEFT) {
            AffineTransform t = AffineTransform.getRotateInstance(
                    getLabelAngle() - Math.PI / 2.0, labelBounds.getCenterX(),
                    labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = state.getCursor()
                            - insets.getRight() - labelBounds.getWidth() / 2.0;
            double labely = dataArea.getCenterY();
            TextUtilities.drawRotatedString(label, g2, (float) labelx,
                    (float) labely, TextAnchor.CENTER,
                    getLabelAngle() - Math.PI / 2.0, TextAnchor.CENTER);
            state.cursorLeft(insets.getLeft() + labelBounds.getWidth()
                    + insets.getRight());
        }
        else if (edge == RectangleEdge.RIGHT) {

            AffineTransform t = AffineTransform.getRotateInstance(
                    getLabelAngle() + Math.PI / 2.0,
                    labelBounds.getCenterX(), labelBounds.getCenterY());
            Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = state.getCursor()
                            + insets.getLeft() + labelBounds.getWidth() / 2.0;
            double labely = dataArea.getY() + dataArea.getHeight() / 2.0;
            TextUtilities.drawRotatedString(label, g2, (float) labelx,
                    (float) labely, TextAnchor.CENTER,
                    getLabelAngle() + Math.PI / 2.0, TextAnchor.CENTER);
            state.cursorRight(insets.getLeft() + labelBounds.getWidth()
                    + insets.getRight());

        }

        return state;

    }

    /**
     * Draws an axis line at the current cursor position and edge.
     *
     * @param g2  the graphics device.
     * @param cursor  the cursor position.
     * @param dataArea  the data area.
     * @param edge  the edge.
     */
    protected void drawAxisLine(Graphics2D g2, double cursor,
            Rectangle2D dataArea, RectangleEdge edge) {

        Line2D axisLine = null;
        if (edge == RectangleEdge.TOP) {
            axisLine = new Line2D.Double(dataArea.getX(), cursor,
                    dataArea.getMaxX(), cursor);
        }
        else if (edge == RectangleEdge.BOTTOM) {
            axisLine = new Line2D.Double(dataArea.getX(), cursor,
                    dataArea.getMaxX(), cursor);
        }
        else if (edge == RectangleEdge.LEFT) {
            axisLine = new Line2D.Double(cursor, dataArea.getY(), cursor,
                    dataArea.getMaxY());
        }
        else if (edge == RectangleEdge.RIGHT) {
            axisLine = new Line2D.Double(cursor, dataArea.getY(), cursor,
                    dataArea.getMaxY());
        }
        g2.setPaint(this.axisLinePaint);
        g2.setStroke(this.axisLineStroke);
        g2.draw(axisLine);

    }

    /**
     * Returns a clone of the axis.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if some component of the axis does
     *         not support cloning.
     */
    public Object clone() throws CloneNotSupportedException {
        Axis clone = (Axis) super.clone();
        // It's up to the plot which clones up to restore the correct references
        clone.plot = null;
        clone.listenerList = new EventListenerList();
        return clone;
    }

    /**
     * Tests this axis for equality with another object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Axis)) {
            return false;
        }
        Axis that = (Axis) obj;
        if (this.visible != that.visible) {
            return false;
        }
        if (!ObjectUtilities.equal(this.label, that.label)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.labelFont, that.labelFont)) {
            return false;
        }
        if (!PaintUtilities.equal(this.labelPaint, that.labelPaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.labelInsets, that.labelInsets)) {
            return false;
        }
        if (this.labelAngle != that.labelAngle) {
            return false;
        }
        if (this.axisLineVisible != that.axisLineVisible) {
            return false;
        }
        if (!ObjectUtilities.equal(this.axisLineStroke, that.axisLineStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.axisLinePaint, that.axisLinePaint)) {
            return false;
        }
        if (this.tickLabelsVisible != that.tickLabelsVisible) {
            return false;
        }
        if (!ObjectUtilities.equal(this.tickLabelFont, that.tickLabelFont)) {
            return false;
        }
        if (!PaintUtilities.equal(this.tickLabelPaint, that.tickLabelPaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(
            this.tickLabelInsets, that.tickLabelInsets
        )) {
            return false;
        }
        if (this.tickMarksVisible != that.tickMarksVisible) {
            return false;
        }
        if (this.tickMarkInsideLength != that.tickMarkInsideLength) {
            return false;
        }
        if (this.tickMarkOutsideLength != that.tickMarkOutsideLength) {
            return false;
        }
        if (!PaintUtilities.equal(this.tickMarkPaint, that.tickMarkPaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.tickMarkStroke, that.tickMarkStroke)) {
            return false;
        }
        if (this.minorTickMarksVisible != that.minorTickMarksVisible) {
            return false;
        }
        if (this.minorTickMarkInsideLength != that.minorTickMarkInsideLength) {
            return false;
        }
        if (this.minorTickMarkOutsideLength != that.minorTickMarkOutsideLength) {
            return false;
        }
        if (this.fixedDimension != that.fixedDimension) {
            return false;
        }
        return true;
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
        SerialUtilities.writePaint(this.labelPaint, stream);
        SerialUtilities.writePaint(this.tickLabelPaint, stream);
        SerialUtilities.writeStroke(this.axisLineStroke, stream);
        SerialUtilities.writePaint(this.axisLinePaint, stream);
        SerialUtilities.writeStroke(this.tickMarkStroke, stream);
        SerialUtilities.writePaint(this.tickMarkPaint, stream);
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
        this.labelPaint = SerialUtilities.readPaint(stream);
        this.tickLabelPaint = SerialUtilities.readPaint(stream);
        this.axisLineStroke = SerialUtilities.readStroke(stream);
        this.axisLinePaint = SerialUtilities.readPaint(stream);
        this.tickMarkStroke = SerialUtilities.readStroke(stream);
        this.tickMarkPaint = SerialUtilities.readPaint(stream);
        this.listenerList = new EventListenerList();
    }

}
