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
 * LegendGraphic.java
 * ------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 26-Oct-2004 : Version 1 (DG);
 * 21-Jan-2005 : Modified return type of RectangleAnchor.coordinates()
 *               method (DG);
 * 20-Apr-2005 : Added new draw() method (DG);
 * 13-May-2005 : Fixed to respect margin, border and padding settings (DG);
 * 01-Sep-2005 : Implemented PublicCloneable (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 13-Dec-2006 : Added fillPaintTransformer attribute, so legend graphics can
 *               display gradient paint correctly, updated equals() and
 *               corrected clone() (DG);
 * 01-Aug-2007 : Updated API docs (DG);
 *
 */

package org.jfree.chart.title;

import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.block.AbstractBlock;
import org.jfree.chart.block.Block;
import org.jfree.chart.block.LengthConstraintType;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.Size2D;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;

/**
 * The graphical item within a legend item.
 */
public class LegendGraphic extends AbstractBlock
                           implements Block, PublicCloneable {

    /** For serialization. */
    static final long serialVersionUID = -1338791523854985009L;

    /**
     * A flag that controls whether or not the shape is visible - see also
     * lineVisible.
     */
    private boolean shapeVisible;

    /**
     * The shape to display.  To allow for accurate positioning, the center
     * of the shape should be at (0, 0).
     */
    private transient Shape shape;

    /**
     * Defines the location within the block to which the shape will be aligned.
     */
    private RectangleAnchor shapeLocation;

    /**
     * Defines the point on the shape's bounding rectangle that will be
     * aligned to the drawing location when the shape is rendered.
     */
    private RectangleAnchor shapeAnchor;

    /** A flag that controls whether or not the shape is filled. */
    private boolean shapeFilled;

    /** The fill paint for the shape. */
    private transient Paint fillPaint;

    /**
     * The fill paint transformer (used if the fillPaint is an instance of
     * GradientPaint).
     *
     * @since 1.0.4
     */
    private GradientPaintTransformer fillPaintTransformer;

    /** A flag that controls whether or not the shape outline is visible. */
    private boolean shapeOutlineVisible;

    /** The outline paint for the shape. */
    private transient Paint outlinePaint;

    /** The outline stroke for the shape. */
    private transient Stroke outlineStroke;

    /**
     * A flag that controls whether or not the line is visible - see also
     * shapeVisible.
     */
    private boolean lineVisible;

    /** The line. */
    private transient Shape line;

    /** The line stroke. */
    private transient Stroke lineStroke;

    /** The line paint. */
    private transient Paint linePaint;

    /**
     * Creates a new legend graphic.
     *
     * @param shape  the shape (<code>null</code> not permitted).
     * @param fillPaint  the fill paint (<code>null</code> not permitted).
     */
    public LegendGraphic(Shape shape, Paint fillPaint) {
        if (shape == null) {
            throw new IllegalArgumentException("Null 'shape' argument.");
        }
        if (fillPaint == null) {
            throw new IllegalArgumentException("Null 'fillPaint' argument.");
        }
        this.shapeVisible = true;
        this.shape = shape;
        this.shapeAnchor = RectangleAnchor.CENTER;
        this.shapeLocation = RectangleAnchor.CENTER;
        this.shapeFilled = true;
        this.fillPaint = fillPaint;
        this.fillPaintTransformer = new StandardGradientPaintTransformer();
        setPadding(2.0, 2.0, 2.0, 2.0);
    }

    /**
     * Returns a flag that controls whether or not the shape
     * is visible.
     *
     * @return A boolean.
     *
     * @see #setShapeVisible(boolean)
     */
    public boolean isShapeVisible() {
        return this.shapeVisible;
    }

    /**
     * Sets a flag that controls whether or not the shape is
     * visible.
     *
     * @param visible  the flag.
     *
     * @see #isShapeVisible()
     */
    public void setShapeVisible(boolean visible) {
        this.shapeVisible = visible;
    }

    /**
     * Returns the shape.
     *
     * @return The shape.
     *
     * @see #setShape(Shape)
     */
    public Shape getShape() {
        return this.shape;
    }

    /**
     * Sets the shape.
     *
     * @param shape  the shape.
     *
     * @see #getShape()
     */
    public void setShape(Shape shape) {
        this.shape = shape;
    }

    /**
     * Returns a flag that controls whether or not the shapes
     * are filled.
     *
     * @return A boolean.
     *
     * @see #setShapeFilled(boolean)
     */
    public boolean isShapeFilled() {
        return this.shapeFilled;
    }

    /**
     * Sets a flag that controls whether or not the shape is
     * filled.
     *
     * @param filled  the flag.
     *
     * @see #isShapeFilled()
     */
    public void setShapeFilled(boolean filled) {
        this.shapeFilled = filled;
    }

    /**
     * Returns the paint used to fill the shape.
     *
     * @return The fill paint.
     *
     * @see #setFillPaint(Paint)
     */
    public Paint getFillPaint() {
        return this.fillPaint;
    }

    /**
     * Sets the paint used to fill the shape.
     *
     * @param paint  the paint.
     *
     * @see #getFillPaint()
     */
    public void setFillPaint(Paint paint) {
        this.fillPaint = paint;
    }

    /**
     * Returns the transformer used when the fill paint is an instance of
     * <code>GradientPaint</code>.
     *
     * @return The transformer (never <code>null</code>).
     *
     * @since 1.0.4.
     *
     * @see #setFillPaintTransformer(GradientPaintTransformer)
     */
    public GradientPaintTransformer getFillPaintTransformer() {
        return this.fillPaintTransformer;
    }

    /**
     * Sets the transformer used when the fill paint is an instance of
     * <code>GradientPaint</code>.
     *
     * @param transformer  the transformer (<code>null</code> not permitted).
     *
     * @since 1.0.4
     *
     * @see #getFillPaintTransformer()
     */
    public void setFillPaintTransformer(GradientPaintTransformer transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("Null 'transformer' argument.");
        }
        this.fillPaintTransformer = transformer;
    }

    /**
     * Returns a flag that controls whether the shape outline is visible.
     *
     * @return A boolean.
     *
     * @see #setShapeOutlineVisible(boolean)
     */
    public boolean isShapeOutlineVisible() {
        return this.shapeOutlineVisible;
    }

    /**
     * Sets a flag that controls whether or not the shape outline
     * is visible.
     *
     * @param visible  the flag.
     *
     * @see #isShapeOutlineVisible()
     */
    public void setShapeOutlineVisible(boolean visible) {
        this.shapeOutlineVisible = visible;
    }

    /**
     * Returns the outline paint.
     *
     * @return The paint.
     *
     * @see #setOutlinePaint(Paint)
     */
    public Paint getOutlinePaint() {
        return this.outlinePaint;
    }

    /**
     * Sets the outline paint.
     *
     * @param paint  the paint.
     *
     * @see #getOutlinePaint()
     */
    public void setOutlinePaint(Paint paint) {
        this.outlinePaint = paint;
    }

    /**
     * Returns the outline stroke.
     *
     * @return The stroke.
     *
     * @see #setOutlineStroke(Stroke)
     */
    public Stroke getOutlineStroke() {
        return this.outlineStroke;
    }

    /**
     * Sets the outline stroke.
     *
     * @param stroke  the stroke.
     *
     * @see #getOutlineStroke()
     */
    public void setOutlineStroke(Stroke stroke) {
        this.outlineStroke = stroke;
    }

    /**
     * Returns the shape anchor.
     *
     * @return The shape anchor.
     *
     * @see #getShapeAnchor()
     */
    public RectangleAnchor getShapeAnchor() {
        return this.shapeAnchor;
    }

    /**
     * Sets the shape anchor.  This defines a point on the shapes bounding
     * rectangle that will be used to align the shape to a location.
     *
     * @param anchor  the anchor (<code>null</code> not permitted).
     *
     * @see #setShapeAnchor(RectangleAnchor)
     */
    public void setShapeAnchor(RectangleAnchor anchor) {
        if (anchor == null) {
            throw new IllegalArgumentException("Null 'anchor' argument.");
        }
        this.shapeAnchor = anchor;
    }

    /**
     * Returns the shape location.
     *
     * @return The shape location.
     *
     * @see #setShapeLocation(RectangleAnchor)
     */
    public RectangleAnchor getShapeLocation() {
        return this.shapeLocation;
    }

    /**
     * Sets the shape location.  This defines a point within the drawing
     * area that will be used to align the shape to.
     *
     * @param location  the location (<code>null</code> not permitted).
     *
     * @see #getShapeLocation()
     */
    public void setShapeLocation(RectangleAnchor location) {
        if (location == null) {
            throw new IllegalArgumentException("Null 'location' argument.");
        }
        this.shapeLocation = location;
    }

    /**
     * Returns the flag that controls whether or not the line is visible.
     *
     * @return A boolean.
     *
     * @see #setLineVisible(boolean)
     */
    public boolean isLineVisible() {
        return this.lineVisible;
    }

    /**
     * Sets the flag that controls whether or not the line is visible.
     *
     * @param visible  the flag.
     *
     * @see #isLineVisible()
     */
    public void setLineVisible(boolean visible) {
        this.lineVisible = visible;
    }

    /**
     * Returns the line centered about (0, 0).
     *
     * @return The line.
     *
     * @see #setLine(Shape)
     */
    public Shape getLine() {
        return this.line;
    }

    /**
     * Sets the line.  A Shape is used here, because then you can use Line2D,
     * GeneralPath or any other Shape to represent the line.
     *
     * @param line  the line.
     *
     * @see #getLine()
     */
    public void setLine(Shape line) {
        this.line = line;
    }

    /**
     * Returns the line paint.
     *
     * @return The paint.
     *
     * @see #setLinePaint(Paint)
     */
    public Paint getLinePaint() {
        return this.linePaint;
    }

    /**
     * Sets the line paint.
     *
     * @param paint  the paint.
     *
     * @see #getLinePaint()
     */
    public void setLinePaint(Paint paint) {
        this.linePaint = paint;
    }

    /**
     * Returns the line stroke.
     *
     * @return The stroke.
     *
     * @see #setLineStroke(Stroke)
     */
    public Stroke getLineStroke() {
        return this.lineStroke;
    }

    /**
     * Sets the line stroke.
     *
     * @param stroke  the stroke.
     *
     * @see #getLineStroke()
     */
    public void setLineStroke(Stroke stroke) {
        this.lineStroke = stroke;
    }

    /**
     * Arranges the contents of the block, within the given constraints, and
     * returns the block size.
     *
     * @param g2  the graphics device.
     * @param constraint  the constraint (<code>null</code> not permitted).
     *
     * @return The block size (in Java2D units, never <code>null</code>).
     */
    public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
        RectangleConstraint contentConstraint = toContentConstraint(constraint);
        LengthConstraintType w = contentConstraint.getWidthConstraintType();
        LengthConstraintType h = contentConstraint.getHeightConstraintType();
        Size2D contentSize = null;
        if (w == LengthConstraintType.NONE) {
            if (h == LengthConstraintType.NONE) {
                contentSize = arrangeNN(g2);
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        else if (w == LengthConstraintType.RANGE) {
            if (h == LengthConstraintType.NONE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        else if (w == LengthConstraintType.FIXED) {
            if (h == LengthConstraintType.NONE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.FIXED) {
                contentSize = new Size2D(
                    contentConstraint.getWidth(),
                    contentConstraint.getHeight()
                );
            }
        }
        return new Size2D(
            calculateTotalWidth(contentSize.getWidth()),
            calculateTotalHeight(contentSize.getHeight())
        );
    }

    /**
     * Performs the layout with no constraint, so the content size is
     * determined by the bounds of the shape and/or line drawn to represent
     * the series.
     *
     * @param g2  the graphics device.
     *
     * @return  The content size.
     */
    protected Size2D arrangeNN(Graphics2D g2) {
        Rectangle2D contentSize = new Rectangle2D.Double();
        if (this.line != null) {
            contentSize.setRect(this.line.getBounds2D());
        }
        if (this.shape != null) {
            contentSize = contentSize.createUnion(this.shape.getBounds2D());
        }
        return new Size2D(contentSize.getWidth(), contentSize.getHeight());
    }

    /**
     * Draws the graphic item within the specified area.
     *
     * @param g2  the graphics device.
     * @param area  the area.
     */
    public void draw(Graphics2D g2, Rectangle2D area) {

        area = trimMargin(area);
        drawBorder(g2, area);
        area = trimBorder(area);
        area = trimPadding(area);

        if (this.lineVisible) {
            Point2D location = RectangleAnchor.coordinates(area,
                    this.shapeLocation);
            Shape aLine = ShapeUtilities.createTranslatedShape(getLine(),
                    this.shapeAnchor, location.getX(), location.getY());
            g2.setPaint(this.linePaint);
            g2.setStroke(this.lineStroke);
            g2.draw(aLine);
        }

        if (this.shapeVisible) {
            Point2D location = RectangleAnchor.coordinates(area,
                    this.shapeLocation);

            Shape s = ShapeUtilities.createTranslatedShape(this.shape,
                    this.shapeAnchor, location.getX(), location.getY());
            if (this.shapeFilled) {
                Paint p = this.fillPaint;
                if (p instanceof GradientPaint) {
                    GradientPaint gp = (GradientPaint) this.fillPaint;
                    p = this.fillPaintTransformer.transform(gp, s);
                }
                g2.setPaint(p);
                g2.fill(s);
            }
            if (this.shapeOutlineVisible) {
                g2.setPaint(this.outlinePaint);
                g2.setStroke(this.outlineStroke);
                g2.draw(s);
            }
        }

    }

    /**
     * Draws the block within the specified area.
     *
     * @param g2  the graphics device.
     * @param area  the area.
     * @param params  ignored (<code>null</code> permitted).
     *
     * @return Always <code>null</code>.
     */
    public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
        draw(g2, area);
        return null;
    }

    /**
     * Tests this <code>LegendGraphic</code> instance for equality with an
     * arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof LegendGraphic)) {
            return false;
        }
        LegendGraphic that = (LegendGraphic) obj;
        if (this.shapeVisible != that.shapeVisible) {
            return false;
        }
        if (!ShapeUtilities.equal(this.shape, that.shape)) {
            return false;
        }
        if (this.shapeFilled != that.shapeFilled) {
            return false;
        }
        if (!PaintUtilities.equal(this.fillPaint, that.fillPaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.fillPaintTransformer,
                that.fillPaintTransformer)) {
            return false;
        }
        if (this.shapeOutlineVisible != that.shapeOutlineVisible) {
            return false;
        }
        if (!PaintUtilities.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.outlineStroke, that.outlineStroke)) {
            return false;
        }
        if (this.shapeAnchor != that.shapeAnchor) {
            return false;
        }
        if (this.shapeLocation != that.shapeLocation) {
            return false;
        }
        if (this.lineVisible != that.lineVisible) {
            return false;
        }
        if (!ShapeUtilities.equal(this.line, that.line)) {
            return false;
        }
        if (!PaintUtilities.equal(this.linePaint, that.linePaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.lineStroke, that.lineStroke)) {
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
        int result = 193;
        result = 37 * result + ObjectUtilities.hashCode(this.fillPaint);
        // FIXME: use other fields too
        return result;
    }

    /**
     * Returns a clone of this <code>LegendGraphic</code> instance.
     *
     * @return A clone of this <code>LegendGraphic</code> instance.
     *
     * @throws CloneNotSupportedException if there is a problem cloning.
     */
    public Object clone() throws CloneNotSupportedException {
        LegendGraphic clone = (LegendGraphic) super.clone();
        clone.shape = ShapeUtilities.clone(this.shape);
        clone.line = ShapeUtilities.clone(this.line);
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
        SerialUtilities.writeShape(this.shape, stream);
        SerialUtilities.writePaint(this.fillPaint, stream);
        SerialUtilities.writePaint(this.outlinePaint, stream);
        SerialUtilities.writeStroke(this.outlineStroke, stream);
        SerialUtilities.writeShape(this.line, stream);
        SerialUtilities.writePaint(this.linePaint, stream);
        SerialUtilities.writeStroke(this.lineStroke, stream);
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
        this.fillPaint = SerialUtilities.readPaint(stream);
        this.outlinePaint = SerialUtilities.readPaint(stream);
        this.outlineStroke = SerialUtilities.readStroke(stream);
        this.line = SerialUtilities.readShape(stream);
        this.linePaint = SerialUtilities.readPaint(stream);
        this.lineStroke = SerialUtilities.readStroke(stream);
    }

}
