/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
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
 *    CustomPeriodicTestEditor.java
 *    Copyright (C) 2011 Pentaho Corporation
 */


package weka.classifiers.timeseries.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;


import weka.classifiers.timeseries.core.CustomPeriodicTest;

/**
 * Provides an editor for a single interval from a custom periodic test. See
 * the javadoc for CustomPeriodicTest for information on the format that a
 * single test interval takes.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 */
public class CustomPeriodicTestEditor extends JPanel {
  
  /** For serialization */
  private static final long serialVersionUID = 8515802184515862677L;
  
  /** Combo box for the operator */
  protected JComboBox m_operator = new JComboBox();
  
  /** Combo box for the year field */
  protected JComboBox m_year = new JComboBox();
  
  /** Combo box for the month field */
  protected JComboBox m_month = new JComboBox();
  
  /** Combo box for the week of the year field */
  protected JComboBox m_week_of_yr = new JComboBox();
  
  /** Combo box for the week of the month field */
  protected JComboBox m_week_of_month = new JComboBox();
  
  /** Combo box for the day of the year field */
  protected JComboBox m_day_of_yr = new JComboBox();
  
  /** Combo box for the day of the month field */
  protected JComboBox m_day_of_month = new JComboBox();
  
  /** Combo box for the day of the week field */
  protected JComboBox m_day_of_week = new JComboBox();
  
  /** Combo box for the hour of the day field */
  protected JComboBox m_hour_of_day = new JComboBox();
  
  /** Combo box for the minute field */
  protected JComboBox m_min_of_hour = new JComboBox();
  
  /** Combo box for the second field */
  protected JComboBox m_second = new JComboBox();

  /** true if we are editing the upper bound */
  protected boolean m_right;
  
  /** The test to edit */
  protected CustomPeriodicTest m_testToEdit = 
    new CustomPeriodicTest(">*:*:*:*:*:*:*:*:*:*");
  
  /** The bound that we are editing */
  protected CustomPeriodicTest.TestPart m_partToEdit;
  
  /** Support for property change events */
  protected PropertyChangeSupport m_support = new PropertyChangeSupport(this);
  
