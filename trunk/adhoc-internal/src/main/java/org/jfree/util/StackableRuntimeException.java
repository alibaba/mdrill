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
 * ------------------------------
 * StackableRuntimeException.java
 * ------------------------------
 * (C)opyright 2002-2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: StackableRuntimeException.java,v 1.3 2005/11/14 10:57:07 mungady Exp $
 *
 * Changes
 * -------
 * 06-Dec-2002 : Initial version
 * 10-Dec-2002 : Fixed issues reported by Checkstyle (DG);
 * 29-Apr-2003 : Distilled from the JFreeReport project and moved into JCommon
 *
 */

package org.jfree.util;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A baseclass for RuntimeExceptions, which could have parent exceptions. These parent exceptions
 * are raised in a subclass and are now wrapped into a subclass of this Exception.
 * <p>
 * The parents are printed when this exception is printed. This class exists mainly for
 * debugging reasons, as with them it is easier to detect the root cause of an error.
 *
 * @author Thomas Morgner
 */
public class StackableRuntimeException extends RuntimeException {

    /** The parent exception. */
    private Exception parent;

    /**
     * Creates a StackableRuntimeException with no message and no parent.
     */
    public StackableRuntimeException() {
        super();
    }

    /**
     * Creates an exception.
     *
     * @param message  the exception message.
     * @param ex  the parent exception.
     */
    public StackableRuntimeException(final String message, final Exception ex) {
        super(message);
        this.parent = ex;
    }

    /**
     * Creates an exception.
     *
     * @param message  the exception message.
     */
    public StackableRuntimeException(final String message) {
        super(message);
    }

    /**
     * Returns the parent exception (possibly null).
     *
     * @return the parent exception.
     */
    public Exception getParent() {
        return this.parent;
    }

    /**
     * Prints the stack trace to the specified stream.
     *
     * @param stream  the output stream.
     */
    public void printStackTrace(final PrintStream stream) {
        super.printStackTrace(stream);
        if (getParent() != null) {
            stream.println("ParentException: ");
            getParent().printStackTrace(stream);
        }
    }

    /**
     * Prints the stack trace to the specified writer.
     *
     * @param writer  the writer.
     */
    public void printStackTrace(final PrintWriter writer) {
        super.printStackTrace(writer);
        if (getParent() != null) {
            writer.println("ParentException: ");
            getParent().printStackTrace(writer);
        }
    }

}
