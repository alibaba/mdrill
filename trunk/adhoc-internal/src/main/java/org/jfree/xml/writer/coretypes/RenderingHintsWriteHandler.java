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
 * --------------------------
 * RenderingHintsHandler.java
 * --------------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: RenderingHintsWriteHandler.java,v 1.5 2005/10/18 13:35:21 mungady Exp $
 *
 * Changes 
 * -------------------------
 * 22.11.2003 : Initial version
 *  
 */

package org.jfree.xml.writer.coretypes;

import java.awt.RenderingHints;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;

import org.jfree.util.Log;
import org.jfree.xml.writer.AbstractXmlWriteHandler;
import org.jfree.xml.writer.AttributeList;
import org.jfree.xml.writer.XMLWriter;
import org.jfree.xml.writer.XMLWriterException;
import org.jfree.xml.writer.XMLWriterSupport;

/**
 * A handler for writing a {@link RenderingHints} object.
 */
public class RenderingHintsWriteHandler extends AbstractXmlWriteHandler {

    /**
     * Default constructor.
     */
    public RenderingHintsWriteHandler() {
        super();
    }

    /**
     * Performs the writing of a single object.
     *
     * @param tagName  the tag name.
     * @param object  the object.
     * @param writer  the writer.
     * @param mPlexAttribute  ??.
     * @param mPlexValue  ??.
     * 
     * @throws IOException if there is an I/O error.
     * @throws XMLWriterException if there is a writer problem.
     */
    public void write(final String tagName, final Object object, final XMLWriter writer,
                      final String mPlexAttribute, final String mPlexValue)
        throws IOException, XMLWriterException {

        writer.writeTag(tagName, mPlexAttribute, mPlexValue, XMLWriterSupport.OPEN);
        writer.allowLineBreak();
        final RenderingHints hints = (RenderingHints) object;
        final Iterator it = hints.keySet().iterator();
        while (it.hasNext()) {
            final RenderingHints.Key key = (RenderingHints.Key) it.next();
            final String keyname = hintFieldToString(key);
            final String value = hintFieldToString(hints.get(key));
            final AttributeList attribs = new AttributeList();
            attribs.setAttribute("key", keyname);
            attribs.setAttribute("value", value);
            writer.writeTag("entry", attribs, XMLWriterSupport.CLOSE);
            writer.allowLineBreak();
        }
        writer.writeCloseTag(tagName);
        writer.allowLineBreak();
    }

    private String hintFieldToString(final Object o) {
        final Field[] fields = RenderingHints.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            final Field f = fields[i];
            if (Modifier.isFinal(f.getModifiers()) 
                && Modifier.isPublic(f.getModifiers()) 
                && Modifier.isStatic(f.getModifiers())) {
                try {
                    final Object value = f.get(null);
                    if (o.equals(value)) {
                        return f.getName();
                    }
                }
                catch (Exception e) {
                    Log.info ("Unable to write RenderingHint", e);
                }
            }
        }
        throw new IllegalArgumentException("Invalid value given");
    }

}
