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
 * ------------------------------------
 * StandardContourToolTipGenerator.java
 * ------------------------------------
 * (C) Copyright 2002-2008, by David M. O'Donnell and Contributors.
 *
 * Original Author:  David M. O'Donnell;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 23-Jan-2003 : Added standard header (DG);
 * 21-Mar-2003 : Implemented Serializable (DG);
 * 15-Jul-2004 : Switched the getZ() and getZValue() methods (DG);
 * 19-Jan-2005 : Now accesses primitives only from dataset (DG);
 *
 */

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.contour.ContourDataset;

/**
 * A standard tooltip generator for plots that use data from an
 * {@link ContourDataset}.
 *
 * @deprecated This class is no longer supported (as of version 1.0.4).  If
 *     you are creating contour plots, please try to use {@link XYPlot} and
 *     {@link XYBlockRenderer}.
 */
public class StandardContourToolTipGenerator implements ContourToolTipGenerator,
                                                        Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -1881659351247502711L;

    /** The number formatter. */
    private DecimalFormat valueForm = new DecimalFormat("##.###");

    /**
     * Generates a tooltip text item for a particular item within a series.
     *
     * @param data  the dataset.
     * @param item  the item index (zero-based).
     *
     * @return The tooltip text.
     */
    public String generateToolTip(ContourDataset data, int item) {

        double x = data.getXValue(0, item);
        double y = data.getYValue(0, item);
        double z = data.getZValue(0, item);
        String xString = null;

        if (data.isDateAxis(0)) {
            SimpleDateFormat formatter
                = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
            StringBuffer strbuf = new StringBuffer();
            strbuf = formatter.format(
                new Date((long) x), strbuf, new java.text.FieldPosition(0)
            );
            xString = strbuf.toString();
        }
        else {
            xString = this.valueForm.format(x);
        }
        if (!Double.isNaN(z)) {
            return "X: " + xString
                   + ", Y: " + this.valueForm.format(y)
                   + ", Z: " + this.valueForm.format(z);
        }
        else {
            return "X: " + xString
                 + ", Y: " + this.valueForm.format(y)
                 + ", Z: no data";
        }

    }

    /**
     * Tests if this object is equal to another.
     *
     * @param obj  the other object.
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof StandardContourToolTipGenerator)) {
            return false;
        }
        StandardContourToolTipGenerator that
            = (StandardContourToolTipGenerator) obj;
        if (this.valueForm != null) {
            return this.valueForm.equals(that.valueForm);
        }
        return false;

    }

}
