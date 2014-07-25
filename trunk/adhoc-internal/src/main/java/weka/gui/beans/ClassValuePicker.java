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
 *    ClassValuePicker.java
 *    Copyright (C) 2004 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.SwapValues;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.io.Serializable;
import java.util.Vector;

import javax.swing.JPanel;

/**
 * @author Mark Hall
 * @version $Revision: 7424 $
 */
public class ClassValuePicker
  extends JPanel
  implements Visible, DataSourceListener, BeanCommon,
	     EventConstraints, Serializable, StructureProducer {

  /** for serialization */
  private static final long serialVersionUID = -1196143276710882989L;

  /** the class value considered to be the positive class */
  private String m_classValue;

  /** format of instances for the current incoming connection (if any) */
  private Instances m_connectedFormat;

  private Object m_dataProvider;

  private Vector m_dataListeners = new Vector();
  private Vector m_dataFormatListeners = new Vector();

  protected transient weka.gui.Logger m_logger = null;
  
  protected BeanVisual m_visual = 
    new BeanVisual("ClassValuePicker", 
		   BeanVisual.ICON_PATH+"ClassValuePicker.gif",
		   BeanVisual.ICON_PATH+"ClassValuePicker_animated.gif");

  /**
   * Global info for this bean
   *
   * @return a <code>String</code> value
   */
  public String globalInfo() {
    return "Designate which class value is to be considered the \"positive\" "
      +"class value (useful for ROC style curves).";
  }

  public ClassValuePicker() {
    setLayout(new BorderLayout());
    add(m_visual, BorderLayout.CENTER);    
  }

  /**
   * Set a custom (descriptive) name for this bean
   * 
   * @param name the name to use
   */
  public void setCustomName(String name) {
    m_visual.setText(name);
  }

  /**
   * Get the custom (descriptive) name for this bean (if one has been set)
   * 
   * @return the custom name (or the default name)
   */
  public String getCustomName() {
    return m_visual.getText();
  }
  
  public Instances getStructure(String eventName) {
    if (!eventName.equals("dataSet")) {
      return null;
    }
    if (m_dataProvider == null) {
      return null;
    }
    
    if (m_dataProvider != null && m_dataProvider instanceof StructureProducer) {
      m_connectedFormat =  ((StructureProducer)m_dataProvider).getStructure("dataSet");
    }
    
    return m_connectedFormat;
  }
  
  protected Instances getStructure() {
    if (m_dataProvider != null) {
      return getStructure("dataSet");
    }
    
    return null;
  }

  /**
   * Returns the structure of the incoming instances (if any)
   *
   * @return an <code>Instances</code> value
   */
  public Instances getConnectedFormat() {
    // loaders will push instances format to us
    // when the user makes configuration changes
    // to the loader in the gui. However, if a fully
    // configured flow is loaded then we won't get
    // this information pushed to us until the
    // flow is run. In this case we want to pull
    // it (if possible) from upstream steps so
    // that our customizer can provide the nice
    // UI with the drop down box of class names.
//    if (m_connectedFormat == null) {
      // try and pull the incoming structure
      // from the upstream step (if possible)
  //    m_connectedFormat = getStructure();
   // }
    return getStructure();
  }

  /**
   * Set the class value considered to be the "positive"
   * class value.
   *
   * @param index the class value index to use
   */
  public void setClassValue(String value) {
    m_classValue = value;
    if (m_connectedFormat != null) {
      notifyDataFormatListeners();
    }
  }

  /**
   * Gets the class value considered to be the "positive"
   * class value.
   *
   * @return the class value index
   */
  public String getClassValue() {
    return m_classValue;
  }

  public void acceptDataSet(DataSetEvent e) {
    if (e.isStructureOnly()) {
      if (m_connectedFormat == null ||
	  !m_connectedFormat.equalHeaders(e.getDataSet())) { 
	m_connectedFormat = new Instances(e.getDataSet(), 0);
	// tell any listening customizers (or other
	notifyDataFormatListeners();
      }
    }
    Instances dataSet = e.getDataSet();
    Instances newDataSet = assignClassValue(dataSet);
    
    if (newDataSet != null) {
      e = new DataSetEvent(this, newDataSet);
      notifyDataListeners(e);
    }
  }

  private Instances assignClassValue(Instances dataSet) {
    if (dataSet.classIndex() < 0) {
      if (m_logger != null) {
	m_logger.
	  logMessage("[ClassValuePicker] " 
	      + statusMessagePrefix() 
	      + " No class attribute defined in data set.");
	m_logger.statusMessage(statusMessagePrefix()
	    + "WARNING: No class attribute defined in data set.");
      }
      return dataSet;
    }
    
    if (dataSet.classAttribute().isNumeric()) {
      if (m_logger != null) {
	m_logger.
	  logMessage("[ClassValuePicker] "
	      + statusMessagePrefix()
	      + " Class attribute must be nominal (ClassValuePicker)");
	m_logger.statusMessage(statusMessagePrefix()
	    + "WARNING: Class attribute must be nominal.");
      }
      return dataSet;
    } else {
      if (m_logger != null) {
        m_logger.statusMessage(statusMessagePrefix() + "remove");
      }
    }
    
    if ((m_classValue == null || m_classValue.length() == 0) && 
        dataSet.numInstances() > 0) {

      if (m_logger != null) {
        m_logger.
          logMessage("[ClassValuePicker] "
              + statusMessagePrefix()
              + " Class value to consider as positive has not been set" +
              		" (ClassValuePicker)");
        m_logger.statusMessage(statusMessagePrefix()
            + "WARNING: Class value to consider as positive has not been set.");
      }
      return dataSet;
    }
    
    if (m_classValue == null) {
      // in this case we must just have a structure only
      // dataset, so don't fuss about it and return the
      // exsting structure so that it can get pushed downstream
      return dataSet;
    }
    
    Attribute classAtt = dataSet.classAttribute();
    int classValueIndex = -1;
    
    // if first char is "/" then see if we have "first" or "last"
    // or if the remainder can be parsed as a number
    if (m_classValue.startsWith("/") && m_classValue.length() > 1) {
      String remainder = m_classValue.substring(1);
      remainder = remainder.trim();
      if (remainder.equalsIgnoreCase("first")) {
        classValueIndex = 0;
      } else if (remainder.equalsIgnoreCase("last")) {
        classValueIndex = classAtt.numValues() - 1;
      } else {
        // try and parse as a number
        try {
          classValueIndex = Integer.parseInt(remainder);
          classValueIndex--; // 0-based index
          
          if (classValueIndex < 0 || 
              classValueIndex > classAtt.numValues() - 1) {
            if (m_logger != null) {
              m_logger.
                logMessage("[ClassValuePicker] "
                    + statusMessagePrefix()
                    + " Class value index is out of range!" +
                              " (ClassValuePicker)");
              m_logger.statusMessage(statusMessagePrefix()
                  + "ERROR: Class value index is out of range!.");
            }
          }
        } catch (NumberFormatException n) {
          if (m_logger != null) {
            m_logger.
              logMessage("[ClassValuePicker] "
                  + statusMessagePrefix()
                  + " Unable to parse supplied class value index as an integer" +
                            " (ClassValuePicker)");
            m_logger.statusMessage(statusMessagePrefix()
                + "WARNING: Unable to parse supplied class value index " +
                		"as an integer.");
            return dataSet;
          }
        }
      }
    } else {
      // treat the string as the label to look for
      classValueIndex = classAtt.indexOfValue(m_classValue.trim());
    }
    
    if (classValueIndex < 0) {
      return null; // error
    }

    if (classValueIndex != 0) { // nothing to do if == 0
      // swap selected index with index 0
      try {
	SwapValues sv = new SwapValues();
	sv.setAttributeIndex(""+(dataSet.classIndex()+1));
	sv.setFirstValueIndex("first");
	sv.setSecondValueIndex(""+(classValueIndex+1));
	sv.setInputFormat(dataSet);
	Instances newDataSet = Filter.useFilter(dataSet, sv);
	newDataSet.setRelationName(dataSet.relationName());
	return newDataSet;
      } catch (Exception ex) {
	if (m_logger != null) {
	  m_logger.
	    logMessage("[ClassValuePicker] "
	        +statusMessagePrefix()
	        + " Unable to swap class attibute values.");
	  m_logger.statusMessage(statusMessagePrefix()
	      + "ERROR: (See log for details)");
	  return null;
	}
      }
    }
    return dataSet;
  }

  protected void notifyDataListeners(DataSetEvent tse) {
    Vector l;
    synchronized (this) {
      l = (Vector)m_dataListeners.clone();
    }
    if (l.size() > 0) {
      for(int i = 0; i < l.size(); i++) {
	System.err.println("Notifying data listeners "
			   +"(ClassValuePicker)");
	((DataSourceListener)l.elementAt(i)).acceptDataSet(tse);
      }
    }
  }

  protected void notifyDataFormatListeners() {
    Vector l;
    synchronized (this) {
      l = (Vector)m_dataFormatListeners.clone();
    }
    if (l.size() > 0) {
      DataSetEvent dse = new DataSetEvent(this, m_connectedFormat);
      for(int i = 0; i < l.size(); i++) {
	((DataFormatListener)l.elementAt(i)).newDataFormat(dse);
      }
    }
  }

  public synchronized void addDataSourceListener(DataSourceListener tsl) {
    m_dataListeners.addElement(tsl);
  }

  public synchronized void removeDataSourceListener(DataSourceListener tsl) {
    m_dataListeners.removeElement(tsl);
  }

  public synchronized void addDataFormatListener(DataFormatListener dfl) {
    m_dataFormatListeners.addElement(dfl);
  }

  public synchronized void removeDataFormatListener(DataFormatListener dfl) {
    m_dataFormatListeners.removeElement(dfl);
  }

  public void setVisual(BeanVisual newVisual) {
    m_visual = newVisual;
  }

  public BeanVisual getVisual() {
    return m_visual;
  }

  public void useDefaultVisual() {
    m_visual.loadIcons(BeanVisual.ICON_PATH+"ClassValuePicker.gif",
		       BeanVisual.ICON_PATH+"ClassValuePicker_animated.gif");
  }

  /**
   * Returns true if, at this time, 
   * the object will accept a connection according to the supplied
   * event name
   *
   * @param eventName the event
   * @return true if the object will accept a connection
   */
  public boolean connectionAllowed(String eventName) {
    if (eventName.compareTo("dataSet") == 0 && 
	(m_dataProvider != null)) { 
      return false;
    }

    return true;
  }

  /**
   * Returns true if, at this time, 
   * the object will accept a connection according to the supplied
   * EventSetDescriptor
   *
   * @param esd the EventSetDescriptor
   * @return true if the object will accept a connection
   */
  public boolean connectionAllowed(EventSetDescriptor esd) {
    return connectionAllowed(esd.getName());
  }

  /**
   * Notify this object that it has been registered as a listener with
   * a source with respect to the supplied event name
   *
   * @param eventName the event
   * @param source the source with which this object has been registered as
   * a listener
   */
  public synchronized void connectionNotification(String eventName,
						  Object source) {
    if (connectionAllowed(eventName)) {
      if (eventName.compareTo("dataSet") == 0) {
	m_dataProvider = source;
      }
    }
    m_connectedFormat = null;
  }

  /**
   * Notify this object that it has been deregistered as a listener with
   * a source with respect to the supplied event name
   *
   * @param eventName the event
   * @param source the source with which this object has been registered as
   * a listener
   */
  public synchronized void disconnectionNotification(String eventName,
						     Object source) {

    if (eventName.compareTo("dataSet") == 0) {
      if (m_dataProvider == source) {
	m_dataProvider = null;
      }
    }
    m_connectedFormat = null;
  }

  public void setLog(weka.gui.Logger logger) {
    m_logger = logger;
  }

  public void stop() {
    // nothing to do
  }
  
  /**
   * Returns true if. at this time, the bean is busy with some
   * (i.e. perhaps a worker thread is performing some calculation).
   * 
   * @return true if the bean is busy.
   */
  public boolean isBusy() {
    return false;
  }

  /**
   * Returns true, if at the current time, the named event could
   * be generated. Assumes that the supplied event name is
   * an event that could be generated by this bean
   *
   * @param eventName the name of the event in question
   * @return true if the named event could be generated at this point in
   * time
   */
  public boolean eventGeneratable(String eventName) {
    if (eventName.compareTo("dataSet") != 0) {
      return false;
    }

    if (eventName.compareTo("dataSet") == 0) { 
      if (m_dataProvider == null) {
	m_connectedFormat = null;
	notifyDataFormatListeners();
	return false;
      } else {
	if (m_dataProvider instanceof EventConstraints) {
	  if (!((EventConstraints)m_dataProvider).
	      eventGeneratable("dataSet")) {
	    m_connectedFormat = null;
	    notifyDataFormatListeners();
	    return false;
	  }
	}
      }
    }
    return true;
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|";
  }
}
