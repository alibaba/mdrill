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
 *    HeadlessEventCollector.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.util.EventObject;
import java.util.List;

/**
 * Interface for Knowledge Flow components that (typically) provide
 * an interactive graphical visualization to implement. This allows
 * events that would normally be processed to provide a graphical display
 * to be collected and retrieved when running in headless mode (perhaps on
 * a server for example). A copy of the component that is running with
 * access to a display can be passed the list of events in order to
 * construct its display-dependent output.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com).
 * @version $Revision: 7567 $
 */
public interface HeadlessEventCollector {

  /**
   * Get the list of events processed in headless mode. May return
   * null or an empty list if not running in headless mode or no
   * events were processed
   * 
   * @return a list of EventObjects or null.
   */
  List<EventObject> retrieveHeadlessEvents();
  
  /**
   * Process a list of events that have been collected earlier. Has
   * no affect if the component is running in headless mode.
   * 
   * @param headless a list of EventObjects to process.
   */
  void processHeadlessEvents(List<EventObject> headless);
}
