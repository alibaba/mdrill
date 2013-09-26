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
 *    WekaForecaster.java
 *    Copyright (C) 2010 Pentaho Corporation
 */

package weka.classifiers.timeseries;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.timeseries.core.ConfidenceIntervalForecaster;
import weka.classifiers.timeseries.core.CustomPeriodicTest;
import weka.classifiers.timeseries.core.ErrorBasedConfidenceIntervalEstimator;
import weka.classifiers.timeseries.core.IncrementallyPrimeable;
import weka.classifiers.timeseries.core.OverlayForecaster;
import weka.classifiers.timeseries.core.TSLagMaker;
import weka.classifiers.timeseries.core.TSLagUser;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.logging.Logger;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.RemoveType;

/**
 * Class that implements time series forecasting using a Weka regression scheme.
 * Makes use of the TSLagMaker class to handle all lagged attribute creation,
 * periodic attributes etc.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 51180 $
 */
public class WekaForecaster extends AbstractForecaster implements TSLagUser,
    ConfidenceIntervalForecaster, OverlayForecaster, IncrementallyPrimeable,
    OptionHandler, Serializable {

  /** For serialization */
  private static final long serialVersionUID = 5562710925011828590L;

  /** The format of the original incoming instances */
  protected Instances m_originalHeader;

  /** A copy of the input data provided to primeForecaster() */
  protected transient Instances m_primedInput;

  /** The format of the transformed data */
  protected Instances m_transformedHeader;

  /** The base regression scheme to use */
  protected Classifier m_forecaster = new SMOreg();

  /** The individual forecasters for each target */
  protected List<SingleTargetForecaster> m_singleTargetForecasters;

  /** True if the forecaster has been built */
  protected boolean m_modelBuilt = false;

  /** True if an artificial time index has been added to the data */
  protected boolean m_useArtificialTimeIndex = false;

  /**
   * The estimator used for calculating confidence limits.
   */
  protected ErrorBasedConfidenceIntervalEstimator m_confidenceLimitEstimator;

  /**
   * Number of steps ahead to calculate confidence limits for (0 = don't
   * calculate confidence limits
   */
  protected int m_calculateConfLimitsSteps = 0;

  /** Confidence level to compute confidence limits at */
  protected double m_confidenceLevel = 0.95;

  /** The lag maker to use */
  TSLagMaker m_lagMaker = new TSLagMaker();

  /**
   * For removing any date attributes (TSLagMaker will remap date timestamps to
   * numeric)
   */
  protected RemoveType m_dateRemover;

  /**
   * Holds a list of training instance indexes that contained missing target
   * values that were replaced via interpolation
   */
  protected List<Integer> m_missingTargetList;

  /**
   * Holds a list of training instance indexes that contained missing date
   * values (if a date time stamp is being used)
   */
  protected List<Integer> m_missingTimeStampList;

  protected List<String> m_missingTimeStampRows;

  /**
   * Logging object
   */
  protected Logger m_log;

  /**
   * Provides a short name that describes the underlying algorithm in some way.
   * 
   * @return a short description of this forecaster.
   */
  public String getAlgorithmName() {
    if (m_forecaster != null) {
      String spec = getForecasterSpec();
      spec = spec.replace("weka.classifiers.", "");
      spec = spec.replace("functions.", "");
      spec = spec.replace("bayes.", "");
      spec = spec.replace("rules.", "");
      spec = spec.replace("trees.", "");
      spec = spec.replace("meta.", "");
      spec = spec.replace("lazy.", "");
      spec = spec.replace("supportVector.", "");
      return spec;
    }

    return "";
  }

  /**
   * Set the TSLagMaker to use. All options pertaining to lag creation, periodic
   * attributes etc. are set via the lag maker.
   * 
   * @param lagger the TSLagMaker to use.
   */
  public void setTSLagMaker(TSLagMaker lagMaker) {
    m_lagMaker = lagMaker;
  }

  /**
   * Get the TSLagMaker that we are using. All options pertaining to lag
   * creation, periodic attributes etc. are set via the lag maker.
   * 
   * @return the TSLagMaker that we are using.
   */
  public TSLagMaker getTSLagMaker() {
    return m_lagMaker;
  }

  /**
   * Returns an enumeration describing the available options.
   * 
   * @return an enumeration of all the available options.
   */
  public Enumeration<Option> listOptions() {
    Vector<Option> newVector = new Vector<Option>();

    newVector.add(new Option("\tSet the fields to forecast.", "F", 1,
        "-F <comma separated list of names>"));
    newVector.add(new Option("\tSet the fields to be considered "
        + "as overlay data.", "overlay", 1,
        "-overlay <comma separated list of names>"));

    newVector.add(new Option("\tSet the minimum lag length to generate."
        + "\n\t(default = 1)", "L", 1, "-L <num>"));
    newVector.add(new Option("\tSet the maximum lag length to generate."
        + "\n\t(default = 12)", "M", 1, "-M <num>"));
    newVector.add(new Option("\tFine tune selection of lags within min and "
        + "max by specifying" + " ranges", "R", 1, "-R <ranges>"));
    newVector.add(new Option("\tAverage consecutive long lags.", "A", 0, "-A"));
    newVector.add(new Option("\tAverage those lags longer than this number of"
        + "time steps.\n\tUse in conjuction with -A.\n\t" + "(default = 2)",
        "B", 1, "-B <num>"));
    newVector.add(new Option("\tAverage this many consecutive long lags.\n\t"
        + "Use in conjuction with -B (default = 2)", "C", 1, "-C <num>"));
    newVector.add(new Option("\tDon't adjust for trends.", "Z", 0, "-Z"));
    newVector.add(new Option("\tSpecify the name of the timestamp field", "G",
        1, "-G <timestamp name>"));
    newVector.add(new Option("\tAdjust for variance.", "V", 0, "-V"));
    newVector.add(new Option("\tSpecify the primary periodic field, "
        + "\n\tif one exists already in the data "
        + "(e.g. day, month, quarter etc.\n\tIf there is more than"
        + "one such field, choose the one with the finest granularity.\n\t"
        + "This field must be" + "cyclic and declared as nominal.", "periodic",
        1, "-periodic <field name>"));
    newVector.add(new Option(
        "\tCalculate confidence limits for predictions\n\t"
            + "(based on errors) for up to, and including, "
            + "the specified\n\tnumber of time steps"
            + "into the future\n\t(default = 0 (don't compute conf. levels)).",
        "conf", 1, "-conf <num steps>"));
    newVector.add(new Option(
        "\tConfidence level for computing confidence limits.\n\t"
            + "Use in conjunction with -conf.\n\t(default = 0.95).", "P", 1,
        "-P <confidence level>"));
    newVector.add(new Option("\tSpecify the base regression scheme to use.\n\t"
        + "Supply a fully qualified name, along with options, enclosed in\n\t"
        + "quotes (e.g. \"weka.classifiers.functions.SMOreg -R 0.5\")."
        + "\n\t(default = weka.classifiers.functions.SMOreg)", "W", 1, "-W"));
    newVector.add(new Option(
        "\tAdd an AM/PM indicator (requires a date timestamp)", "am-pm", 0,
        "-am-pm"));
    newVector.add(new Option("\tAdd a day of the week field (requres a date"
        + " timestamp)", "day", 0, "-dayofweek"));
    newVector.add(new Option("\tAdd a day of the month field (requres a date"
        + " timestamp)", "dayofmonth", 0, "-dayofmonth"));
    newVector.add(new Option(
        "\tAdd a number of days in the month field (requres a date"
            + " timestamp)", "numdaysinmonth", 0, "-numdaysinmonth"));
    newVector.add(new Option(
        "\tAdd a weekend indicator (requires a date timestamp)", "weekend", 0,
        "-weekend"));
    newVector.add(new Option("\tAdd a month field (requires a date timestamp)",
        "month", 0, "-month"));
    newVector.add(new Option("\tAdd a quarter of the year field ("
        + "requires a date timestamp)", "quarter", 0, "-quarter"));

    newVector.add(new Option("\tAdd a custom date-derived boolean field ("
        + "requires a date timestamp).\n\tFormat: \"fieldName="
        + "Test Test|Test Test| ...\n\twhere "
        + "Test = OPERATORyear:month:week-of-yr:week-of-month:"
        + "day-of-yr:day-of-month:day-of-week:hour:min:second\n\te.g."
        + "XmasHoliday=>:dec::::24::: <:jan::::3:::\n\t"
        + "Legal OPERATORs are =,>,<,>=,<=. For = operator only\n\t"
        + "one Test is needed rather than a pair.\n\tThis option may"
        + " be specified more than once on the command line\n\t"
        + "in order to define multiple variables.", "custom", 1, "-custom"));
    newVector
        .add(new Option(
            "\tAdd a comma-separated 'skip' list of dates that should not\n\t"
                + "be considered as a time step. Days of the week,\n\t"
                + "months of the year, 'weekend', integers (indicating day of year\n\t"
                + ", hour of day etc.) or specific dates are all valid entries.\n\t"
                + "E.g sat,sun,27-08-2011,28-08-2011", "skip", 1, "-skip"));

    return newVector.elements();
  }

  /**
   * Set the options for the forecaster
   * 
   * @param options an array of options
   * @throws Exception if unknown options are supplied
   */
  public void setOptions(String[] options) throws Exception {

    String fieldsToForecast = Utils.getOption('F', options);
    if (fieldsToForecast.length() == 0) {
      throw new Exception(
          "Must specify the name of at least one field to forecast!");
    }
    setFieldsToForecast(fieldsToForecast);

    String overlayFields = Utils.getOption("overlay", options);
    if (overlayFields.length() > 0) {
      setOverlayFields(overlayFields);
    }

    String minL = Utils.getOption('L', options);
    if (minL.length() > 0) {
      int mL = Integer.parseInt(minL);
      // setMinLag(mL);
      m_lagMaker.setMinLag(mL);
      if (mL < 1) {
        throw new Exception("Minimum lag can't be less than 1!");
      }
    }

    String maxL = Utils.getOption('M', options);
    if (maxL.length() > 0) {
      int mL = Integer.parseInt(maxL);
      // setMaxLag(mL);
      m_lagMaker.setMaxLag(mL);
    }

    if (m_lagMaker.getMaxLag() < m_lagMaker.getMinLag()) {
      throw new Exception(
          "Can't have the maximum lag set lower than the minimum lag!");
    }

    String lagRange = Utils.getOption('R', options);
    if (lagRange.length() > 0) {
      m_lagMaker.setLagRange(lagRange);
    }

    boolean avLongLags = Utils.getFlag('A', options);
    // setAverageConsecutiveLongLags(!dontAv);
    m_lagMaker.setAverageConsecutiveLongLags(avLongLags);

    String avLongerThan = Utils.getOption('B', options);
    if (avLongerThan.length() > 0) {
      int avL = Integer.parseInt(avLongerThan);
      if (avL < m_lagMaker.getMinLag() || avL > m_lagMaker.getMaxLag()) {
        throw new Exception("Average consecutive long lags value can't "
            + "be less than the minimum lag or greater than the "
            + "maximum lag!");
      }
      // setAverageLagsAfter(avL);
      m_lagMaker.setAverageLagsAfter(avL);
    }

    String consecutiveLongLagS = Utils.getOption('C', options);
    if (consecutiveLongLagS.length() > 0) {
      int consecutive = Integer.parseInt(consecutiveLongLagS);
      if (consecutive < 1
          || consecutive > (m_lagMaker.getMaxLag() - m_lagMaker
              .getAverageLagsAfter())) {
        throw new Exception("Number of consecutive long lags to average "
            + "must be greater than 0 and less than "
            + (m_lagMaker.getMaxLag() - m_lagMaker.getMinLag()));
      }
      // setNumConsecutiveLongLagsToAverage(consecutive);
      m_lagMaker.setNumConsecutiveLongLagsToAverage(consecutive);
    }

    boolean dontAdjTrends = Utils.getFlag('Z', options);
    // setAdjustForTrends(!dontAdjTrends);
    m_lagMaker.setAdjustForTrends(!dontAdjTrends);

    boolean adjVariance = Utils.getFlag("V", options);
    // setAdjustForVariance(!dontAdjVariance);
    m_lagMaker.setAdjustForVariance(adjVariance);

    String timeStampF = Utils.getOption('G', options);
    if (timeStampF.length() > 0) {
      m_lagMaker.setTimeStampField(timeStampF);
    }

    m_lagMaker.setAddAMIndicator(Utils.getFlag("am-pm", options));
    m_lagMaker.setAddDayOfWeek(Utils.getFlag("dayofweek", options));
    m_lagMaker.setAddWeekendIndicator(Utils.getFlag("weekend", options));
    m_lagMaker.setAddMonthOfYear(Utils.getFlag("month", options));
    m_lagMaker.setAddQuarterOfYear(Utils.getFlag("quarter", options));
    m_lagMaker.setAddDayOfMonth(Utils.getFlag("dayofmonth", options));
    m_lagMaker.setAddNumDaysInMonth(Utils.getFlag("numdaysinmonth", options));

    // custom date-derived periodic fields
    String customPeriodic = Utils.getOption("custom", options);
    while (customPeriodic.length() > 0) {
      m_lagMaker.addCustomPeriodic(customPeriodic);
    }

    String primaryPeriodicN = Utils.getOption("periodic", options);
    if (primaryPeriodicN.length() > 0) {
      m_lagMaker.setPrimaryPeriodicFieldName(primaryPeriodicN);
    }

    String skipString = Utils.getOption("skip", options);
    if (skipString.length() > 0) {
      m_lagMaker.setSkipEntries(skipString);
    }

    String confSteps = Utils.getOption("conf", options);
    if (confSteps.length() > 0) {
      int numSteps = Integer.parseInt(confSteps);
      if (numSteps < 0) {
        throw new Exception("Number of steps must be >= 0");
      }
      setCalculateConfIntervalsForForecasts(numSteps);
    }

    String confLevel = Utils.getOption('P', options);
    if (confLevel.length() > 0) {
      double cL = Double.parseDouble(confLevel);
      if (cL < 0 || cL > 1) {
        throw new Exception("Confidence level must be between 0 and 1.");
      }
      setConfidenceLevel(cL);
    }

    String baseClassifierS = Utils.getOption('W', options);
    if (baseClassifierS.length() == 0) {
      baseClassifierS = "weka.classifiers.functions.SMOreg";
    }
    String[] classifierSpec = Utils.splitOptions(baseClassifierS);
    if (classifierSpec.length == 0) {
      throw new Exception("Invalid classifier specification.");
    }
    String classifierName = classifierSpec[0];
    classifierSpec[0] = "";
    setBaseForecaster(AbstractClassifier
        .forName(classifierName, classifierSpec));
  }

  /**
   * Gets the current settings of this Forecaster.
   * 
   * @return an array of strings suitable for passing to setOptions
   */
  public String[] getOptions() {
    ArrayList<String> options = new ArrayList<String>();

    // List<String> fieldsToForecast = m_lagMaker.getFieldsToLag();
    options.add("-F"); // options.add(fieldsToForecast.toString());
    options.add(getFieldsToForecast());

    if (getOverlayFields() != null && getOverlayFields().length() > 0) {
      options.add("-O");
      options.add(getOverlayFields());
    }

    options.add("-L");
    options.add("" + m_lagMaker.getMinLag());
    options.add("-M");
    options.add("" + m_lagMaker.getMaxLag());

    if (m_lagMaker.getLagRange().length() > 0) {
      options.add("-R");
      options.add(m_lagMaker.getLagRange());
    }

    if (m_lagMaker.getAverageConsecutiveLongLags()) {
      options.add("-A");
    } else {
      options.add("-B");
      options.add("" + m_lagMaker.getAverageLagsAfter());
    }

    options.add("-C");
    options.add("" + m_lagMaker.getNumConsecutiveLongLagsToAverage());

    if (!m_lagMaker.getAdjustForTrends()) {
      options.add("-Z");
    }

    if (m_lagMaker.getAdjustForVariance()) {
      options.add("-V");
    }

    if (m_lagMaker.getTimeStampField() != null
        && m_lagMaker.getTimeStampField().length() > 0) {
      options.add("-G");
      options.add(m_lagMaker.getTimeStampField());
    }

    if (m_lagMaker.getAddAMIndicator()) {
      options.add("-am-pm");
    }

    if (m_lagMaker.getAddDayOfWeek()) {
      options.add("-dayofweek");
    }

    if (m_lagMaker.getAddDayOfMonth()) {
      options.add("-dayofmonth");
    }

    if (m_lagMaker.getAddWeekendIndicator()) {
      options.add("-weekend");
    }

    if (m_lagMaker.getAddMonthOfYear()) {
      options.add("-month");
    }

    if (m_lagMaker.getAddNumDaysInMonth()) {
      options.add("-numdaysinmonth");
    }

    if (m_lagMaker.getAddQuarterOfYear()) {
      options.add("-quarter");
    }

    Map<String, ArrayList<CustomPeriodicTest>> customPeriodics = m_lagMaker
        .getCustomPeriodics();

    if (customPeriodics != null && customPeriodics.keySet().size() > 0) {
      for (String name : customPeriodics.keySet()) {
        List<CustomPeriodicTest> tests = customPeriodics.get(name);

        options.add("-custom");
        StringBuffer tempBuff = new StringBuffer();
        tempBuff.append("\"");
        for (int i = 0; i < tests.size(); i++) {
          tempBuff.append(tests.get(i).toString());
          if (i < tests.size() - 1) {
            tempBuff.append("|");
          } else {
            tempBuff.append("\"");
          }
        }
        options.add(tempBuff.toString());
      }
    }

    if (m_lagMaker.getSkipEntries() != null
        && m_lagMaker.getSkipEntries().length() > 0) {
      options.add("-skip");
      options.add(m_lagMaker.getSkipEntries());
    }

    if (m_lagMaker.getPrimaryPeriodicFieldName() != null
        && m_lagMaker.getPrimaryPeriodicFieldName().length() > 0) {
      options.add("-periodic");
      options.add(m_lagMaker.getPrimaryPeriodicFieldName());
    }

    options.add("-conf");
    options.add("" + getCalculateConfIntervalsForForecasts());

    options.add("-P");
    options.add("" + getConfidenceLevel());

    options.add("-W");
    options.add(getForecasterSpec());

    return options.toArray(new String[1]);
  }

  /**
   * Get the specification (scheme name + option setttings) of the underlying
   * Weka classifier.
   * 
   * @return the scheme name and options of the underlying Weka classifier
   */
  protected String getForecasterSpec() {
    Classifier c = getBaseForecaster();

    if (c instanceof OptionHandler) {
      return c.getClass().getName() + " "
          + Utils.joinOptions(((OptionHandler) c).getOptions());
    } else {
      return c.getClass().getName();
    }
  }

  /**
   * Add a custom date-derived periodic attribute
   * 
   * @param customPeriodic the string definition of the custom date derived
   *          periodic attribute to add
   */
  public void addCustomPeriodic(String customPeriodic) {
    m_lagMaker.addCustomPeriodic(customPeriodic);
  }

  /**
   * clear the list of custom date-derived periodic attributes
   */
  public void clearCustomPeriodics() {
    m_lagMaker.clearCustomPeriodics();
  }

  /**
   * Set the names of the fields/attributes in the data to forecast.
   * 
   * @param targets a list of names of fields to forecast
   * @throws Exception if a field(s) can't be found, or if multiple fields are
   *           specified and this forecaster can't predict multiple fields.
   */
  @Override
  public void setFieldsToForecast(String fieldsToForecast) throws Exception {
    super.setFieldsToForecast(fieldsToForecast);
    m_lagMaker.setFieldsToLag(m_fieldsToForecast);
  }

  /**
   * Set the fields to consider as overlay fields
   * 
   * @param overlayFields a comma-separated list of fieldnames
   * @throws Exception if there is a problem setting the overlay fields
   */
  public void setOverlayFields(String overlayFields) throws Exception {
    if (overlayFields == null) {
      m_lagMaker.setOverlayFields(null);
    } else {
      m_lagMaker.setOverlayFields(AbstractForecaster
          .stringToList(overlayFields));
    }
  }

  /**
   * Get a comma-separated list of fields that considered to be overlay fields
   * 
   * @return a list of field names
   */
  public String getOverlayFields() {
    String list = "";
    List<String> overlayF = m_lagMaker.getOverlayFields();
    if (overlayF != null) {
      for (String f : overlayF) {
        list += (f + ",");
      }

      list = list.substring(0, list.lastIndexOf(','));
    }

    return list;
  }

  /**
   * Set the name of the time stamp field
   * 
   * @param name the name of the time stamp attribute
   */
  /*
   * public void setTimeStampField(String name) {
   * m_lagMaker.setTimeStampField(name); }
   */

  /**
   * Get the name of the time stamp attribute
   * 
   * @return the name of the time stamp attribute or an empty string if none has
   *         been specified/is in use
   */
  /*
   * public String getTimeStampField() { return m_lagMaker.getTimeStampField();
   * }
   */

  /**
   * Set whether to include an AM binary indicator attribute.
   * 
   * @param am true if a binary AM indicator attribute is to be generated. Only
   *          has an effect if a date time stamp is in use.
   */
  /*
   * public void setAddAMIndicator(boolean am) {
   * m_lagMaker.setAddAMIndicator(am); }
   */

  /**
   * Returns true if an AM binary indicator is to be generated.
   * 
   * @return true if an AM binary indicator is to be generated.
   */
  /*
   * public boolean getAddAMIndicator() { return m_lagMaker.getAddAMIndicator();
   * }
   */

  /**
   * Set whether to include a day of the week attribute
   * 
   * @param am true if a day of the week attribute is to be generated. Only has
   *          an effect if a date time stamp is in use.
   */
  /*
   * public void setAddDayOfWeek(boolean d) { m_lagMaker.setAddDayOfWeek(d); }
   */

  /**
   * Returns true if a day of the week attribute is to be generated.
   * 
   * @return true if a day of the week attribute is to be generated.
   */
  /*
   * public boolean getAddDayOfWeek() { return m_lagMaker.getAddDayOfWeek(); }
   */

  /**
   * Set whether to include a weekend indicator attribute.
   * 
   * @param am true if a binary weekend indicator attribute is to be generated.
   *          Only has an effect if a date time stamp is in use.
   */
  /*
   * public void setAddWeekendIndicator(boolean w) {
   * m_lagMaker.setAddWeekendIndicator(w); }
   */

  /**
   * Returns true if a weekend binary indicator attribute is to be generated.
   * 
   * @return true if a weekend binary indicator attribute is to be generated.
   */
  /*
   * public boolean getAddWeekendIndicator() { return
   * m_lagMaker.getAddWeekendIndicator(); }
   */

  /**
   * Set whether to include a month of the year attribute.
   * 
   * @param am true if a month of the year attribute is to be generated. Only
   *          has an effect if a date time stamp is in use.
   */
  /*
   * public void setAddMonthOfYear(boolean m) { m_lagMaker.setAddMonthOfYear(m);
   * }
   */

  /**
   * Returns true if a month of the year attribute is to be generated.
   * 
   * @return true if a month of the year attribute is to be generated.
   */
  /*
   * public boolean getAddMonthOfYear() { return m_lagMaker.getAddMonthOfYear();
   * }
   */

  /**
   * Set whether to include a quarter of the year attribute.
   * 
   * @param am true if a quarter of the year attribute is to be generated. Only
   *          has an effect if a date time stamp is in use.
   */
  /*
   * public void setAddQuarterOfYear(boolean q) {
   * m_lagMaker.setAddQuarterOfYear(q); }
   */

  /**
   * Return true if a quarter of the year attribute is to be generated.
   * 
   * @return if a quarter of the year attribute is to be generated.
   */
  /*
   * public boolean getAddQuarterOfYear() { return
   * m_lagMaker.getAddQuarterOfYear(); }
   */

  /**
   * Set the name of the field to be considered the primary periodic field (if
   * any). This field is one which is not a date-based attribute but is periodic
   * and cyclic and declared as nominal. Each distinct value can only be
   * succeeded by a single value (so that it is possible to set the appropriate
   * values in successive future instances). Any secondary, higher-grained
   * periodic fields will automatically be detected once a primary field is
   * specified.
   * 
   * 
   * @param p the name of a primary periodic field (if any)
   */
  /*
   * public void setPrimaryPeriodicFieldName(String p) { //m_primaryPeriodicName
   * = p; m_lagMaker.setPrimaryPeriodicFieldName(p); }
   */

  /**
   * Get the name of the primary periodic field (if set).
   * 
   * @return the name of the primary periodic field or an empty string if none
   *         has been set/exists.
   */
  /*
   * public String getPrimaryPeriodicFieldName() { return
   * m_lagMaker.getPrimaryPeriodicFieldName(); }
   */

  /**
   * Set the number of steps for which to compute confidence intervals for. E.g.
   * a value of 5 means that confidence bounds will be computed for 1-step-ahead
   * predictions, 2-step-ahead predictions, ..., 5-step-ahead predictions.
   * 
   * @param steps the number of steps for which to compute confidence intervals
   *          for.
   */
  public void setCalculateConfIntervalsForForecasts(int steps) {
    m_calculateConfLimitsSteps = steps;
  }

  /**
   * Return the number of steps for which confidence intervals will be computed.
   * 
   * @return the number of steps for which confidence intervals will be
   *         computed.
   */
  public int getCalculateConfIntervalsForForecasts() {
    return m_calculateConfLimitsSteps;
  }

  /**
   * Returns true if this forecaster is computing confidence limits for some or
   * all of its future forecasts (i.e. getCalculateConfIntervalsForForecasts() >
   * 0).
   * 
   * @return true if confidence limits will be produced for some or all of its
   *         future forecasts.
   */
  public boolean isProducingConfidenceIntervals() {
    return (getCalculateConfIntervalsForForecasts() > 0);
  }

  /**
   * Set the confidence level for confidence intervals.
   * 
   * @param confLevel the confidence level to use.
   */
  public void setConfidenceLevel(double confLevel) {
    m_confidenceLevel = confLevel;
  }

  /**
   * Get the confidence level in use for computing confidence intervals.
   * 
   * @return the confidence level.
   */
  public double getConfidenceLevel() {
    return m_confidenceLevel;
  }

  /**
   * Set the base Weka regression scheme to use.
   * 
   * @param f the base Weka regression scheme to use for forecasting.
   */
  public void setBaseForecaster(Classifier f) {
    m_forecaster = f;
  }

  /**
   * Get the base Weka regression scheme being used to make forecasts
   * 
   * @return the base Weka regression scheme
   */
  public Classifier getBaseForecaster() {
    return m_forecaster;
  }

  /**
   * Returns true if overlay data has been used to train this forecaster, and
   * thus is expected to be supplied for future time steps when making a
   * forecast.
   * 
   * @return true if overlay data is expected.
   */
  public boolean isUsingOverlayData() {
    if (m_lagMaker.getOverlayFields() != null
        && m_lagMaker.getOverlayFields().size() > 0) {
      return true;
    }

    return false;
  }

  /**
   * Reset the forecaster.
   */
  public void reset() {
    m_modelBuilt = false;

    /*
     * m_varianceAdjusters = null; m_lagMakers = null; m_averagedLagMakers =
     * null; m_timeIndexMakers = null; m_timeLagCrossProductMakers = null;
     */

    m_lagMaker.reset();
    m_dateRemover = null;

    m_primedInput = null;
    m_confidenceLimitEstimator = null;
    m_missingTargetList = new ArrayList<Integer>();
    m_missingTimeStampList = new ArrayList<Integer>();
    m_missingTimeStampRows = new ArrayList<String>();
  }

  /**
   * Inner class implementing a forecaster for a single target.
   * 
   * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
   * 
   */
  protected class SingleTargetForecaster implements Serializable {

    /** for serialization */
    private static final long serialVersionUID = -4404412501006669036L;

    /** the underlying Weka classifier used to make forecasts */
    protected Classifier m_targetForecaster;

    /** filter for removing the targets other than the one to be forecasted */
    private Remove m_otherTargetRemover;
    private int m_classIndex;
    private String m_className;

    /**
     * Set the base classifier to use
     * 
     * @param classifier the base classifier to use
     */
    public void setClassifier(Classifier classifier) {
      m_targetForecaster = classifier;
    }

    /**
     * Builds the single target forecaster. Assumes that the training data has
     * already been transformed by the lag maker.
     * 
     * @param train the transformed training data
     * @param targetName the name of the target to forecast
     * @param progress an optional varargs parameter of PrintStream to report
     *          progress to
     * @throws Exception if a problem occurs
     */
    public void buildForecaster(Instances train, String targetName,
        PrintStream... progress) throws Exception {

      if (m_targetForecaster == null) {
        throw new Exception("[SingleTargetForecaster] base classifier has"
            + " not been set!");
      }

      train = new Instances(train);
      m_classIndex = train.attribute(targetName).index();

      if (m_classIndex < 0) {
        throw new Exception("Can't find target field '" + targetName + "' in"
            + "the data!");
      }

      if (!train.attribute(m_classIndex).isNumeric()) {
        throw new Exception("[SingleTargetForecaster] target '" + targetName
            + "' is not numeric!");
      }

      train.setClassIndex(m_classIndex);
      m_className = targetName;

      String otherTargets = "";
      for (String n : m_fieldsToForecast) {
        if (!n.equals(targetName)) {
          int i = train.attribute(n).index();
          if (i >= 0) {
            otherTargets += (i + 1) + ",";
          }
        }
      }

      if (otherTargets.length() > 0) {
        otherTargets = otherTargets.substring(0, otherTargets.lastIndexOf(','));
        m_otherTargetRemover = new Remove();
        m_otherTargetRemover.setAttributeIndices(otherTargets);
        m_otherTargetRemover.setInputFormat(train);
        train = Filter.useFilter(train, m_otherTargetRemover);
      }

      for (PrintStream p : progress) {
        p.println("Building forecaster for target: " + m_className);
      }

      m_targetForecaster.buildClassifier(train);
    }

    /**
     * Makes a one-step-ahead forecast
     * 
     * @param transformed the test instance for the next time step. This will
     *          have already been processed by the lag maker, and thus will
     *          contain lagged variables and other derived variables.
     * 
     * @return the one-step-ahead forecast corresponding to the supplied test
     *         instance
     * @throws Exception if something goes wrong during the forecasting process
     */
    public double forecastOneStepAhead(Instance transformed) throws Exception {
      transformed.dataset().setClassIndex(m_classIndex);

      if (m_otherTargetRemover != null) {
        m_otherTargetRemover.input(transformed);
        transformed = m_otherTargetRemover.output();
      }
      double pred = m_targetForecaster.classifyInstance(transformed);

      // undo the log if adjusting for variance
      if (m_lagMaker.getAdjustForVariance()) {
        pred = Math.exp(pred);
      }
      return pred;
    }

    @Override
    public String toString() {
      if (m_targetForecaster == null) {
        return "SingleTargetForecaster: no model built yet!";
      }

      return m_className + ":\n" + m_targetForecaster.toString();
    }
  }

  /**
   * Builds a new forecasting model using the supplied training data. The
   * instances in the data are assumed to be sorted in ascending order of time
   * and equally spaced in time. Some methods may not need to implement this
   * method and may instead do their work in the primeForecaster method.
   * 
   * @param insts the training instances.
   * @param progress an optional varargs parameter supplying progress objects to
   *          report/log to
   * @throws Exception if the model can't be constructed for some reason.
   */
  @Override
  public void buildForecaster(Instances insts, PrintStream... progress)
      throws Exception {

    reset();
    m_originalHeader = new Instances(insts, 0);
    /*
     * insts = m_lagMaker.replaceMissing(insts, false, m_missingTargetList,
     * m_missingTimeStampList);
     */
    insts = new Instances(insts);
    insts = weka.classifiers.timeseries.core.Utils.replaceMissing(insts,
        m_fieldsToForecast, m_lagMaker.getTimeStampField(), false,
        m_lagMaker.getPeriodicity(), m_lagMaker.getSkipEntries(),
        m_missingTargetList, m_missingTimeStampList, m_missingTimeStampRows);

    /*
     * int classIndex = insts.attribute(m_fieldsToForecast.get(0)).index(); if
     * (classIndex < 0) { throw new Exception("Can't find target field '" +
     * m_fieldsToForecast + "' in" + "the data!"); }
     */

    // setupPeriodicMaps(insts);

    for (PrintStream p : progress) {
      p.println("Transforming input data...");
    }

    // Instances trainingData = removeExtraneousAttributes(insts);
    Instances trainingData = insts;
    trainingData = m_lagMaker.getTransformedData(trainingData);
    // System.err.println(trainingData);
    m_transformedHeader = new Instances(trainingData, 0);

    m_dateRemover = new RemoveType();
    m_dateRemover.setOptions(new String[] { "-T", "date" });
    m_dateRemover.setInputFormat(trainingData);
    trainingData = Filter.useFilter(trainingData, m_dateRemover);

    // m_lastHistoricInstance = insts.instance(insts.numInstances() - 1);

    m_singleTargetForecasters = new ArrayList<SingleTargetForecaster>();
    for (int i = 0; i < m_fieldsToForecast.size(); i++) {
      SingleTargetForecaster f = new SingleTargetForecaster();
      Classifier c = AbstractClassifier.makeCopy(m_forecaster);
      f.setClassifier(c);
      f.buildForecaster(trainingData, m_fieldsToForecast.get(i));
      m_singleTargetForecasters.add(f);
    }

    /*
     * classIndex = trainingData.attribute(m_fieldsToForecast.get(0)).index();
     * trainingData.setClassIndex(classIndex);
     * m_forecaster.buildClassifier(trainingData);
     */

    m_modelBuilt = true;
    /*
     * for (int i = 0; i < m_singleTargetForecasters.size(); i++) {
     * System.out.println(m_singleTargetForecasters.get(i)); }
     */

    if (m_calculateConfLimitsSteps > 0) {
      for (PrintStream p : progress) {
        p.println("Computing confidence intervals...");
      }
      // -1 indicates not using an artificial time index
      int artificialTimeStart = (m_lagMaker.isUsingAnArtificialTimeIndex()) ? 1
          : -1;
      ErrorBasedConfidenceIntervalEstimator e = new ErrorBasedConfidenceIntervalEstimator();
      e.calculateConfidenceOffsets(this, insts, m_lagMaker.getMaxLag(),
          artificialTimeStart, m_calculateConfLimitsSteps, m_confidenceLevel,
          progress);
      m_confidenceLimitEstimator = e;
    }
    // System.out.println(trainingData);
  }

  @Override
  public String toString() {
    if (!m_modelBuilt) {
      return "Forecaster has not been built yet!";
    }

    StringBuffer result = new StringBuffer();
    result.append("Transformed training data:\n\n");
    for (int i = 0; i < m_transformedHeader.numAttributes(); i++) {
      result.append("              " + m_transformedHeader.attribute(i).name())
          .append("\n");
    }

    if (m_missingTimeStampRows != null && m_missingTimeStampRows.size() > 0) {
      result
          .append("\n--------------------------------------------------------\n"
              + "Instances were inserted in the taining data for the\n"
              + "following time-stamps (target values set by interpolation):\n\n");
      for (int i = 0; i < m_missingTimeStampRows.size(); i++) {
        if (i == 0) {
          result.append("              " + m_missingTimeStampRows.get(i));
        } else {
          result.append(", " + m_missingTimeStampRows.get(i));
        }
      }

      result
          .append("\n--------------------------------------------------------\n");
    }

    if (m_missingTargetList != null && m_missingTargetList.size() > 0) {
      Collections.sort(m_missingTargetList);
      result.append("\n---------------------------------------------------\n"
          + "The following training instances had missing values\n"
          + "imputed via interpolation. Check source data as\n"
          + "this may affect forecasting performance:\n\n");
      for (int i = 0; i < m_missingTargetList.size(); i++) {
        if (i == 0) {
          result.append("              " + m_missingTargetList.get(i));
        } else if (!m_missingTargetList.get(i).equals(
            m_missingTargetList.get(i - 1))) {
          result.append("," + m_missingTargetList.get(i));
        }
      }
      result.append("\n---------------------------------------------------\n");
    }

    if (m_missingTimeStampList != null && m_missingTimeStampList.size() > 0) {
      Collections.sort(m_missingTimeStampList);
      result
          .append("\n--------------------------------------------------------\n"
              + "The following training instances had missing time stamps:\n\n");
      for (int i = 0; i < m_missingTimeStampList.size(); i++) {
        if (i == 0) {
          result.append("              " + m_missingTimeStampList.get(i));
        } else {
          result.append("," + m_missingTimeStampList.get(i));
        }
      }
      result
          .append("\n-------------------------------------------------------\n");
    }

    // System.out.println(m_transformedHeader);
    for (int i = 0; i < m_singleTargetForecasters.size(); i++) {
      result.append("\n" + m_singleTargetForecasters.get(i)).append("\n");
    }

    return result.toString();
  }

  protected Instance applyFilters(Instance source,
      boolean incrementArtificialTime, boolean setAnyPeriodic) throws Exception {
    Instance result = source;

    /*
     * if (m_extraneousAttributeRemover != null) {
     * m_extraneousAttributeRemover.input(result); result =
     * m_extraneousAttributeRemover.output(); }
     */

    result = m_lagMaker.processInstance(result, incrementArtificialTime,
        setAnyPeriodic);

    return result;
  }

  /**
   * Supply the (potentially) trained model with enough historical data, up to
   * and including the current time point, in order to produce a forecast.
   * Instances are assumed to be sorted in ascending order of time and equally
   * spaced in time.
   * 
   * @param insts the instances to prime the model with
   * @throws Exception if the model can't be primed for some reason.
   */
  @Override
  public void primeForecaster(Instances insts) throws Exception {

    m_primedInput = new Instances(insts);
    m_previousPrimeInstance = null; // only used by the incremental method
    m_missingBuffer = new Instances(insts, 0);
    m_hadLeadingMissingPrime = false;
    m_first = true;
    m_atLeastOneNonMissingTimeStamp = false;

    // m_lastHistoricInstance =
    // m_primedInput.instance(m_primedInput.numInstances() - 1);
    m_lagMaker.clearLagHistories();

    // System.err.println(insts + "\n\n");
    for (int i = 0; i < m_primedInput.numInstances(); i++) {
      // applyFilters(m_primedInput.instance(i), false, false);

      primeForecasterIncremental(m_primedInput.instance(i));
      m_first = false;
    }
  }

  // used by the incremental method when detecting missing values in
  // targets/date
  private transient Instance m_previousPrimeInstance = null;
  private transient Instances m_missingBuffer = null;
  private transient boolean m_hadLeadingMissingPrime = false;
  private transient boolean m_first = false;
  private transient boolean m_atLeastOneNonMissingTimeStamp = false;

  /**
   * Update the priming information incrementally, i.e. one instance at a time.
   * To indicate the start of a new batch of priming data an empty set of
   * instances must be passed to TSForecaster.primeForecaster() before the first
   * call to primeForecasterIncremental()
   * 
   * @param inst the instance to prime with.
   * @throws Exception if something goes wrong.
   */
  public void primeForecasterIncremental(Instance inst) throws Exception {
    if (m_primedInput == null) {
      throw new Exception("WekaForecaster hasn't been initialized with "
          + "a call to primeForecaster()!!");
    }

    if (!m_lagMaker.isUsingAnArtificialTimeIndex()
        && m_lagMaker.getAdjustForTrends()
        && m_lagMaker.getTimeStampField() != null
        && m_lagMaker.getTimeStampField().length() > 0) {

      // we have at least one valid time stamp value - missing value routine
      // can increment/decrement from this to fill in missing time stamp values
      // forward (increment) is done below; backward is done by
      // Utils.replaceMissing()

      // if we have a previous row, then check that time values are increasing
      if (!m_first
          && m_previousPrimeInstance != null
          && !m_previousPrimeInstance.isMissing(inst.dataset().attribute(
              m_lagMaker.getTimeStampField()))) {

        double previous = m_previousPrimeInstance.value(inst.dataset()
            .attribute(m_lagMaker.getTimeStampField()));
        double current = inst.value(inst.dataset().attribute(
            m_lagMaker.getTimeStampField()));

        if (current <= previous) {
          throw new Exception("Priming instances do not appear to be in "
              + "ascending order of the time stamp field ("
              + m_lagMaker.getTimeStampField() + ")! "
              + m_previousPrimeInstance + " : " + inst);
        }
      }
    }

    boolean wasBuffered = false;
    boolean onlyTimeMissing = false;

    if (inst.hasMissingValue()) {
      // first check to see if its a target or date
      boolean ok = true;
      for (String target : m_fieldsToForecast) {
        if (inst.isMissing(inst.dataset().attribute(target))) {
          ok = false;
          break;
        }
      }
      // check date
      if (!m_lagMaker.isUsingAnArtificialTimeIndex()
          && m_lagMaker.getAdjustForTrends()
          && m_lagMaker.getTimeStampField() != null
          && m_lagMaker.getTimeStampField().length() > 0) {

        if (inst.isMissing(inst.dataset().attribute(
            m_lagMaker.getTimeStampField()))) {
          onlyTimeMissing = ok;

          // do we have a previous instance with non-missing time stamp?
          if (m_previousPrimeInstance != null
              && !m_previousPrimeInstance.isMissing(inst.dataset().attribute(
                  m_lagMaker.getTimeStampField()))) {

            // set the correct time stamp value by incrementing by the
            // lag maker's delta time

            // this handles trailing missing time stamp values
            double newValue = m_previousPrimeInstance.value(inst.dataset()
                .attribute(m_lagMaker.getTimeStampField()));
            newValue = m_lagMaker.advanceSuppliedTimeValue(newValue);
            inst.setValue(
                inst.dataset().attribute(m_lagMaker.getTimeStampField()),
                newValue);
            // System.err.println("** " + inst);
          }
        } else {
          m_atLeastOneNonMissingTimeStamp = true;
        }
      }

      if (!ok) {
        if (m_first) {
          // can't do anything with leading missing values, unless its the
          // time stamp that's missing

          // leading missing time stamps will get filled in by the missing
          // value replacement routine when the buffer gets flushed. Trailing
          // missing time stamp values get handled above. Exception when
          // *all* time stamp values are missing. Nothing can be done in this
          // case

          m_hadLeadingMissingPrime = !onlyTimeMissing;
          // ---
          m_missingBuffer.add(inst);
          // m_previousPrimeInstance = inst;
          wasBuffered = true;
        } /*
           * else if (m_missingBuffer.numInstances() == 0 &&
           * m_previousPrimeInstance != null) { // first one with missing - need
           * to add the previous instance
           * m_missingBuffer.add(m_previousPrimeInstance);
           * m_previousPrimeInstance = null;
           * 
           * m_missingBuffer.add(inst); wasBuffered = true; }
           */else /* if (m_missingBuffer.numInstances() > 0) */{
          m_missingBuffer.add(inst);
          wasBuffered = true;
          // m_previousPrimeInstance = inst;
        }
      }
    } else {
      if (!m_lagMaker.isUsingAnArtificialTimeIndex()
          && m_lagMaker.getAdjustForTrends()
          && m_lagMaker.getTimeStampField() != null
          && m_lagMaker.getTimeStampField().length() > 0) {

        // we have at least one valid time stamp value - missing value routine
        // can increment/decrement from this to fill in missing time stamp
        // values
        // forward (increment) is done above; backward is done by
        // Utils.replaceMissing()
        m_atLeastOneNonMissingTimeStamp = true;
      }
    }

    m_previousPrimeInstance = inst;

    if (!wasBuffered && m_missingBuffer.numInstances() > 0) {
      // add this one first
      m_missingBuffer.add(inst);
      wasBuffered = true;

      // interpolate missing and then flush the buffer
      Instances missingReplaced = weka.classifiers.timeseries.core.Utils
          .replaceMissing(m_missingBuffer, m_fieldsToForecast,
              m_lagMaker.getTimeStampField(), false,
              m_lagMaker.getPeriodicity(), m_lagMaker.getSkipEntries());

      /*
       * // don't push the first instance into the filters because this one //
       * has already been pushed in earlier.
       */
      for (int i = 0; i < missingReplaced.numInstances(); i++) {
        applyFilters(missingReplaced.instance(i), false, false);
      }
      m_missingBuffer = new Instances(m_primedInput, 0);
      // m_previousPrimeInstance = inst;
    } else if (!wasBuffered) {
      applyFilters(inst, false, false);
      // m_previousPrimeInstance = inst;
    }
    m_first = false;
  }

  /**
   * Make a one-step-ahead forecast for the supplied test instance
   * 
   * @param transformed a test instance, corresponding to the next time step,
   *          that has been transformed using the lag maker
   * 
   * @return a one-step-ahead forecast corresponding to the test instance.
   * @throws Exception if a problem occurs
   */
  protected double forecastOneStepAhead(Instance transformed) throws Exception {
    return m_forecaster.classifyInstance(transformed);
  }

  /**
   * Produce a forecast for the target field(s). Assumes that the model has been
   * built and/or primed so that a forecast can be generated.
   * 
   * @param numSteps number of forecasted values to produce for each target.
   *          E.g. a value of 5 would produce a prediction for t+1, t+2, ...,
   *          t+5. if no overlay data has been used during training)
   * @param progress an optional varargs parameter supplying progress objects to
   *          report/log to
   * @return a List of Lists (one for each step) of forecasted values for each
   *         target
   * @throws Exception if the forecast can't be produced for some reason.
   */
  @Override
  public List<List<NumericPrediction>> forecast(int numSteps,
      PrintStream... progress) throws Exception {
    return forecast(numSteps, null, progress);
  }

  /**
   * Produce a forecast for the target field(s). Assumes that the model has been
   * built and/or primed so that a forecast can be generated.
   * 
   * @param numSteps number of forecasted values to produce for each target.
   *          E.g. a value of 5 would produce a prediction for t+1, t+2, ...,
   *          t+5.
   * @param overlay optional overlay data for the period to be forecasted (may
   *          be null if no overlay data has been used during training)
   * @param progress an optional varargs parameter supplying progress objects to
   *          report/log to
   * @return a List of Lists (one for each step) of forecasted values for each
   *         target
   * @throws Exception if the forecast can't be produced for some reason.
   */
  public List<List<NumericPrediction>> forecast(int numSteps,
      Instances overlay, PrintStream... progress) throws Exception {

    if (overlay != null) {
      if (m_lagMaker.getOverlayFields() == null
          || m_lagMaker.getOverlayFields().size() == 0) {
        throw new Exception(
            "[WekaForecaster] overlay data has been supplied to the"
                + " forecasting routine but no overlay data has been trained with.");
      }

      String message = m_originalHeader.equalHeadersMsg(overlay);
      if (message != null) {
        throw new Exception("[WekaForecaster] supplied overlay data does not "
            + "have the same structure as the data used to learn "
            + "the model!");
      }
    } else {
      // check to see if we've been trained with overlay data
      if (m_lagMaker.getOverlayFields() != null
          && m_lagMaker.getOverlayFields().size() > 0) {
        throw new Exception(
            "[WekaForecaster] was trained with overlay data but "
                + "none has been supplied for making a forecast!");
      }
    }

    // we need to:
    // 1) input a new instance with ? for target into the filter chain in order
    // to push the most recent
    // known target value into the history
    // 2) output() from filter
    // 3) make the t + 1 prediction
    // 4) set the value of the target for the input instance (this instance, now
    // stored in
    // the history buffer of the TimeseriesTranslate filters, will now have the
    // predicted target
    // value - hopefully)

    // 4 won't work. Need to add the input instance (with prediction set) to the
    // end
    // of the primed input data set and then call primeForecaster() again

    // double[] finalForecast = new double[numSteps];

    // Check the incremental prime buffer to see if there are any pending
    // instances to prime. We won't be able to interpolate missing values for
    // the
    // remaining instances (since there wasn't a prime instance received with
    // non-missing
    // values to right-hand-side bracket the ones with missing values. So, we'll
    // just
    // have to flush this buffer (which means the missing values will go into
    // the history
    // list and the underlying predictor's missing value strategy will be
    // invoked). We
    // should warn to the progress/log though. Similarly, for leading prime
    // instances
    // with missing values (i.e. no left-hand-side non-missing bracketing
    // instance) we
    // should warn to the progress/log
    if (m_missingBuffer != null && m_missingBuffer.numInstances() > 0) {
      // make one more attempt to interpolate missing values. In the incremental
      // priming process, the missing value interpolation for currently buffered
      // leading instances is *only* triggered when receiving a priming instance
      // where *all* target values are not missing. If this never occurs, i.e.
      // every priming instance has at least one of the targets missing, then
      // it is still possible that some of the missing values for some targets
      // can be interpolated
      System.err.println("Here..... \n\n" + m_missingBuffer);
      Instances missingReplaced = weka.classifiers.timeseries.core.Utils
          .replaceMissing(m_missingBuffer, m_fieldsToForecast,
              m_lagMaker.getTimeStampField(), false,
              m_lagMaker.getPeriodicity(), m_lagMaker.getSkipEntries());

      for (int i = 0; i < m_missingBuffer.numInstances(); i++) {
        applyFilters(missingReplaced.instance(i), false, false);
      }

      for (PrintStream p : progress) {
        p.println("WARNING: priming data contained missing target/date values that could "
            + "not be interpolated/replaced. Forecasting performance may be "
            + "adversely affected.");
      }
    }
    if (m_hadLeadingMissingPrime) {
      for (PrintStream p : progress) {
        p.println("WARNING: priming data contained missing target/date values that could "
            + "not be interpolated/replaced. Forecasting performance may be "
            + "adversely affected.");
      }
    }

    if (!m_lagMaker.isUsingAnArtificialTimeIndex()
        && m_lagMaker.getAdjustForTrends()
        && m_lagMaker.getTimeStampField() != null
        && m_lagMaker.getTimeStampField().length() > 0
        && !m_atLeastOneNonMissingTimeStamp) {

      throw new Exception("All values of the time stamp field ("
          + m_lagMaker.getTimeStampField() + ") were missing in the priming "
          + "data!");

    }

    List<List<NumericPrediction>> forecastForSteps = new ArrayList<List<NumericPrediction>>();
    int stepsToDo = (overlay != null) ? overlay.numInstances() : numSteps;
    boolean setPeriodics = true, incrementTime = true;

    for (int i = 0; i < stepsToDo; i++) {
      incrementTime = true;

      // set the target to missing first
      double[] newVals = new double[m_originalHeader.numAttributes()];
      // set all to missing
      for (int j = 0; j < newVals.length; j++) {
        newVals[j] = Utils.missingValue();
      }

      // copy over any overlay fields and time (if present in overlay data)
      if (overlay != null) {
        Instance overlayI = overlay.instance(i);
        for (String field : m_lagMaker.getOverlayFields()) {
          int index = m_originalHeader.attribute(field).index();
          newVals[index] = overlayI.value(index);
        }

        // non missing time stamp?
        if (!m_lagMaker.isUsingAnArtificialTimeIndex()
            && m_lagMaker.getAdjustForTrends()
            && m_lagMaker.getTimeStampField() != null
            && m_lagMaker.getTimeStampField().length() > 0) {

          int timeStampIndex = m_originalHeader.attribute(
              m_lagMaker.getTimeStampField()).index();
          if (!overlayI.isMissing(timeStampIndex)) {
            newVals[timeStampIndex] = overlayI.value(timeStampIndex);

            // want to store, rather than increment, time value since
            // we've read a time value from the overlay data
            incrementTime = false;
          }
        }
      }

      // create the test instance (original format)
      Instance origTest = new DenseInstance(1.0, newVals);
      origTest.setDataset(m_originalHeader);

      // System.err.println("Original with periodic set " + origTest);

      Instance transformedWithDate = origTest;

      // do all the filters
      // System.err.println("--- " + transformedWithDate);

      // transformedWithDate = applyFilters(transformedWithDate, true, true);
      transformedWithDate = m_lagMaker.processInstancePreview(
          transformedWithDate, incrementTime, setPeriodics);

      // the date time stamp (if exists) has now been remapped, so we can remove
      // the original
      m_dateRemover.input(transformedWithDate);
      Instance transformed = m_dateRemover.output();

      // System.err.println(transformedWithDate.dataset());
      // System.err.println(transformedWithDate);

      // System.err.println("Transformed: " + transformed);

      // get a prediction
      double[] preds = new double[m_singleTargetForecasters.size()];
      for (int j = 0; j < m_singleTargetForecasters.size(); j++) {
        preds[j] = m_singleTargetForecasters.get(j).forecastOneStepAhead(
            transformed);
      }

      // predictions at step i for all the targets (can only handle a single
      // target at
      // present)
      List<NumericPrediction> finalForecast = new ArrayList<NumericPrediction>();

      // add confidence limits (if applicable)
      for (int j = 0; j < m_fieldsToForecast.size(); j++) {
        if (m_confidenceLimitEstimator != null
            && i < m_calculateConfLimitsSteps) {
          double[] limits = m_confidenceLimitEstimator
              .getConfidenceLimitsForTarget(m_fieldsToForecast.get(j),
                  preds[j], i + 1);
          double[][] limitsToAdd = new double[1][];
          limitsToAdd[0] = limits;
          finalForecast.add(new NumericPrediction(Utils.missingValue(),
              preds[j], 1.0, limitsToAdd));
        } else {
          finalForecast.add(new NumericPrediction(Utils.missingValue(),
              preds[j]));
        }
      }
      forecastForSteps.add(finalForecast);

      // set the value of the target in the original test instance
      for (int j = 0; j < m_fieldsToForecast.size(); j++) {
        int targetIndex = m_originalHeader.attribute(m_fieldsToForecast.get(j))
            .index();
        origTest.setValue(targetIndex, preds[j]);
      }

      // If we have a real time stamp, then set the incremented value in the
      // original
      // test instance (doesn't really need to be done if we've read a
      // non-missing
      // time value out of any supplied overlay data)
      if (!m_lagMaker.isUsingAnArtificialTimeIndex()
          && m_lagMaker.getAdjustForTrends()
          && m_lagMaker.getTimeStampField() != null
          && m_lagMaker.getTimeStampField().length() > 0) {
        int timeIndex = m_originalHeader.attribute(
            m_lagMaker.getTimeStampField()).index();
        double timeValue = transformedWithDate.value(transformedWithDate
            .dataset().attribute(m_lagMaker.getTimeStampField()));
        origTest.setValue(timeIndex, timeValue);
      }

      // now re-prime the forecaster. Incremental method will never buffer here
      // because we never have missing targets, since we've just forecasted
      // them!
      primeForecasterIncremental(origTest);
    }

    // TODO fix this - move to eval class?
    if (m_lagMaker.isUsingAnArtificialTimeIndex()) {
      m_lagMaker.incrementArtificialTimeValue(-(stepsToDo - 1));
      // -= (numSteps - 1);
    }

    // invalidate the primed input header
    m_primedInput = null;

    return forecastForSteps;
  }

  /**
   * Main method for running this class from the command line
   * 
   * @param args general and scheme-specific command line arguments
   */
  public static void main(String[] args) {
    try {
      /*
       * Instances train = new Instances(new BufferedReader(new
       * FileReader(args[0]))); WekaForecaster wf = new WekaForecaster();
       * ArrayList<String> fieldsToForecast = new ArrayList<String>();
       * fieldsToForecast.add(args[1]);
       * wf.setFieldsToForecast(fieldsToForecast);
       * wf.setPrimaryPeriodicFieldName(args[2]); Instances trans =
       * wf.getTransformedData(train); System.out.println(trans);
       */

      WekaForecaster fs = new WekaForecaster();
      fs.runForecaster(fs, args);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
