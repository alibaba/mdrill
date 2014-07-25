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
 * -------------------------
 * XYDrawableAnnotation.java
 * -------------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 21-May-2003 : Version 1 (DG);
 * 21-Jan-2004 : Update for renamed method in ValueAxis (DG);
 * 30-Sep-2004 : Added support for tool tips and URLs (DG);
 * 18-Jun-2008 : Added scaling factor (DG);
 *
 */

package org.jfree.chart.annotations;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.Drawable;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A general annotation that can be placed on an {@link XYPlot}.
 */
public class XYDrawableAnnotation extends AbstractXYAnnotation
        implements Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -6540812859722691020L;

    /** The scaling factor. */
    private double drawScaleFactor;

    /** The x-coordinate. */
    private double x;

    /** The y-coordinate. */
    private double y;

    /** The width. */
    private double displayWidth;

    /** The height. */
    private double displayHeight;

    /** The drawable object. */
    private Drawable drawable;

    /**
     * Creates a new annotation to be displayed within the given area.
     *
     * @param x  the x-coordinate for the area.
     * @param y  the y-coordinate for the area.
     * @param width  the width of the area.
     * @param height  the height of the area.
     * @param drawable  the drawable object (<code>null</code> not permitted).
     */
    public XYDrawableAnnotation(double x, double y, double width, double height,
                                Drawable drawable) {
        this(x, y, width, height, 1.0, drawable);
    }

    /**
     * Creates a new annotation to be displayed within the given area.  If you
     * specify a <code>drawScaleFactor</code> of 2.0, the <code>drawable</code>
     * will be drawn at twice the requested display size then scaled down to
     * fit the space.
     *
     * @param x  the x-coordinate for the area.
     * @param y  the y-coordinate for the area.
     * @param displayWidth  the width of the area.
     * @param displayHeight  the height of the area.
     * @param drawScaleFactor  the scaling factor for drawing.
     * @param drawable  the drawable object (<code>null</code> not permitted).
     *
     * @since 1.0.11
     */
    public XYDrawableAnnotation(double x, double y, double displayWidth,
            double displayHeight, double drawScaleFactor, Drawable drawable) {

        if (drawable == null) {
            throw new IllegalArgumentException("Null 'drawable' argument.");
        }
        this.x = x;
        this.y = y;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.drawScaleFactor = drawScaleFactor;
        this.drawable = drawable;

    }

    /**
     * Draws the annotation.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param dataArea  the data area.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param rendererIndex  the renderer index.
     * @param info  if supplied, this info object will be populated with
     *              entity information.
     */
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
                     ValueAxis domainAxis, ValueAxis rangeAxis,
                     int rendererIndex,
                     PlotRenderingInfo info) {

        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
                plot.getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
                plot.getRangeAxisLocation(), orientation);
        float j2DX = (float) domainAxis.valueToJava2D(this.x, dataArea,
                domainEdge);
        float j2DY = (float) rangeAxis.valueToJava2D(this.y, dataArea,
                rangeEdge);
        Rectangle2D displayArea = new Rectangle2D.Double(
                j2DX - this.displayWidth / 2.0,
                j2DY - this.displayHeight / 2.0, this.displayWidth,
                this.displayHeight);

        // here we change the AffineTransform so we can draw the annotation
        // to a larger area and scale it down into the display area
        // afterwards, the original transform is restored
        AffineTransform savedTransform = g2.getTransform();
        Rectangle2D drawArea = new Rectangle2D.Double(0.0, 0.0,
                this.displayWidth * this.drawScaleFactor,
                this.displayHeight * this.drawScaleFactor);

        g2.scale(1/this.drawScaleFactor, 1/this.drawScaleFactor);
        g2.translate((j2DX - this.displayWidth / 2.0) * this.drawScaleFactor,
                (j2DY - this.displayHeight / 2.0) * this.drawScaleFactor);
        this.drawable.draw(g2, drawArea);
        g2.setTransform(savedTransform);
        String toolTip = getToolTipText();
        String url = getURL();
        if (toolTip != null || url != null) {
            addEntity(info, displayArea, rendererIndex, toolTip, url);
        }

    }

    /**
     * Tests this annotation for equality with an arbitrary object.
     *
     * @param obj  the object to test against.
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object obj) {

        if (obj == this) { // simple case
            return true;
        }
        // now try to reject equality...
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof XYDrawableAnnotation)) {
            return false;
        }
        XYDrawableAnnotation that = (XYDrawableAnnotation) obj;
        if (this.x != that.x) {
            return false;
        }
        if (this.y != that.y) {
            return false;
        }
        if (this.displayWidth != that.displayWidth) {
            return false;
        }
        if (this.displayHeight != that.displayHeight) {
            return false;
        }
        if (this.drawScaleFactor != that.drawScaleFactor) {
            return false;
        }
        if (!ObjectUtilities.equal(this.drawable, that.drawable)) {
            return false;
        }
        // seem to be the same...
        return true;

    }

    /**
     * Returns a hash code.
     *
     * @return A hash code.
     */
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(this.x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.y);
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.displayWidth);
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.displayHeight);
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * Returns a clone of the annotation.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  if the annotation can't be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
