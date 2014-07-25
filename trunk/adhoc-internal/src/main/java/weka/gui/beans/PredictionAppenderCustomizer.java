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
 *    PredictionAppenderCustomizer.java
 *    Copyright (C) 2003 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import weka.gui.PropertySheetPanel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * GUI Customizer for the prediction appender bean
 *
 * @author <a href="mailto:mhall@cs.waikato.ac.nz">Mark Hall</a>
 * @version $Revision: 7320 $
 */

public class PredictionAppenderCustomizer
  extends JPanel
  implements BeanCustomizer, CustomizerCloseRequester, 
  CustomizerClosingListener {

  /** for serialization */
  private static final long serialVersionUID = 6884933202506331888L;

  private PropertyChangeSupport m_pcSupport = 
    new PropertyChangeSupport(this);
  
  private PropertySheetPanel m_paEditor = 
    new PropertySheetPanel();
  
  private PredictionAppender m_appender;
  private boolean m_appendProbsBackup;
  
  private ModifyListener m_modifyListener;
  private Window m_parent;

  public PredictionAppenderCustomizer() {
    setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5));

    setLayout(new BorderLayout());
    add(m_paEditor, BorderLayout.CENTER);
    add(new javax.swing.JLabel("PredcitionAppenderCustomizer"), 
	BorderLayout.NORTH);
    addButtons();
  }
  
  private void addButtons() {
    JButton okBut = new JButton("OK");
    JButton cancelBut = new JButton("Cancel");
    
    JPanel butHolder = new JPanel();
    butHolder.setLayout(new GridLayout(1, 2));
    butHolder.add(okBut); butHolder.add(cancelBut);
    add(butHolder, BorderLayout.SOUTH);
    
    okBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_modifyListener.setModifiedStatus(PredictionAppenderCustomizer.this, true);
        if (m_parent != null) {
          m_parent.dispose();
        }
      }
    });
    
    cancelBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        customizerClosing();
        if (m_parent != null) {
          m_parent.dispose();
        }
      }
    });
  }

  /**
   * Set the object to be edited
   *
   * @param object a PredictionAppender object
   */
  public void setObject(Object object) {
    m_appender = ((PredictionAppender)object);
    m_paEditor.setTarget(m_appender);
  }

  /**
   * Add a property change listener
   *
   * @param pcl a <code>PropertyChangeListener</code> value
   */
  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    m_pcSupport.addPropertyChangeListener(pcl);
  }
  
  /**
   * Remove a property change listener
   *
   * @param pcl a <code>PropertyChangeListener</code> value
   */
  public void removePropertyChangeListener(PropertyChangeListener pcl) {
    m_pcSupport.removePropertyChangeListener(pcl);
  }

  @Override
  public void setModifiedListener(ModifyListener l) {
    m_modifyListener = l;
  }

  @Override
  public void setParentWindow(Window parent) {
    m_parent = parent;
  }

  @Override
  public void customizerClosing() {
    // restore the backup value
    m_appender.setAppendPredictedProbabilities(m_appendProbsBackup);
    
  }
}
