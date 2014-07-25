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
 * -------------------------
 * RectangleInsetsTests.java
 * -------------------------
 * (C) Copyright 2004, 2005, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: RectangleInsetsTests.java,v 1.7 2007/11/02 17:50:37 taqua Exp $
 *
 * Changes
 * -------
 * 14-Jun-2004 : Version 1 (DG);
 * 03-May-2005 : Fixed tests for changes to RectangleInsets constructor (DG);
 *
 */

package org.jfree.ui.junit;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.UnitType;

/**
 * Tests for the {@link RectangleInsets} class.
 */
public class RectangleInsetsTests extends TestCase {

    /**
     * Returns the tests as a test suite.
     *
     * @return The test suite.
     */
    public static Test suite() {
        return new TestSuite(RectangleInsetsTests.class);
    }

    /**
     * Constructs a new set of tests.
     *
     * @param name  the name of the tests.
     */
    public RectangleInsetsTests(final String name) {
        super(name);
    }
    
    /**
     * Some checks for the createAdjustedRectangle() method.
     */
    public void testCreateAdjustedRectangleAbsolute() {
        
        RectangleInsets i1 = new RectangleInsets(
            UnitType.ABSOLUTE, 1.0, 2.0, 3.0, 4.0
        );
        Rectangle2D base = new Rectangle2D.Double(10.0, 20.0, 30.0, 40.0);
        
        // no adjustment
        Rectangle2D adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.NO_CHANGE, LengthAdjustmentType.NO_CHANGE
        );
        assertEquals(new Rectangle2D.Double(10.0, 20.0, 30.0, 40.0), adjusted);
        
