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
 * ---------------
 * FontReadHandler
 * ---------------
 * (C) Copyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: FontReadHandler.java,v 1.2 2005/10/18 13:33:32 mungady Exp $
 *
 * Changes (from 25-Nov-2003)
 * --------------------------
 * 25-Nov-2003 : Added standard header and Javadocs (DG);
 *
 */

package org.jfree.xml.parser.coretypes;

import java.awt.Font;

import org.jfree.xml.parser.AbstractXmlReadHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A SAX handler for reading a font definition.
 */
public class FontReadHandler extends AbstractXmlReadHandler  {
    
    /** The font under construction. */
    private Font font;

    /**
     * Creates a new SAX handler for reading a {@link Font} from XML.
     */
    public FontReadHandler() {
        super();
    }

    /**
     * Called at the start of parsing a font element, this method reads the attributes and
     * constructs the font. 
     * 
     * @param attrs  the attributes.
     * 
     * @throws SAXException to indicate a parsing error.
     */
    protected void startParsing(final Attributes attrs) throws SAXException {
        final String family = attrs.getValue("family");
        final int size = Integer.parseInt(attrs.getValue("size"));
        final int style = getFontStyle(attrs.getValue("style"));
        this.font = new Font(family, style, size);
    }

    /**
     * Converts a string to a font style constant.
     * 
     * @param style  the style as text.
     * 
     * @return The font style.
     */
    private int getFontStyle (final String style) {
        if ("bold-italic".equals(style)) {
            return Font.BOLD | Font.ITALIC;
        }
        if ("bold".equals(style)) {
            return Font.BOLD;
        }
        if ("italic".equals(style)) {
            return Font.ITALIC;
        }
        return Font.PLAIN;
    }

    /**
     * Returns the font under construction.
     * 
     * @return the font.
     */
    public Object getObject() {
        return this.font;
    }
    
}
