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
 * --------------------
 * ItemLabelAnchor.java
 * --------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 29-Apr-2003 : Version 1 (DG);
 * 19-Feb-2004 : Moved to org.jfree.chart.labels package, added readResolve()
 *               method (DG);
 * 11-Jan-2005 : Removed deprecated code in preparation for the 1.0.0
 *               release (DG);
 *
 */

package org.jfree.chart.labels;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * An enumeration of the positions that a value label can take, relative to an
 * item in a {@link org.jfree.chart.plot.CategoryPlot}.
 */
public final class ItemLabelAnchor implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -1233101616128695658L;

    /** CENTER. */
    public static final ItemLabelAnchor CENTER
        = new ItemLabelAnchor("ItemLabelAnchor.CENTER");

    /** INSIDE1. */
    public static final ItemLabelAnchor INSIDE1
        = new ItemLabelAnchor("ItemLabelAnchor.INSIDE1");

    /** INSIDE2. */
    public static final ItemLabelAnchor INSIDE2
        = new ItemLabelAnchor("ItemLabelAnchor.INSIDE2");

    /** INSIDE3. */
    public static final ItemLabelAnchor INSIDE3
        = new ItemLabelAnchor("ItemLabelAnchor.INSIDE3");

    /** INSIDE4. */
    public static final ItemLabelAnchor INSIDE4
        = new ItemLabelAnchor("ItemLabelAnchor.INSIDE4");

    /** INSIDE5. */
    public static final ItemLabelAnchor INSIDE5
        = new ItemLabelAnchor("ItemLabelAnchor.INSIDE5");

    /** INSIDE6. */
    public static final ItemLabelAnchor INSIDE6
        = new ItemLabelAnchor("ItemLabelAnchor.INSIDE6");

    /** INSIDE7. */
    public static final ItemLabelAnchor INSIDE7
        = new ItemLabelAnchor("ItemLabelAnchor.INSIDE7");

    /** INSIDE8. */
    public static final ItemLabelAnchor INSIDE8
        = new ItemLabelAnchor("ItemLabelAnchor.INSIDE8");

    /** INSIDE9. */
    public static final ItemLabelAnchor INSIDE9
        = new ItemLabelAnchor("ItemLabelAnchor.INSIDE9");

    /** INSIDE10. */
    public static final ItemLabelAnchor INSIDE10
        = new ItemLabelAnchor("ItemLabelAnchor.INSIDE10");

    /** INSIDE11. */
    public static final ItemLabelAnchor INSIDE11
        = new ItemLabelAnchor("ItemLabelAnchor.INSIDE11");

    /** INSIDE12. */
    public static final ItemLabelAnchor INSIDE12
        = new ItemLabelAnchor("ItemLabelAnchor.INSIDE12");

    /** OUTSIDE1. */
    public static final ItemLabelAnchor OUTSIDE1
        = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE1");

    /** OUTSIDE2. */
    public static final ItemLabelAnchor OUTSIDE2
        = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE2");

    /** OUTSIDE3. */
    public static final ItemLabelAnchor OUTSIDE3
        = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE3");

    /** OUTSIDE4. */
    public static final ItemLabelAnchor OUTSIDE4
        = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE4");

    /** OUTSIDE5. */
    public static final ItemLabelAnchor OUTSIDE5
        = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE5");

    /** OUTSIDE6. */
    public static final ItemLabelAnchor OUTSIDE6
        = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE6");

    /** OUTSIDE7. */
    public static final ItemLabelAnchor OUTSIDE7
        = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE7");

    /** OUTSIDE8. */
    public static final ItemLabelAnchor OUTSIDE8
        = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE8");

    /** OUTSIDE9. */
    public static final ItemLabelAnchor OUTSIDE9
        = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE9");

    /** OUTSIDE10. */
    public static final ItemLabelAnchor OUTSIDE10
        = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE10");

    /** OUTSIDE11. */
    public static final ItemLabelAnchor OUTSIDE11
        = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE11");

    /** OUTSIDE12. */
    public static final ItemLabelAnchor OUTSIDE12
        = new ItemLabelAnchor("ItemLabelAnchor.OUTSIDE12");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private ItemLabelAnchor(String name) {
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
     * @param o  the other object.
     *
     * @return A boolean.
     */
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemLabelAnchor)) {
            return false;
        }

        ItemLabelAnchor order = (ItemLabelAnchor) o;
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
        ItemLabelAnchor result = null;
        if (this.equals(ItemLabelAnchor.CENTER)) {
            result = ItemLabelAnchor.CENTER;
        }
        else if (this.equals(ItemLabelAnchor.INSIDE1)) {
            result = ItemLabelAnchor.INSIDE1;
        }
        else if (this.equals(ItemLabelAnchor.INSIDE2)) {
            result = ItemLabelAnchor.INSIDE2;
        }
        else if (this.equals(ItemLabelAnchor.INSIDE3)) {
            result = ItemLabelAnchor.INSIDE3;
        }
        else if (this.equals(ItemLabelAnchor.INSIDE4)) {
            result = ItemLabelAnchor.INSIDE4;
        }
        else if (this.equals(ItemLabelAnchor.INSIDE5)) {
            result = ItemLabelAnchor.INSIDE5;
        }
        else if (this.equals(ItemLabelAnchor.INSIDE6)) {
            result = ItemLabelAnchor.INSIDE6;
        }
        else if (this.equals(ItemLabelAnchor.INSIDE7)) {
            result = ItemLabelAnchor.INSIDE7;
        }
        else if (this.equals(ItemLabelAnchor.INSIDE8)) {
            result = ItemLabelAnchor.INSIDE8;
        }
        else if (this.equals(ItemLabelAnchor.INSIDE9)) {
            result = ItemLabelAnchor.INSIDE9;
        }
        else if (this.equals(ItemLabelAnchor.INSIDE10)) {
            result = ItemLabelAnchor.INSIDE10;
        }
        else if (this.equals(ItemLabelAnchor.INSIDE11)) {
            result = ItemLabelAnchor.INSIDE11;
        }
        else if (this.equals(ItemLabelAnchor.INSIDE12)) {
            result = ItemLabelAnchor.INSIDE12;
        }
        else if (this.equals(ItemLabelAnchor.OUTSIDE1)) {
            result = ItemLabelAnchor.OUTSIDE1;
        }
        else if (this.equals(ItemLabelAnchor.OUTSIDE2)) {
            result = ItemLabelAnchor.OUTSIDE2;
        }
        else if (this.equals(ItemLabelAnchor.OUTSIDE3)) {
            result = ItemLabelAnchor.OUTSIDE3;
        }
        else if (this.equals(ItemLabelAnchor.OUTSIDE4)) {
            result = ItemLabelAnchor.OUTSIDE4;
        }
        else if (this.equals(ItemLabelAnchor.OUTSIDE5)) {
            result = ItemLabelAnchor.OUTSIDE5;
        }
        else if (this.equals(ItemLabelAnchor.OUTSIDE6)) {
            result = ItemLabelAnchor.OUTSIDE6;
        }
        else if (this.equals(ItemLabelAnchor.OUTSIDE7)) {
            result = ItemLabelAnchor.OUTSIDE7;
        }
        else if (this.equals(ItemLabelAnchor.OUTSIDE8)) {
            result = ItemLabelAnchor.OUTSIDE8;
        }
        else if (this.equals(ItemLabelAnchor.OUTSIDE9)) {
            result = ItemLabelAnchor.OUTSIDE9;
        }
        else if (this.equals(ItemLabelAnchor.OUTSIDE10)) {
            result = ItemLabelAnchor.OUTSIDE10;
        }
        else if (this.equals(ItemLabelAnchor.OUTSIDE11)) {
            result = ItemLabelAnchor.OUTSIDE11;
        }
        else if (this.equals(ItemLabelAnchor.OUTSIDE12)) {
            result = ItemLabelAnchor.OUTSIDE12;
        }
        return result;
    }

}
