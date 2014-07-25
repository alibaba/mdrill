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
 * ModelBuilder.java
 * -----------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ModelBuilder.java,v 1.3 2005/10/18 13:32:20 mungady Exp $
 *
 * Changes
 * -------
 * 21-Jun-2003 : Initial version (TM);
 * 26-Nov-2003 : Updated header and Javadocs (DG);
 * 
 */

package org.jfree.xml.generator;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import org.jfree.util.HashNMap;
import org.jfree.xml.generator.model.ClassDescription;
import org.jfree.xml.generator.model.DescriptionModel;
import org.jfree.xml.generator.model.MultiplexMappingInfo;
import org.jfree.xml.generator.model.PropertyInfo;
import org.jfree.xml.generator.model.PropertyType;
import org.jfree.xml.generator.model.TypeInfo;
import org.jfree.xml.util.BasicTypeSupport;

/**
 * A model builder.  This class performs the work of creating a class description model from
 * a set of source files.
 */
public final class ModelBuilder {

    /** The single instance. */
    private static ModelBuilder instance;

    /**
     * Returns the single instance of this class.
     * 
     * @return the single instance of this class.
     */
    public static ModelBuilder getInstance() {
        if (instance == null) {
            instance = new ModelBuilder();
        }
        return instance;
    }

    /** The handler mapping. */
    private Properties handlerMapping;

    /**
     * Creates a single instance.
     */
    private ModelBuilder() {
        this.handlerMapping = new Properties();
    }

    /**
     * Adds attribute handlers.
     * 
     * @param p  the handlers.
     */
    public void addAttributeHandlers(final Properties p) {
        this.handlerMapping.putAll(p);
    }

    /**
     * Builds a model from the classes provided by the {@link SourceCollector}. 
     * <P>
     * The {@link DescriptionGenerator} class invokes this.
     * 
     * @param c  the source collector.
     * @param model  the model under construction (<code>null</code> permitted).
     * 
     * @return The completed model.
     */
    public DescriptionModel buildModel(final SourceCollector c, DescriptionModel model) {
        
        Class[] classes = c.getClasses();

        if (model == null) {
            model = new DescriptionModel();
        }

        while (classes.length != 0) {
            classes = fillModel(classes, model);
        }

        fillSuperClasses(model);
        // search for multiplexer classes

        // first search all classes used in parameters and add them to
        // our list of possible base classes
        final Class[] baseClasses = findElementTypes(model);

        final HashNMap classMap = new HashNMap();
        for (int i = 0; i < baseClasses.length; i++) {

            final Class base = baseClasses[i];

            for (int j = 0; j < baseClasses.length; j++) {

                final Class child = baseClasses[j];
                if (Modifier.isAbstract(child.getModifiers())) {
                    continue;
                }
                if (base.isAssignableFrom(child)) {
                    classMap.add(base, child);
                }
            }
        }

        // at this point, the keys of 'classMap' represent all required
        // multiplexers, while the values assigned to these keys define the
        // possible childs
        final Iterator keys = classMap.keys();
        while (keys.hasNext()) {
            final Class base = (Class) keys.next();
            final Class[] childs = (Class[]) classMap.toArray(base, new Class[0]);
            if (childs.length < 2) {
                continue;
            }

            boolean isNew = false;
            MultiplexMappingInfo mmi = model.getMappingModel().lookupMultiplexMapping(base);
            final ArrayList typeInfoList;
            if (mmi == null) {
                mmi = new MultiplexMappingInfo(base);
                typeInfoList = new ArrayList();
                isNew = true;
            }
            else {
                typeInfoList = new ArrayList(Arrays.asList(mmi.getChildClasses()));
            }

            for (int i = 0; i < childs.length; i++) {
                // the generic information is only added, if no other information
                // is already present ...
                final TypeInfo typeInfo = new TypeInfo(childs[i].getName(), childs[i]);
                if (!typeInfoList.contains(typeInfo)) {
                    typeInfoList.add(typeInfo);
                }
            }

            mmi.setChildClasses((TypeInfo[]) typeInfoList.toArray(new TypeInfo[0]));
            if (isNew) {
                model.getMappingModel().addMultiplexMapping(mmi);
            }
        }

        // when resolving a class to an handler, the resolver first has to
        // search for an multiplexer before searching for handlers. Otherwise
        // non-abstract baseclasses will be found before the multiplexer can
        // resolve the situation.
        return model;
    }

