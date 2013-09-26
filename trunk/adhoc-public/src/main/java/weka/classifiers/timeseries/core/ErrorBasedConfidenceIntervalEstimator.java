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

/**
 * ErrorBasedConfidenceIntervalEstimator.java
 * Copyright (C) 2010 Pentaho Corporation
 */
package weka.classifiers.timeseries.core;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.timeseries.AbstractForecaster;
import weka.classifiers.timeseries.TSForecaster;
import weka.classifiers.timeseries.eval.ErrorModule;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Class that computes confidence intervals for a time series forecaster
 * using errors computed on the training data.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 50211 $
 *
 */
public class ErrorBasedConfidenceIntervalEstimator implements Serializable {
  
  /** For serialization */
  private static final long serialVersionUID = -2748314799535071043L;

  /**
   * Holds confidence bound offsets for targets at a certain level. 
   * The outer list corresponds to the fields to forecast (in the same
   * order as supplied to the TSForecaster.setFieldsToForecast() method. The inner
   * lists hold two element arrays of doubles (upper, lower bounds). The
   * first index of these lists are bounds for to a one-step-ahead forecast,
   * the second a two-step-ahead forecast, and so on 
   */
  protected List<List<double[]>> m_confidenceLimitsForTargets;
  
  /** The names of the target field(s) */
  protected List<String> m_targetFields;
  
  /** The confidence level for the limits */
  protected double m_confidenceLevel;
  
  /**
   * Get the confidence bound offsets for each target at the supplied
   * confidence level
   * 
   * @param confidenceLevel the confidence level to use
   * @return a List of confidence offsets - one for each target. Each
   * target's confidence limits are stored in a two element array,
   * where the first element stores the upper bound and the second
   * the lower bound (both are expressed as an offset)
   * @throws Exception if the confidence limits can't be computed
   * for some reason
   */
  public List<double[]> getConfidenceOffsets(double confidenceLevel,
      List<List<NumericPrediction>> predictions)
    throws Exception {
    
    if (predictions == null || predictions.get(0).size() == 0) {
      throw new Exception("No predictions have been seen yet!");
    }
    
    List<double[]> result = new ArrayList<double[]>();
    
    for (int i = 0; i < m_targetFields.size(); i++) {
      List<NumericPrediction> preds = predictions.get(i);
      
      // need to separate the positive and negative errors
      // into two separate lists
      List<Double> posErrs = new ArrayList<Double>();
      List<Double> negErrs = new ArrayList<Double>();
      
      for (NumericPrediction p : preds) {
        if (!Utils.isMissingValue(p.error())) {
          if (p.error() < 0) {
            negErrs.add(new Double(Math.abs(p.error())));
          }
          
          if (p.error() > 0) {
            posErrs.add(new Double(p.error()));
          }
        }
      }
      
      // sort into ascending order
      Collections.sort(posErrs);
      Collections.sort(negErrs);
      double[] bounds = new double[2];
      bounds[0] = Utils.missingValue();
      bounds[1] = Utils.missingValue();
      
      if (posErrs.size() > 0 && negErrs.size() > 0) {
        double cL = 1.0 - confidenceLevel;
        int posPosition = (int)Math.round(posErrs.size() * cL);
        if (posPosition < 1) {
          posPosition = 1;
        }
        int negPosition = (int)Math.round(negErrs.size() * cL);
        if (negPosition < 1) {
          negPosition = 1;
        }

        //double upperBound = posErrs.get(posErrs.size() - posPosition);
        double upperBound = negErrs.get(negErrs.size() - negPosition);
        //double lowerBound = negErrs.get(negErrs.size() - negPosition);
        double lowerBound = posErrs.get(posErrs.size() - posPosition);
        lowerBound = -lowerBound;
        bounds[0] = lowerBound;
        bounds[1] = upperBound;
        /*System.err.println("Neg pos " + negPosition);
        System.err.println("lower: " + lowerBound + " Upper " + upperBound);*/
      }
      
      result.add(bounds);
    }

    return result;
  }
  
