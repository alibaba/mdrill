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
 * ---------------------
 * ExtendedDrawable.java
 * ---------------------
 * (C) Copyright 2002-2005, by Object Refinery Limited.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   -;
 *
 * $Id: ExtendedDrawable.java,v 1.4 2005/10/18 13:18:34 mungady Exp $
 *
 * Changes
 * -------
 * 27-Apr-2005: Initial version.
 *
 */
package org.jfree.ui;

import java.awt.Dimension;

/**
 * A drawable that has a preferred size and aspect ratio. Implement this interface to gain
 * some control over the rendering and layouting process for the drawable.
 *
 * @author Thomas Morgner
 */
public interface ExtendedDrawable extends Drawable
{
  /**
   * Returns the preferred size of the drawable. If the drawable is aspect ratio aware,
   * these bounds should be used to compute the preferred aspect ratio for this drawable.
   *
   * @return the preferred size.
   */
  public Dimension getPreferredSize ();

  /**
   * Returns true, if this drawable will preserve an aspect ratio during the drawing.
   *
   * @return true, if an aspect ratio is preserved, false otherwise.
   */
  public boolean isPreserveAspectRatio ();
}
