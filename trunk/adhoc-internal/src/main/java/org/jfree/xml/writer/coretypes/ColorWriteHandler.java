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
 * ColorWriteHandler.java
 * ----------------------
 * (C) Copyright 2003-2005, by Object Refinery Limited and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ColorWriteHandler.java,v 1.3 2005/10/18 13:35:21 mungady Exp $
 *
 * Changes (from 23-Dec-2003)
 * --------------------------
 * 23-Dec-2003 : Added standard header and Javadocs (DG);
 *
 */

package org.jfree.xml.writer.coretypes;

import java.awt.Color;
import java.io.IOException;

import org.jfree.xml.writer.AbstractXmlWriteHandler;
import org.jfree.xml.writer.AttributeList;
import org.jfree.xml.writer.XMLWriter;
import org.jfree.xml.writer.XMLWriterException;

/**
 * A handler for writing a {@link Color} object.
 */
public class ColorWriteHandler extends AbstractXmlWriteHandler  {

    /**
     * Default constructor.
     */
    public ColorWriteHandler() {
        super();
    }

    /**
     * Performs the writing of a {@link Color} object.
     *
     * @param tagName  the tag name.
     * @param object  the {@link Color} object.
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
        final Color color = (Color) object;
        final AttributeList attribs = new AttributeList();
        if (mPlexAttribute != null) {
            attribs.setAttribute(mPlexAttribute, mPlexValue);
        }
        attribs.setAttribute("value", encodeColor(color));
        if (color.getAlpha() != 255) {
            attribs.setAttribute("alpha", String.valueOf(color.getAlpha()));
        }
        writer.writeTag(tagName, attribs, true);
    }

    private String encodeColor(final Color color) {
        return "#" + encodeInt(color.getRed()) 
            + encodeInt(color.getGreen()) + encodeInt(color.getBlue());
    }

    private String encodeInt(final int i) {
        final String retVal = Integer.toHexString(i);
        if (retVal.length() == 1) {
            return "0" + retVal;
        } 
        else {
            return retVal;
        }
    }
}
