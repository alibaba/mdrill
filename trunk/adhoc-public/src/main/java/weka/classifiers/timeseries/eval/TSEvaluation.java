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
 *    TSEvaluation.java
 *    Copyright (C) 2010 Pentaho Corporation
 */

package weka.classifiers.timeseries.eval;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.timeseries.AbstractForecaster;
import weka.classifiers.timeseries.TSForecaster;
import weka.classifiers.timeseries.core.OverlayForecaster;
import weka.classifiers.timeseries.core.TSLagMaker;
import weka.classifiers.timeseries.core.TSLagUser;
import weka.classifiers.timeseries.eval.graph.GraphDriver;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

/**
 * Main evaluation routines for time series forecasting models. Also provides a
 * command line interface for building and evaluating time series models.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 51352 $
 */
public class TSEvaluation {

  /**
   * Number of time steps to forecast out for in each forecasting iteration
   * <-horizon>
   */
  protected int m_horizon = 1;

  /**
   * The number of instances to prime the forecaster with prior to each
   * forecasting iteration. <-p>
   */
  protected int m_primeWindowSize = 1;

  /**
   * In the case where there is both training and test data available, setting
   * this to true will prime the forecaster with the first primeWindowSize
   * instances of the test data (instead of the last primeWindowSize instances
   * of the training data) before predicting for the test data. This should be
   * set to true if the test data does not follow immediately in time from the
   * training data. <-primeWithTest>
   */
  protected boolean m_primeForTestDataWithTestData = false;

  /**
   * If true, then the model will be rebuilt after each forecasting iteration on
   * the test data. I.e. if N is the training data and t is the first testing
   * data point, then then after forecasting t, the model will be
   * rebuilt/updated to encompass N + t. Otherwise, the model remains static
   * after N and only lagged inputs will be updated using subsequent test data
   * points. <-r>
   */
  protected boolean m_rebuildModelAfterEachTestForecastStep = false;

  /** Forecast future values beyond the end of the series <-f> */
  protected boolean m_forecastFuture = true;

  /** Evaluate on the training data (if supplied) <-a> */
  protected boolean m_evaluateTrainingData = true;

  /** Evaluate on the test data (if supplied) <-b> */
  protected boolean m_evaluateTestData = true;

  /**
   * A list of errors (and predictions) for the training data. The list is
   * indexed by step (i.e. the first element holds the errors/predictions that
   * are one-step-ahead, the second holds those that are two-steps-ahead etc.).
   */
  protected List<ErrorModule> m_predictionsForTrainingData;

  /**
   * A list of errors (and predictions) for the test data. The list is indexed
   * by step (i.e. the first element holds the errors/predictions that are
   * one-step-ahead, the second holds those that are two-steps-ahead etc.).
   */
  protected List<ErrorModule> m_predictionsForTestData;

  /** Predictions for time steps beyond the end of the training data */
  protected List<List<NumericPrediction>> m_trainingFuture;

  /** Predictions for time steps beyond the end of the test data */
  protected List<List<NumericPrediction>> m_testFuture;

  /**
   * A map (keyed by metric name) of metrics for the training data. The value
   * for a key is a List (indexed by step) of the associated metric.
   */
  protected Map<String, List<TSEvalModule>> m_metricsForTrainingData;

  /**
   * A map (keyed by metric name) of metrics for the test data. The value for a
   * key is a List (indexed by step) of the associated metric.
   */
  protected Map<String, List<TSEvalModule>> m_metricsForTestData;

  /** The evaluation modules to use */
  protected List<TSEvalModule> m_evalModules;

  /** The training data */
  protected Instances m_trainingData;

  /** The test data */
  protected Instances m_testData;

  protected Instances m_dataStructure;

  protected List<Integer> m_missingTargetListTestSet;
  protected List<Integer> m_missingTimeStampListTestSet;
  protected List<String> m_missingTimeStampTestSetRows;

  /**
   * Constructor.
   * 
   * @param trainingData the training data
   * @param testSplitSize the number or percentage of instances to hold out from
   *          the end of the training data to be test data.
   * @throws Exception if a problem occurs.
   */
  public TSEvaluation(Instances trainingData, double testSplitSize)
      throws Exception {

    if (trainingData != null) {
      Instances train = new Instances(trainingData);
      Instances test = null;

      if (testSplitSize > 0) {
        if (trainingData.numInstances() < 2) {
          throw new Exception(
              "Need at least 2 training instances to do hold out evaluation!");
        }
        int numToHoldOut = 0;
        int trainSize = 0;
        if (testSplitSize >= 1) {
          numToHoldOut = (int) testSplitSize;
          trainSize = trainingData.numInstances() - numToHoldOut;
          if (trainSize <= 0) {
            throw new Exception(
                "Can't hold out more instances than there is in the data!");
          }
        } else if (testSplitSize > 0) {
          double trainP = 1.0 - testSplitSize;
          trainSize = (int) Math.round(trainingData.numInstances() * trainP);
          numToHoldOut = trainingData.numInstances() - trainSize;
        }

        train = new Instances(trainingData, 0, trainSize);
        test = new Instances(trainingData, trainSize, numToHoldOut);
      } else if (testSplitSize < 0) {
        throw new Exception("Testing holdout size can't be less than zero!");
      }

      setTrainingData(train);
      setTestData(test);
    } else {
      setEvaluateOnTrainingData(false);
    }

    // default eval modules
    setEvaluationModules("MAE,RMSE"); // automatically adds an error module too
    // setEvaluationModules(""); // automatically adds an error module too
  }

  /**
   * Constructor.
   * 
   * @param trainingData the training data to use
   * @param testData the test data to use
   * @throws Exception if a problem occurs.
   */
  public TSEvaluation(Instances trainingData, Instances testData)
      throws Exception {
    // training or test data may be null (but not both)
    if (trainingData != null && testData != null
        && !trainingData.equalHeaders(testData)) {
      throw new Exception("Training and testing data are not compatible!");
    }

    if (trainingData == null && testData == null) {
      throw new Exception("Can't specify null for both training and test data");
    }

    setTrainingData(trainingData);
    setTestData(testData);

    // default eval modules
    setEvaluationModules("MAE,RMSE"); // automatically adds an error module too
    // setEvaluationModules(""); // automatically adds an error module too
  }

  /**
   * Set the training data to use
   * 
   * @param train the training data to use
   */
  public void setTrainingData(Instances train) {
    if (train == null || train.numInstances() > 0) {
      m_trainingData = train;
      if (m_trainingData == null) {
        m_evaluateTrainingData = false;
      }
    } else {
      m_dataStructure = train;
    }
  }

  /**
   * Set the test data to use
   * 
   * @param testData the test data to use
   */
  public void setTestData(Instances testData) {
    if (testData == null || testData.numInstances() > 0) {
      m_testData = testData;

      if (m_testData == null) {
        m_evaluateTestData = false;
      } else {
        m_evaluateTestData = true;
      }
    } else {
      m_dataStructure = testData;
      m_evaluateTestData = false;
    }
  }

  /**
   * Get the training data (if any)
   * 
   * @return the training data or null if none is in use
   */
  public Instances getTrainingData() {
    return m_trainingData;
  }

  /**
   * Get the test data (if any)
   * 
   * @return the test data or null if none is in use.
   */
  public Instances getTestData() {
    return m_testData;
  }

  /**
   * Set whether to perform evaluation on the training data
   * 
   * @param evalOnTraining true if evaluation is to be performed on the training
   *          data
   */
  public void setEvaluateOnTrainingData(boolean evalOnTraining) {
    m_evaluateTrainingData = evalOnTraining;
  }

  /**
   * Get whether evaluation is to be performed on the training data
   * 
   * @return true if evaluation is to be performed on the training data.
   */
  public boolean getEvaluateOnTrainingData() {
    return m_evaluateTrainingData;
  }

  /**
   * Set whether to perform evaluation on the training data
   * 
   * @param evalOnTest true if evalution is to be performed on the training data
   */
  public void setEvaluateOnTestData(boolean evalOnTest) {
    m_evaluateTestData = evalOnTest;
  }

  /**
   * Get whether evaluation is to be performed on the test data.
   * 
   * @return true if evaluation is to be performed on the test data.
   */
  public boolean getEvaluateOnTestData() {
    return m_evaluateTestData;
  }

  /**
   * Set the horizon - i.e. the number of steps to forecast into the future.
   * 
   * @param horizon the number of steps to forecast into the future
   */
  public void setHorizon(int horizon) {
    m_horizon = horizon;
  }

  /**
   * Set the size of the priming window - i.e. the number of historical
   * instances to be presented to the forecaster before a forecast is requested
   * 
   * @param primeSize the number of instances to prime with
   */
  public void setPrimeWindowSize(int primeSize) {
    m_primeWindowSize = primeSize;
  }

  /**
   * Get the size of the priming window - i.e. the number of historical
   * instances that will be presented to the forecaster before a forecast is
   * requested.
   * 
   * @return the size of the priming window.
   */
  public int getPrimeWindowSize() {
    return m_primeWindowSize;
  }

