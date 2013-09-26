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
 * ----------------------
 * DefaultAxisEditor.java
 * ----------------------
 * (C) Copyright 2005-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert;
 * Contributor(s):   Andrzej Porebski;
 *                   Arnaud Lelievre;
 *
 * Changes
 * -------
 * 24-Nov-2005 : Version 1, based on AxisPropertyEditPanel.java (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.chart.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.layout.LCBLayout;
import org.jfree.ui.FontChooserPanel;
import org.jfree.ui.FontDisplayField;
import org.jfree.ui.PaintSample;
import org.jfree.ui.RectangleInsets;

/**
 * A panel for editing the properties of an axis.
 */
class DefaultAxisEditor extends JPanel implements ActionListener {

    /** The axis label. */
    private JTextField label;

    /** The label font. */
    private Font labelFont;

    /** The label paint. */
    private PaintSample labelPaintSample;

    /** A field showing a description of the label font. */
    private JTextField labelFontField;

    /** The font for displaying tick labels on the axis. */
    private Font tickLabelFont;

    /**
     * A field containing a description of the font for displaying tick labels
     * on the axis.
     */
    private JTextField tickLabelFontField;

    /** The paint (color) for the tick labels. */
    private PaintSample tickLabelPaintSample;

    /**
     * An empty sub-panel for extending the user interface to handle more
     * complex axes.
     */
    private JPanel slot1;

    /**
     * An empty sub-panel for extending the user interface to handle more
     * complex axes.
     */
    private JPanel slot2;

    /** A flag that indicates whether or not the tick labels are visible. */
    private JCheckBox showTickLabelsCheckBox;

    /** A flag that indicates whether or not the tick marks are visible. */
    private JCheckBox showTickMarksCheckBox;

//    /** Insets text field. */
//    private InsetsTextField tickLabelInsetsTextField;
//
//    /** Label insets text field. */
//    private InsetsTextField labelInsetsTextField;

    /** The tick label insets. */
    private RectangleInsets tickLabelInsets;

    /** The label insets. */
    private RectangleInsets labelInsets;

    /** A tabbed pane for... */
    private JTabbedPane otherTabs;

    /** The resourceBundle for the localization. */
    protected static ResourceBundle localizationResources
            = ResourceBundleWrapper.getBundle(
                    "org.jfree.chart.editor.LocalizationBundle");

    /**
     * A static method that returns a panel that is appropriate for the axis
     * type.
     *
     * @param axis  the axis whose properties are to be displayed/edited in
     *              the panel.
     *
     * @return A panel or <code>null</code< if axis is <code>null</code>.
     */
    public static DefaultAxisEditor getInstance(Axis axis) {

        if (axis != null) {
            // figure out what type of axis we have and instantiate the
            // appropriate panel
            if (axis instanceof NumberAxis) {
                return new DefaultNumberAxisEditor((NumberAxis) axis);
            }
            else {
                return new DefaultAxisEditor(axis);
            }
        }
        else {
            return null;
        }

    }

    /**
     * Standard constructor: builds a panel for displaying/editing the
     * properties of the specified axis.
     *
     * @param axis  the axis whose properties are to be displayed/edited in
     *              the panel.
     */
    public DefaultAxisEditor(Axis axis) {

        this.labelFont = axis.getLabelFont();
        this.labelPaintSample = new PaintSample(axis.getLabelPaint());
        this.tickLabelFont = axis.getTickLabelFont();
        this.tickLabelPaintSample = new PaintSample(axis.getTickLabelPaint());

        // Insets values
        this.tickLabelInsets = axis.getTickLabelInsets();
        this.labelInsets = axis.getLabelInsets();

        setLayout(new BorderLayout());

        JPanel general = new JPanel(new BorderLayout());
        general.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                localizationResources.getString("General")
            )
        );

        JPanel interior = new JPanel(new LCBLayout(5));
        interior.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        interior.add(new JLabel(localizationResources.getString("Label")));
        this.label = new JTextField(axis.getLabel());
        interior.add(this.label);
        interior.add(new JPanel());

        interior.add(new JLabel(localizationResources.getString("Font")));
        this.labelFontField = new FontDisplayField(this.labelFont);
        interior.add(this.labelFontField);
        JButton b = new JButton(localizationResources.getString("Select..."));
        b.setActionCommand("SelectLabelFont");
        b.addActionListener(this);
        interior.add(b);

        interior.add(new JLabel(localizationResources.getString("Paint")));
        interior.add(this.labelPaintSample);
        b = new JButton(localizationResources.getString("Select..."));
        b.setActionCommand("SelectLabelPaint");
        b.addActionListener(this);
        interior.add(b);

//        interior.add(
//            new JLabel(localizationResources.getString("Label_Insets"))
//        );
//        b = new JButton(localizationResources.getString("Edit..."));
//        b.setActionCommand("LabelInsets");
//        b.addActionListener(this);
//        this.labelInsetsTextField = new InsetsTextField(this.labelInsets);
//        interior.add(this.labelInsetsTextField);
//        interior.add(b);
//
//        interior.add(
//            new JLabel(localizationResources.getString("Tick_Label_Insets"))
//        );
//        b = new JButton(localizationResources.getString("Edit..."));
//        b.setActionCommand("TickLabelInsets");
//        b.addActionListener(this);
//        this.tickLabelInsetsTextField
//            = new InsetsTextField(this.tickLabelInsets);
//        interior.add(this.tickLabelInsetsTextField);
//        interior.add(b);

        general.add(interior);

        add(general, BorderLayout.NORTH);

        this.slot1 = new JPanel(new BorderLayout());

        JPanel other = new JPanel(new BorderLayout());
        other.setBorder(BorderFactory.createTitledBorder(
                             BorderFactory.createEtchedBorder(),
                             localizationResources.getString("Other")));

        this.otherTabs = new JTabbedPane();
        this.otherTabs.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        JPanel ticks = new JPanel(new LCBLayout(3));
        ticks.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        this.showTickLabelsCheckBox = new JCheckBox(
            localizationResources.getString("Show_tick_labels"),
            axis.isTickLabelsVisible()
        );
        ticks.add(this.showTickLabelsCheckBox);
        ticks.add(new JPanel());
        ticks.add(new JPanel());

        ticks.add(
            new JLabel(localizationResources.getString("Tick_label_font"))
        );
        this.tickLabelFontField = new FontDisplayField(this.tickLabelFont);
        ticks.add(this.tickLabelFontField);
        b = new JButton(localizationResources.getString("Select..."));
        b.setActionCommand("SelectTickLabelFont");
        b.addActionListener(this);
        ticks.add(b);

        this.showTickMarksCheckBox = new JCheckBox(
            localizationResources.getString("Show_tick_marks"),
            axis.isTickMarksVisible()
        );
        ticks.add(this.showTickMarksCheckBox);
        ticks.add(new JPanel());
        ticks.add(new JPanel());

        this.otherTabs.add(localizationResources.getString("Ticks"), ticks);

        other.add(this.otherTabs);

        this.slot1.add(other);

        this.slot2 = new JPanel(new BorderLayout());
        this.slot2.add(this.slot1, BorderLayout.NORTH);
        add(this.slot2);

    }

    /**
     * Returns the current axis label.
     *
     * @return The current axis label.
     */
    public String getLabel() {
        return this.label.getText();
    }

    /**
     * Returns the current label font.
     *
     * @return The current label font.
     */
    public Font getLabelFont() {
        return this.labelFont;
    }

    /**
     * Returns the current label paint.
     *
     * @return The current label paint.
     */
    public Paint getLabelPaint() {
        return this.labelPaintSample.getPaint();
    }

    /**
     * Returns a flag that indicates whether or not the tick labels are visible.
     *
     * @return <code>true</code> if ick mark labels are visible.
     */
    public boolean isTickLabelsVisible() {
        return this.showTickLabelsCheckBox.isSelected();
    }

    /**
     * Returns the font used to draw the tick labels (if they are showing).
     *
     * @return The font used to draw the tick labels.
     */
    public Font getTickLabelFont() {
        return this.tickLabelFont;
    }

    /**
     * Returns the current tick label paint.
     *
     * @return The current tick label paint.
     */
    public Paint getTickLabelPaint() {
        return this.tickLabelPaintSample.getPaint();
    }

    /**
     * Returns the current value of the flag that determines whether or not
     * tick marks are visible.
     *
     * @return <code>true</code> if tick marks are visible.
     */
    public boolean isTickMarksVisible() {
        return this.showTickMarksCheckBox.isSelected();
    }

    /**
     * Returns the current tick label insets value
     *
     * @return The current tick label insets value.
     */
    public RectangleInsets getTickLabelInsets() {
        return (this.tickLabelInsets == null)
            ? new RectangleInsets(0, 0, 0, 0)
            : this.tickLabelInsets;
    }

    /**
     * Returns the current label insets value
     *
     * @return The current label insets value.
     */
    public RectangleInsets getLabelInsets() {
        return (this.labelInsets == null)
            ? new RectangleInsets(0, 0, 0, 0) : this.labelInsets;
    }

    /**
     * Returns a reference to the tabbed pane.
     *
     * @return A reference to the tabbed pane.
     */
    public JTabbedPane getOtherTabs() {
        return this.otherTabs;
    }

    /**
     * Handles user interaction with the property panel.
     *
     * @param event  information about the event that triggered the call to
     *      this method.
     */
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command.equals("SelectLabelFont")) {
            attemptLabelFontSelection();
        }
        else if (command.equals("SelectLabelPaint")) {
            attemptModifyLabelPaint();
        }
        else if (command.equals("SelectTickLabelFont")) {
            attemptTickLabelFontSelection();
        }
