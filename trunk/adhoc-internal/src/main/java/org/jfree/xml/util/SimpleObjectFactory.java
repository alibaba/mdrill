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
 * SimpleObjectFactory.java
 * ------------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: SimpleObjectFactory.java,v 1.3 2005/10/18 13:33:53 mungady Exp $
 *
 * Changes 
 * -------
 * 02-Dec-2003 : Initial version
 *  
 */

package org.jfree.xml.util;

import java.util.HashMap;

/**
 * A simple object factory.
 */
public class SimpleObjectFactory implements ObjectFactory {

    /** The object mappings. */
    private HashMap objectMappings;
    
    /** The manual object mappings. */
    private HashMap manualMappings;
    
    /** The multiplex mappings. */
    private HashMap multiplexMappings;

    /**
     * Creates a new instance.
     */
    public SimpleObjectFactory() {
        this.objectMappings = new HashMap();
        this.manualMappings = new HashMap();
        this.multiplexMappings = new HashMap();
    }

    /**
     * Adds a manual mapping.
     * 
     * @param mapping  the mapping.
     */
    public void addManualMapping(final ManualMappingDefinition mapping) {
        this.manualMappings.put(mapping.getBaseClass(), mapping);
    }

    /**
     * Adds a generic handler.
     * 
     * @param handler  the handler.
     */
    public void addGenericHandler(final GenericObjectFactory handler) {
        this.objectMappings.put(handler.getBaseClass(), handler);
    }

    /**
     * Adds a multiplex mapping.
     * 
     * @param mplex  the multiplex mapping.
     */
    public void addMultiplexMapping(final MultiplexMappingDefinition mplex) {
        this.multiplexMappings.put(mplex.getBaseClass(), mplex);
    }

    /**
     * Clears the mappings.
     */
    public void clear() {
        this.objectMappings.clear();
        this.manualMappings.clear();
        this.multiplexMappings.clear();
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

    /**
     * Checks, whether a generic handler exists for the given class. This does
     * not check, whether an mapping exists.
     *
     * @param c the class for which to check
     * @return true, if an generic handler is defined, false otherwise.
     */
    public boolean isGenericHandler(final Class c) {
        return this.objectMappings.containsKey(c);
    }
}
