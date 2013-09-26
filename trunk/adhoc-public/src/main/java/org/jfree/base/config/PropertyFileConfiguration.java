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
 * ------------------------------------
 * PropertyFileReportConfiguration.java
 * ------------------------------------
 * (C)opyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: PropertyFileConfiguration.java,v 1.10 2008/09/10 09:17:28 mungady Exp $
 *
 * Changes
 * -------
 * 14-Jan-2003 : Initial version
 */
package org.jfree.base.config;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jfree.util.Log;
import org.jfree.util.ObjectUtilities;

/**
 * A report configuration that reads its values from an arbitary property file.
 *
 * @author Thomas Morgner
 */
public class PropertyFileConfiguration extends HierarchicalConfiguration
{
  /**
   * Default constructor.
   */
  public PropertyFileConfiguration() {
      // nothing required
  }

  /**
   * Load the properties in the given file.
   *
   * @param resourceName  the file name.
   */
  public void load(final String resourceName)
  {
    load(resourceName, PropertyFileConfiguration.class);
  }

  /**
   * Loads the properties stored in the given file. This method does nothing if
   * the file does not exist or is unreadable. Appends the contents of the loaded
   * properties to the already stored contents.
   *
   * @param resourceName the file name of the stored properties.
   * @param resourceSource ?
   */
  public void load(final String resourceName, final Class resourceSource)
  {
    final InputStream in = ObjectUtilities.getResourceRelativeAsStream
            (resourceName, resourceSource);
    if (in != null)
    {
      try
      {
        load(in);
      }
      finally
      {
        try
        {
          in.close();
        }
        catch (IOException e)
        {
          // ignore
        }
      }
    }
    else
    {
      Log.debug ("Configuration file not found in the classpath: " + resourceName);
    }

  }

  /**
   * Loads the properties stored in the given file. This method does nothing if
   * the file does not exist or is unreadable. Appends the contents of the loaded
   * properties to the already stored contents.
   *
   * @param in the input stream used to read the properties.
   */
  public void load(final InputStream in)
  {
    if (in == null)
    {
      throw new NullPointerException();
    }

    try
    {
      final BufferedInputStream bin = new BufferedInputStream(in);
      final Properties p = new Properties();
      p.load(bin);
      this.getConfiguration().putAll(p);
      bin.close();
    }
    catch (IOException ioe)
    {
      Log.warn("Unable to read configuration", ioe);
    }

  }

}
