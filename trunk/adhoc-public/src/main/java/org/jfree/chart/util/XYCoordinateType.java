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
 * XYCoordinateType.java
 * ---------------------
 * (C) Copyright 2007, 2008 by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 02-Feb-2007 : Version 1 (DG);
 * 03-Sep-2008 : Moved from experimental to main (DG);
 *
 */

package org.jfree.chart.util;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Represents several possible interpretations for an (x, y) coordinate.
 *
 * @since 1.0.11
 */
public final class XYCoordinateType implements Serializable {

    /** The (x, y) coordinates represent a point in the data space. */
    public static final XYCoordinateType DATA
            = new XYCoordinateType("XYCoordinateType.DATA");

    /**
     * The (x, y) coordinates represent a relative position in the data space.
     * In this case, the values should be in the range (0.0 to 1.0).
     */
    public static final XYCoordinateType RELATIVE
            = new XYCoordinateType("XYCoordinateType.RELATIVE");

    /**
     * The (x, y) coordinates represent indices in a dataset.
     * In this case, the values should be in the range (0.0 to 1.0).
     */
    public static final XYCoordinateType INDEX
            = new XYCoordinateType("XYCoordinateType.INDEX");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private XYCoordinateType(String name) {
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
        if (!(obj instanceof XYCoordinateType)) {
            return false;
        }
        XYCoordinateType order = (XYCoordinateType) obj;
        if (!this.name.equals(order.toString())) {
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
        if (this.equals(XYCoordinateType.DATA)) {
            return XYCoordinateType.DATA;
        }
        else if (this.equals(XYCoordinateType.RELATIVE)) {
            return XYCoordinateType.RELATIVE;
        }
        else if (this.equals(XYCoordinateType.INDEX)) {
            return XYCoordinateType.INDEX;
        }
        return null;
    }

}
