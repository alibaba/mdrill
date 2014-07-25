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
 * ----------------------
 * PieLabelLinkStyle.java
 * ----------------------
 * (C) Copyright 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 31-Mar-2008 : Version 1 (DG);
 *
 */

package org.jfree.chart.plot;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Used to indicate the style for the lines linking pie sections to their
 * corresponding labels.
 *
 * @since 1.0.10
 */
public final class PieLabelLinkStyle implements Serializable {

    /** STANDARD. */
    public static final PieLabelLinkStyle STANDARD
            = new PieLabelLinkStyle("PieLabelLinkStyle.STANDARD");

    /** QUAD_CURVE. */
    public static final PieLabelLinkStyle QUAD_CURVE
            = new PieLabelLinkStyle("PieLabelLinkStyle.QUAD_CURVE");

    /** CUBIC_CURVE. */
    public static final PieLabelLinkStyle CUBIC_CURVE
            = new PieLabelLinkStyle("PieLabelLinkStyle.CUBIC_CURVE");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private PieLabelLinkStyle(String name) {
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
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PieLabelLinkStyle)) {
            return false;
        }
        PieLabelLinkStyle style = (PieLabelLinkStyle) obj;
        if (!this.name.equals(style.toString())) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code.
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
        Object result = null;
        if (this.equals(PieLabelLinkStyle.STANDARD)) {
            result = PieLabelLinkStyle.STANDARD;
        }
        else if (this.equals(PieLabelLinkStyle.QUAD_CURVE)) {
            result = PieLabelLinkStyle.QUAD_CURVE;
        }
        else if (this.equals(PieLabelLinkStyle.CUBIC_CURVE)) {
            result = PieLabelLinkStyle.CUBIC_CURVE;
        }
        return result;
    }

}
