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
 * ----------------------
 * BorderArrangement.java
 * ----------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 22-Oct-2004 : Version 1 (DG);
 * 08-Feb-2005 : Updated for changes in RectangleConstraint (DG);
 * 24-Feb-2005 : Improved arrangeRR() method (DG);
 * 03-May-2005 : Implemented Serializable and added equals() method (DG);
 * 13-May-2005 : Fixed bugs in the arrange() method (DG);
 * 08-Apr-2008 : Fixed bug in arrangeFF() method where width is too small for
 *               left and right blocks (DG);
 *
 */

package org.jfree.chart.block;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.Size2D;
import org.jfree.util.ObjectUtilities;

/**
 * An arrangement manager that lays out blocks in a similar way to
 * Swing's BorderLayout class.
 */
public class BorderArrangement implements Arrangement, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 506071142274883745L;

    /** The block (if any) at the center of the layout. */
    private Block centerBlock;

    /** The block (if any) at the top of the layout. */
    private Block topBlock;

    /** The block (if any) at the bottom of the layout. */
    private Block bottomBlock;

    /** The block (if any) at the left of the layout. */
    private Block leftBlock;

    /** The block (if any) at the right of the layout. */
    private Block rightBlock;

    /**
     * Creates a new instance.
     */
    public BorderArrangement() {
    }

    /**
     * Adds a block to the arrangement manager at the specified edge.
     *
     * @param block  the block (<code>null</code> permitted).
     * @param key  the edge (an instance of {@link RectangleEdge}) or
     *             <code>null</code> for the center block.
     */
    public void add(Block block, Object key) {

        if (key == null) {
            this.centerBlock = block;
        }
        else {
            RectangleEdge edge = (RectangleEdge) key;
            if (edge == RectangleEdge.TOP) {
                this.topBlock = block;
            }
            else if (edge == RectangleEdge.BOTTOM) {
                this.bottomBlock = block;
            }
            else if (edge == RectangleEdge.LEFT) {
                this.leftBlock = block;
            }
            else if (edge == RectangleEdge.RIGHT) {
                this.rightBlock = block;
            }
        }
    }

    /**
     * Arranges the items in the specified container, subject to the given
     * constraint.
     *
     * @param container  the container.
     * @param g2  the graphics device.
     * @param constraint  the constraint.
     *
     * @return The block size.
     */
    public Size2D arrange(BlockContainer container,
                          Graphics2D g2,
                          RectangleConstraint constraint) {
        RectangleConstraint contentConstraint
                = container.toContentConstraint(constraint);
        Size2D contentSize = null;
        LengthConstraintType w = contentConstraint.getWidthConstraintType();
        LengthConstraintType h = contentConstraint.getHeightConstraintType();
        if (w == LengthConstraintType.NONE) {
            if (h == LengthConstraintType.NONE) {
                contentSize = arrangeNN(container, g2);
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not implemented.");
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not implemented.");
            }
        }
        else if (w == LengthConstraintType.FIXED) {
            if (h == LengthConstraintType.NONE) {
                contentSize = arrangeFN(container, g2, constraint.getWidth());
            }
            else if (h == LengthConstraintType.FIXED) {
                contentSize = arrangeFF(container, g2, constraint);
            }
            else if (h == LengthConstraintType.RANGE) {
                contentSize = arrangeFR(container, g2, constraint);
            }
        }
        else if (w == LengthConstraintType.RANGE) {
            if (h == LengthConstraintType.NONE) {
                throw new RuntimeException("Not implemented.");
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not implemented.");
            }
            else if (h == LengthConstraintType.RANGE) {
                contentSize = arrangeRR(container, constraint.getWidthRange(),
                        constraint.getHeightRange(), g2);
            }
        }
        return new Size2D(container.calculateTotalWidth(contentSize.getWidth()),
                container.calculateTotalHeight(contentSize.getHeight()));
    }

    /**
     * Performs an arrangement without constraints.
     *
     * @param container  the container.
     * @param g2  the graphics device.
     *
     * @return The container size after the arrangement.
     */
    protected Size2D arrangeNN(BlockContainer container, Graphics2D g2) {
        double[] w = new double[5];
        double[] h = new double[5];
        if (this.topBlock != null) {
            Size2D size = this.topBlock.arrange(g2, RectangleConstraint.NONE);
            w[0] = size.width;
            h[0] = size.height;
        }
        if (this.bottomBlock != null) {
            Size2D size = this.bottomBlock.arrange(g2,
                    RectangleConstraint.NONE);
            w[1] = size.width;
            h[1] = size.height;
        }
        if (this.leftBlock != null) {
            Size2D size = this.leftBlock.arrange(g2, RectangleConstraint.NONE);
            w[2] = size.width;
            h[2] = size.height;
       }
        if (this.rightBlock != null) {
            Size2D size = this.rightBlock.arrange(g2, RectangleConstraint.NONE);
            w[3] = size.width;
            h[3] = size.height;
        }

        h[2] = Math.max(h[2], h[3]);
        h[3] = h[2];

        if (this.centerBlock != null) {
            Size2D size = this.centerBlock.arrange(g2,
                    RectangleConstraint.NONE);
            w[4] = size.width;
            h[4] = size.height;
        }
        double width = Math.max(w[0], Math.max(w[1], w[2] + w[4] + w[3]));
        double centerHeight = Math.max(h[2], Math.max(h[3], h[4]));
        double height = h[0] + h[1] + centerHeight;
        if (this.topBlock != null) {
            this.topBlock.setBounds(new Rectangle2D.Double(0.0, 0.0, width,
                    h[0]));
        }
        if (this.bottomBlock != null) {
            this.bottomBlock.setBounds(new Rectangle2D.Double(0.0,
                    height - h[1], width, h[1]));
        }
        if (this.leftBlock != null) {
            this.leftBlock.setBounds(new Rectangle2D.Double(0.0, h[0], w[2],
                    centerHeight));
        }
        if (this.rightBlock != null) {
            this.rightBlock.setBounds(new Rectangle2D.Double(width - w[3],
                    h[0], w[3], centerHeight));
        }

        if (this.centerBlock != null) {
            this.centerBlock.setBounds(new Rectangle2D.Double(w[2], h[0],
                    width - w[2] - w[3], centerHeight));
        }
        return new Size2D(width, height);
    }

    /**
     * Performs an arrangement with a fixed width and a range for the height.
     *
     * @param container  the container.
     * @param g2  the graphics device.
     * @param constraint  the constraint.
     *
     * @return The container size after the arrangement.
     */
    protected Size2D arrangeFR(BlockContainer container, Graphics2D g2,
                               RectangleConstraint constraint) {
        Size2D size1 = arrangeFN(container, g2, constraint.getWidth());
        if (constraint.getHeightRange().contains(size1.getHeight())) {
            return size1;
        }
        else {
            double h = constraint.getHeightRange().constrain(size1.getHeight());
            RectangleConstraint c2 = constraint.toFixedHeight(h);
            return arrange(container, g2, c2);
        }
    }

    /**
     * Arranges the container width a fixed width and no constraint on the
     * height.
     *
     * @param container  the container.
     * @param g2  the graphics device.
     * @param width  the fixed width.
     *
     * @return The container size after arranging the contents.
     */
    protected Size2D arrangeFN(BlockContainer container, Graphics2D g2,
                               double width) {
        double[] w = new double[5];
        double[] h = new double[5];
        RectangleConstraint c1 = new RectangleConstraint(width, null,
                LengthConstraintType.FIXED, 0.0, null,
                LengthConstraintType.NONE);
        if (this.topBlock != null) {
            Size2D size = this.topBlock.arrange(g2, c1);
            w[0] = size.width;
            h[0] = size.height;
        }
        if (this.bottomBlock != null) {
            Size2D size = this.bottomBlock.arrange(g2, c1);
            w[1] = size.width;
            h[1] = size.height;
        }
        RectangleConstraint c2 = new RectangleConstraint(0.0,
                new Range(0.0, width), LengthConstraintType.RANGE,
                0.0, null, LengthConstraintType.NONE);
        if (this.leftBlock != null) {
            Size2D size = this.leftBlock.arrange(g2, c2);
            w[2] = size.width;
            h[2] = size.height;
        }
        if (this.rightBlock != null) {
            double maxW = Math.max(width - w[2], 0.0);
            RectangleConstraint c3 = new RectangleConstraint(0.0,
                    new Range(Math.min(w[2], maxW), maxW),
                    LengthConstraintType.RANGE, 0.0, null,
                    LengthConstraintType.NONE);
            Size2D size = this.rightBlock.arrange(g2, c3);
            w[3] = size.width;
            h[3] = size.height;
        }

        h[2] = Math.max(h[2], h[3]);
        h[3] = h[2];

        if (this.centerBlock != null) {
            RectangleConstraint c4 = new RectangleConstraint(width - w[2]
                    - w[3], null, LengthConstraintType.FIXED, 0.0, null,
                    LengthConstraintType.NONE);
            Size2D size = this.centerBlock.arrange(g2, c4);
            w[4] = size.width;
            h[4] = size.height;
        }
        double height = h[0] + h[1] + Math.max(h[2], Math.max(h[3], h[4]));
        return arrange(container, g2, new RectangleConstraint(width, height));
    }

    /**
     * Performs an arrangement with range constraints on both the vertical
     * and horizontal sides.
     *
     * @param container  the container.
     * @param widthRange  the allowable range for the container width.
     * @param heightRange  the allowable range for the container height.
     * @param g2  the graphics device.
     *
     * @return The container size.
     */
    protected Size2D arrangeRR(BlockContainer container,
                               Range widthRange, Range heightRange,
                               Graphics2D g2) {
        double[] w = new double[5];
        double[] h = new double[5];
        if (this.topBlock != null) {
            RectangleConstraint c1 = new RectangleConstraint(widthRange,
                    heightRange);
            Size2D size = this.topBlock.arrange(g2, c1);
            w[0] = size.width;
            h[0] = size.height;
        }
        if (this.bottomBlock != null) {
            Range heightRange2 = Range.shift(heightRange, -h[0], false);
            RectangleConstraint c2 = new RectangleConstraint(widthRange,
                    heightRange2);
            Size2D size = this.bottomBlock.arrange(g2, c2);
            w[1] = size.width;
            h[1] = size.height;
        }
        Range heightRange3 = Range.shift(heightRange, -(h[0] + h[1]));
        if (this.leftBlock != null) {
            RectangleConstraint c3 = new RectangleConstraint(widthRange,
                    heightRange3);
            Size2D size = this.leftBlock.arrange(g2, c3);
            w[2] = size.width;
            h[2] = size.height;
        }
        Range widthRange2 = Range.shift(widthRange, -w[2], false);
        if (this.rightBlock != null) {
            RectangleConstraint c4 = new RectangleConstraint(widthRange2,
                    heightRange3);
            Size2D size = this.rightBlock.arrange(g2, c4);
            w[3] = size.width;
            h[3] = size.height;
        }

        h[2] = Math.max(h[2], h[3]);
        h[3] = h[2];
        Range widthRange3 = Range.shift(widthRange, -(w[2] + w[3]), false);
        if (this.centerBlock != null) {
            RectangleConstraint c5 = new RectangleConstraint(widthRange3,
                    heightRange3);
            Size2D size = this.centerBlock.arrange(g2, c5);
            w[4] = size.width;
            h[4] = size.height;
        }
        double width = Math.max(w[0], Math.max(w[1], w[2] + w[4] + w[3]));
        double height = h[0] + h[1] + Math.max(h[2], Math.max(h[3], h[4]));
        if (this.topBlock != null) {
            this.topBlock.setBounds(new Rectangle2D.Double(0.0, 0.0, width,
                    h[0]));
        }
        if (this.bottomBlock != null) {
            this.bottomBlock.setBounds(new Rectangle2D.Double(0.0,
                    height - h[1], width, h[1]));
        }
        if (this.leftBlock != null) {
            this.leftBlock.setBounds(new Rectangle2D.Double(0.0, h[0], w[2],
                    h[2]));
        }
        if (this.rightBlock != null) {
            this.rightBlock.setBounds(new Rectangle2D.Double(width - w[3],
                    h[0], w[3], h[3]));
        }

        if (this.centerBlock != null) {
            this.centerBlock.setBounds(new Rectangle2D.Double(w[2], h[0],
                    width - w[2] - w[3], height - h[0] - h[1]));
        }
        return new Size2D(width, height);
    }

    /**
     * Arranges the items within a container.
     *
     * @param container  the container.
     * @param constraint  the constraint.
     * @param g2  the graphics device.
     *
     * @return The container size after the arrangement.
     */
    protected Size2D arrangeFF(BlockContainer container, Graphics2D g2,
                               RectangleConstraint constraint) {
        double[] w = new double[5];
        double[] h = new double[5];
        w[0] = constraint.getWidth();
        if (this.topBlock != null) {
            RectangleConstraint c1 = new RectangleConstraint(w[0], null,
                    LengthConstraintType.FIXED, 0.0,
                    new Range(0.0, constraint.getHeight()),
                    LengthConstraintType.RANGE);
            Size2D size = this.topBlock.arrange(g2, c1);
            h[0] = size.height;
        }
        w[1] = w[0];
        if (this.bottomBlock != null) {
            RectangleConstraint c2 = new RectangleConstraint(w[0], null,
                    LengthConstraintType.FIXED, 0.0, new Range(0.0,
                    constraint.getHeight() - h[0]), LengthConstraintType.RANGE);
            Size2D size = this.bottomBlock.arrange(g2, c2);
            h[1] = size.height;
        }
        h[2] = constraint.getHeight() - h[1] - h[0];
        if (this.leftBlock != null) {
            RectangleConstraint c3 = new RectangleConstraint(0.0,
                    new Range(0.0, constraint.getWidth()),
                    LengthConstraintType.RANGE, h[2], null,
                    LengthConstraintType.FIXED);
            Size2D size = this.leftBlock.arrange(g2, c3);
            w[2] = size.width;
        }
        h[3] = h[2];
        if (this.rightBlock != null) {
            RectangleConstraint c4 = new RectangleConstraint(0.0,
                    new Range(0.0, Math.max(constraint.getWidth() - w[2], 0.0)),
                    LengthConstraintType.RANGE, h[2], null,
                    LengthConstraintType.FIXED);
            Size2D size = this.rightBlock.arrange(g2, c4);
            w[3] = size.width;
        }
        h[4] = h[2];
        w[4] = constraint.getWidth() - w[3] - w[2];
        RectangleConstraint c5 = new RectangleConstraint(w[4], h[4]);
        if (this.centerBlock != null) {
            this.centerBlock.arrange(g2, c5);
        }

        if (this.topBlock != null) {
            this.topBlock.setBounds(new Rectangle2D.Double(0.0, 0.0, w[0],
                    h[0]));
        }
        if (this.bottomBlock != null) {
            this.bottomBlock.setBounds(new Rectangle2D.Double(0.0, h[0] + h[2],
                    w[1], h[1]));
        }
        if (this.leftBlock != null) {
            this.leftBlock.setBounds(new Rectangle2D.Double(0.0, h[0], w[2],
                    h[2]));
        }
        if (this.rightBlock != null) {
            this.rightBlock.setBounds(new Rectangle2D.Double(w[2] + w[4], h[0],
                    w[3], h[3]));
        }
        if (this.centerBlock != null) {
            this.centerBlock.setBounds(new Rectangle2D.Double(w[2], h[0], w[4],
                    h[4]));
        }
        return new Size2D(constraint.getWidth(), constraint.getHeight());
    }

    /**
     * Clears the layout.
     */
    public void clear() {
        this.centerBlock = null;
        this.topBlock = null;
        this.bottomBlock = null;
        this.leftBlock = null;
        this.rightBlock = null;
    }

    /**
     * Tests this arrangement for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BorderArrangement)) {
            return false;
        }
        BorderArrangement that = (BorderArrangement) obj;
        if (!ObjectUtilities.equal(this.topBlock, that.topBlock)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.bottomBlock, that.bottomBlock)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.leftBlock, that.leftBlock)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.rightBlock, that.rightBlock)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.centerBlock, that.centerBlock)) {
            return false;
        }
        return true;
    }
}
