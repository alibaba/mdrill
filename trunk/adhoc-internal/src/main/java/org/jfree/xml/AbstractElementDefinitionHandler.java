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
 * -------------------------------------
 * AbstractElementDefinitionHandler.java
 * -------------------------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: AbstractElementDefinitionHandler.java,v 1.3 2005/10/18 13:25:44 mungady Exp $
 *
 * Changes 
 * -------------------------
 * 23.06.2003 : Initial version
 *  
 */

package org.jfree.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * An abstract element definition handler.
 * 
 * @author Thomas Morgner
 */
public abstract class AbstractElementDefinitionHandler implements ElementDefinitionHandler {

    /** A parser. */
    private Parser parser;

    /**
     * Creates a new handler.
     * 
     * @param parser  the parser.
     */
    public AbstractElementDefinitionHandler(final Parser parser) {
        this.parser = parser;
    }

    /**
     * Callback to indicate that an XML element start tag has been read by the parser.
     *
     * @param tagName  the tag name.
     * @param attrs  the attributes.
     *
     * @throws SAXException if a parser error occurs or the validation failed.
     */
    public void startElement(final String tagName, final Attributes attrs) throws SAXException {
        // nothing required
    }

    /**
     * Callback to indicate that some character data has been read.
     *
     * @param ch  the character array.
     * @param start  the start index for the characters.
     * @param length  the length of the character sequence.
     * @throws SAXException if a parser error occurs or the validation failed.
     */
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        // nothing required
    }

    /**
     * Callback to indicate that an XML element end tag has been read by the parser.
     *
     * @param tagName  the tag name.
     *
     * @throws SAXException if a parser error occurs or the validation failed.
     */
    public void endElement(final String tagName) throws SAXException {
        // nothing required
    }

    /**
     * Returns the parser.
     *
     * @return The parser.
     */
    public Parser getParser() {
        return this.parser;
    }
}