  /**
   * Set whether evaluation for test data should begin by priming with the first
   * x test data instances and then forecasting from step x + 1. This is the
   * only option if there is no training data and a model has been deserialized
   * from disk. If we have training data, and it occurs immediately before the
   * test data in time, then we can prime with the last x instances from the
   * training data.
   * 
   * @param p true if we should start evaluation of the test data by priming
   *          with the first instances from the test data.
   */
  public void setPrimeForTestDataWithTestData(boolean p) {
    m_primeForTestDataWithTestData = p;
  }

  /**
   * Gets whether evaluation for the test data will begin by priming with the
   * first x instances from the test data and then forecasting from step x + 1.
   * This is the only option if there is no training data and a model has been
   * deserialized from disk. If we have training data, and it occurs immediately
   * before the test data in time, then we can prime with the last x instances
   * from the training data.
   * 
   * @return true if evaluation of the test data will begin by priming with the
   *         first x instances from the test data.
   */
  public boolean getPrimeForTestDataWithTestData() {
    return m_primeForTestDataWithTestData;
  }

  /**
   * Set whether the forecasting model should be rebuilt after each forecasting
   * step on the test data using both the training data and test data up to the
   * current instance.
   * 
   * @param r true if the forecasting model should be rebuilt after each
   *          forecasting step on the test data to take into account all data up
   *          to the current point in time
   */
  public void setRebuildModelAfterEachTestForecastStep(boolean r) {
    m_rebuildModelAfterEachTestForecastStep = r;
  }

  /**
   * Set whether we should generate a future forecast beyond the end of the
   * training and/or test data.
   * 
   * @param future true if future forecasts beyond the end of training/test data
   *          should be generated.
   */
  public void setForecastFuture(boolean future) {
    m_forecastFuture = future;
  }

  /**
   * Get whether future forecasts beyond the end of the training and/or test
   * data will be generated.
   * 
   * @return true if future forecasts will be generated.
   */
  public boolean getForecastFuture() {
    return m_forecastFuture;
  }

  /**
   * Set the evaluation modules to use/
   * 
   * @param evalModNames a comma-separated list of evaluation module names.
   * @throws Exception if there are unknown evaluation modules requested.
   */
  public void setEvaluationModules(String evalModNames) throws Exception {
    String[] names = evalModNames.split(",");

    m_evalModules = new ArrayList<TSEvalModule>();
    // we always want an error module
    m_evalModules.add(new ErrorModule());

    for (String modName : names) {
      if (modName.length() > 0) {
        TSEvalModule mod = TSEvalModule.getModule(modName.trim());
        if (!mod.equals("Error")) {
          m_evalModules.add(mod);
        }
      }
    }
  }

  /*
   * public void setEvaluationModules(List<TSEvalModule> modules) { // check for
   * an error module for (TSEvalModule m : modules) {
   * 
   * }
   * 
   * m_evalModules = modules; }
   */

  /**
   * Get the evaluation modules in use
   * 
   * @return a list of the evaluation modules in use.
   */
  public List<TSEvalModule> getEvaluationModules() {
    return m_evalModules;
  }

  /**
   * Get predictions for all targets for the specified step number on the
   * training data
   * 
   * @param stepNumber number of the step into the future to return predictions
   *          for
   * @return the stepNumber step ahead predictions for all targets
   * @throws Exception if there are no predictions available for the training
   *           data
   */
  public ErrorModule getPredictionsForTrainingData(int stepNumber)
      throws Exception {

    if (m_predictionsForTrainingData == null) {
      throw new Exception("No predictions for training data available!");
    }

    int numSteps = m_predictionsForTrainingData.size();
    if (stepNumber > m_predictionsForTrainingData.size()) {
      throw new Exception("Only predictions up to " + numSteps
          + (numSteps > 1 ? "steps" : "step") + "-ahead are available");
    }

    ErrorModule m = m_predictionsForTrainingData.get(stepNumber - 1);

    return m;
  }

  /**
   * Get predictions for all targets for the specified step number on the test
   * data
   * 
   * @param stepNumber number of the step into the future to return predictions
   *          for
   * @return the stepNumber step ahead predictions for all targets
   * @throws Exception if there are no predictions available for the test data
   */
  public ErrorModule getPredictionsForTestData(int stepNumber) throws Exception {
    if (m_predictionsForTestData == null) {
      throw new Exception("No predictions for test data available!");
    }

    int numSteps = m_predictionsForTestData.size();
    if (stepNumber > m_predictionsForTestData.size()) {
      throw new Exception("Only predictions up to " + numSteps
          + (numSteps > 1 ? "steps" : "step") + "-ahead are available");
    }

    ErrorModule m = m_predictionsForTestData.get(stepNumber - 1);

    return m;
  }

  private void setupEvalModules(List<ErrorModule> predHolders,
      Map<String, List<TSEvalModule>> evalHolders, List<String> fieldsToForecast) {
    for (int i = 0; i < m_horizon; i++) {
      ErrorModule e = new ErrorModule();
      e.setTargetFields(fieldsToForecast);
      predHolders.add(e);
    }

    for (TSEvalModule m : m_evalModules) {
      if (!(m.getEvalName().equals("Error"))) {
        String key = m.getEvalName();

        List<TSEvalModule> evalForSteps = new ArrayList<TSEvalModule>();
        TSEvalModule firstMod = null;
        for (int i = 0; i < m_horizon; i++) {
          TSEvalModule newMod = TSEvalModule.getModule(key);
          newMod.setTargetFields(fieldsToForecast);

          if (i == 0) {
            firstMod = newMod;
          } else {
            if (newMod.getEvalName().equals("RRSE")) {
              // make relative to the one step ahead RRSE module
              ((RRSEModule) newMod)
                  .setRelativeRRSEModule((RRSEModule) firstMod);
            } else if (newMod.getEvalName().equals("RAE")) {
              ((RAEModule) newMod).setRelativeRAEModule((RAEModule) firstMod);
            }
          }
          evalForSteps.add(newMod);
        }

        evalHolders.put(key, evalForSteps);
      }
    }
  }

  private void updateEvalModules(List<ErrorModule> predHolders,
      Map<String, List<TSEvalModule>> evalHolders,
      List<List<NumericPrediction>> predsForSteps, int currentInstanceNum,
      Instances toPredict) throws Exception {

    // errors first
    for (int i = 0; i < m_horizon; i++) {
      // when using overlay data there will only be as many predictions as there
      // are
      // instances to predict
      if (i < predsForSteps.size()) {
        List<NumericPrediction> predsForStepI = predsForSteps.get(i);
        if (currentInstanceNum + i < toPredict.numInstances()) {
          predHolders.get(i).evaluateForInstance(predsForStepI,
              toPredict.instance(currentInstanceNum + i));
        } else {
          predHolders.get(i).evaluateForInstance(predsForStepI, null);
        }
      }
    }

    // other evaluation metrics
    for (TSEvalModule m : m_evalModules) {
      if (!(m.getEvalName().equals("Error"))) {
        String key = m.getEvalName();

        List<TSEvalModule> evalForSteps = evalHolders.get(key);
        for (int i = 0; i < m_horizon; i++) {
          if (i < predsForSteps.size()) {
            List<NumericPrediction> predsForStepI = predsForSteps.get(i);
            if (currentInstanceNum + i < toPredict.numInstances()) {
              evalForSteps.get(i).evaluateForInstance(predsForStepI,
                  toPredict.instance(currentInstanceNum + i));
            } else {
              evalForSteps.get(i).evaluateForInstance(predsForStepI, null);
            }
          }
        }
      }
    }
  }

  /**
   * Evaluate the supplied forecaster. Trains the forecaster if a training set
   * has been configured.
   * 
   * @param forecaster the forecaster to evaluate
   * @throws Exception if something goes wrong during evaluation
   */
  public void evaluateForecaster(TSForecaster forecaster,
      PrintStream... progress) throws Exception {
    evaluateForecaster(forecaster, true, progress);
  }

  /**
   * Creates overlay data to use during evaluation.
   * 
   * @param forecaster the forecaster being evaluated
   * @param source the source instances
   * @param start the index of the first instance to be part of the overlay data
   * @param numSteps the number of steps to forecast (and hence the number of
   *          instances to make up the overlay data set
   * @return the overlay instances.
   */
  protected Instances createOverlayForecastData(TSForecaster forecaster,
      Instances source, int start, int numSteps) {

    int toCopy = Math.min(numSteps, source.numInstances() - start);
    Instances overlay = new Instances(source, start, toCopy);

    // set all targets to missing
    List<String> fieldsToForecast = AbstractForecaster.stringToList(forecaster
        .getFieldsToForecast());
    for (int i = 0; i < overlay.numInstances(); i++) {
      Instance current = overlay.instance(i);
      for (String target : fieldsToForecast) {
        current.setValue(overlay.attribute(target), Utils.missingValue());
      }
    }

    return overlay;
  }

