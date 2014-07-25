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
 * --------------------------
 * DatasetRenderingOrder.java
 * --------------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 02-May-2003 : Version 1 (DG);
 * 02-Jun-2004 : Changed 'STANDARD' --> 'FORWARD' (DG);
 * 21-Nov-2007 : Implemented hashCode() (DG);
 *
 */

package org.jfree.chart.plot;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Defines the tokens that indicate the rendering order for datasets in a
 * {@link org.jfree.chart.plot.CategoryPlot} or an
 * {@link org.jfree.chart.plot.XYPlot}.
 */
public final class DatasetRenderingOrder implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -600593412366385072L;

    /**
     * Render datasets in the order 0, 1, 2, ..., N-1, where N is the number
     * of datasets.
     */
    public static final DatasetRenderingOrder FORWARD
            = new DatasetRenderingOrder("DatasetRenderingOrder.FORWARD");

    /**
     * Render datasets in the order N-1, N-2, ..., 2, 1, 0, where N is the
     * number of datasets.
     */
    public static final DatasetRenderingOrder REVERSE
            = new DatasetRenderingOrder("DatasetRenderingOrder.REVERSE");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private DatasetRenderingOrder(String name) {
        this.name = name;
    }

    /**
     * Returns a string representing the object.
     *
     * @return The string (never <code>null</code>).
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
        if (!(obj instanceof DatasetRenderingOrder)) {
            return false;
        }
        DatasetRenderingOrder order = (DatasetRenderingOrder) obj;
        if (!this.name.equals(order.toString())) {
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
        if (this.equals(DatasetRenderingOrder.FORWARD)) {
            return DatasetRenderingOrder.FORWARD;
        }
        else if (this.equals(DatasetRenderingOrder.REVERSE)) {
            return DatasetRenderingOrder.REVERSE;
        }
        return null;
    }

}
