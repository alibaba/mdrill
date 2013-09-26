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
 * -----------------
 * DetailEditor.java
 * -----------------
 * (C) Copyright 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: FloatingButtonEnabler.java,v 1.4 2007/11/02 17:50:36 taqua Exp $
 *
 * Changes
 * -------
 * 07-Jun-2004 : Added JCommon header (DG);
 *
 */

package org.jfree.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;

/**
 * Enables a button to have a simple floating effect. The border of the button is only visible,
 * when the mouse pointer is floating over the button.
 *
 * @author Thomas Morgner
 */
public final class FloatingButtonEnabler extends MouseAdapter {
  
    /** A single instance. */
    private static FloatingButtonEnabler singleton;

    /**
     * Default constructor.
     */
    private FloatingButtonEnabler() {
        // nothing required
    }

    /**
     * Returns a default instance of this enabler.
     *
     * @return a shared instance of this class.
     */
    public static FloatingButtonEnabler getInstance() {
        if (singleton == null) {
            singleton = new FloatingButtonEnabler();
        }
        return singleton;
    }

    /**
     * Adds a button to this enabler.
     *
     * @param button  the button.
     */
    public void addButton(final AbstractButton button) {
        button.addMouseListener(this);
        button.setBorderPainted(false);
    }

    /**
     * Removes a button from the enabler.
     *
     * @param button  the button.
     */
    public void removeButton(final AbstractButton button) {
        button.addMouseListener(this);
        button.setBorderPainted(true);
    }

    /**
     * Triggers the drawing of the border when the mouse entered the button area.
     *
     * @param e  the mouse event.
     */
    public void mouseEntered(final MouseEvent e) {
        if (e.getSource() instanceof AbstractButton) {
            final AbstractButton button = (AbstractButton) e.getSource();
            if (button.isEnabled()) {
                button.setBorderPainted(true);
            }
        }
    }

    /**
     * Disables the drawing of the border when the mouse leaves the button area.
     *
     * @param e  the mouse event.
     */
    public void mouseExited(final MouseEvent e) {
        if (e.getSource() instanceof AbstractButton) {
            final AbstractButton button = (AbstractButton) e.getSource();
            button.setBorderPainted(false);
            if (button.getParent() != null)
            {
//                button.getParent().repaint(button.getX(), button.getY(),
//                    button.getWidth(), button.getHeight());
                button.getParent().repaint();
            }
        }
    }

}
