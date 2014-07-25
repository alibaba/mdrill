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
 *    CustomPeriodicTest.java
 *    Copyright (C) 2011 Pentaho Corporation
 */

package weka.classifiers.timeseries.core;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Class that evaluates a supplied date against user-specified
 * date constant fields. Fields that can be tested against include
 * year, month, week of year, week of month, day of year, day of month,
 * day of week, hour of day, minute of hour and second. Wildcard "*"
 * matches any value for a particular field. Each CustomPeriodicTest
 * is made up of one or two test parts. If the first test part's operator
 * is "=", then no second part is necessary. Otherwise the first test part
 * may use > or >= operators and the second test part < or <= operators.
 * Taken together, the two parts define an interval. An optional label
 * may be associated with the interval.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 */
public class CustomPeriodicTest implements Serializable {
  
  /**
   * For serialization
   */
  private static final long serialVersionUID = -1470297629040276248L;  
  
  /**
   * Enum defining inequality operations
   */
  public static enum Operator {
    NONE("None") {
      boolean evaluate(int first, int second) {
        return false;
      }
    },
    EQUALS("=") {
      boolean evaluate(int first, int second) {
        return (first == second);
      }
    },
    GREATER_THAN_OR_EQUAL_TO(">=") {
      boolean evaluate(int first, int second) {
        return (first >= second);
      }
    },
    GREATER_THAN(">") {
      boolean evaluate(int first, int second) {
        return (first > second);
      }
    },
    LESS_THAN_OR_EQUAL_TO("<=") {
      boolean evaluate(int first, int second) {
        return (first <= second);
      }
    },
    LESS_THAN("<") {
      boolean evaluate(int first, int second) {
        return (first < second);
      }
    };
    
    private final String m_stringVal;
    
    abstract boolean evaluate(int first, int second);
    
    Operator(String name) {
      m_stringVal = name;
    }
    
    public String toString() {
      return m_stringVal;
    }
  }
  
  /**
   * Inner class defining one boundary of an interval
   */
  public class TestPart implements Serializable {
    /**
     * For serialization
     */
    private static final long serialVersionUID = -898852333853148631L;

    /** the operator for this bound */
    public Operator m_boundOperator = Operator.NONE;
    
    /** true if this is the upper bound */
    protected boolean m_isUpper = false;
    
    /** date fields */
    public int m_year = -100;
    protected int m_month = -100;
    public int m_week_of_yr = -100;
    public int m_week_of_month = -100;
    public int m_day_of_yr = -100;
    public int m_day_of_month = -100;
    protected int m_day_of_week = -100;
    public int m_hour_of_day = -100;
    public int m_min_of_hour = -100;
    public int m_second = -100;
    
    protected Calendar c = new GregorianCalendar();
    
    /**
     * Set whether this is the upper bound or not.
     * 
     * @param upper true if this is the upper bound.
     */
    public void setIsUpper(boolean upper) {
      m_isUpper = upper;
    }
    
    /**
     * Returns true if this is the upper bound.
     * 
     * @return true if this is the upper bound.
     */
    public boolean isUpper() {
      return m_isUpper;
    }
    
