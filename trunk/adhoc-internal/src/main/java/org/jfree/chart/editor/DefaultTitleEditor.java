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
 * -----------------------
 * DefaultTitleEditor.java
 * -----------------------
 * (C) Copyright 2005-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Arnaud Lelievre;
 *                   Daniel Gredler;
 *
 * Changes
 * -------
 * 24-Nov-2005 : Version 1, based on TitlePropertyEditPanel.java (DG);
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
import javax.swing.JTextField;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.layout.LCBLayout;
import org.jfree.ui.FontChooserPanel;
import org.jfree.ui.FontDisplayField;
import org.jfree.ui.PaintSample;

/**
 * A panel for editing the properties of a chart title.
 */
class DefaultTitleEditor extends JPanel implements ActionListener {

    /** Whether or not to display the title on the chart. */
    private boolean showTitle;

    /** The checkbox to indicate whether or not to display the title. */
    private JCheckBox showTitleCheckBox;

    /** A field for displaying/editing the title text. */
    private JTextField titleField;

    /** The font used to draw the title. */
    private Font titleFont;

    /** A field for displaying a description of the title font. */
    private JTextField fontfield;

    /** The button to use to select a new title font. */
    private JButton selectFontButton;

    /** The paint (color) used to draw the title. */
    private PaintSample titlePaint;

    /** The button to use to select a new paint (color) to draw the title. */
    private JButton selectPaintButton;

    /** The resourceBundle for the localization. */
    protected static ResourceBundle localizationResources
            = ResourceBundleWrapper.getBundle(
                    "org.jfree.chart.editor.LocalizationBundle");

    /**
     * Standard constructor: builds a panel for displaying/editing the
     * properties of the specified title.
     *
     * @param title  the title, which should be changed.
     */
    public DefaultTitleEditor(Title title) {

        TextTitle t = (title != null ? (TextTitle) title
                : new TextTitle(localizationResources.getString("Title")));
        this.showTitle = (title != null);
        this.titleFont = t.getFont();
        this.titleField = new JTextField(t.getText());
        this.titlePaint = new PaintSample(t.getPaint());

        setLayout(new BorderLayout());

        JPanel general = new JPanel(new BorderLayout());
        general.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                localizationResources.getString("General")
            )
        );

        JPanel interior = new JPanel(new LCBLayout(4));
        interior.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        interior.add(new JLabel(localizationResources.getString("Show_Title")));
        this.showTitleCheckBox = new JCheckBox();
        this.showTitleCheckBox.setSelected(this.showTitle);
        this.showTitleCheckBox.setActionCommand("ShowTitle");
        this.showTitleCheckBox.addActionListener(this);
        interior.add(new JPanel());
        interior.add(this.showTitleCheckBox);

        JLabel titleLabel = new JLabel(localizationResources.getString("Text"));
        interior.add(titleLabel);
        interior.add(this.titleField);
        interior.add(new JPanel());

        JLabel fontLabel = new JLabel(localizationResources.getString("Font"));
        this.fontfield = new FontDisplayField(this.titleFont);
        this.selectFontButton = new JButton(
            localizationResources.getString("Select...")
        );
        this.selectFontButton.setActionCommand("SelectFont");
        this.selectFontButton.addActionListener(this);
        interior.add(fontLabel);
        interior.add(this.fontfield);
        interior.add(this.selectFontButton);

        JLabel colorLabel = new JLabel(
            localizationResources.getString("Color")
        );
        this.selectPaintButton = new JButton(
            localizationResources.getString("Select...")
        );
        this.selectPaintButton.setActionCommand("SelectPaint");
        this.selectPaintButton.addActionListener(this);
        interior.add(colorLabel);
        interior.add(this.titlePaint);
        interior.add(this.selectPaintButton);

        this.enableOrDisableControls();

        general.add(interior);
        add(general, BorderLayout.NORTH);
    }

    /**
     * Returns the title text entered in the panel.
     *
     * @return The title text entered in the panel.
     */
    public String getTitleText() {
        return this.titleField.getText();
    }

    /**
     * Returns the font selected in the panel.
     *
     * @return The font selected in the panel.
     */
    public Font getTitleFont() {
        return this.titleFont;
    }

    /**
     * Returns the paint selected in the panel.
     *
     * @return The paint selected in the panel.
     */
    public Paint getTitlePaint() {
        return this.titlePaint.getPaint();
    }

    /**
     * Handles button clicks by passing control to an appropriate handler
     * method.
     *
     * @param event  the event
     */
    public void actionPerformed(ActionEvent event) {

        String command = event.getActionCommand();

        if (command.equals("SelectFont")) {
            attemptFontSelection();
        }
        else if (command.equals("SelectPaint")) {
            attemptPaintSelection();
        }
        else if (command.equals("ShowTitle")) {
            attemptModifyShowTitle();
        }
    }

    /**
     * Presents a font selection dialog to the user.
     */
    public void attemptFontSelection() {

        FontChooserPanel panel = new FontChooserPanel(this.titleFont);
        int result =
            JOptionPane.showConfirmDialog(
                this, panel, localizationResources.getString("Font_Selection"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
            );

        if (result == JOptionPane.OK_OPTION) {
            this.titleFont = panel.getSelectedFont();
            this.fontfield.setText(
                this.titleFont.getFontName() + " " + this.titleFont.getSize()
            );
        }
    }

    /**
     * Allow the user the opportunity to select a Paint object.  For now, we
     * just use the standard color chooser - all colors are Paint objects, but
     * not all Paint objects are colors (later we can implement a more general
     * Paint chooser).
     */
    public void attemptPaintSelection() {
        Paint p = this.titlePaint.getPaint();
        Color defaultColor = (p instanceof Color ? (Color) p : Color.blue);
        Color c = JColorChooser.showDialog(
            this, localizationResources.getString("Title_Color"), defaultColor
        );
        if (c != null) {
            this.titlePaint.setPaint(c);
        }
    }

    /**
     * Allow the user the opportunity to change whether the title is
     * displayed on the chart or not.
     */
    private void attemptModifyShowTitle() {
        this.showTitle = this.showTitleCheckBox.isSelected();
        this.enableOrDisableControls();
    }

    /**
     * If we are supposed to show the title, the controls are enabled.
     * If we are not supposed to show the title, the controls are disabled.
     */
    private void enableOrDisableControls() {
        boolean enabled = (this.showTitle == true);
        this.titleField.setEnabled(enabled);
        this.selectFontButton.setEnabled(enabled);
        this.selectPaintButton.setEnabled(enabled);
    }

    /**
     * Sets the properties of the specified title to match the properties
     * defined on this panel.
     *
     * @param chart  the chart whose title is to be modified.
     */
    public void setTitleProperties(JFreeChart chart) {
        if (this.showTitle) {
            TextTitle title = chart.getTitle();
            if (title == null) {
                title = new TextTitle();
                chart.setTitle(title);
            }
            title.setText(getTitleText());
            title.setFont(getTitleFont());
            title.setPaint(getTitlePaint());
        }
        else {
            chart.setTitle((TextTitle) null);
        }
    }

}
