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
 * ------------------------
 * DefaultValueDataset.java
 * ------------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 27-Mar-2003 : Version 1 (DG);
 * 18-Aug-2003 : Implemented Cloneable (DG);
 * 03-Mar-2005 : Implemented PublicCloneable (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 30-Jan-2007 : Added explicit super() call in constructor (for clarity) (DG);
 *
 */

package org.jfree.data.general;

import java.io.Serializable;

import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A dataset that stores a single value (that is possibly <code>null</code>).
 * This class provides a default implementation of the {@link ValueDataset}
 * interface.
 */
public class DefaultValueDataset extends AbstractDataset
        implements ValueDataset, Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 8137521217249294891L;

    /** The value. */
    private Number value;

    /**
     * Constructs a new dataset, initially empty.
     */
    public DefaultValueDataset() {
        this(null);
    }

    /**
     * Creates a new dataset with the specified value.
     *
     * @param value  the value.
     */
    public DefaultValueDataset(double value) {
        this(new Double(value));
    }

    /**
     * Creates a new dataset with the specified value.
     *
     * @param value  the initial value (<code>null</code> permitted).
     */
    public DefaultValueDataset(Number value) {
        super();
        this.value = value;
    }

    /**
     * Returns the value.
     *
     * @return The value (possibly <code>null</code>).
     */
    public Number getValue() {
        return this.value;
    }

    /**
     * Sets the value and sends a {@link DatasetChangeEvent} to all registered
     * listeners.
     *
     * @param value  the new value (<code>null</code> permitted).
     */
    public void setValue(Number value) {
        this.value = value;
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    /**
     * Tests this dataset for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ValueDataset) {
            ValueDataset vd = (ValueDataset) obj;
            return ObjectUtilities.equal(this.value, vd.getValue());
        }
        return false;
    }

    /**
     * Returns a hash code.
     *
     * @return A hash code.
     */
    public int hashCode() {
        return (this.value != null ? this.value.hashCode() : 0);
    }

}
