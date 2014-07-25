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
 * -------------------------
 * GenericObjectFactory.java
 * -------------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: GenericObjectFactory.java,v 1.4 2005/10/18 13:33:53 mungady Exp $
 *
 * Changes
 * -------
 * 23-Sep-2003 : Initial version (TM);
 *
 */

package org.jfree.xml.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * The generic object factory contains all methods necessary to collect
 * the property values needed to produce a fully instantiated object.
 */
public final class GenericObjectFactory {

    /** Storage for the constructor definitions. */
    private final ConstructorDefinition[] constructorDefinitions;
    
    /** Storage for the property definitions. */
    private final PropertyDefinition[] propertyDefinitions;
    
    /** Storage for the lookup definitions. */
    private final LookupDefinition[] lookupDefinitions;
    
    /** Storage for the attribute definitions. */
    private final AttributeDefinition[] attributeDefinitions;
    
    /** The ordered property names. */
    private final String[] orderedPropertyNames;

    /** Storage for property info. */
    private final HashMap propertyInfos;
    
    /** Storage for property values. */
    private final HashMap propertyValues;

    /** The base class. */
    private final Class baseClass;
    
    /** The register name. */
    private final String registerName;

    /**
     * Creates a new generic object factory.
     * 
     * @param c  the class.
     * @param registerName the (optional) name under which to register the class for
     *                     any later lookup.
     * @param constructors  the constructor definitions.
     * @param propertyDefinitions  the property definitions.
     * @param lookupDefinitions  the lookup definitions.
     * @param attributeDefinitions  the attribute definitions.
     * @param orderedPropertyNames  the ordered property names.
     * 
     * @throws ObjectDescriptionException if there is a problem.
     */
    public GenericObjectFactory(final Class c, 
                                final String registerName,
                                final ConstructorDefinition[] constructors,
                                final PropertyDefinition[] propertyDefinitions,
                                final LookupDefinition[] lookupDefinitions,
                                final AttributeDefinition[] attributeDefinitions,
                                final String[] orderedPropertyNames)
        throws ObjectDescriptionException {

        if (c == null) {
            throw new NullPointerException("BaseClass cannot be null.");
        }
        this.baseClass = c;
        this.registerName = registerName;

        this.propertyInfos = new HashMap();
        this.propertyValues = new HashMap();

        this.constructorDefinitions = constructors;
        this.propertyDefinitions = propertyDefinitions;
        this.lookupDefinitions = lookupDefinitions;
        this.attributeDefinitions = attributeDefinitions;
        this.orderedPropertyNames = orderedPropertyNames;

        try {
            final BeanInfo chartBeaninfo = Introspector.getBeanInfo(c, Object.class);
            final PropertyDescriptor[] pd = chartBeaninfo.getPropertyDescriptors();
            for (int i = 0; i < pd.length; i++) {
                this.propertyInfos.put(pd[i].getName(), pd[i]);
            }
        }
        catch (IntrospectionException ioe) {
            throw new ObjectDescriptionException(
                "This is an ugly solution right now ... dirty hack attack"
            );
        }
    }

    /**
     * A copy constructor.
     * 
     * @param factory  the factory to copy.
     */
    private GenericObjectFactory (final GenericObjectFactory factory) {
        this.baseClass = factory.baseClass;
        this.propertyValues = new HashMap();
        this.orderedPropertyNames = factory.orderedPropertyNames;
        this.constructorDefinitions = factory.constructorDefinitions;
        this.propertyDefinitions = factory.propertyDefinitions;
        this.attributeDefinitions = factory.attributeDefinitions;
        this.propertyInfos = factory.propertyInfos;
        this.registerName = factory.registerName;
        this.lookupDefinitions = factory.lookupDefinitions;
    }

    /**
     * Returns a copy of this instance.
     * 
     * @return a copy of this instance.
     */
    public GenericObjectFactory getInstance () {
        return new GenericObjectFactory(this);
    }

    /**
     * Returns the register name.
     * 
     * @return the register name.
     */
    public String getRegisterName() {
        return this.registerName;
    }

