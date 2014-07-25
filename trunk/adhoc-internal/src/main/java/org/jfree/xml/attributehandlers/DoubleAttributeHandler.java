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
 * DoubleAttributeHandler.java
 * ---------------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: DoubleAttributeHandler.java,v 1.2 2005/10/18 13:30:16 mungady Exp $
 *
 * Changes 
 * -------
 * 24.09.2003 : Initial version
 *  
 */

package org.jfree.xml.attributehandlers;

/**
 * A class that handles the conversion of {@link Double} attributes to and from an appropriate
 * {@link String} representation.
 */
public class DoubleAttributeHandler implements AttributeHandler {

    /**
     * Creates a new attribute handler.
     */
    public DoubleAttributeHandler() {
        super();
    }

    /**
     * Converts the attribute to a string.
     * 
     * @param o  the attribute ({@link Double} expected).
     * 
     * @return A string representing the {@link Double} value.
     */
    public String toAttributeValue(final Object o) {
        final Double in = (Double) o;
        return in.toString();
    }

    /**
     * Converts a string to a {@link Double}.
     * 
     * @param s  the string.
     * 
     * @return a {@link Double}.
     */
    public Object toPropertyValue(final String s) {
        return new Double(s);
    }
    
}
