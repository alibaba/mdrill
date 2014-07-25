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
 * AxisChangeEvent.java
 * --------------------
 * (C) Copyright 2000-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes (from 24-Aug-2001)
 * --------------------------
 * 24-Aug-2001 : Added standard source header. Fixed DOS encoding problem (DG);
 * 09-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.chart.event;

import org.jfree.chart.axis.Axis;

/**
 * A change event that encapsulates information about a change to an axis.
 */
public class AxisChangeEvent extends ChartChangeEvent {

    /** The axis that generated the change event. */
    private Axis axis;

    /**
     * Creates a new AxisChangeEvent.
     *
     * @param axis  the axis that generated the event.
     */
    public AxisChangeEvent(Axis axis) {
        super(axis);
        this.axis = axis;
    }

    /**
     * Returns the axis that generated the event.
     *
     * @return The axis that generated the event.
     */
    public Axis getAxis() {
        return this.axis;
    }

}
