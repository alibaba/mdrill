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
 * --------------------------
 * OutlierListCollection.java
 * --------------------------
 * (C) Copyright 2003-2008, by David Browning and Contributors.
 *
 * Original Author:  David Browning (for Australian Institute of Marine
 *                   Science);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 05-Aug-2003 : Version 1, contributed by David Browning (DG);
 * 01-Sep-2003 : Made storage internal rather than extending ArrayList (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags from all over JFreeChart sources (DG);
 *
 */

package org.jfree.chart.renderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of outlier lists for a box and whisker plot. Each collection is
 * associated with a single box and whisker entity.
 *
 * Outliers are grouped in lists for each entity. Lists contain
 * one or more outliers, determined by whether overlaps have
 * occurred. Overlapping outliers are grouped in the same list.
 *
 * @see org.jfree.chart.renderer.OutlierList
 */
public class OutlierListCollection {

    /** Storage for the outlier lists. */
    private List outlierLists;

    /**
     * Unbelievably, outliers which are more than 2 * interquartile range are
     * called far outs...  See Tukey EDA  (a classic one of a kind...)
     */
    private boolean highFarOut = false;

    /**
     * A flag that indicates whether or not the collection contains low far
     * out values.
     */
    private boolean lowFarOut = false;

    /**
     * Creates a new empty collection.
     */
    public OutlierListCollection() {
        this.outlierLists = new ArrayList();
    }

    /**
     * A flag to indicate the presence of one or more far out values at the
     * top end of the range.
     *
     * @return A <code>boolean</code>.
     */
    public boolean isHighFarOut() {
        return this.highFarOut;
    }

    /**
     * Sets the flag that indicates the presence of one or more far out values
     * at the top end of the range.
     *
     * @param farOut  the flag.
     */
    public void setHighFarOut(boolean farOut) {
        this.highFarOut = farOut;
    }

    /**
     * A flag to indicate the presence of one or more far out values at the
     * bottom end of the range.
     *
     * @return A <code>boolean</code>.
     */
    public boolean isLowFarOut() {
        return this.lowFarOut;
    }

    /**
     * Sets the flag that indicates the presence of one or more far out values
     * at the bottom end of the range.
     *
     * @param farOut  the flag.
     */
    public void setLowFarOut(boolean farOut) {
        this.lowFarOut = farOut;
    }
    /**
     * Appends the specified element as a new <code>OutlierList</code> to the
     * end of this list if it does not overlap an outlier in an existing list.
     *
     * If it does overlap, it is appended to the outlier list which it overlaps
     * and that list is updated.
     *
     * @param outlier  element to be appended to this list.
     *
     * @return <tt>true</tt> (as per the general contract of Collection.add).
     */
    public boolean add(Outlier outlier) {

        if (this.outlierLists.isEmpty()) {
            return this.outlierLists.add(new OutlierList(outlier));
        }
        else {
            boolean updated = false;
            for (Iterator iterator = this.outlierLists.iterator();
                 iterator.hasNext();) {
                OutlierList list = (OutlierList) iterator.next();
                if (list.isOverlapped(outlier)) {
                    updated = updateOutlierList(list, outlier);
                }
            }
            if (!updated) {
                //System.err.print(" creating new outlier list ");
                updated = this.outlierLists.add(new OutlierList(outlier));
            }
            return updated;
        }

    }

    /**
     * Returns an iterator for the outlier lists.
     *
     * @return An iterator.
     */
    public Iterator iterator() {
        return this.outlierLists.iterator();
    }


    /**
     * Updates the outlier list by adding the outlier to the end of the list and
     * setting the averaged outlier to the average x and y coordinnate values
     * of the outliers in the list.
     *
     * @param list  the outlier list to be updated.
     * @param outlier  the outlier to be added
     *
     * @return <tt>true</tt> (as per the general contract of Collection.add).
     */
    private boolean updateOutlierList(OutlierList list, Outlier outlier) {
        boolean result = false;
        result = list.add(outlier);
        list.updateAveragedOutlier();
        list.setMultiple(true);
        return result;
    }

}