    /**
     * Evaluate the supplied date against this bound. Handles
     * date fields that are cyclic (such as month, day of week etc.)
     * so that intervals such as oct < date < mar evaluate correctly.
     * 
     * @param d the date to test
     * @param other the other bound
     * @return true if the supplied date is within this bound
     */
    public boolean eval(Date d, TestPart other) {
      
      c.setTime(d);
      
      boolean result = true;
      
      if (m_year != -100) {
        result = (result && m_boundOperator.
            evaluate(c.get(Calendar.YEAR), m_year));
      }
      if (m_month != -100) {        
        int monthBound = m_month;
        int val = c.get(Calendar.MONTH);
        
        // are we the upper bound and part of a range?
        if (m_isUpper && other != null 
            && other.m_boundOperator != Operator.EQUALS &&
            other.m_month != -100) {
          
          // translate the upper bound by subtracting lower bound
          monthBound -= other.m_month;
          if (monthBound < 0) {
            monthBound += 12;
          }
          // translate the test value
          val -= other.m_month;
          if (val < 0) {
            val += 12;
          }           
        }
        
        // are we the lower bound and part of a range?
        if (!m_isUpper && m_boundOperator != Operator.EQUALS && other != null &&
            other.m_month != -100) {
          monthBound = 0; // lower bound becomes zero
          val -= m_month; // translate value
          if (val < 0) {
            val += 12;
          }
        }
        
        result = (result && m_boundOperator.
            evaluate(val, monthBound));
      }
      if (m_week_of_yr != -100) {
        int weekBound = m_week_of_yr;        
        int val = c.get(Calendar.WEEK_OF_YEAR);
        
        // are we the upper bound and part of a range?
        if (m_isUpper && other != null 
            && other.m_boundOperator != Operator.EQUALS &&
            other.m_week_of_yr != -100) {
          // translate by subtracting lower bound
          weekBound -= other.m_week_of_yr;
          if (weekBound < 0) {
            weekBound += 52;
          }
          // translate the test value
          val -= other.m_week_of_yr;
          if (val < 0) {
            val += 52;
          }
        }
        
        // are we the lower bound and part of a range?
        if (!m_isUpper && m_boundOperator != Operator.EQUALS && other != null &&
            other.m_week_of_yr != -100) {
          weekBound = 0;
          val -= m_week_of_yr;
          if (val < 0) {
            val += 52;
          }
        }
        
        result = (result && m_boundOperator.
            evaluate(val, weekBound));
      }
      if (m_week_of_month != -100) {
        int weekBound = m_week_of_month;
        int val = c.get(Calendar.WEEK_OF_MONTH);
        
        // are we the upper bound and part of a range?
        if (m_isUpper && other != null 
            && other.m_boundOperator != Operator.EQUALS &&
            other.m_week_of_month != -100) {
          // translate by subtracting lower bound
          weekBound -= other.m_week_of_month;
          if (weekBound < 0) {
            weekBound += 6;
          }
          // translate the test value
          val -= other.m_week_of_month;
          if (val < 0) {
            val += 6;
          }
        }
        
        // are we the lower bound and part of a range?
        if (!m_isUpper && m_boundOperator != Operator.EQUALS && other != null &&
            other.m_week_of_month != -100) {
          weekBound = 0;
          val -= m_week_of_month;
          if (val < 0) {
            val += 6;
          }
        }
        
        result = (result && m_boundOperator.evaluate(val, weekBound));
      }
      if (m_day_of_yr != -100) {
        int dayBound = m_day_of_yr;
        int val = c.get(Calendar.DAY_OF_YEAR);
        
        // are we the upper bound and part of a range?
        if (m_isUpper && other != null 
            && other.m_boundOperator != Operator.EQUALS &&
            other.m_day_of_yr != -100) {
          // translate by subtracting lower bound
          dayBound -= other.m_day_of_yr;
          if (dayBound < 0) {
            dayBound += 365;
          }
          // translate the test value
          val -= other.m_day_of_yr;
          if (val < 0) {
            val += 365;
          }
        }
        
        // are we the lower bound and part of a range?
        if (!m_isUpper && m_boundOperator != Operator.EQUALS && other != null &&
            other.m_day_of_yr != -100) {
          dayBound = 0;
          val -= m_day_of_yr;
          if (val < 0) {
            val += 365;
          }
        }
        
        result = (result && m_boundOperator.
            evaluate(val, dayBound));
      }
      if (m_day_of_month != -100) {
        int dayBound = m_day_of_month;
        int val = c.get(Calendar.DAY_OF_MONTH);
        
        // are we the upper bound and part of a range?
        if (m_isUpper && other != null 
            && other.m_boundOperator != Operator.EQUALS &&
            other.m_day_of_month != -100) {
          // translate by subtracting lower bound
          dayBound -= other.m_day_of_month;
          if (dayBound < 0) {
            dayBound += 31;
          }
          // translate the test value
          val -= other.m_day_of_month;
          if (val < 0) {
            val += 31;
          }
        }
        
        // are we the lower bound and part of a range?
        if (!m_isUpper && m_boundOperator != Operator.EQUALS && other != null &&
            other.m_day_of_month != -100) {
          dayBound = 0;
          val -= m_day_of_month;
          if (val < 0) {
            val += 31;
          }          
        }
        
        result = (result && m_boundOperator.
            evaluate(val, dayBound));
      }
      if (m_day_of_week != -100) {
        int dayBound = m_day_of_week;
        int val = c.get(Calendar.DAY_OF_WEEK);
        
        // are we the upper bound and part of a range?
        if (m_isUpper && other != null 
            && other.m_boundOperator != Operator.EQUALS &&
            other.m_day_of_week != -100) {
          // translate by subtracting lower bound
          dayBound -= other.m_day_of_week;
          if (dayBound < 0) {
            dayBound += 7;
          }
          // translate the test value
          val -= other.m_day_of_week;
          if (val < 0) {
            val += 7;
          }          
        }
        
        // are we the lower bound and part of a range?
        if (!m_isUpper && m_boundOperator != Operator.EQUALS && other != null &&
            other.m_day_of_week != -100) {
          dayBound = 0;
          val -= m_day_of_week;
          if (val < 0) {
            val += 7;
          }
        }
        
        result = (result && m_boundOperator.
            evaluate(val, dayBound));
      }
      if (m_hour_of_day != -100) {
        int hourBound = m_hour_of_day;
        int val = c.get(Calendar.HOUR_OF_DAY);
        
        // are we the upper bound and part of a range?
        if (m_isUpper && other != null 
            && other.m_boundOperator != Operator.EQUALS &&
            other.m_hour_of_day != -100) {
          // translate by subtracting lower bound
          hourBound -= other.m_hour_of_day;
          if (hourBound < 0) {
            hourBound += 24;
          }
          // translate test value
          val -= other.m_hour_of_day;
          if (val < 0) {
            val += 24;
          }
        }
        
        // are we the lower bound and part of a range?
        if (!m_isUpper && m_boundOperator != Operator.EQUALS && other != null &&
            other.m_hour_of_day != -100) {
          hourBound = 0;
          val -= m_hour_of_day;
          if (val < 0) {
            val += 24;
          }          
        }
        
        result = (result && m_boundOperator.
            evaluate(val, hourBound));
      }
      if (m_min_of_hour != -100) {
        int minBound = m_min_of_hour;
        int val = c.get(Calendar.MINUTE);
        
        // are we the upper bound and part of a range?
        if (m_isUpper && other != null 
            && other.m_boundOperator != Operator.EQUALS &&
            other.m_min_of_hour != -100) {
          // translate by subtracting lower bound
          minBound -= other.m_min_of_hour;
          if (minBound < 0) {
            minBound += 60;
          }
          // translate the test value
          val -= other.m_min_of_hour;
          if (val < 0) {
            val += 60;
          }          
        }
        
        // are we the lower bound and part of a range?
        if (!m_isUpper && m_boundOperator != Operator.EQUALS && other != null &&
            other.m_min_of_hour != -100) {
          minBound = 0;
          val -= m_min_of_hour;
          if (val < 0) {
            val += 60;
          }
        }
        
        result = (result && m_boundOperator.
            evaluate(val, minBound));
      }
      if (m_second != -100) {
        int secBound = m_second;
        int val = c.get(Calendar.SECOND);
        
        // are we the upper bound and part of a range?
        if (m_isUpper && other != null 
            && other.m_boundOperator != Operator.EQUALS &&
            other.m_second != -100) {
          // translate by subtracting lower bound
          secBound -= other.m_second;
          if (secBound < 0) {
            secBound += 60;
          }
          // translate the test value
          val -= other.m_second;
          if (val < 0) {
            val += 60;
          }          
        }
        
        // are we the lower bound and part of a range?
        if (!m_isUpper && m_boundOperator != Operator.EQUALS && other != null &&
            other.m_second != -100) {
          secBound = 0;
          val -= m_second;
          if (val < 0) {
            val += 60;
          }
        }
        
        result = (result && m_boundOperator.
            evaluate(val, secBound));
      }
            
      return result;
    }
    
