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
 * --------------
 * TextBlock.java
 * --------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: TextBlock.java,v 1.15 2007/11/02 17:50:35 taqua Exp $
 *
 * Changes
 * -------
 * 07-Nov-2003 : Version 1 (DG);
 * 22-Dec-2003 : Added workaround for Java bug 4245442 (DG);
 * 09-Jan-2004 : Added an extra draw() method for no rotation case (DG);
 * 25-Feb-2004 : Added getLines() method (DG);
 * 22-Mar-2004 : Added equals() method and implemented Serializable (DG);
 * 24-Mar-2004 : Added 'paint' argument to addLine() method (DG);
 * 01-Apr-2004 : Changed java.awt.geom.Dimension2D to org.jfree.ui.Size2D 
 *               because of JDK bug 4976448 which persists on JDK 1.3.1 (DG);
 * 04-Oct-2004 : Renamed ShapeUtils --> ShapeUtilities (DG);
 *
 */
 
package org.jfree.text;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.Size2D;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ShapeUtilities;

/**
 * A list of {@link TextLine} objects that form a block of text.
 * 
 * @see TextUtilities#createTextBlock(String, Font, Paint)
 *
 * @author David Gilbert
 */
public class TextBlock implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -4333175719424385526L;
    
    /** Storage for the lines of text. */
    private List lines;
    
    /** The alignment of the lines. */
    private HorizontalAlignment lineAlignment;

    /**
     * Creates a new empty text block.
     */
    public TextBlock() {
        this.lines = new java.util.ArrayList();
        this.lineAlignment = HorizontalAlignment.CENTER;
    }
    
    /**
     * Returns the alignment of the lines of text within the block.
     * 
     * @return The alignment (never <code>null</code>).
     */
    public HorizontalAlignment getLineAlignment() {
        return this.lineAlignment;   
    }
    
    /**
     * Sets the alignment of the lines of text within the block.
     * 
     * @param alignment  the alignment (<code>null</code> not permitted).
     */
    public void setLineAlignment(HorizontalAlignment alignment) {
        if (alignment == null) {
            throw new IllegalArgumentException("Null 'alignment' argument.");
        }
        this.lineAlignment = alignment;   
    }
    
    /**
     * Adds a line of text that will be displayed using the specified font.
     * 
     * @param text  the text.
     * @param font  the font.
     * @param paint  the paint.
     */
    public void addLine(final String text, final Font font, final Paint paint) {
        addLine(new TextLine(text, font, paint));
    }
    
    /**
     * Adds a {@link TextLine} to the block.
     * 
     * @param line  the line.
     */
    public void addLine(final TextLine line) {
        this.lines.add(line);    
    }
    
    /**
     * Returns the last line in the block.
     * 
     * @return The last line in the block.
     */
    public TextLine getLastLine() {
        TextLine last = null;
        final int index = this.lines.size() - 1;
        if (index >= 0) {
            last = (TextLine) this.lines.get(index);
        }
        return last;
    }
    
    /**
     * Returns an unmodifiable list containing the lines for the text block.
     *
     * @return A list of {@link TextLine} objects.
     */
    public List getLines() {
        return Collections.unmodifiableList(this.lines);
    }
    
    /**
     * Returns the width and height of the text block.
     * 
     * @param g2  the graphics device.
     * 
     * @return The width and height.
     */
    public Size2D calculateDimensions(final Graphics2D g2) {
        double width = 0.0;
        double height = 0.0;
        final Iterator iterator = this.lines.iterator();
        while (iterator.hasNext()) {
            final TextLine line = (TextLine) iterator.next();
            final Size2D dimension = line.calculateDimensions(g2);
            width = Math.max(width, dimension.getWidth());
            height = height + dimension.getHeight();
        }
        return new Size2D(width, height);
    }
    
    /**
     * Returns the bounds of the text block.
     * 
     * @param g2  the graphics device (<code>null</code> not permitted).
     * @param anchorX  the x-coordinate for the anchor point.
     * @param anchorY  the y-coordinate for the anchor point.
     * @param anchor  the text block anchor (<code>null</code> not permitted).
     * @param rotateX  the x-coordinate for the rotation point.
     * @param rotateY  the y-coordinate for the rotation point.
     * @param angle  the rotation angle.
     * 
     * @return The bounds.
     */
    public Shape calculateBounds(final Graphics2D g2,
                                 final float anchorX, final float anchorY, 
                                 final TextBlockAnchor anchor,
                                 final float rotateX, final float rotateY, 
                                 final double angle) {
        
        final Size2D d = calculateDimensions(g2);
        final float[] offsets = calculateOffsets(
            anchor, d.getWidth(), d.getHeight()
        );
        final Rectangle2D bounds = new Rectangle2D.Double(
            anchorX + offsets[0], anchorY + offsets[1], 
            d.getWidth(), d.getHeight()
        );
        final Shape rotatedBounds = ShapeUtilities.rotateShape(
            bounds, angle, rotateX, rotateY
        );
        return rotatedBounds;   
        
    }
    
    /**
     * Draws the text block at a specific location.
     * 
     * @param g2  the graphics device.
     * @param x  the x-coordinate for the anchor point.
     * @param y  the y-coordinate for the anchor point.
     * @param anchor  the anchor point.
     */
    public void draw(final Graphics2D g2, final float x, final float y, 
                     final TextBlockAnchor anchor) {
        draw(g2, x, y, anchor, 0.0f, 0.0f, 0.0);
    }
    
    /**
     * Draws the text block, aligning it with the specified anchor point and 
     * rotating it about the specified rotation point.
     * 
     * @param g2  the graphics device.
     * @param anchorX  the x-coordinate for the anchor point.
     * @param anchorY  the y-coordinate for the anchor point.
     * @param anchor  the point on the text block that is aligned to the 
     *                anchor point.
     * @param rotateX  the x-coordinate for the rotation point.
     * @param rotateY  the x-coordinate for the rotation point.
     * @param angle  the rotation (in radians).
     */
    public void draw(final Graphics2D g2,
                     final float anchorX, final float anchorY, 
                     final TextBlockAnchor anchor,
                     final float rotateX, final float rotateY, 
                     final double angle) {
    
        final Size2D d = calculateDimensions(g2);
        final float[] offsets = calculateOffsets(anchor, d.getWidth(), 
                d.getHeight());
        final Iterator iterator = this.lines.iterator();
        float yCursor = 0.0f;
        while (iterator.hasNext()) {
            TextLine line = (TextLine) iterator.next();
            Size2D dimension = line.calculateDimensions(g2);
            float lineOffset = 0.0f;
            if (this.lineAlignment == HorizontalAlignment.CENTER) {
                lineOffset = (float) (d.getWidth() - dimension.getWidth()) 
                    / 2.0f;   
            }
            else if (this.lineAlignment == HorizontalAlignment.RIGHT) {
                lineOffset = (float) (d.getWidth() - dimension.getWidth());   
            }
            line.draw(
                g2, anchorX + offsets[0] + lineOffset, anchorY + offsets[1] + yCursor,
                TextAnchor.TOP_LEFT, rotateX, rotateY, angle
            );
            yCursor = yCursor + (float) dimension.getHeight();
        }
        
    }
 
    /**
     * Calculates the x and y offsets required to align the text block with the
     * specified anchor point.  This assumes that the top left of the text 
     * block is at (0.0, 0.0).
     * 
     * @param anchor  the anchor position.
     * @param width  the width of the text block.
     * @param height  the height of the text block.
     * 
     * @return The offsets (float[0] = x offset, float[1] = y offset).
     */
    private float[] calculateOffsets(final TextBlockAnchor anchor, 
                                     final double width, final double height) {
        final float[] result = new float[2];
        float xAdj = 0.0f;
        float yAdj = 0.0f;

        if (anchor == TextBlockAnchor.TOP_CENTER
                || anchor == TextBlockAnchor.CENTER
                || anchor == TextBlockAnchor.BOTTOM_CENTER) {
                    
            xAdj = (float) -width / 2.0f;
            
        }
        else if (anchor == TextBlockAnchor.TOP_RIGHT
                || anchor == TextBlockAnchor.CENTER_RIGHT
                || anchor == TextBlockAnchor.BOTTOM_RIGHT) {
                    
            xAdj = (float) -width;
            
        }

        if (anchor == TextBlockAnchor.TOP_LEFT
                || anchor == TextBlockAnchor.TOP_CENTER
                || anchor == TextBlockAnchor.TOP_RIGHT) {
                    
            yAdj = 0.0f;
            
        }
        else if (anchor == TextBlockAnchor.CENTER_LEFT
                || anchor == TextBlockAnchor.CENTER
                || anchor == TextBlockAnchor.CENTER_RIGHT) {
                    
            yAdj = (float) -height / 2.0f;
            
        }
        else if (anchor == TextBlockAnchor.BOTTOM_LEFT
                || anchor == TextBlockAnchor.BOTTOM_CENTER
                || anchor == TextBlockAnchor.BOTTOM_RIGHT) {
                    
            yAdj = (float) -height;
            
        }
        result[0] = xAdj;
        result[1] = yAdj;
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
        if (obj == this) {
            return true;   
        }
        if (obj instanceof TextBlock) {
            final TextBlock block = (TextBlock) obj;
            return this.lines.equals(block.lines);
        }
        return false;
    }

    /**
     * Returns a hash code for this object.
     * 
     * @return A hash code.
     */
    public int hashCode() {
        return (this.lines != null ? this.lines.hashCode() : 0);
    }
}
