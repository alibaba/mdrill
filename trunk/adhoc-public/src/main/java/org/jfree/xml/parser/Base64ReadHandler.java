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
 * ----------------------
 * Base64ReadHandler.java
 * ----------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: Base64ReadHandler.java,v 1.4 2005/11/08 14:16:51 mungady Exp $
 *
 * Changes 
 * -------
 * 11-Feb-2004 : Added standard header and Javadocs (DG);
 *  
 */

package org.jfree.xml.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.jfree.xml.util.Base64;
import org.xml.sax.SAXException;

/**
 * A read handler for Base64 encoded elements.
 * 
 * @deprecated base64 encoded elements are no longer supported ...
 */
public class Base64ReadHandler extends AbstractXmlReadHandler {
    
    /** The encoded object. */
    private String encodedObject;
    
    /**
     * Creates a new handler.
     */
    public Base64ReadHandler() {
        super();
    }

    /**
     * Process character data.
     * 
     * @param ch  the character buffer.
     * @param start  the start index.
     * @param length  the number of characters.
     * 
     * @throws SAXException ???.
     */
    public void characters(final char[] ch, final int start, final int length)
        throws SAXException {
        this.encodedObject = new String(ch, start, length);
    }

    /**
     * Returns the object under construction.
     * 
     * @return the object
     * 
     * @throws XmlReaderException ???.
     */
    public Object getObject() throws XmlReaderException {
        try {
            final byte[] bytes = Base64.decode(this.encodedObject.toCharArray());
            final ObjectInputStream in =
                new ObjectInputStream(new ByteArrayInputStream(bytes));
            return in.readObject();
        } 
        catch (IOException e) {
            throw new XmlReaderException("Can't read class for <" + getTagName() + ">", e);
        } 
        catch (ClassNotFoundException e) {
            throw new XmlReaderException("Class not found for <" + getTagName() + ">", e);
        }
    }
}
