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
 *    JFreeChartDriver.java
 *    Copyright (C) 2010 Pentaho Corporation
 */

package weka.classifiers.timeseries.eval.graph;

import java.awt.BasicStroke;
import java.awt.Image;
import java.io.File;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.timeseries.AbstractForecaster;
import weka.classifiers.timeseries.TSForecaster;
import weka.classifiers.timeseries.WekaForecaster;
import weka.classifiers.timeseries.core.TSLagMaker;
import weka.classifiers.timeseries.core.TSLagUser;
import weka.classifiers.timeseries.eval.ErrorModule;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * A Graph driver that uses the JFreeChart library.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 50889 $
 */
public class JFreeChartDriver extends GraphDriver {

  protected JFreeChart getPredictedTargetsChart(TSForecaster forecaster,
      ErrorModule preds, List<String> targetNames, int stepNumber,
      int instanceNumOffset, Instances data) {

    if (forecaster instanceof TSLagUser && data != null) {
      TSLagMaker lagMaker = ((TSLagUser) forecaster).getTSLagMaker();
      if (lagMaker.getAdjustForTrends()
          && !lagMaker.isUsingAnArtificialTimeIndex()) {

        // fill in any missing time stamps only
        data = new Instances(data);
        data = weka.classifiers.timeseries.core.Utils.replaceMissing(data,
            null, lagMaker.getTimeStampField(), true,
            lagMaker.getPeriodicity(), lagMaker.getSkipEntries());
      }
    }

    // set up a collection of predicted and actual series
    XYIntervalSeriesCollection xyDataset = new XYIntervalSeriesCollection();
    for (String target : targetNames) {
      XYIntervalSeries targetSeries = new XYIntervalSeries(target + "-actual",
          false, false);
      xyDataset.addSeries(targetSeries);
      targetSeries = new XYIntervalSeries(target + "-predicted", false, false);
      xyDataset.addSeries(targetSeries);

    }

    ValueAxis timeAxis = null;
    NumberAxis valueAxis = new NumberAxis("");
    valueAxis.setAutoRangeIncludesZero(false);
    int timeIndex = -1;
    boolean timeAxisIsDate = false;
    if (forecaster instanceof TSLagUser && data != null) {
      TSLagMaker lagMaker = ((TSLagUser) forecaster).getTSLagMaker();
      if (!lagMaker.isUsingAnArtificialTimeIndex()
          && lagMaker.getAdjustForTrends()) {
        String timeName = lagMaker.getTimeStampField();
        if (data.attribute(timeName).isDate()) {
          timeAxis = new DateAxis("");
          timeAxisIsDate = true;
          timeIndex = data.attribute(timeName).index();
        }
      }
    }

    if (timeAxis == null) {
      timeAxis = new NumberAxis("");
      ((NumberAxis) timeAxis).setAutoRangeIncludesZero(false);
    }

    // now populate the series
    boolean hasConfidenceIntervals = false;
    for (int i = 0; i < targetNames.size(); i++) {
      String targetName = targetNames.get(i);
      List<NumericPrediction> predsForI = preds
          .getPredictionsForTarget(targetName);
      int predIndex = xyDataset.indexOf(targetName + "-predicted");
      int actualIndex = xyDataset.indexOf(targetName + "-actual");
      XYIntervalSeries predSeries = xyDataset.getSeries(predIndex);
      XYIntervalSeries actualSeries = xyDataset.getSeries(actualIndex);

      for (int j = 0; j < predsForI.size(); j++) {
        double x = Utils.missingValue();
        if (timeAxisIsDate) {
          if (instanceNumOffset + j + stepNumber - 1 < data.numInstances()) {
            x = data.instance(instanceNumOffset + j + stepNumber - 1).value(
                timeIndex);
          }
        } else {
          x = instanceNumOffset + j + stepNumber;
        }

        double yPredicted = predsForI.get(j).predicted();
        double yHigh = yPredicted;
        double yLow = yPredicted;
        double[][] conf = predsForI.get(j).predictionIntervals();
        if (conf.length > 0) {
          yLow = conf[0][0];
          yHigh = conf[0][1];
          hasConfidenceIntervals = true;
        }
        if (!Utils.isMissingValue(x) && !Utils.isMissingValue(yPredicted)) {
          if (predSeries != null) {
            predSeries.add(x, x, x, yPredicted, yLow, yHigh);
          }
          // System.err.println("* " + yPredicted + " " + x);
        }

        double yActual = predsForI.get(j).actual();
        if (!Utils.isMissingValue(x) && !Utils.isMissingValue(yActual)) {
          if (actualSeries != null) {
            actualSeries.add(x, x, x, yActual, yActual, yActual);
          }
        }
      }
    }

    // set up the chart
    String title = "" + stepNumber + " step-ahead predictions for: ";
    for (String s : targetNames) {
      title += s + ",";
    }
    title = title.substring(0, title.lastIndexOf(","));

    /*
     * String algoSpec = forecaster.getAlgorithmName(); title += " (" + algoSpec
     * + ")";
     */

    if (forecaster instanceof WekaForecaster && hasConfidenceIntervals) {
      double confPerc = ((WekaForecaster) forecaster).getConfidenceLevel() * 100.0;
      title += " [" + Utils.doubleToString(confPerc, 0) + "% conf. intervals]";
    }

    XYErrorRenderer renderer = new XYErrorRenderer();
    renderer.setBaseLinesVisible(true);
    renderer.setDrawXError(false);
    renderer.setDrawYError(true);
    // renderer.setShapesFilled(true);
    XYPlot plot = new XYPlot(xyDataset, timeAxis, valueAxis, renderer);
    JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
        plot, true);
    chart.setBackgroundPaint(java.awt.Color.white);
    TextTitle chartTitle = chart.getTitle();
    String fontName = chartTitle.getFont().getFontName();
    java.awt.Font newFont = new java.awt.Font(fontName, java.awt.Font.PLAIN, 12);
    chartTitle.setFont(newFont);

