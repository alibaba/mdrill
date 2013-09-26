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
 *    BeanCustomizer.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.beans.Customizer;

/**
 * Extends java.beans.Customizer and provides a method to register
 * a listener interested in notification about whether the customizer
 * has modified the object that it is customizing. Typically, an implementation
 * would notify the listener about the modification state when it's window
 * is closed.
 * 
 * @author Mark Hall (mhall{[at]}penthao{[dot]}com)
 * @version $Revision: 7124 $
 *
 */
public interface BeanCustomizer extends Customizer {
  
  /**
   * Interface for something that is interested in the modified status
   * of a source object (typically a BeanCustomizer that is editing an
   * object)
   * 
   * @author mhall
   *
   */
  public interface ModifyListener {
    
    /**
     * Tell the listener about the modified status of the source object.
     * 
     * @param source the source object
     * @param modified true if the source object has been modified
     */
    void setModifiedStatus(Object source, boolean modified);
  }
  
  /**
   * Set a listener to be notified about the modified status of this
   * object
   * 
   * @param l the ModifiedListener
   */
  void setModifiedListener(ModifyListener l);  
}
