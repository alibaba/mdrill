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
 * ---------------------------
 * ArrayObjectDescription.java
 * ---------------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner (taquera@sherito.org);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ArrayObjectDescription.java,v 1.4 2006/01/27 18:53:15 taqua Exp $
 *
 * Changes
 * -------
 * 14-Apr-2003 : Initial version
 * 29-Apr-2003 : Distilled from the JFreeReport project and moved into JCommon
 */
package org.jfree.xml.factory.objects;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

import org.jfree.util.Log;

/**
 * Describes an Object- or primitive value array. This object description is
 * not intended to be created outside the ArrayClassFactory.
 *
 * @author Thomas Morgner
 */
public class ArrayObjectDescription extends AbstractObjectDescription {

    /**
     * Constructs a new array objet description for the given array class.
     * <P>
     * Note:  throws <code>IllegalArgumentException</code> if the given class is no array.
     * 
     * @param c the array class object.
     */
    public ArrayObjectDescription(final Class c) {
        super(c);
        if (!c.isArray()) {
            throw new IllegalArgumentException("Need an array class");
        }
    }

    /**
     * Creates an object based on the description.
     *
     * @return The object.
     */
    public Object createObject() {
        try {
            final Integer size = (Integer) getParameter("size");
            if (size == null) {
                final ArrayList l = new ArrayList();
                int counter = 0;
                while (getParameterDefinition(String.valueOf(counter)) != null) {
                    final Object value = getParameter(String.valueOf(counter));
                    if (value == null) {
                        break;
                    }

                    l.add(value);
                    counter += 1;
                }

                final Object o = Array.newInstance
                    (getObjectClass().getComponentType(), l.size());
                for (int i = 0; i < l.size(); i++) {
                    Array.set(o, i, l.get(i));
                }
                return o;
            }
            else {
                // a size is given, so we can assume that all values are defined.
                final Object o = Array.newInstance
                    (getObjectClass().getComponentType(), size.intValue());
                for (int i = 0; i < size.intValue(); i++) {
                    Array.set(o, i, getParameter(String.valueOf(i)));
                }
                return o;
            }
        }
        catch (Exception ie) {
            Log.warn("Unable to instantiate Object", ie);
            return null;
        }
    }

    /**
     * Sets the parameters of this description object to match the supplied object.
     *
     * @param o  the object.
     *
     * @throws ObjectFactoryException if there is a
     * problem while reading the properties of the given object.
     */
    public void setParameterFromObject(final Object o) throws ObjectFactoryException {
        if (o == null) {
            throw new ObjectFactoryException("Given object is null.");
        }

        if (!o.getClass().isArray()) {
            throw new ObjectFactoryException("Given object is no array");
        }

        if (!getObjectClass().isAssignableFrom(o.getClass())) {
            throw new ObjectFactoryException("Given object is incompatible with base class");
        }

        final int size = Array.getLength(o);
        setParameter("size", new Integer(size));
        for (int i = 0; i < size; i++) {
            setParameter(String.valueOf(i), Array.get(o, i));
        }
    }

    /**
     * Tries to parse the given parameter string into a positive integer.
     * Returns -1 if the parsing failed for some reason.
     *
     * @param name the name of the parameter.
     * @return the parsed int value or -1 on errors.
     */
    private int parseParameterName(final String name) {
        try {
            return Integer.parseInt(name);
        }
        catch (Exception e) {
            return -1;
        }
    }

    /**
     * Returns a parameter definition. If the parameter is invalid, this
     * function returns null.
     *
     * @param name  the definition name.
     *
     * @return The parameter class or null, if the parameter is not defined.
     */
    public Class getParameterDefinition(final String name) {
        if (name.equals("size")) {
            return Integer.TYPE;
        }
        final int par = parseParameterName(name);
        if (par < 0) {
            return null;
        }
        return getObjectClass().getComponentType();
    }

    /**
     * Returns an iterator for the parameter names.
     *
     * @return The iterator.
     */
    public Iterator getParameterNames() {
        final Integer size = (Integer) getParameter("size");
        if (size == null) {
            return getDefinedParameterNames();
        }
        else {
            final ArrayList l = new ArrayList();
            l.add("size");
            for (int i = 0; i < size.intValue(); i++) {
                l.add(String.valueOf(i));
            }
            return l.iterator();
        }
    }

    /**
     * Returns a new instance of the object description.
     *
     * @return The object description.
     */
    public ObjectDescription getInstance() {
        return new ArrayObjectDescription(getObjectClass());
    }
}
