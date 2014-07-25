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
 *    ShadowBorder.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import java.awt.*;
import javax.swing.border.*;

public class ShadowBorder extends AbstractBorder {

  /**
   * For serialization
   */
  private static final long serialVersionUID = -2117842133475125463L;

  /** The width in pixels of the drop shadow */
   private int m_width = 3;

   /** the color of the drop shadow */
   private Color m_color = Color.BLACK;

   /**
    * Constructor. Drop shadow with default width of 2 pixels and black color.
    */
   public ShadowBorder() {
      this(2);
   }

   /**
    * Constructor. Drop shadow, default shadow color is black.
    * @param width the width of the shadow.
    */
   public ShadowBorder(int width) {
      this( width, Color.BLACK );
   }

   /**
    * Constructor. Drop shadow, width and color are adjustable.
    * 
    * @param width the width of the shadow.
    * @param color the color of the shadow.
    */
   public ShadowBorder(int width, Color color) {
      m_width = width;
      m_color = color;
   }

   /**
    * Returns a new Insets instance where the top and left are 1, 
    * the bottom and right fields are the border width + 1.
    * 
    * @param c the component for which this border insets value applies
    * @return a new Insets object initialized as stated above.
    */
   public Insets getBorderInsets(Component c) {
      return new Insets(1, 1, m_width + 1, m_width + 1);
   }

   /**
    * Reinitializes the <code>insets</code> parameter with this ShadowBorder's 
    * current Insets.   
    * 
    * @param c the component for which this border insets value applies
    * @param insets the object to be reinitialized
    * @return the given <code>insets</code> object
    */
   public Insets getBorderInsets(Component c, Insets insets) {
      insets.top = 1;
      insets.left = 1;
      insets.bottom = m_width + 1;
      insets.right = m_width + 1;
      return insets;
   }

   /**
    * This implementation always returns true.
    * 
    * @return true
    */
   public boolean isBorderOpaque() {
      return true;
   }

   /**
    * Paints the drop shadow border around the given component.
    *    
    * @param c - the component for which this border is being painted
    * @param g - the paint graphics
    * @param x - the x position of the painted border
    * @param y - the y position of the painted border
    * @param width - the width of the painted border
    * @param height - the height of the painted border   
    */
   public void paintBorder( Component c, Graphics g, int x, int y, int width, int height ) {
      Color old_color = g.getColor();
      int x1, y1, x2, y2;
      g.setColor(m_color);

      // outline
      g.drawRect(x, y, width - m_width - 1, height - m_width - 1);

      // the drop shadow
      for (int i = 0; i <= m_width; i++) {
         // bottom shadow
         x1 = x + m_width;
         y1 = y + height - i;
         x2 = x + width;
         y2 = y1;
         g.drawLine( x1, y1, x2, y2 );

         // right shadow
         x1 = x + width - m_width + i;
         y1 = y + m_width;
         x2 = x1;
         y2 = y + height;
         g.drawLine( x1, y1, x2, y2 );
      }

      // fill in the corner rectangles with the background color of the parent
      // container
      if (c.getParent() != null) {
         g.setColor( c.getParent().getBackground() );
         for ( int i = 0; i <= m_width; i++ ) {
            x1 = x;
            y1 = y + height - i;
            x2 = x + m_width;
            y2 = y1;
            g.drawLine( x1, y1, x2, y2 );
            x1 = x + width - m_width;
            y1 = y + i;
            x2 = x + width ;
            y2 = y1;
            g.drawLine( x1, y1, x2, y2 );
         }
         // add some slightly darker colored triangles
         g.setColor(g.getColor().darker());
         for ( int i = 0; i < m_width; i++ ) {
            // bottom left triangle
            x1 = x + i + 1;
            y1 = y + height - m_width + i;
            x2 = x + m_width;
            y2 = y1;
            g.drawLine( x1, y1, x2, y2 );

            // top right triangle
            x1 = x + width - m_width;
            y1 = y + i + 1;
            x2 = x1 + i ;
            y2 = y1;
            g.drawLine( x1, y1, x2, y2 );
         }
      }

      g.setColor( old_color );
   }
}
