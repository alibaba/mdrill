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
 * ----------------------------
 * ManualMappingDefinition.java
 * ----------------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ManualMappingDefinition.java,v 1.3 2005/10/18 13:33:53 mungady Exp $
 *
 * Changes 
 * -------
 * 22-Nov-2003 : Initial version
 *  
 */

package org.jfree.xml.util;

/**
 * Maps a class to a read handler and a write handler.
 */
public class ManualMappingDefinition {
    
    /** The class. */
    private Class baseClass;

    /** The read handler. */
    private String readHandler;
    
    /** The write handler. */
    private String writeHandler;
    
    /**
     * Creates a mapping between the class and the read and write handlers.
     * 
     * @param baseClass  the class (<code>null</code> not permitted).
     * @param readHandler  the name of the read handler.
     * @param writeHandler  the name of the write handler.
     */
    public ManualMappingDefinition(final Class baseClass, final String readHandler, final String writeHandler) {
        if (baseClass == null) {
            throw new NullPointerException("BaseClass must not be null");
        }
        if (readHandler == null && writeHandler == null) {
            throw new NullPointerException
                ("At least one of readHandler or writeHandler must be defined.");
        }
        this.baseClass = baseClass;
        this.readHandler = readHandler;
        this.writeHandler = writeHandler;
    }

    /**
     * Returns the class.
     * 
     * @return The class.
     */
    public Class getBaseClass() {
        return this.baseClass;
    }

    /**
     * Returns the name of the read handler.
     * 
     * @return The name of the read handler.
     */
    public String getReadHandler() {
        return this.readHandler;
    }

    /**
     * Returns the name of the write handler.
     * 
     * @return The name of the write handler.
     */
    public String getWriteHandler() {
        return this.writeHandler;
    }
    
}
