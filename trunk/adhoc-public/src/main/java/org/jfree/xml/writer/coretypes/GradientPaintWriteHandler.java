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
 * GradientPaintWriteHandler.java
 * ------------------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: GradientPaintWriteHandler.java,v 1.4 2005/10/18 13:35:21 mungady Exp $
 *
 * Changes
 * -------
 * 12-Nov-2003 : Initial version (TM);
 * 23-Dec-2003 : Updated header (DG);
 * 
 */

package org.jfree.xml.writer.coretypes;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.geom.Point2D;
import java.io.IOException;

import org.jfree.xml.writer.AbstractXmlWriteHandler;
import org.jfree.xml.writer.RootXmlWriteHandler;
import org.jfree.xml.writer.XMLWriter;
import org.jfree.xml.writer.XMLWriterException;

/**
 * A handler for writing {@link GradientPaint} objects.
 */
public class GradientPaintWriteHandler extends AbstractXmlWriteHandler  {

    /**
     * Default constructor.
     */
    public GradientPaintWriteHandler() {
        super();
    }

    /**
     * Performs the writing of a {@link GradientPaint} object.
     *
     * @param tagName  the tag name.
     * @param object  the {@link GradientPaint} object.
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
        final GradientPaint paint = (GradientPaint) object;
        writer.writeTag(tagName, mPlexAttribute, mPlexValue, false);
        writer.startBlock();
        final RootXmlWriteHandler rootHandler = getRootHandler();
        rootHandler.write("color1", paint.getColor1(), Color.class, writer);
        writer.allowLineBreak();
        rootHandler.write("color2", paint.getColor2(), Color.class, writer);
        writer.allowLineBreak();
        rootHandler.write("point1", paint.getPoint1(), Point2D.class, writer);
        writer.allowLineBreak();
        rootHandler.write("point2", paint.getPoint2(), Point2D.class, writer);
        writer.endBlock();
        writer.writeCloseTag(tagName);        
    }
}