    /**
     * Provides a textual representation of this test bound
     * 
     * @return a textual description of this test bound
     */
    public String toString() {
      StringBuffer result = new StringBuffer();
      result.append(m_boundOperator.toString());
      
      if (m_year != -100) {
        result.append("" + m_year);
      } else {
        result.append("*");
      }
      result.append(":");
      if (m_month != -100) {
        result.append(getMonthString());
      } else {
        result.append("*");
      }
      result.append(":");
      if (m_week_of_yr != -100) {
        result.append("" + m_week_of_yr);
      } else {
        result.append("*");
      }
      result.append(":");
      if (m_week_of_month != -100) {
        result.append("" + m_week_of_month);
      } else {
        result.append("*");
      }
      result.append(":");
      if (m_day_of_yr != -100) {
        result.append("" + m_day_of_yr);
      } else {
        result.append("*");
      }
      result.append(":");
      if (m_day_of_month != -100) {
        result.append("" + m_day_of_month);
      } else {
        result.append("*");
      }
      result.append(":");
      if (m_day_of_week != -100) {
        result.append(getDayString());
      } else {
        result.append("*");
      }
      result.append(":");
      if (m_hour_of_day != -100) {
        result.append("" + m_hour_of_day);
      } else {
        result.append("*");
      }
      result.append(":");
      if (m_min_of_hour != -100) {
        result.append("" + m_min_of_hour);
      } else {
        result.append("*");
      }
      result.append(":");
      if (m_second != -100) {
        result.append("" + m_second);
      } else {
        result.append("*");
      }
      
      return result.toString();
    }
    
