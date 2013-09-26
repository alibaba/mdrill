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
 * ------------------
 * MeterInterval.java
 * ------------------
 * (C) Copyright 2005-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 22-Mar-2005 : Version 1 (DG);
 * 29-Mar-2005 : Fixed serialization (DG);
 *
 */

package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.data.Range;
import org.jfree.io.SerialUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;

/**
 * An interval to be highlighted on a {@link MeterPlot}.  Instances of this
 * class are immutable.
 */
public class MeterInterval implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 1530982090622488257L;

    /** The interval label. */
    private String label;

    /** The interval range. */
    private Range range;

    /** The outline paint (used for the arc marking the interval). */
    private transient Paint outlinePaint;

    /** The outline stroke (used for the arc marking the interval). */
    private transient Stroke outlineStroke;

    /** The background paint for the interval. */
    private transient Paint backgroundPaint;

    /**
     * Creates a new interval.
     *
     * @param label  the label (<code>null</code> not permitted).
     * @param range  the range (<code>null</code> not permitted).
     */
    public MeterInterval(String label, Range range) {
        this(label, range, Color.yellow, new BasicStroke(2.0f), null);
    }

    /**
     * Creates a new interval.
     *
     * @param label  the label (<code>null</code> not permitted).
     * @param range  the range (<code>null</code> not permitted).
     * @param outlinePaint  the outline paint (<code>null</code> permitted).
     * @param outlineStroke  the outline stroke (<code>null</code> permitted).
     * @param backgroundPaint  the background paint (<code>null</code>
     *                         permitted).
     */
    public MeterInterval(String label, Range range, Paint outlinePaint,
                         Stroke outlineStroke, Paint backgroundPaint) {
        if (label == null) {
            throw new IllegalArgumentException("Null 'label' argument.");
        }
        if (range == null) {
            throw new IllegalArgumentException("Null 'range' argument.");
        }
        this.label = label;
        this.range = range;
        this.outlinePaint = outlinePaint;
        this.outlineStroke = outlineStroke;
        this.backgroundPaint = backgroundPaint;
    }

    /**
     * Returns the label.
     *
     * @return The label (never <code>null</code>).
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Returns the range.
     *
     * @return The range (never <code>null</code>).
     */
    public Range getRange() {
        return this.range;
    }

    /**
     * Returns the background paint.  If <code>null</code>, the background
     * should remain unfilled.
     *
     * @return The background paint (possibly <code>null</code>).
     */
    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }

    /**
     * Returns the outline paint.
     *
     * @return The outline paint (possibly <code>null</code>).
     */
    public Paint getOutlinePaint() {
        return this.outlinePaint;
    }

    /**
     * Returns the outline stroke.
     *
     * @return The outline stroke (possibly <code>null</code>).
     */
    public Stroke getOutlineStroke() {
        return this.outlineStroke;
    }

    /**
     * Checks this instance for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MeterInterval)) {
            return false;
        }
        MeterInterval that = (MeterInterval) obj;
        if (!this.label.equals(that.label)) {
            return false;
        }
        if (!this.range.equals(that.range)) {
            return false;
        }
        if (!PaintUtilities.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.outlineStroke, that.outlineStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;
        }
        return true;
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
        SerialUtilities.writePaint(this.outlinePaint, stream);
        SerialUtilities.writeStroke(this.outlineStroke, stream);
        SerialUtilities.writePaint(this.backgroundPaint, stream);
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
        this.outlinePaint = SerialUtilities.readPaint(stream);
        this.outlineStroke = SerialUtilities.readStroke(stream);
        this.backgroundPaint = SerialUtilities.readPaint(stream);
    }

}
