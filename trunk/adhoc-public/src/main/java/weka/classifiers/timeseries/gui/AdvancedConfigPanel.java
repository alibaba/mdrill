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
 *    AdvancedConfigPanel.java
 *    Copyright (C) 2010 Pentaho Corporation
 */

package weka.classifiers.timeseries.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import weka.classifiers.Classifier;
import weka.classifiers.timeseries.WekaForecaster;
import weka.classifiers.timeseries.core.CustomPeriodicTest;
import weka.classifiers.timeseries.core.OverlayForecaster;
import weka.classifiers.timeseries.core.TSLagMaker;
import weka.classifiers.timeseries.eval.TSEvalModule;
import weka.classifiers.timeseries.eval.TSEvaluation;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instances;
import weka.core.Range;
import weka.core.SerializedObject;
import weka.gui.AttributeSelectionPanel;
import weka.gui.ExtensionFileFilter;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertyPanel;

public class AdvancedConfigPanel extends JPanel {
  
  /**
   * For serialization
   */
  private static final long serialVersionUID = 5465960083615138964L;

  /** The training instances to operate on */
  protected Instances m_instances;
  
  /** A reference to the simple config panel */
  protected SimpleConfigPanel m_simpleConfig;
  
  /** A tabbed pane to hold the individual advanced configuration panels */
  JTabbedPane m_configHolder = new JTabbedPane();
  
  /** Editor for selecting and configuring the base algorithm */
  protected GenericObjectEditor m_baseLearnerEditor = 
    new GenericObjectEditor();
  
  /** Property panel for editing base algorithm */
  protected PropertyPanel m_baseLearnerPanel = 
    new PropertyPanel(m_baseLearnerEditor);
  
  /** Custom lags checkbox */
  protected JCheckBox m_useCustomLags = 
    new JCheckBox("Use custom lag lengths");
  
  /** Min lag spinner */
  protected JSpinner m_minLagSpinner;
  
  /** Max lag spinner */
  protected JSpinner m_maxLagSpinner;
  
  /** Adjust for variance check box */
  protected JCheckBox m_adjustForVarianceCheckBox = 
    new JCheckBox("Adjust for variance");
  
  /** Field for fine tuning the lags that are created by specifying a range */
  protected JTextField m_fineTuneLagsField = new JTextField();
  
  /** Check box for averaging consecutive long lags */
  protected JCheckBox m_averageLongLags =
    new JCheckBox("Average consecutive long lags");
  
  /** Spinner for selecting which lag to start averaging from */
  protected JSpinner m_averageLagsAfter;
  
  /** Spinner for selecting how many consecutive long lags to average into one new field */
  protected JSpinner m_numConsecutiveToAverage;
  
  /** Data structure for holding the field names for date-derived fields */
  protected Instances m_dateDerivedPeriodicsHeader;
  
  /** Custom date-derived fields check box */
  protected JCheckBox m_customizeDateDerivedPeriodics = 
    new JCheckBox("Customize");
  
  /** Edit button for custom date-derived fields */
  protected JButton m_editCustomPeriodicBut = new JButton("Edit");
  
  /** Add button for custom date-derived fields */
  protected JButton m_addCustomPeriodicBut = new JButton("New");
  
  /** Delete button for custom date-derived fields */
  protected JButton m_deleteCustomPeriodicBut = new JButton("Delete");

  /** Save button for saving date-derived periodics to a file */
  protected JButton m_savePeriodicBut = new JButton("Save");
  
  /** Load button for loading pre-defined date-derived periodics from a file */
  protected JButton m_loadPeriodicBut = new JButton("Load");
  
  /** File chooser for saving/loading date-derived periodics */
  protected JFileChooser m_fileChooser;
  
  /** Map of custom date-derived fields */
  protected Map<String, ArrayList<CustomPeriodicTest>> m_customPeriodics = 
    new HashMap<String, ArrayList<CustomPeriodicTest>>();
  
  protected static final int NUM_PREDEFINED_PERIODICS = 7;
  
  /**
   * Inner class extending AttributeSelectionPanel that adds methods
   * to get the encapsulated JTable and clear the current table model.
   */
  protected class AttributeSelectionPanelExtended extends 
    AttributeSelectionPanel {
    
    /** For serialization */
    private static final long serialVersionUID = -6863797282164060690L;

    public AttributeSelectionPanelExtended(boolean include, boolean remove,
        boolean invert, boolean pattern) {
      super(include, remove, invert, pattern);
    }
    
    public JTable getTable() {
      return m_Table;
    }
    
    public void clearTableModel() {
      m_Model = null;
      m_Table.setModel(new DefaultTableModel());
      m_Table.revalidate();
      m_Table.repaint();
    }
  }
  
  /** Panel for holding and selecting date-derived periodic attributes */
  protected AttributeSelectionPanelExtended m_dateDerivedPeriodicSelector = 
    new AttributeSelectionPanelExtended(false, false, false, false);
  
  /** Non date-based primary periodic stuff */
  protected JComboBox m_primaryPeriodicCombo = new JComboBox();
  
  /** Panel for holding and selecting overlay fields */
  protected AttributeSelectionPanelExtended m_overlaySelector = 
    new AttributeSelectionPanelExtended(true, true, true, true);
  
  /** Check box for overlay fields */
  protected JCheckBox m_useOverlayData = new JCheckBox("Use overlay data");
  
  /** Holds the structure of overlay fields */
  protected Instances m_overlayHeader;
  
  /** Holds the names of the currently selected evaluation modules */
  protected Instances m_evaluationModsHeader;
  
  /** Panel for holding and selecting evaluation modules */
  protected AttributeSelectionPanel m_evaluationMetrics = 
    new AttributeSelectionPanel(false, false, false, false);
  
  /** Perform training set evaluation check box */
  protected JCheckBox m_trainingCheckBox = new JCheckBox("Evaluate on training");
  
  /** Perform hold-out evaluation check box */
  protected JCheckBox m_holdoutCheckBox = new JCheckBox("Evaluate on held out training");
  
  /** Text field for setting the size of the hold-out set */
  protected JTextField m_holdoutSize = new JTextField();
  
  /** show the separate test set checkbox and button? */
  protected boolean m_allowSeparateTestSet = true;
  
  /** Separate test set check box */
  protected JCheckBox m_separateTestSetCheckBox = 
    new JCheckBox("Evaluate on a separate test set");
  
  /** Button for selecting a separate test set */
  protected JButton m_testSetBut = new JButton("Separate test set");
  
  /** Check box for outputting predictions */
  protected JCheckBox m_outputPredsCheckBox = 
    new JCheckBox("Output predictions at step");
  
  /** Combo box for selecting which target to output predictions for */
  protected JComboBox m_outputPredsCombo = new JComboBox();
  protected JLabel m_outputPredsComboLabel = new JLabel("Target to output", JLabel.RIGHT);
  
  /** Spinner for selecting which step to output */
  protected JSpinner m_outputStepSpinner;
  protected JLabel m_outputStepLabel = new JLabel("Step to output", JLabel.RIGHT);
  
  /** Output future predictions check box */
  protected JCheckBox m_outputFutureCheckBox = 
    new JCheckBox("Output future predictions beyond end of series ");
  
  /** Graph predictions at step check box */
  protected JCheckBox m_graphPredsAtStepCheckBox =
    new JCheckBox("Graph predictions at step");
  
  /** Spinner for selecting which step to graph predictions at */
  protected JSpinner m_graphPredsAtStepSpinner;
  protected JLabel m_stepLab = new JLabel("Steps to graph");
  
  /** Graph targets for specific step */
  protected JCheckBox m_graphTargetForStepsCheckBox =
    new JCheckBox("Graph target at steps:");  
  
  /** Graph target at steps combo box */
  protected JComboBox m_graphTargetAtStepsCombo = new JComboBox();
  protected JLabel m_targetComboLabel = new JLabel("Target to graph", JLabel.RIGHT);
  
  /** Text field for specifying a range of steps to graph for the selected target */
  protected JTextField m_stepRange = new JTextField("1");
  
  /** Graph future predictions check box */
  protected JCheckBox m_graphFutureCheckBox =
    new JCheckBox("Graph future predictions beyond end of series");
  
  static {
    GenericObjectEditor.registerEditors();
  }
  
  /**
   * Get a title for displaying in the tab that will hold this panel
   * 
   * @return a title for this configuration panel
   */
  public String getTabTitle() {
    return "Advanced configuration";
  }
  
  /**
   * Get the tool tip for this configuration panel
   * 
   * @return the tool tip for this configuration panel
   */
  public String getTabTitleToolTip() {
    return "Advanced configuration";
  }
  
