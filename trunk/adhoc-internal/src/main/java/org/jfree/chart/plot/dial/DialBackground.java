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
 * -------------------
 * DialBackground.java
 * -------------------
 * (C) Copyright 2006-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 03-Nov-2006 : Version 1 (DG);
 * 16-Oct-2007 : The equals() method needs to call super.equals() (DG);
 *
 */

package org.jfree.chart.plot.dial;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.HashUtilities;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A regular dial layer that can be used to draw the background for a dial.
 *
 * @since 1.0.7
 */
public class DialBackground extends AbstractDialLayer implements DialLayer,
        Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    static final long serialVersionUID = -9019069533317612375L;

    /**
     * The background paint.  This field is transient because serialization
     * requires special handling.
     */
    private transient Paint paint;

    /**
     * The transformer used when the background paint is an instance of
     * <code>GradientPaint</code>.
     */
    private GradientPaintTransformer gradientPaintTransformer;

    /**
     * Creates a new instance of <code>DialBackground</code>.  The
     * default background paint is <code>Color.white</code>.
     */
    public DialBackground() {
        this(Color.white);
    }

    /**
     * Creates a new instance of <code>DialBackground</code>.  The
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @throws IllegalArgumentException if <code>paint</code> is
     *     <code>null</code>.
     */
    public DialBackground(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.paint = paint;
        this.gradientPaintTransformer = new StandardGradientPaintTransformer();
    }

    /**
     * Returns the paint used to fill the background.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setPaint(Paint)
     */
    public Paint getPaint() {
        return this.paint;
    }

    /**
     * Sets the paint for the dial background and sends a
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
     * Returns the transformer used to adjust the coordinates of any
     * <code>GradientPaint</code> instance used for the background paint.
     *
     * @return The transformer (never <code>null</code>).
     *
     * @see #setGradientPaintTransformer(GradientPaintTransformer)
     */
    public GradientPaintTransformer getGradientPaintTransformer() {
        return this.gradientPaintTransformer;
    }

    /**
     * Sets the transformer used to adjust the coordinates of any
     * <code>GradientPaint</code> instance used for the background paint, and
     * sends a {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param t  the transformer (<code>null</code> not permitted).
     *
     * @see #getGradientPaintTransformer()
     */
    public void setGradientPaintTransformer(GradientPaintTransformer t) {
        if (t == null) {
            throw new IllegalArgumentException("Null 't' argument.");
        }
        this.gradientPaintTransformer = t;
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

        Paint p = this.paint;
        if (p instanceof GradientPaint) {
            p = this.gradientPaintTransformer.transform((GradientPaint) p,
                    view);
        }
        g2.setPaint(p);
        g2.fill(view);
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
        if (!(obj instanceof DialBackground)) {
            return false;
        }
        DialBackground that = (DialBackground) obj;
        if (!PaintUtilities.equal(this.paint, that.paint)) {
            return false;
        }
        if (!this.gradientPaintTransformer.equals(
                that.gradientPaintTransformer)) {
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
        result = 37 * result + this.gradientPaintTransformer.hashCode();
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
