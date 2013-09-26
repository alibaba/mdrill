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
 * ----------------
 * GreyPalette.java
 * ----------------
 * (C) Copyright 2002-2008, by David M. O'Donnell and Contributors.
 *
 * Original Author:  David M. O'Donnell;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 26-Nov-2002 : Version 1 contributed by David M. O'Donnell (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 31-Jan-2007 : Deprecated (DG);
 *
 */

package org.jfree.chart.plot;

import java.io.Serializable;

import org.jfree.chart.renderer.xy.XYBlockRenderer;


/**
 * A grey color palette.
 *
 * @deprecated This class is no longer supported (as of version 1.0.4).  If
 *     you are creating contour plots, please try to use {@link XYPlot} and
 *     {@link XYBlockRenderer}.
 */
public class GreyPalette extends ColorPalette implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -2120941170159987395L;

    /**
     * Creates a new palette.
     */
    public GreyPalette() {
        super();
        initialize();
    }

    /**
     * Intializes the palette's indices.
     */
    public void initialize() {

        setPaletteName("Grey");

        this.r = new int[256];
        this.g = new int[256];
        this.b = new int[256];

        this.r[0] = 255;
        this.g[0] = 255;
        this.b[0] = 255;
        this.r[1] = 0;
        this.g[1] = 0;
        this.b[1] = 0;

        for (int i = 2; i < 256; i++) {
            this.r[i] = i;
            this.g[i] = i;
            this.b[i] = i;
        }

    }

}
