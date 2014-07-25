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
 *    AttributeSummarizerCustomizer.java
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
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import weka.core.Environment;
import weka.core.EnvironmentHandler;

/**
 * GUI customizer for attribute summarizer. Allows the customization of
 * options for offscreen chart rendering (i.e. the payload of "ImageEvent"
 * connections).
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 7691 $
 */
public class AttributeSummarizerCustomizer extends JPanel implements
    BeanCustomizer, EnvironmentHandler, CustomizerClosingListener,
    CustomizerCloseRequester {
  

  /**
   * For serialization
   */
  private static final long serialVersionUID = -6973666187676272788L;

  private DataVisualizer m_dataVis;

  private Environment m_env = Environment.getSystemWide();
  
  private ModifyListener m_modifyListener;
  
  private Window m_parent;
  
  private String m_rendererNameBack;
  private String m_xAxisBack;
  private String m_widthBack;
  private String m_heightBack;
  private String m_optsBack;
  
  private JComboBox m_rendererCombo;
  private EnvironmentField m_xAxis;
  private EnvironmentField m_width;
  private EnvironmentField m_height;
  private EnvironmentField m_opts;
  
  
  /**
   * Constructor
   */
  public AttributeSummarizerCustomizer() {
    setLayout(new BorderLayout());
  }
  
  
  /**
   * Set the model performance chart object to customize
   * 
   * @param object the model performance chart to customize
   */
  public void setObject(Object object) {
    m_dataVis = (DataVisualizer)object;
    m_rendererNameBack = m_dataVis.getOffscreenRendererName();
    m_xAxisBack = m_dataVis.getOffscreenXAxis();
    m_widthBack = m_dataVis.getOffscreenWidth();
    m_heightBack = m_dataVis.getOffscreenHeight();
    m_optsBack = m_dataVis.getOffscreenAdditionalOpts();
    
    setup();
  }
  
  private void setup() {
    JPanel holder = new JPanel();
    holder.setLayout(new GridLayout(5, 2));
    
    Vector<String> comboItems = new Vector<String>();
    comboItems.add("Weka Chart Renderer");
    Set<String> pluginRenderers = 
      PluginManager.getPluginNamesOfType("weka.gui.beans.OffscreenChartRenderer");
    if (pluginRenderers != null) {
      for (String plugin : pluginRenderers) {
        comboItems.add(plugin);      
      }
    }
    
    JLabel rendererLab = new JLabel("Renderer", SwingConstants.RIGHT);
    holder.add(rendererLab);
    m_rendererCombo = new JComboBox(comboItems);
    holder.add(m_rendererCombo);    
    
    JLabel xLab = new JLabel("Attribute to chart", SwingConstants.RIGHT);
    xLab.setToolTipText("Attribute name or /first or /last or /<index>");
    m_xAxis = new EnvironmentField(m_env);
    m_xAxis.setText(m_xAxisBack);    
    
    JLabel widthLab = new JLabel("Chart width (pixels)", SwingConstants.RIGHT);
    m_width = new EnvironmentField(m_env);
    m_width.setText(m_widthBack);
    
    JLabel heightLab = new JLabel("Chart height (pixels)", SwingConstants.RIGHT);
    m_height = new EnvironmentField(m_env);
    m_height.setText(m_heightBack);
    
    final JLabel optsLab = new JLabel("Renderer options", SwingConstants.RIGHT);
    m_opts = new EnvironmentField(m_env);
    m_opts.setText(m_optsBack);
    holder.add(xLab); holder.add(m_xAxis);  
    holder.add(widthLab); holder.add(m_width);  
    holder.add(heightLab); holder.add(m_height); 
    holder.add(optsLab); holder.add(m_opts); 
    
    add(holder, BorderLayout.CENTER);
    
    String globalInfo = m_dataVis.globalInfo();
    globalInfo += " This dialog allows you to configure offscreen " +
    		"rendering options. Offscreen images are passed via" +
    		" 'image' connections.";

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
    add(jp, BorderLayout.NORTH);
    
    addButtons();
    
    m_rendererCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setupRendererOptsTipText(optsLab);
      }
    });
    m_rendererCombo.setSelectedItem(m_rendererNameBack);    
    
    setupRendererOptsTipText(optsLab);
  }
  
  private void setupRendererOptsTipText(JLabel optsLab) {
    String renderer = m_rendererCombo.getSelectedItem().toString();
    if (renderer.equalsIgnoreCase("weka chart renderer")) {
      // built-in renderer
      WekaOffscreenChartRenderer rcr = new WekaOffscreenChartRenderer();
      String tipText = rcr.optionsTipTextHTML();
      tipText = tipText.replace("<html>", "<html>Comma separated list of options:<br>");
      optsLab.setToolTipText(tipText);
    } else {
      try {
        Object rendererO = PluginManager.getPluginInstance("weka.gui.beans.OffscreenChartRenderer",
            renderer);

        if (rendererO != null) {
          String tipText = ((OffscreenChartRenderer)rendererO).optionsTipTextHTML();
          if (tipText != null && tipText.length() > 0) {
            optsLab.setToolTipText(tipText);
          }
        }
      } catch (Exception ex) {

      }
    }
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
        m_dataVis.setOffscreenXAxis(m_xAxis.getText());
        m_dataVis.setOffscreenWidth(m_width.getText());
        m_dataVis.setOffscreenHeight(m_height.getText());
        m_dataVis.setOffscreenAdditionalOpts(m_opts.getText());
        m_dataVis.setOffscreenRendererName(m_rendererCombo.
            getSelectedItem().toString());
        
        if (m_modifyListener != null) {
          m_modifyListener.
            setModifiedStatus(AttributeSummarizerCustomizer.this, true);
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
    m_dataVis.setOffscreenXAxis(m_xAxisBack);
    m_dataVis.setOffscreenWidth(m_widthBack);
    m_dataVis.setOffscreenHeight(m_heightBack);
    m_dataVis.setOffscreenAdditionalOpts(m_optsBack);
    m_dataVis.setOffscreenRendererName(m_rendererNameBack);
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
}
