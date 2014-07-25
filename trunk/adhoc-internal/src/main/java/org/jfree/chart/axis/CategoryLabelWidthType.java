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
 * ---------------------------
 * CategoryLabelWidthType.java
 * ---------------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 24-Mar-2004 : Version 1 (DG);
 *
 */

package org.jfree.chart.axis;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Represents the width types for a category label.
 */
public final class CategoryLabelWidthType implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -6976024792582949656L;

    /** Percentage of category. */
    public static final CategoryLabelWidthType
        CATEGORY = new CategoryLabelWidthType(
            "CategoryLabelWidthType.CATEGORY"
        );

    /** Percentage of range. */
    public static final CategoryLabelWidthType
        RANGE = new CategoryLabelWidthType("CategoryLabelWidthType.RANGE");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name (<code>null</code> not permitted).
     */
    private CategoryLabelWidthType(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Null 'name' argument.");
        }
        this.name = name;
    }

    /**
     * Returns a string representing the object.
     *
     * @return The string (never </code>null</code>).
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
        if (!(obj instanceof CategoryLabelWidthType)) {
            return false;
        }
        CategoryLabelWidthType t = (CategoryLabelWidthType) obj;
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
        if (this.equals(CategoryLabelWidthType.CATEGORY)) {
            return CategoryLabelWidthType.CATEGORY;
        }
        else if (this.equals(CategoryLabelWidthType.RANGE)) {
            return CategoryLabelWidthType.RANGE;
        }
        return null;
    }

}
