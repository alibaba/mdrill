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
 *    ImageEvent.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.awt.image.BufferedImage;
import java.util.EventObject;

/**
 * Event that encapsulates an Image
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 7615 $
 */
public class ImageEvent extends EventObject {
  
  /** For serialization */
  private static final long serialVersionUID = -8126533743311557969L;
  
  /** The image */
  protected BufferedImage m_image;
  
  /**
   * Construct a new ImageEvent
   * 
   * @param source the source of this event
   * @param image the image to encapsulate
   */
  public ImageEvent(Object source, BufferedImage image) {
    super(source);
    
    m_image = image;
  }
  
  /**
   * Get the encapsulated image
   * 
   * @return the encapsulated image
   */
  public BufferedImage getImage() {
    return m_image;
  }
}