  /**
   * Get the underlying Weka classifier that will be used to make the
   * predictions
   * 
   * @return the underlying Weka classifier
   */
  public Classifier getBaseClassifier() {
    return (Classifier)m_baseLearnerEditor.getValue();
  }
  
  /**
   * Set the instances that will be used in the training and evaluation of
   * the forecaster 
   * 
   * @param train the instances to use in the training and evaluation process
   */
  public void setInstances(Instances train) {
    m_instances = train;
    updatePanel();
  }
  
  /**
   * Set the enabled/disabled status of date-derived periodic panel
   * and its associated widgets.
   * 
   * @param s true if date-derived periodicis is to be enabled
   */
  public void enableDateDerivedPeriodics(boolean s) {
    m_customizeDateDerivedPeriodics.setEnabled(s);
    if (!s) {
      m_customizeDateDerivedPeriodics.setSelected(false);
      m_addCustomPeriodicBut.setEnabled(false);
      m_editCustomPeriodicBut.setEnabled(false);
      m_deleteCustomPeriodicBut.setEnabled(false);
      m_savePeriodicBut.setEnabled(false);
      m_loadPeriodicBut.setEnabled(false);
    }
  }
  
  /**
   * Returns true if date-derived periodics is enabled
   * 
   * @return if date-derived periodics is enabled.
   */
  public boolean isEnabledCustomizeDateDerivedPeriodics() {
    return m_customizeDateDerivedPeriodics.isEnabled();
  }
  
  /**
   * Returns true if the date-derived periodics check box is selected.
   * 
   * @return true if the date-derived periodics check box is selected.
   */
  public boolean getCustomizeDateDerivedPeriodics() {
    return m_customizeDateDerivedPeriodics.isSelected();
  }
  
  /**
   * Returns true if the user has opted to customize the lags.
   * 
   * @return true if custom lags are in use.
   */
  public boolean isUsingCustomLags() {
    return m_useCustomLags.isSelected();
  }
  
  /**
   * Gets the size of the holdout set.
   * 
   * @return the size of the holdout set or 0 if no holdout set is being used.
   */
  public double getHoldoutSetSize() {
    double result = 0;
    
    if (m_holdoutCheckBox.isSelected()) {
      try {
        result = Double.parseDouble(m_holdoutSize.getText());
      } catch (NumberFormatException ex) {}
    }
    
    return result;
  }
  
  /**
   * Returns true if the user has opted to output future predictions
   * 
   * @return if the user has opted to output future predictions
   */
  public boolean getOutputFuturePredictions() {
    return m_outputFutureCheckBox.isSelected();
  }
  
  /**
   * Returns at which step to output predictions. Returns
   * 0 if the user has not opted to output predictions.
   * 
   * @return step at which to output predictions or 0 if
   * no predictions are to be output.
   */
  public int getOutputPredictionsAtStep() {
    if (!m_outputPredsCheckBox.isSelected()) {
      return 0;
    }
    
    return ((SpinnerNumberModel)m_outputStepSpinner.
        getModel()).getNumber().intValue();
  }
  
  /**
   * Returns true if the user has opted to graph a target at
   * specified steps
   * 
   * @return true if the user has opted to graph a target at
   * specified steps
   */
  public boolean getGraphTargetForSteps() {
    return m_graphTargetForStepsCheckBox.isSelected();
  }
  
  /**
   * Return the target that is to be graphed at various
   * steps
   * 
   * @return the target that is to be graphed at specified steps or
   * null if the user has not opted to graph a targert at specified
   * steps
   */
  public String getGraphTargetForStepsTarget() {
    if (!getGraphTargetForSteps()) {
      return null;
    }
    
    return m_graphTargetAtStepsCombo.getSelectedItem().toString();
  }
  
  /**
   * If the user has opted to graph a target a various steps, then this
   * method returns the list of steps that they have selected.
   * 
   * @return the list of steps at which to graph a target, or null if the
   * user has not opted to graph a target at various steps.
   */
  public List<Integer> getGraphTargetForStepsStepList() {
    String rng = m_stepRange.getText();
    
    if (rng == null || rng.length() == 0) {
      return null;
    }
    
    Range range = new Range(rng);
    range.setUpper(m_simpleConfig.getHorizonValue());
    int[] indices = range.getSelection();
    
    List<Integer> rangeList = new ArrayList<Integer>();
    for (int i : indices) {
      rangeList.add((i + 1));
    }
    
    return rangeList;
  }
  
  /**
   * Get the selected target to output predictions for. Returns
   * null if the user has not opted to output predictions.
   * 
   * @return the name of the target to output predictions for or
   * null if no predictions are to be output.
   */
  public String getOutputPredictionsTarget() {
    if (!m_outputPredsCheckBox.isSelected()) {
      return null;
    }
    
    return m_outputPredsCombo.getSelectedItem().toString();
  }
  
  /**
   * Get the step number to graph all the targets at.
   * 
   * @return the step number to graph all the targets at, or
   * 0 if the user has not opted to graph all the targets.
   */
  public int getGraphPredictionsAtStep() {
    if (!m_graphPredsAtStepCheckBox.isSelected()) {
      return 0;
    }
    
    return ((SpinnerNumberModel)m_graphPredsAtStepSpinner.
        getModel()).getNumber().intValue();
  }
  
  /**
   * Returns true if the user has opted to graph future predictions
   * 
   * @return true if the user has opted graph future predictions
   */
  public boolean getGraphFuturePredictions() {
    return m_graphFutureCheckBox.isSelected();
  }
  
  /**
   * Constructor
   * 
   * @param s a reference to the simple configuration panel
   * @param allowSeparateTestSet true if the separate test set button
   * is to be displayed
   */
  public AdvancedConfigPanel(SimpleConfigPanel s, 
      boolean allowSeparateTestSet) {
    
    m_allowSeparateTestSet = allowSeparateTestSet;
    m_simpleConfig = s;

    setLayout(new BorderLayout());
    layoutLearnerPanel();
    layoutLagPanel();        
    layoutDateDerivedPeriodicPanel();
    layoutOverlayPanel();
    layoutEvaluationPanel();
    layoutOutputPanel();

    add(m_configHolder, BorderLayout.CENTER);
  }
  
  /**
   * Constructor
   * 
   * @param s a reference to the simple configuration panel
   */
  public AdvancedConfigPanel(SimpleConfigPanel s) {
    this(s, true);
  }
  
  /**
   * Updates various enabled/selected status of widgets based on the current
   * configuration
   */
  public void updatePanel() {
    updateDateDerivedPanel();
    updatePrimaryPeriodic();
    updateOutputPanel();
    updateOverlayPanel();
  }
  
  /**
   * Updates the status/selection of widgets on the overlay panel
   */
  public void updateOverlayPanel() {
    Instances newI = createAvailableOverlayList();
    if (newI == null) {
      m_useOverlayData.setSelected(false);
      m_useOverlayData.setEnabled(false);
      m_overlaySelector.clearTableModel();
    } else {
      m_useOverlayData.setEnabled(true);
      if (m_useOverlayData.isSelected()) {
        if (m_overlayHeader == null || 
            !newI.equalHeaders(m_overlayHeader)) {
          m_overlayHeader = newI;
          m_overlaySelector.setInstances(newI);
        }
      }
    }
  }
  
  private Instances createAvailableOverlayList() {
    
    String ppfn = m_primaryPeriodicCombo.getSelectedItem().toString();
    Instances result = null;
    if (m_simpleConfig.m_targetHeader != null) {
      ArrayList<Attribute> availableAtts = new ArrayList<Attribute>();
      
      int[] selectedTargets = m_simpleConfig.m_targetPanel.getSelectedAttributes();
      for (int i = 0; i < m_instances.numAttributes(); i++) {
        // skip all date attributes
        if (m_instances.attribute(i).isDate()) {
          continue;
        }
        
        // skip any primary periodic
        if (m_instances.attribute(i).name().equals(ppfn)) {
          continue;
        }
        
        if (m_simpleConfig.m_targetHeader.
            attribute(m_instances.attribute(i).name()) != null) {
          // now need to check whether it's been selected as a target
          int indexToCheck = m_simpleConfig.m_targetHeader.
            attribute(m_instances.attribute(i).name()).index();
          
          boolean ok = true;
          for (int j = 0; j < selectedTargets.length; j++) {
            if (indexToCheck == selectedTargets[j]) {
              ok = false; // can't use this attribute
              break;
            }
          }
          
          if (ok) {
            availableAtts.add(new Attribute(m_instances.attribute(i).name()));
          }
        } else {
          // this is available
          availableAtts.add(new Attribute(m_instances.attribute(i).name()));
        }
      }
      if (availableAtts.size() > 0) {
        result = new Instances("Overlay",availableAtts, 1);
      }
    }
    
    return result;
  }
  
