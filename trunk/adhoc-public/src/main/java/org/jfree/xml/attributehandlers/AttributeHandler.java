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
 * AttributeHandler.java
 * ---------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: AttributeHandler.java,v 1.2 2005/10/18 13:30:16 mungady Exp $
 *
 * Changes 
 * -------
 * 23.09.2003 : Initial version
 *  
 */

package org.jfree.xml.attributehandlers;

/**
 * An attribute handler is an object that can transform an object into a string or vice
 * versa.
 */
public interface AttributeHandler {
    
    /**
     * Converts an object to an attribute value.
     * 
     * @param o  the object.
     * 
     * @return the attribute value.
     */
    public String toAttributeValue (Object o);
    
    /**
     * Converts a string to a property value.
     * 
     * @param s  the string.
     * 
     * @return a property value.
     */    
    public Object toPropertyValue (String s);

}
