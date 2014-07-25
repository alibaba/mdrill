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
 * --------------
 * AxisSpace.java
 * --------------
 * (C) Copyright 2003-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 03-Jul-2003 : Version 1 (DG);
 * 14-Aug-2003 : Implemented Cloneable (DG);
 * 18-Aug-2003 : Implemented Serializable (DG);
 * 17-Mar-2004 : Added a toString() method for debugging (DG);
 * 07-Jan-2005 : Updated equals() method (DG);
 * 11-Jan-2005 : Removed deprecated methods in preparation for 1.0.0
 *               release (DG);
 *
 */

package org.jfree.chart.axis;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.ui.RectangleEdge;
import org.jfree.util.PublicCloneable;

/**
 * A record that contains the space required at each edge of a plot.
 */
public class AxisSpace implements Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -2490732595134766305L;

    /** The top space. */
    private double top;

    /** The bottom space. */
    private double bottom;

    /** The left space. */
    private double left;

    /** The right space. */
    private double right;

    /**
     * Creates a new axis space record.
     */
    public AxisSpace() {
        this.top = 0.0;
        this.bottom = 0.0;
        this.left = 0.0;
        this.right = 0.0;
    }

    /**
     * Returns the space reserved for axes at the top of the plot area.
     *
     * @return The space (in Java2D units).
     */
    public double getTop() {
        return this.top;
    }

    /**
     * Sets the space reserved for axes at the top of the plot area.
     *
     * @param space  the space (in Java2D units).
     */
    public void setTop(double space) {
        this.top = space;
    }

    /**
     * Returns the space reserved for axes at the bottom of the plot area.
     *
     * @return The space (in Java2D units).
     */
    public double getBottom() {
        return this.bottom;
    }

    /**
     * Sets the space reserved for axes at the bottom of the plot area.
     *
     * @param space  the space (in Java2D units).
     */
    public void setBottom(double space) {
        this.bottom = space;
    }

    /**
     * Returns the space reserved for axes at the left of the plot area.
     *
     * @return The space (in Java2D units).
     */
    public double getLeft() {
        return this.left;
    }

    /**
     * Sets the space reserved for axes at the left of the plot area.
     *
     * @param space  the space (in Java2D units).
     */
    public void setLeft(double space) {
        this.left = space;
    }

    /**
     * Returns the space reserved for axes at the right of the plot area.
     *
     * @return The space (in Java2D units).
     */
    public double getRight() {
        return this.right;
    }

    /**
     * Sets the space reserved for axes at the right of the plot area.
     *
     * @param space  the space (in Java2D units).
     */
    public void setRight(double space) {
        this.right = space;
    }

    /**
     * Adds space to the top, bottom, left or right edge of the plot area.
     *
     * @param space  the space (in Java2D units).
     * @param edge  the edge (<code>null</code> not permitted).
     */
    public void add(double space, RectangleEdge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("Null 'edge' argument.");
        }
        if (edge == RectangleEdge.TOP) {
            this.top += space;
        }
        else if (edge == RectangleEdge.BOTTOM) {
            this.bottom += space;
        }
        else if (edge == RectangleEdge.LEFT) {
            this.left += space;
        }
        else if (edge == RectangleEdge.RIGHT) {
            this.right += space;
        }
        else {
            throw new IllegalStateException("Unrecognised 'edge' argument.");
        }
    }

    /**
     * Ensures that this object reserves at least as much space as another.
     *
     * @param space  the other space.
     */
    public void ensureAtLeast(AxisSpace space) {
        this.top = Math.max(this.top, space.top);
        this.bottom = Math.max(this.bottom, space.bottom);
        this.left = Math.max(this.left, space.left);
        this.right = Math.max(this.right, space.right);
    }

    /**
     * Ensures there is a minimum amount of space at the edge corresponding to
     * the specified axis location.
     *
     * @param space  the space.
     * @param edge  the location.
     */
    public void ensureAtLeast(double space, RectangleEdge edge) {
        if (edge == RectangleEdge.TOP) {
            if (this.top < space) {
                this.top = space;
            }
        }
        else if (edge == RectangleEdge.BOTTOM) {
            if (this.bottom < space) {
                this.bottom = space;
            }
        }
        else if (edge == RectangleEdge.LEFT) {
            if (this.left < space) {
                this.left = space;
            }
        }
        else if (edge == RectangleEdge.RIGHT) {
            if (this.right < space) {
                this.right = space;
            }
        }
        else {
            throw new IllegalStateException(
                "AxisSpace.ensureAtLeast(): unrecognised AxisLocation."
            );
        }
    }

    /**
     * Shrinks an area by the space attributes.
     *
     * @param area  the area to shrink.
     * @param result  an optional carrier for the result.
     *
     * @return The result.
     */
    public Rectangle2D shrink(Rectangle2D area, Rectangle2D result) {
        if (result == null) {
            result = new Rectangle2D.Double();
        }
        result.setRect(
            area.getX() + this.left,
            area.getY() + this.top,
            area.getWidth() - this.left - this.right,
            area.getHeight() - this.top - this.bottom
        );
        return result;
    }

    /**
     * Expands an area by the amount of space represented by this object.
     *
     * @param area  the area to expand.
     * @param result  an optional carrier for the result.
     *
     * @return The result.
     */
    public Rectangle2D expand(Rectangle2D area, Rectangle2D result) {
        if (result == null) {
            result = new Rectangle2D.Double();
        }
        result.setRect(
            area.getX() - this.left,
            area.getY() - this.top,
            area.getWidth() + this.left + this.right,
            area.getHeight() + this.top + this.bottom
        );
        return result;
    }

    /**
     * Calculates the reserved area.
     *
     * @param area  the area.
     * @param edge  the edge.
     *
     * @return The reserved area.
     */
    public Rectangle2D reserved(Rectangle2D area, RectangleEdge edge) {
        Rectangle2D result = null;
        if (edge == RectangleEdge.TOP) {
            result = new Rectangle2D.Double(
                area.getX(), area.getY(), area.getWidth(), this.top
            );
        }
        else if (edge == RectangleEdge.BOTTOM) {
            result = new Rectangle2D.Double(
                area.getX(), area.getMaxY() - this.top,
                area.getWidth(), this.bottom
            );
        }
        else if (edge == RectangleEdge.LEFT) {
            result = new Rectangle2D.Double(
                area.getX(), area.getY(), this.left, area.getHeight()
            );
        }
        else if (edge == RectangleEdge.RIGHT) {
            result = new Rectangle2D.Double(
                area.getMaxX() - this.right, area.getY(),
                this.right, area.getHeight()
            );
        }
        return result;
    }

    /**
     * Returns a clone of the object.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException This class won't throw this exception,
     *         but subclasses (if any) might.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Tests this object for equality with another object.
     *
     * @param obj  the object to compare against.
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AxisSpace)) {
            return false;
        }
        AxisSpace that = (AxisSpace) obj;
        if (this.top != that.top) {
            return false;
        }
        if (this.bottom != that.bottom) {
            return false;
        }
        if (this.left != that.left) {
            return false;
        }
        if (this.right != that.right) {
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
        int result = 23;
        long l = Double.doubleToLongBits(this.top);
        result = 37 * result + (int) (l ^ (l >>> 32));
        l = Double.doubleToLongBits(this.bottom);
        result = 37 * result + (int) (l ^ (l >>> 32));
        l = Double.doubleToLongBits(this.left);
        result = 37 * result + (int) (l ^ (l >>> 32));
        l = Double.doubleToLongBits(this.right);
        result = 37 * result + (int) (l ^ (l >>> 32));
        return result;
    }

    /**
     * Returns a string representing the object (for debugging purposes).
     *
     * @return A string.
     */
    public String toString() {
        return super.toString() + "[left=" + this.left + ",right=" + this.right
                    + ",top=" + this.top + ",bottom=" + this.bottom + "]";
    }

}
