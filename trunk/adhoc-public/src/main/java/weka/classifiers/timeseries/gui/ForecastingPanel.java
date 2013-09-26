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
 *    ForecastingPanel.java
 *    Copyright (C) 2010 Pentaho Corporation
 */

package weka.classifiers.timeseries.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import weka.classifiers.timeseries.AbstractForecaster;
import weka.classifiers.timeseries.TSForecaster;
import weka.classifiers.timeseries.WekaForecaster;
import weka.classifiers.timeseries.core.OverlayForecaster;
import weka.classifiers.timeseries.core.TSLagMaker;
import weka.classifiers.timeseries.core.TSLagUser;
import weka.classifiers.timeseries.eval.TSEvaluation;
import weka.classifiers.timeseries.eval.graph.GraphDriver;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.gui.BrowserHelper;
import weka.gui.LogPanel;
import weka.gui.Logger;
import weka.gui.ResultHistoryPanel;
import weka.gui.TaskLogger;
import weka.gui.WekaTaskMonitor;
import weka.gui.beans.KnowledgeFlowApp;
import weka.gui.beans.TimeSeriesForecasting;
import weka.gui.beans.TimeSeriesForecastingKFPerspective;

/**
 * Main GUI panel for the forecasting environment.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 */
public class ForecastingPanel extends JPanel { 
  
  /** For serialization */
  private static final long serialVersionUID = -8415151090793037265L;

  /** The training instances to operate on */
  protected Instances m_instances;
  
  /** Panel for logging */
  protected Logger m_log;// = new LogPanel(new WekaTaskMonitor());
  
  /** The simple configuration panel */
  protected SimpleConfigPanel m_simpleConfigPanel;
  
  /** The advanced configuration panel */
  protected AdvancedConfigPanel m_advancedConfigPanel;    
  
  /** The current forecaster */
  protected WekaForecaster m_forecaster = new WekaForecaster();
  
  /** Tabbed pane to hold the simple and advanced config panels */
  protected JTabbedPane m_configPane = new JTabbedPane();
  
  /** The main textual output area */
  protected JTextArea m_outText = new JTextArea(20, 50);
  
  /** A panel holding and controlling the results for viewing */
  protected ResultHistoryPanel m_history = new ResultHistoryPanel(m_outText);
  
  /** Button to launch the forecaster */
  protected JButton m_startBut = new JButton("Start");
  
  /** Button to stop processing */
  protected JButton m_stopBut = new JButton("Stop");
  
  /** Button to visit help docs in browser */
  protected JButton m_helpBut = new JButton("Help");
  
  /** The split panel to divided the history/results area from the configuration */
  protected JSplitPane m_splitP;
  
  protected Thread m_runThread;
  
  /** True if we are running in the KnowledgeFlow as a perspective */
  protected boolean m_isRunningAsPerspective = false;
  
  /** The file chooser for selecting model files. */
  protected JFileChooser m_fileChooser 
    = new JFileChooser(new File(System.getProperty("user.dir")));
  
  /**
   *  For each dataset, perform a check (if a timestamp is specified)
   * just once to see if it is in ascending order
   */
  protected boolean m_sortedCheck;
  
  /**
   * Tabbed pane that holds the main text output plus tabs for
   * any generated graphs
   */
  JTabbedPane m_outputPane = new JTabbedPane();
  
  /**
   * Inner class extending Thread. Executes a forecasting task
   */
  protected class ForecastingThread extends Thread {
    
    protected boolean m_configAndBuild = true;
    
    protected WekaForecaster m_threadForecaster = null;
    
    protected String m_name;
    
    public ForecastingThread(WekaForecaster forecaster, String name) {
      m_threadForecaster = forecaster;
      m_name = name;
    }
    
    public void setConfigureAndBuild(boolean configAndBuild) {
      m_configAndBuild = configAndBuild;
    }

