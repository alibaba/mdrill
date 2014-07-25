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
 * ----------------
 * ColorReadHandler
 * ----------------
 * (C) Copyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ColorReadHandler.java,v 1.2 2005/10/18 13:33:32 mungady Exp $
 *
 * Changes (from 25-Nov-2003)
 * --------------------------
 * 25-Nov-2003 : Added standard header and Javadocs (DG);
 *
 */

package org.jfree.xml.parser.coretypes;

import java.awt.Color;

import org.jfree.xml.parser.AbstractXmlReadHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A SAX handler for reading {@link Color} objects from an XML element.
 */
public class ColorReadHandler extends AbstractXmlReadHandler  {

    /** The color under construction. */
    private Color color;
    
    /**
     * Creates a new handler.
     */
    public ColorReadHandler() {
        super();
    }

    /**
     * Called at the start of parsing a {@link Color} element, this method reads the attributes and
     * constructs the {@link Color}. 
     * 
     * @param attrs  the attributes.
     * 
     * @throws SAXException to indicate a parsing error.
     */
    protected void startParsing(final Attributes attrs) throws SAXException {
        final String encodedValue = attrs.getValue("value");
        this.color = Color.decode(encodedValue);
        if (attrs.getValue("alpha") != null) {
            this.color = new Color(this.color.getRed(), this.color.getGreen(),
                                   this.color.getBlue(), 
                                   Integer.parseInt(attrs.getValue("alpha")));
        }
    }

    /**
     * Returns the color under construction.
     * 
     * @return the color.
     */
    public Object getObject() {
        return this.color;
    }
    
}
