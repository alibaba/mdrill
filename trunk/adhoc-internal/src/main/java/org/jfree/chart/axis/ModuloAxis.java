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
 * ---------------
 * ModuloAxis.java
 * ---------------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 13-Aug-2004 : Version 1 (DG);
 * 13-Nov-2007 : Implemented equals() (DG);
 *
 */

package org.jfree.chart.axis;

import java.awt.geom.Rectangle2D;

import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;

/**
 * An axis that displays numerical values within a fixed range using a modulo
 * calculation.
 */
public class ModuloAxis extends NumberAxis {

    /**
     * The fixed range for the axis - all data values will be mapped to this
     * range using a modulo calculation.
     */
    private Range fixedRange;

    /**
     * The display start value (this will sometimes be > displayEnd, in which
     * case the axis wraps around at some point in the middle of the axis).
     */
    private double displayStart;

    /**
     * The display end value.
     */
    private double displayEnd;

    /**
     * Creates a new axis.
     *
     * @param label  the axis label (<code>null</code> permitted).
     * @param fixedRange  the fixed range (<code>null</code> not permitted).
     */
    public ModuloAxis(String label, Range fixedRange) {
        super(label);
        this.fixedRange = fixedRange;
        this.displayStart = 270.0;
        this.displayEnd = 90.0;
    }

    /**
     * Returns the display start value.
     *
     * @return The display start value.
     */
    public double getDisplayStart() {
        return this.displayStart;
    }

    /**
     * Returns the display end value.
     *
     * @return The display end value.
     */
    public double getDisplayEnd() {
        return this.displayEnd;
    }

    /**
     * Sets the display range.  The values will be mapped to the fixed range if
     * necessary.
     *
     * @param start  the start value.
     * @param end  the end value.
     */
    public void setDisplayRange(double start, double end) {
        this.displayStart = mapValueToFixedRange(start);
        this.displayEnd = mapValueToFixedRange(end);
        if (this.displayStart < this.displayEnd) {
            setRange(this.displayStart, this.displayEnd);
        }
        else {
            setRange(this.displayStart, this.fixedRange.getUpperBound()
                  + (this.displayEnd - this.fixedRange.getLowerBound()));
        }
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * This method should calculate a range that will show all the data values.
     * For now, it just sets the axis range to the fixedRange.
     */
    protected void autoAdjustRange() {
        setRange(this.fixedRange, false, false);
    }

    /**
     * Translates a data value to a Java2D coordinate.
     *
     * @param value  the value.
     * @param area  the area.
     * @param edge  the edge.
     *
     * @return A Java2D coordinate.
     */
    public double valueToJava2D(double value, Rectangle2D area,
                                RectangleEdge edge) {
        double result = 0.0;
        double v = mapValueToFixedRange(value);
        if (this.displayStart < this.displayEnd) {  // regular number axis
            result = trans(v, area, edge);
        }
        else {  // displayStart > displayEnd, need to handle split
            double cutoff = (this.displayStart + this.displayEnd) / 2.0;
            double length1 = this.fixedRange.getUpperBound()
                             - this.displayStart;
            double length2 = this.displayEnd - this.fixedRange.getLowerBound();
            if (v > cutoff) {
                result = transStart(v, area, edge, length1, length2);
            }
            else {
                result = transEnd(v, area, edge, length1, length2);
            }
        }
        return result;
    }

    /**
     * A regular translation from a data value to a Java2D value.
     *
     * @param value  the value.
     * @param area  the data area.
     * @param edge  the edge along which the axis lies.
     *
     * @return The Java2D coordinate.
     */
    private double trans(double value, Rectangle2D area, RectangleEdge edge) {
        double min = 0.0;
        double max = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            min = area.getX();
            max = area.getX() + area.getWidth();
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            min = area.getMaxY();
            max = area.getMaxY() - area.getHeight();
        }
        if (isInverted()) {
            return max - ((value - this.displayStart)
                   / (this.displayEnd - this.displayStart)) * (max - min);
        }
        else {
            return min + ((value - this.displayStart)
                   / (this.displayEnd - this.displayStart)) * (max - min);
        }

    }

