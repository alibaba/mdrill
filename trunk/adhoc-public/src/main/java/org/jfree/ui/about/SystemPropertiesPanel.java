/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
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
 * --------------------------
 * SystemPropertiesPanel.java
 * --------------------------
 * (C) Copyright 2001-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: SystemPropertiesPanel.java,v 1.7 2008/12/18 09:57:32 mungady Exp $
 *
 * Changes
 * -------
 * 26-Nov-2001 : Version 1 (DG);
 * 28-Feb-2002 : Changed package to com.jrefinery.ui.about (DG);
 * 04-Mar-2002 : Added popup menu code by Carl ?? (DG);
 * 15-Mar-2002 : Modified to use ResourceBundle for elements that require
 *               localisation (DG);
 * 26-Jun-2002 : Removed unnecessary import (DG);
 * 08-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see JFreeChart patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.ui.about;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import org.jfree.util.ResourceBundleWrapper;

/**
 * A panel containing a table of system properties.
 *
 * @author David Gilbert
 */
public class SystemPropertiesPanel extends JPanel {

    /** The table that displays the system properties. */
    private JTable table;

    /** Allows for a popup menu for copying. */
    private JPopupMenu copyPopupMenu;

    /** A copy menu item. */
    private JMenuItem copyMenuItem;

    /** A popup listener. */
    private PopupListener copyPopupListener;

    /**
     * Constructs a new panel.
     */
    public SystemPropertiesPanel() {

        final String baseName = "org.jfree.ui.about.resources.AboutResources";
        final ResourceBundle resources = ResourceBundleWrapper.getBundle(
                baseName);

        setLayout(new BorderLayout());
        this.table = SystemProperties.createSystemPropertiesTable();
        add(new JScrollPane(this.table));

        // Add a popup menu to copy to the clipboard...
        this.copyPopupMenu = new JPopupMenu();

        final String label = resources.getString(
                "system-properties-panel.popup-menu.copy");
        final KeyStroke accelerator = (KeyStroke) resources.getObject(
                    "system-properties-panel.popup-menu.copy.accelerator");
        this.copyMenuItem = new JMenuItem(label);
        this.copyMenuItem.setAccelerator(accelerator);
        this.copyMenuItem.getAccessibleContext().setAccessibleDescription(
                label);
        this.copyMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                copySystemPropertiesToClipboard();
            }
        });
        this.copyPopupMenu.add(this.copyMenuItem);

        // add popup Listener to the table
        this.copyPopupListener = new PopupListener();
        this.table.addMouseListener(this.copyPopupListener);

    }

    /**
     * Copies the selected cells in the table to the clipboard, in
     * tab-delimited format.
     */
    public void copySystemPropertiesToClipboard() {

        final StringBuffer buffer = new StringBuffer();
        final ListSelectionModel selection = this.table.getSelectionModel();
        final int firstRow = selection.getMinSelectionIndex();
        final int lastRow = selection.getMaxSelectionIndex();
        if ((firstRow != -1) && (lastRow != -1)) {
            for (int r = firstRow; r <= lastRow; r++) {
                for (int c = 0; c < this.table.getColumnCount(); c++) {
                    buffer.append(this.table.getValueAt(r, c));
                    if (c != 2) {
                        buffer.append("\t");
                    }
                }
                buffer.append("\n");
            }
        }
        final StringSelection ss = new StringSelection(buffer.toString());
        final Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        cb.setContents(ss, ss);

    }


    /**
     * Returns the copy popup menu.
     *
     * @return Returns the copyPopupMenu.
     */
    protected final JPopupMenu getCopyPopupMenu()
    {
      return this.copyPopupMenu;
    }

    /**
     * Returns the table containing the system properties.
     * @return Returns the table.
     */
    protected final JTable getTable()
    {
      return this.table;
    }

    /**
     * A popup listener.
     */
    private class PopupListener extends MouseAdapter {

        /**
         * Default constructor.
         */
        public PopupListener() {
        }

        /**
         * Mouse pressed event.
         *
         * @param e  the event.
         */
        public void mousePressed(final MouseEvent e) {
            maybeShowPopup(e);
        }

        /**
         * Mouse released event.
         *
         * @param e  the event.
         */
        public void mouseReleased(final MouseEvent e) {
            maybeShowPopup(e);
        }

        /**
         * Event handler.
         *
         * @param e  the event.
         */
        private void maybeShowPopup(final MouseEvent e) {
            if (e.isPopupTrigger()) {
                getCopyPopupMenu().show(
                    getTable(), e.getX(), e.getY()
                );
            }
        }
    }

}

