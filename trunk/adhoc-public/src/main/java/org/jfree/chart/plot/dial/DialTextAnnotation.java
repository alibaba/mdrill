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
 * DialTextAnnotation.java
 * -----------------------
 * (C) Copyright 2006-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 03-Nov-2006 : Version 1 (DG);
 * 08-Mar-2007 : Fix in hashCode() (DG);
 * 17-Oct-2007 : Updated equals() (DG);
 * 24-Oct-2007 : Added getAnchor() and setAnchor() methods (DG);
 *
 */

package org.jfree.chart.plot.dial;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.HashUtilities;
import org.jfree.io.SerialUtilities;
import org.jfree.text.TextUtilities;
import org.jfree.ui.TextAnchor;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A text annotation for a {@link DialPlot}.
 *
 * @since 1.0.7
 */
public class DialTextAnnotation extends AbstractDialLayer implements DialLayer,
        Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    static final long serialVersionUID = 3065267524054428071L;

    /** The label text. */
    private String label;

    /** The font. */
    private Font font;

    /**
     * The paint for the label.  This field is transient because it requires
     * special handling for serialization.
     */
    private transient Paint paint;

    /** The angle that defines the anchor point for the annotation. */
    private double angle;

    /** The radius that defines the anchor point for the annotation. */
    private double radius;

    /** The text anchor to be aligned to the annotation's anchor point. */
    private TextAnchor anchor;

    /**
     * Creates a new instance of <code>DialTextAnnotation</code>.
     *
     * @param label  the label (<code>null</code> not permitted).
     */
    public DialTextAnnotation(String label) {
        if (label == null) {
            throw new IllegalArgumentException("Null 'label' argument.");
        }
        this.angle = -90.0;
        this.radius = 0.3;
        this.font = new Font("Dialog", Font.BOLD, 14);
        this.paint = Color.black;
        this.label = label;
        this.anchor = TextAnchor.TOP_CENTER;
    }

    /**
     * Returns the label text.
     *
     * @return The label text (never <code>null</code).
     *
     * @see #setLabel(String)
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Sets the label and sends a {@link DialLayerChangeEvent} to all
     * registered listeners.
     *
     * @param label  the label (<code>null</code> not permitted).
     *
     * @see #getLabel()
     */
    public void setLabel(String label) {
        if (label == null) {
            throw new IllegalArgumentException("Null 'label' argument.");
        }
        this.label = label;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the font used to display the label.
     *
     * @return The font (never <code>null</code>).
     *
     * @see #setFont(Font)
     */
    public Font getFont() {
        return this.font;
    }

    /**
     * Sets the font used to display the label and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param font  the font (<code>null</code> not permitted).
     *
     * @see #getFont()
     */
    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }
        this.font = font;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the paint used to display the label.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setPaint(Paint)
     */
    public Paint getPaint() {
        return this.paint;
    }

    /**
     * Sets the paint used to display the label and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
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
     * Returns the angle used to calculate the anchor point.
     *
     * @return The angle (in degrees).
     *
     * @see #setAngle(double)
     * @see #getRadius()
     */
    public double getAngle() {
        return this.angle;
    }

    /**
     * Sets the angle used to calculate the anchor point and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param angle  the angle (in degrees).
     *
     * @see #getAngle()
     * @see #setRadius(double)
     */
    public void setAngle(double angle) {
        this.angle = angle;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the radius used to calculate the anchor point.  This is
     * specified as a percentage relative to the dial's framing rectangle.
     *
     * @return The radius.
     *
     * @see #setRadius(double)
     * @see #getAngle()
     */
    public double getRadius() {
        return this.radius;
    }

    /**
     * Sets the radius used to calculate the anchor point and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param radius  the radius (as a percentage of the dial's framing
     *                rectangle).
     *
     * @see #getRadius()
     * @see #setAngle(double)
     */
    public void setRadius(double radius) {
        if (radius < 0.0) {
            throw new IllegalArgumentException(
                    "The 'radius' cannot be negative.");
        }
        this.radius = radius;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the text anchor point that will be aligned to the position
     * specified by {@link #getAngle()} and {@link #getRadius()}.
     *
     * @return The anchor point.
     *
     * @see #setAnchor(TextAnchor)
     */
    public TextAnchor getAnchor() {
        return this.anchor;
    }

    /**
     * Sets the text anchor point and sends a {@link DialLayerChangeEvent} to
     * all registered listeners.
     *
     * @param anchor  the anchor point (<code>null</code> not permitted).
     *
     * @see #getAnchor()
     */
    public void setAnchor(TextAnchor anchor) {
        if (anchor == null) {
            throw new IllegalArgumentException("Null 'anchor' argument.");
        }
        this.anchor = anchor;
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
     * Draws the background to the specified graphics device.  If the dial
     * frame specifies a window, the clipping region will already have been
     * set to this window before this method is called.
     *
     * @param g2  the graphics device (<code>null</code> not permitted).
     * @param plot  the plot (ignored here).
     * @param frame  the dial frame (ignored here).
     * @param view  the view rectangle (<code>null</code> not permitted).
     */
    public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame,
            Rectangle2D view) {

        // work out the anchor point
        Rectangle2D f = DialPlot.rectangleByRadius(frame, this.radius,
                this.radius);
        Arc2D arc = new Arc2D.Double(f, this.angle, 0.0, Arc2D.OPEN);
        Point2D pt = arc.getStartPoint();
        g2.setPaint(this.paint);
        g2.setFont(this.font);
        TextUtilities.drawAlignedString(this.label, g2, (float) pt.getX(),
                (float) pt.getY(), this.anchor);

    }

    /**
     * Tests this instance for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DialTextAnnotation)) {
            return false;
        }
        DialTextAnnotation that = (DialTextAnnotation) obj;
        if (!this.label.equals(that.label)) {
            return false;
        }
        if (!this.font.equals(that.font)) {
            return false;
        }
        if (!PaintUtilities.equal(this.paint, that.paint)) {
            return false;
        }
        if (this.radius != that.radius) {
            return false;
        }
        if (this.angle != that.angle) {
            return false;
        }
        if (!this.anchor.equals(that.anchor)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return The hash code.
     */
    public int hashCode() {
        int result = 193;
        result = 37 * result + HashUtilities.hashCodeForPaint(this.paint);
        result = 37 * result + this.font.hashCode();
        result = 37 * result + this.label.hashCode();
        result = 37 * result + this.anchor.hashCode();
        long temp = Double.doubleToLongBits(this.angle);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.radius);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * Returns a clone of this instance.
     *
     * @return The clone.
     *
     * @throws CloneNotSupportedException if some attribute of this instance
     *     cannot be cloned.
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
        SerialUtilities.writePaint(this.paint, stream);
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
    }

}
