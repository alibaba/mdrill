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
 * AbstractBlock.java
 * ------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 22-Oct-2004 : Version 1 (DG);
 * 02-Feb-2005 : Added accessor methods for margin (DG);
 * 04-Feb-2005 : Added equals() method and implemented Serializable (DG);
 * 03-May-2005 : Added null argument checks (DG);
 * 06-May-2005 : Added convenience methods for setting margin, border and
 *               padding (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 16-Mar-2007 : Changed border from BlockBorder to BlockFrame, updated
 *               equals(), and implemented Cloneable (DG);
 *
 */

package org.jfree.chart.block;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.data.Range;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.Size2D;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;

/**
 * A convenience class for creating new classes that implement
 * the {@link Block} interface.
 */
public class AbstractBlock implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 7689852412141274563L;

    /** The id for the block. */
    private String id;

    /** The margin around the outside of the block. */
    private RectangleInsets margin;

    /** The frame (or border) for the block. */
    private BlockFrame frame;

    /** The padding between the block content and the border. */
    private RectangleInsets padding;

    /**
     * The natural width of the block (may be overridden if there are
     * constraints in sizing).
     */
    private double width;

    /**
     * The natural height of the block (may be overridden if there are
     * constraints in sizing).
     */
    private double height;

    /**
     * The current bounds for the block (position of the block in Java2D space).
     */
    private transient Rectangle2D bounds;

    /**
     * Creates a new block.
     */
    protected AbstractBlock() {
        this.id = null;
        this.width = 0.0;
        this.height = 0.0;
        this.bounds = new Rectangle2D.Float();
        this.margin = RectangleInsets.ZERO_INSETS;
        this.frame = BlockBorder.NONE;
        this.padding = RectangleInsets.ZERO_INSETS;
    }

    /**
     * Returns the id.
     *
     * @return The id (possibly <code>null</code>).
     *
     * @see #setID(String)
     */
    public String getID() {
        return this.id;
    }

    /**
     * Sets the id for the block.
     *
     * @param id  the id (<code>null</code> permitted).
     *
     * @see #getID()
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Returns the natural width of the block, if this is known in advance.
     * The actual width of the block may be overridden if layout constraints
     * make this necessary.
     *
     * @return The width.
     *
     * @see #setWidth(double)
     */
    public double getWidth() {
        return this.width;
    }

    /**
     * Sets the natural width of the block, if this is known in advance.
     *
     * @param width  the width (in Java2D units)
     *
     * @see #getWidth()
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Returns the natural height of the block, if this is known in advance.
     * The actual height of the block may be overridden if layout constraints
     * make this necessary.
     *
     * @return The height.
     *
     * @see #setHeight(double)
     */
    public double getHeight() {
        return this.height;
    }

    /**
     * Sets the natural width of the block, if this is known in advance.
     *
     * @param height  the width (in Java2D units)
     *
     * @see #getHeight()
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Returns the margin.
     *
     * @return The margin (never <code>null</code>).
     *
     * @see #getMargin()
     */
    public RectangleInsets getMargin() {
        return this.margin;
    }

    /**
     * Sets the margin (use {@link RectangleInsets#ZERO_INSETS} for no
     * padding).
     *
     * @param margin  the margin (<code>null</code> not permitted).
     *
     * @see #getMargin()
     */
    public void setMargin(RectangleInsets margin) {
        if (margin == null) {
            throw new IllegalArgumentException("Null 'margin' argument.");
        }
        this.margin = margin;
    }

    /**
     * Sets the margin.
     *
     * @param top  the top margin.
     * @param left  the left margin.
     * @param bottom  the bottom margin.
     * @param right  the right margin.
     *
     * @see #getMargin()
     */
    public void setMargin(double top, double left, double bottom,
                          double right) {
        setMargin(new RectangleInsets(top, left, bottom, right));
    }

    /**
     * Returns the border.
     *
     * @return The border (never <code>null</code>).
     *
     * @deprecated Use {@link #getFrame()} instead.
     */
    public BlockBorder getBorder() {
        if (this.frame instanceof BlockBorder) {
            return (BlockBorder) this.frame;
        }
        else {
            return null;
        }
    }

    /**
     * Sets the border for the block (use {@link BlockBorder#NONE} for
     * no border).
     *
     * @param border  the border (<code>null</code> not permitted).
     *
     * @see #getBorder()
     *
     * @deprecated Use {@link #setFrame(BlockFrame)} instead.
     */
    public void setBorder(BlockBorder border) {
        setFrame(border);
    }

    /**
     * Sets a black border with the specified line widths.
     *
     * @param top  the top border line width.
     * @param left  the left border line width.
     * @param bottom  the bottom border line width.
     * @param right  the right border line width.
     */
    public void setBorder(double top, double left, double bottom,
                          double right) {
        setFrame(new BlockBorder(top, left, bottom, right));
    }

    /**
     * Returns the current frame (border).
     *
     * @return The frame.
     *
     * @since 1.0.5
     * @see #setFrame(BlockFrame)
     */
    public BlockFrame getFrame() {
        return this.frame;
    }

    /**
     * Sets the frame (or border).
     *
     * @param frame  the frame (<code>null</code> not permitted).
     *
     * @since 1.0.5
     * @see #getFrame()
     */
    public void setFrame(BlockFrame frame) {
        if (frame == null) {
            throw new IllegalArgumentException("Null 'frame' argument.");
        }
        this.frame = frame;
    }

    /**
     * Returns the padding.
     *
     * @return The padding (never <code>null</code>).
     *
     * @see #setPadding(RectangleInsets)
     */
    public RectangleInsets getPadding() {
        return this.padding;
    }

    /**
     * Sets the padding (use {@link RectangleInsets#ZERO_INSETS} for no
     * padding).
     *
     * @param padding  the padding (<code>null</code> not permitted).
     *
     * @see #getPadding()
     */
    public void setPadding(RectangleInsets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("Null 'padding' argument.");
        }
        this.padding = padding;
    }

    /**
     * Sets the padding.
     *
     * @param top  the top padding.
     * @param left  the left padding.
     * @param bottom  the bottom padding.
     * @param right  the right padding.
     */
    public void setPadding(double top, double left, double bottom,
                           double right) {
        setPadding(new RectangleInsets(top, left, bottom, right));
    }

    /**
     * Returns the x-offset for the content within the block.
     *
     * @return The x-offset.
     *
     * @see #getContentYOffset()
     */
    public double getContentXOffset() {
        return this.margin.getLeft() + this.frame.getInsets().getLeft()
            + this.padding.getLeft();
    }

    /**
     * Returns the y-offset for the content within the block.
     *
     * @return The y-offset.
     *
     * @see #getContentXOffset()
     */
    public double getContentYOffset() {
        return this.margin.getTop() + this.frame.getInsets().getTop()
            + this.padding.getTop();
    }

    /**
     * Arranges the contents of the block, with no constraints, and returns
     * the block size.
     *
     * @param g2  the graphics device.
     *
     * @return The block size (in Java2D units, never <code>null</code>).
     */
    public Size2D arrange(Graphics2D g2) {
        return arrange(g2, RectangleConstraint.NONE);
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
        Size2D base = new Size2D(getWidth(), getHeight());
        return constraint.calculateConstrainedSize(base);
    }

    /**
     * Returns the current bounds of the block.
     *
     * @return The bounds.
     *
     * @see #setBounds(Rectangle2D)
     */
    public Rectangle2D getBounds() {
        return this.bounds;
    }

    /**
     * Sets the bounds of the block.
     *
     * @param bounds  the bounds (<code>null</code> not permitted).
     *
     * @see #getBounds()
     */
    public void setBounds(Rectangle2D bounds) {
        if (bounds == null) {
            throw new IllegalArgumentException("Null 'bounds' argument.");
        }
        this.bounds = bounds;
    }

    /**
     * Calculate the width available for content after subtracting
     * the margin, border and padding space from the specified fixed
     * width.
     *
     * @param fixedWidth  the fixed width.
     *
     * @return The available space.
     *
     * @see #trimToContentHeight(double)
     */
    protected double trimToContentWidth(double fixedWidth) {
        double result = this.margin.trimWidth(fixedWidth);
        result = this.frame.getInsets().trimWidth(result);
        result = this.padding.trimWidth(result);
        return Math.max(result, 0.0);
    }

    /**
     * Calculate the height available for content after subtracting
     * the margin, border and padding space from the specified fixed
     * height.
     *
     * @param fixedHeight  the fixed height.
     *
     * @return The available space.
     *
     * @see #trimToContentWidth(double)
     */
    protected double trimToContentHeight(double fixedHeight) {
        double result = this.margin.trimHeight(fixedHeight);
        result = this.frame.getInsets().trimHeight(result);
        result = this.padding.trimHeight(result);
        return Math.max(result, 0.0);
    }

    /**
     * Returns a constraint for the content of this block that will result in
     * the bounds of the block matching the specified constraint.
     *
     * @param c  the outer constraint (<code>null</code> not permitted).
     *
     * @return The content constraint.
     */
    protected RectangleConstraint toContentConstraint(RectangleConstraint c) {
        if (c == null) {
            throw new IllegalArgumentException("Null 'c' argument.");
        }
        if (c.equals(RectangleConstraint.NONE)) {
            return c;
        }
        double w = c.getWidth();
        Range wr = c.getWidthRange();
        double h = c.getHeight();
        Range hr = c.getHeightRange();
        double ww = trimToContentWidth(w);
        double hh = trimToContentHeight(h);
        Range wwr = trimToContentWidth(wr);
        Range hhr = trimToContentHeight(hr);
        return new RectangleConstraint(
            ww, wwr, c.getWidthConstraintType(),
            hh, hhr, c.getHeightConstraintType()
        );
    }

    private Range trimToContentWidth(Range r) {
        if (r == null) {
            return null;
        }
        double lowerBound = 0.0;
        double upperBound = Double.POSITIVE_INFINITY;
        if (r.getLowerBound() > 0.0) {
            lowerBound = trimToContentWidth(r.getLowerBound());
        }
        if (r.getUpperBound() < Double.POSITIVE_INFINITY) {
            upperBound = trimToContentWidth(r.getUpperBound());
        }
        return new Range(lowerBound, upperBound);
    }

    private Range trimToContentHeight(Range r) {
        if (r == null) {
            return null;
        }
        double lowerBound = 0.0;
        double upperBound = Double.POSITIVE_INFINITY;
        if (r.getLowerBound() > 0.0) {
            lowerBound = trimToContentHeight(r.getLowerBound());
        }
        if (r.getUpperBound() < Double.POSITIVE_INFINITY) {
            upperBound = trimToContentHeight(r.getUpperBound());
        }
        return new Range(lowerBound, upperBound);
    }

    /**
     * Adds the margin, border and padding to the specified content width.
     *
     * @param contentWidth  the content width.
     *
     * @return The adjusted width.
     */
    protected double calculateTotalWidth(double contentWidth) {
        double result = contentWidth;
        result = this.padding.extendWidth(result);
        result = this.frame.getInsets().extendWidth(result);
        result = this.margin.extendWidth(result);
        return result;
    }

    /**
     * Adds the margin, border and padding to the specified content height.
     *
     * @param contentHeight  the content height.
     *
     * @return The adjusted height.
     */
    protected double calculateTotalHeight(double contentHeight) {
        double result = contentHeight;
        result = this.padding.extendHeight(result);
        result = this.frame.getInsets().extendHeight(result);
        result = this.margin.extendHeight(result);
        return result;
    }

    /**
     * Reduces the specified area by the amount of space consumed
     * by the margin.
     *
     * @param area  the area (<code>null</code> not permitted).
     *
     * @return The trimmed area.
     */
    protected Rectangle2D trimMargin(Rectangle2D area) {
        // defer argument checking...
        this.margin.trim(area);
        return area;
    }

    /**
     * Reduces the specified area by the amount of space consumed
     * by the border.
     *
     * @param area  the area (<code>null</code> not permitted).
     *
     * @return The trimmed area.
     */
    protected Rectangle2D trimBorder(Rectangle2D area) {
        // defer argument checking...
        this.frame.getInsets().trim(area);
        return area;
    }

    /**
     * Reduces the specified area by the amount of space consumed
     * by the padding.
     *
     * @param area  the area (<code>null</code> not permitted).
     *
     * @return The trimmed area.
     */
    protected Rectangle2D trimPadding(Rectangle2D area) {
        // defer argument checking...
        this.padding.trim(area);
        return area;
    }

    /**
     * Draws the border around the perimeter of the specified area.
     *
     * @param g2  the graphics device.
     * @param area  the area.
     */
    protected void drawBorder(Graphics2D g2, Rectangle2D area) {
        this.frame.draw(g2, area);
    }

    /**
     * Tests this block for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AbstractBlock)) {
            return false;
        }
        AbstractBlock that = (AbstractBlock) obj;
        if (!ObjectUtilities.equal(this.id, that.id)) {
            return false;
        }
        if (!this.frame.equals(that.frame)) {
            return false;
        }
        if (!this.bounds.equals(that.bounds)) {
            return false;
        }
        if (!this.margin.equals(that.margin)) {
            return false;
        }
        if (!this.padding.equals(that.padding)) {
            return false;
        }
        if (this.height != that.height) {
            return false;
        }
        if (this.width != that.width) {
            return false;
        }
        return true;
    }

    /**
     * Returns a clone of this block.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if there is a problem creating the
     *         clone.
     */
    public Object clone() throws CloneNotSupportedException {
        AbstractBlock clone = (AbstractBlock) super.clone();
        clone.bounds = (Rectangle2D) ShapeUtilities.clone(this.bounds);
        if (this.frame instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable) this.frame;
            clone.frame = (BlockFrame) pc.clone();
        }
        return clone;
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
        SerialUtilities.writeShape(this.bounds, stream);
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
        this.bounds = (Rectangle2D) SerialUtilities.readShape(stream);
    }

}
