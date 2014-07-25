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
 * ----------------------
 * ModuleInitializer.java
 * ----------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ModuleInitializer.java,v 1.2 2005/10/18 13:14:50 mungady Exp $
 *
 * Changes
 * -------
 * 14-Jul-2003 : Initial version
 * 07-Jun-2004 : Added JCommon header (DG);
 *
 */

package org.jfree.base.modules;

/**
 * The module initializer is used to separate the initialization process from
 * the module definition. An invalid classpath setup or an missing base module
 * may throw an ClassCastException if the module class references this missing
 * resource. Separating them is the best way to make sure that the classloader
 * does not interrupt the module loading process.
 *
 * @author Thomas Morgner
 */
public interface ModuleInitializer {
    
    /**
     * Performs the initalization of the module.
     *
     * @throws ModuleInitializeException if an error occurs which prevents the module
     * from being usable.
     */
    public void performInit() throws ModuleInitializeException;
}
