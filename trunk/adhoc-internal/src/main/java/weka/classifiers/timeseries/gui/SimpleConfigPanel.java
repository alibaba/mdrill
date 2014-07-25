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
 *    SimpleConfigPanel.java
 *    Copyright (C) 2010 Pentaho Corporation
 */

package weka.classifiers.timeseries.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import weka.classifiers.timeseries.WekaForecaster;
import weka.classifiers.timeseries.core.TSLagMaker;
import weka.classifiers.timeseries.eval.TSEvaluation;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.AttributeSelectionPanel;

/**
 * Class that renders a simple configuration panel for configuring a time series
 * forecaster.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 50889 $
 */
public class SimpleConfigPanel extends JPanel {

  /**
   * For serialization
   */
  private static final long serialVersionUID = 4339062970124604791L;

  /** The training instances to operate on */
  protected Instances m_instances;

  /** The forecaster to configure */
  protected WekaForecaster m_forecaster;

  /**
   * Holds the header of the training instances after all but numeric attributes
   * are removed (for target selection purposes
   */
  protected Instances m_targetHeader;

  /** Combo box for selecting the time stamp attribute */
  protected JComboBox m_timeStampCombo = new JComboBox();

  /** Combo box for selecting the periodicity */
  protected JComboBox m_periodicityCombo = new JComboBox();

  /** Panel for selecting targets to forecast */
  protected AttributeSelectionPanel m_targetPanel = new AttributeSelectionPanel();

  /** Checkbox for computing confidence intervals */
  protected JCheckBox m_computeConfidence = new JCheckBox();

  /** Spinner for selecting the number of steps to forecast */
  protected JSpinner m_horizonSpinner;

  /** Spinner for selecting the confidence interval */
  protected JSpinner m_confidenceLevelSpinner;

  /** Checkbox for selecting whether to perform evaluation */
  protected JCheckBox m_performEvaluation = new JCheckBox();

  /** A reference to the advanced config panel */
  protected AdvancedConfigPanel m_advancedConfig;

  /**
   * Text field for entering date time stamp values that should be "skipped" -
   * i.e. not considered as a time step
   */
  protected JTextField m_skipText = new JTextField(18);

  /** A reference to the parent panel */
  protected ForecastingPanel m_parentPanel;

