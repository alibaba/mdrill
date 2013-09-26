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
 *    FileEnvironmentField.java
 *    Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.filechooser.FileFilter;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import weka.core.Environment;
import weka.gui.ExtensionFileFilter;
import weka.gui.FileEditor;
import weka.gui.PropertyDialog;

/**
 * Widget that displays a label, editable combo box for selecting
 * environment variables and a button for brining up a file browser. 
 * The user can enter arbitrary text, select an
 * environment variable or a combination of both. Any variables
 * are resolved (if possible) and resolved values are displayed
 * in a tip-text.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 7670 $
 */
public class FileEnvironmentField extends EnvironmentField {
  
  /** For serialization */
  private static final long serialVersionUID = -233731548086207652L;
  
  /** File editor component */
  protected FileEditor m_fileEditor = new FileEditor();  
  
  /** Dialog to hold the file editor */
  protected PropertyDialog m_fileEditorDialog;
  
  /** The button to pop up the file dialog */
  protected JButton m_browseBut;
  
  /**
   * Constructor
   */
  public FileEnvironmentField() {
    this("", JFileChooser.OPEN_DIALOG);
    setEnvironment(Environment.getSystemWide());
  }
  
  /**
   * Constructor
   * 
   * @param env an Environment object to use
   */
  public FileEnvironmentField(Environment env) {
    this("", JFileChooser.OPEN_DIALOG);
    setEnvironment(env);
  }
  
  public FileEnvironmentField(String label, Environment env) {
    this(label, JFileChooser.OPEN_DIALOG);
    setEnvironment(env);
  }
  
  /**
   * Constructor
   * 
   * @param label a label to display alongside the field.
   * @param env an Environment object to use.
   * @param fileChooserType the type of file chooser to use (either JFileChooser.OPEN_DIALOG
   * or JFileChooser.SAVE_DIALOG)
   * @param 
   */
  public FileEnvironmentField(String label, Environment env, 
      int fileChooserType) {
    this(label, fileChooserType);
    setEnvironment(env);
  }  
  
  /**
   * Constructor
   * 
   * @param label a label to display alongside the field.
   * @param fileChooserType the type of file chooser to use (either JFileChooser.OPEN_DIALOG
   * or JFileChooser.SAVE_DIALOG)
   */
  public FileEnvironmentField(String label, int fileChooserType) {
    super(label);
    
    m_fileEditor.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        File selected = (File)m_fileEditor.getValue();
        if (selected != null) {
          FileEnvironmentField.this.setText(selected.toString());
        }        
      }
    });
    
    final JFileChooser embeddedEditor = (JFileChooser)m_fileEditor.getCustomEditor();
    embeddedEditor.setFileSelectionMode(JFileChooser.FILES_ONLY);
    embeddedEditor.setDialogType(fileChooserType);
    ExtensionFileFilter ff =
      new ExtensionFileFilter(".model", "Serialized Weka classifier (*.model)");
    embeddedEditor.addChoosableFileFilter(ff);    
    
    m_browseBut = new JButton("Browse...");
    m_browseBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          String modelPath = getText();
          if (modelPath != null) {
            try {
              modelPath = m_env.substitute(modelPath);
            } catch (Exception ex) {              
            }
            
            File toSet = new File(modelPath);            
            if (toSet.isFile()) {
              m_fileEditor.setValue(new File(modelPath));
              toSet = toSet.getParentFile();              
            }
            if (toSet.isDirectory()) {
              embeddedEditor.setCurrentDirectory(toSet);
            }
          }
          
          showFileEditor();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });
    
    JPanel bP = new JPanel(); bP.setLayout(new BorderLayout());
    //bP.setBorder(BorderFactory.createEmptyBorder(5,0,5,5));
    bP.add(m_browseBut, BorderLayout.CENTER);
    
    add(bP, BorderLayout.EAST);    
  }  
  
  /**
   * Add a file filter to use
   * 
   * @param toSet the file filter to use
   */
  public void addFileFilter(FileFilter toSet) {
    JFileChooser embeddedEditor = (JFileChooser)m_fileEditor.getCustomEditor();
    embeddedEditor.addChoosableFileFilter(toSet);
  }
  
  /**
   * Resets the list of choosable file filters.
   */
  public void resetFileFilters() {
    JFileChooser embeddedEditor = (JFileChooser)m_fileEditor.getCustomEditor();
    embeddedEditor.resetChoosableFileFilters();
  }
  
  private void showFileEditor() {
    if (m_fileEditorDialog == null) {
      int x = getLocationOnScreen().x;
      int y = getLocationOnScreen().y;
      if (PropertyDialog.getParentDialog(this) != null) {
        m_fileEditorDialog = new PropertyDialog(PropertyDialog.getParentDialog(this), 
            m_fileEditor, x, y);
      } else {
        m_fileEditorDialog = new PropertyDialog(PropertyDialog.getParentFrame(this), 
            m_fileEditor, x, y);
      }
    }
    m_fileEditorDialog.setVisible(true);
  }
  
  public void removeNotify() {
    super.removeNotify();
    if (m_fileEditorDialog != null) {
      m_fileEditorDialog.dispose();
      m_fileEditorDialog = null;
    }
  }
  
  /**
   * Set the enabled status of the combo box and button
   * 
   * @param enabled true if the combo box and button are to be enabled
   */
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    m_browseBut.setEnabled(enabled);
  }
}
