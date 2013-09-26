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
 * -----------------
 * ColorPalette.java
 * -----------------
 * (C) Copyright 2002-2008, by David M. O'Donnell and Contributors.
 *
 * Original Author:  David M. O'Donnell;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 26-Nov-2002 : Version 1 contributed by David M. O'Donnell (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 14-Aug-2003 : Implemented Cloneable (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 31-Jan-2007 : Deprecated (DG);
 *
 */

package org.jfree.chart.plot;

import java.awt.Color;
import java.awt.Paint;
import java.io.Serializable;
import java.util.Arrays;

import org.jfree.chart.axis.ValueTick;
import org.jfree.chart.renderer.xy.XYBlockRenderer;

/**
 * Defines palette used by {@link ContourPlot}.
 *
 * @deprecated This class is no longer supported (as of version 1.0.4).  If
 *     you are creating contour plots, please try to use {@link XYPlot} and
 *     {@link XYBlockRenderer}.
 */
public abstract class ColorPalette implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -9029901853079622051L;

    /** The min z-axis value. */
    protected double minZ = -1;

    /** The max z-axis value. */
    protected double maxZ = -1;

    /** Red components. */
    protected int[] r;

    /** Green components. */
    protected int[] g;

    /** Blue components. */
    protected int[] b;

    /** Tick values are stored for use with stepped palette. */
    protected double[] tickValues = null;

    /** Logscale? */
    protected boolean logscale = false;

    /** Inverse palette (ie, min and max colors are reversed). */
    protected boolean inverse = false;

    /** The palette name. */
    protected String paletteName = null;

    /** Controls whether palette colors are stepped (not continuous). */
    protected boolean stepped = false;

    /** Constant for converting loge to log10. */
    protected static final double log10 = Math.log(10);

    /**
     * Default contructor.
     */
    public ColorPalette() {
        super();
    }

    /**
     * Returns the color associated with a value.
     *
     * @param value  the value.
     *
     * @return The color.
     */
    public Paint getColor(double value) {
        int izV = (int) (253 * (value - this.minZ)
                    / (this.maxZ - this.minZ)) + 2;
        return new Color(this.r[izV], this.g[izV], this.b[izV]);
    }

    /**
     * Returns a color.
     *
     * @param izV  the index into the palette (zero based).
     *
     * @return The color.
     */
    public Color getColor(int izV) {
        return new Color(this.r[izV], this.g[izV], this.b[izV]);
    }

    /**
     * Returns Color by mapping a given value to a linear palette.
     *
     * @param value  the value.
     *
     * @return The color.
     */
    public Color getColorLinear(double value) {
        int izV = 0;
        if (this.stepped) {
            int index = Arrays.binarySearch(this.tickValues, value);
            if (index < 0) {
                index = -1 * index - 2;
            }

            if (index < 0) { // For the case were the first tick is greater
                             // than minZ
                value = this.minZ;
            }
            else {
                value = this.tickValues[index];
            }
        }
        izV = (int) (253 * (value - this.minZ) / (this.maxZ - this.minZ)) + 2;
        izV = Math.min(izV, 255);
        izV = Math.max(izV, 2);
        return getColor(izV);
    }

    /**
     * Returns Color by mapping a given value to a common log palette.
     *
     * @param value  the value.
     *
     * @return The color.
     */
    public Color getColorLog(double value) {
        int izV = 0;
        double minZtmp = this.minZ;
        double maxZtmp = this.maxZ;
        if (this.minZ <= 0.0) {
//          negatives = true;
            this.maxZ = maxZtmp - minZtmp + 1;
            this.minZ = 1;
            value = value - minZtmp + 1;
        }
        double minZlog = Math.log(this.minZ) / log10;
        double maxZlog = Math.log(this.maxZ) / log10;
        value = Math.log(value) / log10;
        //  value = Math.pow(10,value);
        if (this.stepped) {
            int numSteps = this.tickValues.length;
            int steps = 256 / (numSteps - 1);
            izV = steps * (int) (numSteps * (value - minZlog)
                    / (maxZlog - minZlog)) + 2;
            //  izV = steps*numSteps*(int)((value/minZ)/(maxZlog-minZlog)) + 2;
        }
        else {
            izV = (int) (253 * (value - minZlog) / (maxZlog - minZlog)) + 2;
        }
        izV = Math.min(izV, 255);
        izV = Math.max(izV, 2);

        this.minZ = minZtmp;
        this.maxZ = maxZtmp;

        return getColor(izV);
    }

    /**
     * Returns the maximum Z value.
     *
     * @return The value.
     */
    public double getMaxZ() {
        return this.maxZ;
    }

    /**
     * Returns the minimum Z value.
     *
     * @return The value.
     */
    public double getMinZ() {
        return this.minZ;
    }

    /**
     * Returns Paint by mapping a given value to a either a linear or common
     * log palette as controlled by the value logscale.
     *
     * @param value  the value.
     *
     * @return The paint.
     */
    public Paint getPaint(double value) {
        if (isLogscale()) {
            return getColorLog(value);
        }
        else {
            return getColorLinear(value);
        }
    }

    /**
     * Returns the palette name.
     *
     * @return The palette name.
     */
    public String getPaletteName () {
        return this.paletteName;
    }

    /**
     * Returns the tick values.
     *
     * @return The tick values.
     */
    public double[] getTickValues() {
        return this.tickValues;
    }

    /**
     * Called to initialize the palette's color indexes
     */
    public abstract void initialize();

    /**
     * Inverts Palette
     */
    public void invertPalette() {

        int[] red = new int[256];
        int[] green = new int[256];
        int[] blue = new int[256];
        for (int i = 0; i < 256; i++) {
            red[i] = this.r[i];
            green[i] = this.g[i];
            blue[i] = this.b[i];
        }

        for (int i = 2; i < 256; i++) {
            this.r[i] = red[257 - i];
            this.g[i] = green[257 - i];
            this.b[i] = blue[257 - i];
        }
    }

    /**
     * Returns the inverse flag.
     *
     * @return The flag.
     */
    public boolean isInverse () {
        return this.inverse;
    }

    /**
     * Returns the log-scale flag.
     *
     * @return The flag.
     */
    public boolean isLogscale() {
        return this.logscale;
    }

    /**
     * Returns the 'is-stepped' flag.
     *
     * @return The flag.
     */
    public boolean isStepped () {
        return this.stepped;
    }

    /**
     * Sets the inverse flag.
     *
     * @param inverse  the new value.
     */
    public void setInverse (boolean inverse) {
        this.inverse = inverse;
        initialize();
        if (inverse) {
            invertPalette();
        }
        return;
    }

    /**
     * Sets the 'log-scale' flag.
     *
     * @param logscale  the new value.
     */
    public void setLogscale(boolean logscale) {
        this.logscale = logscale;
    }

    /**
     * Sets the maximum Z value.
     *
     * @param newMaxZ  the new value.
     */
    public void setMaxZ(double newMaxZ) {
        this.maxZ = newMaxZ;
    }

    /**
     * Sets the minimum Z value.
     *
     * @param newMinZ  the new value.
     */
    public void setMinZ(double newMinZ) {
        this.minZ = newMinZ;
    }

    /**
     * Sets the palette name.
     *
     * @param paletteName  the name.
     */
    public void setPaletteName (String paletteName) {
        //String oldValue = this.paletteName;
        this.paletteName = paletteName;
        return;
    }

    /**
     * Sets the stepped flag.
     *
     * @param stepped  the flag.
     */
    public void setStepped (boolean stepped) {
        this.stepped = stepped;
        return;
    }

    /**
     * Sets the tick values.
     *
     * @param newTickValues  the tick values.
     */
    public void setTickValues(double[] newTickValues) {
        this.tickValues = newTickValues;
    }

    /**
     * Store ticks. Required when doing stepped axis
     *
     * @param ticks  the ticks.
     */
    public void setTickValues(java.util.List ticks) {
        this.tickValues = new double[ticks.size()];
        for (int i = 0; i < this.tickValues.length; i++) {
            this.tickValues[i] = ((ValueTick) ticks.get(i)).getValue();
        }
    }

    /**
     * Tests an object for equality with this instance.
     *
     * @param o  the object to test.
     *
     * @return A boolean.
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ColorPalette)) {
            return false;
        }

        ColorPalette colorPalette = (ColorPalette) o;

        if (this.inverse != colorPalette.inverse) {
            return false;
        }
        if (this.logscale != colorPalette.logscale) {
            return false;
        }
        if (this.maxZ != colorPalette.maxZ) {
            return false;
        }
        if (this.minZ != colorPalette.minZ) {
            return false;
        }
        if (this.stepped != colorPalette.stepped) {
            return false;
        }
        if (!Arrays.equals(this.b, colorPalette.b)) {
            return false;
        }
        if (!Arrays.equals(this.g, colorPalette.g)) {
            return false;
        }
        if (this.paletteName != null
                ? !this.paletteName.equals(colorPalette.paletteName)
                : colorPalette.paletteName != null) {
            return false;
        }
        if (!Arrays.equals(this.r, colorPalette.r)) {
            return false;
        }
        if (!Arrays.equals(this.tickValues, colorPalette.tickValues)) {
            return false;
        }

        return true;
    }

    /**
     * Returns a hash code.
     *
     * @return A hash code.
     */
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(this.minZ);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.maxZ);
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        result = 29 * result + (this.logscale ? 1 : 0);
        result = 29 * result + (this.inverse ? 1 : 0);
        result = 29 * result
                 + (this.paletteName != null ? this.paletteName.hashCode() : 0);
        result = 29 * result + (this.stepped ? 1 : 0);
        return result;
    }

    /**
     * Returns a clone of the palette.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException never.
     */
    public Object clone() throws CloneNotSupportedException {

        ColorPalette clone = (ColorPalette) super.clone();
        return clone;

    }

}
