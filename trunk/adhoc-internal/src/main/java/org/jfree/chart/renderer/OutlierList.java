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
 * ----------------
 * OutlierList.java
 * ----------------
 * (C) Copyright 2003-2008, by David Browning and Contributors.
 *
 * Original Author:  David Browning (for Australian Institute of Marine
 *                   Science);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 05-Aug-2003 : Version 1, contributed by David Browning (DG);
 * 28-Aug-2003 : Minor tidy-up including Javadocs (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags from all over JFreeChart sources (DG);
 *
 */

package org.jfree.chart.renderer;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of outliers for a single entity in a box and whisker plot.
 *
 * Outliers are grouped in lists for each entity. Lists contain
 * one or more outliers, determined by whether overlaps have
 * occured. Overlapping outliers are grouped in the same list.
 *
 * Each list contains an averaged outlier, which is the same as a single
 * outlier if there is only one outlier in the list, but the average of
 * all the outliers in the list if there is more than one.
 *
 * NB This is simply my scheme for displaying outliers, and might not be
 * acceptable by the wider community.
 */
public class OutlierList {

    /** Storage for the outliers. */
    private List outliers;

    /** The averaged outlier. */
    private Outlier averagedOutlier;

    /**
     * A flag that indicates whether or not there are multiple outliers in the
     * list.
     */
    private boolean multiple = false;

    /**
     * Creates a new list containing a single outlier.
     *
     * @param outlier  the outlier.
     */
    public OutlierList(Outlier outlier) {
        this.outliers = new ArrayList();
        setAveragedOutlier(outlier);
    }

    /**
     * Adds an outlier to the list.
     *
     * @param outlier  the outlier.
     *
     * @return A boolean.
     */
    public boolean add(Outlier outlier) {
        return this.outliers.add(outlier);
    }

    /**
     * Returns the number of outliers in the list.
     *
     * @return The item count.
     */
    public int getItemCount() {
        return this.outliers.size();
    }

    /**
     * Returns the averaged outlier.
     *
     * @return The averaged outlier.
     */
    public Outlier getAveragedOutlier() {
        return this.averagedOutlier;
    }

    /**
     * Sets the averaged outlier.
     *
     * @param averagedOutlier  the averaged outlier.
     */
    public void setAveragedOutlier(Outlier averagedOutlier) {
        this.averagedOutlier = averagedOutlier;
    }

    /**
     * Returns <code>true</code> if the list contains multiple outliers, and
     * <code>false</code> otherwise.
     *
     * @return A boolean.
     */
    public boolean isMultiple() {
        return this.multiple;
    }

    /**
     * Sets the flag that indicates whether or not this list represents
     * multiple outliers.
     *
     * @param multiple  the flag.
     */
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    /**
     * Returns <code>true</code> if the outlier overlaps, and
     * <code>false</code> otherwise.
     *
     * @param other  the outlier.
     *
     * @return A boolean.
     */
    public boolean isOverlapped(Outlier other) {

        if (other == null) {
            return false;
        }

        boolean result = other.overlaps(getAveragedOutlier());
        return result;

    }

    /**
     * Updates the averaged outlier.
     *
     */
    public void updateAveragedOutlier() {
        double totalXCoords = 0.0;
        double totalYCoords = 0.0;
        int size = getItemCount();
        for (Iterator iterator = this.outliers.iterator();
                iterator.hasNext();) {
            Outlier o = (Outlier) iterator.next();
            totalXCoords += o.getX();
            totalYCoords += o.getY();
        }
        getAveragedOutlier().getPoint().setLocation(
                new Point2D.Double(totalXCoords / size, totalYCoords / size));
    }

}
