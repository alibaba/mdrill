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
 * ListWriteHandler.java
 * ---------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ListWriteHandler.java,v 1.4 2005/10/18 13:35:21 mungady Exp $
 *
 * Changes
 * -------
 * 22-Nov-2003 : Initial version
 *
 */

package org.jfree.xml.writer.coretypes;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jfree.xml.writer.AbstractXmlWriteHandler;
import org.jfree.xml.writer.XMLWriter;
import org.jfree.xml.writer.XMLWriterException;
import org.jfree.xml.writer.XMLWriterSupport;

/**
 * A handler for writing a {@link List} object.
 */
public class ListWriteHandler extends AbstractXmlWriteHandler {

    /**
     * Performs the writing of a {@link List} object.
     *
     * @param tagName  the tag name.
     * @param object  the {@link List} object.
     * @param writer  the writer.
     * @param mPlexAttribute  ??.
     * @param mPlexValue  ??.
     * 
     * @throws IOException if there is an I/O error.
     * @throws XMLWriterException if there is a writer error.
     */
    public void write(final String tagName, final Object object, final XMLWriter writer,
                      final String mPlexAttribute, final String mPlexValue)
        throws IOException, XMLWriterException {

        writer.writeTag(tagName, mPlexAttribute, mPlexValue, XMLWriterSupport.OPEN);

        final List list = (List) object;
        final Iterator it = list.iterator();
        while (it.hasNext()) {
            final Object value = it.next();
            if (value == null) {
                writer.writeTag("null", XMLWriterSupport.CLOSE);
            }
            else {
                getRootHandler().write("object", value, Object.class, writer);
            }
        }

        writer.writeCloseTag(tagName);
    }
    
}
