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
 * -------------------
 * RainbowPalette.java
 * -------------------
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
 * Contains the data to construct an 8-bit rainbow palette
 * This come from an old application which ran on 8-bit graphics device.
 * Thus indexes 0 and 1 were preserved for rendering white and black
 * respectively.
 *
 * @deprecated This class is no longer supported (as of version 1.0.4).  If
 *     you are creating contour plots, please try to use {@link XYPlot} and
 *     {@link XYBlockRenderer}.
 */
public class RainbowPalette extends ColorPalette implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -1906707320728242478L;

    /** The red values. */
    private int[] red = {255,   0, 120, 115, 111, 106, 102,  97,
                          93,  88,  84,  79,  75,  70,  66,  61,
                          57,  52,  48,  43,  39,  34,  30,  25,
                          21,  16,  12,   7,   3,   0,   0,   0,
                           0,   0,   0,   0,   0,   0,   0,   0,
                           0,   0,   0,   0,   0,   0,   0,   0,
                           0,   0,   0,   0,   0,   0,   0,   0,
                           0,   0,   0,   0,   0,   0,   0,   0,
                           0,   0,   0,   0,   0,   0,   0,   0,
                           0,   0,   0,   0,   0,   0,   0,   0,
                           0,   0,   0,   0,   0,   0,   0,   0,
                           0,   0,   0,   0,   0,   0,   0,   0,
                           0,   0,   0,   0,   0,   0,   0,   0,
                           0,   0,   0,   0,   0,   0,   0,   0,
                           0,   0,   0,   0,   0,   0,   0,   0,
                           0,   0,   0,   0,   0,   0,   0,   0,
                           0,   0,   0,   0,   0,   0,   0,   0,
                           0,   0,   0,   0,   0,   0,   1,   5,
                          10,  14,  19,  23,  28,  32,  37,  41,
                          46,  50,  55,  59,  64,  68,  73,  77,
                          82,  86,  91,  95, 100, 104, 109, 113,
                         118, 123, 127, 132, 136, 141, 145, 150,
                         154, 159, 163, 168, 172, 177, 181, 186,
                         190, 195, 199, 204, 208, 213, 217, 222,
                         226, 231, 235, 240, 244, 249, 253, 255,
                         255, 255, 255, 255, 255, 255, 255, 255,
                         255, 255, 255, 255, 255, 255, 255, 255,
                         255, 255, 255, 255, 255, 255, 255, 255,
                         255, 255, 255, 255, 255, 255, 255, 255,
                         255, 255, 255, 255, 255, 255, 255, 255,
                         255, 255, 255, 255, 255, 255, 255, 255,
                         255, 255, 255, 255, 255, 255, 255, 255};

    /** The green values. */
    private int[] green = {255,   0,   0,   0,   0,   0,   0,   0,
                             0,   0,   0,   0,   0,   0,   0,   0,
                             0,   0,   0,   0,   0,   0,   0,   0,
                             0,   0,   0,   0,   0,   2,   6,  11,
                            15,  20,  24,  29,  33,  38,  42,  47,
                            51,  56,  60,  65,  69,  74,  78,  83,
                            87,  92,  96, 101, 105, 110, 114, 119,
                           123, 128, 132, 137, 141, 146, 150, 155,
                           159, 164, 168, 173, 177, 182, 186, 191,
                           195, 200, 204, 209, 213, 218, 222, 227,
                           231, 236, 241, 245, 250, 254, 255, 255,
                           255, 255, 255, 255, 255, 255, 255, 255,
                           255, 255, 255, 255, 255, 255, 255, 255,
                           255, 255, 255, 255, 255, 255, 255, 255,
                           255, 255, 255, 255, 255, 255, 255, 255,
                           255, 255, 255, 255, 255, 255, 255, 255,
                           255, 255, 255, 255, 255, 255, 255, 255,
                           255, 255, 255, 255, 255, 255, 255, 255,
                           255, 255, 255, 255, 255, 255, 255, 255,
                           255, 255, 255, 255, 255, 255, 255, 255,
                           255, 255, 255, 255, 255, 255, 255, 255,
                           255, 255, 255, 255, 255, 255, 255, 255,
                           255, 255, 255, 255, 255, 255, 255, 255,
                           255, 255, 255, 255, 255, 255, 255, 255,
                           255, 255, 255, 255, 255, 255, 255, 252,
                           248, 243, 239, 234, 230, 225, 221, 216,
                           212, 207, 203, 198, 194, 189, 185, 180,
                           176, 171, 167, 162, 158, 153, 149, 144,
                           140, 135, 131, 126, 122, 117, 113, 108,
                           104,  99,  95,  90,  86,  81,  77,  72,
                            68,  63,  59,  54,  50,  45,  41,  36,
                            32,  27,  23,  18,  14,   9,   5,   0};

    /** The blue values. */
    private int[] blue = {255,   0, 255, 255, 255, 255, 255, 255,
                          255, 255, 255, 255, 255, 255, 255, 255,
                          255, 255, 255, 255, 255, 255, 255, 255,
                          255, 255, 255, 255, 255, 255, 255, 255,
                          255, 255, 255, 255, 255, 255, 255, 255,
                          255, 255, 255, 255, 255, 255, 255, 255,
                          255, 255, 255, 255, 255, 255, 255, 255,
                          255, 255, 255, 255, 255, 255, 255, 255,
                          255, 255, 255, 255, 255, 255, 255, 255,
                          255, 255, 255, 255, 255, 255, 255, 255,
                          255, 255, 255, 255, 255, 255, 251, 247,
                          242, 238, 233, 229, 224, 220, 215, 211,
                          206, 202, 197, 193, 188, 184, 179, 175,
                          170, 166, 161, 157, 152, 148, 143, 139,
                          134, 130, 125, 121, 116, 112, 107, 103,
                          98,  94,  89,  85,  80,  76,  71,  67,
                           62,  58,  53,  49,  44,  40,  35,  31,
                           26,  22,  17,  13,   8,   4,   0,   0,
                            0,   0,   0,   0,   0,   0,   0,   0,
                            0,   0,   0,   0,   0,   0,   0,   0,
                            0,   0,   0,   0,   0,   0,   0,   0,
                            0,   0,   0,   0,   0,   0,   0,   0,
                            0,   0,   0,   0,   0,   0,   0,   0,
                            0,   0,   0,   0,   0,   0,   0,   0,
                            0,   0,   0,   0,   0,   0,   0,   0,
                            0,   0,   0,   0,   0,   0,   0,   0,
                            0,   0,   0,   0,   0,   0,   0,   0,
                            0,   0,   0,   0,   0,   0,   0,   0,
                            0,   0,   0,   0,   0,   0,   0,   0,
                            0,   0,   0,   0,   0,   0,   0,   0,
                            0,   0,   0,   0,   0,   0,   0,   0,
                            0,   0,   0,   0,   0,   0,   0,   0};

    /**
     * Default constructor.
     */
    public RainbowPalette() {
        super();
        initialize();
    }

    /**
     * Intializes the palettes indexes
     */
    public void initialize() {

        setPaletteName("Rainbow");

        this.r = new int[this.red.length];
        this.g = new int[this.green.length];
        this.b = new int[this.blue.length];
        System.arraycopy(this.red, 0, this.r, 0, this.red.length);
        System.arraycopy(this.green, 0, this.g, 0, this.green.length);
        System.arraycopy(this.blue, 0, this.b, 0, this.blue.length);

    }

}
