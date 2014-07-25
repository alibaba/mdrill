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
 * -------------------
 * StandardDialog.java
 * -------------------
 * (C) Copyright 2000-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Arnaud Lelievre;
 *
 * $Id: StandardDialog.java,v 1.7 2008/12/18 09:57:32 mungady Exp $
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 * 08-Sep-2003 : Added internationalization via use of properties
 *               resourceBundle (RFE 690236) (AL);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see JFreeChart patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.ui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.jfree.util.ResourceBundleWrapper;

/**
 * The base class for standard dialogs.
 *
 * @author David Gilbert
 */
public class StandardDialog extends JDialog implements ActionListener {

    /** Flag that indicates whether or not the dialog was cancelled. */
    private boolean cancelled;

    /** The resourceBundle for the localization. */
    protected static final ResourceBundle localizationResources
            = ResourceBundleWrapper.getBundle(
                    "org.jfree.ui.LocalizationBundle");

    /**
     * Standard constructor - builds a dialog...
     *
     * @param owner  the owner.
     * @param title  the title.
     * @param modal  modal?
     */
    public StandardDialog(final Frame owner, final String title,
            final boolean modal) {
        super(owner, title, modal);
        this.cancelled = false;
    }

    /**
     * Standard constructor - builds a dialog...
     *
     * @param owner  the owner.
     * @param title  the title.
     * @param modal  modal?
     */
    public StandardDialog(final Dialog owner, final String title,
            final boolean modal) {
        super(owner, title, modal);
        this.cancelled = false;
    }

    /**
     * Returns a flag that indicates whether or not the dialog has been
     * cancelled.
     *
     * @return boolean.
     */
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Handles clicks on the standard buttons.
     *
     * @param event  the event.
     */
    public void actionPerformed(final ActionEvent event) {
        final String command = event.getActionCommand();
        if (command.equals("helpButton")) {
            // display help information
        }
        else if (command.equals("okButton")) {
            this.cancelled = false;
            setVisible(false);
        }
        else if (command.equals("cancelButton")) {
            this.cancelled = true;
            setVisible(false);
        }
    }

    /**
     * Builds and returns the user interface for the dialog.  This method is
     * shared among the constructors.
     *
     * @return the button panel.
     */
    protected JPanel createButtonPanel() {

        final L1R2ButtonPanel buttons = new L1R2ButtonPanel(
                localizationResources.getString("Help"),
                localizationResources.getString("OK"),
                localizationResources.getString("Cancel"));

        final JButton helpButton = buttons.getLeftButton();
        helpButton.setActionCommand("helpButton");
        helpButton.addActionListener(this);

        final JButton okButton = buttons.getRightButton1();
        okButton.setActionCommand("okButton");
        okButton.addActionListener(this);

        final JButton cancelButton = buttons.getRightButton2();
        cancelButton.setActionCommand("cancelButton");
        cancelButton.addActionListener(this);

        buttons.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        return buttons;
    }

}
