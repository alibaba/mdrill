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
 * RangeType.java
 * --------------
 * (C) Copyright 2005-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 24-Feb-2005 : Version 1 (DG);
 *
 */

package org.jfree.data;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Used to indicate the type of range to display on an axis (full, positive or
 * negative).
 */
public final class RangeType implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -9073319010650549239L;

    /** Full range (positive and negative). */
    public static final RangeType FULL = new RangeType("RangeType.FULL");

    /** Positive range. */
    public static final RangeType POSITIVE
        = new RangeType("RangeType.POSITIVE");

    /** Negative range. */
    public static final RangeType NEGATIVE
        = new RangeType("RangeType.NEGATIVE");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private RangeType(String name) {
        this.name = name;
    }

    /**
     * Returns a string representing the object.
     *
     * @return The string.
     */
    public String toString() {
        return this.name;
    }

    /**
     * Returns <code>true</code> if this object is equal to the specified
     * object, and <code>false</code> otherwise.
     *
     * @param obj  the other object.
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RangeType)) {
            return false;
        }
        RangeType that = (RangeType) obj;
        if (!this.name.equals(that.toString())) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return The hashcode
     */
    public int hashCode() {
        return this.name.hashCode();
    }

    /**
     * Ensures that serialization returns the unique instances.
     *
     * @return The object.
     *
     * @throws ObjectStreamException if there is a problem.
     */
    private Object readResolve() throws ObjectStreamException {
        if (this.equals(RangeType.FULL)) {
            return RangeType.FULL;
        }
        else if (this.equals(RangeType.POSITIVE)) {
            return RangeType.POSITIVE;
        }
        else if (this.equals(RangeType.NEGATIVE)) {
            return RangeType.NEGATIVE;
        }
        return null;
    }

}
