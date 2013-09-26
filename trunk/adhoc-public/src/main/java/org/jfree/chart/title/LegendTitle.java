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
 * ----------------
 * LegendTitle.java
 * ----------------
 * (C) Copyright 2002-2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Pierre-Marie Le Biot;
 *
 * Changes
 * -------
 * 25-Nov-2004 : First working version (DG);
 * 11-Jan-2005 : Removed deprecated code in preparation for 1.0.0 release (DG);
 * 08-Feb-2005 : Updated for changes in RectangleConstraint class (DG);
 * 11-Feb-2005 : Implemented PublicCloneable (DG);
 * 23-Feb-2005 : Replaced chart reference with LegendItemSource (DG);
 * 16-Mar-2005 : Added itemFont attribute (DG);
 * 17-Mar-2005 : Fixed missing fillShape setting (DG);
 * 20-Apr-2005 : Added new draw() method (DG);
 * 03-May-2005 : Modified equals() method to ignore sources (DG);
 * 13-May-2005 : Added settings for legend item label and graphic padding (DG);
 * 09-Jun-2005 : Fixed serialization bug (DG);
 * 01-Sep-2005 : Added itemPaint attribute (PMLB);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 20-Jul-2006 : Use new LegendItemBlockContainer to restore support for
 *               LegendItemEntities (DG);
 * 06-Oct-2006 : Add tooltip and URL text to legend item (DG);
 * 13-Dec-2006 : Added support for GradientPaint in legend items (DG);
 * 16-Mar-2007 : Updated border drawing for changes in AbstractBlock (DG);
 * 18-May-2007 : Pass seriesKey and dataset to legend item block (DG);
 * 15-Aug-2008 : Added getWrapper() method (DG);
 * 19-Mar-2009 : Added entity support - see patch 2603321 by Peter Kolb (DG);
 *
 */

package org.jfree.chart.title;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.block.Arrangement;
import org.jfree.chart.block.Block;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BlockFrame;
import org.jfree.chart.block.BlockResult;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.CenterArrangement;
import org.jfree.chart.block.ColumnArrangement;
import org.jfree.chart.block.EntityBlockParams;
import org.jfree.chart.block.FlowArrangement;
import org.jfree.chart.block.LabelBlock;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.entity.TitleEntity;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.Size2D;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A chart title that displays a legend for the data in the chart.
 * <P>
 * The title can be populated with legend items manually, or you can assign a
 * reference to the plot, in which case the legend items will be automatically
 * created to match the dataset(s).
 */
