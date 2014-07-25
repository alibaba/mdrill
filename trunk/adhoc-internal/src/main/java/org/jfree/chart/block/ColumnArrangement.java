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
 * ColumnArrangement.java
 * ----------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 22-Oct-2004 : Version 1 (DG);
 * 04-Feb-2005 : Added equals() and implemented Serializable (DG);
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
 * Arranges blocks in a column layout.  This class is immutable.
 */
public class ColumnArrangement implements Arrangement, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -5315388482898581555L;

    /** The horizontal alignment of blocks. */
    private HorizontalAlignment horizontalAlignment;

    /** The vertical alignment of blocks within each row. */
    private VerticalAlignment verticalAlignment;

    /** The horizontal gap between columns. */
    private double horizontalGap;

    /** The vertical gap between items in a column. */
    private double verticalGap;

    /**
     * Creates a new instance.
     */
    public ColumnArrangement() {
    }

    /**
     * Creates a new instance.
     *
     * @param hAlign  the horizontal alignment (currently ignored).
     * @param vAlign  the vertical alignment (currently ignored).
     * @param hGap  the horizontal gap.
     * @param vGap  the vertical gap.
     */
    public ColumnArrangement(HorizontalAlignment hAlign,
                             VerticalAlignment vAlign,
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
        // since the flow layout is relatively straightforward, no information
        // needs to be recorded here
    }

    /**
     * Calculates and sets the bounds of all the items in the specified
     * container, subject to the given constraint.  The <code>Graphics2D</code>
     * can be used by some items (particularly items containing text) to
     * calculate sizing parameters.
     *
     * @param container  the container whose items are being arranged.
     * @param g2  the graphics device.
     * @param constraint  the size constraint.
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
                throw new RuntimeException("Not implemented.");
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not implemented.");
            }
        }
        else if (w == LengthConstraintType.FIXED) {
            if (h == LengthConstraintType.NONE) {
                throw new RuntimeException("Not implemented.");
            }
            else if (h == LengthConstraintType.FIXED) {
                return arrangeFF(container, g2, constraint);
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not implemented.");
            }
        }
        else if (w == LengthConstraintType.RANGE) {
            if (h == LengthConstraintType.NONE) {
                throw new RuntimeException("Not implemented.");
            }
            else if (h == LengthConstraintType.FIXED) {
                return arrangeRF(container, g2, constraint);
            }
            else if (h == LengthConstraintType.RANGE) {
                return arrangeRR(container, g2, constraint);
            }
        }
        return new Size2D();  // TODO: complete this

    }

    /**
     * Calculates and sets the bounds of all the items in the specified
     * container, subject to the given constraint.  The <code>Graphics2D</code>
     * can be used by some items (particularly items containing text) to
     * calculate sizing parameters.
     *
     * @param container  the container whose items are being arranged.
     * @param g2  the graphics device.
     * @param constraint  the size constraint.
     *
     * @return The container size after the arrangement.
     */
    protected Size2D arrangeFF(BlockContainer container, Graphics2D g2,
                               RectangleConstraint constraint) {
        // TODO: implement properly
        return arrangeNF(container, g2, constraint);
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
     * @return The container size after the arrangement.
     */
    protected Size2D arrangeNF(BlockContainer container, Graphics2D g2,
                               RectangleConstraint constraint) {

        List blocks = container.getBlocks();

        double height = constraint.getHeight();
        if (height <= 0.0) {
            height = Double.POSITIVE_INFINITY;
        }

        double x = 0.0;
        double y = 0.0;
        double maxWidth = 0.0;
        List itemsInColumn = new ArrayList();
        for (int i = 0; i < blocks.size(); i++) {
            Block block = (Block) blocks.get(i);
            Size2D size = block.arrange(g2, RectangleConstraint.NONE);
            if (y + size.height <= height) {
                itemsInColumn.add(block);
                block.setBounds(
                    new Rectangle2D.Double(x, y, size.width, size.height)
                );
                y = y + size.height + this.verticalGap;
                maxWidth = Math.max(maxWidth, size.width);
            }
            else {
                if (itemsInColumn.isEmpty()) {
                    // place in this column (truncated) anyway
                    block.setBounds(
                        new Rectangle2D.Double(
                            x, y, size.width, Math.min(size.height, height - y)
                        )
                    );
                    y = 0.0;
                    x = x + size.width + this.horizontalGap;
                }
                else {
                    // start new column
                    itemsInColumn.clear();
                    x = x + maxWidth + this.horizontalGap;
                    y = 0.0;
                    maxWidth = size.width;
                    block.setBounds(
                        new Rectangle2D.Double(
                            x, y, size.width, Math.min(size.height, height)
                        )
                    );
                    y = size.height + this.verticalGap;
                    itemsInColumn.add(block);
                }
            }
        }
        return new Size2D(x + maxWidth, constraint.getHeight());
    }

    /**
     * Arranges a container with range constraints for both the horizontal
     * and vertical.
     *
     * @param container  the container.
     * @param g2  the graphics device.
     * @param constraint  the constraint.
     *
     * @return The size of the container.
     */
    protected Size2D arrangeRR(BlockContainer container, Graphics2D g2,
                               RectangleConstraint constraint) {

        // first arrange without constraints, and see if this fits within
        // the required ranges...
        Size2D s1 = arrangeNN(container, g2);
        if (constraint.getHeightRange().contains(s1.height)) {
            return s1;  // TODO: we didn't check the width yet
        }
        else {
            RectangleConstraint c = constraint.toFixedHeight(
                constraint.getHeightRange().getUpperBound()
            );
            return arrangeRF(container, g2, c);
        }
    }

    /**
     * Arranges the blocks in the container using a fixed height and a
     * range for the width.
     *
     * @param container  the container.
     * @param g2  the graphics device.
     * @param constraint  the constraint.
     *
     * @return The size of the container after arrangement.
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
     * Arranges the blocks without any constraints.  This puts all blocks
     * into a single column.
     *
     * @param container  the container.
     * @param g2  the graphics device.
     *
     * @return The size after the arrangement.
     */
    protected Size2D arrangeNN(BlockContainer container, Graphics2D g2) {
        double y = 0.0;
        double height = 0.0;
        double maxWidth = 0.0;
        List blocks = container.getBlocks();
        int blockCount = blocks.size();
        if (blockCount > 0) {
            Size2D[] sizes = new Size2D[blocks.size()];
            for (int i = 0; i < blocks.size(); i++) {
                Block block = (Block) blocks.get(i);
                sizes[i] = block.arrange(g2, RectangleConstraint.NONE);
                height = height + sizes[i].getHeight();
                maxWidth = Math.max(sizes[i].width, maxWidth);
                block.setBounds(
                    new Rectangle2D.Double(
                        0.0, y, sizes[i].width, sizes[i].height
                    )
                );
                y = y + sizes[i].height + this.verticalGap;
            }
            if (blockCount > 1) {
                height = height + this.verticalGap * (blockCount - 1);
            }
            if (this.horizontalAlignment != HorizontalAlignment.LEFT) {
                for (int i = 0; i < blocks.size(); i++) {
                    //Block b = (Block) blocks.get(i);
                    if (this.horizontalAlignment
                            == HorizontalAlignment.CENTER) {
                        //TODO: shift block right by half
                    }
                    else if (this.horizontalAlignment
                            == HorizontalAlignment.RIGHT) {
                        //TODO: shift block over to right
                    }
                }
            }
        }
        return new Size2D(maxWidth, height);
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
        if (!(obj instanceof ColumnArrangement)) {
            return false;
        }
        ColumnArrangement that = (ColumnArrangement) obj;
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
