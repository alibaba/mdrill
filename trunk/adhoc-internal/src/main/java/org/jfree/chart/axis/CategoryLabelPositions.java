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
 * CategoryLabelPositions.java
 * ---------------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 06-Jan-2004 : Version 1 (DG);
 * 17-Feb-2004 : Added equals() method (DG);
 * 05-Nov-2004 : Adjusted settings for UP_90 and DOWN_90 (DG);
 *
 */

package org.jfree.chart.axis;

import java.io.Serializable;

import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

/**
 * Records the label positions for a category axis.  Instances of this class
 * are immutable.
 */
public class CategoryLabelPositions implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -8999557901920364580L;

    /** STANDARD category label positions. */
    public static final CategoryLabelPositions
        STANDARD = new CategoryLabelPositions(
            new CategoryLabelPosition(
                RectangleAnchor.BOTTOM, TextBlockAnchor.BOTTOM_CENTER
            ), // TOP
            new CategoryLabelPosition(
                RectangleAnchor.TOP, TextBlockAnchor.TOP_CENTER
            ), // BOTTOM
            new CategoryLabelPosition(
                RectangleAnchor.RIGHT, TextBlockAnchor.CENTER_RIGHT,
                CategoryLabelWidthType.RANGE, 0.30f
            ), // LEFT
            new CategoryLabelPosition(
                RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT,
                CategoryLabelWidthType.RANGE, 0.30f
            ) // RIGHT
        );

    /** UP_90 category label positions. */
    public static final CategoryLabelPositions
        UP_90 = new CategoryLabelPositions(
            new CategoryLabelPosition(
                RectangleAnchor.BOTTOM, TextBlockAnchor.CENTER_LEFT,
                TextAnchor.CENTER_LEFT, -Math.PI / 2.0,
                CategoryLabelWidthType.RANGE, 0.30f
            ), // TOP
            new CategoryLabelPosition(
                RectangleAnchor.TOP, TextBlockAnchor.CENTER_RIGHT,
                TextAnchor.CENTER_RIGHT, -Math.PI / 2.0,
                CategoryLabelWidthType.RANGE, 0.30f
            ), // BOTTOM
            new CategoryLabelPosition(
                RectangleAnchor.RIGHT, TextBlockAnchor.BOTTOM_CENTER,
                TextAnchor.BOTTOM_CENTER, -Math.PI / 2.0,
                CategoryLabelWidthType.CATEGORY, 0.9f
            ), // LEFT
            new CategoryLabelPosition(
                RectangleAnchor.LEFT, TextBlockAnchor.TOP_CENTER,
                TextAnchor.TOP_CENTER, -Math.PI / 2.0,
                CategoryLabelWidthType.CATEGORY, 0.90f
            ) // RIGHT
        );

    /** DOWN_90 category label positions. */
    public static final CategoryLabelPositions
        DOWN_90 = new CategoryLabelPositions(
            new CategoryLabelPosition(
                RectangleAnchor.BOTTOM, TextBlockAnchor.CENTER_RIGHT,
                TextAnchor.CENTER_RIGHT, Math.PI / 2.0,
                CategoryLabelWidthType.RANGE, 0.30f
            ), // TOP
            new CategoryLabelPosition(
                RectangleAnchor.TOP, TextBlockAnchor.CENTER_LEFT,
                TextAnchor.CENTER_LEFT, Math.PI / 2.0,
                CategoryLabelWidthType.RANGE, 0.30f
            ), // BOTTOM
            new CategoryLabelPosition(
                RectangleAnchor.RIGHT, TextBlockAnchor.TOP_CENTER,
                TextAnchor.TOP_CENTER, Math.PI / 2.0,
                CategoryLabelWidthType.CATEGORY, 0.90f
            ), // LEFT
            new CategoryLabelPosition(
                RectangleAnchor.LEFT, TextBlockAnchor.BOTTOM_CENTER,
                TextAnchor.BOTTOM_CENTER, Math.PI / 2.0,
                CategoryLabelWidthType.CATEGORY, 0.90f
            ) // RIGHT
        );

    /** UP_45 category label positions. */
    public static final CategoryLabelPositions UP_45
        = createUpRotationLabelPositions(Math.PI / 4.0);

    /** DOWN_45 category label positions. */
    public static final CategoryLabelPositions DOWN_45
        = createDownRotationLabelPositions(Math.PI / 4.0);

    /**
     * Creates a new instance where the category labels angled upwards by the
     * specified amount.
     *
     * @param angle  the rotation angle (should be < Math.PI / 2.0).
     *
     * @return A category label position specification.
     */
    public static CategoryLabelPositions createUpRotationLabelPositions(
            double angle) {
        return new CategoryLabelPositions(
            new CategoryLabelPosition(
                RectangleAnchor.BOTTOM, TextBlockAnchor.BOTTOM_LEFT,
                TextAnchor.BOTTOM_LEFT, -angle,
                CategoryLabelWidthType.RANGE, 0.50f
            ), // TOP
            new CategoryLabelPosition(
                RectangleAnchor.TOP, TextBlockAnchor.TOP_RIGHT,
                TextAnchor.TOP_RIGHT, -angle,
                CategoryLabelWidthType.RANGE, 0.50f
            ), // BOTTOM
            new CategoryLabelPosition(
                RectangleAnchor.RIGHT, TextBlockAnchor.BOTTOM_RIGHT,
                TextAnchor.BOTTOM_RIGHT, -angle,
                CategoryLabelWidthType.RANGE, 0.50f
            ), // LEFT
            new CategoryLabelPosition(
                RectangleAnchor.LEFT, TextBlockAnchor.TOP_LEFT,
                TextAnchor.TOP_LEFT, -angle,
                CategoryLabelWidthType.RANGE, 0.50f
            ) // RIGHT
        );
    }

    /**
     * Creates a new instance where the category labels angled downwards by the
     * specified amount.
     *
     * @param angle  the rotation angle (should be < Math.PI / 2.0).
     *
     * @return A category label position specification.
     */
    public static CategoryLabelPositions createDownRotationLabelPositions(
            double angle) {
        return new CategoryLabelPositions(
            new CategoryLabelPosition(
                RectangleAnchor.BOTTOM, TextBlockAnchor.BOTTOM_RIGHT,
                TextAnchor.BOTTOM_RIGHT, angle,
                CategoryLabelWidthType.RANGE, 0.50f
            ), // TOP
            new CategoryLabelPosition(
                RectangleAnchor.TOP, TextBlockAnchor.TOP_LEFT,
                TextAnchor.TOP_LEFT, angle,
                CategoryLabelWidthType.RANGE, 0.50f
            ), // BOTTOM
            new CategoryLabelPosition(
                RectangleAnchor.RIGHT, TextBlockAnchor.TOP_RIGHT,
                TextAnchor.TOP_RIGHT, angle,
                CategoryLabelWidthType.RANGE, 0.50f
            ), // LEFT
            new CategoryLabelPosition(
                RectangleAnchor.LEFT, TextBlockAnchor.BOTTOM_LEFT,
                TextAnchor.BOTTOM_LEFT, angle,
                CategoryLabelWidthType.RANGE, 0.50f
            ) // RIGHT
        );
    }

    /**
     * The label positioning details used when an axis is at the top of a
     * chart.
     */
    private CategoryLabelPosition positionForAxisAtTop;

    /**
     * The label positioning details used when an axis is at the bottom of a
     * chart.
     */
    private CategoryLabelPosition positionForAxisAtBottom;

    /**
     * The label positioning details used when an axis is at the left of a
     * chart.
     */
    private CategoryLabelPosition positionForAxisAtLeft;

    /**
     * The label positioning details used when an axis is at the right of a
     * chart.
     */
    private CategoryLabelPosition positionForAxisAtRight;

    /**
     * Default constructor.
     */
    public CategoryLabelPositions() {
        this.positionForAxisAtTop = new CategoryLabelPosition();
        this.positionForAxisAtBottom = new CategoryLabelPosition();
        this.positionForAxisAtLeft = new CategoryLabelPosition();
        this.positionForAxisAtRight = new CategoryLabelPosition();
    }

    /**
     * Creates a new position specification.
     *
     * @param top  the label position info used when an axis is at the top
     *             (<code>null</code> not permitted).
     * @param bottom  the label position info used when an axis is at the
     *                bottom (<code>null</code> not permitted).
     * @param left  the label position info used when an axis is at the left
     *              (<code>null</code> not permitted).
     * @param right  the label position info used when an axis is at the right
     *               (<code>null</code> not permitted).
     */
    public CategoryLabelPositions(CategoryLabelPosition top,
                                  CategoryLabelPosition bottom,
                                  CategoryLabelPosition left,
                                  CategoryLabelPosition right) {

        if (top == null) {
            throw new IllegalArgumentException("Null 'top' argument.");
        }
        if (bottom == null) {
            throw new IllegalArgumentException("Null 'bottom' argument.");
        }
        if (left == null) {
            throw new IllegalArgumentException("Null 'left' argument.");
        }
        if (right == null) {
            throw new IllegalArgumentException("Null 'right' argument.");
        }

        this.positionForAxisAtTop = top;
        this.positionForAxisAtBottom = bottom;
        this.positionForAxisAtLeft = left;
        this.positionForAxisAtRight = right;

    }

    /**
     * Returns the category label position specification for an axis at the
     * given location.
     *
     * @param edge  the axis location.
     *
     * @return The category label position specification.
     */
    public CategoryLabelPosition getLabelPosition(RectangleEdge edge) {
        CategoryLabelPosition result = null;
        if (edge == RectangleEdge.TOP) {
            result = this.positionForAxisAtTop;
        }
        else if (edge == RectangleEdge.BOTTOM) {
            result = this.positionForAxisAtBottom;
        }
        else if (edge == RectangleEdge.LEFT) {
            result = this.positionForAxisAtLeft;
        }
        else if (edge == RectangleEdge.RIGHT) {
            result = this.positionForAxisAtRight;
        }
        return result;
    }

    /**
     * Returns a new instance based on an existing instance but with the top
     * position changed.
     *
     * @param base  the base (<code>null</code> not permitted).
     * @param top  the top position (<code>null</code> not permitted).
     *
     * @return A new instance (never <code>null</code>).
     */
    public static CategoryLabelPositions replaceTopPosition(
            CategoryLabelPositions base, CategoryLabelPosition top) {

        if (base == null) {
            throw new IllegalArgumentException("Null 'base' argument.");
        }
        if (top == null) {
            throw new IllegalArgumentException("Null 'top' argument.");
        }

        return new CategoryLabelPositions(
            top,
            base.getLabelPosition(RectangleEdge.BOTTOM),
            base.getLabelPosition(RectangleEdge.LEFT),
            base.getLabelPosition(RectangleEdge.RIGHT)
        );
    }

    /**
     * Returns a new instance based on an existing instance but with the bottom
     * position changed.
     *
     * @param base  the base (<code>null</code> not permitted).
     * @param bottom  the bottom position (<code>null</code> not permitted).
     *
     * @return A new instance (never <code>null</code>).
     */
    public static CategoryLabelPositions replaceBottomPosition(
            CategoryLabelPositions base, CategoryLabelPosition bottom) {

        if (base == null) {
            throw new IllegalArgumentException("Null 'base' argument.");
        }
        if (bottom == null) {
            throw new IllegalArgumentException("Null 'bottom' argument.");
        }

        return new CategoryLabelPositions(
            base.getLabelPosition(RectangleEdge.TOP),
            bottom,
            base.getLabelPosition(RectangleEdge.LEFT),
            base.getLabelPosition(RectangleEdge.RIGHT)
        );
    }

    /**
     * Returns a new instance based on an existing instance but with the left
     * position changed.
     *
     * @param base  the base (<code>null</code> not permitted).
     * @param left  the left position (<code>null</code> not permitted).
     *
     * @return A new instance (never <code>null</code>).
     */
    public static CategoryLabelPositions replaceLeftPosition(
            CategoryLabelPositions base, CategoryLabelPosition left) {

        if (base == null) {
            throw new IllegalArgumentException("Null 'base' argument.");
        }
        if (left == null) {
            throw new IllegalArgumentException("Null 'left' argument.");
        }

        return new CategoryLabelPositions(
            base.getLabelPosition(RectangleEdge.TOP),
            base.getLabelPosition(RectangleEdge.BOTTOM),
            left,
            base.getLabelPosition(RectangleEdge.RIGHT)
        );
    }

    /**
     * Returns a new instance based on an existing instance but with the right
     * position changed.
     *
     * @param base  the base (<code>null</code> not permitted).
     * @param right  the right position (<code>null</code> not permitted).
     *
     * @return A new instance (never <code>null</code>).
     */
    public static CategoryLabelPositions replaceRightPosition(
            CategoryLabelPositions base, CategoryLabelPosition right) {

        if (base == null) {
            throw new IllegalArgumentException("Null 'base' argument.");
        }
        if (right == null) {
            throw new IllegalArgumentException("Null 'right' argument.");
        }

        return new CategoryLabelPositions(
            base.getLabelPosition(RectangleEdge.TOP),
            base.getLabelPosition(RectangleEdge.BOTTOM),
            base.getLabelPosition(RectangleEdge.LEFT),
            right
        );
    }

    /**
     * Returns <code>true</code> if this object is equal to the specified
     * object, and <code>false</code> otherwise.
     *
     * @param obj  the other object.
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CategoryLabelPositions)) {
            return false;
        }

        CategoryLabelPositions that = (CategoryLabelPositions) obj;
        if (!this.positionForAxisAtTop.equals(that.positionForAxisAtTop)) {
            return false;
        }
        if (!this.positionForAxisAtBottom.equals(
                that.positionForAxisAtBottom)) {
            return false;
        }
        if (!this.positionForAxisAtLeft.equals(that.positionForAxisAtLeft)) {
            return false;
        }
        if (!this.positionForAxisAtRight.equals(that.positionForAxisAtRight)) {
            return false;
        }

        return true;

    }

    /**
     * Returns a hash code for this object.
     *
     * @return A hash code.
     */
    public int hashCode() {
        int result = 19;
        result = 37 * result + this.positionForAxisAtTop.hashCode();
        result = 37 * result + this.positionForAxisAtBottom.hashCode();
        result = 37 * result + this.positionForAxisAtLeft.hashCode();
        result = 37 * result + this.positionForAxisAtRight.hashCode();
        return result;
    }
}
