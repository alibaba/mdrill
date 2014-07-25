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
 * DialLayerChangeEvent.java
 * -------------------------
 * (C) Copyright 2006-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 06-Nov-2006 : Version 1 (DG);
 *
 */

package org.jfree.chart.plot.dial;

import org.jfree.chart.event.ChartChangeEvent;

/**
 * An event that can be forwarded to any {@link DialLayerChangeListener} to
 * signal a change to a {@link DialLayer}.
 *
 * @since 1.0.7
 */
public class DialLayerChangeEvent extends ChartChangeEvent {

    /** The dial layer that generated the event. */
    private DialLayer layer;

    /**
     * Creates a new instance.
     *
     * @param layer  the dial layer that generated the event.
     */
    public DialLayerChangeEvent(DialLayer layer) {
        super(layer);
        this.layer = layer;
    }

    /**
     * Returns the layer that generated the event.
     *
     * @return The layer that generated the event.
     */
    public DialLayer getDialLayer() {
        return this.layer;
    }

}
