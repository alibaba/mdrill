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
 * ----------------------
 * MarkerChangeEvent.java
 * ----------------------
 * (C) Copyright 2006-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 05-Sep-2006 : Version 1 (DG);
 *
 */

package org.jfree.chart.event;

import org.jfree.chart.plot.Marker;

/**
 * An event that can be forwarded to any {@link MarkerChangeListener} to
 * signal a change to a {@link Marker}.
 *
 * @since 1.0.3
 */
public class MarkerChangeEvent extends ChartChangeEvent {

    /** The plot that generated the event. */
    private Marker marker;

    /**
     * Creates a new <code>MarkerChangeEvent</code> instance.
     *
     * @param marker  the marker that triggered the event (<code>null</code>
     *     not permitted).
     *
     * @since 1.0.3
     */
    public MarkerChangeEvent(Marker marker) {
        super(marker);
        this.marker = marker;
    }

    /**
     * Returns the marker that triggered the event.
     *
     * @return The marker that triggered the event (never <code>null</code>).
     *
     * @since 1.0.3
     */
    public Marker getMarker() {
        return this.marker;
    }

}
