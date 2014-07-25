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
 * -----------------------
 * CombinationDataset.java
 * -----------------------
 * (C) Copyright 2001-2009, by Bill Kelemen.
 *
 * Original Author:  Bill Kelemen;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 06-Dec-2001 : Version 1 (BK);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags from all over JFreeChart sources (DG);
 *
 */

package org.jfree.data.general;

/**
 * Interface that describes the new methods that any combined dataset needs to
 * implement. A combined dataset object will combine one or more datasets and
 * expose a sub-set or union of the combined datasets.
 *
 * @deprecated 1.0.13
 */
public interface CombinationDataset {

    ///////////////////////////////////////////////////////////////////////////
    // New methods from CombinationDataset
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the parent Dataset of this combination. If there is more than
     * one parent, or a child is found that is not a combination, then returns
     * <code>null</code>.
     *
     * @return The parent Dataset of this combination.
     */
    public SeriesDataset getParent();

    /**
     * Returns a map or indirect indexing form our series into parent's series.
     *
     * @return A map or indirect indexing form our series into parent's series.
     */
    public int[] getMap();

}
