/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
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
 * -----------------
 * DatasetGroup.java
 * -----------------
 * (C) Copyright 2002-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 07-Oct-2002 : Version 1 (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 20-Aug-2003 : Implemented Cloneable (DG);
 *
 */

package org.jfree.data.general;

import java.io.Serializable;

/**
 * A class that is used to group datasets (currently not used for any specific
 * purpose).
 */
public class DatasetGroup implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -3640642179674185688L;

    /** The group id. */
    private String id;

    /**
     * Constructs a new group.
     */
    public DatasetGroup() {
        super();
        this.id = "NOID";
    }

    /**
     * Creates a new group with the specified id.
     *
     * @param id  the identification for the group.
     */
    public DatasetGroup(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Null 'id' argument.");
        }
        this.id = id;
    }

    /**
     * Returns the identification string for this group.
     *
     * @return The identification string.
     */
    public String getID() {
        return this.id;
    }

    /**
     * Clones the group.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException not by this class.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Tests this instance for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DatasetGroup)) {
            return false;
        }
        DatasetGroup that = (DatasetGroup) obj;
        if (!this.id.equals(that.id)) {
            return false;
        }
        return true;
    }

}
