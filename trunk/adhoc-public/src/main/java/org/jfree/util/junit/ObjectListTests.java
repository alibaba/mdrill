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
 * ObjectListTests.java
 * --------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: ObjectListTests.java,v 1.3 2007/11/02 17:50:37 taqua Exp $
 *
 * Changes
 * -------
 * 13-Aug-2003 : Version 1 (DG);
 * 17-Sep-2003 : Added new tests for equals and serialization (DG);
 *
 */

package org.jfree.util.junit;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jfree.util.ObjectList;

/**
 * Tests for the {@link ObjectList} class.
 */
public class ObjectListTests extends TestCase {

    /**
     * Returns the tests as a test suite.
     *
     * @return The test suite.
     */
    public static Test suite() {
        return new TestSuite(ObjectListTests.class);
    }

    /**
     * Constructs a new set of tests.
     *
     * @param name  the name of the tests.
     */
    public ObjectListTests(final String name) {
        super(name);
    }

    /**
     * Tests the equals() method.
     */
    public void testEquals() {
        
        final ObjectList l1 = new ObjectList();
        l1.set(0, Color.blue);
        l1.set(1, Color.red);
        
        final ObjectList l2 = new ObjectList();
        l2.set(0, Color.blue);
        l2.set(1, Color.red);
        
        assertTrue(l1.equals(l2));
        assertTrue(l2.equals(l2));
        
    }
    
    /**
     * Another test of the equals method.  The capacity of the internal list shouldn't
     * be a factor.
     */
    public void testEquals2() {
        
        final ObjectList l1 = new ObjectList(20);
        l1.set(0, Color.blue);
        l1.set(1, Color.red);
        
        final ObjectList l2 = new ObjectList();
        l2.set(0, Color.blue);
        l2.set(1, Color.red);
        
        assertTrue(l1.equals(l2));
        assertTrue(l2.equals(l2));
        
    }
    
    /**
     * Confirm that cloning works.
     */
    public void testCloning() {
        
        final ObjectList l1 = new ObjectList();
        l1.set(0, Color.blue);
        l1.set(1, Color.red);
        
        ObjectList l2 = null;
        try {
            l2 = (ObjectList) l1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("ObjectListTests.testCloning: failed to clone.");
        }
        assertTrue(l1 != l2);
        assertTrue(l1.getClass() == l2.getClass());
        assertTrue(l1.equals(l2));
        
        l2.set(0, Color.green);
        assertFalse(l1.equals(l2));
        
    }
    
    /**
     * Serialize an instance, restore it, and check for equality.
     */
    public void testSerialization() {

        final ObjectList l1 = new ObjectList();
        l1.set(0, Color.red);
        l1.set(1, Color.blue);
        l1.set(2, null);
        
        ObjectList l2 = null;

        try {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(l1);
            out.close();

            final ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            l2 = (ObjectList) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(l1, l2);

    }
        
    /**
     * Tests the expand method.  This test reproduces a bug where the list was not expanded beyond
     * the initial default size of 8.  This bug is now fixed.
     */
    public void testExpand() {
        final ObjectList l1 = new ObjectList();
        l1.set(10, Color.blue);
        final Color c = (Color) l1.get(10);
        assertTrue(c.equals(Color.blue));
    }

}
