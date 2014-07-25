/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
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
 * -------------------
 * ShapeUtilities.java
 * -------------------
 * (C)opyright 2003-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: ShapeUtilities.java,v 1.18 2008/06/02 06:58:28 mungady Exp $
 *
 * Changes
 * -------
 * 13-Aug-2003 : Version 1 (DG);
 * 16-Mar-2004 : Moved rotateShape() from RefineryUtilities.java to here (DG);
 * 13-May-2004 : Added new shape creation methods (DG);
 * 30-Sep-2004 : Added createLineRegion() method (DG);
 *               Moved drawRotatedShape() method from RefineryUtilities class
 *               to this class (DG);
 * 04-Oct-2004 : Renamed ShapeUtils --> ShapeUtilities (DG);
 * 26-Oct-2004 : Added a method to test the equality of two Line2D
 *               instances (DG);
 * 10-Nov-2004 : Added new translateShape() and equal(Ellipse2D, Ellipse2D)
 *               methods (DG);
 * 11-Nov-2004 : Renamed translateShape() --> createTranslatedShape() (DG);
 * 07-Jan-2005 : Minor Javadoc fix (DG);
 * 11-Jan-2005 : Removed deprecated code in preparation for 1.0.0 release (DG);
 * 21-Jan-2005 : Modified return type of RectangleAnchor.coordinates()
 *               method (DG);
 * 22-Feb-2005 : Added equality tests for Arc2D and GeneralPath (DG);
 * 16-Mar-2005 : Fixed bug where equal(Shape, Shape) fails for two Polygon
 *               instances (DG);
 * 01-Jun-2008 : Fixed bug in equal(GeneralPath, GeneralPath) method (DG);
 *
 */

package org.jfree.util;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import org.jfree.ui.RectangleAnchor;

/**
 * Utility methods for {@link Shape} objects.
 *
 * @author David Gilbert
 */
public class ShapeUtilities {

    /**
     * Prevents instantiation.
     */
    private ShapeUtilities() {
    }

    /**
     * Returns a clone of the specified shape, or <code>null</code>.  At the
     * current time, this method supports cloning for instances of
     * <code>Line2D</code>, <code>RectangularShape</code>, <code>Area</code>
     * and <code>GeneralPath</code>.
     * <p>
     * <code>RectangularShape</code> includes <code>Arc2D</code>,
     * <code>Ellipse2D</code>, <code>Rectangle2D</code>,
     * <code>RoundRectangle2D</code>.
     *
     * @param shape  the shape to clone (<code>null</code> permitted,
     *               returns <code>null</code>).
     *
     * @return A clone or <code>null</code>.
     */
    public static Shape clone(final Shape shape) {
        if (shape instanceof Cloneable) {
            try {
                return (Shape) ObjectUtilities.clone(shape);
            }
            catch (CloneNotSupportedException cnse) {
            }
        }
        final Shape result = null;
        return result;
    }

