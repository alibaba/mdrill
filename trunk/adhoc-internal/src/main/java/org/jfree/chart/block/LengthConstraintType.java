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
 * -------------------------
 * LengthConstraintType.java
 * -------------------------
 * (C) Copyright 2005-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 08-Feb-2004 : Version 1 (DG);
 *
 */

package org.jfree.chart.block;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Defines tokens used to indicate a length constraint type.
 */
public final class LengthConstraintType implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -1156658804028142978L;

    /** NONE. */
    public static final LengthConstraintType NONE
        = new LengthConstraintType("LengthConstraintType.NONE");

    /** Range. */
    public static final LengthConstraintType RANGE
        = new LengthConstraintType("RectangleConstraintType.RANGE");

    /** FIXED. */
    public static final LengthConstraintType FIXED
        = new LengthConstraintType("LengthConstraintType.FIXED");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private LengthConstraintType(String name) {
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
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LengthConstraintType)) {
            return false;
        }
        LengthConstraintType that = (LengthConstraintType) obj;
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
        if (this.equals(LengthConstraintType.NONE)) {
            return LengthConstraintType.NONE;
        }
        else if (this.equals(LengthConstraintType.RANGE)) {
            return LengthConstraintType.RANGE;
        }
        else if (this.equals(LengthConstraintType.FIXED)) {
            return LengthConstraintType.FIXED;
        }
        return null;
    }

}
