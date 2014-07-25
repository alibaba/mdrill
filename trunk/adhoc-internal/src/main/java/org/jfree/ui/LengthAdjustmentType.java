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
 * -------------------------
 * LengthAdjustmentType.java
 * -------------------------
 * (C) Copyright 2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: LengthAdjustmentType.java,v 1.5 2005/11/03 09:55:27 mungady Exp $
 *
 * Changes:
 * --------
 * 21-Jan-2005 : Version 1 (DG);
 * 
 */

package org.jfree.ui;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Represents the three options for adjusting a length:  expand, contract, and
 * no change.
 *
 * @author David Gilbert
 */
public final class LengthAdjustmentType implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -6097408511380545010L;
    
    /** NO_CHANGE. */
    public static final LengthAdjustmentType NO_CHANGE 
        = new LengthAdjustmentType("NO_CHANGE");

    /** EXPAND. */
    public static final LengthAdjustmentType EXPAND 
        = new LengthAdjustmentType("EXPAND");

    /** CONTRACT. */
    public static final LengthAdjustmentType CONTRACT 
        = new LengthAdjustmentType("CONTRACT");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private LengthAdjustmentType(final String name) {
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
     * @param obj  the other object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LengthAdjustmentType)) {
            return false;
        }
        final LengthAdjustmentType that = (LengthAdjustmentType) obj;
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
        if (this.equals(LengthAdjustmentType.NO_CHANGE)) {
            return LengthAdjustmentType.NO_CHANGE;
        }
        else if (this.equals(LengthAdjustmentType.EXPAND)) {
            return LengthAdjustmentType.EXPAND;
        }
        else if (this.equals(LengthAdjustmentType.CONTRACT)) {
            return LengthAdjustmentType.CONTRACT;
        }
        return null;
    }
    
}
