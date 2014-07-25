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
 * -------------------
 * ChartSelection.java
 * -------------------
 * (C) Copyright 2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 08-Apr-2009 : Version 1, with inspiration from patch 1460845 (DG);
 *
 */

package org.jfree.chart;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * A class used to represent a chart on the clipboard.
 *
 * @since 1.0.13
 */
public class ChartTransferable implements Transferable {

    /** The data flavor. */
    final DataFlavor imageFlavor = new DataFlavor(
            "image/x-java-image; class=java.awt.Image", "Image");    
    
    /** The chart. */
    private JFreeChart chart;

    /** The width of the chart on the clipboard. */
    private int width;

    /** The height of the chart on the clipboard. */
    private int height;

    /**
     * Creates a new chart selection.
     *
     * @param chart  the chart.
     * @param width  the chart width.
     * @param height  the chart height.
     */
    public ChartTransferable(JFreeChart chart, int width, int height) {
        this(chart, width, height, true);
    }

    /**
     * Creates a new chart selection.
     *
     * @param chart  the chart.
     * @param width  the chart width.
     * @param height  the chart height.
     * @param cloneData  clone the dataset(s)?
     */
    public ChartTransferable(JFreeChart chart, int width, int height,
            boolean cloneData) {

        // we clone the chart because presumably there can be some delay
        // between putting this instance on the system clipboard and
        // actually having the getTransferData() method called...
        try {
            this.chart = (JFreeChart) chart.clone();
        }
        catch (CloneNotSupportedException e) {
            this.chart = chart;
        }
        this.width = width;
        this.height = height;
        // FIXME: we've cloned the chart, but the dataset(s) aren't cloned
        // and we should do that
    }

    /**
     * Returns the data flavors supported.
     * 
     * @return The data flavors supported.
     */
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {this.imageFlavor};
    }

    /**
     * Returns <code>true</code> if the specified flavor is supported.
     *
     * @param flavor  the flavor.
     *
     * @return A boolean.
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return this.imageFlavor.equals(flavor);
    }

    /**
     * Returns the content for the requested flavor, if it is supported.
     *
     * @param flavor  the requested flavor.
     *
     * @return The content.
     *
     * @throws java.awt.datatransfer.UnsupportedFlavorException
     * @throws java.io.IOException
     */
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        
        if (this.imageFlavor.equals(flavor)) {
            return this.chart.createBufferedImage(width, height);
        }
        else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

}
