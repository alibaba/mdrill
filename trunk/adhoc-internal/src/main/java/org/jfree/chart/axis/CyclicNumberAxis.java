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
 * ---------------------
 * CyclicNumberAxis.java
 * ---------------------
 * (C) Copyright 2003-2008, by Nicolas Brodu and Contributors.
 *
 * Original Author:  Nicolas Brodu;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 19-Nov-2003 : Initial import to JFreeChart from the JSynoptic project (NB);
 * 16-Mar-2004 : Added plotState to draw() method (DG);
 * 07-Apr-2004 : Modifed text bounds calculation (DG);
 * 21-Apr-2005 : Replaced Insets with RectangleInsets, removed redundant
 *               argument in selectAutoTickUnit() (DG);
 * 22-Apr-2005 : Renamed refreshHorizontalTicks() --> refreshTicksHorizontal
 *               (for consistency with other classes) and removed unused
 *               parameters (DG);
 * 08-Jun-2005 : Fixed equals() method to handle GradientPaint (DG);
 *
 */

package org.jfree.chart.axis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.List;

import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.Range;
import org.jfree.io.SerialUtilities;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;

/**
This class extends NumberAxis and handles cycling.

Traditional representation of data in the range x0..x1
<pre>
|-------------------------|
x0                       x1
</pre>

Here, the range bounds are at the axis extremities.
With cyclic axis, however, the time is split in
"cycles", or "time frames", or the same duration : the period.

A cycle axis cannot by definition handle a larger interval
than the period : <pre>x1 - x0 >= period</pre>. Thus, at most a full
period can be represented with such an axis.

The cycle bound is the number between x0 and x1 which marks
the beginning of new time frame:
<pre>
|---------------------|----------------------------|
x0                   cb                           x1
<---previous cycle---><-------current cycle-------->
</pre>

It is actually a multiple of the period, plus optionally
a start offset: <pre>cb = n * period + offset</pre>

Thus, by definition, two consecutive cycle bounds
period apart, which is precisely why it is called a
period.

The visual representation of a cyclic axis is like that:
<pre>
|----------------------------|---------------------|
cb                         x1|x0                  cb
<-------current cycle--------><---previous cycle--->
</pre>

The cycle bound is at the axis ends, then current
cycle is shown, then the last cycle. When using
dynamic data, the visual effect is the current cycle
erases the last cycle as x grows. Then, the next cycle
bound is reached, and the process starts over, erasing
the previous cycle.

A Cyclic item renderer is provided to do exactly this.

 */
public class CyclicNumberAxis extends NumberAxis {

    /** For serialization. */
    static final long serialVersionUID = -7514160997164582554L;

    /** The default axis line stroke. */
    public static Stroke DEFAULT_ADVANCE_LINE_STROKE = new BasicStroke(1.0f);

    /** The default axis line paint. */
    public static final Paint DEFAULT_ADVANCE_LINE_PAINT = Color.gray;

    /** The offset. */
    protected double offset;

    /** The period.*/
    protected double period;

    /** ??. */
    protected boolean boundMappedToLastCycle;

    /** A flag that controls whether or not the advance line is visible. */
    protected boolean advanceLineVisible;

    /** The advance line stroke. */
    protected transient Stroke advanceLineStroke = DEFAULT_ADVANCE_LINE_STROKE;

    /** The advance line paint. */
    protected transient Paint advanceLinePaint;

    private transient boolean internalMarkerWhenTicksOverlap;
    private transient Tick internalMarkerCycleBoundTick;

    /**
     * Creates a CycleNumberAxis with the given period.
     *
     * @param period  the period.
     */
    public CyclicNumberAxis(double period) {
        this(period, 0.0);
    }

    /**
     * Creates a CycleNumberAxis with the given period and offset.
     *
     * @param period  the period.
     * @param offset  the offset.
     */
    public CyclicNumberAxis(double period, double offset) {
        this(period, offset, null);
    }

    /**
     * Creates a named CycleNumberAxis with the given period.
     *
     * @param period  the period.
     * @param label  the label.
     */
    public CyclicNumberAxis(double period, String label) {
        this(0, period, label);
    }