  /**
   * Updates the status/selection of various widgets on the output panel
   */
  public void updateOutputPanel() {
    if (m_simpleConfig.m_targetHeader != null) {
      // configure the target combos
      Vector<String> candidates = new Vector<String>();
      for (int i = 0; i < m_simpleConfig.m_targetHeader.numAttributes(); i++) {
        candidates.add(m_simpleConfig.m_targetHeader.attribute(i).name());
      }
      
      m_graphTargetAtStepsCombo.setModel(new DefaultComboBoxModel(candidates));
      m_outputPredsCombo.setModel(new DefaultComboBoxModel(candidates));
    }
  }
  
  /**
   * Updates the status/selection of various widgets on the date-derived
   * periodics panel
   */
  protected void updateDateDerivedPanel() {
    if (m_simpleConfig.m_timeStampCombo.getSelectedItem() == null) {
      return;
    }
    String selectedTimeStamp = m_simpleConfig.m_timeStampCombo.getSelectedItem().toString();
    if (selectedTimeStamp.equals("<None>") || 
        selectedTimeStamp.equals("<Use and artificial time stamp>")) {
      m_customizeDateDerivedPeriodics.setSelected(false);
      m_customizeDateDerivedPeriodics.setEnabled(false);
      m_editCustomPeriodicBut.setEnabled(false);
      m_addCustomPeriodicBut.setEnabled(false);
      m_deleteCustomPeriodicBut.setEnabled(false);
      m_savePeriodicBut.setEnabled(false);
      m_loadPeriodicBut.setEnabled(false);
    } else if (m_instances != null) {
      Attribute timeStampAtt = m_instances.attribute(selectedTimeStamp);
      if (timeStampAtt != null && timeStampAtt.isDate()) {
        m_customizeDateDerivedPeriodics.setEnabled(true);
      } else {
        m_customizeDateDerivedPeriodics.setSelected(false);
        m_customizeDateDerivedPeriodics.setEnabled(false);
        m_editCustomPeriodicBut.setEnabled(false);
        m_addCustomPeriodicBut.setEnabled(false);
        m_deleteCustomPeriodicBut.setEnabled(false);
        m_savePeriodicBut.setEnabled(false);
        m_loadPeriodicBut.setEnabled(false);
      }
    } else {
      m_customizeDateDerivedPeriodics.setSelected(false);
      m_customizeDateDerivedPeriodics.setEnabled(false);
      m_editCustomPeriodicBut.setEnabled(false);
      m_addCustomPeriodicBut.setEnabled(false);
      m_deleteCustomPeriodicBut.setEnabled(false);
      m_savePeriodicBut.setEnabled(false);
      m_loadPeriodicBut.setEnabled(false);
    }
  }
  
  /**
   * Sets up the primary periodic drop-down box
   */
  protected void updatePrimaryPeriodic() {
    Vector<String> entries = new Vector<String>();
    
    entries.add("<None>");
    if (m_instances != null) {
      for (int i = 0; i < m_instances.numAttributes(); i++) {
        if (m_instances.attribute(i).isNominal()) {
          entries.add(m_instances.attribute(i).name());
        }
      }
    }
    m_primaryPeriodicCombo.setModel(new DefaultComboBoxModel(entries));
  }
  
  /**
   * Updates the status of eval and output widgets
   */
  protected void updateEvalAndOutputEnabledStatus() {
    boolean enable = (m_trainingCheckBox.isSelected() || 
        m_holdoutCheckBox.isSelected() ||
        m_separateTestSetCheckBox.isSelected()); 

    // enable/disable and deselect all prediction and graphing widgets
    m_outputPredsCheckBox.setEnabled(enable);
    
    //m_outputFutureCheckBox.setEnabled(enable);
    
    m_graphPredsAtStepCheckBox.setEnabled(enable);
    //m_outputStepSpinner.setEnabled(enable);
    //m_graphPredsAtStepSpinner.setEnabled(enable);
    m_graphTargetForStepsCheckBox.setEnabled(enable);
    
    //m_graphFutureCheckBox.setEnabled(enable);
    
    if (!enable) {
      m_outputPredsCheckBox.setSelected(false);
    //  m_outputFutureCheckBox.setSelected(false);
      m_graphPredsAtStepCheckBox.setSelected(false);
      m_graphTargetForStepsCheckBox.setSelected(false);
      
      
      //m_graphFutureCheckBox.setSelected(false);
    }
    
    boolean enabled = m_graphTargetForStepsCheckBox.isSelected();
    m_stepLab.setEnabled(enabled); m_stepRange.setEnabled(enabled);
    m_targetComboLabel.setEnabled(enabled); m_graphTargetAtStepsCombo.setEnabled(enabled);
    m_graphPredsAtStepSpinner.setEnabled(enabled);
    
    enabled = m_outputPredsCheckBox.isSelected();
    m_outputStepLabel.setEnabled(enabled); m_outputPredsCombo.setEnabled(enabled);
    m_outputPredsComboLabel.setEnabled(enable); m_outputStepSpinner.setEnabled(enabled);

        
    m_testSetBut.setEnabled(!m_holdoutCheckBox.isSelected());
    m_separateTestSetCheckBox.setEnabled(!m_holdoutCheckBox.isSelected());
    m_holdoutSize.setEnabled(m_holdoutCheckBox.isSelected());
    if (m_holdoutCheckBox.isSelected()) {
      m_separateTestSetCheckBox.setSelected(false);
    }
  }
  
