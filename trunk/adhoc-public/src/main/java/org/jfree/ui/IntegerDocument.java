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
 * --------------------
 * IntegerDocument.java
 * --------------------
 * (C) Copyright 2000-2004, by Andrzej Porebski.
 *
 * Original Author:  Andrzej Porebski;
 * Contributor(s):   -;
 *
 * $Id: IntegerDocument.java,v 1.3 2005/11/16 15:58:41 taqua Exp $
 *
 * Changes (from 7-Nov-2001)
 * -------------------------
 * 07-Nov-2001 : Added to com.jrefinery.ui package (DG);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * A document for editing integers.
 *
 * @author Andrzej Porebski
 */
public class IntegerDocument extends PlainDocument {

    /**
     * Inserts a string.
     *
     * @param i  i.
     * @param s  s.
     * @param attributes  the attributes.
     *
     * @throws BadLocationException ??
     */
    public void insertString(final int i, final String s, final AttributeSet attributes)
        throws BadLocationException {

        super.insertString(i, s, attributes);
        if (s != null && (!s.equals("-") || i != 0 || s.length() >= 2)) {
            try {
                Integer.parseInt(getText(0, getLength()));
            }
            catch (NumberFormatException e) {
                remove(i, s.length());
            }
        }
    }

}
