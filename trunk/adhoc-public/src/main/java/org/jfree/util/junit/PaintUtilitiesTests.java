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
 * ------------------------
 * PaintUtilitiesTests.java
 * ------------------------
 * (C) Copyright 2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: PaintUtilitiesTests.java,v 1.3 2007/11/02 17:50:37 taqua Exp $
 *
 * Changes
 * -------
 * 23-Feb-2005 : Version 1 (DG);
 *
 */

package org.jfree.util.junit;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jfree.util.PaintUtilities;

/**
 * Some tests for the {@link PaintUtilities} class.
 */
public class PaintUtilitiesTests  extends TestCase {

    /**
     * Returns the tests as a test suite.
     *
     * @return The test suite.
     */
    public static Test suite() {
        return new TestSuite(PaintUtilitiesTests.class);
    }

    /**
     * Constructs a new set of tests.
     *
     * @param name  the name of the tests.
     */
    public PaintUtilitiesTests(String name) {
        super(name);
    }

    /**
     * Some checks for the equal(Paint, Paint) method.
     */
    public void testEqual() {
        Paint p1 = Color.red;
        Paint p2 = Color.blue;
        Paint p3 = new Color(1, 2, 3, 4);
        Paint p4 = new Color(1, 2, 3, 4);
        Paint p5 = new GradientPaint(
            1.0f, 2.0f, Color.red, 3.0f, 4.0f, Color.yellow
        );
        Paint p6 = new GradientPaint(
            1.0f, 2.0f, Color.red, 3.0f, 4.0f, Color.yellow
        );
        Paint p7 = new GradientPaint(
            1.0f, 2.0f, Color.red, 3.0f, 4.0f, Color.blue
        );
        assertTrue(PaintUtilities.equal(null, null));
        assertFalse(PaintUtilities.equal(p1, null));
        assertFalse(PaintUtilities.equal(null, p1));
        assertFalse(PaintUtilities.equal(p1, p2));
        assertTrue(PaintUtilities.equal(p3, p3));
        assertTrue(PaintUtilities.equal(p3, p4));
        assertTrue(PaintUtilities.equal(p5, p6));
        assertFalse(PaintUtilities.equal(p5, p7));
    }

}
