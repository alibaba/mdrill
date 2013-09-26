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
 * --------------------------------
 * SystemPropertyConfiguration.java
 * --------------------------------
 * (C)opyright 2002-2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner
 * Contributor(s):   Stefan Prange;
 *
 * $Id: SystemPropertyConfiguration.java,v 1.3 2005/10/18 13:14:12 mungady Exp $
 *
 * Changes (from 8-Feb-2002)
 * -------------------------
 * 14-Jan-2003 : Initial Version, moved from inner class of ReportConfiguration
 * 05-Feb-2003 : This implementation now handles SecurityExceptions.
 * 
 */

package org.jfree.base.config;

import java.util.Enumeration;
import java.util.Vector;

/**
 * A property configuration based on system properties.
 *
 * @author Thomas Morgner
 */
public class SystemPropertyConfiguration extends HierarchicalConfiguration {

    /**
     * Creates a report configuration that includes all the system properties (whether they are
     * related to reports or not).  The parent configuration is a
     * <code>PropertyFileConfiguration</code>.
     */
    public SystemPropertyConfiguration() {
    }

    /**
     * Sets a configuration property.
     *
     * @param key  the property key.
     * @param value  the property value.
     */
    public void setConfigProperty(final String key, final String value) {
        throw new UnsupportedOperationException("The SystemPropertyConfiguration is readOnly");
    }

    /**
     * Returns the configuration property with the specified key (or the specified default value
     * if there is no such property).
     * <p>
     * If the property is not defined in this configuration, the code will lookup the property in
     * the parent configuration.
     *
     * @param key  the property key.
     * @param defaultValue  the default value.
     *
     * @return the property value.
     */
    public String getConfigProperty(final String key, final String defaultValue) {
        try {
            final String value = System.getProperty(key);
            if (value != null) {
               return value;
            }
        }
        catch (SecurityException se) {
            // ignore security exceptions, continue as if the property was not set..
        }
        return super.getConfigProperty(key, defaultValue);
    }

    /**
     * Checks, whether the given key is locally defined in the system properties.
     * @see HierarchicalConfiguration#isLocallyDefined(java.lang.String)
     *
     * @param key the key that should be checked.
     * @return true, if the key is defined in the system properties, false otherwise.
     */
    public boolean isLocallyDefined(final String key) {
        try {
            return System.getProperties().containsKey(key);
        }
        catch (SecurityException se) {
            return false;
        }
    }

    /**
     * Returns all defined configuration properties for the report. The enumeration
     * contains all keys of the changed properties, properties set from files or
     * the system properties are not included.
     *
     * @return all defined configuration properties for the report.
     */
    public Enumeration getConfigProperties() {
        try {
            return System.getProperties().keys();
        }
        catch (SecurityException se) {
            // should return an empty enumeration ...
            return new Vector().elements();
        }
    }
}
