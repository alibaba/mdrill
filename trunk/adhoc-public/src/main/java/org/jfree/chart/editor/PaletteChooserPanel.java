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
 * ------------------------
 * PaletteChooserPanel.java
 * ------------------------
 * (C) Copyright 2002-2008, by David M. O'Donnell.
 *
 * Original Author:  David M. O'Donnell;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 27-Jan-2003 : Added standard header (DG);
 * 31-Jan-2007 : Deprecated (DG);
 *
 */

package org.jfree.chart.editor;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.jfree.chart.plot.ColorPalette;
import org.jfree.chart.plot.RainbowPalette;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;

/**
 * A component for choosing a palette from a list of available palettes.
 *
 * @deprecated This class is no longer supported.  If you are creating
 *     contour plots, please try to use {@link XYPlot} and
 *     {@link XYBlockRenderer}.
 */
class PaletteChooserPanel extends JPanel {

    /** A combo for selecting the stroke. */
    private JComboBox selector;

    /**
     * Constructor.
     *
     * @param current  the current palette sample.
     * @param available  an array of 'available' palette samples.
     */
    public PaletteChooserPanel(PaletteSample current,
                               PaletteSample[] available) {
        setLayout(new BorderLayout());
        this.selector = new JComboBox(available);
        this.selector.setSelectedItem(current);
        this.selector.setRenderer(new PaletteSample(new RainbowPalette()));
        add(this.selector);
    }

    /**
     * Returns the selected palette.
     *
     * @return The selected palette.
     */
    public ColorPalette getSelectedPalette() {
        PaletteSample sample = (PaletteSample) this.selector.getSelectedItem();
        return sample.getPalette();
    }
}
