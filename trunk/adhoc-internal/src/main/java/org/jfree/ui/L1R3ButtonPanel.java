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
 * L1R3ButtonPanel.java
 * --------------------
 * (C) Copyright 2000-2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: L1R3ButtonPanel.java,v 1.5 2007/11/02 17:50:36 taqua Exp $
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.* (DG);
 * 26-Jun-2002 : Removed unnecessary import (DG);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.ui;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * A 'ready-made' panel that has one button on the left and three buttons on the right - nested
 * panels and layout managers take care of resizing.
 *
 * @author David Gilbert
 */
public class L1R3ButtonPanel extends JPanel {

    /** The left button. */
    private JButton left;

    /** The first button on the right of the panel. */
    private JButton right1;

    /** The second button on the right of the panel. */
    private JButton right2;

    /** The third button on the right of the panel. */
    private JButton right3;

    /**
     * Standard constructor - creates panel with the specified button labels.
     *
     * @param label1  the label for button 1.
     * @param label2  the label for button 2.
     * @param label3  the label for button 3.
     * @param label4  the label for button 4.
     */
    public L1R3ButtonPanel(final String label1, final String label2, final String label3, final String label4) {

        setLayout(new BorderLayout());

        // create the pieces...
        final JPanel panel = new JPanel(new BorderLayout());
        final JPanel panel2 = new JPanel(new BorderLayout());
        this.left = new JButton(label1);
        this.right1 = new JButton(label2);
        this.right2 = new JButton(label3);
        this.right3 = new JButton(label4);

        // ...and put them together
        panel.add(this.left, BorderLayout.WEST);
        panel2.add(this.right1, BorderLayout.EAST);
        panel.add(panel2, BorderLayout.CENTER);
        panel.add(this.right2, BorderLayout.EAST);
        add(panel, BorderLayout.CENTER);
        add(this.right3, BorderLayout.EAST);

    }

    /**
     * Returns a reference to button 1, allowing the caller to set labels, action-listeners etc.
     *
     * @return the left button.
     */
    public JButton getLeftButton() {
        return this.left;
    }

    /**
     * Returns a reference to button 2, allowing the caller to set labels, action-listeners etc.
     *
     * @return the right button 1.
     */
    public JButton getRightButton1() {
        return this.right1;
    }

    /**
     * Returns a reference to button 3, allowing the caller to set labels, action-listeners etc.
     *
     * @return the right button 2.
     */
    public JButton getRightButton2() {
        return this.right2;
    }

    /**
     * Returns a reference to button 4, allowing the caller to set labels, action-listeners etc.
     *
     * @return the right button 3.
     */
    public JButton getRightButton3() {
        return this.right3;
    }

}
