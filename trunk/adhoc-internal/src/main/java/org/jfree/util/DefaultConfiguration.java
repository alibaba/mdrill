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
 * -------------------------
 * DefaultConfiguration.java
 * -------------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: DefaultConfiguration.java,v 1.6 2008/09/10 09:21:30 mungady Exp $
 *
 * Changes
 * -------
 * 04.06.2003 : Initial version (TM);
 *
 */

package org.jfree.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import org.jfree.base.config.ModifiableConfiguration;

/**
 * Default configuration.
 *
 * @author Thomas Morgner.
 */
public class DefaultConfiguration extends Properties
    implements ModifiableConfiguration
{

  /**
   * Creates an empty property list with no default values.
   */
  public DefaultConfiguration()
  {
    super();
  }

  /**
   * Returns the configuration property with the specified key.
   *
   * @param key the property key.
   * @return the property value.
   */
  public String getConfigProperty(final String key)
  {
    return getProperty(key);
  }

  /**
   * Returns the configuration property with the specified key (or the
   * specified default value if there is no such property).
   * <p/>
   * If the property is not defined in this configuration, the code will
   * lookup the property in the parent configuration.
   *
   * @param key          the property key.
   * @param defaultValue the default value.
   * @return the property value.
   */
  public String getConfigProperty(final String key, final String defaultValue)
  {
    return getProperty(key, defaultValue);
  }

  /**
   * Searches all property keys that start with a given prefix.
   *
   * @param prefix the prefix that all selected property keys should share
   * @return the properties as iterator.
   */
  public Iterator findPropertyKeys(final String prefix)
  {
    final TreeSet collector = new TreeSet();
    final Enumeration enum1 = keys();
    while (enum1.hasMoreElements())
    {
      final String key = (String) enum1.nextElement();
      if (key.startsWith(prefix))
      {
        if (collector.contains(key) == false)
        {
          collector.add(key);
        }
      }
    }
    return Collections.unmodifiableSet(collector).iterator();
  }

  /**
   * Returns an enumeration of the property keys.
   *
   * @return An enumeration of the property keys.
   */
  public Enumeration getConfigProperties()
  {
    return keys();
  }

  /**
   * Sets the value of a configuration property.
   *
   * @param key   the property key.
   * @param value the property value.
   */
  public void setConfigProperty(final String key, final String value)
  {
    if (value == null)
    {
      remove(key);
    }
    else
    {
      setProperty(key, value);
    }
  }
}
