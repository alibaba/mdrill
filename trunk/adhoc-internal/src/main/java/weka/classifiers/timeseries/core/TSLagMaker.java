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
 *    TSLagMaker.java
 *    Copyright (C) 2010 Pentaho Corporation
 */

package weka.classifiers.timeseries.core;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Range;
import weka.core.SelectedTag;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.AddExpression;
import weka.filters.unsupervised.attribute.AddID;
import weka.filters.unsupervised.attribute.Copy;
import weka.filters.unsupervised.attribute.MathExpression;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.RenameAttribute;

/**
 * A class for creating lagged versions of target variable(s) for use in time
 * series forecasting. Uses the TimeseriesTranslate filter. Has options for
 * creating averages of consecutive lagged variables (which can be useful for
 * long lagged variables). Some polynomials of time are also created (if there
 * is a time stamp), such as time^2 and time^3. Also creates cross products
 * between time and the lagged and averaged lagged variables. If there is no
 * date time stamp in the data then the user has the option of having an
 * artificial time stamp created. Time stamps, real or otherwise, are used for
 * modeling trends rather than using a differencing-based approach.
 * 
 * Also has routines for dealing with a date timestamp - i.e. it can detect a
 * monthly time period (because months are different lengths) and maps date time
 * stamps to equal spaced time intervals. For example, in general, a date time
 * stamp is remapped by subtracting the first observed value and adding this
 * value divided by the constant delta (difference between consecutive steps) to
 * the result. In the case of a detected monthly time period, the remapping
 * involves subtracting the base year and then adding to this the number of the
 * month within the current year plus twelve times the number of intervening
 * years since the base year.
 * 
 * Also has routines for adding new attributes derived from a date time stamp to
 * the data - e.g. AM indicator, day of the week, month, quarter etc. In the
 * case where there is no real data time stamp, the user may specify a nominal
 * periodic variable (if one exists in the data). For example, month might be
 * coded as a nominal value. In this case it can be specified as the primary
 * periodic variable. The point is, that in all these cases (nominal periodic
 * and date-derived periodics), we are able to determine what the value of these
 * variables will be in future instances (as computed from the last known
 * historic instance).
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 51180 $
 */
public class TSLagMaker implements Serializable {

  /** For serialization */
  private static final long serialVersionUID = -1697901820770907975L;

  /** The names of the fields to create lagged variables for */
  protected List<String> m_fieldsToLag = null;

  /**
   * The names of the fields to be considered "overlay" data - i.e. we will be
   * supplied with values for these for future instances.
   */
  protected List<String> m_overlayFields = null;

  /** The minimum lag */
  protected int m_minLag = 1;

  /** The maximum lag */
  protected int m_maxLag = 12;

  /**
   * Optionally fine tune the selection of lagged attributes within the min and
   * max via a range string.
   */
  protected String m_lagFineTune = "";

  /**
   * Whether to replace a number of consecutive long lagged variables with their
   * average.
   */
  protected boolean m_averageConsecutiveLongLags = false;

  /**
   * If replacing long lagged variables with averages, do so for those long
   * lagged variables with lag greater than this
   */
  protected int m_averageLagsAfter = 2;

  /**
   * How many consecutive lagged variables to average, if averaging long lagged
   * variables
   */
  protected int m_numConsecutiveToAverage = 2;

  /** The name of the timestamp attribute (if there is one) */
  protected String m_timeStampName = "";

  /**
   * Whether to adjust for trends. If a timestamp attribute is named then
   * adjusting for trends will occur. If there is no timestamp attribute in the
   * data, then turning this on will result in an artificial timestamp attribute
   * getting added to the data.
   */
  protected boolean m_adjustForTrends = true;

  /**
   * Whether to stabilize the variance in the field to be forecast by applying a
   * log transform
   */
  protected boolean m_adjustForVariance = false;

  /** True if an artificial time index has been added to the data */
  protected boolean m_useArtificialTimeIndex = false;

  /** Include time/lag interaction terms? */
  protected boolean m_includeTimeLagCrossProducts = true;

  /** artificial time and last known real time value */
  protected double m_lastTimeValue = -1;

  /**
   * Used to add an artificial time attribute to the data if the user has
   * selected to adjust for trends and there isn't a time stamp in the data
   */
  protected AddID m_artificialTimeMaker;

  /** Filters for creating the various lagged and derived attributes */
  protected List<Filter> m_varianceAdjusters;
  protected List<Filter> m_lagMakers;
  protected List<Filter> m_averagedLagMakers;
  protected List<Filter> m_timeIndexMakers;
  protected List<Filter> m_timeLagCrossProductMakers;
  protected Remove m_extraneousAttributeRemover;

  /** The name of the primary periodic attribute */
  protected String m_primaryPeriodicName = "";

  /**
   * Holds a map of primary periodic values as keys and their immediate
   * successors (chronologically) as values. The primary periodic attribute (if
   * available) should relate to the time interval of the instances (e.g.
   * hourly, daily, monthly etc.).
   */
  protected Map<String, String> m_primaryPeriodicSequence;

  /**
   * A map (keyed by attribute) of maps for looking up the values of secondary
   * periodic attribute values that correspond to the values of the primary
   * periodic attribute
   */
  protected Map<Attribute, Map<String, String>> m_secondaryPeriodicLookups;

  protected Instances m_originalHeader;

  /**
   * This holds the most recent (time wise) training or primed instance. We can
   * use it to determine the t+1 periodic value for the primary periodic
   * attribute
   */
  protected Instance m_lastHistoricInstance;

  /** pre-defined fields that can be derived from a genuine date time stamp */
  protected boolean m_am = false;
  protected boolean m_dayOfWeek = false;
  protected boolean m_weekend = false;
  protected boolean m_monthOfYear = false;
  protected boolean m_quarter = false;
  protected boolean m_dayOfMonth = false;
  protected boolean m_numDaysInMonth = false;

  /** custom defined fields that can be derived from a genuine date time stamp */
  protected Map<String, ArrayList<CustomPeriodicTest>> m_customPeriodics;

  protected List<Filter> m_derivedPeriodicMakers;
  // protected boolean m_advanceTimeStampByMonth = false;
  protected PeriodicityHandler m_dateBasedPeriodicity = new PeriodicityHandler();
  protected Periodicity m_userHintPeriodicity = Periodicity.UNKNOWN;

  /**
   * Delete instances from the start of the transformed series where lagged
   * variables are missing? Default leaves missing value handling to the base
   * learner.
   */
  protected boolean m_deleteMissingFromStartOfSeries = false;

  /** Stores the first time stamp value in the data */
  protected long m_dateTimeStampBase;
  protected Add m_addDateMap;

  /**
   * Holds the difference between the time stamps for the two most recent
   * training instances or the average difference over consecutive training
   * instances if the differences are not constant. Either this or date
   * arithmetic (to advance time stamp by month) is used to advance the
   * timestamp for future instances.
   */
  // protected double m_deltaTime = -1;

  /**
   * Date time stamps that should be skipped - i.e. not considered as an
   * increment. E.g financial markets don't trade on the weekend, so the
   * difference between friday closing and the following monday closing is one
   * time unit (and not three). Can accept strings such as "sat", "sunday",
   * "jan", "august", or explicit dates (with optional formatting string) such
   * as "2011-07-04@yyyy-MM-dd", or integers. Integers are interpreted with
   * respect to the periodicity - e.g for daily data they are interpreted as day
   * of the year; for hourly data, hour of the day; weekly data, week of the
   * year.
   */
  protected String m_skipEntries;

  /** Default formatting string for explicit dates in the skip list */
  protected String m_dateFormat = "yyyy-MM-dd'T'HH:mm:ss";

  /**
   * Reset the lag maker.
   */
  public void reset() {
    m_artificialTimeMaker = null;
    m_varianceAdjusters = null;
    m_lagMakers = null;
    m_averagedLagMakers = null;
    m_timeIndexMakers = null;
    m_timeLagCrossProductMakers = null;
    m_derivedPeriodicMakers = null;
    m_extraneousAttributeRemover = null;
    m_lastTimeValue = -1;
    // m_deltaTime = -1;
    // m_dateBasedPeriodicity = Periodicity.UNKNOWN;
    // m_skipEntries = null;
    // m_dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
  }

