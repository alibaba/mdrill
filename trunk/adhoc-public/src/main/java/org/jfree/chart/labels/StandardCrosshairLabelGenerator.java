/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
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
 * -----------------------------------
 * DefaultCrosshairLabelGenerator.java
 * -----------------------------------
 * (C) Copyright 2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 13-Feb-2009 : Version 1 (DG);
 *
 */

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.NumberFormat;
import org.jfree.chart.plot.Crosshair;

/**
 * A default label generator.
 *
 * @since 1.0.13
 */
public class StandardCrosshairLabelGenerator implements CrosshairLabelGenerator,
        Serializable {

    /** The label format string. */
    private String labelTemplate;

    /** A number formatter for the value. */
    private NumberFormat numberFormat;

    /**
     * Creates a new instance with default attributes.
     */
    public StandardCrosshairLabelGenerator() {
        this("{0}", NumberFormat.getNumberInstance());
    }

    /**
     * Creates a new instance with the specified attributes.
     *
     * @param labelTemplate  the label template (<code>null</code> not
     *     permitted).
     * @param numberFormat  the number formatter (<code>null</code> not
     *     permitted).
     */
    public StandardCrosshairLabelGenerator(String labelTemplate,
            NumberFormat numberFormat) {
        super();
        if (labelTemplate == null) {
            throw new IllegalArgumentException(
                    "Null 'labelTemplate' argument.");
        }
        if (numberFormat == null) {
            throw new IllegalArgumentException(
                    "Null 'numberFormat' argument.");
        }
        this.labelTemplate = labelTemplate;
        this.numberFormat = numberFormat;
    }

    /**
     * Returns the label template string.
     *
     * @return The label template string (never <code>null</code>).
     */
    public String getLabelTemplate() {
        return this.labelTemplate;
    }

    /**
     * Returns the number formatter.
     *
     * @return The formatter (never <code>null</code>).
     */
    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }

    /**
     * Returns a string that can be used as the label for a crosshair.
     *
     * @param crosshair  the crosshair (<code>null</code> not permitted).
     *
     * @return The label (possibly <code>null</code>).
     */
    public String generateLabel(Crosshair crosshair) {
        Object[] v = new Object[] { this.numberFormat.format(
                crosshair.getValue()) };
        String result = MessageFormat.format(this.labelTemplate, v);
        return result;
    }

    /**
     * Tests this generator for equality with an arbitrary object.
     * 
     * @param obj  the object (<code>null</code> permitted).
     * 
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StandardCrosshairLabelGenerator)) {
            return false;
        }
        StandardCrosshairLabelGenerator that
                = (StandardCrosshairLabelGenerator) obj;
        if (!this.labelTemplate.equals(that.labelTemplate)) {
            return false;
        }
        if (!this.numberFormat.equals(that.numberFormat)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code for this instance.
     */
    public int hashCode() {
        return this.labelTemplate.hashCode();
    }

}
