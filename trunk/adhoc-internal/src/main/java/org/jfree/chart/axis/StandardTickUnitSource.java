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
 * ---------------------------
 * StandardTickUnitSource.java
 * ---------------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 23-Sep-2003 : Version 1 (DG);
 * 25-Oct-2007 : Implemented Serializable and equals() method (DG);
 *
 */

package org.jfree.chart.axis;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * A source that can used by the {@link NumberAxis} class to obtain a
 * suitable {@link TickUnit}.  Instances of this class are {@link Serializable}
 * from version 1.0.7 onwards.  Cloning is not supported, because instances
 * are immutable.
 */
public class StandardTickUnitSource implements TickUnitSource, Serializable {

    /** Constant for log(10.0). */
    private static final double LOG_10_VALUE = Math.log(10.0);

    /**
     * Default constructor.
     */
    public StandardTickUnitSource() {
        super();
    }

    /**
     * Returns a tick unit that is larger than the supplied unit.
     *
     * @param unit  the unit (<code>null</code> not permitted).
     *
     * @return A tick unit that is larger than the supplied unit.
     */
    public TickUnit getLargerTickUnit(TickUnit unit) {
        double x = unit.getSize();
        double log = Math.log(x) / LOG_10_VALUE;
        double higher = Math.ceil(log);
        return new NumberTickUnit(Math.pow(10, higher),
                new DecimalFormat("0.0E0"));
    }

    /**
     * Returns the tick unit in the collection that is greater than or equal
     * to (in size) the specified unit.
     *
     * @param unit  the unit (<code>null</code> not permitted).
     *
     * @return A unit from the collection.
     */
    public TickUnit getCeilingTickUnit(TickUnit unit) {
        return getLargerTickUnit(unit);
    }

    /**
     * Returns the tick unit in the collection that is greater than or equal
     * to the specified size.
     *
     * @param size  the size.
     *
     * @return A unit from the collection.
     */
    public TickUnit getCeilingTickUnit(double size) {
        double log = Math.log(size) / LOG_10_VALUE;
        double higher = Math.ceil(log);
        return new NumberTickUnit(Math.pow(10, higher),
                new DecimalFormat("0.0E0"));
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
        return (obj instanceof StandardTickUnitSource);
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code.
     */
    public int hashCode() {
        return 0;
    }

}
