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
 * ----------------------------
 * URLTagFragmentGenerator.java
 * ----------------------------
 * (C) Copyright 2003-2008, by Richard Atkinson and Contributors.
 *
 * Original Author:  Richard Atkinson;
 *
 * Changes
 * -------
 * 12-Aug-2003 : Version 1 (RA);
 * 13-Dec-2007 : Updated API docs (DG);
 *
 */

package org.jfree.chart.imagemap;

import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.urls.XYZURLGenerator;

/**
 * Interface for generating the URL fragment of an HTML image map area tag.
 */
public interface URLTagFragmentGenerator {

    /**
     * Generates a URL string to go in an HTML image map.
     * <br><br>
     * Note that the <code>urlText</code> will be created by a URL generator
     * (such as {@link CategoryURLGenerator}, {@link PieURLGenerator},
     * {@link XYURLGenerator} or {@link XYZURLGenerator}) and that generator is
     * responsible for ensuring that the URL text is correctly escaped.
     *
     * @param urlText  the URL text (fully escaped).
     *
     * @return The formatted HTML area tag attribute(s).
     */
    public String generateURLFragment(String urlText);

}
