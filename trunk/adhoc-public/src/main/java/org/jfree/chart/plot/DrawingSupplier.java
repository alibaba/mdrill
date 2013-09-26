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
 * DrawingSupplier.java
 * --------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 16-Jan-2003 : Version 1 (DG);
 * 17-Jan-2003 : Renamed PaintSupplier --> DrawingSupplier (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 13-Jun-2007 : Added getNextOutlinePaint() method.
 *
 */

package org.jfree.chart.plot;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

/**
 * A supplier of <code>Paint</code>, <code>Stroke</code> and <code>Shape</code>
 * objects for use by plots and renderers.  By providing a central place for
 * obtaining these items, we can ensure that duplication is avoided.
 * <p>
 * To support the cloning of charts, classes that implement this interface
 * should also implement <code>PublicCloneable</code>.
 */
public interface DrawingSupplier {

    /**
     * Returns the next paint in a sequence maintained by the supplier.
     *
     * @return The paint.
     */
    public Paint getNextPaint();

    /**
     * Returns the next outline paint in a sequence maintained by the supplier.
     *
     * @return The paint.
     */
    public Paint getNextOutlinePaint();

    /**
     * Returns the next fill paint in a sequence maintained by the supplier.
     *
     * @return The paint.
     *
     * @since 1.0.6
     */
    public Paint getNextFillPaint();

    /**
     * Returns the next <code>Stroke</code> object in a sequence maintained by
     * the supplier.
     *
     * @return The stroke.
     */
    public Stroke getNextStroke();

    /**
     * Returns the next <code>Stroke</code> object in a sequence maintained by
     * the supplier.
     *
     * @return The stroke.
     */
    public Stroke getNextOutlineStroke();

    /**
     * Returns the next <code>Shape</code> object in a sequence maintained by
     * the supplier.
     *
     * @return The shape.
     */
    public Shape getNextShape();

}
