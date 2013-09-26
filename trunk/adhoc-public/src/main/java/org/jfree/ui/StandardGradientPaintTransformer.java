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
 * -------------------------------------
 * StandardGradientPaintTransformer.java
 * -------------------------------------
 * (C) Copyright 2003-2007, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: StandardGradientPaintTransformer.java,v 1.11 2007/04/03 13:55:13 mungady Exp $
 *
 * Changes
 * -------
 * 28-Oct-2003 : Version 1 (DG);
 * 19-Mar-2004 : Added equals() method (DG);
 * 
 */

package org.jfree.ui;

import java.awt.GradientPaint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.util.PublicCloneable;

/**
 * Transforms a <code>GradientPaint</code> to range over the width of a target 
 * shape.  Instances of this class are immutable.
 * 
 * @author David Gilbert
 */
public class StandardGradientPaintTransformer 
    implements GradientPaintTransformer, Cloneable, PublicCloneable, 
               Serializable {
    
    /** For serialization. */
    private static final long serialVersionUID = -8155025776964678320L;

    /** The transform type. */
    private GradientPaintTransformType type;
    
    /**
     * Creates a new transformer with the type 
     * {@link GradientPaintTransformType#VERTICAL}.
     */
    public StandardGradientPaintTransformer() {
        this(GradientPaintTransformType.VERTICAL);
    }
    
    /**
     * Creates a new transformer with the specified type.
     * 
     * @param type  the transform type (<code>null</code> not permitted).
     */
    public StandardGradientPaintTransformer(
            final GradientPaintTransformType type) {
        if (type == null) {
            throw new IllegalArgumentException("Null 'type' argument.");
        }
        this.type = type;
    }
    
    /**
     * Returns the type of transform.
     * 
     * @return The type of transform (never <code>null</code>).
     * 
     * @since 1.0.10
     */
    public GradientPaintTransformType getType() {
        return this.type;
    }
    
    /**
     * Transforms a <code>GradientPaint</code> instance to fit the specified
     * <code>target</code> shape.
     * 
     * @param paint  the original paint (<code>null</code> not permitted).
     * @param target  the target shape (<code>null</code> not permitted).
     * 
     * @return The transformed paint.
     */
    public GradientPaint transform(final GradientPaint paint, 
                                   final Shape target) {
        
        GradientPaint result = paint;
        final Rectangle2D bounds = target.getBounds2D();
        
        if (this.type.equals(GradientPaintTransformType.VERTICAL)) {
            result = new GradientPaint((float) bounds.getCenterX(), 
                    (float) bounds.getMinY(), paint.getColor1(), 
                    (float) bounds.getCenterX(), (float) bounds.getMaxY(), 
                    paint.getColor2());
        }
        else if (this.type.equals(GradientPaintTransformType.HORIZONTAL)) {
            result = new GradientPaint((float) bounds.getMinX(), 
                    (float) bounds.getCenterY(), paint.getColor1(), 
                    (float) bounds.getMaxX(), (float) bounds.getCenterY(), 
                    paint.getColor2());            
        }
        else if (this.type.equals(
                GradientPaintTransformType.CENTER_HORIZONTAL)) {
            result = new GradientPaint((float) bounds.getCenterX(), 
                    (float) bounds.getCenterY(), paint.getColor2(), 
                    (float) bounds.getMaxX(), (float) bounds.getCenterY(), 
                    paint.getColor1(), true);            
        }
        else if (this.type.equals(GradientPaintTransformType.CENTER_VERTICAL)) {
            result = new GradientPaint((float) bounds.getCenterX(), 
                    (float) bounds.getMinY(), paint.getColor1(), 
                    (float) bounds.getCenterX(), (float) bounds.getCenterY(), 
                    paint.getColor2(), true);            
        }
        
        return result;
    }
    
    /**
     * Tests this instance for equality with an arbitrary object.
     * 
     * @param obj  the object (<code>null</code> permitted).
     * 
     * @return A boolean.
     */
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;   
        }
        if (!(obj instanceof StandardGradientPaintTransformer)) {
            return false;
        }
        StandardGradientPaintTransformer that 
                = (StandardGradientPaintTransformer) obj;
        if (this.type != that.type) {
            return false;
        }
        return true;
    }
    
    /**
     * Returns a clone of the transformer.  Note that instances of this class
     * are immutable, so cloning an instance isn't really necessary.
     * 
     * @return A clone.
     * 
     * @throws CloneNotSupportedException not thrown by this class, but 
     *         subclasses (if any) might.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Returns a hash code for this object.
     * 
     * @return A hash code.
     */
    public int hashCode() {
        return (this.type != null ? this.type.hashCode() : 0);
    }
    
}
