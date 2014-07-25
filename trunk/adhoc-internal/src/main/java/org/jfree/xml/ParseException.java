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
 * ParseException.java
 * -------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ParseException.java,v 1.4 2005/10/18 13:25:44 mungady Exp $
 *
 * Changes
 * -------------------------
 * 10.06.2003 : Initial version
 *
 */

package org.jfree.xml;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * A parse exception.
 *
 * @author Thomas Morgner
 */
public class ParseException extends SAXException {

    /** The line, where the error occured. */
    private int line;

    /** The column, where the error occured. */
    private int column;

    /**
     * Creates a new ParseException with the given message.
     *
     * @param message the message
     */
    public ParseException(final String message) {
        super(message);
        fillLocation(null);
    }

    /**
     * Creates a new ParseException with the given root exception.
     *
     * @param e the exception
     */
    public ParseException(final Exception e) {
        super(e);
        fillLocation(null);
    }

    /**
     * Creates a new ParseException with the given message and root exception.
     *
     * @param s the message
     * @param e the exception
     */
    public ParseException(final String s, final Exception e) {
        super(s, e);
        fillLocation(null);
    }

    /**
     * Creates a new ParseException with the given message and the locator.
     *
     * @param message the message
     * @param locator the locator of the parser
     */
    public ParseException(final String message, final Locator locator) {
        super(message);
        fillLocation(locator);
    }

    /**
     * Creates a new ParseException with the given root exception
     * and the locator.
     *
     * @param e the exception
     * @param locator the locator of the parser
     */
    public ParseException(final Exception e, final Locator locator) {
        super(e);
        fillLocation(locator);
    }

    /**
     * Creates a new ParseException with the given message, root exception
     * and the locator.
     *
     * @param s the message
     * @param e the exception
     * @param locator the locator of the parser
     */
    public ParseException(final String s, final Exception e, final Locator locator) {
        super(s, e);
        fillLocation(locator);
    }

    /**
     * Modifies the message to give more detailed location information.
     *
     * @return the modified exception message.
     */
    public String getMessage() {
        final StringBuffer message = new StringBuffer(String.valueOf(super.getMessage()));
        message.append(" [Location: Line=");
        message.append(this.line);
        message.append(" Column=");
        message.append(this.column);
        message.append("] ");
        return message.toString();
    }

    /**
     * Fills the location with the given locator.
     *
     * @param locator the locator or null.
     */
    protected void fillLocation (final Locator locator) {
        if (locator == null) {
            this.line = -1;
            this.column = -1;
        }
        else {
            this.line = locator.getLineNumber();
            this.column = locator.getColumnNumber();
        }
    }

    /**
     * Returns the line of the parse position where the error occured.
     *
     * @return the line number or -1 if not known.
     */
    public int getLine() {
        return this.line;
    }

    /**
     * Returns the column of the parse position where the error occured.
     *
     * @return the column number or -1 if not known.
     */
    public int getColumn() {
        return this.column;
    }


    /**
     * Prints the stack trace to the specified stream.
     *
     * @param stream  the output stream.
     */
    public void printStackTrace(final PrintStream stream) {
        super.printStackTrace(stream);
        if (getException() != null) {
            stream.println("ParentException: ");
            getException().printStackTrace(stream);
        }
    }

    /**
     * Override toString to pick up any embedded exception.
     *
     * @return A string representation of this exception.
     */
    public String toString() {
        return getClass().getName() + ": " + getMessage();
    }

    /**
     * Prints the stack trace to the specified writer.
     *
     * @param writer  the writer.
     */
    public void printStackTrace(final PrintWriter writer) {
        super.printStackTrace(writer);
        if (getException() != null) {
            writer.println("ParentException: ");
            getException().printStackTrace(writer);
        }
    }

}

