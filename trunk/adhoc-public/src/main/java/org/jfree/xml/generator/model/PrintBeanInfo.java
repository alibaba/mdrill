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
 * ------------------
 * PrintBeanInfo.java
 * ------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: PrintBeanInfo.java,v 1.3 2005/10/18 13:32:37 mungady Exp $
 *
 * Changes 
 * -------
 * 21.06.2003 : Initial version (TM);
 *  
 */

package org.jfree.xml.generator.model;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/**
 * A utility class for printing information about a class.
 */
public class PrintBeanInfo {

  private PrintBeanInfo ()
  {
  }

    /**
     * Prints the information for a class.
     * 
     * @param c  the class.
     */
    public static void print (final Class c) {
        try {
            System.out.println("Class: " + c.getName());
            System.out.println(
                "========================================================================"
            );
            final BeanInfo bi = Introspector.getBeanInfo(c, c.getSuperclass());
            final PropertyDescriptor[] pd = bi.getPropertyDescriptors();
            for (int i = 0; i < pd.length; i++) {
                System.out.println ("Property: " + pd[i].getDisplayName());
                System.out.println(
                    "---------------------------------------------------------------------"
                );
                System.out.println (" ( " + pd[i].getShortDescription() + ")");
                if (pd[i] instanceof IndexedPropertyDescriptor) {
                    final IndexedPropertyDescriptor id = (IndexedPropertyDescriptor) pd[i];
                    System.out.println ("  - idx-type   : " + id.getIndexedPropertyType());
                    System.out.println ("  - idx-read   : " + id.getIndexedReadMethod());
                    System.out.println ("  - idx-write  : " + id.getIndexedWriteMethod());
                }
                else {
                    System.out.println ("  - type       : " + pd[i].getPropertyType());
                    System.out.println ("  - read       : " + pd[i].getReadMethod());
                    System.out.println ("  - write      : " + pd[i].getWriteMethod());
                }
                System.out.println ("  - bound      : " + pd[i].isBound());
                System.out.println ("  - constrained: " + pd[i].isConstrained());
            }
        }
        catch (IntrospectionException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Entry point for this utility application.
     * 
     * @param args  the class names.
     * 
     * @throws Exception if there is a problem.
     */
    public static void main(final String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            print(Class.forName(args[i]));
        }
    }

}
