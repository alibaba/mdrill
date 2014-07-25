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
 * ElementDefinitionHandler.java
 * ----------------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ElementDefinitionHandler.java,v 1.4 2005/11/14 10:57:22 mungady Exp $
 *
 * Changes
 * -------
 * 21-Feb-2003 : Added standard header and Javadocs (DG);
 * 29-Apr-2003 : Distilled from the JFreeReport project and moved into JCommon
 *
 */

package org.jfree.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A element definition handler. The element definition handler is used to
 * represent a certain parser state. The current state is set in the parser
 * using the pushFactory() method. The parser forwards any incoming SAXEvent
 * to the current handler, until the handler is removed with popFactory().
 *
 * @author Thomas Morgner
 */
public interface ElementDefinitionHandler {

    /**
     * Callback to indicate that an XML element start tag has been read by the parser.
     *
     * @param tagName  the tag name.
     * @param attrs  the attributes.
     *
     * @throws SAXException if a parser error occurs or the validation failed.
     */
    public void startElement(String tagName, Attributes attrs) throws SAXException;

    /**
     * Callback to indicate that some character data has been read.
     *
     * @param ch  the character array.
     * @param start  the start index for the characters.
     * @param length  the length of the character sequence.
     * @throws SAXException if a parser error occurs or the validation failed.
     */
    public void characters(char[] ch, int start, int length) throws SAXException;

    /**
     * Callback to indicate that an XML element end tag has been read by the parser.
     *
     * @param tagName  the tag name.
     *
     * @throws SAXException if a parser error occurs or the validation failed.
     */
    public void endElement(String tagName) throws SAXException;

    /**
     * Returns the parser.
     *
     * @return The parser.
     */
    public Parser getParser();

}
