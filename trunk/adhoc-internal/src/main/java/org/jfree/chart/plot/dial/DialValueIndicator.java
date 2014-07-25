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
 * DialValueIndicator.java
 * -----------------------
 * (C) Copyright 2006-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 03-Nov-2006 : Version 1 (DG);
 * 17-Oct-2007 : Updated equals() (DG);
 * 24-Oct-2007 : Added default constructor and missing event notification (DG);
 *
 */

package org.jfree.chart.plot.dial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.jfree.chart.HashUtilities;
import org.jfree.io.SerialUtilities;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.Size2D;
import org.jfree.ui.TextAnchor;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A value indicator for a {@link DialPlot}.
 *
 * @since 1.0.7
 */
public class DialValueIndicator extends AbstractDialLayer implements DialLayer,
        Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    static final long serialVersionUID = 803094354130942585L;

    /** The dataset index. */
    private int datasetIndex;

    /** The angle that defines the anchor point. */
    private double angle;

    /** The radius that defines the anchor point. */
    private double radius;

    /** The frame anchor. */
    private RectangleAnchor frameAnchor;

    /** The template value. */
    private Number templateValue;

    /** The formatter. */
    private NumberFormat formatter;

    /** The font. */
    private Font font;

    /** The paint. */
    private transient Paint paint;

    /** The background paint. */
    private transient Paint backgroundPaint;

    /** The outline stroke. */
    private transient Stroke outlineStroke;

    /** The outline paint. */
    private transient Paint outlinePaint;

    /** The insets. */
    private RectangleInsets insets;

    /** The value anchor. */
    private RectangleAnchor valueAnchor;

    /** The text anchor for displaying the value. */
    private TextAnchor textAnchor;

    /**
     * Creates a new instance of <code>DialValueIndicator</code>.
     */
    public DialValueIndicator() {
        this(0);
    }

    /**
     * Creates a new instance of <code>DialValueIndicator</code>.
     *
     * @param datasetIndex  the dataset index.
     */
    public DialValueIndicator(int datasetIndex) {
        this.datasetIndex = datasetIndex;
        this.angle = -90.0;
        this.radius = 0.3;
        this.frameAnchor = RectangleAnchor.CENTER;
        this.templateValue = new Double(100.0);
        this.formatter = new DecimalFormat("0.0");
        this.font = new Font("Dialog", Font.BOLD, 14);
        this.paint = Color.black;
        this.backgroundPaint = Color.white;
        this.outlineStroke = new BasicStroke(1.0f);
        this.outlinePaint = Color.blue;
        this.insets = new RectangleInsets(4, 4, 4, 4);
        this.valueAnchor = RectangleAnchor.RIGHT;
        this.textAnchor = TextAnchor.CENTER_RIGHT;
    }

    /**
     * Returns the index of the dataset from which this indicator fetches its
     * current value.
     *
     * @return The dataset index.
     *
     * @see #setDatasetIndex(int)
     */
    public int getDatasetIndex() {
        return this.datasetIndex;
    }

    /**
     * Sets the dataset index and sends a {@link DialLayerChangeEvent} to all
     * registered listeners.
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
     * Returns the angle for the anchor point.  The angle is specified in
     * degrees using the same orientation as Java's <code>Arc2D</code> class.
     *
     * @return The angle (in degrees).
     *
     * @see #setAngle(double)
     */
    public double getAngle() {
        return this.angle;
    }

    /**
     * Sets the angle for the anchor point and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param angle  the angle (in degrees).
     *
     * @see #getAngle()
     */
    public void setAngle(double angle) {
        this.angle = angle;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the radius.
     *
     * @return The radius.
     *
     * @see #setRadius(double)
     */
    public double getRadius() {
        return this.radius;
    }

    /**
     * Sets the radius and sends a {@link DialLayerChangeEvent} to all
     * registered listeners.
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
     * Returns the frame anchor.
     *
     * @return The frame anchor.
     *
     * @see #setFrameAnchor(RectangleAnchor)
     */
    public RectangleAnchor getFrameAnchor() {
        return this.frameAnchor;
    }

    /**
     * Sets the frame anchor and sends a {@link DialLayerChangeEvent} to all
     * registered listeners.
     *
     * @param anchor  the anchor (<code>null</code> not permitted).
     *
     * @see #getFrameAnchor()
     */
    public void setFrameAnchor(RectangleAnchor anchor) {
        if (anchor == null) {
            throw new IllegalArgumentException("Null 'anchor' argument.");
        }
        this.frameAnchor = anchor;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the template value.
     *
     * @return The template value (never <code>null</code>).
     *
     * @see #setTemplateValue(Number)
     */
    public Number getTemplateValue() {
        return this.templateValue;
    }

    /**
     * Sets the template value and sends a {@link DialLayerChangeEvent} to
     * all registered listeners.
     *
     * @param value  the value (<code>null</code> not permitted).
     *
     * @see #setTemplateValue(Number)
     */
    public void setTemplateValue(Number value) {
        if (value == null) {
            throw new IllegalArgumentException("Null 'value' argument.");
        }
        this.templateValue = value;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the formatter used to format the value.
     *
     * @return The formatter (never <code>null</code>).
     *
     * @see #setNumberFormat(NumberFormat)
     */
    public NumberFormat getNumberFormat() {
        return this.formatter;
    }

    /**
     * Sets the formatter used to format the value and sends a
     * {@link DialLayerChangeEvent} to all registered listeners.
     *
     * @param formatter  the formatter (<code>null</code> not permitted).
     *
     * @see #getNumberFormat()
     */
    public void setNumberFormat(NumberFormat formatter) {
        if (formatter == null) {
            throw new IllegalArgumentException("Null 'formatter' argument.");
        }
        this.formatter = formatter;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the font.
     *
     * @return The font (never <code>null</code>).
     *
     * @see #getFont()
     */
    public Font getFont() {
        return this.font;
    }

    /**
     * Sets the font and sends a {@link DialLayerChangeEvent} to all registered
     * listeners.
     *
     * @param font  the font (<code>null</code> not permitted).
     */
    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }
        this.font = font;
        notifyListeners(new DialLayerChangeEvent(this));
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
     * Returns the background paint.
     *
     * @return The background paint.
     *
     * @see #setBackgroundPaint(Paint)
     */
    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }

    /**
     * Sets the background paint and sends a {@link DialLayerChangeEvent} to
     * all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getBackgroundPaint()
     */
    public void setBackgroundPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.backgroundPaint = paint;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the outline stroke.
     *
     * @return The outline stroke (never <code>null</code>).
     *
     * @see #setOutlineStroke(Stroke)
     */
    public Stroke getOutlineStroke() {
        return this.outlineStroke;
    }

    /**
     * Sets the outline stroke and sends a {@link DialLayerChangeEvent} to
     * all registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     *
     * @see #getOutlineStroke()
     */
    public void setOutlineStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.outlineStroke = stroke;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the outline paint.
     *
     * @return The outline paint (never <code>null</code>).
     *
     * @see #setOutlinePaint(Paint)
     */
    public Paint getOutlinePaint() {
        return this.outlinePaint;
    }

    /**
     * Sets the outline paint and sends a {@link DialLayerChangeEvent} to all
     * registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getOutlinePaint()
     */
    public void setOutlinePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.outlinePaint = paint;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the insets.
     *
     * @return The insets (never <code>null</code>).
     *
     * @see #setInsets(RectangleInsets)
     */
    public RectangleInsets getInsets() {
        return this.insets;
    }

    /**
     * Sets the insets and sends a {@link DialLayerChangeEvent} to all
     * registered listeners.
     *
     * @param insets  the insets (<code>null</code> not permitted).
     *
     * @see #getInsets()
     */
    public void setInsets(RectangleInsets insets) {
        if (insets == null) {
            throw new IllegalArgumentException("Null 'insets' argument.");
        }
        this.insets = insets;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the value anchor.
     *
     * @return The value anchor (never <code>null</code>).
     *
     * @see #setValueAnchor(RectangleAnchor)
     */
    public RectangleAnchor getValueAnchor() {
        return this.valueAnchor;
    }

    /**
     * Sets the value anchor and sends a {@link DialLayerChangeEvent} to all
     * registered listeners.
     *
     * @param anchor  the anchor (<code>null</code> not permitted).
     *
     * @see #getValueAnchor()
     */
    public void setValueAnchor(RectangleAnchor anchor) {
        if (anchor == null) {
            throw new IllegalArgumentException("Null 'anchor' argument.");
        }
        this.valueAnchor = anchor;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    /**
     * Returns the text anchor.
     *
     * @return The text anchor (never <code>null</code>).
     *
     * @see #setTextAnchor(TextAnchor)
     */
    public TextAnchor getTextAnchor() {
        return this.textAnchor;
    }

    /**
     * Sets the text anchor and sends a {@link DialLayerChangeEvent} to all
     * registered listeners.
     *
     * @param anchor  the anchor (<code>null</code> not permitted).
     *
     * @see #getTextAnchor()
     */
    public void setTextAnchor(TextAnchor anchor) {
        if (anchor == null) {
            throw new IllegalArgumentException("Null 'anchor' argument.");
        }
        this.textAnchor = anchor;
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

        // calculate the bounds of the template value
        FontMetrics fm = g2.getFontMetrics(this.font);
        String s = this.formatter.format(this.templateValue);
        Rectangle2D tb = TextUtilities.getTextBounds(s, g2, fm);

        // align this rectangle to the frameAnchor
        Rectangle2D bounds = RectangleAnchor.createRectangle(new Size2D(
                tb.getWidth(), tb.getHeight()), pt.getX(), pt.getY(),
                this.frameAnchor);

        // add the insets
        Rectangle2D fb = this.insets.createOutsetRectangle(bounds);

        // draw the background
        g2.setPaint(this.backgroundPaint);
        g2.fill(fb);

        // draw the border
        g2.setStroke(this.outlineStroke);
        g2.setPaint(this.outlinePaint);
        g2.draw(fb);


        // now find the text anchor point
        double value = plot.getValue(this.datasetIndex);
        String valueStr = this.formatter.format(value);
        Point2D pt2 = RectangleAnchor.coordinates(bounds, this.valueAnchor);
        g2.setPaint(this.paint);
        g2.setFont(this.font);
        TextUtilities.drawAlignedString(valueStr, g2, (float) pt2.getX(),
                (float) pt2.getY(), this.textAnchor);

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
        if (!(obj instanceof DialValueIndicator)) {
            return false;
        }
        DialValueIndicator that = (DialValueIndicator) obj;
        if (this.datasetIndex != that.datasetIndex) {
            return false;
        }
        if (this.angle != that.angle) {
            return false;
        }
        if (this.radius != that.radius) {
            return false;
        }
        if (!this.frameAnchor.equals(that.frameAnchor)) {
            return false;
        }
        if (!this.templateValue.equals(that.templateValue)) {
            return false;
        }
        if (!this.font.equals(that.font)) {
            return false;
        }
        if (!PaintUtilities.equal(this.paint, that.paint)) {
            return false;
        }
        if (!PaintUtilities.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;
        }
        if (!this.outlineStroke.equals(that.outlineStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
        }
        if (!this.insets.equals(that.insets)) {
            return false;
        }
        if (!this.valueAnchor.equals(that.valueAnchor)) {
            return false;
        }
        if (!this.textAnchor.equals(that.textAnchor)) {
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
        result = 37 * result + HashUtilities.hashCodeForPaint(
                this.backgroundPaint);
        result = 37 * result + HashUtilities.hashCodeForPaint(
                this.outlinePaint);
        result = 37 * result + this.outlineStroke.hashCode();
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
        SerialUtilities.writePaint(this.backgroundPaint, stream);
        SerialUtilities.writePaint(this.outlinePaint, stream);
        SerialUtilities.writeStroke(this.outlineStroke, stream);
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
        this.backgroundPaint = SerialUtilities.readPaint(stream);
        this.outlinePaint = SerialUtilities.readPaint(stream);
        this.outlineStroke = SerialUtilities.readStroke(stream);
    }

}
