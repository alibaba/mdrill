/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Time Series 
 * Forecasting.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

/*
 *    TimeSeriesForecastingBeanInfo.java
 *    Copyright (C) 2011 Pentaho Corporation
 *
 */

package weka.gui.beans;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * BeanInfo class for the TimeSeriesForecasting bean
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 *
 */
public class TimeSeriesForecastingBeanInfo extends SimpleBeanInfo {
  public EventSetDescriptor [] getEventSetDescriptors() {
    try {
      EventSetDescriptor [] esds = {
          new EventSetDescriptor(TimeSeriesForecasting.class,
              "instance",
              InstanceListener.class,
              "acceptInstance"),
      };
      return esds;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /**                                                                                           
   * Get the bean descriptor for this bean                                                      
   *                                                                                            
   * @return a <code>BeanDescriptor</code> value                                                
   */
  public BeanDescriptor getBeanDescriptor() {
    return new BeanDescriptor(weka.gui.beans.TimeSeriesForecasting.class,
                              TimeSeriesForecastingCustomizer.class);
  }
}
