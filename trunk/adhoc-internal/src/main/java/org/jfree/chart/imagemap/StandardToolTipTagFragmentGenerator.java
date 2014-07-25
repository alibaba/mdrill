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
 * ----------------------------------------
 * StandardToolTipTagFragmentGenerator.java
 * ----------------------------------------
 * (C) Copyright 2003-2008, by Richard Atkinson and Contributors.
 *
 * Original Author:  Richard Atkinson;
 * Contributors:     David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 12-Aug-2003 : Version 1 (RA);
 * 04-Dec-2007 : Escape tool tip text to fix bug 1400917 (DG);
 *
 */

package org.jfree.chart.imagemap;

/**
 * Generates tooltips using the HTML title attribute for image map area tags.
 */
public class StandardToolTipTagFragmentGenerator
        implements ToolTipTagFragmentGenerator {

    /**
     * Creates a new instance.
     */
    public StandardToolTipTagFragmentGenerator() {
        super();
    }

    /**
     * Generates a tooltip string to go in an HTML image map.
     *
     * @param toolTipText  the tooltip.
     *
     * @return The formatted HTML area tag attribute(s).
     */
    public String generateToolTipFragment(String toolTipText) {
        return " title=\"" + ImageMapUtilities.htmlEscape(toolTipText)
            + "\" alt=\"\"";
    }

}
