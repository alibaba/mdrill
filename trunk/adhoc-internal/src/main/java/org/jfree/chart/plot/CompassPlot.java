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
 * ----------------
 * CompassPlot.java
 * ----------------
 * (C) Copyright 2002-2008, by the Australian Antarctic Division and
 * Contributors.
 *
 * Original Author:  Bryan Scott (for the Australian Antarctic Division);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Arnaud Lelievre;
 *
 * Changes:
 * --------
 * 25-Sep-2002 : Version 1, contributed by Bryan Scott (DG);
 * 23-Jan-2003 : Removed one constructor (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 27-Mar-2003 : Changed MeterDataset to ValueDataset (DG);
 * 21-Aug-2003 : Implemented Cloneable (DG);
 * 08-Sep-2003 : Added internationalization via use of properties
 *               resourceBundle (RFE 690236) (AL);
 * 09-Sep-2003 : Changed Color --> Paint (DG);
 * 15-Sep-2003 : Added null data value check (bug report 805009) (DG);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 16-Mar-2004 : Added support for revolutionDistance to enable support for
 *               other units than degrees.
 * 16-Mar-2004 : Enabled LongNeedle to rotate about center.
 * 11-Jan-2005 : Removed deprecated code in preparation for 1.0.0 release (DG);
 * 17-Apr-2005 : Fixed bug in clone() method (DG);
 * 05-May-2005 : Updated draw() method parameters (DG);
 * 08-Jun-2005 : Fixed equals() method to handle GradientPaint (DG);
 * 16-Jun-2005 : Renamed getData() --> getDatasets() and
 *               addData() --> addDataset() (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 20-Mar-2007 : Fixed serialization (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.ResourceBundle;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.needle.ArrowNeedle;
import org.jfree.chart.needle.LineNeedle;
import org.jfree.chart.needle.LongNeedle;
import org.jfree.chart.needle.MeterNeedle;
import org.jfree.chart.needle.MiddlePinNeedle;
import org.jfree.chart.needle.PinNeedle;
import org.jfree.chart.needle.PlumNeedle;
import org.jfree.chart.needle.PointerNeedle;
import org.jfree.chart.needle.ShipNeedle;
import org.jfree.chart.needle.WindNeedle;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;

/**
 * A specialised plot that draws a compass to indicate a direction based on the
 * value from a {@link ValueDataset}.
 */
public class CompassPlot extends Plot implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 6924382802125527395L;

    /** The default label font. */
    public static final Font DEFAULT_LABEL_FONT = new Font("SansSerif",
            Font.BOLD, 10);

    /** A constant for the label type. */
    public static final int NO_LABELS = 0;

    /** A constant for the label type. */
    public static final int VALUE_LABELS = 1;

    /** The label type (NO_LABELS, VALUE_LABELS). */
    private int labelType;

    /** The label font. */
    private Font labelFont;

    /** A flag that controls whether or not a border is drawn. */
    private boolean drawBorder = false;

    /** The rose highlight paint. */
    private transient Paint roseHighlightPaint = Color.black;

    /** The rose paint. */
    private transient Paint rosePaint = Color.yellow;

    /** The rose center paint. */
    private transient Paint roseCenterPaint = Color.white;

    /** The compass font. */
    private Font compassFont = new Font("Arial", Font.PLAIN, 10);

    /** A working shape. */
    private transient Ellipse2D circle1;

    /** A working shape. */
    private transient Ellipse2D circle2;

    /** A working area. */
    private transient Area a1;

    /** A working area. */
    private transient Area a2;

    /** A working shape. */
    private transient Rectangle2D rect1;

    /** An array of value datasets. */
    private ValueDataset[] datasets = new ValueDataset[1];

    /** An array of needles. */
    private MeterNeedle[] seriesNeedle = new MeterNeedle[1];

    /** The resourceBundle for the localization. */
    protected static ResourceBundle localizationResources
            = ResourceBundleWrapper.getBundle(
                    "org.jfree.chart.plot.LocalizationBundle");

    /**
     * The count to complete one revolution.  Can be arbitrarily set
     * For degrees (the default) it is 360, for radians this is 2*Pi, etc
     */
    protected double revolutionDistance = 360;

    /**
     * Default constructor.
     */
    public CompassPlot() {
        this(new DefaultValueDataset());
    }

    /**
     * Constructs a new compass plot.
     *
     * @param dataset  the dataset for the plot (<code>null</code> permitted).
     */
    public CompassPlot(ValueDataset dataset) {
        super();
        if (dataset != null) {
            this.datasets[0] = dataset;
            dataset.addChangeListener(this);
        }
        this.circle1 = new Ellipse2D.Double();
        this.circle2 = new Ellipse2D.Double();
        this.rect1   = new Rectangle2D.Double();
        setSeriesNeedle(0);
    }

    /**
     * Returns the label type.  Defined by the constants: {@link #NO_LABELS}
     * and {@link #VALUE_LABELS}.
     *
     * @return The label type.
     *
     * @see #setLabelType(int)
     */
    public int getLabelType() {
        // FIXME: this attribute is never used - deprecate?
        return this.labelType;
    }

    /**
     * Sets the label type (either {@link #NO_LABELS} or {@link #VALUE_LABELS}.
     *
     * @param type  the type.
     *
     * @see #getLabelType()
     */
    public void setLabelType(int type) {
        // FIXME: this attribute is never used - deprecate?
        if ((type != NO_LABELS) && (type != VALUE_LABELS)) {
            throw new IllegalArgumentException(
                    "MeterPlot.setLabelType(int): unrecognised type.");
        }
        if (this.labelType != type) {
            this.labelType = type;
            fireChangeEvent();
        }
    }

    /**
     * Returns the label font.
     *
     * @return The label font.
     *
     * @see #setLabelFont(Font)
     */
    public Font getLabelFont() {
        // FIXME: this attribute is not used - deprecate?
        return this.labelFont;
    }

    /**
     * Sets the label font and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param font  the new label font.
     *
     * @see #getLabelFont()
     */
    public void setLabelFont(Font font) {
        // FIXME: this attribute is not used - deprecate?
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' not allowed.");
        }
        this.labelFont = font;
        fireChangeEvent();
    }

    /**
     * Returns the paint used to fill the outer circle of the compass.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setRosePaint(Paint)
     */
    public Paint getRosePaint() {
        return this.rosePaint;
    }

    /**
     * Sets the paint used to fill the outer circle of the compass,
     * and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getRosePaint()
     */
    public void setRosePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.rosePaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the paint used to fill the inner background area of the
     * compass.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setRoseCenterPaint(Paint)
     */
    public Paint getRoseCenterPaint() {
        return this.roseCenterPaint;
    }

    /**
     * Sets the paint used to fill the inner background area of the compass,
     * and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getRoseCenterPaint()
     */
    public void setRoseCenterPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.roseCenterPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the paint used to draw the circles, symbols and labels on the
     * compass.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setRoseHighlightPaint(Paint)
     */
    public Paint getRoseHighlightPaint() {
        return this.roseHighlightPaint;
    }

    /**
     * Sets the paint used to draw the circles, symbols and labels of the
     * compass, and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getRoseHighlightPaint()
     */
    public void setRoseHighlightPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.roseHighlightPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns a flag that controls whether or not a border is drawn.
     *
     * @return The flag.
     *
     * @see #setDrawBorder(boolean)
     */
    public boolean getDrawBorder() {
        return this.drawBorder;
    }

    /**
     * Sets a flag that controls whether or not a border is drawn.
     *
     * @param status  the flag status.
     *
     * @see #getDrawBorder()
     */
    public void setDrawBorder(boolean status) {
        this.drawBorder = status;
        fireChangeEvent();
    }

    /**
     * Sets the series paint.
     *
     * @param series  the series index.
     * @param paint  the paint.
     *
     * @see #setSeriesOutlinePaint(int, Paint)
     */
    public void setSeriesPaint(int series, Paint paint) {
       // super.setSeriesPaint(series, paint);
        if ((series >= 0) && (series < this.seriesNeedle.length)) {
            this.seriesNeedle[series].setFillPaint(paint);
        }
    }

    /**
     * Sets the series outline paint.
     *
     * @param series  the series index.
     * @param p  the paint.
     *
     * @see #setSeriesPaint(int, Paint)
     */
    public void setSeriesOutlinePaint(int series, Paint p) {

        if ((series >= 0) && (series < this.seriesNeedle.length)) {
            this.seriesNeedle[series].setOutlinePaint(p);
        }

    }

    /**
     * Sets the series outline stroke.
     *
     * @param series  the series index.
     * @param stroke  the stroke.
     *
     * @see #setSeriesOutlinePaint(int, Paint)
     */
    public void setSeriesOutlineStroke(int series, Stroke stroke) {

        if ((series >= 0) && (series < this.seriesNeedle.length)) {
            this.seriesNeedle[series].setOutlineStroke(stroke);
        }

    }

    /**
     * Sets the needle type.
     *
     * @param type  the type.
     *
     * @see #setSeriesNeedle(int, int)
     */
    public void setSeriesNeedle(int type) {
        setSeriesNeedle(0, type);
    }

    /**
     * Sets the needle for a series.  The needle type is one of the following:
     * <ul>
     * <li>0 = {@link ArrowNeedle};</li>
     * <li>1 = {@link LineNeedle};</li>
     * <li>2 = {@link LongNeedle};</li>
     * <li>3 = {@link PinNeedle};</li>
     * <li>4 = {@link PlumNeedle};</li>
     * <li>5 = {@link PointerNeedle};</li>
     * <li>6 = {@link ShipNeedle};</li>
     * <li>7 = {@link WindNeedle};</li>
     * <li>8 = {@link ArrowNeedle};</li>
     * <li>9 = {@link MiddlePinNeedle};</li>
     * </ul>
     * @param index  the series index.
     * @param type  the needle type.
     *
     * @see #setSeriesNeedle(int)
     */
    public void setSeriesNeedle(int index, int type) {
        switch (type) {
            case 0:
                setSeriesNeedle(index, new ArrowNeedle(true));
                setSeriesPaint(index, Color.red);
                this.seriesNeedle[index].setHighlightPaint(Color.white);
                break;
            case 1:
                setSeriesNeedle(index, new LineNeedle());
                break;
            case 2:
                MeterNeedle longNeedle = new LongNeedle();
                longNeedle.setRotateY(0.5);
                setSeriesNeedle(index, longNeedle);
                break;
            case 3:
                setSeriesNeedle(index, new PinNeedle());
                break;
            case 4:
                setSeriesNeedle(index, new PlumNeedle());
                break;
            case 5:
                setSeriesNeedle(index, new PointerNeedle());
                break;
            case 6:
                setSeriesPaint(index, null);
                setSeriesOutlineStroke(index, new BasicStroke(3));
                setSeriesNeedle(index, new ShipNeedle());
                break;
            case 7:
                setSeriesPaint(index, Color.blue);
                setSeriesNeedle(index, new WindNeedle());
                break;
            case 8:
                setSeriesNeedle(index, new ArrowNeedle(true));
                break;
            case 9:
                setSeriesNeedle(index, new MiddlePinNeedle());
                break;

            default:
                throw new IllegalArgumentException("Unrecognised type.");
        }

    }

    /**
     * Sets the needle for a series and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param index  the series index.
     * @param needle  the needle.
     */
    public void setSeriesNeedle(int index, MeterNeedle needle) {
        if ((needle != null) && (index < this.seriesNeedle.length)) {
            this.seriesNeedle[index] = needle;
        }
        fireChangeEvent();
    }

    /**
     * Returns an array of dataset references for the plot.
     *
     * @return The dataset for the plot, cast as a ValueDataset.
     *
     * @see #addDataset(ValueDataset)
     */
    public ValueDataset[] getDatasets() {
        return this.datasets;
    }

    /**
     * Adds a dataset to the compass.
     *
     * @param dataset  the new dataset (<code>null</code> ignored).
     *
     * @see #addDataset(ValueDataset, MeterNeedle)
     */
    public void addDataset(ValueDataset dataset) {
        addDataset(dataset, null);
    }

    /**
     * Adds a dataset to the compass.
     *
     * @param dataset  the new dataset (<code>null</code> ignored).
     * @param needle  the needle (<code>null</code> permitted).
     */
    public void addDataset(ValueDataset dataset, MeterNeedle needle) {

        if (dataset != null) {
            int i = this.datasets.length + 1;
            ValueDataset[] t = new ValueDataset[i];
            MeterNeedle[] p = new MeterNeedle[i];
            i = i - 2;
            for (; i >= 0; --i) {
                t[i] = this.datasets[i];
                p[i] = this.seriesNeedle[i];
            }
            i = this.datasets.length;
            t[i] = dataset;
            p[i] = ((needle != null) ? needle : p[i - 1]);

            ValueDataset[] a = this.datasets;
            MeterNeedle[] b = this.seriesNeedle;
            this.datasets = t;
            this.seriesNeedle = p;

            for (--i; i >= 0; --i) {
                a[i] = null;
                b[i] = null;
            }
            dataset.addChangeListener(this);
        }
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

        int outerRadius = 0;
        int innerRadius = 0;
        int x1, y1, x2, y2;
        double a;

        if (info != null) {
            info.setPlotArea(area);
        }

        // adjust for insets...
        RectangleInsets insets = getInsets();
        insets.trim(area);

        // draw the background
        if (this.drawBorder) {
            drawBackground(g2, area);
        }

        int midX = (int) (area.getWidth() / 2);
        int midY = (int) (area.getHeight() / 2);
        int radius = midX;
        if (midY < midX) {
            radius = midY;
        }
        --radius;
        int diameter = 2 * radius;

        midX += (int) area.getMinX();
        midY += (int) area.getMinY();

        this.circle1.setFrame(midX - radius, midY - radius, diameter, diameter);
        this.circle2.setFrame(
            midX - radius + 15, midY - radius + 15,
            diameter - 30, diameter - 30
        );
        g2.setPaint(this.rosePaint);
        this.a1 = new Area(this.circle1);
        this.a2 = new Area(this.circle2);
        this.a1.subtract(this.a2);
        g2.fill(this.a1);

        g2.setPaint(this.roseCenterPaint);
        x1 = diameter - 30;
        g2.fillOval(midX - radius + 15, midY - radius + 15, x1, x1);
        g2.setPaint(this.roseHighlightPaint);
        g2.drawOval(midX - radius, midY - radius, diameter, diameter);
        x1 = diameter - 20;
        g2.drawOval(midX - radius + 10, midY - radius + 10, x1, x1);
        x1 = diameter - 30;
        g2.drawOval(midX - radius + 15, midY - radius + 15, x1, x1);
        x1 = diameter - 80;
        g2.drawOval(midX - radius + 40, midY - radius + 40, x1, x1);

        outerRadius = radius - 20;
        innerRadius = radius - 32;
        for (int w = 0; w < 360; w += 15) {
            a = Math.toRadians(w);
            x1 = midX - ((int) (Math.sin(a) * innerRadius));
            x2 = midX - ((int) (Math.sin(a) * outerRadius));
            y1 = midY - ((int) (Math.cos(a) * innerRadius));
            y2 = midY - ((int) (Math.cos(a) * outerRadius));
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setPaint(this.roseHighlightPaint);
        innerRadius = radius - 26;
        outerRadius = 7;
        for (int w = 45; w < 360; w += 90) {
            a = Math.toRadians(w);
            x1 = midX - ((int) (Math.sin(a) * innerRadius));
            y1 = midY - ((int) (Math.cos(a) * innerRadius));
            g2.fillOval(x1 - outerRadius, y1 - outerRadius, 2 * outerRadius,
                    2 * outerRadius);
        }

        /// Squares
        for (int w = 0; w < 360; w += 90) {
            a = Math.toRadians(w);
            x1 = midX - ((int) (Math.sin(a) * innerRadius));
            y1 = midY - ((int) (Math.cos(a) * innerRadius));

            Polygon p = new Polygon();
            p.addPoint(x1 - outerRadius, y1);
            p.addPoint(x1, y1 + outerRadius);
            p.addPoint(x1 + outerRadius, y1);
            p.addPoint(x1, y1 - outerRadius);
            g2.fillPolygon(p);
        }

        /// Draw N, S, E, W
        innerRadius = radius - 42;
        Font f = getCompassFont(radius);
        g2.setFont(f);
        g2.drawString("N", midX - 5, midY - innerRadius + f.getSize());
        g2.drawString("S", midX - 5, midY + innerRadius - 5);
        g2.drawString("W", midX - innerRadius + 5, midY + 5);
        g2.drawString("E", midX + innerRadius - f.getSize(), midY + 5);

        // plot the data (unless the dataset is null)...
        y1 = radius / 2;
        x1 = radius / 6;
        Rectangle2D needleArea = new Rectangle2D.Double(
            (midX - x1), (midY - y1), (2 * x1), (2 * y1)
        );
        int x = this.seriesNeedle.length;
        int current = 0;
        double value = 0;
        int i = (this.datasets.length - 1);
        for (; i >= 0; --i) {
            ValueDataset data = this.datasets[i];

            if (data != null && data.getValue() != null) {
                value = (data.getValue().doubleValue())
                    % this.revolutionDistance;
                value = value / this.revolutionDistance * 360;
                current = i % x;
                this.seriesNeedle[current].draw(g2, needleArea, value);
            }
        }

        if (this.drawBorder) {
            drawOutline(g2, area);
        }

    }

    /**
     * Returns a short string describing the type of plot.
     *
     * @return A string describing the plot.
     */
    public String getPlotType() {
        return localizationResources.getString("Compass_Plot");
    }

    /**
     * Returns the legend items for the plot.  For now, no legend is available
     * - this method returns null.
     *
     * @return The legend items.
     */
    public LegendItemCollection getLegendItems() {
        return null;
    }

    /**
     * No zooming is implemented for compass plot, so this method is empty.
     *
     * @param percent  the zoom amount.
     */
    public void zoom(double percent) {
        // no zooming possible
    }

    /**
     * Returns the font for the compass, adjusted for the size of the plot.
     *
     * @param radius the radius.
     *
     * @return The font.
     */
    protected Font getCompassFont(int radius) {
        float fontSize = radius / 10.0f;
        if (fontSize < 8) {
            fontSize = 8;
        }
        Font newFont = this.compassFont.deriveFont(fontSize);
        return newFont;
    }

    /**
     * Tests an object for equality with this plot.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CompassPlot)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        CompassPlot that = (CompassPlot) obj;
        if (this.labelType != that.labelType) {
            return false;
        }
        if (!ObjectUtilities.equal(this.labelFont, that.labelFont)) {
            return false;
        }
        if (this.drawBorder != that.drawBorder) {
            return false;
        }
        if (!PaintUtilities.equal(this.roseHighlightPaint,
                that.roseHighlightPaint)) {
            return false;
        }
        if (!PaintUtilities.equal(this.rosePaint, that.rosePaint)) {
            return false;
        }
        if (!PaintUtilities.equal(this.roseCenterPaint,
                that.roseCenterPaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.compassFont, that.compassFont)) {
            return false;
        }
        if (!Arrays.equals(this.seriesNeedle, that.seriesNeedle)) {
            return false;
        }
        if (getRevolutionDistance() != that.getRevolutionDistance()) {
            return false;
        }
        return true;

    }

    /**
     * Returns a clone of the plot.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  this class will not throw this
     *         exception, but subclasses (if any) might.
     */
    public Object clone() throws CloneNotSupportedException {

        CompassPlot clone = (CompassPlot) super.clone();
        if (this.circle1 != null) {
            clone.circle1 = (Ellipse2D) this.circle1.clone();
        }
        if (this.circle2 != null) {
            clone.circle2 = (Ellipse2D) this.circle2.clone();
        }
        if (this.a1 != null) {
            clone.a1 = (Area) this.a1.clone();
        }
        if (this.a2 != null) {
            clone.a2 = (Area) this.a2.clone();
        }
        if (this.rect1 != null) {
            clone.rect1 = (Rectangle2D) this.rect1.clone();
        }
        clone.datasets = (ValueDataset[]) this.datasets.clone();
        clone.seriesNeedle = (MeterNeedle[]) this.seriesNeedle.clone();

        // clone share data sets => add the clone as listener to the dataset
        for (int i = 0; i < this.datasets.length; ++i) {
            if (clone.datasets[i] != null) {
                clone.datasets[i].addChangeListener(clone);
            }
        }
        return clone;

    }

    /**
     * Sets the count to complete one revolution.  Can be arbitrarily set
     * For degrees (the default) it is 360, for radians this is 2*Pi, etc
     *
     * @param size the count to complete one revolution.
     *
     * @see #getRevolutionDistance()
     */
    public void setRevolutionDistance(double size) {
        if (size > 0) {
            this.revolutionDistance = size;
        }
    }

    /**
     * Gets the count to complete one revolution.
     *
     * @return The count to complete one revolution.
     *
     * @see #setRevolutionDistance(double)
     */
    public double getRevolutionDistance() {
        return this.revolutionDistance;
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
        SerialUtilities.writePaint(this.rosePaint, stream);
        SerialUtilities.writePaint(this.roseCenterPaint, stream);
        SerialUtilities.writePaint(this.roseHighlightPaint, stream);
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
        this.rosePaint = SerialUtilities.readPaint(stream);
        this.roseCenterPaint = SerialUtilities.readPaint(stream);
        this.roseHighlightPaint = SerialUtilities.readPaint(stream);
    }

}