//        else if (command.equals("LabelInsets")) {
//            editLabelInsets();
//        }
//        else if (command.equals("TickLabelInsets")) {
//            editTickLabelInsets();
//        }
    }

    /**
     * Presents a font selection dialog to the user.
     */
    private void attemptLabelFontSelection() {

        FontChooserPanel panel = new FontChooserPanel(this.labelFont);
        int result = JOptionPane.showConfirmDialog(this, panel,
            localizationResources.getString("Font_Selection"),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            this.labelFont = panel.getSelectedFont();
            this.labelFontField.setText(
                this.labelFont.getFontName() + " " + this.labelFont.getSize()
            );
        }

    }

    /**
     * Allows the user the opportunity to change the outline paint.
     */
    private void attemptModifyLabelPaint() {
        Color c;
        c = JColorChooser.showDialog(
            this, localizationResources.getString("Label_Color"), Color.blue
        );
        if (c != null) {
            this.labelPaintSample.setPaint(c);
        }
    }

    /**
     * Presents a tick label font selection dialog to the user.
     */
    public void attemptTickLabelFontSelection() {

        FontChooserPanel panel = new FontChooserPanel(this.tickLabelFont);
        int result = JOptionPane.showConfirmDialog(this, panel,
            localizationResources.getString("Font_Selection"),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            this.tickLabelFont = panel.getSelectedFont();
            this.tickLabelFontField.setText(
                this.tickLabelFont.getFontName() + " "
                + this.tickLabelFont.getSize()
            );
        }

    }