    private Class[] findElementTypes(final DescriptionModel model) {
        final ArrayList baseClasses = new ArrayList();

        for (int i = 0; i < model.size(); i++) {
            final ClassDescription cd = model.get(i);
            if (!baseClasses.contains(cd.getObjectClass())) {
                baseClasses.add(cd.getObjectClass());
            }

            final PropertyInfo[] properties = cd.getProperties();
            for (int p = 0; p < properties.length; p++) {
                // filter primitive types ... they cannot form a generalization
                // relation
                if (!properties[p].getPropertyType().equals(PropertyType.ELEMENT)) {
                    continue;
                }
                final Class type = properties[p].getType();
                if (baseClasses.contains(type)) {
                    continue;
                }
                // filter final classes, they too cannot have derived classes
                if (Modifier.isFinal(type.getModifiers())) {
                    continue;
                }
                baseClasses.add(type);
            }
        }
        return (Class[]) baseClasses.toArray(new Class[baseClasses.size()]);
    }

    /**
     * Fills the super class for all object descriptions of the model. The
     * super class is only filled, if the object's super class is contained
     * in the model.
     *
     * @param model the model which should get its superclasses updated.
     */
    private void fillSuperClasses(final DescriptionModel model) {
        // Fill superclasses
        for (int i = 0; i < model.size(); i++) {
            final ClassDescription cd = model.get(i);
            final Class parent = cd.getObjectClass().getSuperclass();
            if (parent == null) {
                continue;
            }
            final ClassDescription superCD = model.get(parent);
            if (superCD != null) {
                cd.setSuperClass(superCD.getObjectClass());
            }
        }
    }

    /**
     * Updates the model to contain the given classes.
     *
     * @param classes  a list of classes which should be part of the model.
     * @param model  the model which is updated
     * 
     * @return A list of super classes which should also be contained in the model.
     */
    private Class[] fillModel(final Class[] classes, final DescriptionModel model) {
        // first check all direct matches from the source collector.
        // but make sure that we also detect external superclasses -
        // we have to get all properties ...
        final ArrayList superClasses = new ArrayList();
        for (int i = 0; i < classes.length; i++) {

            Class superClass = classes[i].getSuperclass();
            if (superClass != null) {
                if (!Object.class.equals(superClass) 
                    && !contains(classes, superClass) 
                    && !superClasses.contains(superClass)) {
                    superClasses.add(superClass);
                }
            }
            else {
                superClass = Object.class;
            }

            try {
                final BeanInfo bi = Introspector.getBeanInfo(classes[i], superClass);
                final ClassDescription parent = model.get(classes[i]);
                final ClassDescription cd = createClassDescription(bi, parent);
                if (cd != null) {
                    model.addClassDescription(cd);
                }
            }
            catch (IntrospectionException ie) {
                // swallowed....
            }
        }
        return (Class[]) superClasses.toArray(new Class[0]);
    }

    /**
     * Creates a {@link ClassDescription} object for the specified bean info.
     * 
     * @param beanInfo  the bean info.
     * @param parent  the parent class description.
     * 
     * @return The class description.
     */
    private ClassDescription createClassDescription (final BeanInfo beanInfo, final ClassDescription parent) {
        final PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
        final ArrayList properties = new ArrayList();
        for (int i = 0; i < props.length; i++) {
            final PropertyDescriptor propertyDescriptor = props[i];
            PropertyInfo pi;
            if (parent != null) {
                pi = parent.getProperty(propertyDescriptor.getName());
                if (pi != null) {
                    // Property already found, don't touch it
//                    Log.info (new Log.SimpleMessage
//                        ("Ignore predefined property: ", propertyDescriptor.getName()));
                    properties.add(pi);
                    continue;
                }
            }

            if (props[i] instanceof IndexedPropertyDescriptor) {
                // this would handle lists and array access. We don't support
                // this in the direct approach. We will need some cheating:
                // <Chart>
                //    <Subtitle-list>
                //         <title1 ..>
                //         <title2 ..>
                // pi = createIndexedPropertyInfo((IndexedPropertyDescriptor) props[i]);
            }
            else {
                pi = createSimplePropertyInfo(props[i]);
                if (pi != null) {
                    properties.add(pi);
                }
            }
        }

        final PropertyInfo[] propArray = (PropertyInfo[])
            properties.toArray(new PropertyInfo[properties.size()]);

        final ClassDescription cd;
        if (parent != null) {
            cd = parent;
        }
        else {
            cd = new ClassDescription(beanInfo.getBeanDescriptor().getBeanClass());
            cd.setDescription(beanInfo.getBeanDescriptor().getShortDescription());
        }

        cd.setProperties(propArray);
        return cd;
    }

