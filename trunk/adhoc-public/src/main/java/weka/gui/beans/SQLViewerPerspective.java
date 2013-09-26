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
 *    SQLViewerPerspective.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.beancontext.BeanContextSupport;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

import weka.core.Instances;
import weka.core.converters.DatabaseLoader;
import weka.gui.beans.KnowledgeFlowApp.MainKFPerspective;
import weka.gui.sql.SqlViewer;
import weka.gui.sql.event.ConnectionEvent;
import weka.gui.sql.event.ConnectionListener;

/**
 * Simple Knowledge Flow perspective that wraps the SqlViewer class
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 7339 $
 */
public class SQLViewerPerspective extends JPanel implements KnowledgeFlowApp.KFPerspective {
  
  /**
   * For serialization
   */
  private static final long serialVersionUID = 3684166225482042972L;
  
  protected MainKFPerspective m_mainPerspective;
  protected SqlViewer m_viewer;
  protected JButton m_newFlowBut;
  
  /**
   * Constructor
   */
  public SQLViewerPerspective() {
    setLayout(new BorderLayout());
    
    m_viewer = new SqlViewer(null);
    add(m_viewer, BorderLayout.CENTER);
    
    m_newFlowBut = new JButton("New Flow");
    m_newFlowBut.setToolTipText("Set up a new Knowledge Flow with the " +
    		"current connection and query");
    JPanel butHolder = new JPanel();
    butHolder.add(m_newFlowBut);
    add(butHolder, BorderLayout.SOUTH);
    
    m_newFlowBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_mainPerspective != null) {
          newFlow();
        }
      }
    });
    m_newFlowBut.setEnabled(false);
    
    m_viewer.addConnectionListener(new ConnectionListener() {

      @Override
      public void connectionChange(ConnectionEvent evt) {
        if (evt.getType() == ConnectionEvent.DISCONNECT) {
          m_newFlowBut.setEnabled(false);
        } else {
          m_newFlowBut.setEnabled(true);
        }
      }
    });
  }
  
  protected void newFlow() {
    m_newFlowBut.setEnabled(false);
    
    String user = m_viewer.getUser();
    String password = m_viewer.getPassword();
    String uRL = m_viewer.getURL();
    String query = m_viewer.getQuery();
    
    if (query == null) {
      query = "";
    }
    
    try {
      DatabaseLoader dbl = new DatabaseLoader();
      dbl.setUser(user);
      dbl.setPassword(password);
      dbl.setUrl(uRL);
      dbl.setQuery(query);
      
      BeanContextSupport bc = new BeanContextSupport();
      bc.setDesignTime(true);
      
      Loader loaderComp = new Loader();
      bc.add(loaderComp);
      loaderComp.setLoader(dbl);
      
      KnowledgeFlowApp singleton = KnowledgeFlowApp.getSingleton();      
      m_mainPerspective.addTab("DBSource");
      BeanInstance beanI = 
        new BeanInstance(m_mainPerspective.getBeanLayout(m_mainPerspective.getNumTabs() - 1),
            loaderComp, 50, 50, m_mainPerspective.getNumTabs() - 1);
      Vector beans = BeanInstance.getBeanInstances(m_mainPerspective.getNumTabs() - 1);
      Vector connections = BeanConnection.getConnections(m_mainPerspective.getNumTabs() - 1);
      singleton.integrateFlow(beans, connections, true, false);
      singleton.setActivePerspective(0); // switch back to the main perspective                  
      
      m_newFlowBut.setEnabled(true);
      
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Set instances (if the perspective accepts them)
   * 
   * @param insts the instances
   */
  public void setInstances(Instances insts) throws Exception {
    // nothing to do - we don't take instances
    
  }

  /**
   * Returns true if this perspective accepts instances
   * 
   * @return true if this perspective can accept instances
   */
  public boolean acceptsInstances() {
    return false;
  }

  /**
   * Get the title of this perspective
   * 
   * @return the title of this perspective
   */
  public String getPerspectiveTitle() {
    return "SQL Viewer";
  }

  /**
   * Get the tool tip text for this perspective.
   * 
   * @return the tool tip text for this perspective
   */
  public String getPerspectiveTipText() {
    return "Explore database tables with SQL";
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
      getResource("weka/gui/beans/icons/database.png");

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
    // nothing to do
    
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
    // nothing to do
  }

  /**
   * Set a reference to the main KnowledgeFlow perspective - i.e.
   * the perspective that manages flow layouts.
   * 
   * @param main the main KnowledgeFlow perspective.
   */
  public void setMainKFPerspective(MainKFPerspective main) {
    // nothing to do (could potentially create a new flow in 
    // the knowledge flow with a configured DatabaseLoader).
    m_mainPerspective = main;    
  }
  
  /**
   * Main method for testing this class
   * 
   * @param args command line arguments
   */
  public static void main(String[] args) {
    final javax.swing.JFrame jf = new javax.swing.JFrame();
    jf.getContentPane().setLayout(new java.awt.BorderLayout());
    SQLViewerPerspective p = new SQLViewerPerspective();
    
    jf.getContentPane().add(p, BorderLayout.CENTER);
    jf.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        jf.dispose();
        System.exit(0);
      }
    });
    jf.setSize(800,600);
    jf.setVisible(true);
  }

}
