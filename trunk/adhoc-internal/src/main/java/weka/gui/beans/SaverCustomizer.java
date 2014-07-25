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
 *    SaverCustomizer.java
 *    Copyright (C) 2004 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.converters.DatabaseConverter;
import weka.core.converters.DatabaseSaver;
import weka.core.converters.FileSourcedConverter;
import weka.gui.ExtensionFileFilter;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertySheetPanel;

/**
 * GUI Customizer for the saver bean
 *
 * @author <a href="mailto:mutter@cs.waikato.ac.nz">Stefan Mutter</a>
 * @version $Revision: 7776 $
 */
public class SaverCustomizer
extends JPanel
implements BeanCustomizer, CustomizerCloseRequester, EnvironmentHandler {

  /** for serialization */
  private static final long serialVersionUID = -4874208115942078471L;

  static {
    GenericObjectEditor.registerEditors();
  }

  private PropertyChangeSupport m_pcSupport = 
    new PropertyChangeSupport(this);

  private weka.gui.beans.Saver m_dsSaver;

  private PropertySheetPanel m_SaverEditor = 
    new PropertySheetPanel();

  private JFileChooser m_fileChooser 
  = new JFileChooser(new File(System.getProperty("user.dir")));


  private Window m_parentWindow;
  
  private JDialog m_fileChooserFrame;

  private EnvironmentField m_dbaseURLText;

  private EnvironmentField m_userNameText;

  private JPasswordField m_passwordText;

  private EnvironmentField m_tableText;

  private JCheckBox m_idBox;

  private JCheckBox m_tabBox;

  private EnvironmentField m_prefixText;

  private JCheckBox m_relativeFilePath;

  private JCheckBox m_relationNameForFilename;

  private Environment m_env = Environment.getSystemWide();
  
  private EnvironmentField m_directoryText;
  
  private FileEnvironmentField m_dbProps;
  
  private ModifyListener m_modifyListener;


  /** Constructor */  
  public SaverCustomizer() {

    setLayout(new BorderLayout());
    m_fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    m_fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    m_fileChooser.setApproveButtonText("Select directory");
    m_fileChooser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
          try {
            File selectedFile = m_fileChooser.getSelectedFile();
            m_directoryText.setText(selectedFile.toString());

            /* (m_dsSaver.getSaver()).setFilePrefix(m_prefixText.getText());
                (m_dsSaver.getSaver()).setDir(m_fileChooser.getSelectedFile().getPath());
                m_dsSaver.
                  setRelationNameForFilename(m_relationNameForFilename.isSelected()); */

          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
        // closing
        if (m_fileChooserFrame != null) {
          m_fileChooserFrame.dispose();
        }
      }
    });   
  }

  public void setParentWindow(Window parent) {
    m_parentWindow = parent;
  }

  /** Sets up dialog for saving instances in other data sinks then files
   * To be extended.
   */ 
  private void setUpOther() {
    removeAll();
    add(m_SaverEditor, BorderLayout.CENTER);
    validate();
    repaint();
  }

  /** Sets up the dialog for saving to a database*/
  private void setUpDatabase() {

    removeAll();
    JPanel db = new JPanel();
    GridBagLayout gbLayout = new GridBagLayout();
    db.setLayout(gbLayout);
    
    JLabel dbaseURLLab = new JLabel(" Database URL", SwingConstants.RIGHT);
    dbaseURLLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    GridBagConstraints gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 0; gbConstraints.gridx = 0;
    gbLayout.setConstraints(dbaseURLLab, gbConstraints);
    db.add(dbaseURLLab);
    
    m_dbaseURLText = new EnvironmentField();
    m_dbaseURLText.setEnvironment(m_env);
/*    int width = m_dbaseURLText.getPreferredSize().width;
    int height = m_dbaseURLText.getPreferredSize().height;
    m_dbaseURLText.setMinimumSize(new Dimension(width * 2, height));
    m_dbaseURLText.setPreferredSize(new Dimension(width * 2, height)); */
    m_dbaseURLText.setText(((DatabaseConverter)m_dsSaver.getSaverTemplate()).getUrl());
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 0; gbConstraints.gridx = 1;
    gbConstraints.weightx = 5;
    gbLayout.setConstraints(m_dbaseURLText, gbConstraints);
    db.add(m_dbaseURLText);    
    
    JLabel userLab = new JLabel("Username", SwingConstants.RIGHT);
    userLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 1; gbConstraints.gridx = 0;
    gbLayout.setConstraints(userLab, gbConstraints);
    db.add(userLab);

    m_userNameText = new EnvironmentField();
    m_userNameText.setEnvironment(m_env);
/*    m_userNameText.setMinimumSize(new Dimension(width * 2, height));
    m_userNameText.setPreferredSize(new Dimension(width * 2, height)); */
    m_userNameText.setText(((DatabaseConverter)m_dsSaver.getSaverTemplate()).getUser());
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 1; gbConstraints.gridx = 1;
    gbLayout.setConstraints(m_userNameText, gbConstraints);
    db.add(m_userNameText);
    
    JLabel passwordLab = new JLabel("Password ", SwingConstants.RIGHT);
    passwordLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 2; gbConstraints.gridx = 0;
    gbLayout.setConstraints(passwordLab, gbConstraints);
    db.add(passwordLab);

    m_passwordText = new JPasswordField();
    JPanel passwordHolder = new JPanel();
    passwordHolder.setLayout(new BorderLayout());
    passwordHolder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    passwordHolder.add(m_passwordText, BorderLayout.CENTER);
    /*passwordHolder.setMinimumSize(new Dimension(width * 2, height));
    passwordHolder.setPreferredSize(new Dimension(width * 2, height)); */
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 2; gbConstraints.gridx = 1;
    gbLayout.setConstraints(passwordHolder, gbConstraints);
    db.add(passwordHolder);

    JLabel tableLab = new JLabel("Table Name", SwingConstants.RIGHT);
    tableLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 3; gbConstraints.gridx = 0;
    gbLayout.setConstraints(tableLab, gbConstraints);
    db.add(tableLab);
    
    m_tableText = new EnvironmentField();
    m_tableText.setEnvironment(m_env);
/*    m_tableText.setMinimumSize(new Dimension(width * 2, height));
    m_tableText.setPreferredSize(new Dimension(width * 2, height)); */
    m_tableText.setEnabled(!((DatabaseSaver)m_dsSaver.getSaverTemplate()).getRelationForTableName());
    m_tableText.setText(((DatabaseSaver)m_dsSaver.getSaverTemplate()).getTableName());
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 3; gbConstraints.gridx = 1;
    gbLayout.setConstraints(m_tableText, gbConstraints);
    db.add(m_tableText);
        
    JLabel tabLab = new JLabel("Use relation name", SwingConstants.RIGHT);
    tabLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 4; gbConstraints.gridx = 0;
    gbLayout.setConstraints(tabLab, gbConstraints);
    db.add(tabLab);
    
    m_tabBox = new JCheckBox();
    m_tabBox.setSelected(((DatabaseSaver)m_dsSaver.getSaverTemplate()).getRelationForTableName()); 
    m_tabBox.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        m_tableText.setEnabled(!m_tabBox.isSelected());
      }
    });
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 4; gbConstraints.gridx = 1;
    gbLayout.setConstraints(m_tabBox, gbConstraints);
    db.add(m_tabBox);

    JLabel idLab = new JLabel("Automatic primary key", SwingConstants.RIGHT);
    idLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 5; gbConstraints.gridx = 0;
    gbLayout.setConstraints(idLab, gbConstraints);
    db.add(idLab);
    
    m_idBox = new JCheckBox();
    m_idBox.setSelected(((DatabaseSaver)m_dsSaver.getSaverTemplate()).getAutoKeyGeneration());
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 5; gbConstraints.gridx = 1;
    gbLayout.setConstraints(m_idBox, gbConstraints);
    db.add(m_idBox);
    
    JLabel propsLab = new JLabel("DB config props", SwingConstants.RIGHT);
    propsLab.setToolTipText("The custom properties that the user can use to override the default ones.");
    propsLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 6; gbConstraints.gridx = 0;
    gbLayout.setConstraints(propsLab, gbConstraints);
    db.add(propsLab);
    
    m_dbProps = new FileEnvironmentField();
    m_dbProps.setEnvironment(m_env);
    m_dbProps.resetFileFilters();
    m_dbProps.addFileFilter(new ExtensionFileFilter(".props" , 
        "DatabaseUtils property file (*.props)"));
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 6; gbConstraints.gridx = 1;
    gbLayout.setConstraints(m_dbProps, gbConstraints);
    db.add(m_dbProps);
    File toSet = ((DatabaseSaver)m_dsSaver.getSaverTemplate()).getCustomPropsFile();
    if (toSet != null) {
      m_dbProps.setText(toSet.getPath());
    }
    JButton loadPropsBut = new JButton("Load");
    loadPropsBut.setToolTipText("Load config");
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 6; gbConstraints.gridx = 2;
    gbLayout.setConstraints(loadPropsBut, gbConstraints);
    db.add(loadPropsBut);
    loadPropsBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_dbProps.getText() != null &&
            m_dbProps.getText().length() > 0) {
          String propsS = m_dbProps.getText();
          try {
            propsS = m_env.substitute(propsS);
          } catch (Exception ex) { }
          File propsFile = new File(propsS);
          if (propsFile.exists()) {
            ((DatabaseSaver)m_dsSaver.getSaverTemplate()).setCustomPropsFile(propsFile);
            ((DatabaseSaver)m_dsSaver.getSaverTemplate()).resetOptions();
            m_dbaseURLText.setText(((DatabaseConverter)m_dsSaver.getSaverTemplate()).getUrl());
          }
        }
      }
    });

    JPanel buttonsP = new JPanel();
    buttonsP.setLayout(new FlowLayout());
    JButton ok,cancel;
    buttonsP.add(ok = new JButton("OK"));
    buttonsP.add(cancel=new JButton("Cancel"));
    ok.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        if (m_dbProps.getText().length() > 0) {
          ((DatabaseSaver)m_dsSaver.getSaverTemplate()).
            setCustomPropsFile(new File(m_dbProps.getText()));
        }
        ((DatabaseSaver)m_dsSaver.getSaverTemplate()).resetStructure();
        ((DatabaseSaver)m_dsSaver.getSaverTemplate()).resetOptions();  
        ((DatabaseConverter)m_dsSaver.getSaverTemplate()).setUrl(m_dbaseURLText.getText());
        ((DatabaseConverter)m_dsSaver.getSaverTemplate()).setUser(m_userNameText.getText());
        ((DatabaseConverter)m_dsSaver.getSaverTemplate()).setPassword(new String(m_passwordText.getPassword()));
        if(!m_tabBox.isSelected()) {
          ((DatabaseSaver)m_dsSaver.getSaverTemplate()).setTableName(m_tableText.getText());
        }
        ((DatabaseSaver)m_dsSaver.getSaverTemplate()).setAutoKeyGeneration(m_idBox.isSelected());
        ((DatabaseSaver)m_dsSaver.getSaverTemplate()).setRelationForTableName(m_tabBox.isSelected());
        
        if (m_modifyListener != null) {
          m_modifyListener.setModifiedStatus(SaverCustomizer.this, true);
        }
        
        if (m_parentWindow != null) {
          m_parentWindow.dispose();
        }
      }
    });
    cancel.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        if (m_modifyListener != null) {
          m_modifyListener.setModifiedStatus(SaverCustomizer.this, false);
        }
        
        if (m_parentWindow != null) {
          m_parentWindow.dispose();
        }
      }
    });
    
    JPanel holderP = new JPanel();
    holderP.setLayout(new BorderLayout());
    holderP.add(db, BorderLayout.NORTH);
    holderP.add(buttonsP, BorderLayout.SOUTH);