  /**
   * Constructor
   * 
   * @param parent the parent ForecastingPanel
   */
  public SimpleConfigPanel(ForecastingPanel parent) {

    m_parentPanel = parent;
    setLayout(new BorderLayout());

    JPanel colSelect = new JPanel();
    colSelect.setLayout(new BorderLayout());
    colSelect.setBorder(BorderFactory.createTitledBorder("Target Selection"));
    JPanel tempHolder1 = new JPanel();
    tempHolder1.setLayout(new BorderLayout());
    tempHolder1.add(m_targetPanel, BorderLayout.NORTH);
    colSelect.add(tempHolder1, BorderLayout.NORTH);

    m_targetPanel.setPreferredScrollableViewportSize(new Dimension(250, 80));
    SpinnerNumberModel snm = new SpinnerNumberModel();
    snm.setValue(1);
    snm.setMinimum(1);
    m_horizonSpinner = new JSpinner(snm);
    Dimension spinD = m_horizonSpinner.getPreferredSize();
    spinD = new Dimension((int) (spinD.getWidth() * 1.5),
        (int) spinD.getHeight());
    m_horizonSpinner.setPreferredSize(spinD);
    JPanel spinnerHolder = new JPanel();
    spinnerHolder.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
    spinnerHolder.setLayout(new BorderLayout());
    spinnerHolder.add(m_horizonSpinner, BorderLayout.EAST);
    spinnerHolder.add(new JLabel("Number of time units to forecast ",
        JLabel.LEFT), BorderLayout.CENTER);

    // tempHolder1.add(spinnerHolder, BorderLayout.CENTER);
    // JPanel spacer = new JPanel();
    // spacer.setMinimumSize(spinD);
    // tempHolder1.add(spacer, BorderLayout.SOUTH);

    add(colSelect, BorderLayout.CENTER);
    Box comboHolder = new Box(BoxLayout.PAGE_AXIS);
    comboHolder.add(spinnerHolder);
    JPanel timeHolder = new JPanel();
    timeHolder.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
    timeHolder.setLayout(new BorderLayout());
    // timeHolder.setBorder(BorderFactory.createTitledBorder("Time stamp"));
    timeHolder.add(new JLabel("Time stamp ", JLabel.RIGHT), BorderLayout.WEST);
    timeHolder.add(m_timeStampCombo, BorderLayout.EAST);
    comboHolder.add(timeHolder);
    JPanel periodicityHolder = new JPanel();
    periodicityHolder.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
    periodicityHolder.setLayout(new BorderLayout());
    // periodicityHolder.setBorder(BorderFactory.createTitledBorder("Periodicity"));
    periodicityHolder.add(new JLabel("Periodicity", JLabel.RIGHT),
        BorderLayout.WEST);
    periodicityHolder.add(m_periodicityCombo, BorderLayout.EAST);
    comboHolder.add(periodicityHolder);
    m_periodicityCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        checkSkipEnabledStatus();
        checkPeriodicity(null);
      }
    });

    //
    JPanel skipPanel = new JPanel();
    skipPanel.setLayout(new BorderLayout());
    skipPanel.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
    String skipTipText = "<html>Set date time stamp values that should be 'skipped'<br>"
        + "i.e. times that shouldn't count as a time step increment.<br><br>"
        + "E.g. financial trading does not occur on the weekend, so the <br>"
        + "difference between Friday and the following Monday is actually one<br>"
        + "time step (not three).<br><br>Examples:<br><br>"
        + "\"sat,weekend,aug,2011-01-11@yyyy-MM-dd\"</html>";
    JLabel skipLab = new JLabel("Skip list", JLabel.RIGHT);
    skipLab.setToolTipText(skipTipText);
    skipPanel.add(skipLab, BorderLayout.WEST);
    skipPanel.add(m_skipText, BorderLayout.EAST);
    m_skipText.setToolTipText(skipTipText);
    comboHolder.add(skipPanel);

    m_computeConfidence.setHorizontalTextPosition(SwingConstants.LEFT);

    JPanel confHolder = new JPanel();
    confHolder.setLayout(new BorderLayout());
    confHolder.add(new JLabel("Confidence intervals", JLabel.RIGHT),
        BorderLayout.WEST);
    confHolder.add(m_computeConfidence, BorderLayout.EAST);
    comboHolder.add(confHolder);

    JPanel confLevHolder = new JPanel();
    confLevHolder.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
    confLevHolder.setLayout(new BorderLayout());
    final JLabel levelLab = new JLabel("Level (%) ", JLabel.RIGHT);
    levelLab.setEnabled(false);
    snm = new SpinnerNumberModel();
    snm.setValue(95);
    snm.setMinimum(1);
    snm.setMaximum(99);
    m_confidenceLevelSpinner = new JSpinner(snm);
    m_confidenceLevelSpinner.setEnabled(false);
    confLevHolder.add(m_confidenceLevelSpinner, BorderLayout.EAST);
    confLevHolder.add(levelLab, BorderLayout.CENTER);

    comboHolder.add(confLevHolder);

    m_computeConfidence.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean enable = m_computeConfidence.isSelected();
        levelLab.setEnabled(enable);
        m_confidenceLevelSpinner.setEnabled(enable);
      }
    });

    JPanel evalHolder = new JPanel();
    evalHolder.setLayout(new BorderLayout());
    evalHolder.add(new JLabel("Perform evaluation", JLabel.RIGHT),
        BorderLayout.WEST);
    m_performEvaluation.setHorizontalAlignment(SwingConstants.LEFT);
    evalHolder.add(m_performEvaluation, BorderLayout.EAST);
    comboHolder.add(evalHolder);
    m_performEvaluation.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_advancedConfig.m_trainingCheckBox.setSelected(m_performEvaluation
            .isSelected());
        // don't enable test set evaluation, only disable
        if (!m_performEvaluation.isSelected()) {
          m_advancedConfig.m_holdoutCheckBox.setSelected(false);
        }
      }
    });

    // comboHolder.add(m_horizon);

    JPanel temp = new JPanel();
    temp.setLayout(new BorderLayout());
    temp.setBorder(BorderFactory.createTitledBorder("Parameters"));

    temp.add(comboHolder, BorderLayout.NORTH);
    add(temp, BorderLayout.EAST);

    m_timeStampCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_advancedConfig != null) {
          m_advancedConfig.updateDateDerivedPanel();
          checkSkipEnabledStatus();
        }
      }
    });

    /*
     * m_periodicityCombo.addActionListener(new ActionListener() { public void
     * actionPerformed(ActionEvent e) { if (m_forecaster != null) {
     * 
     * } } });
     */

    // m_targetPanel
    /*
     * Dimension d = tempHolder1.getPreferredSize(); Dimension d2 =
     * spinnerHolder.getPreferredSize(); tempHolder1.setMinimumSize(new
     * Dimension(tempHolder1.getPreferredSize().width, d.height + d2.height));
     * tempHolder1.setPreferredSize(new
     * Dimension(tempHolder1.getPreferredSize().width, d.height + d2.height));
     */
  }

  private void checkSkipEnabledStatus() {
    boolean enable = false;
    if (m_instances != null) {
      if (m_timeStampCombo.getSelectedItem() != null) {
        String timeName = m_timeStampCombo.getSelectedItem().toString();
        Attribute timeAtt = m_instances.attribute(timeName);
        if (timeAtt != null) {
          if (m_periodicityCombo.getSelectedItem() != null) {
            String periodicity = m_periodicityCombo.getSelectedItem()
                .toString();
            enable = (timeAtt.isDate() && !periodicity.equals("<Unknown>") && !periodicity
                .equals("<Detect automatically>"));
          }
        }
      }
    }
    m_skipText.setEnabled(enable);
  }

  /**
   * Get the title for this panel suitable for displaying in a tab.
   * 
   * @return the title for this panel.
   */
  public String getTabTitle() {
    return "Basic configuration";
  }

  /**
   * Get the tool tip for this panel.
   * 
   * @return the tool tip for this panel.
   */
  public String getTabTitleToolTip() {
    return "Basic configuration";
  }

  /**
   * Get the value in the horizon spinner (i.e. the number of steps to forecast)
   * 
   * @return the number of steps to forecast.
   */
  public int getHorizonValue() {
    SpinnerNumberModel snm = (SpinnerNumberModel) m_horizonSpinner.getModel();

    return snm.getNumber().intValue();
  }

  /**
   * Set a reference to the advanced configuration panel.
   * 
   * @param adv a reference to the advanced configuration panel.
   */
  public void setAdvancedConfig(AdvancedConfigPanel adv) {
    m_advancedConfig = adv;
  }

  /**
   * Set the WekaForecaster that is to be configured by this panel.
   * 
   * @param forecaster the WekaForecaster that is to be configured by this
   *          panel.
   */
  public void setForecaster(WekaForecaster forecaster) {
    m_forecaster = forecaster;
    if (m_instances != null) {
      if (!m_forecaster.getTSLagMaker().getAdjustForTrends()) {
        m_timeStampCombo.setSelectedItem("<None>");
        m_periodicityCombo.setSelectedIndex(0);
      } else {
        if (m_forecaster.getTSLagMaker().isUsingAnArtificialTimeIndex()) {
          m_timeStampCombo.setSelectedItem("<Use an artificial time index>");
        } else if (m_forecaster.getTSLagMaker().getTimeStampField() != null
            && m_forecaster.getTSLagMaker().getTimeStampField().length() > 0) {
          m_timeStampCombo.setSelectedItem(m_forecaster.getTSLagMaker()
              .getTimeStampField());
        }
      }

      int maxLag = m_forecaster.getTSLagMaker().getMaxLag();
      if (maxLag == 24) {
        m_periodicityCombo.setSelectedItem("Hourly");
      }
      if (maxLag == 7) {
        m_periodicityCombo.setSelectedItem("Daily");
      }
      if (maxLag == 5) {
        m_periodicityCombo.setSelectedItem("Weekly");
      }
      if (maxLag == 12) {
        m_periodicityCombo.setSelectedItem("Monthly");
      }
      if (maxLag == 4) {
        m_periodicityCombo.setSelectedItem("Quarterly");
      }
      if (maxLag == 5) {
        m_periodicityCombo.setSelectedItem("Yearly");
      } else {
        m_periodicityCombo.setSelectedItem("<Unknown>");
      }
    }
  }

  /**
   * Returns true if the forecaster is using a time stamp defined in the data
   * (rather than no time stamp or an artificially generated one)
   * 
   * @return true if a native time stamp is in use.
   */
  public boolean isUsingANativeTimeStamp() {

    String selected = m_timeStampCombo.getSelectedItem().toString();

    if (!selected.equals("<None>")
        && !selected.equals("<Use an artificial time index>")) {
      return true;
    }

    return false;
  }

  /**
   * Get the selected time stamp field name.
   * 
   * @return the selected time stamp field name.
   */
  public String getSelectedTimeStampField() {
    return m_timeStampCombo.getSelectedItem().toString();
  }

  /**
   * Get the WekaForecaster that is being configured.
   * 
   * @return the WekaForecaster that is being configured.
   */
  public WekaForecaster getForecaster() {
    return m_forecaster;
  }

  /**
   * Set the horizon (i.e. the number of steps to forecast).
   * 
   * @param horizon the number of steps to forecast.
   */
  public void setHorizon(int horizon) {
    m_horizonSpinner.setValue(horizon);
  }

  /**
   * Get the horizon (i.e. the number of steps to forecast)
   * 
   * @return the number of steps to forecast
   */
  public int getHorizon() {
    SpinnerNumberModel model = (SpinnerNumberModel) m_horizonSpinner.getModel();
    return model.getNumber().intValue();
  }

  /**
   * Set the instances that will be used to train and/or test the forecaster.
   * 
   * @param insts the instances for training/testing
   * @throws Exception if a problem occurs while configuring based on the
   *           supplied instances.
   */
  public void setInstances(Instances insts) throws Exception {
    m_instances = insts;

    String removeList = "";
    for (int i = 0; i < m_instances.numAttributes(); i++) {
      if (m_instances.attribute(i).isDate()
          || m_instances.attribute(i).isNominal()
          || m_instances.attribute(i).isString()
          || m_instances.attribute(i).isRelationValued()) {
        removeList += (i + 1) + ",";
      }
    }
    m_targetHeader = new Instances(m_instances, 0);
    if (removeList.length() > 0) {
      removeList = removeList.substring(0, removeList.lastIndexOf(","));

      Remove r = new Remove();
      r.setAttributeIndices(removeList);
      r.setInputFormat(m_instances);
      m_targetHeader = Filter.useFilter(m_instances, r);
      m_targetHeader = new Instances(m_targetHeader, 0);
    }
    m_targetPanel.setInstances(m_targetHeader);
    if (m_targetHeader.numAttributes() == 1) {
      // auto select the only target
      m_targetPanel.setSelectedAttributes(new boolean[] { true });
      if (m_parentPanel != null) {
        m_parentPanel.enableStartButton(true);
      }
    }
    setupTimeCombo();
    setupPeriodicityCombo();
  }

  /**
   * Initialize the time stamp combo box
   */
  protected void setupTimeCombo() {
    Vector<String> candidateNames = new Vector<String>();

    candidateNames.add("<Use an artificial time index>");
    candidateNames.add("<None>");
    String firstDateName = null;
    for (int i = 0; i < m_instances.numAttributes(); i++) {
      if (m_instances.attribute(i).isNumeric()) {
        candidateNames.add(m_instances.attribute(i).name());
      }
      if (m_instances.attribute(i).isDate() && firstDateName == null) {
        firstDateName = m_instances.attribute(i).name();
      }
    }

    m_timeStampCombo.setModel(new DefaultComboBoxModel(candidateNames));
    if (firstDateName != null) {
      m_timeStampCombo.setSelectedItem(firstDateName);
    }
  }

  /**
   * Initialize the periodicity combo box
   */
  protected void setupPeriodicityCombo() {
    Vector<String> candidateNames = new Vector<String>();

    candidateNames.add("<Unknown>");
    candidateNames.add("<Detect automatically>");
    candidateNames.add("Hourly");
    candidateNames.add("Daily");
    candidateNames.add("Weekly");
    candidateNames.add("Monthly");
    candidateNames.add("Quarterly");
    candidateNames.add("Yearly");

    m_periodicityCombo.setModel(new DefaultComboBoxModel(candidateNames));

    // check to see if a date is set in the time combo and, if so, select
    // <detect automatically> by default
    String timeSelected = (String) m_timeStampCombo.getSelectedItem();
    System.err.println("Selected " + timeSelected);
    if (timeSelected != null && timeSelected.length() > 0
        && !timeSelected.equals("<None>")
        && !timeSelected.equals("<Use an artificial time index>")
        && m_instances.attribute(timeSelected).isDate()) {
      m_periodicityCombo.setSelectedItem("<Detect automatically>");
    }
  }

  /**
   * Make some default settings for lag lengths based on the selected
   * periodicity.
   * 
   * @param forecaster the forecaster to configure.
   */
  protected void checkPeriodicity(WekaForecaster forecaster) {

    if (m_advancedConfig != null) {
      String timeStampSelected = m_timeStampCombo.getSelectedItem().toString();
      String selectedP = m_periodicityCombo.getSelectedItem().toString();
      if (selectedP.equals("<Detect automatically>")
          && !timeStampSelected.equals("<Use an artificial time index>")
          && !timeStampSelected.equals("<None>") && forecaster != null) {

        forecaster.getTSLagMaker().setPeriodicity(
            TSLagMaker.Periodicity.UNKNOWN);
        TSLagMaker.PeriodicityHandler detected = TSLagMaker
            .determinePeriodicity(m_instances, forecaster.getTSLagMaker()
                .getTimeStampField(), forecaster.getTSLagMaker()
                .getPeriodicity());
        switch (detected.getPeriodicity()) {
        case HOURLY:
          selectedP = "Hourly";
          break;
        case DAILY:
          selectedP = "Daily";
          break;
        case WEEKLY:
          selectedP = "Weekly";
          break;
        case MONTHLY:
          selectedP = "Monthly";
          break;
        case QUARTERLY:
          selectedP = "Quarterly";
          break;
        case YEARLY:
          selectedP = "Yearly";
          break;
        }
      }

      if (forecaster != null) {
        if (selectedP.equals("Hourly")) {
          forecaster.getTSLagMaker().setPeriodicity(
              TSLagMaker.Periodicity.HOURLY);
        } else if (selectedP.equals("Daily")) {
          forecaster.getTSLagMaker().setPeriodicity(
              TSLagMaker.Periodicity.DAILY);
        } else if (selectedP.equals("Weekly")) {
          forecaster.getTSLagMaker().setPeriodicity(
              TSLagMaker.Periodicity.WEEKLY);
        } else if (selectedP.equals("Monthly")) {
          forecaster.getTSLagMaker().setPeriodicity(
              TSLagMaker.Periodicity.MONTHLY);
        } else if (selectedP.equals("Quarterly")) {
          forecaster.getTSLagMaker().setPeriodicity(
              TSLagMaker.Periodicity.QUARTERLY);
        } else if (selectedP.equals("Yearly")) {
          forecaster.getTSLagMaker().setPeriodicity(
              TSLagMaker.Periodicity.YEARLY);
        } else {
          forecaster.getTSLagMaker().setPeriodicity(
              TSLagMaker.Periodicity.UNKNOWN);
        }
        if (m_skipText.isEnabled() && m_skipText.getText() != null
            && m_skipText.getText().length() > 0) {
          forecaster.getTSLagMaker()
              .setSkipEntries(m_skipText.getText().trim());
        } else {
          forecaster.getTSLagMaker().setSkipEntries(""); // clear any previously
                                                         // set ones
        }
      }

      // only set these defaults if the user is not using custom lag lengths!
      if (!m_advancedConfig.isUsingCustomLags()) {
        if (forecaster != null) {
          forecaster.getTSLagMaker().setMinLag(1);
        }
        m_advancedConfig.m_minLagSpinner.setValue(1);

        if (selectedP.equals("Hourly")) {
          if (forecaster != null) {
            forecaster.getTSLagMaker().setMaxLag(
                Math.min(m_instances.numInstances() / 2, 24));
          }
          m_advancedConfig.m_maxLagSpinner.setValue(Math.min(
              m_instances.numInstances() / 2, 24));
        } else if (selectedP.equals("Daily")) {
          if (forecaster != null) {
            forecaster.getTSLagMaker().setMaxLag(
                Math.min(m_instances.numInstances() / 2, 7));
            // forecaster.getTSLagMaker().setSkipEntries("sat,sun");
          }
          m_advancedConfig.m_maxLagSpinner.setValue(Math.min(
              m_instances.numInstances() / 2, 7));
        } else if (selectedP.equals("Weekly")) {
          if (forecaster != null) {
            forecaster.getTSLagMaker().setMaxLag(
                Math.min(m_instances.numInstances() / 2, 52));
          }
          m_advancedConfig.m_maxLagSpinner.setValue(Math.min(
              m_instances.numInstances() / 2, 52));
        } else if (selectedP.equals("Monthly")) {
          if (forecaster != null) {
            forecaster.getTSLagMaker().setMaxLag(
                Math.min(m_instances.numInstances() / 2, 12));
          }
          m_advancedConfig.m_maxLagSpinner.setValue(Math.min(
              m_instances.numInstances() / 2, 12));
        } else if (selectedP.equals("Quarterly")) {
          if (forecaster != null) {
            forecaster.getTSLagMaker().setMaxLag(
                Math.min(m_instances.numInstances() / 2, 4));
          }
          m_advancedConfig.m_maxLagSpinner.setValue(Math.min(
              m_instances.numInstances() / 2, 4));
        } else if (selectedP.equals("Yearly")) {
          if (forecaster != null) {
            forecaster.getTSLagMaker().setMaxLag(
                Math.min(m_instances.numInstances() / 2, 5));
          }
          m_advancedConfig.m_maxLagSpinner.setValue(Math.min(
              m_instances.numInstances() / 2, 5));
        } else {
          // default (<Unknown>)
          if (forecaster != null) {
            forecaster.getTSLagMaker().setMaxLag(
                Math.min(m_instances.numInstances() / 2, 12));
          }
          m_advancedConfig.m_maxLagSpinner.setValue(Math.min(
              m_instances.numInstances() / 2, 12));
        }
      }

      if (!m_advancedConfig.getCustomizeDateDerivedPeriodics()
          && forecaster != null) {
        // configure defaults based on the above periodicity
        if (selectedP.equals("Hourly")) {
          forecaster.getTSLagMaker().setAddAMIndicator(true);
        } else if (selectedP.equals("Daily")) {
          forecaster.getTSLagMaker().setAddDayOfWeek(true);
          forecaster.getTSLagMaker().setAddWeekendIndicator(true);
        } else if (selectedP.equals("Weekly")) {
          forecaster.getTSLagMaker().setAddMonthOfYear(true);
          forecaster.getTSLagMaker().setAddQuarterOfYear(true);
        } else if (selectedP.equals("Monthly")) {
          forecaster.getTSLagMaker().setAddMonthOfYear(true);
          forecaster.getTSLagMaker().setAddQuarterOfYear(true);
        }
      }
    }
  }

  /**
   * Apply the selected settings of this panel to the supplied WekaForecaster.
   * 
   * @param forecaster the WekaForecaster to configure
   * @throws Exception if there is a problem configuring.
   */
  public void applyToForecaster(WekaForecaster forecaster) throws Exception {
    if (forecaster != null) {
      TSLagMaker lagMaker = forecaster.getTSLagMaker();

      String selected = m_timeStampCombo.getSelectedItem().toString();
      if (!selected.equals("<Use an artificial time index>")
          && !selected.equals("<None>")) {
        lagMaker.setTimeStampField(selected);
      } else {
        lagMaker.setTimeStampField("");
      }

      if (selected.equals("<None>")) {
        lagMaker.setAdjustForTrends(false);
      } else {
        lagMaker.setAdjustForTrends(true);
      }

      if (m_periodicityCombo.getSelectedItem().toString()
          .equals("<Detect automatically>")
          && (selected.equals("<Use an artificial time index>") && !m_advancedConfig
              .isUsingCustomLags())) {

        /*
         * JOptionPane.showConfirmDialog(this,
         * "Cannot automatically detect periodicity" +
         * " when using an artificial time index.", "Forecasting",
         * JOptionPane.OK_OPTION);
         */
        throw new Exception(
            "Cannot automatically detect periodicity when using "
                + "an artificial time index.");
      }

      if (m_periodicityCombo.getSelectedItem().toString()
          .equals("<Detect automatically>")
          && !selected.equals("<Use an artificial time index>")
          && !selected.equals("<None>")
          && (!m_instances.attribute(selected).isDate() && !m_advancedConfig
              .isUsingCustomLags())) {
        throw new Exception(
            "Cannot automatically detect periodicity when using "
                + "a non-date time stamp (select manually or use custom lags.");
      }

      // reset any date-derived periodics to false
      lagMaker.setAddAMIndicator(false);
      lagMaker.setAddDayOfWeek(false);
      lagMaker.setAddMonthOfYear(false);
      lagMaker.setAddQuarterOfYear(false);
      lagMaker.setAddWeekendIndicator(false);
      if (!selected.equals("<None>")) {
        checkPeriodicity(forecaster);
      }

      if (m_computeConfidence.isSelected()) {
        forecaster.setCalculateConfIntervalsForForecasts(getHorizon());
        double confLevel = ((SpinnerNumberModel) m_confidenceLevelSpinner
            .getModel()).getNumber().doubleValue();
        forecaster.setConfidenceLevel(confLevel / 100.0);
      } else {
        forecaster.setCalculateConfIntervalsForForecasts(0);
      }

      int[] selectedTargets = m_targetPanel.getSelectedAttributes();
      StringBuffer targetBuf = new StringBuffer();
      for (int i = 0; i < selectedTargets.length; i++) {
        targetBuf.append(m_targetHeader.attribute(selectedTargets[i]).name())
            .append(",");
      }
      String temp = targetBuf.substring(0, targetBuf.lastIndexOf(","));
      if (temp.length() == 0) {
        throw new Exception("You must select some fields to forecast!");
      }
      forecaster.setFieldsToForecast(temp);
    }
  }

  /**
   * Apply the selected settings of this panel to the supplied evaluation module
   * with respect to the supplied forecaster
   * 
   * @param eval the evaluation module to configure
   * @param forecaster the forecaster in use
   * @throws Exception if a problem occurs during configuration.
   */
  public void applyToEvaluation(TSEvaluation eval, WekaForecaster forecaster)
      throws Exception {
    int horizon = ((SpinnerNumberModel) m_horizonSpinner.getModel())
        .getNumber().intValue();
    if (horizon < 1) {
      throw new Exception("Must specify a non-zero number of steps to"
          + "forecast into the future");
    }

    eval.setHorizon(horizon);
    eval.setPrimeWindowSize(forecaster.getTSLagMaker().getMaxLag());
  }

  /**
   * Tests the simple config panel from the command line.
   * 
   * @param args must contain the name of an arff file to load.
   */
  public static void main(String[] args) {

    try {
      if (args.length == 0) {
        throw new Exception("supply the name of an arff file");
      }
      Instances i = new Instances(new java.io.BufferedReader(
          new java.io.FileReader(args[0])));
      SimpleConfigPanel scp = new SimpleConfigPanel(null);
      scp.setInstances(i);
      final javax.swing.JFrame jf = new javax.swing.JFrame(
          "Simple Config Panel");
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(scp, BorderLayout.CENTER);
      jf.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
          jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
