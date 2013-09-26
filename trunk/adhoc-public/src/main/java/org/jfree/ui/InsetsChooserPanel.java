/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
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
 * InsetsChooserPanel.java
 * -----------------------
 * (C) Copyright 2000-2008, by Andrzej Porebski and Contributors.
 *
 * Original Author:  Andrzej Porebski;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Arnaud Lelievre;
 *
 * $Id: InsetsChooserPanel.java,v 1.8 2008/12/18 09:57:32 mungady Exp $
 *
 * Changes (from 7-Nov-2001)
 * -------------------------
 * 07-Nov-2001 : Added to com.jrefinery.ui package (DG);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 03-Feb-2003 : Added Math.abs() to ensure no negative insets can be set (DG);
 * 08-Sep-2003 : Added internationalization via use of properties
 *               resourceBundle (RFE 690236) (AL);
 * 07-Oct-2005 : Renamed getInsets() --> getInsetsValue() to avoid conflict
 *               with JComponent's getInsets() (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see JFreeChart patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.jfree.util.ResourceBundleWrapper;

/**
 * A component for editing an instance of the Insets class.
 *
 * @author Andrzej Porebski
 */
public class InsetsChooserPanel extends JPanel {

    /** A text field for the 'top' setting. */
    private JTextField topValueEditor;

    /** A text field for the 'left' setting. */
    private JTextField leftValueEditor;

    /** A text field for the 'bottom' setting. */
    private JTextField bottomValueEditor;

    /** A text field for the 'right' setting. */
    private JTextField rightValueEditor;

    /** The resourceBundle for the localization. */
    protected static ResourceBundle localizationResources
            = ResourceBundleWrapper.getBundle(
                    "org.jfree.ui.LocalizationBundle");

    /**
     * Creates a chooser panel that allows manipulation of Insets values.
     * The values are initialized to the empty insets (0,0,0,0).
     */
    public InsetsChooserPanel() {
        this(new Insets(0, 0, 0, 0));
    }

    /**
     * Creates a chooser panel that allows manipulation of Insets values.
     * The values are initialized to the current values of provided insets.
     *
     * @param current  the insets.
     */
    public InsetsChooserPanel(Insets current) {
        current = (current == null) ? new Insets(0, 0, 0, 0) : current;

        this.topValueEditor = new JTextField(new IntegerDocument(), ""
                + current.top, 0);
        this.leftValueEditor = new JTextField(new IntegerDocument(), ""
                + current.left, 0);
        this.bottomValueEditor = new JTextField(new IntegerDocument(), ""
                + current.bottom, 0);
        this.rightValueEditor = new JTextField(new IntegerDocument(), ""
                + current.right, 0);

        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(
                new TitledBorder(localizationResources.getString("Insets")));

        // First row
        panel.add(new JLabel(localizationResources.getString("Top")),
            new GridBagConstraints(1, 0, 3, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0));

        // Second row
        panel.add(new JLabel(" "), new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 12, 0, 12), 8, 0));
        panel.add(this.topValueEditor, new GridBagConstraints(2, 1, 1, 1, 0.0,
            0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
            new Insets(0, 0, 0, 0), 0, 0));
        panel.add(new JLabel(" "),  new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 12, 0, 11), 8, 0));

        // Third row
        panel.add(new JLabel(localizationResources.getString("Left")),
            new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 4, 0, 4), 0, 0));
        panel.add(this.leftValueEditor, new GridBagConstraints(1, 2, 1, 1,
            0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        panel.add(new JLabel(" "), new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.NONE,
            new Insets(0, 12, 0, 12), 8, 0));
        panel.add(this.rightValueEditor, new GridBagConstraints(3, 2, 1, 1,
            0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
            new Insets(0, 0, 0, 0), 0, 0));
        panel.add(new JLabel(localizationResources.getString("Right")),
            new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.NONE,
            new Insets(0, 4, 0, 4), 0, 0));

        // Fourth row
        panel.add(this.bottomValueEditor, new GridBagConstraints(2, 3, 1, 1,
            0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
            new Insets(0, 0, 0, 0), 0, 0));

        // Fifth row
        panel.add(new JLabel(localizationResources.getString("Bottom")),
            new GridBagConstraints(1, 4, 3, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0));
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

    }

    /**
     * Returns a new <code>Insets</code> instance to match the values entered
     * on the panel.
     *
     * @return The insets.
     */
    public Insets getInsetsValue() {
        return new Insets(
            Math.abs(stringToInt(this.topValueEditor.getText())),
            Math.abs(stringToInt(this.leftValueEditor.getText())),
            Math.abs(stringToInt(this.bottomValueEditor.getText())),
            Math.abs(stringToInt(this.rightValueEditor.getText())));
    }

    /**
     * Converts a string representing an integer into its numerical value.
     * If this string does not represent a valid integer value, value of 0
     * is returned.
     *
     * @param value  the string.
     *
     * @return the value.
     */
    protected int stringToInt(String value) {
        value = value.trim();
        if (value.length() == 0) {
            return 0;
        }
        else {
            try {
                return Integer.parseInt(value);
            }
            catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    /**
     * Calls super removeNotify and removes all subcomponents from this panel.
     */
    public void removeNotify() {
        super.removeNotify();
        removeAll();
    }

}
