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
 * RenderingHintsReadHandler.java
 * ------------------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: RenderingHintsReadHandler.java,v 1.3 2005/10/18 13:33:32 mungady Exp $
 *
 * Changes
 * -------
 * 03-Dec-2003 : Initial version
 * 11-Feb-2004 : Added missing Javadocs (DG);
 * 
 */

package org.jfree.xml.parser.coretypes;

import java.awt.RenderingHints;
import java.util.ArrayList;

import org.jfree.xml.parser.AbstractXmlReadHandler;
import org.jfree.xml.parser.XmlReadHandler;
import org.jfree.xml.parser.XmlReaderException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A read handler that can parse the XML element for a {@link RenderingHints} collection.
 */
public class RenderingHintsReadHandler extends AbstractXmlReadHandler {

    /** The subhandlers. */
    private ArrayList handlers;
    
    /** The rendering hints under construction. */
    private RenderingHints renderingHints;

    /**
     * Creates a new read handler.
     */
    public RenderingHintsReadHandler() {
        super();
    }

    /**
     * Starts parsing.
     *
     * @param attrs  the attributes.
     *
     * @throws SAXException never.
     */
    protected void startParsing(final Attributes attrs) throws SAXException {
        this.handlers = new ArrayList();
    }

    /**
     * Returns the handler for a child element.
     *
     * @param tagName  the tag name.
     * @param atts  the attributes.
     *
     * @return the handler.
     *
     * @throws SAXException  if there is a parsing error.
     * @throws XmlReaderException if there is a reader error.
     */
    protected XmlReadHandler getHandlerForChild(final String tagName, final Attributes atts)
        throws XmlReaderException, SAXException {

        if (!tagName.equals("entry")) {
            throw new SAXException("Expected 'entry' tag.");
        }

        final XmlReadHandler handler = new RenderingHintValueReadHandler();
        this.handlers.add(handler);
        return handler;
    }

    /**
     * Done parsing.
     *
     * @throws SAXException if there is a parsing error.
     * @throws XmlReaderException if there is a reader error.
     */
    protected void doneParsing() throws SAXException, XmlReaderException {
        this.renderingHints = new RenderingHints(null);

        for (int i = 0; i < this.handlers.size(); i++) {
            final RenderingHintValueReadHandler rh =
                (RenderingHintValueReadHandler) this.handlers.get(i);
            this.renderingHints.put(rh.getKey(), rh.getValue());
        }
    }

    /**
     * Returns the object for this element.
     *
     * @return the object.
     *
     * @throws XmlReaderException if there is a parsing error.
     */
    public Object getObject() throws XmlReaderException {
        return this.renderingHints;
    }
}