    public void run() {

      LogPrintStream logger = new LogPrintStream();          
      logger.println("Setting up...");

      String name = m_name;
      StringBuffer outBuff = null;
      
      if (name == null) {
        name = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
        outBuff = new StringBuffer();
      }      
      
      String fname = "";
      try {            

        if (!m_sortedCheck) {
          sortCheck();
        }

        // copy the current state of things
        Instances inst = new Instances(m_instances);

        TSEvaluation eval = new TSEvaluation(inst, 
            m_advancedConfigPanel.getHoldoutSetSize());                
        
        if (m_configAndBuild) {
          // configure the WekaForecaster with the base
          // learner
          m_threadForecaster.setBaseForecaster(m_advancedConfigPanel.getBaseClassifier());

          m_simpleConfigPanel.applyToForecaster(m_threadForecaster);
          m_advancedConfigPanel.applyToForecaster(m_threadForecaster);
        }
        
        m_simpleConfigPanel.applyToEvaluation(eval, m_threadForecaster);
        m_advancedConfigPanel.applyToEvaluation(eval, m_threadForecaster);

        eval.setForecastFuture(m_advancedConfigPanel.getOutputFuturePredictions() ||
            m_advancedConfigPanel.getGraphFuturePredictions());
        
        if (m_threadForecaster instanceof OverlayForecaster &&
            ((OverlayForecaster)m_threadForecaster).isUsingOverlayData()) {
          if (!eval.getEvaluateOnTestData() &&
              (m_advancedConfigPanel.m_outputFutureCheckBox.isSelected() ||
                  m_advancedConfigPanel.m_graphFutureCheckBox.isSelected())) {

            // warn the user that future forecast can't be produced for the training data
            dontShowMessageDialog("weka.classifiers.timeseries.gui.CantFutureForecastTraining",
                "Unable to generate a future forecast beyond the end of the training\n" +
                "data because there is no future overlay data available. Use a holdout\n" +
                "set for evaluation in order to simulate having \"future\" overlay\n" +
                "data available.\n\n",
            "ForecastingPanel");
          }
          
          if (eval.getEvaluateOnTestData() && 
              (m_advancedConfigPanel.m_outputFutureCheckBox.isSelected() ||
                  m_advancedConfigPanel.m_graphFutureCheckBox.isSelected())) {
            
            // warn the user that future forecast can't be produced for the test data
            dontShowMessageDialog("weka.classifiers.timeseries.gui.CantFutureForecastTesting",
                "Unable to generate a future forecast beyond the end of the test\n" +
                "data because there is no future overlay data available.\n\n",
            "ForecastingPanel");
          }
        }                


        fname = m_threadForecaster.getAlgorithmName();
        
        if (m_name == null) {
          String algoName = fname.substring(0, fname.indexOf(' '));
          if (algoName.startsWith("weka.classifiers.")) {
            name += algoName.substring("weka.classifiers.".length());
          } else {
            name += algoName;
          }
        }        
        
        String lagOptions = "";
        if (m_threadForecaster instanceof TSLagUser) {
          TSLagMaker lagMaker = ((TSLagUser)m_threadForecaster).getTSLagMaker();
          lagOptions = Utils.joinOptions(lagMaker.getOptions());
        }
        
        if (lagOptions.length() > 0 && m_name == null) {
          name += " [" + lagOptions + "]";
        }
        
        if (m_name == null) {
          outBuff.append("=== Run information ===\n\n");
        } else {
          outBuff = m_history.getNamedBuffer(name);
          outBuff.append("\n=== Model re-evaluation===\n\n");
        }
        
        outBuff.append("Scheme:\n\t" + fname).append("\n\n");
        
        if (lagOptions.length() > 0) {
          outBuff.append("Lagged and derived variable options:\n\t").
          append(lagOptions + "\n\n");
        }

        outBuff.append("Relation:     " + inst.relationName() + '\n');
        outBuff.append("Instances:    " + inst.numInstances() + '\n');
        outBuff.append("Attributes:   " + inst.numAttributes() + '\n');
        if (inst.numAttributes() < 100) {
          for (int i = 0; i < inst.numAttributes(); i++) {
            outBuff.append("              " + inst.attribute(i).name()
                + '\n');
          }
        } else {
          outBuff.append("              [list of attributes omitted]\n");
        }
        
        if (m_configAndBuild) {
          m_history.addResult(name, outBuff);
        }
        m_history.setSingle(name);

        if (m_log != null) {
          m_log.logMessage("Started " + fname);
          if (m_configAndBuild) {
            logger.println("Training forecaster...");
          }
          if (m_log instanceof TaskLogger) {
            ((TaskLogger)m_log).taskStarted();
          }
        }

        Instances trainInst = eval.getTrainingData();
        if (m_configAndBuild) {
          m_threadForecaster.buildForecaster(trainInst, logger);
          outBuff.append("\n" + m_threadForecaster.toString());
          m_history.updateResult(name);
        }

        if (eval.getEvaluateOnTrainingData() || 
            eval.getEvaluateOnTestData()) {
          logger.println("Evaluating...");
        }

        // evaluate the forecaster
        eval.evaluateForecaster(m_threadForecaster, false, logger);

        // output any predictions
        if (m_advancedConfigPanel.getOutputPredictionsAtStep() > 0) {
          int step = m_advancedConfigPanel.getOutputPredictionsAtStep();
          String targetName = m_advancedConfigPanel.getOutputPredictionsTarget();
          String fieldsToForecast = m_threadForecaster.getFieldsToForecast();
          if (!fieldsToForecast.contains(targetName)) {
            throw new Exception("Cannot output predictions for \"" 
                + targetName + "\" because that field is not being predicted.");
          }
          if (eval.getTrainingData() != null &&
              eval.getEvaluateOnTrainingData()) {
            String predString = eval.printPredictionsForTrainingData("=== Predictions " +
                "for training data: " + targetName + " (" 
                + step + (step > 1 ? "-steps ahead)" : "-step ahead)") + " ===", 
                targetName, step, eval.getPrimeWindowSize());
            outBuff.append("\n").append(predString);
          }              

          if (eval.getTestData() != null) {
            int instanceNumOffset = 
              (eval.getTrainingData() != null && 
                  m_advancedConfigPanel.getHoldoutSetSize() > 0) 
                  ? eval.getTrainingData().numInstances()
                      : 0;

                  String predString = eval.printPredictionsForTestData("=== Predictions " +
                      "for test data: " + targetName + " (" 
                      + step + (step > 1 ? "-steps ahead)" : "-step ahead)") + " ===", 
                      targetName, step, instanceNumOffset);
                  outBuff.append("\n").append(predString);
          }
          m_history.updateResult(name);
        }

        // output any future predictions
        if (m_advancedConfigPanel.getOutputFuturePredictions()) {
          if (eval.getTrainingData() != null /*&& 
                eval.getEvaluateOnTrainingData()*/) {
            outBuff.append("\n=== Future predictions from end of training data ===\n");
            outBuff.append(eval.printFutureTrainingForecast(m_threadForecaster));
          }

          if (eval.getTestData() != null && 
              eval.getEvaluateOnTestData()) {
            outBuff.append("\n=== Future predictions from end of test data ===\n");
            outBuff.append(eval.printFutureTestForecast(m_threadForecaster));
          }
          m_history.updateResult(name);
        }

        // evaluation summary
        if (eval.getEvaluateOnTrainingData() || eval.getEvaluateOnTestData()) {
          outBuff.append("\n" + eval.toSummaryString());
          m_history.updateResult(name);
        }

        // result object list
        List<Object> resultList = (m_configAndBuild) 
        ? new ArrayList<Object>() 
            : (List<Object>)m_history.getNamedObject(name);
        
        if (!m_configAndBuild) {
          // go through and remove any JPanels
          List<Object> newResultList = new ArrayList<Object>();
          for (int z = 0; z < resultList.size(); z++) {
            if (resultList.get(z) instanceof TSForecaster || 
                resultList.get(z) instanceof Instances) {
              newResultList.add(resultList.get(z));
            }
          }
          resultList = newResultList;
        }

        // handle graphs
        List<JPanel> graphList = new ArrayList<JPanel>();
        // graph predictions for targets at specific step
        if (m_advancedConfigPanel.getGraphPredictionsAtStep() > 0) {
          int stepNum = m_advancedConfigPanel.getGraphPredictionsAtStep();
          List<String> targets = 
            AbstractForecaster.stringToList(m_threadForecaster.getFieldsToForecast());
          if (eval.getTrainingData() != null && eval.getEvaluateOnTrainingData()) {                
            JPanel trainTargetsAtStep = 
              eval.graphPredictionsForTargetsOnTraining(GraphDriver.getDefaultDriver(), 
                  m_threadForecaster, targets, stepNum, eval.getPrimeWindowSize());
            trainTargetsAtStep.setToolTipText("Train pred. for targets");

            graphList.add(trainTargetsAtStep);
          }

          if (eval.getTestData() != null && eval.getEvaluateOnTestData()) {
            int instanceOffset = (eval.getPrimeForTestDataWithTestData())
            ? eval.getPrimeWindowSize()
                : 0;
            JPanel testTargetsAtStep =
              eval.graphPredictionsForTargetsOnTesting(GraphDriver.getDefaultDriver(), 
                  m_threadForecaster, targets, stepNum, instanceOffset);
            testTargetsAtStep.setToolTipText("Test pred. for targets");

            graphList.add(testTargetsAtStep);
          }
        }

        // graph predictions for specific target at selected steps
        if (m_advancedConfigPanel.getGraphTargetForSteps()) {
          String fieldsToForecast = m_threadForecaster.getFieldsToForecast();
          String selectedTarget = 
            m_advancedConfigPanel.getGraphTargetForStepsTarget();

          if (!fieldsToForecast.contains(selectedTarget)) {
            throw new Exception("Cannot graph predictions for \"" 
                + selectedTarget + "\" because that field is not being predicted.");
          }
          List<Integer> stepList = 
            m_advancedConfigPanel.getGraphTargetForStepsStepList();

          if (eval.getTrainingData() != null && eval.getEvaluateOnTrainingData()) {
            JPanel trainStepsForTarget = 
              eval.graphPredictionsForStepsOnTraining(GraphDriver.getDefaultDriver(), 
                  m_threadForecaster, selectedTarget, stepList, eval.getPrimeWindowSize());
            trainStepsForTarget.setToolTipText("Train pred. at steps");

            graphList.add(trainStepsForTarget);
          }

          if (eval.getTestData() != null && eval.getEvaluateOnTestData()) {
            int instanceOffset = (eval.getPrimeForTestDataWithTestData())
            ? eval.getPrimeWindowSize()
                : 0;
            JPanel testStepsForTarget =
              eval.graphPredictionsForStepsOnTesting(GraphDriver.getDefaultDriver(), 
                  m_threadForecaster, selectedTarget, stepList, instanceOffset);
            testStepsForTarget.setToolTipText("Test pred. at steps");

            graphList.add(testStepsForTarget);
          }
        }

        // graph future predictions
        if (m_advancedConfigPanel.getGraphFuturePredictions()) {
          if (eval.getTrainingData() != null /*&& eval.getEvaluateOnTrainingData()*/) {
            try {
              JPanel trainFuture = eval.
              graphFutureForecastOnTraining(GraphDriver.getDefaultDriver(), 
                  m_threadForecaster, AbstractForecaster.
                  stringToList(m_threadForecaster.getFieldsToForecast()));
              trainFuture.setToolTipText("Train future pred.");

              graphList.add(trainFuture);
            } catch (Exception ex) {
              if (m_threadForecaster instanceof OverlayForecaster &&
                  ((OverlayForecaster)m_threadForecaster).isUsingOverlayData()) {
                if (m_log != null) {
                  m_log.logMessage("Unable to graph future forecast for training " +
                  		"data because no future overlay data is available");
                }
              } else {
                if (m_log != null) {
                  m_log.logMessage("Unable to graph future forecast for " +
                  		"training data: " + ex.getMessage());
                }
              }
            }
          }

          if (eval.getTestData() != null && eval.getEvaluateOnTestData()) {
            try {
              JPanel testFuture = eval.
              graphFutureForecastOnTesting(GraphDriver.getDefaultDriver(), 
                  m_threadForecaster, AbstractForecaster.
                  stringToList(m_threadForecaster.getFieldsToForecast()));
              testFuture.setToolTipText("Test future pred.");

              graphList.add(testFuture);
            } catch (Exception ex) {
              if (m_threadForecaster instanceof OverlayForecaster &&
                  ((OverlayForecaster)m_threadForecaster).isUsingOverlayData()) {
                if (m_log != null) {
                  m_log.logMessage("Unable to graph future forecast for test " +
                        "data because no future overlay data is available");
                }
              } else {
                if (m_log != null) {
                  m_log.logMessage("Unable to graph future forecast for test " +
                  		"data: " + ex.getMessage());
                }
              }
            }
          }
        }

        try {
          if (m_configAndBuild) {
            WekaForecaster copiedForecaster = 
              (WekaForecaster)AbstractForecaster.makeCopy(m_threadForecaster);
            resultList.add(copiedForecaster);
            Instances trainHeader = new Instances(trainInst, 0);
            resultList.add(trainHeader);
          }
        } catch (Exception ex) {
          if (m_log != null) {
            logger.println("Problem copying model.");
            m_log.logMessage("Problem copying model: " + ex.getMessage());
          }
          ex.printStackTrace();
        }

        if (graphList.size() > 0) {
          resultList.add(graphList);              
        }

        m_history.addObject(name, resultList);
        if (graphList.size() > 0) {
          updateMainTabs(name);
        }

        if (m_log != null) {
          m_log.logMessage("Finished " + fname);
          logger.println("OK");
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        if (m_log != null) {
          m_log.logMessage(ex.getMessage());
          logger.println("Problem evaluating forecaster");
        }
        JOptionPane.showMessageDialog(ForecastingPanel.this, 
            "Problem evaluating forecaster:\n" + ex.getMessage(),
            "Evaluate forecaster", JOptionPane.ERROR_MESSAGE);

      } finally {

        if (isInterrupted()) {
          if (m_log != null) {
            m_log.logMessage("Interrupted " + fname);
            logger.println("Interrupted");
          }
        }

        synchronized (this) {
          m_startBut.setEnabled(true);
          m_stopBut.setEnabled(false);
          m_runThread = null;
        }

        if (m_log instanceof TaskLogger) {
          ((TaskLogger)m_log).taskFinished();
        }
      }
    }
  }
  
