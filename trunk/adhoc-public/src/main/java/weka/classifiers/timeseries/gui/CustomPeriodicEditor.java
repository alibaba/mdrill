/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
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
 *    CustomPeriodicEditor.java
 *    Copyright (C) 2011 Pentaho Corporation
 */

package weka.classifiers.timeseries.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import weka.classifiers.timeseries.core.CustomPeriodicTest;

/**
 * Provides a gui editor for a custom periodic test
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49983 $
 */
public class CustomPeriodicEditor extends JPanel {
  
  /** For serialization */
  private static final long serialVersionUID = 9027916898659519804L;

  /** The list of tests to edit/add to */
  protected List<CustomPeriodicTest> m_testsToEdit;
  
  /** The lower bound editor */
  protected CustomPeriodicTestEditor m_lowerBoundEditor = 
    new CustomPeriodicTestEditor(false);
  
  /** The upper bound editor */
  protected CustomPeriodicTestEditor m_upperBoundEditor = 
    new CustomPeriodicTestEditor(true);
  
  /** The name of the custom periodic field */
  protected JTextField m_fieldName = new JTextField("MyNewField");
  
  /** The list of tests to edit/add to */
  protected JList m_testsToEditList;
  
  /** Button that adds a new interval to the custom periodic test */
  protected JButton m_newBut = new JButton("New");
  
  /** Button that deletes the selected interval from the test */
  protected JButton m_deleteBut = new JButton("Delete");
  
  /** field for defining labels for tests */
  protected JTextField m_labelField = new JTextField();
  
