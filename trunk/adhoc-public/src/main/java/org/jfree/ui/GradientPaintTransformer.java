/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2007, by Object Refinery Limited and Contributors.
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
 * -----------------------------
 * GradientPaintTransformer.java
 * -----------------------------
 * (C) Copyright 2003-2007, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: GradientPaintTransformer.java,v 1.4 2007/04/03 12:34:13 mungady Exp $
 *
 * Changes
 * -------
 * 28-Oct-2003 : Version 1 (DG);
 * 
 */
 
package org.jfree.ui;

import java.awt.GradientPaint;
import java.awt.Shape;

/**
 * The interface for a class that can transform a <code>GradientPaint</code> to
 * fit an arbitrary shape.
 * 
 * @author David Gilbert
 */
public interface GradientPaintTransformer {
    
    /**
     * Transforms a <code>GradientPaint</code> instance to fit some target 
     * shape.  Classes that implement this method typically return a new
     * instance of <code>GradientPaint</code>.
     * 
     * @param paint  the original paint (not <code>null</code>).
     * @param target  the reference area (not <code>null</code>).
     * 
     * @return A transformed paint.
     */
    public GradientPaint transform(GradientPaint paint, Shape target);

}
