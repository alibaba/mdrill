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
 * ------------
 * Spinner.java
 * ------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id $
 *
 * Changes
 * -------
 * 14-Oct-2002 : Version 1 (DG);
 *
 */

package org.jfree.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * A very basic spinner component, used for demo purposes only.
 *
 * @author David Gilbert
 */
public class Spinner extends JPanel implements MouseListener {

    /** The current value. */
    private int value;

    /** The text field displaying the value. */
    private JTextField textField;

    /** The arrow button panel. */
    private JPanel buttonPanel;

    /** The up button. */
    private ArrowPanel upButton;

    /** The down button. */
    private ArrowPanel downButton;

    /**
     * Creates a new spinner.
     *
     * @param value  the initial value.
     */
    public Spinner(final int value) {
        super(new BorderLayout());
        this.value = value;
        this.textField = new JTextField(Integer.toString(this.value));
        this.textField.setHorizontalAlignment(SwingConstants.RIGHT);
        add(this.textField);
        this.buttonPanel = new JPanel(new GridLayout(2, 1, 0, 1));
        this.upButton = new ArrowPanel(ArrowPanel.UP);
        this.upButton.addMouseListener(this);
        this.downButton = new ArrowPanel(ArrowPanel.DOWN);
        this.downButton.addMouseListener(this);
        this.buttonPanel.add(this.upButton);
        this.buttonPanel.add(this.downButton);
        add(this.buttonPanel, BorderLayout.EAST);
    }

    /**
     * Returns the current value.
     *
     * @return the current value.
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Receives notification of mouse clicks.
     *
     * @param e  the mouse event.
     */
    public void mouseClicked(final MouseEvent e) {
        if (e.getSource() == this.upButton) {
            this.value++;
            this.textField.setText(Integer.toString(this.value));
            firePropertyChange("value", this.value - 1, this.value);
        }
        else if (e.getSource() == this.downButton) {
            this.value--;
            this.textField.setText(Integer.toString(this.value));
            firePropertyChange("value", this.value + 1, this.value);
        }
    }

    /**
     * Receives notification of mouse events.
     *
     * @param e  the mouse event.
     */
    public void mouseEntered(final MouseEvent e) {
        // ignored
    }

    /**
     * Receives notification of mouse events.
     *
     * @param e  the mouse event.
     */
    public void mouseExited(final MouseEvent e) {
        // ignored
    }

    /**
     * Receives notification of mouse events.
     *
     * @param e  the mouse event.
     */
    public void mousePressed(final MouseEvent e) {
        // ignored
    }

    /**
     * Receives notification of mouse events.
     *
     * @param e  the mouse event.
     */
    public void mouseReleased(final MouseEvent e) {
        // ignored
    }

}
