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
 *    Note.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/**
 * Simple bean for displaying a textual note on the layout.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 7154 $
 *
 */
public class Note extends JPanel {
  
  /**
   * For serialization 
   */
  private static final long serialVersionUID = -7272355421198069040L;

  /** The note text */
  protected String m_noteText = "New note";
  
  /** The label that displays the note text */
  protected JLabel m_label = new JLabel();
  
  /** Adjustment for the font size */
  protected int m_fontSizeAdjust = -1;
  
  /**
   * Constructor
   */
  public Note() {
    setLayout(new BorderLayout());
//    setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    setBorder(new ShadowBorder(2, Color.GRAY));

    m_label.setText(convertToHTML(m_noteText));
    m_label.setOpaque(true);
    m_label.setBackground(Color.YELLOW);
    JPanel holder = new JPanel();
    holder.setLayout(new BorderLayout());
    holder.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    holder.setOpaque(true);
    holder.setBackground(Color.YELLOW);
    holder.add(m_label, BorderLayout.CENTER);
    add(holder, BorderLayout.CENTER);
  }
  
  public void setHighlighted(boolean highlighted) {
    if (highlighted) {
      setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLUE));
    } else {
      //setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
      setBorder(new ShadowBorder(2, Color.GRAY));
    }
    revalidate();
  }
  
  private String convertToHTML(String text) {
    String htmlString = m_noteText.replace("\n", "<br>");
    htmlString = "<html><font size=" 
      + m_fontSizeAdjust + ">" 
      + htmlString
      + "</font>"
      + "</html>";
    
    return htmlString;
  }    
  
  /**
   * Set the text to display
   * 
   * @param noteText the text to display in the note.
   */
  public void setNoteText(String noteText) {
    m_noteText = noteText;
    
    m_label.setText(convertToHTML(m_noteText));
  }
  
  /**
   * Get the note text
   * 
   * @return the note text
   */
  public String getNoteText() {
    return m_noteText;
  }
  
  /**
   * set the font size adjustment
   * 
   * @param adjust the font size adjustment
   */
  public void setFontSizeAdjust(int adjust) {
    m_fontSizeAdjust = adjust;
  }
  
  /**
   * Get the font size adjustment
   * 
   * @return the font size adjustment
   */
  public int getFontSizeAdjust() {
    return m_fontSizeAdjust;
  }
  
  /**
   * Decrease the font size by one
   */
  public void decreaseFontSize() {
    m_fontSizeAdjust--;
  }
  
  /**
   * Increase the font size by one
   */
  public void increaseFontSize() {
    m_fontSizeAdjust++;
  }
}
