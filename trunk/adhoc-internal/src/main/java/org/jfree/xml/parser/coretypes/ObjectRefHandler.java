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
 * ---------------------
 * ObjectRefHandler.java
 * ---------------------
 * (C)opyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ObjectRefHandler.java,v 1.3 2005/10/18 13:33:32 mungady Exp $
 *
 * Changes
 * -------
 * 29-Sep-2003 : Initial version (TM);
 * 25-Nov-2003 : Updated header and Javadocs (DG);
 *
 */

package org.jfree.xml.parser.coretypes;

import org.jfree.xml.parser.AbstractXmlReadHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A SAX handler for an object reference.
 */
public class ObjectRefHandler extends AbstractXmlReadHandler {

    /** The object. */
    private Object object;
  
    /** The property name. */
    private String propertyName;

    /**
     * Creates a new handler.
     */
    public ObjectRefHandler() {
        super();
    }

    /**
     * Starts parsing.
     * 
     * @param attrs  the attributes.
     * 
     * @throws SAXException ???.
     */
    protected void startParsing(final Attributes attrs) throws SAXException {
        final String tagName = getTagName();
        if (tagName.equals("objectRef")) {
            final String sourceName = attrs.getValue("source");
            if (sourceName == null) {
                throw new SAXException("Source name is not defined.");
            }
            this.propertyName = attrs.getValue("property");
            if (this.propertyName == null) {
                throw new SAXException("Property name is not defined.");
            }

            this.object = getRootHandler().getHelperObject(sourceName);
            if (this.object == null) {
                throw new SAXException("Referenced object is undefined.");
            }
        }
    }

    /**
     * Returns the property name.
     * 
     * @return the property name.
     */
    public String getPropertyName() {
        return this.propertyName;
    }

    /**
     * Returns the value.
     * 
     * @return the value.
     */
    public Object getObject() {
        return this.object;
    }

}
