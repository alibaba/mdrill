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
 * MarkerAxisBand.java
 * -------------------
 * (C) Copyright 2000-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 03-Sep-2002 : Updated Javadoc comments (DG);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 08-Nov-2002 : Moved to new package com.jrefinery.chart.axis (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 13-May-2003 : Renamed HorizontalMarkerAxisBand --> MarkerAxisBand (DG);
 * 29-Oct-2003 : Added workaround for font alignment in PDF output (DG);
 * 21-Jan-2004 : Update for renamed method in ValueAxis (DG);
 * 07-Apr-2004 : Changed text bounds calculation (DG);
 *
 */

package org.jfree.chart.axis;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.plot.IntervalMarker;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ObjectUtilities;

/**
 * A band that can be added to a number axis to display regions.
 */
public class MarkerAxisBand implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -1729482413886398919L;

    /** The axis that the band belongs to. */
    private NumberAxis axis;

    /** The top outer gap. */
    private double topOuterGap;

    /** The top inner gap. */
    private double topInnerGap;

    /** The bottom outer gap. */
    private double bottomOuterGap;

    /** The bottom inner gap. */
    private double bottomInnerGap;

    /** The font. */
    private Font font;

    /** Storage for the markers. */
    private List markers;

    /**
     * Constructs a new axis band.
     *
     * @param axis  the owner.
     * @param topOuterGap  the top outer gap.
     * @param topInnerGap  the top inner gap.
     * @param bottomOuterGap  the bottom outer gap.
     * @param bottomInnerGap  the bottom inner gap.
     * @param font  the font.
     */
    public MarkerAxisBand(NumberAxis axis,
                          double topOuterGap, double topInnerGap,
                          double bottomOuterGap, double bottomInnerGap,
                          Font font) {
        this.axis = axis;
        this.topOuterGap = topOuterGap;
        this.topInnerGap = topInnerGap;
        this.bottomOuterGap = bottomOuterGap;
        this.bottomInnerGap = bottomInnerGap;
        this.font = font;
        this.markers = new java.util.ArrayList();
    }

    /**
     * Adds a marker to the band.
     *
     * @param marker  the marker.
     */
    public void addMarker(IntervalMarker marker) {
        this.markers.add(marker);
    }

    /**
     * Returns the height of the band.
     *
     * @param g2  the graphics device.
     *
     * @return The height of the band.
     */
    public double getHeight(Graphics2D g2) {

        double result = 0.0;
        if (this.markers.size() > 0) {
            LineMetrics metrics = this.font.getLineMetrics(
                "123g", g2.getFontRenderContext()
            );
            result = this.topOuterGap + this.topInnerGap + metrics.getHeight()
                     + this.bottomInnerGap + this.bottomOuterGap;
        }
        return result;

    }

    /**
     * A utility method that draws a string inside a rectangle.
     *
     * @param g2  the graphics device.
     * @param bounds  the rectangle.
     * @param font  the font.
     * @param text  the text.
     */
    private void drawStringInRect(Graphics2D g2, Rectangle2D bounds, Font font,
                                  String text) {

        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics(font);
        Rectangle2D r = TextUtilities.getTextBounds(text, g2, fm);
        double x = bounds.getX();
        if (r.getWidth() < bounds.getWidth()) {
            x = x + (bounds.getWidth() - r.getWidth()) / 2;
        }
        LineMetrics metrics = font.getLineMetrics(
            text, g2.getFontRenderContext()
        );
        g2.drawString(
            text, (float) x, (float) (bounds.getMaxY()
                - this.bottomInnerGap - metrics.getDescent())
        );
    }

    /**
     * Draws the band.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param dataArea  the data area.
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     */
    public void draw(Graphics2D g2, Rectangle2D plotArea, Rectangle2D dataArea,
                     double x, double y) {

        double h = getHeight(g2);
        Iterator iterator = this.markers.iterator();
        while (iterator.hasNext()) {
            IntervalMarker marker = (IntervalMarker) iterator.next();
            double start =  Math.max(
                marker.getStartValue(), this.axis.getRange().getLowerBound()
            );
            double end = Math.min(
                marker.getEndValue(), this.axis.getRange().getUpperBound()
            );
            double s = this.axis.valueToJava2D(
                start, dataArea, RectangleEdge.BOTTOM
            );
            double e = this.axis.valueToJava2D(
                end, dataArea, RectangleEdge.BOTTOM
            );
            Rectangle2D r = new Rectangle2D.Double(
                s, y + this.topOuterGap, e - s,
                h - this.topOuterGap - this.bottomOuterGap
            );

            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, marker.getAlpha())
            );
            g2.setPaint(marker.getPaint());
            g2.fill(r);
            g2.setPaint(marker.getOutlinePaint());
            g2.draw(r);
            g2.setComposite(originalComposite);

            g2.setPaint(Color.black);
            drawStringInRect(g2, r, this.font, marker.getLabel());
        }

    }

    /**
     * Tests this axis for equality with another object.  Note that the axis
     * that the band belongs to is ignored in the test.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MarkerAxisBand)) {
            return false;
        }
        MarkerAxisBand that = (MarkerAxisBand) obj;
        if (this.topOuterGap != that.topOuterGap) {
            return false;
        }
        if (this.topInnerGap != that.topInnerGap) {
            return false;
        }
        if (this.bottomInnerGap != that.bottomInnerGap) {
            return false;
        }
        if (this.bottomOuterGap != that.bottomOuterGap) {
            return false;
        }
        if (!ObjectUtilities.equal(this.font, that.font)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.markers, that.markers)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code for the object.
     *
     * @return A hash code.
     */
    public int hashCode() {
        int result = 37;
        result = 19 * result + this.font.hashCode();
        result = 19 * result + this.markers.hashCode();
        return result;
    }

}