        // expand height
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.NO_CHANGE, LengthAdjustmentType.EXPAND
        );
        assertEquals(new Rectangle2D.Double(10.0, 19.0, 30.0, 44.0), adjusted);
        
        // contract height
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.NO_CHANGE, LengthAdjustmentType.CONTRACT
        );
        assertEquals(new Rectangle2D.Double(10.0, 21.0, 30.0, 36.0), adjusted);
            
        // expand width
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.EXPAND, LengthAdjustmentType.NO_CHANGE
        );
        assertEquals(new Rectangle2D.Double(8.0, 20.0, 36.0, 40.0), adjusted);
            
        // contract width
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.CONTRACT, LengthAdjustmentType.NO_CHANGE
        );  
        assertEquals(new Rectangle2D.Double(12.0, 20.0, 24.0, 40.0), adjusted);
        
        // expand both
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.EXPAND, LengthAdjustmentType.EXPAND
        );
        assertEquals(new Rectangle2D.Double(8.0, 19.0, 36.0, 44.0), adjusted);
            
        // expand width contract height
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.EXPAND, LengthAdjustmentType.CONTRACT
        );    
        assertEquals(new Rectangle2D.Double(8.0, 21.0, 36.0, 36.0), adjusted);
        
        // contract both
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.CONTRACT, LengthAdjustmentType.CONTRACT
        );
        assertEquals(new Rectangle2D.Double(12.0, 21.0, 24.0, 36.0), adjusted);
        
        // contract width expand height
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.CONTRACT, LengthAdjustmentType.EXPAND
        );
        assertEquals(new Rectangle2D.Double(12.0, 19.0, 24.0, 44.0), adjusted);
        
    }
    
    private static final double EPSILON = 0.0000001;
    
    /**
     * Some checks for the createAdjustedRectangle() method.
     */
    public void testCreateAdjustedRectangleRelative() {
        
        RectangleInsets i1 = new RectangleInsets(
            UnitType.RELATIVE, 0.04, 0.03, 0.02, 0.01
        );
        Rectangle2D base = new Rectangle2D.Double(10.0, 20.0, 30.0, 40.0);
        
        // no adjustment
        Rectangle2D adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.NO_CHANGE, LengthAdjustmentType.NO_CHANGE
        );
        assertEquals(new Rectangle2D.Double(10.0, 20.0, 30.0, 40.0), adjusted);
        
        // expand height
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.NO_CHANGE, LengthAdjustmentType.EXPAND
        );
        assertEquals(10.0, adjusted.getX(), EPSILON);
        assertEquals(18.2978723404, adjusted.getY(), EPSILON);
        assertEquals(30.0, adjusted.getWidth(), EPSILON);
        assertEquals(42.553191489, adjusted.getHeight(), EPSILON);
        
        // contract height
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.NO_CHANGE, LengthAdjustmentType.CONTRACT
        );
        assertEquals(10.0, adjusted.getX(), EPSILON);
        assertEquals(21.6, adjusted.getY(), EPSILON);
        assertEquals(30.0, adjusted.getWidth(), EPSILON);
        assertEquals(37.6, adjusted.getHeight(), EPSILON);
            
        // expand width
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.EXPAND, LengthAdjustmentType.NO_CHANGE
        );
        assertEquals(9.0625, adjusted.getX(), EPSILON);
        assertEquals(20.0, adjusted.getY(), EPSILON);
        assertEquals(31.25, adjusted.getWidth(), EPSILON);
        assertEquals(40.0, adjusted.getHeight(), EPSILON);
            
        // contract width
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.CONTRACT, LengthAdjustmentType.NO_CHANGE
        );  
        assertEquals(10.9, adjusted.getX(), EPSILON);
        assertEquals(20.0, adjusted.getY(), EPSILON);
        assertEquals(28.8, adjusted.getWidth(), EPSILON);
        assertEquals(40.0, adjusted.getHeight(), EPSILON);
        
        // expand both
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.EXPAND, LengthAdjustmentType.EXPAND
        );
        assertEquals(9.0625, adjusted.getX(), EPSILON);
        assertEquals(18.2978723404, adjusted.getY(), EPSILON);
        assertEquals(31.25, adjusted.getWidth(), EPSILON);
        assertEquals(42.553191489, adjusted.getHeight(), EPSILON);
            
        // expand width contract height
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.EXPAND, LengthAdjustmentType.CONTRACT
        );    
        assertEquals(9.0625, adjusted.getX(), EPSILON);
        assertEquals(21.6, adjusted.getY(), EPSILON);
        assertEquals(31.25, adjusted.getWidth(), EPSILON);
        assertEquals(37.6, adjusted.getHeight(), EPSILON);
        
        // contract both
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.CONTRACT, LengthAdjustmentType.CONTRACT
        );
        assertEquals(10.9, adjusted.getX(), EPSILON);
        assertEquals(21.6, adjusted.getY(), EPSILON);
        assertEquals(28.8, adjusted.getWidth(), EPSILON);
        assertEquals(37.6, adjusted.getHeight(), EPSILON);
        
        // contract width expand height
        adjusted = i1.createAdjustedRectangle(
            base, LengthAdjustmentType.CONTRACT, LengthAdjustmentType.EXPAND
        );
        assertEquals(10.9, adjusted.getX(), EPSILON);
        assertEquals(18.2978723404, adjusted.getY(), EPSILON);
        assertEquals(28.8, adjusted.getWidth(), EPSILON);
        assertEquals(42.553191489, adjusted.getHeight(), EPSILON);
        
    }

    /**
     * Test the equals() method.
     */
    public void testEquals() {
        RectangleInsets i1 = new RectangleInsets(
            UnitType.ABSOLUTE, 1.0, 2.0, 3.0, 4.0
        );
        RectangleInsets i2 = new RectangleInsets(
            UnitType.ABSOLUTE, 1.0, 2.0, 3.0, 4.0
        );
        assertTrue(i1.equals(i2));
        assertTrue(i2.equals(i1));
        
        i1 = new RectangleInsets(UnitType.RELATIVE, 1.0, 2.0, 3.0, 4.0);
        assertFalse(i1.equals(i2));
        i2 = new RectangleInsets(UnitType.RELATIVE, 1.0, 2.0, 3.0, 4.0);
        assertTrue(i1.equals(i2));

        i1 = new RectangleInsets(UnitType.RELATIVE, 0.0, 2.0, 3.0, 4.0);
        assertFalse(i1.equals(i2));
        i2 = new RectangleInsets(UnitType.RELATIVE, 0.0, 2.0, 3.0, 4.0);
        assertTrue(i1.equals(i2));
        
        i1 = new RectangleInsets(UnitType.RELATIVE, 0.0, 0.0, 3.0, 4.0);
        assertFalse(i1.equals(i2));
        i2 = new RectangleInsets(UnitType.RELATIVE, 0.0, 0.0, 3.0, 4.0);
        assertTrue(i1.equals(i2));
        
        i1 = new RectangleInsets(UnitType.RELATIVE, 0.0, 0.0, 0.0, 4.0);
        assertFalse(i1.equals(i2));
        i2 = new RectangleInsets(UnitType.RELATIVE, 0.0, 0.0, 0.0, 4.0);
        assertTrue(i1.equals(i2));
        
        i1 = new RectangleInsets(UnitType.RELATIVE, 0.0, 0.0, 0.0, 0.0);
        assertFalse(i1.equals(i2));
        i2 = new RectangleInsets(UnitType.RELATIVE, 0.0, 0.0, 0.0, 0.0);
        assertTrue(i1.equals(i2));
        
    }
    
    /**
     * Serialize an instance, restore it, and check for identity.
     */
    public void testSerialization() {
        final RectangleInsets i1 = new RectangleInsets(
            UnitType.ABSOLUTE, 1.0, 2.0, 3.0, 4.0
        );
        RectangleInsets i2 = null;
        try {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(i1);
            out.close();

            final ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            i2 = (RectangleInsets) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertTrue(i1.equals(i2)); 
    }
    
    /**
     * Runs the test suite using the JUnit text-based runner.
     * 
     * @param args  ignored.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
