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
 * PieLabelRecord.java
 * -------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 08-Mar-2004 : Version 1 (DG);
 * 14-Jun-2007 : Implemented Serializable, updated API docs (DG);
 * 21-Nov-2007 : Implemented equals() to shut up FindBugs (DG);
 *
 */

package org.jfree.chart.plot;

import java.io.Serializable;

import org.jfree.text.TextBox;

/**
 * A structure that retains information about the label for a section in a pie
 * chart.
 */
public class PieLabelRecord implements Comparable, Serializable {

    /** The section key. */
    private Comparable key;

    /** The angle of the centre of the section (in radians). */
    private double angle;

    /** The base y-coordinate. */
    private double baseY;

    /** The allocated y-coordinate. */
    private double allocatedY;

    /** The label. */
    private TextBox label;

    /** The label height. */
    private double labelHeight;

    /** The gap. */
    private double gap;

    /** The link percent. */
    private double linkPercent;

    /**
     * Creates a new record.
     *
     * @param key  the section key.
     * @param angle  the angle to the middle of the section (in radians).
     * @param baseY  the base y-coordinate.
     * @param label  the section label.
     * @param labelHeight  the label height (in Java2D units).
     * @param gap  the offset to the left.
     * @param linkPercent  the link percent.
     */
    public PieLabelRecord(Comparable key, double angle, double baseY,
                          TextBox label, double labelHeight, double gap,
                          double linkPercent) {
        this.key = key;
        this.angle = angle;
        this.baseY = baseY;
        this.allocatedY = baseY;
        this.label = label;
        this.labelHeight = labelHeight;
        this.gap = gap;
        this.linkPercent = linkPercent;
    }

    /**
     * Returns the base y-coordinate.  This is where the label will appear if
     * there is no overlapping of labels.
     *
     * @return The base y-coordinate.
     */
    public double getBaseY() {
        return this.baseY;
    }

    /**
     * Sets the base y-coordinate.
     *
     * @param base  the base y-coordinate.
     */
    public void setBaseY(double base) {
        this.baseY = base;
    }

    /**
     * Returns the lower bound of the label.
     *
     * @return The lower bound.
     */
    public double getLowerY() {
        return this.allocatedY - this.labelHeight / 2.0;
    }

    /**
     * Returns the upper bound of the label.
     *
     * @return The upper bound.
     */
    public double getUpperY() {
        return this.allocatedY + this.labelHeight / 2.0;
    }

    /**
     * Returns the angle of the middle of the section, in radians.
     *
     * @return The angle, in radians.
     */
    public double getAngle() {
        return this.angle;
    }

    /**
     * Returns the key for the section that the label applies to.
     *
     * @return The key.
     */
    public Comparable getKey() {
        return this.key;
    }

    /**
     * Returns the label.
     *
     * @return The label.
     */
    public TextBox getLabel() {
        return this.label;
    }

    /**
     * Returns the label height (you could derive this from the label itself,
     * but we cache the value so it can be retrieved quickly).
     *
     * @return The label height (in Java2D units).
     */
    public double getLabelHeight() {
        return this.labelHeight;
    }

    /**
     * Returns the allocated y-coordinate.
     *
     * @return The allocated y-coordinate.
     */
    public double getAllocatedY() {
        return this.allocatedY;
    }

    /**
     * Sets the allocated y-coordinate.
     *
     * @param y  the y-coordinate.
     */
    public void setAllocatedY(double y) {
        this.allocatedY = y;
    }

    /**
     * Returns the gap.
     *
     * @return The gap.
     */
    public double getGap() {
        return this.gap;
    }

    /**
     * Returns the link percent.
     *
     * @return The link percent.
     */
    public double getLinkPercent() {
        return this.linkPercent;
    }

    /**
     * Compares this object to an arbitrary object.
     *
     * @param obj  the object to compare against.
     *
     * @return An integer that specifies the relative order of the two objects.
     */
    public int compareTo(Object obj) {
        int result = 0;
        if (obj instanceof PieLabelRecord) {
            PieLabelRecord plr = (PieLabelRecord) obj;
            if (this.baseY < plr.baseY) {
                result = -1;
            }
            else if (this.baseY > plr.baseY) {
                result = 1;
            }
        }
        return result;
    }

    /**
     * Tests this record for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PieLabelRecord)) {
            return false;
        }
        PieLabelRecord that = (PieLabelRecord) obj;
        if (!this.key.equals(that.key)) {
            return false;
        }
        if (this.angle != that.angle) {
            return false;
        }
        if (this.gap != that.gap) {
            return false;
        }
        if (this.allocatedY != that.allocatedY) {
            return false;
        }
        if (this.baseY != that.baseY) {
            return false;
        }
        if (this.labelHeight != that.labelHeight) {
            return false;
        }
        if (this.linkPercent != that.linkPercent) {
            return false;
        }
        if (!this.label.equals(that.label)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a string describing the object.  This is used for debugging only.
     *
     * @return A string.
     */
    public String toString() {
        return this.baseY + ", " + this.key.toString();
    }
}
