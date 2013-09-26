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
 *    AbstractOffscreenChartRenderer.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.util.List;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Abstract base class for offscreen chart renderers. Provides a method
 * for retrieving options and a convenience method for getting the
 * index of an attribute given its name.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 7695 $
 */
public abstract class AbstractOffscreenChartRenderer implements OffscreenChartRenderer {
  
  /**
   * Utility method that splits the supplied Instances out to a list
   * of Instances, where each Instances object contains just the instances
   * of one nominal class value. Does not check whether the supplied class
   * index is a nominal attribute.
   * 
   * @param insts the instances to split
   * @param classIndex the index of the nominal class attribute
   * @return a list of instances
   */
  protected List<Instances> splitToClasses(Instances insts, int classIndex) {
    List<Instances> newSeries = new ArrayList<Instances>();
    
    Instances[] classes = new Instances[insts.attribute(classIndex).numValues()];
    for (int i = 0; i < classes.length; i++) {
      classes[i] = new Instances(insts, 0);
      classes[i].setRelationName(insts.attribute(classIndex).value(i));
    }
    
    for (int i = 0; i < insts.numInstances(); i++) {
      Instance current = insts.instance(i);
      classes[(int)current.value(classIndex)].add((Instance)current.copy());
    }
    
    for (int i = 0; i < classes.length; i++) {
      newSeries.add(classes[i]);
    }
    
    return newSeries;
  }
    
  /**
   * Gets a short list of additional options (if any),
   * suitable for displaying in a tip text, in HTML form. Concrete
   * subclasses should override this if they can actually process
   * additional options.
   * 
   * @return additional options description in simple HTML form
   */
  public String optionsTipTextHTML() {
    return "<html><ul><li>No options for this renderer</li></ul></html>";
  }
  
  /**
   * Utility method for retrieving the value of an option from a
   * list of options. Returns null if the option to get isn't
   * present in the list, an empty string if it is but no value
   * has been specified (i.e. option is a flag) or the value
   * of the option.<p>
   * 
   * Format is:<p>
   * <code>optionName=optionValue</code>
   * 
   * @param options a list of options
   * @param toGet the option to get the value of
   * @return
   */
  protected String getOption(List<String> options, String toGet) {
    String value = null;
    
    if (options == null) {
      return null;
    }
    
    for (String option : options) {
      if (option.startsWith(toGet)) {
        String[] parts = option.split("=");
        if (parts.length != 2) {
          return ""; // indicates a flag
        }
        value = parts[1];
        break;
      }
    }
    
    return value;
  }
  
  /**
   * Get the index of a named attribute in a set of Instances.
   * 
   * @param insts the Instances
   * @param attName the name of the attribute.
   * 
   * @return the index of the attribute or -1 if the attribute is
   * not in the Instances.
   */
  protected int getIndexOfAttribute(Instances insts, String attName) {
    
    if (attName == null) {
      return -1;
    }
    
    // special first and last strings
    if (attName.equalsIgnoreCase("/last")) {
      return insts.numAttributes() - 1;
    }
    if (attName.equalsIgnoreCase("/first")) {
      return 0;
    }
    if (attName.startsWith("/")) {
      // try and parse remainder as a number
      String numS = attName.replace("/", "");
      try {
        int index = Integer.parseInt(numS);
        index--; // from 1-based to 0-based
        if (index >= 0 && index < insts.numAttributes()) {
          return index;
        }
      } catch (NumberFormatException e) {        
      }      
    }
    
    Attribute att = insts.attribute(attName);
    if (att != null) {
      return att.index();
    }
    
    return -1; // not found
  }  
}