  /**
   * Layout the evaluation panel
   */
  protected void layoutEvaluationPanel() {
    JPanel basePanel = new JPanel();
    basePanel.setLayout(new BorderLayout());
    /*basePanel.setBorder(BorderFactory.
        createTitledBorder("Evaluation")); */
    
    JPanel base1 = new JPanel();
    base1.setLayout(new BorderLayout());
    base1.add(m_evaluationMetrics, BorderLayout.CENTER);
    List<TSEvalModule> evalModulesL = TSEvalModule.getModuleList();
    ArrayList<Attribute> atts = new ArrayList<Attribute>();
    int numMods = 0;
    for (TSEvalModule s : evalModulesL) {
      if (!(s.getEvalName().equals("Error"))) {
        atts.add(new Attribute(s.toString()));
        numMods++;
      }
    }
    Instances modInsts = new Instances("Eval modules", atts, 1);
    m_evaluationModsHeader = modInsts;
    m_evaluationMetrics.setInstances(modInsts);
    boolean[] selected = new boolean[numMods];
    selected[0] = true; selected[2] = true;
    try {
      m_evaluationMetrics.setSelectedAttributes(selected);
    } catch (Exception ex) {}
    m_evaluationMetrics.setPreferredScrollableViewportSize(new Dimension(260,80));
    m_evaluationMetrics.setBorder(BorderFactory.createTitledBorder("Metrics"));
    
    JPanel temp1 = new JPanel();
    temp1.setLayout(new BorderLayout());
    temp1.setBorder(BorderFactory.createTitledBorder("Test options"));
    temp1.add(m_trainingCheckBox, BorderLayout.NORTH);
    //m_trainingCheckBox.setSelected(true);
    m_trainingCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateEvalAndOutputEnabledStatus();
        if (m_simpleConfig != null) {
          m_simpleConfig.m_performEvaluation.
            setSelected(m_trainingCheckBox.isSelected() ||
                m_holdoutCheckBox.isSelected());
        }
      }
    });
    
    JPanel temp2 = new JPanel();
    temp2.setLayout(new BorderLayout());
    temp2.add(m_holdoutCheckBox, BorderLayout.WEST);
    temp2.add(m_holdoutSize, BorderLayout.EAST);
    m_holdoutSize.setEnabled(false);
    
    int width = m_minLagSpinner.getPreferredSize().width;
    int height = m_minLagSpinner.getPreferredSize().height;
    m_holdoutSize.setPreferredSize(new Dimension(width * 1, height));
    m_holdoutSize.setMinimumSize(new Dimension(width * 1, height));
    m_holdoutSize.setToolTipText("Number of instances (value >=1) or percentage " +
    		"(value < 1) to hold out from the end of the data");
    m_holdoutSize.setText("0.3");
    m_holdoutCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateEvalAndOutputEnabledStatus();
        if (m_simpleConfig != null) {
          m_simpleConfig.m_performEvaluation.
            setSelected(m_trainingCheckBox.isSelected() ||
                m_holdoutCheckBox.isSelected());
        }
      }
    });
    temp1.add(temp2, BorderLayout.CENTER);
    
    if (m_allowSeparateTestSet) {
      JPanel checkAndButHolder = new JPanel();
      checkAndButHolder.setLayout(new BorderLayout());
      checkAndButHolder.add(m_separateTestSetCheckBox, BorderLayout.NORTH);
      checkAndButHolder.add(m_testSetBut, BorderLayout.SOUTH);
      temp1.add(checkAndButHolder, BorderLayout.SOUTH);
      m_separateTestSetCheckBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          updateEvalAndOutputEnabledStatus();
        }
      });
    }
                
    JPanel temp3 = new JPanel();
    temp3.setLayout(new BorderLayout());
    temp3.add(temp1, BorderLayout.NORTH);
    base1.add(temp3, BorderLayout.EAST);
    basePanel.add(base1, BorderLayout.NORTH);
    //basePanel.add(outputOptsHolder, BorderLayout.EAST);
    
    
    
    m_testSetBut.setToolTipText("Evaluate on a separate test set");
    m_testSetBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // TODO
      }
    });
    
    //holder.add(basePanel, BorderLayout.EAST);
    m_configHolder.addTab("Evaluation", null, basePanel, "Evaluation options");
  }
  
  /**
   * Layout the output panel
   */
  protected void layoutOutputPanel() {
    JPanel outputOptsHolder = new JPanel();
    outputOptsHolder.setLayout(new GridLayout(1,2));    

    JPanel textOutHolder = new JPanel();
    textOutHolder.setLayout(new BorderLayout());
    textOutHolder.setBorder(BorderFactory.createTitledBorder("Output options"));
    
    SpinnerNumberModel snm = new SpinnerNumberModel();
    snm.setValue(1); snm.setMinimum(1);
    m_outputStepSpinner = new JSpinner(snm);
    Dimension spinD = m_outputStepSpinner.getPreferredSize();
    spinD = new Dimension((int)(spinD.getWidth() * 1.5), 
        (int)spinD.getHeight());
    m_outputStepSpinner.setPreferredSize(spinD);
    JPanel checkHolder = new JPanel();
    checkHolder.setLayout(new BorderLayout());
    checkHolder.add(m_outputPredsCheckBox, BorderLayout.WEST);
    //labAndSpinnerHolder.add(m_outputStepSpinner, BorderLayout.EAST);
    JPanel comboAndSpinnerHolder = new JPanel();
    comboAndSpinnerHolder.setLayout(new BorderLayout());
    
    JPanel combo1Holder = new JPanel(); combo1Holder.setLayout(new BorderLayout());
    combo1Holder.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
    combo1Holder.add(m_outputPredsCombo, BorderLayout.EAST);

    combo1Holder.add(m_outputPredsComboLabel, BorderLayout.CENTER);
    
    comboAndSpinnerHolder.add(combo1Holder, BorderLayout.NORTH);
    
    JPanel spinnerHolder1 = new JPanel(); 
    spinnerHolder1.setLayout(new BorderLayout());
    spinnerHolder1.add(m_outputStepSpinner, BorderLayout.EAST);
    spinnerHolder1.add(m_outputStepLabel, BorderLayout.CENTER);
    
    JPanel spinnerHolder2 = new JPanel();
    spinnerHolder2.setLayout(new BorderLayout());
    spinnerHolder2.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
    spinnerHolder2.add(spinnerHolder1, BorderLayout.NORTH);
    
    comboAndSpinnerHolder.add(spinnerHolder2, BorderLayout.CENTER);
    textOutHolder.add(comboAndSpinnerHolder, BorderLayout.CENTER);
    
    textOutHolder.add(checkHolder, BorderLayout.NORTH);
    m_outputStepSpinner.setEnabled(false);
    m_outputStepLabel.setEnabled(false);
    m_outputPredsCombo.setEnabled(false);
    m_outputPredsCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_outputStepSpinner.setEnabled(m_outputPredsCheckBox.isSelected());
        m_outputStepLabel.setEnabled(m_outputPredsCheckBox.isSelected());
        m_outputPredsComboLabel.setEnabled(m_outputPredsCheckBox.isSelected());
        m_outputPredsCombo.setEnabled(m_outputPredsCheckBox.isSelected());
      }
    });
    
    JPanel temp1 = new JPanel();
    temp1.setLayout(new BorderLayout());
    temp1.add(m_outputFutureCheckBox, BorderLayout.NORTH);
    textOutHolder.add(temp1, BorderLayout.SOUTH);
    outputOptsHolder.add(textOutHolder);
    m_outputFutureCheckBox.setSelected(true);
    
    JPanel graphOutputHolder = new JPanel();
    graphOutputHolder.setLayout(new BorderLayout());
    graphOutputHolder.setBorder(BorderFactory.
        createTitledBorder("Graphing options"));
    
    outputOptsHolder.add(graphOutputHolder);
    
    snm = new SpinnerNumberModel();
    snm.setValue(1); snm.setMinimum(1);
    m_graphPredsAtStepSpinner = new JSpinner(snm);
    spinD = m_graphPredsAtStepSpinner.getPreferredSize();
    spinD = new Dimension((int)(spinD.getWidth() * 1.5), 
        (int)spinD.getHeight());
    m_graphPredsAtStepSpinner.setPreferredSize(spinD);
    JPanel labAndSpinnerHolder = new JPanel();
    labAndSpinnerHolder.setLayout(new BorderLayout());
    labAndSpinnerHolder.add(m_graphPredsAtStepCheckBox, BorderLayout.WEST);
    labAndSpinnerHolder.add(m_graphPredsAtStepSpinner, BorderLayout.EAST);
    graphOutputHolder.add(labAndSpinnerHolder, BorderLayout.NORTH);
    m_graphPredsAtStepCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_graphPredsAtStepSpinner.
          setEnabled(m_graphPredsAtStepCheckBox.isSelected());
      }
    });
    m_graphPredsAtStepSpinner.setEnabled(false);
    
    JPanel comboAndRangeHolder = new JPanel();
    comboAndRangeHolder.setLayout(new BorderLayout());
    comboAndRangeHolder.add(m_graphTargetForStepsCheckBox, BorderLayout.NORTH);
    JPanel temp2 = new JPanel();
    temp2.setLayout(new BorderLayout());
    temp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
    temp2.add(m_graphTargetAtStepsCombo, BorderLayout.EAST);

    temp2.add(m_targetComboLabel, BorderLayout.CENTER);
    JPanel temp3 = new JPanel();
    temp3.setLayout(new BorderLayout());
    temp3.add(temp2, BorderLayout.NORTH);
    
    JPanel textHolder = new JPanel();
    textHolder.setLayout(new BorderLayout());
    //textHolder.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
    textHolder.add(m_stepRange, BorderLayout.EAST);
    m_stepRange.setPreferredSize(new Dimension((int)spinD.getWidth() * 2,
        (int)spinD.getHeight()));
    
    m_stepLab.setToolTipText("Comma separated list of step numbers to graph");
    m_stepRange.setToolTipText("Comma separated list of step numbers to graph");
    JPanel ll = new JPanel(); ll.setLayout(new BorderLayout());
    ll.add(m_stepLab, BorderLayout.EAST);    

    textHolder.add(ll, BorderLayout.CENTER);
    temp3.add(textHolder, BorderLayout.CENTER);

    JPanel ll2 = new JPanel(); ll2.setLayout(new BorderLayout());
    ll2.add(temp3, BorderLayout.NORTH);
    comboAndRangeHolder.add(ll2, BorderLayout.CENTER);        
    comboAndRangeHolder.add(m_graphFutureCheckBox, BorderLayout.SOUTH);
    m_graphFutureCheckBox.setSelected(true);
    
    m_stepLab.setEnabled(false); m_stepRange.setEnabled(false);
    m_targetComboLabel.setEnabled(false); m_graphTargetAtStepsCombo.setEnabled(false);
    m_graphTargetForStepsCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean enabled = m_graphTargetForStepsCheckBox.isSelected();
        m_stepLab.setEnabled(enabled); m_stepRange.setEnabled(enabled);
        m_targetComboLabel.setEnabled(enabled); m_graphTargetAtStepsCombo.setEnabled(enabled);
      }
    });
    
    graphOutputHolder.add(comboAndRangeHolder, BorderLayout.CENTER);
    
    m_configHolder.addTab("Output", null, outputOptsHolder, "Configure output");
  }
  
  private Instances createDateDerivedPeriodicList() {
    ArrayList<Attribute> atts = new ArrayList<Attribute>();
    atts.add(new Attribute("AM")); atts.add(new Attribute("DayOfWeek"));
    atts.add(new Attribute("DayOfMonth")); atts.add(new Attribute("NumDaysInMonth"));
    atts.add(new Attribute("Weekend")); atts.add(new Attribute("Month"));
    atts.add(new Attribute("Quarter"));
    
    // Custom periodics
    for (String name : m_customPeriodics.keySet()) {
      atts.add(new Attribute("c_" + name));
    }
    
    Instances insts = new Instances("Periodics", atts, 1);
    
    return insts;
  }
  
  private String displayAddEditDialog(List<CustomPeriodicTest> testList, 
      String fieldName) {
    CustomPeriodicEditor ed = new CustomPeriodicEditor(testList);
    if (fieldName != null && fieldName.length() > 0) {
      ed.setFieldName(fieldName);
    }
    int result = JOptionPane.showConfirmDialog(this, ed, 
        "Add/Edit custom periodic field", JOptionPane.OK_CANCEL_OPTION);
    
    if (result == JOptionPane.OK_OPTION) {
      if (testList.size() == 0) {
        // Nothing added to the list, so just cancel
        return "";
      }
      return ed.getFieldName();
    } else {
      return ""; // indicates cancel
    }
  }
  
  /**
   * Layout the overlay panel
   */
  protected void layoutOverlayPanel() {
    JPanel basePanel = new JPanel();
    basePanel.setLayout(new BorderLayout());
    
    JPanel temp = new JPanel();
    temp.setLayout(new BorderLayout());
    //temp
    JPanel checkHolder = new JPanel();
    checkHolder.setLayout(new BorderLayout());
    checkHolder.add(m_useOverlayData, BorderLayout.NORTH);
    m_useOverlayData.setEnabled(false);
    
    temp.add(checkHolder, BorderLayout.EAST);
    temp.add(m_overlaySelector, BorderLayout.CENTER);
    m_overlaySelector.setPreferredScrollableViewportSize(new Dimension(250,70));
    
    JPanel botP = new JPanel();
    botP.setLayout(new BorderLayout());
    botP.setBorder(BorderFactory.
        createTitledBorder("Overlay data selection"));
    botP.add(temp, BorderLayout.CENTER);
    
    m_useOverlayData.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_instances != null && m_useOverlayData.isSelected()) {
          if (m_overlaySelector.getSelectionModel() != null) {
            if (m_overlayHeader == null){
              m_overlayHeader = createAvailableOverlayList();
            } else {
              Instances newI = createAvailableOverlayList();
              if (newI == null) {
                m_overlaySelector.clearTableModel();
                m_overlayHeader = null;
                return;
              } else if (!newI.equalHeaders(m_overlayHeader)) {
                m_overlayHeader = newI;
              }
            }
            m_overlaySelector.setInstances(m_overlayHeader);
          }
        } else {
          m_overlaySelector.clearTableModel();
        }
      }
    });
    
    basePanel.add(botP, BorderLayout.NORTH);
    
    m_configHolder.addTab("Overlay data", null, basePanel,
      "Specify attributes that are to be considered as \"overlay\" data");
  }
  
  protected void savePeriodicsToFile() throws IOException {
    if (m_fileChooser == null) {
      m_fileChooser = 
        new JFileChooser(new File(System.getProperty("user.home")));
      m_fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      FileFilter filter = new ExtensionFileFilter(".periodics", "Time series date-derived " +
      		"periodic attribute definitions (*periodics)");
      m_fileChooser.addChoosableFileFilter(filter);
    }
    int result = m_fileChooser.showSaveDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File saveFile = m_fileChooser.getSelectedFile();
      if (saveFile.toString().indexOf(".periodics") < 0) {
        saveFile = new File(saveFile.toString() + ".periodics");
      }
      
      PrintWriter br = 
        new PrintWriter(new BufferedWriter(new FileWriter(saveFile)));
      
      br.print("time-series-periodics\n");    
      
      int[] selected = m_dateDerivedPeriodicSelector.getSelectedAttributes();
      if (m_dateDerivedPeriodicsHeader != null) {
        for (int i = 0; i < selected.length; i++) {
          int s = selected[i];
          String name = m_dateDerivedPeriodicsHeader.attribute(s).name();
          if (s < NUM_PREDEFINED_PERIODICS) {
            br.print("*pre-defined*:" + name + "\n");
          } else {
            
            // remove the "c_"
            name = name.replaceFirst("c_", "");
            ArrayList<CustomPeriodicTest> selectedP = 
              m_customPeriodics.get(name);
            
            br.print("*custom*:" + name + "\n");
            for (int j = 0; j < selectedP.size(); j++) {
              br.print(selectedP.get(j).toString() + "\n");
            }            
          }
        }
      }
      br.flush();
      br.close();
    }        
  }
  
  protected void loadPeriodicsFromFile() throws IOException {
    if (m_fileChooser == null) {
      m_fileChooser = 
        new JFileChooser(new File(System.getProperty("user.home")));
      m_fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      FileFilter filter = new ExtensionFileFilter(".periodics", "Time series date-derived " +
                "periodic attribute definitions (*periodics)");
      m_fileChooser.addChoosableFileFilter(filter);
    }
    
    int result = m_fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File loadFile = m_fileChooser.getSelectedFile();
      
      BufferedReader br = new BufferedReader(new FileReader(loadFile));
      String identifierLine = br.readLine();
      boolean ok = false;
      if (identifierLine != null) {
        if (identifierLine.equalsIgnoreCase("time-series-periodics")) {
          ok = true;
        }
      }
      
      if (!ok) {
        JOptionPane.showConfirmDialog(this, "\"" + loadFile.toString() + "\" does not" +
        		"\nappear to be a periodic attribute definition file", 
        		"Unrecognised file type", JOptionPane.ERROR_MESSAGE);
        return;
      }
      
      String line = null;
      boolean withinCustom = false;
      m_customPeriodics.clear();
      List<String> predefined = new ArrayList<String>();
      ArrayList<CustomPeriodicTest> testList = null;
      String currentCustomName = null;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("*pre-defined*:")) {
          if (withinCustom) {
            // finish custom
            m_customPeriodics.put(currentCustomName, testList);
          }
          
          withinCustom = false;
          line = line.substring(line.indexOf(":") + 1, line.length()).trim();
          // process predefined type
          predefined.add(line);
        } else if (line.startsWith("*custom*:")) {
          if (withinCustom) {
            // finish custom
            m_customPeriodics.put(currentCustomName, testList);
          }

          // start new custom      
          testList = new ArrayList<CustomPeriodicTest>();
          currentCustomName = 
            line.substring(line.indexOf(":") + 1, line.length()).trim();
          withinCustom = true;

        } else if (withinCustom) {
          // process custom part
          try {
            CustomPeriodicTest t = new CustomPeriodicTest(line);
            testList.add(t);
          } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            JOptionPane.showConfirmDialog(this, "A problem occurred while parsing\n" +
            		"the following custom periodic test:\n"
                + line, "Error parsing custom test", JOptionPane.ERROR_MESSAGE);
            return;
          }
          
        }
      }
      
      br.close();
      
      // check for last custom (if exists)
      if (testList != null && testList.size() > 0) {
        m_customPeriodics.put(currentCustomName, testList);
      }
      
      m_dateDerivedPeriodicSelector.clearTableModel();
      Instances insts = createDateDerivedPeriodicList();
      m_dateDerivedPeriodicSelector.setInstances(insts);
      m_dateDerivedPeriodicsHeader = insts;
      
      boolean[] selected = new boolean[insts.numAttributes()];
      for (int i = 0; i < insts.numAttributes(); i++) {
        if (i < NUM_PREDEFINED_PERIODICS) {
          switch (i) {
            case 0:
              if (predefined.contains("AM")) {
                selected[i] = true;
              }
              break;
            case 1:
              if (predefined.contains("DayOfWeek")) {
                selected[i] = true;
              }
              break;
            case 2:
              if (predefined.contains("DayOfMonth")) {
                selected[i] = true;
              }
              break;
            case 3:
              if (predefined.contains("NumDaysInMonth")) {
                selected[i] = true;
              }
              break;
            case 4:
              if (predefined.contains("Weekend")) {
                selected[i] = true;
              }
              break;
            case 5:
              if (predefined.contains("Month")) {
                selected[i] = true;
              }
              break;
            case 6:
              if (predefined.contains("Quarter")) {
                selected[i] = true;
              }
              break;
          }
        } else {
          selected[i] = true;
        }
      }
      try {
        m_dateDerivedPeriodicSelector.setSelectedAttributes(selected);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  
  protected void layoutDateDerivedPeriodicPanel() {
    JPanel basePanel = new JPanel();
    basePanel.setLayout(new BorderLayout());        
    
    JPanel temp = new JPanel();
    temp.setLayout(new BorderLayout());
    //temp
    JPanel checkHolder = new JPanel();
    checkHolder.setLayout(new BorderLayout());
    checkHolder.add(m_customizeDateDerivedPeriodics, BorderLayout.EAST);
    JPanel editButsP = new JPanel();
    editButsP.setLayout(new GridLayout(1,5));
    editButsP.add(m_addCustomPeriodicBut);
    editButsP.add(m_deleteCustomPeriodicBut);
    editButsP.add(m_editCustomPeriodicBut);
    editButsP.add(m_savePeriodicBut);
    editButsP.add(m_loadPeriodicBut);
    
    m_loadPeriodicBut.setEnabled(false);
    m_savePeriodicBut.setEnabled(false);
    m_savePeriodicBut.setToolTipText("Save checked date-derived " +
    		"periodic definitions to a file");
    m_savePeriodicBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          savePeriodicsToFile();
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    });
    
    m_loadPeriodicBut.setToolTipText("Load pre-saved date-derived periodic " +
    		"definitions from a file");
    m_loadPeriodicBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          loadPeriodicsFromFile();
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    });
    
    checkHolder.add(editButsP, BorderLayout.WEST);
    m_editCustomPeriodicBut.setEnabled(false);
    m_editCustomPeriodicBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int selectedRow = m_dateDerivedPeriodicSelector.getTable().getSelectedRow();
        if (selectedRow > NUM_PREDEFINED_PERIODICS - 1) {
          String fieldName = m_dateDerivedPeriodicsHeader.attribute(selectedRow).name();
          
          // need to trim off the "c_"
          fieldName = fieldName.replaceFirst("c_", "");
          ArrayList<CustomPeriodicTest> toEdit = m_customPeriodics.get(fieldName);
          if (toEdit == null) {
            System.err.println("Oh oh, couldn't find " + fieldName + " to edit!");
          } else {
            // now make a copy of it
            try {
              SerializedObject so = new SerializedObject(toEdit);
              toEdit = (ArrayList<CustomPeriodicTest>)so.getObject();
              
              String newFieldName = displayAddEditDialog(toEdit, fieldName);
              if (newFieldName.length() == 0) {
                // cancel selected so do nothing
              } else if (!newFieldName.equals(fieldName)) {
                // user has changed the field name. Delete the old one first
                m_customPeriodics.remove(fieldName);
                m_customPeriodics.put(newFieldName, toEdit);
                
                // now have to refresh the list in order to show the new name                
                // what's been selected so far?
                int[] selected = m_dateDerivedPeriodicSelector.getSelectedAttributes();
                Instances insts = createDateDerivedPeriodicList();
                m_dateDerivedPeriodicSelector.setInstances(insts);
                m_dateDerivedPeriodicsHeader = insts;
                
                boolean[] newSelected = new boolean[insts.numAttributes()];
                for (int i = 0; i < selected.length; i++) {
                  newSelected[selected[i]] = true;
                }
                try {
                  m_dateDerivedPeriodicSelector.setSelectedAttributes(newSelected);
                } catch (Exception ex) {
                  ex.printStackTrace();
                }
              } else {
                // name unchanged, so can just update the map
                m_customPeriodics.put(fieldName, toEdit);
              }
            } catch (Exception e1) {
              e1.printStackTrace();
            }
          }          
        }
      }
    });
    
    m_deleteCustomPeriodicBut.setEnabled(false);
    m_deleteCustomPeriodicBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int selectedRow = m_dateDerivedPeriodicSelector.getTable().getSelectedRow();
        if (selectedRow > NUM_PREDEFINED_PERIODICS - 1) {
          String fieldName = m_dateDerivedPeriodicsHeader.attribute(selectedRow).name();
          
          // need to trim off the "c_"
          fieldName = fieldName.replaceFirst("c_", "");
          
          // Confirm delete
          int result = JOptionPane.showConfirmDialog(AdvancedConfigPanel.this, 
              "Delete field " + fieldName
              + "?", "Delete custom periodic field", JOptionPane.YES_NO_OPTION);
          if (result == JOptionPane.NO_OPTION) {
            return;
          }
          
          m_customPeriodics.remove(fieldName);
          
          // what's been selected so far?
          int[] selected = m_dateDerivedPeriodicSelector.getSelectedAttributes();
          Instances oldList = m_dateDerivedPeriodicsHeader;

          // now need to rebuild the list
          Instances insts = createDateDerivedPeriodicList();
          m_dateDerivedPeriodicSelector.setInstances(insts);
          m_dateDerivedPeriodicsHeader = insts;
          
          // make sure that selected stuff stays selected
          boolean[] newSelected = new boolean[insts.numAttributes()];
          for (int i = 0; i < selected.length; i++) {
            Attribute toFind = oldList.attribute(selected[i]);
            Attribute toSet = insts.attribute(toFind.name());
            if (toSet != null) {
              newSelected[toSet.index()] = true;
            }
          }
          try {
            m_dateDerivedPeriodicSelector.setSelectedAttributes(newSelected);
          } catch (Exception e1) {
            e1.printStackTrace();
          }
        }
      }
    });
    
    m_addCustomPeriodicBut.setEnabled(false);
    m_addCustomPeriodicBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {        
        ArrayList<CustomPeriodicTest> testList = new ArrayList<CustomPeriodicTest>();
        String fieldName = displayAddEditDialog(testList, null);
        if (fieldName.length() == 0) {          
          // basically do nothing
        } else {
          // Add this new one to the Map
          m_customPeriodics.put(fieldName, testList);

          // what's been selected so far?
          int[] selected = m_dateDerivedPeriodicSelector.getSelectedAttributes();
          Instances insts = createDateDerivedPeriodicList();
          m_dateDerivedPeriodicSelector.setInstances(insts);
          m_dateDerivedPeriodicsHeader = insts;
          
          boolean[] newSelected = new boolean[insts.numAttributes()];
          for (int i = 0; i < selected.length; i++) {
            newSelected[selected[i]] = true;
          }

          // select the newly created field
          newSelected[newSelected.length - 1] = true;
          try {
            m_dateDerivedPeriodicSelector.setSelectedAttributes(newSelected);
          } catch (Exception e1) {
            e1.printStackTrace();
          }
        }
      }
    });
    
    temp.add(checkHolder, BorderLayout.NORTH);
    temp.add(m_dateDerivedPeriodicSelector, BorderLayout.CENTER);
    m_dateDerivedPeriodicSelector.setPreferredScrollableViewportSize(new Dimension(250,80));
    JPanel botP = new JPanel();
    botP.setLayout(new BorderLayout());
    botP.setBorder(BorderFactory.
        createTitledBorder("Date-derived periodic creation"));
    botP.add(temp, BorderLayout.CENTER);
    
    JPanel primaryPeriodicP = new JPanel();
    primaryPeriodicP.setLayout(new BorderLayout());
    //primaryPeriodicP
      
    primaryPeriodicP.add(m_primaryPeriodicCombo, BorderLayout.NORTH);
    JPanel topP = new JPanel();
    topP.setLayout(new BorderLayout());
    topP.setBorder(BorderFactory.createTitledBorder("Periodic attribute"));
    topP.add(primaryPeriodicP, BorderLayout.EAST);
    
    JPanel temp2 = new JPanel();
    temp2.setLayout(new BorderLayout());
    temp2.add(topP, BorderLayout.EAST);
    temp2.add(botP, BorderLayout.CENTER);