  /**
   * Constructor
   * 
   * @param tests the list of test intervals to edit - may be an empty
   * list for a new custom periodic test.
   */
  public CustomPeriodicEditor(List<CustomPeriodicTest> tests) {
    m_testsToEdit = tests;
    //m_testsToEdit = tests;
    final DefaultListModel testVec = new DefaultListModel();
    for (CustomPeriodicTest t : tests) {
      testVec.addElement(t);
    }
    
    m_testsToEditList = new JList(testVec);
    JScrollPane js = new JScrollPane(m_testsToEditList);
    js.setBorder(BorderFactory.createTitledBorder("Test list (rows are OR'ed)"));
/*    JPanel scrollHolder = new JPanel();
    scrollHolder.setLayout(new BorderLayout());
    scrollHolder.setBorder(BorderFactory.createTitledBorder("Test list (rows are OR'ed)")); */
    m_testsToEditList.setVisibleRowCount(5);
    m_deleteBut.setEnabled(false);
    
    setLayout(new BorderLayout());
    JPanel fieldHolder = new JPanel();
    fieldHolder.setLayout(new BorderLayout());
    fieldHolder.setBorder(BorderFactory.createTitledBorder("Field name"));
    fieldHolder.add(m_fieldName, BorderLayout.CENTER);
    
    JPanel buttHolder = new JPanel();
    buttHolder.setLayout(new BorderLayout());
    buttHolder.setBorder(BorderFactory.createTitledBorder("Edit"));
    JPanel tempH = new JPanel(); tempH.setLayout(new BorderLayout());
    tempH.add(m_newBut, BorderLayout.WEST);
    tempH.add(m_deleteBut, BorderLayout.EAST);
    buttHolder.add(tempH, BorderLayout.WEST);
    
    JPanel fieldAndButtHolder = new JPanel();
    fieldAndButtHolder.setLayout(new BorderLayout());
    fieldAndButtHolder.add(fieldHolder, BorderLayout.NORTH);
    fieldAndButtHolder.add(buttHolder, BorderLayout.CENTER);
    JPanel testEditorHolder = new JPanel();
    testEditorHolder.setLayout(new BorderLayout());
    JPanel tempP = new JPanel(); tempP.setLayout(new BorderLayout());
    testEditorHolder.setBorder(BorderFactory.createTitledBorder("Test interval"));
    tempP.add(m_lowerBoundEditor, BorderLayout.NORTH);
    tempP.add(m_upperBoundEditor, BorderLayout.SOUTH);
    JPanel labelHolder = new JPanel();
    labelHolder.setLayout(new BorderLayout());
    JLabel labelLabel = new JLabel("Label");
    labelLabel.setToolTipText("The label for this test. If all tests have " +
    		"a label then a nominal attribute is created; otherwise it is binary");
    labelHolder.add(labelLabel, BorderLayout.WEST);
    labelHolder.add(m_labelField, BorderLayout.CENTER);
    m_labelField.setToolTipText("The label for this test. If all tests have " +
        "a label then a nominal attribute is created; otherwise it is binary");
    
    testEditorHolder.add(tempP, BorderLayout.NORTH);
    testEditorHolder.add(labelHolder, BorderLayout.SOUTH);
    fieldAndButtHolder.add(testEditorHolder, BorderLayout.SOUTH);    
    
    
    add(fieldAndButtHolder, BorderLayout.NORTH);
    
    add(js, BorderLayout.CENTER);
    m_testsToEditList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          if (!m_deleteBut.isEnabled()) {
            m_deleteBut.setEnabled(true);
          }
          
          Object test = m_testsToEditList.getSelectedValue();
          if (test != null) {
            CustomPeriodicTest t = (CustomPeriodicTest)test;
            m_lowerBoundEditor.setTestToEdit(t);
            m_upperBoundEditor.setTestToEdit(t);
            if (t.getLabel() != null && t.getLabel().length() > 0) {
              m_labelField.setText(t.getLabel());
            } else {
              m_labelField.setText("");
            }
          }
        }
      }      
    });
    
    m_newBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CustomPeriodicTest newTest = 
          new CustomPeriodicTest(">*:*:*:*:*:*:*:*:*:* <*:*:*:*:*:*:*:*:*:*");
        m_lowerBoundEditor.setTestToEdit(newTest);
        m_upperBoundEditor.setTestToEdit(newTest);
        testVec.addElement(newTest);
        m_testsToEdit.add(newTest);
        m_labelField.setText("");
        m_testsToEditList.setSelectedIndex(m_testsToEdit.size() - 1);
      }
    });
    
    m_deleteBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int selected = m_testsToEditList.getSelectedIndex();
        if (selected >= 0) {
          testVec.removeElementAt(selected);
          m_testsToEdit.remove(selected);
        }
      }
    });
    
    m_lowerBoundEditor.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent e) {
        /*m_testsToEditList.revalidate();
        m_testsToEditList.invalidate(); */
        m_testsToEditList.repaint();
        if (m_lowerBoundEditor.getTestBeingEdited().getLowerTest().m_boundOperator ==
          CustomPeriodicTest.Operator.EQUALS) {
          m_upperBoundEditor.setEnabled(false);
        } else {
          m_upperBoundEditor.setEnabled(true);
        }
      }      
    });
    
    m_upperBoundEditor.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent e) {
        /*m_testsToEditList.revalidate();
        m_testsToEditList.invalidate(); */
        m_testsToEditList.repaint();
      }      
    });
    
    m_labelField.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        Object test = m_testsToEditList.getSelectedValue();
        if (test != null) {
          CustomPeriodicTest t = (CustomPeriodicTest)test;
          t.setLabel(m_labelField.getText());
          m_testsToEditList.repaint();
        }
      }
    });
    /*m_labelField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        Object test = m_testsToEditList.getSelectedValue();
        if (test != null) {
          CustomPeriodicTest t = (CustomPeriodicTest)test;
          t.setLabel(m_labelField.getText());
          m_testsToEditList.repaint();
        }
      }
    });*/
  }
  
  /**
   * Get the name for this custom periodic test (will be used as the field name)
   * 
   * @return the name to use for this custom periodic test
   */
  public String getFieldName() {
    return m_fieldName.getText();
  }
  
  /**
   * Set the name for this custom periodic test
   * 
   * @param name the name for this custom periodic test
   */
  public void setFieldName(String name) {
    m_fieldName.setText(name);
  }
  
  /**
   * Main method for testing this class.
   * 
   * @param args no command line arguments are needed.  
   */
  public static void main(String[] args) {
    try {
      final List<CustomPeriodicTest> testList = 
        new java.util.ArrayList<CustomPeriodicTest>();
      
      CustomPeriodicEditor ed = new CustomPeriodicEditor(testList);
      
      final javax.swing.JFrame jf =
        new javax.swing.JFrame("Periodic test editor");
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(ed, BorderLayout.CENTER);
      jf.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
          for (CustomPeriodicTest t : testList) {
            System.out.println(t.toString());
          }
          jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}