  /**
   * Evaluate a forecaster on training and/or test data.
   * 
   * @param forecaster the forecaster to evaluate
   * @param buildModel true if the model is to be built (given that there is a
   *          training data set to build it with)
   * @throws Exception if something goes wrong during evaluation
   */
  public void evaluateForecaster(TSForecaster forecaster, boolean buildModel,
      PrintStream... progress) throws Exception {
    m_predictionsForTrainingData = null;
    m_predictionsForTestData = null;
    m_trainingFuture = null;
    m_testFuture = null;

    // train the forecaster first (if necessary)
    if (m_trainingData != null && buildModel) {
      for (PrintStream p : progress) {
        p.println("Building forecaster...");
      }
      forecaster.buildForecaster(m_trainingData);
    }

    // We need to know if the forecaster's lag maker has
    // inserted any entirely missing rows into the training data
    if (forecaster instanceof TSLagUser) {

      // Check training data even if there is no evaluation on the
      // training data as output for a future forecast beyond the
      // end of the training data will require updated training data
      // too
      TSLagMaker lagMaker = ((TSLagUser) forecaster).getTSLagMaker();
      Instances trainMod = new Instances(m_trainingData);
      trainMod = weka.classifiers.timeseries.core.Utils.replaceMissing(
          trainMod, lagMaker.getFieldsToLag(), lagMaker.getTimeStampField(),
          false, lagMaker.getPeriodicity(), lagMaker.getSkipEntries());

      if (trainMod.numInstances() != m_trainingData.numInstances()) {
        m_trainingData = trainMod;
      }

      if (m_evaluateTestData) {
        m_missingTimeStampTestSetRows = new ArrayList<String>();
        m_missingTargetListTestSet = new ArrayList<Integer>();
        m_missingTimeStampListTestSet = new ArrayList<Integer>();

        Instances testMod = new Instances(m_testData);
        testMod = weka.classifiers.timeseries.core.Utils.replaceMissing(
            testMod, lagMaker.getFieldsToLag(), lagMaker.getTimeStampField(),
            false, lagMaker.getPeriodicity(), lagMaker.getSkipEntries(),
            m_missingTargetListTestSet, m_missingTimeStampListTestSet,
            m_missingTimeStampTestSetRows);

        if (testMod.numInstances() != m_testData.numInstances()) {
          m_testData = testMod;
        }
      }

    }

    if (m_evaluateTrainingData) {
      for (PrintStream p : progress) {
        p.println("Evaluating on training set...");
      }

      // set up training set prediction and eval modules
      m_predictionsForTrainingData = new ArrayList<ErrorModule>();
      m_metricsForTrainingData = new HashMap<String, List<TSEvalModule>>();
      setupEvalModules(m_predictionsForTrainingData, m_metricsForTrainingData,
          AbstractForecaster.stringToList(forecaster.getFieldsToForecast()));

      Instances primeData = new Instances(m_trainingData, 0);
      if (forecaster instanceof TSLagUser) {
        // if an artificial time stamp is being used, make sure it is reset for
        // evaluating the training data
        if (((TSLagUser) forecaster).getTSLagMaker()
            .isUsingAnArtificialTimeIndex()) {
          ((TSLagUser) forecaster).getTSLagMaker().setArtificialTimeStartValue(
              m_primeWindowSize);
        }
      }
      for (int i = 0; i < m_trainingData.numInstances(); i++) {
        Instance current = m_trainingData.instance(i);

        if (i < m_primeWindowSize) {
          primeData.add(current);
        } else {
          if (i % 10 == 0) {
            for (PrintStream p : progress) {
              p.println("Evaluating on training set: processed " + i
                  + " instances...");
            }
          }

          forecaster.primeForecaster(primeData);
          /*
           * System.err.println(primeData); System.exit(1);
           */

          List<List<NumericPrediction>> forecast = null;
          if (forecaster instanceof OverlayForecaster
              && ((OverlayForecaster) forecaster).isUsingOverlayData()) {

            // can only generate forecasts for remaining training data that
            // we can use as overlay data
            if (current != null) {
              Instances overlay = createOverlayForecastData(forecaster,
                  m_trainingData, i, m_horizon);

              forecast = ((OverlayForecaster) forecaster).forecast(m_horizon,
                  overlay, progress);
            }
          } else {
            forecast = forecaster.forecast(m_horizon, progress);
          }

          updateEvalModules(m_predictionsForTrainingData,
              m_metricsForTrainingData, forecast, i, m_trainingData);

          // remove the oldest prime instance and add this one
          if (m_primeWindowSize > 0 && current != null) {
            primeData.remove(0);
            primeData.add(current);
            primeData.compactify();
          }
        }
      }
    }

    if (m_trainingData != null && m_forecastFuture
    /* && !m_evaluateTrainingData */) {

      // generate a forecast beyond the end of the training data
      for (PrintStream p : progress) {
        p.println("Generating future forecast for training data...");
      }
      Instances primeData = new Instances(m_trainingData,
          m_trainingData.numInstances() - m_primeWindowSize, m_primeWindowSize);

      forecaster.primeForecaster(primeData);

      // if overlay data is being used then the only way we can make a forecast
      // beyond the end of the training data is if we have a held-out test set
      // to
      // use for the "future" overlay fields values
      if (forecaster instanceof OverlayForecaster
          && ((OverlayForecaster) forecaster).isUsingOverlayData()) {
        if (m_testData != null) {
          Instances overlay = createOverlayForecastData(forecaster, m_testData,
              0, m_horizon);
          m_trainingFuture = ((OverlayForecaster) forecaster).forecast(
              m_horizon, overlay, progress);
        } else {
          // print an error message
          for (PrintStream p : progress) {
            p.println("WARNING: Unable to generate a future forecast beyond the end "
                + "of the training data because there is no future overlay "
                + "data available.");
          }
        }
      } else {
        m_trainingFuture = forecaster.forecast(m_horizon);
      }
    }

    if (m_evaluateTestData) {
      for (PrintStream p : progress) {
        p.println("Evaluating on test set...");
      }

      // set up training set prediction and eval modules
      m_predictionsForTestData = new ArrayList<ErrorModule>();
      m_metricsForTestData = new HashMap<String, List<TSEvalModule>>();
      setupEvalModules(m_predictionsForTestData, m_metricsForTestData,
          AbstractForecaster.stringToList(forecaster.getFieldsToForecast()));

      Instances primeData = null;
      Instances rebuildData = null;
      if (m_trainingData != null) {
        primeData = new Instances(m_trainingData, 0);
        if (forecaster instanceof TSLagUser) {
          // initialize the artificial time stamp value (if in use)
          if (((TSLagUser) forecaster).getTSLagMaker()
              .isUsingAnArtificialTimeIndex()) {
            ((TSLagUser) forecaster).getTSLagMaker()
                .setArtificialTimeStartValue(m_trainingData.numInstances());
          }
        }
        if (m_rebuildModelAfterEachTestForecastStep) {
          rebuildData = new Instances(m_trainingData);
        }
      } else {
        primeData = new Instances(m_testData, 0);
      }

      int predictionOffsetForTestData = 0;
      if (m_trainingData == null || m_primeForTestDataWithTestData) {
        if (m_primeWindowSize >= m_testData.numInstances()) {
          throw new Exception("The test data needs to have at least as many "
              + "instances as the the priming window size!");
        }
        predictionOffsetForTestData = m_primeWindowSize;
        if (predictionOffsetForTestData >= m_testData.numInstances()) {
          throw new Exception(
              "Priming using test data requires more instances "
                  + "than are available in the test data!");
        }
        primeData = new Instances(m_testData, 0, m_primeWindowSize);

        if (forecaster instanceof TSLagUser) {
          if (((TSLagUser) forecaster).getTSLagMaker()
              .isUsingAnArtificialTimeIndex()) {
            double artificialTimeStampStart = 0;
            if (m_primeForTestDataWithTestData) {
              if (m_trainingData == null) {
                artificialTimeStampStart = ((TSLagUser) forecaster)
                    .getTSLagMaker().getArtificialTimeStartValue();
                artificialTimeStampStart += m_primeWindowSize;
                ((TSLagUser) forecaster).getTSLagMaker()
                    .setArtificialTimeStartValue(artificialTimeStampStart);
              } else {
                ((TSLagUser) forecaster).getTSLagMaker()
                    .setArtificialTimeStartValue(
                        m_trainingData.numInstances() + m_primeWindowSize);
              }
            }
          }
        }
      } else {
        // use the last primeWindowSize instances from the training
        // data to prime with
        predictionOffsetForTestData = 0;
        primeData = new Instances(m_trainingData,
            (m_trainingData.numInstances() - m_primeWindowSize),
            m_primeWindowSize);

      }

      for (int i = predictionOffsetForTestData; i < m_testData.numInstances(); i++) {
        Instance current = m_testData.instance(i);
        if (m_primeWindowSize > 0) {
          forecaster.primeForecaster(primeData);
        }

        if (i % 10 == 0) {
          for (PrintStream p : progress) {
            p.println("Evaluating on test set: processed " + i
                + " instances...");
          }
        }
        List<List<NumericPrediction>> forecast = null;
        if (forecaster instanceof OverlayForecaster
            && ((OverlayForecaster) forecaster).isUsingOverlayData()) {

          // can only generate forecasts for remaining test data that
          // we can use as overlay data
          if (current != null) {
            Instances overlay = createOverlayForecastData(forecaster,
                m_testData, i, m_horizon);

            forecast = ((OverlayForecaster) forecaster).forecast(m_horizon,
                overlay, progress);
          }
        } else {
          forecast = forecaster.forecast(m_horizon, progress);
        }

        updateEvalModules(m_predictionsForTestData, m_metricsForTestData,
            forecast, i, m_testData);

        // remove the oldest prime instance and add this one
        if (m_primeWindowSize > 0 && current != null) {
          primeData.remove(0);
          primeData.add(current);
          primeData.compactify();
        }

        // add the current instance to the training data and rebuild the
        // forecaster (if requested and if there is training data).
        if (m_rebuildModelAfterEachTestForecastStep && rebuildData != null) {
          rebuildData.add(current);
          forecaster.buildForecaster(rebuildData);
        }
      }
    }

    if (m_testData != null && m_forecastFuture
    /* && !m_evaluateTestData */) {
      // generate a forecast beyond the end of the training data
      // if we have training data but we're not evaluating on
      // the training data (evaluation gets us the future forecast
      // as a by-product of evaluating
      for (PrintStream p : progress) {
        p.println("Generating future forecast for test data...");
      }

      // If we need more priming data than is available in the test
      // set then use some of the training data (if available)
      Instances primeData = null;
      if (m_primeWindowSize > m_testData.numInstances()) {
        int difference = m_primeWindowSize - m_testData.numInstances();
        if (m_trainingData != null) {
          if (difference > m_trainingData.numInstances()) {
            primeData = new Instances(m_trainingData);
          } else {
            primeData = new Instances(m_trainingData,
                m_trainingData.numInstances() - difference, difference);
          }
        }
        // now add all the test data too
        for (int z = 0; z < m_testData.numInstances(); z++) {
          primeData.add(m_testData.instance(z));
        }
      } else {
        primeData = new Instances(m_testData, m_testData.numInstances()
            - m_primeWindowSize, m_primeWindowSize);
      }
      forecaster.primeForecaster(primeData);

      if (forecaster instanceof OverlayForecaster
          && ((OverlayForecaster) forecaster).isUsingOverlayData()) {
        // There is no way to produce a forecast beyond the end of
        // the test data because we have no future overlay data

        for (PrintStream p : progress) {
          p.println("WARNING: Unable to generate a future forecast beyond the end "
              + "of the test data because there is no future overlay "
              + "data available.");
        }
      } else {
        m_testFuture = forecaster.forecast(m_horizon, progress);
      }
    }
  }

