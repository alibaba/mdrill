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
 * ------------------------
 * GenericWriteHandler.java
 * ------------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: GenericWriteHandler.java,v 1.6 2005/11/16 15:58:41 taqua Exp $
 *
 * Changes
 * -------
 * 23-Sep-2003 : Initial version (TM);
 * 23-Dec-2003 : Added missing Javadocs (DG);
 * 
 */

package org.jfree.xml.writer.coretypes;

import java.io.IOException;
import java.util.ArrayList;

import org.jfree.util.Log;
import org.jfree.xml.util.AttributeDefinition;
import org.jfree.xml.util.GenericObjectFactory;
import org.jfree.xml.util.ObjectDescriptionException;
import org.jfree.xml.util.PropertyDefinition;
import org.jfree.xml.writer.AbstractXmlWriteHandler;
import org.jfree.xml.writer.AttributeList;
import org.jfree.xml.writer.RootXmlWriteHandler;
import org.jfree.xml.writer.XMLWriter;
import org.jfree.xml.writer.XMLWriterException;

/**
 * A handler for writing generic objects.
 */
public class GenericWriteHandler extends AbstractXmlWriteHandler {

    private GenericObjectFactory factory;

    /**
     * Creates a new handler.
     * 
     * @param factory  the object factory.
     */
    public GenericWriteHandler(final GenericObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * Performs the writing of a generic object.
     *
     * @param tagName  the tag name.
     * @param object  the generic object.
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

        try {
            this.factory.readProperties(object);

            final AttributeList attributes = new AttributeList();
            if (mPlexAttribute != null) {
                attributes.setAttribute(mPlexAttribute, mPlexValue);
            }
            final AttributeDefinition[] attribDefs = this.factory.getAttributeDefinitions();
            final ArrayList properties = new ArrayList();
            for (int i = 0; i < attribDefs.length; i++) {
                final AttributeDefinition adef = attribDefs[i];
                final String pName = adef.getAttributeName();
                final Object propValue = this.factory.getProperty(adef.getPropertyName());
                if (propValue != null) {
                    Log.debug(
                        "Here: " + this.factory.getBaseClass() + " -> " + adef.getPropertyName()
                    );
                    final String value = adef.getHandler().toAttributeValue(propValue);
                    if (value != null) {
                        attributes.setAttribute(pName, value);
                    }
                }
                properties.add(adef.getPropertyName());
            }
            writer.writeTag(tagName, attributes, false);
            writer.startBlock();

            final PropertyDefinition[] propertyDefs = this.factory.getPropertyDefinitions();
            final RootXmlWriteHandler rootHandler = getRootHandler();
            for (int i = 0; i < propertyDefs.length; i++) {
                final PropertyDefinition pDef = propertyDefs[i];
                final String elementName = pDef.getElementName();
                rootHandler.write
                    (elementName, this.factory.getProperty(pDef.getPropertyName()),
                            this.factory.getTypeForTagName(elementName), writer);
            }
            writer.endBlock();
            writer.writeCloseTag(tagName);
        }
        catch (ObjectDescriptionException ode) {
            Log.warn ("Unable to write element", ode);
            throw new IOException(ode.getMessage());
        }
    }

}
