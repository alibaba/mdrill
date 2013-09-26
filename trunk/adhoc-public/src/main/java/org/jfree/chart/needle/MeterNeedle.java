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
 * MeterNeedle.java
 * ----------------
 * (C) Copyright 2002-2008, by the Australian Antarctic Division and
 *                          Contributors.
 *
 * Original Author:  Bryan Scott (for the Australian Antarctic Division);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Nicolas Brodu (for Astrium and EADS Corporate Research
 *                   Center);
 *
 * Changes:
 * --------
 * 25-Sep-2002 : Version 1, contributed by Bryan Scott (DG);
 * 07-Nov-2002 : Fixed errors reported by Checkstyle (DG);
 * 01-Sep-2003 : Implemented Serialization (NB);
 * 16-Mar-2004 : Changed transform from private to protected (BRS);
 * 08-Jun-2005 : Fixed equals() method to handle GradientPaint (DG);
 * 22-Nov-2007 : Implemented hashCode() (DG);
 *
 */

package org.jfree.chart.needle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.HashUtilities;
import org.jfree.io.SerialUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;

/**
 * The base class used to represent the needle on a
 * {@link org.jfree.chart.plot.CompassPlot}.
 */
public abstract class MeterNeedle implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 5203064851510951052L;

    /** The outline paint. */
    private transient Paint outlinePaint = Color.black;

    /** The outline stroke. */
    private transient Stroke outlineStroke = new BasicStroke(2);

    /** The fill paint. */
    private transient Paint fillPaint = null;

    /** The highlight paint. */
    private transient Paint highlightPaint = null;

    /** The size. */
    private int size = 5;

    /** Scalar to aply to locate the rotation x point. */
    private double rotateX = 0.5;

    /** Scalar to aply to locate the rotation y point. */
    private double rotateY = 0.5;

    /** A transform. */
    protected static AffineTransform transform = new AffineTransform();

    /**
     * Creates a new needle.
     */
    public MeterNeedle() {
        this(null, null, null);
    }

    /**
     * Creates a new needle.
     *
     * @param outline  the outline paint (<code>null</code> permitted).
     * @param fill  the fill paint (<code>null</code> permitted).
     * @param highlight  the highlight paint (<code>null</code> permitted).
     */
    public MeterNeedle(Paint outline, Paint fill, Paint highlight) {
        this.fillPaint = fill;
        this.highlightPaint = highlight;
        this.outlinePaint = outline;
    }

    /**
     * Returns the outline paint.
     *
     * @return The outline paint.
     */
    public Paint getOutlinePaint() {
        return this.outlinePaint;
    }

    /**
     * Sets the outline paint.
     *
     * @param p  the new paint.
     */
    public void setOutlinePaint(Paint p) {
        if (p != null) {
            this.outlinePaint = p;
        }
    }

    /**
     * Returns the outline stroke.
     *
     * @return The outline stroke.
     */
    public Stroke getOutlineStroke() {
        return this.outlineStroke;
    }

    /**
     * Sets the outline stroke.
     *
     * @param s  the new stroke.
     */
    public void setOutlineStroke(Stroke s) {
        if (s != null) {
            this.outlineStroke = s;
        }
    }

    /**
     * Returns the fill paint.
     *
     * @return The fill paint.
     */
    public Paint getFillPaint() {
        return this.fillPaint;
    }

    /**
     * Sets the fill paint.
     *
     * @param p  the fill paint.
     */
    public void setFillPaint(Paint p) {
        if (p != null) {
            this.fillPaint = p;
        }
    }

    /**
     * Returns the highlight paint.
     *
     * @return The highlight paint.
     */
    public Paint getHighlightPaint() {
        return this.highlightPaint;
    }

    /**
     * Sets the highlight paint.
     *
     * @param p  the highlight paint.
     */
    public void setHighlightPaint(Paint p) {
        if (p != null) {
            this.highlightPaint = p;
        }
    }

    /**
     * Returns the scalar used for determining the rotation x value.
     *
     * @return The x rotate scalar.
     */
    public double getRotateX() {
        return this.rotateX;
    }

    /**
     * Sets the rotateX value.
     *
     * @param x  the new value.
     */
    public void setRotateX(double x) {
        this.rotateX = x;
    }

    /**
     * Sets the rotateY value.
     *
     * @param y  the new value.
     */
    public void setRotateY(double y) {
        this.rotateY = y;
    }

    /**
     * Returns the scalar used for determining the rotation y value.
     *
     * @return The y rotate scalar.
     */
    public double getRotateY() {
        return this.rotateY;
    }

    /**
     * Draws the needle.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     */
    public void draw(Graphics2D g2, Rectangle2D plotArea) {
        draw(g2, plotArea, 0);
    }

    /**
     * Draws the needle.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param angle  the angle.
     */
    public void draw(Graphics2D g2, Rectangle2D plotArea, double angle) {

        Point2D.Double pt = new Point2D.Double();
        pt.setLocation(
            plotArea.getMinX() + this.rotateX * plotArea.getWidth(),
            plotArea.getMinY() + this.rotateY * plotArea.getHeight()
        );
        draw(g2, plotArea, pt, angle);

    }

    /**
     * Draws the needle.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param rotate  the rotation point.
     * @param angle  the angle.
     */
    public void draw(Graphics2D g2, Rectangle2D plotArea, Point2D rotate,
                     double angle) {

        Paint savePaint = g2.getColor();
        Stroke saveStroke = g2.getStroke();

        drawNeedle(g2, plotArea, rotate, Math.toRadians(angle));

        g2.setStroke(saveStroke);
        g2.setPaint(savePaint);

    }

    /**
     * Draws the needle.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param rotate  the rotation point.
     * @param angle  the angle.
     */
    protected abstract void drawNeedle(Graphics2D g2,
                                       Rectangle2D plotArea, Point2D rotate,
                                       double angle);

    /**
     * Displays a shape.
     *
     * @param g2  the graphics device.
     * @param shape  the shape.
     */
    protected void defaultDisplay(Graphics2D g2, Shape shape) {

        if (this.fillPaint != null) {
            g2.setPaint(this.fillPaint);
            g2.fill(shape);
        }

        if (this.outlinePaint != null) {
            g2.setStroke(this.outlineStroke);
            g2.setPaint(this.outlinePaint);
            g2.draw(shape);
        }

    }

    /**
     * Returns the size.
     *
     * @return The size.
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Sets the size.
     *
     * @param pixels  the new size.
     */
    public void setSize(int pixels) {
        this.size = pixels;
    }

    /**
     * Returns the transform.
     *
     * @return The transform.
     */
    public AffineTransform getTransform() {
        return MeterNeedle.transform;
    }

    /**
     * Tests another object for equality with this object.
     *
     * @param obj the object to test (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MeterNeedle)) {
            return false;
        }
        MeterNeedle that = (MeterNeedle) obj;
        if (!PaintUtilities.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.outlineStroke, that.outlineStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.fillPaint, that.fillPaint)) {
            return false;
        }
        if (!PaintUtilities.equal(this.highlightPaint, that.highlightPaint)) {
            return false;
        }
        if (this.size != that.size) {
            return false;
        }
        if (this.rotateX != that.rotateX) {
            return false;
        }
        if (this.rotateY != that.rotateY) {
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
        int result = HashUtilities.hashCode(193, this.fillPaint);
        result = HashUtilities.hashCode(result, this.highlightPaint);
        result = HashUtilities.hashCode(result, this.outlinePaint);
        result = HashUtilities.hashCode(result, this.outlineStroke);
        result = HashUtilities.hashCode(result, this.rotateX);
        result = HashUtilities.hashCode(result, this.rotateY);
        result = HashUtilities.hashCode(result, this.size);
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
        SerialUtilities.writeStroke(this.outlineStroke, stream);
        SerialUtilities.writePaint(this.outlinePaint, stream);
        SerialUtilities.writePaint(this.fillPaint, stream);
        SerialUtilities.writePaint(this.highlightPaint, stream);
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
        this.outlineStroke = SerialUtilities.readStroke(stream);
        this.outlinePaint = SerialUtilities.readPaint(stream);
        this.fillPaint = SerialUtilities.readPaint(stream);
        this.highlightPaint = SerialUtilities.readPaint(stream);
    }

}
