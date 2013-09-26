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
 *    AttributeSummarizer.java
 *    Copyright (C) 2003 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import weka.core.Attribute;
import weka.core.Environment;
import weka.core.Instance;
import weka.core.Instances;
import weka.gui.AttributeVisualizationPanel;

/**
 * Bean that encapsulates displays bar graph summaries for attributes in
 * a data set.
 *
 * @author <a href="mailto:mhall@cs.waikato.ac.nz">Mark Hall</a>
 * @version $Revision: 7681 $
 */
public class AttributeSummarizer
  extends DataVisualizer implements KnowledgeFlowApp.KFPerspective {

  /** for serialization */
  private static final long serialVersionUID = -294354961169372758L;

  /**
   * The number of plots horizontally in the display
   */
  protected int m_gridWidth = 4;
  
  /**
   * The maximum number of plots to show
   */
  protected int m_maxPlots = 100;

  /**
   * Index on which to color the plots.
   */
  protected int m_coloringIndex = -1;
  
  protected boolean m_showClassCombo = false;
  protected boolean m_runningAsPerspective = false;
  protected boolean m_activePerspective = false;
  
  protected transient List<AttributeVisualizationPanel> m_plots;

  /**
   * Creates a new <code>AttributeSummarizer</code> instance.
   */
  public AttributeSummarizer() {
    useDefaultVisual();
    m_visual.setText("AttributeSummarizer");
    
    java.awt.GraphicsEnvironment ge = 
      java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment(); 
    if (!ge.isHeadless()) {
      appearanceFinal();
    }
  }

  /**
   * Global info for this bean
   *
   * @return a <code>String</code> value
   */
  public String globalInfo() {
    return "Plot summary bar charts for incoming data/training/test sets.";
  }

  /**
   * Set the coloring index for the attribute summary plots
   *
   * @param ci an <code>int</code> value
   */
  public void setColoringIndex(int ci) {
    m_coloringIndex = ci;
  }

  /**
   * Return the coloring index for the attribute summary plots
   *
   * @return an <code>int</code> value
   */
  public int getColoringIndex() {
    return m_coloringIndex;
  }

  /**
   * Set the width of the grid of plots
   *
   * @param gw the width of the grid
   */
  public void setGridWidth(int gw) {
    if (gw > 0) {
      m_bcSupport.firePropertyChange("gridWidth", new Integer(m_gridWidth),
				     new Integer(gw));
      m_gridWidth = gw;
    }
  }

  /**
   * Get the width of the grid of plots
   *
   * @return the grid width
   */
  public int getGridWidth() {
    return m_gridWidth;
  }
  
  /**
   * Set the maximum number of plots to display
   *
   * @param mp the number of plots to display
   */
  public void setMaxPlots(int mp) {
    if (mp > 0) {
      m_bcSupport.firePropertyChange("maxPlots", new Integer(m_maxPlots),
				     new Integer(mp));
      m_maxPlots = mp;
    }
  }

  /**
   * Get the number of plots to display
   *
   * @return the number of plots to display
   */
  public int getMaxPlots() {
    return m_maxPlots;
  }

  /**
   * Set whether the appearance of this bean should be design or
   * application
   *
   * @param design true if bean should appear in design mode
   */
  public void setDesign(boolean design) {
    m_design = true;
    appearanceDesign();
  }

  protected void appearanceDesign() {
    removeAll();
    setLayout(new BorderLayout());
    add(m_visual, BorderLayout.CENTER);
  }

  protected void appearanceFinal() {
    removeAll();
    setLayout(new BorderLayout());
  }

  protected void setUpFinal() {
    removeAll();
    
    if (m_visualizeDataSet == null) {
      return;
    }

    if (!m_runningAsPerspective || m_activePerspective) {
      final JScrollPane hp = makePanel();
      add(hp, BorderLayout.CENTER);

      if (m_showClassCombo) {
        Vector<String> atts = new Vector<String>();
        for (int i = 0; i < m_visualizeDataSet.numAttributes(); i++) {
          atts.add("(" + Attribute.typeToStringShort(m_visualizeDataSet.attribute(i)) + ") "
              + m_visualizeDataSet.attribute(i).name());
        }

        final JComboBox classCombo = new JComboBox();
        classCombo.setModel(new DefaultComboBoxModel(atts));      

        if (atts.size() > 0) {
          if (m_visualizeDataSet.classIndex() < 0) {
            classCombo.setSelectedIndex(atts.size() - 1);
          } else {
            classCombo.setSelectedIndex(m_visualizeDataSet.classIndex());
          }
          classCombo.setEnabled(true);
          for (int i = 0; i < m_plots.size(); i++) {
            m_plots.get(i).setColoringIndex(classCombo.getSelectedIndex());
          }
        }

        JPanel comboHolder = new JPanel();
        comboHolder.setLayout(new BorderLayout());
        JPanel tempHolder = new JPanel();
        tempHolder.setLayout(new BorderLayout());
        tempHolder.add(new JLabel("Class: "), BorderLayout.WEST);
        tempHolder.add(classCombo, BorderLayout.EAST);
        comboHolder.add(tempHolder, BorderLayout.WEST);
        add(comboHolder, BorderLayout.NORTH);

        classCombo.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            int selected = classCombo.getSelectedIndex();
            if (selected >= 0) {
              for (int i = 0; i < m_plots.size(); i++) {
                m_plots.get(i).setColoringIndex(selected);
              }
            }
          }
        });
      }
    }
  }

  /**
   * Use the default appearance for this bean
   */
  public void useDefaultVisual() {
    
    m_visual.loadIcons(BeanVisual.ICON_PATH+"AttributeSummarizer.gif",
		       BeanVisual.ICON_PATH+"AttributeSummarizer_animated.gif");
  }

  /**
   * Return an enumeration of actions that the user can ask this bean to
   * perform
   *
   * @return an <code>Enumeration</code> value
   */
  public Enumeration enumerateRequests() {
    Vector newVector = new Vector(0);
    if (m_visualizeDataSet != null) {
      newVector.addElement("Show summaries");
    }
    return newVector.elements();
  }

  private JScrollPane makePanel() {
    String fontFamily = this.getFont().getFamily();
    Font newFont = new Font(fontFamily, Font.PLAIN, 10);
    JPanel hp = new JPanel();
    hp.setFont(newFont);
    int numPlots = Math.min(m_visualizeDataSet.numAttributes(), m_maxPlots);
    int gridHeight = numPlots / m_gridWidth;
    
    if (numPlots % m_gridWidth != 0) {
      gridHeight++;
    }
    hp.setLayout(new GridLayout(gridHeight, 4));
    
    m_plots = new ArrayList<AttributeVisualizationPanel>();      
    
    for (int i = 0; i < numPlots; i++) {
      JPanel temp = new JPanel();
      temp.setLayout(new BorderLayout());
      temp.setBorder(BorderFactory.createTitledBorder(m_visualizeDataSet.
						      attribute(i).name()));

      AttributeVisualizationPanel ap = new AttributeVisualizationPanel();
      m_plots.add(ap);
      ap.setInstances(m_visualizeDataSet);
      if (m_coloringIndex < 0 && m_visualizeDataSet.classIndex() >= 0) {
        ap.setColoringIndex(m_visualizeDataSet.classIndex());
      } else {
        ap.setColoringIndex(m_coloringIndex);
      }
      temp.add(ap, BorderLayout.CENTER);
      ap.setAttribute(i);
      hp.add(temp);
    }
    
    Dimension d = new Dimension(830, gridHeight * 100);
    hp.setMinimumSize(d);
    hp.setMaximumSize(d);
    hp.setPreferredSize(d);
    
    JScrollPane scroller = new JScrollPane(hp);

    return scroller;
  }

  /**
   * Set a bean context for this bean
   *
   * @param bc a <code>BeanContext</code> value
   */
  /*  public void setBeanContext(BeanContext bc) {
    m_beanContext = bc;
    m_design = m_beanContext.isDesignTime();
    if (m_design) {
      appearanceDesign();
    } 
    } */

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
    setUpFinal();
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
   * Get the title of this perspective
   * 
   * @return the title of this perspective
   */
  public String getPerspectiveTitle() {
    return "Attribute summary";
  }
  
  /**
   * Get the tool tip text for this perspective.
   * 
   * @return the tool tip text for this perspective
   */
  public String getPerspectiveTipText() {
    return "Matrix of attribute summary histograms";
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
      getResource("weka/gui/beans/icons/chart_bar.png");

    if (imageURL == null) {
    } else {
      pic = java.awt.Toolkit.getDefaultToolkit().
        getImage(imageURL);
    }
    return new javax.swing.ImageIcon(pic);
  }
  
  /**
   * Set active status of this perspective. True indicates
   * that this perspective is the visible active perspective
   * in the KnowledgeFlow
   * 
   * @param active true if this perspective is the active one
   */
  public void setActive(boolean active) {
    m_activePerspective = active;
    m_plots = null;
    setUpFinal();
  }
  
  /**
   * Set whether this perspective is "loaded" - i.e. whether
   * or not the user has opted to have it available in the
   * perspective toolbar. The perspective can make the decision
   * as to allocating or freeing resources on the basis of this.
   * 
   * @param loaded true if the perspective is available in
   * the perspective toolbar of the KnowledgeFlow
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
    m_showClassCombo = true;
    m_runningAsPerspective = true;
  }

  /**
   * Perform a named user request
   *
   * @param request a string containing the name of the request to perform
   * @exception IllegalArgumentException if request is not supported
   */
  public void performRequest(String request) {
    if (m_design == false) {
      setUpFinal();
      return;
    }
    if (request.compareTo("Show summaries") == 0) {
      try {
	// popup matrix panel
	if (!m_framePoppedUp) {
	  m_framePoppedUp = true;
	  final JScrollPane holderP = makePanel();

	  final javax.swing.JFrame jf = 
	    new javax.swing.JFrame("Visualize");
	  jf.setSize(800,600);
	  jf.getContentPane().setLayout(new BorderLayout());
	  jf.getContentPane().add(holderP, BorderLayout.CENTER);
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
		+ " not supported (AttributeSummarizer)");
    }
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
      
      // only need the x-axis (used to specify the attribute to plot)
      String xAxis = m_xAxis;
      try {
        xAxis = m_env.substitute(xAxis);
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
        BufferedImage osi = m_offscreenRenderer.renderHistogram(defWidth, defHeight, 
            m_offscreenPlotData, xAxis, options);

        ImageEvent ie = new ImageEvent(this, osi);
        notifyImageListeners(ie);
      } catch (Exception e1) {
        e1.printStackTrace();
      }
      
    }    
  }

  public static void main(String [] args) {
    try {
      if (args.length != 1) {
	System.err.println("Usage: AttributeSummarizer <dataset>");
	System.exit(1);
      }
      java.io.Reader r = new java.io.BufferedReader(
			 new java.io.FileReader(args[0]));
      Instances inst = new Instances(r);
      final javax.swing.JFrame jf = new javax.swing.JFrame();
      jf.getContentPane().setLayout(new java.awt.BorderLayout());
      final AttributeSummarizer as = new AttributeSummarizer();
      as.setInstances(inst);
      
      jf.getContentPane().add(as, java.awt.BorderLayout.CENTER);
      jf.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
          jf.dispose();
          System.exit(0);
        }
      });
      jf.setSize(830,600);
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
