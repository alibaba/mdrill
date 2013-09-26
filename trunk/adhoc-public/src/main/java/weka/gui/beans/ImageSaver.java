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
 *    ImageSaver.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.beans.EventSetDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.gui.Logger;

/**
 * Component that can accept ImageEvents and save their encapsulated
 * images to a file.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 7615 $
 */
public class ImageSaver extends JPanel implements ImageListener, BeanCommon,
    Visible, Serializable, EnvironmentHandler {
  
  /**
   * For serialization
   */
  private static final long serialVersionUID = -641438159956934314L;

  /**
   * Default visual for data sources
   */
  protected BeanVisual m_visual = 
    new BeanVisual("AbstractDataSink", 
                   BeanVisual.ICON_PATH+"SerializedModelSaver.gif",
                   BeanVisual.ICON_PATH+"SerializedModelSaver_animated.gif");
  
  /**
   * Non null if this object is a target for any events.
   * Provides for the simplest case when only one incomming connection
   * is allowed.
   */
  protected Object m_listenee = null;
  
  /**
   * The log for this bean
   */
  protected transient weka.gui.Logger m_logger = null;
  
  /**
   * The environment variables.
   */
  protected transient Environment m_env;
  
  /** The file to save to */
  protected String m_fileName;
  
  /**
   * Global info for this bean
   *
   * @return a <code>String</code> value
   */
  public String globalInfo() {
    return "Save static images (such as those produced by " +
    		"ModelPerformanceChart) to a file.";
  }
  
  /**
   * Constructs a new ImageSaver
   */
  public ImageSaver() {
    useDefaultVisual();
    setLayout(new BorderLayout());
    add(m_visual, BorderLayout.CENTER);
    
    m_env = Environment.getSystemWide();
  }

  /**
   * Set the filename to save to
   * 
   * @param filename the filename to save to
   */
  public void setFilename(String filename) {
    m_fileName = filename;
  }
  
  /**
   * Get the filename to save to
   * 
   * @return the filename to save to
   */
  public String getFilename() {
    return m_fileName;
  }
  
  /**
   * Set environment variables to use
   * 
   * @param env the environment variables to use 
   */
  public void setEnvironment(Environment env) {
    m_env = env;
  }

  @Override
  public void useDefaultVisual() {
    m_visual.loadIcons(BeanVisual.ICON_PATH+"SerializedModelSaver.gif",
        BeanVisual.ICON_PATH+"SerializedModelSaver_animated.gif");
    m_visual.setText("ImageSaver");
  }

  @Override
  public void setVisual(BeanVisual newVisual) {
    m_visual = newVisual;
  }

  @Override
  public BeanVisual getVisual() {
    return m_visual;
  }

  @Override
  public void setCustomName(String name) {
    m_visual.setText(name);
  }

  @Override
  public String getCustomName() {
    return m_visual.getText();
  }

  @Override
  public void stop() {
  }

  @Override
  public boolean isBusy() {
    return false;
  }

  @Override
  public void setLog(Logger logger) {
    m_logger = logger;
  }

  @Override
  public boolean connectionAllowed(EventSetDescriptor esd) {
    return connectionAllowed(esd.getName());
  }

  @Override
  public boolean connectionAllowed(String eventName) {
    return (m_listenee == null);
  }

  @Override
  public void connectionNotification(String eventName, Object source) {
    if (connectionAllowed(eventName)) {
      m_listenee = source;
    }
  }

  @Override
  public void disconnectionNotification(String eventName, Object source) {
    if (m_listenee == source) {
      m_listenee = null;
    }
  }

  /**
   * Accept and process an ImageEvent
   * 
   * @param imageE the ImageEvent to process
   */
  public void acceptImage(ImageEvent imageE) {
    BufferedImage image = imageE.getImage();
    
    if (m_fileName != null && m_fileName.length() > 0) {
      if (m_env == null) {
        m_env = Environment.getSystemWide();
      }
      String filename = m_fileName;
      try {
        filename = m_env.substitute(m_fileName);
      } catch (Exception ex) {        
      }
      
      // append .png if necessary
      if (filename.toLowerCase().indexOf(".png") < 0) {
        filename += ".png";
      }
      
      File file = new File(filename);
      if (!file.isDirectory()) {
        try {
          ImageIO.write(image, "png", file);
        } catch (IOException e) {
          if (m_logger != null) {
            m_logger.statusMessage(statusMessagePrefix() + "WARNING: "
                + "an error occurred whilte trying to write image (see log)");
            m_logger.logMessage("[" + getCustomName() + "] " 
                + "an error occurred whilte trying to write image: " + e.getMessage());
          } else {
            e.printStackTrace();
          }
        }
      } else {
        String message = "Can't write image to file because supplied filename" +
        		" is a directory!";
        if (m_logger != null) {
          m_logger.statusMessage(statusMessagePrefix() + "WARNING: " + message);
          m_logger.logMessage("[" + getCustomName() + "] " + message);
        }
      }
    } else {
      String message = "Can't write image bacause no filename has been supplied!" +
      " is a directory!";
      if (m_logger != null) {
        m_logger.statusMessage(statusMessagePrefix() + "WARNING: " + message);
        m_logger.logMessage("[" + getCustomName() + "] " + message);
      }
    }
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|";
  }
}