    return chart;
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
  @Override
  public void saveChartToFile(JPanel chart, String filename, int width,
      int height) throws Exception {

    if (!(chart instanceof ChartPanel)) {
      throw new Exception("Chart is not a JFreeChart!");
    }

    if (filename.toLowerCase().lastIndexOf(".png") < 0) {
      filename = filename + ".png";
    }
    ChartUtilities.saveChartAsPNG(new File(filename),
        ((ChartPanel) chart).getChart(), width, height);
  }

  /**
   * Get an image representation of the supplied chart.
   * 
   * @param chart the chart to get an image of.
   * @param width width of the chart
   * @param height height of the chart
   * @return an Image of the chart
   * @throws Exception if the image can't be created for some reason
   */
  @Override
  public Image getImageFromChart(JPanel chart, int width, int height)
      throws Exception {
    if (!(chart instanceof ChartPanel)) {
      throw new Exception("Chart is not a JFreeChart!");
    }

    Image result = ((ChartPanel) chart).getChart().createBufferedImage(width,
        height);
    return result;
  }

  /**
   * Return the graph encapsulated in a panel.
   * 
   * @param width the width in pixels of the graph
   * @param height the height in pixels of the graph
   * @param forecaster the forecaster
   * @param preds an ErrorModule that contains predictions for all targets for
   *          the specified step ahead. Targets are in the same order returned
   *          by TSForecaster.getFieldsToForecast()
   * @param targetNames the list of target names to plot
   * @param stepNumber which step ahead to graph for specified targets
   * @param instanceNumOffset how far into the data the predictions start from
   * @param data the instances that these predictions correspond to (may be
   *          null)
   * @return an image of the graph
   */
  @Override
  public JPanel getGraphPanelTargets(TSForecaster forecaster,
      ErrorModule preds, List<String> targetNames, int stepNumber,
      int instanceNumOffset, Instances data) throws Exception {

    JFreeChart chart = getPredictedTargetsChart(forecaster, preds, targetNames,
        stepNumber, instanceNumOffset, data);

    ChartPanel result = new ChartPanel(chart, false, true, true, true, false);

    return result;
  }

