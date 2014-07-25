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
 * --------------------
 * PieURLGenerator.java
 * --------------------
 * (C) Copyright 2002-2008, by Richard Atkinson and Contributors.
 *
 * Original Author:  Richard Atkinson;
 * Contributors:     David Gilbert (for Object Refinery Limited);
 *
 * Changes:
 * --------
 * 05-Aug-2002 : Version 1, contributed by Richard Atkinson;
 * 09-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 07-Mar-2003 : Modified to use KeyedValuesDataset and added pieIndex
 *               parameter (DG);
 * 24-Apr-2003 : Switched around PieDataset and KeyedValuesDataset (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 24-Nov-2006 : Updated API docs (DG);
 * 13-Dec-2007 : Updated API docs (DG);
 *
 */

package org.jfree.chart.urls;

import org.jfree.data.general.PieDataset;

/**
 * Interface for a URL generator for plots that use data from a
 * {@link PieDataset}.  Classes that implement this interface:
 * <ul>
 * <li>are responsible for correctly escaping any text that is derived from the
 *     dataset, as this may be user-specified and could pose a security
 *     risk;</li>
 * <li>should be either (a) immutable, or (b) cloneable via the
 *     <code>PublicCloneable</code> interface (defined in the JCommon class
 *     library).  This provides a mechanism for the referring plot to clone
 *     the generator if necessary.</li>
 * </ul>
 */
public interface PieURLGenerator {

    /**
     * Generates a URL for one item in a {@link PieDataset}. As a guideline,
     * the URL should be valid within the context of an XHTML 1.0 document.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param key  the item key (<code>null</code> not permitted).
     * @param pieIndex  the pie index (differentiates between pies in a
     *                  'multi' pie chart).
     *
     * @return A string containing the URL.
     */
    public String generateURL(PieDataset dataset, Comparable key, int pieIndex);

}