    /**
     * Set the operator for this bound
     * 
     * @param operator as a String
     */
    public void setOperator(String operator) {
      for (Operator t : Operator.values()) {
        if (operator.equals(t.toString())) {
          m_boundOperator = t;
          break;
        }
      }
    }
    
    /**
     * Set the year for this bound.
     * 
     * @param year the year or "*" to match any value.
     */
    public void setYear(String year) {
      if (year.equals("*")) {
        m_year = -100;
      } else {
        m_year = Integer.parseInt(year);
      }
    }
    
    /**
     * Set the week of the year for this bound.
     * 
     * @param week the week of the year or "*" to match any value.
     */
    public void setWeekOfYear(String week) {
      if (week.equals("*")) {
        m_week_of_yr = -100;
      } else {
        m_week_of_yr = Integer.parseInt(week);
      }
    }
    
    /**
     * Set the week of the month for this bound.
     * 
     * @param week the week of the month or "*" to match any value.
     */
    public void setWeekOfMonth(String week) {
      if (week.equals("*")) {
        m_week_of_yr = -100;
      } else {
        m_week_of_month = Integer.parseInt(week);
      }
    }
    
    /**
     * Set the day of the year for this bound.
     * 
     * @param dayOfYear the day of the year or "*" to match any value.
     */
    public void setDayOfYear(String dayOfYear) {
      if (dayOfYear.equals("*")) {
        m_day_of_yr = -100;
      } else {
        m_day_of_yr = Integer.parseInt(dayOfYear);
      }
    }
    
    /**
     * Set the day of the month for this bound.
     * 
     * @param dayOfMonth the day of the month or "*" to match any value.
     */
    public void setDayOfMonth(String dayOfMonth) {
      if (dayOfMonth.equals("*")) {
        m_day_of_month = -100;
      } else {
        m_day_of_month = Integer.parseInt(dayOfMonth);
      }
    }
    
    /**
     * Set the month for this bound.
     * 
     * @param month the month or "*" to match any value.
     */
    public void setMonth(String month) {
      m_month = parseMonth(month);
    }
    
    /**
     * Set the day of the week for this bound.
     * 
     * @param dayOfWeek the day of the week or "*" to match any value.
     */
    public void setDayOfWeek(String dayOfWeek) {
      m_day_of_week = parseDay(dayOfWeek);
    }
    
    /**
     * Set the hour of the day for this bound.
     * 
     * @param hourOfDay the hour of the day or "*" to match any value.
     */
    public void setHourOfDay(String hourOfDay) {
      if (hourOfDay.equals("*")) {
        m_hour_of_day = -100;
      } else {
        m_hour_of_day = Integer.parseInt(hourOfDay);
      }
    }
    
    /**
     * Set the minute of the hour for this bound.
     * 
     * @param min the minute of the hour or "*" to match any value.
     */
    public void setMinuteOfHour(String min) {
      if (min.equals("*")) {
        m_min_of_hour = -100;
      } else {
        m_min_of_hour = Integer.parseInt(min);
      }
    }
    