  /**
   * Creates overlay data for "future" instances taken from the training
   * data. All targets are set to missing value in this data. This method
   * is used when the forecaster is being trained using overlay fields.
   * 
   * @param forecaster the forecaster being used
   * @param source the source data to create a set of overlay instances from
   * @param start the index of the instance in the source data that will
   * be the first instance in the overlay data
   * @param numSteps the number of steps to be forecast
   * @return the overlay data as an Instances object
   */
  protected Instances createOverlayForecastData(TSForecaster forecaster, 
      Instances source, int start, int numSteps) {
    
    int toCopy = Math.min(numSteps, source.numInstances() - start);
    Instances overlay = new Instances(source, start, toCopy);
    
    // set all targets to missing
    List<String> fieldsToForecast = 
      AbstractForecaster.stringToList(forecaster.getFieldsToForecast());
    for (int i = 0; i < overlay.numInstances(); i++) {
      Instance current = overlay.instance(i);
      for (String target : fieldsToForecast) {
        current.setValue(overlay.attribute(target), Utils.missingValue());
      }
    }

    return overlay;
  }
  
  /**
   * Computes confidence intervals using the supplied forecster and
   * training data.
   * 
   * @param forecaster the forecaster to use
   * @param insts the training data to use
   * @param numPrime the number of instances to prime the forecaster with
   * @param numSteps the number of steps to forecast (and hence compute
   * intervals for)
   * @param confidenceLevel the confidence level to use
   * @param progress PrintStream objects to report progress to
   * @throws Exception if a problem occurs
   */
  public void calculateConfidenceOffsets(TSForecaster forecaster,
      Instances insts, int numPrime, int numSteps, double confidenceLevel, 
      PrintStream... progress) throws Exception {
    calculateConfidenceOffsets(forecaster, insts, numPrime, -1, numSteps, 
        confidenceLevel, progress);    
  }

  // artificialTimeStartValue is assumed to be the time value for
  // the first instance in the supplied set of instances
  
