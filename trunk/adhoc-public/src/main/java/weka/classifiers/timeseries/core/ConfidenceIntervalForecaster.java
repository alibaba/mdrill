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
 *    ConfidenceIntervalForecaster.java
 *    Copyright (C) 2011 Pentaho Corporation
 */

package weka.classifiers.timeseries.core;

/**
 * Interface to a forecaster that can compute confidence intervals
 * for its forecasts
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 */
public interface ConfidenceIntervalForecaster {

  /**
   * Set the confidence level for confidence intervals.
   * 
   * @param confLevel the confidence level to use.
   */
  void setConfidenceLevel(double confLevel);
  
  /**
   * Get the confidence level in use for computing confidence intervals.
   * 
   * @return the confidence level.
   */
  double getConfidenceLevel();
  
  /**
   * Returns true if this forecaster is computing confidence
   * limits for some or all of its future forecasts (i.e. 
   * getCalculateConfIntervalsForForecasts() > 0).
   * 
   * @return true if confidence limits will be produced for some
   * or all of its future forecasts.
   */
  boolean isProducingConfidenceIntervals();
  
  /**
   * Set the number of steps for which to compute confidence intervals for.
   * E.g. a value of 5 means that confidence bounds will be computed for
   * 1-step-ahead predictions, 2-step-ahead predictions, ..., 5-step-ahead
   * predictions. Setting a value of 0 indicates that no confidence intervals
   * will be computed/produced.
   * 
   * @param steps the number of steps for which to compute confidence intervals
   * for.
   */
  void setCalculateConfIntervalsForForecasts(int steps);
  
  /**
   * Return the number of steps for which confidence intervals will be computed. A 
   * value of 0 indicates that no confidence intervals will be computed/produced.
   * 
   * @return the number of steps for which confidence intervals will be computed.
   */
  int getCalculateConfIntervalsForForecasts();    
}
