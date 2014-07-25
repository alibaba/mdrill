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
 * ------------------------------
 * ModuleInitializeException.java
 * ------------------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ModuleInitializeException.java,v 1.2 2005/10/18 13:14:50 mungady Exp $
 *
 * Changes
 * -------
 * 05-Jul-2003 : Initial version
 * 07-Jun-2004 : Added JCommon header (DG);
 *
 */

package org.jfree.base.modules;

import org.jfree.util.StackableException;

/**
 * This exception is thrown when the module initialization encountered an
 * unrecoverable error which prevents the module from being used.
 *
 * @author Thomas Morgner
 */
public class ModuleInitializeException extends StackableException {

    /**
     * Creates a ModuleInitializeException with no message and no base
     * exception.
     */
    public ModuleInitializeException() {
        // nothing required
    }

    /**
     * Creates a ModuleInitializeException with the given message and base
     * exception.
     *
     * @param s the message
     * @param e the root exception
     */
    public ModuleInitializeException(final String s, final Exception e) {
        super(s, e);
    }

    /**
     * Creates a ModuleInitializeException with the given message and no base
     * exception.
     *
     * @param s the exception message
     */
    public ModuleInitializeException(final String s) {
        super(s);
    }
    
}
