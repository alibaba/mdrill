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
 * AttributeDefinition.java
 * ------------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: AttributeDefinition.java,v 1.3 2005/10/18 13:33:53 mungady Exp $
 *
 * Changes 
 * -------
 * 24.09.2003 : Initial version
 *  
 */

package org.jfree.xml.util;

import org.jfree.xml.attributehandlers.AttributeHandler;

/**
 * An attribute definition.
 */
public class AttributeDefinition {
    
    /** The attribute name. */
    private String attributeName;
    
    /** The attribute handler. */
    private AttributeHandler handler;
    
    /** The property name. */
    private String propertyName;

    /**
     * Creates a new attribute definition.
     * 
     * @param propertyName  the property name.
     * @param attributeName  the attribute name.
     * @param handler  the handler.
     */
    public AttributeDefinition(final String propertyName,
                               final String attributeName,
                               final AttributeHandler handler) {
        this.propertyName = propertyName;
        this.attributeName = attributeName;
        this.handler = handler;
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
     * Returns the attribute name.
     * 
     * @return the attribute name.
     */
    public String getAttributeName() {
        return this.attributeName;
    }

    /**
     * Returns the handler.
     * 
     * @return the handler.
     */
    public AttributeHandler getHandler() {
        return this.handler;
    }
    
}
