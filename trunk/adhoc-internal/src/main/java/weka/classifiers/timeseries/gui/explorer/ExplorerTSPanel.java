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
 *    ExplorerTSPanel.java
 *    Copyright (C) 2010 Pentaho Corporation
 */

package weka.classifiers.timeseries.gui.explorer;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.io.BufferedReader;
import java.io.FileReader;

import weka.classifiers.timeseries.gui.ForecastingPanel;
import weka.core.Instances;
import weka.gui.LogPanel;
import weka.gui.Logger;
import weka.gui.WekaTaskMonitor;
import weka.gui.explorer.Explorer;
import weka.gui.explorer.Explorer.ExplorerPanel;
import weka.gui.explorer.Explorer.LogHandler;

/**
 * GUI class that provides a time series forecasting plugin tab for the Weka 
 * Explorer. Wraps around the ForecastingPanel.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 */
public class ExplorerTSPanel extends JPanel implements ExplorerPanel, LogHandler {
  
  /** For serialization */
  private static final long serialVersionUID = -7313227058091543628L;

  /** Logging object */
  protected Logger m_log;
  
  /** The forecasting panel to wrap */
  protected ForecastingPanel m_forecastingPanel;
  
  /**
   * Constructor
   */
  public ExplorerTSPanel() {
    setLayout(new BorderLayout());
    
    m_forecastingPanel = new ForecastingPanel(null, false, false, false);
    add(m_forecastingPanel, BorderLayout.CENTER);
  }

  /**
   * Unused
   */
  public void setExplorer(Explorer parent) {
  }

  /**
   * Unused - just returns null
   * 
   * @return null
   */
  public Explorer getExplorer() {
    return null;
  }

  /**
   * Set the working instances for this panel. Passes the instances on to the
   * wrapped ForecastingPanel
   * 
   * @param inst the instances to use
   */
  public void setInstances(Instances inst) {
    if (m_forecastingPanel != null) {
      try {
        m_forecastingPanel.setInstances(inst);
      } catch (Exception ex) {
        if (m_log != null) {
          m_log.logMessage(ex.getMessage());
        }
        ex.printStackTrace();
      }
    }
  }

  /**
   * Get the title for this tab
   * 
   * @return the title for this tab
   */
  public String getTabTitle() {
    return "Forecast";
  }

  /**
   * Get the tool tip for this tab
   * 
   * @return the tool tip for this tab
   */
  public String getTabTitleToolTip() {
    return "Build and evaluate time series forecasting models";
  }

  /**
   * Set the logging object to use
   * 
   * @param newLog the log to use
   */
  public void setLog(Logger newLog) {
    if (newLog instanceof JComponent && m_forecastingPanel != null) {
      m_log = newLog;
      m_forecastingPanel.setLog(newLog);
    }
  }
  
  /**
   * Main method for testing this class. Expects the path to an ARFF file
   * as an argument
   * 
   * @param args an array of command line arguments
   */
  public static void main(String[] args) {
    try {
      Instances insts = new Instances(new BufferedReader(new FileReader(args[0])));

      final ExplorerTSPanel pan = new ExplorerTSPanel();
      pan.setInstances(insts);
      pan.setLog(new LogPanel(new WekaTaskMonitor()));

      final JFrame frame = new JFrame("Forecasting");
      frame.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
          frame.dispose();
          System.exit(1);
        }
      });
      frame.setSize(800, 600);
      frame.setContentPane(pan);
      frame.setVisible(true);

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