    /**
     * Translates a data value to a Java2D value for the first section of the
     * axis.
     *
     * @param value  the value.
     * @param area  the data area.
     * @param edge  the edge along which the axis lies.
     * @param length1  the length of the first section.
     * @param length2  the length of the second section.
     *
     * @return The Java2D coordinate.
     */
    private double transStart(double value, Rectangle2D area,
                              RectangleEdge edge,
                              double length1, double length2) {
        double min = 0.0;
        double max = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            min = area.getX();
            max = area.getX() + area.getWidth() * length1 / (length1 + length2);
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            min = area.getMaxY();
            max = area.getMaxY() - area.getHeight() * length1
                  / (length1 + length2);
        }
        if (isInverted()) {
            return max - ((value - this.displayStart)
                / (this.fixedRange.getUpperBound() - this.displayStart))
                * (max - min);
        }
        else {
            return min + ((value - this.displayStart)
                / (this.fixedRange.getUpperBound() - this.displayStart))
                * (max - min);
        }

    }

    /**
     * Translates a data value to a Java2D value for the second section of the
     * axis.
     *
     * @param value  the value.
     * @param area  the data area.
     * @param edge  the edge along which the axis lies.
     * @param length1  the length of the first section.
     * @param length2  the length of the second section.
     *
     * @return The Java2D coordinate.
     */
    private double transEnd(double value, Rectangle2D area, RectangleEdge edge,
                            double length1, double length2) {
        double min = 0.0;
        double max = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            max = area.getMaxX();
            min = area.getMaxX() - area.getWidth() * length2
                  / (length1 + length2);
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            max = area.getMinY();
            min = area.getMinY() + area.getHeight() * length2
                  / (length1 + length2);
        }
        if (isInverted()) {
            return max - ((value - this.fixedRange.getLowerBound())
                    / (this.displayEnd - this.fixedRange.getLowerBound()))
                    * (max - min);
        }
        else {
            return min + ((value - this.fixedRange.getLowerBound())
                    / (this.displayEnd - this.fixedRange.getLowerBound()))
                    * (max - min);
        }

    }

    /**
     * Maps a data value into the fixed range.
     *
     * @param value  the value.
     *
     * @return The mapped value.
     */
    private double mapValueToFixedRange(double value) {
        double lower = this.fixedRange.getLowerBound();
        double length = this.fixedRange.getLength();
        if (value < lower) {
            return lower + length + ((value - lower) % length);
        }
        else {
            return lower + ((value - lower) % length);
        }
    }

    /**
     * Translates a Java2D coordinate into a data value.
     *
     * @param java2DValue  the Java2D coordinate.
     * @param area  the area.
     * @param edge  the edge.
     *
     * @return The Java2D coordinate.
     */
    public double java2DToValue(double java2DValue, Rectangle2D area,
                                RectangleEdge edge) {
        double result = 0.0;
        if (this.displayStart < this.displayEnd) {  // regular number axis
            result = super.java2DToValue(java2DValue, area, edge);
        }
        else {  // displayStart > displayEnd, need to handle split

        }
        return result;
    }

    /**
     * Returns the display length for the axis.
     *
     * @return The display length.
     */
    private double getDisplayLength() {
        if (this.displayStart < this.displayEnd) {
            return (this.displayEnd - this.displayStart);
        }
        else {
            return (this.fixedRange.getUpperBound() - this.displayStart)
                + (this.displayEnd - this.fixedRange.getLowerBound());
        }
    }

    /**
     * Returns the central value of the current display range.
     *
     * @return The central value.
     */
    private double getDisplayCentralValue() {
        return mapValueToFixedRange(
            this.displayStart + (getDisplayLength() / 2)
        );
    }

    /**
     * Increases or decreases the axis range by the specified percentage about
     * the central value and sends an {@link AxisChangeEvent} to all registered
     * listeners.
     * <P>
     * To double the length of the axis range, use 200% (2.0).
     * To halve the length of the axis range, use 50% (0.5).
     *
     * @param percent  the resize factor.
     */
    public void resizeRange(double percent) {
        resizeRange(percent, getDisplayCentralValue());
    }

    /**
     * Increases or decreases the axis range by the specified percentage about
     * the specified anchor value and sends an {@link AxisChangeEvent} to all
     * registered listeners.
     * <P>
     * To double the length of the axis range, use 200% (2.0).
     * To halve the length of the axis range, use 50% (0.5).
     *
     * @param percent  the resize factor.
     * @param anchorValue  the new central value after the resize.
     */
    public void resizeRange(double percent, double anchorValue) {

        if (percent > 0.0) {
            double halfLength = getDisplayLength() * percent / 2;
            setDisplayRange(anchorValue - halfLength, anchorValue + halfLength);
        }
        else {
            setAutoRange(true);
        }

    }

    /**
     * Converts a length in data coordinates into the corresponding length in
     * Java2D coordinates.
     *
     * @param length  the length.
     * @param area  the plot area.
     * @param edge  the edge along which the axis lies.
     *
     * @return The length in Java2D coordinates.
     */
    public double lengthToJava2D(double length, Rectangle2D area,
                                 RectangleEdge edge) {
        double axisLength = 0.0;
        if (this.displayEnd > this.displayStart) {
            axisLength = this.displayEnd - this.displayStart;
        }
        else {
            axisLength = (this.fixedRange.getUpperBound() - this.displayStart)
                + (this.displayEnd - this.fixedRange.getLowerBound());
        }
        double areaLength = 0.0;
        if (RectangleEdge.isLeftOrRight(edge)) {
            areaLength = area.getHeight();
        }
        else {
            areaLength = area.getWidth();
        }
        return (length / axisLength) * areaLength;
    }

    /**
     * Tests this axis for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ModuloAxis)) {
            return false;
        }
        ModuloAxis that = (ModuloAxis) obj;
        if (this.displayStart != that.displayStart) {
            return false;
        }
        if (this.displayEnd != that.displayEnd) {
            return false;
        }
        if (!this.fixedRange.equals(that.fixedRange)) {
            return false;
        }
        return super.equals(obj);
    }

}
