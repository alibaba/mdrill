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
 * Rectangle2DReadHandler
 * ----------------------
 * (C) Copyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   -;
 *
 * $Id: Rectangle2DReadHandler.java,v 1.2 2005/10/18 13:33:32 mungady Exp $
 *
 * Changes
 * -------
 *
 */

package org.jfree.xml.parser.coretypes;

import java.awt.geom.Rectangle2D;

import org.jfree.xml.parser.AbstractXmlReadHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A handler for reading a {@link Rectangle2D} object.
 */
public class Rectangle2DReadHandler extends AbstractXmlReadHandler  {
    
    /** The rectangle being constructed. */
    private Rectangle2D rectangle;

    /**
     * Default constructor.
     */
    public Rectangle2DReadHandler() {
        super();
    }

    /**
     * Begins parsing.
     * 
     * @param attrs  the attributes.
     * 
     * @throws SAXException if there is a parsing error.
     */
    protected void startParsing(final Attributes attrs) throws SAXException {
        final String type = attrs.getValue("type");
        this.rectangle = createRect(type);
        final String x = attrs.getValue("x");
        final String y = attrs.getValue("y");
        final String w = attrs.getValue("width");
        final String h = attrs.getValue("height");

        this.rectangle.setRect(
            Double.parseDouble(x), Double.parseDouble(y),
            Double.parseDouble(w), Double.parseDouble(h)
        );
    }

    /**
     * Creates a rectangle.
     * 
     * @param type  the type ('float' or 'double').
     * 
     * @return The rectangle.
     */
    private Rectangle2D createRect(final String type) {
        if ("float".equals(type)) {
            return new Rectangle2D.Float();
        }
        return new Rectangle2D.Double();
    }

    /**
     * Returns the object under construction.
     * 
     * @return The object.
     */
    public Object getObject() {
        return this.rectangle;
    }
}
