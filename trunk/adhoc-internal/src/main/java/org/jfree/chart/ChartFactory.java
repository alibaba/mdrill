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
 * -----------------
 * ChartFactory.java
 * -----------------
 * (C) Copyright 2001-2009, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Serge V. Grachov;
 *                   Joao Guilherme Del Valle;
 *                   Bill Kelemen;
 *                   Jon Iles;
 *                   Jelai Wang;
 *                   Richard Atkinson;
 *                   David Browning (for Australian Institute of Marine
 *                       Science);
 *                   Benoit Xhenseval;
 *
 * Changes
 * -------
 * 19-Oct-2001 : Version 1, most methods transferred from JFreeChart.java (DG);
 * 22-Oct-2001 : Added methods to create stacked bar charts (DG);
 *               Renamed DataSource.java --> Dataset.java etc. (DG);
 * 31-Oct-2001 : Added 3D-effect vertical bar and stacked-bar charts,
 *               contributed by Serge V. Grachov (DG);
 * 07-Nov-2001 : Added a flag to control whether or not a legend is added to
 *               the chart (DG);
 * 17-Nov-2001 : For pie chart, changed dataset from CategoryDataset to
 *               PieDataset (DG);
 * 30-Nov-2001 : Removed try/catch handlers from chart creation, as the
 *               exception are now RuntimeExceptions, as suggested by Joao
 *               Guilherme Del Valle (DG);
 * 06-Dec-2001 : Added createCombinableXXXXXCharts methods (BK);
 * 12-Dec-2001 : Added createCandlestickChart() method (DG);
 * 13-Dec-2001 : Updated methods for charts with new renderers (DG);
 * 08-Jan-2002 : Added import for
 *               com.jrefinery.chart.combination.CombinedChart (DG);
 * 31-Jan-2002 : Changed the createCombinableVerticalXYBarChart() method to use
 *               renderer (DG);
 * 06-Feb-2002 : Added new method createWindPlot() (DG);
 * 23-Apr-2002 : Updates to the chart and plot constructor API (DG);
 * 21-May-2002 : Added new method createAreaChart() (JI);
 * 06-Jun-2002 : Added new method createGanttChart() (DG);
 * 11-Jun-2002 : Renamed createHorizontalStackedBarChart()
 *               --> createStackedHorizontalBarChart() for consistency (DG);
 * 06-Aug-2002 : Updated Javadoc comments (DG);
 * 21-Aug-2002 : Added createPieChart(CategoryDataset) method (DG);
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 09-Oct-2002 : Added methods including tooltips and URL flags (DG);
 * 06-Nov-2002 : Moved renderers into a separate package (DG);
 * 18-Nov-2002 : Changed CategoryDataset to TableDataset (DG);
 * 21-Mar-2003 : Incorporated HorizontalCategoryAxis3D, see bug id 685501 (DG);
 * 13-May-2003 : Merged some horizontal and vertical methods (DG);
 * 24-May-2003 : Added support for timeline in createHighLowChart (BK);
 * 07-Jul-2003 : Added createHistogram() method contributed by Jelai Wang (DG);
 * 27-Jul-2003 : Added createStackedAreaXYChart() method (RA);
 * 05-Aug-2003 : added new method createBoxAndWhiskerChart (DB);
 * 08-Sep-2003 : Changed ValueAxis API (DG);
 * 07-Oct-2003 : Added stepped area XY chart contributed by Matthias Rose (DG);
 * 06-Nov-2003 : Added createWaterfallChart() method (DG);
 * 20-Nov-2003 : Set rendering order for 3D bar charts to fix overlapping
 *               problems (DG);
 * 25-Nov-2003 : Added createWaferMapChart() method (DG);
 * 23-Dec-2003 : Renamed createPie3DChart() --> createPieChart3D for
 *               consistency (DG);
 * 20-Jan-2004 : Added createPolarChart() method (DG);
 * 28-Jan-2004 : Fixed bug (882890) with axis range in
 *               createStackedXYAreaChart() method (DG);
 * 25-Feb-2004 : Renamed XYToolTipGenerator --> XYItemLabelGenerator (DG);
 * 11-Mar-2004 : Updated for pie chart changes (DG);
 * 27-Apr-2004 : Added new createPieChart() method contributed by Benoit
 *               Xhenseval (see RFE 942195) (DG);
 * 11-May-2004 : Split StandardCategoryItemLabelGenerator
 *               --> StandardCategoryToolTipGenerator and
 *               StandardCategoryLabelGenerator (DG);
 * 06-Jan-2005 : Removed deprecated methods (DG);
 * 27-Jan-2005 : Added new constructor to LineAndShapeRenderer (DG);
 * 28-Feb-2005 : Added docs to createBubbleChart() method (DG);
 * 17-Mar-2005 : Added createRingPlot() method (DG);
 * 21-Apr-2005 : Replaced Insets with RectangleInsets (DG);
 * 29-Nov-2005 : Removed signal chart (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 26-Jan-2006 : Corrected API docs for createScatterPlot() (DG);
 * 23-Aug-2006 : Modified createStackedXYAreaChart() to use
 *               StackedXYAreaRenderer2, because StackedXYAreaRenderer doesn't
 *               handle negative values (DG);
 * 27-Sep-2006 : Update createPieChart() method for deprecated code (DG);
 * 29-Nov-2006 : Update createXYBarChart() to use a time based tool tip
 *               generator is a DateAxis is requested (DG);
 * 17-Jan-2007 : Added createBoxAndWhiskerChart() method from patch 1603937
 *               submitted by Darren Jung (DG);
 * 10-Jul-2007 : Added new methods to create pie charts with locale for
 *               section label and tool tip formatting (DG);
 * 14-Aug-2008 : Added ChartTheme facility (DG);
 * 23-Oct-2008 : Check for legacy theme in setChartTheme() and reset default
 *               bar painters (DG);
 * 20-Dec-2008 : In createStackedAreaChart(), set category margin to 0.0 (DG);
 *
 */

package org.jfree.chart;

import java.awt.Color;
import java.awt.Font;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.axis.Timeline;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.labels.IntervalCategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.StandardXYZToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.WaferMapPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.chart.renderer.WaferMapRenderer;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.chart.renderer.category.GradientBarPainter;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.LineRenderer3D;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.category.WaterfallBarRenderer;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.GradientXYBarPainter;
import org.jfree.chart.renderer.xy.HighLowRenderer;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.WindItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYBoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.chart.urls.StandardXYZURLGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.general.WaferMapDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.WindDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.util.SortOrder;
import org.jfree.util.TableOrder;

/**
 * A collection of utility methods for creating some standard charts with
 * JFreeChart.
 */
public abstract class ChartFactory {

    /** The chart theme. */
    private static ChartTheme currentTheme = new StandardChartTheme("JFree");

    /**
     * Returns the current chart theme used by the factory.
     *
     * @return The chart theme.
     *
     * @see #setChartTheme(ChartTheme)
     * @see ChartUtilities#applyCurrentTheme(JFreeChart)
     *
     * @since 1.0.11
     */
    public static ChartTheme getChartTheme() {
        return currentTheme;
    }

    /**
     * Sets the current chart theme.  This will be applied to all new charts
     * created via methods in this class.
     *
     * @param theme  the theme (<code>null</code> not permitted).
     *
     * @see #getChartTheme()
     * @see ChartUtilities#applyCurrentTheme(JFreeChart)
     *
     * @since 1.0.11
     */
    public static void setChartTheme(ChartTheme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("Null 'theme' argument.");
        }
        currentTheme = theme;

        // here we do a check to see if the user is installing the "Legacy"
        // theme, and reset the bar painters in that case...
        if (theme instanceof StandardChartTheme) {
            StandardChartTheme sct = (StandardChartTheme) theme;
            if (sct.getName().equals("Legacy")) {
                BarRenderer.setDefaultBarPainter(new StandardBarPainter());
                XYBarRenderer.setDefaultBarPainter(new StandardXYBarPainter());
            }
            else {
                BarRenderer.setDefaultBarPainter(new GradientBarPainter());
                XYBarRenderer.setDefaultBarPainter(new GradientXYBarPainter());
            }
        }
    }

    /**
     * Creates a pie chart with default settings.
     * <P>
     * The chart object returned by this method uses a {@link PiePlot} instance
     * as the plot.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param locale  the locale (<code>null</code> not permitted).
     *
     * @return A pie chart.
     *
     * @since 1.0.7
     */
    public static JFreeChart createPieChart(String title, PieDataset dataset,
            boolean legend, boolean tooltips, Locale locale) {

        PiePlot plot = new PiePlot(dataset);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(locale));
        plot.setInsets(new RectangleInsets(0.0, 5.0, 5.0, 5.0));
        if (tooltips) {
            plot.setToolTipGenerator(new StandardPieToolTipGenerator(locale));
        }
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a pie chart with default settings.
     * <P>
     * The chart object returned by this method uses a {@link PiePlot} instance
     * as the plot.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A pie chart.
     */
    public static JFreeChart createPieChart(String title,
                                            PieDataset dataset,
                                            boolean legend,
                                            boolean tooltips,
                                            boolean urls) {

        PiePlot plot = new PiePlot(dataset);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator());
        plot.setInsets(new RectangleInsets(0.0, 5.0, 5.0, 5.0));
        if (tooltips) {
            plot.setToolTipGenerator(new StandardPieToolTipGenerator());
        }
        if (urls) {
            plot.setURLGenerator(new StandardPieURLGenerator());
        }
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;
    }

    /**
     * Creates a pie chart with default settings that compares 2 datasets.
     * The colour of each section will be determined by the move from the value
     * for the same key in <code>previousDataset</code>. ie if value1 > value2
     * then the section will be in green (unless <code>greenForIncrease</code>
     * is <code>false</code>, in which case it would be <code>red</code>).
     * Each section can have a shade of red or green as the difference can be
     * tailored between 0% (black) and percentDiffForMaxScale% (bright
     * red/green).
     * <p>
     * For instance if <code>percentDiffForMaxScale</code> is 10 (10%), a
     * difference of 5% will have a half shade of red/green, a difference of
     * 10% or more will have a maximum shade/brightness of red/green.
     * <P>
     * The chart object returned by this method uses a {@link PiePlot} instance
     * as the plot.
     * <p>
     * Written by <a href="mailto:opensource@objectlab.co.uk">Benoit
     * Xhenseval</a>.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param previousDataset  the dataset for the last run, this will be used
     *                         to compare each key in the dataset
     * @param percentDiffForMaxScale scale goes from bright red/green to black,
     *                               percentDiffForMaxScale indicate the change
     *                               required to reach top scale.
     * @param greenForIncrease  an increase since previousDataset will be
     *                          displayed in green (decrease red) if true.
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param locale  the locale (<code>null</code> not permitted).
     * @param subTitle displays a subtitle with colour scheme if true
     * @param showDifference  create a new dataset that will show the %
     *                        difference between the two datasets.
     *
     * @return A pie chart.
     *
     * @since 1.0.7
     */
    public static JFreeChart createPieChart(String title, PieDataset dataset,
            PieDataset previousDataset, int percentDiffForMaxScale,
            boolean greenForIncrease, boolean legend, boolean tooltips,
            Locale locale, boolean subTitle, boolean showDifference) {

        PiePlot plot = new PiePlot(dataset);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(locale));
        plot.setInsets(new RectangleInsets(0.0, 5.0, 5.0, 5.0));

        if (tooltips) {
            plot.setToolTipGenerator(new StandardPieToolTipGenerator(locale));
        }

        List keys = dataset.getKeys();
        DefaultPieDataset series = null;
        if (showDifference) {
            series = new DefaultPieDataset();
        }

        double colorPerPercent = 255.0 / percentDiffForMaxScale;
        for (Iterator it = keys.iterator(); it.hasNext();) {
            Comparable key = (Comparable) it.next();
            Number newValue = dataset.getValue(key);
            Number oldValue = previousDataset.getValue(key);

            if (oldValue == null) {
                if (greenForIncrease) {
                    plot.setSectionPaint(key, Color.green);
                }
                else {
                    plot.setSectionPaint(key, Color.red);
                }
                if (showDifference) {
                    series.setValue(key + " (+100%)", newValue);
                }
            }
            else {
                double percentChange = (newValue.doubleValue()
                        / oldValue.doubleValue() - 1.0) * 100.0;
                double shade
                    = (Math.abs(percentChange) >= percentDiffForMaxScale ? 255
                    : Math.abs(percentChange) * colorPerPercent);
                if (greenForIncrease
                        && newValue.doubleValue() > oldValue.doubleValue()
                        || !greenForIncrease && newValue.doubleValue()
                        < oldValue.doubleValue()) {
                    plot.setSectionPaint(key, new Color(0, (int) shade, 0));
                }
                else {
                    plot.setSectionPaint(key, new Color((int) shade, 0, 0));
                }
                if (showDifference) {
                    series.setValue(key + " (" + (percentChange >= 0 ? "+" : "")
                            + NumberFormat.getPercentInstance().format(
                            percentChange / 100.0) + ")", newValue);
                }
            }
        }

        if (showDifference) {
            plot.setDataset(series);
        }

        JFreeChart chart =  new JFreeChart(title,
                JFreeChart.DEFAULT_TITLE_FONT, plot, legend);

        if (subTitle) {
            TextTitle subtitle = null;
            subtitle = new TextTitle("Bright " + (greenForIncrease ? "red"
                    : "green") + "=change >=-" + percentDiffForMaxScale
                    + "%, Bright " + (!greenForIncrease ? "red" : "green")
                    + "=change >=+" + percentDiffForMaxScale + "%",
                    new Font("SansSerif", Font.PLAIN, 10));
            chart.addSubtitle(subtitle);
        }
        currentTheme.apply(chart);
        return chart;
    }

    /**
     * Creates a pie chart with default settings that compares 2 datasets.
     * The colour of each section will be determined by the move from the value
     * for the same key in <code>previousDataset</code>. ie if value1 > value2
     * then the section will be in green (unless <code>greenForIncrease</code>
     * is <code>false</code>, in which case it would be <code>red</code>).
     * Each section can have a shade of red or green as the difference can be
     * tailored between 0% (black) and percentDiffForMaxScale% (bright
     * red/green).
     * <p>
     * For instance if <code>percentDiffForMaxScale</code> is 10 (10%), a
     * difference of 5% will have a half shade of red/green, a difference of
     * 10% or more will have a maximum shade/brightness of red/green.
     * <P>
     * The chart object returned by this method uses a {@link PiePlot} instance
     * as the plot.
     * <p>
     * Written by <a href="mailto:opensource@objectlab.co.uk">Benoit
     * Xhenseval</a>.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param previousDataset  the dataset for the last run, this will be used
     *                         to compare each key in the dataset
     * @param percentDiffForMaxScale scale goes from bright red/green to black,
     *                               percentDiffForMaxScale indicate the change
     *                               required to reach top scale.
     * @param greenForIncrease  an increase since previousDataset will be
     *                          displayed in green (decrease red) if true.
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     * @param subTitle displays a subtitle with colour scheme if true
     * @param showDifference  create a new dataset that will show the %
     *                        difference between the two datasets.
     *
     * @return A pie chart.
     */
    public static JFreeChart createPieChart(String title,
                                            PieDataset dataset,
                                            PieDataset previousDataset,
                                            int percentDiffForMaxScale,
                                            boolean greenForIncrease,
                                            boolean legend,
                                            boolean tooltips,
                                            boolean urls,
                                            boolean subTitle,
                                            boolean showDifference) {

        PiePlot plot = new PiePlot(dataset);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator());
        plot.setInsets(new RectangleInsets(0.0, 5.0, 5.0, 5.0));

        if (tooltips) {
            plot.setToolTipGenerator(new StandardPieToolTipGenerator());
        }
        if (urls) {
            plot.setURLGenerator(new StandardPieURLGenerator());
        }

        List keys = dataset.getKeys();
        DefaultPieDataset series = null;
        if (showDifference) {
            series = new DefaultPieDataset();
        }

        double colorPerPercent = 255.0 / percentDiffForMaxScale;
        for (Iterator it = keys.iterator(); it.hasNext();) {
            Comparable key = (Comparable) it.next();
            Number newValue = dataset.getValue(key);
            Number oldValue = previousDataset.getValue(key);

            if (oldValue == null) {
                if (greenForIncrease) {
                    plot.setSectionPaint(key, Color.green);
                }
                else {
                    plot.setSectionPaint(key, Color.red);
                }
                if (showDifference) {
                    series.setValue(key + " (+100%)", newValue);
                }
            }
            else {
                double percentChange = (newValue.doubleValue()
                        / oldValue.doubleValue() - 1.0) * 100.0;
                double shade
                    = (Math.abs(percentChange) >= percentDiffForMaxScale ? 255
                    : Math.abs(percentChange) * colorPerPercent);
                if (greenForIncrease
                        && newValue.doubleValue() > oldValue.doubleValue()
                        || !greenForIncrease && newValue.doubleValue()
                        < oldValue.doubleValue()) {
                    plot.setSectionPaint(key, new Color(0, (int) shade, 0));
                }
                else {
                    plot.setSectionPaint(key, new Color((int) shade, 0, 0));
                }
                if (showDifference) {
                    series.setValue(key + " (" + (percentChange >= 0 ? "+" : "")
                            + NumberFormat.getPercentInstance().format(
                            percentChange / 100.0) + ")", newValue);
                }
            }
        }

        if (showDifference) {
            plot.setDataset(series);
        }

        JFreeChart chart =  new JFreeChart(title,
                JFreeChart.DEFAULT_TITLE_FONT, plot, legend);

        if (subTitle) {
            TextTitle subtitle = null;
            subtitle = new TextTitle("Bright " + (greenForIncrease ? "red"
                    : "green") + "=change >=-" + percentDiffForMaxScale
                    + "%, Bright " + (!greenForIncrease ? "red" : "green")
                    + "=change >=+" + percentDiffForMaxScale + "%",
                    new Font("SansSerif", Font.PLAIN, 10));
            chart.addSubtitle(subtitle);
        }
        currentTheme.apply(chart);
        return chart;
    }

    /**
     * Creates a ring chart with default settings.
     * <P>
     * The chart object returned by this method uses a {@link RingPlot}
     * instance as the plot.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param locale  the locale (<code>null</code> not permitted).
     *
     * @return A ring chart.
     *
     * @since 1.0.7
     */
    public static JFreeChart createRingChart(String title, PieDataset dataset,
            boolean legend, boolean tooltips, Locale locale) {

        RingPlot plot = new RingPlot(dataset);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(locale));
        plot.setInsets(new RectangleInsets(0.0, 5.0, 5.0, 5.0));
        if (tooltips) {
            plot.setToolTipGenerator(new StandardPieToolTipGenerator(locale));
        }
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;
    }

    /**
     * Creates a ring chart with default settings.
     * <P>
     * The chart object returned by this method uses a {@link RingPlot}
     * instance as the plot.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A ring chart.
     */
    public static JFreeChart createRingChart(String title,
                                             PieDataset dataset,
                                             boolean legend,
                                             boolean tooltips,
                                             boolean urls) {

        RingPlot plot = new RingPlot(dataset);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator());
        plot.setInsets(new RectangleInsets(0.0, 5.0, 5.0, 5.0));
        if (tooltips) {
            plot.setToolTipGenerator(new StandardPieToolTipGenerator());
        }
        if (urls) {
            plot.setURLGenerator(new StandardPieURLGenerator());
        }
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a chart that displays multiple pie plots.  The chart object
     * returned by this method uses a {@link MultiplePiePlot} instance as the
     * plot.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param dataset  the dataset (<code>null</code> permitted).
     * @param order  the order that the data is extracted (by row or by column)
     *               (<code>null</code> not permitted).
     * @param legend  include a legend?
     * @param tooltips  generate tooltips?
     * @param urls  generate URLs?
     *
     * @return A chart.
     */
    public static JFreeChart createMultiplePieChart(String title,
                                                    CategoryDataset dataset,
                                                    TableOrder order,
                                                    boolean legend,
                                                    boolean tooltips,
                                                    boolean urls) {

        if (order == null) {
            throw new IllegalArgumentException("Null 'order' argument.");
        }
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        plot.setDataExtractOrder(order);
        plot.setBackgroundPaint(null);
        plot.setOutlineStroke(null);

        if (tooltips) {
            PieToolTipGenerator tooltipGenerator
                = new StandardPieToolTipGenerator();
            PiePlot pp = (PiePlot) plot.getPieChart().getPlot();
            pp.setToolTipGenerator(tooltipGenerator);
        }

        if (urls) {
            PieURLGenerator urlGenerator = new StandardPieURLGenerator();
            PiePlot pp = (PiePlot) plot.getPieChart().getPlot();
            pp.setURLGenerator(urlGenerator);
        }

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a 3D pie chart using the specified dataset.  The chart object
     * returned by this method uses a {@link PiePlot3D} instance as the
     * plot.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param locale  the locale (<code>null</code> not permitted).
     *
     * @return A pie chart.
     *
     * @since 1.0.7
     */
    public static JFreeChart createPieChart3D(String title, PieDataset dataset,
            boolean legend, boolean tooltips, Locale locale) {

        PiePlot3D plot = new PiePlot3D(dataset);
        plot.setInsets(new RectangleInsets(0.0, 5.0, 5.0, 5.0));
        if (tooltips) {
            plot.setToolTipGenerator(new StandardPieToolTipGenerator(locale));
        }
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a 3D pie chart using the specified dataset.  The chart object
     * returned by this method uses a {@link PiePlot3D} instance as the
     * plot.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A pie chart.
     */
    public static JFreeChart createPieChart3D(String title,
                                              PieDataset dataset,
                                              boolean legend,
                                              boolean tooltips,
                                              boolean urls) {

        PiePlot3D plot = new PiePlot3D(dataset);
        plot.setInsets(new RectangleInsets(0.0, 5.0, 5.0, 5.0));
        if (tooltips) {
            plot.setToolTipGenerator(new StandardPieToolTipGenerator());
        }
        if (urls) {
            plot.setURLGenerator(new StandardPieURLGenerator());
        }
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a chart that displays multiple pie plots.  The chart object
     * returned by this method uses a {@link MultiplePiePlot} instance as the
     * plot.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param dataset  the dataset (<code>null</code> permitted).
     * @param order  the order that the data is extracted (by row or by column)
     *               (<code>null</code> not permitted).
     * @param legend  include a legend?
     * @param tooltips  generate tooltips?
     * @param urls  generate URLs?
     *
     * @return A chart.
     */
    public static JFreeChart createMultiplePieChart3D(String title,
                                                      CategoryDataset dataset,
                                                      TableOrder order,
                                                      boolean legend,
                                                      boolean tooltips,
                                                      boolean urls) {

        if (order == null) {
            throw new IllegalArgumentException("Null 'order' argument.");
        }
        MultiplePiePlot plot = new MultiplePiePlot(dataset);
        plot.setDataExtractOrder(order);
        plot.setBackgroundPaint(null);
        plot.setOutlineStroke(null);

        JFreeChart pieChart = new JFreeChart(new PiePlot3D(null));
        TextTitle seriesTitle = new TextTitle("Series Title",
                new Font("SansSerif", Font.BOLD, 12));
        seriesTitle.setPosition(RectangleEdge.BOTTOM);
        pieChart.setTitle(seriesTitle);
        pieChart.removeLegend();
        pieChart.setBackgroundPaint(null);
        plot.setPieChart(pieChart);

        if (tooltips) {
            PieToolTipGenerator tooltipGenerator
                = new StandardPieToolTipGenerator();
            PiePlot pp = (PiePlot) plot.getPieChart().getPlot();
            pp.setToolTipGenerator(tooltipGenerator);
        }

        if (urls) {
            PieURLGenerator urlGenerator = new StandardPieURLGenerator();
            PiePlot pp = (PiePlot) plot.getPieChart().getPlot();
            pp.setURLGenerator(urlGenerator);
        }

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a bar chart.  The chart object returned by this method uses a
     * {@link CategoryPlot} instance as the plot, with a {@link CategoryAxis}
     * for the domain axis, a {@link NumberAxis} as the range axis, and a
     * {@link BarRenderer} as the renderer.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param categoryAxisLabel  the label for the category axis
     *                           (<code>null</code> permitted).
     * @param valueAxisLabel  the label for the value axis
     *                        (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the plot orientation (horizontal or vertical)
     *                     (<code>null</code> not permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A bar chart.
     */
    public static JFreeChart createBarChart(String title,
                                            String categoryAxisLabel,
                                            String valueAxisLabel,
                                            CategoryDataset dataset,
                                            PlotOrientation orientation,
                                            boolean legend,
                                            boolean tooltips,
                                            boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        CategoryAxis categoryAxis = new CategoryAxis(categoryAxisLabel);
        ValueAxis valueAxis = new NumberAxis(valueAxisLabel);

        BarRenderer renderer = new BarRenderer();
        if (orientation == PlotOrientation.HORIZONTAL) {
            ItemLabelPosition position1 = new ItemLabelPosition(
                    ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT);
            renderer.setBasePositiveItemLabelPosition(position1);
            ItemLabelPosition position2 = new ItemLabelPosition(
                    ItemLabelAnchor.OUTSIDE9, TextAnchor.CENTER_RIGHT);
            renderer.setBaseNegativeItemLabelPosition(position2);
         }
        else if (orientation == PlotOrientation.VERTICAL) {
            ItemLabelPosition position1 = new ItemLabelPosition(
                    ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER);
            renderer.setBasePositiveItemLabelPosition(position1);
            ItemLabelPosition position2 = new ItemLabelPosition(
                    ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER);
            renderer.setBaseNegativeItemLabelPosition(position2);
        }
        if (tooltips) {
            renderer.setBaseToolTipGenerator(
                    new StandardCategoryToolTipGenerator());
        }
        if (urls) {
            renderer.setBaseItemURLGenerator(
                    new StandardCategoryURLGenerator());
        }

        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis,
                renderer);
        plot.setOrientation(orientation);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a stacked bar chart with default settings.  The chart object
     * returned by this method uses a {@link CategoryPlot} instance as the
     * plot, with a {@link CategoryAxis} for the domain axis, a
     * {@link NumberAxis} as the range axis, and a {@link StackedBarRenderer}
     * as the renderer.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param domainAxisLabel  the label for the category axis
     *                         (<code>null</code> permitted).
     * @param rangeAxisLabel  the label for the value axis
     *                        (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the orientation of the chart (horizontal or
     *                     vertical) (<code>null</code> not permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A stacked bar chart.
     */
    public static JFreeChart createStackedBarChart(String title,
                                                   String domainAxisLabel,
                                                   String rangeAxisLabel,
                                                   CategoryDataset dataset,
                                                   PlotOrientation orientation,
                                                   boolean legend,
                                                   boolean tooltips,
                                                   boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }

        CategoryAxis categoryAxis = new CategoryAxis(domainAxisLabel);
        ValueAxis valueAxis = new NumberAxis(rangeAxisLabel);

        StackedBarRenderer renderer = new StackedBarRenderer();
        if (tooltips) {
            renderer.setBaseToolTipGenerator(
                    new StandardCategoryToolTipGenerator());
        }
        if (urls) {
            renderer.setBaseItemURLGenerator(
                    new StandardCategoryURLGenerator());
        }

        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis,
                renderer);
        plot.setOrientation(orientation);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a bar chart with a 3D effect. The chart object returned by this
     * method uses a {@link CategoryPlot} instance as the plot, with a
     * {@link CategoryAxis3D} for the domain axis, a {@link NumberAxis3D} as
     * the range axis, and a {@link BarRenderer3D} as the renderer.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param categoryAxisLabel  the label for the category axis
     *                           (<code>null</code> permitted).
     * @param valueAxisLabel  the label for the value axis (<code>null</code>
     *                        permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the plot orientation (horizontal or vertical)
     *                     (<code>null</code> not permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A bar chart with a 3D effect.
     */
    public static JFreeChart createBarChart3D(String title,
                                              String categoryAxisLabel,
                                              String valueAxisLabel,
                                              CategoryDataset dataset,
                                              PlotOrientation orientation,
                                              boolean legend,
                                              boolean tooltips,
                                              boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        CategoryAxis categoryAxis = new CategoryAxis3D(categoryAxisLabel);
        ValueAxis valueAxis = new NumberAxis3D(valueAxisLabel);

        BarRenderer3D renderer = new BarRenderer3D();
        if (tooltips) {
            renderer.setBaseToolTipGenerator(
                    new StandardCategoryToolTipGenerator());
        }
        if (urls) {
            renderer.setBaseItemURLGenerator(
                    new StandardCategoryURLGenerator());
        }

        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis,
                renderer);
        plot.setOrientation(orientation);
        if (orientation == PlotOrientation.HORIZONTAL) {
            // change rendering order to ensure that bar overlapping is the
            // right way around
            plot.setRowRenderingOrder(SortOrder.DESCENDING);
            plot.setColumnRenderingOrder(SortOrder.DESCENDING);
        }
        plot.setForegroundAlpha(0.75f);

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a stacked bar chart with a 3D effect and default settings. The
     * chart object returned by this method uses a {@link CategoryPlot}
     * instance as the plot, with a {@link CategoryAxis3D} for the domain axis,
     * a {@link NumberAxis3D} as the range axis, and a
     * {@link StackedBarRenderer3D} as the renderer.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param categoryAxisLabel  the label for the category axis
     *                           (<code>null</code> permitted).
     * @param valueAxisLabel  the label for the value axis (<code>null</code>
     *                        permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the orientation (horizontal or vertical)
     *                     (<code>null</code> not permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A stacked bar chart with a 3D effect.
     */
    public static JFreeChart createStackedBarChart3D(String title,
                                                    String categoryAxisLabel,
                                                    String valueAxisLabel,
                                                    CategoryDataset dataset,
                                                    PlotOrientation orientation,
                                                    boolean legend,
                                                    boolean tooltips,
                                                    boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        CategoryAxis categoryAxis = new CategoryAxis3D(categoryAxisLabel);
        ValueAxis valueAxis = new NumberAxis3D(valueAxisLabel);

        // create the renderer...
        CategoryItemRenderer renderer = new StackedBarRenderer3D();
        if (tooltips) {
            renderer.setBaseToolTipGenerator(
                    new StandardCategoryToolTipGenerator());
        }
        if (urls) {
            renderer.setBaseItemURLGenerator(
                    new StandardCategoryURLGenerator());
        }

        // create the plot...
        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis,
                renderer);
        plot.setOrientation(orientation);
        if (orientation == PlotOrientation.HORIZONTAL) {
            // change rendering order to ensure that bar overlapping is the
            // right way around
            plot.setColumnRenderingOrder(SortOrder.DESCENDING);
        }

        // create the chart...
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates an area chart with default settings.  The chart object returned
     * by this method uses a {@link CategoryPlot} instance as the plot, with a
     * {@link CategoryAxis} for the domain axis, a {@link NumberAxis} as the
     * range axis, and an {@link AreaRenderer} as the renderer.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param categoryAxisLabel  the label for the category axis
     *                           (<code>null</code> permitted).
     * @param valueAxisLabel  the label for the value axis (<code>null</code>
     *                        permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the plot orientation (<code>null</code> not
     *                     permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return An area chart.
     */
    public static JFreeChart createAreaChart(String title,
                                             String categoryAxisLabel,
                                             String valueAxisLabel,
                                             CategoryDataset dataset,
                                             PlotOrientation orientation,
                                             boolean legend,
                                             boolean tooltips,
                                             boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        CategoryAxis categoryAxis = new CategoryAxis(categoryAxisLabel);
        categoryAxis.setCategoryMargin(0.0);

        ValueAxis valueAxis = new NumberAxis(valueAxisLabel);

        AreaRenderer renderer = new AreaRenderer();
        if (tooltips) {
            renderer.setBaseToolTipGenerator(
                    new StandardCategoryToolTipGenerator());
        }
        if (urls) {
            renderer.setBaseItemURLGenerator(
                    new StandardCategoryURLGenerator());
        }

        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis,
                renderer);
        plot.setOrientation(orientation);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a stacked area chart with default settings.  The chart object
     * returned by this method uses a {@link CategoryPlot} instance as the
     * plot, with a {@link CategoryAxis} for the domain axis, a
     * {@link NumberAxis} as the range axis, and a {@link StackedAreaRenderer}
     * as the renderer.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param categoryAxisLabel  the label for the category axis
     *                           (<code>null</code> permitted).
     * @param valueAxisLabel  the label for the value axis (<code>null</code>
     *                        permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the plot orientation (horizontal or vertical)
     *                     (<code>null</code> not permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A stacked area chart.
     */
    public static JFreeChart createStackedAreaChart(String title,
            String categoryAxisLabel, String valueAxisLabel,
            CategoryDataset dataset, PlotOrientation orientation,
            boolean legend, boolean tooltips, boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        CategoryAxis categoryAxis = new CategoryAxis(categoryAxisLabel);
        categoryAxis.setCategoryMargin(0.0);
        ValueAxis valueAxis = new NumberAxis(valueAxisLabel);

        StackedAreaRenderer renderer = new StackedAreaRenderer();
        if (tooltips) {
            renderer.setBaseToolTipGenerator(
                    new StandardCategoryToolTipGenerator());
        }
        if (urls) {
            renderer.setBaseItemURLGenerator(
                    new StandardCategoryURLGenerator());
        }

        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis,
                renderer);
        plot.setOrientation(orientation);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a line chart with default settings.  The chart object returned
     * by this method uses a {@link CategoryPlot} instance as the plot, with a
     * {@link CategoryAxis} for the domain axis, a {@link NumberAxis} as the
     * range axis, and a {@link LineAndShapeRenderer} as the renderer.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param categoryAxisLabel  the label for the category axis
     *                           (<code>null</code> permitted).
     * @param valueAxisLabel  the label for the value axis (<code>null</code>
     *                        permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the chart orientation (horizontal or vertical)
     *                     (<code>null</code> not permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A line chart.
     */
    public static JFreeChart createLineChart(String title,
                                             String categoryAxisLabel,
                                             String valueAxisLabel,
                                             CategoryDataset dataset,
                                             PlotOrientation orientation,
                                             boolean legend,
                                             boolean tooltips,
                                             boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        CategoryAxis categoryAxis = new CategoryAxis(categoryAxisLabel);
        ValueAxis valueAxis = new NumberAxis(valueAxisLabel);

        LineAndShapeRenderer renderer = new LineAndShapeRenderer(true, false);
        if (tooltips) {
            renderer.setBaseToolTipGenerator(
                    new StandardCategoryToolTipGenerator());
        }
        if (urls) {
            renderer.setBaseItemURLGenerator(
                    new StandardCategoryURLGenerator());
        }
        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis,
                renderer);
        plot.setOrientation(orientation);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a line chart with default settings. The chart object returned by
     * this method uses a {@link CategoryPlot} instance as the plot, with a
     * {@link CategoryAxis3D} for the domain axis, a {@link NumberAxis3D} as
     * the range axis, and a {@link LineRenderer3D} as the renderer.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param categoryAxisLabel  the label for the category axis
     *                           (<code>null</code> permitted).
     * @param valueAxisLabel  the label for the value axis (<code>null</code>
     *                        permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the chart orientation (horizontal or vertical)
     *                     (<code>null</code> not permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A line chart.
     */
    public static JFreeChart createLineChart3D(String title,
                                               String categoryAxisLabel,
                                               String valueAxisLabel,
                                               CategoryDataset dataset,
                                               PlotOrientation orientation,
                                               boolean legend,
                                               boolean tooltips,
                                               boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        CategoryAxis categoryAxis = new CategoryAxis3D(categoryAxisLabel);
        ValueAxis valueAxis = new NumberAxis3D(valueAxisLabel);

        LineRenderer3D renderer = new LineRenderer3D();
        if (tooltips) {
            renderer.setBaseToolTipGenerator(
                    new StandardCategoryToolTipGenerator());
        }
        if (urls) {
            renderer.setBaseItemURLGenerator(
                    new StandardCategoryURLGenerator());
        }
        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis,
                renderer);
        plot.setOrientation(orientation);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a Gantt chart using the supplied attributes plus default values
     * where required.  The chart object returned by this method uses a
     * {@link CategoryPlot} instance as the plot, with a {@link CategoryAxis}
     * for the domain axis, a {@link DateAxis} as the range axis, and a
     * {@link GanttRenderer} as the renderer.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param categoryAxisLabel  the label for the category axis
     *                           (<code>null</code> permitted).
     * @param dateAxisLabel  the label for the date axis
     *                       (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A Gantt chart.
     */
    public static JFreeChart createGanttChart(String title,
                                              String categoryAxisLabel,
                                              String dateAxisLabel,
                                              IntervalCategoryDataset dataset,
                                              boolean legend,
                                              boolean tooltips,
                                              boolean urls) {

        CategoryAxis categoryAxis = new CategoryAxis(categoryAxisLabel);
        DateAxis dateAxis = new DateAxis(dateAxisLabel);

        CategoryItemRenderer renderer = new GanttRenderer();
        if (tooltips) {
            renderer.setBaseToolTipGenerator(
                    new IntervalCategoryToolTipGenerator(
                    "{3} - {4}", DateFormat.getDateInstance()));
        }
        if (urls) {
            renderer.setBaseItemURLGenerator(
                    new StandardCategoryURLGenerator());
        }

        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, dateAxis,
                renderer);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a waterfall chart.  The chart object returned by this method
     * uses a {@link CategoryPlot} instance as the plot, with a
     * {@link CategoryAxis} for the domain axis, a {@link NumberAxis} as the
     * range axis, and a {@link WaterfallBarRenderer} as the renderer.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param categoryAxisLabel  the label for the category axis
     *                           (<code>null</code> permitted).
     * @param valueAxisLabel  the label for the value axis (<code>null</code>
     *                        permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the plot orientation (horizontal or vertical)
     *                     (<code>null</code> NOT permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A waterfall chart.
     */
    public static JFreeChart createWaterfallChart(String title,
                                                  String categoryAxisLabel,
                                                  String valueAxisLabel,
                                                  CategoryDataset dataset,
                                                  PlotOrientation orientation,
                                                  boolean legend,
                                                  boolean tooltips,
                                                  boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        CategoryAxis categoryAxis = new CategoryAxis(categoryAxisLabel);
        categoryAxis.setCategoryMargin(0.0);

        ValueAxis valueAxis = new NumberAxis(valueAxisLabel);

        WaterfallBarRenderer renderer = new WaterfallBarRenderer();
        if (orientation == PlotOrientation.HORIZONTAL) {
            ItemLabelPosition position = new ItemLabelPosition(
                    ItemLabelAnchor.CENTER, TextAnchor.CENTER,
                    TextAnchor.CENTER, Math.PI / 2.0);
            renderer.setBasePositiveItemLabelPosition(position);
            renderer.setBaseNegativeItemLabelPosition(position);
         }
        else if (orientation == PlotOrientation.VERTICAL) {
            ItemLabelPosition position = new ItemLabelPosition(
                    ItemLabelAnchor.CENTER, TextAnchor.CENTER,
                    TextAnchor.CENTER, 0.0);
            renderer.setBasePositiveItemLabelPosition(position);
            renderer.setBaseNegativeItemLabelPosition(position);
        }
        if (tooltips) {
            StandardCategoryToolTipGenerator generator
                = new StandardCategoryToolTipGenerator();
            renderer.setBaseToolTipGenerator(generator);
        }
        if (urls) {
            renderer.setBaseItemURLGenerator(
                    new StandardCategoryURLGenerator());
        }

        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis,
                renderer);
        plot.clearRangeMarkers();
        Marker baseline = new ValueMarker(0.0);
        baseline.setPaint(Color.black);
        plot.addRangeMarker(baseline, Layer.FOREGROUND);
        plot.setOrientation(orientation);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a polar plot for the specified dataset (x-values interpreted as
     * angles in degrees).  The chart object returned by this method uses a
     * {@link PolarPlot} instance as the plot, with a {@link NumberAxis} for
     * the radial axis.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param dataset  the dataset (<code>null</code> permitted).
     * @param legend  legend required?
     * @param tooltips  tooltips required?
     * @param urls  URLs required?
     *
     * @return A chart.
     */
    public static JFreeChart createPolarChart(String title,
                                              XYDataset dataset,
                                              boolean legend,
                                              boolean tooltips,
                                              boolean urls) {

        PolarPlot plot = new PolarPlot();
        plot.setDataset(dataset);
        NumberAxis rangeAxis = new NumberAxis();
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);
        rangeAxis.setTickLabelInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        plot.setAxis(rangeAxis);
        plot.setRenderer(new DefaultPolarItemRenderer());
        JFreeChart chart = new JFreeChart(
                title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a scatter plot with default settings.  The chart object
     * returned by this method uses an {@link XYPlot} instance as the plot,
     * with a {@link NumberAxis} for the domain axis, a  {@link NumberAxis}
     * as the range axis, and an {@link XYLineAndShapeRenderer} as the
     * renderer.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param xAxisLabel  a label for the X-axis (<code>null</code> permitted).
     * @param yAxisLabel  a label for the Y-axis (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the plot orientation (horizontal or vertical)
     *                     (<code>null</code> NOT permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A scatter plot.
     */
    public static JFreeChart createScatterPlot(String title, String xAxisLabel,
            String yAxisLabel, XYDataset dataset, PlotOrientation orientation,
            boolean legend, boolean tooltips, boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        NumberAxis xAxis = new NumberAxis(xAxisLabel);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis(yAxisLabel);
        yAxis.setAutoRangeIncludesZero(false);

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);

        XYToolTipGenerator toolTipGenerator = null;
        if (tooltips) {
            toolTipGenerator = new StandardXYToolTipGenerator();
        }

        XYURLGenerator urlGenerator = null;
        if (urls) {
            urlGenerator = new StandardXYURLGenerator();
        }
        XYItemRenderer renderer = new XYLineAndShapeRenderer(false, true);
        renderer.setBaseToolTipGenerator(toolTipGenerator);
        renderer.setURLGenerator(urlGenerator);
        plot.setRenderer(renderer);
        plot.setOrientation(orientation);

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates and returns a default instance of an XY bar chart.
     * <P>
     * The chart object returned by this method uses an {@link XYPlot} instance
     * as the plot, with a {@link DateAxis} for the domain axis, a
     * {@link NumberAxis} as the range axis, and a {@link XYBarRenderer} as the
     * renderer.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param xAxisLabel  a label for the X-axis (<code>null</code> permitted).
     * @param dateAxis  make the domain axis display dates?
     * @param yAxisLabel  a label for the Y-axis (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the orientation (horizontal or vertical)
     *                     (<code>null</code> NOT permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return An XY bar chart.
     */
    public static JFreeChart createXYBarChart(String title,
                                              String xAxisLabel,
                                              boolean dateAxis,
                                              String yAxisLabel,
                                              IntervalXYDataset dataset,
                                              PlotOrientation orientation,
                                              boolean legend,
                                              boolean tooltips,
                                              boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        ValueAxis domainAxis = null;
        if (dateAxis) {
            domainAxis = new DateAxis(xAxisLabel);
        }
        else {
            NumberAxis axis = new NumberAxis(xAxisLabel);
            axis.setAutoRangeIncludesZero(false);
            domainAxis = axis;
        }
        ValueAxis valueAxis = new NumberAxis(yAxisLabel);

        XYBarRenderer renderer = new XYBarRenderer();
        if (tooltips) {
            XYToolTipGenerator tt;
            if (dateAxis) {
                tt = StandardXYToolTipGenerator.getTimeSeriesInstance();
            }
            else {
                tt = new StandardXYToolTipGenerator();
            }
            renderer.setBaseToolTipGenerator(tt);
        }
        if (urls) {
            renderer.setURLGenerator(new StandardXYURLGenerator());
        }

        XYPlot plot = new XYPlot(dataset, domainAxis, valueAxis, renderer);
        plot.setOrientation(orientation);

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates an area chart using an {@link XYDataset}.
     * <P>
     * The chart object returned by this method uses an {@link XYPlot} instance
     * as the plot, with a {@link NumberAxis} for the domain axis, a
     * {@link NumberAxis} as the range axis, and a {@link XYAreaRenderer} as
     * the renderer.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param xAxisLabel  a label for the X-axis (<code>null</code> permitted).
     * @param yAxisLabel  a label for the Y-axis (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the plot orientation (horizontal or vertical)
     *                     (<code>null</code> NOT permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return An XY area chart.
     */
    public static JFreeChart createXYAreaChart(String title,
                                               String xAxisLabel,
                                               String yAxisLabel,
                                               XYDataset dataset,
                                               PlotOrientation orientation,
                                               boolean legend,
                                               boolean tooltips,
                                               boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        NumberAxis xAxis = new NumberAxis(xAxisLabel);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis(yAxisLabel);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);
        plot.setOrientation(orientation);
        plot.setForegroundAlpha(0.5f);

        XYToolTipGenerator tipGenerator = null;
        if (tooltips) {
            tipGenerator = new StandardXYToolTipGenerator();
        }

        XYURLGenerator urlGenerator = null;
        if (urls) {
            urlGenerator = new StandardXYURLGenerator();
        }

        plot.setRenderer(new XYAreaRenderer(XYAreaRenderer.AREA, tipGenerator,
                urlGenerator));
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a stacked XY area plot.  The chart object returned by this
     * method uses an {@link XYPlot} instance as the plot, with a
     * {@link NumberAxis} for the domain axis, a {@link NumberAxis} as the
     * range axis, and a {@link StackedXYAreaRenderer2} as the renderer.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param xAxisLabel  a label for the X-axis (<code>null</code> permitted).
     * @param yAxisLabel  a label for the Y-axis (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the plot orientation (horizontal or vertical)
     *                     (<code>null</code> NOT permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A stacked XY area chart.
     */
    public static JFreeChart createStackedXYAreaChart(String title,
                                                    String xAxisLabel,
                                                    String yAxisLabel,
                                                    TableXYDataset dataset,
                                                    PlotOrientation orientation,
                                                    boolean legend,
                                                    boolean tooltips,
                                                    boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        NumberAxis xAxis = new NumberAxis(xAxisLabel);
        xAxis.setAutoRangeIncludesZero(false);
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        NumberAxis yAxis = new NumberAxis(yAxisLabel);
        XYToolTipGenerator toolTipGenerator = null;
        if (tooltips) {
            toolTipGenerator = new StandardXYToolTipGenerator();
        }

        XYURLGenerator urlGenerator = null;
        if (urls) {
            urlGenerator = new StandardXYURLGenerator();
        }
        StackedXYAreaRenderer2 renderer = new StackedXYAreaRenderer2(
                toolTipGenerator, urlGenerator);
        renderer.setOutline(true);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setOrientation(orientation);

        plot.setRangeAxis(yAxis);  // forces recalculation of the axis range

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a line chart (based on an {@link XYDataset}) with default
     * settings.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param xAxisLabel  a label for the X-axis (<code>null</code> permitted).
     * @param yAxisLabel  a label for the Y-axis (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the plot orientation (horizontal or vertical)
     *                     (<code>null</code> NOT permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return The chart.
     */
    public static JFreeChart createXYLineChart(String title,
                                               String xAxisLabel,
                                               String yAxisLabel,
                                               XYDataset dataset,
                                               PlotOrientation orientation,
                                               boolean legend,
                                               boolean tooltips,
                                               boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        NumberAxis xAxis = new NumberAxis(xAxisLabel);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis(yAxisLabel);
        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setOrientation(orientation);
        if (tooltips) {
            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        }
        if (urls) {
            renderer.setURLGenerator(new StandardXYURLGenerator());
        }

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a stepped XY plot with default settings.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param xAxisLabel  a label for the X-axis (<code>null</code> permitted).
     * @param yAxisLabel  a label for the Y-axis (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the plot orientation (horizontal or vertical)
     *                     (<code>null</code> NOT permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A chart.
     */
    public static JFreeChart createXYStepChart(String title,
                                               String xAxisLabel,
                                               String yAxisLabel,
                                               XYDataset dataset,
                                               PlotOrientation orientation,
                                               boolean legend,
                                               boolean tooltips,
                                               boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        DateAxis xAxis = new DateAxis(xAxisLabel);
        NumberAxis yAxis = new NumberAxis(yAxisLabel);
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        XYToolTipGenerator toolTipGenerator = null;
        if (tooltips) {
            toolTipGenerator = new StandardXYToolTipGenerator();
        }

        XYURLGenerator urlGenerator = null;
        if (urls) {
            urlGenerator = new StandardXYURLGenerator();
        }
        XYItemRenderer renderer = new XYStepRenderer(toolTipGenerator,
                urlGenerator);

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);
        plot.setRenderer(renderer);
        plot.setOrientation(orientation);
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a filled stepped XY plot with default settings.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param xAxisLabel  a label for the X-axis (<code>null</code> permitted).
     * @param yAxisLabel  a label for the Y-axis (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the plot orientation (horizontal or vertical)
     *                     (<code>null</code> NOT permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A chart.
     */
    public static JFreeChart createXYStepAreaChart(String title,
                                                   String xAxisLabel,
                                                   String yAxisLabel,
                                                   XYDataset dataset,
                                                   PlotOrientation orientation,
                                                   boolean legend,
                                                   boolean tooltips,
                                                   boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        NumberAxis xAxis = new NumberAxis(xAxisLabel);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis(yAxisLabel);

        XYToolTipGenerator toolTipGenerator = null;
        if (tooltips) {
            toolTipGenerator = new StandardXYToolTipGenerator();
        }

        XYURLGenerator urlGenerator = null;
        if (urls) {
            urlGenerator = new StandardXYURLGenerator();
        }
        XYItemRenderer renderer = new XYStepAreaRenderer(
                XYStepAreaRenderer.AREA_AND_SHAPES, toolTipGenerator,
                urlGenerator);

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);
        plot.setRenderer(renderer);
        plot.setOrientation(orientation);
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;
    }

    /**
     * Creates and returns a time series chart.  A time series chart is an
     * {@link XYPlot} with a {@link DateAxis} for the x-axis and a
     * {@link NumberAxis} for the y-axis.  The default renderer is an
     * {@link XYLineAndShapeRenderer}.
     * <P>
     * A convenient dataset to use with this chart is a
     * {@link org.jfree.data.time.TimeSeriesCollection}.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param timeAxisLabel  a label for the time axis (<code>null</code>
     *                       permitted).
     * @param valueAxisLabel  a label for the value axis (<code>null</code>
     *                        permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A time series chart.
     */
    public static JFreeChart createTimeSeriesChart(String title,
                                                   String timeAxisLabel,
                                                   String valueAxisLabel,
                                                   XYDataset dataset,
                                                   boolean legend,
                                                   boolean tooltips,
                                                   boolean urls) {

        ValueAxis timeAxis = new DateAxis(timeAxisLabel);
        timeAxis.setLowerMargin(0.02);  // reduce the default margins
        timeAxis.setUpperMargin(0.02);
        NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
        valueAxis.setAutoRangeIncludesZero(false);  // override default
        XYPlot plot = new XYPlot(dataset, timeAxis, valueAxis, null);

        XYToolTipGenerator toolTipGenerator = null;
        if (tooltips) {
            toolTipGenerator
                = StandardXYToolTipGenerator.getTimeSeriesInstance();
        }

        XYURLGenerator urlGenerator = null;
        if (urls) {
            urlGenerator = new StandardXYURLGenerator();
        }

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true,
                false);
        renderer.setBaseToolTipGenerator(toolTipGenerator);
        renderer.setURLGenerator(urlGenerator);
        plot.setRenderer(renderer);

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates and returns a default instance of a candlesticks chart.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param timeAxisLabel  a label for the time axis (<code>null</code>
     *                       permitted).
     * @param valueAxisLabel  a label for the value axis (<code>null</code>
     *                        permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     *
     * @return A candlestick chart.
     */
    public static JFreeChart createCandlestickChart(String title,
                                                    String timeAxisLabel,
                                                    String valueAxisLabel,
                                                    OHLCDataset dataset,
                                                    boolean legend) {

        ValueAxis timeAxis = new DateAxis(timeAxisLabel);
        NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
        XYPlot plot = new XYPlot(dataset, timeAxis, valueAxis, null);
        plot.setRenderer(new CandlestickRenderer());
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates and returns a default instance of a high-low-open-close chart.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param timeAxisLabel  a label for the time axis (<code>null</code>
     *                       permitted).
     * @param valueAxisLabel  a label for the value axis (<code>null</code>
     *                        permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     *
     * @return A high-low-open-close chart.
     */
    public static JFreeChart createHighLowChart(String title,
                                                String timeAxisLabel,
                                                String valueAxisLabel,
                                                OHLCDataset dataset,
                                                boolean legend) {

        ValueAxis timeAxis = new DateAxis(timeAxisLabel);
        NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
        HighLowRenderer renderer = new HighLowRenderer();
        renderer.setBaseToolTipGenerator(new HighLowItemLabelGenerator());
        XYPlot plot = new XYPlot(dataset, timeAxis, valueAxis, renderer);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates and returns a default instance of a high-low-open-close chart
     * with a special timeline. This timeline can be a
     * {@link org.jfree.chart.axis.SegmentedTimeline} such as the Monday
     * through Friday timeline that will remove Saturdays and Sundays from
     * the axis.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param timeAxisLabel  a label for the time axis (<code>null</code>
     *                       permitted).
     * @param valueAxisLabel  a label for the value axis (<code>null</code>
     *                        permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param timeline  the timeline.
     * @param legend  a flag specifying whether or not a legend is required.
     *
     * @return A high-low-open-close chart.
     */
    public static JFreeChart createHighLowChart(String title,
                                                String timeAxisLabel,
                                                String valueAxisLabel,
                                                OHLCDataset dataset,
                                                Timeline timeline,
                                                boolean legend) {

        DateAxis timeAxis = new DateAxis(timeAxisLabel);
        timeAxis.setTimeline(timeline);
        NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
        HighLowRenderer renderer = new HighLowRenderer();
        renderer.setBaseToolTipGenerator(new HighLowItemLabelGenerator());
        XYPlot plot = new XYPlot(dataset, timeAxis, valueAxis, renderer);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a bubble chart with default settings.  The chart is composed of
     * an {@link XYPlot}, with a {@link NumberAxis} for the domain axis,
     * a {@link NumberAxis} for the range axis, and an {@link XYBubbleRenderer}
     * to draw the data items.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param xAxisLabel  a label for the X-axis (<code>null</code> permitted).
     * @param yAxisLabel  a label for the Y-axis (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the orientation (horizontal or vertical)
     *                     (<code>null</code> NOT permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A bubble chart.
     */
    public static JFreeChart createBubbleChart(String title,
                                               String xAxisLabel,
                                               String yAxisLabel,
                                               XYZDataset dataset,
                                               PlotOrientation orientation,
                                               boolean legend,
                                               boolean tooltips,
                                               boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        NumberAxis xAxis = new NumberAxis(xAxisLabel);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis(yAxisLabel);
        yAxis.setAutoRangeIncludesZero(false);

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);

        XYItemRenderer renderer = new XYBubbleRenderer(
                XYBubbleRenderer.SCALE_ON_RANGE_AXIS);
        if (tooltips) {
            renderer.setBaseToolTipGenerator(new StandardXYZToolTipGenerator());
        }
        if (urls) {
            renderer.setURLGenerator(new StandardXYZURLGenerator());
        }
        plot.setRenderer(renderer);
        plot.setOrientation(orientation);

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a histogram chart.  This chart is constructed with an
     * {@link XYPlot} using an {@link XYBarRenderer}.  The domain and range
     * axes are {@link NumberAxis} instances.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param xAxisLabel  the x axis label (<code>null</code> permitted).
     * @param yAxisLabel  the y axis label (<code>null</code> permitted).
     * @param dataset  the dataset (<code>null</code> permitted).
     * @param orientation  the orientation (horizontal or vertical)
     *                     (<code>null</code> NOT permitted).
     * @param legend  create a legend?
     * @param tooltips  display tooltips?
     * @param urls  generate URLs?
     *
     * @return The chart.
     */
    public static JFreeChart createHistogram(String title,
            String xAxisLabel, String yAxisLabel, IntervalXYDataset dataset,
            PlotOrientation orientation, boolean legend, boolean tooltips,
            boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        NumberAxis xAxis = new NumberAxis(xAxisLabel);
        xAxis.setAutoRangeIncludesZero(false);
        ValueAxis yAxis = new NumberAxis(yAxisLabel);

        XYItemRenderer renderer = new XYBarRenderer();
        if (tooltips) {
            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        }
        if (urls) {
            renderer.setURLGenerator(new StandardXYURLGenerator());
        }

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setOrientation(orientation);
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates and returns a default instance of a box and whisker chart
     * based on data from a {@link BoxAndWhiskerCategoryDataset}.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param categoryAxisLabel  a label for the category axis
     *     (<code>null</code> permitted).
     * @param valueAxisLabel  a label for the value axis (<code>null</code>
     *     permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     *
     * @return A box and whisker chart.
     *
     * @since 1.0.4
     */
    public static JFreeChart createBoxAndWhiskerChart(String title,
            String categoryAxisLabel, String valueAxisLabel,
            BoxAndWhiskerCategoryDataset dataset, boolean legend) {

        CategoryAxis categoryAxis = new CategoryAxis(categoryAxisLabel);
        NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
        valueAxis.setAutoRangeIncludesZero(false);

        BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        renderer.setBaseToolTipGenerator(new BoxAndWhiskerToolTipGenerator());

        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis,
                renderer);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;
    }

    /**
     * Creates and returns a default instance of a box and whisker chart.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param timeAxisLabel  a label for the time axis (<code>null</code>
     *                       permitted).
     * @param valueAxisLabel  a label for the value axis (<code>null</code>
     *                        permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     *
     * @return A box and whisker chart.
     */
    public static JFreeChart createBoxAndWhiskerChart(String title,
                                                 String timeAxisLabel,
                                                 String valueAxisLabel,
                                                 BoxAndWhiskerXYDataset dataset,
                                                 boolean legend) {

        ValueAxis timeAxis = new DateAxis(timeAxisLabel);
        NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
        valueAxis.setAutoRangeIncludesZero(false);
        XYBoxAndWhiskerRenderer renderer = new XYBoxAndWhiskerRenderer(10.0);
        XYPlot plot = new XYPlot(dataset, timeAxis, valueAxis, renderer);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a wind plot with default settings.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param xAxisLabel  a label for the x-axis (<code>null</code> permitted).
     * @param yAxisLabel  a label for the y-axis (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param legend  a flag that controls whether or not a legend is created.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A wind plot.
     *
     */
    public static JFreeChart createWindPlot(String title,
                                            String xAxisLabel,
                                            String yAxisLabel,
                                            WindDataset dataset,
                                            boolean legend,
                                            boolean tooltips,
                                            boolean urls) {

        ValueAxis xAxis = new DateAxis(xAxisLabel);
        ValueAxis yAxis = new NumberAxis(yAxisLabel);
        yAxis.setRange(-12.0, 12.0);

        WindItemRenderer renderer = new WindItemRenderer();
        if (tooltips) {
            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        }
        if (urls) {
            renderer.setURLGenerator(new StandardXYURLGenerator());
        }
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;

    }

    /**
     * Creates a wafer map chart.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param dataset  the dataset (<code>null</code> permitted).
     * @param orientation  the plot orientation (horizontal or vertical)
     *                     (<code>null</code> NOT permitted.
     * @param legend  display a legend?
     * @param tooltips  generate tooltips?
     * @param urls  generate URLs?
     *
     * @return A wafer map chart.
     */
    public static JFreeChart createWaferMapChart(String title,
                                                 WaferMapDataset dataset,
                                                 PlotOrientation orientation,
                                                 boolean legend,
                                                 boolean tooltips,
                                                 boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        WaferMapPlot plot = new WaferMapPlot(dataset);
        WaferMapRenderer renderer = new WaferMapRenderer();
        plot.setRenderer(renderer);

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        currentTheme.apply(chart);
        return chart;
    }

}
