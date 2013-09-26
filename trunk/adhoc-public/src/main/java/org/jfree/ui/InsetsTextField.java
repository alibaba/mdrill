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
 * --------------------
 * InsetsTextField.java
 * --------------------
 * (C) Copyright 2000-2008, by Andrzej Porebski.
 *
 * Original Author:  Andrzej Porebski;
 * Contributor(s):   Arnaud Lelievre;
 *
 * $Id: InsetsTextField.java,v 1.4 2008/12/18 09:57:32 mungady Exp $
 *
 * Changes (from 7-Nov-2001)
 * -------------------------
 * 07-Nov-2001 : Added to com.jrefinery.ui package (DG);
 * 08-Sep-2003 : Added internationalization via use of properties
 *               resourceBundle (RFE 690236) (AL);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see JFreeChart patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.ui;

import java.awt.Insets;
import java.util.ResourceBundle;

import javax.swing.JTextField;

import org.jfree.util.ResourceBundleWrapper;

/**
 * A JTextField for displaying insets.
 *
 * @author Andrzej Porebski
 */
public class InsetsTextField extends JTextField {

    /** The resourceBundle for the localization. */
    protected static ResourceBundle localizationResources
            = ResourceBundleWrapper.getBundle(
                    "org.jfree.ui.LocalizationBundle");

    /**
     * Default constructor. Initializes this text field with formatted string
     * describing provided insets.
     *
     * @param insets  the insets.
     */
    public InsetsTextField(final Insets insets) {
        super();
        setInsets(insets);
        setEnabled(false);
    }

    /**
     * Returns a formatted string describing provided insets.
     *
     * @param insets  the insets.
     *
     * @return the string.
     */
    public String formatInsetsString(Insets insets) {
        insets = (insets == null) ? new Insets(0, 0, 0, 0) : insets;
        return
            localizationResources.getString("T") + insets.top + ", "
             + localizationResources.getString("L") + insets.left + ", "
             + localizationResources.getString("B") + insets.bottom + ", "
             + localizationResources.getString("R") + insets.right;
    }

    /**
     * Sets the text of this text field to the formatted string
     * describing provided insets. If insets is null, empty insets
     * (0,0,0,0) are used.
     *
     * @param insets  the insets.
     */
    public void setInsets(final Insets insets) {
        setText(formatInsetsString(insets));
    }

}
