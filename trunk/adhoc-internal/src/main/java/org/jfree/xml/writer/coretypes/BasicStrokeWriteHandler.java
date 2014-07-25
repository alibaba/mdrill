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
 * BasicStrokeWriteHandler.java
 * ----------------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: BasicStrokeWriteHandler.java,v 1.3 2005/10/18 13:35:21 mungady Exp $
 *
 * Changes
 * -------
 * 12-Nov-2003 : Initial version (TM);
 * 25-Nov-2003 : Updated header (DG);
 * 
 */

package org.jfree.xml.writer.coretypes;

import java.awt.BasicStroke;
import java.io.IOException;

import org.jfree.xml.writer.AbstractXmlWriteHandler;
import org.jfree.xml.writer.AttributeList;
import org.jfree.xml.writer.XMLWriter;
import org.jfree.xml.writer.XMLWriterException;

/**
 * A handler that can write the XML description for a {@link BasicStroke} object.
 */
public class BasicStrokeWriteHandler extends AbstractXmlWriteHandler  {

    /**
     * Creates a new handler.
     */
    public BasicStrokeWriteHandler() {
        super();
    }

    /**
     * Performs the writing of a single object.
     *
     * @param tagName  the tag name.
     * @param object  the object ({@link BasicStroke} expected).
     * @param writer  the writer.
     * @param mPlexAttribute  ??
     * @param mPlexValue  ??
     * 
     * @throws IOException if there is an I/O problem.
     * @throws XMLWriterException if there is a problem with the writer.
     */
    public void write(final String tagName, final Object object, final XMLWriter writer,
                      final String mPlexAttribute, final String mPlexValue)
        throws IOException, XMLWriterException {
        final BasicStroke stroke = (BasicStroke) object;
        final float[] dashArray = stroke.getDashArray();
        final float dashPhase = stroke.getDashPhase();
        final int endCap = stroke.getEndCap();
        final int lineJoin = stroke.getLineJoin();
        final float lineWidth = stroke.getLineWidth();
        final float miterLimit = stroke.getMiterLimit();
        final AttributeList attribs = new AttributeList();
        if (mPlexAttribute != null) {
            attribs.setAttribute(mPlexAttribute, mPlexValue);
        }
        attribs.setAttribute("type", "basic");
        attribs.setAttribute("endCap", String.valueOf(endCap));
        attribs.setAttribute("lineJoin", String.valueOf(lineJoin));
        attribs.setAttribute("lineWidth", String.valueOf(lineWidth));
        attribs.setAttribute("miterLimit", String.valueOf(miterLimit));
        if (dashArray != null) {
            attribs.setAttribute("dashArray", toString(dashArray));
            attribs.setAttribute("dashPhase", String.valueOf(dashPhase));
        }
        writer.writeTag(tagName, attribs, true);
    }

    /**
     * A utility method that converts a dash array (from a {@link BasicStroke} object) to 
     * a {@link String}.
     * 
     * @param dashArray  the dash array.
     * 
     * @return a {@link String} representing the dash array.
     */
    private String toString(final float[] dashArray) {
        final StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < dashArray.length; i++) {
            final float f = dashArray[i];
            if (i != 0) {
                buffer.append(',');
            }
            buffer.append(f);
        }
        return buffer.toString();
    }
    
}
