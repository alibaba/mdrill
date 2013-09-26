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
 *    TSForecaster.java
 *    Copyright (C) 2010 Pentaho Corporation
 */

package weka.classifiers.timeseries;

import java.io.PrintStream;
import java.util.List;

import weka.classifiers.evaluation.NumericPrediction;
import weka.core.Instances;

/**
 * Interface for something that can produce time series predictions.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 */
public interface TSForecaster {
  
  
  /**
   * Provides a short name that describes the underlying algorithm
   * in some way.
   * 
   * @return a short description of this forecaster.
   */
  public String getAlgorithmName();
  
  /**
   * Reset this forecaster so that it is ready to construct a
   * new model.
   */
  public void reset();
  
  /**
   * Set the names of the fields/attributes in the data to forecast.
   * 
   * @param targets a list of names of fields to forecast
   * @throws Exception if a field(s) can't be found, or if multiple
   * fields are specified and this forecaster can't predict multiple
   * fields.
   */
  public void setFieldsToForecast(String targets) throws Exception;
  
  /**
   * Get the fields to forecast.
   * 
   * @return the fields to forecast
   */
  public String getFieldsToForecast();
  
  /**
   * Builds a new forecasting model using the supplied training
   * data. The instances in the data are assumed to be sorted in
   * ascending order of time and equally spaced in time. Some
   * methods may not need to implement this method and may
   * instead do their work in the primeForecaster method.
   * 
   * @param insts the training instances.
   * @param progress an optional varargs parameter supplying progress objects
   * to report/log to
   * @throws Exception if the model can't be constructed for some
   * reason.
   */
  public void buildForecaster(Instances insts, 
      PrintStream... progress) throws Exception;
  
  /**
   * Supply the (potentially) trained model with enough historical
   * data, up to and including the current time point, in order
   * to produce a forecast. Instances are assumed to be sorted in
   * ascending order of time and equally spaced in time.
   * 
   * @param insts the instances to prime the model with
   * @throws Exception if the model can't be primed for some
   * reason.
   */
  public void primeForecaster(Instances insts) throws Exception;  
  
  /**
   * Produce a forecast for the target field(s). 
   * Assumes that the model has been built
   * and/or primed so that a forecast can be generated.
   * 
   * @param numSteps number of forecasted values to produce for each target. E.g.
   * a value of 5 would produce a prediction for t+1, t+2, ..., t+5.
   * @param progress an optional varargs parameter supplying progress objects
   * to report/log to
   * @return a List of Lists (one for each step) of forecasted values for each target
   * @throws Exception if the forecast can't be produced for some reason.
   */
  public List<List<NumericPrediction>> forecast(int numSteps, 
      PrintStream... progress) throws Exception;
  
  /**
   * Run the supplied forecaster with the supplied options on the command line.
   * 
   * @param forecaster the forecaster to run
   * @param options the options to pass to the forecaster
   */
  public abstract void runForecaster(TSForecaster forecaster, String[] options);
}
