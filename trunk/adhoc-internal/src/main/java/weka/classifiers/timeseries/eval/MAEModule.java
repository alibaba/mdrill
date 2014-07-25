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
 *    MAEModule.java
 *    Copyright (C) 2010 Pentaho Corporation
 */

package weka.classifiers.timeseries.eval;

import java.util.List;

import weka.classifiers.evaluation.NumericPrediction;
import weka.core.Utils;

/**
 * An evaluation module that computes the mean absolute error
 * of forecasted values. 
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 */
public class MAEModule extends ErrorModule {
  
  /**
   * Return the short identifying name of this evaluation module
   * 
   * @return the short identifying name of this evaluation module
   */
  public String getEvalName() {
    return "MAE";
  }
    
  /**
   * Return the longer (single sentence) description
   * of this evaluation module
   * 
   * @return the longer description of this module
   */
  public String getDescription() {
    return "Mean absolute error";
  }
  
  /**
   * Return the mathematical formula that this
   * evaluation module computes.
   * 
   * @return the mathematical formula that this module
   * computes.
   */
  public String getDefinition() {
    return "sum(abs(predicted - actual)) / N";
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
      double sumAbs = 0;
      List<NumericPrediction> preds = m_predictions.get(i);
     
      int count = 0;
      for (NumericPrediction p : preds) {
        if (!Utils.isMissingValue(p.error())) {
          sumAbs += Math.abs(p.error());
          count++;
        }
      }
      
      if (m_counts[i] > 0) {
        sumAbs /= m_counts[i];
      }
      
      if (count > 0) {
        result[i] = sumAbs;
      } else {
        result[i] = Utils.missingValue();
      }
    }
    
    return result;
  }
}
