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
 * -------------
 * TextLine.java
 * -------------
 * (C) Copyright 2003-2005, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: TextLine.java,v 1.13 2007/11/02 17:50:35 taqua Exp $
 *
 * Changes
 * -------
 * 07-Nov-2003 : Version 1 (DG);
 * 22-Dec-2003 : Added workaround for Java bug 4245442 (DG);
 * 29-Jan-2004 : Added new constructor (DG);
 * 22-Mar-2004 : Added equals() method and implemented Serializable (DG);
 * 01-Apr-2004 : Changed java.awt.geom.Dimension2D to org.jfree.ui.Size2D 
 *               because of JDK bug 4976448 which persists on JDK 1.3.1 (DG);
 * 03-Sep-2004 : Added a method to remove a fragment (DG);
 * 08-Jul-2005 : Fixed bug in calculateBaselineOffset() (DG);
 *
 */

package org.jfree.text;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.jfree.ui.Size2D;
import org.jfree.ui.TextAnchor;

/**
 * A sequence of {@link TextFragment} objects that together form a line of 
 * text.  A sequence of text lines is managed by the {@link TextBlock} class.
 *
 * @author David Gilbert
 */
public class TextLine implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 7100085690160465444L;
    
    /** Storage for the text fragments that make up the line. */
    private List fragments;

    /**
     * Creates a new empty line.
     */
    public TextLine() {
        this.fragments = new java.util.ArrayList();
    }
    
    /**
     * Creates a new text line using the default font.
     * 
     * @param text  the text (<code>null</code> not permitted).
     */
    public TextLine(final String text) {
        this(text, TextFragment.DEFAULT_FONT);   
    }
    
    /**
     * Creates a new text line.
     * 
     * @param text  the text (<code>null</code> not permitted).
     * @param font  the text font (<code>null</code> not permitted).
     */
    public TextLine(final String text, final Font font) {
        this.fragments = new java.util.ArrayList();
        final TextFragment fragment = new TextFragment(text, font);
        this.fragments.add(fragment);
    }
    
    /**
     * Creates a new text line.
     * 
     * @param text  the text (<code>null</code> not permitted).
     * @param font  the text font (<code>null</code> not permitted).
     * @param paint  the text color (<code>null</code> not permitted).
     */
    public TextLine(final String text, final Font font, final Paint paint) {
        if (text == null) {
            throw new IllegalArgumentException("Null 'text' argument.");   
        }
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");   
        }
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");   
        }
        this.fragments = new java.util.ArrayList();
        final TextFragment fragment = new TextFragment(text, font, paint);
        this.fragments.add(fragment);
    }
    
    /**
     * Adds a text fragment to the text line.
     * 
     * @param fragment  the text fragment (<code>null</code> not permitted).
     */
    public void addFragment(final TextFragment fragment) {
        this.fragments.add(fragment);        
    }
    
    /**
     * Removes a fragment from the line.
     * 
     * @param fragment  the fragment to remove.
     */
    public void removeFragment(final TextFragment fragment) {
        this.fragments.remove(fragment);
    }
    
    /**
     * Draws the text line.
     * 
     * @param g2  the graphics device.
     * @param anchorX  the x-coordinate for the anchor point.
     * @param anchorY  the y-coordinate for the anchor point.
     * @param anchor  the point on the text line that is aligned to the anchor 
     *                point.
     * @param rotateX  the x-coordinate for the rotation point.
     * @param rotateY  the y-coordinate for the rotation point.
     * @param angle  the rotation angle (in radians).
     */
    public void draw(final Graphics2D g2,
                     final float anchorX, final float anchorY, 
                     final TextAnchor anchor,
                     final float rotateX, final float rotateY, 
                     final double angle) {
    
        float x = anchorX;
        final float yOffset = calculateBaselineOffset(g2, anchor);
        final Iterator iterator = this.fragments.iterator();
        while (iterator.hasNext()) {
            final TextFragment fragment = (TextFragment) iterator.next();
            final Size2D d = fragment.calculateDimensions(g2);
            fragment.draw(
                g2, x, anchorY + yOffset, TextAnchor.BASELINE_LEFT, 
                rotateX, rotateY, angle
            );
            x = x + (float) d.getWidth();
        }
    
    }
    
    /**
     * Calculates the width and height of the text line.
     * 
     * @param g2  the graphics device.
     * 
     * @return The width and height.
     */
    public Size2D calculateDimensions(final Graphics2D g2) {
        double width = 0.0;
        double height = 0.0;
        final Iterator iterator = this.fragments.iterator();
        while (iterator.hasNext()) {
            final TextFragment fragment = (TextFragment) iterator.next();
            final Size2D dimension = fragment.calculateDimensions(g2);
            width = width + dimension.getWidth();
            height = Math.max(height, dimension.getHeight());
        }
        return new Size2D(width, height);
    }
    
    /**
     * Returns the first text fragment in the line.
     * 
     * @return The first text fragment in the line.
     */
    public TextFragment getFirstTextFragment() {
        TextFragment result = null;
        if (this.fragments.size() > 0) {
            result = (TextFragment) this.fragments.get(0);
        }    
        return result;
    }
    
    /**
     * Returns the last text fragment in the line.
     * 
     * @return The last text fragment in the line.
     */
    public TextFragment getLastTextFragment() {
        TextFragment result = null;
        if (this.fragments.size() > 0) {
            result = (TextFragment) this.fragments.get(this.fragments.size() 
                    - 1);
        }    
        return result;
    }
    
    /**
     * Calculate the offsets required to translate from the specified anchor 
     * position to the left baseline position.
     * 
     * @param g2  the graphics device.
     * @param anchor  the anchor position.
     * 
     * @return The offsets.
     */
    private float calculateBaselineOffset(final Graphics2D g2, 
                                          final TextAnchor anchor) {
        float result = 0.0f;
        Iterator iterator = this.fragments.iterator();
        while (iterator.hasNext()) {
            TextFragment fragment = (TextFragment) iterator.next();
            result = Math.max(result, 
                    fragment.calculateBaselineOffset(g2, anchor));
        }
        return result;
    }
    
    /**
     * Tests this object for equality with an arbitrary object.
     * 
     * @param obj  the object to test against (<code>null</code> permitted).
     * 
     * @return A boolean.
     */
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;   
        }
        if (obj instanceof TextLine) {
            final TextLine line = (TextLine) obj;
            return this.fragments.equals(line.fragments);
        }
        return false;
    }

    /**
     * Returns a hash code for this object.
     * 
     * @return A hash code.
     */
    public int hashCode() {
        return (this.fragments != null ? this.fragments.hashCode() : 0);
    }

}
