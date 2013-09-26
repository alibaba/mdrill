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
 * PieLabelDistributor.java
 * ------------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 08-Mar-2004 : Version 1 (DG);
 * 18-Apr-2005 : Use StringBuffer (DG);
 * 14-Jun-2007 : Now extends AbstractPieLabelDistributor (DG);
 * 31-Mar-2008 : Fix bugs in label distribution (DG);
 *
 */

package org.jfree.chart.plot;

import java.util.Collections;

/**
 * This class distributes the section labels for one side of a pie chart so
 * that they do not overlap.
 */
public class PieLabelDistributor extends AbstractPieLabelDistributor {

    /** The minimum gap. */
    private double minGap = 4.0;

    /**
     * Creates a new distributor.
     *
     * @param labelCount  the number of labels (ignored).
     */
    public PieLabelDistributor(int labelCount) {
        super();
    }

    /**
     * Distributes the labels.
     *
     * @param minY  the minimum y-coordinate in Java2D-space.
     * @param height  the available height (in Java2D units).
     */
    public void distributeLabels(double minY, double height) {
        sort();  // sorts the label records into ascending order by baseY
//        if (isOverlap()) {
//            adjustInwards();
//        }
        // if still overlapping, do something else...
        if (isOverlap()) {
            adjustDownwards(minY, height);
        }

        if (isOverlap()) {
            adjustUpwards(minY, height);
        }

        if (isOverlap()) {
            spreadEvenly(minY, height);
        }
    }

    /**
     * Returns <code>true</code> if there are overlapping labels in the list,
     * and <code>false</code> otherwise.
     *
     * @return A boolean.
     */
    private boolean isOverlap() {
        double y = 0.0;
        for (int i = 0; i < this.labels.size(); i++) {
            PieLabelRecord plr = getPieLabelRecord(i);
            if (y > plr.getLowerY()) {
                return true;
            }
            y = plr.getUpperY();
        }
        return false;
    }

    /**
     * Adjusts the y-coordinate for the labels in towards the center in an
     * attempt to fix overlapping.
     */
    protected void adjustInwards() {
        int lower = 0;
        int upper = this.labels.size() - 1;
        while (upper > lower) {
            if (lower < upper - 1) {
                PieLabelRecord r0 = getPieLabelRecord(lower);
                PieLabelRecord r1 = getPieLabelRecord(lower + 1);
                if (r1.getLowerY() < r0.getUpperY()) {
                    double adjust = r0.getUpperY() - r1.getLowerY()
                                    + this.minGap;
                    r1.setAllocatedY(r1.getAllocatedY() + adjust);
                }
            }
            PieLabelRecord r2 = getPieLabelRecord(upper - 1);
            PieLabelRecord r3 = getPieLabelRecord(upper);
            if (r2.getUpperY() > r3.getLowerY()) {
                double adjust = (r2.getUpperY() - r3.getLowerY()) + this.minGap;
                r3.setAllocatedY(r3.getAllocatedY() + adjust);
            }
            lower++;
            upper--;
        }
    }

    /**
     * Any labels that are overlapping are moved down in an attempt to
     * eliminate the overlaps.
     *
     * @param minY  the minimum y value (in Java2D coordinate space).
     * @param height  the height available for all labels.
     */
    protected void adjustDownwards(double minY, double height) {
        for (int i = 0; i < this.labels.size() - 1; i++) {
            PieLabelRecord record0 = getPieLabelRecord(i);
            PieLabelRecord record1 = getPieLabelRecord(i + 1);
            if (record1.getLowerY() < record0.getUpperY()) {
                record1.setAllocatedY(Math.min(minY + height
                        - record1.getLabelHeight() / 2.0,
                        record0.getUpperY() + this.minGap
                        + record1.getLabelHeight() / 2.0));
            }
        }
    }

    /**
     * Any labels that are overlapping are moved up in an attempt to eliminate
     * the overlaps.
     *
     * @param minY  the minimum y value (in Java2D coordinate space).
     * @param height  the height available for all labels.
     */
    protected void adjustUpwards(double minY, double height) {
        for (int i = this.labels.size() - 1; i > 0; i--) {
            PieLabelRecord record0 = getPieLabelRecord(i);
            PieLabelRecord record1 = getPieLabelRecord(i - 1);
            if (record1.getUpperY() > record0.getLowerY()) {
                record1.setAllocatedY(Math.max(minY
                        + record1.getLabelHeight() / 2.0, record0.getLowerY()
                        - this.minGap - record1.getLabelHeight() / 2.0));
            }
        }
    }

    /**
     * Labels are spaced evenly in the available space in an attempt to
     * eliminate the overlaps.
     *
     * @param minY  the minimum y value (in Java2D coordinate space).
     * @param height  the height available for all labels.
     */
    protected void spreadEvenly(double minY, double height) {
        double y = minY;
        double sumOfLabelHeights = 0.0;
        for (int i = 0; i < this.labels.size(); i++) {
            sumOfLabelHeights += getPieLabelRecord(i).getLabelHeight();
        }
        double gap = height - sumOfLabelHeights;
        if (this.labels.size() > 1) {
            gap = gap / (this.labels.size() - 1);
        }
        for (int i = 0; i < this.labels.size(); i++) {
            PieLabelRecord record = getPieLabelRecord(i);
            y = y + record.getLabelHeight() / 2.0;
            record.setAllocatedY(y);
            y = y + record.getLabelHeight() / 2.0 + gap;
        }
    }

    /**
     * Sorts the label records into ascending order by y-value.
     */
    public void sort() {
        Collections.sort(this.labels);
    }

    /**
     * Returns a string containing a description of the object for
     * debugging purposes.
     *
     * @return A string.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < this.labels.size(); i++) {
            result.append(getPieLabelRecord(i).toString()).append("\n");
        }
        return result.toString();
    }

}