  /**
   * Constructor.
   * 
   * @param log the log to use (may be null for no log)
   * @param displayWelcome true if the welcome message is to be
   * displayed as the first log entry
   */
  public ForecastingPanel(LogPanel log, boolean displayWelcome) {
    this(log, displayWelcome, true, false);
  }
  
  /**
   * Constructor
   * 
   * @param log the log to use (may be null for no log)
   * @param displayLogWelcome true if the welcome message is to be
   * displayed as the first log entry
   * @param allowSeparateTestSet true if the separate test set button is
   * to be displayed
   */
  public ForecastingPanel(LogPanel log, boolean displayLogWelcome, 
      boolean allowSeparateTestSet, boolean kfPerspective) {
    m_isRunningAsPerspective = kfPerspective;
    
    m_simpleConfigPanel = new SimpleConfigPanel(this);
    
    m_advancedConfigPanel = 
      new AdvancedConfigPanel(m_simpleConfigPanel, allowSeparateTestSet);
    
    setLayout(new BorderLayout());
    
    m_log = log;
    if (m_log != null) {
      if (displayLogWelcome) {
        String date = (new SimpleDateFormat("EEEE, d MMMM yyyy")).format(new Date());
        m_log.logMessage("Weka Forecaster");
        /*m_logPanel.logMessage("(c) " + Copyright.getFromYear() + "-" + Copyright.getToYear() 
        + " " + Copyright.getOwner() + ", " + Copyright.getAddress()); */
        // m_logPanel.logMessage("web: " + Copyright.getURL());
        m_log.logMessage("Started on " + date);
        m_log.statusMessage("Welcome to the Weka Forecaster");
      }
    }
    
    m_simpleConfigPanel.setAdvancedConfig(m_advancedConfigPanel);
    m_configPane.addTab(m_simpleConfigPanel.getTabTitle(), null, 
        m_simpleConfigPanel, m_simpleConfigPanel.getTabTitleToolTip());
    m_configPane.addTab(m_advancedConfigPanel.getTabTitle(), null, 
        m_advancedConfigPanel, m_advancedConfigPanel.getTabTitleToolTip());
    
    m_outText.setEditable(false);
    m_outText.setFont(new Font("Monospaced", Font.PLAIN, 12));
    m_outText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    final JScrollPane js = new JScrollPane(m_outText);
    js.getViewport().addChangeListener(new ChangeListener() {
      private int lastHeight;
      public void stateChanged(ChangeEvent e) {
        JViewport vp = (JViewport)e.getSource();
        int h = vp.getViewSize().height; 
        if (h != lastHeight) { // i.e. an addition not just a user scrolling
          lastHeight = h;
          int x = h - vp.getExtentSize().height;
          vp.setViewPosition(new Point(0, x));
        }
      }
    });
    
    m_outputPane.setBorder(BorderFactory.createTitledBorder("Output/Visualization"));
    
    m_outputPane.addTab("Output", null, js, "Forecaster output");
    
    m_history.setBorder(BorderFactory.createTitledBorder("Result list"));
    m_history.getList().getSelectionModel().
      addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {

          synchronized (ForecastingPanel.this) {
            if (!e.getValueIsAdjusting()) {
              ListSelectionModel lm = (ListSelectionModel)e.getSource();
              for (int j = e.getFirstIndex(); j <= e.getLastIndex(); j++) {
                if (lm.isSelectedIndex(j)) {
                  String name = m_history.getSelectedName();
                  updateMainTabs(name);
                }
              }
            }
          }
        }
      });
    