  /**
   * Construct a string containing all general and forecaster-specific options.
   * 
   * @param forecaster the forecaster to use
   * @param globalInfo true if global information (about info) on the forecaster
   *          is to be included in the string
   * @return a string containing all general and forecaster-specific options +
   *         possibly global about info
   */
  protected static String makeOptionString(TSForecaster forecaster,
      boolean globalInfo) {
    StringBuffer optionsText = new StringBuffer("");

    // General options
    optionsText.append("\n\tGeneral options:\n\n");
    optionsText.append("-h or -help\n");
    optionsText.append("\tOutput help information.\n");
    optionsText.append("-synopsis or -info\n");
    optionsText.append("\tOutput synopsis for forecaster (use in conjunction "
        + " with -h)\n");
    optionsText.append("-t <name of training file>\n");
    optionsText.append("\tSets training file.\n");
    optionsText.append("-T <name of test file>\n");
    optionsText.append("\tSets test file.\n");
    optionsText.append("-holdout <#instances | percentage>\n");
    optionsText.append("\tSets the number of instances or percentage of"
        + " the training\n\tdata to hold out for testing.\n");
    optionsText.append("-horizon <#steps>\n");
    optionsText
        .append("\tThe maximum number of steps to forecast into the future.\n");
    optionsText.append("-prime <#instances>\n");
    optionsText
        .append("\tThe number of instances to prime the forecaster with "
            + "before\n\tgenerating a forecast.\n");
    optionsText.append("-metrics <list of metrics>\n");
    optionsText.append("\tA list of metrics to use for evaluation."
        + "\n\t(default: MAE,RMSE)\n");
    optionsText.append("-p <target:step#>\n");
    optionsText
        .append("\tOutput predictions for the specified target at the\n\t"
            + "specified step-ahead level.\n");
    optionsText.append("-graph <file name:target list:step list>\n");
    optionsText
        .append("\tGenerate a PNG graph of predictions for the specified\n\t"
            + "target(s) at the specified step-ahead level(s) on the training\n\t"
            + "and/or test data. Note that only one of the targets or steps can\n\t"
            + " be a list - i.e. either a single target is plotted for multiple "
            + "step-ahead levels, or, multiple targets are plotted at a single\n\t"
            + " step-ahead level.\n");
    optionsText.append("-future\n");
    optionsText
        .append("\tOutput training/test data plus predictions for all\n\t"
            + "targets up to horizon steps into the future.\n");
    optionsText.append("-future-graph <file name>\n");
    optionsText
        .append("\tGenerate a PNG graph of future predictions (beyond the end\n\t"
            + "of the series) for the training and/or test data for all targets.\n");
    optionsText.append("-l <name of input file>\n");
    optionsText.append("\tSets model input file.\n");
    optionsText.append("-d <name of output file>\n");
    optionsText.append("\tSets model output file.\n");

    // Get scheme-specific options

    if (forecaster instanceof OptionHandler) {
      optionsText.append("\nOptions specific to "
          + forecaster.getClass().getName() + ":\n\n");

      Enumeration enu = ((OptionHandler) forecaster).listOptions();
      while (enu.hasMoreElements()) {
        Option option = (Option) enu.nextElement();
        optionsText.append(option.synopsis()).append("\n");
        optionsText.append(option.description()).append("\n");
      }
    }

    // Get global information (if available)
    if (globalInfo) {
      try {
        String gi = getGlobalInfo(forecaster);
        optionsText.append(gi);
      } catch (Exception ex) {
        // quietly ignore
      }
    }

    return optionsText.toString();
  }

  /**
   * Return the global info (if it exists) for the supplied forecaster.
   * 
   * @param forecaster the forecaster to get the global info for
   * @return the global info (synopsis) for the classifier
   * @throws Exception if there is a problem reflecting on the forecaster
   */
  protected static String getGlobalInfo(TSForecaster forecaster)
      throws Exception {
    BeanInfo bi = Introspector.getBeanInfo(forecaster.getClass());
    MethodDescriptor[] methods;
    methods = bi.getMethodDescriptors();
    Object[] args = {};
    String result = "\nSynopsis for " + forecaster.getClass().getName()
        + ":\n\n";

    for (int i = 0; i < methods.length; i++) {
      String name = methods[i].getDisplayName();
      Method meth = methods[i].getMethod();
      if (name.equals("globalInfo")) {
        String globalInfo = (String) (meth.invoke(forecaster, args));
        result += globalInfo;
        break;
      }
    }

    return result;
  }

  /**
   * Evaluate the supplied forecaster using the supplied command-line options.
   * 
   * @param forecaster the forecaster to evaluate
   * @param options an array of command-line options
   * @throws Exception if a problem occurs during evaluation
   */
  public static void evaluateForecaster(TSForecaster forecaster,
      String[] options) throws Exception {

    if (Utils.getFlag('h', options) || Utils.getFlag("-help", options)) {

      // global info requested as well?
      boolean globalInfo = Utils.getFlag("synopsis", options)
          || Utils.getFlag("info", options);

      throw new Exception("\nHelp requested."
          + makeOptionString(forecaster, globalInfo));
    }

    String trainingFileName = Utils.getOption('t', options);
    String testingFileName = Utils.getOption('T', options);
    String testSizeS = Utils.getOption("holdout", options);
    String loadForecasterName = Utils.getOption('l', options);
    String saveForecasterName = Utils.getOption('d', options);
    String horizonS = Utils.getOption("horizon", options);
    String primeWindowSizeS = Utils.getOption("prime", options);
    String evalModuleList = Utils.getOption("metrics", options);
    String outputPredictionsS = Utils.getOption('p', options);
    String saveGraphS = Utils.getOption("graph", options);
    boolean printFutureForecast = Utils.getFlag("future", options);
    String graphFutureForecastS = Utils.getOption("future-graph", options);

    Instances inputHeader = null;
    Instances trainingData = null;
    Instances testData = null;
    int testSize = 0;
    int horizon = 1;
    int primeWindowSize = 1;
    boolean primeWithTest = Utils.getFlag("primeWithTest", options);
    boolean rebuildModelAfterEachStep = Utils.getFlag('r', options);
    boolean noEvalForTrainingData = Utils.getFlag('v', options);

    if (loadForecasterName.length() > 0 && trainingFileName.length() > 0) {
      System.out.println("When a model is loaded from a file any training"
          + " data is only used for priming the model.");
    }

    if (loadForecasterName.length() > 0) {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
          loadForecasterName));
      forecaster = (TSForecaster) ois.readObject();