    /**
     * Set the second for this bound.
     * 
     * @param second the second or "*" to match any value.
     */
    public void setSecond(String second) {
      if (second.equals("*")) {
        m_second = -100;
      } else {
        m_second = Integer.parseInt(second);
      }
    }
    
    /**
     * Get the day of the week as a string.
     * 
     * @return the day of the week.
     */
    public String getDayString() {
      
      if (m_day_of_week == Calendar.SUNDAY) {
        return "sun";
      }
      if (m_day_of_week == Calendar.MONDAY) {
        return "mon";
      }
      if (m_day_of_week == Calendar.TUESDAY) {
        return "tue";
      }
      if (m_day_of_week == Calendar.WEDNESDAY) {
        return "wed";
      }
      if (m_day_of_week == Calendar.THURSDAY) {
        return "thu";
      }
      if (m_day_of_week == Calendar.FRIDAY) {
        return "fri";
      }
      if (m_day_of_week == Calendar.SATURDAY) {
        return "sat";
      }
      
      return "*";
    }
    
    /**
     * Get the month as a String.
     * 
     * @return the month.
     */
    public String getMonthString() {
      if (m_month == Calendar.JANUARY) {
        return "jan";
      }
      if (m_month == Calendar.FEBRUARY) {
        return "feb";
      }
      if (m_month == Calendar.MARCH) {
        return "mar";
      }
      if (m_month == Calendar.APRIL) {
        return "apr";
      }
      if (m_month == Calendar.MAY) {
        return "may";
      }
      if (m_month == Calendar.JUNE) {
        return "jun";
      }
      if (m_month == Calendar.JULY) {
        return "jul";
      }
      if (m_month == Calendar.AUGUST) {
        return "aug";
      }
      if (m_month == Calendar.SEPTEMBER) {
        return "sep";
      }
      if (m_month == Calendar.OCTOBER) {
        return "oct";
      }
      if (m_month == Calendar.NOVEMBER) {
        return "nov";
      }
      if (m_month == Calendar.DECEMBER) {
        return "dec";
      }
      
      return "*";
    }
  }
  
  /** the upper bound */
  protected TestPart m_upperTest;
  
  /** the lower bound */
  protected TestPart m_lowerTest;
  
  /** the (optional) label for this interval */
  protected String m_label;
  
  /**
   * Constructor.
   * 
   * @param theTest the test definition as a String
   * @throws IllegalArgumentException if the test can't be parsed or is 
   * ill defined.
   */
  public CustomPeriodicTest(String theTest) throws IllegalArgumentException {
    setTest(theTest);
  }
  
  /**
   * Get the lower bound test
   * 
   * @return the lower bound test
   */
  public TestPart getLowerTest() {
    return m_lowerTest;
  }
  
  /**
   * Get the upper bound test
   * 
   * @return the upper bound test.
   */
  public TestPart getUpperTest() {
    return m_upperTest;
  }
  
  /**
   * Set the test as a String
   * 
   * @param theTest the test
   * @throws IllegalArgumentException if the test can't be parsed or is
   * ill defined.
   */
  public void setTest(String theTest) throws IllegalArgumentException {
    /*// check for brackets first
    if (theTest.indexOf('(') < 0 || theTest.indexOf(')') < 0) {
      throw new IllegalArgumentException("Custom periodic test needs enclosing " +
      		"parenthesis!");
    } */
      
/*    // split off brackets
    theTest = theTest.substring(1, theTest.indexOf(')') - 1); */
    theTest = theTest.trim();
    
    if (theTest.indexOf(' ') > 0) {
      String[] parts = theTest.split(" ");
      
      m_lowerTest = parseTest(parts[0]);
      m_upperTest = parseTest(parts[1]);
      m_upperTest.setIsUpper(true);
      
      if (m_upperTest.m_boundOperator == m_lowerTest.m_boundOperator) {
        throw new IllegalArgumentException("Doesn't make sense to define " +
        		"an upper and lower test that uses the same operator");
      }      
    } else {
      m_lowerTest = parseTest(theTest);
    }
  }
  
  /**
   * Get the optional label for this interval.
   * 
   * @return the label for this interval
   */
  public String getLabel() {
    return m_label;
  }
  
