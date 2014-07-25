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
 *    CrossValidationFoldMakerCustomizer.java
 *    Copyright (C) 2002 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import weka.gui.PropertySheetPanel;
import weka.gui.beans.BeanCustomizer.ModifyListener;

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
 * GUI Customizer for the cross validation fold maker bean
 *
 * @author <a href="mailto:mhall@cs.waikato.ac.nz">Mark Hall</a>
 * @version $Revision: 7316 $
 */
public class CrossValidationFoldMakerCustomizer
  extends JPanel
  implements BeanCustomizer, CustomizerCloseRequester, 
  CustomizerClosingListener {

  /** for serialization */
  private static final long serialVersionUID = 1229878140258668581L;

  private PropertyChangeSupport m_pcSupport = 
    new PropertyChangeSupport(this);

  private PropertySheetPanel m_cvEditor = 
    new PropertySheetPanel();
  
  private CrossValidationFoldMaker m_cvMaker;
  private ModifyListener m_modifyListener;
  private int m_foldsBackup;
  private boolean m_orderBackup;
  private int m_seedBackup;
  
  private Window m_parent;

  public CrossValidationFoldMakerCustomizer() {
    setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5));

    setLayout(new BorderLayout());
    add(m_cvEditor, BorderLayout.CENTER);
    add(new javax.swing.JLabel("CrossValidationFoldMakerCustomizer"), 
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
        if (m_modifyListener != null) {
          m_modifyListener.
            setModifiedStatus(CrossValidationFoldMakerCustomizer.this, true);
        }
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
   * @param object a CrossValidationFoldMaker object
   */
  public void setObject(Object object) {
    m_cvMaker = ((CrossValidationFoldMaker)object);
    m_foldsBackup = m_cvMaker.getFolds();
    m_orderBackup = m_cvMaker.getPreserveOrder();
    m_seedBackup = m_cvMaker.getSeed();
    
    m_cvEditor.setTarget(m_cvMaker);
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
    
    // restore the backup
    m_cvMaker.setSeed(m_seedBackup);
    m_cvMaker.setFolds(m_foldsBackup);
    m_cvMaker.setPreserveOrder(m_orderBackup);
    
  }
}
