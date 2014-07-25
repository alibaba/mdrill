/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
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
 * StrokeChooserPanel.java
 * -----------------------
 * (C) Copyright 2000-2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Dirk Zeitz;
 *
 * $Id: StrokeChooserPanel.java,v 1.8 2009/02/27 13:58:41 mungady Exp $
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 16-Mar-2004 : Fix for focus problems (DZ);
 * 27-Feb-2009 : Fixed bug 2612649, NullPointerException (DG);
 *
 */

package org.jfree.ui;

import java.awt.BorderLayout;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * A component for choosing a stroke from a list of available strokes.  This
 * class needs work.
 *
 * @author David Gilbert
 */
public class StrokeChooserPanel extends JPanel {

    /** A combo for selecting the stroke. */
    private JComboBox selector;

    /**
     * Creates a panel containing a combo-box that allows the user to select
     * one stroke from a list of available strokes.
     *
     * @param current  the current stroke sample.
     * @param available  an array of 'available' stroke samples.
     */
    public StrokeChooserPanel(StrokeSample current, StrokeSample[] available) {
        setLayout(new BorderLayout());
        // we've changed the behaviour here to populate the combo box
        // with Stroke objects directly - ideally we'd change the signature
        // of the constructor too...maybe later.
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (int i = 0; i < available.length; i++) {
            model.addElement(available[i].getStroke());
        }
        this.selector = new JComboBox(model);
        this.selector.setSelectedItem(current.getStroke());
        this.selector.setRenderer(new StrokeSample(null));
        add(this.selector);
        // Changes due to focus problems!! DZ
        this.selector.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
                getSelector().transferFocus();
            }
        });
    }


    /**
     * Returns the selector component.
     *
     * @return Returns the selector.
     */
    protected final JComboBox getSelector() {
        return this.selector;
    }

    /**
     * Returns the selected stroke.
     *
     * @return The selected stroke (possibly <code>null</code>).
     */
    public Stroke getSelectedStroke() {
        return (Stroke) this.selector.getSelectedItem();
    }

}
