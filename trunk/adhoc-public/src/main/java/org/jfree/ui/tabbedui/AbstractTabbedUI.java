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
 * ---------------------
 * AbstractTabbedUI.java
 * ---------------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: AbstractTabbedUI.java,v 1.10 2007/11/02 17:50:37 taqua Exp $
 *
 * Changes
 * -------------------------
 * 16-Feb-2004 : Initial version
 * 07-Jun-2004 : Added standard header (DG);
 */

package org.jfree.ui.tabbedui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.util.Log;

/**
 * A tabbed GUI. All views on the data are contained in tabs. 
 *
 * @author Thomas Morgner
 */
public abstract class AbstractTabbedUI extends JComponent {

    /** The menu bar property key. */
    public static final String JMENUBAR_PROPERTY = "jMenuBar";
    
    /** The global menu property. */
    public static final String GLOBAL_MENU_PROPERTY = "globalMenu";

    /**
     * An exit action.
     */
    protected class ExitAction extends AbstractAction {

        /**
         * Defines an <code>Action</code> object with a default
         * description string and default icon.
         */
        public ExitAction() {
            putValue(NAME, "Exit");
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e the event.
         */
        public void actionPerformed(final ActionEvent e) {
            attempExit();
        }

    }

    /**
     * A tab change handler.
     */
    private class TabChangeHandler implements ChangeListener {

        /** The tabbed pane to which this handler is registered. */
        private final JTabbedPane pane;

        /**
         * Creates a new handler.
         *
         * @param pane the pane.
         */
        public TabChangeHandler(final JTabbedPane pane) {
            this.pane = pane;
        }

        /**
         * Invoked when the target of the listener has changed its state.
         *
         * @param e a ChangeEvent object
         */
        public void stateChanged(final ChangeEvent e) {
            setSelectedEditor(this.pane.getSelectedIndex());
        }
    }

    /**
     * A tab enable change listener.
     */
    private class TabEnableChangeListener implements PropertyChangeListener {
        
