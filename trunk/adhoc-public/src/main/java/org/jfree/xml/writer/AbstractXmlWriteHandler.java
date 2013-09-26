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
 * ----------------------------
 * AbstractXmlWriteHandler.java
 * ----------------------------
 * (C) Copyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: AbstractXmlWriteHandler.java,v 1.3 2005/10/18 13:35:06 mungady Exp $
 *
 * Changes (from 26-Nov-2003)
 * --------------------------
 * 26-Nov-2003 : Added standard header and Javadocs (DG);
 *
 */

package org.jfree.xml.writer;

/**
 * A base class for implementing a handler that writes the XML for an object of a particular
 * class.
 */
public abstract class AbstractXmlWriteHandler implements XmlWriteHandler {
    
    /** The root handler. */
    private RootXmlWriteHandler rootHandler;

    /**
     * Creates a new handler.
     */
    public AbstractXmlWriteHandler() {
        super();
    }

    /**
     * Returns the root handler.
     * 
     * @return the root handler.
     */
    public RootXmlWriteHandler getRootHandler() {
        return this.rootHandler;
    }
    
    /**
     * Sets the root handler.
     * 
     * @param rootHandler  the root handler.
     */
    public void setRootHandler(final RootXmlWriteHandler rootHandler) {
        this.rootHandler = rootHandler;
    }

}
