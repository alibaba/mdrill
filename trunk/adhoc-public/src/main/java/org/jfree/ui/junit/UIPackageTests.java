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
 * UIPackageTests.java
 * -------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: UIPackageTests.java,v 1.4 2005/11/02 16:30:23 mungady Exp $
 *
 * Changes:
 * --------
 * 08-Jan-2004 : Version 1 (DG);
 *
 */

package org.jfree.ui.junit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A collection of tests for the <code>org.jfree.ui<code> package. These tests can be run using 
 * JUnit (http://www.junit.org).
 */
public class UIPackageTests extends TestCase {

    /**
     * Returns a test suite to the JUnit test runner.
     *
     * @return The test suite.
     */
    public static Test suite() {
        final TestSuite suite = new TestSuite("org.jfree.ui");
        suite.addTestSuite(GradientPaintTransformTypeTests.class);
        suite.addTestSuite(HorizontalAlignmentTests.class);
        suite.addTestSuite(LayerTests.class);
        suite.addTestSuite(RectangleAnchorTests.class);
        suite.addTestSuite(RectangleEdgeTests.class);
        suite.addTestSuite(RectangleInsetsTests.class);
        suite.addTestSuite(Size2DTests.class);
        suite.addTestSuite(StandardGradientPaintTransformerTests.class);
        suite.addTestSuite(TextAnchorTests.class);
        suite.addTestSuite(VerticalAlignmentTests.class);
        return suite;
    }

    /**
     * Constructs the test suite.
     *
     * @param name  the suite name.
     */
    public UIPackageTests(final String name) {
        super(name);
    }

}
