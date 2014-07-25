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
 * XmlWriteHandler.java
 * --------------------
 * (C) Copyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: XmlWriteHandler.java,v 1.3 2005/10/18 13:35:06 mungady Exp $
 *
 * Changes (from 26-Nov-2003)
 * --------------------------
 * 26-Nov-2003 : Added standard header and Javadocs (DG);
 *
 */

package org.jfree.xml.writer;

import java.io.IOException;

/**
 * The interface that must be supported by all XML write handlers.
 */
public interface XmlWriteHandler {

    /**
     * Returns the root handler for this write handler. The root handler
     * will be used to resolve the child handlers.
     * 
     * @return the root handler.
     */
    public RootXmlWriteHandler getRootHandler();

    /**
     * Sets the root handler.
     * 
     * @param rootHandler  the root handler.
     */
    public void setRootHandler(RootXmlWriteHandler rootHandler);

    /**
     * Performs the writing of a single object.
     *
     * @param tagName  the tag name for the generated xml element.
     * @param object  the object to be written.
     * @param writer  the writer.
     * @param mPlexAttribute  the multiplexer selector attribute name.
     * @param mPlexValue the multiplexers attribute value corresponding to this
     * object type.
     * 
     * @throws IOException if an IOError occured.
     * @throws XMLWriterException if an XmlDefinition error occured.
     */
    public void write(String tagName, Object object, XMLWriter writer,
                      String mPlexAttribute, String mPlexValue)
        throws IOException, XMLWriterException;

}
