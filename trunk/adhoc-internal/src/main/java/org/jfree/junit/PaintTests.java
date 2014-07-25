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
 * ---------------
 * PaintTests.java
 * ---------------
 * (C) Copyright 2003-2005, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: PaintTests.java,v 1.3 2005/10/18 13:16:37 mungady Exp $
 *
 * Changes
 * -------
 * 23-Oct-2003 : Version 1 (DG);
 *
 */

package org.jfree.junit;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the {@link Paint} interface and known subclasses.
 */
public class PaintTests extends TestCase {

    /**
     * Returns the tests as a test suite.
     *
     * @return the test suite.
     */
    public static Test suite() {
        return new TestSuite(PaintTests.class);
    }

    /**
     * Constructs a new set of tests.
     *
     * @param name  the name of the tests.
     */
    public PaintTests(final String name) {
        super(name);
    }

    /**
     * Check that the equals() method distinguishes all fields.
     */
    public void testColorEquals() {
        final Paint p1 = new Color(0xFF, 0xEE, 0xDD);
        final Paint p2 = new Color(0xFF, 0xEE, 0xDD);
        assertEquals(p1, p2);
    }

    /**
     * Two objects that are equal are required to return the same hashCode. 
     */
    public void testColorHashcode() {
        final Paint p1 = new Color(0xFF, 0xEE, 0xDD);
        final Paint p2 = new Color(0xFF, 0xEE, 0xDD);
        assertTrue(p1.equals(p2));
        final int h1 = p1.hashCode();
        final int h2 = p2.hashCode();
        assertEquals(h1, h2);
    }

    /**
     * Check that the equals() method distinguishes all fields.
     */
    public void testGradientPaintEquals() {
        final Paint p1 = new GradientPaint(10.0f, 20.0f, Color.blue, 30.0f, 40.0f, Color.red);
        final Paint p2 = new GradientPaint(10.0f, 20.0f, Color.blue, 30.0f, 40.0f, Color.red);
        assertEquals(p1, p2);
    }

    /**
     * Check that the equals() method distinguishes all fields.
     */
    public void testTexturePaintEquals() {
        final Paint p1 = new TexturePaint(
            new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB), new Rectangle2D.Double()
        );
        final Paint p2 = new TexturePaint(
            new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB), new Rectangle2D.Double()
        );
        assertEquals(p1, p2);
    }

}
