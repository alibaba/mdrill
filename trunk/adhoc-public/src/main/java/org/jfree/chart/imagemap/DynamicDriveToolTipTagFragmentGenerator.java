/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
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
 * --------------------------------------------
 * DynamicDriveToolTipTagFragmentGenerator.java
 * --------------------------------------------
 * (C) Copyright 2003-2009, by Richard Atkinson and Contributors.
 *
 * Original Author:  Richard Atkinson;
 * Contributors:     David Gilbert (for Object Refinery Limited);
 *                   Fawad Halim - bug 2690293;
 *
 * Changes
 * -------
 * 12-Aug-2003 : Version 1 (RA);
 * 04-Dec-2007 : Escape tool tip text to fix bug 1400917 (DG);
 * 19-Mar-2009 : Escape apostrophes - see bug 2690293 with fix by FH (DG);
 *
 */

package org.jfree.chart.imagemap;

/**
 * Generates tooltips using the Dynamic Drive DHTML Tip Message
 * library (http://www.dynamicdrive.com).
 */
public class DynamicDriveToolTipTagFragmentGenerator
        implements ToolTipTagFragmentGenerator {

    /** The title, empty string not to display */
    protected String title = "";

    /** The style number */
    protected int style = 1;

    /**
     * Blank constructor.
     */
    public DynamicDriveToolTipTagFragmentGenerator() {
        super();
    }

    /**
     * Creates a new generator with specific title and style settings.
     *
     * @param title  title for use in all tooltips, use empty String not to
     *               display a title.
     * @param style  style number, see http://www.dynamicdrive.com for more
     *               information.
     */
    public DynamicDriveToolTipTagFragmentGenerator(String title, int style) {
        this.title = title;
        this.style = style;
    }

    /**
     * Generates a tooltip string to go in an HTML image map.
     *
     * @param toolTipText  the tooltip.
     *
     * @return The formatted HTML area tag attribute(s).
     */
    public String generateToolTipFragment(String toolTipText) {
        return " onMouseOver=\"return stm(['"
            + ImageMapUtilities.javascriptEscape(this.title) + "','"
            + ImageMapUtilities.javascriptEscape(toolTipText) + "'],Style["
            + this.style + "]);\"" + " onMouseOut=\"return htm();\"";
    }

}
