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
 * ObjectFactoryLoader.java
 * ------------------------
 * (C) Copyright 2002-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   -;
 *
 * $Id: ObjectFactoryLoader.java,v 1.4 2005/10/18 13:33:53 mungady Exp $
 *
 * Changes
 * -------
 * 24-Sep-2003: Initial version
 *
 */

package org.jfree.xml.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.jfree.util.Log;
import org.jfree.xml.attributehandlers.AttributeHandler;

/**
 * The object factory loader loads the xml specification for the generic
 * handlers. The specification may be distributed over multiple files.
 * <p>
 * This class provides the model management for the reader and writer.
 * The instantiation of the handlers is done elsewhere.
 *
 * @author TM
 */
public class ObjectFactoryLoader extends AbstractModelReader implements ObjectFactory {

    /** Maps classes to GenericObjectFactory instances. */
    private HashMap objectMappings;
    
    /** Manual mappings. */
    private HashMap manualMappings;
    
    /** Multiplex mappings. */
    private HashMap multiplexMappings;

    /** The target class. */
    private Class target;
    
    /** The register name. */
    private String registerName;
    
    /** The property definition. */
    private ArrayList propertyDefinition;
    
    /** The attribute definition. */
    private ArrayList attributeDefinition;
    
    /** The constructor definition. */
    private ArrayList constructorDefinition;
    
    /** The lookup definitions. */
    private ArrayList lookupDefinitions;
    
    /** The ordered names. */
    private ArrayList orderedNames;

    /** The base class. */
    private String baseClass;
    
    /** The attribute name. */
    private String attributeName;
    
    /** The multiplex entries. */
    private ArrayList multiplexEntries;

    /**
     * Creates a new object factory loader for the given base file.
     *
     * @param resourceName the URL of the initial specification file.
     * 
     * @throws ObjectDescriptionException if the file could not be parsed.
     */
    public ObjectFactoryLoader(final URL resourceName) throws ObjectDescriptionException {
        this.objectMappings = new HashMap();
        this.manualMappings = new HashMap();
        this.multiplexMappings = new HashMap();
        parseXml(resourceName);
        rebuildSuperClasses();
    }

    private void rebuildSuperClasses() throws ObjectDescriptionException {
        this.propertyDefinition = new ArrayList();
        this.attributeDefinition = new ArrayList();
        this.constructorDefinition = new ArrayList();
        this.lookupDefinitions = new ArrayList();
        this.orderedNames = new ArrayList();

        final HashMap newObjectDescriptions = new HashMap();
        final Iterator it = this.objectMappings.keySet().iterator();
        while (it.hasNext()) {
            final Object key = it.next();
            final GenericObjectFactory gef = (GenericObjectFactory) this.objectMappings.get(key);
            performSuperClassUpdate(gef);

            final PropertyDefinition[] propertyDefs = (PropertyDefinition[])
            this.propertyDefinition.toArray(new PropertyDefinition[0]);
            final LookupDefinition[] lookupDefs = (LookupDefinition[])
            this.lookupDefinitions.toArray(new LookupDefinition[0]);
            final AttributeDefinition[] attribDefs = (AttributeDefinition[])
            this.attributeDefinition.toArray(new AttributeDefinition[0]);
            final ConstructorDefinition[] constructorDefs = (ConstructorDefinition[])
            this.constructorDefinition.toArray(new ConstructorDefinition[0]);
            final String[] orderedNamesDefs = (String[])
            this.orderedNames.toArray(new String[0]);

            final GenericObjectFactory objectFactory = new GenericObjectFactory
                (gef.getBaseClass(), gef.getRegisterName(), constructorDefs,
                    propertyDefs, lookupDefs, attribDefs, orderedNamesDefs);
            newObjectDescriptions.put(key, objectFactory);

            this.propertyDefinition.clear();
            this.attributeDefinition.clear();
            this.constructorDefinition.clear();
            this.lookupDefinitions.clear();
            this.orderedNames.clear();
        }

        this.objectMappings.clear();
        this.objectMappings = newObjectDescriptions;

        this.propertyDefinition = null;
        this.attributeDefinition = null;
        this.constructorDefinition = null;
        this.lookupDefinitions = null;
        this.orderedNames = null;
    }

    private void performSuperClassUpdate(final GenericObjectFactory gef) {
        // first handle the super classes, ...
        final Class superClass = gef.getBaseClass().getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            final GenericObjectFactory superGef = (GenericObjectFactory) this.objectMappings.get(
                superClass
            );
            if (superGef != null) {
                performSuperClassUpdate(superGef);
            }
        }

