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
 * --------------
 * RootPanel.java
 * --------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: RootPanel.java,v 1.4 2005/10/18 13:23:37 mungady Exp $
 *
 * Changes
 * -------------------------
 * 16.02.2004 : Initial version
 *
 */

package org.jfree.ui.tabbedui;

import javax.swing.JComponent;

/**
 * A root panel is a panel implementation of an root editor.
 *
 * @author Thomas Morgner
 */
public abstract class RootPanel extends JComponent implements RootEditor {

    /**a flag indicating whether this editor is the currently active editor. */
    private boolean active;

    /**
     * Default constructor.
     */
    public RootPanel() {
        // nothing required.
    }

    /**
     * Returns a flag that indicates whether the panel is active or not.
     * 
     * @return A flag.
     */
    public final boolean isActive() {
        return this.active;
    }

    /**
     * Called when the panel is activated.
     */
    protected void panelActivated()
    {
    }

    /**
     * Called when the panel is deactivated.
     */
    protected void panelDeactivated()
    {
    }

    /**
     * Sets the status of the panel to active or inactive.
     * 
     * @param active  the flag.
     */
    public final void setActive(final boolean active) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        if (active) {
            panelActivated();
        } 
        else {
            panelDeactivated();
        }
    }

    /**
     * Returns the main panel. Returns the self reference, as this panel
     * implements all necessary methods.
     *
     * @return The main panel.
     */
    public JComponent getMainPanel() {
        return this;
    }

    /**
     * Returns the toolbar. This default implementation return null, to indicate
     * that no toolbar is used.
     *
     * @return The toolbar.
     */
    public JComponent getToolbar() {
        return null;
    }


}
