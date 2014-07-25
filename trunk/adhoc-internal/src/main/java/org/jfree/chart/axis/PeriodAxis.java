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
 * ---------------
 * PeriodAxis.java
 * ---------------
 * (C) Copyright 2004-2009, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 01-Jun-2004 : Version 1 (DG);
 * 16-Sep-2004 : Fixed bug in equals() method, added clone() method and
 *               PublicCloneable interface (DG);
 * 25-Nov-2004 : Updates to support major and minor tick marks (DG);
 * 25-Feb-2005 : Fixed some tick mark bugs (DG);
 * 15-Apr-2005 : Fixed some more tick mark bugs (DG);
 * 26-Apr-2005 : Removed LOGGER (DG);
 * 16-Jun-2005 : Fixed zooming (DG);
 * 15-Sep-2005 : Changed configure() method to check autoRange flag,
 *               and added ticks to state (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 06-Oct-2006 : Updated for deprecations in RegularTimePeriod and
 *               subclasses (DG);
 * 22-Mar-2007 : Use new defaultAutoRange attribute (DG);
 * 31-Jul-2007 : Fix for inverted axis labelling (see bug 1763413) (DG);
 * 08-Apr-2008 : Notify listeners in setRange(Range, boolean, boolean) - fixes
 *               bug 1932146 (DG);
 * 16-Jan-2009 : Fixed bug 2490803, a problem in the setRange() method (DG);
 * 02-Mar-2009 : Added locale - see patch 2569670 by Benjamin Bignell (DG);
 * 02-Mar-2009 : Fixed draw() method to check tickMarksVisible and
 *               tickLabelsVisible (DG);
 *
 */

package org.jfree.chart.axis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Year;
import org.jfree.io.SerialUtilities;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.PublicCloneable;

/**
 * An axis that displays a date scale based on a
 * {@link org.jfree.data.time.RegularTimePeriod}.  This axis works when
 * displayed across the bottom or top of a plot, but is broken for display at
 * the left or right of charts.
 */
public class PeriodAxis extends ValueAxis
        implements Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 8353295532075872069L;

    /** The first time period in the overall range. */
    private RegularTimePeriod first;

    /** The last time period in the overall range. */
    private RegularTimePeriod last;

    /**
     * The time zone used to convert 'first' and 'last' to absolute
     * milliseconds.
     */
    private TimeZone timeZone;

    /**
     * The locale (never <code>null</code>).
     * 
     * @since 1.0.13
     */
    private Locale locale;

    /**
     * A calendar used for date manipulations in the current time zone and
     * locale.
     */
    private Calendar calendar;

    /**
     * The {@link RegularTimePeriod} subclass used to automatically determine
     * the axis range.
     */
    private Class autoRangeTimePeriodClass;

    /**
     * Indicates the {@link RegularTimePeriod} subclass that is used to
     * determine the spacing of the major tick marks.
     */
    private Class majorTickTimePeriodClass;

    /**
     * A flag that indicates whether or not tick marks are visible for the
     * axis.
     */
    private boolean minorTickMarksVisible;

    /**
     * Indicates the {@link RegularTimePeriod} subclass that is used to
     * determine the spacing of the minor tick marks.
     */
    private Class minorTickTimePeriodClass;

    /** The length of the tick mark inside the data area (zero permitted). */
    private float minorTickMarkInsideLength = 0.0f;

    /** The length of the tick mark outside the data area (zero permitted). */
    private float minorTickMarkOutsideLength = 2.0f;

    /** The stroke used to draw tick marks. */
    private transient Stroke minorTickMarkStroke = new BasicStroke(0.5f);

    /** The paint used to draw tick marks. */
    private transient Paint minorTickMarkPaint = Color.black;

    /** Info for each labelling band. */
    private PeriodAxisLabelInfo[] labelInfo;

    /**
     * Creates a new axis.
     *
     * @param label  the axis label.
     */
    public PeriodAxis(String label) {
        this(label, new Day(), new Day());
    }

    /**
     * Creates a new axis.
     *
     * @param label  the axis label (<code>null</code> permitted).
     * @param first  the first time period in the axis range
     *               (<code>null</code> not permitted).
     * @param last  the last time period in the axis range
     *              (<code>null</code> not permitted).
     */
    public PeriodAxis(String label,
                      RegularTimePeriod first, RegularTimePeriod last) {
        this(label, first, last, TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * Creates a new axis.
     *
     * @param label  the axis label (<code>null</code> permitted).
     * @param first  the first time period in the axis range
     *               (<code>null</code> not permitted).
     * @param last  the last time period in the axis range
     *              (<code>null</code> not permitted).
     * @param timeZone  the time zone (<code>null</code> not permitted).
     *
     * @deprecated As of version 1.0.13, you should use the constructor that
     *     specifies a Locale also.
     */
    public PeriodAxis(String label,
                      RegularTimePeriod first, RegularTimePeriod last,
                      TimeZone timeZone) {
        this(label, first, last, timeZone, Locale.getDefault());
    }

    /**
     * Creates a new axis.
     *
     * @param label  the axis label (<code>null</code> permitted).
     * @param first  the first time period in the axis range
     *               (<code>null</code> not permitted).
     * @param last  the last time period in the axis range
     *              (<code>null</code> not permitted).
     * @param timeZone  the time zone (<code>null</code> not permitted).
     * @param locale  the locale (<code>null</code> not permitted).
     *
     * @since 1.0.13
     */
    public PeriodAxis(String label, RegularTimePeriod first,
            RegularTimePeriod last, TimeZone timeZone, Locale locale) {
        super(label, null);
        if (timeZone == null) {
            throw new IllegalArgumentException("Null 'timeZone' argument.");
        }
        if (locale == null) {
            throw new IllegalArgumentException("Null 'locale' argument.");
        }
        this.first = first;
        this.last = last;
        this.timeZone = timeZone;
        this.locale = locale;
        this.calendar = Calendar.getInstance(timeZone, locale);
        this.first.peg(this.calendar);
        this.last.peg(this.calendar);
        this.autoRangeTimePeriodClass = first.getClass();
        this.majorTickTimePeriodClass = first.getClass();
        this.minorTickMarksVisible = false;
        this.minorTickTimePeriodClass = RegularTimePeriod.downsize(
                this.majorTickTimePeriodClass);
        setAutoRange(true);
        this.labelInfo = new PeriodAxisLabelInfo[2];
        this.labelInfo[0] = new PeriodAxisLabelInfo(Month.class,
                new SimpleDateFormat("MMM", locale));
        this.labelInfo[1] = new PeriodAxisLabelInfo(Year.class,
                new SimpleDateFormat("yyyy", locale));
    }

    /**
     * Returns the first time period in the axis range.
     *
     * @return The first time period (never <code>null</code>).
     */
    public RegularTimePeriod getFirst() {
        return this.first;
    }

    /**
     * Sets the first time period in the axis range and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param first  the time period (<code>null</code> not permitted).
     */
    public void setFirst(RegularTimePeriod first) {
        if (first == null) {
            throw new IllegalArgumentException("Null 'first' argument.");
        }
        this.first = first;
        this.first.peg(this.calendar);
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns the last time period in the axis range.
     *
     * @return The last time period (never <code>null</code>).
     */
    public RegularTimePeriod getLast() {
        return this.last;
    }

    /**
     * Sets the last time period in the axis range and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param last  the time period (<code>null</code> not permitted).
     */
    public void setLast(RegularTimePeriod last) {
        if (last == null) {
            throw new IllegalArgumentException("Null 'last' argument.");
        }
        this.last = last;
        this.last.peg(this.calendar);
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns the time zone used to convert the periods defining the axis
     * range into absolute milliseconds.
     *
     * @return The time zone (never <code>null</code>).
     */
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    /**
     * Sets the time zone that is used to convert the time periods into
     * absolute milliseconds.
     *
     * @param zone  the time zone (<code>null</code> not permitted).
     */
    public void setTimeZone(TimeZone zone) {
        if (zone == null) {
            throw new IllegalArgumentException("Null 'zone' argument.");
        }
        this.timeZone = zone;
        this.calendar = Calendar.getInstance(zone, this.locale);
        this.first.peg(this.calendar);
        this.last.peg(this.calendar);
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns the locale for this axis.
     *
     * @return The locale (never (<code>null</code>).
     *
     * @since 1.0.13
     */
    public Locale getLocale() {
        return this.locale;
    }

    /**
     * Returns the class used to create the first and last time periods for
     * the axis range when the auto-range flag is set to <code>true</code>.
     *
     * @return The class (never <code>null</code>).
     */
    public Class getAutoRangeTimePeriodClass() {
        return this.autoRangeTimePeriodClass;
    }

    /**
     * Sets the class used to create the first and last time periods for the
     * axis range when the auto-range flag is set to <code>true</code> and
     * sends an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param c  the class (<code>null</code> not permitted).
     */
    public void setAutoRangeTimePeriodClass(Class c) {
        if (c == null) {
            throw new IllegalArgumentException("Null 'c' argument.");
        }
        this.autoRangeTimePeriodClass = c;
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns the class that controls the spacing of the major tick marks.
     *
     * @return The class (never <code>null</code>).
     */
    public Class getMajorTickTimePeriodClass() {
        return this.majorTickTimePeriodClass;
    }

    /**
     * Sets the class that controls the spacing of the major tick marks, and
     * sends an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param c  the class (a subclass of {@link RegularTimePeriod} is
     *           expected).
     */
    public void setMajorTickTimePeriodClass(Class c) {
        if (c == null) {
            throw new IllegalArgumentException("Null 'c' argument.");
        }
        this.majorTickTimePeriodClass = c;
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns the flag that controls whether or not minor tick marks
     * are displayed for the axis.
     *
     * @return A boolean.
     */
    public boolean isMinorTickMarksVisible() {
        return this.minorTickMarksVisible;
    }

    /**
     * Sets the flag that controls whether or not minor tick marks
     * are displayed for the axis, and sends a {@link AxisChangeEvent}
     * to all registered listeners.
     *
     * @param visible  the flag.
     */
    public void setMinorTickMarksVisible(boolean visible) {
        this.minorTickMarksVisible = visible;
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns the class that controls the spacing of the minor tick marks.
     *
     * @return The class (never <code>null</code>).
     */
    public Class getMinorTickTimePeriodClass() {
        return this.minorTickTimePeriodClass;
    }

    /**
     * Sets the class that controls the spacing of the minor tick marks, and
     * sends an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param c  the class (a subclass of {@link RegularTimePeriod} is
     *           expected).
     */
    public void setMinorTickTimePeriodClass(Class c) {
        if (c == null) {
            throw new IllegalArgumentException("Null 'c' argument.");
        }
        this.minorTickTimePeriodClass = c;
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns the stroke used to display minor tick marks, if they are
     * visible.
     *
     * @return A stroke (never <code>null</code>).
     */
    public Stroke getMinorTickMarkStroke() {
        return this.minorTickMarkStroke;
    }

    /**
     * Sets the stroke used to display minor tick marks, if they are
     * visible, and sends a {@link AxisChangeEvent} to all registered
     * listeners.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     */
    public void setMinorTickMarkStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.minorTickMarkStroke = stroke;
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns the paint used to display minor tick marks, if they are
     * visible.
     *
     * @return A paint (never <code>null</code>).
     */
    public Paint getMinorTickMarkPaint() {
        return this.minorTickMarkPaint;
    }

    /**
     * Sets the paint used to display minor tick marks, if they are
     * visible, and sends a {@link AxisChangeEvent} to all registered
     * listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     */
    public void setMinorTickMarkPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.minorTickMarkPaint = paint;
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns the inside length for the minor tick marks.
     *
     * @return The length.
     */
    public float getMinorTickMarkInsideLength() {
        return this.minorTickMarkInsideLength;
    }

    /**
     * Sets the inside length of the minor tick marks and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param length  the length.
     */
    public void setMinorTickMarkInsideLength(float length) {
        this.minorTickMarkInsideLength = length;
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns the outside length for the minor tick marks.
     *
     * @return The length.
     */
    public float getMinorTickMarkOutsideLength() {
        return this.minorTickMarkOutsideLength;
    }

    /**
     * Sets the outside length of the minor tick marks and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param length  the length.
     */
    public void setMinorTickMarkOutsideLength(float length) {
        this.minorTickMarkOutsideLength = length;
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns an array of label info records.
     *
     * @return An array.
     */
    public PeriodAxisLabelInfo[] getLabelInfo() {
        return this.labelInfo;
    }

    /**
     * Sets the array of label info records and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param info  the info.
     */
    public void setLabelInfo(PeriodAxisLabelInfo[] info) {
        this.labelInfo = info;
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Sets the range for the axis, if requested, sends an
     * {@link AxisChangeEvent} to all registered listeners.  As a side-effect,
     * the auto-range flag is set to <code>false</code> (optional).
     *
     * @param range  the range (<code>null</code> not permitted).
     * @param turnOffAutoRange  a flag that controls whether or not the auto
     *                          range is turned off.
     * @param notify  a flag that controls whether or not listeners are
     *                notified.
     */
    public void setRange(Range range, boolean turnOffAutoRange,
                         boolean notify) {
        long upper = Math.round(range.getUpperBound());
        long lower = Math.round(range.getLowerBound());
        this.first = createInstance(this.autoRangeTimePeriodClass,
                new Date(lower), this.timeZone, this.locale);
        this.last = createInstance(this.autoRangeTimePeriodClass,
                new Date(upper), this.timeZone, this.locale);
        super.setRange(new Range(this.first.getFirstMillisecond(),
                this.last.getLastMillisecond() + 1.0), turnOffAutoRange,
                notify);
    }

    /**
     * Configures the axis to work with the current plot.  Override this method
     * to perform any special processing (such as auto-rescaling).
     */
    public void configure() {
        if (this.isAutoRange()) {
            autoAdjustRange();
        }
    }

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
    public AxisSpace reserveSpace(Graphics2D g2, Plot plot,
                                  Rectangle2D plotArea, RectangleEdge edge,
                                  AxisSpace space) {
        // create a new space object if one wasn't supplied...
        if (space == null) {
            space = new AxisSpace();
        }

        // if the axis is not visible, no additional space is required...
        if (!isVisible()) {
            return space;
        }

        // if the axis has a fixed dimension, return it...
        double dimension = getFixedDimension();
        if (dimension > 0.0) {
            space.ensureAtLeast(dimension, edge);
        }

        // get the axis label size and update the space object...
        Rectangle2D labelEnclosure = getLabelEnclosure(g2, edge);
        double labelHeight = 0.0;
        double labelWidth = 0.0;
        double tickLabelBandsDimension = 0.0;

        for (int i = 0; i < this.labelInfo.length; i++) {
            PeriodAxisLabelInfo info = this.labelInfo[i];
            FontMetrics fm = g2.getFontMetrics(info.getLabelFont());
            tickLabelBandsDimension
                += info.getPadding().extendHeight(fm.getHeight());
        }

        if (RectangleEdge.isTopOrBottom(edge)) {
            labelHeight = labelEnclosure.getHeight();
            space.add(labelHeight + tickLabelBandsDimension, edge);
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            labelWidth = labelEnclosure.getWidth();
            space.add(labelWidth + tickLabelBandsDimension, edge);
        }

        // add space for the outer tick labels, if any...
        double tickMarkSpace = 0.0;
        if (isTickMarksVisible()) {
            tickMarkSpace = getTickMarkOutsideLength();
        }
        if (this.minorTickMarksVisible) {
            tickMarkSpace = Math.max(tickMarkSpace,
                    this.minorTickMarkOutsideLength);
        }
        space.add(tickMarkSpace, edge);
        return space;
    }

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
    public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea,
            Rectangle2D dataArea, RectangleEdge edge,
            PlotRenderingInfo plotState) {

        AxisState axisState = new AxisState(cursor);
        if (isAxisLineVisible()) {
            drawAxisLine(g2, cursor, dataArea, edge);
        }
        if (isTickMarksVisible()) {
            drawTickMarks(g2, axisState, dataArea, edge);
        }
        if (isTickLabelsVisible()) {
            for (int band = 0; band < this.labelInfo.length; band++) {
                axisState = drawTickLabels(band, g2, axisState, dataArea, edge);
            }
        }

        // draw the axis label (note that 'state' is passed in *and*
        // returned)...
        axisState = drawLabel(getLabel(), g2, plotArea, dataArea, edge,
                axisState);
        return axisState;

    }

    /**
     * Draws the tick marks for the axis.
     *
     * @param g2  the graphics device.
     * @param state  the axis state.
     * @param dataArea  the data area.
     * @param edge  the edge.
     */
    protected void drawTickMarks(Graphics2D g2, AxisState state,
                                 Rectangle2D dataArea,
                                 RectangleEdge edge) {
        if (RectangleEdge.isTopOrBottom(edge)) {
            drawTickMarksHorizontal(g2, state, dataArea, edge);
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            drawTickMarksVertical(g2, state, dataArea, edge);
        }
    }

    /**
     * Draws the major and minor tick marks for an axis that lies at the top or
     * bottom of the plot.
     *
     * @param g2  the graphics device.
     * @param state  the axis state.
     * @param dataArea  the data area.
     * @param edge  the edge.
     */
    protected void drawTickMarksHorizontal(Graphics2D g2, AxisState state,
                                           Rectangle2D dataArea,
                                           RectangleEdge edge) {
        List ticks = new ArrayList();
        double x0 = dataArea.getX();
        double y0 = state.getCursor();
        double insideLength = getTickMarkInsideLength();
        double outsideLength = getTickMarkOutsideLength();
        RegularTimePeriod t = createInstance(this.majorTickTimePeriodClass, 
                this.first.getStart(), getTimeZone(), this.locale);
        long t0 = t.getFirstMillisecond();
        Line2D inside = null;
        Line2D outside = null;
        long firstOnAxis = getFirst().getFirstMillisecond();
        long lastOnAxis = getLast().getLastMillisecond() + 1;
        while (t0 <= lastOnAxis) {
            ticks.add(new NumberTick(new Double(t0), "", TextAnchor.CENTER,
                    TextAnchor.CENTER, 0.0));
            x0 = valueToJava2D(t0, dataArea, edge);
            if (edge == RectangleEdge.TOP) {
                inside = new Line2D.Double(x0, y0, x0, y0 + insideLength);
                outside = new Line2D.Double(x0, y0, x0, y0 - outsideLength);
            }
            else if (edge == RectangleEdge.BOTTOM) {
                inside = new Line2D.Double(x0, y0, x0, y0 - insideLength);
                outside = new Line2D.Double(x0, y0, x0, y0 + outsideLength);
            }
            if (t0 >= firstOnAxis) {
                g2.setPaint(getTickMarkPaint());
                g2.setStroke(getTickMarkStroke());
                g2.draw(inside);
                g2.draw(outside);
            }
            // draw minor tick marks
            if (this.minorTickMarksVisible) {
                RegularTimePeriod tminor = createInstance(
                        this.minorTickTimePeriodClass, new Date(t0),
                        getTimeZone(), this.locale);
                long tt0 = tminor.getFirstMillisecond();
                while (tt0 < t.getLastMillisecond()
                        && tt0 < lastOnAxis) {
                    double xx0 = valueToJava2D(tt0, dataArea, edge);
                    if (edge == RectangleEdge.TOP) {
                        inside = new Line2D.Double(xx0, y0, xx0,
                                y0 + this.minorTickMarkInsideLength);
                        outside = new Line2D.Double(xx0, y0, xx0,
                                y0 - this.minorTickMarkOutsideLength);
                    }
                    else if (edge == RectangleEdge.BOTTOM) {
                        inside = new Line2D.Double(xx0, y0, xx0,
                                y0 - this.minorTickMarkInsideLength);
                        outside = new Line2D.Double(xx0, y0, xx0,
                                y0 + this.minorTickMarkOutsideLength);
                    }
                    if (tt0 >= firstOnAxis) {
                        g2.setPaint(this.minorTickMarkPaint);
                        g2.setStroke(this.minorTickMarkStroke);
                        g2.draw(inside);
                        g2.draw(outside);
                    }
                    tminor = tminor.next();
                    tminor.peg(this.calendar);
                    tt0 = tminor.getFirstMillisecond();
                }
            }
            t = t.next();
            t.peg(this.calendar);
            t0 = t.getFirstMillisecond();
        }
        if (edge == RectangleEdge.TOP) {
            state.cursorUp(Math.max(outsideLength,
                    this.minorTickMarkOutsideLength));
        }
        else if (edge == RectangleEdge.BOTTOM) {
            state.cursorDown(Math.max(outsideLength,
                    this.minorTickMarkOutsideLength));
        }
        state.setTicks(ticks);
    }

    /**
     * Draws the tick marks for a vertical axis.
     *
     * @param g2  the graphics device.
     * @param state  the axis state.
     * @param dataArea  the data area.
     * @param edge  the edge.
     */
    protected void drawTickMarksVertical(Graphics2D g2, AxisState state,
                                         Rectangle2D dataArea,
                                         RectangleEdge edge) {
        // FIXME:  implement this...
    }

    /**
     * Draws the tick labels for one "band" of time periods.
     *
     * @param band  the band index (zero-based).
     * @param g2  the graphics device.
     * @param state  the axis state.
     * @param dataArea  the data area.
     * @param edge  the edge where the axis is located.
     *
     * @return The updated axis state.
     */
    protected AxisState drawTickLabels(int band, Graphics2D g2, AxisState state,
                                       Rectangle2D dataArea,
                                       RectangleEdge edge) {

        // work out the initial gap
        double delta1 = 0.0;
        FontMetrics fm = g2.getFontMetrics(this.labelInfo[band].getLabelFont());
        if (edge == RectangleEdge.BOTTOM) {
            delta1 = this.labelInfo[band].getPadding().calculateTopOutset(
                    fm.getHeight());
        }
        else if (edge == RectangleEdge.TOP) {
            delta1 = this.labelInfo[band].getPadding().calculateBottomOutset(
                    fm.getHeight());
        }
        state.moveCursor(delta1, edge);
        long axisMin = this.first.getFirstMillisecond();
        long axisMax = this.last.getLastMillisecond();
        g2.setFont(this.labelInfo[band].getLabelFont());
        g2.setPaint(this.labelInfo[band].getLabelPaint());

        // work out the number of periods to skip for labelling
        RegularTimePeriod p1 = this.labelInfo[band].createInstance(
                new Date(axisMin), this.timeZone, this.locale);
        RegularTimePeriod p2 = this.labelInfo[band].createInstance(
                new Date(axisMax), this.timeZone, this.locale);
        String label1 = this.labelInfo[band].getDateFormat().format(
                new Date(p1.getMiddleMillisecond()));
        String label2 = this.labelInfo[band].getDateFormat().format(
                new Date(p2.getMiddleMillisecond()));
        Rectangle2D b1 = TextUtilities.getTextBounds(label1, g2,
                g2.getFontMetrics());
        Rectangle2D b2 = TextUtilities.getTextBounds(label2, g2,
                g2.getFontMetrics());
        double w = Math.max(b1.getWidth(), b2.getWidth());
        long ww = Math.round(java2DToValue(dataArea.getX() + w + 5.0,
                dataArea, edge));
        if (isInverted()) {
            ww = axisMax - ww;
        }
        else {
            ww = ww - axisMin;
        }
        long length = p1.getLastMillisecond()
                      - p1.getFirstMillisecond();
        int periods = (int) (ww / length) + 1;

        RegularTimePeriod p = this.labelInfo[band].createInstance(
                new Date(axisMin), this.timeZone, this.locale);
        Rectangle2D b = null;
        long lastXX = 0L;
        float y = (float) (state.getCursor());
        TextAnchor anchor = TextAnchor.TOP_CENTER;
        float yDelta = (float) b1.getHeight();
        if (edge == RectangleEdge.TOP) {
            anchor = TextAnchor.BOTTOM_CENTER;
            yDelta = -yDelta;
        }
        while (p.getFirstMillisecond() <= axisMax) {
            float x = (float) valueToJava2D(p.getMiddleMillisecond(), dataArea,
                    edge);
            DateFormat df = this.labelInfo[band].getDateFormat();
            String label = df.format(new Date(p.getMiddleMillisecond()));
            long first = p.getFirstMillisecond();
            long last = p.getLastMillisecond();
            if (last > axisMax) {
                // this is the last period, but it is only partially visible
                // so check that the label will fit before displaying it...
                Rectangle2D bb = TextUtilities.getTextBounds(label, g2,
                        g2.getFontMetrics());
                if ((x + bb.getWidth() / 2) > dataArea.getMaxX()) {
                    float xstart = (float) valueToJava2D(Math.max(first,
                            axisMin), dataArea, edge);
                    if (bb.getWidth() < (dataArea.getMaxX() - xstart)) {
                        x = ((float) dataArea.getMaxX() + xstart) / 2.0f;
                    }
                    else {
                        label = null;
                    }
                }
            }
            if (first < axisMin) {
                // this is the first period, but it is only partially visible
                // so check that the label will fit before displaying it...
                Rectangle2D bb = TextUtilities.getTextBounds(label, g2,
                        g2.getFontMetrics());
                if ((x - bb.getWidth() / 2) < dataArea.getX()) {
                    float xlast = (float) valueToJava2D(Math.min(last,
                            axisMax), dataArea, edge);
                    if (bb.getWidth() < (xlast - dataArea.getX())) {
                        x = (xlast + (float) dataArea.getX()) / 2.0f;
                    }
                    else {
                        label = null;
                    }
                }

            }
            if (label != null) {
                g2.setPaint(this.labelInfo[band].getLabelPaint());
                b = TextUtilities.drawAlignedString(label, g2, x, y, anchor);
            }
            if (lastXX > 0L) {
                if (this.labelInfo[band].getDrawDividers()) {
                    long nextXX = p.getFirstMillisecond();
                    long mid = (lastXX + nextXX) / 2;
                    float mid2d = (float) valueToJava2D(mid, dataArea, edge);
                    g2.setStroke(this.labelInfo[band].getDividerStroke());
                    g2.setPaint(this.labelInfo[band].getDividerPaint());
                    g2.draw(new Line2D.Float(mid2d, y, mid2d, y + yDelta));
                }
            }
            lastXX = last;
            for (int i = 0; i < periods; i++) {
                p = p.next();
            }
            p.peg(this.calendar);
        }
        double used = 0.0;
        if (b != null) {
            used = b.getHeight();
            // work out the trailing gap
            if (edge == RectangleEdge.BOTTOM) {
                used += this.labelInfo[band].getPadding().calculateBottomOutset(
                        fm.getHeight());
            }
            else if (edge == RectangleEdge.TOP) {
                used += this.labelInfo[band].getPadding().calculateTopOutset(
                        fm.getHeight());
            }
        }
        state.moveCursor(used, edge);
        return state;
    }

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
    public List refreshTicks(Graphics2D g2, AxisState state,
            Rectangle2D dataArea, RectangleEdge edge) {
        return Collections.EMPTY_LIST;
    }

    /**
     * Converts a data value to a coordinate in Java2D space, assuming that the
     * axis runs along one edge of the specified dataArea.
     * <p>
     * Note that it is possible for the coordinate to fall outside the area.
     *
     * @param value  the data value.
     * @param area  the area for plotting the data.
     * @param edge  the edge along which the axis lies.
     *
     * @return The Java2D coordinate.
     */
    public double valueToJava2D(double value, Rectangle2D area,
            RectangleEdge edge) {

        double result = Double.NaN;
        double axisMin = this.first.getFirstMillisecond();
        double axisMax = this.last.getLastMillisecond();
        if (RectangleEdge.isTopOrBottom(edge)) {
            double minX = area.getX();
            double maxX = area.getMaxX();
            if (isInverted()) {
                result = maxX + ((value - axisMin) / (axisMax - axisMin))
                         * (minX - maxX);
            }
            else {
                result = minX + ((value - axisMin) / (axisMax - axisMin))
                         * (maxX - minX);
            }
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            double minY = area.getMinY();
            double maxY = area.getMaxY();
            if (isInverted()) {
                result = minY + (((value - axisMin) / (axisMax - axisMin))
                         * (maxY - minY));
            }
            else {
                result = maxY - (((value - axisMin) / (axisMax - axisMin))
                         * (maxY - minY));
            }
        }
        return result;

    }

    /**
     * Converts a coordinate in Java2D space to the corresponding data value,
     * assuming that the axis runs along one edge of the specified dataArea.
     *
     * @param java2DValue  the coordinate in Java2D space.
     * @param area  the area in which the data is plotted.
     * @param edge  the edge along which the axis lies.
     *
     * @return The data value.
     */
    public double java2DToValue(double java2DValue, Rectangle2D area,
            RectangleEdge edge) {

        double result = Double.NaN;
        double min = 0.0;
        double max = 0.0;
        double axisMin = this.first.getFirstMillisecond();
        double axisMax = this.last.getLastMillisecond();
        if (RectangleEdge.isTopOrBottom(edge)) {
            min = area.getX();
            max = area.getMaxX();
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            min = area.getMaxY();
            max = area.getY();
        }
        if (isInverted()) {
             result = axisMax - ((java2DValue - min) / (max - min)
                      * (axisMax - axisMin));
        }
        else {
             result = axisMin + ((java2DValue - min) / (max - min)
                      * (axisMax - axisMin));
        }
        return result;
    }

    /**
     * Rescales the axis to ensure that all data is visible.
     */
    protected void autoAdjustRange() {

        Plot plot = getPlot();
        if (plot == null) {
            return;  // no plot, no data
        }

        if (plot instanceof ValueAxisPlot) {
            ValueAxisPlot vap = (ValueAxisPlot) plot;

            Range r = vap.getDataRange(this);
            if (r == null) {
                r = getDefaultAutoRange();
            }

            long upper = Math.round(r.getUpperBound());
            long lower = Math.round(r.getLowerBound());
            this.first = createInstance(this.autoRangeTimePeriodClass,
                    new Date(lower), this.timeZone, this.locale);
            this.last = createInstance(this.autoRangeTimePeriodClass,
                    new Date(upper), this.timeZone, this.locale);
            setRange(r, false, false);
        }

    }

    /**
     * Tests the axis for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PeriodAxis)) {
            return false;
        }
        PeriodAxis that = (PeriodAxis) obj;
        if (!this.first.equals(that.first)) {
            return false;
        }
        if (!this.last.equals(that.last)) {
            return false;
        }
        if (!this.timeZone.equals(that.timeZone)) {
            return false;
        }
        if (!this.locale.equals(that.locale)) {
            return false;
        }
        if (!this.autoRangeTimePeriodClass.equals(
                that.autoRangeTimePeriodClass)) {
            return false;
        }
        if (!(isMinorTickMarksVisible() == that.isMinorTickMarksVisible())) {
            return false;
        }
        if (!this.majorTickTimePeriodClass.equals(
                that.majorTickTimePeriodClass)) {
            return false;
        }
        if (!this.minorTickTimePeriodClass.equals(
                that.minorTickTimePeriodClass)) {
            return false;
        }
        if (!this.minorTickMarkPaint.equals(that.minorTickMarkPaint)) {
            return false;
        }
        if (!this.minorTickMarkStroke.equals(that.minorTickMarkStroke)) {
            return false;
        }
        if (!Arrays.equals(this.labelInfo, that.labelInfo)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return A hash code.
     */
    public int hashCode() {
        if (getLabel() != null) {
            return getLabel().hashCode();
        }
        else {
            return 0;
        }
    }

    /**
     * Returns a clone of the axis.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  this class is cloneable, but
     *         subclasses may not be.
     */
    public Object clone() throws CloneNotSupportedException {
        PeriodAxis clone = (PeriodAxis) super.clone();
        clone.timeZone = (TimeZone) this.timeZone.clone();
        clone.labelInfo = new PeriodAxisLabelInfo[this.labelInfo.length];
        for (int i = 0; i < this.labelInfo.length; i++) {
            clone.labelInfo[i] = this.labelInfo[i];  // copy across references
                                                     // to immutable objs
        }
        return clone;
    }

    /**
     * A utility method used to create a particular subclass of the
     * {@link RegularTimePeriod} class that includes the specified millisecond,
     * assuming the specified time zone.
     *
     * @param periodClass  the class.
     * @param millisecond  the time.
     * @param zone  the time zone.
     * @param locale  the locale.
     *
     * @return The time period.
     */
    private RegularTimePeriod createInstance(Class periodClass, 
            Date millisecond, TimeZone zone, Locale locale) {
        RegularTimePeriod result = null;
        try {
            Constructor c = periodClass.getDeclaredConstructor(new Class[] {
                    Date.class, TimeZone.class, Locale.class});
            result = (RegularTimePeriod) c.newInstance(new Object[] {
                    millisecond, zone, locale});
        }
        catch (Exception e) {
            try {
                Constructor c = periodClass.getDeclaredConstructor(new Class[] {
                        Date.class});
                result = (RegularTimePeriod) c.newInstance(new Object[] {
                        millisecond});
            }
            catch (Exception e2) {
                // do nothing
            }
        }
        return result;
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
        SerialUtilities.writeStroke(this.minorTickMarkStroke, stream);
        SerialUtilities.writePaint(this.minorTickMarkPaint, stream);
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
        this.minorTickMarkStroke = SerialUtilities.readStroke(stream);
        this.minorTickMarkPaint = SerialUtilities.readPaint(stream);
    }

}
