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
 * --------------------------
 * MultiplexMappingEntry.java
 * --------------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: MultiplexMappingEntry.java,v 1.3 2005/10/18 13:33:53 mungady Exp $
 *
 * Changes 
 * -------
 * 22-Nov-2003 : Initial version
 *  
 */

package org.jfree.xml.util;

/**
 * A multiplex mapping entry.
 */
public class MultiplexMappingEntry {
    
    /** The attribute value. */
    private String attributeValue;
    
    /** The target class. */
    private String targetClass;

    /**
     * Creates a new instance.
     * 
     * @param attributeValue  the attribute value.
     * @param targetClass  the target class.
     */
    public MultiplexMappingEntry(final String attributeValue, final String targetClass) {
        this.attributeValue = attributeValue;
        this.targetClass = targetClass;
    }

    /**
     * Returns the attribute value.
     * 
     * @return The attribute value.
     */
    public String getAttributeValue() {
        return this.attributeValue;
    }

    /**
     * Returns the target class.
     * 
     * @return The target class.
     */
    public String getTargetClass() {
        return this.targetClass;
    }
    
}
