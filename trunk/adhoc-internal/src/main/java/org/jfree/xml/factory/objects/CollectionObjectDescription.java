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
 * ----------------------
 * ObjectDescription.java
 * ----------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: CollectionObjectDescription.java,v 1.2 2005/10/18 13:31:58 mungady Exp $
 *
 * Changes
 * -------------------------
 * 06-May-2003 : Initial version
 */
package org.jfree.xml.factory.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jfree.util.Log;

/**
 * An object description for simple collection objects, like java.util.List
 * or java.util.Set.
 *
 * @author Thomas Morgner
 */
public class CollectionObjectDescription extends AbstractObjectDescription {

    /**
     * Creates a list object description for the given collection class.
     * <P>
     * Throws <code>ClassCastException</code> if the given class is no collection instance.
     * 
     * @param c the class of the collection implementation.
     */
    public CollectionObjectDescription(final Class c) {
        super(c);
        if (!Collection.class.isAssignableFrom(c)) {
            throw new ClassCastException("The given class is no Collection instance");
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
        return Object.class;
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
     * Creates an object based on the description.
     *
     * @return The object.
     */
    public Object createObject() {
        try {
            final Collection l = (Collection) getObjectClass().newInstance();
            int counter = 0;
            while (getParameterDefinition(String.valueOf(counter)) != null) {
                final Object value = getParameter(String.valueOf(counter));
                if (value == null) {
                    break;
                }

                l.add(value);
                counter += 1;
            }
            return l;
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
     * @throws ObjectFactoryException if there is a problem while reading the
     * properties of the given object.
     */
    public void setParameterFromObject(final Object o) throws ObjectFactoryException {
        if (o == null) {
            throw new NullPointerException("Given object is null");
        }
        final Class c = getObjectClass();
        if (!c.isInstance(o)) {
            throw new ObjectFactoryException("Object is no instance of " + c + "(is "
                + o.getClass() + ")");
        }

        final Collection l = (Collection) o;
        final Iterator it = l.iterator();
        int counter = 0;
        while (it.hasNext()) {
            final Object ob = it.next();
            setParameter(String.valueOf(counter), ob);
            counter++;
        }
    }
}
