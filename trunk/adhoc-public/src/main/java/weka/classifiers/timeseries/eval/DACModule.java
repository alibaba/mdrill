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
 *    DACModule.java
 *    Copyright (C) 2010 Pentaho Corporation
 */

package weka.classifiers.timeseries.eval;

import java.util.List;

import weka.classifiers.evaluation.NumericPrediction;
import weka.core.Instance;
import weka.core.Utils;

/**
 * An evaluation module that computes the accuracy of the direction
 * of forecasted values. I.e. the direction accuracy is the number
 * of times the movement of the predicted values matches the movement
 * of the actual values, expressed as a percentage of the number of
 * values predicted.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 */
public class DACModule extends ErrorModule {

  /** the previous instance */
  protected Instance previousInstance;
  
  /** a count of the number of "correct" direction movements for each target */
  protected double[] m_correct;
  
  /** a count of the number of non-missing values for each target */
  protected double[] m_directionsCount;
  
  /**
   * Return the short identifying name of this evaluation module
   * 
   * @return the short identifying name of this evaluation module
   */
  public String getEvalName() {
    return "DAC";
  }
  
  /**
   * Return the longer (single sentence) description
   * of this evaluation module
   * 
   * @return the longer description of this module
   */
  public String getDescription() {
    return "Direction accuracy";
  }
  
  /**
   * Return the mathematical formula that this
   * evaluation module computes.
   * 
   * @return the mathematical formula that this module
   * computes.
   */
  public String getDefinition() {
    return "count(sign(actual_current - actual_previous) == " +
    		"sign(pred_current - pred_previous)) / N";
  }
  
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
  public void evaluateForInstance(List<NumericPrediction> forecasts,
      Instance inst) throws Exception {
    super.evaluateForInstance(forecasts, inst);
    
    if (m_predictions.get(0).size() > 1) {
      for (int i = 0; i < m_targetFieldNames.size(); i++) {
        NumericPrediction currentForI = 
          m_predictions.get(i).get(m_predictions.get(i).size() - 1);
        NumericPrediction previousForI =
          m_predictions.get(i).get(m_predictions.get(i).size() - 2);
        
        if (!Utils.isMissingValue(currentForI.predicted()) &&
            !Utils.isMissingValue(previousForI.predicted()) &&
            !Utils.isMissingValue(currentForI.actual()) &&
            !Utils.isMissingValue(previousForI.actual())) {
          double predictedDirection = 
            currentForI.predicted() - previousForI.predicted();
          double actualDirection = 
            currentForI.actual() - previousForI.actual();
          
          if (actualDirection > 0 && predictedDirection > 0) {
            m_correct[i]++;            
          } else if (actualDirection < 0 && predictedDirection < 0) {
            m_correct[i]++;
          } else if (actualDirection == 0 && predictedDirection == 0) {
            m_correct[i]++;
          }
          
          m_directionsCount[i]++;
        }
      }
    } else {
      m_correct = new double[m_targetFieldNames.size()];
      m_directionsCount = new double[m_targetFieldNames.size()];
    }
  }
  
  /**
   * Calculate the measure that this module represents.
   * 
   * @return the value of the measure for this module for each
   * of the target(s).
   * @throws Exception if the measure can't be computed for some reason.
   */
  public double[] calculateMeasure() throws Exception {
    double[] result = new double[m_targetFieldNames.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = Utils.missingValue();
    }
    
    for (int i = 0; i < m_targetFieldNames.size(); i++) {
      if (m_directionsCount[i] > 0) {
        result[i] = m_correct[i] / m_directionsCount[i] * 100.0;
      }
    }
    
    return result;
  }
}