  protected JFreeChart getPredictedStepsChart(TSForecaster forecaster,
      List<ErrorModule> preds, String targetName, List<Integer> stepsToPlot,
      int instanceNumOffset, Instances data) {

    if (forecaster instanceof TSLagUser && data != null) {
      TSLagMaker lagMaker = ((TSLagUser) forecaster).getTSLagMaker();
      if (lagMaker.getAdjustForTrends()
          && !lagMaker.isUsingAnArtificialTimeIndex()) {

        // fill in any missing time stamps only
        data = new Instances(data);
        data = weka.classifiers.timeseries.core.Utils.replaceMissing(data,
            null, lagMaker.getTimeStampField(), true,
            lagMaker.getPeriodicity(), lagMaker.getSkipEntries());
      }
    }

    // set up a collection of predicted series
    XYIntervalSeriesCollection xyDataset = new XYIntervalSeriesCollection();

    XYIntervalSeries targetSeries = new XYIntervalSeries(targetName, false,
        false);
    xyDataset.addSeries(targetSeries);
    // for (int i = 0; i < preds.size(); i++) {
    for (int z = 0; z < stepsToPlot.size(); z++) {
      int i = stepsToPlot.get(z);
      i--;
      // ignore out of range steps
      if (i < 0 || i >= preds.size()) {
        continue;
      }

      String step = "-steps";
      if (i == 0) {
        step = "-step";
      }
      targetSeries = new XYIntervalSeries(targetName + "_" + (i + 1) + step
          + "-ahead", false, false);
      xyDataset.addSeries(targetSeries);
    }

    ValueAxis timeAxis = null;
    NumberAxis valueAxis = new NumberAxis("");
    valueAxis.setAutoRangeIncludesZero(false);
    int timeIndex = -1;
    boolean timeAxisIsDate = false;
    if (forecaster instanceof TSLagUser && data != null) {
      TSLagMaker lagMaker = ((TSLagUser) forecaster).getTSLagMaker();
      if (!lagMaker.isUsingAnArtificialTimeIndex()
          && lagMaker.getAdjustForTrends()) {
        String timeName = lagMaker.getTimeStampField();
        if (data.attribute(timeName).isDate()) {
          timeAxis = new DateAxis("");
          timeAxisIsDate = true;
          timeIndex = data.attribute(timeName).index();
        }
      }
    }

    if (timeAxis == null) {
      timeAxis = new NumberAxis("");
      ((NumberAxis) timeAxis).setAutoRangeIncludesZero(false);
    }

    // now populate the series
    // for (int i = 0; i < preds.size(); i++) {
    boolean doneActual = false;
    boolean hasConfidenceIntervals = false;
    for (int z = 0; z < stepsToPlot.size(); z++) {
      int i = stepsToPlot.get(z);
      i--;

      // ignore out of range steps
      if (i < 0 || i >= preds.size()) {
        continue;
      }
      ErrorModule predsForStepI = preds.get(i);
      List<NumericPrediction> predsForTargetAtI = predsForStepI
          .getPredictionsForTarget(targetName);

      String step = "-steps";
      if (i == 0) {
        step = "-step";
      }
      int predIndex = xyDataset.indexOf(targetName + "_" + (i + 1) + step
          + "-ahead");
      XYIntervalSeries predSeries = xyDataset.getSeries(predIndex);
      XYIntervalSeries actualSeries = null;
      if (!doneActual) {
        int actualIndex = xyDataset.indexOf(targetName);
        actualSeries = xyDataset.getSeries(actualIndex);
      }

      for (int j = 0; j < predsForTargetAtI.size(); j++) {
        double x = Utils.missingValue();
        if (timeAxisIsDate) {
          if (instanceNumOffset + j + i < data.numInstances()) {
            x = data.instance(instanceNumOffset + j + i).value(timeIndex);
          }
        } else {
          x = instanceNumOffset + j + i;
        }

        double yPredicted = predsForTargetAtI.get(j).predicted();
        double yHigh = yPredicted;
        double yLow = yPredicted;
        double[][] conf = predsForTargetAtI.get(j).predictionIntervals();
        if (conf.length > 0) {
          yLow = conf[0][0];
          yHigh = conf[0][1];
          hasConfidenceIntervals = true;
        }
        if (!Utils.isMissingValue(x) && !Utils.isMissingValue(yPredicted)) {
          if (predSeries != null) {
            predSeries.add(x, x, x, yPredicted, yLow, yHigh);
          }
          // System.err.println("* " + yPredicted + " " + x);
        }

        if (!doneActual && actualSeries != null) {
          double yActual = predsForTargetAtI.get(j).actual();
          if (!Utils.isMissingValue(x) && !Utils.isMissingValue(yActual)) {
            actualSeries.add(x, x, x, yActual, yActual, yActual);
          }
        }
      }

      if (actualSeries != null) {
        doneActual = true;
      }
    }

    // set up the chart
    String title = "";
    for (int i : stepsToPlot) {
      title += i + ",";
    }
    title = title.substring(0, title.lastIndexOf(","));
    title += " step-ahead predictions for " + targetName;

    /*
     * String algoSpec = forecaster.getAlgorithmName(); title += " (" + algoSpec
     * + ")";
     */

    if (forecaster instanceof WekaForecaster && hasConfidenceIntervals) {
      double confPerc = ((WekaForecaster) forecaster).getConfidenceLevel() * 100.0;
      title += " [" + Utils.doubleToString(confPerc, 0) + "% conf. intervals]";
    }

    XYErrorRenderer renderer = new XYErrorRenderer();
    renderer.setBaseLinesVisible(true);
    // renderer.setShapesFilled(true);
    XYPlot plot = new XYPlot(xyDataset, timeAxis, valueAxis, renderer);
    JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
        plot, true);
    chart.setBackgroundPaint(java.awt.Color.white);
    TextTitle chartTitle = chart.getTitle();
    String fontName = chartTitle.getFont().getFontName();
    java.awt.Font newFont = new java.awt.Font(fontName, java.awt.Font.PLAIN, 12);
    chartTitle.setFont(newFont);

