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
 * XYZURLGenerator.java
 * --------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributors:     -;
 *
 * Changes:
 * --------
 * 03-Feb-2003 : Version 1 (DG);
 * 13-Dec-2007 : Updated API docs (DG);
 *
 */

package org.jfree.chart.urls;

import org.jfree.data.xy.XYZDataset;

/**
 * Interface for a URL generator for plots that uses data from an
 * {@link XYZDataset}.  Classes that implement this interface are responsible
 * for correctly escaping any text that is derived from the dataset, as this
 * may be user-specified and could pose a security risk.
 */
public interface XYZURLGenerator extends XYURLGenerator {

    /**
     * Generates a URL for a particular item within a series. As a guideline,
     * the URL should be valid within the context of an XHTML 1.0 document.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return A string containing the generated URL.
     */
    public String generateURL(XYZDataset dataset, int series, int item);

}
