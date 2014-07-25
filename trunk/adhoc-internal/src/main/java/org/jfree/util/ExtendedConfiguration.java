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
 * -------------
 * ExtendedConfiguration.java
 * -------------
 * (C)opyright 2002-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ExtendedConfiguration.java,v 1.3 2005/10/18 13:24:19 mungady Exp $
 *
 * Changes
 * -------
 * 20-May-2005 : Initial version.
 */
package org.jfree.util;

/**
 * The extended configuration provides methods to make using the
 * configuration easier.
 *
 * @author Thomas Morgner
 */
public interface ExtendedConfiguration extends Configuration
{
  /**
   * Checks, whether a given property is defined.
   *
   * @param name the name of the property
   * @return true, if the property is defined, false otherwise.
   */
  public boolean isPropertySet (String name);

  /**
   * Returns a given property as int value. Zero is returned if the
   * property value is no number or the property is not set.
   *
   * @param name the name of the property
   * @return the parsed number value or zero
   */
  public int getIntProperty (String name);

  /**
   * Returns a given property as int value. The specified default value is returned if the
   * property value is no number or the property is not set.
   *
   * @param name the name of the property
   * @param defaultValue the value to be returned if the property is no integer value
   * @return the parsed number value or the specified default value
   */
  public int getIntProperty (String name, int defaultValue);

  /**
   * Returns the boolean value of a given configuration property. The boolean value true
   * is returned, if the contained string is equal to 'true'.
   *
   * @param name the name of the property
   * @return the boolean value of the property.
   */
  public boolean getBoolProperty (String name);

  /**
   * Returns the boolean value of a given configuration property. The boolean value true
   * is returned, if the contained string is equal to 'true'. If the property is not set,
   * the default value is returned.
   *
   * @param name the name of the property
   * @param defaultValue the default value to be returned if the property is not set
   * @return the boolean value of the property.
   */
  public boolean getBoolProperty (String name, boolean defaultValue);
}
