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
 * ---------------------
 * JCommonTestSuite.java
 * ---------------------
 * (C) Copyright 2001-2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: JCommonTestSuite.java,v 1.5 2007/11/02 17:50:35 taqua Exp $
 *
 * Changes
 * -------
 * 11-Nov-2001 : Version 1 (DG);
 * 02-Sep-2002 : Removed DataPackageTests (DG);
 * 18-Sep-2003 : Added new org.jfree.io package tests (DG);
 * 09-Jan-2004 : Added new org.jfree.ui package tests (DG);
 * 22-Mar-2004 : Added tests for the org.jfree.text package (DG);
 * 18-Jan-2005 : Added main() method (DG);
 *
 */

package org.jfree.junit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jfree.date.junit.DatePackageTests;
import org.jfree.io.junit.IOPackageTests;
import org.jfree.text.junit.TextPackageTests;
import org.jfree.ui.junit.UIPackageTests;
import org.jfree.util.junit.UtilPackageTests;

/**
 * A test suite for the JCommon class library that can be run using JUnit (http://www.junit.org).
 */
public class JCommonTestSuite extends TestCase {

    /**
     * Returns a test suite to the JUnit test runner.
     *
     * @return a test suite.
     */
    public static Test suite() {
        final TestSuite suite = new TestSuite("JCommon");
        suite.addTest(DatePackageTests.suite());
        suite.addTest(IOPackageTests.suite());
        suite.addTest(TextPackageTests.suite());
        suite.addTest(UIPackageTests.suite());
        suite.addTest(UtilPackageTests.suite());
        return suite;
    }

    /**
     * Constructs the test suite.
     *
     * @param name  the suite name.
     */
    public JCommonTestSuite(final String name) {
        super(name);
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