  /**
   * Set the label for this interval
   * 
   * @param label the label for this interval
   */
  public void setLabel(String label) {
    m_label = label;
  }
  
  /**
   * Evaluate the supplied date with respect to this custom periodic
   * test interval
   * 
   * @param d the date to test
   * @return true if the date lies within the interval.
   */
  public boolean evaluate(Date d) {
    boolean result = m_lowerTest.eval(d, m_upperTest);
    
    // only evaluate the upper test if there is one and the lower operator
    // is not equals
    if (m_upperTest != null && m_lowerTest.m_boundOperator != Operator.EQUALS) {      
      result = (result && m_upperTest.eval(d, m_lowerTest));
    }
    
    return result;
  }
  
  /**
   * Parses the day of the week as either a number or
   * a string
   * 
   * @param day the day of the week
   * @return the integer index of the day of the week
   * @throws IllegalArgumentException if the supplied day can't be parsed
   */
  private int parseDay(String day) throws IllegalArgumentException {
    // try parsing as a number
    try {
      int result = Integer.parseInt(day);
      return result;
    } catch (NumberFormatException e) {}
    
    // try parsing as a word
    day = day.toLowerCase();
    if (day.equals("sunday") || day.equals("sun")) {
      return Calendar.SUNDAY;
    }
    if (day.equals("monday") || day.equals("mon")) {
      return Calendar.MONDAY;
    }
    if (day.equals("tuesday") || day.equals("tue")) {
      return Calendar.TUESDAY;
    }
    if (day.equals("wednesday") || day.equals("wed")) {
      return Calendar.WEDNESDAY;
    }
    if (day.equals("thursday") || day.equals("thu")) {
      return Calendar.THURSDAY;
    }
    if (day.equals("friday") || day.equals("fri")) {
      return Calendar.FRIDAY;
    }
    if (day.equals("saturday") || day.equals("sat")) {
      return Calendar.SATURDAY;
    }
    if (day.equals("*")) {
      return -100;
    }
    throw new IllegalArgumentException("Can't parse month!");
  }
  
  /**
   * Parses the month as either a number or
   * a string
   * 
   * @param month the month of the year
   * @return the integer index of the month
   * @throws IllegalArgumentException if the supplied month can't be parsed
   */
  private int parseMonth(String month) throws IllegalArgumentException {
    // try parsing as a number
    try {
      int result = Integer.parseInt(month);
      return result;
    } catch (NumberFormatException e) {}
    
    // try parsing as a word
    month = month.toLowerCase();
    if (month.equals("january") || month.equals("jan")) {
      return Calendar.JANUARY;
    }
    if (month.equals("february") || month.equals("feb")) {
      return Calendar.FEBRUARY;
    }
    if (month.equals("march") || month.equals("mar")) {
      return Calendar.MARCH;
    }
    if (month.equals("april") || month.equals("apr")) {
      return Calendar.APRIL;
    }
    if (month.equals("may")) {
      return Calendar.MAY;
    }
    if (month.equals("june") || month.equals("jun")) {
      return Calendar.JUNE;
    }
    if (month.equals("july") || month.equals("jul")) {
      return Calendar.JULY;
    }
    if (month.equals("august") || month.equals("aug")) {
      return Calendar.AUGUST;
    }
    if (month.equals("september") || month.equals("sep")) {
      return Calendar.SEPTEMBER;
    }
    if (month.equals("october") || month.equals("oct")) {
      return Calendar.OCTOBER;
    }
    if (month.equals("november") || month.equals("nov")) {
      return Calendar.NOVEMBER;
    }
    if (month.equals("december") || month.equals("dec")) {
      return Calendar.DECEMBER;
    }
    if (month.equals("*")) {
      return -100;
    }
    
    throw new IllegalArgumentException("Can't parse month!");
  }
  
