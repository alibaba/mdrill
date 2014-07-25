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
 * ------------
 * PiePlot.java
 * ------------
 * (C) Copyright 2000-2008, by Andrzej Porebski and Contributors.
 *
 * Original Author:  Andrzej Porebski;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Martin Cordova (percentages in labels);
 *                   Richard Atkinson (URL support for image maps);
 *                   Christian W. Zuckschwerdt;
 *                   Arnaud Lelievre;
 *                   Martin Hilpert (patch 1891849);
 *                   Andreas Schroeder (very minor);
 *                   Christoph Beck (bug 2121818);
 *
 * Changes
 * -------
 * 21-Jun-2001 : Removed redundant JFreeChart parameter from constructors (DG);
 * 18-Sep-2001 : Updated header (DG);
 * 15-Oct-2001 : Data source classes moved to com.jrefinery.data.* (DG);
 * 19-Oct-2001 : Moved series paint and stroke methods from JFreeChart.java to
 *               Plot.java (DG);
 * 22-Oct-2001 : Renamed DataSource.java --> Dataset.java etc. (DG);
 * 13-Nov-2001 : Modified plot subclasses so that null axes are possible for
 *               pie plot (DG);
 * 17-Nov-2001 : Added PieDataset interface and amended this class accordingly,
 *               and completed removal of BlankAxis class as it is no longer
 *               required (DG);
 * 19-Nov-2001 : Changed 'drawCircle' property to 'circular' property (DG);
 * 21-Nov-2001 : Added options for exploding pie sections and filled out range
 *               of properties (DG);
 *               Added option for percentages in chart labels, based on code
 *               by Martin Cordova (DG);
 * 30-Nov-2001 : Changed default font from "Arial" --> "SansSerif" (DG);
 * 12-Dec-2001 : Removed unnecessary 'throws' clause in constructor (DG);
 * 13-Dec-2001 : Added tooltips (DG);
 * 16-Jan-2002 : Renamed tooltips class (DG);
 * 22-Jan-2002 : Fixed bug correlating legend labels with pie data (DG);
 * 05-Feb-2002 : Added alpha-transparency to plot class, and updated
 *               constructors accordingly (DG);
 * 06-Feb-2002 : Added optional background image and alpha-transparency to Plot
 *               and subclasses.  Clipped drawing within plot area (DG);
 * 26-Mar-2002 : Added an empty zoom method (DG);
 * 18-Apr-2002 : PieDataset is no longer sorted (oldman);
 * 23-Apr-2002 : Moved dataset from JFreeChart to Plot.  Added
 *               getLegendItemLabels() method (DG);
 * 19-Jun-2002 : Added attributes to control starting angle and direction
 *               (default is now clockwise) (DG);
 * 25-Jun-2002 : Removed redundant imports (DG);
 * 02-Jul-2002 : Fixed sign of percentage bug introduced in 0.9.2 (DG);
 * 16-Jul-2002 : Added check for null dataset in getLegendItemLabels() (DG);
 * 30-Jul-2002 : Moved summation code to DatasetUtilities (DG);
 * 05-Aug-2002 : Added URL support for image maps - new member variable for
 *               urlGenerator, modified constructor and minor change to the
 *               draw method (RA);
 * 18-Sep-2002 : Modified the percent label creation and added setters for the
 *               formatters (AS);
 * 24-Sep-2002 : Added getLegendItems() method (DG);
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 09-Oct-2002 : Added check for null entity collection (DG);
 * 30-Oct-2002 : Changed PieDataset interface (DG);
 * 18-Nov-2002 : Changed CategoryDataset to TableDataset (DG);
 * 02-Jan-2003 : Fixed "no data" message (DG);
 * 23-Jan-2003 : Modified to extract data from rows OR columns in
 *               CategoryDataset (DG);
 * 14-Feb-2003 : Fixed label drawing so that foreground alpha does not apply
 *               (bug id 685536) (DG);
 * 07-Mar-2003 : Modified to pass pieIndex on to PieSectionEntity and tooltip
 *               and URL generators (DG);
 * 21-Mar-2003 : Added a minimum angle for drawing arcs
 *               (see bug id 620031) (DG);
 * 24-Apr-2003 : Switched around PieDataset and KeyedValuesDataset (DG);
 * 02-Jun-2003 : Fixed bug 721733 (DG);
 * 30-Jul-2003 : Modified entity constructor (CZ);
 * 19-Aug-2003 : Implemented Cloneable (DG);
 * 29-Aug-2003 : Fixed bug 796936 (null pointer on setOutlinePaint()) (DG);
 * 08-Sep-2003 : Added internationalization via use of properties
 *               resourceBundle (RFE 690236) (AL);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 29-Oct-2003 : Added workaround for font alignment in PDF output (DG);
 * 05-Nov-2003 : Fixed missing legend bug (DG);
 * 10-Nov-2003 : Re-added the DatasetChangeListener to constructors (CZ);
 * 29-Jan-2004 : Fixed clipping bug in draw() method (DG);
 * 11-Mar-2004 : Major overhaul to improve labelling (DG);
 * 31-Mar-2004 : Made an adjustment for the plot area when the label generator
 *               is null.  Fixed null pointer exception when the label
 *               generator returns null for a label (DG);
 * 06-Apr-2004 : Added getter, setter, serialization and draw support for
 *               labelBackgroundPaint (AS);
 * 08-Apr-2004 : Added flag to control whether null values are ignored or
 *               not (DG);
 * 15-Apr-2004 : Fixed some minor warnings from Eclipse (DG);
 * 26-Apr-2004 : Added attributes for label outline and shadow (DG);
 * 04-Oct-2004 : Renamed ShapeUtils --> ShapeUtilities (DG);
 * 04-Nov-2004 : Fixed null pointer exception with new LegendTitle class (DG);
 * 09-Nov-2004 : Added user definable legend item shape (DG);
 * 25-Nov-2004 : Added new legend label generator (DG);
 * 20-Apr-2005 : Added a tool tip generator for legend labels (DG);
 * 26-Apr-2005 : Removed LOGGER (DG);
 * 05-May-2005 : Updated draw() method parameters (DG);
 * 10-May-2005 : Added flag to control visibility of label linking lines, plus
 *               another flag to control the handling of zero values (DG);
 * 08-Jun-2005 : Fixed bug in getLegendItems() method (not respecting flags
 *               for ignoring null and zero values), and fixed equals() method
 *               to handle GradientPaint (DG);
 * 15-Jul-2005 : Added sectionOutlinesVisible attribute (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 09-Jan-2006 : Fixed bug 1400442, inconsistent treatment of null and zero
 *               values in dataset (DG);
 * 28-Feb-2006 : Fixed bug 1440415, bad distribution of pie section
 *               labels (DG);
 * 27-Sep-2006 : Initialised baseSectionPaint correctly, added lookup methods
 *               for section paint, outline paint and outline stroke (DG);
 * 27-Sep-2006 : Refactored paint and stroke methods to use keys rather than
 *               section indices (DG);
 * 03-Oct-2006 : Replaced call to JRE 1.5 method (DG);
 * 23-Nov-2006 : Added support for URLs for the legend items (DG);
 * 24-Nov-2006 : Cloning fixes (DG);
 * 17-Apr-2007 : Check for null label in legend items (DG);
 * 19-Apr-2007 : Deprecated override settings (DG);
 * 18-May-2007 : Set dataset for LegendItem (DG);
 * 14-Jun-2007 : Added label distributor attribute (DG);
 * 18-Jul-2007 : Added simple label option (DG);
 * 21-Nov-2007 : Fixed labelling bugs, added debug code, restored default
 *               white background (DG);
 * 19-Mar-2008 : Fixed IllegalArgumentException when drawing with null
 *               dataset (DG);
 * 31-Mar-2008 : Adjust the label area for the interiorGap (DG);
 * 31-Mar-2008 : Added quad and cubic curve label link lines - see patch
 *               1891849 by Martin Hilpert (DG);
 * 02-Jul-2008 : Added autoPopulate flags (DG);
 * 15-Aug-2008 : Added methods to clear section attributes (DG);
 * 15-Aug-2008 : Fixed bug 2051168 - problem with LegendItemEntity
 *               generation (DG);
 * 23-Sep-2008 : Added getLabelLinkDepth() method - see bug 2121818 reported
 *               by Christoph Beck (DG);
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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.PaintMap;
import org.jfree.chart.StrokeMap;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.PieDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.text.G2TextMeasurer;
import org.jfree.text.TextBlock;
import org.jfree.text.TextBox;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;
import org.jfree.util.Rotation;
import org.jfree.util.ShapeUtilities;
import org.jfree.util.UnitType;

/**
 * A plot that displays data in the form of a pie chart, using data from any
 * class that implements the {@link PieDataset} interface.
 * The example shown here is generated by the <code>PieChartDemo2.java</code>
 * program included in the JFreeChart Demo Collection:
 * <br><br>
 * <img src="../../../../images/PiePlotSample.png"
 * alt="PiePlotSample.png" />
 * <P>
 * Special notes:
 * <ol>
 * <li>the default starting point is 12 o'clock and the pie sections proceed
 * in a clockwise direction, but these settings can be changed;</li>
 * <li>negative values in the dataset are ignored;</li>
 * <li>there are utility methods for creating a {@link PieDataset} from a
 * {@link org.jfree.data.category.CategoryDataset};</li>
 * </ol>
 *
 * @see Plot
 * @see PieDataset
 */
public class PiePlot extends Plot implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -795612466005590431L;

    /** The default interior gap. */
    public static final double DEFAULT_INTERIOR_GAP = 0.08;

    /** The maximum interior gap (currently 40%). */
    public static final double MAX_INTERIOR_GAP = 0.40;

    /** The default starting angle for the pie chart. */
    public static final double DEFAULT_START_ANGLE = 90.0;

    /** The default section label font. */
    public static final Font DEFAULT_LABEL_FONT = new Font("SansSerif",
            Font.PLAIN, 10);

    /** The default section label paint. */
    public static final Paint DEFAULT_LABEL_PAINT = Color.black;

    /** The default section label background paint. */
    public static final Paint DEFAULT_LABEL_BACKGROUND_PAINT = new Color(255,
            255, 192);

    /** The default section label outline paint. */
    public static final Paint DEFAULT_LABEL_OUTLINE_PAINT = Color.black;

    /** The default section label outline stroke. */
    public static final Stroke DEFAULT_LABEL_OUTLINE_STROKE = new BasicStroke(
            0.5f);

    /** The default section label shadow paint. */
    public static final Paint DEFAULT_LABEL_SHADOW_PAINT = new Color(151, 151,
            151, 128);

    /** The default minimum arc angle to draw. */
    public static final double DEFAULT_MINIMUM_ARC_ANGLE_TO_DRAW = 0.00001;

    /** The dataset for the pie chart. */
    private PieDataset dataset;

    /** The pie index (used by the {@link MultiplePiePlot} class). */
    private int pieIndex;

    /**
     * The amount of space left around the outside of the pie plot, expressed
     * as a percentage of the plot area width and height.
     */
    private double interiorGap;

    /** Flag determining whether to draw an ellipse or a perfect circle. */
    private boolean circular;

    /** The starting angle. */
    private double startAngle;

    /** The direction for the pie segments. */
    private Rotation direction;

    /** The section paint map. */
    private PaintMap sectionPaintMap;

    /** The base section paint (fallback). */
    private transient Paint baseSectionPaint;

    /**
     * A flag that controls whether or not the section paint is auto-populated
     * from the drawing supplier.
     *
     * @since 1.0.11
     */
    private boolean autoPopulateSectionPaint;

    /**
     * A flag that controls whether or not an outline is drawn for each
     * section in the plot.
     */
    private boolean sectionOutlinesVisible;

    /** The section outline paint map. */
    private PaintMap sectionOutlinePaintMap;

    /** The base section outline paint (fallback). */
    private transient Paint baseSectionOutlinePaint;

    /**
     * A flag that controls whether or not the section outline paint is
     * auto-populated from the drawing supplier.
     *
     * @since 1.0.11
     */
    private boolean autoPopulateSectionOutlinePaint;

    /** The section outline stroke map. */
    private StrokeMap sectionOutlineStrokeMap;

    /** The base section outline stroke (fallback). */
    private transient Stroke baseSectionOutlineStroke;

    /**
     * A flag that controls whether or not the section outline stroke is
     * auto-populated from the drawing supplier.
     *
     * @since 1.0.11
     */
    private boolean autoPopulateSectionOutlineStroke;

    /** The shadow paint. */
    private transient Paint shadowPaint = Color.gray;

    /** The x-offset for the shadow effect. */
    private double shadowXOffset = 4.0f;

    /** The y-offset for the shadow effect. */
    private double shadowYOffset = 4.0f;

    /** The percentage amount to explode each pie section. */
    private Map explodePercentages;

    /** The section label generator. */
    private PieSectionLabelGenerator labelGenerator;

    /** The font used to display the section labels. */
    private Font labelFont;

    /** The color used to draw the section labels. */
    private transient Paint labelPaint;

    /**
     * The color used to draw the background of the section labels.  If this
     * is <code>null</code>, the background is not filled.
     */
    private transient Paint labelBackgroundPaint;

    /**
     * The paint used to draw the outline of the section labels
     * (<code>null</code> permitted).
     */
    private transient Paint labelOutlinePaint;

    /**
     * The stroke used to draw the outline of the section labels
     * (<code>null</code> permitted).
     */
    private transient Stroke labelOutlineStroke;

    /**
     * The paint used to draw the shadow for the section labels
     * (<code>null</code> permitted).
     */
    private transient Paint labelShadowPaint;

    /**
     * A flag that controls whether simple or extended labels are used.
     *
     * @since 1.0.7
     */
    private boolean simpleLabels = true;

    /**
     * The padding between the labels and the label outlines.  This is not
     * allowed to be <code>null</code>.
     *
     * @since 1.0.7
     */
    private RectangleInsets labelPadding;

    /**
     * The simple label offset.
     *
     * @since 1.0.7
     */
    private RectangleInsets simpleLabelOffset;

    /** The maximum label width as a percentage of the plot width. */
    private double maximumLabelWidth = 0.14;

    /**
     * The gap between the labels and the link corner, as a percentage of the
     * plot width.
     */
    private double labelGap = 0.025;

    /** A flag that controls whether or not the label links are drawn. */
    private boolean labelLinksVisible;

    /**
     * The label link style.
     *
     * @since 1.0.10
     */
    private PieLabelLinkStyle labelLinkStyle = PieLabelLinkStyle.STANDARD;

    /** The link margin. */
    private double labelLinkMargin = 0.025;

    /** The paint used for the label linking lines. */
    private transient Paint labelLinkPaint = Color.black;

    /** The stroke used for the label linking lines. */
    private transient Stroke labelLinkStroke = new BasicStroke(0.5f);

    /**
     * The pie section label distributor.
     *
     * @since 1.0.6
     */
    private AbstractPieLabelDistributor labelDistributor;

    /** The tooltip generator. */
    private PieToolTipGenerator toolTipGenerator;

    /** The URL generator. */
    private PieURLGenerator urlGenerator;

    /** The legend label generator. */
    private PieSectionLabelGenerator legendLabelGenerator;

    /** A tool tip generator for the legend. */
    private PieSectionLabelGenerator legendLabelToolTipGenerator;

    /**
     * A URL generator for the legend items (optional).
     *
     * @since 1.0.4.
     */
    private PieURLGenerator legendLabelURLGenerator;

    /**
     * A flag that controls whether <code>null</code> values are ignored.
     */
    private boolean ignoreNullValues;

    /**
     * A flag that controls whether zero values are ignored.
     */
    private boolean ignoreZeroValues;

    /** The legend item shape. */
    private transient Shape legendItemShape;

    /**
     * The smallest arc angle that will get drawn (this is to avoid a bug in
     * various Java implementations that causes the JVM to crash).  See this
     * link for details:
     *
     * http://www.jfree.org/phpBB2/viewtopic.php?t=2707
     *
     * ...and this bug report in the Java Bug Parade:
     *
     * http://developer.java.sun.com/developer/bugParade/bugs/4836495.html
     */
    private double minimumArcAngleToDraw;

    /** The resourceBundle for the localization. */
    protected static ResourceBundle localizationResources
            = ResourceBundleWrapper.getBundle(
                    "org.jfree.chart.plot.LocalizationBundle");

    /**
     * This debug flag controls whether or not an outline is drawn showing the
     * interior of the plot region.  This is drawn as a lightGray rectangle
     * showing the padding provided by the 'interiorGap' setting.
     */
    static final boolean DEBUG_DRAW_INTERIOR = false;

    /**
     * This debug flag controls whether or not an outline is drawn showing the
     * link area (in blue) and link ellipse (in yellow).  This controls where
     * the label links have 'elbow' points.
     */
    static final boolean DEBUG_DRAW_LINK_AREA = false;

    /**
     * This debug flag controls whether or not an outline is drawn showing
     * the pie area (in green).
     */
    static final boolean DEBUG_DRAW_PIE_AREA = false;

    /**
     * Creates a new plot.  The dataset is initially set to <code>null</code>.
     */
    public PiePlot() {
        this(null);
    }

    /**
     * Creates a plot that will draw a pie chart for the specified dataset.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     */
    public PiePlot(PieDataset dataset) {
        super();
        this.dataset = dataset;
        if (dataset != null) {
            dataset.addChangeListener(this);
        }
        this.pieIndex = 0;

        this.interiorGap = DEFAULT_INTERIOR_GAP;
        this.circular = true;
        this.startAngle = DEFAULT_START_ANGLE;
        this.direction = Rotation.CLOCKWISE;
        this.minimumArcAngleToDraw = DEFAULT_MINIMUM_ARC_ANGLE_TO_DRAW;

        this.sectionPaint = null;
        this.sectionPaintMap = new PaintMap();
        this.baseSectionPaint = Color.gray;
        this.autoPopulateSectionPaint = true;

        this.sectionOutlinesVisible = true;
        this.sectionOutlinePaint = null;
        this.sectionOutlinePaintMap = new PaintMap();
        this.baseSectionOutlinePaint = DEFAULT_OUTLINE_PAINT;
        this.autoPopulateSectionOutlinePaint = false;

        this.sectionOutlineStroke = null;
        this.sectionOutlineStrokeMap = new StrokeMap();
        this.baseSectionOutlineStroke = DEFAULT_OUTLINE_STROKE;
        this.autoPopulateSectionOutlineStroke = false;

        this.explodePercentages = new TreeMap();

        this.labelGenerator = new StandardPieSectionLabelGenerator();
        this.labelFont = DEFAULT_LABEL_FONT;
        this.labelPaint = DEFAULT_LABEL_PAINT;
        this.labelBackgroundPaint = DEFAULT_LABEL_BACKGROUND_PAINT;
        this.labelOutlinePaint = DEFAULT_LABEL_OUTLINE_PAINT;
        this.labelOutlineStroke = DEFAULT_LABEL_OUTLINE_STROKE;
        this.labelShadowPaint = DEFAULT_LABEL_SHADOW_PAINT;
        this.labelLinksVisible = true;
        this.labelDistributor = new PieLabelDistributor(0);

        this.simpleLabels = false;
        this.simpleLabelOffset = new RectangleInsets(UnitType.RELATIVE, 0.18,
                0.18, 0.18, 0.18);
        this.labelPadding = new RectangleInsets(2, 2, 2, 2);

        this.toolTipGenerator = null;
        this.urlGenerator = null;
        this.legendLabelGenerator = new StandardPieSectionLabelGenerator();
        this.legendLabelToolTipGenerator = null;
        this.legendLabelURLGenerator = null;
        this.legendItemShape = Plot.DEFAULT_LEGEND_ITEM_CIRCLE;

        this.ignoreNullValues = false;
        this.ignoreZeroValues = false;
    }

    /**
     * Returns the dataset.
     *
     * @return The dataset (possibly <code>null</code>).
     *
     * @see #setDataset(PieDataset)
     */
    public PieDataset getDataset() {
        return this.dataset;
    }

    /**
     * Sets the dataset and sends a {@link DatasetChangeEvent} to 'this'.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @see #getDataset()
     */
    public void setDataset(PieDataset dataset) {
        // if there is an existing dataset, remove the plot from the list of
        // change listeners...
        PieDataset existing = this.dataset;
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
     * Returns the pie index (this is used by the {@link MultiplePiePlot} class
     * to track subplots).
     *
     * @return The pie index.
     *
     * @see #setPieIndex(int)
     */
    public int getPieIndex() {
        return this.pieIndex;
    }

    /**
     * Sets the pie index (this is used by the {@link MultiplePiePlot} class to
     * track subplots).
     *
     * @param index  the index.
     *
     * @see #getPieIndex()
     */
    public void setPieIndex(int index) {
        this.pieIndex = index;
    }

    /**
     * Returns the start angle for the first pie section.  This is measured in
     * degrees starting from 3 o'clock and measuring anti-clockwise.
     *
     * @return The start angle.
     *
     * @see #setStartAngle(double)
     */
    public double getStartAngle() {
        return this.startAngle;
    }

    /**
     * Sets the starting angle and sends a {@link PlotChangeEvent} to all
     * registered listeners.  The initial default value is 90 degrees, which
     * corresponds to 12 o'clock.  A value of zero corresponds to 3 o'clock...
     * this is the encoding used by Java's Arc2D class.
     *
     * @param angle  the angle (in degrees).
     *
     * @see #getStartAngle()
     */
    public void setStartAngle(double angle) {
        this.startAngle = angle;
        fireChangeEvent();
    }

    /**
     * Returns the direction in which the pie sections are drawn (clockwise or
     * anti-clockwise).
     *
     * @return The direction (never <code>null</code>).
     *
     * @see #setDirection(Rotation)
     */
    public Rotation getDirection() {
        return this.direction;
    }

    /**
     * Sets the direction in which the pie sections are drawn and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param direction  the direction (<code>null</code> not permitted).
     *
     * @see #getDirection()
     */
    public void setDirection(Rotation direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Null 'direction' argument.");
        }
        this.direction = direction;
        fireChangeEvent();

    }

    /**
     * Returns the interior gap, measured as a percentage of the available
     * drawing space.
     *
     * @return The gap (as a percentage of the available drawing space).
     *
     * @see #setInteriorGap(double)
     */
    public double getInteriorGap() {
        return this.interiorGap;
    }

    /**
     * Sets the interior gap and sends a {@link PlotChangeEvent} to all
     * registered listeners.  This controls the space between the edges of the
     * pie plot and the plot area itself (the region where the section labels
     * appear).
     *
     * @param percent  the gap (as a percentage of the available drawing space).
     *
     * @see #getInteriorGap()
     */
    public void setInteriorGap(double percent) {

        if ((percent < 0.0) || (percent > MAX_INTERIOR_GAP)) {
            throw new IllegalArgumentException(
                "Invalid 'percent' (" + percent + ") argument.");
        }

        if (this.interiorGap != percent) {
            this.interiorGap = percent;
            fireChangeEvent();
        }

    }

    /**
     * Returns a flag indicating whether the pie chart is circular, or
     * stretched into an elliptical shape.
     *
     * @return A flag indicating whether the pie chart is circular.
     *
     * @see #setCircular(boolean)
     */
    public boolean isCircular() {
        return this.circular;
    }

    /**
     * A flag indicating whether the pie chart is circular, or stretched into
     * an elliptical shape.
     *
     * @param flag  the new value.
     *
     * @see #isCircular()
     */
    public void setCircular(boolean flag) {
        setCircular(flag, true);
    }

    /**
     * Sets the circular attribute and, if requested, sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param circular  the new value of the flag.
     * @param notify  notify listeners?
     *
     * @see #isCircular()
     */
    public void setCircular(boolean circular, boolean notify) {
        this.circular = circular;
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the flag that controls whether <code>null</code> values in the
     * dataset are ignored.
     *
     * @return A boolean.
     *
     * @see #setIgnoreNullValues(boolean)
     */
    public boolean getIgnoreNullValues() {
        return this.ignoreNullValues;
    }

    /**
     * Sets a flag that controls whether <code>null</code> values are ignored,
     * and sends a {@link PlotChangeEvent} to all registered listeners.  At
     * present, this only affects whether or not the key is presented in the
     * legend.
     *
     * @param flag  the flag.
     *
     * @see #getIgnoreNullValues()
     * @see #setIgnoreZeroValues(boolean)
     */
    public void setIgnoreNullValues(boolean flag) {
        this.ignoreNullValues = flag;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether zero values in the
     * dataset are ignored.
     *
     * @return A boolean.
     *
     * @see #setIgnoreZeroValues(boolean)
     */
    public boolean getIgnoreZeroValues() {
        return this.ignoreZeroValues;
    }

    /**
     * Sets a flag that controls whether zero values are ignored,
     * and sends a {@link PlotChangeEvent} to all registered listeners.  This
     * only affects whether or not a label appears for the non-visible
     * pie section.
     *
     * @param flag  the flag.
     *
     * @see #getIgnoreZeroValues()
     * @see #setIgnoreNullValues(boolean)
     */
    public void setIgnoreZeroValues(boolean flag) {
        this.ignoreZeroValues = flag;
        fireChangeEvent();
    }

    //// SECTION PAINT ////////////////////////////////////////////////////////

    /**
     * Returns the paint for the specified section.  This is equivalent to
     * <code>lookupSectionPaint(section, getAutoPopulateSectionPaint())</code>.
     *
     * @param key  the section key.
     *
     * @return The paint for the specified section.
     *
     * @since 1.0.3
     *
     * @see #lookupSectionPaint(Comparable, boolean)
     */
    protected Paint lookupSectionPaint(Comparable key) {
        return lookupSectionPaint(key, getAutoPopulateSectionPaint());
    }

    /**
     * Returns the paint for the specified section.  The lookup involves these
     * steps:
     * <ul>
     * <li>if {@link #getSectionPaint()} is non-<code>null</code>, return
     *         it;</li>
     * <li>if {@link #getSectionPaint(int)} is non-<code>null</code> return
     *         it;</li>
     * <li>if {@link #getSectionPaint(int)} is <code>null</code> but
     *         <code>autoPopulate</code> is <code>true</code>, attempt to fetch
     *         a new paint from the drawing supplier
     *         ({@link #getDrawingSupplier()});
     * <li>if all else fails, return {@link #getBaseSectionPaint()}.
     * </ul>
     *
     * @param key  the section key.
     * @param autoPopulate  a flag that controls whether the drawing supplier
     *     is used to auto-populate the section paint settings.
     *
     * @return The paint.
     *
     * @since 1.0.3
     */
    protected Paint lookupSectionPaint(Comparable key, boolean autoPopulate) {

        // is there an override?
        Paint result = getSectionPaint();
        if (result != null) {
            return result;
        }

        // if not, check if there is a paint defined for the specified key
        result = this.sectionPaintMap.getPaint(key);
        if (result != null) {
            return result;
        }

        // nothing defined - do we autoPopulate?
        if (autoPopulate) {
            DrawingSupplier ds = getDrawingSupplier();
            if (ds != null) {
                result = ds.getNextPaint();
                this.sectionPaintMap.put(key, result);
            }
            else {
                result = this.baseSectionPaint;
            }
        }
        else {
            result = this.baseSectionPaint;
        }
        return result;
    }

    /**
     * Returns the paint for ALL sections in the plot.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setSectionPaint(Paint)
     *
     * @deprecated Use {@link #getSectionPaint(Comparable)} and
     *     {@link #getBaseSectionPaint()}.  Deprecated as of version 1.0.6.
     */
    public Paint getSectionPaint() {
        return this.sectionPaint;
    }

    /**
     * Sets the paint for ALL sections in the plot.  If this is set to
     * </code>null</code>, then a list of paints is used instead (to allow
     * different colors to be used for each section).
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getSectionPaint()
     *
     * @deprecated Use {@link #setSectionPaint(Comparable, Paint)} and
     *     {@link #setBaseSectionPaint(Paint)}.  Deprecated as of version 1.0.6.
     */
    public void setSectionPaint(Paint paint) {
        this.sectionPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns a key for the specified section.  If there is no such section
     * in the dataset, we generate a key.  This is to provide some backward
     * compatibility for the (now deprecated) methods that get/set attributes
     * based on section indices.  The preferred way of doing this now is to
     * link the attributes directly to the section key (there are new methods
     * for this, starting from version 1.0.3).
     *
     * @param section  the section index.
     *
     * @return The key.
     *
     * @since 1.0.3
     */
    protected Comparable getSectionKey(int section) {
        Comparable key = null;
        if (this.dataset != null) {
            if (section >= 0 && section < this.dataset.getItemCount()) {
                key = this.dataset.getKey(section);
            }
        }
        if (key == null) {
            key = new Integer(section);
        }
        return key;
    }

    /**
     * Returns the paint associated with the specified key, or
     * <code>null</code> if there is no paint associated with the key.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The paint associated with the specified key, or
     *     <code>null</code>.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     *
     * @see #setSectionPaint(Comparable, Paint)
     *
     * @since 1.0.3
     */
    public Paint getSectionPaint(Comparable key) {
        // null argument check delegated...
        return this.sectionPaintMap.getPaint(key);
    }

    /**
     * Sets the paint associated with the specified key, and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param key  the key (<code>null</code> not permitted).
     * @param paint  the paint.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     *
     * @see #getSectionPaint(Comparable)
     *
     * @since 1.0.3
     */
    public void setSectionPaint(Comparable key, Paint paint) {
        // null argument check delegated...
        this.sectionPaintMap.put(key, paint);
        fireChangeEvent();
    }

    /**
     * Clears the section paint settings for this plot and, if requested, sends
     * a {@link PlotChangeEvent} to all registered listeners.  Be aware that
     * if the <code>autoPopulateSectionPaint</code> flag is set, the section
     * paints may be repopulated using the same colours as before.
     *
     * @param notify  notify listeners?
     *
     * @since 1.0.11
     *
     * @see #autoPopulateSectionPaint
     */
    public void clearSectionPaints(boolean notify) {
        this.sectionPaintMap.clear();
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base section paint.  This is used when no other paint is
     * defined, which is rare.  The default value is <code>Color.gray</code>.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setBaseSectionPaint(Paint)
     */
    public Paint getBaseSectionPaint() {
        return this.baseSectionPaint;
    }

    /**
     * Sets the base section paint and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getBaseSectionPaint()
     */
    public void setBaseSectionPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.baseSectionPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not the section paint is
     * auto-populated by the {@link #lookupSectionPaint(Comparable)} method.
     *
     * @return A boolean.
     *
     * @since 1.0.11
     */
    public boolean getAutoPopulateSectionPaint() {
        return this.autoPopulateSectionPaint;
    }

    /**
     * Sets the flag that controls whether or not the section paint is
     * auto-populated by the {@link #lookupSectionPaint(Comparable)} method,
     * and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param auto  auto-populate?
     *
     * @since 1.0.11
     */
    public void setAutoPopulateSectionPaint(boolean auto) {
        this.autoPopulateSectionPaint = auto;
        fireChangeEvent();
    }

    //// SECTION OUTLINE PAINT ////////////////////////////////////////////////

    /**
     * Returns the flag that controls whether or not the outline is drawn for
     * each pie section.
     *
     * @return The flag that controls whether or not the outline is drawn for
     *         each pie section.
     *
     * @see #setSectionOutlinesVisible(boolean)
     */
    public boolean getSectionOutlinesVisible() {
        return this.sectionOutlinesVisible;
    }

    /**
     * Sets the flag that controls whether or not the outline is drawn for
     * each pie section, and sends a {@link PlotChangeEvent} to all registered
     * listeners.
     *
     * @param visible  the flag.
     *
     * @see #getSectionOutlinesVisible()
     */
    public void setSectionOutlinesVisible(boolean visible) {
        this.sectionOutlinesVisible = visible;
        fireChangeEvent();
    }

    /**
     * Returns the outline paint for the specified section.  This is equivalent
     * to <code>lookupSectionPaint(section,
     * getAutoPopulateSectionOutlinePaint())</code>.
     *
     * @param key  the section key.
     *
     * @return The paint for the specified section.
     *
     * @since 1.0.3
     *
     * @see #lookupSectionOutlinePaint(Comparable, boolean)
     */
    protected Paint lookupSectionOutlinePaint(Comparable key) {
        return lookupSectionOutlinePaint(key,
                getAutoPopulateSectionOutlinePaint());
    }

    /**
     * Returns the outline paint for the specified section.  The lookup
     * involves these steps:
     * <ul>
     * <li>if {@link #getSectionOutlinePaint()} is non-<code>null</code>,
     *         return it;</li>
     * <li>otherwise, if {@link #getSectionOutlinePaint(int)} is
     *         non-<code>null</code> return it;</li>
     * <li>if {@link #getSectionOutlinePaint(int)} is <code>null</code> but
     *         <code>autoPopulate</code> is <code>true</code>, attempt to fetch
     *         a new outline paint from the drawing supplier
     *         ({@link #getDrawingSupplier()});
     * <li>if all else fails, return {@link #getBaseSectionOutlinePaint()}.
     * </ul>
     *
     * @param key  the section key.
     * @param autoPopulate  a flag that controls whether the drawing supplier
     *     is used to auto-populate the section outline paint settings.
     *
     * @return The paint.
     *
     * @since 1.0.3
     */
    protected Paint lookupSectionOutlinePaint(Comparable key,
            boolean autoPopulate) {

        // is there an override?
        Paint result = getSectionOutlinePaint();
        if (result != null) {
            return result;
        }

        // if not, check if there is a paint defined for the specified key
        result = this.sectionOutlinePaintMap.getPaint(key);
        if (result != null) {
            return result;
        }

        // nothing defined - do we autoPopulate?
        if (autoPopulate) {
            DrawingSupplier ds = getDrawingSupplier();
            if (ds != null) {
                result = ds.getNextOutlinePaint();
                this.sectionOutlinePaintMap.put(key, result);
            }
            else {
                result = this.baseSectionOutlinePaint;
            }
        }
        else {
            result = this.baseSectionOutlinePaint;
        }
        return result;
    }

    /**
     * Returns the outline paint associated with the specified key, or
     * <code>null</code> if there is no paint associated with the key.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The paint associated with the specified key, or
     *     <code>null</code>.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     *
     * @see #setSectionOutlinePaint(Comparable, Paint)
     *
     * @since 1.0.3
     */
    public Paint getSectionOutlinePaint(Comparable key) {
        // null argument check delegated...
        return this.sectionOutlinePaintMap.getPaint(key);
    }

    /**
     * Sets the outline paint associated with the specified key, and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param key  the key (<code>null</code> not permitted).
     * @param paint  the paint.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     *
     * @see #getSectionOutlinePaint(Comparable)
     *
     * @since 1.0.3
     */
    public void setSectionOutlinePaint(Comparable key, Paint paint) {
        // null argument check delegated...
        this.sectionOutlinePaintMap.put(key, paint);
        fireChangeEvent();
    }

    /**
     * Clears the section outline paint settings for this plot and, if
     * requested, sends a {@link PlotChangeEvent} to all registered listeners.
     * Be aware that if the <code>autoPopulateSectionPaint</code> flag is set,
     * the section paints may be repopulated using the same colours as before.
     *
     * @param notify  notify listeners?
     *
     * @since 1.0.11
     *
     * @see #autoPopulateSectionOutlinePaint
     */
    public void clearSectionOutlinePaints(boolean notify) {
        this.sectionOutlinePaintMap.clear();
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base section paint.  This is used when no other paint is
     * available.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setBaseSectionOutlinePaint(Paint)
     */
    public Paint getBaseSectionOutlinePaint() {
        return this.baseSectionOutlinePaint;
    }

    /**
     * Sets the base section paint.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getBaseSectionOutlinePaint()
     */
    public void setBaseSectionOutlinePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.baseSectionOutlinePaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not the section outline paint
     * is auto-populated by the {@link #lookupSectionOutlinePaint(Comparable)}
     * method.
     *
     * @return A boolean.
     *
     * @since 1.0.11
     */
    public boolean getAutoPopulateSectionOutlinePaint() {
        return this.autoPopulateSectionOutlinePaint;
    }

    /**
     * Sets the flag that controls whether or not the section outline paint is
     * auto-populated by the {@link #lookupSectionOutlinePaint(Comparable)}
     * method, and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param auto  auto-populate?
     *
     * @since 1.0.11
     */
    public void setAutoPopulateSectionOutlinePaint(boolean auto) {
        this.autoPopulateSectionOutlinePaint = auto;
        fireChangeEvent();
    }

    //// SECTION OUTLINE STROKE ///////////////////////////////////////////////

    /**
     * Returns the outline stroke for the specified section.  This is
     * equivalent to <code>lookupSectionOutlineStroke(section,
     * getAutoPopulateSectionOutlineStroke())</code>.
     *
     * @param key  the section key.
     *
     * @return The stroke for the specified section.
     *
     * @since 1.0.3
     *
     * @see #lookupSectionOutlineStroke(Comparable, boolean)
     */
    protected Stroke lookupSectionOutlineStroke(Comparable key) {
        return lookupSectionOutlineStroke(key,
                getAutoPopulateSectionOutlineStroke());
    }

    /**
     * Returns the outline stroke for the specified section.  The lookup
     * involves these steps:
     * <ul>
     * <li>if {@link #getSectionOutlineStroke()} is non-<code>null</code>,
     *         return it;</li>
     * <li>otherwise, if {@link #getSectionOutlineStroke(int)} is
     *         non-<code>null</code> return it;</li>
     * <li>if {@link #getSectionOutlineStroke(int)} is <code>null</code> but
     *         <code>autoPopulate</code> is <code>true</code>, attempt to fetch
     *         a new outline stroke from the drawing supplier
     *         ({@link #getDrawingSupplier()});
     * <li>if all else fails, return {@link #getBaseSectionOutlineStroke()}.
     * </ul>
     *
     * @param key  the section key.
     * @param autoPopulate  a flag that controls whether the drawing supplier
     *     is used to auto-populate the section outline stroke settings.
     *
     * @return The stroke.
     *
     * @since 1.0.3
     */
    protected Stroke lookupSectionOutlineStroke(Comparable key,
            boolean autoPopulate) {

        // is there an override?
        Stroke result = getSectionOutlineStroke();
        if (result != null) {
            return result;
        }

        // if not, check if there is a stroke defined for the specified key
        result = this.sectionOutlineStrokeMap.getStroke(key);
        if (result != null) {
            return result;
        }

        // nothing defined - do we autoPopulate?
        if (autoPopulate) {
            DrawingSupplier ds = getDrawingSupplier();
            if (ds != null) {
                result = ds.getNextOutlineStroke();
                this.sectionOutlineStrokeMap.put(key, result);
            }
            else {
                result = this.baseSectionOutlineStroke;
            }
        }
        else {
            result = this.baseSectionOutlineStroke;
        }
        return result;
    }

    /**
     * Returns the outline stroke associated with the specified key, or
     * <code>null</code> if there is no stroke associated with the key.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The stroke associated with the specified key, or
     *     <code>null</code>.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     *
     * @see #setSectionOutlineStroke(Comparable, Stroke)
     *
     * @since 1.0.3
     */
    public Stroke getSectionOutlineStroke(Comparable key) {
        // null argument check delegated...
        return this.sectionOutlineStrokeMap.getStroke(key);
    }

    /**
     * Sets the outline stroke associated with the specified key, and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param key  the key (<code>null</code> not permitted).
     * @param stroke  the stroke.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     *
     * @see #getSectionOutlineStroke(Comparable)
     *
     * @since 1.0.3
     */
    public void setSectionOutlineStroke(Comparable key, Stroke stroke) {
        // null argument check delegated...
        this.sectionOutlineStrokeMap.put(key, stroke);
        fireChangeEvent();
    }

    /**
     * Clears the section outline stroke settings for this plot and, if
     * requested, sends a {@link PlotChangeEvent} to all registered listeners.
     * Be aware that if the <code>autoPopulateSectionPaint</code> flag is set,
     * the section paints may be repopulated using the same colours as before.
     *
     * @param notify  notify listeners?
     *
     * @since 1.0.11
     *
     * @see #autoPopulateSectionOutlineStroke
     */
    public void clearSectionOutlineStrokes(boolean notify) {
        this.sectionOutlineStrokeMap.clear();
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base section stroke.  This is used when no other stroke is
     * available.
     *
     * @return The stroke (never <code>null</code>).
     *
     * @see #setBaseSectionOutlineStroke(Stroke)
     */
    public Stroke getBaseSectionOutlineStroke() {
        return this.baseSectionOutlineStroke;
    }

    /**
     * Sets the base section stroke.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     *
     * @see #getBaseSectionOutlineStroke()
     */
    public void setBaseSectionOutlineStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.baseSectionOutlineStroke = stroke;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not the section outline stroke
     * is auto-populated by the {@link #lookupSectionOutlinePaint(Comparable)}
     * method.
     *
     * @return A boolean.
     *
     * @since 1.0.11
     */
    public boolean getAutoPopulateSectionOutlineStroke() {
        return this.autoPopulateSectionOutlineStroke;
    }

    /**
     * Sets the flag that controls whether or not the section outline stroke is
     * auto-populated by the {@link #lookupSectionOutlineStroke(Comparable)}
     * method, and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param auto  auto-populate?
     *
     * @since 1.0.11
     */
    public void setAutoPopulateSectionOutlineStroke(boolean auto) {
        this.autoPopulateSectionOutlineStroke = auto;
        fireChangeEvent();
    }

    /**
     * Returns the shadow paint.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setShadowPaint(Paint)
     */
    public Paint getShadowPaint() {
        return this.shadowPaint;
    }

    /**
     * Sets the shadow paint and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getShadowPaint()
     */
    public void setShadowPaint(Paint paint) {
        this.shadowPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the x-offset for the shadow effect.
     *
     * @return The offset (in Java2D units).
     *
     * @see #setShadowXOffset(double)
     */
    public double getShadowXOffset() {
        return this.shadowXOffset;
    }

    /**
     * Sets the x-offset for the shadow effect and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param offset  the offset (in Java2D units).
     *
     * @see #getShadowXOffset()
     */
    public void setShadowXOffset(double offset) {
        this.shadowXOffset = offset;
        fireChangeEvent();
    }

    /**
     * Returns the y-offset for the shadow effect.
     *
     * @return The offset (in Java2D units).
     *
     * @see #setShadowYOffset(double)
     */
    public double getShadowYOffset() {
        return this.shadowYOffset;
    }

    /**
     * Sets the y-offset for the shadow effect and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param offset  the offset (in Java2D units).
     *
     * @see #getShadowYOffset()
     */
    public void setShadowYOffset(double offset) {
        this.shadowYOffset = offset;
        fireChangeEvent();
    }

    /**
     * Returns the amount that the section with the specified key should be
     * exploded.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The amount that the section with the specified key should be
     *     exploded.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     *
     * @since 1.0.3
     *
     * @see #setExplodePercent(Comparable, double)
     */
    public double getExplodePercent(Comparable key) {
        double result = 0.0;
        if (this.explodePercentages != null) {
            Number percent = (Number) this.explodePercentages.get(key);
            if (percent != null) {
                result = percent.doubleValue();
            }
        }
        return result;
    }

    /**
     * Sets the amount that a pie section should be exploded and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param key  the section key (<code>null</code> not permitted).
     * @param percent  the explode percentage (0.30 = 30 percent).
     *
     * @since 1.0.3
     *
     * @see #getExplodePercent(Comparable)
     */
    public void setExplodePercent(Comparable key, double percent) {
        if (key == null) {
            throw new IllegalArgumentException("Null 'key' argument.");
        }
        if (this.explodePercentages == null) {
            this.explodePercentages = new TreeMap();
        }
        this.explodePercentages.put(key, new Double(percent));
        fireChangeEvent();
    }

    /**
     * Returns the maximum explode percent.
     *
     * @return The percent.
     */
    public double getMaximumExplodePercent() {
        if (this.dataset == null) {
            return 0.0;
        }
        double result = 0.0;
        Iterator iterator = this.dataset.getKeys().iterator();
        while (iterator.hasNext()) {
            Comparable key = (Comparable) iterator.next();
            Number explode = (Number) this.explodePercentages.get(key);
            if (explode != null) {
                result = Math.max(result, explode.doubleValue());
            }
        }
        return result;
    }

    /**
     * Returns the section label generator.
     *
     * @return The generator (possibly <code>null</code>).
     *
     * @see #setLabelGenerator(PieSectionLabelGenerator)
     */
    public PieSectionLabelGenerator getLabelGenerator() {
        return this.labelGenerator;
    }

    /**
     * Sets the section label generator and sends a {@link PlotChangeEvent} to
     * all registered listeners.
     *
     * @param generator  the generator (<code>null</code> permitted).
     *
     * @see #getLabelGenerator()
     */
    public void setLabelGenerator(PieSectionLabelGenerator generator) {
        this.labelGenerator = generator;
        fireChangeEvent();
    }

    /**
     * Returns the gap between the edge of the pie and the labels, expressed as
     * a percentage of the plot width.
     *
     * @return The gap (a percentage, where 0.05 = five percent).
     *
     * @see #setLabelGap(double)
     */
    public double getLabelGap() {
        return this.labelGap;
    }

    /**
     * Sets the gap between the edge of the pie and the labels (expressed as a
     * percentage of the plot width) and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param gap  the gap (a percentage, where 0.05 = five percent).
     *
     * @see #getLabelGap()
     */
    public void setLabelGap(double gap) {
        this.labelGap = gap;
        fireChangeEvent();
    }

    /**
     * Returns the maximum label width as a percentage of the plot width.
     *
     * @return The width (a percentage, where 0.20 = 20 percent).
     *
     * @see #setMaximumLabelWidth(double)
     */
    public double getMaximumLabelWidth() {
        return this.maximumLabelWidth;
    }

    /**
     * Sets the maximum label width as a percentage of the plot width and sends
     * a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param width  the width (a percentage, where 0.20 = 20 percent).
     *
     * @see #getMaximumLabelWidth()
     */
    public void setMaximumLabelWidth(double width) {
        this.maximumLabelWidth = width;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not label linking lines are
     * visible.
     *
     * @return A boolean.
     *
     * @see #setLabelLinksVisible(boolean)
     */
    public boolean getLabelLinksVisible() {
        return this.labelLinksVisible;
    }

    /**
     * Sets the flag that controls whether or not label linking lines are
     * visible and sends a {@link PlotChangeEvent} to all registered listeners.
     * Please take care when hiding the linking lines - depending on the data
     * values, the labels can be displayed some distance away from the
     * corresponding pie section.
     *
     * @param visible  the flag.
     *
     * @see #getLabelLinksVisible()
     */
    public void setLabelLinksVisible(boolean visible) {
        this.labelLinksVisible = visible;
        fireChangeEvent();
    }

    /**
     * Returns the label link style.
     *
     * @return The label link style (never <code>null</code>).
     *
     * @see #setLabelLinkStyle(PieLabelLinkStyle)
     *
     * @since 1.0.10
     */
    public PieLabelLinkStyle getLabelLinkStyle() {
        return this.labelLinkStyle;
    }

    /**
     * Sets the label link style and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param style  the new style (<code>null</code> not permitted).
     *
     * @see #getLabelLinkStyle()
     *
     * @since 1.0.10
     */
    public void setLabelLinkStyle(PieLabelLinkStyle style) {
        if (style == null) {
            throw new IllegalArgumentException("Null 'style' argument.");
        }
        this.labelLinkStyle = style;
        fireChangeEvent();
    }

    /**
     * Returns the margin (expressed as a percentage of the width or height)
     * between the edge of the pie and the link point.
     *
     * @return The link margin (as a percentage, where 0.05 is five percent).
     *
     * @see #setLabelLinkMargin(double)
     */
    public double getLabelLinkMargin() {
        return this.labelLinkMargin;
    }

    /**
     * Sets the link margin and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param margin  the margin.
     *
     * @see #getLabelLinkMargin()
     */
    public void setLabelLinkMargin(double margin) {
        this.labelLinkMargin = margin;
        fireChangeEvent();
    }

    /**
     * Returns the paint used for the lines that connect pie sections to their
     * corresponding labels.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setLabelLinkPaint(Paint)
     */
    public Paint getLabelLinkPaint() {
        return this.labelLinkPaint;
    }

    /**
     * Sets the paint used for the lines that connect pie sections to their
     * corresponding labels, and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getLabelLinkPaint()
     */
    public void setLabelLinkPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.labelLinkPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the stroke used for the label linking lines.
     *
     * @return The stroke.
     *
     * @see #setLabelLinkStroke(Stroke)
     */
    public Stroke getLabelLinkStroke() {
        return this.labelLinkStroke;
    }

    /**
     * Sets the link stroke and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param stroke  the stroke.
     *
     * @see #getLabelLinkStroke()
     */
    public void setLabelLinkStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.labelLinkStroke = stroke;
        fireChangeEvent();
    }

    /**
     * Returns the distance that the end of the label link is embedded into
     * the plot, expressed as a percentage of the plot's radius.
     * <br><br>
     * This method is overridden in the {@link RingPlot} class to resolve
     * bug 2121818.
     *
     * @return <code>0.10</code>.
     *
     * @since 1.0.12
     */
    protected double getLabelLinkDepth() {
        return 0.1;
    }

    /**
     * Returns the section label font.
     *
     * @return The font (never <code>null</code>).
     *
     * @see #setLabelFont(Font)
     */
    public Font getLabelFont() {
        return this.labelFont;
    }

    /**
     * Sets the section label font and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param font  the font (<code>null</code> not permitted).
     *
     * @see #getLabelFont()
     */
    public void setLabelFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }
        this.labelFont = font;
        fireChangeEvent();
    }

    /**
     * Returns the section label paint.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setLabelPaint(Paint)
     */
    public Paint getLabelPaint() {
        return this.labelPaint;
    }

    /**
     * Sets the section label paint and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getLabelPaint()
     */
    public void setLabelPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.labelPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the section label background paint.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setLabelBackgroundPaint(Paint)
     */
    public Paint getLabelBackgroundPaint() {
        return this.labelBackgroundPaint;
    }

    /**
     * Sets the section label background paint and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getLabelBackgroundPaint()
     */
    public void setLabelBackgroundPaint(Paint paint) {
        this.labelBackgroundPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the section label outline paint.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setLabelOutlinePaint(Paint)
     */
    public Paint getLabelOutlinePaint() {
        return this.labelOutlinePaint;
    }

    /**
     * Sets the section label outline paint and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getLabelOutlinePaint()
     */
    public void setLabelOutlinePaint(Paint paint) {
        this.labelOutlinePaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the section label outline stroke.
     *
     * @return The stroke (possibly <code>null</code>).
     *
     * @see #setLabelOutlineStroke(Stroke)
     */
    public Stroke getLabelOutlineStroke() {
        return this.labelOutlineStroke;
    }

    /**
     * Sets the section label outline stroke and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> permitted).
     *
     * @see #getLabelOutlineStroke()
     */
    public void setLabelOutlineStroke(Stroke stroke) {
        this.labelOutlineStroke = stroke;
        fireChangeEvent();
    }

    /**
     * Returns the section label shadow paint.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setLabelShadowPaint(Paint)
     */
    public Paint getLabelShadowPaint() {
        return this.labelShadowPaint;
    }

    /**
     * Sets the section label shadow paint and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getLabelShadowPaint()
     */
    public void setLabelShadowPaint(Paint paint) {
        this.labelShadowPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the label padding.
     *
     * @return The label padding (never <code>null</code>).
     *
     * @since 1.0.7
     *
     * @see #setLabelPadding(RectangleInsets)
     */
    public RectangleInsets getLabelPadding() {
        return this.labelPadding;
    }

    /**
     * Sets the padding between each label and its outline and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param padding  the padding (<code>null</code> not permitted).
     *
     * @since 1.0.7
     *
     * @see #getLabelPadding()
     */
    public void setLabelPadding(RectangleInsets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("Null 'padding' argument.");
        }
        this.labelPadding = padding;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether simple or extended labels are
     * displayed on the plot.
     *
     * @return A boolean.
     *
     * @since 1.0.7
     */
    public boolean getSimpleLabels() {
        return this.simpleLabels;
    }

    /**
     * Sets the flag that controls whether simple or extended labels are
     * displayed on the plot, and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param simple  the new flag value.
     *
     * @since 1.0.7
     */
    public void setSimpleLabels(boolean simple) {
        this.simpleLabels = simple;
        fireChangeEvent();
    }

    /**
     * Returns the offset used for the simple labels, if they are displayed.
     *
     * @return The offset (never <code>null</code>).
     *
     * @since 1.0.7
     *
     * @see #setSimpleLabelOffset(RectangleInsets)
     */
    public RectangleInsets getSimpleLabelOffset() {
        return this.simpleLabelOffset;
    }

    /**
     * Sets the offset for the simple labels and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param offset  the offset (<code>null</code> not permitted).
     *
     * @since 1.0.7
     *
     * @see #getSimpleLabelOffset()
     */
    public void setSimpleLabelOffset(RectangleInsets offset) {
        if (offset == null) {
            throw new IllegalArgumentException("Null 'offset' argument.");
        }
        this.simpleLabelOffset = offset;
        fireChangeEvent();
    }

    /**
     * Returns the object responsible for the vertical layout of the pie
     * section labels.
     *
     * @return The label distributor (never <code>null</code>).
     *
     * @since 1.0.6
     */
    public AbstractPieLabelDistributor getLabelDistributor() {
        return this.labelDistributor;
    }

    /**
     * Sets the label distributor and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param distributor  the distributor (<code>null</code> not permitted).
     *
     * @since 1.0.6
     */
    public void setLabelDistributor(AbstractPieLabelDistributor distributor) {
        if (distributor == null) {
            throw new IllegalArgumentException("Null 'distributor' argument.");
        }
        this.labelDistributor = distributor;
        fireChangeEvent();
    }

    /**
     * Returns the tool tip generator, an object that is responsible for
     * generating the text items used for tool tips by the plot.  If the
     * generator is <code>null</code>, no tool tips will be created.
     *
     * @return The generator (possibly <code>null</code>).
     *
     * @see #setToolTipGenerator(PieToolTipGenerator)
     */
    public PieToolTipGenerator getToolTipGenerator() {
        return this.toolTipGenerator;
    }

    /**
     * Sets the tool tip generator and sends a {@link PlotChangeEvent} to all
     * registered listeners.  Set the generator to <code>null</code> if you
     * don't want any tool tips.
     *
     * @param generator  the generator (<code>null</code> permitted).
     *
     * @see #getToolTipGenerator()
     */
    public void setToolTipGenerator(PieToolTipGenerator generator) {
        this.toolTipGenerator = generator;
        fireChangeEvent();
    }

    /**
     * Returns the URL generator.
     *
     * @return The generator (possibly <code>null</code>).
     *
     * @see #setURLGenerator(PieURLGenerator)
     */
    public PieURLGenerator getURLGenerator() {
        return this.urlGenerator;
    }

    /**
     * Sets the URL generator and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param generator  the generator (<code>null</code> permitted).
     *
     * @see #getURLGenerator()
     */
    public void setURLGenerator(PieURLGenerator generator) {
        this.urlGenerator = generator;
        fireChangeEvent();
    }

    /**
     * Returns the minimum arc angle that will be drawn.  Pie sections for an
     * angle smaller than this are not drawn, to avoid a JDK bug.
     *
     * @return The minimum angle.
     *
     * @see #setMinimumArcAngleToDraw(double)
     */
    public double getMinimumArcAngleToDraw() {
        return this.minimumArcAngleToDraw;
    }

    /**
     * Sets the minimum arc angle that will be drawn.  Pie sections for an
     * angle smaller than this are not drawn, to avoid a JDK bug.  See this
     * link for details:
     * <br><br>
     * <a href="http://www.jfree.org/phpBB2/viewtopic.php?t=2707">
     * http://www.jfree.org/phpBB2/viewtopic.php?t=2707</a>
     * <br><br>
     * ...and this bug report in the Java Bug Parade:
     * <br><br>
     * <a href=
     * "http://developer.java.sun.com/developer/bugParade/bugs/4836495.html">
     * http://developer.java.sun.com/developer/bugParade/bugs/4836495.html</a>
     *
     * @param angle  the minimum angle.
     *
     * @see #getMinimumArcAngleToDraw()
     */
    public void setMinimumArcAngleToDraw(double angle) {
        this.minimumArcAngleToDraw = angle;
    }

    /**
     * Returns the shape used for legend items.
     *
     * @return The shape (never <code>null</code>).
     *
     * @see #setLegendItemShape(Shape)
     */
    public Shape getLegendItemShape() {
        return this.legendItemShape;
    }

    /**
     * Sets the shape used for legend items and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     *
     * @param shape  the shape (<code>null</code> not permitted).
     *
     * @see #getLegendItemShape()
     */
    public void setLegendItemShape(Shape shape) {
        if (shape == null) {
            throw new IllegalArgumentException("Null 'shape' argument.");
        }
        this.legendItemShape = shape;
        fireChangeEvent();
    }

    /**
     * Returns the legend label generator.
     *
     * @return The legend label generator (never <code>null</code>).
     *
     * @see #setLegendLabelGenerator(PieSectionLabelGenerator)
     */
    public PieSectionLabelGenerator getLegendLabelGenerator() {
        return this.legendLabelGenerator;
    }

    /**
     * Sets the legend label generator and sends a {@link PlotChangeEvent} to
     * all registered listeners.
     *
     * @param generator  the generator (<code>null</code> not permitted).
     *
     * @see #getLegendLabelGenerator()
     */
    public void setLegendLabelGenerator(PieSectionLabelGenerator generator) {
        if (generator == null) {
            throw new IllegalArgumentException("Null 'generator' argument.");
        }
        this.legendLabelGenerator = generator;
        fireChangeEvent();
    }

    /**
     * Returns the legend label tool tip generator.
     *
     * @return The legend label tool tip generator (possibly <code>null</code>).
     *
     * @see #setLegendLabelToolTipGenerator(PieSectionLabelGenerator)
     */
    public PieSectionLabelGenerator getLegendLabelToolTipGenerator() {
        return this.legendLabelToolTipGenerator;
    }

    /**
     * Sets the legend label tool tip generator and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param generator  the generator (<code>null</code> permitted).
     *
     * @see #getLegendLabelToolTipGenerator()
     */
    public void setLegendLabelToolTipGenerator(
            PieSectionLabelGenerator generator) {
        this.legendLabelToolTipGenerator = generator;
        fireChangeEvent();
    }

    /**
     * Returns the legend label URL generator.
     *
     * @return The legend label URL generator (possibly <code>null</code>).
     *
     * @see #setLegendLabelURLGenerator(PieURLGenerator)
     *
     * @since 1.0.4
     */
    public PieURLGenerator getLegendLabelURLGenerator() {
        return this.legendLabelURLGenerator;
    }

    /**
     * Sets the legend label URL generator and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param generator  the generator (<code>null</code> permitted).
     *
     * @see #getLegendLabelURLGenerator()
     *
     * @since 1.0.4
     */
    public void setLegendLabelURLGenerator(PieURLGenerator generator) {
        this.legendLabelURLGenerator = generator;
        fireChangeEvent();
    }

    /**
     * Initialises the drawing procedure.  This method will be called before
     * the first item is rendered, giving the plot an opportunity to initialise
     * any state information it wants to maintain.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area (<code>null</code> not permitted).
     * @param plot  the plot.
     * @param index  the secondary index (<code>null</code> for primary
     *               renderer).
     * @param info  collects chart rendering information for return to caller.
     *
     * @return A state object (maintains state information relevant to one
     *         chart drawing).
     */
    public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea,
            PiePlot plot, Integer index, PlotRenderingInfo info) {

        PiePlotState state = new PiePlotState(info);
        state.setPassesRequired(2);
        if (this.dataset != null) {
            state.setTotal(DatasetUtilities.calculatePieDatasetTotal(
                    plot.getDataset()));
        }
        state.setLatestAngle(plot.getStartAngle());
        return state;

    }

    /**
     * Draws the plot on a Java 2D graphics device (such as the screen or a
     * printer).
     *
     * @param g2  the graphics device.
     * @param area  the area within which the plot should be drawn.
     * @param anchor  the anchor point (<code>null</code> permitted).
     * @param parentState  the state from the parent plot, if there is one.
     * @param info  collects info about the drawing
     *              (<code>null</code> permitted).
     */
    public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
                     PlotState parentState, PlotRenderingInfo info) {

        // adjust for insets...
        RectangleInsets insets = getInsets();
        insets.trim(area);

        if (info != null) {
            info.setPlotArea(area);
            info.setDataArea(area);
        }

        drawBackground(g2, area);
        drawOutline(g2, area);

        Shape savedClip = g2.getClip();
        g2.clip(area);

        Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                getForegroundAlpha()));

        if (!DatasetUtilities.isEmptyOrNull(this.dataset)) {
            drawPie(g2, area, info);
        }
        else {
            drawNoDataMessage(g2, area);
        }

        g2.setClip(savedClip);
        g2.setComposite(originalComposite);

        drawOutline(g2, area);

    }

    /**
     * Draws the pie.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param info  chart rendering info.
     */
    protected void drawPie(Graphics2D g2, Rectangle2D plotArea,
                           PlotRenderingInfo info) {

        PiePlotState state = initialise(g2, plotArea, this, null, info);

        // adjust the plot area for interior spacing and labels...
        double labelReserve = 0.0;
        if (this.labelGenerator != null && !this.simpleLabels) {
            labelReserve = this.labelGap + this.maximumLabelWidth;
        }
        double gapHorizontal = plotArea.getWidth() * (this.interiorGap
                + labelReserve) * 2.0;
        double gapVertical = plotArea.getHeight() * this.interiorGap * 2.0;


        if (DEBUG_DRAW_INTERIOR) {
            double hGap = plotArea.getWidth() * this.interiorGap;
            double vGap = plotArea.getHeight() * this.interiorGap;

            double igx1 = plotArea.getX() + hGap;
            double igx2 = plotArea.getMaxX() - hGap;
            double igy1 = plotArea.getY() + vGap;
            double igy2 = plotArea.getMaxY() - vGap;
            g2.setPaint(Color.gray);
            g2.draw(new Rectangle2D.Double(igx1, igy1, igx2 - igx1,
                    igy2 - igy1));
        }

        double linkX = plotArea.getX() + gapHorizontal / 2;
        double linkY = plotArea.getY() + gapVertical / 2;
        double linkW = plotArea.getWidth() - gapHorizontal;
        double linkH = plotArea.getHeight() - gapVertical;

        // make the link area a square if the pie chart is to be circular...
        if (this.circular) {
            double min = Math.min(linkW, linkH) / 2;
            linkX = (linkX + linkX + linkW) / 2 - min;
            linkY = (linkY + linkY + linkH) / 2 - min;
            linkW = 2 * min;
            linkH = 2 * min;
        }

        // the link area defines the dog leg points for the linking lines to
        // the labels
        Rectangle2D linkArea = new Rectangle2D.Double(linkX, linkY, linkW,
                linkH);
        state.setLinkArea(linkArea);

        if (DEBUG_DRAW_LINK_AREA) {
            g2.setPaint(Color.blue);
            g2.draw(linkArea);
            g2.setPaint(Color.yellow);
            g2.draw(new Ellipse2D.Double(linkArea.getX(), linkArea.getY(),
                    linkArea.getWidth(), linkArea.getHeight()));
        }

        // the explode area defines the max circle/ellipse for the exploded
        // pie sections.  it is defined by shrinking the linkArea by the
        // linkMargin factor.
        double lm = 0.0;
        if (!this.simpleLabels) {
            lm = this.labelLinkMargin;
        }
        double hh = linkArea.getWidth() * lm * 2.0;
        double vv = linkArea.getHeight() * lm * 2.0;
        Rectangle2D explodeArea = new Rectangle2D.Double(linkX + hh / 2.0,
                linkY + vv / 2.0, linkW - hh, linkH - vv);

        state.setExplodedPieArea(explodeArea);

        // the pie area defines the circle/ellipse for regular pie sections.
        // it is defined by shrinking the explodeArea by the explodeMargin
        // factor.
        double maximumExplodePercent = getMaximumExplodePercent();
        double percent = maximumExplodePercent / (1.0 + maximumExplodePercent);

        double h1 = explodeArea.getWidth() * percent;
        double v1 = explodeArea.getHeight() * percent;
        Rectangle2D pieArea = new Rectangle2D.Double(explodeArea.getX()
                + h1 / 2.0, explodeArea.getY() + v1 / 2.0,
                explodeArea.getWidth() - h1, explodeArea.getHeight() - v1);

        if (DEBUG_DRAW_PIE_AREA) {
            g2.setPaint(Color.green);
            g2.draw(pieArea);
        }
        state.setPieArea(pieArea);
        state.setPieCenterX(pieArea.getCenterX());
        state.setPieCenterY(pieArea.getCenterY());
        state.setPieWRadius(pieArea.getWidth() / 2.0);
        state.setPieHRadius(pieArea.getHeight() / 2.0);

        // plot the data (unless the dataset is null)...
        if ((this.dataset != null) && (this.dataset.getKeys().size() > 0)) {

            List keys = this.dataset.getKeys();
            double totalValue = DatasetUtilities.calculatePieDatasetTotal(
                    this.dataset);

            int passesRequired = state.getPassesRequired();
            for (int pass = 0; pass < passesRequired; pass++) {
                double runningTotal = 0.0;
                for (int section = 0; section < keys.size(); section++) {
                    Number n = this.dataset.getValue(section);
                    if (n != null) {
                        double value = n.doubleValue();
                        if (value > 0.0) {
                            runningTotal += value;
                            drawItem(g2, section, explodeArea, state, pass);
                        }
                    }
                }
            }
            if (this.simpleLabels) {
                drawSimpleLabels(g2, keys, totalValue, plotArea, linkArea,
                        state);
            }
            else {
                drawLabels(g2, keys, totalValue, plotArea, linkArea, state);
            }

        }
        else {
            drawNoDataMessage(g2, plotArea);
        }
    }

    /**
     * Draws a single data item.
     *
     * @param g2  the graphics device (<code>null</code> not permitted).
     * @param section  the section index.
     * @param dataArea  the data plot area.
     * @param state  state information for one chart.
     * @param currentPass  the current pass index.
     */
    protected void drawItem(Graphics2D g2, int section, Rectangle2D dataArea,
                            PiePlotState state, int currentPass) {

        Number n = this.dataset.getValue(section);
        if (n == null) {
            return;
        }
        double value = n.doubleValue();
        double angle1 = 0.0;
        double angle2 = 0.0;

        if (this.direction == Rotation.CLOCKWISE) {
            angle1 = state.getLatestAngle();
            angle2 = angle1 - value / state.getTotal() * 360.0;
        }
        else if (this.direction == Rotation.ANTICLOCKWISE) {
            angle1 = state.getLatestAngle();
            angle2 = angle1 + value / state.getTotal() * 360.0;
        }
        else {
            throw new IllegalStateException("Rotation type not recognised.");
        }

        double angle = (angle2 - angle1);
        if (Math.abs(angle) > getMinimumArcAngleToDraw()) {
            double ep = 0.0;
            double mep = getMaximumExplodePercent();
            if (mep > 0.0) {
                ep = getExplodePercent(section) / mep;
            }
            Rectangle2D arcBounds = getArcBounds(state.getPieArea(),
                    state.getExplodedPieArea(), angle1, angle, ep);
            Arc2D.Double arc = new Arc2D.Double(arcBounds, angle1, angle,
                    Arc2D.PIE);

            if (currentPass == 0) {
                if (this.shadowPaint != null) {
                    Shape shadowArc = ShapeUtilities.createTranslatedShape(
                            arc, (float) this.shadowXOffset,
                            (float) this.shadowYOffset);
                    g2.setPaint(this.shadowPaint);
                    g2.fill(shadowArc);
                }
            }
            else if (currentPass == 1) {
                Comparable key = getSectionKey(section);
                Paint paint = lookupSectionPaint(key);
                g2.setPaint(paint);
                g2.fill(arc);

                Paint outlinePaint = lookupSectionOutlinePaint(key);
                Stroke outlineStroke = lookupSectionOutlineStroke(key);
                if (this.sectionOutlinesVisible) {
                    g2.setPaint(outlinePaint);
                    g2.setStroke(outlineStroke);
                    g2.draw(arc);
                }

                // update the linking line target for later
                // add an entity for the pie section
                if (state.getInfo() != null) {
                    EntityCollection entities = state.getEntityCollection();
                    if (entities != null) {
                        String tip = null;
                        if (this.toolTipGenerator != null) {
                            tip = this.toolTipGenerator.generateToolTip(
                                    this.dataset, key);
                        }
                        String url = null;
                        if (this.urlGenerator != null) {
                            url = this.urlGenerator.generateURL(this.dataset,
                                    key, this.pieIndex);
                        }
                        PieSectionEntity entity = new PieSectionEntity(
                                arc, this.dataset, this.pieIndex, section, key,
                                tip, url);
                        entities.add(entity);
                    }
                }
            }
        }
        state.setLatestAngle(angle2);
    }

    /**
     * Draws the pie section labels in the simple form.
     *
     * @param g2  the graphics device.
     * @param keys  the section keys.
     * @param totalValue  the total value for all sections in the pie.
     * @param plotArea  the plot area.
     * @param pieArea  the area containing the pie.
     * @param state  the plot state.
     *
     * @since 1.0.7
     */
    protected void drawSimpleLabels(Graphics2D g2, List keys,
            double totalValue, Rectangle2D plotArea, Rectangle2D pieArea,
            PiePlotState state) {

        Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                1.0f));

        RectangleInsets labelInsets = new RectangleInsets(UnitType.RELATIVE,
                0.18, 0.18, 0.18, 0.18);
        Rectangle2D labelsArea = labelInsets.createInsetRectangle(pieArea);
        double runningTotal = 0.0;
        Iterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            Comparable key = (Comparable) iterator.next();
            boolean include = true;
            double v = 0.0;
            Number n = getDataset().getValue(key);
            if (n == null) {
                include = !getIgnoreNullValues();
            }
            else {
                v = n.doubleValue();
                include = getIgnoreZeroValues() ? v > 0.0 : v >= 0.0;
            }

            if (include) {
                runningTotal = runningTotal + v;
                // work out the mid angle (0 - 90 and 270 - 360) = right,
                // otherwise left
                double mid = getStartAngle() + (getDirection().getFactor()
                        * ((runningTotal - v / 2.0) * 360) / totalValue);

                Arc2D arc = new Arc2D.Double(labelsArea, getStartAngle(),
                        mid - getStartAngle(), Arc2D.OPEN);
                int x = (int) arc.getEndPoint().getX();
                int y = (int) arc.getEndPoint().getY();

                PieSectionLabelGenerator labelGenerator = getLabelGenerator();
                if (labelGenerator == null) {
                    continue;
                }
                String label = labelGenerator.generateSectionLabel(
                        this.dataset, key);
                if (label == null) {
                    continue;
                }
                g2.setFont(this.labelFont);
                FontMetrics fm = g2.getFontMetrics();
                Rectangle2D bounds = TextUtilities.getTextBounds(label, g2, fm);
                Rectangle2D out = this.labelPadding.createOutsetRectangle(
                        bounds);
                Shape bg = ShapeUtilities.createTranslatedShape(out,
                        x - bounds.getCenterX(), y - bounds.getCenterY());
                if (this.labelShadowPaint != null) {
                    Shape shadow = ShapeUtilities.createTranslatedShape(bg,
                            this.shadowXOffset, this.shadowYOffset);
                    g2.setPaint(this.labelShadowPaint);
                    g2.fill(shadow);
                }
                if (this.labelBackgroundPaint != null) {
                    g2.setPaint(this.labelBackgroundPaint);
                    g2.fill(bg);
                }
                if (this.labelOutlinePaint != null
                        && this.labelOutlineStroke != null) {
                    g2.setPaint(this.labelOutlinePaint);
                    g2.setStroke(this.labelOutlineStroke);
                    g2.draw(bg);
                }

                g2.setPaint(this.labelPaint);
                g2.setFont(this.labelFont);
                TextUtilities.drawAlignedString(getLabelGenerator()
                        .generateSectionLabel(getDataset(), key), g2, x, y,
                        TextAnchor.CENTER);

            }
        }

        g2.setComposite(originalComposite);

    }

    /**
     * Draws the labels for the pie sections.
     *
     * @param g2  the graphics device.
     * @param keys  the keys.
     * @param totalValue  the total value.
     * @param plotArea  the plot area.
     * @param linkArea  the link area.
     * @param state  the state.
     */
    protected void drawLabels(Graphics2D g2, List keys, double totalValue,
                              Rectangle2D plotArea, Rectangle2D linkArea,
                              PiePlotState state) {

        Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                1.0f));

        // classify the keys according to which side the label will appear...
        DefaultKeyedValues leftKeys = new DefaultKeyedValues();
        DefaultKeyedValues rightKeys = new DefaultKeyedValues();

        double runningTotal = 0.0;
        Iterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            Comparable key = (Comparable) iterator.next();
            boolean include = true;
            double v = 0.0;
            Number n = this.dataset.getValue(key);
            if (n == null) {
                include = !this.ignoreNullValues;
            }
            else {
                v = n.doubleValue();
                include = this.ignoreZeroValues ? v > 0.0 : v >= 0.0;
            }

            if (include) {
                runningTotal = runningTotal + v;
                // work out the mid angle (0 - 90 and 270 - 360) = right,
                // otherwise left
                double mid = this.startAngle + (this.direction.getFactor()
                        * ((runningTotal - v / 2.0) * 360) / totalValue);
                if (Math.cos(Math.toRadians(mid)) < 0.0) {
                    leftKeys.addValue(key, new Double(mid));
                }
                else {
                    rightKeys.addValue(key, new Double(mid));
                }
            }
        }

        g2.setFont(getLabelFont());

        // calculate the max label width from the plot dimensions, because
        // a circular pie can leave a lot more room for labels...
        double marginX = plotArea.getX() + this.interiorGap
                * plotArea.getWidth();
        double gap = plotArea.getWidth() * this.labelGap;
        double ww = linkArea.getX() - gap - marginX;
        float labelWidth = (float) this.labelPadding.trimWidth(ww);

        // draw the labels...
        if (this.labelGenerator != null) {
            drawLeftLabels(leftKeys, g2, plotArea, linkArea, labelWidth,
                    state);
            drawRightLabels(rightKeys, g2, plotArea, linkArea, labelWidth,
                    state);
        }
        g2.setComposite(originalComposite);

    }

    /**
     * Draws the left labels.
     *
     * @param leftKeys  a collection of keys and angles (to the middle of the
     *         section, in degrees) for the sections on the left side of the
     *         plot.
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param linkArea  the link area.
     * @param maxLabelWidth  the maximum label width.
     * @param state  the state.
     */
    protected void drawLeftLabels(KeyedValues leftKeys, Graphics2D g2,
                                  Rectangle2D plotArea, Rectangle2D linkArea,
                                  float maxLabelWidth, PiePlotState state) {

        this.labelDistributor.clear();
        double lGap = plotArea.getWidth() * this.labelGap;
        double verticalLinkRadius = state.getLinkArea().getHeight() / 2.0;
        for (int i = 0; i < leftKeys.getItemCount(); i++) {
            String label = this.labelGenerator.generateSectionLabel(
                    this.dataset, leftKeys.getKey(i));
            if (label != null) {
                TextBlock block = TextUtilities.createTextBlock(label,
                        this.labelFont, this.labelPaint, maxLabelWidth,
                        new G2TextMeasurer(g2));
                TextBox labelBox = new TextBox(block);
                labelBox.setBackgroundPaint(this.labelBackgroundPaint);
                labelBox.setOutlinePaint(this.labelOutlinePaint);
                labelBox.setOutlineStroke(this.labelOutlineStroke);
                labelBox.setShadowPaint(this.labelShadowPaint);
                labelBox.setInteriorGap(this.labelPadding);
                double theta = Math.toRadians(
                        leftKeys.getValue(i).doubleValue());
                double baseY = state.getPieCenterY() - Math.sin(theta)
                               * verticalLinkRadius;
                double hh = labelBox.getHeight(g2);

                this.labelDistributor.addPieLabelRecord(new PieLabelRecord(
                        leftKeys.getKey(i), theta, baseY, labelBox, hh,
                        lGap / 2.0 + lGap / 2.0 * -Math.cos(theta), 1.0
                        - getLabelLinkDepth()
                        + getExplodePercent(leftKeys.getKey(i))));
            }
        }
        double hh = plotArea.getHeight();
        double gap = hh * getInteriorGap();
        this.labelDistributor.distributeLabels(plotArea.getMinY() + gap,
                hh - 2 * gap);
        for (int i = 0; i < this.labelDistributor.getItemCount(); i++) {
            drawLeftLabel(g2, state,
                    this.labelDistributor.getPieLabelRecord(i));
        }
    }

    /**
     * Draws the right labels.
     *
     * @param keys  the keys.
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param linkArea  the link area.
     * @param maxLabelWidth  the maximum label width.
     * @param state  the state.
     */
    protected void drawRightLabels(KeyedValues keys, Graphics2D g2,
                                   Rectangle2D plotArea, Rectangle2D linkArea,
                                   float maxLabelWidth, PiePlotState state) {

        // draw the right labels...
        this.labelDistributor.clear();
        double lGap = plotArea.getWidth() * this.labelGap;
        double verticalLinkRadius = state.getLinkArea().getHeight() / 2.0;

        for (int i = 0; i < keys.getItemCount(); i++) {
            String label = this.labelGenerator.generateSectionLabel(
                    this.dataset, keys.getKey(i));

            if (label != null) {
                TextBlock block = TextUtilities.createTextBlock(label,
                        this.labelFont, this.labelPaint, maxLabelWidth,
                        new G2TextMeasurer(g2));
                TextBox labelBox = new TextBox(block);
                labelBox.setBackgroundPaint(this.labelBackgroundPaint);
                labelBox.setOutlinePaint(this.labelOutlinePaint);
                labelBox.setOutlineStroke(this.labelOutlineStroke);
                labelBox.setShadowPaint(this.labelShadowPaint);
                labelBox.setInteriorGap(this.labelPadding);
                double theta = Math.toRadians(keys.getValue(i).doubleValue());
                double baseY = state.getPieCenterY()
                              - Math.sin(theta) * verticalLinkRadius;
                double hh = labelBox.getHeight(g2);
                this.labelDistributor.addPieLabelRecord(new PieLabelRecord(
                        keys.getKey(i), theta, baseY, labelBox, hh,
                        lGap / 2.0 + lGap / 2.0 * Math.cos(theta),
                        1.0 - getLabelLinkDepth()
                        + getExplodePercent(keys.getKey(i))));
            }
        }
        double hh = plotArea.getHeight();
        double gap = hh * getInteriorGap();
        this.labelDistributor.distributeLabels(plotArea.getMinY() + gap,
                hh - 2 * gap);
        for (int i = 0; i < this.labelDistributor.getItemCount(); i++) {
            drawRightLabel(g2, state,
                    this.labelDistributor.getPieLabelRecord(i));
        }

    }

    /**
     * Returns a collection of legend items for the pie chart.
     *
     * @return The legend items (never <code>null</code>).
     */
    public LegendItemCollection getLegendItems() {

        LegendItemCollection result = new LegendItemCollection();
        if (this.dataset == null) {
            return result;
        }
        List keys = this.dataset.getKeys();
        int section = 0;
        Shape shape = getLegendItemShape();
        Iterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            Comparable key = (Comparable) iterator.next();
            Number n = this.dataset.getValue(key);
            boolean include = true;
            if (n == null) {
                include = !this.ignoreNullValues;
            }
            else {
                double v = n.doubleValue();
                if (v == 0.0) {
                    include = !this.ignoreZeroValues;
                }
                else {
                    include = v > 0.0;
                }
            }
            if (include) {
                String label = this.legendLabelGenerator.generateSectionLabel(
                        this.dataset, key);
                if (label != null) {
                    String description = label;
                    String toolTipText = null;
                    if (this.legendLabelToolTipGenerator != null) {
                        toolTipText = this.legendLabelToolTipGenerator
                                .generateSectionLabel(this.dataset, key);
                    }
                    String urlText = null;
                    if (this.legendLabelURLGenerator != null) {
                        urlText = this.legendLabelURLGenerator.generateURL(
                                this.dataset, key, this.pieIndex);
                    }
                    Paint paint = lookupSectionPaint(key);
                    Paint outlinePaint = lookupSectionOutlinePaint(key);
                    Stroke outlineStroke = lookupSectionOutlineStroke(key);
                    LegendItem item = new LegendItem(label, description,
                            toolTipText, urlText, true, shape, true, paint,
                            true, outlinePaint, outlineStroke,
                            false,          // line not visible
                            new Line2D.Float(), new BasicStroke(), Color.black);
                    item.setDataset(getDataset());
                    item.setSeriesIndex(this.dataset.getIndex(key));
                    item.setSeriesKey(key);
                    result.add(item);
                }
                section++;
            }
            else {
                section++;
            }
        }
        return result;
    }

    /**
     * Returns a short string describing the type of plot.
     *
     * @return The plot type.
     */
    public String getPlotType() {
        return localizationResources.getString("Pie_Plot");
    }

    /**
     * Returns a rectangle that can be used to create a pie section (taking
     * into account the amount by which the pie section is 'exploded').
     *
     * @param unexploded  the area inside which the unexploded pie sections are
     *                    drawn.
     * @param exploded  the area inside which the exploded pie sections are
     *                  drawn.
     * @param angle  the start angle.
     * @param extent  the extent of the arc.
     * @param explodePercent  the amount by which the pie section is exploded.
     *
     * @return A rectangle that can be used to create a pie section.
     */
    protected Rectangle2D getArcBounds(Rectangle2D unexploded,
                                       Rectangle2D exploded,
                                       double angle, double extent,
                                       double explodePercent) {

        if (explodePercent == 0.0) {
            return unexploded;
        }
        else {
            Arc2D arc1 = new Arc2D.Double(unexploded, angle, extent / 2,
                    Arc2D.OPEN);
            Point2D point1 = arc1.getEndPoint();
            Arc2D.Double arc2 = new Arc2D.Double(exploded, angle, extent / 2,
                    Arc2D.OPEN);
            Point2D point2 = arc2.getEndPoint();
            double deltaX = (point1.getX() - point2.getX()) * explodePercent;
            double deltaY = (point1.getY() - point2.getY()) * explodePercent;
            return new Rectangle2D.Double(unexploded.getX() - deltaX,
                    unexploded.getY() - deltaY, unexploded.getWidth(),
                    unexploded.getHeight());
        }
    }

    /**
     * Draws a section label on the left side of the pie chart.
     *
     * @param g2  the graphics device.
     * @param state  the state.
     * @param record  the label record.
     */
    protected void drawLeftLabel(Graphics2D g2, PiePlotState state,
                                 PieLabelRecord record) {

        double anchorX = state.getLinkArea().getMinX();
        double targetX = anchorX - record.getGap();
        double targetY = record.getAllocatedY();

        if (this.labelLinksVisible) {
            double theta = record.getAngle();
            double linkX = state.getPieCenterX() + Math.cos(theta)
                    * state.getPieWRadius() * record.getLinkPercent();
            double linkY = state.getPieCenterY() - Math.sin(theta)
                    * state.getPieHRadius() * record.getLinkPercent();
            double elbowX = state.getPieCenterX() + Math.cos(theta)
                    * state.getLinkArea().getWidth() / 2.0;
            double elbowY = state.getPieCenterY() - Math.sin(theta)
                    * state.getLinkArea().getHeight() / 2.0;
            double anchorY = elbowY;
            g2.setPaint(this.labelLinkPaint);
            g2.setStroke(this.labelLinkStroke);
            PieLabelLinkStyle style = getLabelLinkStyle();
            if (style.equals(PieLabelLinkStyle.STANDARD)) {
                g2.draw(new Line2D.Double(linkX, linkY, elbowX, elbowY));
                g2.draw(new Line2D.Double(anchorX, anchorY, elbowX, elbowY));
                g2.draw(new Line2D.Double(anchorX, anchorY, targetX, targetY));
            }
            else if (style.equals(PieLabelLinkStyle.QUAD_CURVE)) {
                QuadCurve2D q = new QuadCurve2D.Float();
                q.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY);
                g2.draw(q);
                g2.draw(new Line2D.Double(elbowX, elbowY, linkX, linkY));
            }
            else if (style.equals(PieLabelLinkStyle.CUBIC_CURVE)) {
                CubicCurve2D c = new CubicCurve2D .Float();
                c.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY,
                        linkX, linkY);
                g2.draw(c);
            }
        }
        TextBox tb = record.getLabel();
        tb.draw(g2, (float) targetX, (float) targetY, RectangleAnchor.RIGHT);

    }

    /**
     * Draws a section label on the right side of the pie chart.
     *
     * @param g2  the graphics device.
     * @param state  the state.
     * @param record  the label record.
     */
    protected void drawRightLabel(Graphics2D g2, PiePlotState state,
                                  PieLabelRecord record) {

        double anchorX = state.getLinkArea().getMaxX();
        double targetX = anchorX + record.getGap();
        double targetY = record.getAllocatedY();

        if (this.labelLinksVisible) {
            double theta = record.getAngle();
            double linkX = state.getPieCenterX() + Math.cos(theta)
                    * state.getPieWRadius() * record.getLinkPercent();
            double linkY = state.getPieCenterY() - Math.sin(theta)
                    * state.getPieHRadius() * record.getLinkPercent();
            double elbowX = state.getPieCenterX() + Math.cos(theta)
                    * state.getLinkArea().getWidth() / 2.0;
            double elbowY = state.getPieCenterY() - Math.sin(theta)
                    * state.getLinkArea().getHeight() / 2.0;
            double anchorY = elbowY;
            g2.setPaint(this.labelLinkPaint);
            g2.setStroke(this.labelLinkStroke);
            PieLabelLinkStyle style = getLabelLinkStyle();
            if (style.equals(PieLabelLinkStyle.STANDARD)) {
                g2.draw(new Line2D.Double(linkX, linkY, elbowX, elbowY));
                g2.draw(new Line2D.Double(anchorX, anchorY, elbowX, elbowY));
                g2.draw(new Line2D.Double(anchorX, anchorY, targetX, targetY));
            }
            else if (style.equals(PieLabelLinkStyle.QUAD_CURVE)) {
                QuadCurve2D q = new QuadCurve2D.Float();
                q.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY);
                g2.draw(q);
                g2.draw(new Line2D.Double(elbowX, elbowY, linkX, linkY));
            }
            else if (style.equals(PieLabelLinkStyle.CUBIC_CURVE)) {
                CubicCurve2D c = new CubicCurve2D .Float();
                c.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY,
                        linkX, linkY);
                g2.draw(c);
            }
        }

        TextBox tb = record.getLabel();
        tb.draw(g2, (float) targetX, (float) targetY, RectangleAnchor.LEFT);

    }

    /**
     * Tests this plot for equality with an arbitrary object.  Note that the
     * plot's dataset is NOT included in the test for equality.
     *
     * @param obj  the object to test against (<code>null</code> permitted).
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PiePlot)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        PiePlot that = (PiePlot) obj;
        if (this.pieIndex != that.pieIndex) {
            return false;
        }
        if (this.interiorGap != that.interiorGap) {
            return false;
        }
        if (this.circular != that.circular) {
            return false;
        }
        if (this.startAngle != that.startAngle) {
            return false;
        }
        if (this.direction != that.direction) {
            return false;
        }
        if (this.ignoreZeroValues != that.ignoreZeroValues) {
            return false;
        }
        if (this.ignoreNullValues != that.ignoreNullValues) {
            return false;
        }
        if (!PaintUtilities.equal(this.sectionPaint, that.sectionPaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.sectionPaintMap,
                that.sectionPaintMap)) {
            return false;
        }
        if (!PaintUtilities.equal(this.baseSectionPaint,
                that.baseSectionPaint)) {
            return false;
        }
        if (this.sectionOutlinesVisible != that.sectionOutlinesVisible) {
            return false;
        }
        if (!PaintUtilities.equal(this.sectionOutlinePaint,
                that.sectionOutlinePaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.sectionOutlinePaintMap,
                that.sectionOutlinePaintMap)) {
            return false;
        }
        if (!PaintUtilities.equal(
            this.baseSectionOutlinePaint, that.baseSectionOutlinePaint
        )) {
            return false;
        }
        if (!ObjectUtilities.equal(this.sectionOutlineStroke,
                that.sectionOutlineStroke)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.sectionOutlineStrokeMap,
                that.sectionOutlineStrokeMap)) {
            return false;
        }
        if (!ObjectUtilities.equal(
            this.baseSectionOutlineStroke, that.baseSectionOutlineStroke
        )) {
            return false;
        }
        if (!PaintUtilities.equal(this.shadowPaint, that.shadowPaint)) {
            return false;
        }
        if (!(this.shadowXOffset == that.shadowXOffset)) {
            return false;
        }
        if (!(this.shadowYOffset == that.shadowYOffset)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.explodePercentages,
                that.explodePercentages)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.labelGenerator,
                that.labelGenerator)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.labelFont, that.labelFont)) {
            return false;
        }
        if (!PaintUtilities.equal(this.labelPaint, that.labelPaint)) {
            return false;
        }
        if (!PaintUtilities.equal(this.labelBackgroundPaint,
                that.labelBackgroundPaint)) {
            return false;
        }
        if (!PaintUtilities.equal(this.labelOutlinePaint,
                that.labelOutlinePaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.labelOutlineStroke,
                that.labelOutlineStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.labelShadowPaint,
                that.labelShadowPaint)) {
            return false;
        }
        if (this.simpleLabels != that.simpleLabels) {
            return false;
        }
        if (!this.simpleLabelOffset.equals(that.simpleLabelOffset)) {
            return false;
        }
        if (!this.labelPadding.equals(that.labelPadding)) {
            return false;
        }
        if (!(this.maximumLabelWidth == that.maximumLabelWidth)) {
            return false;
        }
        if (!(this.labelGap == that.labelGap)) {
            return false;
        }
        if (!(this.labelLinkMargin == that.labelLinkMargin)) {
            return false;
        }
        if (this.labelLinksVisible != that.labelLinksVisible) {
            return false;
        }
        if (!this.labelLinkStyle.equals(that.labelLinkStyle)) {
            return false;
        }
        if (!PaintUtilities.equal(this.labelLinkPaint, that.labelLinkPaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.labelLinkStroke,
                that.labelLinkStroke)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.toolTipGenerator,
                that.toolTipGenerator)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.urlGenerator, that.urlGenerator)) {
            return false;
        }
        if (!(this.minimumArcAngleToDraw == that.minimumArcAngleToDraw)) {
            return false;
        }
        if (!ShapeUtilities.equal(this.legendItemShape, that.legendItemShape)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.legendLabelGenerator,
                that.legendLabelGenerator)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.legendLabelToolTipGenerator,
                that.legendLabelToolTipGenerator)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.legendLabelURLGenerator,
                that.legendLabelURLGenerator)) {
            return false;
        }
        if (this.autoPopulateSectionPaint != that.autoPopulateSectionPaint) {
            return false;
        }
        if (this.autoPopulateSectionOutlinePaint
                != that.autoPopulateSectionOutlinePaint) {
            return false;
        }
        if (this.autoPopulateSectionOutlineStroke
                != that.autoPopulateSectionOutlineStroke) {
            return false;
        }
        // can't find any difference...
        return true;
    }

    /**
     * Returns a clone of the plot.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if some component of the plot does
     *         not support cloning.
     */
    public Object clone() throws CloneNotSupportedException {
        PiePlot clone = (PiePlot) super.clone();
        if (clone.dataset != null) {
            clone.dataset.addChangeListener(clone);
        }
        if (this.urlGenerator instanceof PublicCloneable) {
            clone.urlGenerator = (PieURLGenerator) ObjectUtilities.clone(
                    this.urlGenerator);
        }
        clone.legendItemShape = ShapeUtilities.clone(this.legendItemShape);
        if (this.legendLabelGenerator != null) {
            clone.legendLabelGenerator = (PieSectionLabelGenerator)
                    ObjectUtilities.clone(this.legendLabelGenerator);
        }
        if (this.legendLabelToolTipGenerator != null) {
            clone.legendLabelToolTipGenerator = (PieSectionLabelGenerator)
                    ObjectUtilities.clone(this.legendLabelToolTipGenerator);
        }
        if (this.legendLabelURLGenerator instanceof PublicCloneable) {
            clone.legendLabelURLGenerator = (PieURLGenerator)
                    ObjectUtilities.clone(this.legendLabelURLGenerator);
        }
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
        SerialUtilities.writePaint(this.sectionPaint, stream);
        SerialUtilities.writePaint(this.baseSectionPaint, stream);
        SerialUtilities.writePaint(this.sectionOutlinePaint, stream);
        SerialUtilities.writePaint(this.baseSectionOutlinePaint, stream);
        SerialUtilities.writeStroke(this.sectionOutlineStroke, stream);
        SerialUtilities.writeStroke(this.baseSectionOutlineStroke, stream);
        SerialUtilities.writePaint(this.shadowPaint, stream);
        SerialUtilities.writePaint(this.labelPaint, stream);
        SerialUtilities.writePaint(this.labelBackgroundPaint, stream);
        SerialUtilities.writePaint(this.labelOutlinePaint, stream);
        SerialUtilities.writeStroke(this.labelOutlineStroke, stream);
        SerialUtilities.writePaint(this.labelShadowPaint, stream);
        SerialUtilities.writePaint(this.labelLinkPaint, stream);
        SerialUtilities.writeStroke(this.labelLinkStroke, stream);
        SerialUtilities.writeShape(this.legendItemShape, stream);
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
        this.sectionPaint = SerialUtilities.readPaint(stream);
        this.baseSectionPaint = SerialUtilities.readPaint(stream);
        this.sectionOutlinePaint = SerialUtilities.readPaint(stream);
        this.baseSectionOutlinePaint = SerialUtilities.readPaint(stream);
        this.sectionOutlineStroke = SerialUtilities.readStroke(stream);
        this.baseSectionOutlineStroke = SerialUtilities.readStroke(stream);
        this.shadowPaint = SerialUtilities.readPaint(stream);
        this.labelPaint = SerialUtilities.readPaint(stream);
        this.labelBackgroundPaint = SerialUtilities.readPaint(stream);
        this.labelOutlinePaint = SerialUtilities.readPaint(stream);
        this.labelOutlineStroke = SerialUtilities.readStroke(stream);
        this.labelShadowPaint = SerialUtilities.readPaint(stream);
        this.labelLinkPaint = SerialUtilities.readPaint(stream);
        this.labelLinkStroke = SerialUtilities.readStroke(stream);
        this.legendItemShape = SerialUtilities.readShape(stream);
    }

    // DEPRECATED FIELDS AND METHODS...

    /**
     * The paint for ALL sections (overrides list).
     *
     * @deprecated This field is redundant, it is sufficient to use
     *     sectionPaintMap and baseSectionPaint.  Deprecated as of version
     *     1.0.6.
     */
    private transient Paint sectionPaint;

    /**
     * The outline paint for ALL sections (overrides list).
     *
     * @deprecated This field is redundant, it is sufficient to use
     *     sectionOutlinePaintMap and baseSectionOutlinePaint.  Deprecated as
     *     of version 1.0.6.
     */
    private transient Paint sectionOutlinePaint;

    /**
     * The outline stroke for ALL sections (overrides list).
     *
     * @deprecated This field is redundant, it is sufficient to use
     *     sectionOutlineStrokeMap and baseSectionOutlineStroke.  Deprecated as
     *     of version 1.0.6.
     */
    private transient Stroke sectionOutlineStroke;

    /**
     * Returns the paint for the specified section.
     *
     * @param section  the section index (zero-based).
     *
     * @return The paint (never <code>null</code>).
     *
     * @deprecated Use {@link #getSectionPaint(Comparable)} instead.
     */
    public Paint getSectionPaint(int section) {
        Comparable key = getSectionKey(section);
        return getSectionPaint(key);
    }

    /**
     * Sets the paint used to fill a section of the pie and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param section  the section index (zero-based).
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @deprecated Use {@link #setSectionPaint(Comparable, Paint)} instead.
     */
    public void setSectionPaint(int section, Paint paint) {
        Comparable key = getSectionKey(section);
        setSectionPaint(key, paint);
    }

    /**
     * Returns the outline paint for ALL sections in the plot.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setSectionOutlinePaint(Paint)
     *
     * @deprecated Use {@link #getSectionOutlinePaint(Comparable)} and
     *     {@link #getBaseSectionOutlinePaint()}.  Deprecated as of version
     *     1.0.6.
     */
    public Paint getSectionOutlinePaint() {
        return this.sectionOutlinePaint;
    }

    /**
     * Sets the outline paint for ALL sections in the plot.  If this is set to
     * </code>null</code>, then a list of paints is used instead (to allow
     * different colors to be used for each section).
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getSectionOutlinePaint()
     *
     * @deprecated Use {@link #setSectionOutlinePaint(Comparable, Paint)} and
     *     {@link #setBaseSectionOutlinePaint(Paint)}.  Deprecated as of
     *     version 1.0.6.
     */
    public void setSectionOutlinePaint(Paint paint) {
        this.sectionOutlinePaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the paint for the specified section.
     *
     * @param section  the section index (zero-based).
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @deprecated Use {@link #getSectionOutlinePaint(Comparable)} instead.
     */
    public Paint getSectionOutlinePaint(int section) {
        Comparable key = getSectionKey(section);
        return getSectionOutlinePaint(key);
    }

    /**
     * Sets the paint used to fill a section of the pie and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param section  the section index (zero-based).
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @deprecated Use {@link #setSectionOutlinePaint(Comparable, Paint)}
     *     instead.
     */
    public void setSectionOutlinePaint(int section, Paint paint) {
        Comparable key = getSectionKey(section);
        setSectionOutlinePaint(key, paint);
    }

    /**
     * Returns the outline stroke for ALL sections in the plot.
     *
     * @return The stroke (possibly <code>null</code>).
     *
     * @see #setSectionOutlineStroke(Stroke)
     *
     * @deprecated Use {@link #getSectionOutlineStroke(Comparable)} and
     *     {@link #getBaseSectionOutlineStroke()}.  Deprecated as of version
     *     1.0.6.
     */
    public Stroke getSectionOutlineStroke() {
        return this.sectionOutlineStroke;
    }

    /**
     * Sets the outline stroke for ALL sections in the plot.  If this is set to
     * </code>null</code>, then a list of paints is used instead (to allow
     * different colors to be used for each section).
     *
     * @param stroke  the stroke (<code>null</code> permitted).
     *
     * @see #getSectionOutlineStroke()
     *
     * @deprecated Use {@link #setSectionOutlineStroke(Comparable, Stroke)} and
     *     {@link #setBaseSectionOutlineStroke(Stroke)}.  Deprecated as of
     *     version 1.0.6.
     */
    public void setSectionOutlineStroke(Stroke stroke) {
        this.sectionOutlineStroke = stroke;
        fireChangeEvent();
    }

    /**
     * Returns the stroke for the specified section.
     *
     * @param section  the section index (zero-based).
     *
     * @return The stroke (possibly <code>null</code>).
     *
     * @deprecated Use {@link #getSectionOutlineStroke(Comparable)} instead.
     */
    public Stroke getSectionOutlineStroke(int section) {
        Comparable key = getSectionKey(section);
        return getSectionOutlineStroke(key);
    }

    /**
     * Sets the stroke used to fill a section of the pie and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param section  the section index (zero-based).
     * @param stroke  the stroke (<code>null</code> permitted).
     *
     * @deprecated Use {@link #setSectionOutlineStroke(Comparable, Stroke)}
     *     instead.
     */
    public void setSectionOutlineStroke(int section, Stroke stroke) {
        Comparable key = getSectionKey(section);
        setSectionOutlineStroke(key, stroke);
    }

    /**
     * Returns the amount that a section should be 'exploded'.
     *
     * @param section  the section number.
     *
     * @return The amount that a section should be 'exploded'.
     *
     * @deprecated Use {@link #getExplodePercent(Comparable)} instead.
     */
    public double getExplodePercent(int section) {
        Comparable key = getSectionKey(section);
        return getExplodePercent(key);
    }

    /**
     * Sets the amount that a pie section should be exploded and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param section  the section index.
     * @param percent  the explode percentage (0.30 = 30 percent).
     *
     * @deprecated Use {@link #setExplodePercent(Comparable, double)} instead.
     */
    public void setExplodePercent(int section, double percent) {
        Comparable key = getSectionKey(section);
        setExplodePercent(key, percent);
    }

}
