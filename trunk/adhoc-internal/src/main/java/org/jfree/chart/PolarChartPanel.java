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
 * PolarChartPanel.java
 * --------------------
 * (C) Copyright 2004-2008, by Solution Engineering, Inc. and Contributors.
 *
 * Original Author:  Daniel Bridenbecker, Solution Engineering, Inc.;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 19-Jan-2004 : Version 1, contributed by DB with minor changes by DG (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags all over JFreeChart sources (DG);
 *
 */

package org.jfree.chart;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PolarPlot;

/**
 * <code>PolarChartPanel</code> is the top level object for using the
 * {@link PolarPlot}. Since this class has a <code>JPanel</code> in the
 * inheritance hierarchy, one uses this class to integrate the Polar plot into
 * their application.
 * <p>
 * The main modification to <code>ChartPanel</code> is the popup menu.  It
 * removes <code>ChartPanel</code>'s versions of:
 * <ul>
 *    <li><code>Zoom In</code></li>
 *    <li><code>Zoom Out</code></li>
 *    <li><code>Auto Range</code></li>
 * </ul>
 * and replaces them with versions more appropriate for {@link PolarPlot}.
 */
public class PolarChartPanel extends ChartPanel {

    // -----------------
    // --- Constants ---
    // -----------------

    /** Zoom in command string. */
    private static final String POLAR_ZOOM_IN_ACTION_COMMAND = "Polar Zoom In";

    /** Zoom out command string. */
    private static final String POLAR_ZOOM_OUT_ACTION_COMMAND
        = "Polar Zoom Out";

    /** Auto range command string. */
    private static final String POLAR_AUTO_RANGE_ACTION_COMMAND
        = "Polar Auto Range";

    // ------------------------
    // --- Member Variables ---
    // ------------------------

    // --------------------
    // --- Constructors ---
    // --------------------
    /**
     * Constructs a JFreeChart panel.
     *
     * @param chart  the chart.
     */
    public PolarChartPanel(JFreeChart chart) {
        this(chart, true);
    }

    /**
     * Creates a new panel.
     *
     * @param chart  the chart.
     * @param useBuffer  buffered?
     */
    public PolarChartPanel(JFreeChart chart, boolean useBuffer) {
        super(chart, useBuffer);
        checkChart(chart);
        setMinimumDrawWidth(200);
        setMinimumDrawHeight(200);
        setMaximumDrawWidth(2000);
        setMaximumDrawHeight(2000);
    }

    // --------------------------
    // --- ChartPanel Methods ---
    // --------------------------
    /**
     * Sets the chart that is displayed in the panel.
     *
     * @param chart  The chart.
     */
    public void setChart(JFreeChart chart) {
        checkChart(chart);
        super.setChart(chart);
    }

    /**
     * Creates a popup menu for the panel.
     *
     * @param properties  include a menu item for the chart property editor.
     * @param save  include a menu item for saving the chart.
     * @param print  include a menu item for printing the chart.
     * @param zoom  include menu items for zooming.
     *
     * @return The popup menu.
     */
    protected JPopupMenu createPopupMenu(boolean properties,
                                         boolean save,
                                         boolean print,
                                         boolean zoom) {

       JPopupMenu result = super.createPopupMenu(properties, save, print, zoom);
       int zoomInIndex  = getPopupMenuItem(result, "Zoom In");
       int zoomOutIndex = getPopupMenuItem(result, "Zoom Out");
       int autoIndex     = getPopupMenuItem(result, "Auto Range");
       if (zoom) {
           JMenuItem zoomIn = new JMenuItem("Zoom In");
           zoomIn.setActionCommand(POLAR_ZOOM_IN_ACTION_COMMAND);
           zoomIn.addActionListener(this);

           JMenuItem zoomOut = new JMenuItem("Zoom Out");
           zoomOut.setActionCommand(POLAR_ZOOM_OUT_ACTION_COMMAND);
           zoomOut.addActionListener(this);

           JMenuItem auto = new JMenuItem("Auto Range");
           auto.setActionCommand(POLAR_AUTO_RANGE_ACTION_COMMAND);
           auto.addActionListener(this);

           if (zoomInIndex != -1) {
               result.remove(zoomInIndex);
           }
           else {
               zoomInIndex = result.getComponentCount() - 1;
           }
           result.add(zoomIn, zoomInIndex);
           if (zoomOutIndex != -1) {
               result.remove(zoomOutIndex);
           }
           else {
               zoomOutIndex = zoomInIndex + 1;
           }
           result.add(zoomOut, zoomOutIndex);
           if (autoIndex != -1) {
               result.remove(autoIndex);
           }
           else {
               autoIndex = zoomOutIndex + 1;
           }
           result.add(auto, autoIndex);
       }
       return result;
    }

    /**
     * Handles action events generated by the popup menu.
     *
     * @param event  the event.
     */
    public void actionPerformed(ActionEvent event) {
       String command = event.getActionCommand();

       if (command.equals(POLAR_ZOOM_IN_ACTION_COMMAND)) {
           PolarPlot plot = (PolarPlot) getChart().getPlot();
           plot.zoom(0.5);
       }
       else if (command.equals(POLAR_ZOOM_OUT_ACTION_COMMAND)) {
           PolarPlot plot = (PolarPlot) getChart().getPlot();
           plot.zoom(2.0);
       }
       else if (command.equals(POLAR_AUTO_RANGE_ACTION_COMMAND)) {
           PolarPlot plot = (PolarPlot) getChart().getPlot();
           plot.getAxis().setAutoRange(true);
       }
       else {
           super.actionPerformed(event);
       }
    }

    // ----------------------
    // --- Public Methods ---
    // ----------------------

    // -----------------------
    // --- Private Methods ---
    // -----------------------

    /**
     * Test that the chart is using an xy plot with time as the domain axis.
     *
     * @param chart  the chart.
     */
    private void checkChart(JFreeChart chart) {
        Plot plot = chart.getPlot();
        if (!(plot instanceof PolarPlot)) {
            throw new IllegalArgumentException("plot is not a PolarPlot");
       }
    }

    /**
     * Returns the index of an item in a popup menu.
     *
     * @param menu  the menu.
     * @param text  the label.
     *
     * @return The item index.
     */
    private int getPopupMenuItem(JPopupMenu menu, String text) {
        int index = -1;
        for (int i = 0; (index == -1) && (i < menu.getComponentCount()); i++) {
            Component comp = menu.getComponent(i);
            if (comp instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) comp;
                if (text.equals(item.getText())) {
                    index = i;
                }
            }
       }
       return index;
    }

}
