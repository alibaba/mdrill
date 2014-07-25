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
 * -------------------------
 * CategoryURLGenerator.java
 * -------------------------
 * (C) Copyright 2002-2008, by Richard Atkinson and Contributors.
 *
 * Original Author:  Richard Atkinson;
 * Contributors:     David Gilbert (for Object Refinery Limited);
 *
 * Changes:
 * --------
 * 05-Aug-2002 : Version 1, contributed by Richard Atkinson;
 * 09-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 10-Apr-2003 : Replaced reference to CategoryDataset with
 *               KeyedValues2DDataset (DG);
 * 23-Apr-2003 : Switched around CategoryDataset and KeyedValues2DDataset
 *               (again) (DG);
 * 13-Aug-2003 : Added clone() method (DG);
 * 14-Jun-2004 : Removed clone() method - classes that implement the interface
 *               should implement the PublicCloneable interface instead,
 *               wherever possible (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags from all over JFreeChart sources (DG);
 * 13-Dec-2007 : Updated API docs (DG);
 *
 */

package org.jfree.chart.urls;

import org.jfree.data.category.CategoryDataset;

/**
 * A URL generator for items in a {@link CategoryDataset}.
 */
public interface CategoryURLGenerator {

    /**
     * Returns a URL for one item in a dataset. As a guideline, the URL
     * should be valid within the context of an XHTML 1.0 document.  Classes
     * that implement this interface are responsible for correctly escaping
     * any text that is derived from the dataset, as this may be user-specified
     * and could pose a security risk.
     *
     * @param dataset  the dataset.
     * @param series  the series (zero-based index).
     * @param category  the category.
     *
     * @return A string containing the URL.
     */
    public String generateURL(CategoryDataset dataset, int series,
            int category);

}
