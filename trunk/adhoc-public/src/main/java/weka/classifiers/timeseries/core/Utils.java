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
 *    Utils.java
 *    Copyright (C) 2011 Pentaho Corporation
 */

package weka.classifiers.timeseries.core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import weka.classifiers.timeseries.core.TSLagMaker.Periodicity;
import weka.classifiers.timeseries.core.TSLagMaker.PeriodicityHandler;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Static utility routines.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 50889 $
 * 
 */
public class Utils {

  protected static Instance makeInstance(double timeToAdvance,
      PeriodicityHandler periodicityHandler, int numAtts, int timeIndex) {

    double incrTime = advanceSuppliedTimeValue(timeToAdvance,
        periodicityHandler, false);
    double[] newVals = new double[numAtts];
    for (int i = 0; i < newVals.length; i++) {
      newVals[i] = weka.core.Utils.missingValue();
    }
    newVals[timeIndex] = incrTime;
    Instance newInst = new DenseInstance(1.0, newVals);

    /*
     * if (missingReport != null) { if (periodicityHandler.isDateBased()) {
     * String timeFormat = "yyyy-MM-dd'T'HH:mm:ss"; SimpleDateFormat sdf = new
     * SimpleDateFormat(); sdf.applyPattern(timeFormat); Date d = new
     * Date((long) incrTime); String result = sdf.format(d); //
     * System.err.println("Creating a missing row " + result);
     * missingReport.add(result); } else { missingReport.add("" + incrTime); } }
     */

    return newInst;
  }

  protected static void addToMissingReport(double incrTime,
      PeriodicityHandler periodicityHandler, List<String> missingReport) {
    if (missingReport != null) {
      if (periodicityHandler.isDateBased()) {
        String timeFormat = "yyyy-MM-dd'T'HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern(timeFormat);
        Date d = new Date((long) incrTime);
        String result = sdf.format(d);
        // System.err.println("Creating a missing row " + result);
        missingReport.add(result);
      } else {
        missingReport.add("" + incrTime);
      }
    }
  }

