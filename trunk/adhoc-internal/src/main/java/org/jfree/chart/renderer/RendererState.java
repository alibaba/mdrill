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
 * ------------------
 * RendererState.java
 * ------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 07-Oct-2003 : Version 1 (DG);
 * 09-Jun-2005 : Added a convenience method to access the entity
 *               collection (DG);
 *
 */

package org.jfree.chart.renderer;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotRenderingInfo;

/**
 * Represents the current state of a renderer.
 */
public class RendererState {

    /** The plot rendering info. */
    private PlotRenderingInfo info;

    /**
     * Creates a new state object.
     *
     * @param info  the plot rendering info.
     */
    public RendererState(PlotRenderingInfo info) {
        this.info = info;
    }

    /**
     * Returns the plot rendering info.
     *
     * @return The info.
     */
    public PlotRenderingInfo getInfo() {
        return this.info;
    }

    /**
     * A convenience method that returns a reference to the entity
     * collection (may be <code>null</code>) being used to record
     * chart entities.
     *
     * @return The entity collection (possibly <code>null</code>).
     */
    public EntityCollection getEntityCollection() {
        EntityCollection result = null;
        if (this.info != null) {
            ChartRenderingInfo owner = this.info.getOwner();
            if (owner != null) {
                result = owner.getEntityCollection();
            }
        }
        return result;
    }

}
