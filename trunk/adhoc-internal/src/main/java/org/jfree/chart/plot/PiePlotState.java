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
 * PiePlotState.java
 * -----------------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 06-Mar-2004 : Version 1 (DG);
 *
 */

package org.jfree.chart.plot;

import java.awt.geom.Rectangle2D;

import org.jfree.chart.renderer.RendererState;

/**
 * A renderer state.
 */
public class PiePlotState extends RendererState {

    /** The number of passes required by the renderer. */
    private int passesRequired;

    /** The total of the values in the dataset. */
    private double total;

    /** The latest angle. */
    private double latestAngle;

    /** The exploded pie area. */
    private Rectangle2D explodedPieArea;

    /** The pie area. */
    private Rectangle2D pieArea;

    /** The center of the pie in Java 2D coordinates. */
    private double pieCenterX;

    /** The center of the pie in Java 2D coordinates. */
    private double pieCenterY;

    /** The vertical pie radius. */
    private double pieHRadius;

    /** The horizontal pie radius. */
    private double pieWRadius;

    /** The link area. */
    private Rectangle2D linkArea;

    /**
     * Creates a new object for recording temporary state information for a
     * renderer.
     *
     * @param info  the plot rendering info.
     */
    public PiePlotState(PlotRenderingInfo info) {
        super(info);
        this.passesRequired = 1;
        this.total = 0.0;
    }

    /**
     * Returns the number of passes required by the renderer.
     *
     * @return The number of passes.
     */
    public int getPassesRequired() {
        return this.passesRequired;
    }

    /**
     * Sets the number of passes required by the renderer.
     *
     * @param passes  the passes.
     */
    public void setPassesRequired(int passes) {
        this.passesRequired = passes;
    }

    /**
     * Returns the total of the values in the dataset.
     *
     * @return The total.
     */
    public double getTotal() {
        return this.total;
    }

    /**
     * Sets the total.
     *
     * @param total  the total.
     */
    public void setTotal(double total) {
        this.total = total;
    }

    /**
     * Returns the latest angle.
     *
     * @return The latest angle.
     */
    public double getLatestAngle() {
        return this.latestAngle;
    }

    /**
     * Sets the latest angle.
     *
     * @param angle  the angle.
     */
    public void setLatestAngle(double angle) {
        this.latestAngle = angle;
    }

    /**
     * Returns the pie area.
     *
     * @return The pie area.
     */
    public Rectangle2D getPieArea() {
        return this.pieArea;
    }

    /**
     * Sets the pie area.
     *
     * @param area  the area.
     */
    public void setPieArea(Rectangle2D area) {
       this.pieArea = area;
    }

    /**
     * Returns the exploded pie area.
     *
     * @return The exploded pie area.
     */
    public Rectangle2D getExplodedPieArea() {
        return this.explodedPieArea;
    }

    /**
     * Sets the exploded pie area.
     *
     * @param area  the area.
     */
    public void setExplodedPieArea(Rectangle2D area) {
        this.explodedPieArea = area;
    }

    /**
     * Returns the x-coordinate of the center of the pie chart.
     *
     * @return The x-coordinate (in Java2D space).
     */
    public double getPieCenterX() {
        return this.pieCenterX;
    }

    /**
     * Sets the x-coordinate of the center of the pie chart.
     *
     * @param x  the x-coordinate (in Java2D space).
     */
    public void setPieCenterX(double x) {
        this.pieCenterX = x;
    }

    /**
     * Returns the y-coordinate (in Java2D space) of the center of the pie
     * chart.  For the {@link PiePlot3D} class, we derive this from the top of
     * the pie.
     *
     * @return The y-coordinate (in Java2D space).
     */
    public double getPieCenterY() {
        return this.pieCenterY;
    }

    /**
     * Sets the y-coordinate of the center of the pie chart.  This method is
     * used by the plot and typically is not called directly by applications.
     *
     * @param y  the y-coordinate (in Java2D space).
     */
    public void setPieCenterY(double y) {
        this.pieCenterY = y;
    }

    /**
     * Returns the link area.  This defines the "dog-leg" point for the label
     * linking lines.
     *
     * @return The link area.
     */
    public Rectangle2D getLinkArea() {
        return this.linkArea;
    }

    /**
     * Sets the label link area.  This defines the "dog-leg" point for the
     * label linking lines.
     *
     * @param area  the area.
     */
    public void setLinkArea(Rectangle2D area) {
        this.linkArea = area;
    }

    /**
     * Returns the vertical pie radius.
     *
     * @return The radius.
     */
    public double getPieHRadius() {
        return this.pieHRadius;
    }

    /**
     * Sets the vertical pie radius.
     *
     * @param radius  the radius.
     */
    public void setPieHRadius(double radius) {
        this.pieHRadius = radius;
    }

    /**
     * Returns the horizontal pie radius.
     *
     * @return The radius.
     */
    public double getPieWRadius() {
        return this.pieWRadius;
    }

    /**
     * Sets the horizontal pie radius.
     *
     * @param radius  the radius.
     */
    public void setPieWRadius(double radius) {
        this.pieWRadius = radius;
    }

}
