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
 * ---------------
 * ModuleInfo.java
 * ---------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ModuleInfo.java,v 1.3 2005/10/18 13:14:50 mungady Exp $
 *
 * Changes
 * -------
 * 07-Jun-2004 : Added JCommon header (DG);
 *
 */

package org.jfree.base.modules;

/**
 * The Module info class encapsulates metadata about a given module. It holds the
 * list of dependencies and the module version and description.
 *
 * @author Thomas Morgner
 */
public interface ModuleInfo {

    /**
     * Returns the module class of the desired base module.
     *
     * @return The module class.
     */
    public String getModuleClass();

    /**
     * Returns the major version of the base module. The string should
     * contain a compareable character sequence so that higher versions
     * of the module are considered greater than lower versions.
     *
     * @return The major version of the module.
     */
    public String getMajorVersion();

    /**
     * Returns the minor version of the base module. The string should
     * contain a compareable character sequence so that higher versions
     * of the module are considered greater than lower versions.
     *
     * @return The minor version of the module.
     */
    public String getMinorVersion();

    /**
     * Returns the patchlevel version of the base module. The patch level
     * should be used to mark bugfixes. The string should
     * contain a compareable character sequence so that higher versions
     * of the module are considered greater than lower versions.
     *
     * @return The patch level version of the module.
     */
    public String getPatchLevel();
  
}
