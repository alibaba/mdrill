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
 * ---------------------------
 * CategoryLineAnnotation.java
 * ---------------------------
 * (C) Copyright 2005-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 29-Jul-2005 : Version 1, based on CategoryTextAnnotation (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 06-Mar-2007 : Reimplemented hashCode() (DG);
 * 23-Apr-2008 : Implemented PublicCloneable (DG);
 *
 */

package org.jfree.chart.annotations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.HashUtilities;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A line annotation that can be placed on a {@link CategoryPlot}.
 */
public class CategoryLineAnnotation implements CategoryAnnotation,
        Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    static final long serialVersionUID = 3477740483341587984L;

    /** The category for the start of the line. */
    private Comparable category1;

    /** The value for the start of the line. */
    private double value1;

    /** The category for the end of the line. */
    private Comparable category2;

    /** The value for the end of the line. */
    private double value2;

    /** The line color. */
    private transient Paint paint = Color.black;

    /** The line stroke. */
    private transient Stroke stroke = new BasicStroke(1.0f);

    /**
     * Creates a new annotation that draws a line between (category1, value1)
     * and (category2, value2).
     *
     * @param category1  the category (<code>null</code> not permitted).
     * @param value1  the value.
     * @param category2  the category (<code>null</code> not permitted).
     * @param value2  the value.
     * @param paint  the line color (<code>null</code> not permitted).
     * @param stroke  the line stroke (<code>null</code> not permitted).
     */
    public CategoryLineAnnotation(Comparable category1, double value1,
                                  Comparable category2, double value2,
                                  Paint paint, Stroke stroke) {
        if (category1 == null) {
            throw new IllegalArgumentException("Null 'category1' argument.");
        }
        if (category2 == null) {
            throw new IllegalArgumentException("Null 'category2' argument.");
        }
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.category1 = category1;
        this.value1 = value1;
        this.category2 = category2;
        this.value2 = value2;
        this.paint = paint;
        this.stroke = stroke;
    }

    /**
     * Returns the category for the start of the line.
     *
     * @return The category for the start of the line (never <code>null</code>).
     *
     * @see #setCategory1(Comparable)
     */
    public Comparable getCategory1() {
        return this.category1;
    }

    /**
     * Sets the category for the start of the line.
     *
     * @param category  the category (<code>null</code> not permitted).
     *
     * @see #getCategory1()
     */
    public void setCategory1(Comparable category) {
        if (category == null) {
            throw new IllegalArgumentException("Null 'category' argument.");
        }
        this.category1 = category;
    }

    /**
     * Returns the y-value for the start of the line.
     *
     * @return The y-value for the start of the line.
     *
     * @see #setValue1(double)
     */
    public double getValue1() {
        return this.value1;
    }

    /**
     * Sets the y-value for the start of the line.
     *
     * @param value  the value.
     *
     * @see #getValue1()
     */
    public void setValue1(double value) {
        this.value1 = value;
    }

    /**
     * Returns the category for the end of the line.
     *
     * @return The category for the end of the line (never <code>null</code>).
     *
     * @see #setCategory2(Comparable)
     */
    public Comparable getCategory2() {
        return this.category2;
    }

    /**
     * Sets the category for the end of the line.
     *
     * @param category  the category (<code>null</code> not permitted).
     *
     * @see #getCategory2()
     */
    public void setCategory2(Comparable category) {
        if (category == null) {
            throw new IllegalArgumentException("Null 'category' argument.");
        }
        this.category2 = category;
    }

    /**
     * Returns the y-value for the end of the line.
     *
     * @return The y-value for the end of the line.
     *
     * @see #setValue2(double)
     */
    public double getValue2() {
        return this.value2;
    }

    /**
     * Sets the y-value for the end of the line.
     *
     * @param value  the value.
     *
     * @see #getValue2()
     */
    public void setValue2(double value) {
        this.value2 = value;
    }

    /**
     * Returns the paint used to draw the connecting line.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setPaint(Paint)
     */
    public Paint getPaint() {
        return this.paint;
    }

    /**
     * Sets the paint used to draw the connecting line.
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
    }

    /**
     * Returns the stroke used to draw the connecting line.
     *
     * @return The stroke (never <code>null</code>).
     *
     * @see #setStroke(Stroke)
     */
    public Stroke getStroke() {
        return this.stroke;
    }

    /**
     * Sets the stroke used to draw the connecting line.
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
    }

    /**
     * Draws the annotation.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param dataArea  the data area.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     */
    public void draw(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea,
                     CategoryAxis domainAxis, ValueAxis rangeAxis) {

        CategoryDataset dataset = plot.getDataset();
        int catIndex1 = dataset.getColumnIndex(this.category1);
        int catIndex2 = dataset.getColumnIndex(this.category2);
        int catCount = dataset.getColumnCount();

        double lineX1 = 0.0f;
        double lineY1 = 0.0f;
        double lineX2 = 0.0f;
        double lineY2 = 0.0f;
        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
            plot.getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
            plot.getRangeAxisLocation(), orientation);

        if (orientation == PlotOrientation.HORIZONTAL) {
            lineY1 = domainAxis.getCategoryJava2DCoordinate(
                CategoryAnchor.MIDDLE, catIndex1, catCount, dataArea,
                domainEdge);
            lineX1 = rangeAxis.valueToJava2D(this.value1, dataArea, rangeEdge);
            lineY2 = domainAxis.getCategoryJava2DCoordinate(
                CategoryAnchor.MIDDLE, catIndex2, catCount, dataArea,
                domainEdge);
            lineX2 = rangeAxis.valueToJava2D(this.value2, dataArea, rangeEdge);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            lineX1 = domainAxis.getCategoryJava2DCoordinate(
                CategoryAnchor.MIDDLE, catIndex1, catCount, dataArea,
                domainEdge);
            lineY1 = rangeAxis.valueToJava2D(this.value1, dataArea, rangeEdge);
            lineX2 = domainAxis.getCategoryJava2DCoordinate(
                CategoryAnchor.MIDDLE, catIndex2, catCount, dataArea,
                domainEdge);
            lineY2 = rangeAxis.valueToJava2D(this.value2, dataArea, rangeEdge);
        }
        g2.setPaint(this.paint);
        g2.setStroke(this.stroke);
        g2.drawLine((int) lineX1, (int) lineY1, (int) lineX2, (int) lineY2);
    }

    /**
     * Tests this object for equality with another.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CategoryLineAnnotation)) {
            return false;
        }
        CategoryLineAnnotation that = (CategoryLineAnnotation) obj;
        if (!this.category1.equals(that.getCategory1())) {
            return false;
        }
        if (this.value1 != that.getValue1()) {
            return false;
        }
        if (!this.category2.equals(that.getCategory2())) {
            return false;
        }
        if (this.value2 != that.getValue2()) {
            return false;
        }
        if (!PaintUtilities.equal(this.paint, that.paint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.stroke, that.stroke)) {
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
        int result = 193;
        result = 37 * result + this.category1.hashCode();
        long temp = Double.doubleToLongBits(this.value1);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        result = 37 * result + this.category2.hashCode();
        temp = Double.doubleToLongBits(this.value2);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        result = 37 * result + HashUtilities.hashCodeForPaint(this.paint);
        result = 37 * result + this.stroke.hashCode();
        return result;
    }

    /**
     * Returns a clone of the annotation.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  this class will not throw this
     *         exception, but subclasses (if any) might.
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
