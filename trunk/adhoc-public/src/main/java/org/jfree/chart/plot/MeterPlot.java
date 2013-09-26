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
 * --------------
 * MeterPlot.java
 * --------------
 * (C) Copyright 2000-2008, by Hari and Contributors.
 *
 * Original Author:  Hari (ourhari@hotmail.com);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Bob Orchard;
 *                   Arnaud Lelievre;
 *                   Nicolas Brodu;
 *                   David Bastend;
 *
 * Changes
 * -------
 * 01-Apr-2002 : Version 1, contributed by Hari (DG);
 * 23-Apr-2002 : Moved dataset from JFreeChart to Plot (DG);
 * 22-Aug-2002 : Added changes suggest by Bob Orchard, changed Color to Paint
 *               for consistency, plus added Javadoc comments (DG);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 23-Jan-2003 : Removed one constructor (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 20-Aug-2003 : Changed dataset from MeterDataset --> ValueDataset, added
 *               equals() method,
 * 08-Sep-2003 : Added internationalization via use of properties
 *               resourceBundle (RFE 690236) (AL);
 *               implemented Cloneable, and various other changes (DG);
 * 08-Sep-2003 : Added serialization methods (NB);
 * 11-Sep-2003 : Added cloning support (NB);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 25-Sep-2003 : Fix useless cloning. Correct dataset listener registration in
 *               constructor. (NB)
 * 29-Oct-2003 : Added workaround for font alignment in PDF output (DG);
 * 17-Jan-2004 : Changed to allow dialBackgroundPaint to be set to null - see
 *               bug 823628 (DG);
 * 07-Apr-2004 : Changed string bounds calculation (DG);
 * 12-May-2004 : Added tickLabelFormat attribute - see RFE 949566.  Also
 *               updated the equals() method (DG);
 * 02-Nov-2004 : Added sanity checks for range, and only draw the needle if the
 *               value is contained within the overall range - see bug report
 *               1056047 (DG);
 * 11-Jan-2005 : Removed deprecated code in preparation for the 1.0.0
 *               release (DG);
 * 02-Feb-2005 : Added optional background paint for each region (DG);
 * 22-Mar-2005 : Removed 'normal', 'warning' and 'critical' regions and put in
 *               facility to define an arbitrary number of MeterIntervals,
 *               based on a contribution by David Bastend (DG);
 * 20-Apr-2005 : Small update for change to LegendItem constructors (DG);
 * 05-May-2005 : Updated draw() method parameters (DG);
 * 08-Jun-2005 : Fixed equals() method to handle GradientPaint (DG);
 * 10-Nov-2005 : Added tickPaint, tickSize and valuePaint attributes, and
 *               put value label drawing code into a separate method (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 05-Mar-2007 : Restore clip region correctly (see bug 1667750) (DG);
 * 18-May-2007 : Set dataset for LegendItem (DG);
 * 29-Nov-2007 : Fixed serialization bug with dialOutlinePaint (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.ValueDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;

/**
 * A plot that displays a single value in the form of a needle on a dial.
 * Defined ranges (for example, 'normal', 'warning' and 'critical') can be
 * highlighted on the dial.
 */
public class MeterPlot extends Plot implements Serializable, Cloneable {

    /** For serialization. */
    private static final long serialVersionUID = 2987472457734470962L;

    /** The default background paint. */
    static final Paint DEFAULT_DIAL_BACKGROUND_PAINT = Color.black;

    /** The default needle paint. */
    static final Paint DEFAULT_NEEDLE_PAINT = Color.green;

    /** The default value font. */
    static final Font DEFAULT_VALUE_FONT = new Font("SansSerif", Font.BOLD, 12);

    /** The default value paint. */
    static final Paint DEFAULT_VALUE_PAINT = Color.yellow;

    /** The default meter angle. */
    public static final int DEFAULT_METER_ANGLE = 270;

    /** The default border size. */
    public static final float DEFAULT_BORDER_SIZE = 3f;

    /** The default circle size. */
    public static final float DEFAULT_CIRCLE_SIZE = 10f;

    /** The default label font. */
    public static final Font DEFAULT_LABEL_FONT = new Font("SansSerif",
            Font.BOLD, 10);

    /** The dataset (contains a single value). */
    private ValueDataset dataset;

    /** The dial shape (background shape). */
    private DialShape shape;

    /** The dial extent (measured in degrees). */
    private int meterAngle;

    /** The overall range of data values on the dial. */
    private Range range;

    /** The tick size. */
    private double tickSize;

    /** The paint used to draw the ticks. */
    private transient Paint tickPaint;

    /** The units displayed on the dial. */
    private String units;

    /** The font for the value displayed in the center of the dial. */
    private Font valueFont;

    /** The paint for the value displayed in the center of the dial. */
    private transient Paint valuePaint;

    /** A flag that controls whether or not the border is drawn. */
    private boolean drawBorder;

    /** The outline paint. */
    private transient Paint dialOutlinePaint;

    /** The paint for the dial background. */
    private transient Paint dialBackgroundPaint;

    /** The paint for the needle. */
    private transient Paint needlePaint;

    /** A flag that controls whether or not the tick labels are visible. */
    private boolean tickLabelsVisible;

    /** The tick label font. */
    private Font tickLabelFont;

    /** The tick label paint. */
    private transient Paint tickLabelPaint;

    /** The tick label format. */
    private NumberFormat tickLabelFormat;

    /** The resourceBundle for the localization. */
    protected static ResourceBundle localizationResources
            = ResourceBundleWrapper.getBundle(
                    "org.jfree.chart.plot.LocalizationBundle");

    /**
     * A (possibly empty) list of the {@link MeterInterval}s to be highlighted
     * on the dial.
     */
    private List intervals;

    /**
     * Creates a new plot with a default range of <code>0</code> to
     * <code>100</code> and no value to display.
     */
    public MeterPlot() {
        this(null);
    }

    /**
     * Creates a new plot that displays the value from the supplied dataset.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     */
    public MeterPlot(ValueDataset dataset) {
        super();
        this.shape = DialShape.CIRCLE;
        this.meterAngle = DEFAULT_METER_ANGLE;
        this.range = new Range(0.0, 100.0);
        this.tickSize = 10.0;
        this.tickPaint = Color.white;
        this.units = "Units";
        this.needlePaint = MeterPlot.DEFAULT_NEEDLE_PAINT;
        this.tickLabelsVisible = true;
        this.tickLabelFont = MeterPlot.DEFAULT_LABEL_FONT;
        this.tickLabelPaint = Color.black;
        this.tickLabelFormat = NumberFormat.getInstance();
        this.valueFont = MeterPlot.DEFAULT_VALUE_FONT;
        this.valuePaint = MeterPlot.DEFAULT_VALUE_PAINT;
        this.dialBackgroundPaint = MeterPlot.DEFAULT_DIAL_BACKGROUND_PAINT;
        this.intervals = new java.util.ArrayList();
        setDataset(dataset);
    }

    /**
     * Returns the dial shape.  The default is {@link DialShape#CIRCLE}).
     *
     * @return The dial shape (never <code>null</code>).
     *
     * @see #setDialShape(DialShape)
     */
    public DialShape getDialShape() {
        return this.shape;
    }

    /**
     * Sets the dial shape and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param shape  the shape (<code>null</code> not permitted).
     *
     * @see #getDialShape()
     */
    public void setDialShape(DialShape shape) {
        if (shape == null) {
            throw new IllegalArgumentException("Null 'shape' argument.");
        }
        this.shape = shape;
        fireChangeEvent();
    }

    /**
     * Returns the meter angle in degrees.  This defines, in part, the shape
     * of the dial.  The default is 270 degrees.
     *
     * @return The meter angle (in degrees).
     *
     * @see #setMeterAngle(int)
     */
    public int getMeterAngle() {
        return this.meterAngle;
    }

    /**
     * Sets the angle (in degrees) for the whole range of the dial and sends
     * a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param angle  the angle (in degrees, in the range 1-360).
     *
     * @see #getMeterAngle()
     */
    public void setMeterAngle(int angle) {
        if (angle < 1 || angle > 360) {
            throw new IllegalArgumentException("Invalid 'angle' (" + angle
                    + ")");
        }
        this.meterAngle = angle;
        fireChangeEvent();
    }

    /**
     * Returns the overall range for the dial.
     *
     * @return The overall range (never <code>null</code>).
     *
     * @see #setRange(Range)
     */
    public Range getRange() {
        return this.range;
    }

    /**
     * Sets the range for the dial and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param range  the range (<code>null</code> not permitted and zero-length
     *               ranges not permitted).
     *
     * @see #getRange()
     */
    public void setRange(Range range) {
        if (range == null) {
            throw new IllegalArgumentException("Null 'range' argument.");
        }
        if (!(range.getLength() > 0.0)) {
            throw new IllegalArgumentException(
                    "Range length must be positive.");
        }
        this.range = range;
        fireChangeEvent();
    }

    /**
     * Returns the tick size (the interval between ticks on the dial).
     *
     * @return The tick size.
     *
     * @see #setTickSize(double)
     */
    public double getTickSize() {
        return this.tickSize;
    }

    /**
     * Sets the tick size and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param size  the tick size (must be > 0).
     *
     * @see #getTickSize()
     */
    public void setTickSize(double size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Requires 'size' > 0.");
        }
        this.tickSize = size;
        fireChangeEvent();
    }

    /**
     * Returns the paint used to draw the ticks around the dial.
     *
     * @return The paint used to draw the ticks around the dial (never
     *         <code>null</code>).
     *
     * @see #setTickPaint(Paint)
     */
    public Paint getTickPaint() {
        return this.tickPaint;
    }

    /**
     * Sets the paint used to draw the tick labels around the dial and sends
     * a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getTickPaint()
     */
    public void setTickPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.tickPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns a string describing the units for the dial.
     *
     * @return The units (possibly <code>null</code>).
     *
     * @see #setUnits(String)
     */
    public String getUnits() {
        return this.units;
    }

    /**
     * Sets the units for the dial and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param units  the units (<code>null</code> permitted).
     *
     * @see #getUnits()
     */
    public void setUnits(String units) {
        this.units = units;
        fireChangeEvent();
    }

    /**
     * Returns the paint for the needle.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setNeedlePaint(Paint)
     */
    public Paint getNeedlePaint() {
        return this.needlePaint;
    }

    /**
     * Sets the paint used to display the needle and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getNeedlePaint()
     */
    public void setNeedlePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.needlePaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the flag that determines whether or not tick labels are visible.
     *
     * @return The flag.
     *
     * @see #setTickLabelsVisible(boolean)
     */
    public boolean getTickLabelsVisible() {
        return this.tickLabelsVisible;
    }

    /**
     * Sets the flag that controls whether or not the tick labels are visible
     * and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param visible  the flag.
     *
     * @see #getTickLabelsVisible()
     */
    public void setTickLabelsVisible(boolean visible) {
        if (this.tickLabelsVisible != visible) {
            this.tickLabelsVisible = visible;
            fireChangeEvent();
        }
    }

    /**
     * Returns the tick label font.
     *
     * @return The font (never <code>null</code>).
     *
     * @see #setTickLabelFont(Font)
     */
    public Font getTickLabelFont() {
        return this.tickLabelFont;
    }

    /**
     * Sets the tick label font and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param font  the font (<code>null</code> not permitted).
     *
     * @see #getTickLabelFont()
     */
    public void setTickLabelFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }
        if (!this.tickLabelFont.equals(font)) {
            this.tickLabelFont = font;
            fireChangeEvent();
        }
    }

    /**
     * Returns the tick label paint.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setTickLabelPaint(Paint)
     */
    public Paint getTickLabelPaint() {
        return this.tickLabelPaint;
    }

    /**
     * Sets the tick label paint and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getTickLabelPaint()
     */
    public void setTickLabelPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        if (!this.tickLabelPaint.equals(paint)) {
            this.tickLabelPaint = paint;
            fireChangeEvent();
        }
    }

    /**
     * Returns the tick label format.
     *
     * @return The tick label format (never <code>null</code>).
     *
     * @see #setTickLabelFormat(NumberFormat)
     */
    public NumberFormat getTickLabelFormat() {
        return this.tickLabelFormat;
    }

    /**
     * Sets the format for the tick labels and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     *
     * @param format  the format (<code>null</code> not permitted).
     *
     * @see #getTickLabelFormat()
     */
    public void setTickLabelFormat(NumberFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("Null 'format' argument.");
        }
        this.tickLabelFormat = format;
        fireChangeEvent();
    }

    /**
     * Returns the font for the value label.
     *
     * @return The font (never <code>null</code>).
     *
     * @see #setValueFont(Font)
     */
    public Font getValueFont() {
        return this.valueFont;
    }

    /**
     * Sets the font used to display the value label and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param font  the font (<code>null</code> not permitted).
     *
     * @see #getValueFont()
     */
    public void setValueFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }
        this.valueFont = font;
        fireChangeEvent();
    }

    /**
     * Returns the paint for the value label.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setValuePaint(Paint)
     */
    public Paint getValuePaint() {
        return this.valuePaint;
    }

    /**
     * Sets the paint used to display the value label and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getValuePaint()
     */
    public void setValuePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.valuePaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the paint for the dial background.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setDialBackgroundPaint(Paint)
     */
    public Paint getDialBackgroundPaint() {
        return this.dialBackgroundPaint;
    }

    /**
     * Sets the paint used to fill the dial background.  Set this to
     * <code>null</code> for no background.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getDialBackgroundPaint()
     */
    public void setDialBackgroundPaint(Paint paint) {
        this.dialBackgroundPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns a flag that controls whether or not a rectangular border is
     * drawn around the plot area.
     *
     * @return A flag.
     *
     * @see #setDrawBorder(boolean)
     */
    public boolean getDrawBorder() {
        return this.drawBorder;
    }

    /**
     * Sets the flag that controls whether or not a rectangular border is drawn
     * around the plot area and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param draw  the flag.
     *
     * @see #getDrawBorder()
     */
    public void setDrawBorder(boolean draw) {
        // TODO: fix output when this flag is set to true
        this.drawBorder = draw;
        fireChangeEvent();
    }

    /**
     * Returns the dial outline paint.
     *
     * @return The paint.
     *
     * @see #setDialOutlinePaint(Paint)
     */
    public Paint getDialOutlinePaint() {
        return this.dialOutlinePaint;
    }

    /**
     * Sets the dial outline paint and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param paint  the paint.
     *
     * @see #getDialOutlinePaint()
     */
    public void setDialOutlinePaint(Paint paint) {
        this.dialOutlinePaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the dataset for the plot.
     *
     * @return The dataset (possibly <code>null</code>).
     *
     * @see #setDataset(ValueDataset)
     */
    public ValueDataset getDataset() {
        return this.dataset;
    }

    /**
     * Sets the dataset for the plot, replacing the existing dataset if there
     * is one, and triggers a {@link PlotChangeEvent}.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @see #getDataset()
     */
    public void setDataset(ValueDataset dataset) {

        // if there is an existing dataset, remove the plot from the list of
        // change listeners...
        ValueDataset existing = this.dataset;
        if (existing != null) {
            existing.removeChangeListener(this);
        }

        // set the new dataset, and register the chart as a change listener...
        this.dataset = dataset;
        if (dataset != null) {
            setDatasetGroup(dataset.getGroup());
            dataset.addChangeListener(this);
        }

        // send a dataset change event to self...
        DatasetChangeEvent event = new DatasetChangeEvent(this, dataset);
        datasetChanged(event);

    }

    /**
     * Returns an unmodifiable list of the intervals for the plot.
     *
     * @return A list.
     *
     * @see #addInterval(MeterInterval)
     */
    public List getIntervals() {
        return Collections.unmodifiableList(this.intervals);
    }

    /**
     * Adds an interval and sends a {@link PlotChangeEvent} to all registered
     * listeners.
     *
     * @param interval  the interval (<code>null</code> not permitted).
     *
     * @see #getIntervals()
     * @see #clearIntervals()
     */
    public void addInterval(MeterInterval interval) {
        if (interval == null) {
            throw new IllegalArgumentException("Null 'interval' argument.");
        }
        this.intervals.add(interval);
        fireChangeEvent();
    }

    /**
     * Clears the intervals for the plot and sends a {@link PlotChangeEvent} to
     * all registered listeners.
     *
     * @see #addInterval(MeterInterval)
     */
    public void clearIntervals() {
        this.intervals.clear();
        fireChangeEvent();
    }

    /**
     * Returns an item for each interval.
     *
     * @return A collection of legend items.
     */
    public LegendItemCollection getLegendItems() {
        LegendItemCollection result = new LegendItemCollection();
        Iterator iterator = this.intervals.iterator();
        while (iterator.hasNext()) {
            MeterInterval mi = (MeterInterval) iterator.next();
            Paint color = mi.getBackgroundPaint();
            if (color == null) {
                color = mi.getOutlinePaint();
            }
            LegendItem item = new LegendItem(mi.getLabel(), mi.getLabel(),
                    null, null, new Rectangle2D.Double(-4.0, -4.0, 8.0, 8.0),
                    color);
            item.setDataset(getDataset());
            result.add(item);
        }
        return result;
    }

    /**
     * Draws the plot on a Java 2D graphics device (such as the screen or a
     * printer).
     *
     * @param g2  the graphics device.
     * @param area  the area within which the plot should be drawn.
     * @param anchor  the anchor point (<code>null</code> permitted).
     * @param parentState  the state from the parent plot, if there is one.
     * @param info  collects info about the drawing.
     */
    public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
                     PlotState parentState,
                     PlotRenderingInfo info) {

        if (info != null) {
            info.setPlotArea(area);
        }

        // adjust for insets...
        RectangleInsets insets = getInsets();
        insets.trim(area);

        area.setRect(area.getX() + 4, area.getY() + 4, area.getWidth() - 8,
                area.getHeight() - 8);

        // draw the background
        if (this.drawBorder) {
            drawBackground(g2, area);
        }

        // adjust the plot area by the interior spacing value
        double gapHorizontal = (2 * DEFAULT_BORDER_SIZE);
        double gapVertical = (2 * DEFAULT_BORDER_SIZE);
        double meterX = area.getX() + gapHorizontal / 2;
        double meterY = area.getY() + gapVertical / 2;
        double meterW = area.getWidth() - gapHorizontal;
        double meterH = area.getHeight() - gapVertical
                + ((this.meterAngle <= 180) && (this.shape != DialShape.CIRCLE)
                ? area.getHeight() / 1.25 : 0);

        double min = Math.min(meterW, meterH) / 2;
        meterX = (meterX + meterX + meterW) / 2 - min;
        meterY = (meterY + meterY + meterH) / 2 - min;
        meterW = 2 * min;
        meterH = 2 * min;

        Rectangle2D meterArea = new Rectangle2D.Double(meterX, meterY, meterW,
                meterH);

        Rectangle2D.Double originalArea = new Rectangle2D.Double(
                meterArea.getX() - 4, meterArea.getY() - 4,
                meterArea.getWidth() + 8, meterArea.getHeight() + 8);

        double meterMiddleX = meterArea.getCenterX();
        double meterMiddleY = meterArea.getCenterY();

        // plot the data (unless the dataset is null)...
        ValueDataset data = getDataset();
        if (data != null) {
            double dataMin = this.range.getLowerBound();
            double dataMax = this.range.getUpperBound();

            Shape savedClip = g2.getClip();
            g2.clip(originalArea);
            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    getForegroundAlpha()));

            if (this.dialBackgroundPaint != null) {
                fillArc(g2, originalArea, dataMin, dataMax,
                        this.dialBackgroundPaint, true);
            }
            drawTicks(g2, meterArea, dataMin, dataMax);
            drawArcForInterval(g2, meterArea, new MeterInterval("", this.range,
                    this.dialOutlinePaint, new BasicStroke(1.0f), null));

            Iterator iterator = this.intervals.iterator();
            while (iterator.hasNext()) {
                MeterInterval interval = (MeterInterval) iterator.next();
                drawArcForInterval(g2, meterArea, interval);
            }

            Number n = data.getValue();
            if (n != null) {
                double value = n.doubleValue();
                drawValueLabel(g2, meterArea);

                if (this.range.contains(value)) {
                    g2.setPaint(this.needlePaint);
                    g2.setStroke(new BasicStroke(2.0f));

                    double radius = (meterArea.getWidth() / 2)
                                    + DEFAULT_BORDER_SIZE + 15;
                    double valueAngle = valueToAngle(value);
                    double valueP1 = meterMiddleX
                            + (radius * Math.cos(Math.PI * (valueAngle / 180)));
                    double valueP2 = meterMiddleY
                            - (radius * Math.sin(Math.PI * (valueAngle / 180)));

                    Polygon arrow = new Polygon();
                    if ((valueAngle > 135 && valueAngle < 225)
                        || (valueAngle < 45 && valueAngle > -45)) {

                        double valueP3 = (meterMiddleY
                                - DEFAULT_CIRCLE_SIZE / 4);
                        double valueP4 = (meterMiddleY
                                + DEFAULT_CIRCLE_SIZE / 4);
                        arrow.addPoint((int) meterMiddleX, (int) valueP3);
                        arrow.addPoint((int) meterMiddleX, (int) valueP4);

                    }
                    else {
                        arrow.addPoint((int) (meterMiddleX
                                - DEFAULT_CIRCLE_SIZE / 4), (int) meterMiddleY);
                        arrow.addPoint((int) (meterMiddleX
                                + DEFAULT_CIRCLE_SIZE / 4), (int) meterMiddleY);
                    }
                    arrow.addPoint((int) valueP1, (int) valueP2);
                    g2.fill(arrow);

                    Ellipse2D circle = new Ellipse2D.Double(meterMiddleX
                            - DEFAULT_CIRCLE_SIZE / 2, meterMiddleY
                            - DEFAULT_CIRCLE_SIZE / 2, DEFAULT_CIRCLE_SIZE,
                            DEFAULT_CIRCLE_SIZE);
                    g2.fill(circle);
                }
            }

            g2.setClip(savedClip);
            g2.setComposite(originalComposite);

        }
        if (this.drawBorder) {
            drawOutline(g2, area);
        }

    }

    /**
     * Draws the arc to represent an interval.
     *
     * @param g2  the graphics device.
     * @param meterArea  the drawing area.
     * @param interval  the interval.
     */
    protected void drawArcForInterval(Graphics2D g2, Rectangle2D meterArea,
                                      MeterInterval interval) {

        double minValue = interval.getRange().getLowerBound();
        double maxValue = interval.getRange().getUpperBound();
        Paint outlinePaint = interval.getOutlinePaint();
        Stroke outlineStroke = interval.getOutlineStroke();
        Paint backgroundPaint = interval.getBackgroundPaint();

        if (backgroundPaint != null) {
            fillArc(g2, meterArea, minValue, maxValue, backgroundPaint, false);
        }
        if (outlinePaint != null) {
            if (outlineStroke != null) {
                drawArc(g2, meterArea, minValue, maxValue, outlinePaint,
                        outlineStroke);
            }
            drawTick(g2, meterArea, minValue, true);
            drawTick(g2, meterArea, maxValue, true);
        }
    }

    /**
     * Draws an arc.
     *
     * @param g2  the graphics device.
     * @param area  the plot area.
     * @param minValue  the minimum value.
     * @param maxValue  the maximum value.
     * @param paint  the paint.
     * @param stroke  the stroke.
     */
    protected void drawArc(Graphics2D g2, Rectangle2D area, double minValue,
                           double maxValue, Paint paint, Stroke stroke) {

        double startAngle = valueToAngle(maxValue);
        double endAngle = valueToAngle(minValue);
        double extent = endAngle - startAngle;

        double x = area.getX();
        double y = area.getY();
        double w = area.getWidth();
        double h = area.getHeight();
        g2.setPaint(paint);
        g2.setStroke(stroke);

        if (paint != null && stroke != null) {
            Arc2D.Double arc = new Arc2D.Double(x, y, w, h, startAngle,
                    extent, Arc2D.OPEN);
            g2.setPaint(paint);
            g2.setStroke(stroke);
            g2.draw(arc);
        }

    }

    /**
     * Fills an arc on the dial between the given values.
     *
     * @param g2  the graphics device.
     * @param area  the plot area.
     * @param minValue  the minimum data value.
     * @param maxValue  the maximum data value.
     * @param paint  the background paint (<code>null</code> not permitted).
     * @param dial  a flag that indicates whether the arc represents the whole
     *              dial.
     */
    protected void fillArc(Graphics2D g2, Rectangle2D area,
                           double minValue, double maxValue, Paint paint,
                           boolean dial) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument");
        }
        double startAngle = valueToAngle(maxValue);
        double endAngle = valueToAngle(minValue);
        double extent = endAngle - startAngle;

        double x = area.getX();
        double y = area.getY();
        double w = area.getWidth();
        double h = area.getHeight();
        int joinType = Arc2D.OPEN;
        if (this.shape == DialShape.PIE) {
            joinType = Arc2D.PIE;
        }
        else if (this.shape == DialShape.CHORD) {
            if (dial && this.meterAngle > 180) {
                joinType = Arc2D.CHORD;
            }
            else {
                joinType = Arc2D.PIE;
            }
        }
        else if (this.shape == DialShape.CIRCLE) {
            joinType = Arc2D.PIE;
            if (dial) {
                extent = 360;
            }
        }
        else {
            throw new IllegalStateException("DialShape not recognised.");
        }

        g2.setPaint(paint);
        Arc2D.Double arc = new Arc2D.Double(x, y, w, h, startAngle, extent,
                joinType);
        g2.fill(arc);
    }

    /**
     * Translates a data value to an angle on the dial.
     *
     * @param value  the value.
     *
     * @return The angle on the dial.
     */
    public double valueToAngle(double value) {
        value = value - this.range.getLowerBound();
        double baseAngle = 180 + ((this.meterAngle - 180) / 2);
        return baseAngle - ((value / this.range.getLength()) * this.meterAngle);
    }

    /**
     * Draws the ticks that subdivide the overall range.
     *
     * @param g2  the graphics device.
     * @param meterArea  the meter area.
     * @param minValue  the minimum value.
     * @param maxValue  the maximum value.
     */
    protected void drawTicks(Graphics2D g2, Rectangle2D meterArea,
                             double minValue, double maxValue) {
        for (double v = minValue; v <= maxValue; v += this.tickSize) {
            drawTick(g2, meterArea, v);
        }
    }

    /**
     * Draws a tick.
     *
     * @param g2  the graphics device.
     * @param meterArea  the meter area.
     * @param value  the value.
     */
    protected void drawTick(Graphics2D g2, Rectangle2D meterArea,
            double value) {
        drawTick(g2, meterArea, value, false);
    }

    /**
     * Draws a tick on the dial.
     *
     * @param g2  the graphics device.
     * @param meterArea  the meter area.
     * @param value  the tick value.
     * @param label  a flag that controls whether or not a value label is drawn.
     */
    protected void drawTick(Graphics2D g2, Rectangle2D meterArea,
                            double value, boolean label) {

        double valueAngle = valueToAngle(value);

        double meterMiddleX = meterArea.getCenterX();
        double meterMiddleY = meterArea.getCenterY();

        g2.setPaint(this.tickPaint);
        g2.setStroke(new BasicStroke(2.0f));

        double valueP2X = 0;
        double valueP2Y = 0;

        double radius = (meterArea.getWidth() / 2) + DEFAULT_BORDER_SIZE;
        double radius1 = radius - 15;

        double valueP1X = meterMiddleX
                + (radius * Math.cos(Math.PI * (valueAngle / 180)));
        double valueP1Y = meterMiddleY
                - (radius * Math.sin(Math.PI * (valueAngle / 180)));

        valueP2X = meterMiddleX
                + (radius1 * Math.cos(Math.PI * (valueAngle / 180)));
        valueP2Y = meterMiddleY
                - (radius1 * Math.sin(Math.PI * (valueAngle / 180)));

        Line2D.Double line = new Line2D.Double(valueP1X, valueP1Y, valueP2X,
                valueP2Y);
        g2.draw(line);

        if (this.tickLabelsVisible && label) {

            String tickLabel =  this.tickLabelFormat.format(value);
            g2.setFont(this.tickLabelFont);
            g2.setPaint(this.tickLabelPaint);

            FontMetrics fm = g2.getFontMetrics();
            Rectangle2D tickLabelBounds
                = TextUtilities.getTextBounds(tickLabel, g2, fm);

            double x = valueP2X;
            double y = valueP2Y;
            if (valueAngle == 90 || valueAngle == 270) {
                x = x - tickLabelBounds.getWidth() / 2;
            }
            else if (valueAngle < 90 || valueAngle > 270) {
                x = x - tickLabelBounds.getWidth();
            }
            if ((valueAngle > 135 && valueAngle < 225)
                    || valueAngle > 315 || valueAngle < 45) {
                y = y - tickLabelBounds.getHeight() / 2;
            }
            else {
                y = y + tickLabelBounds.getHeight() / 2;
            }
            g2.drawString(tickLabel, (float) x, (float) y);
        }
    }

    /**
     * Draws the value label just below the center of the dial.
     *
     * @param g2  the graphics device.
     * @param area  the plot area.
     */
    protected void drawValueLabel(Graphics2D g2, Rectangle2D area) {
        g2.setFont(this.valueFont);
        g2.setPaint(this.valuePaint);
        String valueStr = "No value";
        if (this.dataset != null) {
            Number n = this.dataset.getValue();
            if (n != null) {
                valueStr = this.tickLabelFormat.format(n.doubleValue()) + " "
                         + this.units;
            }
        }
        float x = (float) area.getCenterX();
        float y = (float) area.getCenterY() + DEFAULT_CIRCLE_SIZE;
        TextUtilities.drawAlignedString(valueStr, g2, x, y,
                TextAnchor.TOP_CENTER);
    }

    /**
     * Returns a short string describing the type of plot.
     *
     * @return A string describing the type of plot.
     */
    public String getPlotType() {
        return localizationResources.getString("Meter_Plot");
    }

    /**
     * A zoom method that does nothing.  Plots are required to support the
     * zoom operation.  In the case of a meter plot, it doesn't make sense to
     * zoom in or out, so the method is empty.
     *
     * @param percent   The zoom percentage.
     */
    public void zoom(double percent) {
        // intentionally blank
    }

    /**
     * Tests the plot for equality with an arbitrary object.  Note that the
     * dataset is ignored for the purposes of testing equality.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MeterPlot)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        MeterPlot that = (MeterPlot) obj;
        if (!ObjectUtilities.equal(this.units, that.units)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.range, that.range)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.intervals, that.intervals)) {
            return false;
        }
        if (!PaintUtilities.equal(this.dialOutlinePaint,
                that.dialOutlinePaint)) {
            return false;
        }
        if (this.shape != that.shape) {
            return false;
        }
        if (!PaintUtilities.equal(this.dialBackgroundPaint,
                that.dialBackgroundPaint)) {
            return false;
        }
        if (!PaintUtilities.equal(this.needlePaint, that.needlePaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.valueFont, that.valueFont)) {
            return false;
        }
        if (!PaintUtilities.equal(this.valuePaint, that.valuePaint)) {
            return false;
        }
        if (!PaintUtilities.equal(this.tickPaint, that.tickPaint)) {
            return false;
        }
        if (this.tickSize != that.tickSize) {
            return false;
        }
        if (this.tickLabelsVisible != that.tickLabelsVisible) {
            return false;
        }
        if (!ObjectUtilities.equal(this.tickLabelFont, that.tickLabelFont)) {
            return false;
        }
        if (!PaintUtilities.equal(this.tickLabelPaint, that.tickLabelPaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.tickLabelFormat,
                that.tickLabelFormat)) {
            return false;
        }
        if (this.drawBorder != that.drawBorder) {
            return false;
        }
        if (this.meterAngle != that.meterAngle) {
            return false;
        }
        return true;
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint(this.dialBackgroundPaint, stream);
        SerialUtilities.writePaint(this.dialOutlinePaint, stream);
        SerialUtilities.writePaint(this.needlePaint, stream);
        SerialUtilities.writePaint(this.valuePaint, stream);
        SerialUtilities.writePaint(this.tickPaint, stream);
        SerialUtilities.writePaint(this.tickLabelPaint, stream);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.dialBackgroundPaint = SerialUtilities.readPaint(stream);
        this.dialOutlinePaint = SerialUtilities.readPaint(stream);
        this.needlePaint = SerialUtilities.readPaint(stream);
        this.valuePaint = SerialUtilities.readPaint(stream);
        this.tickPaint = SerialUtilities.readPaint(stream);
        this.tickLabelPaint = SerialUtilities.readPaint(stream);
        if (this.dataset != null) {
            this.dataset.addChangeListener(this);
        }
    }

    /**
     * Returns an independent copy (clone) of the plot.  The dataset is NOT
     * cloned - both the original and the clone will have a reference to the
     * same dataset.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if some component of the plot cannot
     *         be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        MeterPlot clone = (MeterPlot) super.clone();
        clone.tickLabelFormat = (NumberFormat) this.tickLabelFormat.clone();
        // the following relies on the fact that the intervals are immutable
        clone.intervals = new java.util.ArrayList(this.intervals);
        if (clone.dataset != null) {
            clone.dataset.addChangeListener(clone);
        }
        return clone;
    }

}
