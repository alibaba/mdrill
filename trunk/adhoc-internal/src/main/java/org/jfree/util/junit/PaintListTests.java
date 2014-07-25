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
 * -------------------
 * PaintListTests.java
 * -------------------
 * (C) Copyright 2003-2005, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: PaintListTests.java,v 1.4 2007/11/02 17:50:37 taqua Exp $
 *
 * Changes
 * -------
 * 13-Aug-2003 : Version 1 (DG);
 * 27-Jun-2005 : Added test for equals() where the list contains 
 *               GradientPaint instances (DG);
 */

package org.jfree.util.junit;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jfree.util.PaintList;

/**
 * Some tests for the {@link PaintList} class.
 */
public class PaintListTests extends TestCase {

    /**
     * Returns the tests as a test suite.
     *
     * @return The test suite.
     */
    public static Test suite() {
        return new TestSuite(PaintListTests.class);
    }

    /**
     * Constructs a new set of tests.
     *
     * @param name  the name of the tests.
     */
    public PaintListTests(final String name) {
        super(name);
    }

    /**
     * Tests the equals() method.
     */
    public void testEquals() {
        final PaintList l1 = new PaintList();
        l1.setPaint(0, Color.red);
        l1.setPaint(1, Color.blue);
        l1.setPaint(2, null);
        
        final PaintList l2 = new PaintList();
        l2.setPaint(0, Color.red);
        l2.setPaint(1, Color.blue);
        l2.setPaint(2, null);
        
        assertTrue(l1.equals(l2));
        assertTrue(l2.equals(l2));
    }
    
    /**
     * Tests the equals method.
     */
    public void testEquals2() {
        // check two separate (but equal) colors
        final PaintList l1 = new PaintList();
        final Color color1 = new Color(200, 200, 200);
        l1.setPaint(0, color1);
        final PaintList l2 = new PaintList();
        final Color color2 = new Color(200, 200, 200);
        l2.setPaint(0, color2);
        assertEquals(l1, l2);
    }
    
    /**
     * Tests the equals() method when the list contains a GradientPaint 
     * instance.
     */
    public void testEquals3() {
        // check two separate (but equal) colors
        PaintList l1 = new PaintList();
        Paint p1 = new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.blue);
        l1.setPaint(0, p1);
        PaintList l2 = new PaintList();
        Paint p2 = new GradientPaint(1.0f, 2.0f, Color.red, 
                3.0f, 4.0f, Color.blue);
        l2.setPaint(0, p2);
        assertEquals(l1, l2);
    }
    
    /**
     * Confirm that cloning works.
     */
    public void testCloning() {
        
        final PaintList l1 = new PaintList();
        l1.setPaint(0, Color.red);
        l1.setPaint(1, Color.blue);
        l1.setPaint(2, null);
        
        PaintList l2 = null;
        try {
            l2 = (PaintList) l1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("PaintListTests.testCloning: failed to clone.");
        }
        assertTrue(l1 != l2);
        assertTrue(l1.getClass() == l2.getClass());
        assertTrue(l1.equals(l2));
        
        l2.setPaint(0, Color.green);
        assertFalse(l1.equals(l2));
        
    }
    
    /**
     * Serialize an instance, restore it, and check for equality.
     */
    public void testSerialization() {

        final PaintList l1 = new PaintList();
        l1.setPaint(0, Color.red);
        l1.setPaint(1, Color.blue);
        l1.setPaint(2, null);
        
        PaintList l2 = null;

        try {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(l1);
            out.close();

            final ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            l2 = (PaintList) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(l1, l2);

    }

}
