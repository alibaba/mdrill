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
 * --------------------
 * ListReadHandler.java
 * --------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ListReadHandler.java,v 1.3 2005/10/18 13:33:32 mungady Exp $
 *
 * Changes
 * -------
 * 12-Nov-2003 : Initial version (TM);
 *  
 */

package org.jfree.xml.parser.coretypes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.jfree.xml.parser.AbstractXmlReadHandler;
import org.jfree.xml.parser.XmlReadHandler;
import org.jfree.xml.parser.XmlReaderException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A SAX handler for reading a list from an XML element.
 */
public class ListReadHandler extends AbstractXmlReadHandler {

    /** The list under construction. */
    private List retval;
    
    /** The handlers. */
    private ArrayList handlers;
    
    /** The type of list ('array-list', 'linked-list', 'stack', 'vector'). */
    private String listType;

    /**
     * Default constructor.
     */
    public ListReadHandler() {
        super();
    }

    /**
     * Start parsing.
     * 
     * @param attrs  the attributes.
     * 
     * @throws SAXException if there is a parsing error.
     */
    protected void startParsing(final Attributes attrs) throws SAXException {
        this.listType = attrs.getValue("type");
        if (this.listType == null) {
            this.listType = "array-list";
        }
        this.handlers = new ArrayList();
    }

    /**
     * Gets a handler for a child.
     * 
     * @param tagName  the tag name.
     * @param atts  the attributes.
     * 
     * @return A handler.
     * 
     * @throws XmlReaderException if there is a problem with the reader.
     * @throws SAXException if there is a parsing error.
     */
    protected XmlReadHandler getHandlerForChild(final String tagName, final Attributes atts)
        throws XmlReaderException, SAXException {
        final XmlReadHandler handler = getRootHandler().createHandler(Object.class, tagName, atts);
        this.handlers.add(handler);
        return handler;
    }

    /**
     * Parsing is finished.
     * 
     * @throws SAXException if there is a parsing error.
     * @throws XmlReaderException if there is a problem with the reader.
     * 
     */
    protected void doneParsing() throws SAXException, XmlReaderException {
        final XmlReadHandler[] handler = (XmlReadHandler[])
        this.handlers.toArray(new XmlReadHandler[this.handlers.size()]);
        this.retval = createList(handler.length);
        for (int i = 0; i < handler.length; i++) {
            this.retval.add(handler[i].getObject());
        }
        this.handlers.clear();
    }

    /**
     * Creates a list.
     * 
     * @param initialSize  the initial size.
     * 
     * @return A new list.
     */
    private List createList(final int initialSize) {
        if (this.listType.equals("stack")) {
            return new Stack();
        }
        if (this.listType.equals("linked-list")) {
            return new LinkedList();
        }
        if (this.listType.equals("vector")) {
            return new Vector(initialSize);
        }
        return new ArrayList(initialSize);
    }

    /**
     * Returns the object under construction.
     * 
     * @return The list.
     */
    public Object getObject() {
        return this.retval;
    }
    
}
