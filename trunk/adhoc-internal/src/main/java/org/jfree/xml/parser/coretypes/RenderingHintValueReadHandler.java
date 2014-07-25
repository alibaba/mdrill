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
 * ----------------------------------
 * RenderingHintValueReadHandler.java
 * ----------------------------------
 * (C)opyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: RenderingHintValueReadHandler.java,v 1.3 2005/10/18 13:33:32 mungady Exp $
 *
 * Changes 
 * -------
 * 03-Dec-2003 : Initial version
 * 11-Feb-2004 : Added missing Javadocs (DG);
 * 
 */

package org.jfree.xml.parser.coretypes;

import java.awt.RenderingHints;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.jfree.util.Log;
import org.jfree.xml.parser.AbstractXmlReadHandler;
import org.jfree.xml.parser.XmlReaderException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A read handler for a rendering hint value.
 */
public class RenderingHintValueReadHandler extends AbstractXmlReadHandler {

    /** The key under construction. */
    private Object key;
    
    /** The value under construction. */
    private Object value;

    /**
     * Creates a new read handler.
     */
    public RenderingHintValueReadHandler() {
        super();
    }

    /**
     * Starts parsing.
     *
     * @param attrs  the attributes.
     *
     * @throws SAXException if there is a parsing error.
     */
    protected void startParsing(final Attributes attrs) throws SAXException {
        final String keyText = attrs.getValue("key");
        final String valueText = attrs.getValue("value");
        this.key = stringToHintField(keyText);
        this.value = stringToHintField(valueText);
    }

    private Object stringToHintField (final String name) {
        final Field[] fields = RenderingHints.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            final Field f = fields[i];
            if (Modifier.isFinal(f.getModifiers()) 
                && Modifier.isPublic(f.getModifiers()) 
                && Modifier.isStatic(f.getModifiers())) {
                try {
                    final String fieldName = f.getName();
                    if (fieldName.equals(name)) {
                        return f.get(null);
                    }
                }
                catch (Exception e) {
                    Log.info ("Unable to write RenderingHint", e);
                }
            }
        }
        throw new IllegalArgumentException("Invalid value given");
    }

    /**
     * Returns the object for this element.
     *
     * @return the object.
     *
     * @throws XmlReaderException if there is a parsing error.
     */
    public Object getObject() throws XmlReaderException {
        return new Object[] {this.key, this.value};
    }

    /**
     * Returns the key.
     * 
     * @return the key.
     */
    public Object getKey() {
        return this.key;
    }

    /**
     * Returns the value.
     * 
     * @return the value.
     */
    public Object getValue() {
        return this.value;
    }
    
}
