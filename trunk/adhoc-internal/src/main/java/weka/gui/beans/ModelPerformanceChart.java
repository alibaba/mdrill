/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    ModelPerformanceChart.java
 *    Copyright (C) 2004 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.beans.EventSetDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.gui.Logger;
import weka.gui.visualize.Plot2D;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.VisualizePanel;

/**
 * Bean that can be used for displaying threshold curves (e.g. ROC
 * curves) and scheme error plots
 *
 * @author Mark Hall
 * @version $Revision: 7748 $
 */
public class ModelPerformanceChart
  extends JPanel
  implements ThresholdDataListener, VisualizableErrorListener, 
             Visible, UserRequestAcceptor, EventConstraints,
	     Serializable, BeanContextChild, HeadlessEventCollector,
	     BeanCommon, EnvironmentHandler {

  /** for serialization */
  private static final long serialVersionUID = -4602034200071195924L;
  
  protected BeanVisual m_visual = new BeanVisual("ModelPerformanceChart", 
      BeanVisual.ICON_PATH+"ModelPerformanceChart.gif",
      BeanVisual.ICON_PATH
      +"ModelPerformanceChart_animated.gif");

  protected transient PlotData2D m_masterPlot;
  
  /** For rendering plots to encapsulate in ImageEvents */
  protected transient List<Instances> m_offscreenPlotData;
  protected transient List<String> m_thresholdSeriesTitles;
  protected transient OffscreenChartRenderer m_offscreenRenderer;
  
  /** Name of the renderer to use for offscreen chart rendering */
  protected String m_offscreenRendererName = "Weka Chart Renderer";
  
  /** 
   * The name of the attribute to use for the x-axis of offscreen plots. 
   * If left empty, False Positive Rate is used for threshold curves 
   */
  protected String m_xAxis = "";
  
  /** 
   * The name of the attribute to use for the y-axis of offscreen plots. 
   * If left empty, True Positive Rate is used for threshold curves 
   */
  protected String m_yAxis = "";
  
  /**
   * Additional options for the offscreen renderer
   */
  protected String m_additionalOptions = "";
  
  /** Width of offscreen plots */
  protected String m_width = "500";
  
  /** Height of offscreen plots */
  protected String m_height = "400";
  
  protected transient JFrame m_popupFrame;

  protected boolean m_framePoppedUp = false;

  /** Events received and stored during headless execution */
  protected List<EventObject> m_headlessEvents;
  
  /** 
   * Set to true when processing events stored during headless
   * execution. Used to prevent sending ImageEvents to listeners
   * a second time (since these will have been passed on during
   * headless execution).
   */
  protected transient boolean m_processingHeadlessEvents = false;
  
  protected ArrayList<ImageListener> m_imageListeners = new ArrayList<ImageListener>();
  
  protected List<Object> m_listenees = new ArrayList<Object>();

  /**
   * True if this bean's appearance is the design mode appearance
   */
  protected boolean m_design;

  /**
   * BeanContex that this bean might be contained within
   */
  protected transient BeanContext m_beanContext = null;

  private transient VisualizePanel m_visPanel;
  
  /**
   * The environment variables.
   */
  protected transient Environment m_env;
  
  /**
   * BeanContextChild support
   */
  protected BeanContextChildSupport m_bcSupport = 
    new BeanContextChildSupport(this);

  public ModelPerformanceChart() {
    useDefaultVisual();
    
    java.awt.GraphicsEnvironment ge = 
      java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
    if (!ge.isHeadless()) {
      appearanceFinal();
    } else {
      m_headlessEvents = new ArrayList<EventObject>();
    }
  }

  /**
   * Global info for this bean
   *
   * @return a <code>String</code> value
   */
  public String globalInfo() {
    return "Visualize performance charts (such as ROC).";
  }

  protected void appearanceDesign() {
    removeAll();

    setLayout(new BorderLayout());
    add(m_visual, BorderLayout.CENTER);
  }

  protected void appearanceFinal() {
    removeAll();
    setLayout(new BorderLayout());
    setUpFinal();
  }

  protected void setUpFinal() {
    if (m_visPanel == null) {
      m_visPanel = new VisualizePanel();
    }
    add(m_visPanel, BorderLayout.CENTER);
  }
  
  protected void setupOffscreenRenderer() {
    if (m_offscreenRenderer == null) {
      if (m_offscreenRendererName == null || m_offscreenRendererName.length() == 0) {
        m_offscreenRenderer = new WekaOffscreenChartRenderer();
        return;
      }
      
      if (m_offscreenRendererName.equalsIgnoreCase("weka chart renderer")) {
        m_offscreenRenderer = new WekaOffscreenChartRenderer();
      } else {
        try {
          Object r = PluginManager.getPluginInstance("weka.gui.beans.OffscreenChartRenderer", 
              m_offscreenRendererName);
          if (r != null && r instanceof weka.gui.beans.OffscreenChartRenderer) {
            m_offscreenRenderer = (OffscreenChartRenderer)r;
          } else {
            // use built-in default
            m_offscreenRenderer = new WekaOffscreenChartRenderer();
          }
        } catch (Exception ex) {
          // use built-in default
          m_offscreenRenderer = new WekaOffscreenChartRenderer();
        }
      }
    }
  }

  /**
   * Display a threshold curve.
   *
   * @param e a ThresholdDataEvent
   */
  public synchronized void acceptDataSet(ThresholdDataEvent e) {
    if (m_env == null) {
      m_env = Environment.getSystemWide();
    }
    
    if (!GraphicsEnvironment.isHeadless()) {
      if (m_visPanel == null) {
        m_visPanel = new VisualizePanel();
      }
      if (m_masterPlot == null) {
        m_masterPlot = e.getDataSet();
      }
      try {
        // check for compatable data sets
        if (!m_masterPlot.getPlotInstances().relationName().
            equals(e.getDataSet().getPlotInstances().relationName())) {

          // if not equal then remove all plots and set as new master plot
          m_masterPlot = e.getDataSet();
          m_visPanel.setMasterPlot(m_masterPlot);
          m_visPanel.validate(); m_visPanel.repaint();          
        } else {
          // add as new plot
          m_visPanel.addPlot(e.getDataSet());
          m_visPanel.validate(); m_visPanel.repaint();
        }
        m_visPanel.setXIndex(4); m_visPanel.setYIndex(5);        
      } catch (Exception ex) {
        System.err.println("Problem setting up visualization (ModelPerformanceChart)");
        ex.printStackTrace();
      }      
    } else {
      m_headlessEvents.add(e);      
    }

    if (m_imageListeners.size() > 0 && !m_processingHeadlessEvents) {
      // configure the renderer (if necessary)
      setupOffscreenRenderer();

      if (m_offscreenPlotData == null || 
          !m_offscreenPlotData.get(0).relationName().
          equals(e.getDataSet().getPlotInstances().relationName())) {
        m_offscreenPlotData = new ArrayList<Instances>();
        m_thresholdSeriesTitles = new ArrayList<String>();
      }
      m_offscreenPlotData.add(e.getDataSet().getPlotInstances());
      m_thresholdSeriesTitles.add(e.getDataSet().getPlotName());
      List<String> options = new ArrayList<String>();
      
      String additional = "-color=/last";
      if (m_additionalOptions != null && m_additionalOptions.length() > 0) {
        additional = m_additionalOptions;
        try {
          additional = m_env.substitute(additional);
        } catch (Exception ex) { }
      }
      String[] optsParts = additional.split(",");
      for (String p : optsParts) {
        options.add(p.trim());
      }
      
      String xAxis = "False Positive Rate";
      if (m_xAxis != null && m_xAxis.length() > 0) {
        xAxis = m_xAxis;
        try {
          xAxis = m_env.substitute(xAxis);
        } catch (Exception ex) { }
      }
      String yAxis = "True Positive Rate";
      if (m_yAxis != null && m_yAxis.length() > 0) {
        yAxis = m_yAxis;
        try {
          yAxis = m_env.substitute(yAxis);
        } catch (Exception ex) { }
      }
      
      String width = m_width;
      String height = m_height;
      int defWidth = 500;
      int defHeight = 400;
      try {
        width = m_env.substitute(width);
        height = m_env.substitute(height);
        
        defWidth = Integer.parseInt(width);
        defHeight = Integer.parseInt(height);
      } catch (Exception ex) { }
     
      try {
        List<Instances> series = new ArrayList<Instances>();
        for (int i = 0; i < m_offscreenPlotData.size(); i++) {
          Instances temp = new Instances(m_offscreenPlotData.get(i));
          
          // set relation name to scheme name
          temp.setRelationName(m_thresholdSeriesTitles.get(i));
          series.add(temp);
        }
        BufferedImage osi = m_offscreenRenderer.renderXYLineChart(defWidth, defHeight, 
            series, xAxis, yAxis, options);

        ImageEvent ie = new ImageEvent(this, osi);
        notifyImageListeners(ie);
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    }    
  }

  /**
   * Display a scheme error plot.
   *
   * @param e a VisualizableErrorEvent
   */
  public synchronized void acceptDataSet(VisualizableErrorEvent e) {
    if (m_env == null) {
      m_env = Environment.getSystemWide();
    }

    if (!GraphicsEnvironment.isHeadless()) {
      if (m_visPanel == null) {
        m_visPanel = new VisualizePanel();
      }
      if (m_masterPlot == null) {
        m_masterPlot = e.getDataSet();
      }
      try {
        m_visPanel.setMasterPlot(m_masterPlot);
      } catch (Exception ex) {
        System.err.println("Problem setting up visualization (ModelPerformanceChart)");
        ex.printStackTrace();
      }
      m_visPanel.validate();
      m_visPanel.repaint();
    } else {
      m_headlessEvents = new ArrayList<EventObject>();
      m_headlessEvents.add(e);
    }
    
    if (m_imageListeners.size() > 0 && !m_processingHeadlessEvents) {
      // configure the renderer (if necessary)
      setupOffscreenRenderer();
     
      m_offscreenPlotData = new ArrayList<Instances>();      
      Instances predictedI = e.getDataSet().getPlotInstances();
      if (predictedI.classAttribute().isNominal()) {
        
        // split the classes out into individual series.
        // add a new attribute to hold point sizes - correctly
        // classified instances get default point size (2); 
        // misclassified instances get point size (5).
        // WekaOffscreenChartRenderer can take advantage of this
        // information - other plugin renderers may or may not
        // be able to use it
        FastVector atts = new FastVector();
        for (int i = 0; i < predictedI.numAttributes(); i++) {
          atts.add(predictedI.attribute(i).copy());
        }
        atts.add(new Attribute("@@size@@"));
        Instances newInsts = new Instances(predictedI.relationName(),
            atts, predictedI.numInstances());
        newInsts.setClassIndex(predictedI.classIndex());
        
        for (int i = 0; i < predictedI.numInstances(); i++) {
          double[] vals = new double[newInsts.numAttributes()];
          for (int j = 0; j < predictedI.numAttributes(); j++) {
            vals[j] = predictedI.instance(i).value(j);
          }
          vals[vals.length - 1] = 2; // default shape size
          Instance ni = new DenseInstance(1.0, vals);
          newInsts.add(ni);
        }
        
        // predicted class attribute is always actualClassIndex - 1
        Instances[] classes = new Instances[newInsts.numClasses()];
        for (int i = 0; i < newInsts.numClasses(); i++) {
          classes[i] = new Instances(newInsts, 0);
          classes[i].setRelationName(newInsts.classAttribute().value(i));
        }
        Instances errors = new Instances(newInsts, 0);
        int actualClass = newInsts.classIndex();
        for (int i = 0; i < newInsts.numInstances(); i++) {
          Instance current = newInsts.instance(i);
          classes[(int)current.classValue()].add((Instance)current.copy());
          
          if (current.value(actualClass) != current.value(actualClass - 1)) {
            Instance toAdd = (Instance)current.copy();
            
            // larger shape for an error
            toAdd.setValue(toAdd.numAttributes() - 1, 5);
            
            // swap predicted and actual class value so
            // that the color plotted for the error series
            // is that of the predicted class
            double actualClassV = toAdd.value(actualClass);
            double predictedClassV = toAdd.value(actualClass - 1);
            toAdd.setValue(actualClass, predictedClassV);
            toAdd.setValue(actualClass - 1, actualClassV);
              
            errors.add(toAdd);            
          }
        }
        
        errors.setRelationName("Errors");
        m_offscreenPlotData.add(errors);
        
        for (int i = 0; i < classes.length; i++) {
          m_offscreenPlotData.add(classes[i]);
        }
  
      } else {
        // numeric class - have to make a new set of instances
        // with the point sizes added as an additional attribute
        FastVector atts = new FastVector();
        for (int i = 0; i < predictedI.numAttributes(); i++) {
          atts.add(predictedI.attribute(i).copy());
        }
        atts.add(new Attribute("@@size@@"));
        Instances newInsts = new Instances(predictedI.relationName(),
            atts, predictedI.numInstances());

        int[] shapeSizes = e.getDataSet().getShapeSize();

        for (int i = 0; i < predictedI.numInstances(); i++) {
          double[] vals = new double[newInsts.numAttributes()];
          for (int j = 0; j < predictedI.numAttributes(); j++) {
            vals[j] = predictedI.instance(i).value(j);
          }
          vals[vals.length - 1] = shapeSizes[i];
          Instance ni = new DenseInstance(1.0, vals);
          newInsts.add(ni);
        }
        newInsts.setRelationName(predictedI.classAttribute().name());
        m_offscreenPlotData.add(newInsts);
      }
      
      List<String> options = new ArrayList<String>();
      
      String additional = "-color=" + predictedI.classAttribute().name()
        + ",-hasErrors";
      if (m_additionalOptions != null && m_additionalOptions.length() > 0) {
        additional += "," + m_additionalOptions;
        try {
          additional = m_env.substitute(additional);
        } catch (Exception ex) { }
      }            
      String[] optionsParts = additional.split(",");
      for (String p : optionsParts) {
        options.add(p.trim());
      }
      
//      if (predictedI.classAttribute().isNumeric()) {
      options.add("-shapeSize=@@size@@");
//      }
      
      String xAxis = m_xAxis;
      try {
        xAxis = m_env.substitute(xAxis);
      } catch (Exception ex) { }
      
      String yAxis = m_yAxis;
      try {
        yAxis = m_env.substitute(yAxis);
      } catch (Exception ex) { }
      
      String width = m_width;
      String height = m_height;
      int defWidth = 500;
      int defHeight = 400;
      try {
        width = m_env.substitute(width);
        height = m_env.substitute(height);
        
        defWidth = Integer.parseInt(width);
        defHeight = Integer.parseInt(height);
      } catch (Exception ex) { }
      
      try {
        BufferedImage osi = m_offscreenRenderer.renderXYScatterPlot(defWidth, defHeight, 
            m_offscreenPlotData, xAxis, yAxis, options);

        ImageEvent ie = new ImageEvent(this, osi);
        notifyImageListeners(ie);
      } catch (Exception e1) {
        e1.printStackTrace();
      }      
    }
  }

  /**
   * Notify all text listeners of a TextEvent
   *
   * @param te a <code>ImageEvent</code> value
   */
  @SuppressWarnings("unchecked")
  private void notifyImageListeners(ImageEvent te) {
    ArrayList<ImageListener> l;
    synchronized (this) {
      l = (ArrayList<ImageListener>)m_imageListeners.clone();
    }
    if (l.size() > 0) {
      for(int i = 0; i < l.size(); i++) {
        l.get(i).acceptImage(te);
      }
    }
  }
  
  /**
   * Get the list of events processed in headless mode. May return
   * null or an empty list if not running in headless mode or no
   * events were processed
   * 
   * @return a list of EventObjects or null.
   */
  public List<EventObject> retrieveHeadlessEvents() {
    return m_headlessEvents;
  }
  
  /**
   * Process a list of events that have been collected earlier. Has
   * no affect if the component is running in headless mode.
   * 
   * @param headless a list of EventObjects to process.
   */
  public void processHeadlessEvents(List<EventObject> headless) {
    
    // only process if we're not headless
    if (!GraphicsEnvironment.isHeadless()) {
      m_processingHeadlessEvents = true;
      for (EventObject e : headless) {
        if (e instanceof ThresholdDataEvent) {
          acceptDataSet((ThresholdDataEvent)e);
        } else if (e instanceof VisualizableErrorEvent) {
          acceptDataSet((VisualizableErrorEvent)e);
        }
      }
    }
    m_processingHeadlessEvents = false;
  }

  /**
   * Set the visual appearance of this bean
   *
   * @param newVisual a <code>BeanVisual</code> value
   */
  public void setVisual(BeanVisual newVisual) {
    m_visual = newVisual;
  }

  /**
   * Return the visual appearance of this bean
   */
  public BeanVisual getVisual() {
    return m_visual;
  }

  /**
   * Use the default appearance for this bean
   */
  public void useDefaultVisual() {
    m_visual.loadIcons(BeanVisual.ICON_PATH+"ModelPerformanceChart.gif",
        BeanVisual.ICON_PATH+"ModelPerformanceChart_animated.gif");
  }

  /**
   * Describe <code>enumerateRequests</code> method here.
   *
   * @return an <code>Enumeration</code> value
   */
  public Enumeration enumerateRequests() {
    Vector newVector = new Vector(0);
    if (m_masterPlot != null) {
      newVector.addElement("Show chart");
      newVector.addElement("?Clear all plots");
    }
    return newVector.elements();
  }

  /**
   * Add a property change listener to this bean
   *
   * @param name the name of the property of interest
   * @param pcl a <code>PropertyChangeListener</code> value
   */
  public void addPropertyChangeListener(String name,
					PropertyChangeListener pcl) {
    m_bcSupport.addPropertyChangeListener(name, pcl);
  }

  /**
   * Remove a property change listener from this bean
   *
   * @param name the name of the property of interest
   * @param pcl a <code>PropertyChangeListener</code> value
   */
  public void removePropertyChangeListener(String name,
					   PropertyChangeListener pcl) {
    m_bcSupport.removePropertyChangeListener(name, pcl);
  }

  /**
   * Add a vetoable change listener to this bean
   *
   * @param name the name of the property of interest
   * @param vcl a <code>VetoableChangeListener</code> value
   */
  public void addVetoableChangeListener(String name,
				       VetoableChangeListener vcl) {
    m_bcSupport.addVetoableChangeListener(name, vcl);
  }
  
  /**
   * Remove a vetoable change listener from this bean
   *
   * @param name the name of the property of interest
   * @param vcl a <code>VetoableChangeListener</code> value
   */
  public void removeVetoableChangeListener(String name,
					   VetoableChangeListener vcl) {
    m_bcSupport.removeVetoableChangeListener(name, vcl);
  }

  /**
   * Set a bean context for this bean
   *
   * @param bc a <code>BeanContext</code> value
   */
  public void setBeanContext(BeanContext bc) {
    m_beanContext = bc;
    m_design = m_beanContext.isDesignTime();
    if (m_design) {
      appearanceDesign();
    } else {
      java.awt.GraphicsEnvironment ge = 
        java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment(); 
      if (!ge.isHeadless()) {
        appearanceFinal();
      }
    }
  }

  /**
   * Return the bean context (if any) that this bean is embedded in
   *
   * @return a <code>BeanContext</code> value
   */
  public BeanContext getBeanContext() {
    return m_beanContext;
  }

  /**
   * Describe <code>performRequest</code> method here.
   *
   * @param request a <code>String</code> value
   * @exception IllegalArgumentException if an error occurs
   */
  public void performRequest(String request) {
    if (request.compareTo("Show chart") == 0) {
      try {
	// popup visualize panel
	if (!m_framePoppedUp) {
	  m_framePoppedUp = true;

	  final javax.swing.JFrame jf = 
	    new javax.swing.JFrame("Model Performance Chart");
	  jf.setSize(800,600);
	  jf.getContentPane().setLayout(new BorderLayout());
	  jf.getContentPane().add(m_visPanel, BorderLayout.CENTER);
	  jf.addWindowListener(new java.awt.event.WindowAdapter() {
	      public void windowClosing(java.awt.event.WindowEvent e) {
		jf.dispose();
		m_framePoppedUp = false;
	      }
	    });
	  jf.setVisible(true);
	  m_popupFrame = jf;
	} else {
	  m_popupFrame.toFront();
	}
      } catch (Exception ex) {
	ex.printStackTrace();
	m_framePoppedUp = false;
      }
    } else if (request.equals("Clear all plots")) {
        m_visPanel.removeAllPlots();
        m_visPanel.validate(); m_visPanel.repaint();
        m_visPanel = null;
        m_masterPlot = null;
        m_offscreenPlotData = null;
    } else {
      throw new IllegalArgumentException(request
					 + " not supported (Model Performance Chart)");
    }
  }
  
  public static void main(String [] args) {
    try {
      if (args.length != 1) {
	System.err.println("Usage: ModelPerformanceChart <dataset>");
	System.exit(1);
      }
      java.io.Reader r = new java.io.BufferedReader(
			 new java.io.FileReader(args[0]));
      Instances inst = new Instances(r);
      final javax.swing.JFrame jf = new javax.swing.JFrame();
      jf.getContentPane().setLayout(new java.awt.BorderLayout());
      final ModelPerformanceChart as = new ModelPerformanceChart();
      PlotData2D pd = new PlotData2D(inst);
      pd.setPlotName(inst.relationName());
      ThresholdDataEvent roc = new ThresholdDataEvent(as, pd);
      as.acceptDataSet(roc);      

      jf.getContentPane().add(as, java.awt.BorderLayout.CENTER);
      jf.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
          jf.dispose();
          System.exit(0);
        }
      });
      jf.setSize(800,600);
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }

  /**
   * Set a custom (descriptive) name for this bean
   * 
   * @param name the name to use
   */
  public void setCustomName(String name) {
    m_visual.setText(name);
  }

  /**
   * Get the custom (descriptive) name for this bean (if one has been set)
   * 
   * @return the custom name (or the default name)
   */
  public String getCustomName() {
    return m_visual.getText();
  }

  /**
   * Stop any processing that the bean might be doing.
   */
  public void stop() {
  }

  /**
   * Returns true if. at this time, the bean is busy with some
   * (i.e. perhaps a worker thread is performing some calculation).
   * 
   * @return true if the bean is busy.
   */
  public boolean isBusy() {
    return false;
  }
  
  /**
   * Add an image listener
   *
   * @param cl a <code>ImageListener</code> value
   */
  public synchronized void addImageListener(ImageListener cl) {
    m_imageListeners.add(cl);
  }

  /**
   * Remove an image listener
   *
   * @param cl a <code>ImageListener</code> value
   */
  public synchronized void removeImageListener(ImageListener cl) {
    m_imageListeners.remove(cl);
  }

  /**
   * Set a logger
   *
   * @param logger a <code>Logger</code> value
   */
  public void setLog(Logger logger) {
  }

  /**
   * Returns true if, at this time, 
   * the object will accept a connection via the supplied
   * EventSetDescriptor
   *
   * @param esd the EventSetDescriptor
   * @return true if the object will accept a connection
   */
  public boolean connectionAllowed(EventSetDescriptor esd) {
    return connectionAllowed(esd.getName());
  }

  /**
   * Returns true if, at this time, 
   * the object will accept a connection via the named event
   *
   * @param eventName the name of the event
   * @return true if the object will accept a connection
   */
  public boolean connectionAllowed(String eventName) {
    return eventName.equals("thresholdData") || eventName.equals("visualizableError");
  }

  /**
   * Notify this object that it has been registered as a listener with
   * a source for recieving events described by the named event
   * This object is responsible for recording this fact.
   *
   * @param eventName the event
   * @param source the source with which this object has been registered as
   * a listener
   */
  public void connectionNotification(String eventName, Object source) {
    if (connectionAllowed(eventName)) {
      m_listenees.add(source);
    }
  }

  /**
   * Notify this object that it has been deregistered as a listener with
   * a source for named event. This object is responsible
   * for recording this fact.
   *
   * @param eventName the event
   * @param source the source with which this object has been registered as
   * a listener
   */
  public void disconnectionNotification(String eventName, Object source) {
    m_listenees.remove(source);
  }

  /**
   * Returns true, if at the current time, the named event could
   * be generated. Assumes that supplied event names are names of
   * events that could be generated by this bean.
   *
   * @param eventName the name of the event in question
   * @return true if the named event could be generated at this point in
   * time
   */
  public boolean eventGeneratable(String eventName) {
    if (m_listenees.size() == 0) {
      return false;
    }
    
    boolean ok = false;
    for (Object o : m_listenees) {
      if (o instanceof EventConstraints) {
        if (((EventConstraints)o).eventGeneratable("thresholdData") ||
            ((EventConstraints)o).eventGeneratable("visualizableError")) {
          ok = true;
          break;
        }
      }
    }
    
    return ok;
  }

  @Override
  public void setEnvironment(Environment env) {
    m_env = env;
  }
  
  /**
   * Set the name of the attribute for the x-axis in offscreen plots. This defaults
   * to "False Positive Rate" for threshold curves if not specified.
   * 
   * @param xAxis the name of the xAxis
   */
  public void setOffscreenXAxis(String xAxis) {
    m_xAxis = xAxis;
  }
  
  /**
   * Get the name of the attribute for the x-axis in offscreen plots
   * 
   * @return the name of the xAxis
   */
  public String getOffscreenXAxis() {
    return m_xAxis;
  }
  
  /**
   * Set the name of the attribute for the y-axis in offscreen plots. This defaults
   * to "True Positive Rate" for threshold curves if not specified.
   * 
   * @param yAxis the name of the xAxis
   */
  public void setOffscreenYAxis(String yAxis) {
    m_yAxis = yAxis;
  }
  
  /**
   * Get the name of the attribute for the y-axix of offscreen plots.
   * 
   * @return the name of the yAxis.
   */
  public String getOffscreenYAxis() {
    return m_yAxis;
  }
  
  /**
   * Set the width (in pixels) of the offscreen image to generate. 
   * 
   * @param width the width in pixels.
   */
  public void setOffscreenWidth(String width) {
    m_width = width;
  }
  
  /**
   * Get the width (in pixels) of the offscreen image to generate.
   * 
   * @return the width in pixels.
   */
  public String getOffscreenWidth() {
    return m_width;
  }
  
  /**
   * Set the height (in pixels) of the offscreen image to generate
   * 
   * @param height the height in pixels
   */
  public void setOffscreenHeight(String height) {
    m_height = height;
  }
  
  /**
   * Get the height (in pixels) of the offscreen image to generate
   * @return the height in pixels
   */
  public String getOffscreenHeight() {
    return m_height;
  }
  
  /**
   * Set the name of the renderer to use for offscreen chart
   * rendering operations
   * 
   * @param rendererName the name of the renderer to use
   */
  public void setOffscreenRendererName(String rendererName) {
    m_offscreenRendererName = rendererName;
    m_offscreenRenderer = null;
  }
  
  /**
   * Get the name of the renderer to use for offscreen chart
   * rendering operations
   * 
   * @return the name of the renderer to use
   */
  public String getOffscreenRendererName() {
    return m_offscreenRendererName;
  }
  
  /**
   * Set the additional options for the offscreen renderer
   * 
   * @param additional additional options
   */
  public void setOffscreenAdditionalOpts(String additional) {
    m_additionalOptions = additional;
  }
  
  /**
   * Get the additional options for the offscreen renderer
   * 
   * @return the additional options
   */
  public String getOffscreenAdditionalOpts() {
    return m_additionalOptions;
  }
}
