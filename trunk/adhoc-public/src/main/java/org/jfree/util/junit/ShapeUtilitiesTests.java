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
 * ------------------------
 * ShapeUtilitiesTests.java
 * ------------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: ShapeUtilitiesTests.java,v 1.9 2008/06/02 06:58:28 mungady Exp $
 *
 * Changes
 * -------
 * 26-Oct-2004 : Version 1 (DG);
 * 10-Nov-2004 : Extended test for equal shapes to include Ellipse2D (DG);
 * 16-Mar-2005 : Extended test for equal shapes to include Polygon (DG);
 * 26-Sep-2007 : Removed a couple of checks that don't hold on JRE 1.6 and
 *               later (DG);
 * 01-Jun-2008 : Added testEqualGeneralPaths() (DG);
 *
 */

package org.jfree.util.junit;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jfree.util.ShapeUtilities;

/**
 * Tests for the {@link ShapeUtilities} class.
 */
public class ShapeUtilitiesTests extends TestCase {

    /**
     * Returns the tests as a test suite.
     *
     * @return The test suite.
     */
    public static Test suite() {
        return new TestSuite(ShapeUtilitiesTests.class);
    }

    /**
     * Constructs a new set of tests.
     *
     * @param name  the name of the tests.
     */
    public ShapeUtilitiesTests(final String name) {
        super(name);
    }

    /**
     * Tests the equal() method.
     */
    public void testEqualLine2Ds() {

        assertTrue(ShapeUtilities.equal((Line2D) null, (Line2D) null));
        Line2D l1 = new Line2D.Float(1.0f, 2.0f, 3.0f, 4.0f);
        Line2D l2 = new Line2D.Float(1.0f, 2.0f, 3.0f, 4.0f);
        assertTrue(ShapeUtilities.equal(l1, l2));

        l1 = new Line2D.Float(4.0f, 3.0f, 2.0f, 1.0f);
        assertFalse(ShapeUtilities.equal(l1, l2));
        l2 = new Line2D.Float(4.0f, 3.0f, 2.0f, 1.0f);
        assertTrue(ShapeUtilities.equal(l1, l2));

        l1 = new Line2D.Double(4.0f, 3.0f, 2.0f, 1.0f);
        assertTrue(ShapeUtilities.equal(l1, l2));

    }

    /**
     * Some checks for the equal(Shape, Shape) method.
     */
    public void testEqualShapes() {

        // NULL
        Shape s1 = null;
        Shape s2 = null;
        assertTrue(ShapeUtilities.equal(s1, s2));

        // LINE2D
        s1 = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        assertFalse(ShapeUtilities.equal(s1, s2));
        s2 = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        assertTrue(ShapeUtilities.equal(s1, s2));
        assertFalse(s1.equals(s2));

        // RECTANGLE2D
        s1 = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        assertFalse(ShapeUtilities.equal(s1, s2));
        s2 = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        assertTrue(ShapeUtilities.equal(s1, s2));
        assertTrue(s1.equals(s2));  // Rectangle2D overrides equals()

        // ELLIPSE2D
        s1 = new Ellipse2D.Double(1.0, 2.0, 3.0, 4.0);
        assertFalse(ShapeUtilities.equal(s1, s2));
        s2 = new Ellipse2D.Double(1.0, 2.0, 3.0, 4.0);
        assertTrue(ShapeUtilities.equal(s1, s2));

        // ARC2D
        s1 = new Arc2D.Double(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, Arc2D.PIE);
        assertFalse(ShapeUtilities.equal(s1, s2));
        s2 = new Arc2D.Double(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, Arc2D.PIE);
        assertTrue(ShapeUtilities.equal(s1, s2));

        // POLYGON
        Polygon p1 = new Polygon(new int[] {0, 1, 0}, new int[] {1, 0, 1}, 3);
        Polygon p2 = new Polygon(new int[] {1, 1, 0}, new int[] {1, 0, 1}, 3);
        s1 = p1;
        s2 = p2;
        assertFalse(ShapeUtilities.equal(s1, s2));
        p2 = new Polygon(new int[] {0, 1, 0}, new int[] {1, 0, 1}, 3);
        s2 = p2;
        assertTrue(ShapeUtilities.equal(s1, s2));

        // GENERALPATH
        GeneralPath g1 = new GeneralPath();
        g1.moveTo(1.0f, 2.0f);
        g1.lineTo(3.0f, 4.0f);
        g1.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g1.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g1.closePath();
        s1 = g1;
        assertFalse(ShapeUtilities.equal(s1, s2));
        GeneralPath g2 = new GeneralPath();
        g2.moveTo(1.0f, 2.0f);
        g2.lineTo(3.0f, 4.0f);
        g2.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g2.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g2.closePath();
        s2 = g2;
        assertTrue(ShapeUtilities.equal(s1, s2));
        assertFalse(s1.equals(s2));

    }

