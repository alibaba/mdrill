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
 * ----------
 * Block.java
 * ----------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 22-Oct-2004 : Version 1 (DG);
 * 08-Feb-2005 : Added ID (DG);
 * 20-Apr-2005 : Added a new draw() method that can accept params
 *               and return results (DG);
 *
 */

package org.jfree.chart.block;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.ui.Drawable;
import org.jfree.ui.Size2D;

/**
 * A block is an arbitrary item that can be drawn (in Java2D space) within a
 * rectangular area, has a preferred size, and can be arranged by an
 * {@link Arrangement} manager.
 */
public interface Block extends Drawable {

    /**
     * Returns an ID for the block.
     *
     * @return An ID.
     */
    public String getID();

    /**
     * Sets the ID for the block.
     *
     * @param id  the ID.
     */
    public void setID(String id);

    /**
     * Arranges the contents of the block, with no constraints, and returns
     * the block size.
     *
     * @param g2  the graphics device.
     *
     * @return The size of the block.
     */
    public Size2D arrange(Graphics2D g2);

    /**
     * Arranges the contents of the block, within the given constraints, and
     * returns the block size.
     *
     * @param g2  the graphics device.
     * @param constraint  the constraint (<code>null</code> not permitted).
     *
     * @return The block size (in Java2D units, never <code>null</code>).
     */
    public Size2D arrange(Graphics2D g2, RectangleConstraint constraint);

    /**
     * Returns the current bounds of the block.
     *
     * @return The bounds.
     */
    public Rectangle2D getBounds();

    /**
     * Sets the bounds of the block.
     *
     * @param bounds  the bounds.
     */
    public void setBounds(Rectangle2D bounds);

    /**
     * Draws the block within the specified area.  Refer to the documentation
     * for the implementing class for information about the <code>params</code>
     * and return value supported.
     *
     * @param g2  the graphics device.
     * @param area  the area.
     * @param params  optional parameters (<code>null</code> permitted).
     *
     * @return An optional return value (possibly <code>null</code>).
     */
    public Object draw(Graphics2D g2, Rectangle2D area, Object params);

}
