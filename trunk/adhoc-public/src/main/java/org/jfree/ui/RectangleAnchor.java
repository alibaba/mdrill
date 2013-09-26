/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
 * 
 * Project Info:  http://www.jfree.org/jcommon/index.html
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
 * RectangleAnchor.java
 * --------------------
 * (C) Copyright 2003-2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: RectangleAnchor.java,v 1.6 2005/10/18 13:18:34 mungady Exp $
 *
 * Changes:
 * --------
 * 31-Oct-2003 (DG);
 * 01-Apr-2004 : Changed java.awt.geom.Dimension2D to org.jfree.ui.Size2D 
 *               because of JDK bug 4976448 which persists on JDK 1.3.1 (DG);
 * 21-Jan-2005 : Changed return type of coordinates() method (DG);
 * 
 */

package org.jfree.ui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Used to indicate an anchor point for a rectangle.
 *
 * @author David Gilbert
 */
public final class RectangleAnchor implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -2457494205644416327L;
    
    /** Center. */
    public static final RectangleAnchor CENTER 
        = new RectangleAnchor("RectangleAnchor.CENTER");

    /** Top. */
    public static final RectangleAnchor TOP 
        = new RectangleAnchor("RectangleAnchor.TOP");

    /** Top-Left. */
    public static final RectangleAnchor TOP_LEFT 
        = new RectangleAnchor("RectangleAnchor.TOP_LEFT");

    /** Top-Right. */
    public static final RectangleAnchor TOP_RIGHT 
        = new RectangleAnchor("RectangleAnchor.TOP_RIGHT");

    /** Bottom. */
    public static final RectangleAnchor BOTTOM 
        = new RectangleAnchor("RectangleAnchor.BOTTOM");

    /** Bottom-Left. */
    public static final RectangleAnchor BOTTOM_LEFT 
        = new RectangleAnchor("RectangleAnchor.BOTTOM_LEFT");

    /** Bottom-Right. */
    public static final RectangleAnchor BOTTOM_RIGHT 
        = new RectangleAnchor("RectangleAnchor.BOTTOM_RIGHT");

    /** Left. */
    public static final RectangleAnchor LEFT 
        = new RectangleAnchor("RectangleAnchor.LEFT");

    /** Right. */
    public static final RectangleAnchor RIGHT 
        = new RectangleAnchor("RectangleAnchor.RIGHT");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private RectangleAnchor(final String name) {
        this.name = name;
    }

    /**
     * Returns a string representing the object.
     *
     * @return The string.
     */
    public String toString() {
        return this.name;
    }

    /**
     * Returns <code>true</code> if this object is equal to the specified 
     * object, and <code>false</code> otherwise.
     *
     * @param obj  the other object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RectangleAnchor)) {
            return false;
        }

        final RectangleAnchor order = (RectangleAnchor) obj;
        if (!this.name.equals(order.name)) {
            return false;
        }

        return true;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return The hashcode
     */
    public int hashCode() {
        return this.name.hashCode();
    }

    /**
     * Returns the (x, y) coordinates of the specified anchor.
     * 
     * @param rectangle  the rectangle.
     * @param anchor  the anchor.
     * 
     * @return The (x, y) coordinates.
     */
    public static Point2D coordinates(final Rectangle2D rectangle, 
                                      final RectangleAnchor anchor) {
        Point2D result = new Point2D.Double();
        if (anchor == RectangleAnchor.CENTER) {
            result.setLocation(rectangle.getCenterX(), rectangle.getCenterY());
        }
        else if (anchor == RectangleAnchor.TOP) {
            result.setLocation(rectangle.getCenterX(), rectangle.getMinY());
        }
        else if (anchor == RectangleAnchor.BOTTOM) {
            result.setLocation(rectangle.getCenterX(), rectangle.getMaxY());
        }
        else if (anchor == RectangleAnchor.LEFT) {
            result.setLocation(rectangle.getMinX(), rectangle.getCenterY());
        }
        else if (anchor == RectangleAnchor.RIGHT) {
            result.setLocation(rectangle.getMaxX(), rectangle.getCenterY());
        }
        else if (anchor == RectangleAnchor.TOP_LEFT) {
            result.setLocation(rectangle.getMinX(), rectangle.getMinY());
        }
        else if (anchor == RectangleAnchor.TOP_RIGHT) {
            result.setLocation(rectangle.getMaxX(), rectangle.getMinY());
        }
        else if (anchor == RectangleAnchor.BOTTOM_LEFT) {
            result.setLocation(rectangle.getMinX(), rectangle.getMaxY());
        }
        else if (anchor == RectangleAnchor.BOTTOM_RIGHT) {
            result.setLocation(rectangle.getMaxX(), rectangle.getMaxY());
        }
        return result;
    }
    
    /**
     * Creates a new rectangle with the specified dimensions that is aligned to
     * the given anchor point <code>(anchorX, anchorY)</code>.
     * 
     * @param dimensions  the dimensions (<code>null</code> not permitted).
     * @param anchorX  the x-anchor.
     * @param anchorY  the y-anchor.
     * @param anchor  the anchor (<code>null</code> not permitted).
     * 
     * @return A rectangle.
     */
    public static Rectangle2D createRectangle(final Size2D dimensions,
                                              final double anchorX,
                                              final double anchorY,
                                              final RectangleAnchor anchor) {
        Rectangle2D result = null;
        final double w = dimensions.getWidth();
        final double h = dimensions.getHeight();
        if (anchor == RectangleAnchor.CENTER) {
            result = new Rectangle2D.Double(
                anchorX - w / 2.0, anchorY - h / 2.0, w, h
            );
        }
        else if (anchor == RectangleAnchor.TOP) {
            result = new Rectangle2D.Double(
                anchorX - w / 2.0, anchorY - h / 2.0, w, h
            );
        }
        else if (anchor == RectangleAnchor.BOTTOM) {
            result = new Rectangle2D.Double(
                anchorX - w / 2.0, anchorY - h / 2.0, w, h
            );
        }
        else if (anchor == RectangleAnchor.LEFT) {
            result = new Rectangle2D.Double(
                anchorX, anchorY - h / 2.0, w, h
            );
        }
        else if (anchor == RectangleAnchor.RIGHT) {
            result = new Rectangle2D.Double(
                anchorX - w, anchorY - h / 2.0, w, h
            );
        }
        else if (anchor == RectangleAnchor.TOP_LEFT) {
            result = new Rectangle2D.Double(
                anchorX - w / 2.0, anchorY - h / 2.0, w, h
            );
        }
        else if (anchor == RectangleAnchor.TOP_RIGHT) {
            result = new Rectangle2D.Double(
                anchorX - w / 2.0, anchorY - h / 2.0, w, h
            );
        }
        else if (anchor == RectangleAnchor.BOTTOM_LEFT) {
            result = new Rectangle2D.Double(
                anchorX - w / 2.0, anchorY - h / 2.0, w, h
            );
        }
        else if (anchor == RectangleAnchor.BOTTOM_RIGHT) {
            result = new Rectangle2D.Double(
                anchorX - w / 2.0, anchorY - h / 2.0, w, h
            );
        }
        return result;
    }
    
    /**
     * Ensures that serialization returns the unique instances.
     * 
     * @return The object.
     * 
     * @throws ObjectStreamException if there is a problem.
     */
    private Object readResolve() throws ObjectStreamException {
        RectangleAnchor result = null;
        if (this.equals(RectangleAnchor.CENTER)) {
            result = RectangleAnchor.CENTER;
        }
        else if (this.equals(RectangleAnchor.TOP)) {
            result = RectangleAnchor.TOP;
        }
        else if (this.equals(RectangleAnchor.BOTTOM)) {
            result = RectangleAnchor.BOTTOM;
        }
        else if (this.equals(RectangleAnchor.LEFT)) {
            result = RectangleAnchor.LEFT;
        }
        else if (this.equals(RectangleAnchor.RIGHT)) {
            result = RectangleAnchor.RIGHT;
        }
        else if (this.equals(RectangleAnchor.TOP_LEFT)) {
            result = RectangleAnchor.TOP_LEFT;
        }
        else if (this.equals(RectangleAnchor.TOP_RIGHT)) {
            result = RectangleAnchor.TOP_RIGHT;
        }
        else if (this.equals(RectangleAnchor.BOTTOM_LEFT)) {
            result = RectangleAnchor.BOTTOM_LEFT;
        }
        else if (this.equals(RectangleAnchor.BOTTOM_RIGHT)) {
            result = RectangleAnchor.BOTTOM_RIGHT;
        }
        return result;
    }
    
}
