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
 * ClassDescription.java
 * ---------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ClassDescription.java,v 1.2 2005/10/18 13:32:37 mungady Exp $
 *
 * Changes
 * -------
 * 21-Jun-2003 : Initial version (TM);
 * 26-Nov-2003 : Updated header and Javadocs (DG);
 * 
 */

package org.jfree.xml.generator.model;

/**
 * A description of a Java class.
 */
public class ClassDescription {

    /** Storage for info about properties. */
    private PropertyInfo[] properties;
    
    /** Constructor descriptions. */
    private TypeInfo[] constructorDescription;
    
    /** The class. */
    private Class objectClass;
    
    /** A description. */
    private String description;
    
    /** The register key. */
    private String registerKey;
    
    /** The super class. */
    private Class superClass;
    
    /** ??. */
    private boolean preserve;
    
    /** The comments. */
    private Comments comments;
    
    /** The source. */
    private String source;

    /**
     * Creates a new class description.
     * 
     * @param objectClass  the class.
     */
    public ClassDescription(final Class objectClass) {
        if (objectClass == null) {
            throw new NullPointerException();
        }
        this.objectClass = objectClass;
    }

    /**
     * Returns the info about properties.
     * 
     * @return the info about properties.
     */
    public PropertyInfo[] getProperties() {
        return this.properties;
    }

    /**
     * Sets the info about the class properties.
     * 
     * @param properties  the properties.
     */
    public void setProperties(final PropertyInfo[] properties) {
        this.properties = properties;
    }

    /**
     * Returns the object's class.
     * 
     * @return the object's class.
     */
    public Class getObjectClass() {
        return this.objectClass;
    }

    /**
     * Returns the description.
     * 
     * @return the description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description for the object.
     * 
     * @param description  the description.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Returns the class name.
     * 
     * @return the class name.
     */
    public String getName() {
        if (getObjectClass() == null) {
            return null;
        }
        return getObjectClass().getName();
    }

    /**
     * Returns the super class.
     * 
     * @return the super class.
     */
    public Class getSuperClass() {
        return this.superClass;
    }

    /**
     * Sets the super class.
     * 
     * @param superClass  the super class.
     */
    public void setSuperClass(final Class superClass) {
        this.superClass = superClass;
    }

    /**
     * Returns the preserve flag.
     * 
     * @return a boolean.
     */
    public boolean isPreserve() {
        return this.preserve;
    }

    /**
     * Sets the preserve flag.
     * 
     * @param preserve  the new value of the flag.
     */
    public void setPreserve(final boolean preserve) {
        this.preserve = preserve;
    }

    /**
     * Returns the register key.
     * 
     * @return the register key.
     */
    public String getRegisterKey() {
        return this.registerKey;
    }

    /**
     * Sets the register key.
     * 
     * @param registerKey the register key.
     */
    public void setRegisterKey(final String registerKey) {
        this.registerKey = registerKey;
    }

    /**
     * Returns the constructor descriptions.
     * 
     * @return the constructor descriptions.
     */
    public TypeInfo[] getConstructorDescription() {
        return this.constructorDescription;
    }

    /**
     * Sets the constructor description.
     * 
     * @param constructorDescription  the constructor description.
     */
    public void setConstructorDescription(final TypeInfo[] constructorDescription) {
        this.constructorDescription = constructorDescription;
    }

    /**
     * Returns a property.
     * 
     * @param name  the property name.
     * 
     * @return a property.
     */
    public PropertyInfo getProperty (final String name) {
        if (this.properties == null) {
            return null;
        }
        for (int i = 0; i < this.properties.length; i++) {
            if (this.properties[i].getName().equals(name)) {
                return this.properties[i];
            }
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the description is undefined.
     * 
     * @return a boolean.
     */
    public boolean isUndefined() {
        if (this.properties != null) {
            if (this.properties.length > 0) {
                return false;
            }
        }
        if (isPreserve()) {
            return false;
        }
        if (getRegisterKey() != null) {
            return false;
        }
        if (getConstructorDescription() != null) {
            if (getConstructorDescription().length > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the comments for the class description.
     * 
     * @return The comments.
     */
    public Comments getComments() {
        return this.comments;
    }

    /**
     * Sets the comments for the class description.
     * 
     * @param comments  the comments.
     */
    public void setComments(final Comments comments) {
        this.comments = comments;
    }

    /**
     * Returns the source for the class description.
     * 
     * @return The source.
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Sets the source for the class description.
     * 
     * @param source  the source.
     */
    public void setSource(final String source) {
        this.source = source;
    }
    
}