public class LegendTitle extends Title
        implements Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 2644010518533854633L;

    /** The default item font. */
    public static final Font DEFAULT_ITEM_FONT
            = new Font("SansSerif", Font.PLAIN, 12);

    /** The default item paint. */
    public static final Paint DEFAULT_ITEM_PAINT = Color.black;

    /** The sources for legend items. */
    private LegendItemSource[] sources;

    /** The background paint (possibly <code>null</code>). */
    private transient Paint backgroundPaint;

    /** The edge for the legend item graphic relative to the text. */
    private RectangleEdge legendItemGraphicEdge;

    /** The anchor point for the legend item graphic. */
    private RectangleAnchor legendItemGraphicAnchor;

    /** The legend item graphic location. */
    private RectangleAnchor legendItemGraphicLocation;

    /** The padding for the legend item graphic. */
    private RectangleInsets legendItemGraphicPadding;

    /** The item font. */
    private Font itemFont;

    /** The item paint. */
    private transient Paint itemPaint;

    /** The padding for the item labels. */
    private RectangleInsets itemLabelPadding;

    /**
     * A container that holds and displays the legend items.
     */
    private BlockContainer items;

    /**
     * The layout for the legend when it is positioned at the top or bottom
     * of the chart.
     */
    private Arrangement hLayout;

    /**
     * The layout for the legend when it is positioned at the left or right
     * of the chart.
     */
    private Arrangement vLayout;

    /**
     * An optional container for wrapping the legend items (allows for adding
     * a title or other text to the legend).
     */
    private BlockContainer wrapper;

    /**
     * Constructs a new (empty) legend for the specified source.
     *
     * @param source  the source.
     */
    public LegendTitle(LegendItemSource source) {
        this(source, new FlowArrangement(), new ColumnArrangement());
    }

    /**
     * Creates a new legend title with the specified arrangement.
     *
     * @param source  the source.
     * @param hLayout  the horizontal item arrangement (<code>null</code> not
     *                 permitted).
     * @param vLayout  the vertical item arrangement (<code>null</code> not
     *                 permitted).
     */
    public LegendTitle(LegendItemSource source,
                       Arrangement hLayout, Arrangement vLayout) {
        this.sources = new LegendItemSource[] {source};
        this.items = new BlockContainer(hLayout);
        this.hLayout = hLayout;
        this.vLayout = vLayout;
        this.backgroundPaint = null;
        this.legendItemGraphicEdge = RectangleEdge.LEFT;
        this.legendItemGraphicAnchor = RectangleAnchor.CENTER;
        this.legendItemGraphicLocation = RectangleAnchor.CENTER;
        this.legendItemGraphicPadding = new RectangleInsets(2.0, 2.0, 2.0, 2.0);
        this.itemFont = DEFAULT_ITEM_FONT;
        this.itemPaint = DEFAULT_ITEM_PAINT;
        this.itemLabelPadding = new RectangleInsets(2.0, 2.0, 2.0, 2.0);
    }

    /**
     * Returns the legend item sources.
     *
     * @return The sources.
     */
    public LegendItemSource[] getSources() {
        return this.sources;
    }

    /**
     * Sets the legend item sources and sends a {@link TitleChangeEvent} to
     * all registered listeners.
     *
     * @param sources  the sources (<code>null</code> not permitted).
     */
    public void setSources(LegendItemSource[] sources) {
        if (sources == null) {
            throw new IllegalArgumentException("Null 'sources' argument.");
        }
        this.sources = sources;
        notifyListeners(new TitleChangeEvent(this));
    }

    /**
     * Returns the background paint.
     *
     * @return The background paint (possibly <code>null</code>).
     */
    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }

    /**
     * Sets the background paint for the legend and sends a
     * {@link TitleChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> permitted).
     */
    public void setBackgroundPaint(Paint paint) {
        this.backgroundPaint = paint;
        notifyListeners(new TitleChangeEvent(this));
    }

    /**
     * Returns the location of the shape within each legend item.
     *
     * @return The location (never <code>null</code>).
     */
    public RectangleEdge getLegendItemGraphicEdge() {
        return this.legendItemGraphicEdge;
    }

    /**
     * Sets the location of the shape within each legend item.
     *
     * @param edge  the edge (<code>null</code> not permitted).
     */
    public void setLegendItemGraphicEdge(RectangleEdge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("Null 'edge' argument.");
        }
        this.legendItemGraphicEdge = edge;
        notifyListeners(new TitleChangeEvent(this));
    }

    /**
     * Returns the legend item graphic anchor.
     *
     * @return The graphic anchor (never <code>null</code>).
     */
    public RectangleAnchor getLegendItemGraphicAnchor() {
        return this.legendItemGraphicAnchor;
    }

    /**
     * Sets the anchor point used for the graphic in each legend item.
     *
     * @param anchor  the anchor point (<code>null</code> not permitted).
     */
    public void setLegendItemGraphicAnchor(RectangleAnchor anchor) {
        if (anchor == null) {
            throw new IllegalArgumentException("Null 'anchor' point.");
        }
        this.legendItemGraphicAnchor = anchor;
    }

    /**
     * Returns the legend item graphic location.
     *
     * @return The location (never <code>null</code>).
     */
    public RectangleAnchor getLegendItemGraphicLocation() {
        return this.legendItemGraphicLocation;
    }

    /**
     * Sets the legend item graphic location.
     *
     * @param anchor  the anchor (<code>null</code> not permitted).
     */
    public void setLegendItemGraphicLocation(RectangleAnchor anchor) {
        this.legendItemGraphicLocation = anchor;
    }

    /**
     * Returns the padding that will be applied to each item graphic.
     *
     * @return The padding (never <code>null</code>).
     */
    public RectangleInsets getLegendItemGraphicPadding() {
        return this.legendItemGraphicPadding;
    }

    /**
     * Sets the padding that will be applied to each item graphic in the
     * legend and sends a {@link TitleChangeEvent} to all registered listeners.
     *
     * @param padding  the padding (<code>null</code> not permitted).
     */
    public void setLegendItemGraphicPadding(RectangleInsets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("Null 'padding' argument.");
        }
        this.legendItemGraphicPadding = padding;
        notifyListeners(new TitleChangeEvent(this));
    }

    /**
     * Returns the item font.
     *
     * @return The font (never <code>null</code>).
     */
    public Font getItemFont() {
        return this.itemFont;
    }

    /**
     * Sets the item font and sends a {@link TitleChangeEvent} to
     * all registered listeners.
     *
     * @param font  the font (<code>null</code> not permitted).
     */
    public void setItemFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }
        this.itemFont = font;
        notifyListeners(new TitleChangeEvent(this));
    }

    /**
     * Returns the item paint.
     *
     * @return The paint (never <code>null</code>).
     */
    public Paint getItemPaint() {
        return this.itemPaint;
    }

    /**
     * Sets the item paint.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     */
    public void setItemPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.itemPaint = paint;
        notifyListeners(new TitleChangeEvent(this));
    }

    /**
     * Returns the padding used for the items labels.
     *
     * @return The padding (never <code>null</code>).
     */
    public RectangleInsets getItemLabelPadding() {
        return this.itemLabelPadding;
    }

    /**
     * Sets the padding used for the item labels in the legend.
     *
     * @param padding  the padding (<code>null</code> not permitted).
     */
    public void setItemLabelPadding(RectangleInsets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("Null 'padding' argument.");
        }
        this.itemLabelPadding = padding;
        notifyListeners(new TitleChangeEvent(this));
    }

    /**
     * Fetches the latest legend items.
     */
    protected void fetchLegendItems() {
        this.items.clear();
        RectangleEdge p = getPosition();
        if (RectangleEdge.isTopOrBottom(p)) {
            this.items.setArrangement(this.hLayout);
        }
        else {
            this.items.setArrangement(this.vLayout);
        }
        for (int s = 0; s < this.sources.length; s++) {
            LegendItemCollection legendItems = this.sources[s].getLegendItems();
            if (legendItems != null) {
                for (int i = 0; i < legendItems.getItemCount(); i++) {
                    LegendItem item = legendItems.get(i);
                    Block block = createLegendItemBlock(item);
                    this.items.add(block);
                }
            }
        }
    }

    /**
     * Creates a legend item block.
     *
     * @param item  the legend item.
     *
     * @return The block.
     */
    protected Block createLegendItemBlock(LegendItem item) {
        BlockContainer result = null;
        LegendGraphic lg = new LegendGraphic(item.getShape(),
                item.getFillPaint());
        lg.setFillPaintTransformer(item.getFillPaintTransformer());
        lg.setShapeFilled(item.isShapeFilled());
        lg.setLine(item.getLine());
        lg.setLineStroke(item.getLineStroke());
        lg.setLinePaint(item.getLinePaint());
        lg.setLineVisible(item.isLineVisible());
        lg.setShapeVisible(item.isShapeVisible());
        lg.setShapeOutlineVisible(item.isShapeOutlineVisible());
        lg.setOutlinePaint(item.getOutlinePaint());
        lg.setOutlineStroke(item.getOutlineStroke());
        lg.setPadding(this.legendItemGraphicPadding);

        LegendItemBlockContainer legendItem = new LegendItemBlockContainer(
                new BorderArrangement(), item.getDataset(),
                item.getSeriesKey());
        lg.setShapeAnchor(getLegendItemGraphicAnchor());
        lg.setShapeLocation(getLegendItemGraphicLocation());
        legendItem.add(lg, this.legendItemGraphicEdge);
        Font textFont = item.getLabelFont();
        if (textFont == null) {
            textFont = this.itemFont;
        }
        Paint textPaint = item.getLabelPaint();
        if (textPaint == null) {
            textPaint = this.itemPaint;
        }
        LabelBlock labelBlock = new LabelBlock(item.getLabel(), textFont,
                textPaint);
        labelBlock.setPadding(this.itemLabelPadding);
        legendItem.add(labelBlock);
        legendItem.setToolTipText(item.getToolTipText());
        legendItem.setURLText(item.getURLText());

        result = new BlockContainer(new CenterArrangement());
        result.add(legendItem);

        return result;
    }

    /**
     * Returns the container that holds the legend items.
     *
     * @return The container for the legend items.
     */
    public BlockContainer getItemContainer() {
        return this.items;
    }

    /**
     * Arranges the contents of the block, within the given constraints, and
     * returns the block size.
     *
     * @param g2  the graphics device.
     * @param constraint  the constraint (<code>null</code> not permitted).
     *
     * @return The block size (in Java2D units, never <code>null</code>).
     */
    public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
        Size2D result = new Size2D();
        fetchLegendItems();
        if (this.items.isEmpty()) {
            return result;
        }
        BlockContainer container = this.wrapper;
        if (container == null) {
            container = this.items;
        }
        RectangleConstraint c = toContentConstraint(constraint);
        Size2D size = container.arrange(g2, c);
        result.height = calculateTotalHeight(size.height);
        result.width = calculateTotalWidth(size.width);
        return result;
    }

    /**
     * Draws the title on a Java 2D graphics device (such as the screen or a
     * printer).
     *
     * @param g2  the graphics device.
     * @param area  the available area for the title.
     */
    public void draw(Graphics2D g2, Rectangle2D area) {
        draw(g2, area, null);
    }

    /**
     * Draws the block within the specified area.
     *
     * @param g2  the graphics device.
     * @param area  the area.
     * @param params  ignored (<code>null</code> permitted).
     *
     * @return An {@link org.jfree.chart.block.EntityBlockResult} or
     *         <code>null</code>.
     */
    public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
        Rectangle2D target = (Rectangle2D) area.clone();
        Rectangle2D hotspot = (Rectangle2D) area.clone();
        StandardEntityCollection sec = null;
        if (params instanceof EntityBlockParams
                && ((EntityBlockParams) params).getGenerateEntities()) {
			sec = new StandardEntityCollection();
            sec.add(new TitleEntity(hotspot,this));
        }
        target = trimMargin(target);
        if (this.backgroundPaint != null) {
            g2.setPaint(this.backgroundPaint);
            g2.fill(target);
        }
        BlockFrame border = getFrame();
        border.draw(g2, target);
        border.getInsets().trim(target);
        BlockContainer container = this.wrapper;
        if (container == null) {
            container = this.items;
        }
        target = trimPadding(target);
        Object val = container.draw(g2, target, params);
        if (val instanceof BlockResult){
        	EntityCollection ec = ((BlockResult) val).getEntityCollection();
        	if (ec != null && sec != null){
        		sec.addAll(ec);
        		((BlockResult) val).setEntityCollection(sec);
        	}
        }
        return val;
    }

    /**
     * Returns the wrapper container, if any.
     *
     * @return The wrapper container (possibly <code>null</code>).
     *
     * @since 1.0.11
     */
    public BlockContainer getWrapper() {
        return this.wrapper;
    }

    /**
     * Sets the wrapper container for the legend.
     *
     * @param wrapper  the wrapper container.
     */
    public void setWrapper(BlockContainer wrapper) {
        this.wrapper = wrapper;
    }

    /**
     * Tests this title for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LegendTitle)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        LegendTitle that = (LegendTitle) obj;
        if (!PaintUtilities.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;
        }
        if (this.legendItemGraphicEdge != that.legendItemGraphicEdge) {
            return false;
        }
        if (this.legendItemGraphicAnchor != that.legendItemGraphicAnchor) {
            return false;
        }
        if (this.legendItemGraphicLocation != that.legendItemGraphicLocation) {
            return false;
        }
        if (!this.itemFont.equals(that.itemFont)) {
            return false;
        }
        if (!this.itemPaint.equals(that.itemPaint)) {
            return false;
        }
        if (!this.hLayout.equals(that.hLayout)) {
            return false;
        }
        if (!this.vLayout.equals(that.vLayout)) {
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
        SerialUtilities.writePaint(this.backgroundPaint, stream);
        SerialUtilities.writePaint(this.itemPaint, stream);
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
        this.backgroundPaint = SerialUtilities.readPaint(stream);
        this.itemPaint = SerialUtilities.readPaint(stream);
    }

}