    return chart;
  }

  /**
   * Return the graph encapsulated in a JPanel.
   * 
   * @param forecaster the forecaster
   * @param preds a list of ErrorModules, one for each consecutive step ahead
   *          prediction set
   * @param targetName the name of the target field to plot
   * @param stepsToPlot a list of step numbers for the step-ahead prediction
   *          sets to plot to plot for the specified target.
   * @param instanceNumOffset how far into the data the predictions start from
   * @param data the instances that these predictions correspond to (may be
   *          null)
   * @return an image of the graph.
   */
  @Override
  public JPanel getGraphPanelSteps(TSForecaster forecaster,
      List<ErrorModule> preds, String targetName, List<Integer> stepsToPlot,
      int instanceNumOffset, Instances data) throws Exception {

    JFreeChart chart = getPredictedStepsChart(forecaster, preds, targetName,
        stepsToPlot, instanceNumOffset, data);

    ChartPanel result = new ChartPanel(chart, false, true, true, true, false);

    return result;
  }

  protected JFreeChart getFutureForecastChart(TSForecaster forecaster,
      List<List<NumericPrediction>> preds, List<String> targetNames,
      Instances history) {

    if (forecaster instanceof TSLagUser && history != null) {
      TSLagMaker lagMaker = ((TSLagUser) forecaster).getTSLagMaker();
      if (lagMaker.getAdjustForTrends()
          && !lagMaker.isUsingAnArtificialTimeIndex()) {

        // fill in any missing time stamps only
        history = new Instances(history);
        history = weka.classifiers.timeseries.core.Utils.replaceMissing(
            history, null, lagMaker.getTimeStampField(), true,
            lagMaker.getPeriodicity(), lagMaker.getSkipEntries());
      }
    }

    // set up a collection of series
    XYIntervalSeriesCollection xyDataset = new XYIntervalSeriesCollection();

    if (history != null) {
      // add actual historical data values
      for (String targetName : targetNames) {
        XYIntervalSeries targetSeries = new XYIntervalSeries(targetName, false,
            false);
        xyDataset.addSeries(targetSeries);
      }
    }

    // add predicted series
    for (String targetName : targetNames) {
      XYIntervalSeries targetSeries = new XYIntervalSeries(targetName
          + "-predicted", false, false);
      xyDataset.addSeries(targetSeries);
    }

    ValueAxis timeAxis = null;
    NumberAxis valueAxis = new NumberAxis("");
    valueAxis.setAutoRangeIncludesZero(false);
    int timeIndex = -1;
    boolean timeAxisIsDate = false;
    double artificialTimeStart = 0;
    double lastRealTimeValue = Utils.missingValue();
    if (forecaster instanceof TSLagUser && history != null) {
      TSLagMaker lagMaker = ((TSLagUser) forecaster).getTSLagMaker();
      if (!lagMaker.isUsingAnArtificialTimeIndex()
          && lagMaker.getAdjustForTrends()) {
        String timeName = lagMaker.getTimeStampField();
        if (history.attribute(timeName).isDate()) {
          timeAxis = new DateAxis("");
          timeAxisIsDate = true;
          timeIndex = history.attribute(timeName).index();
        }
      } else {
        try {
          artificialTimeStart = (history != null) ? 1 : lagMaker
              .getArtificialTimeStartValue() + 1;
        } catch (Exception ex) {
        }
      }
    }

    if (timeAxis == null) {
      timeAxis = new NumberAxis("");
      ((NumberAxis) timeAxis).setAutoRangeIncludesZero(false);
    }

    boolean hasConfidenceIntervals = false;

    // now populate the series
    if (history != null) {

      // do the actuals first
      for (int i = 0; i < history.numInstances(); i++) {
        Instance current = history.instance(i);

        for (String targetName : targetNames) {
          int dataIndex = history.attribute(targetName.trim()).index();

          if (dataIndex >= 0) {
            XYIntervalSeries actualSeries = null;
            int actualIndex = xyDataset.indexOf(targetName);
            actualSeries = xyDataset.getSeries(actualIndex);
            double x = Utils.missingValue();

            if (timeAxisIsDate) {
              x = current.value(timeIndex);
              if (!Utils.isMissingValue(x)) {
                lastRealTimeValue = x;
              }
            } else {
              x = artificialTimeStart;
            }

            double y = Utils.missingValue();
            y = current.value(dataIndex);

            if (!Utils.isMissingValue(x) && !Utils.isMissingValue(y)) {
              if (actualSeries != null) {
                actualSeries.add(x, x, x, y, y, y);
              }
            }
          }
        }

        if (!timeAxisIsDate) {
          artificialTimeStart++;
        }
      }
    }

    // now do the futures
    List<String> forecasterTargets = AbstractForecaster.stringToList(forecaster
        .getFieldsToForecast());

    // loop over the steps
    for (int j = 0; j < preds.size(); j++) {
      List<NumericPrediction> predsForStepJ = preds.get(j);

      // advance the real time index (if appropriate)
      if (timeAxisIsDate) {
        lastRealTimeValue = ((TSLagUser) forecaster).getTSLagMaker()
            .advanceSuppliedTimeValue(lastRealTimeValue);
      }
      for (String targetName : targetNames) {

        // look up this requested target in the list that the forecaster
        // has predicted
        int predIndex = forecasterTargets.indexOf(targetName.trim());
        if (predIndex >= 0) {
          NumericPrediction predsForTargetAtStepJ = predsForStepJ
              .get(predIndex);
          XYIntervalSeries predSeries = null;
          int datasetIndex = xyDataset.indexOf(targetName + "-predicted");
          predSeries = xyDataset.getSeries(datasetIndex);

          if (predSeries != null) {
            double y = predsForTargetAtStepJ.predicted();
            double x = Utils.missingValue();
            double yHigh = y;
            double yLow = y;
            double[][] conf = predsForTargetAtStepJ.predictionIntervals();
            if (conf.length > 0) {
              yLow = conf[0][0];
              yHigh = conf[0][1];
              hasConfidenceIntervals = true;
            }

            if (!timeAxisIsDate) {
              x = artificialTimeStart;
            } else {
              x = lastRealTimeValue;
            }

            if (!Utils.isMissingValue(x) && !Utils.isMissingValue(y)) {
              predSeries.add(x, x, x, y, yLow, yHigh);
            }
          }
        }
      }

      // advance the artificial time index (if appropriate)
      if (!timeAxisIsDate) {
        artificialTimeStart++;
      }
    }

    String title = "Future forecast for: ";
    for (String s : targetNames) {
      title += s + ",";
    }
    title = title.substring(0, title.lastIndexOf(","));

    /*
     * String algoSpec = forecaster.getAlgorithmName(); title += " (" + algoSpec
     * + ")";
     */

    if (forecaster instanceof WekaForecaster && hasConfidenceIntervals) {
      double confPerc = ((WekaForecaster) forecaster).getConfidenceLevel() * 100.0;
      title += " [" + Utils.doubleToString(confPerc, 0) + "% conf. intervals]";
    }

    XYErrorRenderer renderer = new XYErrorRenderer();

    // renderer.setShapesFilled(true);
    XYPlot plot = new XYPlot(xyDataset, timeAxis, valueAxis, renderer);
    // renderer = (XYErrorRenderer)plot.getRenderer();
    if (history != null) {
      for (String targetName : targetNames) {
        XYIntervalSeries predSeries = null;
        int predIndex = xyDataset.indexOf(targetName + "-predicted");
        predSeries = xyDataset.getSeries(predIndex);

        XYIntervalSeries actualSeries = null;
        int actualIndex = xyDataset.indexOf(targetName);
        actualSeries = xyDataset.getSeries(actualIndex);

        if (actualSeries != null && predSeries != null) {
          // match the color of the actual series
          java.awt.Paint actualPaint = renderer.lookupSeriesPaint(actualIndex);
          renderer.setSeriesPaint(predIndex, actualPaint);

          // now set the line style to dashed
          BasicStroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
              BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f }, 0.0f);

          renderer.setSeriesStroke(predIndex, dashed);
        }
      }
    }

    renderer.setBaseLinesVisible(true);
    renderer.setDrawXError(false);
    renderer.setDrawYError(true);

    JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
        plot, true);
    chart.setBackgroundPaint(java.awt.Color.white);
    TextTitle chartTitle = chart.getTitle();
    String fontName = chartTitle.getFont().getFontName();
    java.awt.Font newFont = new java.awt.Font(fontName, java.awt.Font.PLAIN, 12);
    chartTitle.setFont(newFont);

    return chart;
  }

  /**
   * Return the graph encapsulated in a JPanel
   * 
   * @param forecaster the forecaster
   * @param preds a list of list of predictions for *all* targets. The outer
   *          list is indexed by step number (i.e. the first entry is the 1-step
   *          ahead forecasts, the second is the 2-steps ahead forecasts etc.)
   *          and the inner list is indexed by target in the same order as the
   *          list of targets returned by TSForecaster.getFieldsToForecast().
   * @param targetNames the list of target names to plot
   * @param history a set of instances from which predictions are assumed to
   *          follow on from. May be null, in which case just the predictions
   *          are plotted.
   * @return an image of the graph
   */
  @Override
  public JPanel getPanelFutureForecast(TSForecaster forecaster,
      List<List<NumericPrediction>> preds, List<String> targetNames,
      Instances history) throws Exception {

    JFreeChart chart = getFutureForecastChart(forecaster, preds, targetNames,
        history);

    ChartPanel result = new ChartPanel(chart, false, true, true, true, false);

    return result;
  }
}
