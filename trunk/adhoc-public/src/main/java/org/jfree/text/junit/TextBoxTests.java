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
 * -----------------
 * TextBoxTests.java
 * -----------------
 * (C) Copyright 2004, 2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: TextBoxTests.java,v 1.6 2007/11/02 17:50:35 taqua Exp $
 *
 * Changes:
 * --------
 * 22-Mar-2004 : Version 1 (DG);
 *
 */

package org.jfree.text.junit;

import java.awt.BasicStroke;
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
import org.jfree.text.TextBlock;
import org.jfree.text.TextBox;
import org.jfree.text.TextLine;
import org.jfree.ui.RectangleInsets;

/**
 * Tests for the {@link TextBox} class.
 */
public class TextBoxTests extends TestCase {

    /**
     * Returns the tests as a test suite.
     *
     * @return The test suite.
     */
    public static Test suite() {
        return new TestSuite(TextBoxTests.class);
    }

    /**
     * Constructs a new set of tests.
     *
     * @param name  the name of the tests.
     */
    public TextBoxTests(final String name) {
        super(name);
    }

    /**
     * Confirm that the equals method can distinguish all the required fields.
     */
    public void testEquals() {
        
        final TextBox b1 = new TextBox("Hello");
        final TextBox b2 = new TextBox("Hello");
        assertTrue(b1.equals(b2));
        assertTrue(b2.equals(b1));
        
        // outlinePaint
        b1.setOutlinePaint(Color.blue);
        assertFalse(b1.equals(b2));
        b2.setOutlinePaint(Color.blue);
        assertTrue(b1.equals(b2));

        // outlineStroke
        b1.setOutlineStroke(new BasicStroke(1.1f));
        assertFalse(b1.equals(b2));
        b2.setOutlineStroke(new BasicStroke(1.1f));
        assertTrue(b1.equals(b2));

        // interiorGap
        b1.setInteriorGap(new RectangleInsets(10, 10, 10, 10));
        assertFalse(b1.equals(b2));
        b2.setInteriorGap(new RectangleInsets(10, 10, 10, 10));
        assertTrue(b1.equals(b2));
        
        // backgroundPaint
        b1.setBackgroundPaint(Color.blue);
        assertFalse(b1.equals(b2));
        b2.setBackgroundPaint(Color.blue);
        assertTrue(b1.equals(b2));

        // shadowPaint
        b1.setShadowPaint(Color.blue);
        assertFalse(b1.equals(b2));
        b2.setShadowPaint(Color.blue);
        assertTrue(b1.equals(b2));

        // shadowXOffset
        b1.setShadowXOffset(1.0);
        assertFalse(b1.equals(b2));
        b2.setShadowXOffset(1.0);
        assertTrue(b1.equals(b2));
        
        // shadowYOffset
        b1.setShadowYOffset(1.0);
        assertFalse(b1.equals(b2));
        b2.setShadowYOffset(1.0);
        assertTrue(b1.equals(b2));
        
        // textBlock
        final TextBlock tb1 = new TextBlock();
        tb1.addLine(new TextLine("Testing"));
        b1.setTextBlock(tb1);
        assertFalse(b1.equals(b2));
        final TextBlock tb2 = new TextBlock();
        tb2.addLine(new TextLine("Testing"));
        b2.setTextBlock(tb2);
        assertTrue(b1.equals(b2));
        
    }

    /**
     * Serialize an instance, restore it, and check for equality.
     */
    public void testSerialization() {

        final TextBox b1 = new TextBox("Hello");
        TextBox b2 = null;

        try {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(b1);
            out.close();

            final ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            b2 = (TextBox) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(b1, b2);

    }

}