      // try and read the header
      try {
        inputHeader = (Instances) ois.readObject();
      } catch (Exception ex) {
      }

      ois.close();
      // TODO should we save the last primeWindowSize instances of the training
      // data
      // for priming the model (probably not)?
    }

    if (testingFileName.length() > 0 && testSizeS.length() > 0) {
      throw new Exception("Can't specify both a test file and a holdout size.");
    }

    if (testingFileName.length() == 0 && primeWithTest) {
      throw new Exception("Can't prime the forecaster with data from the test "
          + "file if no test file is specified!");
    }

    if (testSizeS.length() > 0) {
      testSize = Integer.parseInt(testSizeS);
    }

    if (testingFileName.length() == 0 && testSize == 0
        && rebuildModelAfterEachStep) {
      throw new Exception(
          "Can't rebuild the model after each forecasting step on"
              + " the test data if there is no test data specified or no holdout"
              + " size specified!");
    }

    if (trainingFileName.length() > 0) {
      trainingData = new Instances(new BufferedReader(new FileReader(
          trainingFileName)));
    } else {
      noEvalForTrainingData = true;
    }

    if (testingFileName.length() > 0) {
      testData = new Instances(new BufferedReader(new FileReader(
          testingFileName)));
    }

    if (horizonS.length() > 0) {
      horizon = Integer.parseInt(horizonS);
    }

    if (primeWindowSizeS.length() > 0) {
      primeWindowSize = Integer.parseInt(primeWindowSizeS);
    } else {
      throw new Exception("Must specify the number of instances to prime the "
          + "forecaster with for generating predictions (-prime)");
    }

    // output predictions setup
    String outputPredsForTarget = null;
    int stepNum = 1;
    if (outputPredictionsS.length() > 0) {
      String[] parts = outputPredictionsS.split(":");
      outputPredsForTarget = parts[0].trim();
      if (parts.length > 1) {
        stepNum = Integer.parseInt(parts[1]);
      }
    }

    // save graph setup
    String saveGraphFileName = null;
    List<String> graphTargets = null;
    List<Integer> stepsToPlot = new ArrayList<Integer>();
    graphTargets = new ArrayList<String>();
    if (saveGraphS.length() > 0) {
      String[] parts = saveGraphS.split(":");
      saveGraphFileName = parts[0].trim();
      String targets = parts[1].trim();
      String[] targetParts = targets.split(",");
      for (String s : targetParts) {
        graphTargets.add(s.trim());
      }
      if (parts.length > 2) {
        String[] graphStepNums = parts[2].split(",");
        // graphStepNum = Integer.parseInt(parts[2]);
        for (String s : graphStepNums) {
          Integer step = new Integer(Integer.parseInt(s.trim()));
          if (step < 1 || step > horizon) {
            throw new Exception("Can't specify a step to graph that is less "
                + "than 1 or greater than the selected horizon!");
          }
          stepsToPlot.add(step);
        }
      } else {
        stepsToPlot.add(new Integer(1));
      }
    }

    if (graphTargets.size() > 1 && stepsToPlot.size() > 1) {
      throw new Exception(
          "Can't specify multiple targets to plot and multiple steps. "
              + "Specify either multiple target names to plot for a single step, or, "
              + "a single target to plot at multiple steps.");
    }

    if (forecaster instanceof OptionHandler && loadForecasterName.length() == 0) {
      ((OptionHandler) forecaster).setOptions(options);
    }

    TSEvaluation eval = new TSEvaluation(trainingData, testSize);
    if (testData != null) {
      eval.setTestData(testData);
    }
    eval.setHorizon(horizon);
    eval.setPrimeWindowSize(primeWindowSize);
    eval.setPrimeForTestDataWithTestData(primeWithTest);
    eval.setRebuildModelAfterEachTestForecastStep(rebuildModelAfterEachStep);
    eval.setForecastFuture(printFutureForecast
        || graphFutureForecastS.length() > 0);
    if (evalModuleList.length() > 0) {
      eval.setEvaluationModules(evalModuleList);
    }
    eval.setEvaluateOnTrainingData(!noEvalForTrainingData);

    if (loadForecasterName.length() == 0) {
      forecaster.buildForecaster(eval.getTrainingData());
    }

    System.out.println(forecaster.toString());

    // save the forecaster if requested
    if (saveForecasterName.length() > 0) {
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
          saveForecasterName));
      oos.writeObject(forecaster);
      oos.writeObject(new Instances(eval.getTrainingData(), 0));
      oos.close();
    }

    eval.evaluateForecaster(forecaster, false);

    if (outputPredsForTarget != null && trainingData != null
        && !noEvalForTrainingData) {

      System.out.println(eval.printPredictionsForTrainingData(
          "=== Predictions " + "for training data: " + outputPredsForTarget
              + " (" + stepNum
              + (stepNum > 1 ? "-steps ahead)" : "-step ahead)") + " ===",
          outputPredsForTarget, stepNum, primeWindowSize));
    }

    if (outputPredsForTarget != null && (testData != null || testSize > 0)) {

      int instanceNumberOffset = (trainingData != null && testSize > 0) ? eval
          .getTrainingData().numInstances() : 0;

      if (primeWithTest) {
        instanceNumberOffset += primeWindowSize;
      }

      System.out.println(eval.printPredictionsForTestData(
          "=== Predictions for test data: " + outputPredsForTarget + " ("
              + stepNum + (stepNum > 1 ? "-steps ahead)" : "-step ahead)")
              + " ===", outputPredsForTarget, stepNum, instanceNumberOffset));
    }

    if (printFutureForecast) {
      if (trainingData != null && !noEvalForTrainingData) {
        System.out
            .println("=== Future predictions from end of training data ===\n");
        System.out.println(eval.printFutureTrainingForecast(forecaster));
      }

      if (testData != null || testSize > 0) {
        System.out
            .println("=== Future predictions from end of test data ===\n");
        System.out.println(eval.printFutureTestForecast(forecaster));
      }
    }

    System.out.println(eval.toSummaryString());

    if (saveGraphFileName != null && trainingData != null
        && !noEvalForTrainingData) {
      if (graphTargets.size() >= 1
          || (graphTargets.size() == 1 && stepsToPlot.size() == 1)) {
        JPanel result = eval.graphPredictionsForTargetsOnTraining(
            GraphDriver.getDefaultDriver(), forecaster, graphTargets,
            stepsToPlot.get(0), primeWindowSize);

        GraphDriver.getDefaultDriver().saveChartToFile(result,
            saveGraphFileName + "_train", 1000, 600);

      } else {
        JPanel result = eval.graphPredictionsForStepsOnTraining(
            GraphDriver.getDefaultDriver(), forecaster, graphTargets.get(0),
            stepsToPlot, primeWindowSize);

        GraphDriver.getDefaultDriver().saveChartToFile(result,
            saveGraphFileName + "_train", 1000, 600);
      }
    }

    if (saveGraphFileName != null && (testData != null || testSize > 0)) {
      int instanceOffset = (primeWithTest) ? primeWindowSize : 0;
      if (graphTargets.size() > 1
          || (graphTargets.size() == 1 && stepsToPlot.size() == 1)) {
        JPanel result = eval.graphPredictionsForTargetsOnTesting(
            GraphDriver.getDefaultDriver(), forecaster, graphTargets,
            stepsToPlot.get(0), instanceOffset);

        GraphDriver.getDefaultDriver().saveChartToFile(result,
            saveGraphFileName + "_test", 1000, 600);
      } else {
        JPanel result = eval.graphPredictionsForStepsOnTesting(
            GraphDriver.getDefaultDriver(), forecaster, graphTargets.get(0),
            stepsToPlot, instanceOffset);

        GraphDriver.getDefaultDriver().saveChartToFile(result,
            saveGraphFileName + "_test", 1000, 600);
      }
    }

    if (graphFutureForecastS.length() > 0) {
      if (trainingData != null && !noEvalForTrainingData
          && eval.m_trainingFuture != null) {
        JPanel result = eval.graphFutureForecastOnTraining(
            GraphDriver.getDefaultDriver(), forecaster,
            AbstractForecaster.stringToList(forecaster.getFieldsToForecast()));

        GraphDriver.getDefaultDriver().saveChartToFile(result,
            graphFutureForecastS + "_train", 1000, 600);
      }

      if (testData != null || testSize > 0 && eval.m_testFuture != null) {
        JPanel result = eval.graphFutureForecastOnTesting(
            GraphDriver.getDefaultDriver(), forecaster,
            AbstractForecaster.stringToList(forecaster.getFieldsToForecast()));

        GraphDriver.getDefaultDriver().saveChartToFile(result,
            graphFutureForecastS + "_test", 1000, 600);
      }
    }
  }

  /**
   * Print a textual future forecast. Prints the historical supplied targets
   * followed by the forecasted values
   * 
   * @param forecaster the forecaster used to generate the supplied predictions
   * @param futureForecast the future forecasted values as a list of list of
   *          NumericPrediction objects. The outer list is indexed by step, i.e.
   *          the first element contains the predictions for t+1, the second for
   *          t+2, .... The inner list is indexed by target.
   * @param insts the historical instances
   * @return a String containing the historical data and forecasted values
   * @throws Exception if a problem occurs.
   */
  protected String printFutureForecast(TSForecaster forecaster,
      List<List<NumericPrediction>> futureForecast, Instances insts)
      throws Exception {

    if (futureForecast == null) {
      if (forecaster instanceof OverlayForecaster
          && ((OverlayForecaster) forecaster).isUsingOverlayData()) {
        return "Unable to generate future forecast because there is no "
            + "future overlay data available.";
      } else {
        return "Unable to generate future forecast.";
      }
    }

    boolean timeIsInstanceNum = true;
    double timeStart = 1;
    double timeDelta = 1;
    String dateFormat = null;
    TSLagMaker lagMaker = null;
    if (forecaster instanceof TSLagUser) {
      lagMaker = ((TSLagUser) forecaster).getTSLagMaker();

      if (!lagMaker.isUsingAnArtificialTimeIndex()
          && lagMaker.getAdjustForTrends()) {
        // suss out the time stamp
        String timeStampName = lagMaker.getTimeStampField();
        if (insts.attribute(timeStampName).isDate()) {
          dateFormat = insts.attribute(timeStampName).getDateFormat();
        }
        timeStart = insts.instance(0).value(insts.attribute(timeStampName));
        timeDelta = lagMaker.getDeltaTime();
        timeIsInstanceNum = false;
      }
    }

    List<String> targets = AbstractForecaster.stringToList(forecaster
        .getFieldsToForecast());
    int maxColWidth = 1;
    for (String t : targets) {
      if (t.length() > maxColWidth) {
        maxColWidth = t.length();
      }
    }

    int maxTimeColWidth = "inst#".length();
    SimpleDateFormat format = null;
    if (dateFormat != null) {
      // timestamp needs to be interpreted as a date
      maxTimeColWidth = "Time".length();
      format = new SimpleDateFormat(dateFormat);
      String formatted = format.format(new Date((long) timeStart));
      if (formatted.length() > maxTimeColWidth) {
        maxTimeColWidth = formatted.length();
      }
    } else {
      // covers instance numbers and numeric timestamps
      double maxTime = timeStart
          + ((insts.numInstances() + m_horizon) * timeDelta);
      maxTimeColWidth = maxWidth(maxTimeColWidth, maxTime);
    }

    for (int i = 0; i < insts.numInstances(); i++) {
      Instance current = insts.instance(i);
      for (String t : targets) {
        if (!current.isMissing(insts.attribute(t))) {
          maxColWidth = maxWidth(maxColWidth, current.value(insts.attribute(t)));
        }
      }
    }

    StringBuffer result = new StringBuffer();
    if (dateFormat != null) {
      result.append(pad("Time", " ", maxTimeColWidth, false)).append("  ");
    } else {
      result.append(pad("inst#", " ", maxTimeColWidth, false)).append("  ");
    }

    for (String t : targets) {
      result.append(pad(t, " ", maxColWidth, true)).append(" ");
    }
    result.append("\n");

    int instNum = (int) timeStart;
    long dateTime = (long) timeStart;
    for (int i = 0; i < insts.numInstances() + futureForecast.size(); i++) {
      String predMarker = (i < insts.numInstances()) ? "" : "*";
      if (timeIsInstanceNum) {
        result.append(
            pad("" + instNum + predMarker, " ", maxTimeColWidth, false))
            .append("  ");
        instNum += (int) timeDelta;
      } else if (dateFormat != null) {
        result.append(
            pad(format.format(new Date(dateTime)) + predMarker, " ",
                maxTimeColWidth, false)).append("  ");
        if (lagMaker != null) {
          dateTime = (long) lagMaker.advanceSuppliedTimeValue(dateTime);
        } else {
          dateTime += (long) timeDelta;
        }
      } else {
        result.append(
            pad(Utils.doubleToString(timeStart, 4) + predMarker, " ",
                maxTimeColWidth, false)).append("  ");
        timeStart += timeDelta;
      }

      if (i < insts.numInstances()) {
        Instance current = insts.instance(i);
        for (String t : targets) {
          if (!current.isMissing(insts.attribute(t))) {
            double value = current.value(insts.attribute(t));
            result.append(
                pad(Utils.doubleToString(value, 4), " ", maxColWidth, true))
                .append(" ");
          } else {
            result.append(pad("?", " ", maxColWidth, true)).append(" ");
          }
        }
      } else {
        if (futureForecast != null) {
          int step = i - insts.numInstances();
          List<NumericPrediction> preds = futureForecast.get(step);
          // loop over the targets
          for (NumericPrediction p : preds) {
            result.append(
                pad(Utils.doubleToString(p.predicted(), 4), " ", maxColWidth,
                    true)).append(" ");
          }
        }
      }
      result.append("\n");
    }

    return result.toString();
  }

  /**
   * Print the target values from the test data followed by the future forecast
   * from the end of the test data.
   * 
   * @param forecaster the forecaster in use
   * @return a String containing the test target values and the future
   *         forecasted values beyond the end of the test data
   * @throws Exception if something goes wrong.
   */
  public String printFutureTestForecast(TSForecaster forecaster)
      throws Exception {

    if (m_testData == null || m_testData.numInstances() == 0) {
      throw new Exception("Can't forecast beyond the end of the"
          + " test instances because no test instances have been "
          + "supplied!");
    }

    return printFutureForecast(forecaster, m_testFuture, m_testData);
  }

  /**
   * Print the forecasted values (for all targets) beyond the end of the
   * training data
   * 
   * @param forecaster the forecaster
   * @return A string which contains the target values from the training data
   *         followed by predictions for all targets for 1 - horizon number of
   *         steps into the future.
   */
  public String printFutureTrainingForecast(TSForecaster forecaster)
      throws Exception {

    if (m_trainingData == null || m_trainingData.numInstances() == 0) {
      throw new Exception("Can't forecast beyond the end of the"
          + " training instances because no training instances have been "
          + "supplied!");
    }

    // Assume that client has called evaluateForecaster first
    /*
     * // make sure that the evaluation has been run! if
     * (m_predictionsForTrainingData == null && m_trainingFuture == null) {
     * evaluateForecaster(forecaster); }
     */

    return printFutureForecast(forecaster, m_trainingFuture, m_trainingData);
  }

  /**
   * Graph historical and future forecasted values using a graph driver.
   * 
   * @param driver the graph driver to graph with
   * @param forecaster the forecaster in use
   * @param targetNames the names of the targets to graph
   * @param preds a list (indexed by step) of list (indexed by target) of
   *          predictions
   * @param history Instances containing historical target values
   * @return a JPanel containing the graph
   * @throws Exception if a problem occurs during graphing
   */
  protected JPanel graphFutureForecast(GraphDriver driver,
      TSForecaster forecaster, List<String> targetNames,
      List<List<NumericPrediction>> preds, Instances history) throws Exception {

    JPanel result = driver.getPanelFutureForecast(forecaster, preds,
        targetNames, history);

    return result;
  }

  /**
   * Graph historical and future forecasted values using a graph driver for the
   * training data.
   * 
   * @param driver the graph driver to graph with
   * @param forecaster the forecaster in use
   * @param targetNames the names of the targets to graph
   * @return a JPanel containing the graph
   * @throws Exception if a problem occurs during graphing
   */
  public JPanel graphFutureForecastOnTraining(GraphDriver driver,
      TSForecaster forecaster, List<String> targetNames) throws Exception {

    if (m_trainingData == null || m_trainingData.numInstances() == 0) {
      throw new Exception("Can't forecast beyond the end of the"
          + " training instances because no training instances have been "
          + "supplied!");
    }

    if (m_trainingFuture != null) {
      return graphFutureForecast(driver, forecaster, targetNames,
          m_trainingFuture, m_trainingData);
    }

    throw new Exception(
        "[TSEvaluation] can't graph future forecast for training "
            + " because there are no future predicitons.");
  }

  /**
   * Graph historical and future forecasted values using a graph driver for the
   * test.
   * 
   * @param driver the graph driver to graph with
   * @param forecaster the forecaster in use
   * @param targetNames the names of the targets to graph
   * @return a JPanel containing the graph
   * @throws Exception if a problem occurs during graphing
   */
  public JPanel graphFutureForecastOnTesting(GraphDriver driver,
      TSForecaster forecaster, List<String> targetNames) throws Exception {

    if (m_testData == null || m_testData.numInstances() == 0) {
      throw new Exception("Can't forecast beyond the end of the"
          + " test instances because no test instances have been "
          + "supplied!");
    }

    if (m_testFuture != null) {
      return graphFutureForecast(driver, forecaster, targetNames, m_testFuture,
          m_testData);
    }

    throw new Exception(
        "[TSEvaluation] can't graph future forecast for test set "
            + " because there are no future predicitons.");
  }

  /**
   * Graph predicted values at the given step-ahead levels for a single target
   * on the training data.
   * 
   * @param driver the graph driver to graph with
   * @param forecaster the forecaster in use
   * @param targetName the target to plot
   * @param stepsToPlot a list of steps at which to plot the predicted target
   *          values - e.g. 3,4,5 would plot 3-step-ahead, 4-step-ahead and
   *          5-step-ahead predictions
   * @return a JPanel containing the graph
   * @throws Exception if a problem occurs during graphing
   */
  public JPanel graphPredictionsForStepsOnTraining(GraphDriver driver,
      TSForecaster forecaster, String targetName, List<Integer> stepsToPlot,
      int instanceNumberOffset) throws Exception {

    JPanel result = driver.getGraphPanelSteps(forecaster,
        m_predictionsForTrainingData, targetName, stepsToPlot,
        instanceNumberOffset, getTrainingData());

    return result;
  }

  /**
   * Graph predicted values at the given step-ahead levels for a single target
   * on the test data.
   * 
   * @param driver the graph driver to graph with
   * @param forecaster the forecaster in use
   * @param targetName the target to plot
   * @param stepsToPlot a list of steps at which to plot the predicted target
   *          values - e.g. 3,4,5 would plot 3-step-ahead, 4-step-ahead and
   *          5-step-ahead predictions
   * @return a JPanel containing the graph
   * @throws Exception if a problem occurs during graphing
   */
  public JPanel graphPredictionsForStepsOnTesting(GraphDriver driver,
      TSForecaster forecaster, String targetName, List<Integer> stepsToPlot,
      int instanceNumberOffset) throws Exception {

    JPanel result = driver.getGraphPanelSteps(forecaster,
        m_predictionsForTestData, targetName, stepsToPlot,
        instanceNumberOffset, getTestData());

    return result;
  }

  /**
   * Graph predicted values at the given step-ahead level for the supplied
   * targets on the training data.
   * 
   * @param driver the graph driver to graph with
   * @param forecaster the forecaster in use
   * @param graphTargets a list of targets to plot
   * @param graphStepNum the step at which to plot the targets - e.g. 4 would
   *          result in a graph of the 4-step-ahead predictions for the targets
   * @return a JPanel containing the graph
   * @throws Exception if a problem occurs during graphing
   */
  public JPanel graphPredictionsForTargetsOnTraining(GraphDriver driver,
      TSForecaster forecaster, List<String> graphTargets, int graphStepNum,
      int instanceNumberOffset) throws Exception {

    ErrorModule preds = m_predictionsForTrainingData.get(graphStepNum - 1);

    JPanel result = driver.getGraphPanelTargets(forecaster, preds,
        graphTargets, graphStepNum, instanceNumberOffset, getTrainingData());

    return result;
  }

  /**
   * Graph predicted values at the given step-ahead level for the supplied
   * targets on the test data.
   * 
   * @param driver the graph driver to graph with
   * @param forecaster the forecaster in use
   * @param graphTargets a list of targets to plot
   * @param graphStepNum the step at which to plot the targets - e.g. 4 would
   *          result in a graph of the 4-step-ahead predictions for the targets
   * @return a JPanel containing the graph
   * @throws Exception if a problem occurs during graphing
   */
  public JPanel graphPredictionsForTargetsOnTesting(GraphDriver driver,
      TSForecaster forecaster, List<String> graphTargets, int graphStepNum,
      int primeWindowSize) throws Exception {

    ErrorModule preds = m_predictionsForTestData.get(graphStepNum - 1);

    JPanel result = driver.getGraphPanelTargets(forecaster, preds,
        graphTargets, graphStepNum, primeWindowSize, getTestData());

    return result;
  }

  /**
   * Print the predictions for a given target at a given step-ahead level on the
   * training data.
   * 
   * @param title a title for the output
   * @param targetName the name of the target to print predictions for
   * @param stepAhead the step-ahead level - e.g. 3 would print the 3-step-ahead
   *          predictions
   * @return a String containing the predicted and actual values
   * @throws Exception if the predictions can't be printed for some reason.
   */
  public String printPredictionsForTrainingData(String title,
      String targetName, int stepAhead) throws Exception {
    return printPredictionsForTrainingData(title, targetName, stepAhead, 0);
  }

  /**
   * Print the predictions for a given target at a given step-ahead level from a
   * given offset on the training data.
   * 
   * @param title a title for the output
   * @param targetName the name of the target to print predictions for
   * @param stepAhead the step-ahead level - e.g. 3 would print the 3-step-ahead
   *          predictions
   * @param instanceNumberOffset the offset from the start of the training data
   *          from which to print actual and predicted values
   * @return a String containing the predicted and actual values
   * @throws Exception if the predictions can't be printed for some reason.
   */
  public String printPredictionsForTrainingData(String title,
      String targetName, int stepAhead, int instanceNumberOffset)
      throws Exception {
    ErrorModule predsForStep = getPredictionsForTrainingData(stepAhead);
    List<NumericPrediction> preds = predsForStep
        .getPredictionsForTarget(targetName);

    return printPredictions(title, preds, stepAhead, instanceNumberOffset);
  }

  /**
   * Print the predictions for a given target at a given step-ahead level on the
   * training data.
   * 
   * @param title a title for the output
   * @param targetName the name of the target to print predictions for
   * @param stepAhead the step-ahead level - e.g. 3 would print the 3-step-ahead
   *          predictions
   * @return a String containing the predicted and actual values
   * @throws Exception if the predictions can't be printed for some reason.
   */
  public String printPredictionsForTestData(String title, String targetName,
      int stepAhead) throws Exception {
    return printPredictionsForTestData(title, targetName, stepAhead, 0);
  }

  /**
   * Print the predictions for a given target at a given step-ahead level from a
   * given offset on the training data.
   * 
   * @param title a title for the output
   * @param targetName the name of the target to print predictions for
   * @param stepAhead the step-ahead level - e.g. 3 would print the 3-step-ahead
   *          predictions
   * @param instanceNumberOffset the offset from the start of the training data
   *          from which to print actual and predicted values
   * @return a String containing the predicted and actual values
   * @throws Exception if the predictions can't be printed for some reason.
   */
  public String printPredictionsForTestData(String title, String targetName,
      int stepAhead, int instanceNumberOffset) throws Exception {
    ErrorModule predsForStep = getPredictionsForTestData(stepAhead);
    List<NumericPrediction> preds = predsForStep
        .getPredictionsForTarget(targetName);

    return printPredictions(title, preds, stepAhead, instanceNumberOffset);
  }

  /**
   * Prints predictions at the supplied step ahead level from the supplied
   * instance number offset.
   * 
   * @param title a title to use for the output
   * @param preds the predictions for a single target
   * @param stepAhead the step that the supplied predictions are at
   * @param instanceNumberOffset the offset tfrom the start of the data that was
   *          used to generate the predictions that the predictions start at
   * @return
   */
  protected String printPredictions(String title,
      List<NumericPrediction> preds, int stepAhead, int instanceNumberOffset) {
    StringBuffer temp = new StringBuffer();
    boolean hasConfidenceIntervals = false;
    int maxColWidth = "predictions".length();
    int maxConfWidth = "interval".length();

    for (NumericPrediction p : preds) {
      if (!Utils.isMissingValue(p.actual())) {
        maxColWidth = maxWidth(maxColWidth, p.actual());
      }

      if (!Utils.isMissingValue(p.predicted())) {
        maxColWidth = maxWidth(maxColWidth, p.predicted());
      }

      double[][] conf = p.predictionIntervals();
      if (conf.length > 0) {
        hasConfidenceIntervals = true;
        maxConfWidth = maxWidth(maxConfWidth, conf[0][0]);
        maxConfWidth = maxWidth(maxConfWidth, conf[0][1]);
      }
    }
    maxConfWidth = (maxConfWidth * 2) + 1;

    temp.append(title + "\n\n");

    temp.append(pad("inst#", " ", maxColWidth, true) + "  ");
    temp.append(pad("actual", " ", maxColWidth, true) + "  ");
    temp.append(pad("predicted", " ", maxColWidth, true) + "  ");
    if (hasConfidenceIntervals) {
      temp.append(pad("conf", " ", maxConfWidth, true) + "  ");
    }
    temp.append(pad("error", " ", maxColWidth, true) + "\n");

    for (int i = 0; i < preds.size(); i++) {
      NumericPrediction pred = preds.get(i);
      String instNum = pad("" + (instanceNumberOffset + i + stepAhead), " ",
          maxColWidth, true);
      temp.append(instNum + "  ");

      double actual = pred.actual();
      String actualS = null;
      if (Utils.isMissingValue(actual)) {
        actualS = pad("?", " ", maxColWidth, true);
      } else {
        actualS = Utils.doubleToString(actual, 4);
        actualS = pad(actualS, " ", maxColWidth, true);
      }
      temp.append(actualS + "  ");

      double predicted = pred.predicted();
      String predictedS = null;
      if (Utils.isMissingValue(predicted)) {
        predictedS = pad("?", " ", maxColWidth, true);
      } else {
        predictedS = Utils.doubleToString(predicted, 4);
        predictedS = pad(predictedS, " ", maxColWidth, true);
      }
      temp.append(predictedS + "  ");

      if (hasConfidenceIntervals) {
        double[][] limits = pred.predictionIntervals();
        double low = limits[0][0];
        double high = limits[0][1];
        String limitsS = Utils.doubleToString(low, 3) + ":"
            + Utils.doubleToString(high, 3);
        limitsS = pad(limitsS, " ", maxConfWidth, true);
        temp.append(limitsS).append("  ");
      }

      double error = pred.error();
      String errorS = null;
      if (Utils.isMissingValue(error)) {
        errorS = pad("?", " ", maxColWidth, true);
      } else {
        errorS = Utils.doubleToString(error, 4);
        errorS = pad(errorS, " ", maxColWidth, true);
      }
      temp.append(errorS + "\n");
    }

    return temp.toString();
  }

  private static String pad(String source, String padChar, int length,
      boolean leftPad) {
    StringBuffer temp = new StringBuffer();
    length = length - source.length();

    if (length > 0) {
      if (leftPad) {
        for (int i = 0; i < length; i++) {
          temp.append(padChar);
        }
        temp.append(source);
      } else {
        temp.append(source);
        for (int i = 0; i < length; i++) {
          temp.append(padChar);
        }
      }
    } else {
      temp.append(source);
    }
    return temp.toString();
  }

  private static int maxWidth(int maxWidth, double value) {
    double width = Math.log(Math.abs(value)) / Math.log(10);

    if (width < 0) {
      width = 1;
    }

    // decimal + # decimal places + 1
    width += 6.0;

    if ((int) width > maxWidth) {
      maxWidth = (int) width;
    }

    return maxWidth;
  }

  /**
   * Generates a String containing the results of evaluating the forecaster.
   * 
   * @return a String containing the results of evaluating the forecaster
   * @throws Exception if a problem occurs
   */
  public String toSummaryString() throws Exception {
    StringBuffer result = new StringBuffer();

    int maxWidth = "10-steps-ahead".length() + 1;
    int maxWidthForLeftCol = "Target".length();
    List<String> targetFieldNames = null;

    // update max width with respect to metrics for training data (if any)
    if (m_evaluateTrainingData) {
      Set<String> keys = m_metricsForTrainingData.keySet();
      for (String key : keys) {
        List<TSEvalModule> evalsForKey = m_metricsForTrainingData.get(key);
        for (TSEvalModule e : evalsForKey) {
          if (targetFieldNames == null) {
            targetFieldNames = e.getTargetFields();
          }
          double[] metricsForTargets = e.calculateMeasure();
          for (double m : metricsForTargets) {
            maxWidth = maxWidth(maxWidth, m);
          }
        }
      }
    }

    // update max width with respect to metrics for test data (if any)
    if (m_evaluateTestData) {
      Set<String> keys = m_metricsForTestData.keySet();
      for (String key : keys) {
        List<TSEvalModule> evalsForKey = m_metricsForTestData.get(key);
        for (TSEvalModule e : evalsForKey) {
          if (targetFieldNames == null) {
            targetFieldNames = e.getTargetFields();
          }
          double[] metricsForTargets = e.calculateMeasure();
          for (double m : metricsForTargets) {
            maxWidth = maxWidth(maxWidth, m);
          }
        }
      }
    }

    // update the max width of the left-hand column with respect to
    // the target field names
    for (String targetName : targetFieldNames) {
      if (targetName.length() > maxWidthForLeftCol) {
        maxWidthForLeftCol = targetName.length();
      }
    }

    // update the max width of the left-hand column with respect to
    // the metric names
    for (TSEvalModule mod : m_evalModules) {
      if (mod.getDescription().length() + 2 > maxWidthForLeftCol) {
        maxWidthForLeftCol = mod.getDescription().length() + 2; // we'll indent
                                                                // these 2
                                                                // spaces
      }
    }

    if (m_missingTimeStampTestSetRows != null
        && m_missingTimeStampTestSetRows.size() > 0) {
      result.append("\n--------------------------------------------------\n"
          + "Instances were inserted in the test data for the following\n"
          + "time-stamps (target values set by interpolation):\n\n");
      for (int i = 0; i < m_missingTimeStampTestSetRows.size(); i++) {
        if (i == 0) {
          result
              .append("              " + m_missingTimeStampTestSetRows.get(i));
        } else {
          result.append(", " + m_missingTimeStampTestSetRows.get(i));
        }
      }

      result.append("\n--------------------------------------------------\n");
    }

    if (m_missingTargetListTestSet != null
        && m_missingTargetListTestSet.size() > 0) {
      Collections.sort(m_missingTargetListTestSet);
      result.append("\n---------------------------------------------------\n"
          + "The following test instances had missing values\n"
          + "imputed via interpolation. Check source data as\n"
          + "this may affect forecasting performance:\n\n");
      for (int i = 0; i < m_missingTargetListTestSet.size(); i++) {
        if (i == 0) {
          result.append("              " + m_missingTargetListTestSet.get(i));
        } else if (!m_missingTargetListTestSet.get(i).equals(
            m_missingTargetListTestSet.get(i - 1))) {
          result.append("," + m_missingTargetListTestSet.get(i));
        }
      }
      result.append("\n---------------------------------------------------\n");
    }

    if (m_missingTimeStampListTestSet != null
        && m_missingTimeStampListTestSet.size() > 0) {
      Collections.sort(m_missingTimeStampListTestSet);
      result
          .append("\n--------------------------------------------------------\n"
              + "The following test instances had missing time stamps:\n\n");
      for (int i = 0; i < m_missingTimeStampListTestSet.size(); i++) {
        if (i == 0) {
          result
              .append("              " + m_missingTimeStampListTestSet.get(i));
        } else {
          result.append("," + m_missingTimeStampListTestSet.get(i));
        }
      }
      result
          .append("\n-------------------------------------------------------\n");
    }

    if (m_evaluateTrainingData) {
      String temp = summaryMetrics(maxWidthForLeftCol, maxWidth,
          targetFieldNames, m_metricsForTrainingData);

      result.append("=== Evaluation on training data ===\n");
      result.append(temp).append("\n");
      result.append(
          "Total number of instances: " + m_trainingData.numInstances())
          .append("\n\n");
    }

    if (m_evaluateTestData) {
      String temp = summaryMetrics(maxWidthForLeftCol, maxWidth,
          targetFieldNames, m_metricsForTestData);

      result.append("=== Evaluation on test data ===\n");
      result.append(temp).append("\n");
      result.append("Total number of instances: " + m_testData.numInstances())
          .append("\n\n");
    }

    return result.toString();
  }

  private String summaryMetrics(int maxWidthForLeftCol, int maxWidth,
      List<String> targetFieldNames,
      Map<String, List<TSEvalModule>> metricsToUse) throws Exception {
    StringBuffer temp = new StringBuffer();
    temp.append(pad("Target", " ", maxWidthForLeftCol, false));

    /*
     * for (String targetName : targetFieldNames) { temp.append(pad(targetName,
     * " ", maxWidth, true)); }
     */

    for (int i = 0; i < m_horizon; i++) {
      String stepS = (i == 0) ? "step-ahead" : "steps-ahead";
      temp.append(pad("" + (i + 1) + "-" + stepS, " ", maxWidth, true));
    }
    temp.append("\n");

    /*
     * String tempKey = m_evalModules.get(1).getEvalName();
     * temp.append(pad("     ", " ", maxWidthForLeftCol, false)); for (int i =
     * 0; i < m_horizon; i++) { int n = metricsToUse.get(tempKey).get(i).getN }
     */

    temp.append(pad("=", "=", maxWidthForLeftCol + (m_horizon * maxWidth), true));
    temp.append("\n");

    for (int i = 0; i < targetFieldNames.size(); i++) {
      temp.append(targetFieldNames.get(i) + "\n");

      // print out the count (N) for each step ahead for each target
      if (m_evalModules.get(1) instanceof ErrorModule) {
        String tempKey = m_evalModules.get(1).getEvalName();
        temp.append(pad("  N", " ", maxWidthForLeftCol, false));
        List<TSEvalModule> metricForSteps = metricsToUse.get(tempKey);
        for (TSEvalModule m : metricForSteps) {
          double[] countsForTargets = ((ErrorModule) m).countsForTargets();
          double countForTarget = countsForTargets[i];
          temp.append(pad(Utils.doubleToString(countForTarget, 0), " ",
              maxWidth, true));
        }
        temp.append("\n");
      }

      Set<String> keys = metricsToUse.keySet();
      for (String key : keys) {
        String metricName = "  "
            + metricsToUse.get(key).get(0).getDescription();
        List<TSEvalModule> metricForSteps = metricsToUse.get(key);
        temp.append(pad(metricName, " ", maxWidthForLeftCol, false));

        for (TSEvalModule m : metricForSteps) {

          double[] metricsForTargets = m.calculateMeasure();
          double metricForTargetI = metricsForTargets[i];
          String result = (Utils.isMissingValue(metricForTargetI)) ? "N/A"
              : Utils.doubleToString(metricForTargetI, 4);
          temp.append(pad(result, " ", maxWidth, true));
        }
        temp.append("\n");
      }
    }

    return temp.toString();
  }
}
