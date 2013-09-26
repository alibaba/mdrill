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
 * ------------
 * LogTest.java
 * ------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: LogTest.java,v 1.4 2007/11/02 17:50:37 taqua Exp $
 *
 * Changes
 * -------
 * 21-Feb-2004 : Initial version
 * 07-Jun-2004 : Added JCommon header (DG);
 */

package org.jfree.util.junit;

import junit.framework.TestCase;
import org.jfree.util.Log;
import org.jfree.util.LogContext;
import org.jfree.util.LogTarget;

/**
 * A test for...
 */
public class LogTest extends TestCase {

    private class LogTargetImpl implements LogTarget {

        /**
         * Default constructor.
         */
        public LogTargetImpl() {
            super();
        }

        /**
         * Logs a message at a specified log level.
         *
         * @param level  the log level.
         * @param message  the log message.
         */
        public void log(final int level, final Object message) {
            // nothing required.
        }

        /**
         * Logs a message at a specified log level.
         *
         * @param level  the log level.
         * @param message  the log message.
         * @param e  the exception
         */
        public void log(final int level, final Object message, final Exception e) {
            // nothing required.
        }
    }

    /**
     * Creates a new test.
     * 
     * @param s  the test name.
     */
    public LogTest(final String s) {
        super(s);
    }

    /**
     * Tests the addTarget() and removeTarget() methods.
     */
    public void testAddRemove() {
        final LogTarget a = new LogTargetImpl();
        final LogTarget b = new LogTargetImpl();

        Log.getInstance().removeTarget(a);
        Log.getInstance().removeTarget(b);

        Log.getInstance().addTarget(a);
        Log.getInstance().addTarget(b);

        Log.getInstance().removeTarget(a);
        Log.getInstance().removeTarget(b);

        Log.getInstance().addTarget(a);
        Log.getInstance().addTarget(b);

        Log.getInstance().removeTarget(b);
        Log.getInstance().removeTarget(a);

        Log.getInstance().getTargets();
    }

    /**
     * Tests the log message methods.
     */
    public void testLogMessage () {
        Log.debug("Test");
        Log.info("Test");
        Log.warn("Test");
        Log.error("Test");
    }

    /**
     * Tests the log context.
     */
    public void testLogContext() {
        final LogContext ctx = Log.createContext((String) null);
        assertEquals("Context = null", ctx, Log.createContext((String) null));

        final LogContext ctx2 = Log.createContext("Test");
        assertEquals("Context Test", ctx2, Log.createContext("Test"));

    }
}
