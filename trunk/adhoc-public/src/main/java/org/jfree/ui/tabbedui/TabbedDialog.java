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
 * ----------------------
 * AbstractTabbedGUI.java
 * ----------------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: TabbedDialog.java,v 1.7 2008/09/10 09:19:04 mungady Exp $
 *
 * Changes
 * -------------------------
 * 16-Feb-2004 : Initial version
 * 07-Jun-2004 : Added standard header (DG);
 */

package org.jfree.ui.tabbedui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * A JDialog implementation that uses a tabbed UI as backend.
 *
 * @author Thomas Morgner
 */
public class TabbedDialog extends JDialog {

    /** The backend. */
    private AbstractTabbedUI tabbedUI;

    /**
     * A property change listener that waits for the menubar to change.
     */
    private class MenuBarChangeListener implements PropertyChangeListener {

        /**
         * Creates a new change listener.
         */
        public MenuBarChangeListener() {
        }

        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source
         *            and the property that has changed.
         */
        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(AbstractTabbedUI.JMENUBAR_PROPERTY)) {
                setJMenuBar(getTabbedUI().getJMenuBar());
            }
        }
    }
    /**
     * Default constructor.
     */
    public TabbedDialog() {
    }

    /**
     * Creates a new dialog.
     *
     * @param owner  the owner.
     */
    public TabbedDialog(final Dialog owner) {
        super(owner);
    }

    /**
     * Creates a new dialog.
     *
     * @param owner  the owner.
     * @param modal  modal dialog?
     */
    public TabbedDialog(final Dialog owner, final boolean modal) {
        super(owner, modal);
    }

    /**
     * Creates a new dialog.
     *
     * @param owner  the owner.
     * @param title  the dialog title.
     */
    public TabbedDialog(final Dialog owner, final String title) {
        super(owner, title);
    }

    /**
     * Creates a new dialog.
     *
     * @param owner  the owner.
     * @param title  the dialog title.
     * @param modal  modal dialog?
     */
    public TabbedDialog(final Dialog owner, final String title, final boolean modal) {
        super(owner, title, modal);
    }

    /**
     * Creates a new dialog.
     *
     * @param owner  the owner.
     */
    public TabbedDialog(final Frame owner) {
        super(owner);
    }

    /**
     * Creates a new dialog.
     *
     * @param owner  the owner.
     * @param modal  modal dialog?
     */
    public TabbedDialog(final Frame owner, final boolean modal) {
        super(owner, modal);
    }

    /**
     * Creates a new dialog.
     *
     * @param owner  the owner.
     * @param title  the dialog title.
     */
    public TabbedDialog(final Frame owner, final String title) {
        super(owner, title);
    }

    /**
     * Creates a new dialog.
     *
     * @param owner  the owner.
     * @param title  the dialog title.
     * @param modal  modal dialog?
     */
    public TabbedDialog(final Frame owner, final String title, final boolean modal) {
        super(owner, title, modal);
    }


    /**
     * Returns the UI implementation for the dialog.
     *
     * @return Returns the tabbedUI.
     */
    protected final AbstractTabbedUI getTabbedUI()
    {
      return this.tabbedUI;
    }

    /**
     * Initialises the dialog.
     *
     * @param tabbedUI  the UI that controls the dialog.
     */
    public void init(final AbstractTabbedUI tabbedUI) {

        this.tabbedUI = tabbedUI;
        this.tabbedUI.addPropertyChangeListener
            (AbstractTabbedUI.JMENUBAR_PROPERTY, new MenuBarChangeListener());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                getTabbedUI().getCloseAction().actionPerformed
                    (new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null, 0));
            }
        });

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(tabbedUI, BorderLayout.CENTER);
        setContentPane(panel);
        setJMenuBar(tabbedUI.getJMenuBar());

    }

}
