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
 * ---------------
 * ChartTheme.java
 * ---------------
 * (C) Copyright 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 14-Aug-2008 : Version 1 (DG);
 *
 */

package org.jfree.chart;

import org.jfree.chart.JFreeChart;

/**
 * A {@link ChartTheme} a class that can apply a style or 'theme' to a chart.
 * It can be implemented in an arbitrary manner, with the styling applied to
 * the chart via the <code>apply(JFreeChart)</code> method.  We provide one
 * implementation ({@link StandardChartTheme}) that just mimics the manual
 * process of calling methods to set various chart parameters.
 *
 * @since 1.0.11
 */
public interface ChartTheme {

    /**
     * Applies this theme to the supplied chart.
     *
     * @param chart  the chart (<code>null</code> not permitted).
     */
    public void apply(JFreeChart chart);

}
