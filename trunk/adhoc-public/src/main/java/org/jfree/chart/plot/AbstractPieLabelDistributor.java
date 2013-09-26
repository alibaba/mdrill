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
 * --------------------------------
 * AbstractPieLabelDistributor.java
 * --------------------------------
 * (C) Copyright 2007, 2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 14-Jun-2007 : Version 1 (DG);
 *
 */

package org.jfree.chart.plot;

import java.io.Serializable;
import java.util.List;

/**
 * A base class for handling the distribution of pie section labels.  Create
 * your own subclass and set it using the
 * {@link PiePlot#setLabelDistributor(AbstractPieLabelDistributor)} method
 * if you want to customise the label distribution.
 */
public abstract class AbstractPieLabelDistributor implements Serializable {

    /** The label records. */
    protected List labels;

    /**
     * Creates a new instance.
     */
    public AbstractPieLabelDistributor() {
        this.labels = new java.util.ArrayList();
    }

    /**
     * Returns a label record from the list.
     *
     * @param index  the index.
     *
     * @return The label record.
     */
    public PieLabelRecord getPieLabelRecord(int index) {
        return (PieLabelRecord) this.labels.get(index);
    }

    /**
     * Adds a label record.
     *
     * @param record  the label record (<code>null</code> not permitted).
     */
    public void addPieLabelRecord(PieLabelRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Null 'record' argument.");
        }
        this.labels.add(record);
    }

    /**
     * Returns the number of items in the list.
     *
     * @return The item count.
     */
    public int getItemCount() {
        return this.labels.size();
    }

    /**
     * Clears the list of labels.
     */
    public void clear() {
        this.labels.clear();
    }

    /**
     * Called by the {@link PiePlot} class.  Implementations should distribute
     * the labels in this.labels then return.
     *
     * @param minY  the y-coordinate for the top of the label area.
     * @param height  the height of the label area.
     */
    public abstract void distributeLabels(double minY, double height);

}