    /**
     * Tests two shapes for equality.  If both shapes are <code>null</code>,
     * this method will return <code>true</code>.
     * <p>
     * In the current implementation, the following shapes are supported:
     * <code>Ellipse2D</code>, <code>Line2D</code> and <code>Rectangle2D</code>
     * (implicit).
     *
     * @param s1  the first shape (<code>null</code> permitted).
     * @param s2  the second shape (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public static boolean equal(final Shape s1, final Shape s2) {
        if (s1 instanceof Line2D && s2 instanceof Line2D) {
            return equal((Line2D) s1, (Line2D) s2);
        }
        else if (s1 instanceof Ellipse2D && s2 instanceof Ellipse2D) {
            return equal((Ellipse2D) s1, (Ellipse2D) s2);
        }
        else if (s1 instanceof Arc2D && s2 instanceof Arc2D) {
            return equal((Arc2D) s1, (Arc2D) s2);
        }
        else if (s1 instanceof Polygon && s2 instanceof Polygon) {
            return equal((Polygon) s1, (Polygon) s2);
        }
        else if (s1 instanceof GeneralPath && s2 instanceof GeneralPath) {
            return equal((GeneralPath) s1, (GeneralPath) s2);
        }
        else {
            // this will handle Rectangle2D...
            return ObjectUtilities.equal(s1, s2);
        }
    }

    /**
     * Compares two lines are returns <code>true</code> if they are equal or
     * both <code>null</code>.
     *
     * @param l1  the first line (<code>null</code> permitted).
     * @param l2  the second line (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public static boolean equal(final Line2D l1, final Line2D l2) {
        if (l1 == null) {
            return (l2 == null);
        }
        if (l2 == null) {
            return false;
        }
        if (!l1.getP1().equals(l2.getP1())) {
            return false;
        }
        if (!l1.getP2().equals(l2.getP2())) {
            return false;
        }
        return true;
    }

    /**
     * Compares two ellipses and returns <code>true</code> if they are equal or
     * both <code>null</code>.
     *
     * @param e1  the first ellipse (<code>null</code> permitted).
     * @param e2  the second ellipse (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public static boolean equal(final Ellipse2D e1, final Ellipse2D e2) {
        if (e1 == null) {
            return (e2 == null);
        }
        if (e2 == null) {
            return false;
        }
        if (!e1.getFrame().equals(e2.getFrame())) {
            return false;
        }
        return true;
    }

    /**
     * Compares two arcs and returns <code>true</code> if they are equal or
     * both <code>null</code>.
     *
     * @param a1  the first arc (<code>null</code> permitted).
     * @param a2  the second arc (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public static boolean equal(final Arc2D a1, final Arc2D a2) {
        if (a1 == null) {
            return (a2 == null);
        }
        if (a2 == null) {
            return false;
        }
        if (!a1.getFrame().equals(a2.getFrame())) {
            return false;
        }
        if (a1.getAngleStart() != a2.getAngleStart()) {
            return false;
        }
        if (a1.getAngleExtent() != a2.getAngleExtent()) {
            return false;
        }
        if (a1.getArcType() != a2.getArcType()) {
            return false;
        }
        return true;
    }

    /**
     * Tests two polygons for equality.  If both are <code>null</code> this
     * method returns <code>true</code>.
     *
     * @param p1  polygon 1 (<code>null</code> permitted).
     * @param p2  polygon 2 (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public static boolean equal(final Polygon p1, final Polygon p2) {
        if (p1 == null) {
            return (p2 == null);
        }
        if (p2 == null) {
            return false;
        }
        if (p1.npoints != p2.npoints) {
            return false;
        }
        if (!Arrays.equals(p1.xpoints, p2.xpoints)) {
            return false;
        }
        if (!Arrays.equals(p1.ypoints, p2.ypoints)) {
            return false;
        }
        return true;
    }

    /**
     * Tests two polygons for equality.  If both are <code>null</code> this
     * method returns <code>true</code>.
     *
     * @param p1  path 1 (<code>null</code> permitted).
     * @param p2  path 2 (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public static boolean equal(final GeneralPath p1, final GeneralPath p2) {
        if (p1 == null) {
            return (p2 == null);
        }
        if (p2 == null) {
            return false;
        }
        if (p1.getWindingRule() != p2.getWindingRule()) {
            return false;
        }
        PathIterator iterator1 = p1.getPathIterator(null);
        PathIterator iterator2 = p2.getPathIterator(null);
        double[] d1 = new double[6];
        double[] d2 = new double[6];
        boolean done = iterator1.isDone() && iterator2.isDone();
        while (!done) {
            if (iterator1.isDone() != iterator2.isDone()) {
                return false;
            }
            int seg1 = iterator1.currentSegment(d1);
            int seg2 = iterator2.currentSegment(d2);
            if (seg1 != seg2) {
                return false;
            }
            if (!Arrays.equals(d1, d2)) {
                return false;
            }
            iterator1.next();
            iterator2.next();
            done = iterator1.isDone() && iterator2.isDone();
        }
        return true;
    }

    /**
     * Creates and returns a translated shape.
     *
     * @param shape  the shape (<code>null</code> not permitted).
     * @param transX  the x translation (in Java2D space).
     * @param transY  the y translation (in Java2D space).
     *
     * @return The translated shape.
     */
    public static Shape createTranslatedShape(final Shape shape,
                                              final double transX,
                                              final double transY) {
        if (shape == null) {
            throw new IllegalArgumentException("Null 'shape' argument.");
        }
        final AffineTransform transform = AffineTransform.getTranslateInstance(
                transX, transY);
        return transform.createTransformedShape(shape);
    }

