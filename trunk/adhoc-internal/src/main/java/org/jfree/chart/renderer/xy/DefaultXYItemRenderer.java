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
 * DefaultXYItemRenderer.java
 * --------------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 23-Jul-2003 : Version 1 (DG);
 * 22-Feb-2005 : Now extends XYLineAndShapeRenderer (DG);
 *
 */

package org.jfree.chart.renderer.xy;

import java.io.Serializable;

/**
 * A default renderer for the {@link org.jfree.chart.plot.XYPlot} class.  This
 * is an alias for the {@link XYLineAndShapeRenderer} class.
 */
public class DefaultXYItemRenderer extends XYLineAndShapeRenderer
                                   implements Serializable {

    /** For serialization. */
    static final long serialVersionUID = 3450423530996888074L;

    // no new methods

}
