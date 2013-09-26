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
 * BasicTypeSupport.java
 * ---------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: BasicTypeSupport.java,v 1.4 2005/10/18 13:33:53 mungady Exp $
 *
 * Changes 
 * -------
 * 12-Nov-2003 : Initial version (TM);
 * 26-Nov-2003 : Updated header and Javadocs (DG);
 *  
 */

package org.jfree.xml.util;

import org.jfree.xml.attributehandlers.BooleanAttributeHandler;
import org.jfree.xml.attributehandlers.ByteAttributeHandler;
import org.jfree.xml.attributehandlers.DoubleAttributeHandler;
import org.jfree.xml.attributehandlers.FloatAttributeHandler;
import org.jfree.xml.attributehandlers.IntegerAttributeHandler;
import org.jfree.xml.attributehandlers.LongAttributeHandler;
import org.jfree.xml.attributehandlers.ShortAttributeHandler;
import org.jfree.xml.attributehandlers.StringAttributeHandler;

/**
 * A class that contains information about some basic types.
 */
public class BasicTypeSupport {

  private BasicTypeSupport ()
  {
  }

    /**
     * Returns the fully qualified class name for the attribute handler for a property of 
     * the specified class.
     * 
     * @param c  the property class.
     * 
     * @return the attribute handler class name.
     */
    public static String getHandlerClass(final Class c) {
        if (c.equals(Integer.class) || c.equals(Integer.TYPE)) {
            return IntegerAttributeHandler.class.getName();
        }
        if (c.equals(Short.class) || c.equals(Short.TYPE)) {
            return ShortAttributeHandler.class.getName();
        }
        if (c.equals(Long.class) || c.equals(Long.TYPE)) {
            return LongAttributeHandler.class.getName();
        }
        if (c.equals(Boolean.class) || c.equals(Boolean.TYPE)) {
            return BooleanAttributeHandler.class.getName();
        }
        if (c.equals(Float.class) || c.equals(Float.TYPE)) {
            return FloatAttributeHandler.class.getName();
        }
        if (c.equals(Double.class) || c.equals(Double.TYPE)) {
            return DoubleAttributeHandler.class.getName();
        }
        if (c.equals(Byte.class) || c.equals(Byte.TYPE)) {
            return ByteAttributeHandler.class.getName();
        }
        // string can also be handled directly ...
        if (c.equals(String.class)) {
            return StringAttributeHandler.class.getName();
        }
        throw new IllegalArgumentException("BasicTypeSupport.getHandlerClass(Class): "
            + "this is no attribute type.");
    }

    /**
     * Returns <code>true</code> if the specified class is a "basic" type, and <code>false</code>
     * otherwise.  Basic types are written as attributes (rather than elements) in XML output.
     * 
     * @param c  the class.
     * 
     * @return a boolean.
     */
    public static boolean isBasicDataType (final Class c) {
        if (c.equals(Integer.class) || c.equals(Integer.TYPE)) {
            return true;
        }
        if (c.equals(Short.class) || c.equals(Short.TYPE)) {
            return true;
        }
        if (c.equals(Long.class) || c.equals(Long.TYPE)) {
            return true;
        }
        if (c.equals(Boolean.class) || c.equals(Boolean.TYPE)) {
            return true;
        }
        if (c.equals(Float.class) || c.equals(Float.TYPE)) {
            return true;
        }
        if (c.equals(Double.class) || c.equals(Double.TYPE)) {
            return true;
        }
        if (c.equals(Byte.class) || c.equals(Byte.TYPE)) {
            return true;
        }
        // string can also be handled directly ...
        if (c.equals(String.class)) {
            return true;
        }
        return false;
    }

    /**
     * Returns the class for a given primitive class type.
     * 
     * @param className  the primitive class name.
     * 
     * @return a class.
     */
    public static Class getClassRepresentation(final String className) {
        if (className.equals("::double")) {
            return Double.TYPE;
        }
        if (className.equals("::boolean")) {
            return Boolean.TYPE;
        }
        if (className.equals("::int")) {
            return Integer.TYPE;
        }
        if (className.equals("::short")) {
            return Short.TYPE;
        }
        if (className.equals("::long")) {
            return Long.TYPE;
        }
        if (className.equals("::byte")) {
            return Byte.TYPE;
        }
        throw new IllegalArgumentException("This is none of my primitives.");
    }

}
