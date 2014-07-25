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
 * -----------------------
 * XMLWriterException.java
 * -----------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: XMLWriterException.java,v 1.3 2005/10/18 13:35:06 mungady Exp $
 *
 * Changes 
 * -------
 * 21-Nov-2003 : Initial version (TM);
 * 26-Nov-2003 : Header and Javadoc updates (DG);
 *  
 */

package org.jfree.xml.writer;

import org.jfree.xml.util.ObjectDescriptionException;

/**
 * An exception that can be thrown by the {@link XMLWriter} class.
 */
public class XMLWriterException extends ObjectDescriptionException {

    /**
     * Creates a new exception with no message or parent.
     */
    public XMLWriterException() {
        super();
    }

    /**
     * Creates an exception.
     *
     * @param message  the exception message.
     */
    public XMLWriterException(final String message) {
        super(message);
    }

    /**
     * Creates an exception.
     *
     * @param message  the exception message.
     * @param ex  the parent exception.
     */
    public XMLWriterException(final String message, final Exception ex) {
        super(message, ex);
    }
    
}
