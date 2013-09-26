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
 * ModifiableConfiguration.java
 * ----------------------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Thomas Morgner;
 *
 * $Id: ModifiableConfiguration.java,v 1.3 2005/10/18 13:14:12 mungady Exp $
 *
 * Changes
 * -------
 * 07-Jun-2004 : Added JCommon header (DG);
 *
 */

package org.jfree.base.config;

import java.util.Enumeration;
import java.util.Iterator;

import org.jfree.util.Configuration;

/**
 * A modifiable configuration.
 *
 * @author Thomas Morgner
 */
public interface ModifiableConfiguration extends Configuration {
 
    /**
     * Sets the value of a configuration property.
     * 
     * @param key  the property key.
     * @param value  the property value.
     */
    public void setConfigProperty(final String key, final String value);
  
    /**
     * Returns the configuration properties.
     * 
     * @return The configuration properties.
     */
    public Enumeration getConfigProperties();
  
    /**
     * Returns an iterator for the keys beginning with the specified prefix.
     * 
     * @param prefix  the prefix.
     * 
     * @return The iterator.
     */
    public Iterator findPropertyKeys(final String prefix);

}
