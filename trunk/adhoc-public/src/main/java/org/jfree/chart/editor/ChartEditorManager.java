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
 * -----------------------
 * ChartEditorManager.java
 * -----------------------
 * (C) Copyright 2005-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   ;
 *
 * Changes
 * -------
 * 24-Nov-2005 : Version 1 (DG);
 *
 */

package org.jfree.chart.editor;

import org.jfree.chart.JFreeChart;

/**
 * The central point for obtaining {@link ChartEditor} instances for editing
 * charts.  Right now, the API is minimal - the plan is to extend this class
 * to provide customisation options for chart editors (for example, make some
 * editor items read-only).
 */
public class ChartEditorManager {

    /** This factory creates new {@link ChartEditor} instances as required. */
    static ChartEditorFactory factory = new DefaultChartEditorFactory();

    /**
     * Private constructor prevents instantiation.
     */
    private ChartEditorManager() {
        // nothing to do
    }

    /**
     * Returns the current factory.
     *
     * @return The current factory (never <code>null</code>).
     */
    public static ChartEditorFactory getChartEditorFactory() {
        return factory;
    }

    /**
     * Sets the chart editor factory.
     *
     * @param f  the new factory (<code>null</code> not permitted).
     */
    public static void setChartEditorFactory(ChartEditorFactory f) {
        if (f == null) {
            throw new IllegalArgumentException("Null 'f' argument.");
        }
        factory = f;
    }

    /**
     * Returns a component that can be used to edit the given chart.
     *
     * @param chart  the chart.
     *
     * @return The chart editor.
     */
    public static ChartEditor getChartEditor(JFreeChart chart) {
        return factory.createEditor(chart);
    }
}
