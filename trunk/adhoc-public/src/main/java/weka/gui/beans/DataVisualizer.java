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
 *    DataVisualizer.java
 *    Copyright (C) 2002 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.gui.Logger;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.VisualizePanel;

import java.awt.BorderLayout;
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

/**
 * Bean that encapsulates weka.gui.visualize.VisualizePanel
 *
 * @author <a href="mailto:mhall@cs.waikato.ac.nz">Mark Hall</a>
 * @version $Revision: 7680 $
 */
public class DataVisualizer extends JPanel
  implements DataSourceListener, TrainingSetListener,
	     TestSetListener, Visible, UserRequestAcceptor, Serializable,
	     BeanContextChild, HeadlessEventCollector, EnvironmentHandler,
	     BeanCommon, EventConstraints {

  /** for serialization */
  private static final long serialVersionUID = 1949062132560159028L;

  protected BeanVisual m_visual = new BeanVisual("DataVisualizer", 
      BeanVisual.ICON_PATH+"DefaultDataVisualizer.gif",
      BeanVisual.ICON_PATH
      +"DefaultDataVisualizer_animated.gif");

  protected transient Instances m_visualizeDataSet;

  protected transient JFrame m_popupFrame;

  protected boolean m_framePoppedUp = false;

  /**
   * True if this bean's appearance is the design mode appearance
   */
  protected boolean m_design;

  /**
   * BeanContex that this bean might be contained within
   */
  protected transient BeanContext m_beanContext = null;

  private VisualizePanel m_visPanel;
  
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
   * Objects listening for data set events
   */
  private Vector m_dataSetListeners = new Vector();
  
  /** For rendering plots to encapsulate in ImageEvents */
  protected transient List<Instances> m_offscreenPlotData;
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
  
  /**
   * The environment variables.
   */
  protected transient Environment m_env;
  
  /**
   * BeanContextChild support
   */
  protected BeanContextChildSupport m_bcSupport = 
    new BeanContextChildSupport(this);

  public DataVisualizer() {
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
    return "Visualize incoming data/training/test sets in a 2D scatter plot.";
  }

  protected void appearanceDesign() {
    m_visPanel = null;
    removeAll();
    useDefaultVisual();
    setLayout(new BorderLayout());
    add(m_visual, BorderLayout.CENTER);
  }

  protected void appearanceFinal() {
    java.awt.GraphicsEnvironment ge = 
      java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment(); 
    
    removeAll();
    if (!ge.isHeadless()) {
      setLayout(new BorderLayout());
      setUpFinal();
    }
  }

  protected void setUpFinal() {
    if (m_visPanel == null) {
      m_visPanel = new VisualizePanel();
    }
    add(m_visPanel, BorderLayout.CENTER);
  }

  /**
   * Accept a training set
   *
   * @param e a <code>TrainingSetEvent</code> value
   */
  public void acceptTrainingSet(TrainingSetEvent e) {
    Instances trainingSet = e.getTrainingSet();
    DataSetEvent dse = new DataSetEvent(this, trainingSet);
    acceptDataSet(dse);
  }

  /**
   * Accept a test set
   *
   * @param e a <code>TestSetEvent</code> value
   */
  public void acceptTestSet(TestSetEvent e) {
    Instances testSet = e.getTestSet();
    DataSetEvent dse = new DataSetEvent(this, testSet);
    acceptDataSet(dse);
  }

  /**
   * Accept a data set
   *
   * @param e a <code>DataSetEvent</code> value
   */
  public synchronized void acceptDataSet(DataSetEvent e) {
    // ignore structure only events
    if (e.isStructureOnly()) {
      return;
    }
    m_visualizeDataSet = new Instances(e.getDataSet());
    if (m_visualizeDataSet.classIndex() < 0) {
      m_visualizeDataSet.setClassIndex(m_visualizeDataSet.numAttributes()-1);
    }
    if (!m_design) {
      try {
	setInstances(m_visualizeDataSet);
      } catch (Exception ex) {
	ex.printStackTrace();
      }
    } else {
      if (m_headlessEvents != null) {
        m_headlessEvents = new ArrayList<EventObject>();
        m_headlessEvents.add(e);
      }
    }

    // pass on the event to any listeners
    notifyDataSetListeners(e);
    
    renderOffscreenImage(e);
  }
  
  protected void renderOffscreenImage(DataSetEvent e) {
    if (m_env == null) {
      m_env = Environment.getSystemWide();
    }
    
    if (m_imageListeners.size() > 0 && !m_processingHeadlessEvents) {
      // configure the renderer (if necessary)
      setupOffscreenRenderer();
     
      m_offscreenPlotData = new ArrayList<Instances>();      
      Instances predictedI = e.getDataSet();
      if (predictedI.classIndex() >= 0 && predictedI.classAttribute().isNominal()) {
        // set up multiple series - one for each class
        Instances[] classes = new Instances[predictedI.numClasses()];
        for (int i = 0; i < predictedI.numClasses(); i++) {
          classes[i] = new Instances(predictedI, 0);
          classes[i].setRelationName(predictedI.classAttribute().value(i));
        }
        for (int i = 0; i < predictedI.numInstances(); i++) {
          Instance current = predictedI.instance(i);
          classes[(int)current.classValue()].add((Instance)current.copy());
        }
        for (int i = 0; i < classes.length; i++) {
          m_offscreenPlotData.add(classes[i]);
        }
      } else {
        m_offscreenPlotData.add(new Instances(predictedI));
      }
      
      List<String> options = new ArrayList<String>();
      String additional = m_additionalOptions;
      if (m_additionalOptions != null && m_additionalOptions.length() > 0) {
        try {
          additional = m_env.substitute(additional);
        } catch (Exception ex) { }
      }          
      if (additional != null && additional.indexOf("-color") < 0) {
        // for WekaOffscreenChartRenderer only
        if (additional.length() > 0) {
          additional += ",";
        }
        if (predictedI.classIndex() >= 0) {
          additional += "-color=" + predictedI.classAttribute().name();
        } else {
          additional += "-color=/last";
        }
      }
      String[] optionsParts = additional.split(",");
      for (String p : optionsParts) {
        options.add(p.trim());
      }
      
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
   * Notify all image listeners of a ImageEvent
   *
   * @param te a <code>ImageEvent</code> value
   */
  @SuppressWarnings("unchecked")
  protected void notifyImageListeners(ImageEvent te) {
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
    if (!java.awt.GraphicsEnvironment.isHeadless()) {
      m_processingHeadlessEvents = true;
      for (EventObject e : headless) {
        if (e instanceof DataSetEvent) {
          acceptDataSet((DataSetEvent)e);
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
    m_visual.loadIcons(BeanVisual.ICON_PATH+"DefaultDataVisualizer.gif",
		       BeanVisual.ICON_PATH+"DefaultDataVisualizer_animated.gif");
  }

  /**
   * Describe <code>enumerateRequests</code> method here.
   *
   * @return an <code>Enumeration</code> value
   */
  public Enumeration enumerateRequests() {
    Vector newVector = new Vector(0);
    if (m_visualizeDataSet != null) {
      newVector.addElement("Show plot");
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
   * Set instances for this bean. This method is a convenience method
   * for clients who use this component programatically
   *
   * @param inst an <code>Instances</code> value
   * @exception Exception if an error occurs
   */
  public void setInstances(Instances inst) throws Exception {
    if (m_design) {
      throw new Exception("This method is not to be used during design "
			  +"time. It is meant to be used if this "
			  +"bean is being used programatically as as "
			  +"stand alone component.");
    }
    m_visualizeDataSet = inst;
    PlotData2D pd1 = new PlotData2D(m_visualizeDataSet);
    String relationName = m_visualizeDataSet.relationName();
    pd1.setPlotName(relationName);
    try {
      m_visPanel.setMasterPlot(pd1);
    } catch (Exception ex) {
      System.err.println("Problem setting up "
			 +"visualization (DataVisualizer)");
      ex.printStackTrace();
    }
  }

  /**
   * Notify all data set listeners of a data set event
   *
   * @param ge a <code>DataSetEvent</code> value
   */
  private void notifyDataSetListeners(DataSetEvent ge) {
    Vector l;
    synchronized (this) {
      l = (Vector)m_dataSetListeners.clone();
    }
    if (l.size() > 0) {
      for(int i = 0; i < l.size(); i++) {
	((DataSourceListener)l.elementAt(i)).acceptDataSet(ge);
      }
    }
  }
  
  /**
   * Describe <code>performRequest</code> method here.
   *
   * @param request a <code>String</code> value
   * @exception IllegalArgumentException if an error occurs
   */
  public void performRequest(String request) {
    if (request.compareTo("Show plot") == 0) {
      try {
	// popup visualize panel
	if (!m_framePoppedUp) {
	  m_framePoppedUp = true;
	  final VisualizePanel vis = new VisualizePanel();
	  PlotData2D pd1 = new PlotData2D(m_visualizeDataSet);
	  
	  String relationName = m_visualizeDataSet.relationName();
	  
	  // A bit of a nasty hack. Allows producers of instances-based
	  // events to specify that the points should be connected
	  if (relationName.startsWith("__")) {
	    boolean[] connect = new boolean[m_visualizeDataSet.numInstances()];
	    for (int i = 1; i < connect.length; i++) { connect[i] = true; }
	    pd1.setConnectPoints(connect);
	    relationName = relationName.substring(2);
	  }
	  pd1.setPlotName(relationName);
	  try {
	    vis.setMasterPlot(pd1);
	  } catch (Exception ex) {
	    System.err.println("Problem setting up "
			       +"visualization (DataVisualizer)");
	    ex.printStackTrace();
	  }
	  final JFrame jf = new JFrame("Visualize");
	  jf.setSize(800,600);
	  jf.getContentPane().setLayout(new BorderLayout());
	  jf.getContentPane().add(vis, BorderLayout.CENTER);
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
    } else {
      throw new IllegalArgumentException(request
					 + " not supported (DataVisualizer)");
    }
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
   * Add a listener
   *
   * @param dsl a <code>DataSourceListener</code> value
   */
  public synchronized void addDataSourceListener(DataSourceListener dsl) {
    m_dataSetListeners.addElement(dsl);
  }

  /**
   * Remove a listener
   *
   * @param dsl a <code>DataSourceListener</code> value
   */
  public synchronized void removeDataSourceListener(DataSourceListener dsl) {
    m_dataSetListeners.remove(dsl);
  }

  public static void main(String [] args) {
    try {
      if (args.length != 1) {
	System.err.println("Usage: DataVisualizer <dataset>");
	System.exit(1);
      }
      java.io.Reader r = new java.io.BufferedReader(
			 new java.io.FileReader(args[0]));
      Instances inst = new Instances(r);
      final javax.swing.JFrame jf = new javax.swing.JFrame();
      jf.getContentPane().setLayout(new java.awt.BorderLayout());
      final DataVisualizer as = new DataVisualizer();
      as.setInstances(inst);
      
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
    // TODO Auto-generated method stub
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
    return eventName.equals("dataSet") || eventName.equals("trainingSet") || 
      eventName.equals("testSet");
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
        if (((EventConstraints)o).eventGeneratable("dataSet") ||
            ((EventConstraints)o).eventGeneratable("trainingSet") ||
            ((EventConstraints)o).eventGeneratable("testSet")) {
          ok = true;
          break;
        }
      }
    }
    return ok;
  }
}
