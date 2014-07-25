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
 * ------------------------------
 * AbstractObjectDescription.java
 * ------------------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: AbstractObjectDescription.java,v 1.3 2005/11/14 10:58:37 mungady Exp $
 *
 * Changes (from 19-Feb-2003)
 * -------------------------
 * 19-Feb-2003 : Added standard header and Javadocs (DG);
 * 29-Apr-2003 : Distilled from the JFreeReport project and moved into JCommon
 *
 */

package org.jfree.xml.factory.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.jfree.util.Configuration;
import org.jfree.util.Log;
import org.jfree.util.ReadOnlyIterator;

/**
 * An abstract base class for object descriptions.
 *
 * @author Thomas Morgner.
 */
public abstract class AbstractObjectDescription implements ObjectDescription, Cloneable {

    /** The class. */
    private Class className;

    /** Storage for parameters. */
    private HashMap parameters;

    /** Storage for parameter definitions. */
    private HashMap parameterDefs;

    /** The configuration for the object description. */
    private Configuration config;

    /**
     * Creates a new object description.
     *
     * @param className  the class.
     */
    public AbstractObjectDescription(final Class className) {
        this.className = className;
        this.parameters = new HashMap();
        this.parameterDefs = new HashMap();
    }

    /**
     * Returns a parameter class.
     *
     * @param name  the parameter definition.
     *
     * @return The class.
     */
    public Class getParameterDefinition(final String name) {
        return (Class) this.parameterDefs.get(name);
    }

    /**
     * Sets the class for a parameter.
     *
     * @param name  the parameter name.
     * @param obj  the parameter class.
     */
    public void setParameterDefinition(final String name, final Class obj) {
        if (obj == null) {
            this.parameterDefs.remove(name);
        }
        else {
            this.parameterDefs.put(name, obj);
        }
    }

    /**
     * Converts primitives to corresponding object class.
     *
     * @param obj  the class.
     *
     * @return The class.
     */
    public static Class convertPrimitiveClass(final Class obj) {
        if (!obj.isPrimitive()) {
            return obj;
        }
        if (obj == Boolean.TYPE) {
            return Boolean.class;
        }
        if (obj == Byte.TYPE) {
            return Byte.class;
        }
        if (obj == Character.TYPE) {
            return Character.class;
        }
        if (obj == Short.TYPE) {
            return Short.class;
        }
        if (obj == Integer.TYPE) {
            return Integer.class;
        }
        if (obj == Long.TYPE) {
            return Long.class;
        }
        if (obj == Float.TYPE) {
            return Float.class;
        }
        if (obj == Double.TYPE) {
            return Double.class;
        }
        throw new IllegalArgumentException("Class 'void' is not allowed here");
    }

    /**
     * Sets a parameter.
     *
     * @param name  the name.
     * @param value  the value.
     */
    public void setParameter(final String name, final Object value) {
        if (getParameterDefinition(name) == null) {
            throw new IllegalArgumentException("No such Parameter defined: " + name
                + " in class " + getObjectClass());
        }
        final Class parameterClass = convertPrimitiveClass(getParameterDefinition(name));
        if (!parameterClass.isAssignableFrom(value.getClass())) {
            throw new ClassCastException("In Object " + getObjectClass()
                + ": Value is not assignable: " + value.getClass()
                + " is not assignable from " + parameterClass);
        }
        this.parameters.put(name, value);
    }

    /**
     * Returns an iterator for the parameter names.
     *
     * @return The iterator.
     */
    public synchronized Iterator getParameterNames() {
        final ArrayList parameterNames = new ArrayList(this.parameterDefs.keySet());
        Collections.sort(parameterNames);
        return new ReadOnlyIterator (parameterNames.iterator());
    }

    /**
     * Returns an iterator for the parameter names.
     *
     * @return The iterator.
     */
    protected Iterator getDefinedParameterNames() {
        return new ReadOnlyIterator (this.parameters.keySet().iterator());
    }

    /**
     * Returns a parameter value.
     *
     * @param name  the parameter name.
     *
     * @return The parameter value.
     */
    public Object getParameter(final String name) {
        return this.parameters.get(name);
    }

    /**
     * Returns the class for the object.
     *
     * @return The class.
     */
    public Class getObjectClass() {
        return this.className;
    }

    /**
     * Returns a cloned instance of the object description. The contents
     * of the parameter objects collection are cloned too, so that any
     * already defined parameter value is copied to the new instance.
     * <p>
     * Parameter definitions are not cloned, as they are considered read-only.
     * <p>
     * The newly instantiated object description is not configured. If it
     * need to be configured, then you have to call configure on it.
     *
     * @return A cloned instance.
     */
    public ObjectDescription getInstance() {
        try {
            final AbstractObjectDescription c = (AbstractObjectDescription) super.clone();
            c.parameters = (HashMap) this.parameters.clone();
            return c;
        }
        catch (Exception e) {
            Log.error("Should not happen: Clone Error: ", e);
            return null;
        }
    }


    /**
     * Returns a cloned instance of the object description. The contents
     * of the parameter objects collection are cloned too, so that any
     * already defined parameter value is copied to the new instance.
     * <p>
     * Parameter definitions are not cloned, as they are considered read-only.
     * <p>
     * The newly instantiated object description is not configured. If it
     * need to be configured, then you have to call configure on it.
     *
     * @return A cloned instance.
     */
    public ObjectDescription getUnconfiguredInstance() {
        try {
            final AbstractObjectDescription c = (AbstractObjectDescription) super.clone();
            c.parameters = (HashMap) this.parameters.clone();
            c.config = null;
            return c;
        }
        catch (Exception e) {
            Log.error("Should not happen: Clone Error: ", e);
            return null;
        }
    }

    /**
     * Configures this factory. The configuration contains several keys and
     * their defined values. The given reference to the configuration object
     * will remain valid until the report parsing or writing ends.
     * <p>
     * The configuration contents may change during the reporting.
     *
     * @param config the configuration, never null
     */
    public void configure(final Configuration config) {
        if (config == null) {
            throw new NullPointerException("The given configuration is null");
        }
        this.config = config;
    }

    /**
     * Returns the configuration for that object description.
     *
     * @return the configuration or null, if not yet set.
     */
    public Configuration getConfig() {
        return this.config;
    }

    /**
     * Tests for equality.
     * 
     * @param o  the object to test.
     * 
     * @return A boolean.
     */
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractObjectDescription)) {
            return false;
        }

        final AbstractObjectDescription abstractObjectDescription = (AbstractObjectDescription) o;

        if (!this.className.equals(abstractObjectDescription.className)) {
            return false;
        }

        return true;
    }

    /**
     * Returns a hash code for the object.
     * 
     * @return The hash code.
     */
    public int hashCode() {
        return this.className.hashCode();
    }
}