//    /**
//     * Presents insets chooser panel allowing user to modify tick label's
//     * individual insets values. Updates the current insets text field if
//     * edit is accepted.
//     */
//    private void editTickLabelInsets() {
//        InsetsChooserPanel panel = new InsetsChooserPanel(
//            this.tickLabelInsets);
//        int result =  JOptionPane.showConfirmDialog(
//            this, panel, localizationResources.getString("Edit_Insets"),
//            JOptionPane.PLAIN_MESSAGE
//        );
//
//        if (result == JOptionPane.OK_OPTION) {
//            this.tickLabelInsets = panel.getInsets();
//            this.tickLabelInsetsTextField.setInsets(this.tickLabelInsets);
//        }
//    }
//
//    /**
//     * Presents insets chooser panel allowing user to modify label's
//     * individual insets values. Updates the current insets text field if edit
//     * is accepted.
//     */
//    private void editLabelInsets() {
//        InsetsChooserPanel panel = new InsetsChooserPanel(this.labelInsets);
//        int result = JOptionPane.showConfirmDialog(
//            this, panel, localizationResources.getString("Edit_Insets"),
//            JOptionPane.PLAIN_MESSAGE
//        );
//
//        if (result == JOptionPane.OK_OPTION) {
//            this.labelInsets = panel.getInsets();
//            this.labelInsetsTextField.setInsets(this.labelInsets);
//        }
//    }

    /**
     * Sets the properties of the specified axis to match the properties
     * defined on this panel.
     *
     * @param axis  the axis.
     */
    public void setAxisProperties(Axis axis) {
        axis.setLabel(getLabel());
        axis.setLabelFont(getLabelFont());
        axis.setLabelPaint(getLabelPaint());
        axis.setTickMarksVisible(isTickMarksVisible());
        // axis.setTickMarkStroke(getTickMarkStroke());
        axis.setTickLabelsVisible(isTickLabelsVisible());
        axis.setTickLabelFont(getTickLabelFont());
        axis.setTickLabelPaint(getTickLabelPaint());
        axis.setTickLabelInsets(getTickLabelInsets());
        axis.setLabelInsets(getLabelInsets());
    }

}
