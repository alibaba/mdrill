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
 * ---------------------------------
 * ExtendedConfigurationWrapper.java
 * ---------------------------------
 * (C)opyright 2002-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ExtendedConfigurationWrapper.java,v 1.8 2008/09/10 09:22:04 mungady Exp $
 *
 * Changes
 * -------
 * 20-May-2005 : Initial version.
 */

package org.jfree.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * A wrapper for the extended configuration interface around a plain configuration.
 *
 * @author Thomas Morgner
 */
public class ExtendedConfigurationWrapper
        implements ExtendedConfiguration
{
  /** The base configuration. */
  private Configuration parent;

  /**
   * Creates a wrapper around the given configuration.
   *
   * @param parent the wrapped up configuration.
   * @throws NullPointerException if the parent is null.
   */
  public ExtendedConfigurationWrapper (final Configuration parent)
  {
    if (parent == null)
    {
      throw new NullPointerException("Parent given must not be null");
    }
    this.parent = parent;
  }

  /**
   * Returns the boolean value of a given configuration property. The boolean value true
   * is returned, if the contained string is equal to 'true'.
   *
   * @param name the name of the property
   * @return the boolean value of the property.
   */
  public boolean getBoolProperty (final String name)
  {
    return getBoolProperty(name, false);
  }

  /**
   * Returns the boolean value of a given configuration property. The boolean value true
   * is returned, if the contained string is equal to 'true'. If the property is not set,
   * the default value is returned.
   *
   * @param name the name of the property
   * @param defaultValue the default value to be returned if the property is not set
   * @return the boolean value of the property.
   */
  public boolean getBoolProperty (final String name,
                                  final boolean defaultValue)
  {
    return "true".equals(this.parent.getConfigProperty(name, String.valueOf(defaultValue)));
  }

  /**
   * Returns a given property as int value. Zero is returned if the
   * property value is no number or the property is not set.
   *
   * @param name the name of the property
   * @return the parsed number value or zero
   */
  public int getIntProperty (final String name)
  {
    return getIntProperty(name, 0);
  }

  /**
   * Returns a given property as int value. The specified default value is returned if the
   * property value is no number or the property is not set.
   *
   * @param name the name of the property
   * @param defaultValue the value to be returned if the property is no integer value
   * @return the parsed number value or the specified default value
   */
  public int getIntProperty (final String name,
                             final int defaultValue)
  {
    final String retval = this.parent.getConfigProperty(name);
    if (retval == null)
    {
      return defaultValue;
    }
    try
    {
      return Integer.parseInt(retval);
    }
    catch (Exception e)
    {
      return defaultValue;
    }
  }

  /**
   * Checks, whether a given property is defined.
   *
   * @param name the name of the property
   * @return true, if the property is defined, false otherwise.
   */
  public boolean isPropertySet (final String name)
  {
    return this.parent.getConfigProperty(name) != null;
  }

  /**
   * Returns all keys with the given prefix.
   *
   * @param prefix the prefix
   * @return the iterator containing all keys with that prefix
   */
  public Iterator findPropertyKeys (final String prefix)
  {
    return this.parent.findPropertyKeys(prefix);
  }

  /**
   * Returns the configuration property with the specified key.
   *
   * @param key the property key.
   * @return the property value.
   */
  public String getConfigProperty (final String key)
  {
    return this.parent.getConfigProperty(key);
  }

  /**
   * Returns the configuration property with the specified key (or the specified default
   * value if there is no such property).
   * <p/>
   * If the property is not defined in this configuration, the code will lookup the
   * property in the parent configuration.
   *
   * @param key          the property key.
   * @param defaultValue the default value.
   * @return the property value.
   */
  public String getConfigProperty (final String key, final String defaultValue)
  {
    return this.parent.getConfigProperty(key, defaultValue);
  }

  /**
   * Returns an enumeration of the configuration properties.
   *
   * @return An enumeration.
   */
  public Enumeration getConfigProperties()
  {
    return this.parent.getConfigProperties();
  }

  /**
   * Returns a clone of this instance.
   *
   * @return A clone.
   *
   * @throws CloneNotSupportedException if there is a problem cloning.
   */
  public Object clone () throws CloneNotSupportedException
  {
    ExtendedConfigurationWrapper wrapper = (ExtendedConfigurationWrapper) super.clone();
    wrapper.parent = (Configuration) this.parent.clone();
    return this.parent;
  }
}