//    basePanel.add(primaryPeriodicP, BorderLayout.EAST);
    basePanel.add(temp2, BorderLayout.NORTH);
    
    m_customizeDateDerivedPeriodics.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_instances != null && m_customizeDateDerivedPeriodics.isSelected()) {
          String selectedTimeStamp = 
            m_simpleConfig.m_timeStampCombo.getSelectedItem().toString();
          Attribute timeStampAtt = m_instances.attribute(selectedTimeStamp);
          
          if (timeStampAtt != null && timeStampAtt.isDate()) {
            if (m_dateDerivedPeriodicSelector.getSelectionModel() != null) {
              Instances insts = createDateDerivedPeriodicList();
              
              m_dateDerivedPeriodicSelector.setInstances(insts);
              m_dateDerivedPeriodicsHeader = insts;
              m_addCustomPeriodicBut.setEnabled(true);
              m_savePeriodicBut.setEnabled(true);
              m_loadPeriodicBut.setEnabled(true);
              
              m_dateDerivedPeriodicSelector.getSelectionModel().
                addListSelectionListener(new ListSelectionListener() {
                  public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                      if (m_dateDerivedPeriodicSelector.getTable().
                          getSelectedRow() > NUM_PREDEFINED_PERIODICS - 1) {
                        m_editCustomPeriodicBut.setEnabled(true);
                        m_deleteCustomPeriodicBut.setEnabled(true);
                      } else {
                        m_editCustomPeriodicBut.setEnabled(false);
                        m_deleteCustomPeriodicBut.setEnabled(false);
                      }
                    }
                  }
                });
            }
          }
        } else {
          m_addCustomPeriodicBut.setEnabled(false);
          m_loadPeriodicBut.setEnabled(false);
          m_savePeriodicBut.setEnabled(false);
        }
      }
    });
    
    updatePrimaryPeriodic();
        
    //holder.add(basePanel, BorderLayout.WEST);
    m_configHolder.addTab("Periodic attributes", null, basePanel,
        "Specify/clustomize periodic attributes");
    updateDateDerivedPanel();
  }
  
  /**
   * Layout the learner panel
   */
  protected void layoutLearnerPanel() {
    JPanel basePanel = new JPanel();
    basePanel.setLayout(new BorderLayout());
    basePanel.setBorder(BorderFactory.
        createTitledBorder("Base learner configuration"));
    
    m_baseLearnerEditor.setClassType(Classifier.class);
    m_baseLearnerEditor.setValue(new weka.classifiers.functions.SMOreg());
    
    Capabilities capabilities = new Capabilities(null);
    capabilities.disableAll();
    capabilities.enable(Capability.NOMINAL_ATTRIBUTES);
    capabilities.enable(Capability.NUMERIC_ATTRIBUTES);
    capabilities.enable(Capability.NUMERIC_CLASS);
    capabilities.enableAllAttributeDependencies();
    capabilities.enableAllClassDependencies();
    m_baseLearnerEditor.setCapabilitiesFilter(capabilities);
    
    basePanel.add(m_baseLearnerPanel, BorderLayout.NORTH);
    //add(basePanel, BorderLayout.NORTH);
    m_configHolder.addTab("Base learner", null, basePanel, 
        "Base learner configuration");
  }
  
  /**
   * Layout the lag panel 
   */
  protected void layoutLagPanel() {
    // lag stuff
    JPanel lagPanel = new JPanel();
    lagPanel.setLayout(new GridLayout(1,2));
    //lagPanel.setBorder(BorderFactory.createTitledBorder("Lag creation"));    
    
    SpinnerNumberModel snm = new SpinnerNumberModel();
    snm.setValue(1); snm.setMinimum(1);
    m_minLagSpinner = new JSpinner(snm);
    Dimension spinD = m_minLagSpinner.getPreferredSize();
    spinD = new Dimension((int)(spinD.getWidth() * 1.5), 
        (int)spinD.getHeight());
    m_minLagSpinner.setPreferredSize(spinD);
    
    JPanel temp1 = new JPanel();
    temp1.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
    temp1.setLayout(new BorderLayout());
    final JLabel minLagLab = new JLabel("Minimum lag", JLabel.RIGHT);
    temp1.add(minLagLab, BorderLayout.CENTER);
    temp1.add(m_minLagSpinner, BorderLayout.EAST);
    JPanel spinnerHolder = new JPanel();
    spinnerHolder.setLayout(new BorderLayout());
    spinnerHolder.setBorder(BorderFactory.createTitledBorder("Lag length"));
    
    JPanel vH = new JPanel();
    vH.setLayout(new BorderLayout());

    JPanel varianceHolder = new JPanel();
    varianceHolder.setLayout(new BorderLayout());
    varianceHolder.add(m_adjustForVarianceCheckBox, BorderLayout.EAST);
    m_adjustForVarianceCheckBox.setSelected(false);
    vH.add(varianceHolder, BorderLayout.NORTH);
    
    JPanel checkHolder = new JPanel();
    checkHolder.setLayout(new BorderLayout());
    checkHolder.add(m_useCustomLags, BorderLayout.EAST);
    vH.add(checkHolder, BorderLayout.SOUTH);
    
    spinnerHolder.add(vH, BorderLayout.NORTH);
    spinnerHolder.add(temp1, BorderLayout.CENTER);
    
    snm = new SpinnerNumberModel();
    snm.setValue(12); snm.setMinimum(1);
    m_maxLagSpinner = new JSpinner(snm);
    m_maxLagSpinner.setPreferredSize(spinD);
    JPanel temp2 = new JPanel();
    temp2.setLayout(new BorderLayout());
    temp2.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
    final JLabel maxLagLab = new JLabel("Maximum lag", JLabel.RIGHT);
    temp2.add(maxLagLab, BorderLayout.CENTER);
    temp2.add(m_maxLagSpinner, BorderLayout.EAST);
    
    final JLabel fineTuneLab = new JLabel("Fine tune lag selection", JLabel.RIGHT);
    JPanel fPanel = new JPanel(); fPanel.setLayout(new BorderLayout());
    fPanel.add(temp2, BorderLayout.NORTH);
    
    JPanel fineHolder = new JPanel(); fineHolder.setLayout(new BorderLayout());
    fineHolder.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
    fineHolder.add(fineTuneLab, BorderLayout.CENTER);
    fineHolder.add(m_fineTuneLagsField, BorderLayout.EAST);
    m_fineTuneLagsField.setPreferredSize(spinD);
    fineTuneLab.setEnabled(false);
    fineTuneLab.setToolTipText("Specify ranges to fine tune " +
    		"lags within minimum and maximum (e.g. 2,3,6-8)");
    m_fineTuneLagsField.setEnabled(false);
    m_fineTuneLagsField.setToolTipText("Specify ranges to fine tune " +
        "lags within minimum and maximum (e.g. 2,3,6-8)");
    
    fPanel.add(fineHolder,BorderLayout.SOUTH);
            
    spinnerHolder.add(fPanel, BorderLayout.SOUTH);
    
    m_useCustomLags.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean enabled = m_useCustomLags.isSelected();
        m_minLagSpinner.setEnabled(enabled);
        m_maxLagSpinner.setEnabled(enabled);
        minLagLab.setEnabled(enabled);
        maxLagLab.setEnabled(enabled);
        fineTuneLab.setEnabled(enabled);
        m_fineTuneLagsField.setEnabled(enabled);
        
        m_averageLongLags.setEnabled(enabled);
        if (!enabled) {
          m_averageLongLags.setSelected(false);
        }
        /*m_averageLagsAfter.setEnabled(enabled);
        m_numConsecutiveToAverage.setEnabled(enabled); */
      }
    });    
    
    snm = new SpinnerNumberModel();
    snm.setValue(2); snm.setMinimum(1);
    m_averageLagsAfter = new JSpinner(snm);
    m_averageLagsAfter.setPreferredSize(spinD);
    
    snm = new SpinnerNumberModel();
    snm.setValue(2); snm.setMinimum(2);
    m_numConsecutiveToAverage = new JSpinner(snm);
    m_numConsecutiveToAverage.setPreferredSize(spinD);
    
    JPanel averageLagHolder = new JPanel();
    averageLagHolder.setBorder(BorderFactory.createTitledBorder("Averaging"));
    averageLagHolder.setLayout(new BorderLayout());
    JPanel avCheckHolder = new JPanel();
    avCheckHolder.setLayout(new BorderLayout());
    avCheckHolder.add(m_averageLongLags, BorderLayout.EAST);
    averageLagHolder.add(avCheckHolder, BorderLayout.NORTH);
    JPanel temp3 = new JPanel();
    temp3.setLayout(new BorderLayout());
    temp3.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
    final JLabel avLab =new JLabel("Average lags longer than", JLabel.RIGHT); 
    temp3.add(avLab, BorderLayout.CENTER);
    temp3.add(m_averageLagsAfter, BorderLayout.EAST);
    
    JPanel aH = new JPanel();
    aH.setLayout(new BorderLayout());
    aH.add(temp3, BorderLayout.NORTH);
    
    //averageLagHolder.add(temp3, BorderLayout.CENTER);
    JPanel temp4 = new JPanel();
    temp4.setLayout(new BorderLayout());
    temp4.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
    final JLabel numConsLab = new JLabel("# consecutive lags to average", JLabel.RIGHT); 
    temp4.add(numConsLab, BorderLayout.CENTER);
    temp4.add(m_numConsecutiveToAverage, BorderLayout.EAST);
    aH.add(temp4, BorderLayout.SOUTH);
    averageLagHolder.add(aH, BorderLayout.SOUTH);
    
    m_useCustomLags.setSelected(false);
    m_minLagSpinner.setEnabled(false);
    m_maxLagSpinner.setEnabled(false);
    minLagLab.setEnabled(false);
    maxLagLab.setEnabled(false);
    m_averageLongLags.setEnabled(false);
    avLab.setEnabled(false);
    m_averageLagsAfter.setEnabled(false);
    m_numConsecutiveToAverage.setEnabled(false);
    numConsLab.setEnabled(false);
    
    m_averageLongLags.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_useCustomLags.isSelected()) {
          boolean enabled = m_averageLongLags.isSelected();
          m_averageLagsAfter.setEnabled(enabled);
          m_numConsecutiveToAverage.setEnabled(enabled);
          avLab.setEnabled(enabled);
          numConsLab.setEnabled(enabled);
        }
      }
    });
        
    lagPanel.add(spinnerHolder);//, BorderLayout.CENTER);
    lagPanel.add(averageLagHolder);//, BorderLayout.EAST);
    
    JPanel tempP = new JPanel();
    tempP.setLayout(new BorderLayout());
    tempP.add(lagPanel, BorderLayout.NORTH);
    
    
            
    //add(tempP, BorderLayout.CENTER);
    m_configHolder.addTab("Lag creation", null, tempP, 
        "Customize lagged variables");
  }
  
  /**
   * Apply the configuration defined in this panel to the supplied forecaster
   * 
   * @param forecaster the forecaster to apply the configuration to
   * @throws Exception if a problem occurs
   */
  public void applyToForecaster(WekaForecaster forecaster) throws Exception {
    if (forecaster != null) {
      TSLagMaker lagMaker = forecaster.getTSLagMaker();
      
      // Set variance adjustment
      lagMaker.setAdjustForVariance(m_adjustForVarianceCheckBox.isSelected());
      
      int minLag = ((SpinnerNumberModel)m_minLagSpinner.getModel()).
        getNumber().intValue();
      int maxLag = ((SpinnerNumberModel)m_maxLagSpinner.getModel()).
        getNumber().intValue();
      if (m_useCustomLags.isSelected()) {
        
        // set lag lengths from spinners                
        if (maxLag < minLag) {
          throw new Exception("Maximum lag value must be greater than or equal to" +
          		" the minimum lag value");
        }
        
        if (m_instances != null && maxLag > m_instances.numInstances()) {
          throw new Exception("The maximum lag can't exceed the number instances (" +
          		m_instances.numInstances() + ") in the data!");
        }
        
        lagMaker.setMinLag(minLag); lagMaker.setMaxLag(maxLag);
        
        if (m_fineTuneLagsField.getText() != null && 
            m_fineTuneLagsField.getText().length() > 0) {
          lagMaker.setLagRange(m_fineTuneLagsField.getText());
        }
        
        // set up averaging from spinners
        if (m_averageLongLags.isSelected()) {
          lagMaker.setAverageConsecutiveLongLags(true);
          
          int avLagsAfter = ((SpinnerNumberModel)m_averageLagsAfter.getModel()).
            getNumber().intValue();
          int numToAv = ((SpinnerNumberModel)m_numConsecutiveToAverage.getModel()).
            getNumber().intValue();
          
          if (avLagsAfter < minLag || avLagsAfter > maxLag) {
            throw new Exception("Point at which to start lag averaging must " +
            		"lie between the minimum and maximum lag value.");
          }
          
          lagMaker.setAverageLagsAfter(avLagsAfter);
          lagMaker.setNumConsecutiveLongLagsToAverage(numToAv);
        } else {
          lagMaker.setAverageConsecutiveLongLags(false);
        }
      } else {
        lagMaker.setAverageConsecutiveLongLags(false);
        lagMaker.setLagRange("");
      }
      
      // date-derived periodic customization
      if (getCustomizeDateDerivedPeriodics()) {
        // reset all to false
        lagMaker.setAddAMIndicator(false); lagMaker.setAddDayOfWeek(false);
        lagMaker.setAddMonthOfYear(false); lagMaker.setAddQuarterOfYear(false);
        lagMaker.setAddWeekendIndicator(false); lagMaker.setAddDayOfMonth(false);
        lagMaker.setAddNumDaysInMonth(false);
        
        int[] selected = m_dateDerivedPeriodicSelector.getSelectedAttributes();
        if (m_dateDerivedPeriodicsHeader != null) {
          Map<String, ArrayList<CustomPeriodicTest>> custom =
            new HashMap<String, ArrayList<CustomPeriodicTest>>();
          for (int i = 0; i < selected.length; i++) {
            int s = selected[i];
            String name = m_dateDerivedPeriodicsHeader.attribute(s).name();
            if (s < NUM_PREDEFINED_PERIODICS) {
              if (name.equals("AM")) {
                lagMaker.setAddAMIndicator(true);              
              }
              if (name.equals("DayOfWeek")) {
                lagMaker.setAddDayOfWeek(true);
              }
              if (name.equals("DayOfMonth")) {
                lagMaker.setAddDayOfMonth(true);
              }
              if (name.equals("NumDaysInMonth")) {
                lagMaker.setAddNumDaysInMonth(true);
              }
              if (name.equals("Weekend")) {
                lagMaker.setAddWeekendIndicator(true);
              }
              if (name.equals("Month")) {
                lagMaker.setAddMonthOfYear(true);
              }
              if (name.equals("Quarter")) {
                lagMaker.setAddQuarterOfYear(true);
              }
            } else {
              // remove the "c_"
              name = name.replaceFirst("c_", "");
              ArrayList<CustomPeriodicTest> selectedP = 
                m_customPeriodics.get(name);
              custom.put(name, selectedP);
                
            }
          }
          lagMaker.clearCustomPeriodics();
          if (custom.size() > 0) {            
            lagMaker.setCustomPeriodics(custom);
          }
        }
      }
      
      // non-date primary periodic attribute 
      String ppfn = m_primaryPeriodicCombo.getSelectedItem().toString();
      if (!ppfn.equals("<None>")) {
        lagMaker.setPrimaryPeriodicFieldName(ppfn);
      } else {
        lagMaker.setPrimaryPeriodicFieldName("");
      }
      
      if (m_useOverlayData.isSelected() && m_overlayHeader != null && 
          forecaster instanceof OverlayForecaster) {
        int[] selected = m_overlaySelector.getSelectedAttributes();
        String overlayList = "";
        for (int i = 0; i < selected.length; i++) {
          overlayList += m_overlayHeader.attribute(selected[i]).name() + ",";
        }
        
        if (overlayList.length() > 0) {
          overlayList = overlayList.substring(0, overlayList.lastIndexOf(','));
          ((OverlayForecaster)forecaster).setOverlayFields(overlayList);
        } else {
          ((OverlayForecaster)forecaster).setOverlayFields(null);
        }
      } else {
        if (forecaster instanceof OverlayForecaster) {
          ((OverlayForecaster)forecaster).setOverlayFields(null);
        }
      }
    }
  }
  
  /**
   * Apply the configuration defined in this panel to the supplied evaluation
   * object
   * 
   * @param eval the evaluation object to apply the configuration to
   * @param forecaster the forecaster in use
   * @throws Exception if a problem occurs
   */
  public void applyToEvaluation(TSEvaluation eval, WekaForecaster forecaster) 
    throws Exception {
    
    // eval on training
    eval.setEvaluateOnTrainingData(m_trainingCheckBox.isSelected());
    
    if (!eval.getEvaluateOnTrainingData() && !eval.getEvaluateOnTestData()) {
      // throw new Exception("Must evaluate")
    }
    
    // evaluation modules
    int[] selected = m_evaluationMetrics.getSelectedAttributes();
    
    if (selected.length == 0) {
      throw new Exception("Must select at least one evaluation metric.");
    }
    
    List<TSEvalModule> modsList = TSEvalModule.getModuleList();
    String modListS = "";
    for (int s : selected) {
      String name = m_evaluationModsHeader.attribute(s).name();
      for (TSEvalModule mod : modsList) {
        if (name.equals(mod.toString())) {
          modListS += mod.getEvalName() + ",";
        }
      }
    }
    
    modListS = modListS.substring(0, modListS.lastIndexOf(','));
    eval.setEvaluationModules(modListS);
    
    // TODO separate test set
  }
  
  /**
   * Tests the Weka advanced config panel from the command line.
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
      AdvancedConfigPanel acp = new AdvancedConfigPanel(scp);
      //acp.setInstances(i);
      
      final javax.swing.JFrame jf =
        new javax.swing.JFrame("Weka Forecasting");
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(acp, BorderLayout.CENTER);
      jf.addWindowListener(new java.awt.event.WindowAdapter() {
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
