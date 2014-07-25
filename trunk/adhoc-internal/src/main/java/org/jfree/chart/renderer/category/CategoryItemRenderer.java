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
 * -------------------------
 * CategoryItemRenderer.java
 * -------------------------
 *
 * (C) Copyright 2001-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Mark Watson (www.markwatson.com);
 *
 * Changes
 * -------
 * 23-Oct-2001 : Version 1 (DG);
 * 16-Jan-2002 : Renamed HorizontalCategoryItemRenderer.java
 *               --> CategoryItemRenderer.java (DG);
 * 05-Feb-2002 : Changed return type of the drawCategoryItem method from void
 *               to Shape, as part of the tooltips implementation (DG)
 *
 *               NOTE (30-May-2002) : this has subsequently been changed back
 *               to void, tooltips are now collected along with entities in
 *               ChartRenderingInfo (DG);
 *
 * 14-Mar-2002 : Added the initialise method, and changed all bar plots to use
 *               this renderer (DG);
 * 23-May-2002 : Added ChartRenderingInfo to the initialise method (DG);
 * 29-May-2002 : Added the getAxisArea(Rectangle2D) method (DG);
 * 06-Jun-2002 : Updated Javadoc comments (DG);
 * 26-Jun-2002 : Added range axis to the initialise method (DG);
 * 24-Sep-2002 : Added getLegendItem() method (DG);
 * 23-Oct-2002 : Added methods to get/setToolTipGenerator (DG);
 * 05-Nov-2002 : Replaced references to CategoryDataset with TableDataset (DG);
 * 06-Nov-2002 : Added the domain axis to the drawCategoryItem method.  Renamed
 *               drawCategoryItem() --> drawItem() (DG);
 * 20-Nov-2002 : Changed signature of drawItem() method to reflect use of
 *               TableDataset (DG);
 * 26-Nov-2002 : Replaced the isStacked() method with the getRangeType()
 *               method (DG);
 * 08-Jan-2003 : Changed getSeriesCount() --> getRowCount() and
 *               getCategoryCount() --> getColumnCount() (DG);
 * 09-Jan-2003 : Changed name of grid-line methods (DG);
 * 21-Jan-2003 : Merged TableDataset with CategoryDataset (DG);
 * 10-Apr-2003 : Changed CategoryDataset to KeyedValues2DDataset in
 *               drawItem() method (DG);
 * 29-Apr-2003 : Eliminated Renderer interface (DG);
 * 02-Sep-2003 : Fix for bug 790407 (DG);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 20-Oct-2003 : Added setOutlinePaint() method (DG);
 * 06-Feb-2004 : Added missing methods, and moved deprecated methods (DG);
 * 19-Feb-2004 : Added extra setXXXLabelsVisible() methods (DG);
 * 29-Apr-2004 : Changed Integer --> int in initialise() method (DG);
 * 18-May-2004 : Added methods for item label paint (DG);
 * 05-Nov-2004 : Added getPassCount() method and 'pass' parameter to drawItem()
 *               method (DG);
 * 07-Jan-2005 : Renamed getRangeExtent() --> findRangeBounds (DG);
 * 11-Jan-2005 : Removed deprecated code in preparation for 1.0.0 release (DG);
 * 23-Feb-2005 : Now extends LegendItemSource (DG);
 * 20-Apr-2005 : Renamed CategoryLabelGenerator
 *               --> CategoryItemLabelGenerator (DG);
 * 20-May-2005 : Added drawDomainMarker() method (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 20-Feb-2007 : Updated API docs (DG);
 * 19-Apr-2007 : Deprecated seriesVisible and seriesVisibleInLegend flags (DG);
 * 20-Apr-2007 : Deprecated paint, fillPaint, outlinePaint, stroke,
 *               outlineStroke, shape, itemLabelsVisible, itemLabelFont,
 *               itemLabelPaint, positiveItemLabelPosition,
 *               negativeItemLabelPosition and createEntities override
 *               fields (DG);
 * 26-Jun-2008 : Added new method required for crosshair support - THIS CHANGES
 *               THE API as of version 1.0.11 (DG);
 *
 */

package org.jfree.chart.renderer.category;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;

/**
 * A plug-in object that is used by the {@link CategoryPlot} class to display
 * individual data items from a {@link CategoryDataset}.
 * <p>
 * This interface defines the methods that must be provided by all renderers.
 * If you are implementing a custom renderer, you should consider extending the
 * {@link AbstractCategoryItemRenderer} class.
 * <p>
 * Most renderer attributes are defined using a "three layer" approach.  When
 * looking up an attribute (for example, the outline paint) the renderer first
 * checks to see if there is a setting (in layer 0) that applies to ALL items
 * that the renderer draws.  If there is, that setting is used, but if it is
 * <code>null</code> the renderer looks up the next layer, which contains
 * "per series" settings for the attribute (many attributes are defined on a
 * per series basis, so this is the layer that is most commonly used).  If the
 * layer 1 setting is <code>null</code>, the renderer will look up the final
 * layer, which provides a default or "base" setting.  Some attributes allow
 * the base setting to be <code>null</code>, while other attributes enforce
 * non-<code>null</code> values.
 */
public interface CategoryItemRenderer extends LegendItemSource {

    /**
     * Returns the number of passes through the dataset required by the
     * renderer.  Usually this will be one, but some renderers may use
     * a second or third pass to overlay items on top of things that were
     * drawn in an earlier pass.
     *
     * @return The pass count.
     */
    public int getPassCount();

    /**
     * Returns the plot that the renderer has been assigned to (where
     * <code>null</code> indicates that the renderer is not currently assigned
     * to a plot).
     *
     * @return The plot (possibly <code>null</code>).
     *
     * @see #setPlot(CategoryPlot)
     */
    public CategoryPlot getPlot();

    /**
     * Sets the plot that the renderer has been assigned to.  This method is
     * usually called by the {@link CategoryPlot}, in normal usage you
     * shouldn't need to call this method directly.
     *
     * @param plot  the plot (<code>null</code> not permitted).
     *
     * @see #getPlot()
     */
    public void setPlot(CategoryPlot plot);

    /**
     * Adds a change listener.
     *
     * @param listener  the listener.
     *
     * @see #removeChangeListener(RendererChangeListener)
     */
    public void addChangeListener(RendererChangeListener listener);

    /**
     * Removes a change listener.
     *
     * @param listener  the listener.
     *
     * @see #addChangeListener(RendererChangeListener)
     */
    public void removeChangeListener(RendererChangeListener listener);

    /**
     * Returns the range of values the renderer requires to display all the
     * items from the specified dataset.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @return The range (or <code>null</code> if the dataset is
     *         <code>null</code> or empty).
     */
    public Range findRangeBounds(CategoryDataset dataset);

    /**
     * Initialises the renderer.  This method will be called before the first
     * item is rendered, giving the renderer an opportunity to initialise any
     * state information it wants to maintain. The renderer can do nothing if
     * it chooses.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area inside the axes.
     * @param plot  the plot.
     * @param rendererIndex  the renderer index.
     * @param info  collects chart rendering information for return to caller.
     *
     * @return A state object (maintains state information relevant to one
     *         chart drawing).
     */
    public CategoryItemRendererState initialise(Graphics2D g2,
                                                Rectangle2D dataArea,
                                                CategoryPlot plot,
                                                int rendererIndex,
                                                PlotRenderingInfo info);

    /**
     * Returns a boolean that indicates whether or not the specified item
     * should be drawn (this is typically used to hide an entire series).
     *
     * @param series  the series index.
     * @param item  the item index.
     *
     * @return A boolean.
     */
    public boolean getItemVisible(int series, int item);

    /**
     * Returns a boolean that indicates whether or not the specified series
     * should be drawn (this is typically used to hide an entire series).
     *
     * @param series  the series index.
     *
     * @return A boolean.
     */
    public boolean isSeriesVisible(int series);

    /**
     * Returns the flag that controls the visibility of ALL series.  This flag
     * overrides the per series and default settings - you must set it to
     * <code>null</code> if you want the other settings to apply.
     *
     * @return The flag (possibly <code>null</code>).
     *
     * @see #setSeriesVisible(Boolean)
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #getSeriesVisible(int)} and
     *     {@link #getBaseSeriesVisible()}.
     */
    public Boolean getSeriesVisible();

    /**
     * Sets the flag that controls the visibility of ALL series and sends a
     * {@link RendererChangeEvent} to all registered listeners.  This flag
     * overrides the per series and default settings - you must set it to
     * <code>null</code> if you want the other settings to apply.
     *
     * @param visible  the flag (<code>null</code> permitted).
     *
     * @see #getSeriesVisible()
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesVisible(int, Boolean)}
     *     and {@link #setBaseSeriesVisible(boolean)}.
     */
    public void setSeriesVisible(Boolean visible);

    /**
     * Sets the flag that controls the visibility of ALL series and sends a
     * {@link RendererChangeEvent} to all registered listeners.  This flag
     * overrides the per series and default settings - you must set it to
     * <code>null</code> if you want the other settings to apply.
     *
     * @param visible  the flag (<code>null</code> permitted).
     * @param notify  notify listeners?
     *
     * @see #getSeriesVisible()
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesVisible(int, Boolean,
     *     boolean)} and {@link #setBaseSeriesVisible(boolean, boolean)}.
     */
    public void setSeriesVisible(Boolean visible, boolean notify);

    /**
     * Returns the flag that controls whether a series is visible.
     *
     * @param series  the series index (zero-based).
     *
     * @return The flag (possibly <code>null</code>).
     *
     * @see #setSeriesVisible(int, Boolean)
     */
    public Boolean getSeriesVisible(int series);

    /**
     * Sets the flag that controls whether a series is visible and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param visible  the flag (<code>null</code> permitted).
     *
     * @see #getSeriesVisible(int)
     */
    public void setSeriesVisible(int series, Boolean visible);

    /**
     * Sets the flag that controls whether a series is visible and, if
     * requested, sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param series  the series index.
     * @param visible  the flag (<code>null</code> permitted).
     * @param notify  notify listeners?
     *
     * @see #getSeriesVisible(int)
     */
    public void setSeriesVisible(int series, Boolean visible, boolean notify);

    /**
     * Returns the base visibility for all series.
     *
     * @return The base visibility.
     *
     * @see #setBaseSeriesVisible(boolean)
     */
    public boolean getBaseSeriesVisible();

    /**
     * Sets the base visibility and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param visible  the flag.
     *
     * @see #getBaseSeriesVisible()
     */
    public void setBaseSeriesVisible(boolean visible);

    /**
     * Sets the base visibility and, if requested, sends
     * a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the visibility.
     * @param notify  notify listeners?
     *
     * @see #getBaseSeriesVisible()
     */
    public void setBaseSeriesVisible(boolean visible, boolean notify);

    // SERIES VISIBLE IN LEGEND (not yet respected by all renderers)

    /**
     * Returns <code>true</code> if the series should be shown in the legend,
     * and <code>false</code> otherwise.
     *
     * @param series  the series index.
     *
     * @return A boolean.
     */
    public boolean isSeriesVisibleInLegend(int series);

    /**
     * Returns the flag that controls the visibility of ALL series in the
     * legend.  This flag overrides the per series and default settings - you
     * must set it to <code>null</code> if you want the other settings to
     * apply.
     *
     * @return The flag (possibly <code>null</code>).
     *
     * @see #setSeriesVisibleInLegend(Boolean)
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #getSeriesVisibleInLegend(int)}
     *     and {@link #getBaseSeriesVisibleInLegend()}.
     */
    public Boolean getSeriesVisibleInLegend();

    /**
     * Sets the flag that controls the visibility of ALL series in the legend
     * and sends a {@link RendererChangeEvent} to all registered listeners.
     * This flag overrides the per series and default settings - you must set
     * it to <code>null</code> if you want the other settings to apply.
     *
     * @param visible  the flag (<code>null</code> permitted).
     *
     * @see #getSeriesVisibleInLegend()
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesVisibleInLegend(int,
     *     Boolean)} and {@link #setBaseSeriesVisibleInLegend(boolean)}.
     */
    public void setSeriesVisibleInLegend(Boolean visible);

    /**
     * Sets the flag that controls the visibility of ALL series in the legend
     * and sends a {@link RendererChangeEvent} to all registered listeners.
     * This flag overrides the per series and default settings - you must set
     * it to <code>null</code> if you want the other settings to apply.
     *
     * @param visible  the flag (<code>null</code> permitted).
     * @param notify  notify listeners?
     *
     * @see #getSeriesVisibleInLegend()
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesVisibleInLegend(int,
     *     Boolean, boolean)} and {@link #setBaseSeriesVisibleInLegend(boolean,
     *     boolean)}.
     */
    public void setSeriesVisibleInLegend(Boolean visible, boolean notify);

    /**
     * Returns the flag that controls whether a series is visible in the
     * legend.  This method returns only the "per series" settings - to
     * incorporate the override and base settings as well, you need to use the
     * {@link #isSeriesVisibleInLegend(int)} method.
     *
     * @param series  the series index (zero-based).
     *
     * @return The flag (possibly <code>null</code>).
     *
     * @see #setSeriesVisibleInLegend(int, Boolean)
     */
    public Boolean getSeriesVisibleInLegend(int series);

    /**
     * Sets the flag that controls whether a series is visible in the legend
     * and sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param visible  the flag (<code>null</code> permitted).
     *
     * @see #getSeriesVisibleInLegend(int)
     */
    public void setSeriesVisibleInLegend(int series, Boolean visible);

    /**
     * Sets the flag that controls whether a series is visible in the legend
     * and, if requested, sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param series  the series index.
     * @param visible  the flag (<code>null</code> permitted).
     * @param notify  notify listeners?
     *
     * @see #getSeriesVisibleInLegend(int)
     */
    public void setSeriesVisibleInLegend(int series, Boolean visible,
                                         boolean notify);

    /**
     * Returns the base visibility in the legend for all series.
     *
     * @return The base visibility.
     *
     * @see #setBaseSeriesVisibleInLegend(boolean)
     */
    public boolean getBaseSeriesVisibleInLegend();

    /**
     * Sets the base visibility in the legend and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the flag.
     *
     * @see #getBaseSeriesVisibleInLegend()
     */
    public void setBaseSeriesVisibleInLegend(boolean visible);

    /**
     * Sets the base visibility in the legend and, if requested, sends
     * a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the visibility.
     * @param notify  notify listeners?
     *
     * @see #getBaseSeriesVisibleInLegend()
     */
    public void setBaseSeriesVisibleInLegend(boolean visible, boolean notify);


    //// PAINT /////////////////////////////////////////////////////////////////

    /**
     * Returns the paint used to fill data items as they are drawn.
     *
     * @param row  the row (or series) index (zero-based).
     * @param column  the column (or category) index (zero-based).
     *
     * @return The paint (never <code>null</code>).
     */
    public Paint getItemPaint(int row, int column);

    /**
     * Sets the paint to be used for ALL series, and sends a
     * {@link RendererChangeEvent} to all registered listeners.  If this is
     * <code>null</code>, the renderer will use the paint for the series.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesPaint(int, Paint)} and
     *     {@link #setBasePaint(Paint)}.
     */
    public void setPaint(Paint paint);

    /**
     * Returns the paint used to fill an item drawn by the renderer.
     *
     * @param series  the series index (zero-based).
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setSeriesPaint(int, Paint)
     */
    public Paint getSeriesPaint(int series);

    /**
     * Sets the paint used for a series and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getSeriesPaint(int)
     */
    public void setSeriesPaint(int series, Paint paint);

    // FIXME: add setSeriesPaint(int, Paint, boolean)?

    /**
     * Returns the base paint.
     *
     * @return The base paint (never <code>null</code>).
     *
     * @see #setBasePaint(Paint)
     */
    public Paint getBasePaint();

    /**
     * Sets the base paint and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getBasePaint()
     */
    public void setBasePaint(Paint paint);

    // FIXME: add setBasePaint(int, Paint, boolean)?

    //// FILL PAINT /////////////////////////////////////////////////////////

//    /**
//     * Returns the paint used to fill data items as they are drawn.
//     *
//     * @param row  the row (or series) index (zero-based).
//     * @param column  the column (or category) index (zero-based).
//     *
//     * @return The paint (never <code>null</code>).
//     */
//    public Paint getItemFillPaint(int row, int column);
//
//    /**
//     * Returns the paint used to fill an item drawn by the renderer.
//     *
//     * @param series  the series (zero-based index).
//     *
//     * @return The paint (possibly <code>null</code>).
//     *
//     * @see #setSeriesFillPaint(int, Paint)
//     */
//    public Paint getSeriesFillPaint(int series);
//
//    /**
//     * Sets the paint used for a series outline and sends a
//     * {@link RendererChangeEvent} to all registered listeners.
//     *
//     * @param series  the series index (zero-based).
//     * @param paint  the paint (<code>null</code> permitted).
//     *
//     * @see #getSeriesFillPaint(int)
//     */
//    public void setSeriesFillPaint(int series, Paint paint);
//
//    /**
//     * Returns the base outline paint.
//     *
//     * @return The paint (never <code>null</code>).
//     *
//     * @see #setBaseFillPaint(Paint)
//     */
//    public Paint getBaseFillPaint();
//
//    /**
//     * Sets the base outline paint and sends a {@link RendererChangeEvent} to
//     * all registered listeners.
//     *
//     * @param paint  the paint (<code>null</code> not permitted).
//     *
//     * @see #getBaseFillPaint()
//     */
//    public void setBaseFillPaint(Paint paint);

    //// OUTLINE PAINT /////////////////////////////////////////////////////////

    /**
     * Returns the paint used to outline data items as they are drawn.
     *
     * @param row  the row (or series) index (zero-based).
     * @param column  the column (or category) index (zero-based).
     *
     * @return The paint (never <code>null</code>).
     */
    public Paint getItemOutlinePaint(int row, int column);

    /**
     * Sets the outline paint for ALL series (optional).
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesOutlinePaint(int,
     *     Paint)} and {@link #setBaseOutlinePaint(Paint)}.
     */
    public void setOutlinePaint(Paint paint);

    /**
     * Returns the paint used to outline an item drawn by the renderer.
     *
     * @param series  the series (zero-based index).
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setSeriesOutlinePaint(int, Paint)
     */
    public Paint getSeriesOutlinePaint(int series);

    /**
     * Sets the paint used for a series outline and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getSeriesOutlinePaint(int)
     */
    public void setSeriesOutlinePaint(int series, Paint paint);

    // FIXME: add setSeriesOutlinePaint(int, Paint, boolean)?

    /**
     * Returns the base outline paint.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setBaseOutlinePaint(Paint)
     */
    public Paint getBaseOutlinePaint();

    /**
     * Sets the base outline paint and sends a {@link RendererChangeEvent} to
     * all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getBaseOutlinePaint()
     */
    public void setBaseOutlinePaint(Paint paint);

    // FIXME: add setBaseOutlinePaint(Paint, boolean)?

    //// STROKE ////////////////////////////////////////////////////////////////

    /**
     * Returns the stroke used to draw data items.
     *
     * @param row  the row (or series) index (zero-based).
     * @param column  the column (or category) index (zero-based).
     *
     * @return The stroke (never <code>null</code>).
     */
    public Stroke getItemStroke(int row, int column);

    /**
     * Sets the stroke for ALL series and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> permitted).
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesStroke(int, Stroke)}
     *     and {@link #setBaseStroke(Stroke)}.
     */
    public void setStroke(Stroke stroke);

    /**
     * Returns the stroke used to draw the items in a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The stroke (never <code>null</code>).
     *
     * @see #setSeriesStroke(int, Stroke)
     */
    public Stroke getSeriesStroke(int series);

    /**
     * Sets the stroke used for a series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param stroke  the stroke (<code>null</code> permitted).
     *
     * @see #getSeriesStroke(int)
     */
    public void setSeriesStroke(int series, Stroke stroke);

    // FIXME: add setSeriesStroke(int, Stroke, boolean) ?

    /**
     * Returns the base stroke.
     *
     * @return The base stroke (never <code>null</code>).
     *
     * @see #setBaseStroke(Stroke)
     */
    public Stroke getBaseStroke();

    /**
     * Sets the base stroke and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     *
     * @see #getBaseStroke()
     */
    public void setBaseStroke(Stroke stroke);

    // FIXME: add setBaseStroke(Stroke, boolean) ?

    //// OUTLINE STROKE ////////////////////////////////////////////////////////

    /**
     * Returns the stroke used to outline data items.
     * <p>
     * The default implementation passes control to the
     * lookupSeriesOutlineStroke method.  You can override this method if you
     * require different behaviour.
     *
     * @param row  the row (or series) index (zero-based).
     * @param column  the column (or category) index (zero-based).
     *
     * @return The stroke (never <code>null</code>).
     */
    public Stroke getItemOutlineStroke(int row, int column);

    /**
     * Sets the outline stroke for ALL series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> permitted).
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesOutlineStroke(int,
     *     Stroke)} and {@link #setBaseOutlineStroke(Stroke)}.
     */
    public void setOutlineStroke(Stroke stroke);

    /**
     * Returns the stroke used to outline the items in a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The stroke (possibly <code>null</code>).
     *
     * @see #setSeriesOutlineStroke(int, Stroke)
     */
    public Stroke getSeriesOutlineStroke(int series);

    /**
     * Sets the outline stroke used for a series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param stroke  the stroke (<code>null</code> permitted).
     *
     * @see #getSeriesOutlineStroke(int)
     */
    public void setSeriesOutlineStroke(int series, Stroke stroke);

    // FIXME: add setSeriesOutlineStroke(int, Stroke, boolean) ?

    /**
     * Returns the base outline stroke.
     *
     * @return The stroke (never <code>null</code>).
     *
     * @see #setBaseOutlineStroke(Stroke)
     */
    public Stroke getBaseOutlineStroke();

    /**
     * Sets the base outline stroke and sends a {@link RendererChangeEvent} to
     * all registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     *
     * @see #getBaseOutlineStroke()
     */
    public void setBaseOutlineStroke(Stroke stroke);

    // FIXME: add setBaseOutlineStroke(Stroke, boolean) ?

    //// SHAPE /////////////////////////////////////////////////////////////////

    /**
     * Returns a shape used to represent a data item.
     *
     * @param row  the row (or series) index (zero-based).
     * @param column  the column (or category) index (zero-based).
     *
     * @return The shape (never <code>null</code>).
     */
    public Shape getItemShape(int row, int column);

    /**
     * Sets the shape for ALL series (optional) and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param shape  the shape (<code>null</code> permitted).
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesShape(int, Shape)} and
     *     {@link #setBaseShape(Shape)}.
     */
    public void setShape(Shape shape);

    /**
     * Returns a shape used to represent the items in a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The shape (possibly <code>null</code>).
     *
     * @see #setSeriesShape(int, Shape)
     */
    public Shape getSeriesShape(int series);

    /**
     * Sets the shape used for a series and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param shape  the shape (<code>null</code> permitted).
     *
     * @see #getSeriesShape(int)
     */
    public void setSeriesShape(int series, Shape shape);

    // FIXME: add setSeriesShape(int, Shape, boolean) ?

    /**
     * Returns the base shape.
     *
     * @return The shape (never <code>null</code>).
     *
     * @see #setBaseShape(Shape)
     */
    public Shape getBaseShape();

    /**
     * Sets the base shape and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param shape  the shape (<code>null</code> not permitted).
     *
     * @see #getBaseShape()
     */
    public void setBaseShape(Shape shape);

    // FIXME: add setBaseShape(Shape, boolean) ?

    // ITEM LABELS VISIBLE

    /**
     * Returns <code>true</code> if an item label is visible, and
     * <code>false</code> otherwise.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return A boolean.
     */
    public boolean isItemLabelVisible(int row, int column);

    /**
     * Sets a flag that controls whether or not the item labels for ALL series
     * are visible.
     *
     * @param visible  the flag.
     *
     * @see #setItemLabelsVisible(Boolean)
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesItemLabelsVisible(int,
     *     Boolean)} and {@link #setBaseItemLabelsVisible(boolean)}.
     */
    public void setItemLabelsVisible(boolean visible);

    /**
     * Sets a flag that controls whether or not the item labels for ALL series
     * are visible.
     *
     * @param visible  the flag (<code>null</code> permitted).
     *
     * @see #setItemLabelsVisible(boolean)
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesItemLabelsVisible(int,
     *     Boolean)} and {@link #setBaseItemLabelsVisible(boolean)}.
     */
    public void setItemLabelsVisible(Boolean visible);

    /**
     * Sets the visibility of item labels for ALL series and, if requested,
     * sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  a flag that controls whether or not the item labels are
     *                 visible (<code>null</code> permitted).
     * @param notify  a flag that controls whether or not listeners are
     *                notified.
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesItemLabelsVisible(int,
     *     Boolean, boolean)} and {@link #setBaseItemLabelsVisible(Boolean,
     *     boolean)}.
     */
    public void setItemLabelsVisible(Boolean visible, boolean notify);

    /**
     * Returns <code>true</code> if the item labels for a series are visible,
     * and <code>false</code> otherwise.
     *
     * @param series  the series index (zero-based).
     *
     * @return A boolean.
     *
     * @see #setSeriesItemLabelsVisible(int, Boolean)
     */
    public boolean isSeriesItemLabelsVisible(int series);

    /**
     * Sets a flag that controls the visibility of the item labels for a series.
     *
     * @param series  the series index (zero-based).
     * @param visible  the flag.
     *
     * @see #isSeriesItemLabelsVisible(int)
     */
    public void setSeriesItemLabelsVisible(int series, boolean visible);

    /**
     * Sets a flag that controls the visibility of the item labels for a series.
     *
     * @param series  the series index (zero-based).
     * @param visible  the flag (<code>null</code> permitted).
     *
     * @see #isSeriesItemLabelsVisible(int)
     */
    public void setSeriesItemLabelsVisible(int series, Boolean visible);

    /**
     * Sets the visibility of item labels for a series and, if requested, sends
     * a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param visible  the visible flag.
     * @param notify  a flag that controls whether or not listeners are
     *                notified.
     *
     * @see #isSeriesItemLabelsVisible(int)
     */
    public void setSeriesItemLabelsVisible(int series, Boolean visible,
                                           boolean notify);

    /**
     * Returns the base setting for item label visibility.  A <code>null</code>
     * result should be interpreted as equivalent to <code>Boolean.FALSE</code>
     * (this is an error in the API design, the return value should have been
     * a boolean primitive).
     *
     * @return A flag (possibly <code>null</code>).
     *
     * @see #setBaseItemLabelsVisible(Boolean)
     */
    public Boolean getBaseItemLabelsVisible();

    /**
     * Sets the base flag that controls whether or not item labels are visible
     * and sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the flag.
     *
     * @see #getBaseItemLabelsVisible()
     */
    public void setBaseItemLabelsVisible(boolean visible);

    /**
     * Sets the base setting for item label visibility and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the flag (<code>null</code> permitted).
     *
     * @see #getBaseItemLabelsVisible()
     */
    public void setBaseItemLabelsVisible(Boolean visible);

    /**
     * Sets the base visibility for item labels and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the visibility flag.
     * @param notify  a flag that controls whether or not listeners are
     *                notified.
     *
     * @see #getBaseItemLabelsVisible()
     */
    public void setBaseItemLabelsVisible(Boolean visible, boolean notify);

    // ITEM LABEL GENERATOR

    /**
     * Returns the item label generator for the specified data item.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The generator (possibly <code>null</code>).
     */
    public CategoryItemLabelGenerator getItemLabelGenerator(int series,
            int item);

    /**
     * Sets the item label generator for ALL series and sends a
     * {@link RendererChangeEvent} to all registered listeners.  This overrides
     * the per-series settings.
     *
     * @param generator  the generator (<code>null</code> permitted).
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesItemLabelGenerator(int,
     *     CategoryItemLabelGenerator)} and
     *     {@link #setBaseItemLabelGenerator(CategoryItemLabelGenerator)}.
     */
    public void setItemLabelGenerator(CategoryItemLabelGenerator generator);

    /**
     * Returns the item label generator for a series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The label generator (possibly <code>null</code>).
     *
     * @see #setSeriesItemLabelGenerator(int, CategoryItemLabelGenerator)
     */
    public CategoryItemLabelGenerator getSeriesItemLabelGenerator(int series);

    /**
     * Sets the item label generator for a series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param generator  the generator.
     *
     * @see #getSeriesItemLabelGenerator(int)
     */
    public void setSeriesItemLabelGenerator(int series,
            CategoryItemLabelGenerator generator);

    // FIXME: add setSeriesItemLabelGenerator(int, CategoryItemLabelGenerator,
    //            boolean)

    /**
     * Returns the base item label generator.
     *
     * @return The generator (possibly <code>null</code>).
     *
     * @see #setBaseItemLabelGenerator(CategoryItemLabelGenerator)
     */
    public CategoryItemLabelGenerator getBaseItemLabelGenerator();

    /**
     * Sets the base item label generator and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param generator  the generator (<code>null</code> permitted).
     *
     * @see #getBaseItemLabelGenerator()
     */
    public void setBaseItemLabelGenerator(CategoryItemLabelGenerator generator);

    // FIXME: add setBaseItemLabelGenerator(CategoryItemLabelGenerator,
    //            boolean) ?

    // TOOL TIP GENERATOR

    /**
     * Returns the tool tip generator that should be used for the specified
     * item.  This method looks up the generator using the "three-layer"
     * approach outlined in the general description of this interface.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The generator (possibly <code>null</code>).
     */
    public CategoryToolTipGenerator getToolTipGenerator(int row, int column);

    /**
     * Returns the tool tip generator that will be used for ALL items in the
     * dataset (the "layer 0" generator).
     *
     * @return A tool tip generator (possibly <code>null</code>).
     *
     * @see #setToolTipGenerator(CategoryToolTipGenerator)
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #getSeriesToolTipGenerator(int)}
     *     and {@link #getBaseToolTipGenerator()}.
     */
    public CategoryToolTipGenerator getToolTipGenerator();

    /**
     * Sets the tool tip generator for ALL series and sends a
     * {@link org.jfree.chart.event.RendererChangeEvent} to all registered
     * listeners.
     *
     * @param generator  the generator (<code>null</code> permitted).
     *
     * @see #getToolTipGenerator()
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesToolTipGenerator(int,
     *     CategoryToolTipGenerator)} and
     *     {@link #setBaseToolTipGenerator(CategoryToolTipGenerator)}.
     */
    public void setToolTipGenerator(CategoryToolTipGenerator generator);

    /**
     * Returns the tool tip generator for the specified series (a "layer 1"
     * generator).
     *
     * @param series  the series index (zero-based).
     *
     * @return The tool tip generator (possibly <code>null</code>).
     *
     * @see #setSeriesToolTipGenerator(int, CategoryToolTipGenerator)
     */
    public CategoryToolTipGenerator getSeriesToolTipGenerator(int series);

    /**
     * Sets the tool tip generator for a series and sends a
     * {@link org.jfree.chart.event.RendererChangeEvent} to all registered
     * listeners.
     *
     * @param series  the series index (zero-based).
     * @param generator  the generator (<code>null</code> permitted).
     *
     * @see #getSeriesToolTipGenerator(int)
     */
    public void setSeriesToolTipGenerator(int series,
                                          CategoryToolTipGenerator generator);

    // FIXME: add setSeriesToolTipGenerator(int, CategoryToolTipGenerator,
    //            boolean) ?

    /**
     * Returns the base tool tip generator (the "layer 2" generator).
     *
     * @return The tool tip generator (possibly <code>null</code>).
     *
     * @see #setBaseToolTipGenerator(CategoryToolTipGenerator)
     */
    public CategoryToolTipGenerator getBaseToolTipGenerator();

    /**
     * Sets the base tool tip generator and sends a
     * {@link org.jfree.chart.event.RendererChangeEvent} to all registered
     * listeners.
     *
     * @param generator  the generator (<code>null</code> permitted).
     *
     * @see #getBaseToolTipGenerator()
     */
    public void setBaseToolTipGenerator(CategoryToolTipGenerator generator);

    // FIXME: add setBaseToolTipGenerator(CategoryToolTipGenerator, boolean) ?

    //// ITEM LABEL FONT  //////////////////////////////////////////////////////

    /**
     * Returns the font for an item label.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The font (never <code>null</code>).
     */
    public Font getItemLabelFont(int row, int column);

    /**
     * Returns the font used for all item labels.  This may be
     * <code>null</code>, in which case the per series font settings will apply.
     *
     * @return The font (possibly <code>null</code>).
     *
     * @see #setItemLabelFont(Font)
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #getSeriesItemLabelFont(int)} and
     *     {@link #getBaseItemLabelFont()}.
     */
    public Font getItemLabelFont();

    /**
     * Sets the item label font for ALL series and sends a
     * {@link RendererChangeEvent} to all registered listeners.  You can set
     * this to <code>null</code> if you prefer to set the font on a per series
     * basis.
     *
     * @param font  the font (<code>null</code> permitted).
     *
     * @see #getItemLabelFont()
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesItemLabelFont(int,
     *     Font)} and {@link #setBaseItemLabelFont(Font)}.
     */
    public void setItemLabelFont(Font font);

    /**
     * Returns the font for all the item labels in a series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The font (possibly <code>null</code>).
     *
     * @see #setSeriesItemLabelFont(int, Font)
     */
    public Font getSeriesItemLabelFont(int series);

    /**
     * Sets the item label font for a series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param font  the font (<code>null</code> permitted).
     *
     * @see #getSeriesItemLabelFont(int)
     */
    public void setSeriesItemLabelFont(int series, Font font);

    // FIXME: add setSeriesItemLabelFont(int, Font, boolean) ?

    /**
     * Returns the base item label font (this is used when no other font
     * setting is available).
     *
     * @return The font (<code>never</code> null).
     *
     * @see #setBaseItemLabelFont(Font)
     */
    public Font getBaseItemLabelFont();

    /**
     * Sets the base item label font and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param font  the font (<code>null</code> not permitted).
     *
     * @see #getBaseItemLabelFont()
     */
    public void setBaseItemLabelFont(Font font);

    // FIXME: add setBaseItemLabelFont(Font, boolean) ?

    //// ITEM LABEL PAINT  /////////////////////////////////////////////////////

    /**
     * Returns the paint used to draw an item label.
     *
     * @param row  the row index (zero based).
     * @param column  the column index (zero based).
     *
     * @return The paint (never <code>null</code>).
     */
    public Paint getItemLabelPaint(int row, int column);

    /**
     * Returns the paint used for all item labels.  This may be
     * <code>null</code>, in which case the per series paint settings will
     * apply.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setItemLabelPaint(Paint)
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #getSeriesItemLabelPaint(int)}
     *     and {@link #getBaseItemLabelPaint()}.
     */
    public Paint getItemLabelPaint();

    /**
     * Sets the item label paint for ALL series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getItemLabelPaint()
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesItemLabelPaint(int,
     *     Paint)} and {@link #setBaseItemLabelPaint(Paint)}.
     */
    public void setItemLabelPaint(Paint paint);

    /**
     * Returns the paint used to draw the item labels for a series.
     *
     * @param series  the series index (zero based).
     *
     * @return The paint (possibly <code>null<code>).
     *
     * @see #setSeriesItemLabelPaint(int, Paint)
     */
    public Paint getSeriesItemLabelPaint(int series);

    /**
     * Sets the item label paint for a series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series (zero based index).
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getSeriesItemLabelPaint(int)
     */
    public void setSeriesItemLabelPaint(int series, Paint paint);

    // FIXME: add setSeriesItemLabelPaint(int, Paint, boolean) ?

    /**
     * Returns the base item label paint.
     *
     * @return The paint (never <code>null<code>).
     *
     * @see #setBaseItemLabelPaint(Paint)
     */
    public Paint getBaseItemLabelPaint();

    /**
     * Sets the base item label paint and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getBaseItemLabelPaint()
     */
    public void setBaseItemLabelPaint(Paint paint);

    // FIXME: add setBaseItemLabelPaint(Paint, boolean) ?

    // POSITIVE ITEM LABEL POSITION...

    /**
     * Returns the item label position for positive values.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The item label position (never <code>null</code>).
     */
    public ItemLabelPosition getPositiveItemLabelPosition(int row, int column);

    /**
     * Returns the item label position for positive values in ALL series.
     *
     * @return The item label position (possibly <code>null</code>).
     *
     * @see #setPositiveItemLabelPosition(ItemLabelPosition)
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on
     *     {@link #getSeriesPositiveItemLabelPosition(int)}
     *     and {@link #getBasePositiveItemLabelPosition()}.
     */
    public ItemLabelPosition getPositiveItemLabelPosition();

    /**
     * Sets the item label position for positive values in ALL series, and
     * sends a {@link RendererChangeEvent} to all registered listeners.  You
     * need to set this to <code>null</code> to expose the settings for
     * individual series.
     *
     * @param position  the position (<code>null</code> permitted).
     *
     * @see #getPositiveItemLabelPosition()
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on
     *     {@link #setSeriesPositiveItemLabelPosition(int, ItemLabelPosition)}
     *     and {@link #setBasePositiveItemLabelPosition(ItemLabelPosition)}.
     */
    public void setPositiveItemLabelPosition(ItemLabelPosition position);

    /**
     * Sets the positive item label position for ALL series and (if requested)
     * sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param position  the position (<code>null</code> permitted).
     * @param notify  notify registered listeners?
     *
     * @see #getPositiveItemLabelPosition()
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on
     *     {@link #setSeriesPositiveItemLabelPosition(int, ItemLabelPosition,
     *     boolean)} and {@link #setBasePositiveItemLabelPosition(
     *     ItemLabelPosition, boolean)}.
     */
    public void setPositiveItemLabelPosition(ItemLabelPosition position,
                                             boolean notify);

    /**
     * Returns the item label position for all positive values in a series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The item label position.
     *
     * @see #setSeriesPositiveItemLabelPosition(int, ItemLabelPosition)
     */
    public ItemLabelPosition getSeriesPositiveItemLabelPosition(int series);

    /**
     * Sets the item label position for all positive values in a series and
     * sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param position  the position (<code>null</code> permitted).
     *
     * @see #getSeriesPositiveItemLabelPosition(int)
     */
    public void setSeriesPositiveItemLabelPosition(int series,
                                                   ItemLabelPosition position);

    /**
     * Sets the item label position for all positive values in a series and (if
     * requested) sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param series  the series index (zero-based).
     * @param position  the position (<code>null</code> permitted).
     * @param notify  notify registered listeners?
     *
     * @see #getSeriesPositiveItemLabelPosition(int)
     */
    public void setSeriesPositiveItemLabelPosition(int series,
            ItemLabelPosition position, boolean notify);

    /**
     * Returns the base positive item label position.
     *
     * @return The position.
     *
     * @see #setBasePositiveItemLabelPosition(ItemLabelPosition)
     */
    public ItemLabelPosition getBasePositiveItemLabelPosition();

    /**
     * Sets the base positive item label position.
     *
     * @param position  the position.
     *
     * @see #getBasePositiveItemLabelPosition()
     */
    public void setBasePositiveItemLabelPosition(ItemLabelPosition position);

    /**
     * Sets the base positive item label position and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param position  the position.
     * @param notify  notify registered listeners?
     *
     * @see #getBasePositiveItemLabelPosition()
     */
    public void setBasePositiveItemLabelPosition(ItemLabelPosition position,
                                                 boolean notify);


    // NEGATIVE ITEM LABEL POSITION...

    /**
     * Returns the item label position for negative values.  This method can be
     * overridden to provide customisation of the item label position for
     * individual data items.
     *
     * @param row  the row index (zero-based).
     * @param column  the column (zero-based).
     *
     * @return The item label position.
     */
    public ItemLabelPosition getNegativeItemLabelPosition(int row, int column);

    /**
     * Returns the item label position for negative values in ALL series.
     *
     * @return The item label position (possibly <code>null</code>).
     *
     * @see #setNegativeItemLabelPosition(ItemLabelPosition)
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on
     *     {@link #getSeriesNegativeItemLabelPosition(int)}
     *     and {@link #getBaseNegativeItemLabelPosition()}.
     */
    public ItemLabelPosition getNegativeItemLabelPosition();

    /**
     * Sets the item label position for negative values in ALL series, and
     * sends a {@link RendererChangeEvent} to all registered listeners.  You
     * need to set this to <code>null</code> to expose the settings for
     * individual series.
     *
     * @param position  the position (<code>null</code> permitted).
     *
     * @see #getNegativeItemLabelPosition()
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on
     *     {@link #setSeriesNegativeItemLabelPosition(int, ItemLabelPosition)}
     *     and {@link #setBaseNegativeItemLabelPosition(ItemLabelPosition)}.
     */
    public void setNegativeItemLabelPosition(ItemLabelPosition position);

    /**
     * Sets the item label position for negative values in ALL series and (if
     * requested) sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param position  the position (<code>null</code> permitted).
     * @param notify  notify registered listeners?
     *
     * @see #getNegativeItemLabelPosition()
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on
     *     {@link #setSeriesNegativeItemLabelPosition(int, ItemLabelPosition,
     *     boolean)} and {@link #setBaseNegativeItemLabelPosition(
     *     ItemLabelPosition, boolean)}.
     */
    public void setNegativeItemLabelPosition(ItemLabelPosition position,
                                             boolean notify);

    /**
     * Returns the item label position for all negative values in a series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The item label position.
     *
     * @see #setSeriesNegativeItemLabelPosition(int, ItemLabelPosition)
     */
    public ItemLabelPosition getSeriesNegativeItemLabelPosition(int series);

    /**
     * Sets the item label position for negative values in a series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param position  the position (<code>null</code> permitted).
     *
     * @see #getSeriesNegativeItemLabelPosition(int)
     */
    public void setSeriesNegativeItemLabelPosition(int series,
                                                   ItemLabelPosition position);

    /**
     * Sets the item label position for negative values in a series and (if
     * requested) sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param series  the series index (zero-based).
     * @param position  the position (<code>null</code> permitted).
     * @param notify  notify registered listeners?
     *
     * @see #getSeriesNegativeItemLabelPosition(int)
     */
    public void setSeriesNegativeItemLabelPosition(int series,
                                                   ItemLabelPosition position,
                                                   boolean notify);

    /**
     * Returns the base item label position for negative values.
     *
     * @return The position.
     *
     * @see #setBaseNegativeItemLabelPosition(ItemLabelPosition)
     */
    public ItemLabelPosition getBaseNegativeItemLabelPosition();

    /**
     * Sets the base item label position for negative values and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param position  the position.
     *
     * @see #getBaseNegativeItemLabelPosition()
     */
    public void setBaseNegativeItemLabelPosition(ItemLabelPosition position);

    /**
     * Sets the base negative item label position and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param position  the position.
     * @param notify  notify registered listeners?
     *
     * @see #getBaseNegativeItemLabelPosition()
     */
    public void setBaseNegativeItemLabelPosition(ItemLabelPosition position,
                                                 boolean notify);

    // CREATE ENTITIES
    // FIXME:  these methods should be defined

//    public boolean getItemCreateEntity(int series, int item);
//
//    public Boolean getSeriesCreateEntities(int series);
//
//    public void setSeriesCreateEntities(int series, Boolean create);
//
//    public void setSeriesCreateEntities(int series, Boolean create,
//            boolean notify);
//
//    public boolean getBaseCreateEntities();
//
//    public void setBaseCreateEntities(boolean create);
//
//    public void setBaseCreateEntities(boolean create, boolean notify);


    // ITEM URL GENERATOR

    /**
     * Returns the URL generator for an item.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The item URL generator.
     */
    public CategoryURLGenerator getItemURLGenerator(int series, int item);

    /**
     * Sets the item URL generator for ALL series.
     *
     * @param generator  the generator.
     *
     * @see #getSeriesItemURLGenerator(int)
     *
     * @deprecated This method should no longer be used (as of version 1.0.6).
     *     It is sufficient to rely on {@link #setSeriesItemURLGenerator(int,
     *     CategoryURLGenerator)} and
     *     {@link #setBaseItemURLGenerator(CategoryURLGenerator)}.
     */
    public void setItemURLGenerator(CategoryURLGenerator generator);

    /**
     * Returns the item URL generator for a series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The URL generator.
     *
     * @see #setSeriesItemURLGenerator(int, CategoryURLGenerator)
     */
    public CategoryURLGenerator getSeriesItemURLGenerator(int series);

    /**
     * Sets the item URL generator for a series.
     *
     * @param series  the series index (zero-based).
     * @param generator  the generator.
     *
     * @see #getSeriesItemURLGenerator(int)
     */
    public void setSeriesItemURLGenerator(int series,
                                          CategoryURLGenerator generator);

    // FIXME: add setSeriesItemURLGenerator(int, CategoryURLGenerator, boolean)?

    /**
     * Returns the base item URL generator.
     *
     * @return The item URL generator (possibly <code>null</code>).
     *
     * @see #setBaseItemURLGenerator(CategoryURLGenerator)
     */
    public CategoryURLGenerator getBaseItemURLGenerator();

    /**
     * Sets the base item URL generator and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param generator  the item URL generator (<code>null</code> permitted).
     *
     * @see #getBaseItemURLGenerator()
     */
    public void setBaseItemURLGenerator(CategoryURLGenerator generator);

    // FIXME: add setBaseItemURLGenerator(CategoryURLGenerator, boolean) ?

    /**
     * Returns a legend item for a series.  This method can return
     * <code>null</code>, in which case the series will have no entry in the
     * legend.
     *
     * @param datasetIndex  the dataset index (zero-based).
     * @param series  the series (zero-based index).
     *
     * @return The legend item (possibly <code>null</code>).
     */
    public LegendItem getLegendItem(int datasetIndex, int series);

    /**
     * Draws a background for the data area.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param dataArea  the data area.
     */
    public void drawBackground(Graphics2D g2, CategoryPlot plot,
            Rectangle2D dataArea);

    /**
     * Draws an outline for the data area.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param dataArea  the data area.
     */
    public void drawOutline(Graphics2D g2, CategoryPlot plot,
            Rectangle2D dataArea);

    /**
     * Draws a single data item.
     *
     * @param g2  the graphics device.
     * @param state  state information for one chart.
     * @param dataArea  the data plot area.
     * @param plot  the plot.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the data.
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     * @param pass  the pass index.
     */
    public void drawItem(Graphics2D g2, CategoryItemRendererState state,
            Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
            ValueAxis rangeAxis, CategoryDataset dataset, int row, int column,
            int pass);

    /**
     * Draws a grid line against the domain axis.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param dataArea  the area for plotting data (not yet adjusted for any
     *                  3D effect).
     * @param value  the value.
     *
     * @see #drawRangeGridline(Graphics2D, CategoryPlot, ValueAxis,
     *     Rectangle2D, double)
     */
    public void drawDomainGridline(Graphics2D g2, CategoryPlot plot,
            Rectangle2D dataArea, double value);

    /**
     * Draws a grid line against the range axis.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the value axis.
     * @param dataArea  the area for plotting data (not yet adjusted for any
     *                  3D effect).
     * @param value  the value.
     *
     * @see #drawDomainGridline(Graphics2D, CategoryPlot, Rectangle2D, double)
     */
    public void drawRangeGridline(Graphics2D g2, CategoryPlot plot,
            ValueAxis axis, Rectangle2D dataArea, double value);

    /**
     * Draws a line (or some other marker) to indicate a particular category on
     * the domain axis.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the category axis.
     * @param marker  the marker.
     * @param dataArea  the area for plotting data (not including 3D effect).
     *
     * @see #drawRangeMarker(Graphics2D, CategoryPlot, ValueAxis, Marker,
     *     Rectangle2D)
     */
    public void drawDomainMarker(Graphics2D g2, CategoryPlot plot,
            CategoryAxis axis, CategoryMarker marker, Rectangle2D dataArea);

    /**
     * Draws a line (or some other marker) to indicate a particular value on
     * the range axis.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the value axis.
     * @param marker  the marker.
     * @param dataArea  the area for plotting data (not including 3D effect).
     *
     * @see #drawDomainMarker(Graphics2D, CategoryPlot, CategoryAxis,
     *     CategoryMarker, Rectangle2D)
     */
    public void drawRangeMarker(Graphics2D g2, CategoryPlot plot,
            ValueAxis axis, Marker marker, Rectangle2D dataArea);

    /**
     * Returns the Java2D coordinate for the middle of the specified data item.
     *
     * @param rowKey  the row key.
     * @param columnKey  the column key.
     * @param dataset  the dataset.
     * @param axis  the axis.
     * @param area  the data area.
     * @param edge  the edge along which the axis lies.
     *
     * @return The Java2D coordinate for the middle of the item.
     *
     * @since 1.0.11
     */
    public double getItemMiddle(Comparable rowKey, Comparable columnKey,
            CategoryDataset dataset, CategoryAxis axis, Rectangle2D area,
            RectangleEdge edge);

}