//    db.add(buttonsP);
    JPanel about = m_SaverEditor.getAboutPanel();
    if (about != null) {
      add(about, BorderLayout.NORTH);
    }
    add(holderP,BorderLayout.SOUTH);
  }

  /** Sets up dialog for saving instances in a file */  
  public void setUpFile() {
    removeAll();
    
    m_fileChooser.setFileFilter(new FileFilter() { 
      public boolean accept(File f) { 
        return f.isDirectory();
      }
      public String getDescription() {
        return "Directory";
      }
    });
    
    m_fileChooser.setAcceptAllFileFilterUsed(false);
    
    try{
      if(!(((m_dsSaver.getSaverTemplate()).retrieveDir()).equals(""))) {
        String dirStr = m_dsSaver.getSaverTemplate().retrieveDir();
        if (Environment.containsEnvVariables(dirStr)) {
          try {
            dirStr = m_env.substitute(dirStr);
          } catch (Exception ex) {
            // ignore
          }          
        }
        File tmp = new File(dirStr);
        tmp = new File(tmp.getAbsolutePath());
        m_fileChooser.setCurrentDirectory(tmp);
      }
    } catch(Exception ex) {
      System.out.println(ex);
    }
    
    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());
    
    JPanel alignedP = new JPanel();
    GridBagLayout gbLayout = new GridBagLayout();
    alignedP.setLayout(gbLayout);
    
    final JLabel prefixLab = new JLabel("Prefix for file name", SwingConstants.RIGHT);
    prefixLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    GridBagConstraints gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 0; gbConstraints.gridx = 0;
    gbLayout.setConstraints(prefixLab, gbConstraints);
    alignedP.add(prefixLab);
    
    m_prefixText = new EnvironmentField();
    m_prefixText.setEnvironment(m_env);
    m_prefixText.setToolTipText("Prefix for file name "
        + "(or filename itself if relation name is not used)");
