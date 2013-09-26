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
 * CenterArrangement.java
 * ----------------------
 * (C) Copyright 2005-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 08-Mar-2005 : Version 1 (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 20-Jul-2006 : Set bounds of contained block when arranging (DG);
 *
 */

package org.jfree.chart.block;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.List;

import org.jfree.ui.Size2D;

/**
 * Arranges a block in the center of its container.  This class is immutable.
 */
public class CenterArrangement implements Arrangement, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -353308149220382047L;

    /**
     * Creates a new instance.
     */
    public CenterArrangement() {
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
                return arrangeFN(container, g2, constraint);
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not implemented.");
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not implemented.");
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
        throw new IllegalArgumentException("Unknown LengthConstraintType.");

    }

    /**
     * Arranges the blocks in the container with a fixed width and no height
     * constraint.
     *
     * @param container  the container.
     * @param g2  the graphics device.
     * @param constraint  the constraint.
     *
     * @return The size.
     */
    protected Size2D arrangeFN(BlockContainer container, Graphics2D g2,
                               RectangleConstraint constraint) {

        List blocks = container.getBlocks();
        Block b = (Block) blocks.get(0);
        Size2D s = b.arrange(g2, RectangleConstraint.NONE);
        double width = constraint.getWidth();
        Rectangle2D bounds = new Rectangle2D.Double((width - s.width) / 2.0,
                0.0, s.width, s.height);
        b.setBounds(bounds);
        return new Size2D((width - s.width) / 2.0, s.height);
    }

    /**
     * Arranges the blocks in the container with a fixed with and a range
     * constraint on the height.
     *
     * @param container  the container.
     * @param g2  the graphics device.
     * @param constraint  the constraint.
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
                    constraint.getHeightRange().constrain(s.getHeight()));
            return arrangeFF(container, g2, c);
        }
    }

    /**
     * Arranges the blocks in the container with the overall height and width
     * specified as fixed constraints.
     *
     * @param container  the container.
     * @param g2  the graphics device.
     * @param constraint  the constraint.
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
     * @param g2  the graphics device.
     * @param constraint  the constraint.
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
                    constraint.getWidthRange().getUpperBound());
            return arrangeFR(container, g2, c);
        }
    }

    /**
     * Arranges the blocks in the container with a range constraint on the
     * width and a fixed height.
     *
     * @param container  the container.
     * @param g2  the graphics device.
     * @param constraint  the constraint.
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
                    constraint.getWidthRange().constrain(s.getWidth()));
            return arrangeFF(container, g2, c);
        }
    }

    /**
     * Arranges the block with a range constraint on the width, and no
     * constraint on the height.
     *
     * @param container  the container.
     * @param g2  the graphics device.
     * @param constraint  the constraint.
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
                    constraint.getWidthRange().getUpperBound());
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
        List blocks = container.getBlocks();
        Block b = (Block) blocks.get(0);
        Size2D s = b.arrange(g2, RectangleConstraint.NONE);
        b.setBounds(new Rectangle2D.Double(0.0, 0.0, s.width, s.height));
        return new Size2D(s.width, s.height);
    }

    /**
     * Arranges the blocks with no width constraint and a fixed height
     * constraint.  This puts all blocks into a single row.
     *
     * @param container  the container.
     * @param g2  the graphics device.
     * @param constraint  the constraint.
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
        if (!(obj instanceof CenterArrangement)) {
            return false;
        }
        return true;
    }

}
