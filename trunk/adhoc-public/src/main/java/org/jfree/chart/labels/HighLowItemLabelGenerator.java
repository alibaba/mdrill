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
 * ------------------------------
 * HighLowItemLabelGenerator.java
 * ------------------------------
 * (C) Copyright 2001-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   David Basten;
 *
 * Changes
 * -------
 * 13-Dec-2001 : Version 1 (DG);
 * 16-Jan-2002 : Completed Javadocs (DG);
 * 23-Apr-2002 : Added date to the tooltip string (DG);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 21-Mar-2003 : Implemented Serializable (DG);
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 17-Nov-2003 : Implemented PublicCloneable (DG);
 * 25-Feb-2004 : Renamed XYToolTipGenerator --> XYItemLabelGenerator (DG);
 * 25-May-2004 : Added number formatter (see patch 890496) (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with 
 *               getYValue() (DG);
 * 20-Apr-2005 : Renamed XYLabelGenerator --> XYItemLabelGenerator (DG);
 * 31-Mar-2008 : Added hashCode() method to appease FindBugs (DG);
 *
 */

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.jfree.chart.HashUtilities;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.PublicCloneable;

/**
 * A standard item label generator for plots that use data from a 
 * {@link OHLCDataset}.
 */
public class HighLowItemLabelGenerator implements XYItemLabelGenerator, 
        XYToolTipGenerator, Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 5617111754832211830L;
    
    /** The date formatter. */
    private DateFormat dateFormatter;

    /** The number formatter. */
    private NumberFormat numberFormatter;

    /**
     * Creates an item label generator using the default date and number 
     * formats.
     */
    public HighLowItemLabelGenerator() {
        this(DateFormat.getInstance(), NumberFormat.getInstance());
    }

    /**
     * Creates a tool tip generator using the supplied date formatter.
     *
     * @param dateFormatter  the date formatter (<code>null</code> not 
     *                       permitted).
     * @param numberFormatter  the number formatter (<code>null</code> not 
     *                         permitted).
     */
    public HighLowItemLabelGenerator(DateFormat dateFormatter, 
                                     NumberFormat numberFormatter) {
        if (dateFormatter == null) {
            throw new IllegalArgumentException(
                    "Null 'dateFormatter' argument.");   
        }
        if (numberFormatter == null) {
            throw new IllegalArgumentException(
                    "Null 'numberFormatter' argument.");
        }
        this.dateFormatter = dateFormatter;
        this.numberFormatter = numberFormatter;
    }

    /**
     * Generates a tooltip text item for a particular item within a series.
     *
     * @param dataset  the dataset.
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The tooltip text.
     */
    public String generateToolTip(XYDataset dataset, int series, int item) {

        String result = null;

        if (dataset instanceof OHLCDataset) {
            OHLCDataset d = (OHLCDataset) dataset;
            Number high = d.getHigh(series, item);
            Number low = d.getLow(series, item);
            Number open = d.getOpen(series, item);
            Number close = d.getClose(series, item);
            Number x = d.getX(series, item);

            result = d.getSeriesKey(series).toString();

            if (x != null) {
                Date date = new Date(x.longValue());
                result = result + "--> Date=" + this.dateFormatter.format(date);
                if (high != null) {
                    result = result + " High=" 
                             + this.numberFormatter.format(high.doubleValue());
                }
                if (low != null) {
                    result = result + " Low=" 
                             + this.numberFormatter.format(low.doubleValue());
                }
                if (open != null) {
                    result = result + " Open=" 
                             + this.numberFormatter.format(open.doubleValue());
                }
                if (close != null) {
                    result = result + " Close=" 
                             + this.numberFormatter.format(close.doubleValue());
                }
            }

        }

        return result;

    }

    /**
     * Generates a label for the specified item. The label is typically a 
     * formatted version of the data value, but any text can be used.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param series  the series index (zero-based).
     * @param category  the category index (zero-based).
     *
     * @return The label (possibly <code>null</code>).
     */
    public String generateLabel(XYDataset dataset, int series, int category) {
        return null;  //TODO: implement this method properly
    }

    /**
     * Returns an independent copy of the generator.
     * 
     * @return A clone.
     * 
     * @throws CloneNotSupportedException if cloning is not supported.
     */
    public Object clone() throws CloneNotSupportedException {
        
        HighLowItemLabelGenerator clone 
            = (HighLowItemLabelGenerator) super.clone();

        if (this.dateFormatter != null) {
            clone.dateFormatter = (DateFormat) this.dateFormatter.clone();
        }
        if (this.numberFormatter != null) {
            clone.numberFormatter = (NumberFormat) this.numberFormatter.clone();
        }
        
        return clone;
        
    }
    
    /**
     * Tests if this object is equal to another.
     *
     * @param obj  the other object.
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof HighLowItemLabelGenerator)) {
            return false;
        }
        HighLowItemLabelGenerator generator = (HighLowItemLabelGenerator) obj;
        if (!this.dateFormatter.equals(generator.dateFormatter)) {
            return false;
        }
        if (!this.numberFormatter.equals(generator.numberFormatter)) {
            return false;   
        }
        return true;
    }
    
    /**
     * Returns a hash code for this instance.
     * 
     * @return A hash code.
     */
    public int hashCode() {
        int result = 127;
        result = HashUtilities.hashCode(result, this.dateFormatter);
        result = HashUtilities.hashCode(result, this.numberFormatter);
        return result;
    }
    
}
