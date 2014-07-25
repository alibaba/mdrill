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
 * ----------------
 * DialPointer.java
 * ----------------
 * (C) Copyright 2006-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 03-Nov-2006 : Version 1 (DG);
 * 17-Oct-2007 : Added equals() overrides (DG);
 * 24-Oct-2007 : Implemented PublicCloneable, changed default radius,
 *               and added argument checks (DG);
 * 23-Nov-2007 : Added fillPaint and outlinePaint attributes to
 *               DialPointer.Pointer (DG);
 *
 */

package org.jfree.chart.plot.dial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.HashUtilities;
import org.jfree.io.SerialUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A base class for the pointer in a {@link DialPlot}.
 *
 * @since 1.0.7
 */
public abstract class DialPointer extends AbstractDialLayer
        implements DialLayer, Cloneable, PublicCloneable, Serializable {

    /** The needle radius. */
    double radius;

    /**
     * The dataset index for the needle.
     */
    int datasetIndex;

    /**
     * Creates a new <code>DialPointer</code> instance.
     */
    protected DialPointer() {
        this(0);
    }

    /**
     * Creates a new pointer for the specified dataset.
     *
     * @param datasetIndex  the dataset index.
     */
    protected DialPointer(int datasetIndex) {
        this.radius = 0.9;
        this.datasetIndex = datasetIndex;
    }

    /**
     * Returns the dataset index that the pointer maps to.
     *
     * @return The dataset index.
     *
     * @see #getDatasetIndex()
     */
    public int getDatasetIndex() {
        return this.datasetIndex;
    }

    /**
     * Sets the dataset index for the pointer and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param index  the index.
     *
     * @see #getDatasetIndex()
     */
    public void setDatasetIndex(int index) {
        this.datasetIndex = index;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the radius of the pointer, as a percentage of the dial's
     * framing rectangle.
     *
     * @return The radius.
     *
     * @see #setRadius(double)
     */
    public double getRadius() {
        return this.radius;
    }

    /**
     * Sets the radius of the pointer and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param radius  the radius.
     *
     * @see #getRadius()
     */
    public void setRadius(double radius) {
        this.radius = radius;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns <code>true</code> to indicate that this layer should be
     * clipped within the dial window.
     *
     * @return <code>true</code>.
     */
    public boolean isClippedToWindow() {
        return true;
    }

    /**
     * Checks this instance for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> not permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DialPointer)) {
            return false;
        }
        DialPointer that = (DialPointer) obj;
        if (this.datasetIndex != that.datasetIndex) {
            return false;
        }
        if (this.radius != that.radius) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns a hash code.
     *
     * @return A hash code.
     */
    public int hashCode() {
        int result = 23;
        result = HashUtilities.hashCode(result, this.radius);
        return result;
    }

    /**
     * Returns a clone of the pointer.
     *
     * @return a clone.
     *
     * @throws CloneNotSupportedException if one of the attributes cannot
     *     be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * A dial pointer that draws a thin line (like a pin).
     */
    public static class Pin extends DialPointer {

        /** For serialization. */
        static final long serialVersionUID = -8445860485367689750L;

        /** The paint. */
        private transient Paint paint;

        /** The stroke. */
        private transient Stroke stroke;

        /**
         * Creates a new instance.
         */
        public Pin() {
            this(0);
        }

        /**
         * Creates a new instance.
         *
         * @param datasetIndex  the dataset index.
         */
        public Pin(int datasetIndex) {
            super(datasetIndex);
            this.paint = Color.red;
            this.stroke = new BasicStroke(3.0f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_BEVEL);
        }

        /**
         * Returns the paint.
         *
         * @return The paint (never <code>null</code>).
         *
         * @see #setPaint(Paint)
         */
        public Paint getPaint() {
            return this.paint;
        }

        /**
         * Sets the paint and sends a {@link DialLayerChangeEvent} to all
         * registered listeners.
         *
         * @param paint  the paint (<code>null</code> not permitted).
         *
         * @see #getPaint()
         */
        public void setPaint(Paint paint) {
            if (paint == null) {
                throw new IllegalArgumentException("Null 'paint' argument.");
            }
            this.paint = paint;
            notifyListeners(new DialLayerChangeEvent(this));
        }

        /**
         * Returns the stroke.
         *
         * @return The stroke (never <code>null</code>).
         *
         * @see #setStroke(Stroke)
         */
        public Stroke getStroke() {
            return this.stroke;
        }

        /**
         * Sets the stroke and sends a {@link DialLayerChangeEvent} to all
         * registered listeners.
         *
         * @param stroke  the stroke (<code>null</code> not permitted).
         *
         * @see #getStroke()
         */
        public void setStroke(Stroke stroke) {
            if (stroke == null) {
                throw new IllegalArgumentException("Null 'stroke' argument.");
            }
            this.stroke = stroke;
            notifyListeners(new DialLayerChangeEvent(this));
        }

        /**
         * Draws the pointer.
         *
         * @param g2  the graphics target.
         * @param plot  the plot.
         * @param frame  the dial's reference frame.
         * @param view  the dial's view.
         */
        public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame,
            Rectangle2D view) {

            g2.setPaint(this.paint);
            g2.setStroke(this.stroke);
            Rectangle2D arcRect = DialPlot.rectangleByRadius(frame,
                    this.radius, this.radius);

            double value = plot.getValue(this.datasetIndex);
            DialScale scale = plot.getScaleForDataset(this.datasetIndex);
            double angle = scale.valueToAngle(value);

            Arc2D arc = new Arc2D.Double(arcRect, angle, 0, Arc2D.OPEN);
            Point2D pt = arc.getEndPoint();

            Line2D line = new Line2D.Double(frame.getCenterX(),
                    frame.getCenterY(), pt.getX(), pt.getY());
            g2.draw(line);
        }

        /**
         * Tests this pointer for equality with an arbitrary object.
         *
         * @param obj  the object (<code>null</code> permitted).
         *
         * @return A boolean.
         */
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof DialPointer.Pin)) {
                return false;
            }
            DialPointer.Pin that = (DialPointer.Pin) obj;
            if (!PaintUtilities.equal(this.paint, that.paint)) {
                return false;
            }
            if (!this.stroke.equals(that.stroke)) {
                return false;
            }
            return super.equals(obj);
        }

        /**
         * Returns a hash code for this instance.
         *
         * @return A hash code.
         */
        public int hashCode() {
            int result = super.hashCode();
            result = HashUtilities.hashCode(result, this.paint);
            result = HashUtilities.hashCode(result, this.stroke);
            return result;
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
            SerialUtilities.writePaint(this.paint, stream);
            SerialUtilities.writeStroke(this.stroke, stream);
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
            this.paint = SerialUtilities.readPaint(stream);
            this.stroke = SerialUtilities.readStroke(stream);
        }

    }

    /**
     * A dial pointer.
     */
    public static class Pointer extends DialPointer {

        /** For serialization. */
        static final long serialVersionUID = -4180500011963176960L;

        /**
         * The radius that defines the width of the pointer at the base.
         */
        private double widthRadius;

        /**
         * The fill paint.
         *
         * @since 1.0.8
         */
        private transient Paint fillPaint;

        /**
         * The outline paint.
         *
         * @since 1.0.8
         */
        private transient Paint outlinePaint;

        /**
         * Creates a new instance.
         */
        public Pointer() {
            this(0);
        }

        /**
         * Creates a new instance.
         *
         * @param datasetIndex  the dataset index.
         */
        public Pointer(int datasetIndex) {
            super(datasetIndex);
            this.widthRadius = 0.05;
            this.fillPaint = Color.gray;
            this.outlinePaint = Color.black;
        }

        /**
         * Returns the width radius.
         *
         * @return The width radius.
         *
         * @see #setWidthRadius(double)
         */
        public double getWidthRadius() {
            return this.widthRadius;
        }

        /**
         * Sets the width radius and sends a {@link DialLayerChangeEvent} to
         * all registered listeners.
         *
         * @param radius  the radius
         *
         * @see #getWidthRadius()
         */
        public void setWidthRadius(double radius) {
            this.widthRadius = radius;
            notifyListeners(new DialLayerChangeEvent(this));
        }

        /**
         * Returns the fill paint.
         *
         * @return The paint (never <code>null</code>).
         *
         * @see #setFillPaint(Paint)
         *
         * @since 1.0.8
         */
        public Paint getFillPaint() {
            return this.fillPaint;
        }

        /**
         * Sets the fill paint and sends a {@link DialLayerChangeEvent} to all
         * registered listeners.
         *
         * @param paint  the paint (<code>null</code> not permitted).
         *
         * @see #getFillPaint()
         *
         * @since 1.0.8
         */
        public void setFillPaint(Paint paint) {
            if (paint == null) {
                throw new IllegalArgumentException("Null 'paint' argument.");
            }
            this.fillPaint = paint;
            notifyListeners(new DialLayerChangeEvent(this));
        }

        /**
         * Returns the outline paint.
         *
         * @return The paint (never <code>null</code>).
         *
         * @see #setOutlinePaint(Paint)
         *
         * @since 1.0.8
         */
        public Paint getOutlinePaint() {
            return this.outlinePaint;
        }

        /**
         * Sets the outline paint and sends a {@link DialLayerChangeEvent} to
         * all registered listeners.
         *
         * @param paint  the paint (<code>null</code> not permitted).
         *
         * @see #getOutlinePaint()
         *
         * @since 1.0.8
         */
        public void setOutlinePaint(Paint paint) {
            if (paint == null) {
                throw new IllegalArgumentException("Null 'paint' argument.");
            }
            this.outlinePaint = paint;
            notifyListeners(new DialLayerChangeEvent(this));
        }

        /**
         * Draws the pointer.
         *
         * @param g2  the graphics target.
         * @param plot  the plot.
         * @param frame  the dial's reference frame.
         * @param view  the dial's view.
         */
        public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame,
                Rectangle2D view) {

            g2.setPaint(Color.blue);
            g2.setStroke(new BasicStroke(1.0f));
            Rectangle2D lengthRect = DialPlot.rectangleByRadius(frame,
                    this.radius, this.radius);
            Rectangle2D widthRect = DialPlot.rectangleByRadius(frame,
                    this.widthRadius, this.widthRadius);
            double value = plot.getValue(this.datasetIndex);
            DialScale scale = plot.getScaleForDataset(this.datasetIndex);
            double angle = scale.valueToAngle(value);

            Arc2D arc1 = new Arc2D.Double(lengthRect, angle, 0, Arc2D.OPEN);
            Point2D pt1 = arc1.getEndPoint();
            Arc2D arc2 = new Arc2D.Double(widthRect, angle - 90.0, 180.0,
                    Arc2D.OPEN);
            Point2D pt2 = arc2.getStartPoint();
            Point2D pt3 = arc2.getEndPoint();
            Arc2D arc3 = new Arc2D.Double(widthRect, angle - 180.0, 0.0,
                    Arc2D.OPEN);
            Point2D pt4 = arc3.getStartPoint();

            GeneralPath gp = new GeneralPath();
            gp.moveTo((float) pt1.getX(), (float) pt1.getY());
            gp.lineTo((float) pt2.getX(), (float) pt2.getY());
            gp.lineTo((float) pt4.getX(), (float) pt4.getY());
            gp.lineTo((float) pt3.getX(), (float) pt3.getY());
            gp.closePath();
            g2.setPaint(this.fillPaint);
            g2.fill(gp);

            g2.setPaint(this.outlinePaint);
            Line2D line = new Line2D.Double(frame.getCenterX(),
                    frame.getCenterY(), pt1.getX(), pt1.getY());
            g2.draw(line);

            line.setLine(pt2, pt3);
            g2.draw(line);

            line.setLine(pt3, pt1);
            g2.draw(line);

            line.setLine(pt2, pt1);
            g2.draw(line);

            line.setLine(pt2, pt4);
            g2.draw(line);

            line.setLine(pt3, pt4);
            g2.draw(line);
        }

        /**
         * Tests this pointer for equality with an arbitrary object.
         *
         * @param obj  the object (<code>null</code> permitted).
         *
         * @return A boolean.
         */
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof DialPointer.Pointer)) {
                return false;
            }
            DialPointer.Pointer that = (DialPointer.Pointer) obj;

            if (this.widthRadius != that.widthRadius) {
                return false;
            }
            if (!PaintUtilities.equal(this.fillPaint, that.fillPaint)) {
                return false;
            }
            if (!PaintUtilities.equal(this.outlinePaint, that.outlinePaint)) {
                return false;
            }
            return super.equals(obj);
        }

        /**
         * Returns a hash code for this instance.
         *
         * @return A hash code.
         */
        public int hashCode() {
            int result = super.hashCode();
            result = HashUtilities.hashCode(result, this.widthRadius);
            result = HashUtilities.hashCode(result, this.fillPaint);
            result = HashUtilities.hashCode(result, this.outlinePaint);
            return result;
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
            SerialUtilities.writePaint(this.fillPaint, stream);
            SerialUtilities.writePaint(this.outlinePaint, stream);
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
            this.fillPaint = SerialUtilities.readPaint(stream);
            this.outlinePaint = SerialUtilities.readPaint(stream);
        }

    }

}
