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
 * ------------------
 * JTextObserver.java
 * ------------------
 * (C) Copyright 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: JTextObserver.java,v 1.6 2007/11/02 17:50:36 taqua Exp $
 *
 * Changes
 * -------
 * 07-Jun-2004 : Added JCommon header (DG);
 *
 */

package org.jfree.ui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.text.JTextComponent;

/**
 * An observer that selects all the text when a field gains the focus.
 *
 * @author Thomas Morgner
 */
public final class JTextObserver implements FocusListener {

    /** The singleton instance. */
    private static JTextObserver singleton;

    /**
     * Creates a new instance.
     */
    private JTextObserver() {
        // nothing required
    }

    /**
     * Returns the single instance.
     * 
     * @return The single instance.
     */
    public static JTextObserver getInstance() {
        if (singleton == null) {
           singleton = new JTextObserver();
        }
        return singleton;
    }

    /**
     * Selects all the text when a field gains the focus.
     * 
     * @param e  the focus event.
     */
    public void focusGained(final FocusEvent e) {
        if (e.getSource() instanceof JTextComponent) {
            final JTextComponent tex = (JTextComponent) e.getSource();
            tex.selectAll();
        }
    }

    /**
     * Deselects the text when a field loses the focus.
     * 
     * @param e  the event.
     */
    public void focusLost(final FocusEvent e) {
        if (e.getSource() instanceof JTextComponent) {
            final JTextComponent tex = (JTextComponent) e.getSource();
            tex.select(0, 0);
        }
    }

    /**
     * Adds this instance as a listener for the specified text component.
     * 
     * @param t  the text component.
     */
    public static void addTextComponent(final JTextComponent t) {
        if (singleton == null) {
            singleton = new JTextObserver();
        }
        t.addFocusListener(singleton);
    }

    /**
     * Removes this instance as a listener for the specified text component.
     * 
     * @param t  the text component.
     */
   public static void removeTextComponent(final JTextComponent t) {
        if (singleton == null) {
            singleton = new JTextObserver();
        }
        t.removeFocusListener(singleton);
    }
    
}