  /**
   * Check to see if there are any instances (time steps) that are missing
   * entirely from the data (and are not in the skip list). We make the
   * assumption that the data won't contain both missing date values *and*
   * entirely missing rows as this is a kind of chicken and egg situation with
   * regards to detecting missing rows. E.g. if we have missing date values
   * bracketing one or more missing rows then we can't do the date comparisons
   * necessary for detecting whether the intermediate rows are missing.
   * Similarly the missing date handling routine assumes that there aren't
   * missing rows so that it can advance the previous date value by one unit in
   * order to compute the value for the current missing date value.
   * 
   * @param toInsert the instances to check
   * @param timeStampAtt the name of time stamp att
   * @param periodicityHandler the periodicity handler
   * @param m_skipEntries the list of data "holes" that are expected (i.e. don't
   *          actually count as time stamp increments)
   * @param missingReport will hold time stamps of any instances we insert
   * @return the (potentially) modified instances
   */
  public static Instances insertMissing(Instances toInsert,
      Attribute timeStampAtt, TSLagMaker.PeriodicityHandler periodicityHandler,
      String m_skipEntries, List<String> missingReport) {

    if (m_skipEntries != null && m_skipEntries.length() > 0) {
      try {
        periodicityHandler.setSkipList(m_skipEntries, "yyyy-MM-dd'T'HH:mm:ss");
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    int timeIndex = timeStampAtt.index();

    /*
     * check to see if there are any instances (time steps) that are missing
     * entirely from the data (and are not in the skip list). We make the
     * assumption that the data won't contain both missing date values *and*
     * entirely missing rows as this is a kind of chicken and egg situation with
     * regards to detecting missing rows. E.g. if we have missing date values
     * bracketing one or more missing rows then we can't do the date comparisons
     * necessary for detecting whether the intermediate rows are missing.
     * Similarly the missing date handling routine assumes that there aren't
     * missing rows so that it can advance the previous date value by one unit
     * in order to compute the value for the current missing date value.
     */
    Instance prevInst = null;
    int current = 0;
    while (true) {
      if (current == toInsert.numInstances()) {
        break;
      }

      if (prevInst != null && !toInsert.instance(current).isMissing(timeIndex)) {
        if (periodicityHandler.getPeriodicity() != Periodicity.MONTHLY
            && periodicityHandler.getPeriodicity() != Periodicity.QUARTERLY) {
          double delta = periodicityHandler.getPeriodicity().deltaTime();

          double diff = toInsert.instance(current).value(timeIndex)
              - prevInst.value(timeIndex);

          /*
           * if the difference is more than 1 delta then insert a new row
           */
          if (diff > 1.5 * delta) {
            if (!periodicityHandler.isDateBased()
                || !periodicityHandler.dateInSkipList(new Date((long) prevInst
                    .value(timeIndex)))) {
              Instance newInst = makeInstance(prevInst.value(timeIndex),
                  periodicityHandler, toInsert.numAttributes(), timeIndex);

              Date candidate = new Date((long) newInst.value(timeIndex));
              if (!periodicityHandler.dateInSkipList(candidate)
                  && candidate.getTime() < toInsert.instance(current).value(
                      timeIndex)) {
                newInst.setDataset(toInsert);
                toInsert.add(current, newInst);
                addToMissingReport(newInst.value(timeIndex),
                    periodicityHandler, missingReport);
              }
            }
          }
        } else {
          Date d = new Date((long) toInsert.instance(current).value(timeIndex));
          Calendar c = new GregorianCalendar();
          c.setTime(d);
          int currentMonth = c.get(Calendar.MONTH);

          d = new Date((long) prevInst.value(timeIndex));
          c.setTime(d);
          int prevMonth = c.get(Calendar.MONTH);

          double diff = (currentMonth + 1)
              - ((prevMonth == Calendar.DECEMBER) ? 0 : prevMonth + 1);

          if (periodicityHandler.getPeriodicity() == Periodicity.MONTHLY) {
            /*
             * if the difference is more than 1 month then insert a new row
             */
            if (diff > 1.5) {
              if (!periodicityHandler.isDateBased()
                  || !periodicityHandler.dateInSkipList(new Date(
                      (long) prevInst.value(timeIndex)))) {
                Instance newInst = makeInstance(prevInst.value(timeIndex),
                    periodicityHandler, toInsert.numAttributes(), timeIndex);
                Date candidate = new Date((long) newInst.value(timeIndex));
                if (!periodicityHandler.dateInSkipList(candidate)
                    && candidate.getTime() < toInsert.instance(current).value(
                        timeIndex)) {
                  newInst.setDataset(toInsert);
                  toInsert.add(current, newInst);
                  addToMissingReport(newInst.value(timeIndex),
                      periodicityHandler, missingReport);
                }
              }
            }
          } else if (periodicityHandler.getPeriodicity() == Periodicity.QUARTERLY) {
            /*
             * if the difference is more than 1 quarter (3 months) then insert a
             * new row
             */
            if (diff > 4.5) {
              if (!periodicityHandler.isDateBased()
                  || !periodicityHandler.dateInSkipList(new Date(
                      (long) prevInst.value(timeIndex)))) {
                Instance newInst = makeInstance(prevInst.value(timeIndex),
                    periodicityHandler, toInsert.numAttributes(), timeIndex);
                Date candidate = new Date((long) newInst.value(timeIndex));
                if (!periodicityHandler.dateInSkipList(candidate)
                    && candidate.getTime() < toInsert.instance(current).value(
                        timeIndex)) {
                  toInsert.add(current, newInst);
                  addToMissingReport(newInst.value(timeIndex),
                      periodicityHandler, missingReport);
                }
              }
            }
          }
        }
      }

      if (!toInsert.instance(current).isMissing(timeIndex)) {
        prevInst = toInsert.instance(current);
      }

      current++;
    }

    return null;
  }

  /**
   * Replace missing target values by interpolation. Also replaces missing date
   * values (if a date time stamp has been specified and if possible).
   * 
   * @param toReplace the instances to replace missing target values and time
   *          stamp values
   * @param targets a list of target attributes to check for missing values
   * @param timeStampName the name of the time stamp attribute (or null if there
   *          is no time stamp)
   * @param dateOnly if true, only replace missing date values and not missing
   *          target values (useful for hold-out test sets)
   * @param userHint user-specified hint as to the data periodicity
   * @param skipEntries any skip entries (may be null)
   * @param missingReport a varargs parameter that, if provided, is expected to
   *          be up to two lists of Integers and one list of Strings. The first
   *          list will be populated with the instance numbers (duplicates are
   *          possible) of instances that have missing targets replaced. The
   *          second list will be populated with the instance numbers of
   *          instances that have missing time stamp values replaced. The third
   *          list will be populated with the time stamps for any new instances
   *          that are inserted into the data (i.e. if we detect that there are
   *          "holes" in the data that aren't covered by the skip list entries.
   * 
   * @return the instances with missing targets and (possibly) missing time
   *         stamp values replaced.
   */
  public static Instances replaceMissing(Instances toReplace,
      List<String> targets, String timeStampName, boolean dateOnly,
      Periodicity userHint, String skipEntries, Object... missingReport) {

    Instances result = toReplace;

    Attribute timeStampAtt = null;
    TSLagMaker.PeriodicityHandler detected = null;

    List<Integer> missingTargetList = null;
    List<Integer> missingTimeStampList = null;
    List<String> missingTimeStampRows = null;
    if (missingReport.length > 0) {
      missingTargetList = (List<Integer>) missingReport[0];

      if (missingReport.length == 2) {
        missingTimeStampList = (List<Integer>) missingReport[1];
      }

      if (missingReport.length == 3) {
        missingTimeStampRows = (List<String>) missingReport[2];
      }
    }

    if (timeStampName != null && timeStampName.length() > 0) {
      timeStampAtt = toReplace.attribute(timeStampName);

      // must be a non-artificial time stamp
      if (timeStampAtt != null) {
        detected = weka.classifiers.timeseries.core.TSLagMaker
            .determinePeriodicity(result, timeStampName, userHint);

        // check insertMissing (if periodicity is not UNKNOWN)
        /*
         * If we do this first, then we can interpolate the missing target
         * values that will be created for the rows that get inserted
         */
        if (detected.getPeriodicity() != Periodicity.UNKNOWN) {
          insertMissing(toReplace, timeStampAtt, detected, skipEntries,
              missingTimeStampRows);
        }
      }
    }

    // do a quick check to see if we need to replace any missing values
    boolean ok = true;
    for (int i = 0; i < toReplace.numInstances(); i++) {
      if (toReplace.instance(i).hasMissingValue()) {
        // now check against targets and possibly date
        if (!dateOnly) {
          for (String target : targets) {
            int attIndex = toReplace.attribute(target).index();
            if (toReplace.instance(i).isMissing(attIndex)) {
              ok = false;
              break;
            }
          }
          if (!ok) {
            break; // outer loop
          }
        }

        // check date if necessary
        if (timeStampAtt != null) {
          if (toReplace.instance(i).isMissing(timeStampAtt)) {
            ok = false;
            break;
          }
        }
      }
    }

    if (ok) {
      // nothing to do
      return result;
    }

    // process the target(s) first
    if (!dateOnly) {
      for (String target : targets) {
        if (result.attribute(target) != null) {
          int attIndex = result.attribute(target).index();
          double lastNonMissing = weka.core.Utils.missingValue();

          // We won't handle missing target values at the start or end
          // as experiments with using simple linear regression to fill
          // the missing values that are created by default by the lagging
          // process showed inferior performance compared to just letting
          // Weka take care of it via mean/mode replacement

          for (int i = 0; i < result.numInstances(); i++) {
            Instance current = result.instance(i);
            if (current.isMissing(attIndex)) {
              if (!weka.core.Utils.isMissingValue(lastNonMissing)) {
                // Search forward to the next non missing value (if any)
                double futureNonMissing = weka.core.Utils.missingValue();

                double x2 = 2; // number of x steps (lastNonMissing is at 0 on x
                               // axis)
                for (int j = i + 1; j < result.numInstances(); j++) {
                  if (!result.instance(j).isMissing(attIndex)) {
                    futureNonMissing = result.instance(j).value(attIndex);
                    break;
                  }
                  x2++;
                }

                if (!weka.core.Utils.isMissingValue(futureNonMissing)) {
                  // Now do the linear interpolation
                  double offset = lastNonMissing;
                  double slope = (futureNonMissing - lastNonMissing) / x2;

                  // fill in the missing values
                  for (int j = i; j < i + x2; j++) {
                    if (result.instance(j).isMissing(attIndex)) {
                      double interpolated = (((j - i) + 1) * slope) + offset;

                      result.instance(j).setValue(attIndex, interpolated);
                      if (missingTargetList != null) {
                        missingTargetList.add(new Integer(j + 1));
                      }
                    }
                  }
                }
              } else {
                // won't do anything with start/end missing values
              }
            } else {
              lastNonMissing = current.value(attIndex);
            }
          }
        }
      }
    }

    // now check for missing date values (if necessary)
    if (timeStampAtt != null) {

      int attIndex = timeStampAtt.index(); // result.attribute(timeStampName).index();

      double firstNonMissing = result.instance(0).value(attIndex);
      double previousNonMissing = firstNonMissing;
      int firstNonMissingIndex = -1;
      boolean leadingMissingDates = weka.core.Utils
          .isMissingValue(firstNonMissing);

      for (int i = 0; i < result.numInstances(); i++) {
        Instance current = result.instance(i);

        if (current.isMissing(attIndex)) {
          if (!weka.core.Utils.isMissingValue(previousNonMissing)) {
            double newV = advanceSuppliedTimeValue(previousNonMissing, detected);
            current.setValue(attIndex, newV);
            // previousNonMissing = newV;
            if (missingTimeStampList != null) {
              missingTimeStampList.add(new Integer(i + 1));
            }
          }
        } else if (firstNonMissingIndex == -1) {
          firstNonMissingIndex = i;
          firstNonMissing = current.value(attIndex);
        }
        previousNonMissing = current.value(attIndex);
      }

      if (leadingMissingDates) {
        if (firstNonMissingIndex > 0) {
          for (int i = firstNonMissingIndex - 1; i >= 0; i--) {
            Instance current = result.instance(i);
            double newV = decrementSuppliedTimeValue(firstNonMissing, detected);
            current.setValue(attIndex, newV);
            if (missingTimeStampList != null) {
              missingTimeStampList.add(new Integer(i + 1));
            }
            firstNonMissing = newV;
          }
        }
      }
    }

    return result;
  }

  /**
   * Utility method to advance a supplied time value by one unit.
   * 
   * @param valueToAdvance the time value to advance
   * @param dateBasedPeriodicity the periodicity to use for data arithmetic
   * @return the advanced value or the original value if this lag maker is not
   *         adjusting for trends.
   * 
   */
  public static double advanceSuppliedTimeValue(double valueToAdvance,
      TSLagMaker.PeriodicityHandler dateBasedPeriodicity) {
    return advanceSuppliedTimeValue(valueToAdvance, dateBasedPeriodicity, false);
  }

  /**
   * Utility method to decrement a supplied time value by one unit.
   * 
   * @param valueToDecrement the time value to decrement
   * @param dateBasedPeriodicity the periodicity to use for data arithmetic
   * @return the advanced value or the original value if this lag maker is not
   *         adjusting for trends.
   * 
   */
  public static double decrementSuppliedTimeValue(double valueToDecrement,
      TSLagMaker.PeriodicityHandler dateBasedPeriodicity) {
    return advanceSuppliedTimeValue(valueToDecrement, dateBasedPeriodicity,
        true);
  }

  protected static double advanceSuppliedTimeValue(double valueToAdvance,
      TSLagMaker.PeriodicityHandler dateBasedPeriodicity, boolean decrement) {
    double result = valueToAdvance;
    int sign = (decrement) ? -1 : 1;

    // if (m_adjustForTrends) {
    result = valueToAdvance + dateBasedPeriodicity.deltaTime();// m_deltaTime;
    if (dateBasedPeriodicity.getPeriodicity() != Periodicity.UNKNOWN) {
      Date d = new Date((long) valueToAdvance);
      Calendar c = new GregorianCalendar();
      c.setTime(d);
      do {
        if (dateBasedPeriodicity.getPeriodicity() == Periodicity.YEARLY) {
          c.add(Calendar.YEAR, 1 * sign);
        } else if (dateBasedPeriodicity.getPeriodicity() == Periodicity.QUARTERLY) {
          c.add(Calendar.MONTH, 3 * sign);
        } else if (dateBasedPeriodicity.getPeriodicity() == Periodicity.MONTHLY) {
          c.add(Calendar.MONTH, 1 * sign);
        } else if (dateBasedPeriodicity.getPeriodicity() == Periodicity.WEEKLY) {
          c.add(Calendar.WEEK_OF_YEAR, 1 * sign);
        } else if (dateBasedPeriodicity.getPeriodicity() == Periodicity.DAILY) {
          c.add(Calendar.DAY_OF_YEAR, 1 * sign);
        } else if (dateBasedPeriodicity.getPeriodicity() == Periodicity.HOURLY) {
          c.add(Calendar.HOUR_OF_DAY, 1 * sign);
        }
        result = c.getTimeInMillis();

      } while (dateBasedPeriodicity.dateInSkipList(c.getTime()));
    } else {
      // just add the delta
      do {
        result += (dateBasedPeriodicity.deltaTime() * sign);
      } while (dateBasedPeriodicity.dateInSkipList(new Date((long) result)));
    }
    // }
    return result;
  }
}
