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
 * DatePackageTests.java
 * ---------------------
 * (C) Copyright 2001-2003, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: DatePackageTests.java,v 1.3 2005/11/16 15:58:40 taqua Exp $
 *
 * Changes
 * -------
 * 16-Nov-2001 : Version 1 (DG);
 * 25-Jun-2002 : Added SerialDateUtilitiesTests (DG);
 * 24-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.date.junit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A test suite for the <code>org.jfree.date</code> package.
 *
 */
public class DatePackageTests extends TestCase {

    /**
     * Returns a test suite for the JUnit test runner.
     *
     * @return the test suite.
     */
    public static Test suite() {
        final TestSuite suite = new TestSuite("org.jfree.date");
        suite.addTestSuite(SerialDateTests.class);
        suite.addTestSuite(SerialDateUtilitiesTests.class);
        suite.addTestSuite(SpreadsheetDateTests.class);
        return suite;
    }

    /**
     * Creates a new test case.
     *
     * @param name  the name.
     */
    public DatePackageTests(final String name) {
        super(name);
    }

}
