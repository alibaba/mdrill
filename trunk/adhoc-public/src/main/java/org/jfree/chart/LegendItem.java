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
 * ---------------
 * LegendItem.java
 * ---------------
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Andrzej Porebski;
 *                   David Li;
 *                   Wolfgang Irler;
 *                   Luke Quinane;
 *
 * Changes (from 2-Oct-2002)
 * -------------------------
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 17-Jan-2003 : Dropped outlineStroke attribute (DG);
 * 08-Oct-2003 : Applied patch for displaying series line style, contributed by
 *               Luke Quinane (DG);
 * 21-Jan-2004 : Added the shapeFilled flag (DG);
 * 04-Jun-2004 : Added equals() method, implemented Serializable (DG);
 * 25-Nov-2004 : Changes required by new LegendTitle implementation (DG);
 * 11-Jan-2005 : Removed deprecated code in preparation for the 1.0.0
 *               release (DG);
 * 20-Apr-2005 : Added tooltip and URL text (DG);
 * 28-Nov-2005 : Separated constructors for AttributedString labels (DG);
 * 10-Dec-2005 : Fixed serialization bug (1377239) (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 20-Jul-2006 : Added dataset and series index fields (DG);
 * 13-Dec-2006 : Added fillPaintTransformer attribute (DG);
 * 18-May-2007 : Added dataset and seriesKey fields (DG);
 * 03-Aug-2007 : Fixed null pointer exception (DG);
 * 23-Apr-2008 : Added new constructor and implemented Cloneable (DG);
 * 17-Jun-2008 : Added optional labelFont and labelPaint attributes (DG);
 * 15-Oct-2008 : Added new constructor (DG);
 *
 */

package org.jfree.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.AttributedString;
import java.text.CharacterIterator;

import org.jfree.data.general.Dataset;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.util.AttributedStringUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;

/**
 * A temporary storage object for recording the properties of a legend item,
 * without any consideration for layout issues.
 */
public class LegendItem implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -797214582948827144L;

    /**
     * The dataset.
     *
     * @since 1.0.6
     */
    private Dataset dataset;

    /**
     * The series key.
     *
     * @since 1.0.6
     */
    private Comparable seriesKey;

    /** The dataset index. */
    private int datasetIndex;

    /** The series index. */
    private int series;

    /** The label. */
    private String label;

    /**
     * The label font (<code>null</code> is permitted).
     *
     * @since 1.0.11
     */
    private Font labelFont;

    /**
     * The label paint (<code>null</code> is permitted).
     *
     * @since 1.0.11
     */
    private transient Paint labelPaint;

    /** The attributed label (if null, fall back to the regular label). */
    private transient AttributedString attributedLabel;

    /**
     * The description (not currently used - could be displayed as a tool tip).
     */
    private String description;

    /** The tool tip text. */
    private String toolTipText;

    /** The url text. */
    private String urlText;

    /** A flag that controls whether or not the shape is visible. */
    private boolean shapeVisible;

    /** The shape. */
    private transient Shape shape;

    /** A flag that controls whether or not the shape is filled. */
    private boolean shapeFilled;

    /** The paint. */
    private transient Paint fillPaint;

    /**
     * A gradient paint transformer.
     *
     * @since 1.0.4
     */
    private GradientPaintTransformer fillPaintTransformer;

    /** A flag that controls whether or not the shape outline is visible. */
    private boolean shapeOutlineVisible;

    /** The outline paint. */
    private transient Paint outlinePaint;

    /** The outline stroke. */
    private transient Stroke outlineStroke;

    /** A flag that controls whether or not the line is visible. */
    private boolean lineVisible;

    /** The line. */
    private transient Shape line;

    /** The stroke. */
    private transient Stroke lineStroke;

    /** The line paint. */
    private transient Paint linePaint;

    /**
     * The shape must be non-null for a LegendItem - if no shape is required,
     * use this.
     */
    private static final Shape UNUSED_SHAPE = new Line2D.Float();

    /**
     * The stroke must be non-null for a LegendItem - if no stroke is required,
     * use this.
     */
    private static final Stroke UNUSED_STROKE = new BasicStroke(0.0f);

    /**
     * Creates a legend item with the specified label.  The remaining
     * attributes take default values.
     *
     * @param label  the label (<code>null</code> not permitted).
     *
     * @since 1.0.10
     */
    public LegendItem(String label) {
        this(label, Color.black);
    }

    /**
     * Creates a legend item with the specified label and fill paint.  The
     * remaining attributes take default values.
     *
     * @param label  the label (<code>null</code> not permitted).
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @since 1.0.12
     */
    public LegendItem(String label, Paint paint) {
        this(label, null, null, null, new Rectangle2D.Double(-4.0, -4.0, 8.0,
                8.0), paint);
    }

    /**
     * Creates a legend item with a filled shape.  The shape is not outlined,
     * and no line is visible.
     *
     * @param label  the label (<code>null</code> not permitted).
     * @param description  the description (<code>null</code> permitted).
     * @param toolTipText  the tool tip text (<code>null</code> permitted).
     * @param urlText  the URL text (<code>null</code> permitted).
     * @param shape  the shape (<code>null</code> not permitted).
     * @param fillPaint  the paint used to fill the shape (<code>null</code>
     *                   not permitted).
     */
    public LegendItem(String label, String description,
                      String toolTipText, String urlText,
                      Shape shape, Paint fillPaint) {

        this(label, description, toolTipText, urlText,
                /* shape visible = */ true, shape,
                /* shape filled = */ true, fillPaint,
                /* shape outlined */ false, Color.black, UNUSED_STROKE,
                /* line visible */ false, UNUSED_SHAPE, UNUSED_STROKE,
                Color.black);

    }

    /**
     * Creates a legend item with a filled and outlined shape.
     *
     * @param label  the label (<code>null</code> not permitted).
     * @param description  the description (<code>null</code> permitted).
     * @param toolTipText  the tool tip text (<code>null</code> permitted).
     * @param urlText  the URL text (<code>null</code> permitted).
     * @param shape  the shape (<code>null</code> not permitted).
     * @param fillPaint  the paint used to fill the shape (<code>null</code>
     *                   not permitted).
     * @param outlineStroke  the outline stroke (<code>null</code> not
     *                       permitted).
     * @param outlinePaint  the outline paint (<code>null</code> not
     *                      permitted).
     */
    public LegendItem(String label, String description,
                      String toolTipText, String urlText,
                      Shape shape, Paint fillPaint,
                      Stroke outlineStroke, Paint outlinePaint) {

        this(label, description, toolTipText, urlText,
                /* shape visible = */ true, shape,
                /* shape filled = */ true, fillPaint,
                /* shape outlined = */ true, outlinePaint, outlineStroke,
                /* line visible */ false, UNUSED_SHAPE, UNUSED_STROKE,
                Color.black);

    }

    /**
     * Creates a legend item using a line.
     *
     * @param label  the label (<code>null</code> not permitted).
     * @param description  the description (<code>null</code> permitted).
     * @param toolTipText  the tool tip text (<code>null</code> permitted).
     * @param urlText  the URL text (<code>null</code> permitted).
     * @param line  the line (<code>null</code> not permitted).
     * @param lineStroke  the line stroke (<code>null</code> not permitted).
     * @param linePaint  the line paint (<code>null</code> not permitted).
     */
    public LegendItem(String label, String description,
                      String toolTipText, String urlText,
                      Shape line, Stroke lineStroke, Paint linePaint) {

        this(label, description, toolTipText, urlText,
                /* shape visible = */ false, UNUSED_SHAPE,
                /* shape filled = */ false, Color.black,
                /* shape outlined = */ false, Color.black, UNUSED_STROKE,
                /* line visible = */ true, line, lineStroke, linePaint);
    }

    /**
     * Creates a new legend item.
     *
     * @param label  the label (<code>null</code> not permitted).
     * @param description  the description (not currently used,
     *        <code>null</code> permitted).
     * @param toolTipText  the tool tip text (<code>null</code> permitted).
     * @param urlText  the URL text (<code>null</code> permitted).
     * @param shapeVisible  a flag that controls whether or not the shape is
     *                      displayed.
     * @param shape  the shape (<code>null</code> permitted).
     * @param shapeFilled  a flag that controls whether or not the shape is
     *                     filled.
     * @param fillPaint  the fill paint (<code>null</code> not permitted).
     * @param shapeOutlineVisible  a flag that controls whether or not the
     *                             shape is outlined.
     * @param outlinePaint  the outline paint (<code>null</code> not permitted).
     * @param outlineStroke  the outline stroke (<code>null</code> not
     *                       permitted).
     * @param lineVisible  a flag that controls whether or not the line is
     *                     visible.
     * @param line  the line.
     * @param lineStroke  the stroke (<code>null</code> not permitted).
     * @param linePaint  the line paint (<code>null</code> not permitted).
     */
    public LegendItem(String label, String description,
                      String toolTipText, String urlText,
                      boolean shapeVisible, Shape shape,
                      boolean shapeFilled, Paint fillPaint,
                      boolean shapeOutlineVisible, Paint outlinePaint,
                      Stroke outlineStroke,
                      boolean lineVisible, Shape line,
                      Stroke lineStroke, Paint linePaint) {

        if (label == null) {
            throw new IllegalArgumentException("Null 'label' argument.");
        }
        if (fillPaint == null) {
            throw new IllegalArgumentException("Null 'fillPaint' argument.");
        }
        if (lineStroke == null) {
            throw new IllegalArgumentException("Null 'lineStroke' argument.");
        }
        if (outlinePaint == null) {
            throw new IllegalArgumentException("Null 'outlinePaint' argument.");
        }
        if (outlineStroke == null) {
            throw new IllegalArgumentException(
                    "Null 'outlineStroke' argument.");
        }
        this.label = label;
        this.labelPaint = null;
        this.attributedLabel = null;
        this.description = description;
        this.shapeVisible = shapeVisible;
        this.shape = shape;
        this.shapeFilled = shapeFilled;
        this.fillPaint = fillPaint;
        this.fillPaintTransformer = new StandardGradientPaintTransformer();
        this.shapeOutlineVisible = shapeOutlineVisible;
        this.outlinePaint = outlinePaint;
        this.outlineStroke = outlineStroke;
        this.lineVisible = lineVisible;
        this.line = line;
        this.lineStroke = lineStroke;
        this.linePaint = linePaint;
        this.toolTipText = toolTipText;
        this.urlText = urlText;
    }

    /**
     * Creates a legend item with a filled shape.  The shape is not outlined,
     * and no line is visible.
     *
     * @param label  the label (<code>null</code> not permitted).
     * @param description  the description (<code>null</code> permitted).
     * @param toolTipText  the tool tip text (<code>null</code> permitted).
     * @param urlText  the URL text (<code>null</code> permitted).
     * @param shape  the shape (<code>null</code> not permitted).
     * @param fillPaint  the paint used to fill the shape (<code>null</code>
     *                   not permitted).
     */
    public LegendItem(AttributedString label, String description,
                      String toolTipText, String urlText,
                      Shape shape, Paint fillPaint) {

        this(label, description, toolTipText, urlText,
                /* shape visible = */ true, shape,
                /* shape filled = */ true, fillPaint,
                /* shape outlined = */ false, Color.black, UNUSED_STROKE,
                /* line visible = */ false, UNUSED_SHAPE, UNUSED_STROKE,
                Color.black);

    }

    /**
     * Creates a legend item with a filled and outlined shape.
     *
     * @param label  the label (<code>null</code> not permitted).
     * @param description  the description (<code>null</code> permitted).
     * @param toolTipText  the tool tip text (<code>null</code> permitted).
     * @param urlText  the URL text (<code>null</code> permitted).
     * @param shape  the shape (<code>null</code> not permitted).
     * @param fillPaint  the paint used to fill the shape (<code>null</code>
     *                   not permitted).
     * @param outlineStroke  the outline stroke (<code>null</code> not
     *                       permitted).
     * @param outlinePaint  the outline paint (<code>null</code> not
     *                      permitted).
     */
    public LegendItem(AttributedString label, String description,
                      String toolTipText, String urlText,
                      Shape shape, Paint fillPaint,
                      Stroke outlineStroke, Paint outlinePaint) {

        this(label, description, toolTipText, urlText,
                /* shape visible = */ true, shape,
                /* shape filled = */ true, fillPaint,
                /* shape outlined = */ true, outlinePaint, outlineStroke,
                /* line visible = */ false, UNUSED_SHAPE, UNUSED_STROKE,
                Color.black);
    }

    /**
     * Creates a legend item using a line.
     *
     * @param label  the label (<code>null</code> not permitted).
     * @param description  the description (<code>null</code> permitted).
     * @param toolTipText  the tool tip text (<code>null</code> permitted).
     * @param urlText  the URL text (<code>null</code> permitted).
     * @param line  the line (<code>null</code> not permitted).
     * @param lineStroke  the line stroke (<code>null</code> not permitted).
     * @param linePaint  the line paint (<code>null</code> not permitted).
     */
    public LegendItem(AttributedString label, String description,
                      String toolTipText, String urlText,
                      Shape line, Stroke lineStroke, Paint linePaint) {

        this(label, description, toolTipText, urlText,
                /* shape visible = */ false, UNUSED_SHAPE,
                /* shape filled = */ false, Color.black,
                /* shape outlined = */ false, Color.black, UNUSED_STROKE,
                /* line visible = */ true, line, lineStroke, linePaint);
    }

    /**
     * Creates a new legend item.
     *
     * @param label  the label (<code>null</code> not permitted).
     * @param description  the description (not currently used,
     *        <code>null</code> permitted).
     * @param toolTipText  the tool tip text (<code>null</code> permitted).
     * @param urlText  the URL text (<code>null</code> permitted).
     * @param shapeVisible  a flag that controls whether or not the shape is
     *                      displayed.
     * @param shape  the shape (<code>null</code> permitted).
     * @param shapeFilled  a flag that controls whether or not the shape is
     *                     filled.
     * @param fillPaint  the fill paint (<code>null</code> not permitted).
     * @param shapeOutlineVisible  a flag that controls whether or not the
     *                             shape is outlined.
     * @param outlinePaint  the outline paint (<code>null</code> not permitted).
     * @param outlineStroke  the outline stroke (<code>null</code> not
     *                       permitted).
     * @param lineVisible  a flag that controls whether or not the line is
     *                     visible.
     * @param line  the line (<code>null</code> not permitted).
     * @param lineStroke  the stroke (<code>null</code> not permitted).
     * @param linePaint  the line paint (<code>null</code> not permitted).
     */
    public LegendItem(AttributedString label, String description,
                      String toolTipText, String urlText,
                      boolean shapeVisible, Shape shape,
                      boolean shapeFilled, Paint fillPaint,
                      boolean shapeOutlineVisible, Paint outlinePaint,
                      Stroke outlineStroke,
                      boolean lineVisible, Shape line, Stroke lineStroke,
                      Paint linePaint) {

        if (label == null) {
            throw new IllegalArgumentException("Null 'label' argument.");
        }
        if (fillPaint == null) {
            throw new IllegalArgumentException("Null 'fillPaint' argument.");
        }
        if (lineStroke == null) {
            throw new IllegalArgumentException("Null 'lineStroke' argument.");
        }
        if (line == null) {
            throw new IllegalArgumentException("Null 'line' argument.");
        }
        if (linePaint == null) {
            throw new IllegalArgumentException("Null 'linePaint' argument.");
        }
        if (outlinePaint == null) {
            throw new IllegalArgumentException("Null 'outlinePaint' argument.");
        }
        if (outlineStroke == null) {
            throw new IllegalArgumentException(
                "Null 'outlineStroke' argument.");
        }
        this.label = characterIteratorToString(label.getIterator());
        this.attributedLabel = label;
        this.description = description;
        this.shapeVisible = shapeVisible;
        this.shape = shape;
        this.shapeFilled = shapeFilled;
        this.fillPaint = fillPaint;
        this.fillPaintTransformer = new StandardGradientPaintTransformer();
        this.shapeOutlineVisible = shapeOutlineVisible;
        this.outlinePaint = outlinePaint;
        this.outlineStroke = outlineStroke;
        this.lineVisible = lineVisible;
        this.line = line;
        this.lineStroke = lineStroke;
        this.linePaint = linePaint;
        this.toolTipText = toolTipText;
        this.urlText = urlText;
    }

    /**
     * Returns a string containing the characters from the given iterator.
     *
     * @param iterator  the iterator (<code>null</code> not permitted).
     *
     * @return A string.
     */
    private String characterIteratorToString(CharacterIterator iterator) {
        int endIndex = iterator.getEndIndex();
        int beginIndex = iterator.getBeginIndex();
        int count = endIndex - beginIndex;
        if (count <= 0) {
            return "";
        }
        char[] chars = new char[count];
        int i = 0;
        char c = iterator.first();
        while (c != CharacterIterator.DONE) {
            chars[i] = c;
            i++;
            c = iterator.next();
        }
        return new String(chars);
    }

    /**
     * Returns the dataset.
     *
     * @return The dataset.
     *
     * @since 1.0.6
     *
     * @see #setDatasetIndex(int)
     */
    public Dataset getDataset() {
        return this.dataset;
    }

    /**
     * Sets the dataset.
     *
     * @param dataset  the dataset.
     *
     * @since 1.0.6
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * Returns the dataset index for this legend item.
     *
     * @return The dataset index.
     *
     * @since 1.0.2
     *
     * @see #setDatasetIndex(int)
     * @see #getDataset()
     */
    public int getDatasetIndex() {
        return this.datasetIndex;
    }

    /**
     * Sets the dataset index for this legend item.
     *
     * @param index  the index.
     *
     * @since 1.0.2
     *
     * @see #getDatasetIndex()
     */
    public void setDatasetIndex(int index) {
        this.datasetIndex = index;
    }

    /**
     * Returns the series key.
     *
     * @return The series key.
     *
     * @since 1.0.6
     *
     * @see #setSeriesKey(Comparable)
     */
    public Comparable getSeriesKey() {
        return this.seriesKey;
    }

    /**
     * Sets the series key.
     *
     * @param key  the series key.
     *
     * @since 1.0.6
     */
    public void setSeriesKey(Comparable key) {
        this.seriesKey = key;
    }

    /**
     * Returns the series index for this legend item.
     *
     * @return The series index.
     *
     * @since 1.0.2
     */
    public int getSeriesIndex() {
        return this.series;
    }

    /**
     * Sets the series index for this legend item.
     *
     * @param index  the index.
     *
     * @since 1.0.2
     */
    public void setSeriesIndex(int index) {
        this.series = index;
    }

    /**
     * Returns the label.
     *
     * @return The label (never <code>null</code>).
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Returns the label font.
     *
     * @return The label font (possibly <code>null</code>).
     *
     * @since 1.0.11
     */
    public Font getLabelFont() {
        return this.labelFont;
    }

    /**
     * Sets the label font.
     *
     * @param font  the font (<code>null</code> permitted).
     *
     * @since 1.0.11
     */
    public void setLabelFont(Font font) {
        this.labelFont = font;
    }

    /**
     * Returns the paint used to draw the label.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @since 1.0.11
     */
    public Paint getLabelPaint() {
        return this.labelPaint;
    }

    /**
     * Sets the paint used to draw the label.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @since 1.0.11
     */
    public void setLabelPaint(Paint paint) {
        this.labelPaint = paint;
    }

    /**
     * Returns the attributed label.
     *
     * @return The attributed label (possibly <code>null</code>).
     */
    public AttributedString getAttributedLabel() {
        return this.attributedLabel;
    }

    /**
     * Returns the description for the legend item.
     *
     * @return The description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the tool tip text.
     *
     * @return The tool tip text (possibly <code>null</code>).
     */
    public String getToolTipText() {
        return this.toolTipText;
    }

    /**
     * Returns the URL text.
     *
     * @return The URL text (possibly <code>null</code>).
     */
    public String getURLText() {
        return this.urlText;
    }

    /**
     * Returns a flag that indicates whether or not the shape is visible.
     *
     * @return A boolean.
     */
    public boolean isShapeVisible() {
        return this.shapeVisible;
    }

    /**
     * Returns the shape used to label the series represented by this legend
     * item.
     *
     * @return The shape (never <code>null</code>).
     */
    public Shape getShape() {
        return this.shape;
    }

    /**
     * Returns a flag that controls whether or not the shape is filled.
     *
     * @return A boolean.
     */
    public boolean isShapeFilled() {
        return this.shapeFilled;
    }

    /**
     * Returns the fill paint.
     *
     * @return The fill paint (never <code>null</code>).
     */
    public Paint getFillPaint() {
        return this.fillPaint;
    }

    /**
     * Sets the fill paint.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @since 1.0.11
     */
    public void setFillPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.fillPaint = paint;
    }

    /**
     * Returns the flag that controls whether or not the shape outline
     * is visible.
     *
     * @return A boolean.
     */
    public boolean isShapeOutlineVisible() {
        return this.shapeOutlineVisible;
    }

    /**
     * Returns the line stroke for the series.
     *
     * @return The stroke (never <code>null</code>).
     */
    public Stroke getLineStroke() {
        return this.lineStroke;
    }

    /**
     * Returns the paint used for lines.
     *
     * @return The paint (never <code>null</code>).
     */
    public Paint getLinePaint() {
        return this.linePaint;
    }

    /**
     * Sets the line paint.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @since 1.0.11
     */
    public void setLinePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.linePaint = paint;
    }

    /**
     * Returns the outline paint.
     *
     * @return The outline paint (never <code>null</code>).
     */
    public Paint getOutlinePaint() {
        return this.outlinePaint;
    }

    /**
     * Sets the outline paint.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @since 1.0.11
     */
    public void setOutlinePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.outlinePaint = paint;
    }

    /**
     * Returns the outline stroke.
     *
     * @return The outline stroke (never <code>null</code>).
     */
    public Stroke getOutlineStroke() {
        return this.outlineStroke;
    }

    /**
     * Returns a flag that indicates whether or not the line is visible.
     *
     * @return A boolean.
     */
    public boolean isLineVisible() {
        return this.lineVisible;
    }

    /**
     * Returns the line.
     *
     * @return The line (never <code>null</code>).
     */
    public Shape getLine() {
        return this.line;
    }

    /**
     * Returns the transformer used when the fill paint is an instance of
     * <code>GradientPaint</code>.
     *
     * @return The transformer (never <code>null</code>).
     *
     * @since 1.0.4
     *
     * @see #setFillPaintTransformer(GradientPaintTransformer)
     */
    public GradientPaintTransformer getFillPaintTransformer() {
        return this.fillPaintTransformer;
    }

    /**
     * Sets the transformer used when the fill paint is an instance of
     * <code>GradientPaint</code>.
     *
     * @param transformer  the transformer (<code>null</code> not permitted).
     *
     * @since 1.0.4
     *
     * @see #getFillPaintTransformer()
     */
    public void setFillPaintTransformer(GradientPaintTransformer transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("Null 'transformer' attribute.");
        }
        this.fillPaintTransformer = transformer;
    }

    /**
     * Tests this item for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LegendItem)) {
                return false;
        }
        LegendItem that = (LegendItem) obj;
        if (this.datasetIndex != that.datasetIndex) {
            return false;
        }
        if (this.series != that.series) {
            return false;
        }
        if (!this.label.equals(that.label)) {
            return false;
        }
        if (!AttributedStringUtilities.equal(this.attributedLabel,
                that.attributedLabel)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.description, that.description)) {
            return false;
        }
        if (this.shapeVisible != that.shapeVisible) {
            return false;
        }
        if (!ShapeUtilities.equal(this.shape, that.shape)) {
            return false;
        }
        if (this.shapeFilled != that.shapeFilled) {
            return false;
        }
        if (!PaintUtilities.equal(this.fillPaint, that.fillPaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.fillPaintTransformer,
                that.fillPaintTransformer)) {
            return false;
        }
        if (this.shapeOutlineVisible != that.shapeOutlineVisible) {
            return false;
        }
        if (!this.outlineStroke.equals(that.outlineStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
        }
        if (!this.lineVisible == that.lineVisible) {
            return false;
        }
        if (!ShapeUtilities.equal(this.line, that.line)) {
            return false;
        }
        if (!this.lineStroke.equals(that.lineStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.linePaint, that.linePaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.labelFont, that.labelFont)) {
            return false;
        }
        if (!PaintUtilities.equal(this.labelPaint, that.labelPaint)) {
            return false;
        }
        return true;
    }

    /**
     * Returns an independent copy of this object (except that the clone will
     * still reference the same dataset as the original
     * <code>LegendItem</code>).
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if the legend item cannot be cloned.
     *
     * @since 1.0.10
     */
    public Object clone() throws CloneNotSupportedException {
        LegendItem clone = (LegendItem) super.clone();
        if (this.seriesKey instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable) this.seriesKey;
            clone.seriesKey = (Comparable) pc.clone();
        }
        // FIXME: Clone the attributed string if it is not null
        clone.shape = ShapeUtilities.clone(this.shape);
        if (this.fillPaintTransformer instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable) this.fillPaintTransformer;
            clone.fillPaintTransformer = (GradientPaintTransformer) pc.clone();

        }
        clone.line = ShapeUtilities.clone(this.line);
        return clone;
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream (<code>null</code> not permitted).
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeAttributedString(this.attributedLabel, stream);
        SerialUtilities.writeShape(this.shape, stream);
        SerialUtilities.writePaint(this.fillPaint, stream);
        SerialUtilities.writeStroke(this.outlineStroke, stream);
        SerialUtilities.writePaint(this.outlinePaint, stream);
        SerialUtilities.writeShape(this.line, stream);
        SerialUtilities.writeStroke(this.lineStroke, stream);
        SerialUtilities.writePaint(this.linePaint, stream);
        SerialUtilities.writePaint(this.labelPaint, stream);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream (<code>null</code> not permitted).
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.attributedLabel = SerialUtilities.readAttributedString(stream);
        this.shape = SerialUtilities.readShape(stream);
        this.fillPaint = SerialUtilities.readPaint(stream);
        this.outlineStroke = SerialUtilities.readStroke(stream);
        this.outlinePaint = SerialUtilities.readPaint(stream);
        this.line = SerialUtilities.readShape(stream);
        this.lineStroke = SerialUtilities.readStroke(stream);
        this.linePaint = SerialUtilities.readPaint(stream);
        this.labelPaint = SerialUtilities.readPaint(stream);
    }

}
