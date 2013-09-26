/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
 * 
 * Project Info:  http://www.jfree.org/jcommon/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 * 
 * -------------------
 * PaintUtilities.java
 * -------------------
 * (C) Copyright 2003-2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: PaintUtilities.java,v 1.10 2007/11/02 17:50:37 taqua Exp $
 *
 * Changes
 * -------
 * 13-Nov-2003 : Version 1 (DG);
 * 04-Oct-2004 : Renamed PaintUtils --> PaintUtilities (DG);
 * 23-Feb-2005 : Rewrote equal() method with less indenting required (DG);
 *
 */

package org.jfree.util;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Utility code that relates to <code>Paint</code> objects.
 *
 * @author David Gilbert
 */
public class PaintUtilities {

    /**
     * Private constructor prevents object creation.
     */
    private PaintUtilities() {
    }

    /**
     * Returns <code>true</code> if the two <code>Paint</code> objects are equal 
     * OR both <code>null</code>.  This method handles
     * <code>GradientPaint</code> as a special case.
     *
     * @param p1  paint 1 (<code>null</code> permitted).
     * @param p2  paint 2 (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public static boolean equal(final Paint p1, final Paint p2) {

        // handle cases where either or both arguments are null
        if (p1 == null) {
            return (p2 == null);   
        }
        if (p2 == null) {
            return false;   
        }
        
        boolean result = false;
        // handle GradientPaint as a special case...
        if (p1 instanceof GradientPaint && p2 instanceof GradientPaint) {
            final GradientPaint gp1 = (GradientPaint) p1;
            final GradientPaint gp2 = (GradientPaint) p2;
            result = gp1.getColor1().equals(gp2.getColor1()) 
                && gp1.getColor2().equals(gp2.getColor2())
                && gp1.getPoint1().equals(gp2.getPoint1())    
                && gp1.getPoint2().equals(gp2.getPoint2())
                && gp1.isCyclic() == gp2.isCyclic()
                && gp1.getTransparency() == gp1.getTransparency(); 
        }
        else {
            result = p1.equals(p2);
        }
        return result;

    }

    /**
     * Converts a color into a string. If the color is equal to one of the
     * defined constant colors, that name is returned instead. Otherwise the
     * color is returned as hex-string.
     *
     * @param c the color.
     * @return the string for this color.
     */
    public static String colorToString (final Color c)
    {
      try {
          final Field[] fields = Color.class.getFields();
          for (int i = 0; i < fields.length; i++) {
              final Field f = fields[i];
              if (Modifier.isPublic(f.getModifiers())
                  && Modifier.isFinal(f.getModifiers())
                  && Modifier.isStatic(f.getModifiers())) {
                  final String name = f.getName();
                  final Object oColor = f.get(null);
                  if (oColor instanceof Color) {
                      if (c.equals(oColor)) {
                          return name;
                      }
                  }
              }
          }
      }
      catch (Exception e) {
          //
      }

      // no defined constant color, so this must be a user defined color
      final String color = Integer.toHexString(c.getRGB() & 0x00ffffff);
      final StringBuffer retval = new StringBuffer(7);
      retval.append("#");

      final int fillUp = 6 - color.length();
      for (int i = 0; i < fillUp; i++) {
          retval.append("0");
      }

      retval.append(color);
      return retval.toString();
    }

    /**
     * Converts a given string into a color.
     *
     * @param value the string, either a name or a hex-string.
     * @return the color.
     */
    public static Color stringToColor (final String value)
    {
      if (value == null) {
          return Color.black;
      }
      try {
          // get color by hex or octal value
          return Color.decode(value);
      }
      catch (NumberFormatException nfe) {
          // if we can't decode lets try to get it by name
          try {
              // try to get a color by name using reflection
              final Field f = Color.class.getField(value);

              return (Color) f.get(null);
          }
          catch (Exception ce) {
              Log.info("No such Color : " + value);
              // if we can't get any color return black
              return Color.black;
          }
      }
    }
}