    m_history.getList().addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (((e.getModifiers() & InputEvent.BUTTON1_MASK)
            != InputEvent.BUTTON1_MASK) || e.isAltDown()) {
         int index = m_history.getList().locationToIndex(e.getPoint());
         if (index != -1) {
           String name = m_history.getNameAtIndex(index);
           resultPopup(name, e.getX(), e.getY());
         } else {
           resultPopup(null, e.getX(), e.getY());
         }
       }
      }
    });
    
    m_history.setHandleRightClicks(false);
    
//    add(m_configPane, BorderLayout.NORTH);
    
    JPanel lowerPanel = new JPanel();
    lowerPanel.setLayout(new BorderLayout());
    JPanel butAndHistHolder = new JPanel();
    butAndHistHolder.setLayout(new BorderLayout());
    butAndHistHolder.add(m_history, BorderLayout.CENTER);
    JPanel butHolder = new JPanel();
    butHolder.setLayout(new GridLayout(1,3));
    butHolder.add(m_startBut);
    butHolder.add(m_stopBut);
    butHolder.add(m_helpBut);
    
    m_startBut.setToolTipText("Start the forecasting process");
    m_stopBut.setToolTipText("Stop the running forecasting process");
    butAndHistHolder.add(butHolder, BorderLayout.NORTH);
    m_startBut.setEnabled(false); m_stopBut.setEnabled(false);
    
    m_stopBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stopForecaster();
      }
    });
    
    m_helpBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        BrowserHelper.openURL(ForecastingPanel.this, 
            "http://wiki.pentaho.com/display/DATAMINING/" +
            "Time+Series+Analysis+and+Forecasting+with+Weka");
      }
    });
    m_helpBut.setToolTipText("Visit documentation for the time series " +
    		"environment in your browser");
    
    lowerPanel.add(butAndHistHolder, BorderLayout.WEST);
    lowerPanel.add(m_outputPane, BorderLayout.CENTER);
    
    m_splitP = new JSplitPane(JSplitPane.VERTICAL_SPLIT, m_configPane, lowerPanel);
    m_splitP.setOneTouchExpandable(true);