/*    int width = m_prefixText.getPreferredSize().width;
    int height = m_prefixText.getPreferredSize().height;
    m_prefixText.setMinimumSize(new Dimension(width * 2, height));
    m_prefixText.setPreferredSize(new Dimension(width * 2, height)); */
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 0; gbConstraints.gridx = 1;
    gbLayout.setConstraints(m_prefixText, gbConstraints);
    alignedP.add(m_prefixText);
    
    try{
//      m_prefixText = new JTextField(m_dsSaver.getSaver().filePrefix(),25);

      m_prefixText.setText(m_dsSaver.getSaverTemplate().filePrefix());
      
/*      final JLabel prefixLab = 
        new JLabel(" Prefix for file name:", SwingConstants.LEFT); */
      
      JLabel relationLab = new JLabel("Relation name for filename", SwingConstants.RIGHT);
      relationLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
      gbConstraints = new GridBagConstraints();
      gbConstraints.anchor = GridBagConstraints.EAST;
      gbConstraints.fill = GridBagConstraints.HORIZONTAL;
      gbConstraints.gridy = 1; gbConstraints.gridx = 0;
      gbLayout.setConstraints(relationLab, gbConstraints);
      alignedP.add(relationLab);
      
      m_relationNameForFilename = new JCheckBox();
      m_relationNameForFilename.setSelected(m_dsSaver.getRelationNameForFilename());
      if (m_dsSaver.getRelationNameForFilename()) {
        prefixLab.setText("Prefix for file name");
      } else {
        prefixLab.setText("File name");
      }
      m_relationNameForFilename.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (m_relationNameForFilename.isSelected()) {
            prefixLab.setText("Prefix for file name");
//            m_fileChooser.setApproveButtonText("Select directory and prefix");
          } else {
            prefixLab.setText("File name");
  //          m_fileChooser.setApproveButtonText("Select directory and filename");
          }
        }
      });
      
      gbConstraints = new GridBagConstraints();
      gbConstraints.anchor = GridBagConstraints.EAST;
      gbConstraints.fill = GridBagConstraints.HORIZONTAL;
      gbConstraints.gridy = 1; gbConstraints.gridx = 1;
      gbConstraints.weightx = 5;
      gbLayout.setConstraints(m_relationNameForFilename, gbConstraints);
      alignedP.add(m_relationNameForFilename);
    } catch(Exception ex){
    }
    //innerPanel.add(m_SaverEditor, BorderLayout.SOUTH);
    JPanel about = m_SaverEditor.getAboutPanel();
    if (about != null) {
      innerPanel.add(about, BorderLayout.NORTH);
    }
    add(innerPanel, BorderLayout.NORTH);
