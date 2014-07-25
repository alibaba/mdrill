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
 *    ScatterPlotMatrix.java
 *    Copyright (C) 2003 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import weka.core.Instances;
import weka.gui.visualize.MatrixPanel;

import java.awt.BorderLayout;

import javax.swing.Icon;

/**
 * Bean that encapsulates weka.gui.visualize.MatrixPanel for displaying a
 * scatter plot matrix.
 *
 * @author <a href="mailto:mhall@cs.waikato.ac.nz">Mark Hall</a>
 * @version $Revision: 7276 $
 */
public class ScatterPlotMatrix
  extends DataVisualizer implements KnowledgeFlowApp.KFPerspective {

  /** for serialization */
  private static final long serialVersionUID = -657856527563507491L;

  protected MatrixPanel m_matrixPanel;

  public ScatterPlotMatrix() {
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
    return "Visualize incoming data/training/test sets in a scatter "
      +"plot matrix.";
  }

  protected void appearanceDesign() {
    m_matrixPanel = null;
    removeAll();
    m_visual = 
      new BeanVisual("ScatterPlotMatrix", 
		     BeanVisual.ICON_PATH+"ScatterPlotMatrix.gif",
		     BeanVisual.ICON_PATH+"ScatterPlotMatrix_animated.gif");
    setLayout(new BorderLayout());
    add(m_visual, BorderLayout.CENTER);
  }

  protected void appearanceFinal() {
    removeAll();
    setLayout(new BorderLayout());
    setUpFinal();
  }

  protected void setUpFinal() {
    if (m_matrixPanel == null) {
      m_matrixPanel = new MatrixPanel();
    }
    add(m_matrixPanel, BorderLayout.CENTER);
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
    m_matrixPanel.setInstances(m_visualizeDataSet);
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
    return "Scatter plot matrix";
  }
  
  /**
   * Get the tool tip text for this perspective.
   * 
   * @return the tool tip text for this perspective
   */
  public String getPerspectiveTipText() {
    return "Scatter plot matrix";
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
      getResource("weka/gui/beans/icons/application_view_tile.png");

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
    
  }

  /**
   * Perform a named user request
   *
   * @param request a string containing the name of the request to perform
   * @exception IllegalArgumentException if request is not supported
   */
  public void performRequest(String request) {
    if (request.compareTo("Show plot") == 0) {
      try {
	// popup matrix panel
	if (!m_framePoppedUp) {
	  m_framePoppedUp = true;
	  final MatrixPanel vis = new MatrixPanel();
	  vis.setInstances(m_visualizeDataSet);

	  final javax.swing.JFrame jf = 
	    new javax.swing.JFrame("Visualize");
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
					 + " not supported (ScatterPlotMatrix)");
    }
  }

  public static void main(String [] args) {
    try {
      if (args.length != 1) {
	System.err.println("Usage: ScatterPlotMatrix <dataset>");
	System.exit(1);
      }
      java.io.Reader r = new java.io.BufferedReader(
			 new java.io.FileReader(args[0]));
      Instances inst = new Instances(r);
      final javax.swing.JFrame jf = new javax.swing.JFrame();
      jf.getContentPane().setLayout(new java.awt.BorderLayout());
      final ScatterPlotMatrix as = new ScatterPlotMatrix();
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
}
