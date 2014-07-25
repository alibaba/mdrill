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
 * -------------
 * UnitType.java
 * -------------
 * (C) Copyright 2004, 2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: UnitType.java,v 1.7 2005/10/18 13:24:19 mungady Exp $
 *
 * Changes:
 * --------
 * 11-Feb-2004 : Version 1 (DG);
 * 
 */

package org.jfree.util;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Used to indicate absolute or relative units.
 *
 * @author David Gilbert
 */
public final class UnitType implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 6531925392288519884L;    
    
    /** Absolute. */
    public static final UnitType ABSOLUTE = new UnitType("UnitType.ABSOLUTE");

    /** Relative. */
    public static final UnitType RELATIVE = new UnitType("UnitType.RELATIVE");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private UnitType(final String name) {
        this.name = name;
    }

    /**
     * Returns a string representing the object.
     *
     * @return The string.
     */
    public String toString() {
        return this.name;
    }

    /**
     * Returns <code>true</code> if this object is equal to the specified 
     * object, and <code>false</code> otherwise.
     *
     * @param obj  the other object.
     *
     * @return A boolean.
     */
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof UnitType)) {
            return false;
        }
        final UnitType that = (UnitType) obj;
        if (!this.name.equals(that.name)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return The hashcode
     */
    public int hashCode() {
        return this.name.hashCode();
    }

    /**
     * Ensures that serialization returns the unique instances.
     * 
     * @return The object.
     * 
     * @throws ObjectStreamException if there is a problem.
     */
    private Object readResolve() throws ObjectStreamException {
        if (this.equals(UnitType.ABSOLUTE)) {
            return UnitType.ABSOLUTE;
        }
        else if (this.equals(UnitType.RELATIVE)) {
            return UnitType.RELATIVE;
        }
        return null;
    }
    
}
