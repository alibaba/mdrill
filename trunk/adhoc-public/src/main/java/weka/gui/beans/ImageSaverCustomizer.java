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
 *    ImageSaverCustomizer.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.gui.PropertySheetPanel;

/**
 * Customizer for the ImageSaver component.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 7671 $
 */
public class ImageSaverCustomizer extends JPanel implements BeanCustomizer,
    EnvironmentHandler, CustomizerClosingListener, CustomizerCloseRequester {
  
  /**
   * For serialization
   */
  private static final long serialVersionUID = 5215477777077643368L;
  
  private ImageSaver m_imageSaver;
  
  private FileEnvironmentField m_fileEditor;
  
  private Environment m_env = Environment.getSystemWide();
  
  private ModifyListener m_modifyListener;
  
  private Window m_parent;   
  
  private String m_fileBackup;
  
  /**
   * Constructor
   */
  public ImageSaverCustomizer() {
    setLayout(new BorderLayout());
    
  }

  /**
   * Set the ImageSaver object to customize.
   * 
   * @param object the ImageSaver to customize
   */
  public void setObject(Object object) {
    m_imageSaver = (ImageSaver)object;
    m_fileBackup = m_imageSaver.getFilename();
    
    setup();
  }
  
  private void setup() {
    JPanel holder = new JPanel();
    holder.setLayout(new BorderLayout());
    
    m_fileEditor = new FileEnvironmentField("Filename", m_env,
        JFileChooser.SAVE_DIALOG);
    m_fileEditor.resetFileFilters();
    holder.add(m_fileEditor, BorderLayout.SOUTH);   
    
    String globalInfo = m_imageSaver.globalInfo();

    JTextArea jt = new JTextArea();
    jt.setColumns(30);
    jt.setFont(new Font("SansSerif", Font.PLAIN,12));
    jt.setEditable(false);
    jt.setLineWrap(true);
    jt.setWrapStyleWord(true);
    jt.setText(globalInfo);
    jt.setBackground(getBackground());
    JPanel jp = new JPanel();
    jp.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createTitledBorder("About"),
                 BorderFactory.createEmptyBorder(5, 5, 5, 5)
         ));
    jp.setLayout(new BorderLayout());
    jp.add(jt, BorderLayout.CENTER);
    
    holder.add(jp, BorderLayout.NORTH);
    
    add(holder, BorderLayout.CENTER);
    
    addButtons();
    
    m_fileEditor.setText(m_imageSaver.getFilename());
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
        m_imageSaver.setFilename(m_fileEditor.getText());
        
        if (m_modifyListener != null) {
          m_modifyListener.
            setModifiedStatus(ImageSaverCustomizer.this, true);
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
   * Set the environment variables to use
   * 
   * @param env the environment variables to use
   */
  public void setEnvironment(Environment env) {
    m_env = env;
  }

  /**
   * Set a listener interested in whether we've modified
   * the ImageSaver that we're customizing
   * 
   * @param l the listener
   */
  public void setModifiedListener(ModifyListener l) {
    m_modifyListener = l;
  }
  
  /**
   * Set the parent window of this dialog
   * 
   * @param parent the parent window
   */
  public void setParentWindow(Window parent) {
    m_parent = parent;
  }
  
  /**
   * Gets called if the use closes the dialog via the
   * close widget on the window - is treated as cancel,
   * so restores the ImageSaver to its previous state.
   */
  public void customizerClosing() {
    m_imageSaver.setFilename(m_fileBackup);
  }
}
