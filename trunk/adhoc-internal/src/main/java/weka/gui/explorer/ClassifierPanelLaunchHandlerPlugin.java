/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    ClassifierPanelLaunchHandlerPlugin.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.explorer;

/**
 * Interface to plugin that can take the current state of the
 * Classifier panel and execute it. E.g. A plugin could be
 * made to train and evaluate the configured classifier on
 * remote machine(s).<p>
 * 
 * For full access to the protected member variables in the
 * ClassifierPanel, an implementation will need to be packaged
 * in weka.gui.explorer. The ClassifierPanel looks for implementations
 * when it is constructed, and will provide a new button (in the case
 * of a single plugin) or a button that pops up a menu (in the
 * case of multiple plugins) in order to invoke the launch() method
 * on the plugin.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 7577 $
 */
public interface ClassifierPanelLaunchHandlerPlugin {
  
  /**
   * Allows the classifier panel to pass in a reference to
   * itself
   * 
   * @param p the ClassifierPanel
   */
  void setClassifierPanel(ClassifierPanel p);
  
  /**
   * Get the name of the launch command (to appear as
   * the button text or in the popup menu)
   * 
   * @return the name of the launch command
   */
  String getLaunchCommand();
  
  /**
   * Gets called when the user clicks the button or selects this
   * plugin's entry from the popup menu.
   */
  void launch();
}
