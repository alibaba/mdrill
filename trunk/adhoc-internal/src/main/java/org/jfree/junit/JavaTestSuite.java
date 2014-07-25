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
 * ------------------
 * JavaTestSuite.java
 * ------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: JavaTestSuite.java,v 1.3 2005/11/16 15:58:40 taqua Exp $
 *
 * Changes
 * -------
 * 23-Oct-2003 : Version 1 (DG);
 *
 */

package org.jfree.junit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A collection of tests that I use to examine the behaviour of Java in particular situations. 
 * I expect some of these tests to fail - that doesn't always indicate a bug, only that Java
 * doesn't work the way I might want it to.
 */
public class JavaTestSuite extends TestCase {

    /**
     * Returns a test suite to the JUnit test runner.
     *
     * @return a test suite.
     */
    public static Test suite() {
        final TestSuite suite = new TestSuite("Java Tests");
        suite.addTest(PaintTests.suite());
        suite.addTest(StrokeTests.suite());
        return suite;
    }

    /**
     * Constructs the test suite.
     *
     * @param name  the suite name.
     */
    public JavaTestSuite(final String name) {
        super(name);
    }

}