  /**
   * Constructor
   * 
   * @param right true if we should edit the upper bound of the test
   */
  public CustomPeriodicTestEditor(boolean right) {
    setLayout(new GridLayout(1,10));
    
    /*SpinnerNumberModel snm = new SpinnerNumberModel();
    snm.setValue(1); snm.setMinimum(1); snm.setMaximum(52);
    m_week_of_yr = new JSpinner(snm);
    snm = new SpinnerNumberModel();
    snm.setValue(1); snm.setMinimum(1); snm.setMaximum(5);
    m_week_of_month = new JSpinner(snm);
    snm = new SpinnerNumberModel();
    snm.setValue(1); snm.setMinimum(1); snm.setMaximum(365);
    m_day_of_yr = new JSpinner(snm);
    snm.setValue(1); snm.setMinimum(1); snm.setMaximum(31);
    m_day_of_month = new JSpinner(snm);
    snm = new SpinnerNumberModel();
    snm.setValue(1); snm.setMinimum(1); snm.setMaximum(23);
    m_hour_of_day = new JSpinner(snm);
    snm = new SpinnerNumberModel();
    snm.setValue(1); snm.setMinimum(1); snm.setMaximum(59);
    m_min_of_hour = new JSpinner(snm);
    snm = new SpinnerNumberModel();
    snm.setValue(1); snm.setMinimum(1); snm.setMaximum(59);
    m_second = new JSpinner(snm);*/
    
    add(m_operator);
    add(m_year); add(m_month); add(m_week_of_yr); add(m_week_of_month);
    add(m_day_of_yr); add(m_day_of_month); add(m_day_of_week);
    add(m_hour_of_day); add(m_min_of_hour); add(m_second);
    
    m_right = right;
    if (m_right) {
      m_testToEdit = new CustomPeriodicTest("<*:*:*:*:*:*:*:*:*:*");
    }
    m_partToEdit = (m_right) ? m_testToEdit.getUpperTest() : m_testToEdit.getLowerTest();
    
    setupCombos();
    
    
    m_operator.setToolTipText("Operator");
    m_operator.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_partToEdit != null){
          m_partToEdit.setOperator(m_operator.getSelectedItem().toString());
          m_support.firePropertyChange("", null, null);
        }
      }
    });
    m_year.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_partToEdit != null){
          m_partToEdit.setYear(m_year.getSelectedItem().toString());
          m_support.firePropertyChange("", null, null);
        }
      }
    }); 
    m_year.setToolTipText("Year");
    m_month.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_partToEdit != null){
          m_partToEdit.setMonth(m_month.getSelectedItem().toString());
          m_support.firePropertyChange("", null, null);
        }
      }
    });        
    m_month.setToolTipText("Month");
    m_week_of_yr.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_partToEdit != null){
          m_partToEdit.setWeekOfYear(m_week_of_yr.getSelectedItem().toString());
          m_support.firePropertyChange("", null, null);
        }
      }
    });
    m_week_of_yr.setToolTipText("Week of the year");
    m_week_of_month.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_partToEdit != null){
          m_partToEdit.setWeekOfMonth(m_week_of_month.getSelectedItem().toString());
          m_support.firePropertyChange("", null, null);
        }
      }
    });
    m_week_of_month.setToolTipText("Week of the month");
    m_day_of_yr.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_partToEdit != null){
          m_partToEdit.setDayOfYear(m_day_of_yr.getSelectedItem().toString());
          m_support.firePropertyChange("", null, null);
        }
      }
    });
    m_day_of_yr.setToolTipText("Day of the year");
    m_day_of_month.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_partToEdit != null){
          m_partToEdit.setDayOfMonth(m_day_of_month.getSelectedItem().toString());
          m_support.firePropertyChange("", null, null);
        }
      }
    });
    m_day_of_month.setToolTipText("Day of the month");
    m_day_of_week.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_partToEdit != null){
          m_partToEdit.setDayOfWeek(m_day_of_week.getSelectedItem().toString());
          m_support.firePropertyChange("", null, null);
        }
      }
    });
    m_day_of_week.setToolTipText("Day of the week");
    m_hour_of_day.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_partToEdit != null){
          m_partToEdit.setHourOfDay(m_hour_of_day.getSelectedItem().toString());
          m_support.firePropertyChange("", null, null);
        }
      }
    });
    m_hour_of_day.setToolTipText("Hour of the day");
    m_min_of_hour.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_partToEdit != null){
          m_partToEdit.setMinuteOfHour(m_min_of_hour.getSelectedItem().toString());
          m_support.firePropertyChange("", null, null);
        }
      }
    });
    m_min_of_hour.setToolTipText("Minute of the hour");
    m_second.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_partToEdit != null){
          m_partToEdit.setSecond(m_second.getSelectedItem().toString());
          m_support.firePropertyChange("", null, null);
        }
      }
    });
    m_second.setToolTipText("Second");
  }
  
  /**
   * Set the test to edit
   * 
   * @param t the CustomPeriodicTest to edit
   */
  public void setTestToEdit(CustomPeriodicTest t) {
    m_testToEdit = t;
    m_partToEdit = (m_right) ? m_testToEdit.getUpperTest() : m_testToEdit.getLowerTest();
    fillGUIElements();
  }
  
  /**
   * Get the test being edited
   * 
   * @return the CustomPeriodicTest being edited
   */
  public CustomPeriodicTest getTestBeingEdited() {
    return m_testToEdit;
  }
  
  /**
   * Initialize GUI elements from the test being edited
   */
  protected void fillGUIElements() {      

    if (m_partToEdit == null) {
      setEnabled(false);
      return;
    }
    
    m_operator.setSelectedItem(m_partToEdit.m_boundOperator.toString());
    m_year.setSelectedItem(numericValueToString(m_partToEdit.m_year));    
    m_month.setSelectedItem(m_partToEdit.getMonthString());        
    m_week_of_yr.setSelectedItem(numericValueToString(m_partToEdit.m_week_of_yr));        
    m_week_of_month.setSelectedItem(numericValueToString(m_partToEdit.m_week_of_month));        
    m_day_of_yr.setSelectedItem(numericValueToString(m_partToEdit.m_day_of_yr));        
    m_day_of_month.setSelectedItem(numericValueToString(m_partToEdit.m_day_of_month));    
    m_day_of_week.setSelectedItem(m_partToEdit.getDayString());    
    m_hour_of_day.setSelectedItem(numericValueToString(m_partToEdit.m_hour_of_day));        
    m_min_of_hour.setSelectedItem(numericValueToString(m_partToEdit.m_min_of_hour));        
    m_second.setSelectedItem(numericValueToString(m_partToEdit.m_second));
    
    /*((SpinnerNumberModel)m_week_of_yr.getModel()).setValue(partToEdit.m_week_of_yr);
    ((SpinnerNumberModel)m_week_of_month.getModel()).setValue(partToEdit.m_week_of_month);
    ((SpinnerNumberModel)m_day_of_yr.getModel()).setValue(partToEdit.m_day_of_yr);
    ((SpinnerNumberModel)m_day_of_month.getModel()).setValue(partToEdit.m_day_of_month); */
    
    /*((SpinnerNumberModel)m_hour_of_day.getModel()).setValue(partToEdit.m_hour_of_day);
    ((SpinnerNumberModel)m_min_of_hour.getModel()).setValue(partToEdit.m_min_of_hour);
    ((SpinnerNumberModel)m_second.getModel()).setValue(partToEdit.m_second); */
  }
  
  /**
   * Converts an integer field value to a string. Values equal to -100
   * indicate match anything "wildcards", represented as "*"
   * 
   * @param val the integer value to convert to a string
   * @return the value converted to a string.
   */
  protected String numericValueToString(int val) {
    if (val == -100) {
      return "*";
    }
    return "" + val;
  }
  
  /**
   * Set up combo boxes
   */
  protected void setupCombos() {
    Vector<String> inequalities = new Vector<String>();
    
    if (!m_right) {
      inequalities.add(">"); inequalities.add(">="); inequalities.add("=");
    } else {
      inequalities.add("<");
      inequalities.add("<=");
    }
    
    m_operator.setModel(new DefaultComboBoxModel(inequalities));
    
    Vector<String> years = new Vector<String>();
    years.add("*");
    for (int i = 1900; i <=2100; i++) {
      years.add("" + i);
    }
    m_year.setModel(new DefaultComboBoxModel(years));
    
    Vector<String> months = new Vector<String>();
    months.add("*");
    months.add("jan"); months.add("feb"); months.add("mar"); months.add("apr");
    months.add("may"); months.add("jun"); months.add("jul"); months.add("aug");
    months.add("sep"); months.add("oct"); months.add("nov"); months.add("dec");
    m_month.setModel(new DefaultComboBoxModel(months));
    
    Vector<String> days = new Vector<String>();
    days.add("*");
    days.add("sun"); days.add("mon"); days.add("tue"); days.add("wed"); 
    days.add("thu"); days.add("fri"); days.add("sat");
    m_day_of_week.setModel(new DefaultComboBoxModel(days));
    
    Vector<String> week = new Vector<String>();
    week.add("*");
    for (int i = 1; i <= 52; i++) {
      week.add("" + i);
    }
    m_week_of_yr.setModel(new DefaultComboBoxModel(week));
    week = new Vector<String>();
    week.add("*");
    for (int i = 0; i <= 6; i++) {
      week.add("" + i);
    }
    m_week_of_month.setModel(new DefaultComboBoxModel(week));
    days = new Vector<String>();
    days.add("*");
    for (int i = 1; i <= 365; i++) {
      days.add("" + i);
    }
    m_day_of_yr.setModel(new DefaultComboBoxModel(days));
    days = new Vector<String>();
    days.add("*");
    for (int i = 1; i <= 31; i++) {
      days.add("" + i);
    }
    m_day_of_month.setModel(new DefaultComboBoxModel(days));
    Vector<String> hour = new Vector<String>();
    hour.add("*");
    for (int i = 0; i < 24; i++) {
      hour.add("" + i);
    }
    m_hour_of_day.setModel(new DefaultComboBoxModel(hour));
    Vector<String> minute = new Vector<String>();
    minute.add("*");
    for (int i = 0; i < 60; i++) {
      minute.add("" + i);
    }
    m_min_of_hour.setModel(new DefaultComboBoxModel(minute));
    Vector<String> second = new Vector<String>();
    second.add("*");
    for (int i = 0; i < 60; i++) {
      second.add("" + i);
    }
    m_second.setModel(new DefaultComboBoxModel(second));
  }
  
  /**
   * Set the enabled status of all the GUI elements
   * 
   * @param enabled true if all elements should be enabled
   */
  public void setEnabled(boolean enabled) {
    m_operator.setEnabled(enabled);
    m_year.setEnabled(enabled);
    m_month.setEnabled(enabled);
    m_week_of_yr.setEnabled(enabled);
    m_week_of_month.setEnabled(enabled);
    m_day_of_yr.setEnabled(enabled);
    m_day_of_month.setEnabled(enabled);
    m_day_of_week.setEnabled(enabled);
    m_hour_of_day.setEnabled(enabled);
    m_min_of_hour.setEnabled(enabled);
    m_second.setEnabled(enabled);    
  }
  
  /**
   * Adds a PropertyChangeListener who will be notified of value changes.
   *
   * @param l a value of type 'PropertyChangeListener'
   */
  public void addPropertyChangeListener(PropertyChangeListener l) {
    m_support.addPropertyChangeListener(l);
  }

  /**
   * Removes a PropertyChangeListener.
   *
   * @param l a value of type 'PropertyChangeListener'
   */
  public void removePropertyChangeListener(PropertyChangeListener l) {
    m_support.removePropertyChangeListener(l);
  }
  
  /**
   * Main method for testing this class. Expects a single quoted argument containing
   * the textual definition of one bound of a periodic test.
   * 
   * @param args
   */
  public static void main(String[] args) {
    try {
      CustomPeriodicTestEditor ed = new CustomPeriodicTestEditor(false);
      final CustomPeriodicTest test = new CustomPeriodicTest(">1965:aug:*:*:*:28:*:*:*:*");
      ed.setTestToEdit(test);
      final javax.swing.JFrame jf =
        new javax.swing.JFrame("Periodic test editor");
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(ed, BorderLayout.CENTER);
      jf.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
          System.out.println(test.toString());
          jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();      
    }
  }
}
