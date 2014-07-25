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
 * -----------------------
 * ActionConcentrator.java
 * -----------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ActionConcentrator.java,v 1.4 2007/11/02 17:50:36 taqua Exp $
 *
 * Changes
 * -------
 * 24-Aug-2003 : Initial version
 * 07-Jun-2004 : Corrected source headers (DG);
 */

package org.jfree.ui.action;

import java.util.ArrayList;
import javax.swing.Action;

/**
 * This class is used to collect actions to be enabled or disabled
 * by a sinle call.
 * 
 * @author Thomas Morgner
 */
public class ActionConcentrator {

    /** The collection used to store the actions of this concentrator. */
    private final ArrayList actions;

    /**
     * DefaultConstructor.
     */
    public ActionConcentrator() {
        this.actions = new ArrayList();
    }

    /**
     * Adds the action to this concentrator.
     * 
     * @param a the action to be added.
     */
    public void addAction(final Action a) {
        if (a == null) {
            throw new NullPointerException();
        }
        this.actions.add(a);
    }

    /**
     * Removes the action from this concentrator.
     * 
     * @param a the action to be removed.
     */
    public void removeAction(final Action a) {
        if (a == null) {
            throw new NullPointerException();
        }
        this.actions.remove(a);
    }

    /**
     * Defines the state for all actions. 
     * 
     * @param b the new state for all actions.
     */
    public void setEnabled(final boolean b) {
        for (int i = 0; i < this.actions.size(); i++) {
            final Action a = (Action) this.actions.get(i);
            a.setEnabled(b);
        }
    }

    /**
     * Returns, whether all actions are disabled.
     * If one action is enabled, then this method will return
     * true.
     * 
     * @return true, if at least one action is enabled, false
     * otherwise.
     */
    public boolean isEnabled() {
        for (int i = 0; i < this.actions.size(); i++) {
            final Action a = (Action) this.actions.get(i);
            if (a.isEnabled()) {
                return true;
            }
        }
        return false;
    }
    
}
