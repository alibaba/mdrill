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
 * DefaultColorBarEditor.java
 * --------------------------
 * (C) Copyright 2002-2008, by David M. O'Donnell and Contributors.
 *
 * Original Author:  David M. O'Donnell;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Arnaud Lelievre;
 *
 * Changes
 * -------
 * 26-Nov-2002 : Version 1 contributed by David M. O'Donnell (DG);
 * 08-Sep-2003 : Added internationalization via use of properties
 *               resourceBundle (RFE 690236) (AL);
 * 24-Nov-2005 : Moved and renamed: org.jfree.chart.ui.ColorBarPropertyEditPanel
 *               --> DefaultColorBarEditor (DG);
 * 02-Feb-2007 : Removed author tags all over JFreeChart sources (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.chart.editor;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.axis.ColorBar;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.GreyPalette;
import org.jfree.chart.plot.RainbowPalette;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.layout.LCBLayout;

/**
 * A DefaultColorBarEditor.  Extends DefaultNumberAxisEditor to allow
 * change general axis type parameters.
 */
class DefaultColorBarEditor extends DefaultNumberAxisEditor {

    /**
     * A checkbox that indicates whether or not the color indices should run
     * high to low.
     */
    private JCheckBox invertPaletteCheckBox;

    /** Flag set by invertPaletteCheckBox. */
    private boolean invertPalette = false;

    /** A checkbox that indicates whether the palette is stepped. */
    private JCheckBox stepPaletteCheckBox;

    /** Flag set by stepPaletteCheckBox. */
    private boolean stepPalette = false;

    /** The Palette Sample displaying the current Palette. */
    private PaletteSample currentPalette;

    /** An array of availiable sample palettes. */
    private PaletteSample[] availablePaletteSamples;

    /** The resourceBundle for the localization. */
   protected  static ResourceBundle localizationResources
           = ResourceBundleWrapper.getBundle(
                   "org.jfree.chart.editor.LocalizationBundle");

    /**
     * Creates a new edit panel for a color bar.
     *
     * @param colorBar  the color bar.
     */
    public DefaultColorBarEditor(ColorBar colorBar) {
        super((NumberAxis) colorBar.getAxis());
        this.invertPalette = colorBar.getColorPalette().isInverse();
        this.stepPalette = colorBar.getColorPalette().isStepped();
        this.currentPalette = new PaletteSample(colorBar.getColorPalette());
        this.availablePaletteSamples = new PaletteSample[2];
        this.availablePaletteSamples[0]
            = new PaletteSample(new RainbowPalette());
        this.availablePaletteSamples[1]
            = new PaletteSample(new GreyPalette());

        JTabbedPane other = getOtherTabs();

        JPanel palettePanel = new JPanel(new LCBLayout(4));
        palettePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        palettePanel.add(new JPanel());
        this.invertPaletteCheckBox = new JCheckBox(
            localizationResources.getString("Invert_Palette"),
            this.invertPalette
        );
        this.invertPaletteCheckBox.setActionCommand("invertPalette");
        this.invertPaletteCheckBox.addActionListener(this);
        palettePanel.add(this.invertPaletteCheckBox);
        palettePanel.add(new JPanel());

        palettePanel.add(new JPanel());
        this.stepPaletteCheckBox = new JCheckBox(
            localizationResources.getString("Step_Palette"),
            this.stepPalette
        );
        this.stepPaletteCheckBox.setActionCommand("stepPalette");
        this.stepPaletteCheckBox.addActionListener(this);
        palettePanel.add(this.stepPaletteCheckBox);
        palettePanel.add(new JPanel());

        palettePanel.add(
            new JLabel(localizationResources.getString("Palette"))
        );
        JButton button
            = new JButton(localizationResources.getString("Set_palette..."));
        button.setActionCommand("PaletteChoice");
        button.addActionListener(this);
        palettePanel.add(this.currentPalette);
        palettePanel.add(button);

        other.add(localizationResources.getString("Palette"), palettePanel);

    }

    /**
     * Handles actions from within the property panel.
     *
     * @param event  the event.
     */
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command.equals("PaletteChoice")) {
            attemptPaletteSelection();
        }
        else if (command.equals("invertPalette")) {
            this.invertPalette = this.invertPaletteCheckBox.isSelected();
        }
        else if (command.equals("stepPalette")) {
            this.stepPalette = this.stepPaletteCheckBox.isSelected();
        }
        else {
            super.actionPerformed(event);  // pass to super-class for handling
        }
    }

    /**
     * Handle a palette selection.
     */
    private void attemptPaletteSelection() {
        PaletteChooserPanel panel
            = new PaletteChooserPanel(null, this.availablePaletteSamples);
        int result = JOptionPane.showConfirmDialog(
            this, panel, localizationResources.getString("Palette_Selection"),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            double zmin = this.currentPalette.getPalette().getMinZ();
            double zmax = this.currentPalette.getPalette().getMaxZ();
            this.currentPalette.setPalette(panel.getSelectedPalette());
            this.currentPalette.getPalette().setMinZ(zmin);
            this.currentPalette.getPalette().setMaxZ(zmax);
        }
    }

    /**
     * Sets the properties of the specified axis to match the properties
     * defined on this panel.
     *
     * @param colorBar  the color bar.
     */
    public void setAxisProperties(ColorBar colorBar) {
        super.setAxisProperties(colorBar.getAxis());
        colorBar.setColorPalette(this.currentPalette.getPalette());
        colorBar.getColorPalette().setInverse(this.invertPalette); //dmo added
        colorBar.getColorPalette().setStepped(this.stepPalette); //dmo added
    }

    /**
     * A static method that returns a panel that is appropriate for the axis
     * type.
     *
     * @param colorBar  the color bar.
     *
     * @return A panel or <code>null</code< if axis is <code>null</code>.
     */
    public static DefaultColorBarEditor getInstance(ColorBar colorBar) {

        if (colorBar != null) {
            return new DefaultColorBarEditor(colorBar);
        }
        else {
            return null;
        }

    }

}