  /**
   * Parses the textual definition of one bound
   * 
   * @param aTest the bound to parse
   * @return a TestPart object encapsulating the bound
   * @throws IllegalArgumentException if the test can't be parsed or
   * is ill defined
   */
  protected TestPart parseTest(String aTest) throws IllegalArgumentException {
    // check for a label
    if (aTest.indexOf('/') > 0) {
      String[] parts = aTest.split("/");
      aTest = parts[0].trim();
      m_label = parts[1].trim();
    }     
    
    aTest = aTest.trim();
    TestPart newTest = new TestPart();
    
    if (aTest.charAt(0) == '=') {
      newTest.m_boundOperator = Operator.EQUALS;
      aTest = aTest.substring(1, aTest.length());
    } else {
      if (aTest.charAt(0) == '>') {
        newTest.m_boundOperator = Operator.GREATER_THAN;
        aTest = aTest.substring(1, aTest.length());
        if (aTest.charAt(0) == '=') {
          newTest.m_boundOperator = Operator.GREATER_THAN_OR_EQUAL_TO;
          aTest = aTest.substring(1, aTest.length());
        }
      } else if (aTest.charAt(0) == '<') {
        newTest.m_boundOperator = Operator.LESS_THAN;
        aTest = aTest.substring(1, aTest.length());
        if (aTest.charAt(0) == '=') {
          newTest.m_boundOperator = Operator.LESS_THAN_OR_EQUAL_TO;
          aTest = aTest.substring(1, aTest.length());
        }
      }
    }
    
    String[] parts = aTest.split(":");
    // do we have the right number of parts?
    if (parts.length != 10) {
/*      System.err.println(aTest);
      System.err.println("-- Num parts " + parts.length);
      for (int z = 0; z < parts.length; z++) {
        System.err.println(parts[z]);
      }*/
      throw new IllegalArgumentException("Test does not contain 10 parts!");
    }
    if (parts[0].trim().length() > 0 && parts[0].charAt(0) != '*') {
      newTest.m_year = Integer.parseInt(parts[0].trim());
    }
    if (parts[1].trim().length() > 0 && parts[1].charAt(0) != '*') {
      newTest.m_month = parseMonth(parts[1].trim());
    }
    if (parts[2].trim().length() > 0 && parts[2].charAt(0) != '*') {
      newTest.m_week_of_yr = Integer.parseInt(parts[2].trim());
    }
    if (parts[3].trim().length() > 0 && parts[3].charAt(0) != '*') {
      newTest.m_week_of_month = Integer.parseInt(parts[3].trim());
    }
    if (parts[4].trim().length() > 0 && parts[4].charAt(0) != '*') {
      newTest.m_day_of_yr = Integer.parseInt(parts[4].trim());
    }
    if (parts[5].trim().length() > 0 && parts[5].charAt(0) != '*') {
      newTest.m_day_of_month = Integer.parseInt(parts[5].trim());
    }
    if (parts[6].trim().length() > 0 && parts[6].charAt(0) != '*') {
      newTest.m_day_of_week = parseDay(parts[6].trim());
    }
    if (parts[7].trim().length() > 0 && parts[7].charAt(0) != '*') {
      newTest.m_hour_of_day = Integer.parseInt(parts[7].trim());
    }
    if (parts[8].trim().length() > 0 && parts[8].charAt(0) != '*') {
      newTest.m_min_of_hour = Integer.parseInt(parts[8].trim());
    }
    if (parts[9].trim().length() > 0 && parts[9].charAt(0) != '*') {
      newTest.m_second = Integer.parseInt(parts[9].trim());
    }
    
    return newTest;
  }
  
  /**
   * Returns a textual description of this test
   * 
   * @return a textual description of this test
   */
  public String toString() {
    String result = m_lowerTest.toString();
    if (m_upperTest != null && m_lowerTest.m_boundOperator != Operator.EQUALS) {
      result += (" " + m_upperTest.toString());
    }
    if (m_label != null && m_label.length()> 0) {
      result += "/" + m_label;
    }
    return result;
  }
  
  /**
   * Main method for testing this class
   * 
   * @param args command line arguments - first element should
   * contain a textual definition of a test
   */
  public static void main(String[] args) {
    try {
      if (args.length != 1) {
        System.err.println("Usage: CustomPeriodicTest \"TestPart TestPart\"");
        System.exit(1);
      }
      CustomPeriodicTest test = new CustomPeriodicTest(args[0]);
      System.out.println("CustomPeriodicTest: \n\n" + test.toString());
    } catch (Exception ex) {
      ex.printStackTrace();      
    }
  }
}