        /**
         * Default constructor.
         */
        public TabEnableChangeListener() {
        }

        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source
         *            and the property that has changed.
         */
        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("enabled") == false) {
                Log.debug ("PropertyName");
                return;
            }
            if (evt.getSource() instanceof RootEditor == false) {
                Log.debug ("Source");
                return;
            }
            final RootEditor editor = (RootEditor) evt.getSource();
            updateRootEditorEnabled(editor);
        }
    }

    /** The list of root editors. One for each tab. */
    private ArrayList rootEditors;
    /** The tabbed pane filling the content area. */
    private JTabbedPane tabbedPane;
    /** The index of the currently selected root editor. */
    private int selectedRootEditor;
    /** The current toolbar. */
    private JComponent currentToolbar;
    /** The container component for the toolbar. */
    private JPanel toolbarContainer;
    /** The close action assigned to this UI. */
    private Action closeAction;
    /** The current menu bar. */
    private JMenuBar jMenuBar;
    /** Whether the UI should build a global menu from all root editors. */
    private boolean globalMenu;

    /**
     * Default constructor.
     */
    public AbstractTabbedUI() {
        this.selectedRootEditor = -1;

        this.toolbarContainer = new JPanel();
        this.toolbarContainer.setLayout(new BorderLayout());

        this.tabbedPane = new JTabbedPane(SwingConstants.BOTTOM);
        this.tabbedPane.addChangeListener(new TabChangeHandler(this.tabbedPane));

        this.rootEditors = new ArrayList();

        setLayout(new BorderLayout());
        add(this.toolbarContainer, BorderLayout.NORTH);
        add(this.tabbedPane, BorderLayout.CENTER);

        this.closeAction = createCloseAction();
    }

    /**
     * Returns the tabbed pane.
     * 
     * @return The tabbed pane.
     */
    protected JTabbedPane getTabbedPane() {
        return this.tabbedPane;
    }

    /**
     * Defines whether to use a global unified menu bar, which contains
     * all menus from all tab-panes or whether to use local menubars.
     * <p>
     * From an usability point of view, global menubars should be preferred,
     * as this way users always see which menus are possibly available and
     * do not wonder where the menus are disappearing.
     *
     * @return true, if global menus should be used, false otherwise.
     */
    public boolean isGlobalMenu() {
        return this.globalMenu;
    }

    /**
     * Sets the global menu flag.
     * 
     * @param globalMenu  the flag.
     */
    public void setGlobalMenu(final boolean globalMenu) {
        this.globalMenu = globalMenu;
        if (isGlobalMenu()) {
            setJMenuBar(updateGlobalMenubar());
        }
        else {
            if (getRootEditorCount () > 0) {
              setJMenuBar(createEditorMenubar(getRootEditor(getSelectedEditor())));
            }
        }
    }

    /**
     * Returns the menu bar.
     * 
     * @return The menu bar.
     */
    public JMenuBar getJMenuBar() {
        return this.jMenuBar;
    }

    /**
     * Sets the menu bar.
     * 
     * @param menuBar  the menu bar.
     */
    protected void setJMenuBar(final JMenuBar menuBar) {
        final JMenuBar oldMenuBar = this.jMenuBar;
        this.jMenuBar = menuBar;
        firePropertyChange(JMENUBAR_PROPERTY, oldMenuBar, menuBar);
    }

    /**
     * Creates a close action.
     * 
     * @return A close action.
     */
    protected Action createCloseAction() {
        return new ExitAction();
    }

    /**
     * Returns the close action.
     * 
     * @return The close action.
     */
    public Action getCloseAction() {
        return this.closeAction;
    }

    /**
     * Returns the prefix menus.
     *
     * @return The prefix menus.
     */
    protected abstract JMenu[] getPrefixMenus();

    /**
     * The postfix menus.
     *
     * @return The postfix menus.
     */
    protected abstract JMenu[] getPostfixMenus();

    /**
     * Adds menus.
     *
     * @param menuBar the menu bar
     * @param customMenus the menus that should be added.
     */
    private void addMenus(final JMenuBar menuBar, final JMenu[] customMenus) {
        for (int i = 0; i < customMenus.length; i++) {
            menuBar.add(customMenus[i]);
        }
    }

    /**
     * Updates the global menu bar.
     * @return the fully initialized menu bar.
     */
    private JMenuBar updateGlobalMenubar () {
      JMenuBar menuBar = getJMenuBar();
      if (menuBar == null) {
          menuBar = new JMenuBar();
      }
      else {
          menuBar.removeAll();
      }

      addMenus(menuBar, getPrefixMenus());
      for (int i = 0; i < this.rootEditors.size(); i++)
      {
          final RootEditor editor = (RootEditor) this.rootEditors.get(i);
          addMenus(menuBar, editor.getMenus());
      }
      addMenus(menuBar, getPostfixMenus());
      return menuBar;
    }

    /**
     * Creates a menu bar.
     *
     * @param root
     * @return A menu bar.
     */
    private JMenuBar createEditorMenubar(final RootEditor root) {

        JMenuBar menuBar = getJMenuBar();
        if (menuBar == null) {
            menuBar = new JMenuBar();
        }
        else {
            menuBar.removeAll();
        }

        addMenus(menuBar, getPrefixMenus());
        if (isGlobalMenu())
        {
            for (int i = 0; i < this.rootEditors.size(); i++)
            {
                final RootEditor editor = (RootEditor) this.rootEditors.get(i);
                addMenus(menuBar, editor.getMenus());
            }
        }
        else
        {
            addMenus(menuBar, root.getMenus());
        }
        addMenus(menuBar, getPostfixMenus());
        return menuBar;
    }

    /**
     * Adds a root editor.
     *
     * @param rootPanel the root panel.
     */
    public void addRootEditor(final RootEditor rootPanel) {
        this.rootEditors.add(rootPanel);
        this.tabbedPane.add(rootPanel.getEditorName(), rootPanel.getMainPanel());
        rootPanel.addPropertyChangeListener("enabled", new TabEnableChangeListener());
        updateRootEditorEnabled(rootPanel);
        if (getRootEditorCount () == 1) {
            setSelectedEditor(0);
        }
        else if (isGlobalMenu()) {
            setJMenuBar(updateGlobalMenubar());
        }
    }

    /**
     * Returns the number of root editors.
     * 
     * @return The count.
     */
    public int getRootEditorCount () {
        return this.rootEditors.size();
    }

    /**
     * Returns the specified editor.
     * 
     * @param pos  the position index.
     *
     * @return The editor at the given position.
     */
    public RootEditor getRootEditor(final int pos) {
        return (RootEditor) this.rootEditors.get(pos);
    }

    /**
     * Returns the selected editor.
     * 
     * @return The selected editor.
     */
    public int getSelectedEditor() {
        return this.selectedRootEditor;
    }

    /**
     * Sets the selected editor.
     *
     * @param selectedEditor the selected editor.
     */
    public void setSelectedEditor(final int selectedEditor) {
        final int oldEditor = this.selectedRootEditor;
        if (oldEditor == selectedEditor) {
            // no change - so nothing to do!
            return;
        }
        this.selectedRootEditor = selectedEditor;
        // make sure that only the selected editor is active.
        // all other editors will be disabled, if needed and
        // not touched if they are already in the correct state

        for (int i = 0; i < this.rootEditors.size(); i++) {
            final boolean shouldBeActive = (i == selectedEditor);
            final RootEditor container =
                (RootEditor) this.rootEditors.get(i);
            if (container.isActive() && (shouldBeActive == false)) {
                container.setActive(false);
            }
        }

        if (this.currentToolbar != null) {
            closeToolbar();
            this.toolbarContainer.removeAll();
            this.currentToolbar = null;
        }

        for (int i = 0; i < this.rootEditors.size(); i++) {
            final boolean shouldBeActive = (i == selectedEditor);
            final RootEditor container =
                (RootEditor) this.rootEditors.get(i);
            if ((container.isActive() == false) && (shouldBeActive == true)) {
                container.setActive(true);
                setJMenuBar(createEditorMenubar(container));
                this.currentToolbar = container.getToolbar();
                if (this.currentToolbar != null) {
                    this.toolbarContainer.add
                        (this.currentToolbar, BorderLayout.CENTER);
                    this.toolbarContainer.setVisible(true);
                    this.currentToolbar.setVisible(true);
                }
                else {
                    this.toolbarContainer.setVisible(false);
                }

                this.getJMenuBar().repaint();
            }
        }
    }

    /**
     * Closes the toolbar.
     */
    private void closeToolbar() {
        if (this.currentToolbar != null) {
            if (this.currentToolbar.getParent() != this.toolbarContainer) {
                // ha!, the toolbar is floating ...
                // Log.debug (currentToolbar.getParent());
                final Window w = SwingUtilities.windowForComponent(this.currentToolbar);
                if (w != null) {
                    w.setVisible(false);
                    w.dispose();
                }
            }
            this.currentToolbar.setVisible(false);
        }
    }

    /**
     * Attempts to exit.
     */
    protected abstract void attempExit();

    /**
     * Update handler for the enable state of the root editor.
     * 
     * @param editor  the editor.
     */
    protected void updateRootEditorEnabled(final RootEditor editor) {

        final boolean enabled = editor.isEnabled();
        for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
            final Component tab = this.tabbedPane.getComponentAt(i);
            if (tab == editor.getMainPanel()) {
                this.tabbedPane.setEnabledAt(i, enabled);
                return;
            }
        }
    }
}