    /**
     * Returns a property descriptor.
     * 
     * @param propertyName  the property name.
     * 
     * @return a property descriptor.
     */
    private PropertyDescriptor getPropertyDescriptor(final String propertyName) {
        return (PropertyDescriptor) this.propertyInfos.get(propertyName);
    }

    /**
     * Returns the class for a tag name.
     * 
     * @param tagName  the tag name.
     * 
     * @return the class.
     * 
     * @throws ObjectDescriptionException if there is a problem.
     */
    public Class getTypeForTagName(final String tagName) throws ObjectDescriptionException {
        final PropertyDefinition pdef = getPropertyDefinitionByTagName(tagName);
        final PropertyDescriptor pdescr = getPropertyDescriptor(pdef.getPropertyName());
        if (pdescr == null) {
            throw new ObjectDescriptionException("Invalid Definition: " + pdef.getPropertyName());
        }
        return pdescr.getPropertyType();
    }

    /**
     * Returns true if there is a property definition for the specified property name.
     * 
     * @param propertyName  the property name.
     * 
     * @return A boolean.
     */
    public boolean isPropertyDefinition (final String propertyName) {
        for (int i = 0; i < this.propertyDefinitions.length; i++) {
            final PropertyDefinition pdef = this.propertyDefinitions[i];
            if (pdef.getPropertyName().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the property definition for the specified property name.
     * 
     * @param propertyName  the property name.
     * 
     * @return the property definition.
     * 
     * @throws ObjectDescriptionException if there is no such property for this object.
     */
    public PropertyDefinition getPropertyDefinitionByPropertyName(final String propertyName)
        throws ObjectDescriptionException {
        for (int i = 0; i < this.propertyDefinitions.length; i++) {
            final PropertyDefinition pdef = this.propertyDefinitions[i];
            if (pdef.getPropertyName().equals(propertyName)) {
                return pdef;
            }
        }
        throw new ObjectDescriptionException(
            "This property is not defined for this kind of object. : " + propertyName
        );
    }

    /**
     * Returns a property definition for the specified tag name.
     * 
     * @param tagName  the tag name.
     * 
     * @return the property definition.
     * 
     * @throws ObjectDescriptionException if there is no such tag defined for this object.
     */
    public PropertyDefinition getPropertyDefinitionByTagName(final String tagName)
        throws ObjectDescriptionException {
        for (int i = 0; i < this.propertyDefinitions.length; i++) {
            final PropertyDefinition pdef = this.propertyDefinitions[i];
            if (pdef.getElementName().equals(tagName)) {
                return pdef;
            }
        }
        throw new ObjectDescriptionException(
            "This tag is not defined for this kind of object. : " + tagName
        );
    }

    /**
     * Returns the constructor definitions.
     * 
     * @return the constructor definitions.
     */
    public ConstructorDefinition[] getConstructorDefinitions() {
        return this.constructorDefinitions;
    }

    /**
     * Returns the attribute definitions.
     * 
     * @return the attribute definitions.
     */
    public AttributeDefinition[] getAttributeDefinitions() {
        return this.attributeDefinitions;
    }

    /**
     * Returns the property definitions.
     * 
     * @return the property definitions.
     */
    public PropertyDefinition[] getPropertyDefinitions() {
        return this.propertyDefinitions;
    }

    /**
     * Returns the property names.
     * 
     * @return the property names.
     */
    public String[] getOrderedPropertyNames() {
        return this.orderedPropertyNames;
    }

    /**
     * Returns the lookup definitions.
     * 
     * @return the lookup definitions.
     */
    public LookupDefinition[] getLookupDefinitions() {
        return this.lookupDefinitions;
    }

    /**
     * Returns the value of the specified property.
     * 
     * @param name  the property name.
     * 
     * @return the property value.
     */
    public Object getProperty(final String name) {
        return this.propertyValues.get(name);
    }

    /**
     * Creates an object according to the definition.
     * 
     * @return the object.
     * 
     * @throws ObjectDescriptionException if there is a problem with the object description.
     */
    public Object createObject() throws ObjectDescriptionException {
        final Class[] cArgs = new Class[this.constructorDefinitions.length];
        final Object[] oArgs = new Object[this.constructorDefinitions.length];
        for (int i = 0; i < cArgs.length; i++) {
            final ConstructorDefinition cDef = this.constructorDefinitions[i];
            cArgs[i] = cDef.getType();
            if (cDef.isNull()) {
                oArgs[i] = null;
            }
            else {
                oArgs[i] = getProperty(cDef.getPropertyName());
            }
        }

        try {
            final Constructor constr = this.baseClass.getConstructor(cArgs);
            final Object o = constr.newInstance(oArgs);
            return o;
        }
        catch (Exception e) {
            throw new ObjectDescriptionException("Ugh! Constructor made a buuuh!", e);
        }
    }

    /**
     * Sets a property value.
     * 
     * @param propertyName  the property name.
     * @param value  the property value.
     * 
     * @throws ObjectDescriptionException if there is a problem with the object description.
     */
    public void setProperty(final String propertyName, final Object value)
        throws ObjectDescriptionException {
        final PropertyDescriptor pdesc = getPropertyDescriptor(propertyName);
        if (pdesc == null) {
            throw new ObjectDescriptionException("Unknown property " + propertyName);
        }

        if (!isAssignableOrPrimitive(pdesc.getPropertyType(), value.getClass())) {
            throw new ObjectDescriptionException(
                "Invalid value: " + pdesc.getPropertyType() + " vs. " + value.getClass()
            );
        }

        this.propertyValues.put(propertyName, value);
    }

    /**
     * Returns <code>true</code> if the base type is a primitive or assignable from the value type.
     * 
     * @param baseType  the base class.
     * @param valueType  the value class.
     * 
     * @return A boolean.
     */
    private boolean isAssignableOrPrimitive(final Class baseType, final Class valueType) {
        if (BasicTypeSupport.isBasicDataType(baseType)) {
            return true;
        }
        // verbose stuff below *should* no longer be needed
        return baseType.isAssignableFrom(valueType);
    }

    /**
     * Returns <code>true<code> if the specified property is...
     * 
     * @param propertyName  the property name.
     * 
     * @return A boolean.
     */
    private boolean isConstructorProperty(final String propertyName) {
        for (int i = 0; i < this.constructorDefinitions.length; i++) {
            final ConstructorDefinition cDef = this.constructorDefinitions[i];
            if (propertyName.equals(cDef.getPropertyName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Writes the properties for the object.
     * 
     * @param object  the object.
     * 
     * @throws ObjectDescriptionException if there is a problem.
     */
    public void writeObjectProperties(final Object object) throws ObjectDescriptionException {
        // this assumes that the order of setting the attributes does not matter.
        for (int i = 0; i < this.orderedPropertyNames.length; i++) {
            try {
                final String name = this.orderedPropertyNames[i];
                if (isConstructorProperty(name)) {
                    continue;
                }
                final Object value = getProperty(name);
                if (value == null) {
                    // do nothing if value is not defined ...
                    continue;
                }
                final PropertyDescriptor pdescr = getPropertyDescriptor(name);
                final Method setter = pdescr.getWriteMethod();
                setter.invoke(object, new Object[]{value});
            }
            catch (Exception e) {
                throw new ObjectDescriptionException(
                    "Failed to set properties." + getBaseClass(), e
                );
            }
        }
    }

    /**
     * Reads the properties.
     * 
     * @param object  the object.
     * 
     * @throws ObjectDescriptionException if there is a problem.
     */
    public void readProperties(final Object object) throws ObjectDescriptionException {
        // this assumes that the order of setting the attributes does not matter.
        for (int i = 0; i < this.orderedPropertyNames.length; i++) {
            try {
                final String name = this.orderedPropertyNames[i];
                final PropertyDescriptor pdescr = getPropertyDescriptor(name);
                if (pdescr == null) {
                    throw new IllegalStateException("No property defined: " + name);
                }
                final Method setter = pdescr.getReadMethod();
                final Object value = setter.invoke(object, new Object[0]);
                if (value == null) {
                    // do nothing if value is not defined ... or null
                    continue;
                }
                setProperty(name, value);
            }
            catch (Exception e) {
                throw new ObjectDescriptionException("Failed to set properties.", e);
            }
        }
    }

    /**
     * Returns the base class.
     * 
     * @return the base class.
     */
    public Class getBaseClass() {
        return this.baseClass;
    }
    
}
