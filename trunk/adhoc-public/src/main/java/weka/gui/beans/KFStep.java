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
 *    KFStep.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * Optional annotation for plugin beans in the Knowledge Flow. The main
 * purpose is to provide info on which top-level folder (category) the
 * plugin should appear under in the GUI. Note that learning algorithms
 * automatically appear in the Knowledge Flow under the correct category 
 * as long as they are in the classpath or are provided in packages. This
 * annotation mechanism is useful for plugin components that are not
 * standard Weka classifiers, clusterers, associators, loaders etc.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 7674 $
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KFStep {

  /**
   * The top-level folder in the JTree that this plugin bean
   * should appear in
   * 
   * @return the name of the top-level folder that this plugin
   * bean should appear in
   */
  String category();
  
  /**
   * Mouse-over tool tip for this plugin component (appears when the
   * mouse hovers over the entry in the JTree)
   * 
   * @return the tool tip text for this plugin bean
   */
  String toolTipText();
}
