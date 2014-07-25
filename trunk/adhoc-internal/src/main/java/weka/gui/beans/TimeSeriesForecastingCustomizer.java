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
 *    TimeSeriesForecastingCustomizer.java
 *    Copyright (C) 2011 Pentaho Corporation
 *
 */

package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import weka.classifiers.timeseries.WekaForecaster;
import weka.core.Environment;
import weka.core.Instances;
import weka.gui.PropertySheetPanel;

/**
 * Customizer for the TimeSeriesForecasting bean
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 *
 */
public class TimeSeriesForecastingCustomizer extends JPanel implements
    Customizer, CustomizerClosingListener, CustomizerCloseRequester {
  
  /**
   * For serialization
   */
  private static final long serialVersionUID = -8638861579301145591L;

  /** The forecaster to customize */
  protected TimeSeriesForecasting m_forecaster = null;
  
  /** The underlying WekaForecaster */
  protected WekaForecaster m_forecastingModel = null;
  
  /** The header of the data used to train the forecaster */
  protected Instances m_header;
  
  /** Handles the text field and file browser */
  protected FileEnvironmentField m_filenameField = 
    new FileEnvironmentField();

  /** Label for the num steps field */
  protected JLabel m_numStepsLab;
  
  /** Number of steps to forecast */
  protected EnvironmentField m_numStepsToForecast = 
    new EnvironmentField();
  
  /** Label for the artificial time stamp offset fields */
  protected JLabel m_artificialLab;
  
  /** 
   * Number of steps beyond the end of the training data that incoming
   * historical priming data is
   */
  protected EnvironmentField m_artificialOffset =
    new EnvironmentField();
  
  /** Rebuild the forecaster ? */
  protected JCheckBox m_rebuildForecasterCheck = new JCheckBox();
  
  /** Label for the save forecaster field */
  protected JLabel m_saveLab;
  
  /** Text field for the filename to save the forecaster to */
  protected FileEnvironmentField m_saveFilenameField =
    new FileEnvironmentField();
  
  /** The text area to display the model in */
  protected JTextArea m_modelDisplay = new JTextArea(20, 60);
  
  /** Property sheet panel used to get the "About" panel for global info */
  protected PropertySheetPanel m_sheetPanel = new PropertySheetPanel();

  /** Environment variables to use */
  protected transient Environment m_env = Environment.getSystemWide();
  
  /** Frame containing us */
  protected Window m_parentWindow;  
  
  /**
   * Constructor
   */
  public TimeSeriesForecastingCustomizer() {
    setLayout(new BorderLayout());
    
    m_filenameField.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        // first check if the field is blank (i.e. been cleared) and
        // we have a loaded model - if so then just return. We'll
        // encode the forecaster to base 64 and clear the filename
        // field when the customizer closes.
        if (TimeSeriesForecasting.isEmpty(m_filenameField.getText())) {
          return;
        }
        
        loadModel();
        if (m_forecastingModel != null) {
          m_modelDisplay.setText(m_forecastingModel.toString());
          checkIfModelIsUsingArtificialTimeStamp();
          checkIfModelIsUsingOverlayData();
        }
      }
    });
  }
  
  /**
   * Set the object to edit
   * 
   * @param object the object to edit
   */
  public void setObject(Object object) {
    setupLayout();
    
    m_forecaster = (TimeSeriesForecasting)object;
    m_sheetPanel.setTarget(m_forecaster);
    String loadFilename = m_forecaster.getFilename();
    if (!TimeSeriesForecasting.isEmpty(loadFilename) && 
        !loadFilename.equals("-NONE-")) {
      m_filenameField.setText(loadFilename);
      loadModel();
    } else {
      String encodedForecaster = m_forecaster.getEncodedForecaster();
      if (!TimeSeriesForecasting.isEmpty(encodedForecaster) &&
          !encodedForecaster.equals("-NONE-")) {
        try {
          List<Object> model = TimeSeriesForecasting.getForecaster(encodedForecaster);
          if (model != null) {
            m_forecastingModel = (WekaForecaster)model.get(0);
            m_header = (Instances)model.get(1);
            m_modelDisplay.setText(m_forecastingModel.toString());
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
    
    if (!TimeSeriesForecasting.isEmpty(m_forecaster.getSaveFilename())) {
      m_saveFilenameField.setText(m_forecaster.getSaveFilename());
    }
    m_numStepsToForecast.setText(m_forecaster.getNumStepsToForecast());
    m_artificialOffset.setText(m_forecaster.getArtificialTimeStartOffset());
    m_rebuildForecasterCheck.setSelected(m_forecaster.getRebuildForecaster());
    
    m_saveLab.setEnabled(m_rebuildForecasterCheck.isSelected());
    m_saveFilenameField.setEnabled(m_rebuildForecasterCheck.isSelected());
    

    checkIfModelIsUsingArtificialTimeStamp();
    checkIfModelIsUsingOverlayData();
  }
  
  private void checkIfModelIsUsingArtificialTimeStamp() {
    if (m_forecastingModel != null) {
      boolean usingA = m_forecastingModel.getTSLagMaker().isUsingAnArtificialTimeIndex(); 
      m_artificialLab.setEnabled(usingA);
      m_artificialOffset.setEnabled(usingA);
    }
  }
  
  private void checkIfModelIsUsingOverlayData() {
    if (m_forecastingModel != null) {
      if (m_forecastingModel.isUsingOverlayData()) {
        m_numStepsToForecast.setEnabled(false);
        m_numStepsLab.setEnabled(false);
        // remove any number set here since size of the overlay data (with missing
        // targets set) determines the number of steps that will be forecast
        m_numStepsToForecast.setText("");
      } else {
        m_numStepsToForecast.setEnabled(true);
        m_numStepsLab.setEnabled(true);
      }
    }
  }
  
  private void setupLayout() {
    removeAll();
    
    JTabbedPane tabHolder = new JTabbedPane();
    
    JPanel modelFilePanel = new JPanel();
    modelFilePanel.setLayout(new BorderLayout());
    JPanel tempP1 = new JPanel();
    
    tempP1.setLayout(new GridLayout(5, 2));
    JLabel fileLab = new JLabel("Load/import forecaster", SwingConstants.RIGHT);
    tempP1.add(fileLab); tempP1.add(m_filenameField);
    m_numStepsLab = new JLabel("Number of steps to forecast", SwingConstants.RIGHT);
    tempP1.add(m_numStepsLab); tempP1.add(m_numStepsToForecast);
    m_artificialLab = new JLabel("Number of historical instances " +
    		"beyond end of training data", SwingConstants.RIGHT);
    tempP1.add(m_artificialLab); tempP1.add(m_artificialOffset);
    JLabel rebuildLab = new JLabel("Rebuild/reestimate on incoming data", 
        SwingConstants.RIGHT);
    tempP1.add(rebuildLab); tempP1.add(m_rebuildForecasterCheck);
    m_saveLab = new JLabel("Save forecaster", SwingConstants.RIGHT);
    tempP1.add(m_saveLab); tempP1.add(m_saveFilenameField);    
    
    modelFilePanel.add(tempP1, BorderLayout.NORTH);
    
    tabHolder.addTab("Model file", modelFilePanel);
    
    add(tabHolder, BorderLayout.CENTER);
    
    JPanel modelPanel = new JPanel();    
    modelPanel.setLayout(new BorderLayout());
    m_modelDisplay.setEditable(false);
    m_modelDisplay.setFont(new Font("Monospaced", Font.PLAIN, 12));
    m_modelDisplay.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    JScrollPane scrollPane = new JScrollPane(m_modelDisplay);
    
    modelPanel.add(scrollPane, BorderLayout.CENTER);
    
    tabHolder.addTab("Model", modelPanel);
    
    JButton okBut = new JButton("OK");
    JButton cancelBut = new JButton("Cancel");
    JPanel butHolder1 = new JPanel();
    butHolder1.setLayout(new GridLayout(1, 2));
    butHolder1.add(okBut); butHolder1.add(cancelBut);
    JPanel butHolder2 = new JPanel();
    butHolder2.setLayout(new GridLayout(1, 3));
    butHolder2.add(new JPanel());
    butHolder2.add(butHolder1);
    butHolder2.add(new JPanel());
    
    add(butHolder2, BorderLayout.SOUTH);
    
    cancelBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // just close the dialog
        m_parentWindow.dispose();
      }
    });
    
    okBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // apply the changes
        customizerClosing();
        m_parentWindow.dispose();
      }
    });
    
    m_saveLab.setEnabled(false);
    m_saveFilenameField.setEnabled(false);
    m_rebuildForecasterCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_saveFilenameField.setEnabled(m_rebuildForecasterCheck.isSelected());
        m_saveLab.setEnabled(m_rebuildForecasterCheck.isSelected());
      }
    });
  }
  
  private void loadModel() {
    if (!TimeSeriesForecasting.isEmpty(m_filenameField.getText())) {
      try {
        String filename = m_filenameField.getText();
        try {
          if (m_env == null) {
            m_env = Environment.getSystemWide();
          }
          filename = m_env.substitute(filename);                    
        } catch (Exception ex) {
          // Quietly ignore any problems with environment variables.
          // A variable might not be set now, but could be when the
          // component is executed at a later time
        }
        File theFile = new File(filename);
        if (theFile.isFile()) {
          ObjectInputStream is = 
            new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)));
          m_forecastingModel = (WekaForecaster)is.readObject();
          m_header = (Instances)is.readObject();
//          Instances header = (Instances)is.readObject();
          is.close();          
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  
  /**
   * Set the environment variables to use.
   *
   * @param env the environment variables to use
   */
  public void setEnvironment(Environment env) {
    m_env = env;
    m_filenameField.setEnvironment(env);
    m_numStepsToForecast.setEnvironment(env);
    m_artificialOffset.setEnvironment(env);
    m_saveFilenameField.setEnvironment(env);
  }

  /**
   * Called when the window containing the customizer is closed 
   */
  public void customizerClosing() {
    if (m_forecaster != null) {
      if (!TimeSeriesForecasting.isEmpty(m_filenameField.getText())) {
        m_forecaster.setFilename(m_filenameField.getText());
        // clear base64 field with -NONE-
        m_forecaster.setEncodedForecaster("-NONE-");
      } else {
        if (m_forecastingModel != null) {
          // set base64 field and clear filename field with -NONE-
          try {
            String encodedModel = 
              TimeSeriesForecasting.encodeForecasterToBase64(m_forecastingModel, m_header);
            m_forecaster.setFilename("-NONE-");
            m_forecaster.setEncodedForecaster(encodedModel);
          } catch (Exception ex) {
            ex.printStackTrace();
          }          
        }        
      }
      m_forecaster.setRebuildForecaster(m_rebuildForecasterCheck.isSelected());
      m_forecaster.setNumStepsToForecast(m_numStepsToForecast.getText());
      m_forecaster.setArtificialTimeStartOffset(m_artificialOffset.getText());
      if (m_rebuildForecasterCheck.isSelected() && 
          !TimeSeriesForecasting.isEmpty(m_saveFilenameField.getText())) {
        m_forecaster.setSaveFilename(m_saveFilenameField.getText());
      } else {
        m_forecaster.setSaveFilename("");
      }
    }
  }

  /**
   * Set the Window that contains this customizer
   * 
   * @param parent the parent Window
   */
  public void setParentWindow(Window parent) {
    m_parentWindow = parent;
  }
}
