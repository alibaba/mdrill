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
 * ----------------
 * StrokeTests.java
 * ----------------
 * (C) Copyright 2003-2005, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: StrokeTests.java,v 1.3 2005/10/18 13:16:37 mungady Exp $
 *
 * Changes
 * -------
 * 23-Oct-2003 : Version 1 (DG);
 *
 */

package org.jfree.junit;

import java.awt.BasicStroke;
import java.awt.Stroke;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the {@link Stroke} interface and known subclass {@link BasicStroke}.
 */
public class StrokeTests extends TestCase {

    /**
     * Returns the tests as a test suite.
     *
     * @return The test suite.
     */
    public static Test suite() {
        return new TestSuite(StrokeTests.class);
    }

    /**
     * Constructs a new set of tests.
     *
     * @param name  the name of the tests.
     */
    public StrokeTests(final String name) {
        super(name);
    }

    /**
     * Check that the equals() method distinguishes all fields.
     */
    public void testBasicStrokeEquals() {
        final Stroke s1 = new BasicStroke(1.5f);
        final Stroke s2 = new BasicStroke(1.5f);
        assertEquals(s1, s2);
    }

    /**
     * Two objects that are equal are required to return the same hashCode. 
     */
    public void testColorHashcode() {
        final Stroke s1 = new BasicStroke(1.5f);
        final Stroke s2 = new BasicStroke(1.5f);
        assertTrue(s1.equals(s2));
        final int h1 = s1.hashCode();
        final int h2 = s2.hashCode();
        assertEquals(h1, h2);
    }

}