    /**
     * Creates a named CycleNumberAxis with the given period and offset.
     *
     * @param period  the period.
     * @param offset  the offset.
     * @param label  the label.
     */
    public CyclicNumberAxis(double period, double offset, String label) {
        super(label);
        this.period = period;
        this.offset = offset;
        setFixedAutoRange(period);
        this.advanceLineVisible = true;
        this.advanceLinePaint = DEFAULT_ADVANCE_LINE_PAINT;
    }

    /**
     * The advance line is the line drawn at the limit of the current cycle,
     * when erasing the previous cycle.
     *
     * @return A boolean.
     */
    public boolean isAdvanceLineVisible() {
        return this.advanceLineVisible;
    }

    /**
     * The advance line is the line drawn at the limit of the current cycle,
     * when erasing the previous cycle.
     *
     * @param visible  the flag.
     */
    public void setAdvanceLineVisible(boolean visible) {
        this.advanceLineVisible = visible;
    }

    /**
     * The advance line is the line drawn at the limit of the current cycle,
     * when erasing the previous cycle.
     *
     * @return The paint (never <code>null</code>).
     */
    public Paint getAdvanceLinePaint() {
        return this.advanceLinePaint;
    }

    /**
     * The advance line is the line drawn at the limit of the current cycle,
     * when erasing the previous cycle.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     */
    public void setAdvanceLinePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.advanceLinePaint = paint;
    }

    /**
     * The advance line is the line drawn at the limit of the current cycle,
     * when erasing the previous cycle.
     *
     * @return The stroke (never <code>null</code>).
     */
    public Stroke getAdvanceLineStroke() {
        return this.advanceLineStroke;
    }
    /**
     * The advance line is the line drawn at the limit of the current cycle,
     * when erasing the previous cycle.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     */
    public void setAdvanceLineStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.advanceLineStroke = stroke;
    }

    /**
     * The cycle bound can be associated either with the current or with the
     * last cycle.  It's up to the user's choice to decide which, as this is
     * just a convention.  By default, the cycle bound is mapped to the current
     * cycle.
     * <br>
     * Note that this has no effect on visual appearance, as the cycle bound is
     * mapped successively for both axis ends. Use this function for correct
     * results in translateValueToJava2D.
     *
     * @return <code>true</code> if the cycle bound is mapped to the last
     *         cycle, <code>false</code> if it is bound to the current cycle
     *         (default)
     */
    public boolean isBoundMappedToLastCycle() {
        return this.boundMappedToLastCycle;
    }

    /**
     * The cycle bound can be associated either with the current or with the
     * last cycle.  It's up to the user's choice to decide which, as this is
     * just a convention. By default, the cycle bound is mapped to the current
     * cycle.
     * <br>
     * Note that this has no effect on visual appearance, as the cycle bound is
     * mapped successively for both axis ends. Use this function for correct
     * results in valueToJava2D.
     *
     * @param boundMappedToLastCycle Set it to true to map the cycle bound to
     *        the last cycle.
     */
    public void setBoundMappedToLastCycle(boolean boundMappedToLastCycle) {
        this.boundMappedToLastCycle = boundMappedToLastCycle;
    }

    /**
     * Selects a tick unit when the axis is displayed horizontally.
     *
     * @param g2  the graphics device.
     * @param drawArea  the drawing area.
     * @param dataArea  the data area.
     * @param edge  the side of the rectangle on which the axis is displayed.
     */
    protected void selectHorizontalAutoTickUnit(Graphics2D g2,
                                                Rectangle2D drawArea,
                                                Rectangle2D dataArea,
                                                RectangleEdge edge) {

        double tickLabelWidth
            = estimateMaximumTickLabelWidth(g2, getTickUnit());

        // Compute number of labels
        double n = getRange().getLength()
                   * tickLabelWidth / dataArea.getWidth();

        setTickUnit(
            (NumberTickUnit) getStandardTickUnits().getCeilingTickUnit(n),
            false, false
        );

     }

    /**
     * Selects a tick unit when the axis is displayed vertically.
     *
     * @param g2  the graphics device.
     * @param drawArea  the drawing area.
     * @param dataArea  the data area.
     * @param edge  the side of the rectangle on which the axis is displayed.
     */
    protected void selectVerticalAutoTickUnit(Graphics2D g2,
                                                Rectangle2D drawArea,
                                                Rectangle2D dataArea,
                                                RectangleEdge edge) {

        double tickLabelWidth
            = estimateMaximumTickLabelWidth(g2, getTickUnit());

        // Compute number of labels
        double n = getRange().getLength()
                   * tickLabelWidth / dataArea.getHeight();

        setTickUnit(
            (NumberTickUnit) getStandardTickUnits().getCeilingTickUnit(n),
            false, false
        );

     }

    /**
     * A special Number tick that also hold information about the cycle bound
     * mapping for this tick.  This is especially useful for having a tick at
     * each axis end with the cycle bound value.  See also
     * isBoundMappedToLastCycle()
     */
    protected static class CycleBoundTick extends NumberTick {

        /** Map to last cycle. */
        public boolean mapToLastCycle;

        /**
         * Creates a new tick.
         *
         * @param mapToLastCycle  map to last cycle?
         * @param number  the number.
         * @param label  the label.
         * @param textAnchor  the text anchor.
         * @param rotationAnchor  the rotation anchor.
         * @param angle  the rotation angle.
         */
        public CycleBoundTick(boolean mapToLastCycle, Number number,
                              String label, TextAnchor textAnchor,
                              TextAnchor rotationAnchor, double angle) {
            super(number, label, textAnchor, rotationAnchor, angle);
            this.mapToLastCycle = mapToLastCycle;
        }
    }

    /**
     * Calculates the anchor point for a tick.
     *
     * @param tick  the tick.
     * @param cursor  the cursor.
     * @param dataArea  the data area.
     * @param edge  the side on which the axis is displayed.
     *
     * @return The anchor point.
     */
    protected float[] calculateAnchorPoint(ValueTick tick, double cursor,
                                           Rectangle2D dataArea,
                                           RectangleEdge edge) {
        if (tick instanceof CycleBoundTick) {
            boolean mapsav = this.boundMappedToLastCycle;
            this.boundMappedToLastCycle
                = ((CycleBoundTick) tick).mapToLastCycle;
            float[] ret = super.calculateAnchorPoint(
                tick, cursor, dataArea, edge
            );
            this.boundMappedToLastCycle = mapsav;
            return ret;
        }
        return super.calculateAnchorPoint(tick, cursor, dataArea, edge);
    }



    /**
     * Builds a list of ticks for the axis.  This method is called when the
     * axis is at the top or bottom of the chart (so the axis is "horizontal").
     *
     * @param g2  the graphics device.
     * @param dataArea  the data area.
     * @param edge  the edge.
     *
     * @return A list of ticks.
     */
    protected List refreshTicksHorizontal(Graphics2D g2,
                                          Rectangle2D dataArea,
                                          RectangleEdge edge) {

        List result = new java.util.ArrayList();

        Font tickLabelFont = getTickLabelFont();
        g2.setFont(tickLabelFont);

        if (isAutoTickUnitSelection()) {
            selectAutoTickUnit(g2, dataArea, edge);
        }

        double unit = getTickUnit().getSize();
        double cycleBound = getCycleBound();
        double currentTickValue = Math.ceil(cycleBound / unit) * unit;
        double upperValue = getRange().getUpperBound();
        boolean cycled = false;

        boolean boundMapping = this.boundMappedToLastCycle;
        this.boundMappedToLastCycle = false;

        CycleBoundTick lastTick = null;
        float lastX = 0.0f;

        if (upperValue == cycleBound) {
            currentTickValue = calculateLowestVisibleTickValue();
            cycled = true;
            this.boundMappedToLastCycle = true;
        }

        while (currentTickValue <= upperValue) {

            // Cycle when necessary
            boolean cyclenow = false;
            if ((currentTickValue + unit > upperValue) && !cycled) {
                cyclenow = true;
            }

            double xx = valueToJava2D(currentTickValue, dataArea, edge);
            String tickLabel;
            NumberFormat formatter = getNumberFormatOverride();
            if (formatter != null) {
                tickLabel = formatter.format(currentTickValue);
            }
            else {
                tickLabel = getTickUnit().valueToString(currentTickValue);
            }
            float x = (float) xx;
            TextAnchor anchor = null;
            TextAnchor rotationAnchor = null;
            double angle = 0.0;
            if (isVerticalTickLabels()) {
                if (edge == RectangleEdge.TOP) {
                    angle = Math.PI / 2.0;
                }
                else {
                    angle = -Math.PI / 2.0;
                }
                anchor = TextAnchor.CENTER_RIGHT;
                // If tick overlap when cycling, update last tick too
                if ((lastTick != null) && (lastX == x)
                        && (currentTickValue != cycleBound)) {
                    anchor = isInverted()
                        ? TextAnchor.TOP_RIGHT : TextAnchor.BOTTOM_RIGHT;
                    result.remove(result.size() - 1);
                    result.add(new CycleBoundTick(
                        this.boundMappedToLastCycle, lastTick.getNumber(),
                        lastTick.getText(), anchor, anchor,
                        lastTick.getAngle())
                    );
                    this.internalMarkerWhenTicksOverlap = true;
                    anchor = isInverted()
                        ? TextAnchor.BOTTOM_RIGHT : TextAnchor.TOP_RIGHT;
                }
                rotationAnchor = anchor;
            }
            else {
                if (edge == RectangleEdge.TOP) {
                    anchor = TextAnchor.BOTTOM_CENTER;
                    if ((lastTick != null) && (lastX == x)
                            && (currentTickValue != cycleBound)) {
                        anchor = isInverted()
                            ? TextAnchor.BOTTOM_LEFT : TextAnchor.BOTTOM_RIGHT;
                        result.remove(result.size() - 1);
                        result.add(new CycleBoundTick(
                            this.boundMappedToLastCycle, lastTick.getNumber(),
                            lastTick.getText(), anchor, anchor,
                            lastTick.getAngle())
                        );
                        this.internalMarkerWhenTicksOverlap = true;
                        anchor = isInverted()
                            ? TextAnchor.BOTTOM_RIGHT : TextAnchor.BOTTOM_LEFT;
                    }
                    rotationAnchor = anchor;
                }
                else {
                    anchor = TextAnchor.TOP_CENTER;
                    if ((lastTick != null) && (lastX == x)
                            && (currentTickValue != cycleBound)) {
                        anchor = isInverted()
                            ? TextAnchor.TOP_LEFT : TextAnchor.TOP_RIGHT;
                        result.remove(result.size() - 1);
                        result.add(new CycleBoundTick(
                            this.boundMappedToLastCycle, lastTick.getNumber(),
                            lastTick.getText(), anchor, anchor,
                            lastTick.getAngle())
                        );
                        this.internalMarkerWhenTicksOverlap = true;
                        anchor = isInverted()
                            ? TextAnchor.TOP_RIGHT : TextAnchor.TOP_LEFT;
                    }
                    rotationAnchor = anchor;
                }
            }

            CycleBoundTick tick = new CycleBoundTick(
                this.boundMappedToLastCycle,
                new Double(currentTickValue), tickLabel, anchor,
                rotationAnchor, angle
            );
            if (currentTickValue == cycleBound) {
                this.internalMarkerCycleBoundTick = tick;
            }
            result.add(tick);
            lastTick = tick;
            lastX = x;

            currentTickValue += unit;

            if (cyclenow) {
                currentTickValue = calculateLowestVisibleTickValue();
                upperValue = cycleBound;
                cycled = true;
                this.boundMappedToLastCycle = true;
            }

        }
        this.boundMappedToLastCycle = boundMapping;
        return result;

    }

    /**
     * Builds a list of ticks for the axis.  This method is called when the
     * axis is at the left or right of the chart (so the axis is "vertical").
     *
     * @param g2  the graphics device.
     * @param dataArea  the data area.
     * @param edge  the edge.
     *
     * @return A list of ticks.
     */
    protected List refreshVerticalTicks(Graphics2D g2,
                                        Rectangle2D dataArea,
                                        RectangleEdge edge) {

        List result = new java.util.ArrayList();
        result.clear();

        Font tickLabelFont = getTickLabelFont();
        g2.setFont(tickLabelFont);
        if (isAutoTickUnitSelection()) {
            selectAutoTickUnit(g2, dataArea, edge);
        }

        double unit = getTickUnit().getSize();
        double cycleBound = getCycleBound();
        double currentTickValue = Math.ceil(cycleBound / unit) * unit;
        double upperValue = getRange().getUpperBound();
        boolean cycled = false;

        boolean boundMapping = this.boundMappedToLastCycle;
        this.boundMappedToLastCycle = true;

        NumberTick lastTick = null;
        float lastY = 0.0f;

        if (upperValue == cycleBound) {
            currentTickValue = calculateLowestVisibleTickValue();
            cycled = true;
            this.boundMappedToLastCycle = true;
        }

        while (currentTickValue <= upperValue) {

            // Cycle when necessary
            boolean cyclenow = false;
            if ((currentTickValue + unit > upperValue) && !cycled) {
                cyclenow = true;
            }

            double yy = valueToJava2D(currentTickValue, dataArea, edge);
            String tickLabel;
            NumberFormat formatter = getNumberFormatOverride();
            if (formatter != null) {
                tickLabel = formatter.format(currentTickValue);
            }
            else {
                tickLabel = getTickUnit().valueToString(currentTickValue);
            }

            float y = (float) yy;
            TextAnchor anchor = null;
            TextAnchor rotationAnchor = null;
            double angle = 0.0;
            if (isVerticalTickLabels()) {

                if (edge == RectangleEdge.LEFT) {
                    anchor = TextAnchor.BOTTOM_CENTER;
                    if ((lastTick != null) && (lastY == y)
                            && (currentTickValue != cycleBound)) {
                        anchor = isInverted()
                            ? TextAnchor.BOTTOM_LEFT : TextAnchor.BOTTOM_RIGHT;
                        result.remove(result.size() - 1);
                        result.add(new CycleBoundTick(
                            this.boundMappedToLastCycle, lastTick.getNumber(),
                            lastTick.getText(), anchor, anchor,
                            lastTick.getAngle())
                        );
                        this.internalMarkerWhenTicksOverlap = true;
                        anchor = isInverted()
                            ? TextAnchor.BOTTOM_RIGHT : TextAnchor.BOTTOM_LEFT;
                    }
                    rotationAnchor = anchor;
                    angle = -Math.PI / 2.0;
                }
                else {
                    anchor = TextAnchor.BOTTOM_CENTER;
                    if ((lastTick != null) && (lastY == y)
                            && (currentTickValue != cycleBound)) {
                        anchor = isInverted()
                            ? TextAnchor.BOTTOM_RIGHT : TextAnchor.BOTTOM_LEFT;
                        result.remove(result.size() - 1);
                        result.add(new CycleBoundTick(
                            this.boundMappedToLastCycle, lastTick.getNumber(),
                            lastTick.getText(), anchor, anchor,
                            lastTick.getAngle())
                        );
                        this.internalMarkerWhenTicksOverlap = true;
                        anchor = isInverted()
                            ? TextAnchor.BOTTOM_LEFT : TextAnchor.BOTTOM_RIGHT;
                    }
                    rotationAnchor = anchor;
                    angle = Math.PI / 2.0;
                }
            }
            else {
                if (edge == RectangleEdge.LEFT) {
                    anchor = TextAnchor.CENTER_RIGHT;
                    if ((lastTick != null) && (lastY == y)
                            && (currentTickValue != cycleBound)) {
                        anchor = isInverted()
                            ? TextAnchor.BOTTOM_RIGHT : TextAnchor.TOP_RIGHT;
                        result.remove(result.size() - 1);
                        result.add(new CycleBoundTick(
                            this.boundMappedToLastCycle, lastTick.getNumber(),
                            lastTick.getText(), anchor, anchor,
                            lastTick.getAngle())
                        );
                        this.internalMarkerWhenTicksOverlap = true;
                        anchor = isInverted()
                            ? TextAnchor.TOP_RIGHT : TextAnchor.BOTTOM_RIGHT;
                    }
                    rotationAnchor = anchor;
                }
                else {
                    anchor = TextAnchor.CENTER_LEFT;
                    if ((lastTick != null) && (lastY == y)
                            && (currentTickValue != cycleBound)) {
                        anchor = isInverted()
                            ? TextAnchor.BOTTOM_LEFT : TextAnchor.TOP_LEFT;
                        result.remove(result.size() - 1);
                        result.add(new CycleBoundTick(
                            this.boundMappedToLastCycle, lastTick.getNumber(),
                            lastTick.getText(), anchor, anchor,
                            lastTick.getAngle())
                        );
                        this.internalMarkerWhenTicksOverlap = true;
                        anchor = isInverted()
                            ? TextAnchor.TOP_LEFT : TextAnchor.BOTTOM_LEFT;
                    }
                    rotationAnchor = anchor;
                }
            }

            CycleBoundTick tick = new CycleBoundTick(
                this.boundMappedToLastCycle, new Double(currentTickValue),
                tickLabel, anchor, rotationAnchor, angle
            );
            if (currentTickValue == cycleBound) {
                this.internalMarkerCycleBoundTick = tick;
            }
            result.add(tick);
            lastTick = tick;
            lastY = y;

            if (currentTickValue == cycleBound) {
                this.internalMarkerCycleBoundTick = tick;
            }

            currentTickValue += unit;

            if (cyclenow) {
                currentTickValue = calculateLowestVisibleTickValue();
                upperValue = cycleBound;
                cycled = true;
                this.boundMappedToLastCycle = false;
            }

        }
        this.boundMappedToLastCycle = boundMapping;
        return result;
    }

    /**
     * Converts a coordinate from Java 2D space to data space.
     *
     * @param java2DValue  the coordinate in Java2D space.
     * @param dataArea  the data area.
     * @param edge  the edge.
     *
     * @return The data value.
     */
    public double java2DToValue(double java2DValue, Rectangle2D dataArea,
                                RectangleEdge edge) {
        Range range = getRange();

        double vmax = range.getUpperBound();
        double vp = getCycleBound();

        double jmin = 0.0;
        double jmax = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            jmin = dataArea.getMinX();
            jmax = dataArea.getMaxX();
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            jmin = dataArea.getMaxY();
            jmax = dataArea.getMinY();
        }

        if (isInverted()) {
            double jbreak = jmax - (vmax - vp) * (jmax - jmin) / this.period;
            if (java2DValue >= jbreak) {
                return vp + (jmax - java2DValue) * this.period / (jmax - jmin);
            }
            else {
                return vp - (java2DValue - jmin) * this.period / (jmax - jmin);
            }
        }
        else {
            double jbreak = (vmax - vp) * (jmax - jmin) / this.period + jmin;
            if (java2DValue <= jbreak) {
                return vp + (java2DValue - jmin) * this.period / (jmax - jmin);
            }
            else {
                return vp - (jmax - java2DValue) * this.period / (jmax - jmin);
            }
        }
    }

    /**
     * Translates a value from data space to Java 2D space.
     *
     * @param value  the data value.
     * @param dataArea  the data area.
     * @param edge  the edge.
     *
     * @return The Java 2D value.
     */
    public double valueToJava2D(double value, Rectangle2D dataArea,
                                RectangleEdge edge) {
        Range range = getRange();

        double vmin = range.getLowerBound();
        double vmax = range.getUpperBound();
        double vp = getCycleBound();

        if ((value < vmin) || (value > vmax)) {
            return Double.NaN;
        }


        double jmin = 0.0;
        double jmax = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            jmin = dataArea.getMinX();
            jmax = dataArea.getMaxX();
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            jmax = dataArea.getMinY();
            jmin = dataArea.getMaxY();
        }

        if (isInverted()) {
            if (value == vp) {
                return this.boundMappedToLastCycle ? jmin : jmax;
            }
            else if (value > vp) {
                return jmax - (value - vp) * (jmax - jmin) / this.period;
            }
            else {
                return jmin + (vp - value) * (jmax - jmin) / this.period;
            }
        }
        else {
            if (value == vp) {
                return this.boundMappedToLastCycle ? jmax : jmin;
            }
            else if (value >= vp) {
                return jmin + (value - vp) * (jmax - jmin) / this.period;
            }
            else {
                return jmax - (vp - value) * (jmax - jmin) / this.period;
            }
        }
    }

    /**
     * Centers the range about the given value.
     *
     * @param value  the data value.
     */
    public void centerRange(double value) {
        setRange(value - this.period / 2.0, value + this.period / 2.0);
    }

    /**
     * This function is nearly useless since the auto range is fixed for this
     * class to the period.  The period is extended if necessary to fit the
     * minimum size.
     *
     * @param size  the size.
     * @param notify  notify?
     *
     * @see org.jfree.chart.axis.ValueAxis#setAutoRangeMinimumSize(double,
     *      boolean)
     */
    public void setAutoRangeMinimumSize(double size, boolean notify) {
        if (size > this.period) {
            this.period = size;
        }
        super.setAutoRangeMinimumSize(size, notify);
    }

    /**
     * The auto range is fixed for this class to the period by default.
     * This function will thus set a new period.
     *
     * @param length  the length.
     *
     * @see org.jfree.chart.axis.ValueAxis#setFixedAutoRange(double)
     */
    public void setFixedAutoRange(double length) {
        this.period = length;
        super.setFixedAutoRange(length);
    }

    /**
     * Sets a new axis range. The period is extended to fit the range size, if
     * necessary.
     *
     * @param range  the range.
     * @param turnOffAutoRange  switch off the auto range.
     * @param notify notify?
     *
     * @see org.jfree.chart.axis.ValueAxis#setRange(Range, boolean, boolean)
     */
    public void setRange(Range range, boolean turnOffAutoRange,
                         boolean notify) {
        double size = range.getUpperBound() - range.getLowerBound();
        if (size > this.period) {
            this.period = size;
        }
        super.setRange(range, turnOffAutoRange, notify);
    }

    /**
     * The cycle bound is defined as the higest value x such that
     * "offset + period * i = x", with i and integer and x &lt;
     * range.getUpperBound() This is the value which is at both ends of the
     * axis :  x...up|low...x
     * The values from x to up are the valued in the current cycle.
     * The values from low to x are the valued in the previous cycle.
     *
     * @return The cycle bound.
     */
    public double getCycleBound() {
        return Math.floor(
            (getRange().getUpperBound() - this.offset) / this.period
        ) * this.period + this.offset;
    }

    /**
     * The cycle bound is a multiple of the period, plus optionally a start
     * offset.
     * <P>
     * <pre>cb = n * period + offset</pre><br>
     *
     * @return The current offset.
     *
     * @see #getCycleBound()
     */
    public double getOffset() {
        return this.offset;
    }

    /**
     * The cycle bound is a multiple of the period, plus optionally a start
     * offset.
     * <P>
     * <pre>cb = n * period + offset</pre><br>
     *
     * @param offset The offset to set.
     *
     * @see #getCycleBound()
     */
    public void setOffset(double offset) {
        this.offset = offset;
    }

    /**
     * The cycle bound is a multiple of the period, plus optionally a start
     * offset.
     * <P>
     * <pre>cb = n * period + offset</pre><br>
     *
     * @return The current period.
     *
     * @see #getCycleBound()
     */
    public double getPeriod() {
        return this.period;
    }

    /**
     * The cycle bound is a multiple of the period, plus optionally a start
     * offset.
     * <P>
     * <pre>cb = n * period + offset</pre><br>
     *
     * @param period The period to set.
     *
     * @see #getCycleBound()
     */
    public void setPeriod(double period) {
        this.period = period;
    }

    /**
     * Draws the tick marks and labels.
     *
     * @param g2  the graphics device.
     * @param cursor  the cursor.
     * @param plotArea  the plot area.
     * @param dataArea  the area inside the axes.
     * @param edge  the side on which the axis is displayed.
     *
     * @return The axis state.
     */
    protected AxisState drawTickMarksAndLabels(Graphics2D g2, double cursor,
            Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge) {
        this.internalMarkerWhenTicksOverlap = false;
        AxisState ret = super.drawTickMarksAndLabels(g2, cursor, plotArea,
                dataArea, edge);

        // continue and separate the labels only if necessary
        if (!this.internalMarkerWhenTicksOverlap) {
            return ret;
        }

        double ol = getTickMarkOutsideLength();
        FontMetrics fm = g2.getFontMetrics(getTickLabelFont());

        if (isVerticalTickLabels()) {
            ol = fm.getMaxAdvance();
        }
        else {
            ol = fm.getHeight();
        }

        double il = 0;
        if (isTickMarksVisible()) {
            float xx = (float) valueToJava2D(getRange().getUpperBound(),
                    dataArea, edge);
            Line2D mark = null;
            g2.setStroke(getTickMarkStroke());
            g2.setPaint(getTickMarkPaint());
            if (edge == RectangleEdge.LEFT) {
                mark = new Line2D.Double(cursor - ol, xx, cursor + il, xx);
            }
            else if (edge == RectangleEdge.RIGHT) {
                mark = new Line2D.Double(cursor + ol, xx, cursor - il, xx);
            }
            else if (edge == RectangleEdge.TOP) {
                mark = new Line2D.Double(xx, cursor - ol, xx, cursor + il);
            }
            else if (edge == RectangleEdge.BOTTOM) {
                mark = new Line2D.Double(xx, cursor + ol, xx, cursor - il);
            }
            g2.draw(mark);
        }
        return ret;
    }

    /**
     * Draws the axis.
     *
     * @param g2  the graphics device (<code>null</code> not permitted).
     * @param cursor  the cursor position.
     * @param plotArea  the plot area (<code>null</code> not permitted).
     * @param dataArea  the data area (<code>null</code> not permitted).
     * @param edge  the edge (<code>null</code> not permitted).
     * @param plotState  collects information about the plot
     *                   (<code>null</code> permitted).
     *
     * @return The axis state (never <code>null</code>).
     */
    public AxisState draw(Graphics2D g2,
                          double cursor,
                          Rectangle2D plotArea,
                          Rectangle2D dataArea,
                          RectangleEdge edge,
                          PlotRenderingInfo plotState) {

        AxisState ret = super.draw(
            g2, cursor, plotArea, dataArea, edge, plotState
        );
        if (isAdvanceLineVisible()) {
            double xx = valueToJava2D(
                getRange().getUpperBound(), dataArea, edge
            );
            Line2D mark = null;
            g2.setStroke(getAdvanceLineStroke());
            g2.setPaint(getAdvanceLinePaint());
            if (edge == RectangleEdge.LEFT) {
                mark = new Line2D.Double(
                    cursor, xx, cursor + dataArea.getWidth(), xx
                );
            }
            else if (edge == RectangleEdge.RIGHT) {
                mark = new Line2D.Double(
                    cursor - dataArea.getWidth(), xx, cursor, xx
                );
            }
            else if (edge == RectangleEdge.TOP) {
                mark = new Line2D.Double(
                    xx, cursor + dataArea.getHeight(), xx, cursor
                );
            }
            else if (edge == RectangleEdge.BOTTOM) {
                mark = new Line2D.Double(
                    xx, cursor, xx, cursor - dataArea.getHeight()
                );
            }
            g2.draw(mark);
        }
        return ret;
    }

    /**
     * Reserve some space on each axis side because we draw a centered label at
     * each extremity.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param plotArea  the plot area.
     * @param edge  the edge.
     * @param space  the space already reserved.
     *
     * @return The reserved space.
     */
    public AxisSpace reserveSpace(Graphics2D g2,
                                  Plot plot,
                                  Rectangle2D plotArea,
                                  RectangleEdge edge,
                                  AxisSpace space) {

        this.internalMarkerCycleBoundTick = null;
        AxisSpace ret = super.reserveSpace(g2, plot, plotArea, edge, space);
        if (this.internalMarkerCycleBoundTick == null) {
            return ret;
        }

        FontMetrics fm = g2.getFontMetrics(getTickLabelFont());
        Rectangle2D r = TextUtilities.getTextBounds(
            this.internalMarkerCycleBoundTick.getText(), g2, fm
        );

        if (RectangleEdge.isTopOrBottom(edge)) {
            if (isVerticalTickLabels()) {
                space.add(r.getHeight() / 2, RectangleEdge.RIGHT);
            }
            else {
                space.add(r.getWidth() / 2, RectangleEdge.RIGHT);
            }
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            if (isVerticalTickLabels()) {
                space.add(r.getWidth() / 2, RectangleEdge.TOP);
            }
            else {
                space.add(r.getHeight() / 2, RectangleEdge.TOP);
            }
        }

        return ret;

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
        SerialUtilities.writePaint(this.advanceLinePaint, stream);
        SerialUtilities.writeStroke(this.advanceLineStroke, stream);

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
        this.advanceLinePaint = SerialUtilities.readPaint(stream);
        this.advanceLineStroke = SerialUtilities.readStroke(stream);

    }


    /**
     * Tests the axis for equality with another object.
     *
     * @param obj  the object to test against.
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CyclicNumberAxis)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        CyclicNumberAxis that = (CyclicNumberAxis) obj;
        if (this.period != that.period) {
            return false;
        }
        if (this.offset != that.offset) {
            return false;
        }
        if (!PaintUtilities.equal(this.advanceLinePaint,
                that.advanceLinePaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.advanceLineStroke,
                that.advanceLineStroke)) {
            return false;
        }
        if (this.advanceLineVisible != that.advanceLineVisible) {
            return false;
        }
        if (this.boundMappedToLastCycle != that.boundMappedToLastCycle) {
            return false;
        }
        return true;
    }
}