    /**
     * Translates a shape to a new location such that the anchor point
     * (relative to the rectangular bounds of the shape) aligns with the
     * specified (x, y) coordinate in Java2D space.
     *
     * @param shape  the shape (<code>null</code> not permitted).
     * @param anchor  the anchor (<code>null</code> not permitted).
     * @param locationX  the x-coordinate (in Java2D space).
     * @param locationY  the y-coordinate (in Java2D space).
     *
     * @return A new and translated shape.
     */
    public static Shape createTranslatedShape(final Shape shape,
                                              final RectangleAnchor anchor,
                                              final double locationX,
                                              final double locationY) {
        if (shape == null) {
            throw new IllegalArgumentException("Null 'shape' argument.");
        }
        if (anchor == null) {
            throw new IllegalArgumentException("Null 'anchor' argument.");
        }
        Point2D anchorPoint = RectangleAnchor.coordinates(
                shape.getBounds2D(), anchor);
        final AffineTransform transform = AffineTransform.getTranslateInstance(
                locationX - anchorPoint.getX(), locationY - anchorPoint.getY());
        return transform.createTransformedShape(shape);
    }

    /**
     * Rotates a shape about the specified coordinates.
     *
     * @param base  the shape (<code>null</code> permitted, returns
     *              <code>null</code>).
     * @param angle  the angle (in radians).
     * @param x  the x coordinate for the rotation point (in Java2D space).
     * @param y  the y coordinate for the rotation point (in Java2D space).
     *
     * @return the rotated shape.
     */
    public static Shape rotateShape(final Shape base, final double angle,
                                    final float x, final float y) {
        if (base == null) {
            return null;
        }
        final AffineTransform rotate = AffineTransform.getRotateInstance(
                angle, x, y);
        final Shape result = rotate.createTransformedShape(base);
        return result;
    }

    /**
     * Draws a shape with the specified rotation about <code>(x, y)</code>.
     *
     * @param g2  the graphics device (<code>null</code> not permitted).
     * @param shape  the shape (<code>null</code> not permitted).
     * @param angle  the angle (in radians).
     * @param x  the x coordinate for the rotation point.
     * @param y  the y coordinate for the rotation point.
     */
    public static void drawRotatedShape(final Graphics2D g2, final Shape shape,
                                        final double angle,
                                        final float x, final float y) {

        final AffineTransform saved = g2.getTransform();
        final AffineTransform rotate = AffineTransform.getRotateInstance(
                angle, x, y);
        g2.transform(rotate);
        g2.draw(shape);
        g2.setTransform(saved);

    }

    /** A useful constant used internally. */
    private static final float SQRT2 = (float) Math.pow(2.0, 0.5);

    /**
     * Creates a diagonal cross shape.
     *
     * @param l  the length of each 'arm'.
     * @param t  the thickness.
     *
     * @return A diagonal cross shape.
     */
    public static Shape createDiagonalCross(final float l, final float t) {
        final GeneralPath p0 = new GeneralPath();
        p0.moveTo(-l - t, -l + t);
        p0.lineTo(-l + t, -l - t);
        p0.lineTo(0.0f, -t * SQRT2);
        p0.lineTo(l - t, -l - t);
        p0.lineTo(l + t, -l + t);
        p0.lineTo(t * SQRT2, 0.0f);
        p0.lineTo(l + t, l - t);
        p0.lineTo(l - t, l + t);
        p0.lineTo(0.0f, t * SQRT2);
        p0.lineTo(-l + t, l + t);
        p0.lineTo(-l - t, l - t);
        p0.lineTo(-t * SQRT2, 0.0f);
        p0.closePath();
        return p0;
    }

    /**
     * Creates a diagonal cross shape.
     *
     * @param l  the length of each 'arm'.
     * @param t  the thickness.
     *
     * @return A diagonal cross shape.
     */
    public static Shape createRegularCross(final float l, final float t) {
        final GeneralPath p0 = new GeneralPath();
        p0.moveTo(-l, t);
        p0.lineTo(-t, t);
        p0.lineTo(-t, l);
        p0.lineTo(t, l);
        p0.lineTo(t, t);
        p0.lineTo(l, t);
        p0.lineTo(l, -t);
        p0.lineTo(t, -t);
        p0.lineTo(t, -l);
        p0.lineTo(-t, -l);
        p0.lineTo(-t, -t);
        p0.lineTo(-l, -t);
        p0.closePath();
        return p0;
    }

    /**
     * Creates a diamond shape.
     *
     * @param s  the size factor (equal to half the height of the diamond).
     *
     * @return A diamond shape.
     */
    public static Shape createDiamond(final float s) {
        final GeneralPath p0 = new GeneralPath();
        p0.moveTo(0.0f, -s);
        p0.lineTo(s, 0.0f);
        p0.lineTo(0.0f, s);
        p0.lineTo(-s, 0.0f);
        p0.closePath();
        return p0;
    }

