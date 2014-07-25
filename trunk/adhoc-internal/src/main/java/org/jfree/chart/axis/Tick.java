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
 * ---------
 * Tick.java
 * ---------
 * (C) Copyright 2000-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Nicolas Brodu;
 *
 * Changes
 * -------
 * 18-Sep-2001 : Added standard header and fixed DOS encoding problem (DG);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 08-Nov-2002 : Moved to new package com.jrefinery.chart.axis (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 12-Sep-2003 : Implemented Cloneable (NB);
 * 07-Nov-2003 : Added subclasses for particular types of ticks (DG);
 *
 */

package org.jfree.chart.axis;

import java.io.Serializable;

import org.jfree.ui.TextAnchor;
import org.jfree.util.ObjectUtilities;

/**
 * The base class used to represent labelled ticks along an axis.
 */
public abstract class Tick implements Serializable, Cloneable {

    /** For serialization. */
    private static final long serialVersionUID = 6668230383875149773L;

    /** A text version of the tick value. */
    private String text;

    /** The text anchor for the tick label. */
    private TextAnchor textAnchor;

    /** The rotation anchor for the tick label. */
    private TextAnchor rotationAnchor;

    /** The rotation angle. */
    private double angle;

    /**
     * Creates a new tick.
     *
     * @param text  the formatted version of the tick value.
     * @param textAnchor  the text anchor (<code>null</code> not permitted).
     * @param rotationAnchor  the rotation anchor (<code>null</code> not
     *                        permitted).
     * @param angle  the angle.
     */
    public Tick(String text, TextAnchor textAnchor, TextAnchor rotationAnchor,
                double angle) {
        if (textAnchor == null) {
            throw new IllegalArgumentException("Null 'textAnchor' argument.");
        }
        if (rotationAnchor == null) {
            throw new IllegalArgumentException(
                "Null 'rotationAnchor' argument."
            );
        }
        this.text = text;
        this.textAnchor = textAnchor;
        this.rotationAnchor = rotationAnchor;
        this.angle = angle;
    }

    /**
     * Returns the text version of the tick value.
     *
     * @return A string (possibly <code>null</code>;
     */
    public String getText() {
        return this.text;
    }

    /**
     * Returns the text anchor.
     *
     * @return The text anchor (never <code>null</code>).
     */
    public TextAnchor getTextAnchor() {
        return this.textAnchor;
    }

    /**
     * Returns the text anchor that defines the point around which the label is
     * rotated.
     *
     * @return A text anchor (never <code>null</code>).
     */
    public TextAnchor getRotationAnchor() {
        return this.rotationAnchor;
    }

    /**
     * Returns the angle.
     *
     * @return The angle.
     */
    public double getAngle() {
        return this.angle;
    }

    /**
     * Tests this tick for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Tick) {
            Tick t = (Tick) obj;
            if (!ObjectUtilities.equal(this.text, t.text)) {
                return false;
            }
            if (!ObjectUtilities.equal(this.textAnchor, t.textAnchor)) {
                return false;
            }
            if (!ObjectUtilities.equal(this.rotationAnchor, t.rotationAnchor)) {
                return false;
            }
            if (!(this.angle == t.angle)) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns a clone of the tick.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if there is a problem cloning.
     */
    public Object clone() throws CloneNotSupportedException {
        Tick clone = (Tick) super.clone();
        return clone;
    }

    /**
     * Returns a string representation of the tick.
     *
     * @return A string.
     */
    public String toString() {
        return this.text;
    }
}
