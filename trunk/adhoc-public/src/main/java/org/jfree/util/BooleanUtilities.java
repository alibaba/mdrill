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
 * ---------------------
 * BooleanUtilities.java
 * ---------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: BooleanUtilities.java,v 1.4 2005/10/18 13:24:19 mungady Exp $
 *
 * Changes
 * -------
 * 23-Oct-2003 : Version 1 (DG);
 * 04-Oct-2004 : Renamed BooleanUtils --> BooleanUtilities (DG);
 *
 */

package org.jfree.util;

/**
 * Utility methods for working with <code>Boolean</code> objects.
 * 
 * @author David Gilbert
 */
public class BooleanUtilities {

    /**
     * Private constructor prevents object creation.
     */
    private BooleanUtilities() {
    }

    /**
     * Returns the object equivalent of the boolean primitive.
     * <p>
     * A similar method is provided by the Boolean class in JDK 1.4, but you can use this one
     * to remain compatible with earlier JDKs.
     * 
     * @param b  the boolean value.
     * 
     * @return <code>Boolean.TRUE</code> or <code>Boolean.FALSE</code>.
     */
    public static Boolean valueOf(final boolean b) {
        return (b ? Boolean.TRUE : Boolean.FALSE);
    }
    
}
