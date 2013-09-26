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
 *    NoteCustomizer.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

/**
 * Customizer for the note component.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 7146 $
 *
 */
public class NoteCustomizer extends JPanel implements BeanCustomizer,
    CustomizerCloseRequester, CustomizerClosingListener {
  
  /**
   * for serialization
   */
  private static final long serialVersionUID = 995648616684953391L;
  
  /** the parent window */
  protected Window m_parentWindow;
  
  /** the note to be edited */
  protected Note m_note;
  
  /** text area for displaying the note's text */
  protected JTextArea m_textArea = new JTextArea(5, 20);
  
  /**
   *  Listener that wants to know the the modified status of the object that
   * we're customizing
   */
  private ModifyListener m_modifyListener;
  
  /**
   * Constructs a new note customizer
   */
  public NoteCustomizer() {
    setLayout(new BorderLayout());
    m_textArea.setLineWrap(true);
    
    JScrollPane sc = new JScrollPane(m_textArea);
    
    add(sc, BorderLayout.CENTER);
    
    JButton okBut = new JButton("OK");
    add(okBut, BorderLayout.SOUTH);
    okBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        customizerClosing();
        if (m_parentWindow != null) {
          m_parentWindow.dispose();
        }
      }
    });
  }

  @Override
  public void setParentWindow(Window parent) {
    // TODO Auto-generated method stub
    m_parentWindow = parent;
  }

  @Override
  public void setObject(Object ob) {
    // TODO Auto-generated method stub
    m_note = (Note)ob;
    m_textArea.setText(m_note.getNoteText());
    m_textArea.selectAll();
  }

  @Override
  public void customizerClosing() {
    if (m_note != null) {
      m_note.setNoteText(m_textArea.getText());
      
      if (m_modifyListener != null) {
        m_modifyListener.setModifiedStatus(NoteCustomizer.this, true);
      }
    }    
  }

  @Override
  public void setModifiedListener(ModifyListener l) {
    m_modifyListener = l;
  }
}
