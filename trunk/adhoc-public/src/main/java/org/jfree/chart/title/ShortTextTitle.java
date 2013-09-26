/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
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
 * ShortTextTitle.java
 * -------------------
 * (C) Copyright 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 28-Apr-2008 : Version 1 (DG);
 *
 */

package org.jfree.chart.title;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.block.LengthConstraintType;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.data.Range;
import org.jfree.text.TextUtilities;
import org.jfree.ui.Size2D;
import org.jfree.ui.TextAnchor;

/**
 * A text title that is only displayed if the entire text will be visible
 * without line wrapping.  It is only intended for use with short titles - for
 * general purpose titles, you should use the {@link TextTitle} class.
 *
 * @since 1.0.10
 *
 * @see TextTitle
 */
public class ShortTextTitle extends TextTitle {

    /**
     * Creates a new title.
     *
     * @param text  the text (<code>null</code> not permitted).
     */
    public ShortTextTitle(String text) {
        setText(text);
    }

    /**
     * Performs a layout for this title, subject to the supplied constraint,
     * and returns the dimensions required for the title (if the title
     * cannot be displayed in the available space, this method will return
     * zero width and height for the dimensions).
     *
     * @param g2  the graphics target.
     * @param constraint  the layout constraints.
     *
     * @return The dimensions for the title.
     */
    public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
        RectangleConstraint cc = toContentConstraint(constraint);
        LengthConstraintType w = cc.getWidthConstraintType();
        LengthConstraintType h = cc.getHeightConstraintType();
        Size2D contentSize = null;
        if (w == LengthConstraintType.NONE) {
            if (h == LengthConstraintType.NONE) {
                contentSize = arrangeNN(g2);
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        else if (w == LengthConstraintType.RANGE) {
            if (h == LengthConstraintType.NONE) {
                contentSize = arrangeRN(g2, cc.getWidthRange());
            }
            else if (h == LengthConstraintType.RANGE) {
                contentSize = arrangeRR(g2, cc.getWidthRange(),
                        cc.getHeightRange());
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        else if (w == LengthConstraintType.FIXED) {
            if (h == LengthConstraintType.NONE) {
                contentSize = arrangeFN(g2, cc.getWidth());
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        if (contentSize.width <= 0.0 || contentSize.height <= 0.0) {
            return new Size2D(0.0, 0.0);
        }
        else {
            return new Size2D(calculateTotalWidth(contentSize.getWidth()),
                    calculateTotalHeight(contentSize.getHeight()));
        }
    }

    /**
     * Arranges the content for this title assuming no bounds on the width
     * or the height, and returns the required size.
     *
     * @param g2  the graphics target.
     *
     * @return The content size.
     */
    protected Size2D arrangeNN(Graphics2D g2) {
        Range max = new Range(0.0, Float.MAX_VALUE);
        return arrangeRR(g2, max, max);
    }

    /**
     * Arranges the content for this title assuming a range constraint for the
     * width and no bounds on the height, and returns the required size.
     *
     * @param g2  the graphics target.
     * @param widthRange  the range for the width.
     *
     * @return The content size.
     */
    protected Size2D arrangeRN(Graphics2D g2, Range widthRange) {
        Size2D s = arrangeNN(g2);
        if (widthRange.contains(s.getWidth())) {
            return s;
        }
        double ww = widthRange.constrain(s.getWidth());
        return arrangeFN(g2, ww);
    }

    /**
     * Arranges the content for this title assuming a fixed width and no bounds
     * on the height, and returns the required size.  This will reflect the
     * fact that a text title positioned on the left or right of a chart will
     * be rotated by 90 degrees.
     *
     * @param g2  the graphics target.
     * @param w  the width.
     *
     * @return The content size.
     */
    protected Size2D arrangeFN(Graphics2D g2, double w) {
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics(getFont());
        Rectangle2D bounds = TextUtilities.getTextBounds(getText(), g2, fm);
        if (bounds.getWidth() <= w) {
            return new Size2D(w, bounds.getHeight());
        }
        else {
            return new Size2D(0.0, 0.0);
        }
    }

    /**
     * Returns the content size for the title.
     *
     * @param g2  the graphics device.
     * @param widthRange  the width range.
     * @param heightRange  the height range.
     *
     * @return The content size.
     */
    protected Size2D arrangeRR(Graphics2D g2, Range widthRange,
            Range heightRange) {

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics(getFont());
        Rectangle2D bounds = TextUtilities.getTextBounds(getText(), g2, fm);
        if (bounds.getWidth() <= widthRange.getUpperBound()
                && bounds.getHeight() <= heightRange.getUpperBound()) {
            return new Size2D(bounds.getWidth(), bounds.getHeight());
        }
        else {
            return new Size2D(0.0, 0.0);
        }
    }

    /**
     * Draws the title using the current font and paint.
     *
     * @param g2  the graphics target.
     * @param area  the title area.
     * @param params  optional parameters (ignored here).
     *
     * @return <code>null</code>.
     */
    public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
        if (area.isEmpty()) {
            return null;
        }
        area = trimMargin(area);
        drawBorder(g2, area);
        area = trimBorder(area);
        area = trimPadding(area);
        g2.setFont(getFont());
        g2.setPaint(getPaint());
        TextUtilities.drawAlignedString(getText(), g2, (float) area.getMinX(),
                (float) area.getMinY(), TextAnchor.TOP_LEFT);

        return null;
    }

}