    /**
     * Some checks for the intersects() method,
     */
    public void testIntersects() {
        final Rectangle2D r1 = new Rectangle2D.Float(0, 0, 100, 100);
        final Rectangle2D r2 = new Rectangle2D.Float(0, 0, 100, 100);
        assertTrue(ShapeUtilities.intersects(r1, r2));

        r1.setRect(100, 0, 100, 0);
        assertTrue(ShapeUtilities.intersects(r1, r2));
        assertTrue(ShapeUtilities.intersects(r2, r1));

        r1.setRect(0, 0, 0, 0);
        assertTrue(ShapeUtilities.intersects(r1, r2));
        assertTrue(ShapeUtilities.intersects(r2, r1));

        r1.setRect(50, 50, 10, 0);
        assertTrue(ShapeUtilities.intersects(r1, r2));
        assertTrue(ShapeUtilities.intersects(r2, r1));
    }

    /**
     * Some checks for the equal(GeneralPath, GeneralPath) method.
     */
    public void testEqualGeneralPaths() {
        GeneralPath g1 = new GeneralPath();
        g1.moveTo(1.0f, 2.0f);
        g1.lineTo(3.0f, 4.0f);
        g1.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g1.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g1.closePath();
        GeneralPath g2 = new GeneralPath();
        g2.moveTo(1.0f, 2.0f);
        g2.lineTo(3.0f, 4.0f);
        g2.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g2.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g2.closePath();
        assertTrue(ShapeUtilities.equal(g1, g2));

        g2 = new GeneralPath();
        g2.moveTo(11.0f, 22.0f);
        g2.lineTo(3.0f, 4.0f);
        g2.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g2.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g2.closePath();
        assertFalse(ShapeUtilities.equal(g1, g2));

        g2 = new GeneralPath();
        g2.moveTo(1.0f, 2.0f);
        g2.lineTo(33.0f, 44.0f);
        g2.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g2.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g2.closePath();
        assertFalse(ShapeUtilities.equal(g1, g2));

        g2 = new GeneralPath();
        g2.moveTo(1.0f, 2.0f);
        g2.lineTo(3.0f, 4.0f);
        g2.curveTo(55.0f, 66.0f, 77.0f, 88.0f, 99.0f, 100.0f);
        g2.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g2.closePath();
        assertFalse(ShapeUtilities.equal(g1, g2));

        g2 = new GeneralPath();
        g2.moveTo(1.0f, 2.0f);
        g2.lineTo(3.0f, 4.0f);
        g2.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g2.quadTo(11.0f, 22.0f, 33.0f, 44.0f);
        g2.closePath();
        assertFalse(ShapeUtilities.equal(g1, g2));

        g2 = new GeneralPath();
        g2.moveTo(1.0f, 2.0f);
        g2.lineTo(3.0f, 4.0f);
        g2.curveTo(5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f);
        g2.quadTo(1.0f, 2.0f, 3.0f, 4.0f);
        g2.lineTo(3.0f, 4.0f);
        g2.closePath();
        assertFalse(ShapeUtilities.equal(g1, g2));
    }

}
