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
 * --------------------
 * FlowArrangement.java
 * --------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 22-Oct-2004 : Version 1 (DG);
 * 04-Feb-2005 : Implemented equals() and made serializable (DG);
 * 08-Feb-2005 : Updated for changes in RectangleConstraint (DG);
 *
 */

package org.jfree.chart.block;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.Size2D;
import org.jfree.ui.VerticalAlignment;

/**
 * Arranges blocks in a flow layout.  This class is immutable.
 */
public class FlowArrangement implements Arrangement, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 4543632485478613800L;

    /** The horizontal alignment of blocks. */
    private HorizontalAlignment horizontalAlignment;

    /** The vertical alignment of blocks within each row. */
    private VerticalAlignment verticalAlignment;

    /** The horizontal gap between items within rows. */
    private double horizontalGap;

    /** The vertical gap between rows. */
    private double verticalGap;

    /**
     * Creates a new instance.
     */
    public FlowArrangement() {
        this(HorizontalAlignment.CENTER, VerticalAlignment.CENTER, 2.0, 2.0);
    }

    /**
     * Creates a new instance.
     *
     * @param hAlign  the horizontal alignment (currently ignored).
     * @param vAlign  the vertical alignment (currently ignored).
     * @param hGap  the horizontal gap.
     * @param vGap  the vertical gap.
     */
    public FlowArrangement(HorizontalAlignment hAlign, VerticalAlignment vAlign,
                           double hGap, double vGap) {
        this.horizontalAlignment = hAlign;
        this.verticalAlignment = vAlign;
        this.horizontalGap = hGap;
        this.verticalGap = vGap;
    }

    /**
     * Adds a block to be managed by this instance.  This method is usually
     * called by the {@link BlockContainer}, you shouldn't need to call it
     * directly.
     *
     * @param block  the block.
     * @param key  a key that controls the position of the block.
     */
    public void add(Block block, Object key) {
        // since the flow layout is relatively straightforward,
        // no information needs to be recorded here
    }

    /**
     * Calculates and sets the bounds of all the items in the specified
     * container, subject to the given constraint.  The <code>Graphics2D</code>
     * can be used by some items (particularly items containing text) to
     * calculate sizing parameters.
     *
     * @param container  the container whose items are being arranged.
     * @param constraint  the size constraint.
     * @param g2  the graphics device.
     *
     * @return The size of the container after arrangement of the contents.
     */
    public Size2D arrange(BlockContainer container, Graphics2D g2,
                          RectangleConstraint constraint) {

        LengthConstraintType w = constraint.getWidthConstraintType();
        LengthConstraintType h = constraint.getHeightConstraintType();
        if (w == LengthConstraintType.NONE) {
            if (h == LengthConstraintType.NONE) {
                return arrangeNN(container, g2);
            }
            else if (h == LengthConstraintType.FIXED) {
                return arrangeNF(container, g2, constraint);
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not implemented.");
            }
        }
        else if (w == LengthConstraintType.FIXED) {
            if (h == LengthConstraintType.NONE) {
                return arrangeFN(container, g2, constraint);
            }
            else if (h == LengthConstraintType.FIXED) {
                return arrangeFF(container, g2, constraint);
            }
            else if (h == LengthConstraintType.RANGE) {
                return arrangeFR(container, g2, constraint);
            }
        }
        else if (w == LengthConstraintType.RANGE) {
            if (h == LengthConstraintType.NONE) {
                return arrangeRN(container, g2, constraint);
            }
            else if (h == LengthConstraintType.FIXED) {
                return arrangeRF(container, g2, constraint);
            }
            else if (h == LengthConstraintType.RANGE) {
                return arrangeRR(container, g2, constraint);
            }
        }
        throw new RuntimeException("Unrecognised constraint type.");

    }

    /**
     * Arranges the blocks in the container with a fixed width and no height
     * constraint.
     *
     * @param container  the container.
     * @param constraint  the constraint.
     * @param g2  the graphics device.
     *
     * @return The size.
     */
    protected Size2D arrangeFN(BlockContainer container, Graphics2D g2,
                               RectangleConstraint constraint) {

        List blocks = container.getBlocks();
        double width = constraint.getWidth();

        double x = 0.0;
        double y = 0.0;
        double maxHeight = 0.0;
        List itemsInRow = new ArrayList();
        for (int i = 0; i < blocks.size(); i++) {
            Block block = (Block) blocks.get(i);
            Size2D size = block.arrange(g2, RectangleConstraint.NONE);
            if (x + size.width <= width) {
                itemsInRow.add(block);
                block.setBounds(
                    new Rectangle2D.Double(x, y, size.width, size.height)
                );
                x = x + size.width + this.horizontalGap;
                maxHeight = Math.max(maxHeight, size.height);
            }
            else {
                if (itemsInRow.isEmpty()) {
                    // place in this row (truncated) anyway
                    block.setBounds(
                        new Rectangle2D.Double(
                            x, y, Math.min(size.width, width - x), size.height
                        )
                    );
                    x = 0.0;
                    y = y + size.height + this.verticalGap;
                }
                else {
                    // start new row
                    itemsInRow.clear();
                    x = 0.0;
                    y = y + maxHeight + this.verticalGap;
                    maxHeight = size.height;
                    block.setBounds(
                        new Rectangle2D.Double(
                            x, y, Math.min(size.width, width), size.height
                        )
                    );
                    x = size.width + this.horizontalGap;
                    itemsInRow.add(block);
                }
            }
        }
        return new Size2D(constraint.getWidth(), y + maxHeight);
    }

    /**
     * Arranges the blocks in the container with a fixed width and a range
     * constraint on the height.
     *
     * @param container  the container.
     * @param constraint  the constraint.
     * @param g2  the graphics device.
     *
     * @return The size following the arrangement.
     */
    protected Size2D arrangeFR(BlockContainer container, Graphics2D g2,
                               RectangleConstraint constraint) {

        Size2D s = arrangeFN(container, g2, constraint);
        if (constraint.getHeightRange().contains(s.height)) {
            return s;
        }
        else {
            RectangleConstraint c = constraint.toFixedHeight(
                constraint.getHeightRange().constrain(s.getHeight())
            );
            return arrangeFF(container, g2, c);
        }
    }

    /**
     * Arranges the blocks in the container with the overall height and width
     * specified as fixed constraints.
     *
     * @param container  the container.
     * @param constraint  the constraint.
     * @param g2  the graphics device.
     *
     * @return The size following the arrangement.
     */
    protected Size2D arrangeFF(BlockContainer container, Graphics2D g2,
                               RectangleConstraint constraint) {

        // TODO: implement this properly
        return arrangeFN(container, g2, constraint);
    }

    /**
     * Arranges the blocks with the overall width and height to fit within
     * specified ranges.
     *
     * @param container  the container.
     * @param constraint  the constraint.
     * @param g2  the graphics device.
     *
     * @return The size after the arrangement.
     */
    protected Size2D arrangeRR(BlockContainer container, Graphics2D g2,
                               RectangleConstraint constraint) {

        // first arrange without constraints, and see if this fits within
        // the required ranges...
        Size2D s1 = arrangeNN(container, g2);
        if (constraint.getWidthRange().contains(s1.width)) {
            return s1;  // TODO: we didn't check the height yet
        }
        else {
            RectangleConstraint c = constraint.toFixedWidth(
                constraint.getWidthRange().getUpperBound()
            );
            return arrangeFR(container, g2, c);
        }
    }

    /**
     * Arranges the blocks in the container with a range constraint on the
     * width and a fixed height.
     *
     * @param container  the container.
     * @param constraint  the constraint.
     * @param g2  the graphics device.
     *
     * @return The size following the arrangement.
     */
    protected Size2D arrangeRF(BlockContainer container, Graphics2D g2,
                               RectangleConstraint constraint) {

        Size2D s = arrangeNF(container, g2, constraint);
        if (constraint.getWidthRange().contains(s.width)) {
            return s;
        }
        else {
            RectangleConstraint c = constraint.toFixedWidth(
                constraint.getWidthRange().constrain(s.getWidth())
            );
            return arrangeFF(container, g2, c);
        }
    }

    /**
     * Arranges the block with a range constraint on the width, and no
     * constraint on the height.
     *
     * @param container  the container.
     * @param constraint  the constraint.
     * @param g2  the graphics device.
     *
     * @return The size following the arrangement.
     */
    protected Size2D arrangeRN(BlockContainer container, Graphics2D g2,
                               RectangleConstraint constraint) {
        // first arrange without constraints, then see if the width fits
        // within the required range...if not, call arrangeFN() at max width
        Size2D s1 = arrangeNN(container, g2);
        if (constraint.getWidthRange().contains(s1.width)) {
            return s1;
        }
        else {
            RectangleConstraint c = constraint.toFixedWidth(
                constraint.getWidthRange().getUpperBound()
            );
            return arrangeFN(container, g2, c);
        }
    }

    /**
     * Arranges the blocks without any constraints.  This puts all blocks
     * into a single row.
     *
     * @param container  the container.
     * @param g2  the graphics device.
     *
     * @return The size after the arrangement.
     */
    protected Size2D arrangeNN(BlockContainer container, Graphics2D g2) {
        double x = 0.0;
        double width = 0.0;
        double maxHeight = 0.0;
        List blocks = container.getBlocks();
        int blockCount = blocks.size();
        if (blockCount > 0) {
            Size2D[] sizes = new Size2D[blocks.size()];
            for (int i = 0; i < blocks.size(); i++) {
                Block block = (Block) blocks.get(i);
                sizes[i] = block.arrange(g2, RectangleConstraint.NONE);
                width = width + sizes[i].getWidth();
                maxHeight = Math.max(sizes[i].height, maxHeight);
                block.setBounds(
                    new Rectangle2D.Double(
                        x, 0.0, sizes[i].width, sizes[i].height
                    )
                );
                x = x + sizes[i].width + this.horizontalGap;
            }
            if (blockCount > 1) {
                width = width + this.horizontalGap * (blockCount - 1);
            }
            if (this.verticalAlignment != VerticalAlignment.TOP) {
                for (int i = 0; i < blocks.size(); i++) {
                    //Block b = (Block) blocks.get(i);
                    if (this.verticalAlignment == VerticalAlignment.CENTER) {
                        //TODO: shift block down by half
                    }
                    else if (this.verticalAlignment
                            == VerticalAlignment.BOTTOM) {
                        //TODO: shift block down to bottom
                    }
                }
            }
        }
        return new Size2D(width, maxHeight);
    }

    /**
     * Arranges the blocks with no width constraint and a fixed height
     * constraint.  This puts all blocks into a single row.
     *
     * @param container  the container.
     * @param constraint  the constraint.
     * @param g2  the graphics device.
     *
     * @return The size after the arrangement.
     */
    protected Size2D arrangeNF(BlockContainer container, Graphics2D g2,
                               RectangleConstraint constraint) {
        // TODO: for now we are ignoring the height constraint
        return arrangeNN(container, g2);
    }

    /**
     * Clears any cached information.
     */
    public void clear() {
        // no action required.
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
        if (!(obj instanceof FlowArrangement)) {
            return false;
        }
        FlowArrangement that = (FlowArrangement) obj;
        if (this.horizontalAlignment != that.horizontalAlignment) {
            return false;
        }
        if (this.verticalAlignment != that.verticalAlignment) {
            return false;
        }
        if (this.horizontalGap != that.horizontalGap) {
            return false;
        }
        if (this.verticalGap != that.verticalGap) {
            return false;
        }
        return true;
    }

}
