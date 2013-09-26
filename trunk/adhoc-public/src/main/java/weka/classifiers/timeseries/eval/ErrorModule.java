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
 *    ErrorModule.java
 *    Copyright (C) 2010 Pentaho Corporation
 */

package weka.classifiers.timeseries.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import weka.classifiers.evaluation.NumericPrediction;
import weka.core.Instance;
import weka.core.Utils;

/**
 * Superclass of error-based evaluation modules. Stores the predictions for each
 * target along with the actual values. Computes the sum of errors for each 
 * target.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 *
 */
public class ErrorModule extends TSEvalModule {
  
  /** The predictions for each target. Outer list indexes targets */
  protected List<List<NumericPrediction>> m_predictions;
  
  /** The counts of each valid target prediction */
  protected double[] m_counts;

  /**
   * Reset this module
   */
  public void reset() {
    if (m_targetFieldNames != null) {
      m_predictions = new ArrayList<List<NumericPrediction>>();
      m_counts = new double[m_targetFieldNames.size()];
      
      for (int i = 0; i < m_targetFieldNames.size(); i++) {
        ArrayList<NumericPrediction> predsForTarget = 
          new ArrayList<NumericPrediction>();
        m_predictions.add(predsForTarget);
      }
    }
  }

  /**
   * Return the short identifying name of this evaluation module
   * 
   * @return the short identifying name of this evaluation module
   */
  public String getEvalName() {
    return "Error";
  }

  /**
   * Return the longer (single sentence) description
   * of this evaluation module
   * 
   * @return the longer description of this module
   */
  public String getDescription() {
    return "Sum of errors";
  }

  /**
   * Return the mathematical formula that this
   * evaluation module computes.
   *    
   * @return the mathematical formula that this module
   * computes.
   */
  public String getDefinition() {
    return "sum(predicted - actual)";
  }
  
  /**
   * Gets a textual description of this module : getDescription() + getEvalName()
   */
  public String toString() {
    return getDescription() + " (" + getEvalName() + ")";
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
  public void evaluateForInstance(List<NumericPrediction> forecasts, Instance inst) 
    throws Exception {
    if (m_predictions == null) {
      throw new Exception("Target fields haven't been set yet!");
    }
    
    if (forecasts.size() != m_targetFieldNames.size()) {
      throw new Exception("The number of forecasted values does not match the" +
      		" number of target fields!");
    }
    
    for (int i = 0; i < m_targetFieldNames.size(); i++) {
      double actualValue = getTargetValue(m_targetFieldNames.get(i), inst);
      double predictedValue = forecasts.get(i).predicted();
      //System.err.println("Actual: " + actualValue + " Predicted: " + predictedValue);
      double[][] intervals = forecasts.get(i).predictionIntervals();
      
      NumericPrediction pred = new NumericPrediction(actualValue, predictedValue, 1, intervals);
      m_predictions.get(i).add(pred);
      
      if (!Utils.isMissingValue(predictedValue) && 
          !Utils.isMissingValue(actualValue)) {
        m_counts[i]++;
      }
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
    if (m_predictions == null || m_predictions.get(0).size() == 0) {
      throw new Exception("No predictions have been seen yet!");
    }
    
    double[] result = new double[m_targetFieldNames.size()];
    for (int i = 0; i < m_targetFieldNames.size(); i++) {
      List<NumericPrediction> preds = m_predictions.get(i);

      double sumOfE = 0;
      for (NumericPrediction p : preds) {
        if (!Utils.isMissingValue(p.error())) {
          sumOfE += p.error();
        }
      }
      
      result[i] = sumOfE;
    }
    
    return result;
  }
  
  /**
   * Gets the number of predicted, actual pairs for each target. Only
   * entries that are non-missing for both actual and predicted contribute
   * to the overall count.
   * 
   * @return the number of predicted, actual pairs for each target.
   * @throws Exception
   */
  public double[] countsForTargets() throws Exception {
    if (m_predictions == null || m_predictions.get(0).size() == 0) {
      throw new Exception("No predictions have been seen yet!");
    }
    
    return m_counts;
  }
  
  /**
   * Get a list of the errors for the supplied target
   * 
   * @param targetName the target to get the errors for
   * @return the errors as a list of Double
   * @throws IllegalArgumentException if the target name is unknown
   */
  public List<Double> getErrorsForTarget(String targetName) 
    throws IllegalArgumentException {

    for (int i = 0; i < m_targetFieldNames.size(); i++) {
      if (m_targetFieldNames.get(i).equals(targetName)) {
        ArrayList<Double> errors = new ArrayList<Double>();
        List<NumericPrediction> preds = m_predictions.get(i);
        for (int j = 0; j < preds.size(); j++) {
          Double err = new Double(preds.get(j).error());
          errors.add(err);
        }
        return errors;
      }
    }
    
    throw new IllegalArgumentException("Unknown target: " + targetName);
  }
  
  /**
   * Get a list of predictions (plus actuals if known) for the supplied target
   * 
   * @param targetName the target to get predictions for
   * @return a list of predictions for the supplied target
   * @throws IllegalArgumentException if the target name is unknown
   */
  public List<NumericPrediction> getPredictionsForTarget(String targetName) 
    throws IllegalArgumentException {
    
    for (int i = 0; i < m_targetFieldNames.size(); i++) {
      if (m_targetFieldNames.get(i).equals(targetName)) {
        return m_predictions.get(i);
      }
    }
    
    throw new IllegalArgumentException("Unknown target: " + targetName);
  }
  
  /**
   * Gets the predictions for all targets
   * 
   * @return the predictions for all targets as a list of lists the outer list
   * indexes targets.
   */
  public List<List<NumericPrediction>> getPredictionsForAllTargets() {
    return m_predictions;
  }
  
  public String toSummaryString() throws Exception {
    StringBuffer result = new StringBuffer();
    
    double[] measures = calculateMeasure();
    for (int i = 0; i < m_targetFieldNames.size(); i++) {
      result.append(getDescription() + " (" + m_targetFieldNames.get(i) + "): " 
          + Utils.doubleToString(measures[i], 4) + " (n = " + m_counts[i] + ")");
      result.append("\n");
    }
    
    return result.toString();
  }
}
