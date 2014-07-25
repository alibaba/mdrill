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
 * -----------------------------------
 * AttributedStringUtilitiesTests.java
 * -----------------------------------
 * (C) Copyright 2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: AttributedStringUtilitiesTests.java,v 1.3 2007/11/02 17:50:37 taqua Exp $
 *
 * Changes
 * -------
 * 29-Jul-2005 : Version 1 (DG);
 *
 */

package org.jfree.util.junit;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jfree.util.AttributedStringUtilities;

/**
 * Some tests for the {@link AttributedStringUtilities} class.
 */
public class AttributedStringUtilitiesTests  extends TestCase {

    /**
     * Returns the tests as a test suite.
     *
     * @return The test suite.
     */
    public static Test suite() {
        return new TestSuite(AttributedStringUtilitiesTests.class);
    }

    /**
     * Constructs a new set of tests.
     *
     * @param name  the name of the tests.
     */
    public AttributedStringUtilitiesTests(String name) {
        super(name);
    }

    /**
     * Some checks for the equal(AttributedString, AttributedString) method.
     */
    public void testEqual() {
        assertTrue(AttributedStringUtilities.equal(null, null));
  
        AttributedString s1 = new AttributedString("ABC");
        assertFalse(AttributedStringUtilities.equal(s1, null));
        assertFalse(AttributedStringUtilities.equal(null, s1));
        
        AttributedString s2 = new AttributedString("ABC");
        assertTrue(AttributedStringUtilities.equal(s1, s2));
        
        s1.addAttribute(TextAttribute.BACKGROUND, Color.blue, 1, 2);
        assertFalse(AttributedStringUtilities.equal(s1, s2));
        s2.addAttribute(TextAttribute.BACKGROUND, Color.blue, 1, 2);
        assertTrue(AttributedStringUtilities.equal(s1, s2));
    }

}