    /**
     * Creates a triangle shape that points upwards.
     *
     * @param s  the size factor (equal to half the height of the triangle).
     *
     * @return A triangle shape.
     */
    public static Shape createUpTriangle(final float s) {
        final GeneralPath p0 = new GeneralPath();
        p0.moveTo(0.0f, -s);
        p0.lineTo(s, s);
        p0.lineTo(-s, s);
        p0.closePath();
        return p0;
    }

    /**
     * Creates a triangle shape that points downwards.
     *
     * @param s  the size factor (equal to half the height of the triangle).
     *
     * @return A triangle shape.
     */
    public static Shape createDownTriangle(final float s) {
        final GeneralPath p0 = new GeneralPath();
        p0.moveTo(0.0f, s);
        p0.lineTo(s, -s);
        p0.lineTo(-s, -s);
        p0.closePath();
        return p0;
    }

    /**
     * Creates a region surrounding a line segment by 'widening' the line
     * segment.  A typical use for this method is the creation of a
     * 'clickable' region for a line that is displayed on-screen.
     *
     * @param line  the line (<code>null</code> not permitted).
     * @param width  the width of the region.
     *
     * @return A region that surrounds the line.
     */
    public static Shape createLineRegion(final Line2D line, final float width) {
        final GeneralPath result = new GeneralPath();
        final float x1 = (float) line.getX1();
        final float x2 = (float) line.getX2();
        final float y1 = (float) line.getY1();
        final float y2 = (float) line.getY2();
        if ((x2 - x1) != 0.0) {
            final double theta = Math.atan((y2 - y1) / (x2 - x1));
            final float dx = (float) Math.sin(theta) * width;
            final float dy = (float) Math.cos(theta) * width;
            result.moveTo(x1 - dx, y1 + dy);
            result.lineTo(x1 + dx, y1 - dy);
            result.lineTo(x2 + dx, y2 - dy);
            result.lineTo(x2 - dx, y2 + dy);
            result.closePath();
        }
        else {
            // special case, vertical line
            result.moveTo(x1 - width / 2.0f, y1);
            result.lineTo(x1 + width / 2.0f, y1);
            result.lineTo(x2 + width / 2.0f, y2);
            result.lineTo(x2 - width / 2.0f, y2);
            result.closePath();
        }
        return result;
    }

    /**
     * Returns a point based on (x, y) but constrained to be within the bounds
     * of a given rectangle.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param area  the constraining rectangle (<code>null</code> not
     *              permitted).
     *
     * @return A point within the rectangle.
     *
     * @throws NullPointerException if <code>area</code> is <code>null</code>.
     */
    public static Point2D getPointInRectangle(double x, double y,
                                              final Rectangle2D area) {

        x = Math.max(area.getMinX(), Math.min(x, area.getMaxX()));
        y = Math.max(area.getMinY(), Math.min(y, area.getMaxY()));
        return new Point2D.Double(x, y);

    }

    /**
     * Checks, whether the given rectangle1 fully contains rectangle 2
     * (even if rectangle 2 has a height or width of zero!).
     *
     * @param rect1  the first rectangle.
     * @param rect2  the second rectangle.
     *
     * @return A boolean.
     */
    public static boolean contains(final Rectangle2D rect1,
                                   final Rectangle2D rect2) {

        final double x0 = rect1.getX();
        final double y0 = rect1.getY();
        final double x = rect2.getX();
        final double y = rect2.getY();
        final double w = rect2.getWidth();
        final double h = rect2.getHeight();

        return ((x >= x0) && (y >= y0)
                && ((x + w) <= (x0 + rect1.getWidth()))
                && ((y + h) <= (y0 + rect1.getHeight())));

    }


    /**
     * Checks, whether the given rectangle1 fully contains rectangle 2
     * (even if rectangle 2 has a height or width of zero!).
     *
     * @param rect1  the first rectangle.
     * @param rect2  the second rectangle.
     *
     * @return A boolean.
     */
    public static boolean intersects (final Rectangle2D rect1,
                                      final Rectangle2D rect2) {

      final double x0 = rect1.getX();
      final double y0 = rect1.getY();

      final double x = rect2.getX();
      final double width = rect2.getWidth();
      final double y = rect2.getY();
      final double height = rect2.getHeight();
      return (x + width >= x0 && y + height >= y0 && x <= x0 + rect1.getWidth()
              && y <= y0 + rect1.getHeight());
    }

}
