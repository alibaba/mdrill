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
 * --------------------
 * ThermometerPlot.java
 * --------------------
 *
 * (C) Copyright 2000-2008, by Bryan Scott and Contributors.
 *
 * Original Author:  Bryan Scott (based on MeterPlot by Hari).
 * Contributor(s):   David Gilbert (for Object Refinery Limited).
 *                   Arnaud Lelievre;
 *                   Julien Henry (see patch 1769088) (DG);
 *
 * Changes
 * -------
 * 11-Apr-2002 : Version 1, contributed by Bryan Scott;
 * 15-Apr-2002 : Changed to implement VerticalValuePlot;
 * 29-Apr-2002 : Added getVerticalValueAxis() method (DG);
 * 25-Jun-2002 : Removed redundant imports (DG);
 * 17-Sep-2002 : Reviewed with Checkstyle utility (DG);
 * 18-Sep-2002 : Extensive changes made to API, to iron out bugs and
 *               inconsistencies (DG);
 * 13-Oct-2002 : Corrected error datasetChanged which would generate exceptions
 *               when value set to null (BRS).
 * 23-Jan-2003 : Removed one constructor (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 02-Jun-2003 : Removed test for compatible range axis (DG);
 * 01-Jul-2003 : Added additional check in draw method to ensure value not
 *               null (BRS);
 * 08-Sep-2003 : Added internationalization via use of properties
 *               resourceBundle (RFE 690236) (AL);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 29-Sep-2003 : Updated draw to set value of cursor to non-zero and allow
 *               painting of axis.  An incomplete fix and needs to be set for
 *               left or right drawing (BRS);
 * 19-Nov-2003 : Added support for value labels to be displayed left of the
 *               thermometer
 * 19-Nov-2003 : Improved axis drawing (now default axis does not draw axis line
 *               and is closer to the bulb).  Added support for the positioning
 *               of the axis to the left or right of the bulb. (BRS);
 * 03-Dec-2003 : Directly mapped deprecated setData()/getData() method to
 *               get/setDataset() (TM);
 * 21-Jan-2004 : Update for renamed method in ValueAxis (DG);
 * 07-Apr-2004 : Changed string width calculation (DG);
 * 12-Nov-2004 : Implemented the new Zoomable interface (DG);
 * 06-Jan-2004 : Added getOrientation() method (DG);
 * 11-Jan-2005 : Removed deprecated code in preparation for 1.0.0 release (DG);
 * 29-Mar-2005 : Fixed equals() method (DG);
 * 05-May-2005 : Updated draw() method parameters (DG);
 * 09-Jun-2005 : Fixed more bugs in equals() method (DG);
 * 10-Jun-2005 : Fixed minor bug in setDisplayRange() method (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 14-Nov-2006 : Fixed margin when drawing (DG);
 * 03-May-2007 : Fixed datasetChanged() to handle null dataset, added null
 *               argument check and event notification to setRangeAxis(),
 *               added null argument check to setPadding(), setValueFont(),
 *               setValuePaint(), setValueFormat() and setMercuryPaint(),
 *               deprecated get/setShowValueLines(), deprecated
 *               getMinimum/MaximumVerticalDataValue(), and fixed serialization
 *               bug (DG);
 * 24-Sep-2007 : Implemented new methods in Zoomable interface (DG);
 * 08-Oct-2007 : Added attributes for thermometer dimensions - see patch 1769088
 *               by Julien Henry (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.ResourceBundle;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.UnitType;

/**
 * A plot that displays a single value (from a {@link ValueDataset}) in a
 * thermometer type display.
 * <p>
 * This plot supports a number of options:
 * <ol>
 * <li>three sub-ranges which could be viewed as 'Normal', 'Warning'
 *   and 'Critical' ranges.</li>
 * <li>the thermometer can be run in two modes:
 *      <ul>
 *      <li>fixed range, or</li>
 *      <li>range adjusts to current sub-range.</li>
 *      </ul>
 * </li>
 * <li>settable units to be displayed.</li>
 * <li>settable display location for the value text.</li>
 * </ol>
 */
public class ThermometerPlot extends Plot implements ValueAxisPlot,
        Zoomable, Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 4087093313147984390L;

    /** A constant for unit type 'None'. */
    public static final int UNITS_NONE = 0;

    /** A constant for unit type 'Fahrenheit'. */
    public static final int UNITS_FAHRENHEIT = 1;

    /** A constant for unit type 'Celcius'. */
    public static final int UNITS_CELCIUS = 2;

    /** A constant for unit type 'Kelvin'. */
    public static final int UNITS_KELVIN = 3;

    /** A constant for the value label position (no label). */
    public static final int NONE = 0;

    /** A constant for the value label position (right of the thermometer). */
    public static final int RIGHT = 1;

    /** A constant for the value label position (left of the thermometer). */
    public static final int LEFT = 2;

    /** A constant for the value label position (in the thermometer bulb). */
    public static final int BULB = 3;

    /** A constant for the 'normal' range. */
    public static final int NORMAL = 0;

    /** A constant for the 'warning' range. */
    public static final int WARNING = 1;

    /** A constant for the 'critical' range. */
    public static final int CRITICAL = 2;

    /**
     * The bulb radius.
     *
     * @deprecated As of 1.0.7, use {@link #getBulbRadius()}.
     */
    protected static final int BULB_RADIUS = 40;

    /**
     * The bulb diameter.
     *
     * @deprecated As of 1.0.7, use {@link #getBulbDiameter()}.
     */
    protected static final int BULB_DIAMETER = BULB_RADIUS * 2;

    /**
     * The column radius.
     *
     * @deprecated As of 1.0.7, use {@link #getColumnRadius()}.
     */
    protected static final int COLUMN_RADIUS = 20;

    /**
     * The column diameter.
     *
     * @deprecated As of 1.0.7, use {@link #getColumnDiameter()}.
     */
    protected static final int COLUMN_DIAMETER = COLUMN_RADIUS * 2;

    /**
     * The gap radius.
     *
     * @deprecated As of 1.0.7, use {@link #getGap()}.
     */
    protected static final int GAP_RADIUS = 5;

    /**
     * The gap diameter.
     *
     * @deprecated As of 1.0.7, use {@link #getGap()} times two.
     */
    protected static final int GAP_DIAMETER = GAP_RADIUS * 2;

    /** The axis gap. */
    protected static final int AXIS_GAP = 10;

    /** The unit strings. */
    protected static final String[] UNITS = {"", "\u00B0F", "\u00B0C",
            "\u00B0K"};

    /** Index for low value in subrangeInfo matrix. */
    protected static final int RANGE_LOW = 0;

    /** Index for high value in subrangeInfo matrix. */
    protected static final int RANGE_HIGH = 1;

    /** Index for display low value in subrangeInfo matrix. */
    protected static final int DISPLAY_LOW = 2;

    /** Index for display high value in subrangeInfo matrix. */
    protected static final int DISPLAY_HIGH = 3;

    /** The default lower bound. */
    protected static final double DEFAULT_LOWER_BOUND = 0.0;

    /** The default upper bound. */
    protected static final double DEFAULT_UPPER_BOUND = 100.0;

    /**
     * The default bulb radius.
     *
     * @since 1.0.7
     */
    protected static final int DEFAULT_BULB_RADIUS = 40;

    /**
     * The default column radius.
     *
     * @since 1.0.7
     */
    protected static final int DEFAULT_COLUMN_RADIUS = 20;

    /**
     * The default gap between the outlines representing the thermometer.
     *
     * @since 1.0.7
     */
    protected static final int DEFAULT_GAP = 5;

    /** The dataset for the plot. */
    private ValueDataset dataset;

    /** The range axis. */
    private ValueAxis rangeAxis;

    /** The lower bound for the thermometer. */
    private double lowerBound = DEFAULT_LOWER_BOUND;

    /** The upper bound for the thermometer. */
    private double upperBound = DEFAULT_UPPER_BOUND;

    /**
     * The value label position.
     *
     * @since 1.0.7
     */
    private int bulbRadius = DEFAULT_BULB_RADIUS;

    /**
     * The column radius.
     *
     * @since 1.0.7
     */
    private int columnRadius = DEFAULT_COLUMN_RADIUS;

    /**
     * The gap between the two outlines the represent the thermometer.
     *
     * @since 1.0.7
     */
    private int gap = DEFAULT_GAP;

    /**
     * Blank space inside the plot area around the outside of the thermometer.
     */
    private RectangleInsets padding;

    /** Stroke for drawing the thermometer */
    private transient Stroke thermometerStroke = new BasicStroke(1.0f);

    /** Paint for drawing the thermometer */
    private transient Paint thermometerPaint = Color.black;

    /** The display units */
    private int units = UNITS_CELCIUS;

    /** The value label position. */
    private int valueLocation = BULB;

    /** The position of the axis **/
    private int axisLocation = LEFT;

    /** The font to write the value in */
    private Font valueFont = new Font("SansSerif", Font.BOLD, 16);

    /** Colour that the value is written in */
    private transient Paint valuePaint = Color.white;

    /** Number format for the value */
    private NumberFormat valueFormat = new DecimalFormat();

    /** The default paint for the mercury in the thermometer. */
    private transient Paint mercuryPaint = Color.lightGray;

    /** A flag that controls whether value lines are drawn. */
    private boolean showValueLines = false;

    /** The display sub-range. */
    private int subrange = -1;

    /** The start and end values for the subranges. */
    private double[][] subrangeInfo = {
        {0.0, 50.0, 0.0, 50.0},
        {50.0, 75.0, 50.0, 75.0},
        {75.0, 100.0, 75.0, 100.0}
    };

    /**
     * A flag that controls whether or not the axis range adjusts to the
     * sub-ranges.
     */
    private boolean followDataInSubranges = false;

    /**
     * A flag that controls whether or not the mercury paint changes with
     * the subranges.
     */
    private boolean useSubrangePaint = true;

    /** Paint for each range */
    private transient Paint[] subrangePaint = {Color.green, Color.orange,
            Color.red};

    /** A flag that controls whether the sub-range indicators are visible. */
    private boolean subrangeIndicatorsVisible = true;

    /** The stroke for the sub-range indicators. */
    private transient Stroke subrangeIndicatorStroke = new BasicStroke(2.0f);

    /** The range indicator stroke. */
    private transient Stroke rangeIndicatorStroke = new BasicStroke(3.0f);

    /** The resourceBundle for the localization. */
    protected static ResourceBundle localizationResources
            = ResourceBundleWrapper.getBundle(
                    "org.jfree.chart.plot.LocalizationBundle");

    /**
     * Creates a new thermometer plot.
     */
    public ThermometerPlot() {
        this(new DefaultValueDataset());
    }

    /**
     * Creates a new thermometer plot, using default attributes where necessary.
     *
     * @param dataset  the data set.
     */
    public ThermometerPlot(ValueDataset dataset) {

        super();

        this.padding = new RectangleInsets(UnitType.RELATIVE, 0.05, 0.05, 0.05,
                0.05);
        this.dataset = dataset;
        if (dataset != null) {
            dataset.addChangeListener(this);
        }
        NumberAxis axis = new NumberAxis(null);
        axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        axis.setAxisLineVisible(false);
        axis.setPlot(this);
        axis.addChangeListener(this);
        this.rangeAxis = axis;
        setAxisRange();
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
     * is one, and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @see #getDataset()
     */
    public void setDataset(ValueDataset dataset) {

        // if there is an existing dataset, remove the plot from the list
        // of change listeners...
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
     * Returns the range axis.
     *
     * @return The range axis (never <code>null</code>).
     *
     * @see #setRangeAxis(ValueAxis)
     */
    public ValueAxis getRangeAxis() {
        return this.rangeAxis;
    }

    /**
     * Sets the range axis for the plot and sends a {@link PlotChangeEvent} to
     * all registered listeners.
     *
     * @param axis  the new axis (<code>null</code> not permitted).
     *
     * @see #getRangeAxis()
     */
    public void setRangeAxis(ValueAxis axis) {
        if (axis == null) {
            throw new IllegalArgumentException("Null 'axis' argument.");
        }
        // plot is registered as a listener with the existing axis...
        this.rangeAxis.removeChangeListener(this);

        axis.setPlot(this);
        axis.addChangeListener(this);
        this.rangeAxis = axis;
        fireChangeEvent();
    }

    /**
     * Returns the lower bound for the thermometer.  The data value can be set
     * lower than this, but it will not be shown in the thermometer.
     *
     * @return The lower bound.
     *
     * @see #setLowerBound(double)
     */
    public double getLowerBound() {
        return this.lowerBound;
    }

    /**
     * Sets the lower bound for the thermometer.
     *
     * @param lower the lower bound.
     *
     * @see #getLowerBound()
     */
    public void setLowerBound(double lower) {
        this.lowerBound = lower;
        setAxisRange();
    }

    /**
     * Returns the upper bound for the thermometer.  The data value can be set
     * higher than this, but it will not be shown in the thermometer.
     *
     * @return The upper bound.
     *
     * @see #setUpperBound(double)
     */
    public double getUpperBound() {
        return this.upperBound;
    }

    /**
     * Sets the upper bound for the thermometer.
     *
     * @param upper the upper bound.
     *
     * @see #getUpperBound()
     */
    public void setUpperBound(double upper) {
        this.upperBound = upper;
        setAxisRange();
    }

    /**
     * Sets the lower and upper bounds for the thermometer.
     *
     * @param lower  the lower bound.
     * @param upper  the upper bound.
     */
    public void setRange(double lower, double upper) {
        this.lowerBound = lower;
        this.upperBound = upper;
        setAxisRange();
    }

    /**
     * Returns the padding for the thermometer.  This is the space inside the
     * plot area.
     *
     * @return The padding (never <code>null</code>).
     *
     * @see #setPadding(RectangleInsets)
     */
    public RectangleInsets getPadding() {
        return this.padding;
    }

    /**
     * Sets the padding for the thermometer and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     *
     * @param padding  the padding (<code>null</code> not permitted).
     *
     * @see #getPadding()
     */
    public void setPadding(RectangleInsets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("Null 'padding' argument.");
        }
        this.padding = padding;
        fireChangeEvent();
    }

    /**
     * Returns the stroke used to draw the thermometer outline.
     *
     * @return The stroke (never <code>null</code>).
     *
     * @see #setThermometerStroke(Stroke)
     * @see #getThermometerPaint()
     */
    public Stroke getThermometerStroke() {
        return this.thermometerStroke;
    }

    /**
     * Sets the stroke used to draw the thermometer outline and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param s  the new stroke (<code>null</code> ignored).
     *
     * @see #getThermometerStroke()
     */
    public void setThermometerStroke(Stroke s) {
        if (s != null) {
            this.thermometerStroke = s;
            fireChangeEvent();
        }
    }

    /**
     * Returns the paint used to draw the thermometer outline.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setThermometerPaint(Paint)
     * @see #getThermometerStroke()
     */
    public Paint getThermometerPaint() {
        return this.thermometerPaint;
    }

    /**
     * Sets the paint used to draw the thermometer outline and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  the new paint (<code>null</code> ignored).
     *
     * @see #getThermometerPaint()
     */
    public void setThermometerPaint(Paint paint) {
        if (paint != null) {
            this.thermometerPaint = paint;
            fireChangeEvent();
        }
    }

    /**
     * Returns a code indicating the unit display type.  This is one of
     * {@link #UNITS_NONE}, {@link #UNITS_FAHRENHEIT}, {@link #UNITS_CELCIUS}
     * and {@link #UNITS_KELVIN}.
     *
     * @return The units type.
     *
     * @see #setUnits(int)
     */
    public int getUnits() {
        return this.units;
    }

    /**
     * Sets the units to be displayed in the thermometer. Use one of the
     * following constants:
     *
     * <ul>
     * <li>UNITS_NONE : no units displayed.</li>
     * <li>UNITS_FAHRENHEIT : units displayed in Fahrenheit.</li>
     * <li>UNITS_CELCIUS : units displayed in Celcius.</li>
     * <li>UNITS_KELVIN : units displayed in Kelvin.</li>
     * </ul>
     *
     * @param u  the new unit type.
     *
     * @see #getUnits()
     */
    public void setUnits(int u) {
        if ((u >= 0) && (u < UNITS.length)) {
            if (this.units != u) {
                this.units = u;
                fireChangeEvent();
            }
        }
    }

    /**
     * Sets the unit type.
     *
     * @param u  the unit type (<code>null</code> ignored).
     *
     * @deprecated Use setUnits(int) instead.  Deprecated as of version 1.0.6,
     *     because this method is a little obscure and redundant anyway.
     */
    public void setUnits(String u) {
        if (u == null) {
            return;
        }

        u = u.toUpperCase().trim();
        for (int i = 0; i < UNITS.length; ++i) {
            if (u.equals(UNITS[i].toUpperCase().trim())) {
                setUnits(i);
                i = UNITS.length;
            }
        }
    }

    /**
     * Returns a code indicating the location at which the value label is
     * displayed.
     *
     * @return The location (one of {@link #NONE}, {@link #RIGHT},
     *         {@link #LEFT} and {@link #BULB}.).
     */
    public int getValueLocation() {
        return this.valueLocation;
    }

    /**
     * Sets the location at which the current value is displayed and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     * <P>
     * The location can be one of the constants:
     * <code>NONE</code>,
     * <code>RIGHT</code>
     * <code>LEFT</code> and
     * <code>BULB</code>.
     *
     * @param location  the location.
     */
    public void setValueLocation(int location) {
        if ((location >= 0) && (location < 4)) {
            this.valueLocation = location;
            fireChangeEvent();
        }
        else {
            throw new IllegalArgumentException("Location not recognised.");
        }
    }

    /**
     * Returns the axis location.
     *
     * @return The location (one of {@link #NONE}, {@link #LEFT} and
     *         {@link #RIGHT}).
     *
     * @see #setAxisLocation(int)
     */
    public int getAxisLocation() {
        return this.axisLocation;
    }

    /**
     * Sets the location at which the axis is displayed relative to the
     * thermometer, and sends a {@link PlotChangeEvent} to all registered
     * listeners.
     *
     * @param location  the location (one of {@link #NONE}, {@link #LEFT} and
     *         {@link #RIGHT}).
     *
     * @see #getAxisLocation()
     */
    public void setAxisLocation(int location) {
        if ((location >= 0) && (location < 3)) {
            this.axisLocation = location;
            fireChangeEvent();
        }
        else {
            throw new IllegalArgumentException("Location not recognised.");
        }
    }

    /**
     * Gets the font used to display the current value.
     *
     * @return The font.
     *
     * @see #setValueFont(Font)
     */
    public Font getValueFont() {
        return this.valueFont;
    }

    /**
     * Sets the font used to display the current value.
     *
     * @param f  the new font (<code>null</code> not permitted).
     *
     * @see #getValueFont()
     */
    public void setValueFont(Font f) {
        if (f == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }
        if (!this.valueFont.equals(f)) {
            this.valueFont = f;
            fireChangeEvent();
        }
    }

    /**
     * Gets the paint used to display the current value.
    *
     * @return The paint.
     *
     * @see #setValuePaint(Paint)
     */
    public Paint getValuePaint() {
        return this.valuePaint;
    }

    /**
     * Sets the paint used to display the current value and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  the new paint (<code>null</code> not permitted).
     *
     * @see #getValuePaint()
     */
    public void setValuePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        if (!this.valuePaint.equals(paint)) {
            this.valuePaint = paint;
            fireChangeEvent();
        }
    }

    // FIXME: No getValueFormat() method?

    /**
     * Sets the formatter for the value label and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param formatter  the new formatter (<code>null</code> not permitted).
     */
    public void setValueFormat(NumberFormat formatter) {
        if (formatter == null) {
            throw new IllegalArgumentException("Null 'formatter' argument.");
        }
        this.valueFormat = formatter;
        fireChangeEvent();
    }

    /**
     * Returns the default mercury paint.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setMercuryPaint(Paint)
     */
    public Paint getMercuryPaint() {
        return this.mercuryPaint;
    }

    /**
     * Sets the default mercury paint and sends a {@link PlotChangeEvent} to
     * all registered listeners.
     *
     * @param paint  the new paint (<code>null</code> not permitted).
     *
     * @see #getMercuryPaint()
     */
    public void setMercuryPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.mercuryPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether not value lines are displayed.
     *
     * @return The flag.
     *
     * @see #setShowValueLines(boolean)
     *
     * @deprecated This flag doesn't do anything useful/visible.  Deprecated
     *     as of version 1.0.6.
     */
    public boolean getShowValueLines() {
        return this.showValueLines;
    }

    /**
     * Sets the display as to whether to show value lines in the output.
     *
     * @param b Whether to show value lines in the thermometer
     *
     * @see #getShowValueLines()
     *
     * @deprecated This flag doesn't do anything useful/visible.  Deprecated
     *     as of version 1.0.6.
     */
    public void setShowValueLines(boolean b) {
        this.showValueLines = b;
        fireChangeEvent();
    }

    /**
     * Sets information for a particular range.
     *
     * @param range  the range to specify information about.
     * @param low  the low value for the range
     * @param hi  the high value for the range
     */
    public void setSubrangeInfo(int range, double low, double hi) {
        setSubrangeInfo(range, low, hi, low, hi);
    }

    /**
     * Sets the subrangeInfo attribute of the ThermometerPlot object
     *
     * @param range  the new rangeInfo value.
     * @param rangeLow  the new rangeInfo value
     * @param rangeHigh  the new rangeInfo value
     * @param displayLow  the new rangeInfo value
     * @param displayHigh  the new rangeInfo value
     */
    public void setSubrangeInfo(int range,
                                double rangeLow, double rangeHigh,
                                double displayLow, double displayHigh) {

        if ((range >= 0) && (range < 3)) {
            setSubrange(range, rangeLow, rangeHigh);
            setDisplayRange(range, displayLow, displayHigh);
            setAxisRange();
            fireChangeEvent();
        }

    }

    /**
     * Sets the bounds for a subrange.
     *
     * @param range  the range type.
     * @param low  the low value.
     * @param high  the high value.
     */
    public void setSubrange(int range, double low, double high) {
        if ((range >= 0) && (range < 3)) {
            this.subrangeInfo[range][RANGE_HIGH] = high;
            this.subrangeInfo[range][RANGE_LOW] = low;
        }
    }

    /**
     * Sets the displayed bounds for a sub range.
     *
     * @param range  the range type.
     * @param low  the low value.
     * @param high  the high value.
     */
    public void setDisplayRange(int range, double low, double high) {

        if ((range >= 0) && (range < this.subrangeInfo.length)
            && isValidNumber(high) && isValidNumber(low)) {

            if (high > low) {
                this.subrangeInfo[range][DISPLAY_HIGH] = high;
                this.subrangeInfo[range][DISPLAY_LOW] = low;
            }
            else {
                this.subrangeInfo[range][DISPLAY_HIGH] = low;
                this.subrangeInfo[range][DISPLAY_LOW] = high;
            }

        }

    }

    /**
     * Gets the paint used for a particular subrange.
     *
     * @param range  the range (.
     *
     * @return The paint.
     *
     * @see #setSubrangePaint(int, Paint)
     */
    public Paint getSubrangePaint(int range) {
        if ((range >= 0) && (range < this.subrangePaint.length)) {
            return this.subrangePaint[range];
        }
        else {
            return this.mercuryPaint;
        }
    }

    /**
     * Sets the paint to be used for a subrange and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param range  the range (0, 1 or 2).
     * @param paint  the paint to be applied (<code>null</code> not permitted).
     *
     * @see #getSubrangePaint(int)
     */
    public void setSubrangePaint(int range, Paint paint) {
        if ((range >= 0)
                && (range < this.subrangePaint.length) && (paint != null)) {
            this.subrangePaint[range] = paint;
            fireChangeEvent();
        }
    }

    /**
     * Returns a flag that controls whether or not the thermometer axis zooms
     * to display the subrange within which the data value falls.
     *
     * @return The flag.
     */
    public boolean getFollowDataInSubranges() {
        return this.followDataInSubranges;
    }

    /**
     * Sets the flag that controls whether or not the thermometer axis zooms
     * to display the subrange within which the data value falls.
     *
     * @param flag  the flag.
     */
    public void setFollowDataInSubranges(boolean flag) {
        this.followDataInSubranges = flag;
        fireChangeEvent();
    }

    /**
     * Returns a flag that controls whether or not the mercury color changes
     * for each subrange.
     *
     * @return The flag.
     *
     * @see #setUseSubrangePaint(boolean)
     */
    public boolean getUseSubrangePaint() {
        return this.useSubrangePaint;
    }

    /**
     * Sets the range colour change option.
     *
     * @param flag the new range colour change option
     *
     * @see #getUseSubrangePaint()
     */
    public void setUseSubrangePaint(boolean flag) {
        this.useSubrangePaint = flag;
        fireChangeEvent();
    }

    /**
     * Returns the bulb radius, in Java2D units.

     * @return The bulb radius.
     *
     * @since 1.0.7
     */
    public int getBulbRadius() {
        return this.bulbRadius;
    }

    /**
     * Sets the bulb radius (in Java2D units) and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param r  the new radius (in Java2D units).
     *
     * @see #getBulbRadius()
     *
     * @since 1.0.7
     */
    public void setBulbRadius(int r) {
        this.bulbRadius = r;
        fireChangeEvent();
    }

    /**
     * Returns the bulb diameter, which is always twice the value returned
     * by {@link #getBulbRadius()}.
     *
     * @return The bulb diameter.
     *
     * @since 1.0.7
     */
    public int getBulbDiameter() {
        return getBulbRadius() * 2;
    }

    /**
     * Returns the column radius, in Java2D units.
     *
     * @return The column radius.
     *
     * @see #setColumnRadius(int)
     *
     * @since 1.0.7
     */
    public int getColumnRadius() {
        return this.columnRadius;
    }

    /**
     * Sets the column radius (in Java2D units) and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param r  the new radius.
     *
     * @see #getColumnRadius()
     *
     * @since 1.0.7
     */
    public void setColumnRadius(int r) {
        this.columnRadius = r;
        fireChangeEvent();
    }

    /**
     * Returns the column diameter, which is always twice the value returned
     * by {@link #getColumnRadius()}.
     *
     * @return The column diameter.
     *
     * @since 1.0.7
     */
    public int getColumnDiameter() {
        return getColumnRadius() * 2;
    }

    /**
     * Returns the gap, in Java2D units, between the two outlines that
     * represent the thermometer.
     *
     * @return The gap.
     *
     * @see #setGap(int)
     *
     * @since 1.0.7
     */
    public int getGap() {
        return this.gap;
    }

    /**
     * Sets the gap (in Java2D units) between the two outlines that represent
     * the thermometer, and sends a {@link PlotChangeEvent} to all registered
     * listeners.
     *
     * @param gap  the new gap.
     *
     * @see #getGap()
     *
     * @since 1.0.7
     */
    public void setGap(int gap) {
        this.gap = gap;
        fireChangeEvent();
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

        RoundRectangle2D outerStem = new RoundRectangle2D.Double();
        RoundRectangle2D innerStem = new RoundRectangle2D.Double();
        RoundRectangle2D mercuryStem = new RoundRectangle2D.Double();
        Ellipse2D outerBulb = new Ellipse2D.Double();
        Ellipse2D innerBulb = new Ellipse2D.Double();
        String temp = null;
        FontMetrics metrics = null;
        if (info != null) {
            info.setPlotArea(area);
        }

        // adjust for insets...
        RectangleInsets insets = getInsets();
        insets.trim(area);
        drawBackground(g2, area);

        // adjust for padding...
        Rectangle2D interior = (Rectangle2D) area.clone();
        this.padding.trim(interior);
        int midX = (int) (interior.getX() + (interior.getWidth() / 2));
        int midY = (int) (interior.getY() + (interior.getHeight() / 2));
        int stemTop = (int) (interior.getMinY() + getBulbRadius());
        int stemBottom = (int) (interior.getMaxY() - getBulbDiameter());
        Rectangle2D dataArea = new Rectangle2D.Double(midX - getColumnRadius(),
                stemTop, getColumnRadius(), stemBottom - stemTop);

        outerBulb.setFrame(midX - getBulbRadius(), stemBottom,
                getBulbDiameter(), getBulbDiameter());

        outerStem.setRoundRect(midX - getColumnRadius(), interior.getMinY(),
                getColumnDiameter(), stemBottom + getBulbDiameter() - stemTop,
                getColumnDiameter(), getColumnDiameter());

        Area outerThermometer = new Area(outerBulb);
        Area tempArea = new Area(outerStem);
        outerThermometer.add(tempArea);

        innerBulb.setFrame(midX - getBulbRadius() + getGap(), stemBottom
                + getGap(), getBulbDiameter() - getGap() * 2, getBulbDiameter()
                - getGap() * 2);

        innerStem.setRoundRect(midX - getColumnRadius() + getGap(),
                interior.getMinY() + getGap(), getColumnDiameter()
                - getGap() * 2, stemBottom + getBulbDiameter() - getGap() * 2
                - stemTop, getColumnDiameter() - getGap() * 2,
                getColumnDiameter() - getGap() * 2);

        Area innerThermometer = new Area(innerBulb);
        tempArea = new Area(innerStem);
        innerThermometer.add(tempArea);

        if ((this.dataset != null) && (this.dataset.getValue() != null)) {
            double current = this.dataset.getValue().doubleValue();
            double ds = this.rangeAxis.valueToJava2D(current, dataArea,
                    RectangleEdge.LEFT);

            int i = getColumnDiameter() - getGap() * 2; // already calculated
            int j = getColumnRadius() - getGap(); // already calculated
            int l = (i / 2);
            int k = (int) Math.round(ds);
            if (k < (getGap() + interior.getMinY())) {
                k = (int) (getGap() + interior.getMinY());
                l = getBulbRadius();
            }

            Area mercury = new Area(innerBulb);

            if (k < (stemBottom + getBulbRadius())) {
                mercuryStem.setRoundRect(midX - j, k, i,
                        (stemBottom + getBulbRadius()) - k, l, l);
                tempArea = new Area(mercuryStem);
                mercury.add(tempArea);
            }

            g2.setPaint(getCurrentPaint());
            g2.fill(mercury);

            // draw range indicators...
            if (this.subrangeIndicatorsVisible) {
                g2.setStroke(this.subrangeIndicatorStroke);
                Range range = this.rangeAxis.getRange();

                // draw start of normal range
                double value = this.subrangeInfo[NORMAL][RANGE_LOW];
                if (range.contains(value)) {
                    double x = midX + getColumnRadius() + 2;
                    double y = this.rangeAxis.valueToJava2D(value, dataArea,
                            RectangleEdge.LEFT);
                    Line2D line = new Line2D.Double(x, y, x + 10, y);
                    g2.setPaint(this.subrangePaint[NORMAL]);
                    g2.draw(line);
                }

                // draw start of warning range
                value = this.subrangeInfo[WARNING][RANGE_LOW];
                if (range.contains(value)) {
                    double x = midX + getColumnRadius() + 2;
                    double y = this.rangeAxis.valueToJava2D(value, dataArea,
                            RectangleEdge.LEFT);
                    Line2D line = new Line2D.Double(x, y, x + 10, y);
                    g2.setPaint(this.subrangePaint[WARNING]);
                    g2.draw(line);
                }

                // draw start of critical range
                value = this.subrangeInfo[CRITICAL][RANGE_LOW];
                if (range.contains(value)) {
                    double x = midX + getColumnRadius() + 2;
                    double y = this.rangeAxis.valueToJava2D(value, dataArea,
                            RectangleEdge.LEFT);
                    Line2D line = new Line2D.Double(x, y, x + 10, y);
                    g2.setPaint(this.subrangePaint[CRITICAL]);
                    g2.draw(line);
                }
            }

            // draw the axis...
            if ((this.rangeAxis != null) && (this.axisLocation != NONE)) {
                int drawWidth = AXIS_GAP;
                if (this.showValueLines) {
                    drawWidth += getColumnDiameter();
                }
                Rectangle2D drawArea;
                double cursor = 0;

                switch (this.axisLocation) {
                    case RIGHT:
                        cursor = midX + getColumnRadius();
                        drawArea = new Rectangle2D.Double(cursor,
                                stemTop, drawWidth, (stemBottom - stemTop + 1));
                        this.rangeAxis.draw(g2, cursor, area, drawArea,
                                RectangleEdge.RIGHT, null);
                        break;

                    case LEFT:
                    default:
                        //cursor = midX - COLUMN_RADIUS - AXIS_GAP;
                        cursor = midX - getColumnRadius();
                        drawArea = new Rectangle2D.Double(cursor, stemTop,
                                drawWidth, (stemBottom - stemTop + 1));
                        this.rangeAxis.draw(g2, cursor, area, drawArea,
                                RectangleEdge.LEFT, null);
                        break;
                }

            }

            // draw text value on screen
            g2.setFont(this.valueFont);
            g2.setPaint(this.valuePaint);
            metrics = g2.getFontMetrics();
            switch (this.valueLocation) {
                case RIGHT:
                    g2.drawString(this.valueFormat.format(current),
                            midX + getColumnRadius() + getGap(), midY);
                    break;
                case LEFT:
                    String valueString = this.valueFormat.format(current);
                    int stringWidth = metrics.stringWidth(valueString);
                    g2.drawString(valueString, midX - getColumnRadius()
                            - getGap() - stringWidth, midY);
                    break;
                case BULB:
                    temp = this.valueFormat.format(current);
                    i = metrics.stringWidth(temp) / 2;
                    g2.drawString(temp, midX - i,
                            stemBottom + getBulbRadius() + getGap());
                    break;
                default:
            }
            /***/
        }

        g2.setPaint(this.thermometerPaint);
        g2.setFont(this.valueFont);

        //  draw units indicator
        metrics = g2.getFontMetrics();
        int tickX1 = midX - getColumnRadius() - getGap() * 2
                     - metrics.stringWidth(UNITS[this.units]);
        if (tickX1 > area.getMinX()) {
            g2.drawString(UNITS[this.units], tickX1,
                    (int) (area.getMinY() + 20));
        }

        // draw thermometer outline
        g2.setStroke(this.thermometerStroke);
        g2.draw(outerThermometer);
        g2.draw(innerThermometer);

        drawOutline(g2, area);
    }

    /**
     * A zoom method that does nothing.  Plots are required to support the
     * zoom operation.  In the case of a thermometer chart, it doesn't make
     * sense to zoom in or out, so the method is empty.
     *
     * @param percent  the zoom percentage.
     */
    public void zoom(double percent) {
        // intentionally blank
   }

    /**
     * Returns a short string describing the type of plot.
     *
     * @return A short string describing the type of plot.
     */
    public String getPlotType() {
        return localizationResources.getString("Thermometer_Plot");
    }

    /**
     * Checks to see if a new value means the axis range needs adjusting.
     *
     * @param event  the dataset change event.
     */
    public void datasetChanged(DatasetChangeEvent event) {
        if (this.dataset != null) {
            Number vn = this.dataset.getValue();
            if (vn != null) {
                double value = vn.doubleValue();
                if (inSubrange(NORMAL, value)) {
                    this.subrange = NORMAL;
                }
                else if (inSubrange(WARNING, value)) {
                   this.subrange = WARNING;
                }
                else if (inSubrange(CRITICAL, value)) {
                    this.subrange = CRITICAL;
                }
                else {
                    this.subrange = -1;
                }
                setAxisRange();
            }
        }
        super.datasetChanged(event);
    }

    /**
     * Returns the minimum value in either the domain or the range, whichever
     * is displayed against the vertical axis for the particular type of plot
     * implementing this interface.
     *
     * @return The minimum value in either the domain or the range.
     *
     * @deprecated This method is not used.  Officially deprecated in version
     *         1.0.6.
     */
    public Number getMinimumVerticalDataValue() {
        return new Double(this.lowerBound);
    }

    /**
     * Returns the maximum value in either the domain or the range, whichever
     * is displayed against the vertical axis for the particular type of plot
     * implementing this interface.
     *
     * @return The maximum value in either the domain or the range
     *
     * @deprecated This method is not used.  Officially deprecated in version
     *         1.0.6.
     */
    public Number getMaximumVerticalDataValue() {
        return new Double(this.upperBound);
    }

    /**
     * Returns the data range.
     *
     * @param axis  the axis.
     *
     * @return The range of data displayed.
     */
    public Range getDataRange(ValueAxis axis) {
       return new Range(this.lowerBound, this.upperBound);
    }

    /**
     * Sets the axis range to the current values in the rangeInfo array.
     */
    protected void setAxisRange() {
        if ((this.subrange >= 0) && (this.followDataInSubranges)) {
            this.rangeAxis.setRange(
                    new Range(this.subrangeInfo[this.subrange][DISPLAY_LOW],
                    this.subrangeInfo[this.subrange][DISPLAY_HIGH]));
        }
        else {
            this.rangeAxis.setRange(this.lowerBound, this.upperBound);
        }
    }

    /**
     * Returns the legend items for the plot.
     *
     * @return <code>null</code>.
     */
    public LegendItemCollection getLegendItems() {
        return null;
    }

    /**
     * Returns the orientation of the plot.
     *
     * @return The orientation (always {@link PlotOrientation#VERTICAL}).
     */
    public PlotOrientation getOrientation() {
        return PlotOrientation.VERTICAL;
    }

    /**
     * Determine whether a number is valid and finite.
     *
     * @param d  the number to be tested.
     *
     * @return <code>true</code> if the number is valid and finite, and
     *         <code>false</code> otherwise.
     */
    protected static boolean isValidNumber(double d) {
        return (!(Double.isNaN(d) || Double.isInfinite(d)));
    }

    /**
     * Returns true if the value is in the specified range, and false otherwise.
     *
     * @param subrange  the subrange.
     * @param value  the value to check.
     *
     * @return A boolean.
     */
    private boolean inSubrange(int subrange, double value) {
        return (value > this.subrangeInfo[subrange][RANGE_LOW]
            && value <= this.subrangeInfo[subrange][RANGE_HIGH]);
    }

    /**
     * Returns the mercury paint corresponding to the current data value.
     * Called from the {@link #draw(Graphics2D, Rectangle2D, Point2D,
     * PlotState, PlotRenderingInfo)} method.
     *
     * @return The paint (never <code>null</code>).
     */
    private Paint getCurrentPaint() {
        Paint result = this.mercuryPaint;
        if (this.useSubrangePaint) {
            double value = this.dataset.getValue().doubleValue();
            if (inSubrange(NORMAL, value)) {
                result = this.subrangePaint[NORMAL];
            }
            else if (inSubrange(WARNING, value)) {
                result = this.subrangePaint[WARNING];
            }
            else if (inSubrange(CRITICAL, value)) {
                result = this.subrangePaint[CRITICAL];
            }
        }
        return result;
    }

    /**
     * Tests this plot for equality with another object.  The plot's dataset
     * is not considered in the test.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ThermometerPlot)) {
            return false;
        }
        ThermometerPlot that = (ThermometerPlot) obj;
        if (!super.equals(obj)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.rangeAxis, that.rangeAxis)) {
            return false;
        }
        if (this.axisLocation != that.axisLocation) {
            return false;
        }
        if (this.lowerBound != that.lowerBound) {
            return false;
        }
        if (this.upperBound != that.upperBound) {
            return false;
        }
        if (!ObjectUtilities.equal(this.padding, that.padding)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.thermometerStroke,
                that.thermometerStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.thermometerPaint,
                that.thermometerPaint)) {
            return false;
        }
        if (this.units != that.units) {
            return false;
        }
        if (this.valueLocation != that.valueLocation) {
            return false;
        }
        if (!ObjectUtilities.equal(this.valueFont, that.valueFont)) {
            return false;
        }
        if (!PaintUtilities.equal(this.valuePaint, that.valuePaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.valueFormat, that.valueFormat)) {
            return false;
        }
        if (!PaintUtilities.equal(this.mercuryPaint, that.mercuryPaint)) {
            return false;
        }
        if (this.showValueLines != that.showValueLines) {
            return false;
        }
        if (this.subrange != that.subrange) {
            return false;
        }
        if (this.followDataInSubranges != that.followDataInSubranges) {
            return false;
        }
        if (!equal(this.subrangeInfo, that.subrangeInfo)) {
            return false;
        }
        if (this.useSubrangePaint != that.useSubrangePaint) {
            return false;
        }
        if (this.bulbRadius != that.bulbRadius) {
            return false;
        }
        if (this.columnRadius != that.columnRadius) {
            return false;
        }
        if (this.gap != that.gap) {
            return false;
        }
        for (int i = 0; i < this.subrangePaint.length; i++) {
            if (!PaintUtilities.equal(this.subrangePaint[i],
                    that.subrangePaint[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests two double[][] arrays for equality.
     *
     * @param array1  the first array (<code>null</code> permitted).
     * @param array2  the second arrray (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    private static boolean equal(double[][] array1, double[][] array2) {
        if (array1 == null) {
            return (array2 == null);
        }
        if (array2 == null) {
            return false;
        }
        if (array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (!Arrays.equals(array1[i], array2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a clone of the plot.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  if the plot cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {

        ThermometerPlot clone = (ThermometerPlot) super.clone();

        if (clone.dataset != null) {
            clone.dataset.addChangeListener(clone);
        }
        clone.rangeAxis = (ValueAxis) ObjectUtilities.clone(this.rangeAxis);
        if (clone.rangeAxis != null) {
            clone.rangeAxis.setPlot(clone);
            clone.rangeAxis.addChangeListener(clone);
        }
        clone.valueFormat = (NumberFormat) this.valueFormat.clone();
        clone.subrangePaint = (Paint[]) this.subrangePaint.clone();

        return clone;

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
        SerialUtilities.writeStroke(this.thermometerStroke, stream);
        SerialUtilities.writePaint(this.thermometerPaint, stream);
        SerialUtilities.writePaint(this.valuePaint, stream);
        SerialUtilities.writePaint(this.mercuryPaint, stream);
        SerialUtilities.writeStroke(this.subrangeIndicatorStroke, stream);
        SerialUtilities.writeStroke(this.rangeIndicatorStroke, stream);
        for (int i = 0; i < 3; i++) {
            SerialUtilities.writePaint(this.subrangePaint[i], stream);
        }
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        this.thermometerStroke = SerialUtilities.readStroke(stream);
        this.thermometerPaint = SerialUtilities.readPaint(stream);
        this.valuePaint = SerialUtilities.readPaint(stream);
        this.mercuryPaint = SerialUtilities.readPaint(stream);
        this.subrangeIndicatorStroke = SerialUtilities.readStroke(stream);
        this.rangeIndicatorStroke = SerialUtilities.readStroke(stream);
        this.subrangePaint = new Paint[3];
        for (int i = 0; i < 3; i++) {
            this.subrangePaint[i] = SerialUtilities.readPaint(stream);
        }
        if (this.rangeAxis != null) {
            this.rangeAxis.addChangeListener(this);
        }
    }

    /**
     * Multiplies the range on the domain axis/axes by the specified factor.
     *
     * @param factor  the zoom factor.
     * @param state  the plot state.
     * @param source  the source point.
     */
    public void zoomDomainAxes(double factor, PlotRenderingInfo state,
                               Point2D source) {
        // no domain axis to zoom
    }

    /**
     * Multiplies the range on the domain axis/axes by the specified factor.
     *
     * @param factor  the zoom factor.
     * @param state  the plot state.
     * @param source  the source point.
     * @param useAnchor  a flag that controls whether or not the source point
     *         is used for the zoom anchor.
     *
     * @since 1.0.7
     */
    public void zoomDomainAxes(double factor, PlotRenderingInfo state,
                               Point2D source, boolean useAnchor) {
        // no domain axis to zoom
    }

    /**
     * Multiplies the range on the range axis/axes by the specified factor.
     *
     * @param factor  the zoom factor.
     * @param state  the plot state.
     * @param source  the source point.
     */
    public void zoomRangeAxes(double factor, PlotRenderingInfo state,
                              Point2D source) {
        this.rangeAxis.resizeRange(factor);
    }

    /**
     * Multiplies the range on the range axis/axes by the specified factor.
     *
     * @param factor  the zoom factor.
     * @param state  the plot state.
     * @param source  the source point.
     * @param useAnchor  a flag that controls whether or not the source point
     *         is used for the zoom anchor.
     *
     * @since 1.0.7
     */
    public void zoomRangeAxes(double factor, PlotRenderingInfo state,
                              Point2D source, boolean useAnchor) {
        double anchorY = this.getRangeAxis().java2DToValue(source.getY(),
                state.getDataArea(), RectangleEdge.LEFT);
        this.rangeAxis.resizeRange(factor, anchorY);
    }

    /**
     * This method does nothing.
     *
     * @param lowerPercent  the lower percent.
     * @param upperPercent  the upper percent.
     * @param state  the plot state.
     * @param source  the source point.
     */
    public void zoomDomainAxes(double lowerPercent, double upperPercent,
                               PlotRenderingInfo state, Point2D source) {
        // no domain axis to zoom
    }

    /**
     * Zooms the range axes.
     *
     * @param lowerPercent  the lower percent.
     * @param upperPercent  the upper percent.
     * @param state  the plot state.
     * @param source  the source point.
     */
    public void zoomRangeAxes(double lowerPercent, double upperPercent,
                              PlotRenderingInfo state, Point2D source) {
        this.rangeAxis.zoomRange(lowerPercent, upperPercent);
    }

    /**
     * Returns <code>false</code>.
     *
     * @return A boolean.
     */
    public boolean isDomainZoomable() {
        return false;
    }

    /**
     * Returns <code>true</code>.
     *
     * @return A boolean.
     */
    public boolean isRangeZoomable() {
        return true;
    }

}
