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
 *    ClassAssignerCustomizer.java
 *    Copyright (C) 2002 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import weka.core.Attribute;
import weka.core.Instances;
import weka.gui.PropertySheetPanel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * GUI customizer for the class assigner bean
 *
 * @author <a href="mailto:mhall@cs.waikato.ac.nz">Mark Hall</a>
 * @version $Revision: 7443 $
 */
public class ClassAssignerCustomizer
  extends JPanel
  implements BeanCustomizer, CustomizerClosingListener, 
  CustomizerCloseRequester, DataFormatListener {

  /** for serialization */
  private static final long serialVersionUID = 476539385765301907L;

  private boolean m_displayColNames = false;

  private transient ClassAssigner m_classAssigner;

  private transient PropertyChangeSupport m_pcSupport = 
    new PropertyChangeSupport(this);

  private transient PropertySheetPanel m_caEditor = 
    new PropertySheetPanel();

  private transient JComboBox m_ClassCombo = new JComboBox();
  private transient JPanel m_holderP = new JPanel();
  
  private transient ModifyListener m_modifyListener;
  
  private transient Window m_parent;
  
  private transient String m_backup;

  public ClassAssignerCustomizer() {
    setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5));

    setLayout(new BorderLayout());
    add(new javax.swing.JLabel("ClassAssignerCustomizer"), 
	BorderLayout.NORTH);
    m_holderP.setLayout(new BorderLayout());
    m_holderP.setBorder(BorderFactory.createTitledBorder("Choose class attribute"));
    m_holderP.add(m_ClassCombo, BorderLayout.CENTER);
    m_ClassCombo.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  if (m_classAssigner != null && m_displayColNames == true) {
	    m_classAssigner.setClassColumn(""+(m_ClassCombo.getSelectedIndex()));
	  }
	}
      });
    add(m_caEditor, BorderLayout.CENTER);
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
        m_modifyListener.setModifiedStatus(ClassAssignerCustomizer.this, true);
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

  private void setUpStandardSelection() {
    if (m_displayColNames == true) {
      remove(m_holderP);
      m_caEditor.setTarget(m_classAssigner);
      add(m_caEditor, BorderLayout.CENTER);
      m_displayColNames = false;
    }
    validate(); repaint();
  }
  
  private void setUpColumnSelection(Instances format) {
    if (m_displayColNames == false) {
      remove(m_caEditor);
    }

    int existingClassCol = 0;
    
    String classColString = m_classAssigner.getClassColumn();
    if (classColString.trim().toLowerCase().compareTo("last") == 0 ||
        classColString.equalsIgnoreCase("/last")) {
      existingClassCol = format.numAttributes() - 1;
    } else if (classColString.trim().toLowerCase().compareTo("first") == 0 ||
        classColString.equalsIgnoreCase("/first")) {
      // nothing to do
    } else {
      // try to look up class attribute as a label
      Attribute classAtt = format.attribute(classColString);
      if (classAtt != null) {
        existingClassCol = classAtt.index();
      } else {
        // parse it as a number
        existingClassCol = Integer.parseInt(classColString);
        if (existingClassCol < 0) {
          existingClassCol = -1; // no class
        } else if (existingClassCol > format.numAttributes() - 1) {
          existingClassCol = format.numAttributes() - 1;
        } else {
          existingClassCol--; // make it zero-based (rather than 1-based)
        }
      }
    }
    
    //int existingClassCol = format.classIndex();

/*    if (existingClassCol < 0) {
      existingClassCol = 0;
    } */
    String [] attribNames = new String [format.numAttributes()+1];
    attribNames[0] = "NO CLASS";
    for (int i = 1; i < attribNames.length; i++) {
      String type = "(" + Attribute.typeToStringShort(format.attribute(i-1)) + ") ";
      attribNames[i] = type + format.attribute(i-1).name();
    }
    m_ClassCombo.setModel(new DefaultComboBoxModel(attribNames));
    if (attribNames.length > 0) {
      m_ClassCombo.setSelectedIndex(existingClassCol+1);
    }
    if (m_displayColNames == false) {
      add(m_holderP, BorderLayout.CENTER);
      m_displayColNames = true;
    }
    validate(); repaint();
  }

  /**
   * Set the bean to be edited
   *
   * @param object an <code>Object</code> value
   */
  public void setObject(Object object) {
    if (m_classAssigner != (ClassAssigner)object) {
      m_classAssigner = (ClassAssigner)object;
      // remove ourselves as a listener from the old ClassAssigner (if necessary)
/*      if (m_classAssigner != null) {
	m_classAssigner.removeDataFormatListener(this);
      }

      // add ourselves as a data format listener
      m_classAssigner.addDataFormatListener(this); */
      m_caEditor.setTarget(m_classAssigner);
      if (m_classAssigner.getConnectedFormat() != null) {
	setUpColumnSelection(m_classAssigner.getConnectedFormat());
      }
      m_backup = m_classAssigner.getClassColumn();
    }
  }

  public void customizerClosing() {
    // remove ourselves as a listener from the ClassAssigner (if necessary)
    if (m_classAssigner != null) {
      //System.out.println("Customizer deregistering with class assigner");
      m_classAssigner.removeDataFormatListener(this);
    }
    
    if (m_backup != null) {
      m_classAssigner.setClassColumn(m_backup);
    }
  }

  public void newDataFormat(DataSetEvent dse) {
    if (dse.getDataSet() != null) {
      //      System.err.println("Setting up column selection.........");
      setUpColumnSelection(m_classAssigner.getConnectedFormat());
    } else {
      setUpStandardSelection();
    }
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
}
