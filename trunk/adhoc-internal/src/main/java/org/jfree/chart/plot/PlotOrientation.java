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
 * PlotOrientation.java
 * --------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 02-May-2003 : Version 1 (DG);
 * 17-Jul-2003 : Added readResolve() method (DG);
 * 21-Nov-2007 : Implemented hashCode() (DG);
 *
 */

package org.jfree.chart.plot;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Used to indicate the orientation (horizontal or vertical) of a 2D plot.
 */
public final class PlotOrientation implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -2508771828190337782L;

    /** For a plot where the range axis is horizontal. */
    public static final PlotOrientation HORIZONTAL
            = new PlotOrientation("PlotOrientation.HORIZONTAL");

    /** For a plot where the range axis is vertical. */
    public static final PlotOrientation VERTICAL
            = new PlotOrientation("PlotOrientation.VERTICAL");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private PlotOrientation(String name) {
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
        if (!(obj instanceof PlotOrientation)) {
            return false;
        }
        PlotOrientation orientation = (PlotOrientation) obj;
        if (!this.name.equals(orientation.toString())) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code.
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
        Object result = null;
        if (this.equals(PlotOrientation.HORIZONTAL)) {
            result = PlotOrientation.HORIZONTAL;
        }
        else if (this.equals(PlotOrientation.VERTICAL)) {
            result = PlotOrientation.VERTICAL;
        }
        return result;
    }

}
