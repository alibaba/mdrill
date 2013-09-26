/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
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
 * StackedBarRenderer3D.java
 * -------------------------
 * (C) Copyright 2000-2009, by Serge V. Grachov and Contributors.
 *
 * Original Author:  Serge V. Grachov;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Richard Atkinson;
 *                   Christian W. Zuckschwerdt;
 *                   Max Herfort (patch 1459313);
 *
 * Changes
 * -------
 * 31-Oct-2001 : Version 1, contributed by Serge V. Grachov (DG);
 * 15-Nov-2001 : Modified to allow for null data values (DG);
 * 13-Dec-2001 : Added tooltips (DG);
 * 15-Feb-2002 : Added isStacked() method (DG);
 * 24-May-2002 : Incorporated tooltips into chart entities (DG);
 * 19-Jun-2002 : Added check for null info in drawCategoryItem method (DG);
 * 25-Jun-2002 : Removed redundant imports (DG);
 * 26-Jun-2002 : Small change to entity (DG);
 * 05-Aug-2002 : Small modification to drawCategoryItem method to support URLs
 *               for HTML image maps (RA);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 24-Oct-2002 : Amendments for changes in CategoryDataset interface and
 *               CategoryToolTipGenerator interface (DG);
 * 05-Nov-2002 : Replaced references to CategoryDataset with TableDataset (DG);
 * 26-Nov-2002 : Replaced isStacked() method with getRangeType() method (DG);
 * 17-Jan-2003 : Moved plot classes to a separate package (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 01-May-2003 : Added default constructor (bug 726235) and fixed bug
 *               726260) (DG);
 * 13-May-2003 : Renamed StackedVerticalBarRenderer3D
 *               --> StackedBarRenderer3D (DG);
 * 30-Jul-2003 : Modified entity constructor (CZ);
 * 07-Oct-2003 : Added renderer state (DG);
 * 21-Nov-2003 : Added a new constructor (DG);
 * 27-Nov-2003 : Modified code to respect maxBarWidth setting (DG);
 * 11-Aug-2004 : Fixed bug where isDrawBarOutline() was ignored (DG);
 * 05-Nov-2004 : Modified drawItem() signature (DG);
 * 07-Jan-2005 : Renamed getRangeExtent() --> findRangeBounds (DG);
 * 18-Mar-2005 : Override for getPassCount() method (DG);
 * 20-Apr-2005 : Renamed CategoryLabelGenerator
 *               --> CategoryItemLabelGenerator (DG);
 * 09-Jun-2005 : Use addItemEntity() method from superclass (DG);
 * 22-Sep-2005 : Renamed getMaxBarWidth() --> getMaximumBarWidth() (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 31-Mar-2006 : Added renderAsPercentages option - see patch 1459313 submitted
 *               by Max Herfort (DG);
 * 16-Jan-2007 : Replaced rendering code to draw whole stack at once (DG);
 * 18-Jan-2007 : Fixed bug handling null values in createStackedValueList()
 *               method (DG);
 * 18-Jan-2007 : Updated block drawing code to take account of inverted axes,
 *               see bug report 1599652 (DG);
 * 08-May-2007 : Fixed bugs 1713401 (drawBarOutlines flag) and  1713474
 *               (shading) (DG);
 * 15-Aug-2008 : Fixed bug 2031407 - no negative zero for stack encoding (DG);
 * 03-Feb-2009 : Fixed regression in findRangeBounds() method for null
 *               dataset (DG);
 * 04-Feb-2009 : Handle seriesVisible flag (DG);
 *
 */

package org.jfree.chart.renderer.category;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.DataUtilities;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.util.BooleanUtilities;
import org.jfree.util.PublicCloneable;

/**
 * Renders stacked bars with 3D-effect, for use with the {@link CategoryPlot}
 * class.  The example shown here is generated by the
 * <code>StackedBarChart3DDemo1.java</code> program included in the
 * JFreeChart Demo Collection:
 * <br><br>
 * <img src="../../../../../images/StackedBarRenderer3DSample.png"
 * alt="StackedBarRenderer3DSample.png" />
 */
public class StackedBarRenderer3D extends BarRenderer3D
        implements Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -5832945916493247123L;

    /** A flag that controls whether the bars display values or percentages. */
    private boolean renderAsPercentages;

    /**
     * Creates a new renderer with no tool tip generator and no URL generator.
     * <P>
     * The defaults (no tool tip or URL generators) have been chosen to
     * minimise the processing required to generate a default chart.  If you
     * require tool tips or URLs, then you can easily add the required
     * generators.
     */
    public StackedBarRenderer3D() {
        this(false);
    }

    /**
     * Constructs a new renderer with the specified '3D effect'.
     *
     * @param xOffset  the x-offset for the 3D effect.
     * @param yOffset  the y-offset for the 3D effect.
     */
    public StackedBarRenderer3D(double xOffset, double yOffset) {
        super(xOffset, yOffset);
    }

    /**
     * Creates a new renderer.
     *
     * @param renderAsPercentages  a flag that controls whether the data values
     *                             are rendered as percentages.
     *
     * @since 1.0.2
     */
    public StackedBarRenderer3D(boolean renderAsPercentages) {
        super();
        this.renderAsPercentages = renderAsPercentages;
    }

    /**
     * Constructs a new renderer with the specified '3D effect'.
     *
     * @param xOffset  the x-offset for the 3D effect.
     * @param yOffset  the y-offset for the 3D effect.
     * @param renderAsPercentages  a flag that controls whether the data values
     *                             are rendered as percentages.
     *
     * @since 1.0.2
     */
    public StackedBarRenderer3D(double xOffset, double yOffset,
            boolean renderAsPercentages) {
        super(xOffset, yOffset);
        this.renderAsPercentages = renderAsPercentages;
    }

    /**
     * Returns <code>true</code> if the renderer displays each item value as
     * a percentage (so that the stacked bars add to 100%), and
     * <code>false</code> otherwise.
     *
     * @return A boolean.
     *
     * @since 1.0.2
     */
    public boolean getRenderAsPercentages() {
        return this.renderAsPercentages;
    }

    /**
     * Sets the flag that controls whether the renderer displays each item
     * value as a percentage (so that the stacked bars add to 100%), and sends
     * a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param asPercentages  the flag.
     *
     * @since 1.0.2
     */
    public void setRenderAsPercentages(boolean asPercentages) {
        this.renderAsPercentages = asPercentages;
        fireChangeEvent();
    }

    /**
     * Returns the range of values the renderer requires to display all the
     * items from the specified dataset.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     *
     * @return The range (or <code>null</code> if the dataset is empty).
     */
    public Range findRangeBounds(CategoryDataset dataset) {
        if (dataset == null) {
            return null;
        }
        if (this.renderAsPercentages) {
            return new Range(0.0, 1.0);
        }
        else {
            return DatasetUtilities.findStackedRangeBounds(dataset);
        }
    }

    /**
     * Calculates the bar width and stores it in the renderer state.
     *
     * @param plot  the plot.
     * @param dataArea  the data area.
     * @param rendererIndex  the renderer index.
     * @param state  the renderer state.
     */
    protected void calculateBarWidth(CategoryPlot plot,
                                     Rectangle2D dataArea,
                                     int rendererIndex,
                                     CategoryItemRendererState state) {

        // calculate the bar width
        CategoryAxis domainAxis = getDomainAxis(plot, rendererIndex);
        CategoryDataset data = plot.getDataset(rendererIndex);
        if (data != null) {
            PlotOrientation orientation = plot.getOrientation();
            double space = 0.0;
            if (orientation == PlotOrientation.HORIZONTAL) {
                space = dataArea.getHeight();
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                space = dataArea.getWidth();
            }
            double maxWidth = space * getMaximumBarWidth();
            int columns = data.getColumnCount();
            double categoryMargin = 0.0;
            if (columns > 1) {
                categoryMargin = domainAxis.getCategoryMargin();
            }

            double used = space * (1 - domainAxis.getLowerMargin()
                                     - domainAxis.getUpperMargin()
                                     - categoryMargin);
            if (columns > 0) {
                state.setBarWidth(Math.min(used / columns, maxWidth));
            }
            else {
                state.setBarWidth(Math.min(used, maxWidth));
            }
        }

    }

    /**
     * Returns a list containing the stacked values for the specified series
     * in the given dataset, plus the supplied base value.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param category  the category key (<code>null</code> not permitted).
     * @param base  the base value.
     * @param asPercentages  a flag that controls whether the values in the
     *     list are converted to percentages of the total.
     *
     * @return The value list.
     *
     * @since 1.0.4
     *
     * @deprecated As of 1.0.13, use {@link #createStackedValueList(
     *     CategoryDataset, Comparable, int[], double, boolean)}.
     */
    protected static List createStackedValueList(CategoryDataset dataset,
            Comparable category, double base, boolean asPercentages) {
        int[] rows = new int[dataset.getRowCount()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }
        return createStackedValueList(dataset, category, rows, base,
                asPercentages);
    }

    /**
     * Returns a list containing the stacked values for the specified series
     * in the given dataset, plus the supplied base value.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param category  the category key (<code>null</code> not permitted).
     * @param includedRows  the included rows.
     * @param base  the base value.
     * @param asPercentages  a flag that controls whether the values in the
     *     list are converted to percentages of the total.
     *
     * @return The value list.
     *
     * @since 1.0.13
     */
    protected static List createStackedValueList(CategoryDataset dataset,
            Comparable category, int[] includedRows, double base,
            boolean asPercentages) {

        List result = new ArrayList();
        double posBase = base;
        double negBase = base;
        double total = 0.0;
        if (asPercentages) {
            total = DataUtilities.calculateColumnTotal(dataset,
                    dataset.getColumnIndex(category), includedRows);
        }

        int baseIndex = -1;
        int rowCount = includedRows.length;
        for (int i = 0; i < rowCount; i++) {
            int r = includedRows[i];
            Number n = dataset.getValue(dataset.getRowKey(r), category);
            if (n == null) {
                continue;
            }
            double v = n.doubleValue();
            if (asPercentages) {
                v = v / total;
            }
            if (v >= 0.0) {
                if (baseIndex < 0) {
                    result.add(new Object[] {null, new Double(base)});
                    baseIndex = 0;
                }
                posBase = posBase + v;
                result.add(new Object[] {new Integer(r), new Double(posBase)});
            }
            else if (v < 0.0) {
                if (baseIndex < 0) {
                    result.add(new Object[] {null, new Double(base)});
                    baseIndex = 0;
                }
                negBase = negBase + v; // '+' because v is negative
                result.add(0, new Object[] {new Integer(-r - 1),
                        new Double(negBase)});
                baseIndex++;
            }
        }
        return result;

    }

    /**
     * Draws the visual representation of one data item from the chart (in
     * fact, this method does nothing until it reaches the last item for each
     * category, at which point it draws all the items for that category).
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the plot area.
     * @param plot  the plot.
     * @param domainAxis  the domain (category) axis.
     * @param rangeAxis  the range (value) axis.
     * @param dataset  the data.
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     * @param pass  the pass index.
     */
    public void drawItem(Graphics2D g2,
                         CategoryItemRendererState state,
                         Rectangle2D dataArea,
                         CategoryPlot plot,
                         CategoryAxis domainAxis,
                         ValueAxis rangeAxis,
                         CategoryDataset dataset,
                         int row,
                         int column,
                         int pass) {

        // wait till we are at the last item for the row then draw the
        // whole stack at once
        if (row < dataset.getRowCount() - 1) {
            return;
        }
        Comparable category = dataset.getColumnKey(column);

        List values = createStackedValueList(dataset,
                dataset.getColumnKey(column), state.getVisibleSeriesArray(),
                getBase(), this.renderAsPercentages);

        Rectangle2D adjusted = new Rectangle2D.Double(dataArea.getX(),
                dataArea.getY() + getYOffset(),
                dataArea.getWidth() - getXOffset(),
                dataArea.getHeight() - getYOffset());


        PlotOrientation orientation = plot.getOrientation();

        // handle rendering separately for the two plot orientations...
        if (orientation == PlotOrientation.HORIZONTAL) {
            drawStackHorizontal(values, category, g2, state, adjusted, plot,
                    domainAxis, rangeAxis, dataset);
        }
        else {
            drawStackVertical(values, category, g2, state, adjusted, plot,
                    domainAxis, rangeAxis, dataset);
        }

    }

    /**
     * Draws a stack of bars for one category, with a horizontal orientation.
     *
     * @param values  the value list.
     * @param category  the category.
     * @param g2  the graphics device.
     * @param state  the state.
     * @param dataArea  the data area (adjusted for the 3D effect).
     * @param plot  the plot.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     *
     * @since 1.0.4
     */
    protected void drawStackHorizontal(List values, Comparable category,
            Graphics2D g2, CategoryItemRendererState state,
            Rectangle2D dataArea, CategoryPlot plot,
            CategoryAxis domainAxis, ValueAxis rangeAxis,
            CategoryDataset dataset) {

        int column = dataset.getColumnIndex(category);
        double barX0 = domainAxis.getCategoryMiddle(column,
                dataset.getColumnCount(), dataArea, plot.getDomainAxisEdge())
                - state.getBarWidth() / 2.0;
        double barW = state.getBarWidth();

        // a list to store the series index and bar region, so we can draw
        // all the labels at the end...
        List itemLabelList = new ArrayList();

        // draw the blocks
        boolean inverted = rangeAxis.isInverted();
        int blockCount = values.size() - 1;
        for (int k = 0; k < blockCount; k++) {
            int index = (inverted ? blockCount - k - 1 : k);
            Object[] prev = (Object[]) values.get(index);
            Object[] curr = (Object[]) values.get(index + 1);
            int series = 0;
            if (curr[0] == null) {
                series = -((Integer) prev[0]).intValue() - 1;
            }
            else {
                series = ((Integer) curr[0]).intValue();
                if (series < 0) {
                    series = -((Integer) prev[0]).intValue() - 1;
                }
            }
            double v0 = ((Double) prev[1]).doubleValue();
            double vv0 = rangeAxis.valueToJava2D(v0, dataArea,
                    plot.getRangeAxisEdge());

            double v1 = ((Double) curr[1]).doubleValue();
            double vv1 = rangeAxis.valueToJava2D(v1, dataArea,
                    plot.getRangeAxisEdge());

            Shape[] faces = createHorizontalBlock(barX0, barW, vv0, vv1,
                    inverted);
            Paint fillPaint = getItemPaint(series, column);
            Paint fillPaintDark = fillPaint;
            if (fillPaintDark instanceof Color) {
                fillPaintDark = ((Color) fillPaint).darker();
            }
            boolean drawOutlines = isDrawBarOutline();
            Paint outlinePaint = fillPaint;
            if (drawOutlines) {
                outlinePaint = getItemOutlinePaint(series, column);
                g2.setStroke(getItemOutlineStroke(series, column));
            }
            for (int f = 0; f < 6; f++) {
                if (f == 5) {
                    g2.setPaint(fillPaint);
                }
                else {
                    g2.setPaint(fillPaintDark);
                }
                g2.fill(faces[f]);
                if (drawOutlines) {
                    g2.setPaint(outlinePaint);
                    g2.draw(faces[f]);
                }
            }

            itemLabelList.add(new Object[] {new Integer(series),
                    faces[5].getBounds2D(),
                    BooleanUtilities.valueOf(v0 < getBase())});

            // add an item entity, if this information is being collected
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                addItemEntity(entities, dataset, series, column, faces[5]);
            }

        }

        for (int i = 0; i < itemLabelList.size(); i++) {
            Object[] record = (Object[]) itemLabelList.get(i);
            int series = ((Integer) record[0]).intValue();
            Rectangle2D bar = (Rectangle2D) record[1];
            boolean neg = ((Boolean) record[2]).booleanValue();
            CategoryItemLabelGenerator generator
                    = getItemLabelGenerator(series, column);
            if (generator != null && isItemLabelVisible(series, column)) {
                drawItemLabel(g2, dataset, series, column, plot, generator,
                        bar, neg);
            }

        }
    }

    /**
     * Creates an array of shapes representing the six sides of a block in a
     * horizontal stack.
     *
     * @param x0  left edge of bar (in Java2D space).
     * @param width  the width of the bar (in Java2D units).
     * @param y0  the base of the block (in Java2D space).
     * @param y1  the top of the block (in Java2D space).
     * @param inverted  a flag indicating whether or not the block is inverted
     *     (this changes the order of the faces of the block).
     *
     * @return The sides of the block.
     */
    private Shape[] createHorizontalBlock(double x0, double width, double y0,
            double y1, boolean inverted) {
        Shape[] result = new Shape[6];
        Point2D p00 = new Point2D.Double(y0, x0);
        Point2D p01 = new Point2D.Double(y0, x0 + width);
        Point2D p02 = new Point2D.Double(p01.getX() + getXOffset(),
                p01.getY() - getYOffset());
        Point2D p03 = new Point2D.Double(p00.getX() + getXOffset(),
                p00.getY() - getYOffset());

        Point2D p0 = new Point2D.Double(y1, x0);
        Point2D p1 = new Point2D.Double(y1, x0 + width);
        Point2D p2 = new Point2D.Double(p1.getX() + getXOffset(),
                p1.getY() - getYOffset());
        Point2D p3 = new Point2D.Double(p0.getX() + getXOffset(),
                p0.getY() - getYOffset());

        GeneralPath bottom = new GeneralPath();
        bottom.moveTo((float) p1.getX(), (float) p1.getY());
        bottom.lineTo((float) p01.getX(), (float) p01.getY());
        bottom.lineTo((float) p02.getX(), (float) p02.getY());
        bottom.lineTo((float) p2.getX(), (float) p2.getY());
        bottom.closePath();

        GeneralPath top = new GeneralPath();
        top.moveTo((float) p0.getX(), (float) p0.getY());
        top.lineTo((float) p00.getX(), (float) p00.getY());
        top.lineTo((float) p03.getX(), (float) p03.getY());
        top.lineTo((float) p3.getX(), (float) p3.getY());
        top.closePath();

        GeneralPath back = new GeneralPath();
        back.moveTo((float) p2.getX(), (float) p2.getY());
        back.lineTo((float) p02.getX(), (float) p02.getY());
        back.lineTo((float) p03.getX(), (float) p03.getY());
        back.lineTo((float) p3.getX(), (float) p3.getY());
        back.closePath();

        GeneralPath front = new GeneralPath();
        front.moveTo((float) p0.getX(), (float) p0.getY());
        front.lineTo((float) p1.getX(), (float) p1.getY());
        front.lineTo((float) p01.getX(), (float) p01.getY());
        front.lineTo((float) p00.getX(), (float) p00.getY());
        front.closePath();

        GeneralPath left = new GeneralPath();
        left.moveTo((float) p0.getX(), (float) p0.getY());
        left.lineTo((float) p1.getX(), (float) p1.getY());
        left.lineTo((float) p2.getX(), (float) p2.getY());
        left.lineTo((float) p3.getX(), (float) p3.getY());
        left.closePath();

        GeneralPath right = new GeneralPath();
        right.moveTo((float) p00.getX(), (float) p00.getY());
        right.lineTo((float) p01.getX(), (float) p01.getY());
        right.lineTo((float) p02.getX(), (float) p02.getY());
        right.lineTo((float) p03.getX(), (float) p03.getY());
        right.closePath();
        result[0] = bottom;
        result[1] = back;
        if (inverted) {
            result[2] = right;
            result[3] = left;
        }
        else {
            result[2] = left;
            result[3] = right;
        }
        result[4] = top;
        result[5] = front;
        return result;
    }

    /**
     * Draws a stack of bars for one category, with a vertical orientation.
     *
     * @param values  the value list.
     * @param category  the category.
     * @param g2  the graphics device.
     * @param state  the state.
     * @param dataArea  the data area (adjusted for the 3D effect).
     * @param plot  the plot.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     *
     * @since 1.0.4
     */
    protected void drawStackVertical(List values, Comparable category,
            Graphics2D g2, CategoryItemRendererState state,
            Rectangle2D dataArea, CategoryPlot plot,
            CategoryAxis domainAxis, ValueAxis rangeAxis,
            CategoryDataset dataset) {

        int column = dataset.getColumnIndex(category);
        double barX0 = domainAxis.getCategoryMiddle(column,
                dataset.getColumnCount(), dataArea, plot.getDomainAxisEdge())
                - state.getBarWidth() / 2.0;
        double barW = state.getBarWidth();

        // a list to store the series index and bar region, so we can draw
        // all the labels at the end...
        List itemLabelList = new ArrayList();

        // draw the blocks
        boolean inverted = rangeAxis.isInverted();
        int blockCount = values.size() - 1;
        for (int k = 0; k < blockCount; k++) {
            int index = (inverted ? blockCount - k - 1 : k);
            Object[] prev = (Object[]) values.get(index);
            Object[] curr = (Object[]) values.get(index + 1);
            int series = 0;
            if (curr[0] == null) {
                series = -((Integer) prev[0]).intValue() - 1;
            }
            else {
                series = ((Integer) curr[0]).intValue();
                if (series < 0) {
                    series = -((Integer) prev[0]).intValue() - 1;
                }
            }
            double v0 = ((Double) prev[1]).doubleValue();
            double vv0 = rangeAxis.valueToJava2D(v0, dataArea,
                    plot.getRangeAxisEdge());

            double v1 = ((Double) curr[1]).doubleValue();
            double vv1 = rangeAxis.valueToJava2D(v1, dataArea,
                    plot.getRangeAxisEdge());

            Shape[] faces = createVerticalBlock(barX0, barW, vv0, vv1,
                    inverted);
            Paint fillPaint = getItemPaint(series, column);
            Paint fillPaintDark = fillPaint;
            if (fillPaintDark instanceof Color) {
                fillPaintDark = ((Color) fillPaint).darker();
            }
            boolean drawOutlines = isDrawBarOutline();
            Paint outlinePaint = fillPaint;
            if (drawOutlines) {
                outlinePaint = getItemOutlinePaint(series, column);
                g2.setStroke(getItemOutlineStroke(series, column));
            }

            for (int f = 0; f < 6; f++) {
                if (f == 5) {
                    g2.setPaint(fillPaint);
                }
                else {
                    g2.setPaint(fillPaintDark);
                }
                g2.fill(faces[f]);
                if (drawOutlines) {
                    g2.setPaint(outlinePaint);
                    g2.draw(faces[f]);
                }
            }

            itemLabelList.add(new Object[] {new Integer(series),
                    faces[5].getBounds2D(),
                    BooleanUtilities.valueOf(v0 < getBase())});

            // add an item entity, if this information is being collected
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                addItemEntity(entities, dataset, series, column, faces[5]);
            }

        }

        for (int i = 0; i < itemLabelList.size(); i++) {
            Object[] record = (Object[]) itemLabelList.get(i);
            int series = ((Integer) record[0]).intValue();
            Rectangle2D bar = (Rectangle2D) record[1];
            boolean neg = ((Boolean) record[2]).booleanValue();
            CategoryItemLabelGenerator generator
                    = getItemLabelGenerator(series, column);
            if (generator != null && isItemLabelVisible(series, column)) {
                drawItemLabel(g2, dataset, series, column, plot, generator,
                        bar, neg);
            }

        }
    }

    /**
     * Creates an array of shapes representing the six sides of a block in a
     * vertical stack.
     *
     * @param x0  left edge of bar (in Java2D space).
     * @param width  the width of the bar (in Java2D units).
     * @param y0  the base of the block (in Java2D space).
     * @param y1  the top of the block (in Java2D space).
     * @param inverted  a flag indicating whether or not the block is inverted
     *     (this changes the order of the faces of the block).
     *
     * @return The sides of the block.
     */
    private Shape[] createVerticalBlock(double x0, double width, double y0,
            double y1, boolean inverted) {
        Shape[] result = new Shape[6];
        Point2D p00 = new Point2D.Double(x0, y0);
        Point2D p01 = new Point2D.Double(x0 + width, y0);
        Point2D p02 = new Point2D.Double(p01.getX() + getXOffset(),
                p01.getY() - getYOffset());
        Point2D p03 = new Point2D.Double(p00.getX() + getXOffset(),
                p00.getY() - getYOffset());


        Point2D p0 = new Point2D.Double(x0, y1);
        Point2D p1 = new Point2D.Double(x0 + width, y1);
        Point2D p2 = new Point2D.Double(p1.getX() + getXOffset(),
                p1.getY() - getYOffset());
        Point2D p3 = new Point2D.Double(p0.getX() + getXOffset(),
                p0.getY() - getYOffset());

        GeneralPath right = new GeneralPath();
        right.moveTo((float) p1.getX(), (float) p1.getY());
        right.lineTo((float) p01.getX(), (float) p01.getY());
        right.lineTo((float) p02.getX(), (float) p02.getY());
        right.lineTo((float) p2.getX(), (float) p2.getY());
        right.closePath();

        GeneralPath left = new GeneralPath();
        left.moveTo((float) p0.getX(), (float) p0.getY());
        left.lineTo((float) p00.getX(), (float) p00.getY());
        left.lineTo((float) p03.getX(), (float) p03.getY());
        left.lineTo((float) p3.getX(), (float) p3.getY());
        left.closePath();

        GeneralPath back = new GeneralPath();
        back.moveTo((float) p2.getX(), (float) p2.getY());
        back.lineTo((float) p02.getX(), (float) p02.getY());
        back.lineTo((float) p03.getX(), (float) p03.getY());
        back.lineTo((float) p3.getX(), (float) p3.getY());
        back.closePath();

        GeneralPath front = new GeneralPath();
        front.moveTo((float) p0.getX(), (float) p0.getY());
        front.lineTo((float) p1.getX(), (float) p1.getY());
        front.lineTo((float) p01.getX(), (float) p01.getY());
        front.lineTo((float) p00.getX(), (float) p00.getY());
        front.closePath();

        GeneralPath top = new GeneralPath();
        top.moveTo((float) p0.getX(), (float) p0.getY());
        top.lineTo((float) p1.getX(), (float) p1.getY());
        top.lineTo((float) p2.getX(), (float) p2.getY());
        top.lineTo((float) p3.getX(), (float) p3.getY());
        top.closePath();

        GeneralPath bottom = new GeneralPath();
        bottom.moveTo((float) p00.getX(), (float) p00.getY());
        bottom.lineTo((float) p01.getX(), (float) p01.getY());
        bottom.lineTo((float) p02.getX(), (float) p02.getY());
        bottom.lineTo((float) p03.getX(), (float) p03.getY());
        bottom.closePath();

        result[0] = bottom;
        result[1] = back;
        result[2] = left;
        result[3] = right;
        result[4] = top;
        result[5] = front;
        if (inverted) {
            result[0] = top;
            result[4] = bottom;
        }
        return result;
    }

    /**
     * Tests this renderer for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StackedBarRenderer3D)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        StackedBarRenderer3D that = (StackedBarRenderer3D) obj;
        if (this.renderAsPercentages != that.getRenderAsPercentages()) {
            return false;
        }
        return true;
    }

}