  /**
   * Returns an enumeration describing the available options.
   * 
   * @return an enumeration of all the available options.
   */
  public Enumeration<Option> listOptions() {
    Vector<Option> newVector = new Vector<Option>();

    newVector.add(new Option("\tSet the fields to lag.", "F", 1,
        "-F <comma separated list of names>"));
    newVector.add(new Option("\tSet the fields to be considered "
        + "as overlay data.", "overlay", 1,
        "-overlay <comma separated list of names>"));

    newVector.add(new Option("\tSet the minimum lag length to generate."
        + "\n\t(default = 1)", "L", 1, "-L <num>"));
    newVector.add(new Option("\tSet the maximum lag length to generate."
        + "\n\t(default = 12)", "M", 1, "-M <num>"));
    newVector.add(new Option("\tAverage consecutive long lags.", "A", 0, "-A"));
    newVector.add(new Option("\tAverage those lags longer than this number of"
        + "time steps.\n\tUse in conjuction with -A is selected.\n\t"
        + "(default = 2)", "B", 1, "-B <num>"));
    newVector.add(new Option("\tFine tune selection of lags within min and "
        + "max by specifying" + " ranges", "R", 1, "-R <ranges>"));
    newVector.add(new Option("\tAverage this many consecutive long lags.\n\t"
        + "Use in conjuction with -B (default = 2)", "C", 1, "-C <num>"));
    newVector.add(new Option("\tDon't adjust for trends.", "Z", 0, "-Z"));
    newVector.add(new Option("\tSpecify the name of the timestamp field", "G",
        1, "-G <timestamp name>"));
    newVector.add(new Option("\tAdjust for variance.", "V", 0, "-V"));
    newVector.add(new Option(
        "\tAdd an AM/PM indicator (requires a date timestamp)", "am-pm", 0,
        "-am-pm"));
    newVector.add(new Option("\tAdd a day of the week field (requres a date"
        + " timestamp)", "dayofweek", 0, "-dayofweek"));
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
        + "Test=OPERATORyear:month:week-of-yr:week-of-month:"
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
   * Creates a Range object for the user-specified lag range String
   * 
   * @param lagRange a range as a String
   * @return a Range object
   * @throws Exception if the supplied range is illegal with respect to the min
   *           and max lag values.
   */
  protected Range getLagRangeSelection(String lagRange) throws Exception {
    Range r = new Range(lagRange);
    try {
      r.setUpper(m_maxLag);
    } catch (IllegalArgumentException e) {
      throw new Exception("The lag selection range '" + lagRange + "' is"
          + "illegal with respect to the specified min and max" + "lags.");
    }

    // still need to check against the min
    int[] selectedIndexes = r.getSelection();
    int max = selectedIndexes[Utils.maxIndex(selectedIndexes)] + 1;
    int min = selectedIndexes[Utils.minIndex(selectedIndexes)] + 1;
    if (max < m_minLag || min > m_maxLag) {
      throw new Exception("The lag selection range '" + lagRange + "' is"
          + "illegal with respect to the specified min and max" + "lags.");
    }

    return r;
  }

  /**
   * Parses a given list of options.
   * 
   * @param options the list of options as an array of strings
   * @exception Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
    String fieldsToLag = Utils.getOption('F', options);
    if (fieldsToLag.length() == 0) {
      throw new Exception("Must specify the name of at least one field "
          + "to create lags for!");
    }
    String[] fieldNames = fieldsToLag.split(",");
    List<String> fieldList = new ArrayList<String>();
    for (String f : fieldNames) {
      fieldList.add(f);
    }
    setFieldsToLag(fieldList);

    String overlayFields = Utils.getOption("overlay", options);
    if (overlayFields.length() > 0) {
      String[] names = overlayFields.split(",");
      List<String> nameList = new ArrayList<String>();
      for (String f : names) {
        nameList.add(f);
      }

      setOverlayFields(nameList);
    }

    String minL = Utils.getOption('L', options);
    if (minL.length() > 0) {
      int mL = Integer.parseInt(minL);
      setMinLag(mL);
      if (mL < 1) {
        throw new Exception("Minimum lag can't be less than 1!");
      }
    }

    String maxL = Utils.getOption('M', options);
    if (maxL.length() > 0) {
      int mL = Integer.parseInt(maxL);
      setMaxLag(mL);
    }

    if (getMaxLag() < getMinLag()) {
      throw new Exception("Can't have the maximum lag set lower than the "
          + "minimum lag!");
    }

    String lagRange = Utils.getOption('R', options);
    m_lagFineTune = lagRange;
    if (m_lagFineTune.length() > 0) {

      // check the range for consistency with respect to min and max
      getLagRangeSelection(lagRange);
    }

    boolean avLongLags = Utils.getFlag('A', options);
    setAverageConsecutiveLongLags(avLongLags);

    String avLongerThan = Utils.getOption('B', options);
    if (avLongerThan.length() > 0) {
      int avL = Integer.parseInt(avLongerThan);
      if (avL < getMinLag() || avL > getMaxLag()) {
        throw new Exception("Average consecutive long lags value can't "
            + "be less than the minimum lag or greater than the "
            + "maximum lag!");
      }
      setAverageLagsAfter(avL);
    }

    String consecutiveLongLagS = Utils.getOption('C', options);
    if (consecutiveLongLagS.length() > 0) {
      int consecutive = Integer.parseInt(consecutiveLongLagS);
      if (consecutive < 1 || consecutive > (getMaxLag() - getMinLag())) {
        throw new Exception("Number of consecutive long lags to average "
            + "must be greater than 0 and less than "
            + (getMaxLag() - getMinLag()));
      }
      setNumConsecutiveLongLagsToAverage(consecutive);
    }

    boolean dontAdjTrends = Utils.getFlag('Z', options);
    setAdjustForTrends(!dontAdjTrends);

    boolean adjVariance = Utils.getFlag("V", options);
    setAdjustForVariance(adjVariance);

    String timeStampF = Utils.getOption('G', options);
    if (timeStampF.length() > 0) {
      setTimeStampField(timeStampF);
    }

    setAddAMIndicator(Utils.getFlag("am-pm", options));
    setAddDayOfWeek(Utils.getFlag("dayofweek", options));
    setAddDayOfMonth(Utils.getFlag("dayofmonth", options));
    setAddNumDaysInMonth(Utils.getFlag("numdaysinmonth", options));
    setAddWeekendIndicator(Utils.getFlag("weekend", options));
    setAddMonthOfYear(Utils.getFlag("month", options));
    setAddQuarterOfYear(Utils.getFlag("quarter", options));

    // custom date-derived periodic fields
    String customPeriodic = Utils.getOption("custom", options);
    while (customPeriodic.length() > 0) {
      addCustomPeriodic(customPeriodic);
    }

    String primaryPeriodicN = Utils.getOption("periodic", options);
    if (primaryPeriodicN.length() > 0) {
      setPrimaryPeriodicFieldName(primaryPeriodicN);
    }

    String skipString = Utils.getOption("skip", options);
    if (skipString.length() > 0) {
      setSkipEntries(skipString);
    }
  }

  /**
   * Gets the current settings of the LagMaker.
   * 
   * @return an array of strings suitable for passing to setOptions
   */
  public String[] getOptions() {
    ArrayList<String> options = new ArrayList<String>();

    List<String> fieldsToLag = getFieldsToLag();
    options.add("-F");
    options.add(fieldsToLag.toString());

    if (getOverlayFields() != null && getOverlayFields().size() > 0) {
      options.add("-O");
      options.add(getOverlayFields().toString());
    }

    options.add("-L");
    options.add("" + getMinLag());
    options.add("-M");
    options.add("" + getMaxLag());

    if (m_lagFineTune.length() > 0) {
      options.add("-R");
      options.add(getLagRange());
    }

    if (getAverageConsecutiveLongLags()) {
      options.add("-A");

      options.add("-B");
      options.add("" + getAverageLagsAfter());
      options.add("-C");
      options.add("" + getNumConsecutiveLongLagsToAverage());
    }

    if (!getAdjustForTrends()) {
      options.add("-Z");
    }

    if (getAdjustForVariance()) {
      options.add("-V");
    }

    if (getTimeStampField() != null && getTimeStampField().length() > 0) {
      options.add("-G");
      options.add(getTimeStampField());
    }

    if (getAddAMIndicator()) {
      options.add("-am-pm");
    }

    if (getAddDayOfWeek()) {
      options.add("-dayofweek");
    }

    if (getAddDayOfMonth()) {
      options.add("-dayofmonth");
    }

    if (getAddNumDaysInMonth()) {
      options.add("-numdaysinmonth");
    }

    if (getAddWeekendIndicator()) {
      options.add("-weekend");
    }

    if (getAddMonthOfYear()) {
      options.add("-month");
    }

    if (getAddQuarterOfYear()) {
      options.add("-quarter");
    }

    if (getSkipEntries() != null && getSkipEntries().length() > 0) {
      options.add("-skip");
      options.add(getSkipEntries());
    }

    if (m_customPeriodics != null && m_customPeriodics.keySet().size() > 0) {
      for (String name : m_customPeriodics.keySet()) {
        List<CustomPeriodicTest> tests = m_customPeriodics.get(name);

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

    return options.toArray(new String[1]);
  }

  /**
   * Get the date-derived custom periodic attributes in use.
   * 
   * @return a Map, keyed by field name, of custom date-derived periodic fields.
   */
  public Map<String, ArrayList<CustomPeriodicTest>> getCustomPeriodics() {
    return m_customPeriodics;
  }

  /**
   * Add a custom date-derived periodic
   * 
   * @param customPeriodic the new custom date-derived periodic in textual form.
   */
  public void addCustomPeriodic(String customPeriodic) {
    if (m_customPeriodics == null) {
      m_customPeriodics = new HashMap<String, ArrayList<CustomPeriodicTest>>();
    }

    ArrayList<CustomPeriodicTest> tests = new ArrayList<CustomPeriodicTest>();
    int nameSplit = customPeriodic.indexOf('=');
    String fieldName = customPeriodic.substring(0, nameSplit);
    customPeriodic = customPeriodic.substring(nameSplit + 1,
        customPeriodic.length());
    String[] parts = customPeriodic.split("|");
    for (String p : parts) {
      CustomPeriodicTest c = new CustomPeriodicTest(p);
      tests.add(c);
    }
    m_customPeriodics.put(fieldName, tests);
  }

  /**
   * Clear all custom date-derived periodic fields.
   */
  public void clearCustomPeriodics() {
    m_customPeriodics = null;
  }

  /**
   * Set the date-derived custom periodic fields to use/compute
   * 
   * @param custom a Map, keyed by field name, of custom date-derived periodic
   *          fields to use.
   */
  public void setCustomPeriodics(
      Map<String, ArrayList<CustomPeriodicTest>> custom) {
    m_customPeriodics = custom;
  }

  /**
   * Set the names of the fields to create lagged variables for
   * 
   * @param names a List of field names for which to create lagged variables
   * @throws Exception if a problem occurs
   */
  public void setFieldsToLag(List<String> names) throws Exception {
    m_fieldsToLag = names;
  }

  /**
   * Get the names of the fields to create lagged variables for.
   * 
   * @return a List of field names for which lagged variables will be created.
   */
  public List<String> getFieldsToLag() {
    return m_fieldsToLag;
  }

  /**
   * Set the names of fields in the data that are to be considered "overlay"
   * fields - i.e. they will be externally provided for future instances.
   * 
   * @param overlayNames the names of the fields that are to be considered
   *          "overlay" fields
   */
  public void setOverlayFields(List<String> overlayNames) {
    m_overlayFields = overlayNames;
  }

  /**
   * Get overlay fields
   * 
   * @return a list of field names that are set as "overlay" fields
   */
  public List<String> getOverlayFields() {
    return m_overlayFields;
  }

  /**
   * Set the name of the time stamp field in the data
   * 
   * @param name the name of the time stamp field
   */
  public void setTimeStampField(String name) {
    m_timeStampName = name;
    /*
     * if (name == null || name.length() == 0) { m_useArtificialTimeIndex =
     * false; } else { m_useArtificialTimeIndex = true; }
     */
  }

  /**
   * Get the name of the time stamp field.
   * 
   * @return the name of the time stamp field or null if one hasn't been
   *         specified.
   */
  public String getTimeStampField() {
    return m_timeStampName;
  }

  /**
   * Set whether to adjust for trends or not. If there is no time stamp field
   * specified, and this is set to true, then an artificial time stamp will be
   * created.
   * 
   * @param a true if we are to adjust for trends via a real or artificial time
   *          stamp
   */
  public void setAdjustForTrends(boolean a) {
    m_adjustForTrends = a;
  }

  /**
   * Returns true if we are adjusting for trends via a real or artificial time
   * stamp.
   * 
   * @return true if we are adjusting for trends via a real or artificial time
   *         stamp in the data.
   */
  public boolean getAdjustForTrends() {
    return m_adjustForTrends;
  }

  /**
   * Set whether to adjust for variance in the data by taking the log of the
   * target(s).
   * 
   * @param v true to adjust for variance by taking the log of the target(s).
   */
  public void setAdjustForVariance(boolean v) {
    m_adjustForVariance = v;
  }

  /**
   * Returns true if we are adjusting for variance by taking the log of the
   * target(s).
   * 
   * @return true if we are adjusting for variance.
   */
  public boolean getAdjustForVariance() {
    return m_adjustForVariance;
  }

  /**
   * Set ranges by which to fine-tune the creation of lagged attributes.
   * 
   * @param ranges a list of ranges as a string
   */
  public void setFineTuneLags(String ranges) {
    m_lagFineTune = ranges;
  }

  /**
   * Get the ranges used to fine tune the creation of lagged attributes.
   * 
   * @return the ranges as a string
   */
  public String getFineTuneLags() {
    return m_lagFineTune;
  }

  /**
   * Set the minimum lag to create (default = 1, i.e. t-1).
   * 
   * @param min the minimum lag to create
   */
  public void setMinLag(int min) {
    m_minLag = min;
  }

  /**
   * Get the minimum lag to create.
   * 
   * @return the minimum lag to create.
   */
  public int getMinLag() {
    return m_minLag;
  }

  /**
   * Set the maximum lag to create (default = 12, i.e. t-12).
   * 
   * @param max the maximum lag to create.
   */
  public void setMaxLag(int max) {
    m_maxLag = max;
  }

  /**
   * Get the maximum lag to create.
   * 
   * @return the maximum lag to create.
   */
  public int getMaxLag() {
    return m_maxLag;
  }

  /**
   * Set ranges to fine tune lag selection.
   * 
   * @param lagRange a set of ranges (e.g. 2,3,4,7-9).
   */
  public void setLagRange(String lagRange) {
    m_lagFineTune = lagRange;
  }

  /**
   * Get the ranges used to fine tune lag selection
   * 
   * @return the ranges (if any) used to fine tune lag selection
   */
  public String getLagRange() {
    return m_lagFineTune;
  }

  /**
   * Sets whether to average consecutive long lagged variables. Setting this to
   * true creates new variables that are averages of long lags and the original
   * lagged variables involved are removed.
   * 
   * @param avg true if consecutive long lags are to be averaged.
   */
  public void setAverageConsecutiveLongLags(boolean avg) {
    m_averageConsecutiveLongLags = avg;
  }

  /**
   * Returns true if consecutive long lagged variables are to be averaged.
   * 
   * @return true if consecutive long lagged variables are to be averaged.
   */
  public boolean getAverageConsecutiveLongLags() {
    return m_averageConsecutiveLongLags;
  }

  /**
   * Set at which point consecutive long lagged variables are to be averaged
   * (default = 2, i.e. start replacing lagged variables after t-2 with
   * averages).
   * 
   * @param a the point at which to start averaging consecutive long lagged
   *          variables.
   */
  public void setAverageLagsAfter(int a) {
    m_averageLagsAfter = a;
  }

  /**
   * Return the point after which long lagged variables will be averaged.
   * 
   * @return the point after which long lagged variables will be averaged.
   */
  public int getAverageLagsAfter() {
    return m_averageLagsAfter;
  }

  /**
   * Set the number of long lagged variables to average for each averaged
   * variable created (default = 2, e.g. a set average after value of 2 and a
   * num consecutive to average = 2 will average t-3 and t-4 into a new
   * variable, t-5 and t-6 into a new variable ect.
   * 
   * @param c the number of consecutive long lagged variables to average.
   */
  public void setNumConsecutiveLongLagsToAverage(int c) {
    m_numConsecutiveToAverage = c;
  }

  /**
   * Get the number of consecutive long lagged variables to average.
   * 
   * @return the number of long lagged variables to average.
   */
  public int getNumConsecutiveLongLagsToAverage() {
    return m_numConsecutiveToAverage;
  }

  /**
   * Set the name of a periodic attribute in the data. This attribute has to be
   * nominal and cyclic so that it is possible to know what the value will be
   * given the current one.
   * 
   * @param p the name of the primary periodic attribute (if any) in the data.
   */
  public void setPrimaryPeriodicFieldName(String p) {
    m_primaryPeriodicName = p;
  }

  /**
   * The name of the primary periodic attribute or null if one hasn't been
   * specified.
   * 
   * @return the name of the primary periodic attribute or null if one hasn't
   *         been specified.
   */
  public String getPrimaryPeriodicFieldName() {
    return m_primaryPeriodicName;
  }

  /**
   * Set whether to create an AM indicator attribute. Has no effect if there
   * isn't a date-based time stamp in the data.
   * 
   * @param am true if an AM indicator attribute is to be created.
   */
  public void setAddAMIndicator(boolean am) {
    m_am = am;
  }

  /**
   * Return true if an AM indicator attribute is to be created.
   * 
   * @return true if an AM indiciator attribute is to be created.
   */
  public boolean getAddAMIndicator() {
    return m_am;
  }

  /**
   * Set whether to create a day of the week attribute. Has no effect if there
   * isn't a date-based time stamp in the data.
   * 
   * @param d true if a day of the week attribute is to be created.
   */
  public void setAddDayOfWeek(boolean d) {
    m_dayOfWeek = d;
  }

  /**
   * Return true if a day of the week attribute is to be created.
   * 
   * @return true if a day of the week attribute is to be created.
   */
  public boolean getAddDayOfWeek() {
    return m_dayOfWeek;
  }

  /**
   * Set whether to create a day of the month attribute. Has no effect if there
   * isn't a date-based time stamp in the data.
   * 
   * @param d true if a day of the month attribute is to be created.
   */
  public void setAddDayOfMonth(boolean d) {
    m_dayOfMonth = d;
  }

  /**
   * Return true if a day of the month attribute is to be created.
   * 
   * @return true if a day of the month attribute is to be created.
   */
  public boolean getAddDayOfMonth() {
    return m_dayOfMonth;
  }

  /**
   * Set whether to create a numeric attribute that holds the number of days in
   * the month.
   * 
   * @param d true if a num days in month attribute is to be created.
   */
  public void setAddNumDaysInMonth(boolean d) {
    m_numDaysInMonth = d;
  }

  /**
   * Return true if a num days in the month attribute is to be created.
   * 
   * @return true if a num days in the month attribute is to be created.
   */
  public boolean getAddNumDaysInMonth() {
    return m_numDaysInMonth;
  }

  /**
   * Set whether to create a weekend indicator attribute. Has no effect if there
   * isn't a date-based time stamp in the data.
   * 
   * @param w true if a weekend indicator attribute is to be created.
   */
  public void setAddWeekendIndicator(boolean w) {
    m_weekend = w;
  }

  /**
   * Returns true if a weekend indicator attribute is to be created.
   * 
   * @return true if a weekend indicator attribute is to be created.
   */
  public boolean getAddWeekendIndicator() {
    return m_weekend;
  }

  /**
   * Set whether to create a month of the year attribute. Has no effect if there
   * isn't a date-based time stamp in the data.
   * 
   * @param m true if a month of the year attribute is to be created.
   */
  public void setAddMonthOfYear(boolean m) {
    m_monthOfYear = m;
  }

  /**
   * Returns true if a month of the year attribute is to be created.
   * 
   * @return true if a month of the year attribute is to be created.
   */
  public boolean getAddMonthOfYear() {
    return m_monthOfYear;
  }

  /**
   * Set whether to create a quarter attribute. Has no effect if there isn't a
   * date-based time stamp in the data.
   * 
   * @param q true if a quarter attribute is to be added.
   */
  public void setAddQuarterOfYear(boolean q) {
    m_quarter = q;
  }

  /**
   * Returns true if a quarter attribute is to be created.
   * 
   * @return true if a quarter attribute is to be created.
   */
  public boolean getAddQuarterOfYear() {
    return m_quarter;
  }

  /**
   * Returns true if an artificial time index is in use.
   * 
   * @return true if an artificial time index is in use.
   */
  public boolean isUsingAnArtificialTimeIndex() {
    return m_useArtificialTimeIndex;
  }

  /**
   * Set the starting value for the artificial time stamp.
   * 
   * @param value the value to initialize the artificial time stamp with.
   * @throws Exception if an artificial time stamp is not being used.
   */
  public void setArtificialTimeStartValue(double value) throws Exception {
    if (isUsingAnArtificialTimeIndex()) {
      m_lastTimeValue = value;
    } else {
      throw new Exception("Not using an artificial time index");
    }
  }

  /**
   * Returns the current value of the artificial time stamp. After training,
   * after priming, and prior to forecasting, this will be equal to the number
   * of training instances seen.
   * 
   * @return the current value of the artificial time stamp.
   * @throws Exception if an artificial time stamp is not being used.
   */
  public double getArtificialTimeStartValue() throws Exception {
    if (!isUsingAnArtificialTimeIndex()) {
      throw new Exception("Not using an artificial time index!");
    }

    return m_lastTimeValue;
  }

  /**
   * Returns the current (i.e. most recent) time stamp value. Unlike an
   * artificial time stamp, the value after training, after priming and before
   * forecasting, will be equal to the time stamp of the most recent priming
   * instance.
   * 
   * @return the current time stamp value
   * @throws Exception if the lag maker is not adjusting for trends or no time
   *           stamp attribute has been specified.
   */
  public double getCurrentTimeStampValue() throws Exception {
    if (m_adjustForTrends && m_timeStampName.length() > 0) {
      return m_lastTimeValue;
    }

    throw new Exception("Not using a time stamp!");
  }

  /**
   * Increment the artificial time value with the supplied incrememt value.
   * 
   * @param increment the value to increment by.
   */
  public void incrementArtificialTimeValue(int increment) {
    m_lastTimeValue += increment;
  }

  /**
   * Return the difference between time values. This may be only approximate for
   * periods based on dates. It is best to used date-based arithmetic in this
   * case for incrementing/decrementing time stamps.
   * 
   * @return the (average) difference between time values.
   */
  public double getDeltaTime() {
    return m_dateBasedPeriodicity.deltaTime(); // m_deltaTime;
  }

  /**
   * Gets the Periodicity representing the time stamp in use for this lag maker.
   * If the lag maker is not adjusting for trends, or an artificial time stamp
   * is being used, then null is returned.
   * 
   * @return the Periodicity in use, or null if the lag maker is not adjusting
   *         for trends or is using an artificial time stamp.
   */
  public Periodicity getPeriodicity() {
    if (!m_adjustForTrends || m_useArtificialTimeIndex) {
      return null;
    }

    return m_dateBasedPeriodicity.getPeriodicity();
  }

  /**
   * Set the periodicity for the data. This is ignored if the lag maker is not
   * adjusting for trends or is using an artificial time stamp. If not specified
   * or set to Periodicity.UNKNOWN (the default) then heuristics will be used to
   * try and automatically determine the periodicity.
   * 
   * @param toUse the periodicity to use
   */
  public void setPeriodicity(Periodicity toUse) {
    m_userHintPeriodicity = toUse;
  }

  /**
   * Set the list of time units to be 'skipped' - i.e. not considered as an
   * increment. E.g financial markets don't trade on the weekend, so the
   * difference between friday closing and the following monday closing is one
   * time unit (and not three). Can accept strings such as "sat", "sunday",
   * "jan", "august", or explicit dates (with optional formatting string) such
   * as "2011-07-04@yyyy-MM-dd", or integers. Integers are interpreted with
   * respect to the periodicity - e.g for daily data they are interpreted as day
   * of the year; for hourly data, hour of the day; weekly data, week of the
   * year.
   * 
   * @param skipEntries a comma separated list of strings, explicit dates and
   *          integers.
   */
  public void setSkipEntries(String skipEntries) {
    m_skipEntries = skipEntries;
  }

  /**
   * Get a list of time units to be 'skipped' - i.e. not considered as an
   * increment. E.g financial markets don't trade on the weekend, so the
   * difference between friday closing and the following monday closing is one
   * time unit (and not three). Can accept strings such as "sat", "sunday",
   * "jan", "august", or explicit dates (with optional formatting string) such
   * as "2011-07-04@yyyy-MM-dd", or integers. Integers are interpreted with
   * respect to the periodicity - e.g for daily data they are interpreted as day
   * of the year; for hourly data, hour of the day; weekly data, week of the
   * year.
   * 
   * @return a comma-separated list of strings, explicit dates and integers
   */
  public String getSkipEntries() {
    return m_skipEntries;
  }

  private List<Object> createLagFiller(Instances insts, String targetName)
      throws Exception {
    // Classifier lagFiller = new weka.classifiers.functions.LeastMedSq();
    Classifier lagFiller = new weka.classifiers.functions.LinearRegression();

    ArrayList<Attribute> atts = new ArrayList<Attribute>();
    atts.add(new Attribute("time"));
    atts.add(new Attribute("target"));
    Instances simple = new Instances("simple", atts, insts.numInstances());
    int targetIndex = insts.attribute(targetName).index();
    for (int i = 0; i < insts.numInstances(); i++) {
      double targetValue = insts.instance(i).value(targetIndex);
      double time = i;
      double[] vals = new double[2];
      vals[0] = time;
      vals[1] = targetValue;
      DenseInstance d = new DenseInstance(1.0, vals);
      simple.add(d);
    }

    simple.setClassIndex(1);
    lagFiller.buildClassifier(simple);
    System.err.println(lagFiller);
    simple = new Instances(simple, 0);

    List<Object> results = new ArrayList<Object>();
    results.add(lagFiller);
    results.add(simple);

    return results;
  }

  private Instances createLags(Instances insts) throws Exception {
    if (m_fieldsToLag == null || m_fieldsToLag.get(0).length() == 0) {
      throw new Exception("Field to forecast is not specified!");
    }

    m_lagMakers = new ArrayList<Filter>();

    // do we have a fine tuning range for lags?
    Range r = null;
    int[] rangeIndexes = null;
    if (m_lagFineTune.length() > 0) {
      r = getLagRangeSelection(m_lagFineTune);
      rangeIndexes = r.getSelection();
    }

    for (int j = 0; j < m_fieldsToLag.size(); j++) {
      int classIndex = insts.attribute(m_fieldsToLag.get(j)).index();
      if (classIndex < 0) {
        throw new Exception("Can't find field '" + m_fieldsToLag.get(j) + "'!");
      }

      // ---------------------
      // List<Object> lagFillerHolder = createLagFiller(insts,
      // m_fieldsToLag.get(j));
      // Classifier missingLagFiller = (Classifier)lagFillerHolder.get(0);
      // Instances lagFillerHeader = (Instances)lagFillerHolder.get(1);
      // ---------------------

      for (int i = m_minLag; i <= m_maxLag; i++) {

        // check against fine tuning ranges if set
        if (rangeIndexes != null) {
          boolean ok = false;
          for (int z = 0; z < rangeIndexes.length; z++) {
            if (rangeIndexes[z] + 1 == i) {
              ok = true;
              break;
            }
          }

          if (!ok) {
            continue;
          }
        }

        Copy c = new Copy();
        c.setAttributeIndices("" + (classIndex + 1));
        c.setInputFormat(insts);
        insts = Filter.useFilter(insts, c);
        m_lagMakers.add(c);
        RenameAttribute rename = new RenameAttribute();
        rename.setAttributeIndices("last");
        rename.setReplace("Lag_" + m_fieldsToLag.get(j));
        rename.setInputFormat(insts);
        insts = Filter.useFilter(insts, rename);
        m_lagMakers.add(rename);

        // AddExpression is convenient to make a copy and set a new name
        /*
         * AddExpression addE = new AddExpression(); addE.setName("Lag_" +
         * m_fieldsToLag.get(j)); addE.setExpression("a" + (classIndex + 1) +
         * "*1"); addE.setInputFormat(insts); insts = Filter.useFilter(insts,
         * addE); m_lagMakers.add(addE);
         */

        // now time shift it
        TimeSeriesTranslate timeS = new TimeSeriesTranslate();
        timeS.setAttributeIndices("last");
        timeS.setInstanceRange(-i);
        timeS.setInputFormat(insts);
        insts = Filter.useFilter(insts, timeS);
        m_lagMakers.add(timeS);

        // --------------
        // now use the missingLagFiller to project back and fill in
        // the unknown values for lag elements before the beginning
        // of the series. Our artificial time begins at the start of
        // the series at 0.
        /*
         * int count = 0; int lagIndex = insts.numAttributes() - 1; for (int z =
         * -i; z < 0; z++) { double time = z; double[] vals = new double[2];
         * vals[0] = time; vals[1] = Utils.missingValue(); DenseInstance d = new
         * DenseInstance(1.0, vals); d.setDataset(lagFillerHeader); double
         * predictedTarget = missingLagFiller.classifyInstance(d); if
         * (insts.instance(count).isMissing(lagIndex)) {
         * insts.instance(count).setValue(lagIndex, predictedTarget); } else {
         * System
         * .err.println("***** lag value is not missing!! (project missing lags)"
         * ); } count++; }
         */

        // --------------
      }
    }
    // System.err.println(insts);
    return insts;
  }

  private Instances createAveragedLags(Instances insts) throws Exception {

    if (!m_averageConsecutiveLongLags) {
      m_averagedLagMakers = null;
      return insts;
    }

    if (m_numConsecutiveToAverage > getMaxLag() - getAverageLagsAfter()) {
      if (getMaxLag() - getAverageLagsAfter() > 1) {
        m_numConsecutiveToAverage = getMaxLag() - getAverageLagsAfter();
      } else {
        m_averagedLagMakers = null;
        return insts;
      }
    }

    m_averagedLagMakers = new ArrayList<Filter>();
    int numAtts = insts.numAttributes();

    String removeLongLagIndexes = "";
    for (int z = 0; z < m_fieldsToLag.size(); z++) {
      int firstLagIndex = -1;
      // locate the first lagged attribute
      for (int i = 0; i < insts.numAttributes(); i++) {
        if (insts.attribute(i).name().startsWith("Lag_" + m_fieldsToLag.get(z))) {
          firstLagIndex = i;
          break;
        }
      }

      if (firstLagIndex < 0) {
        throw new Exception("Can't find the first lag attribute for "
            + m_fieldsToLag.get(z) + "!");
      }

      for (int i = firstLagIndex; i < numAtts;) {
        if (!insts.attribute(i).name()
            .startsWith("Lag_" + m_fieldsToLag.get(z))) {
          // finished
          break;
        }

        // need to parse the lag number out of the name
        String lagNumS = insts.attribute(i).name()
            .replace("Lag_" + m_fieldsToLag.get(z) + "-", "");
        int lagNum = Integer.parseInt(lagNumS);
        int lastLagNum = lagNum;

        if (/* (i - firstLagIndex + 1) */lagNum > m_averageLagsAfter) {
          int attNumber = i + 1;
          removeLongLagIndexes += (i + 1) + ",";
          String avExpression = "(a" + attNumber;
          String avAttName = "Avg(" + insts.attribute(i).name();
          int denom = 1;
          // build the expression
          for (int j = 1; j < m_numConsecutiveToAverage; j++) {
            if ((i + j) < insts.numAttributes()
                && insts.attribute(i + j).name()
                    .startsWith("Lag_" + m_fieldsToLag.get(z))) {
              String currNumS = insts.attribute(i + j).name()
                  .replace("Lag_" + m_fieldsToLag.get(z) + "-", "");
              int currentLagNum = Integer.parseInt(currNumS);

              // only average consecutive long lags (so truncate
              // if there is a jump of more than 1
              if (currentLagNum - lastLagNum == 1) {
                avExpression += " + a" + (attNumber + j);
                avAttName += "," + insts.attribute(i + j).name();
                denom++;
                removeLongLagIndexes += (i + j + 1) + ",";
                lastLagNum = currentLagNum;
              } else {
                break;
              }
            } else {
              break;
            }
          }

          avExpression += ")/" + denom;
          avAttName += ")";
          AddExpression addE = new AddExpression();
          addE.setName(avAttName);
          addE.setExpression(avExpression);
          addE.setInputFormat(insts);
          insts = Filter.useFilter(insts, addE);
          m_averagedLagMakers.add(addE);

          i += denom;
        } else {
          i++;
        }
      }
    }

    if (removeLongLagIndexes.length() > 0) {
      removeLongLagIndexes = removeLongLagIndexes.substring(0,
          removeLongLagIndexes.lastIndexOf(','));
      Remove r = new Remove();
      r.setAttributeIndices(removeLongLagIndexes);
      r.setInputFormat(insts);
      insts = Filter.useFilter(insts, r);
      m_averagedLagMakers.add(r);
    }

    return insts;
  }

  private Instances createTimeIndexes(Instances insts) throws Exception {

    m_timeIndexMakers = null;
    if (m_timeStampName != null && m_timeStampName.length() > 0
        && m_adjustForTrends) {
      int timeStampIndex = insts.attribute(m_timeStampName).index();
      if (timeStampIndex < 0) {
        throw new Exception("Can't find time stamp attribute '"
            + m_timeStampName + "' in the data!");
      }
      String timeStampName = m_timeStampName;

      if (insts.attribute(timeStampIndex).isDate()) {
        // we'll use the remapped one
        timeStampIndex = insts.attribute(m_timeStampName + "-remapped").index();
        timeStampName += "-remapped";
      }

      if (!insts.attribute(timeStampIndex).isNumeric()) {
        throw new Exception("Time stamp attribute '" + m_timeStampName
            + "' is not numeric!");
      }

      /*
       * Instance first = insts.instance(insts.numInstances() - 1); Instance two
       * = insts.instance(insts.numInstances() - 2); m_deltaTime =
       * first.value(timeStampIndex) - two.value(timeStampIndex);
       */

      m_timeIndexMakers = new ArrayList<Filter>();
      AddExpression addE = new AddExpression();
      addE.setName(timeStampName + "^2");
      addE.setExpression("a" + (timeStampIndex + 1) + "^2");
      addE.setInputFormat(insts);
      insts = Filter.useFilter(insts, addE);
      m_timeIndexMakers.add(addE);

      addE = new AddExpression();
      addE.setName(timeStampName + "^3");
      addE.setExpression("a" + (timeStampIndex + 1) + "^3");
      addE.setInputFormat(insts);
      insts = Filter.useFilter(insts, addE);
      m_timeIndexMakers.add(addE);
    }

    return insts;
  }

  public Instances createTimeLagCrossProducts(Instances insts) throws Exception {
    m_timeLagCrossProductMakers = null;

    if (m_timeStampName == null || m_timeStampName.length() == 0
        || !m_adjustForTrends) {
      return insts;
    }

    int numAtts = insts.numAttributes();
    int firstLagIndex = -1;
    // locate the first lagged attribute
    for (int i = 0; i < numAtts; i++) {
      if (insts.attribute(i).name().startsWith("Lag_")) {
        firstLagIndex = i;
        break;
      }
    }

    if (firstLagIndex < 0) {
      m_timeLagCrossProductMakers = null;
      return insts;
    }

    int timeStampIndex = insts.attribute(m_timeStampName).index();
    if (timeStampIndex < 0) {
      return insts;
    }
    String timeStampName = m_timeStampName;

    if (insts.attribute(timeStampIndex).isDate()) {
      // use the remapped one
      timeStampIndex = insts.attribute(m_timeStampName + "-remapped").index();
      timeStampName += "-remapped";
    }

    m_timeLagCrossProductMakers = new ArrayList<Filter>();
    for (int i = firstLagIndex; i < insts.numAttributes(); i++) {
      if (!(insts.attribute(i).name().startsWith("Lag_") || insts.attribute(i)
          .name().startsWith("Avg("))) {
        break;
      }

      AddExpression addE = new AddExpression();
      addE.setName(timeStampName + "*" + insts.attribute(i).name());
      addE.setExpression("a" + (timeStampIndex + 1) + "*a" + (i + 1));
      addE.setInputFormat(insts);
      insts = Filter.useFilter(insts, addE);
      m_timeLagCrossProductMakers.add(addE);
    }

    return insts;
  }

  private Instances createVarianceAdjusters(Instances insts) throws Exception {
    if (!m_adjustForVariance) {
      return insts;
    }

    if (m_fieldsToLag == null || m_fieldsToLag.get(0).length() == 0) {
      throw new Exception("Fields to lag is not specified!");
    }

    m_varianceAdjusters = new ArrayList<Filter>();
    for (String field : m_fieldsToLag) {
      int index = insts.attribute(field).index();
      if (index < 0) {
        throw new Exception("Can't find field '" + field + "'!");
      }

      MathExpression mathE = new MathExpression();
      mathE.setIgnoreRange("" + (index + 1));
      mathE.setInvertSelection(true);
      mathE.setExpression("log(A)");
      mathE.setInputFormat(insts);
      insts = Filter.useFilter(insts, mathE);
      m_varianceAdjusters.add(mathE);
    }

    return insts;
  }

  // this is useful for reducing the scale of a date timestamp. Since dates
  // are stored internally in elapsed milliseconds, they are large numbers and
  // any model coefficient computed for the timestamp is likely to be extremely
  // small (appearing as 0 in output due to 4 decimal places precision).
  // Furthermore,
  // date timestamps with a periodicity of a month are not a constant number of
  // milliseconds in length from one month to the next - remapping corrects this
  protected Instances createDateTimestampRemap(Instances insts)
      throws Exception {
    Instances result = insts;

    if (m_adjustForTrends && !m_useArtificialTimeIndex
        && m_timeStampName != null && m_timeStampName.length() > 0) {
      if (result.attribute(m_timeStampName).isDate()) {
        int origIndex = result.attribute(m_timeStampName).index();

        // find first non-missing date and set as base
        GregorianCalendar c = new GregorianCalendar();
        for (int i = 0; i < result.numInstances(); i++) {
          if (!result.instance(i).isMissing(origIndex)) {
            if (m_dateBasedPeriodicity.getPeriodicity() == Periodicity.MONTHLY
                || m_dateBasedPeriodicity.getPeriodicity() == Periodicity.WEEKLY
                || m_dateBasedPeriodicity.getPeriodicity() == Periodicity.QUARTERLY) {
              Date d = new Date((long) result.instance(i).value(origIndex));
              c.setTime(d);
              m_dateTimeStampBase = c.get(Calendar.YEAR);
            } else {
              m_dateTimeStampBase = (long) result.instance(i).value(origIndex);
            }
            break;
          }
        }
        m_addDateMap = new Add();
        m_addDateMap.setAttributeName(m_timeStampName + "-remapped");
        m_addDateMap.setInputFormat(result);
        result = Filter.useFilter(result, m_addDateMap);

        Instance previous = result.instance(0);
        // now loop through and compute remapped date
        for (int i = 0; i < result.numInstances(); i++) {
          Instance current = result.instance(i);

          current = m_dateBasedPeriodicity.remapDateTimeStamp(current,
              previous, m_timeStampName);
          previous = current;
          /*
           * if (!current.isMissing(origIndex)) { if (m_dateBasedPeriodicity ==
           * Periodicity.MONTHLY || m_dateBasedPeriodicity == Periodicity.WEEKLY
           * || m_dateBasedPeriodicity == Periodicity.QUARTERLY) { Date d = new
           * Date((long)current.value(origIndex)); c.setTime(d); long year =
           * c.get(Calendar.YEAR); long month = c.get(Calendar.MONTH); long week
           * = c.get(Calendar.WEEK_OF_YEAR); long remapped = 0; if
           * (m_dateBasedPeriodicity == Periodicity.MONTHLY) { remapped = ((year
           * - m_dateTimeStampBase) * 12) + month; } else if
           * (m_dateBasedPeriodicity == Periodicity.WEEKLY) { remapped = ((year
           * - m_dateTimeStampBase) * 52) + week;
           * 
           * // adjust for the case where week 1 of the year actually starts //
           * in the last week of December if (month == Calendar.DECEMBER && week
           * == 1) { remapped += 52; } } else if (m_dateBasedPeriodicity ==
           * Periodicity.QUARTERLY) { remapped = ((year - m_dateTimeStampBase) *
           * 4) + ((month / 3L) + 1L); }
           * current.setValue(current.numAttributes() - 1, (double)remapped); }
           * else { double remapped = current.value(origIndex) -
           * m_dateTimeStampBase; remapped /=
           * m_dateBasedPeriodicity.deltaTime();//m_deltaTime;
           * current.setValue(current.numAttributes() - 1, remapped); } }
           */
        }
      }
    }

    return result;
  }

  protected Instance remapDateTimeStamp(Instance inst) throws Exception {
    Instance result = inst;

    if (m_addDateMap != null) {
      m_addDateMap.input(result);
      result = m_addDateMap.output();

      result = m_dateBasedPeriodicity.remapDateTimeStamp(result, null,
          m_timeStampName);

      /*
       * int origIndex = result.dataset().attribute(m_timeStampName).index();
       * Calendar c = new GregorianCalendar();
       * 
       * if (!result.isMissing(origIndex)) { if (m_dateBasedPeriodicity ==
       * Periodicity.MONTHLY || m_dateBasedPeriodicity == Periodicity.WEEKLY ||
       * m_dateBasedPeriodicity == Periodicity.QUARTERLY) { Date d = new
       * Date((long)result.value(origIndex)); c.setTime(d); long year =
       * c.get(Calendar.YEAR); long month = c.get(Calendar.MONTH); long week =
       * c.get(Calendar.WEEK_OF_YEAR); long remapped = 0; if
       * (m_dateBasedPeriodicity == Periodicity.MONTHLY) { remapped = ((year -
       * m_dateTimeStampBase) * 12) + month; } else if (m_dateBasedPeriodicity
       * == Periodicity.WEEKLY) { remapped = ((year - m_dateTimeStampBase) * 52)
       * + week;
       * 
       * // adjust for the case where week 1 of the year actually starts // in
       * the last week of December if (month == Calendar.DECEMBER && week == 1)
       * { remapped += 52; } } else if (m_dateBasedPeriodicity ==
       * Periodicity.QUARTERLY) { remapped = ((year - m_dateTimeStampBase) * 4)
       * + ((month / 3L) + 1L); } result.setValue(result.numAttributes() - 1,
       * (double)remapped); } else { double remapped = result.value(origIndex) -
       * m_dateTimeStampBase; remapped /=
       * m_dateBasedPeriodicity.deltaTime();//m_deltaTime;
       * result.setValue(result.numAttributes() - 1, remapped); } }
       */
    }

    return result;
  }

  /**
   * Enum defining periodicity
   */
  public static enum Periodicity {
    UNKNOWN, HOURLY, DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY;

    private double m_deltaTime;

    public double deltaTime() {
      return m_deltaTime;
    }

    public void setDeltaTime(double deltaTime) {
      m_deltaTime = deltaTime;
    }
  }

  /**
   * Helper class to manage time stamp manipulation with respect to various
   * periodicities. Has a routine to remap the time stamp, which is useful for
   * date time stamps. Since dates are just manipulated internally as the number
   * of milliseconds elapsed since the epoch, and any global trend modelling in
   * regression functions results in enormous coefficients for this variable -
   * remapping to a more reasonable scale prevents this. It also makes it easier
   * to handle the case where there are time periods that shouldn't be
   * considered as a time unit increment, e.g. weekends and public holidays for
   * financial trading data. These "holes" in the data can be accomodated by
   * accumulating a negative offset for the remapped date when a particular
   * data/time occurs in a user-specified "skip" list.
   * 
   * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
   */
  public static class PeriodicityHandler implements Serializable {

    /**
     * For serialization
     */
    private static final long serialVersionUID = 6330232772323425050L;

    /** Periodicity of this handler */
    protected Periodicity m_handlerPeriodicity = Periodicity.UNKNOWN;

    /** Delta time between consecutive units */
    private double m_deltaTime;

    /** True if we are managing a date-based periodicity */
    private boolean m_isDateBased;

    /** first date time stamp seen in batch training */
    private long m_dateTimeStampInitialVal;

    /** last date time stamp value seen in batch training */
    private long m_dateTimeStampFinalVal;

    /**
     * = year of initial time stamp val for weekly, monthly or quarterly
     * periodicies, otherwise is the same as initial time stamp val
     **/
    private long m_dateTimeStampBaseVal;

    /** holds the date-based entries that should be 'skipped' */
    private List<Object> m_skipList;

    /**
     * any adjustment for remapped date values accumulated via time unit skips
     * that occur during the training data time frame
     */
    private long m_trainingRemapSkipAdjust = 0;

    /**
     * Set periodicity to manage
     * 
     * @param p the periodicity to manage
     */
    public void setPeriodicity(Periodicity p) {
      m_handlerPeriodicity = p;
    }

    /**
     * Get periodicity being managed
     * 
     * @return the periodicity being managed
     */
    public Periodicity getPeriodicity() {
      return m_handlerPeriodicity;
    }

    /**
     * Set a list of skip entries
     * 
     * @param aList a comma separated list of date-based entries. May include
     *          strings such as 'sat' or 'june', specific dates (with optional
     *          format string) such as '2011-08-22@yyyy-MM-dd' or integers
     *          (which get interpreted differently depending on the periodicity)
     * 
     * @param dateFormat a default date format to use for parsing dates
     * @throws Exception if an entry in the list is unparsable or unrecognized
     */
    public void setSkipList(String aList, String dateFormat) throws Exception {
      if (aList != null && aList.length() > 0) {
        // reset skip list and skip adjust
        m_skipList = new ArrayList<Object>();
        m_trainingRemapSkipAdjust = 0;

        String[] parts = aList.split(",");

        for (String p : parts) {
          p = p.trim();
          // try as day of week or month of the year first
          if (m_handlerPeriodicity == Periodicity.UNKNOWN
              || m_handlerPeriodicity == Periodicity.HOURLY
              || m_handlerPeriodicity == Periodicity.DAILY
              || m_handlerPeriodicity == Periodicity.MONTHLY) {
            if (p.equalsIgnoreCase("mon") || p.equalsIgnoreCase("monday")) {
              if (m_handlerPeriodicity != Periodicity.MONTHLY)
                m_skipList.add("mon");
              continue;
            } else if (p.equalsIgnoreCase("tue")
                || p.equalsIgnoreCase("tuesday")) {
              if (m_handlerPeriodicity != Periodicity.MONTHLY)
                m_skipList.add("tue");
              continue;
            } else if (p.equalsIgnoreCase("wed")
                || p.equalsIgnoreCase("wednesday")) {
              if (m_handlerPeriodicity != Periodicity.MONTHLY)
                m_skipList.add("wed");
              continue;
            } else if (p.equalsIgnoreCase("thu")
                || p.equalsIgnoreCase("thursday")) {
              if (m_handlerPeriodicity != Periodicity.MONTHLY)
                m_skipList.add("thu");
              continue;
            } else if (p.equalsIgnoreCase("fri")
                || p.equalsIgnoreCase("friday")) {
              if (m_handlerPeriodicity != Periodicity.MONTHLY)
                m_skipList.add("fri");
              continue;
            } else if (p.equalsIgnoreCase("sat")
                || p.equalsIgnoreCase("saturday")) {
              if (m_handlerPeriodicity != Periodicity.MONTHLY)
                m_skipList.add("sat");
              continue;
            } else if (p.equalsIgnoreCase("sun")
                || p.equalsIgnoreCase("sunday")) {
              if (m_handlerPeriodicity != Periodicity.MONTHLY)
                m_skipList.add("sun");
              continue;
            } else if (p.equalsIgnoreCase("weekend")) {
              if (m_handlerPeriodicity != Periodicity.MONTHLY) {
                m_skipList.add("sat");
                m_skipList.add("sun");
                continue;
              }
            } else if (p.equalsIgnoreCase("jan")
                || p.equalsIgnoreCase("january")) {
              m_skipList.add("jan");
              continue;
            } else if (p.equalsIgnoreCase("feb")
                || p.equalsIgnoreCase("february")) {
              m_skipList.add("feb");
              continue;
            } else if (p.equalsIgnoreCase("mar") || p.equalsIgnoreCase("march")) {
              m_skipList.add("mar");
              continue;
            } else if (p.equalsIgnoreCase("apr") || p.equalsIgnoreCase("april")) {
              m_skipList.add("apr");
              continue;
            } else if (p.equalsIgnoreCase("may")) {
              m_skipList.add("may");
              continue;
            } else if (p.equalsIgnoreCase("jun") || p.equalsIgnoreCase("june")) {
              m_skipList.add("jun");
              continue;
            } else if (p.equalsIgnoreCase("jul") || p.equalsIgnoreCase("july")) {
              m_skipList.add("jul");
              continue;
            } else if (p.equalsIgnoreCase("aug")
                || p.equalsIgnoreCase("august")) {
              m_skipList.add("aug");
              continue;
            } else if (p.equalsIgnoreCase("sep")
                || p.equalsIgnoreCase("september")) {
              m_skipList.add("sep");
              continue;
            } else if (p.equalsIgnoreCase("oct")
                || p.equalsIgnoreCase("october")) {
              m_skipList.add("oct");
              continue;
            } else if (p.equalsIgnoreCase("nov")
                || p.equalsIgnoreCase("november")) {
              m_skipList.add("nov");
              continue;
            } else if (p.equalsIgnoreCase("dec")
                || p.equalsIgnoreCase("december")) {
              m_skipList.add("dec");
              continue;
            }
          }

          // try as a number (no checking is done for numbers out of
          // range with respect to a given periodicity)
          try {
            int num = Integer.parseInt(p);
            m_skipList.add(new Integer(p));
            continue;
          } catch (NumberFormatException n) {
          }

          // last of all try as a specific date (if we have a date formatting
          // string)
          if (dateFormat != null && dateFormat.length() > 0) {
            // first check to see if there is a custom format attached to this
            // entry
            String datePart = p;
            if (p.indexOf('@') > 0) {
              String[] dateParts = p.split("@");
              datePart = dateParts[0];
              dateFormat = dateParts[1];
            }

            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern(dateFormat);
            try {
              Date d = sdf.parse(datePart);
              m_skipList.add(d);
              continue;
            } catch (ParseException e) {
            }
          }

          throw new Exception("Unrecognized skip entry string : " + p);
        }
      }
    }

    /**
     * Get the delta time of the periodicity being managed
     * 
     * @return the delta time
     */
    public double deltaTime() {
      return m_deltaTime;
    }

    /**
     * Set the delta time for the periodicity being managed
     * 
     * @param deltaTime the delta time to use
     */
    public void setDeltaTime(double deltaTime) {
      m_deltaTime = deltaTime;
      m_handlerPeriodicity.setDeltaTime(m_deltaTime);
    }

    /**
     * Set the first date time stamp value in the batch training data
     * 
     * @param tsbase the first date time stamp value in the batch training data
     *          as a long (num milliseconds since epoch)
     */
    public void setDateTimeStampInitial(long tsbase) {
      m_isDateBased = true;
      m_dateTimeStampInitialVal = tsbase;
      GregorianCalendar c = new GregorianCalendar();

      Date d = new Date(m_dateTimeStampInitialVal);
      c.setTime(d);

      if (m_handlerPeriodicity == Periodicity.MONTHLY
          || m_handlerPeriodicity == Periodicity.WEEKLY
          || m_handlerPeriodicity == Periodicity.QUARTERLY) {
        m_dateTimeStampBaseVal = c.get(Calendar.YEAR);
      } else {
        m_dateTimeStampBaseVal = m_dateTimeStampInitialVal;
      }
    }

    /**
     * Get the first date time stamp value in the batch training data
     * 
     * @return the first date time stamp value in the batch training data
     * @throws Exception if the periodicity being managed is not date
     *           timestamp-based
     */
    public long getDateTimeStampInitial() throws Exception {
      if (!isDateBased()) {
        throw new Exception("This periodicity is not date timestamp-based");
      }
      return m_dateTimeStampInitialVal;
    }

    /**
     * Set the last date timestamp value in the batch training data
     * 
     * @param tsfinal the last date timestamp value in the batch training data
     *          as a long (num milliseconds since the epoch).
     */
    public void setDateTimeStampFinal(long tsfinal) {
      m_isDateBased = true;
      m_dateTimeStampFinalVal = tsfinal;
    }

    /**
     * Get the last date timestamp value in the batch training data
     * 
     * @return the last date timestamp value in the batch training data
     * @throws Exception if the periodicity being managed is not date
     *           timestamp-based
     */
    public long getDateTimeStampFinal() throws Exception {
      if (!isDateBased()) {
        throw new Exception("This periodicity is not date timestamp-based");
      }
      return m_dateTimeStampFinalVal;
    }

    /**
     * Set whether the periodicity being managed is date timestamp-based
     * 
     * @param isDateBased true if the periodicity being managed is date
     *          timestamp-based
     */
    public void setIsDateBased(boolean isDateBased) {
      m_isDateBased = isDateBased;
    }

    /**
     * Returns true if the periodicity being managed is date timestamp-based
     * 
     * @return true if the periodicity being managed is date timestamp-based
     */
    public boolean isDateBased() {
      return m_isDateBased;
    }

    /**
     * Checks to see if the supplied date is in the list of time units to skip
     * (i.e. should not be considered as a time increment).
     * 
     * @param toCheck the date to check
     * @return true if the date is in the skip list
     */
    public boolean dateInSkipList(Date toCheck) {
      if (m_skipList == null || m_skipList.size() == 0) {
        return false;
      }

      GregorianCalendar c = new GregorianCalendar();
      c.setTime(toCheck);
      for (Object o : m_skipList) {
        if (o instanceof String) {
          if (o.toString().equals("mon")) {
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
              return true;
            }
          }
          if (o.toString().equals("tue")) {
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
              return true;
            }
          }
          if (o.toString().equals("wed")) {
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
              return true;
            }
          }
          if (o.toString().equals("thu")) {
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
              return true;
            }
          }
          if (o.toString().equals("fri")) {
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
              return true;
            }
          }
          if (o.toString().equals("sat") || o.toString().equals("weekend")) {
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
              return true;
            }
          }
          if (o.toString().equals("sun") || o.toString().equals("weekend")) {
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
              return true;
            }
          }
          if (o.toString().equals("jan")) {
            if (c.get(Calendar.MONTH) == Calendar.JANUARY) {
              return true;
            }
          }
          if (o.toString().equals("feb")) {
            if (c.get(Calendar.MONTH) == Calendar.FEBRUARY) {
              return true;
            }
          }
          if (o.toString().equals("mar")) {
            if (c.get(Calendar.MONTH) == Calendar.MARCH) {
              return true;
            }
          }
          if (o.toString().equals("apr")) {
            if (c.get(Calendar.MONTH) == Calendar.APRIL) {
              return true;
            }
          }
          if (o.toString().equals("may")) {
            if (c.get(Calendar.MONTH) == Calendar.MAY) {
              return true;
            }
          }
          if (o.toString().equals("jun")) {
            if (c.get(Calendar.MONTH) == Calendar.JUNE) {
              return true;
            }
          }
          if (o.toString().equals("jul")) {
            if (c.get(Calendar.MONTH) == Calendar.JULY) {
              return true;
            }
          }
          if (o.toString().equals("aug")) {
            if (c.get(Calendar.MONTH) == Calendar.AUGUST) {
              return true;
            }
          }
          if (o.toString().equals("sep")) {
            if (c.get(Calendar.MONTH) == Calendar.SEPTEMBER) {
              return true;
            }
          }
          if (o.toString().equals("oct")) {
            if (c.get(Calendar.MONTH) == Calendar.OCTOBER) {
              return true;
            }
          }
          if (o.toString().equals("nov")) {
            if (c.get(Calendar.MONTH) == Calendar.NOVEMBER) {
              return true;
            }
          }
          if (o.toString().equals("dec")) {
            if (c.get(Calendar.MONTH) == Calendar.DECEMBER) {
              return true;
            }
          }
        } else if (o instanceof Integer) {
          if (m_handlerPeriodicity == Periodicity.DAILY
              || m_handlerPeriodicity == Periodicity.UNKNOWN) {
            // assume value is day of year
            if (c.get(Calendar.DAY_OF_YEAR) == ((Integer) o).intValue()) {
              return true;
            }
          } else if (m_handlerPeriodicity == Periodicity.HOURLY) {
            // assume value is hour of day
            if (c.get(Calendar.HOUR_OF_DAY) == ((Integer) o).intValue()) {
              return true;
            }
          } else if (m_handlerPeriodicity == Periodicity.WEEKLY) {
            // assume value is week of year
            if (c.get(Calendar.WEEK_OF_YEAR) == ((Integer) o).intValue()) {
              return true;
            }
          } else if (m_handlerPeriodicity == Periodicity.MONTHLY) {
            // assume value is month of year
            if (c.get(Calendar.MONTH) == ((Integer) o).intValue()) {
              return true;
            }
          }
        } else if (o instanceof Date) {
          if (((Date) o).equals(toCheck)) {
            return true;
          }
        }
      }

      return false;
    }

    /**
     * Remaps a date timestamp to an integer starting (from the first time stamp
     * seen in the data) at 0. This is makes any coefficients produced by a
     * regression model for the timestamp (global trend modelling) of reasonable
     * scale. It is also useful for dealing with time units that shouldn't be
     * considered an increment as a negative adjustment can be accumulated for
     * these.
     * 
     * @param inst the instance containing a date timestamp to be remapped
     * @param previous the immediately previous instance in the sequence (may be
     *          null).
     * @param timeStampName the name of the timestamp attribute
     * @return
     * @throws Exception if an error occurs
     */
    public Instance remapDateTimeStamp(Instance inst, Instance previous,
        String timeStampName) throws Exception {
      Instance result = inst;

      if (!isDateBased()) {
        throw new Exception("This periodicity is not date timestamp-based");
      }

      int origIndex = result.dataset().attribute(timeStampName).index();
      Calendar c = new GregorianCalendar();

      boolean applyTrainingSkipAdjust = true;
      long localSkipAdjust = 0;

      if (!result.isMissing(origIndex)) {

        Date d = new Date((long) result.value(origIndex));
        double origValue = result.value(origIndex);
        if (m_skipList != null && m_skipList.size() > 0 && previous != null) {
          // check this instance's date time stamp against the skip list -
          // our fundamental assumption (for the training data) is that these
          // dates
          // are not actually
          // present in the data (i.e. sat and sun for stock market data). If
          // they
          // are in the data (but with missing targets) then the missing value
          // interpolation routine will have filled them in, which is the wrong
          // thing to do if they are supposed to be skipped over
          if (dateInSkipList(d)) {
            throw new Exception(
                "This instance contains a date time stamp that is "
                    + "a member of the skip list - skip list entries are not time "
                    + "units with respect to the model and should not be present : "
                    + inst.toString());
          }

          if (!previous.isMissing(origIndex)) {
            if (result.value(origIndex) >= previous.value(origIndex)) {
              // compared to the previous date are we more than one time unit
              // ahead?
              double start = previous.value(origIndex);
              double end = origValue;
              while (start < end) {
                start = weka.classifiers.timeseries.core.Utils
                    .advanceSuppliedTimeValue(start, this);
                if (start < end) {
                  if (dateInSkipList(new Date((long) start))) {
                    m_trainingRemapSkipAdjust--;
                  } else {
                    // oh oh the difference between the current and previous
                    // instance
                    // is more than one time step but the intervening step(s)
                    // are
                    // not in the skip list!
                    throw new Exception("There is an increment of more than "
                        + "one time step between\n" + previous.toString()
                        + "\nand\n" + inst.toString() + "\n but none of the "
                        + "intervening time steps are in the " + "skip list.");
                  }
                }
              }
            } else {
              // we have a problem here - data is not sorted in ascending order
              // of the date time stamp!
              throw new Exception(
                  "The data does not seem to be sorted in ascending order "
                      + "of the date time stamp!");
            }
          }
        }

        if (m_skipList != null && m_skipList.size() > 0 && previous == null) {
          // this case indicates that we are being invoked in a
          // priming/forecasting context

          // check that this instance does not occur before the first training
          // instance!!
          if (origValue < m_dateTimeStampInitialVal) {
            throw new Exception(
                "The timestamp for this instance occurs before the "
                    + "timestamp of the first training instance!");
          }
          // can't prime/forecast for values that occurred before the training
          // data.

          double end = result.value(origIndex);
          // first advance end until it is not in the skip list (this won't
          // be needed for priming instances that are within the training
          // date range), but might occur for closed-loop forecasting when
          // the date is advanced one time unit for each step
          while (dateInSkipList(new Date((long) end))) {
            end = weka.classifiers.timeseries.core.Utils
                .advanceSuppliedTimeValue(end, this);
          }

          double start = 0;
          if (end < m_dateTimeStampFinalVal) {
            // priming/forecasting within the range of the training data -
            // will have to recompute all skips from the initial training
            // time stamp up to this instance and not apply the pre-computed
            // skip total for the full training period
            applyTrainingSkipAdjust = false;
            start = m_dateTimeStampInitialVal;
          } else {
            // priming/forecasting beyond the last training date time stamp seen
            start = m_dateTimeStampFinalVal;
          }

          // now compute local skip adjust from start up to end
          while (start < end) {
            start = weka.classifiers.timeseries.core.Utils
                .advanceSuppliedTimeValue(start, this);
            if (start < end) {
              if (dateInSkipList(new Date((long) start))) {
                localSkipAdjust--;
              }
            }
          }
          // set end as the current value
          d = new Date((long) end);
          origValue = end;
        }

        if (m_handlerPeriodicity == Periodicity.MONTHLY
            || m_handlerPeriodicity == Periodicity.WEEKLY
            || m_handlerPeriodicity == Periodicity.QUARTERLY) {
          c.setTime(d);
          long year = c.get(Calendar.YEAR);
          long month = c.get(Calendar.MONTH);
          long week = c.get(Calendar.WEEK_OF_YEAR);
          long remapped = 0;
          if (m_handlerPeriodicity == Periodicity.MONTHLY) {
            remapped = ((year - m_dateTimeStampBaseVal) * 12) + month;
          } else if (m_handlerPeriodicity == Periodicity.WEEKLY) {
            remapped = ((year - m_dateTimeStampBaseVal) * 52) + week;

            // adjust for the case where week 1 of the year actually starts
            // in the last week of December
            if (month == Calendar.DECEMBER && week == 1) {
              remapped += 52;
            }
          } else if (m_handlerPeriodicity == Periodicity.QUARTERLY) {
            remapped = ((year - m_dateTimeStampBaseVal) * 4)
                + ((month / 3L) + 1L);
          }

          if (m_skipList != null && m_skipList.size() > 0) {
            remapped += (applyTrainingSkipAdjust) ? m_trainingRemapSkipAdjust
                : 0;
            remapped += localSkipAdjust;
          }

          result.setValue(result.numAttributes() - 1, remapped);
        } else {
          double remapped = origValue - m_dateTimeStampInitialVal;
          remapped /= deltaTime();// m_deltaTime;

          // it might (or might not) make sense to take the floor here. For
          // daily data
          // I have the feeling that data arithmetic (adding 1 to day of the
          // year)
          // may actually add slightly more than
          // a day at certain times (to account for) leap seconds/years
          // remapped = Math.floor(remapped);
          if (m_skipList != null && m_skipList.size() > 0) {
            remapped += (applyTrainingSkipAdjust) ? m_trainingRemapSkipAdjust
                : 0;
            remapped += localSkipAdjust;
          }
          result.setValue(result.numAttributes() - 1, remapped);
        }
      }
      return result;
    }
  }

  /**
   * Utility method that uses heuristics to identify the periodicity of the data
   * with respect to a time stamp. If the time stamp is not a date then the
   * periodicity is UNKNOWN with a delta set by computing the average difference
   * between consecutive time stamp values. Configures the periodicity with
   * first and last time stamp entries in the data.
   * 
   * @param insts the instances to determine the periodicity from
   * @param timeName the name of the time stamp attribute
   * @param userHint a specific periodicity to defer to. The user should provide
   *          a specific periodicity when the data has non-constant differences
   *          in time between consecutive elements and a skip list will be used
   *          to correct for this. Specifying UNKNOWN as the periodicity here
   *          will result in the heuristic detection routine being applied.
   * @return the configured Periodicity of the data.
   */
  public static PeriodicityHandler determinePeriodicity(Instances insts,
      String timeName, Periodicity userHint) {

    double fiveMins = 300000D;
    double oneHour = 3600000D;
    double oneDay = oneHour * 24D;
    double oneWeek = oneDay * 7D;
    double thirtyDays = oneHour * 24D * 30D;
    double approxQuarter = thirtyDays * 3D;
    double oneYear = oneDay * 365D;

    double averageDelta = Utils.missingValue();
    int timeIndex = insts.attribute(timeName).index();
    PeriodicityHandler result = new PeriodicityHandler();

    if (timeIndex < 0) {
      result.setPeriodicity(Periodicity.UNKNOWN);
      result.setDeltaTime(Utils.missingValue());
      return result;
    }

    if (userHint != Periodicity.UNKNOWN && insts.attribute(timeIndex).isDate()) {
      // trust the user's indication
      result.setPeriodicity(userHint);
      switch (userHint) {
      case HOURLY:
        result.setDeltaTime(oneHour);
        break;
      case DAILY:
        result.setDeltaTime(oneDay);
        break;
      case WEEKLY:
        result.setDeltaTime(oneWeek);
        break;
      case YEARLY:
        result.setDeltaTime(oneYear);
        break;

      // others don't matter as date arithmetic is used
      }

      long initialTS = (long) insts.instance(0).value(timeIndex);
      long finalTS = (long) insts.instance(insts.numInstances() - 1).value(
          timeIndex);
      result.setDateTimeStampInitial(initialTS);
      result.setDateTimeStampFinal(finalTS);
      return result;
    }

    List<Double> deltas = new ArrayList<Double>();
    for (int i = 1; i < insts.numInstances(); i++) {
      if (!insts.instance(i).isMissing(timeIndex)
          && !insts.instance(i - 1).isMissing(timeIndex)) {
        deltas.add(new Double(insts.instance(i).value(timeIndex)
            - insts.instance(i - 1).value(timeIndex)));
      }
    }

    double previousDelta = -1;
    double deltaSum = 0;
    for (int i = 0; i < deltas.size(); i++) {
      if (i == 0) {
        previousDelta = deltas.get(i);
        deltaSum += previousDelta;
      } else {
        double currentDelta = deltas.get(i);
        if (currentDelta - previousDelta != 0) {
          // nonConstant = true;
        }
        previousDelta = currentDelta;
        deltaSum += currentDelta;
      }
    }
    averageDelta = deltaSum /= deltas.size();

    if (insts.attribute(timeIndex).isDate()) {
      long initialTS = (long) insts.instance(0).value(timeIndex);
      long finalTS = (long) insts.instance(insts.numInstances() - 1).value(
          timeIndex);

      // allow +-5mins for hourly
      if (Math.abs(oneHour - averageDelta) <= fiveMins) {
        result.setPeriodicity(Periodicity.HOURLY);
        result.setDeltaTime(oneHour);
        result.setDateTimeStampInitial(initialTS);
        result.setDateTimeStampFinal(finalTS);
        return result;
      }

      // allow += 1 hour for daily
      if (Math.abs(oneDay - averageDelta) <= oneHour) {
        result.setPeriodicity(Periodicity.DAILY);
        result.setDeltaTime(oneDay);
        result.setDateTimeStampInitial(initialTS);
        result.setDateTimeStampFinal(finalTS);
        return result;
      }

      // allow +- 6 hours for weekly
      if (Math.abs(oneWeek - averageDelta) <= (oneDay / 4.0)) {
        result.setPeriodicity(Periodicity.WEEKLY);
        result.setDeltaTime(oneWeek);
        result.setDateTimeStampInitial(initialTS);
        result.setDateTimeStampFinal(finalTS);
        return result;
      }

      // allow +- 3 days for monthly
      if (Math.abs(thirtyDays - averageDelta) <= (oneDay * 3.0)) {
        result.setPeriodicity(Periodicity.MONTHLY);
        result.setDeltaTime(thirtyDays);
        result.setDateTimeStampInitial(initialTS);
        result.setDateTimeStampFinal(finalTS);
        return result;
      }

      // allow +- 1 week for quarterly
      if (Math.abs(approxQuarter - averageDelta) <= oneWeek) {
        result.setPeriodicity(Periodicity.QUARTERLY);
        result.setDeltaTime(approxQuarter);
        result.setDateTimeStampInitial(initialTS);
        result.setDateTimeStampFinal(finalTS);
        return result;
      }

      // allow +- 2 days for yearly
      if (Math.abs(oneYear - averageDelta) <= (oneDay * 2.0)) {
        result.setPeriodicity(Periodicity.YEARLY);
        result.setDeltaTime(oneYear);
        result.setDateTimeStampInitial(initialTS);
        result.setDateTimeStampFinal(finalTS);
        return result;
      }

      // otherwise UNKNOWN but date-based
      result.setPeriodicity(Periodicity.UNKNOWN);
      result.setIsDateBased(true);
      result.setDeltaTime(averageDelta);
      result.setDateTimeStampInitial(initialTS);
      result.setDateTimeStampFinal(finalTS);
      return result;
    }

    // default for non-date-based time stamps
    result.setPeriodicity(Periodicity.UNKNOWN);
    result.setIsDateBased(false);
    result.setDeltaTime(averageDelta);
    return result;
  }

  protected Instances setupDerivedPeriodics(Instances insts) throws Exception {
    Instances result = insts;

    if (m_adjustForTrends && !m_useArtificialTimeIndex) {

      m_dateBasedPeriodicity = determinePeriodicity(insts, m_timeStampName,
          m_userHintPeriodicity);
      if (m_skipEntries != null && m_skipEntries.length() > 0) {
        m_dateBasedPeriodicity.setSkipList(m_skipEntries, m_dateFormat);
      }

      // int timeIndex = insts.attribute(m_timeStampName).index();

      // m_deltaTime = m_dateBasedPeriodicity.deltaTime();
      /*
       * if (m_dateBasedPeriodicity == Periodicity.MONTHLY) {
       * m_advanceTimeStampByMonth = true; }
       */

      if (insts.attribute(m_timeStampName).isDate()) {
        m_derivedPeriodicMakers = new ArrayList<Filter>();
        // now add filters for each requested derived periodic value
        if (m_am) {
          // numeric binary
          Add a = new Add();
          a.setAttributeName("AM");
          a.setInputFormat(insts);
          result = Filter.useFilter(result, a);
          m_derivedPeriodicMakers.add(a);
        }

        if (m_dayOfWeek) {
          // nominal
          Add a = new Add();
          a.setAttributeName("DayOfWeek");
          a.setNominalLabels("sun,mon,tue,wed,thu,fri,sat");
          a.setInputFormat(result);
          result = Filter.useFilter(result, a);
          m_derivedPeriodicMakers.add(a);
        }

        if (m_dayOfMonth) {
          // nominal
          Add a = new Add();
          a.setAttributeName("DayOfMonth");
          a.setNominalLabels("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,"
              + "20,21,22,23,24,25,26,27,28,29,30,31");
          a.setInputFormat(result);
          result = Filter.useFilter(result, a);
          m_derivedPeriodicMakers.add(a);
        }

        if (m_numDaysInMonth) {
          Add a = new Add();
          a.setAttributeName("NumDaysInMonth");
          a.setInputFormat(insts);
          result = Filter.useFilter(result, a);
          m_derivedPeriodicMakers.add(a);
        }

        if (m_weekend) {
          // numeric binary
          Add a = new Add();
          a.setAttributeName("Weekend");
          a.setInputFormat(result);
          result = Filter.useFilter(result, a);
          m_derivedPeriodicMakers.add(a);
        }

        if (m_monthOfYear) {
          // nominal
          Add a = new Add();
          a.setAttributeName("Month");
          a.setNominalLabels("jan,feb,mar,apr,may,jun,jul,aug,sep,oct,nov,dec");
          a.setInputFormat(result);
          result = Filter.useFilter(result, a);
          m_derivedPeriodicMakers.add(a);
        }

        if (m_quarter) {
          // nominal
          Add a = new Add();
          a.setAttributeName("Quarter");
          a.setNominalLabels("Q1,Q2,Q3,Q4");
          a.setInputFormat(result);
          result = Filter.useFilter(result, a);
          m_derivedPeriodicMakers.add(a);
        }

        // any custom date-derived periodics?
        if (m_customPeriodics != null) {
          for (String name : m_customPeriodics.keySet()) {
            List<CustomPeriodicTest> l = m_customPeriodics.get(name);
            // check to see if we should create a multi-label nominal
            // attribute instead of a numeric binary attribute. If all
            // test intervals have a non-null label then attribute will
            // be nominal
            boolean binary = false;
            String labels = "";
            Set<String> uniqueLabels = new HashSet<String>();
            for (CustomPeriodicTest t : l) {
              if (t.getLabel() == null || t.getLabel().length() == 0) {
                binary = true;
                break;
              } else {
                if (uniqueLabels.add(t.getLabel())) {
                  labels += t.getLabel() + ",";
                }
              }
            }

            Add a = new Add();
            a.setAttributeName("c_" + name);
            if (!binary) {
              labels = labels.substring(0, labels.lastIndexOf(','));
              a.setAttributeType(new SelectedTag("NOM", Add.TAGS_TYPE));
              a.setNominalLabels(labels);
            }

            a.setInputFormat(result);
            result = Filter.useFilter(result, a);
            m_derivedPeriodicMakers.add(a);
          }
        }

        // set the values for each instance in the data
        for (int i = 0; i < result.numInstances(); i++) {
          setDerivedPeriodicValues(result.instance(i));
        }
      }
    }

    return result;
  }

  protected void setDerivedPeriodicValues(Instance inst) {
    if (m_adjustForTrends && !m_useArtificialTimeIndex) {
      if (inst.dataset().attribute(m_timeStampName).isDate()) {
        int timeIndex = inst.dataset().attribute(m_timeStampName).index();
        long time = (inst.isMissing(timeIndex)) ? -1 : (long) inst
            .value(timeIndex);
        Date instDate = null;
        GregorianCalendar cal = new GregorianCalendar();
        if (time != -1) {
          instDate = new Date(time);
          cal.setTime(instDate);
        }

        if (m_am) {
          if (instDate == null) {
            inst.setMissing(inst.dataset().attribute("AM"));
          } else {
            if (cal.get(Calendar.AM_PM) == Calendar.AM) {
              inst.setValue(inst.dataset().attribute("AM"), 1);
            } else {
              inst.setValue(inst.dataset().attribute("AM"), 0);
            }
          }
        }

        if (m_dayOfWeek || m_weekend) {
          if (instDate == null) {
            if (m_dayOfWeek) {
              inst.setMissing(inst.dataset().attribute("DayOfWeek"));
            }
            if (m_weekend) {
              inst.setMissing(inst.dataset().attribute("Weekend"));
            }
          } else {
            int dow = cal.get(Calendar.DAY_OF_WEEK);
            String day = "";
            switch (dow) {
            case Calendar.SUNDAY:
              day = "sun";
              break;
            case Calendar.MONDAY:
              day = "mon";
              break;
            case Calendar.TUESDAY:
              day = "tue";
              break;
            case Calendar.WEDNESDAY:
              day = "wed";
              break;
            case Calendar.THURSDAY:
              day = "thu";
              break;
            case Calendar.FRIDAY:
              day = "fri";
              break;
            case Calendar.SATURDAY:
              day = "sat";
              break;
            }

            if (day.length() > 0) {
              if (m_dayOfWeek) {
                inst.setValue(inst.dataset().attribute("DayOfWeek"), day);
              }

              if (m_weekend) {
                if (day.equals("sat") || day.equals("sun")) {
                  inst.setValue(inst.dataset().attribute("Weekend"), 1);
                } else {
                  inst.setValue(inst.dataset().attribute("Weekend"), 0);
                }
              }
            } else {
              if (m_dayOfWeek) {
                inst.setMissing(inst.dataset().attribute("DayOfWeek"));
              }

              if (m_weekend) {
                inst.setMissing(inst.dataset().attribute("Weekend"));
              }
            }
          }
        }

        if (m_dayOfMonth) {
          if (instDate == null) {
            inst.setMissing(inst.dataset().attribute("DayOfWeek"));
          } else {
            int dom = cal.get(Calendar.DAY_OF_MONTH);
            inst.setValue(inst.dataset().attribute("DayOfMonth"), (dom - 1));
          }
        }

        if (m_numDaysInMonth) {
          if (instDate == null) {
            inst.setMissing(inst.dataset().attribute("NumDaysInMonth"));
          } else {
            boolean isLeap = cal.isLeapYear(cal.get(Calendar.YEAR));
            int daysInMonth = 0;
            int month = cal.get(Calendar.MONTH);
            if (month == Calendar.FEBRUARY) {
              daysInMonth = 28;
              if (isLeap) {
                daysInMonth++;
              }
            } else if (month == Calendar.APRIL || month == Calendar.JUNE
                || month == Calendar.SEPTEMBER || month == Calendar.NOVEMBER) {
              daysInMonth = 30;
            } else {
              daysInMonth = 31;
            }

            inst.setValue(inst.dataset().attribute("NumDaysInMonth"),
                daysInMonth);
          }
        }

        if (m_monthOfYear || m_quarter) {
          if (instDate == null) {
            if (m_monthOfYear) {
              inst.setMissing(inst.dataset().attribute("Month"));
            }

            if (m_quarter) {
              inst.setMissing(inst.dataset().attribute("Quarter"));
            }
          } else {
            int moy = cal.get(Calendar.MONTH);
            if (m_monthOfYear) {
              String month = inst.dataset().attribute("Month").value(moy);
              inst.setValue(inst.dataset().attribute("Month"), month);
            }

            if (m_quarter) {
              String quarter = "";
              if (moy == 0 || moy == 1 || moy == 2) {
                quarter = "Q1";
              } else if (moy == 3 || moy == 4 || moy == 5) {
                quarter = "Q2";
              } else if (moy == 6 || moy == 7 || moy == 8) {
                quarter = "Q3";
              } else {
                quarter = "Q4";
              }

              inst.setValue(inst.dataset().attribute("Quarter"), quarter);
            }
          }
        }

        if (m_customPeriodics != null) {
          for (String name : m_customPeriodics.keySet()) {
            Attribute att = inst.dataset().attribute("c_" + name);
            if (att != null) {
              if (instDate == null) {
                inst.setMissing(att);
              } else {
                // evaluate for this periodic
                List<CustomPeriodicTest> l = m_customPeriodics.get(name);
                boolean result = false;
                String label = null;
                for (CustomPeriodicTest t : l) {
                  result = (result || t.evaluate(instDate));

                  // match?
                  if (result) {
                    label = t.getLabel();
                    break;
                  } else {
                    label = null;
                  }
                }

                if (result) {
                  if (att.isNominal()) {
                    if (label == null) {
                      // inst.setMissing(att);
                      System.err.println("This shouldn't happen!!");
                    } else {
                      inst.setValue(att, att.indexOfValue(label));
                    }
                  } else {
                    // numeric binary attribute
                    inst.setValue(att, 1);
                  }
                } else {
                  if (att.isNominal()) {
                    inst.setMissing(att);
                  } else {
                    inst.setValue(att, 0);
                  }
                }
              }
            } else {
              System.err.println("WARNING: custom periodic att c_" + name
                  + " not found in instances!");
            }
          }
        }
      }
    }
  }

  protected void setupPeriodicMaps(Instances insts) {
    m_primaryPeriodicSequence = null;
    m_secondaryPeriodicLookups = null;

    if (m_primaryPeriodicName != null && m_primaryPeriodicName.length() > 0) {
      int primaryIndex = insts.attribute(m_primaryPeriodicName).index();

      if (primaryIndex < 0) {
        return;
      }

      m_primaryPeriodicSequence = new HashMap<String, String>();
      for (int i = 0; i < insts.numInstances() - 1; i++) {
        Instance current = insts.instance(i);
        Instance next = insts.instance(i + 1);
        if (!Utils.isMissingValue(current.value(primaryIndex))
            && !Utils.isMissingValue(next.value(primaryIndex))) {
          String key = current.stringValue(primaryIndex);
          String value = next.stringValue(primaryIndex);
          if (m_primaryPeriodicSequence.get(key) == null) {
            m_primaryPeriodicSequence.put(key, value);
          } else {
            // check to see if this value is consistent with
            // what we've seen previously
            String previous = m_primaryPeriodicSequence.get(key);
            if (!previous.equals(value)) {
              // we don't have a consistent sequence, so can't
              // use this as the main periodic sequence
              m_primaryPeriodicSequence = null;
              break;
            }
          }
        }
      }

      if (m_primaryPeriodicSequence != null) {
        // now look for any other nominal attributes that
        // might be secondary periodic sequences at a higher
        // granularity than the primary sequence
        m_secondaryPeriodicLookups = new HashMap<Attribute, Map<String, String>>();

        for (int i = 0; i < insts.numAttributes(); i++) {
          if (insts.attribute(i).isNominal() && i != primaryIndex) {
            Attribute candidate = insts.attribute(i);
            Map<String, String> candidateMap = new HashMap<String, String>();
            for (int j = 0; j < insts.numInstances(); j++) {
              Instance current = insts.instance(j);
              if (!Utils.isMissingValue(current.value(primaryIndex))
                  && !Utils.isMissingValue(j)) {
                String key = current.stringValue(primaryIndex);
                String value = current.stringValue(j);

                if (candidateMap.get(key) == null) {
                  candidateMap.put(key, value);
                } else {
                  // check to see if this value is consistent with what
                  // we've seen previously
                  String previous = candidateMap.get(key);
                  if (!previous.equals(value)) {
                    // we need one unique value of the secondary to occur
                    // in conjunction for each primary (e.g. months of the year
                    // and quarters - each month is associated with only one
                    // quarter
                    // of the year)
                    candidateMap = null;
                    break;
                  }
                }
              }
            }

            if (candidateMap != null) {
              m_secondaryPeriodicLookups.put(candidate, candidateMap);
            }
          }
        }
      }
    }
  }

  private void setPeriodicValues(Instance inst) throws Exception {
    if (m_primaryPeriodicName != null && m_primaryPeriodicName.length() > 0) {
      int primaryIndex = m_originalHeader.attribute(m_primaryPeriodicName)
          .index();

      if (primaryIndex < 0) {
        throw new Exception(
            "Can't find the primary periodic variable in the data!");
      }

      // determine the next value in the sequence
      double lastPeriodicIndex = m_lastHistoricInstance.value(primaryIndex);
      if (!Utils.isMissingValue(lastPeriodicIndex)) {
        String lastPeriodicValue = m_lastHistoricInstance
            .stringValue(primaryIndex);
        String successor = m_primaryPeriodicSequence.get(lastPeriodicValue);
        if (successor != null) {
          // newVals[primaryIndex] =
          // m_originalHeader.attribute(primaryIndex).indexOfValue(successor);
          inst.setValue(primaryIndex, m_originalHeader.attribute(primaryIndex)
              .indexOfValue(successor));

          // now we can look for secondary periodic attributes
          if (m_secondaryPeriodicLookups != null) {
            for (int i = 0; i < m_originalHeader.numAttributes(); i++) {
              Attribute current = m_originalHeader.attribute(i);
              Map<String, String> correspondingL = m_secondaryPeriodicLookups
                  .get(current);
              if (correspondingL != null) {
                String correspondingV = correspondingL.get(successor);
                if (correspondingV != null) {
                  // newVals[i] =
                  // m_originalHeader.attribute(i).indexOfValue(correspondingV);
                  inst.setValue(i,
                      m_originalHeader.attribute(i)
                          .indexOfValue(correspondingV));
                } else {
                  // Set a missing value
                  // newVals[i] = Utils.missingValue();
                  inst.setMissing(i);
                }
              }
            }
          }
        } else {
          // TODO
          // We can either set a missing value here if we don't have a successor
          // in the map
          // or we can look at the order that the values are declared in the
          // header for
          // the primary periodic sequence and assume that this order is
          // correct.
          // newVals[primaryIndex] = Utils.missingValue();
          inst.setMissing(primaryIndex);

        }
      } else {
        // newVals[primaryIndex] = Utils.missingValue();
        inst.setMissing(primaryIndex);
      }
    }
  }

  protected Instances removeExtraneousAttributes(Instances insts)
      throws Exception {
    int primaryIndex = -1;
    String removeList = "";

    if (m_primaryPeriodicName != null && m_primaryPeriodicName.length() > 0) {
      primaryIndex = insts.attribute(m_primaryPeriodicName).index();
    }

    for (int i = 0; i < insts.numAttributes(); i++) {
      if (i == primaryIndex) {
        continue;
      }

      if (m_secondaryPeriodicLookups != null) {
        if (m_secondaryPeriodicLookups.containsKey(insts.attribute(i))) {
          continue;
        }
      }

      boolean target = false;
      for (String s : m_fieldsToLag) {
        if (insts.attribute(i).name().equals(s)) {
          target = true;
          break;
        }
      }

      if (target) {
        continue;
      }

      if (m_overlayFields != null) {
        boolean overlay = false;
        for (String s : m_overlayFields) {
          if (insts.attribute(i).name().equals(s)) {
            overlay = true;
            break;
          }
        }

        if (overlay) {
          continue;
        }
      }

      if (m_adjustForTrends && m_timeStampName != null
          && m_timeStampName.length() > 0) {
        if (i == insts.attribute(m_timeStampName).index()) {
          continue;
        }
      }

      // otherwise, this is some attribute that we are not predicting and
      // wont be able to determine the value for when forecasting future
      // instances. So we can't let the model use it.
      removeList += "" + (i + 1) + ",";
    }

    if (removeList.length() > 0) {
      removeList = removeList.substring(0, removeList.lastIndexOf(','));
      m_extraneousAttributeRemover = new Remove();
      m_extraneousAttributeRemover.setAttributeIndices(removeList);
      m_extraneousAttributeRemover.setInputFormat(insts);
      insts = Filter.useFilter(insts, m_extraneousAttributeRemover);
    }

    return insts;
  }

  /**
   * Creates a transformed data set based on the user's settings
   * 
   * @param insts the instances to transform
   * @return a transformed data set
   * @throws Exception if a problem occurs during the creation of lagged and
   *           auxiliary attributes.
   */
  public Instances getTransformedData(Instances insts) throws Exception {
    m_originalHeader = new Instances(insts, 0);
    Instances result = insts;
    m_lastHistoricInstance = result.instance(result.numInstances() - 1);

    setupPeriodicMaps(result);
    result = removeExtraneousAttributes(insts);

    // m_lastArtificialTimeValue = -1;
    m_lastTimeValue = -1;

    if (m_adjustForTrends
        && (m_timeStampName == null || m_timeStampName.length() == 0 || insts
            .attribute(m_timeStampName) == null)) {
      // add an artificial time index. This will be problematic when
      // using the built model to forecast for future time points that do
      // not occur immediately after the last training event. Since the time
      // index is artificial, all we can do for future predictions is assume
      // that the n instances provided to the primeForecaster() method overlap
      // the last n instances of the training data and that future predictions
      // occur from the last known artificial time value + 1.
      m_artificialTimeMaker = new AddID();
      m_artificialTimeMaker.setAttributeName("ArtificialTimeIndex");
      m_artificialTimeMaker.setIDIndex("last");
      m_artificialTimeMaker.setInputFormat(result);
      result = Filter.useFilter(result, m_artificialTimeMaker);
      m_useArtificialTimeIndex = true;
      m_timeStampName = "ArtificialTimeIndex";
      /*
       * m_lastArtificialTimeValue = result.instance(result.numInstances() -
       * 1).value(result.numAttributes() - 1);
       */
      /*
       * m_lastTimeValue = result.instance(result.numInstances() -
       * 1).value(result.numAttributes() - 1);
       */

    } else {
      m_useArtificialTimeIndex = false;
    }

    if (m_adjustForTrends) {
      int timeStampIndex = result.attribute(m_timeStampName).index();

      m_lastTimeValue = result.instance(result.numInstances() - 1).value(
          timeStampIndex);
      Instance last = result.instance(result.numInstances() - 1);
      Instance secondToLast = result.instance(result.numInstances() - 2);
      /*
       * m_deltaTime = last.value(timeStampIndex) -
       * secondToLast.value(timeStampIndex);
       */

      result = setupDerivedPeriodics(result);

      // remap timestamp if it is a date
      result = createDateTimestampRemap(result);
    }

    result = createVarianceAdjusters(result);

    result = createLags(result);
    result = createAveragedLags(result);
    result = createTimeIndexes(result);
    if (m_includeTimeLagCrossProducts) {
      result = createTimeLagCrossProducts(result);
    }

    // remove all instances with missing values at the
    // start of the series?
    if (m_deleteMissingFromStartOfSeries) {
      int start = 0;
      for (int i = 0; i <= m_maxLag; i++) {
        boolean ok = true;
        for (int j = 0; j < result.numAttributes(); j++) {
          if (result.instance(i).isMissing(j)) {
            ok = false;
            break;
          }
        }
        if (!ok) {
          start++;
        } else {
          break;
        }
      }

      System.err.println("******** Discarding " + start
          + " instances from the start.");
      result = new Instances(result, start, result.numInstances() - start);
    }
    // System.err.println(result);
    return result;
  }

  public Instance processInstance(Instance source, boolean incrementTime,
      boolean setAnyPeriodic) throws Exception {
    return processInstance(source, incrementTime, setAnyPeriodic, false);
  }

  public Instance processInstancePreview(Instance source,
      boolean incrementTime, boolean setAnyPeriodic) throws Exception {
    return processInstance(source, incrementTime, setAnyPeriodic, true);
  }

  /**
   * Process an instance in the original format and produce a transformed
   * instance as output. Assumes that the lag maker has been configured an
   * initialized with a call to getTransformedDataset()
   * 
   * @param source an instance in original format
   * @param incrementTime true if any time stamp value should be incremented
   *          based on the time stamp value from the last instance seen and set
   *          in the outputted instance
   * @param setAnyPeriodic true if any user-specified periodic value should be
   *          set in the transformed instance based on the value from the last
   *          instance seen.
   * @return a transformed instance
   * @throws Exception if something goes wrong.
   */
  public Instance processInstance(Instance source, boolean incrementTime,
      boolean setAnyPeriodic, boolean temporary) throws Exception {

    String message = null;
    if ((message = source.dataset().equalHeadersMsg(m_originalHeader)) != null) {
      throw new Exception("[TSLagMaker] cannot process instance because the "
          + "structure\ndiffers from what we were configured with:\n\n"
          + message);
    }

    Instance result = source;

    if (setAnyPeriodic) {
      setPeriodicValues(result);
    }

    m_lastHistoricInstance = new DenseInstance(result);
    m_lastHistoricInstance.setDataset(result.dataset());

    if (m_extraneousAttributeRemover != null) {
      m_extraneousAttributeRemover.input(result);
      result = m_extraneousAttributeRemover.output();
    }

    if (m_artificialTimeMaker != null) {
      m_artificialTimeMaker.input(result);
      result = m_artificialTimeMaker.output();

      // set the correct value here - it can't be done after the fact because
      // of other filters that create the product of time and something else.
      if (incrementTime) {
        double newTime = m_lastTimeValue + 1;
        int timeIndex = result.dataset().attribute(m_timeStampName).index();
        result.setValue(timeIndex, newTime);
        m_lastTimeValue = newTime;
      }
    } else {
      // if we have a genuine time stamp field then make sure
      // that we keep track of the most recent time value
      if (m_adjustForTrends) {
        int timeIndex = result.dataset().attribute(m_timeStampName).index();
        if (incrementTime) {

          double newTime = weka.classifiers.timeseries.core.Utils
              .advanceSuppliedTimeValue(m_lastTimeValue, m_dateBasedPeriodicity);

          // default to add the delta
          /*
           * double newTime = m_lastTimeValue +
           * m_dateBasedPeriodicity.deltaTime();//m_deltaTime; Date d = new
           * Date((long)m_lastTimeValue); Calendar c = new GregorianCalendar();
           * c.setTime(d); if (m_dateBasedPeriodicity == Periodicity.MONTHLY) {
           * c.add(Calendar.MONTH, 1); newTime = (double)c.getTimeInMillis(); }
           * else if (m_dateBasedPeriodicity == Periodicity.WEEKLY) {
           * c.add(Calendar.WEEK_OF_YEAR, 1); newTime =
           * (double)c.getTimeInMillis(); } else if (m_dateBasedPeriodicity ==
           * Periodicity.QUARTERLY) { c.add(Calendar.MONTH, 3); newTime =
           * (double)c.getTimeInMillis(); } else if (m_dateBasedPeriodicity ==
           * Periodicity.DAILY) { c.add(Calendar.DAY_OF_YEAR, 1); newTime =
           * (double)c.getTimeInMillis(); }
           */
          result.setValue(timeIndex, newTime);
          // if (!temporary) {
          m_lastTimeValue = newTime;
          // }
        } else {
          // if (!temporary) {
          // if we have a value, just store it
          if (!result.isMissing(timeIndex)) {
            m_lastTimeValue = result.value(timeIndex);
          }/*
            * else { System.err.println("*****WARNING missing time..."); }
            */
          // }
        }

        // set any derived periodic values
        if (m_derivedPeriodicMakers != null
            && m_derivedPeriodicMakers.size() > 0) {
          for (Filter f : m_derivedPeriodicMakers) {
            f.input(result);
            result = f.output();
          }
          setDerivedPeriodicValues(result);
        }

        // remap the timestamp if necessary
        result = remapDateTimeStamp(result);
      }
    }

    if (m_adjustForVariance) {
      for (Filter f : m_varianceAdjusters) {
        f.input(result);
        result = f.output();
      }
    }

    for (Filter f : m_lagMakers) {
      if (temporary && f instanceof TimeSeriesTranslate) {
        result = ((TimeSeriesTranslate) f).inputOneTemporarily(result);
      } else {
        f.input(result);
        result = f.output();
      }
    }

    if (m_averagedLagMakers != null) {
      for (Filter f : m_averagedLagMakers) {
        f.input(result);
        result = f.output();
      }
    }

    if (m_timeIndexMakers != null) {
      for (Filter f : m_timeIndexMakers) {
        f.input(result);
        result = f.output();
      }
    }

    if (m_includeTimeLagCrossProducts && m_timeLagCrossProductMakers != null) {
      for (Filter f : m_timeLagCrossProductMakers) {
        f.input(result);
        result = f.output();
      }
    }

    return result;
  }

  /**
   * Clears any history accumulated in the lag creating filters.
   * 
   * @throws Exception if something goes wrong.
   */
  public void clearLagHistories() throws Exception {

    if (m_artificialTimeMaker != null) {
      m_artificialTimeMaker.batchFinished();
    }

    for (Filter f : m_lagMakers) {
      f.batchFinished();
    }

    if (m_averagedLagMakers != null) {
      for (Filter f : m_averagedLagMakers) {
        f.batchFinished();
      }
    }

    if (m_timeIndexMakers != null) {
      for (Filter f : m_timeIndexMakers) {
        f.batchFinished();
      }
    }

    if (m_includeTimeLagCrossProducts && m_timeLagCrossProductMakers != null) {
      for (Filter f : m_timeLagCrossProductMakers) {
        f.batchFinished();
      }
    }
  }

  /**
   * Utility method to advance a supplied time value by one unit according to
   * the periodicity set for this LagMaker.
   * 
   * @param valueToAdvance the time value to advance
   * @return the advanced value or the original value if this lag maker is not
   *         adjusting for trends
   */
  public double advanceSuppliedTimeValue(double valueToAdvance) {
    return weka.classifiers.timeseries.core.Utils.advanceSuppliedTimeValue(
        valueToAdvance, m_dateBasedPeriodicity);
  }

  public double decrementSuppliedTimeValue(double valueToDecrement) {
    return weka.classifiers.timeseries.core.Utils.decrementSuppliedTimeValue(
        valueToDecrement, m_dateBasedPeriodicity);
  }
}
