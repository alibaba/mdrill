package weka.gui.beans;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Icon;

import weka.core.Instances;
import weka.classifiers.timeseries.gui.ForecastingPanel;

/**
 * KnowledgeFlow perspective provides the time series environment. 
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 */
public class TimeSeriesForecastingKFPerspective extends JPanel implements KnowledgeFlowApp.KFPerspective {

  /**
   * For serialization
   */
  private static final long serialVersionUID = -2653395123580895862L;
  
  /** The forecasting panel to wrap */
  protected ForecastingPanel m_forecastingPanel;

  /**
   * Constructor
   */
  public TimeSeriesForecastingKFPerspective() {
    setLayout(new BorderLayout());

    m_forecastingPanel = new ForecastingPanel(null, false, false, true);
    add(m_forecastingPanel, BorderLayout.CENTER);
  }
  
  /**
   * Sets the knowledge flow's paste buffer equal to the 
   * supplied configured TimeSeriesForecasting component. 
   * 
   * @param component the TimeSerieseForecasting component to make pasteable
   * @throws Exception if a problem occurs
   */
  public static void setClipboard(TimeSeriesForecasting component) throws Exception {
    // adds us to the first tab
    BeanInstance beanI = 
      new BeanInstance(null, component, 50, 50);
    
    // remove us. TODO add a BeanInstance constructor that doesn't
    // add the newly constructed bean to any list!!
    beanI.removeBean(null);
    Vector beans = new Vector();
    beans.add(beanI);
    
    KnowledgeFlowApp singleton = KnowledgeFlowApp.getSingleton();
    StringBuffer serialized = singleton.copyToBuffer(beans);
    singleton.m_pasteBuffer = serialized;
    
    singleton.setActivePerspective(0);
    
    // Another hack to make the paste button enabled until we make
    // all toolbar buttons protected...
    singleton.m_mainKFPerspective.
      setActiveTab(singleton.m_mainKFPerspective.getCurrentTabIndex());
  }  

  /**
   * Get the title of this perspective
   * 
   * @return the title of this perspective
   */
  public String getPerspectiveTitle() {
    return "Time series forecasting";
  }

  /**
   * Get the tool tip text for this perspective.
   * 
   * @return the tool tip text for this perspective
   */
  public String getPerspectiveTipText() {
    return "Time series forecasting environment";
  }

  /**
   * Get the icon for this perspective.
   * 
   * @return the Icon for this perspective (or null if the
   * perspective does not have an icon)
   */
  public Icon getPerspectiveIcon() {
    java.awt.Image pic = null;
    java.net.URL imageURL = this.getClass().getClassLoader().
    getResource("weka/gui/beans/icons/chart_line.png");

    if (imageURL == null) {
    } else {
      pic = java.awt.Toolkit.getDefaultToolkit().
      getImage(imageURL);
    }
    return new javax.swing.ImageIcon(pic);
  }

  /**
   * Make this perspective the active (visible) one in the KF
   *
   * @param active true if this perspective is the currently active
   * one
   */
  public void setActive(boolean active) {
  }

  /**
   * Tell this perspective whether or not it is part of the users
   * perspectives toolbar in the KnowledgeFlow. If not part of
   * the current set of perspectives, then we can free some resources.
   *
   * @param loaded true if this perspective is part of the user-selected
   * perspectives in the KnowledgeFlow
   */
  public void setLoaded(boolean loaded) {
  }

  /**
   * Set a reference to the main KnowledgeFlow perspective - i.e.
   * the perspective that manages flow layouts.
   * 
   * @param main the main KnowledgeFlow perspective.
   */
  public void setMainKFPerspective(KnowledgeFlowApp.MainKFPerspective main) {
    // don't need this
  }

  /**
   * Set instances (if the perspective accepts them)
   * 
   * @param insts the instances
   */
  public void setInstances(Instances inst) {
    if (m_forecastingPanel != null) {
      try {
        m_forecastingPanel.setInstances(inst);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Returns true if this perspective accepts instances
   * 
   * @return true if this perspective can accept instances
   */
  public boolean acceptsInstances() {
    return true;
  }

  /**
   * Main method for testing this class
   * 
   * @param args
   */
  public static void main(String[] args) {
    try {
      Instances insts = new Instances(new BufferedReader(new FileReader(args[0])));
      
      final TimeSeriesForecastingKFPerspective p = new TimeSeriesForecastingKFPerspective();
      p.setInstances(insts);
      
      final JFrame frame = new JFrame("Time series forecasting");

      frame.setSize(800, 600);
      frame.setContentPane(p);
      frame.setVisible(true);      
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
