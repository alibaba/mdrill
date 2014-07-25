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
 * -----------------
 * PropertyInfo.java
 * -----------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: PropertyInfo.java,v 1.2 2005/10/18 13:32:37 mungady Exp $
 *
 * Changes 
 * -------
 * 21-Jun-2003 : Initial version (TM)
 *  
 */

package org.jfree.xml.generator.model;

/**
 * Information about a property.
 */
public class PropertyInfo extends TypeInfo {

    /** Preserve? */
    private boolean preserve;
    
    /** Is there a read method available? */
    private boolean readMethodAvailable;
    
    /** Is there a write method available? */
    private boolean writeMethodAvailable;

    /** The property type - indicates how the property is described in XML. */
    private PropertyType propertyType;
    
    /** The XML name. */
    private String xmlName;
    
    /** The XML handler. */
    private String xmlHandler;

    /**
     * Creates a new info object for a property.
     * 
     * @param name  the property name.
     * @param type  the class.
     */
    public PropertyInfo(final String name, final Class type) {
        super(name, type);
        this.propertyType = PropertyType.ELEMENT;
    }

    /**
     * Returns the preserve flag.
     * 
     * @return the preserve flag.
     */
    public boolean isPreserve() {
        return this.preserve;
    }

    /**
     * Sets the preserve flag.
     * 
     * @param preserve  the preserve flag.
     */
    public void setPreserve(final boolean preserve) {
        this.preserve = preserve;
    }

    /**
     * Returns the property type.  This describes how the property is handled in XML.
     * 
     * @return the property type.
     */
    public PropertyType getPropertyType() {
        return this.propertyType;
    }

    /**
     * Sets the property type.
     * 
     * @param propertyType  the type (<code>null</code> not permitted).
     */
    public void setPropertyType(final PropertyType propertyType) {
        if (propertyType == null) {
            throw new NullPointerException();
        }
        this.propertyType = propertyType;
    }

    /**
     * Returns the XML handler.
     * 
     * @return the XML handler.
     */
    public String getXmlHandler() {
        return this.xmlHandler;
    }

    /**
     * Sets the XML handler.
     * 
     * @param xmlHandler  the fully qualified class name for the attribute handler.
     */
    public void setXmlHandler(final String xmlHandler) {
        this.xmlHandler = xmlHandler;
    }

    /**
     * Returns the XML name.
     * 
     * @return the XML name.
     */
    public String getXmlName() {
        return this.xmlName;
    }

    /**
     * Sets the XML name.
     * 
     * @param xmlName  the XML name.
     */
    public void setXmlName(final String xmlName) {
        this.xmlName = xmlName;
    }

    /**
     * Returns <code>true</code> if there is a read method available, and <code>false</code> 
     * otherwise.
     * 
     * @return a boolean.
     */
    public boolean isReadMethodAvailable() {
        return this.readMethodAvailable;
    }

    /**
     * Sets a flag indicating whether or not there is a read method for this property.
     * 
     * @param readMethodAvailable  the new value of the flag.
     */
    public void setReadMethodAvailable(final boolean readMethodAvailable) {
        this.readMethodAvailable = readMethodAvailable;
    }

    /**
     * Returns <code>true</code> if there is a write method available, and <code>false</code> 
     * otherwise.
     * 
     * @return a boolean.
     */
    public boolean isWriteMethodAvailable() {
        return this.writeMethodAvailable;
    }

    /**
     * Sets a flag indicating whether or not there is a write method for this property.
     * 
     * @param writeMethodAvailable  the new value of the flag.
     */
    public void setWriteMethodAvailable(final boolean writeMethodAvailable) {
        this.writeMethodAvailable = writeMethodAvailable;
    }
    
}
