/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2006, by Object Refinery Limited and Contributors.
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
 * JCommon.java
 * ------------
 * (C) Copyright 2002-2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: JCommon.java,v 1.5 2006/02/19 21:10:47 taqua Exp $
 *
 * Changes
 * -------
 * 28-Feb-2002 : Version 1 (DG);
 * 22-Mar-2002 : Changed version number to 0.6.0 (DG);
 * 25-Mar-2002 : Moved the project info details into a class so that the text can be localised
 *               more easily (DG);
 * 04-Apr-2002 : Added Hari to contributors (DG);
 * 19-Apr-2002 : Added Sam (oldman) to contributors (DG);
 * 07-Jun-2002 : Added contributors (DG);
 * 24-Jun-2002 : Removed unnecessary imports (DG);
 * 27-Aug-2002 : Updated version number to 0.7.0 (DG);
 *
 */

package org.jfree;

import org.jfree.ui.about.ProjectInfo;

/**
 * This class contains static information about the JCommon class library.
 *
 * @author David Gilbert
 */
public final class JCommon {

    /** Information about the project. */
    public static final ProjectInfo INFO = JCommonInfo.getInstance();
    
    /**
     * Hidden constructor.
     */
    private JCommon() {
        super();
    }
    
    /**
     * Prints information about JCommon to standard output.
     *
     * @param args  no arguments are honored.
     */
    public static void main(final String[] args) {
        System.out.println(JCommon.INFO.toString());
    }

}
