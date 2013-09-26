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
 * ------------
 * Outlier.java
 * ------------
 * (C) Copyright 2003-2008, by David Browning and Contributors.
 *
 * Original Author:  David Browning (for Australian Institute of Marine
 *                   Science);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 05-Aug-2003 : Version 1, contributed by David Browning (DG);
 * 28-Aug-2003 : Minor tidy-up (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags from all over JFreeChart sources (DG);
 * 21-Nov-2007 : Implemented equals() to shut up FindBugs (DG);
 *
 */

package org.jfree.chart.renderer;

import java.awt.geom.Point2D;

/**
 * Represents one outlier in the box and whisker plot.
 * <P>
 * All the coordinates in this class are in Java2D space.
 */
public class Outlier implements Comparable {

    /**
     * The xy coordinates of the bounding box containing the outlier ellipse.
     */
    private Point2D point;

    /** The radius of the ellipse */
    private double radius;

    /**
     * Constructs an outlier item consisting of a point and the radius of the
     * outlier ellipse
     *
     * @param xCoord  the x coordinate of the point.
     * @param yCoord  the y coordinate of the point.
     * @param radius  the radius of the ellipse.
     */
    public Outlier(double xCoord, double yCoord, double radius) {
        this.point = new Point2D.Double(xCoord - radius, yCoord - radius);
        this.radius = radius;
    }

    /**
     * Returns the xy coordinates of the bounding box containing the outlier
     * ellipse.
     *
     * @return The location of the outlier ellipse.
     */
    public Point2D getPoint() {
        return this.point;
    }

    /**
     * Sets the xy coordinates of the bounding box containing the outlier
     * ellipse.
     *
     * @param point  the location.
     */
    public void setPoint(Point2D point) {
        this.point = point;
    }

    /**
     * Returns the x coordinate of the bounding box containing the outlier
     * ellipse.
     *
     * @return The x coordinate.
     */
    public double getX() {
        return getPoint().getX();
    }

    /**
     * Returns the y coordinate of the bounding box containing the outlier
     * ellipse.
     *
     * @return The y coordinate.
     */
    public double getY() {
        return getPoint().getY();
    }

    /**
     * Returns the radius of the outlier ellipse.
     *
     * @return The radius.
     */
    public double getRadius() {
        return this.radius;
    }

    /**
     * Sets the radius of the outlier ellipse.
     *
     * @param radius  the new radius.
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * Compares this object with the specified object for order, based on
     * the outlier's point.
     *
     * @param   o the Object to be compared.
     * @return A negative integer, zero, or a positive integer as this object
     *      is less than, equal to, or greater than the specified object.
     *
     */
    public int compareTo(Object o) {
        Outlier outlier = (Outlier) o;
        Point2D p1 = getPoint();
        Point2D p2 = outlier.getPoint();
        if (p1.equals(p2)) {
            return 0;
        }
        else if ((p1.getX() < p2.getX()) || (p1.getY() < p2.getY())) {
            return -1;
        }
        else {
            return 1;
        }
    }

    /**
     * Returns a true if outlier is overlapped and false if it is not.
     * Overlapping is determined by the respective bounding boxes plus
     * a small margin.
     *
     * @param other  the other outlier.
     *
     * @return A <code>boolean</code> indicating whether or not an overlap has
     *         occurred.
     */
    public boolean overlaps(Outlier other) {
        return ((other.getX() >= getX() - (this.radius * 1.1))
                && (other.getX() <= getX() + (this.radius * 1.1))
                && (other.getY() >= getY() - (this.radius * 1.1))
                && (other.getY() <= getY() + (this.radius * 1.1)));
    }

    /**
     * Tests this outlier for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Outlier)) {
            return false;
        }
        Outlier that = (Outlier) obj;
        if (!this.point.equals(that.point)) {
            return false;
        }
        if (this.radius != that.radius) {
            return false;
        }
        return true;
    }

    /**
     * Returns a textual representation of the outlier.
     *
     * @return A <code>String</code> representing the outlier.
     */
    public String toString() {
        return "{" + getX() + "," + getY() + "}";
    }

}
