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
 * DefaultCategoryItemRenderer.java
 * --------------------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 29-Apr-2003 : Version 1 (DG);
 *
 */

package org.jfree.chart.renderer.category;

import java.io.Serializable;

import org.jfree.chart.plot.CategoryPlot;

/**
 * A default renderer for the {@link CategoryPlot} class.  This is simply an
 * alias for the {@link LineAndShapeRenderer} class.
 */
public class DefaultCategoryItemRenderer extends LineAndShapeRenderer
                                         implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -7793786349384231896L;

    // no new methods

}
