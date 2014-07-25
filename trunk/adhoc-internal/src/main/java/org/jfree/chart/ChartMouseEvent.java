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
 * ChartMouseEvent.java
 * --------------------
 * (C) Copyright 2002-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Alex Weber;
 *
 * Changes
 * -------
 * 27-May-2002 : Version 1, incorporating code and ideas by Alex Weber (DG);
 * 13-Jun-2002 : Added Javadoc comments (DG);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 05-Nov-2002 : Added a reference to the source chart (DG);
 * 13-Jul-2004 : Now extends EventObject and implements Serializable (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 24-May-2007 : Updated API docs (DG);
 *
 */

package org.jfree.chart;

import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.EventObject;

import org.jfree.chart.entity.ChartEntity;

/**
 * A mouse event for a chart that is displayed in a {@link ChartPanel}.
 *
 * @see ChartMouseListener
 */
public class ChartMouseEvent extends EventObject implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -682393837314562149L;

    /** The chart that the mouse event relates to. */
    private JFreeChart chart;

    /** The Java mouse event that triggered this event. */
    private MouseEvent trigger;

    /** The chart entity (if any). */
    private ChartEntity entity;

    /**
     * Constructs a new event.
     *
     * @param chart  the source chart (<code>null</code> not permitted).
     * @param trigger  the mouse event that triggered this event
     *                 (<code>null</code> not permitted).
     * @param entity  the chart entity (if any) under the mouse point
     *                (<code>null</code> permitted).
     */
    public ChartMouseEvent(JFreeChart chart, MouseEvent trigger,
                           ChartEntity entity) {
        super(chart);
        this.chart = chart;
        this.trigger = trigger;
        this.entity = entity;
    }

    /**
     * Returns the chart that the mouse event relates to.
     *
     * @return The chart (never <code>null</code>).
     */
    public JFreeChart getChart() {
        return this.chart;
    }

    /**
     * Returns the mouse event that triggered this event.
     *
     * @return The event (never <code>null</code>).
     */
    public MouseEvent getTrigger() {
        return this.trigger;
    }

    /**
     * Returns the chart entity (if any) under the mouse point.
     *
     * @return The chart entity (possibly <code>null</code>).
     */
    public ChartEntity getEntity() {
        return this.entity;
    }

}