        // and finally append all local properties ...
        this.propertyDefinition.addAll(Arrays.asList(gef.getPropertyDefinitions()));
        this.attributeDefinition.addAll(Arrays.asList(gef.getAttributeDefinitions()));
        this.constructorDefinition.addAll(Arrays.asList(gef.getConstructorDefinitions()));
        this.lookupDefinitions.addAll(Arrays.asList(gef.getLookupDefinitions()));
        this.orderedNames.addAll(Arrays.asList(gef.getOrderedPropertyNames()));
    }

    /**
     * Starts a object definition. The object definition collects all properties of
     * an bean-class and defines, which constructor should be used when creating the
     * class.
     *
     * @param className the class name of the defined object
     * @param register the (optional) register name, to lookup and reference the object later.
     * @param ignore  ignore?
     * 
     * @return true, if the definition was accepted, false otherwise.
     * @throws ObjectDescriptionException if an unexpected error occured.
     */
    protected boolean startObjectDefinition(final String className, final String register, final boolean ignore)
        throws ObjectDescriptionException {

        if (ignore) {
            return false;
        }
        this.target = loadClass(className);
        if (this.target == null) {
            Log.warn(new Log.SimpleMessage("Failed to load class ", className));
            return false;
        }
        this.registerName = register;
        this.propertyDefinition = new ArrayList();
        this.attributeDefinition = new ArrayList();
        this.constructorDefinition = new ArrayList();
        this.lookupDefinitions = new ArrayList();
        this.orderedNames = new ArrayList();
        return true;
    }

    /**
     * Handles an attribute definition. This method gets called after the object definition
     * was started. The method will be called for every defined attribute property.
     *
     * @param name the name of the property
     * @param attribName the xml-attribute name to use later.
     * @param handlerClass the attribute handler class.
     * @throws ObjectDescriptionException if an error occured.
     */
    protected void handleAttributeDefinition(final String name, final String attribName, final String handlerClass)
        throws ObjectDescriptionException {
        final AttributeHandler handler = loadAttributeHandler(handlerClass);
        this.orderedNames.add(name);
        this.attributeDefinition.add(new AttributeDefinition(name, attribName, handler));
    }

    /**
     * Handles an element definition. This method gets called after the object definition
     * was started. The method will be called for every defined element property. Element
     * properties are used to describe complex objects.
     *
     * @param name the name of the property
     * @param element the xml-tag name for the child element.
     * @throws ObjectDescriptionException if an error occurs.
     */
    protected void handleElementDefinition(final String name, final String element)
        throws ObjectDescriptionException {
        this.orderedNames.add(name);
        this.propertyDefinition.add(new PropertyDefinition(name, element));
    }

    /**
     * Handles an lookup definition. This method gets called after the object definition
     * was started. The method will be called for every defined lookup property. Lookup properties
     * reference previously created object using the object's registry name.
     *
     * @param name the property name of the base object
     * @param lookupKey the register key of the referenced object
     * @throws ObjectDescriptionException if an error occured.
     */
    protected void handleLookupDefinition(final String name, final String lookupKey)
        throws ObjectDescriptionException {
        final LookupDefinition ldef = new LookupDefinition(name, lookupKey);
        this.orderedNames.add(name);
        this.lookupDefinitions.add(ldef);
    }

    /**
     * Finializes the object definition.
     *
     * @throws ObjectDescriptionException if an error occures.
     */
    protected void endObjectDefinition()
        throws ObjectDescriptionException {

        final PropertyDefinition[] propertyDefs = (PropertyDefinition[])
        this.propertyDefinition.toArray(new PropertyDefinition[0]);
        final LookupDefinition[] lookupDefs = (LookupDefinition[])
        this.lookupDefinitions.toArray(new LookupDefinition[0]);
        final AttributeDefinition[] attribDefs = (AttributeDefinition[])
        this.attributeDefinition.toArray(new AttributeDefinition[0]);
        final ConstructorDefinition[] constructorDefs = (ConstructorDefinition[])
        this.constructorDefinition.toArray(new ConstructorDefinition[0]);
        final String[] orderedNamesDefs = (String[])
        this.orderedNames.toArray(new String[0]);

        final GenericObjectFactory objectFactory = new GenericObjectFactory
            (this.target, this.registerName, constructorDefs,
                propertyDefs, lookupDefs, attribDefs, orderedNamesDefs);
        this.objectMappings.put(this.target, objectFactory);
    }

    /**
     * Handles a constructor definition. Only one constructor can be defined for
     * a certain object type. The constructor will be filled using the given properties.
     *
     * @param propertyName the property name of the referenced local property
     * @param parameterClass the parameter class for the parameter.
     */
    protected void handleConstructorDefinition(final String propertyName, final String parameterClass) {
        final Class c = loadClass(parameterClass);
        this.orderedNames.add(propertyName);
        this.constructorDefinition.add(new ConstructorDefinition(propertyName, c));
    }

    /**
     * Handles a manual mapping definition. The manual mapping maps specific
     * read and write handlers to a given base class. Manual mappings always
     * override any other definition.
     *
     * @param className the base class name
     * @param readHandler the class name of the read handler
     * @param writeHandler the class name of the write handler
     * @return true, if the mapping was accepted, false otherwise.
     * @throws ObjectDescriptionException if an unexpected error occured.
     */
    protected boolean handleManualMapping(final String className, final String readHandler, final String writeHandler)
        throws ObjectDescriptionException {

        if (!this.manualMappings.containsKey(className)) {
            final Class loadedClass = loadClass(className);
            this.manualMappings.put(loadedClass, new ManualMappingDefinition
                (loadedClass, readHandler, writeHandler));
            return true;
        }
        return false;
    }

    /**
     * Starts a multiplex mapping. Multiplex mappings are used to define polymorphic
     * argument handlers. The mapper will collect all derived classes of the given
     * base class and will select the corresponding mapping based on the given type
     * attribute.
     *
     * @param className the base class name
     * @param typeAttr the xml-attribute name containing the mapping key
     */
    protected void startMultiplexMapping(final String className, final String typeAttr) {
        this.baseClass = className;
        this.attributeName = typeAttr;
        this.multiplexEntries = new ArrayList();
    }

    /**
     * Defines an entry for the multiplex mapping. The new entry will be activated
     * when the base mappers type attribute contains this <code>typename</code> and
     * will resolve to the handler for the given classname.
     *
     * @param typeName the type value for this mapping.
     * @param className the class name to which this mapping resolves.
     * @throws ObjectDescriptionException if an error occurs.
     */
    protected void handleMultiplexMapping(final String typeName, final String className)
        throws ObjectDescriptionException {
        this.multiplexEntries.add
            (new MultiplexMappingEntry(typeName, className));
    }

    /**
     * Finializes the multiplexer mapping.
     *
     * @throws ObjectDescriptionException if an error occurs.
     */
    protected void endMultiplexMapping() throws ObjectDescriptionException {
        final MultiplexMappingEntry[] mappings = (MultiplexMappingEntry[])
        this.multiplexEntries.toArray(new MultiplexMappingEntry[0]);
        final Class c = loadClass(this.baseClass);
        this.multiplexMappings.put(c,
            new MultiplexMappingDefinition(c, this.attributeName, mappings));
        this.multiplexEntries = null;
    }

    /**
     * Loads an instantiates the attribute handler specified by the given
     * class name.
     *
     * @param attribute the attribute handlers classname.
     * @return the created attribute handler instance
     * @throws ObjectDescriptionException if the handler could not be loaded.
     */
    private AttributeHandler loadAttributeHandler(final String attribute)
        throws ObjectDescriptionException {

        final Class c = loadClass(attribute);
        try {
            return (AttributeHandler) c.newInstance();
        }
        catch (Exception e) {
            throw new ObjectDescriptionException
                ("Invalid attribute handler specified: " + attribute);
        }
    }

    /**
     * Checks, whether the factory has a description for the given class.
     *
     * @param c the class to be handled by the factory.
     * @return true, if an description exists for the given class, false otherwise.
     */
    public boolean isGenericHandler(final Class c) {
        return this.objectMappings.containsKey(c);
    }

    /**
     * Returns a factory instance for the given class. The factory is independent
     * from all previously generated instances.
     *
     * @param c the class
     * @return the object factory.
     */
    public GenericObjectFactory getFactoryForClass(final Class c) {
        final GenericObjectFactory factory = (GenericObjectFactory) this.objectMappings.get(c);
        if (factory == null) {
            return null;
        }
        return factory.getInstance();
    }

    /**
     * Returns the manual mapping definition for the given class, or null, if
     * not manual definition exists.
     *
     * @param c the class for which to check the existence of the definition
     * @return the manual mapping definition or null.
     */
    public ManualMappingDefinition getManualMappingDefinition(final Class c) {
        return (ManualMappingDefinition) this.manualMappings.get(c);
    }

    /**
     * Returns the multiplex definition for the given class, or null, if no
     * such definition exists.
     *
     * @param c the class for which to check the existence of the multiplexer
     * @return the multiplexer for the class, or null if no multiplexer exists.
     */
    public MultiplexMappingDefinition getMultiplexDefinition(final Class c) {
        final MultiplexMappingDefinition definition = (MultiplexMappingDefinition)
        this.multiplexMappings.get(c);
        return definition;
    }

}