//    add(lowerPanel, BorderLayout.CENTER);
    add(m_splitP, BorderLayout.CENTER);
    
    if (m_log != null && m_log instanceof JComponent) {
      add((JComponent)m_log, BorderLayout.SOUTH);
    }
    
    double width = m_history.getPreferredSize().width;
    int height = m_history.getPreferredSize().height;    
    width *= 0.75;
    m_history.setPreferredSize(new Dimension((int)width, height));
    
    m_startBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        startForecaster(m_forecaster);
      }
    });
  }  
  
  /**
   * Enable the start button
   * 
   * @param enable true if the start button is to be enabled
   */
  protected void enableStartButton(boolean enable) {
    m_startBut.setEnabled(enable);
  }
  
  /** Map used to store the tabs containing graph output */
  protected HashMap<String, JTabbedPane> m_framedOutputMap = 
    new HashMap<String, JTabbedPane>();
  
  /**
   * Opens the named result in a separate frame
   * 
   * @param name the name of the result from the history list to use
   */
  protected void openResultFrame(String name) {
    StringBuffer buffer = m_history.getNamedBuffer(name);
    JTabbedPane tabbedPane = m_framedOutputMap.get(name);

    if (buffer != null && tabbedPane == null) {
      JTextArea textA = new JTextArea(20, 50);
      textA.setEditable(false);
      textA.setFont(new Font("Monospaced", Font.PLAIN, 12));
      textA.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      textA.setText(buffer.toString());
      tabbedPane = new JTabbedPane();
      tabbedPane.addTab("Output", new JScrollPane(textA));
      updateComponentTabs(name, tabbedPane);
      m_framedOutputMap.put(name, tabbedPane);
      
      final JFrame jf = new JFrame(name);
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          m_framedOutputMap.remove(jf.getTitle());
          jf.dispose();
        }
      });
      jf.setLayout(new BorderLayout());
      jf.add(tabbedPane, BorderLayout.CENTER);
      jf.pack();
      jf.setSize(550, 400);
      jf.setVisible(true);
    }        
  }
  
  /**
   * Pops up a contextual menu in the result history list
   * 
   * @param name the selected entry in the list
   * @param x the x position for the popup
   * @param y the y position for the popup
   */
  protected void resultPopup(final String name, int x, int y) {
    final String selectedName = name;
    JPopupMenu resultListMenu = new JPopupMenu();
    
    JMenuItem showMainBuff = new JMenuItem("View in main window");
    if (selectedName != null) {
      showMainBuff.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          m_history.setSingle(selectedName);
          updateMainTabs(selectedName);
        }
      });
    } else {
      showMainBuff.setEnabled(false);
    }
    resultListMenu.add(showMainBuff);
    
    JMenuItem showSepBuff = new JMenuItem("View in separate window");
    if (selectedName != null) {
      showSepBuff.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          openResultFrame(selectedName);
        }
      });
    } else {
      showSepBuff.setEnabled(false);
    }
    resultListMenu.add(showSepBuff);    
    
    JMenuItem deleteResultBuff = new JMenuItem("Delete result");
    if (selectedName != null && m_runThread == null) {
      deleteResultBuff.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          m_history.removeResult(selectedName);
        }
      });
    } else {
      deleteResultBuff.setEnabled(false);
    }

    
    resultListMenu.add(deleteResultBuff);
    
    resultListMenu.addSeparator();
    List<Object> resultList = null;
    if (selectedName != null) { 
      resultList = (List<Object>)m_history.getNamedObject(name);
    }
    
    WekaForecaster saveForecaster = null;
    Instances saveForecasterStructure = null;
    if (resultList != null) {
      for (Object o : resultList) {
        if (o instanceof WekaForecaster){
          saveForecaster = (WekaForecaster)o;
        } else if (o instanceof Instances) {
          saveForecasterStructure = (Instances)o;
        }
      }
    }
    
    final WekaForecaster toSave = saveForecaster;
    final Instances structureToSave = saveForecasterStructure;
    JMenuItem saveForecasterMenuItem = new JMenuItem("Save forecasting model");
    if (saveForecaster != null) {
      saveForecasterMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveForecaster(name, toSave, structureToSave); 
        }
      });
    } else {
      saveForecasterMenuItem.setEnabled(false);
    }
    resultListMenu.add(saveForecasterMenuItem);
    
    JMenuItem loadForecasterMenuItem = new JMenuItem("Load forecasting model");
    resultListMenu.add(loadForecasterMenuItem);
    loadForecasterMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        loadForecaster();
      }
    });
    
    if (m_isRunningAsPerspective) {
      JMenuItem copyToKFClipboardMenuItem = 
        new JMenuItem("Copy model to Knowledge Flow clipboard");
      resultListMenu.add(copyToKFClipboardMenuItem);
      copyToKFClipboardMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
            KnowledgeFlowApp singleton = KnowledgeFlowApp.getSingleton();
            String encoded = 
              TimeSeriesForecasting.encodeForecasterToBase64(toSave, structureToSave);
            
            TimeSeriesForecasting component = new TimeSeriesForecasting();
            component.setEncodedForecaster(encoded);                        

            TimeSeriesForecastingKFPerspective.setClipboard(component);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      });
    }
    
    
    JMenuItem reevaluateModelItem = new JMenuItem("Re-evaluate model");
    if (selectedName != null && m_runThread == null) {
      
      reevaluateModelItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          reevaluateForecaster(selectedName, toSave, structureToSave);
        }
      });
      
      reevaluateModelItem.
        setEnabled((m_advancedConfigPanel.m_trainingCheckBox.isSelected() ||
            m_advancedConfigPanel.m_holdoutCheckBox.isSelected()) &&
            m_instances != null);
    } else {
      reevaluateModelItem.setEnabled(false);
    }            
    
    resultListMenu.add(reevaluateModelItem);
    
    resultListMenu.show(m_history.getList(), x, y);
  }
  
  /**
   * Load a forecaster and add it to the history list
   */
  protected void loadForecaster() {
    File sFile = null;
    int returnVal = m_fileChooser.showOpenDialog(this);
    if (returnVal == m_fileChooser.APPROVE_OPTION) {
      sFile = m_fileChooser.getSelectedFile();
      
      if (m_log != null) {
        m_log.statusMessage("Loading forecaster...");
      }
      
      Object f = null;
      Instances header = null;
      boolean loadOK = true;
      try {
        ObjectInputStream is = new ObjectInputStream(new FileInputStream(sFile));
        f = is.readObject();
        header = (Instances)is.readObject();
        is.close();
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, ex, "Load failed",
            JOptionPane.ERROR_MESSAGE);
        loadOK = false;
      }
      
      if (!loadOK) {
        if (m_log != null) {
          m_log.logMessage("Loading from file " + sFile.getName() + "' failed.");
          m_log.statusMessage("OK");
        }
      } else if (!(f instanceof WekaForecaster)) {
        JOptionPane.showMessageDialog(this, 
            "Loaded model is not a weka forecaster!", "Weka forecasting", 
            JOptionPane.ERROR_MESSAGE);
        loadOK = false;
      }
      
      if (loadOK) {
        String name = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
        StringBuffer outBuff = new StringBuffer();
        WekaForecaster wf = (WekaForecaster)f;        
        
        String lagOptions = "";
        if (wf instanceof TSLagUser) {
          TSLagMaker lagMaker = ((TSLagUser)wf).getTSLagMaker();
          lagOptions = Utils.joinOptions(lagMaker.getOptions());
        }
        
        String fname = wf.getAlgorithmName();
        String algoName = fname.substring(0, fname.indexOf(' '));
        if (algoName.startsWith("weka.classifiers.")) {
          name += algoName.substring("weka.classifiers.".length());
        } else {
          name += algoName;
        }
        name += " loaded from '" + sFile.getName() + "'";
        
        outBuff.append("Scheme:\n\t" + fname).append("\n");
        outBuff.append("loaded from '" + sFile.getName() + "'\n\n");
        
        if (lagOptions.length() > 0) {
          outBuff.append("Lagged and derived variable options:\n\t").
          append(lagOptions + "\n\n");
        }
        
        outBuff.append(wf.toString());
        
        m_history.addResult(name, outBuff);
        m_history.setSingle(name);
        
        List<Object> resultList = new ArrayList<Object>();
        resultList.add(wf); resultList.add(header);
        m_history.addObject(name, resultList);
        updateMainTabs(name);        
      }      
    }
  }
  
  /**
   * Serialize a forecaster out to a file
   * 
   * @param name the name of forecaster to save
   * @param forecaster the actual forecaster to save
   * @param structure the structure of the instances used to train the forecaster
   */
  protected void saveForecaster(String name, TSForecaster forecaster, 
      Instances structure) {
    File sFile = null;
    boolean saveOK = true;
    
    int returnVal = m_fileChooser.showSaveDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      sFile = m_fileChooser.getSelectedFile();
      if (!sFile.getName().toLowerCase().endsWith(".model")) {
        sFile = new File(sFile.getParent(), sFile.getName() 
                         + ".model");
      }
      if (m_log != null) {
        m_log.statusMessage("Saving forecaster to file...");
      }
      
      try {
        ObjectOutputStream oos =
          new ObjectOutputStream(new FileOutputStream(sFile));
        oos.writeObject(forecaster);
        if (structure != null) {
          oos.writeObject(new Instances(structure, 0));
        }
        oos.close();
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, ex, "Save Failed",
            JOptionPane.ERROR_MESSAGE);
        saveOK = false;
      }
      
      if (saveOK) {
        if (m_log != null) {
          m_log.logMessage("Saved model (" + name
            + " ) to file '" + sFile.getName() + "'");
          m_log.statusMessage("OK");
        }        
      }
    }    
  }
  
  /**
   * Updates the display with the results from the currently selected
   * entry in the result history list
   * 
   * @param name the name of the entry from which to display results
   * @param outputPane the output pane to update
   */
  protected synchronized void updateComponentTabs(String name, 
      JTabbedPane outputPane) {
    // remove any tabs that are not the text output
    int numTabs = outputPane.getTabCount();
    if (numTabs > 1) {
      for (int i = numTabs - 1; i > 0; i--) {
        outputPane.removeTabAt(i);
      }
    }

    // see if there are any graphs associated with this name
    List<Object> storedResults = (List<Object>)m_history.getNamedObject(name);
    List<JPanel> graphList = null;
    if (storedResults != null) {
      for (Object o : storedResults) {
        if (o instanceof List<?>) {
          graphList = (List<JPanel>)o;
        }
      }
    }
    
    if (graphList != null && graphList.size() > 0) {                 
      // add the graphs
      for (JPanel p : graphList) {
        outputPane.addTab(p.getToolTipText(), p);
      }
    }
  }
  
  /**
   * Updates the tabs in the main display.
   * 
   * @param entryName the entry name of the currently displayed results. If
   * the selected result is the same as the current then no update is done.
   */
  protected synchronized void updateMainTabs(String entryName) {
    String name = m_history.getSelectedName();
    if (!name.equals(entryName)) {
      return;
    }
    updateComponentTabs(name, m_outputPane);
  }
  
  /**
   * Set the log to use
   * 
   * @param log the log to use
   */
  public void setLog(Logger log) {
    if (log instanceof JComponent) {
      if (m_log != null) {
        remove((JComponent)m_log);
      }
      m_log = log;
      add((JComponent)m_log, BorderLayout.SOUTH);
    }
  }
  
  /**
   * Set the training instances to use
   * 
   * @param insts the training instances to use
   * @throws Exception if a problem occurs
   */
  public void setInstances(Instances insts) throws Exception {
    
    // if this is the first set of instances seen then
    // install a listener on the simple config target panel's
    // table model so that we can enable/disable the start
    // button
    m_sortedCheck = false;
    boolean first = 
      (m_simpleConfigPanel.m_targetPanel.getTableModel() == null);
    
    m_instances = new Instances(insts);
    m_simpleConfigPanel.setInstances(insts);
    m_advancedConfigPanel.setInstances(insts);
    
    if (first) {            
      TableModel model = m_simpleConfigPanel.m_targetPanel.getTableModel();
      model.addTableModelListener(new TableModelListener() {
        public void tableChanged(TableModelEvent e) {
          int[] selected = m_simpleConfigPanel.m_targetPanel.getSelectedAttributes();
          if (selected != null && selected.length > 0) {
            m_startBut.setEnabled(true);
          } else {
            m_startBut.setEnabled(false);
          }
          m_advancedConfigPanel.updatePanel();
        }
      });
    }
  }
  
  /**
   * Check to see if the data is sorted in order of the date time 
   * stamp (if present)
   */
  protected void sortCheck() {
    if (m_instances == null) {
      return;
    }
    
    if (m_simpleConfigPanel.isUsingANativeTimeStamp()) {
      String timeStampF = m_simpleConfigPanel.getSelectedTimeStampField();
      Attribute timeStampAtt = m_instances.attribute(timeStampF); 
      if (timeStampAtt != null) {
        
        double lastNonMissing = Utils.missingValue();
        boolean ok = true;
        boolean hasMissing = false;
        for (int i = 0; i < m_instances.numInstances(); i++) {
          Instance current = m_instances.instance(i);
          
          if (Utils.isMissingValue(lastNonMissing)) {
            if (!current.isMissing(timeStampAtt)) {
              lastNonMissing = current.value(timeStampAtt);
            } else {
              hasMissing = true;
            }
          } else {
            if (!current.isMissing(timeStampAtt)) {
              if (current.value(timeStampAtt) - lastNonMissing < 0) {
                ok = false;
                break;
              }
              
              lastNonMissing = current.value(timeStampAtt);
            } else {
              hasMissing = true;
            }
          }
        }
        
        if (!ok && !hasMissing) {
          // ask if we should sort
          int result = JOptionPane.showConfirmDialog(ForecastingPanel.this, 
              "The data does not appear to be in sorted order of \""
              + timeStampF + "\". Do you want to sort the data?", 
              "Forecasting", JOptionPane.YES_NO_OPTION);
          
          if (result == JOptionPane.YES_OPTION) {
            if (m_log != null) {
              m_log.statusMessage("Sorting data...");
            }
            m_instances.sort(timeStampAtt);
            m_sortedCheck = true;            
          }
        }
        
        if (!ok && hasMissing) {
          // we can't really proceed in this situation. We can't sort by the timestamp
          // because instances with missing values will be put at the end of the data.
          // The only thing we can do is to remove the instances with missing time
          // stamps but this is likely to screw up the periodicity and majorly impact
          // on results.
          
          int result = JOptionPane.showConfirmDialog(ForecastingPanel.this, 
              "The data does not appear to be in sorted order of \""
              + timeStampF + "\". \nFurthermore, there are rows with\n" +
              		"missing timestamp values. We can remove these\n" +
              		"rows and then sort the data but this is likely to\n" +
              		"result in degraded performance. It is strongly\n" +
              		"recommended that you fix these issues in the data\n" +
              		"before continuing. Do you want the system to proceed\n" +
              		"anyway by removing rows with missing timestamps and\n" +
              		"then sorting the data?", 
              "Forecasting", JOptionPane.YES_NO_OPTION);
          
          if (result == JOptionPane.YES_OPTION) {
            if (m_log != null) {
              m_log.statusMessage("Removing rows with missing time stamps and sorting data...");
            }
            m_instances.deleteWithMissing(timeStampAtt);
            m_instances.sort(timeStampAtt);
            m_sortedCheck = true;            
          }
        }
      }
    }
  }
  
  /**
   * Stop the currently running thread
   */
  protected void stopForecaster() {
    if (m_runThread != null) {
      m_runThread.interrupt();
      m_runThread.stop();
    }
  }
  
  
  /**
   * Inner class defining a log based on PrintStream. This enables command line,
   * gui and central Weka logging to be unified.
   * 
   * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
   */
  class LogPrintStream extends PrintStream {
    public LogPrintStream() {
      super(System.out);
    }
    
    /**
     * Log to the status area. Logs to the log area if the string
     * begins with "WARNING" or "ERROR"
     * 
     * @param string the string to display
     */
    private void logStatusMessage(String string) {
      if (m_log != null) {
        m_log.statusMessage(string);
        if (string.contains("WARNING") || string.contains("ERROR")) {
          m_log.logMessage(string);
        }
      }
    }
    
    /**
     * Log to the status area
     * @param string the string to log
     */
    public void println(String string) {
      // make sure that the global weka log picks it up
      System.out.println(string);    
      logStatusMessage(string);
    }
    
    /**
     * Log to the status area
     * @param obj the object to log
     */
    public void println(Object obj) {
      println(obj.toString());
    }
    
    /**
     * Log to the status area
     * @param string the string to log
     */
    public void print(String string){
      // make sure that the global weka log picks it up
      System.out.print(string);
      logStatusMessage(string);      
    }
    
    /**
     * Log to the status area
     * @param obj the object to log
     */
    public void print(Object obj) {
      print(obj.toString());
    }            
  }
  
  /**
   * Reevaluate the supplied forecaster on the current data
   * 
   * @param name the name of the result from the history list associated
   * with the forecaster to be reevaluated
   * 
   * @param forecaster the forecaster to reevaluate
   * @param trainHeader the header of the data used to train the forecaster
   */
  protected void reevaluateForecaster(final String name, 
      final WekaForecaster forecaster, final Instances trainHeader) {
    
    if (!trainHeader.equalHeaders(m_instances)) {
      JOptionPane.showMessageDialog(null, "Data used to train this forecaster " +
                "is not compatible with the currently loaded data:\n\n" 
          + trainHeader.equalHeadersMsg(m_instances), "Unable to reevaluate model",
          JOptionPane.ERROR_MESSAGE);
    } else {
      if (m_runThread == null) {
        synchronized (this) {
          m_startBut.setEnabled(false);
          m_stopBut.setEnabled(true);
        }
        
        m_runThread = new ForecastingThread(forecaster, name);
        ((ForecastingThread)m_runThread).setConfigureAndBuild(false);
        
        m_runThread.setPriority(Thread.MIN_PRIORITY);
        m_runThread.start();
      }
    }
  }
  
  /**
   * Start the forecasting process for the supplied configured forecaster
   * 
   * @param forecaster the forecaster to run
   */
  protected void startForecaster(final WekaForecaster forecaster) {
    if (m_runThread == null) {
      synchronized (this) {
        m_startBut.setEnabled(false);
        m_stopBut.setEnabled(true);
      }
      
      m_runThread = new ForecastingThread(forecaster, null);      
      
      m_runThread.setPriority(Thread.MIN_PRIORITY);
      m_runThread.start();
    }
  }
  
  private void dontShowMessageDialog(String key, String message, String dialogTitle) {
    if (!Utils.getDontShowDialog(key)) {
      JCheckBox dontShow = new JCheckBox("Do not show this message again");
      Object[] stuff = new Object[2];
      stuff[0] = message + "\n";
      stuff[1] = dontShow;

      JOptionPane.showMessageDialog(this, stuff, 
          dialogTitle, JOptionPane.OK_OPTION);

      if (dontShow.isSelected()) {
        try {
          Utils.setDontShowDialog(key);
        } catch (Exception ex) {
          // quietly ignore
        }
      }
    }
  }
      
  /**
   * Tests the Weka Forecasting panel from the command line.
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
      ForecastingPanel scp = 
        new ForecastingPanel(new LogPanel(new WekaTaskMonitor()), true, false, false);
      scp.setInstances(i);
      final javax.swing.JFrame jf =
        new javax.swing.JFrame("Weka Forecasting");
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(scp, BorderLayout.CENTER);
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
