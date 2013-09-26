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
 * SystemPropertiesFrame.java
 * --------------------------
 * (C) Copyright 2000-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: SystemPropertiesFrame.java,v 1.7 2008/12/18 09:57:32 mungady Exp $
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui (DG);
 * 26-Nov-2001 : Made a separate class SystemPropertiesPanel.java (DG);
 * 28-Feb-2002 : Moved to package com.jrefinery.ui.about (DG);
 * 15-Mar-2002 : Modified to use a ResourceBundle for elements that require
 *               localisation (DG);
 * 08-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see JFreeChart patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.ui.about;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jfree.util.ResourceBundleWrapper;

/**
 * A frame containing a table that displays the system properties for the
 * current Java Virtual Machine (JVM).  It is useful to incorporate this frame
 * into an application for diagnostic purposes, since it provides a convenient
 * means for the user to return configuration and version information when
 * reporting problems.
 *
 * @author David Gilbert
 */
public class SystemPropertiesFrame extends JFrame implements ActionListener {

    /** Copy action command. */
    private static final String COPY_COMMAND = "COPY";

    /** Close action command. */
    private static final String CLOSE_COMMAND = "CLOSE";

    /** A system properties panel. */
    private SystemPropertiesPanel panel;

    /**
     * Constructs a standard frame that displays system properties.
     * <P>
     * If a menu is requested, it provides a menu item that allows the user to
     * copy the contents of the table to the clipboard in tab-delimited format.
     *
     * @param menu  flag indicating whether or not the frame should display a
     *              menu to allow the user to copy properties to the clipboard.
     */
    public SystemPropertiesFrame(final boolean menu) {

        final String baseName = "org.jfree.ui.about.resources.AboutResources";
        final ResourceBundle resources = ResourceBundleWrapper.getBundle(
                baseName);

        final String title = resources.getString("system-frame.title");
        setTitle(title);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        if (menu) {
            setJMenuBar(createMenuBar(resources));
        }

        final JPanel content = new JPanel(new BorderLayout());
        this.panel = new SystemPropertiesPanel();
        this.panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        content.add(this.panel, BorderLayout.CENTER);

        final JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        final String label = resources.getString("system-frame.button.close");
        final Character mnemonic = (Character) resources.getObject(
                "system-frame.button.close.mnemonic");
        final JButton closeButton = new JButton(label);
        closeButton.setMnemonic(mnemonic.charValue());

        closeButton.setActionCommand(CLOSE_COMMAND);
        closeButton.addActionListener(this);

        buttonPanel.add(closeButton, BorderLayout.EAST);
        content.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(content);

    }

    /**
     * Handles action events generated by the user.
     *
     * @param e  the event.
     */
    public void actionPerformed(final ActionEvent e) {

        final String command = e.getActionCommand();
        if (command.equals(CLOSE_COMMAND)) {
            dispose();
        }
        else if (command.equals(COPY_COMMAND)) {
            this.panel.copySystemPropertiesToClipboard();
        }

    }


    /**
     * Creates and returns a menu-bar for the frame.
     *
     * @param resources  localised resources.
     *
     * @return a menu bar.
     */
    private JMenuBar createMenuBar(final ResourceBundle resources) {

        final JMenuBar menuBar = new JMenuBar();

        String label = resources.getString("system-frame.menu.file");
        Character mnemonic = (Character) resources.getObject(
                "system-frame.menu.file.mnemonic");
        final JMenu fileMenu = new JMenu(label, true);
        fileMenu.setMnemonic(mnemonic.charValue());

        label = resources.getString("system-frame.menu.file.close");
        mnemonic = (Character) resources.getObject(
                "system-frame.menu.file.close.mnemonic");
        final JMenuItem closeItem = new JMenuItem(label, mnemonic.charValue());
        closeItem.setActionCommand(CLOSE_COMMAND);

        closeItem.addActionListener(this);
        fileMenu.add(closeItem);

        label = resources.getString("system-frame.menu.edit");
        mnemonic = (Character) resources.getObject(
                "system-frame.menu.edit.mnemonic");
        final JMenu editMenu = new JMenu(label);
        editMenu.setMnemonic(mnemonic.charValue());

        label = resources.getString("system-frame.menu.edit.copy");
        mnemonic = (Character) resources.getObject(
                "system-frame.menu.edit.copy.mnemonic");
        final JMenuItem copyItem = new JMenuItem(label, mnemonic.charValue());
        copyItem.setActionCommand(COPY_COMMAND);
        copyItem.addActionListener(this);
        editMenu.add(copyItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        return menuBar;

    }

}
