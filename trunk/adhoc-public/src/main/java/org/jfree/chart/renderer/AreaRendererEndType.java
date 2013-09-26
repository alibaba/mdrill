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
 * ------------------------
 * AreaRendererEndType.java
 * ------------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 29-April-2004 : Version 1 (DG);
 *
 */

package org.jfree.chart.renderer;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * An enumeration of the 'end types' for an area renderer.
 */
public final class AreaRendererEndType implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -1774146392916359839L;

    /**
     * The area tapers from the first or last value down to zero.
     */
    public static final AreaRendererEndType TAPER = new AreaRendererEndType(
            "AreaRendererEndType.TAPER");

    /**
     * The area is truncated at the first or last value.
     */
    public static final AreaRendererEndType TRUNCATE = new AreaRendererEndType(
            "AreaRendererEndType.TRUNCATE");

    /**
     * The area is levelled at the first or last value.
     */
    public static final AreaRendererEndType LEVEL = new AreaRendererEndType(
            "AreaRendererEndType.LEVEL");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private AreaRendererEndType(String name) {
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
        if (!(obj instanceof AreaRendererEndType)) {
            return false;
        }

        AreaRendererEndType t = (AreaRendererEndType) obj;
        if (!this.name.equals(t.toString())) {
            return false;
        }

        return true;

    }

    /**
     * Ensures that serialization returns the unique instances.
     *
     * @return The object.
     *
     * @throws ObjectStreamException if there is a problem.
     */
    private Object readResolve() throws ObjectStreamException {
        Object result = null;
        if (this.equals(AreaRendererEndType.LEVEL)) {
            result = AreaRendererEndType.LEVEL;
        }
        else if (this.equals(AreaRendererEndType.TAPER)) {
            result = AreaRendererEndType.TAPER;
        }
        else if (this.equals(AreaRendererEndType.TRUNCATE)) {
            result = AreaRendererEndType.TRUNCATE;
        }
        return result;
    }

}
