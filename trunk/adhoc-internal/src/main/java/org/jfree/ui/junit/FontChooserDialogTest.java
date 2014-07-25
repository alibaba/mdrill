/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
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
 * --------------------------
 * FontChooserDialogTest.java
 * --------------------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: FontChooserDialogTest.java,v 1.4 2007/11/02 17:50:37 taqua Exp $
 *
 * Changes 
 * -------
 * 21-Feb-2004 : Initial version
 *  
 */

package org.jfree.ui.junit;

import java.awt.Font;
import java.awt.Frame;

import junit.framework.TestCase;
import org.jfree.ui.FontChooserDialog;

/**
 * A test for the {@link FontChooserDialog} class.
 */
public class FontChooserDialogTest extends TestCase {

    /**
     * Creates a new test.
     * 
     * @param s  the test name.
     */
    public FontChooserDialogTest(final String s) {
        super(s);
    }

    /**
     * Checks that it is possible to create a dialog.
     */
    public void testCreateDialog () {
        try {
            new FontChooserDialog
                (new Frame(), "Title", false, new Font("Serif", Font.PLAIN, 10));
        }
        catch (UnsupportedOperationException use) {
            // Headless mode exception is instance of this ex.
        }
    }
}
