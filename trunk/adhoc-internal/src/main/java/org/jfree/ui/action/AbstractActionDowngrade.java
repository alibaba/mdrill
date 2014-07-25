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
 * ----------------------------
 * AbstractActionDowngrade.java
 * ----------------------------
 * (C)opyright 2002-2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: AbstractActionDowngrade.java,v 1.2 2005/10/18 13:22:13 mungady Exp $
 *
 * Changes
 * -------
 * 30-Aug-2002 : Initial version
 * 10-Dec-2002 : Updated Javadocs (DG);
 * 07-Jun-2004 : Corrected source headers (DG);
 *
 */

package org.jfree.ui.action;

import javax.swing.AbstractAction;

/**
 * A class that allows Action features introduced in JDK 1.3 to be used with JDK 1.2.2, by
 * defining the two new constants introduced by Sun in JDK 1.3.
 *
 * @author Thomas Morgner
 */
public abstract class AbstractActionDowngrade extends AbstractAction implements ActionDowngrade {
    // kills a compile error for JDK's >= 1.3
    // ambiguous reference error ...
    /**
     * The key used for storing a <code>KeyStroke</code> to be used as the
     * accelerator for the action.
     */
    public static final String ACCELERATOR_KEY = ActionDowngrade.ACCELERATOR_KEY;

    /**
     * The key used for storing an int key code to be used as the mnemonic
     * for the action.
     */
    public static final String MNEMONIC_KEY = ActionDowngrade.MNEMONIC_KEY;

    /**
     * Creates a new action with a default (transparent) icon.
     */
    protected AbstractActionDowngrade() {
        // nothing required
    }
    
}
