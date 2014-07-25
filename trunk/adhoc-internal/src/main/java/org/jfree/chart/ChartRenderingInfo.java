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
 * ChartRenderingInfo.java
 * -----------------------
 * (C) Copyright 2002-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 22-Jan-2002 : Version 1 (DG);
 * 05-Feb-2002 : Added a new constructor, completed Javadoc comments (DG);
 * 05-Mar-2002 : Added a clear() method (DG);
 * 23-May-2002 : Renamed DrawInfo --> ChartRenderingInfo (DG);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 17-Sep-2003 : Added PlotRenderingInfo (DG);
 * 01-Nov-2005 : Updated equals() method (DG);
 * 30-Nov-2005 : Removed get/setPlotArea() (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 01-Dec-2006 : Fixed equals() and clone() (DG);
 *
 */

package org.jfree.chart;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.io.SerialUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A structure for storing rendering information from one call to the
 * JFreeChart.draw() method.
 * <P>
 * An instance of the {@link JFreeChart} class can draw itself within an
 * arbitrary rectangle on any <code>Graphics2D</code>.  It is assumed that
 * client code will sometimes render the same chart in more than one view, so
 * the {@link JFreeChart} instance does not retain any information about its
 * rendered dimensions.  This information can be useful sometimes, so you have
 * the option to collect the information at each call to
 * <code>JFreeChart.draw()</code>, by passing an instance of this
 * <code>ChartRenderingInfo</code> class.
 */
public class ChartRenderingInfo implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 2751952018173406822L;

    /** The area in which the chart is drawn. */
    private transient Rectangle2D chartArea;

    /** Rendering info for the chart's plot (and subplots, if any). */
    private PlotRenderingInfo plotInfo;

    /**
     * Storage for the chart entities.  Since retaining entity information for
     * charts with a large number of data points consumes a lot of memory, it
     * is intended that you can set this to <code>null</code> to prevent the
     * information being collected.
     */
    private EntityCollection entities;

    /**
     * Constructs a new ChartRenderingInfo structure that can be used to
     * collect information about the dimensions of a rendered chart.
     */
    public ChartRenderingInfo() {
        this(new StandardEntityCollection());
    }

    /**
     * Constructs a new instance. If an entity collection is supplied, it will
     * be populated with information about the entities in a chart.  If it is
     * <code>null</code>, no entity information (including tool tips) will
     * be collected.
     *
     * @param entities  an entity collection (<code>null</code> permitted).
     */
    public ChartRenderingInfo(EntityCollection entities) {
        this.chartArea = new Rectangle2D.Double();
        this.plotInfo = new PlotRenderingInfo(this);
        this.entities = entities;
    }

    /**
     * Returns the area in which the chart was drawn.
     *
     * @return The area in which the chart was drawn.
     *
     * @see #setChartArea(Rectangle2D)
     */
    public Rectangle2D getChartArea() {
        return this.chartArea;
    }

    /**
     * Sets the area in which the chart was drawn.
     *
     * @param area  the chart area.
     *
     * @see #getChartArea()
     */
    public void setChartArea(Rectangle2D area) {
        this.chartArea.setRect(area);
    }

    /**
     * Returns the collection of entities maintained by this instance.
     *
     * @return The entity collection (possibly <code>null</code>).
     *
     * @see #setEntityCollection(EntityCollection)
     */
    public EntityCollection getEntityCollection() {
        return this.entities;
    }

    /**
     * Sets the entity collection.
     *
     * @param entities  the entity collection (<code>null</code> permitted).
     *
     * @see #getEntityCollection()
     */
    public void setEntityCollection(EntityCollection entities) {
        this.entities = entities;
    }

    /**
     * Clears the information recorded by this object.
     */
    public void clear() {
        this.chartArea.setRect(0.0, 0.0, 0.0, 0.0);
        this.plotInfo = new PlotRenderingInfo(this);
        if (this.entities != null) {
            this.entities.clear();
        }
    }

    /**
     * Returns the rendering info for the chart's plot.
     *
     * @return The rendering info for the plot.
     */
    public PlotRenderingInfo getPlotInfo() {
        return this.plotInfo;
    }

    /**
     * Tests this object for equality with an arbitrary object.
     *
     * @param obj  the object to test against (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ChartRenderingInfo)) {
            return false;
        }
        ChartRenderingInfo that = (ChartRenderingInfo) obj;
        if (!ObjectUtilities.equal(this.chartArea, that.chartArea)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.plotInfo, that.plotInfo)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.entities, that.entities)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a clone of this object.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if the object cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        ChartRenderingInfo clone = (ChartRenderingInfo) super.clone();
        if (this.chartArea != null) {
            clone.chartArea = (Rectangle2D) this.chartArea.clone();
        }
        if (this.entities instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable) this.entities;
            clone.entities = (EntityCollection) pc.clone();
        }
        return clone;
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
        SerialUtilities.writeShape(this.chartArea, stream);
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
        this.chartArea = (Rectangle2D) SerialUtilities.readShape(stream);
    }

}
