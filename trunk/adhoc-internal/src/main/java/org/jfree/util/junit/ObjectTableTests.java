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
 * ObjectTableTests.java
 * --------------------
 * (C) Copyright 2003-2005, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: ObjectTableTests.java,v 1.6 2007/11/02 17:50:37 taqua Exp $
 *
 * Changes
 * -------
 * 29-Apr-2003 : Version 1 (DG);
 *
 */

package org.jfree.util.junit;

import java.awt.Color;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jfree.util.ObjectTable;

/**
 * Tests for the {@link ObjectTable} class.
 */
public class ObjectTableTests extends TestCase {

    /**
     * Basic object table.
     */
    public class TObjectTable extends ObjectTable {

        /**
         * Constructor.
         */
        public TObjectTable() {
            super();
        }

        /**
         * Returns the object from a particular cell in the table.
         * Returns null, if there is no object at the given position.
         *
         * @param row  the row index (zero-based).
         * @param column  the column index (zero-based).
         *
         * @return The object.
         */
        public Object getObject(final int row, final int column) {
            return super.getObject(row, column);
        }

        /**
         * Sets the object for a cell in the table.  The table is expanded if necessary.
         *
         * @param row  the row index (zero-based).
         * @param column  the column index (zero-based).
         * @param object  the object.
         */
        public void setObject(final int row, final int column, final Object object) {
            super.setObject(row, column, object);
        }
    }

    /**
     * Returns the tests as a test suite.
     *
     * @return The test suite.
     */
    public static Test suite() {
        return new TestSuite(ObjectTableTests.class);
    }

    /**
     * Constructs a new set of tests.
     *
     * @param name  the name of the tests.
     */
    public ObjectTableTests(final String name) {
        super(name);
    }

    /**
     * When an ObjectTable is created, it should be empty and return null for all lookups.
     */
    public void testCreate() {

        final TObjectTable t = new TObjectTable();

        // the new table should have zero rows and zero columns...
        assertEquals(t.getColumnCount(), 0);
        assertEquals(t.getRowCount(), 0);

        // ...and should return null for any lookup
        assertNull(t.getObject(0, 0));
        assertNull(t.getObject(12, 12));

    }

    /**
     * When an object is added to the table outside the current bounds, the table
     * should resize automatically.
     */
    public void testSetObject1() {

        final TObjectTable t = new TObjectTable();
        t.setObject(8, 5, Color.red);
        assertEquals(6, t.getColumnCount());
        assertEquals(9, t.getRowCount());
        assertNull(t.getObject(7, 4));
        assertEquals(Color.red, t.getObject(8, 5));

    }

}
