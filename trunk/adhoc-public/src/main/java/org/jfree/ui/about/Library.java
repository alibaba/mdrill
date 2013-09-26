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
 * ------------
 * Library.java
 * ------------
 * (C) Copyright 2001-2004, by Thomas Morgner.
 * 
 * Original Author:  Thomas Morgner;
 * Contributor(s):   -;
 *
 * $Id: Library.java,v 1.5 2005/11/08 14:24:34 mungady Exp $
 *
 * Changes
 * -------
 *
 */


package org.jfree.ui.about;

/**
 * Library specification moved to base package to allow more control
 * over the boot process.
 *
 * @author David Gilbert
 * @deprecated shadow class for deprecation
 */
public class Library extends org.jfree.base.Library {

    /**
     * Creates a new library reference.
     *
     * @param name    the name.
     * @param version the version.
     * @param licence the licence.
     * @param info    the web address or other info.
     */
    public Library(final String name, final String version, final String licence, final String info) {
        super(name, version, licence, info);
    }

    /**
     * Constructs a library reference from a ProjectInfo object.
     *
     * @param project  information about a project.
     */
    public Library(final ProjectInfo project) {

        this(project.getName(), project.getVersion(),
             project.getLicenceName(), project.getInfo());
    }
}