    /**
     * Checks, whether the given method can be called from the generic object factory.
     *
     * @param method the method descriptor
     * @return true, if the method is not null and public, false otherwise.
     */
    public static boolean isValidMethod(final Method method) {
        if (method == null) {
            return false;
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        return true;
    }

    /**
     * Creates a {@link PropertyInfo} object from a {@link PropertyDescriptor}.
     * 
     * @param pd  the property descriptor.
     * 
     * @return the property info (<code>null</code> possible).
     */
    public PropertyInfo createSimplePropertyInfo(final PropertyDescriptor pd) {

        final boolean readMethod = isValidMethod(pd.getReadMethod());
        final boolean writeMethod = isValidMethod(pd.getWriteMethod());
        if (!writeMethod || !readMethod) {
            // a property is useless for our purposes without having a read or write method.
            return null;
        }

        final PropertyInfo pi = new PropertyInfo(pd.getName(), pd.getPropertyType());
        pi.setConstrained(pd.isConstrained());
        pi.setDescription(pd.getShortDescription());
        pi.setNullable(true);
        pi.setPreserve(false);
        pi.setReadMethodAvailable(readMethod);
        pi.setWriteMethodAvailable(writeMethod);
        pi.setXmlName(pd.getName());
        if (isAttributeProperty(pd.getPropertyType())) {
            pi.setPropertyType(PropertyType.ATTRIBUTE);
            pi.setXmlHandler(getHandlerClass(pd.getPropertyType()));
        }
        else {
            pi.setPropertyType(PropertyType.ELEMENT);
        }
        return pi;
    }

    /**
     * Checks, whether the given class can be handled as attribute.
     * All primitive types can be attributes as well as all types which have
     * a custom attribute handler defined.
     *
     * @param c the class which should be checked
     * @return true, if the class can be handled as attribute, false otherwise.
     */
    private boolean isAttributeProperty(final Class c) {
        if (BasicTypeSupport.isBasicDataType(c)) {
            return true;
        }
        return this.handlerMapping.containsKey(c.getName());
    }

    /**
     * Returns the class name for the attribute handler for a property of the specified class.
     *
     * @param c the class for which to search an attribute handler
     * @return the handler class or null, if this class cannot be handled
     * as attribute.
     */
    private String getHandlerClass(final Class c) {
        if (BasicTypeSupport.isBasicDataType(c)) {
            final String handler = BasicTypeSupport.getHandlerClass(c);
            if (handler != null) {
                return handler;
            }
        }
        return this.handlerMapping.getProperty(c.getName());
    }

    /**
     * Checks, whether the class <code>c</code> is contained in the given
     * class array.
     *
     * @param cAll the list of all classes
     * @param c the class to be searched
     * @return true, if the class is contained in the array, false otherwise.
     */
    private boolean contains(final Class[] cAll, final Class c) {
        for (int i = 0; i < cAll.length; i++) {
            if (cAll[i].equals(c)) {
                return true;
            }
        }
        return false;
    }


//  private PropertyInfo createIndexedPropertyInfo(IndexedPropertyDescriptor prop)
//  {
//
//    MethodInfo readMethod = createMethodInfo(prop.getIndexedReadMethod());
//    MethodInfo writeMethod = createMethodInfo(prop.getIndexedWriteMethod());
//    if (writeMethod == null)
//    {
//      return null;
//    }
//    IndexedPropertyInfo pi = new IndexedPropertyInfo(prop.getName());
//    pi.setConstrained(prop.isConstrained());
//    pi.setDescription(prop.getShortDescription());
//    pi.setNullable(true);
//    pi.setPreserve(false);
//    pi.setType(prop.getIndexedPropertyType());
//    pi.setReadMethod(readMethod);
//    pi.setWriteMethod(writeMethod);
//
//    TypeInfo keyInfo = new TypeInfo("index");
//    keyInfo.setType(Integer.TYPE);
//    keyInfo.setNullable(false);
//    keyInfo.setConstrained(true); // throws indexoutofboundsexception
//    keyInfo.setDescription("Generic index value");
//    KeyDescription kd = new KeyDescription(new TypeInfo[]{keyInfo});
//    pi.setKey(kd);
//    return pi;
//  }
}
