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
 *    TSEvalModule.java
 *    Copyright (C) 2010 Pentaho Corporation
 */

package weka.classifiers.timeseries.eval;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.evaluation.NumericPrediction;
import weka.core.Instance;
import weka.core.Utils;

/**
 * Abstract superclass of all evaluation modules.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 *
 */
public abstract class TSEvalModule {
  
  /** the target fields that evaluation is computed for */
  protected List<String> m_targetFieldNames;  
  
  /**
   * Gets a list of known evaluation modules.
   * 
   * @return a list of known evaluation modules
   */
  public static List<TSEvalModule> getModuleList() {
    List<TSEvalModule> result = new ArrayList<TSEvalModule>();
    result.add(new ErrorModule()); result.add(new MAEModule());
    result.add(new MSEModule()); result.add(new RMSEModule());
    result.add(new MAPEModule()); result.add(new DACModule());
    result.add(new RAEModule()); result.add(new RRSEModule());
    
    return result;
  }
  
  /**
   * Factory method for obtaining a named evaluation module. If the
   * name does not match any of the known modules, then this method
   * will assume it is fully qualified and try to instantiate it.
   * 
   * @param moduleName the name of the module to obtain
   * @return the named module
   * @throws IllegalArgumentException if the supplied module
   * name is unknown and can't be instantiated.
   */
  public static TSEvalModule getModule(String moduleName) 
    throws IllegalArgumentException {
    
    if (moduleName.equalsIgnoreCase("Error")) {
      return new ErrorModule();
    } else if (moduleName.equalsIgnoreCase("MAE")) {
      return new MAEModule();
    } else if (moduleName.equalsIgnoreCase("MSE")) {
      return new MSEModule();
    } else if (moduleName.equalsIgnoreCase("RMSE")) {
      return new RMSEModule();
    } else if (moduleName.equalsIgnoreCase("MAPE")) {
      return new MAPEModule();
    } else if (moduleName.equalsIgnoreCase("DAC")) {
      return new DACModule();
    } else if (moduleName.equalsIgnoreCase("RAE")) {
      return new RAEModule();
    } else if (moduleName.equalsIgnoreCase("RRSE")) {
      return new RRSEModule();
    } else {
      // assume a fully qualified class name and try to instantiate
      try {
        Object candidateModule = Class.forName(moduleName).newInstance();
        if (candidateModule instanceof TSEvalModule) {
          return (TSEvalModule)candidateModule;
        }
      } catch (InstantiationException e) {
        throw new IllegalArgumentException("Unable to instantiate " + moduleName);
      } catch (IllegalAccessException e) {
        throw new IllegalArgumentException("Unknown evaluation moduel " + moduleName);
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException("Unable to instantiate " + moduleName);
      }        
    }
    
    throw new IllegalArgumentException("Unknown evaluation moduel " + moduleName);
  }
  
  /**
   * Reset the module
   */
  public abstract void reset();
  
  /**
   * Return the short identifying name of this evaluation module
   * 
   * @return the short identifying name of this evaluation module
   */
  public abstract String getEvalName();
  
  /**
   * Return the longer (single sentence) description
   * of this evaluation module
   * 
   * @return the longer description of this module
   */
  public abstract String getDescription();
  
  /**
   * Return the mathematical formula that this
   * evaluation module computes.
   * 
   * @return the mathematical formula that this module
   * computes.
   */
  public abstract String getDefinition();
  
  /**
   * Evaluate the given forecast(s) with respect to the given
   * test instance. Targets with missing values are ignored.
   * 
   * @param forecasts a List of forecasted values. Each element 
   * corresponds to one of the targets and is assumed to be in the same
   * order as the list of targets supplied to the setTargetFields() method.
   * @throws Exception if the evaluation can't be completed for some
   * reason. 
   */
  public abstract void evaluateForInstance(List<NumericPrediction> forecasts, Instance inst) 
    throws Exception;
  
  /**
   * Calculate the measure that this module represents.
   * 
   * @return the value of the measure for this module for each
   * of the target(s).
   * @throws Exception if the measure can't be computed for some reason.
   */
  public abstract double[] calculateMeasure() throws Exception;
  
  /**
   * Return the summary description of the computed measure for
   * each target.
   * 
   * @return the summary string description of the computed measure.
   * @throws Exception if the measure can't be computed for some reason.
   */
  public abstract String toSummaryString() throws Exception;
  
  /**
   * Set a list of target field names. This list must
   * be the same as that provided to the TSForecaster
   * via TSForecast.setFieldsToForecast()
   * 
   * @param targets a List of target field names
   */
  public void setTargetFields(List<String> targets) {
    m_targetFieldNames = targets;
    reset();
  }
  
  /**
   * Get the list of target field names.
   * 
   * @return the list of target field names.
   */
  public List<String> getTargetFields() {
    return m_targetFieldNames;
  }

  /**
   * Get the value of the named target attribute from
   * the supplied instance.
   * 
   * @param targetName the name of the target attribute to get the
   * value of
   * @param inst the instance to retrieve the target value from
   * @return
   */
  protected double getTargetValue(String targetName, Instance inst) {
    if (inst == null) {
      return Utils.missingValue();
    }
    
    int targetIndex = inst.dataset().attribute(targetName).index();
    
    return inst.value(targetIndex);
  }
}
