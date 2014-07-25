/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
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
 * -------------
 * Pannable.java
 * -------------
 *
 * (C) Copyright 2009, by Object Refinery Limited and Contributors.
 *
 * Original Author:  Ulrich Voigt - patch 2686040;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 19-Mar-2009 : Version 1, with modifications from patch by UV (DG);
 *
 */

package org.jfree.chart.plot;

import java.awt.geom.Point2D;

import org.jfree.chart.ChartPanel;

/**
 * An interface that the {@link ChartPanel} class uses to communicate with
 * plots that support panning.
 *
 * @since 1.0.13
 */
public interface Pannable {

    /**
     * Returns the orientation of the plot.
     *
     * @return The orientation (never <code>null</code>).
     */
    public PlotOrientation getOrientation();

    /**
     * Evaluates if the domain axis can be panned.
     *
     * @return <code>true</code> if the domain axis is pannable.
     */
    public boolean isDomainPannable();

    /**
     * Evaluates if the range axis can be panned.
     *
     * @return <code>true</code> if the range axis is pannable.
     */
    public boolean isRangePannable();

    /**
     * Pans the domain axes by the specified percentage.
     *
     * @param percent  the distance to pan (as a percentage of the axis length).
     * @param info the plot info
     * @param source the source point where the pan action started.
     */
    public void panDomainAxes(double percent, PlotRenderingInfo info,
            Point2D source);

    /**
     * Pans the range axes by the specified percentage.
     *
     * @param percent  the distance to pan (as a percentage of the axis length).
     * @param info the plot info
     * @param source the source point where the pan action started.
     */
    public void panRangeAxes(double percent, PlotRenderingInfo info,
            Point2D source);

}
