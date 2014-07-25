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
 * ---------------------
 * JFreeChartEntity.java
 * --------------------
 * (C) Copyright 2009, by Object Refinery Limited and Contributors.
 *
 * Original Author:  Peter Kolb;
 * Contributor(s):   ;
 *
 * Changes:
 * --------
 * 15-Feb-2009 : Version 1 (PK);
 *
 */

package org.jfree.chart.entity;

import java.awt.Shape;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.HashUtilities;
import org.jfree.io.SerialUtilities;
import org.jfree.util.ObjectUtilities;

/**
 * A class that captures information about an entire chart.
 *
 * @since 1.0.13
 */
public class JFreeChartEntity extends ChartEntity {

    /** For serialization. */
    private static final long serialVersionUID = -4445994133561919083L;
            //same as for ChartEntity!

    /** The chart. */
    private JFreeChart chart;

    /**
     * Creates a new chart entity.
     *
     * @param area  the area (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     */
    public JFreeChartEntity(Shape area, JFreeChart chart) {
        // defer argument checks...
        this(area, chart, null);
    }

    /**
     * Creates a new chart entity.
     *
     * @param area  the area (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param toolTipText  the tool tip text (<code>null</code> permitted).
     */
    public JFreeChartEntity(Shape area, JFreeChart chart, String toolTipText) {
        // defer argument checks...
        this(area, chart, toolTipText, null);
    }

    /**
     * Creates a new chart entity.
     *
     * @param area  the area (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param toolTipText  the tool tip text (<code>null</code> permitted).
     * @param urlText  the URL text for HTML image maps (<code>null</code>
     *                 permitted).
     */
    public JFreeChartEntity(Shape area, JFreeChart chart, String toolTipText,
            String urlText) {
        super(area, toolTipText, urlText);
        if (chart == null) {
            throw new IllegalArgumentException("Null 'chart' argument.");
        }

        this.chart = chart;
    }

    /**
     * Returns the chart that occupies the entity area.
     *
     * @return The chart (never <code>null</code>).
     */
    public JFreeChart getChart() {
        return this.chart;
    }

    /**
     * Returns a string representation of the chart entity, useful for
     * debugging.
     *
     * @return A string.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("JFreeChartEntity: ");
        buf.append("tooltip = ");
        buf.append(getToolTipText());
        return buf.toString();
    }

    /**
     * Tests the entity for equality with an arbitrary object.
     *
     * @param obj  the object to test against (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof JFreeChartEntity)) {
            return false;
        }
        JFreeChartEntity that = (JFreeChartEntity) obj;
        if (!getArea().equals(that.getArea())) {
            return false;
        }
        if (!ObjectUtilities.equal(getToolTipText(), that.getToolTipText())) {
            return false;
        }
        if (!ObjectUtilities.equal(getURLText(), that.getURLText())) {
            return false;
        }
        if (!(this.chart.equals(that.chart))) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code.
     */
    public int hashCode() {
        int result = 39;
        result = HashUtilities.hashCode(result, getToolTipText());
        result = HashUtilities.hashCode(result, getURLText());
        return result;
    }

    /**
     * Returns a clone of the entity.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if there is a problem cloning the
     *         entity.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape(getArea(), stream);
     }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        setArea(SerialUtilities.readShape(stream));
    }

}