//    add(m_fileChooser, BorderLayout.CENTER);
    
    JLabel directoryLab = new JLabel("Directory", SwingConstants.RIGHT);
    directoryLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 2; gbConstraints.gridx = 0;
    gbLayout.setConstraints(directoryLab, gbConstraints);
    alignedP.add(directoryLab);
    
    m_directoryText = new EnvironmentField();
//    m_directoryText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    m_directoryText.setEnvironment(m_env);  
/*    width = m_directoryText.getPreferredSize().width;
    height = m_directoryText.getPreferredSize().height;
    m_directoryText.setMinimumSize(new Dimension(width * 2, height));
    m_directoryText.setPreferredSize(new Dimension(width * 2, height)); */
    
    try {
      m_directoryText.setText(m_dsSaver.getSaverTemplate().retrieveDir());
    } catch (IOException ex) {
      // ignore
    }
    
    JButton browseBut = new JButton("Browse...");
    browseBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          //final JFrame jf = new JFrame("Choose directory");
          final JDialog jf = new JDialog((JDialog)SaverCustomizer.this.getTopLevelAncestor(), 
              "Choose directory", true);
          jf.setLayout(new BorderLayout());
          jf.getContentPane().add(m_fileChooser, BorderLayout.CENTER);
          m_fileChooserFrame = jf;
          jf.pack();
          jf.setVisible(true);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });
    
    JPanel efHolder = new JPanel();
    efHolder.setLayout(new BorderLayout());
    JPanel bP = new JPanel(); bP.setLayout(new BorderLayout());
    bP.setBorder(BorderFactory.createEmptyBorder(5,0,5,5));
    bP.add(browseBut, BorderLayout.CENTER);
    efHolder.add(m_directoryText, BorderLayout.CENTER);
    efHolder.add(bP, BorderLayout.EAST);
    //efHolder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 2; gbConstraints.gridx = 1;
    gbLayout.setConstraints(efHolder, gbConstraints);
    alignedP.add(efHolder);
    

    JLabel relativeLab = new JLabel("Use relative file paths", SwingConstants.RIGHT);
    relativeLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 3; gbConstraints.gridx = 0;
    gbLayout.setConstraints(relativeLab, gbConstraints);
    alignedP.add(relativeLab);
    
    m_relativeFilePath = new JCheckBox();
    m_relativeFilePath.
    setSelected(((FileSourcedConverter)m_dsSaver.getSaverTemplate()).getUseRelativePath());

    m_relativeFilePath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ((FileSourcedConverter)m_dsSaver.getSaverTemplate()).
        setUseRelativePath(m_relativeFilePath.isSelected());
      }
    });
    gbConstraints = new GridBagConstraints();
    gbConstraints.anchor = GridBagConstraints.EAST;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.gridy = 3; gbConstraints.gridx = 1;
    gbLayout.setConstraints(m_relativeFilePath, gbConstraints);
    alignedP.add(m_relativeFilePath);
        
    JButton OKBut = new JButton("OK");
    OKBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {          
          (m_dsSaver.getSaverTemplate()).setFilePrefix(m_prefixText.getText());
          (m_dsSaver.getSaverTemplate()).setDir(m_directoryText.getText());
          m_dsSaver.
            setRelationNameForFilename(m_relationNameForFilename.isSelected());
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        
        if (m_modifyListener != null) {
          m_modifyListener.setModifiedStatus(SaverCustomizer.this, true);
        }
        
        m_parentWindow.dispose();
      }
    });

    JButton CancelBut = new JButton("Cancel");
    CancelBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_modifyListener != null) {
          m_modifyListener.setModifiedStatus(SaverCustomizer.this, false);
        }
        
        m_parentWindow.dispose();
      }
    });
    
    JPanel butHolder = new JPanel();
    butHolder.setLayout(new FlowLayout());
    butHolder.add(OKBut);
    butHolder.add(CancelBut);
    JPanel holder2 = new JPanel();
    holder2.setLayout(new BorderLayout());
    holder2.add(alignedP, BorderLayout.NORTH);
    
    JPanel optionsHolder = new JPanel();
    optionsHolder.setLayout(new BorderLayout());
    optionsHolder.setBorder(BorderFactory.createTitledBorder("Other options"));

    optionsHolder.add(m_SaverEditor, BorderLayout.SOUTH);
    JScrollPane scroller = new JScrollPane(optionsHolder);
    //holder2.add(scroller, BorderLayout.CENTER);
    
    //holder2.add(butHolder, BorderLayout.SOUTH);
    innerPanel.add(holder2, BorderLayout.SOUTH);
    add(scroller, BorderLayout.CENTER);
    add(butHolder, BorderLayout.SOUTH);
  }

  /**
   * Set the saver to be customized
   *
   * @param object a weka.gui.beans.Saver
   */
  public void setObject(Object object) {
    m_dsSaver = (weka.gui.beans.Saver)object;
    m_SaverEditor.setTarget(m_dsSaver.getSaverTemplate());
    if(m_dsSaver.getSaverTemplate() instanceof DatabaseConverter){
      setUpDatabase();
    }
    else{
      if (m_dsSaver.getSaverTemplate() instanceof FileSourcedConverter) {
        setUpFile();
      } else {
        setUpOther();
      }
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

  /* (non-Javadoc)
   * @see weka.core.EnvironmentHandler#setEnvironment(weka.core.Environment)
   */
  public void setEnvironment(Environment env) {
    m_env = env;
  }

  @Override
  public void setModifiedListener(ModifyListener l) {
    // TODO Auto-generated method stub
    m_modifyListener = l;
  }
}