  /**
   * Computes confidence intervals using the supplied forecster and
   * training data.
   * 
   * @param forecaster the forecaster to use
   * @param insts the training data
   * @param numPrime the number of instances to prime with
   * @param artificialTimeStartValue start value for the artificial time stamp
   * (if one is being used or -1 otherwise)
   * @param numSteps number of time steps to compute confidence intervals for
   * @param confidenceLevel the confidence level to use
   * @param progress varargs PrintStream object(s) to report progress to
   * @throws Exception if something goes wrong.
   */
  public void calculateConfidenceOffsets(TSForecaster forecaster,
      Instances insts, int numPrime, int artificialTimeStartValue, 
      int numSteps, double confidenceLevel, PrintStream... progress)
      throws Exception {
    
    if (insts.numInstances() < (numPrime + numSteps)) {
      throw new Exception("We need at least " + (numPrime + numSteps) 
          + " instances in order to calculate confidence limits!");
    }
    
    if (confidenceLevel < 0 || confidenceLevel > 1) {
      throw new Exception("Confidence level must lie between 0 and 1");
    }
    
    m_targetFields = 
      AbstractForecaster.stringToList(forecaster.getFieldsToForecast());
    m_confidenceLevel = confidenceLevel;
    
    List<ErrorModule> confidenceCalculators = new ArrayList<ErrorModule>();
    for (int i = 0; i < numSteps; i++) {
      ErrorModule m = new ErrorModule();
      m.setTargetFields(m_targetFields);
      confidenceCalculators.add(m);
    }
    
    Instances primeInsts = new Instances(insts, 0, numPrime);
/*    for (int i = 0; i < numPrime; i++) {
      primeInsts.add(insts.instance(i));
    } */
    primeInsts.compactify();
    if (forecaster instanceof TSLagUser && artificialTimeStartValue >= 0) {
      ((TSLagUser)forecaster).getTSLagMaker().
        setArtificialTimeStartValue(artificialTimeStartValue - 1 + numPrime);
    }    
    
    for (int i = numPrime; i < insts.numInstances(); i++) {
      forecaster.primeForecaster(primeInsts);
      
      if (i % 10 == 0) {
        for (PrintStream p : progress) {
          p.println("Computing confidence intervals: processed " + i + " instances...");
        }
      }
      
      List<List<NumericPrediction>> forecastForSteps = null; 
      if (forecaster instanceof OverlayForecaster && 
          ((OverlayForecaster)forecaster).isUsingOverlayData()) {        
        // can only generate forecasts for remaining training data that
        // we can use as overlay data
        Instances overlay = 
          createOverlayForecastData(forecaster, insts, i, numSteps);

        forecastForSteps = 
          ((OverlayForecaster)forecaster).forecast(numSteps, overlay);        
      } else {
        forecastForSteps = forecaster.forecast(numSteps);
      }
      
      // single target only at present
      //List<NumericPrediction> preds = forecastForTargets.get(0);
      
      // update the error modules
      for (int j = 0; j < numSteps && 
        (i + j < insts.numInstances()); j++) {
        
        Instance toPredict = insts.instance(i + j);
//        double[] forecastsForStepJ = new double[m_targetFields.size()];
        List<NumericPrediction> predsForTargets = forecastForSteps.get(j);
        
/*        for (int k = 0; k < m_targetFields.size(); k++) {
          forecastsForStepJ[k] = predsForTargets.get(k).predicted();
        } */
        
        confidenceCalculators.get(j).evaluateForInstance(predsForTargets, toPredict);
      }
      
      // remove the first instance from the primeInsts and then add instance i
      // to the end
      primeInsts.delete(0);
      primeInsts.add(insts.instance(i));
      primeInsts.compactify();      
    }
    
    m_confidenceLimitsForTargets = new ArrayList<List<double[]>>();
    for (int j = 0; j < m_targetFields.size(); j++) {
      ArrayList<double[]> limitsForSingleTarget = new ArrayList<double[]>();
      for (int i = 0; i < numSteps; i++) {
        List<List<NumericPrediction>> predsForStepI = 
          confidenceCalculators.get(i).getPredictionsForAllTargets();
        List<double[]> confOffsetsForStepI = 
          getConfidenceOffsets(confidenceLevel, predsForStepI);
        
        double[] limitsAtStepI = confOffsetsForStepI.get(j);
        limitsForSingleTarget.add(limitsAtStepI);
      }
      m_confidenceLimitsForTargets.add(limitsForSingleTarget);
    }    
  }

  /**
   * Get the confidence level in use
   * 
   * @return the confidence level
   */
  public double getConfidenceLevel() {

    return m_confidenceLevel;
  }

  /**
   * Get the confidence limits (upper and lower bounds) for the named target
   * at the given step number
   * 
   * @param targetName the name of the target to return the limits for
   * @param targetValue the predicted target value
   * @param stepNum the step number to return the bounds for this target
   * @return an array containing the lower and upper bounds for the supplied
   * target value in elements 0 and 1 respectively.
   * 
   * @throws Exception if a problem occurs while computing the bounds.
   */
  public double[] getConfidenceLimitsForTarget(String targetName, 
      double targetValue, int stepNum)
      throws Exception {

    int index = m_targetFields.indexOf(targetName);
    
    if (index < 0) {
      throw new Exception("[ErrorBasedConfidenceLimitEstimator] " +
      		"unknown target: " + targetName);
    }
    List<double[]> confForTarget = m_confidenceLimitsForTargets.get(index); 
    if (stepNum > confForTarget.size()) {
      throw new Exception("[ErrorBasedConfidenceLimitEstimator] no limits availalbe for" +
      		"requested step number: " + stepNum);
    }
    
    double[] offsets = confForTarget.get(stepNum - 1);
    double[] limits = new double[2];
    limits[0] = targetValue + offsets[0];
    limits[1] = targetValue + offsets[1];
    
    //return confForTarget.get(stepNum - 1);
    return limits;
  }
}
