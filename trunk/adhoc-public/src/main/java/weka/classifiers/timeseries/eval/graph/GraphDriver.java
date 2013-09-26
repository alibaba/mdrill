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
 *    GraphDriver.java
 *    Copyright (C) 2010 Pentaho Corporation
 */


package weka.classifiers.timeseries.eval.graph;

import java.awt.Image;
import java.util.List;

import javax.swing.JPanel;

import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.timeseries.TSForecaster;
import weka.classifiers.timeseries.eval.ErrorModule;
import weka.core.Instances;

/**
 * Abstract base class for graph drivers. Provides a factory method
 * to obtain concrete implementations. The default graph driver uses
 * the JFreeChart library to generate graphs.
 * 
 * @author Mark Hall (mahll{[at]}pentaho[{dot}]com)
 * @version $Revision: 49983 $
 */
public abstract class GraphDriver {
  
  /**
   * Factory method for obtaining a named graph driver for producing
   * graphs
   * 
   * @param driverName the name of the driver to obtain
   * @return an instance of the named driver
   * @throws IllegalArgumentException if the supplied driver name
   * is unknown.
   */
  public static GraphDriver getDriver(String driverName)
    throws IllegalArgumentException {
    if (driverName.equals("JFreeChartDriver")) {
      return new JFreeChartDriver();
    }

    // assume a fully qualified class name and try to instantiate
    try {
      Object candidateModule = Class.forName(driverName).newInstance();
      if (candidateModule instanceof GraphDriver) {
        return (GraphDriver)candidateModule;
      }
    } catch (InstantiationException e) {
      throw new IllegalArgumentException("Unable to instantiate " + driverName);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("Unknown evaluation moduel " + driverName);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Unable to instantiate " + driverName);
    }        


    throw new IllegalArgumentException("Unknown evaluation moduel " + driverName);
  }
  
  /**
   * Get the default graph driver (currently uses the JFreeChart library)
   * 
   * @return the default graph driver
   */
  public static GraphDriver getDefaultDriver() {
    return new JFreeChartDriver();
  }
  
  /**
   * Save a chart to a file.
   * 
   * @param chart the chart to save
   * @param filename the filename to save to
   * @param width width of the saved image
   * @param height height of the saved image
   * @throws Exception if the chart can't be saved for some reason
   */
  public abstract void saveChartToFile(JPanel chart, String filename, 
      int width, int height) throws Exception;
  
  /**
   * Get an image representation of the supplied chart.
   * 
   * @param chart the chart to get an image of.
   * @param width width of the chart
   * @param height height of the chart
   * @return an Image of the chart
   * @throws Exception if the image can't be created for some reason
   */
  public abstract Image getImageFromChart(JPanel chart, int width, int height)
    throws Exception;
  
  /**
   * Return the graph encapsulated in a panel.
   * 
   * @param width the width in pixels of the graph
   * @param height the height in pixels of the graph
   * @param forecaster the forecaster
   * @param preds an ErrorModule that contains predictions for all targets 
   * for the specified step ahead. Targets are in the same order 
   * returned by TSForecaster.getFieldsToForecast()
   * @param targetNames the list of target names to plot
   * @param stepNumber which step ahead to graph for specified targets
   * @param instanceNumOffset how far into the data the predictions start from
   * @param data the instances that these predictions correspond to (may
   * be null)
   * @return an image of the graph
   */
  public abstract JPanel getGraphPanelTargets(TSForecaster forecaster, 
      ErrorModule preds, List<String> targetNames, int stepNumber, int instanceNumOffset,
      Instances data) throws Exception;
  
  /**
   * Return the graph encapsulated in a JPanel.
   *
   * @param forecaster the forecaster
   * @param preds a list of ErrorModules, one for each consecutive step
   * ahead prediction set
   * @param targetName the name of the target field to plot
   * @param stepsToPlot a list of step numbers for the step-ahead prediction sets to plot
   * to plot for the specified target.
   * @param instanceNumOffset how far into the data the predictions start from
   * @param data the instances that these predictions correspond to (may be null)
   * @return an image of the graph.
   */
  public abstract JPanel getGraphPanelSteps(TSForecaster forecaster, 
      List<ErrorModule> preds, String targetName, List<Integer> stepsToPlot, 
      int instanceNumOffset, Instances data) throws Exception;
  
  /**
   * Return the graph encapsulated in a JPanel
   * 
   * @param forecaster the forecaster
   * @param preds a list of list of predictions for *all* targets. The
   * outer list is indexed by step number (i.e. the first entry
   * is the 1-step ahead forecasts, the second is the 2-steps ahead
   * forecasts etc.) and the inner list is indexed by target in the
   * same order as the list of targets returned by TSForecaster.getFieldsToForecast().
   * @param targetNames the list of target names to plot
   * @param history a set of instances from which predictions are assumed
   * to follow on from. May be null, in which case just the predictions are
   * plotted.
   * @return an image of the graph 
   */
  public abstract JPanel getPanelFutureForecast(TSForecaster forecaster, 
      List<List<NumericPrediction>> preds,
      List<String> targetNames, Instances history) throws Exception;
}
