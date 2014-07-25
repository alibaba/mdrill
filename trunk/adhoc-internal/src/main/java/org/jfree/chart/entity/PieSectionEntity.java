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
 * ---------------------
 * PieSectionEntity.java
 * ---------------------
 * (C) Copyright 2002-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Richard Atkinson;
 *                   Christian W. Zuckschwerdt;
 *
 * Changes:
 * --------
 * 23-May-2002 : Version 1 (DG);
 * 12-Jun-2002 : Added Javadoc comments (DG);
 * 26-Jun-2002 : Added method to generate AREA tag for image map
 *               generation (DG);
 * 05-Aug-2002 : Added new constructor to populate URLText
 *               Moved getImageMapAreaTag() to ChartEntity (superclass) (RA);
 * 03-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 07-Mar-2003 : Added pie index attribute, since the PiePlot class can create
 *               multiple pie plots within one chart.  Also renamed 'category'
 *               --> 'sectionKey' and changed the class from Object -->
 *               Comparable (DG);
 * 30-Jul-2003 : Added PieDataset reference (CZ);
 * 11-Jan-2005 : Removed deprecated code in preparation for 1.0.0 release (DG);
 * 13-Nov-2007 : Implemented equals() and hashCode() (DG);
 *
 */

package org.jfree.chart.entity;

import java.awt.Shape;
import java.io.Serializable;

import org.jfree.chart.HashUtilities;
import org.jfree.data.general.PieDataset;
import org.jfree.util.ObjectUtilities;

/**
 * A chart entity that represents one section within a pie plot.
 */
public class PieSectionEntity extends ChartEntity
                              implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 9199892576531984162L;

    /** The dataset. */
    private PieDataset dataset;

    /** The pie index. */
    private int pieIndex;

    /** The section index. */
    private int sectionIndex;

    /** The section key. */
    private Comparable sectionKey;

    /**
     * Creates a new pie section entity.
     *
     * @param area  the area.
     * @param dataset  the pie dataset.
     * @param pieIndex  the pie index (zero-based).
     * @param sectionIndex  the section index (zero-based).
     * @param sectionKey  the section key.
     * @param toolTipText  the tool tip text.
     * @param urlText  the URL text for HTML image maps.
     */
    public PieSectionEntity(Shape area,
                            PieDataset dataset,
                            int pieIndex, int sectionIndex,
                            Comparable sectionKey,
                            String toolTipText, String urlText) {

        super(area, toolTipText, urlText);
        this.dataset = dataset;
        this.pieIndex = pieIndex;
        this.sectionIndex = sectionIndex;
        this.sectionKey = sectionKey;

    }

    /**
     * Returns the dataset this entity refers to.
     *
     * @return The dataset.
     *
     * @see #setDataset(PieDataset)
     */
    public PieDataset getDataset() {
        return this.dataset;
    }

    /**
     * Sets the dataset this entity refers to.
     *
     * @param dataset  the dataset.
     *
     * @see #getDataset()
     */
    public void setDataset(PieDataset dataset) {
        this.dataset = dataset;
    }

    /**
     * Returns the pie index.  For a regular pie chart, the section index is 0.
     * For a pie chart containing multiple pie plots, the pie index is the row
     * or column index from which the pie data is extracted.
     *
     * @return The pie index.
     *
     * @see #setPieIndex(int)
     */
    public int getPieIndex() {
        return this.pieIndex;
    }

    /**
     * Sets the pie index.
     *
     * @param index  the new index value.
     *
     * @see #getPieIndex()
     */
    public void setPieIndex(int index) {
        this.pieIndex = index;
    }

    /**
     * Returns the section index.
     *
     * @return The section index.
     *
     * @see #setSectionIndex(int)
     */
    public int getSectionIndex() {
        return this.sectionIndex;
    }

    /**
     * Sets the section index.
     *
     * @param index  the section index.
     *
     * @see #getSectionIndex()
     */
    public void setSectionIndex(int index) {
        this.sectionIndex = index;
    }

    /**
     * Returns the section key.
     *
     * @return The section key.
     *
     * @see #setSectionKey(Comparable)
     */
    public Comparable getSectionKey() {
        return this.sectionKey;
    }

    /**
     * Sets the section key.
     *
     * @param key  the section key.
     *
     * @see #getSectionKey()
     */
    public void setSectionKey(Comparable key) {
        this.sectionKey = key;
    }

    /**
     * Tests this entity for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PieSectionEntity)) {
            return false;
        }
        PieSectionEntity that = (PieSectionEntity) obj;
        if (!ObjectUtilities.equal(this.dataset, that.dataset)) {
            return false;
        }
        if (this.pieIndex != that.pieIndex) {
            return false;
        }
        if (this.sectionIndex != that.sectionIndex) {
            return false;
        }
        if (!ObjectUtilities.equal(this.sectionKey, that.sectionKey)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code.
     */
    public int hashCode() {
        int result = super.hashCode();
        result = HashUtilities.hashCode(result, this.pieIndex);
        result = HashUtilities.hashCode(result, this.sectionIndex);
        return result;
    }

    /**
     * Returns a string representing the entity.
     *
     * @return A string representing the entity.
     */
    public String toString() {
        return "PieSection: " + this.pieIndex + ", " + this.sectionIndex + "("
                              + this.sectionKey.toString() + ")";
    }

}
