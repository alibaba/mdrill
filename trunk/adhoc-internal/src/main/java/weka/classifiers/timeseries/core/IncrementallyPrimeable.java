/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
 *    IncrementallyPrimeable.java
 *    Copyright (C) 2010 Pentaho Corporation
 */

package weka.classifiers.timeseries.core;

import weka.core.Instance;

/**
 * An interface to a forecaster that can be primed incrementally. I.e. 
 * one instance at a time.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 */
public interface IncrementallyPrimeable {
  
  /**
   * Update the priming information incrementally, i.e. one instance at
   * a time. To indicate the start of a new batch of priming data
   * an empty set of instances must be passed to TSForecaster.primeForecaster()
   * before the first call to primeForecasterIncremental()
   * 
   * @param inst the instance to prime with.
   * @throws Exception if something goes wrong.
   */
  void primeForecasterIncremental(Instance inst) throws Exception;    
}
