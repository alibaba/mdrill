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
 * ----------------------
 * XYShapeAnnotation.java
 * ----------------------
 * (C) Copyright 2003-2008, by Ondax, Inc. and Contributors.
 *
 * Original Author:  Greg Steckman (for Ondax, Inc.);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes:
 * --------
 * 15-Aug-2003 : Version 1, adapted from
 *               org.jfree.chart.annotations.XYLineAnnotation (GS);
 * 21-Jan-2004 : Update for renamed method in ValueAxis (DG);
 * 20-Apr-2004 : Added new constructor and fixed bug 934258 (DG);
 * 29-Sep-2004 : Added 'fillPaint' to allow for colored shapes, now extends
 *               AbstractXYAnnotation to add tool tip and URL support, and
 *               implemented equals() and Cloneable (DG);
 * 21-Jan-2005 : Modified constructor for consistency with other
 *               constructors (DG);
 * 06-Jun-2005 : Fixed equals() method to handle GradientPaint (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 24-Oct-2006 : Calculate AffineTransform on shape's bounding rectangle
 *               rather than sample points (0, 0) and (1, 1) (DG);
 * 06-Mar-2007 : Implemented hashCode() (DG);
 *
 */

package org.jfree.chart.annotations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.HashUtilities;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A simple <code>Shape</code> annotation that can be placed on an
 * {@link XYPlot}.  The shape coordinates are specified in data space.
 */
public class XYShapeAnnotation extends AbstractXYAnnotation
        implements Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -8553218317600684041L;

    /** The shape. */
    private transient Shape shape;

    /** The stroke used to draw the shape's outline. */
    private transient Stroke stroke;

    /** The paint used to draw the shape's outline. */
    private transient Paint outlinePaint;

    /** The paint used to fill the shape. */
    private transient Paint fillPaint;

    /**
     * Creates a new annotation (where, by default, the shape is drawn
     * with a black outline).
     *
     * @param shape  the shape (coordinates in data space, <code>null</code>
     *     not permitted).
     */
    public XYShapeAnnotation(Shape shape) {
        this(shape, new BasicStroke(1.0f), Color.black);
    }

    /**
     * Creates a new annotation where the shape is drawn as an outline using
     * the specified <code>stroke</code> and <code>outlinePaint</code>.
     *
     * @param shape  the shape (<code>null</code> not permitted).
     * @param stroke  the shape stroke (<code>null</code> permitted).
     * @param outlinePaint  the shape color (<code>null</code> permitted).
     */
    public XYShapeAnnotation(Shape shape, Stroke stroke, Paint outlinePaint) {
        this(shape, stroke, outlinePaint, null);
    }

    /**
     * Creates a new annotation.
     *
     * @param shape  the shape (<code>null</code> not permitted).
     * @param stroke  the shape stroke (<code>null</code> permitted).
     * @param outlinePaint  the shape color (<code>null</code> permitted).
     * @param fillPaint  the paint used to fill the shape (<code>null</code>
     *                   permitted.
     */
    public XYShapeAnnotation(Shape shape, Stroke stroke, Paint outlinePaint,
                             Paint fillPaint) {
        if (shape == null) {
            throw new IllegalArgumentException("Null 'shape' argument.");
        }
        this.shape = shape;
        this.stroke = stroke;
        this.outlinePaint = outlinePaint;
        this.fillPaint = fillPaint;
    }

    /**
     * Draws the annotation.  This method is usually called by the
     * {@link XYPlot} class, you shouldn't need to call it directly.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param dataArea  the data area.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param rendererIndex  the renderer index.
     * @param info  the plot rendering info.
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

        // compute transform matrix elements via sample points. Assume no
        // rotation or shear.
        Rectangle2D bounds = this.shape.getBounds2D();
        double x0 = bounds.getMinX();
        double x1 = bounds.getMaxX();
        double xx0 = domainAxis.valueToJava2D(x0, dataArea, domainEdge);
        double xx1 = domainAxis.valueToJava2D(x1, dataArea, domainEdge);
        double m00 = (xx1 - xx0) / (x1 - x0);
        double m02 = xx0 - x0 * m00;

        double y0 = bounds.getMaxY();
        double y1 = bounds.getMinY();
        double yy0 = rangeAxis.valueToJava2D(y0, dataArea, rangeEdge);
        double yy1 = rangeAxis.valueToJava2D(y1, dataArea, rangeEdge);
        double m11 = (yy1 - yy0) / (y1 - y0);
        double m12 = yy0 - m11 * y0;

        //  create transform & transform shape
        Shape s = null;
        if (orientation == PlotOrientation.HORIZONTAL) {
            AffineTransform t1 = new AffineTransform(0.0f, 1.0f, 1.0f, 0.0f,
                    0.0f, 0.0f);
            AffineTransform t2 = new AffineTransform(m11, 0.0f, 0.0f, m00,
                    m12, m02);
            s = t1.createTransformedShape(this.shape);
            s = t2.createTransformedShape(s);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            AffineTransform t = new AffineTransform(m00, 0, 0, m11, m02, m12);
            s = t.createTransformedShape(this.shape);
        }

        if (this.fillPaint != null) {
            g2.setPaint(this.fillPaint);
            g2.fill(s);
        }

        if (this.stroke != null && this.outlinePaint != null) {
            g2.setPaint(this.outlinePaint);
            g2.setStroke(this.stroke);
            g2.draw(s);
        }
        addEntity(info, s, rendererIndex, getToolTipText(), getURL());

    }

    /**
     * Tests this annotation for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        // now try to reject equality
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof XYShapeAnnotation)) {
            return false;
        }
        XYShapeAnnotation that = (XYShapeAnnotation) obj;
        if (!this.shape.equals(that.shape)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.stroke, that.stroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
        }
        if (!PaintUtilities.equal(this.fillPaint, that.fillPaint)) {
            return false;
        }
        // seem to be the same
        return true;
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code.
     */
    public int hashCode() {
        int result = 193;
        result = 37 * result + this.shape.hashCode();
        if (this.stroke != null) {
            result = 37 * result + this.stroke.hashCode();
        }
        result = 37 * result + HashUtilities.hashCodeForPaint(
                this.outlinePaint);
        result = 37 * result + HashUtilities.hashCodeForPaint(this.fillPaint);
        return result;
    }

    /**
     * Returns a clone.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException ???.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape(this.shape, stream);
        SerialUtilities.writeStroke(this.stroke, stream);
        SerialUtilities.writePaint(this.outlinePaint, stream);
        SerialUtilities.writePaint(this.fillPaint, stream);
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
        this.shape = SerialUtilities.readShape(stream);
        this.stroke = SerialUtilities.readStroke(stream);
        this.outlinePaint = SerialUtilities.readPaint(stream);
        this.fillPaint = SerialUtilities.readPaint(stream);
    }

}
