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
 * ---------------------------
 * StandardXYURLGenerator.java
 * ---------------------------
 * (C) Copyright 2002-2008, by Richard Atkinson and Contributors.
 *
 * Original Author:  Richard Atkinson;
 * Contributors:     David Gilbert (for Object Refinery Limited);
 *
 * Changes:
 * --------
 * 05-Aug-2002 : Version 1, contributed by Richard Atkinson;
 * 29-Aug-2002 : New constructor and member variables to customise series and
 *               item parameter names (RA);
 * 09-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 23-Mar-2003 : Implemented Serializable (DG);
 * 01-Mar-2004 : Added equals() method (DG);
 * 13-Jan-2005 : Modified for XHTML 1.0 compliance (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags from all over JFreeChart sources (DG);
 *
 */

package org.jfree.chart.urls;

import java.io.Serializable;

import org.jfree.data.xy.XYDataset;
import org.jfree.util.ObjectUtilities;

/**
 * A URL generator.
 */
public class StandardXYURLGenerator implements XYURLGenerator, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -1771624523496595382L;

    /** The default prefix. */
    public static final String DEFAULT_PREFIX = "index.html";

    /** The default series parameter. */
    public static final String DEFAULT_SERIES_PARAMETER = "series";

    /** The default item parameter. */
    public static final String DEFAULT_ITEM_PARAMETER = "item";

    /** Prefix to the URL */
    private String prefix;

    /** Series parameter name to go in each URL */
    private String seriesParameterName;

    /** Item parameter name to go in each URL */
    private String itemParameterName;

    /**
     * Creates a new default generator.  This constructor is equivalent to
     * calling <code>StandardXYURLGenerator("index.html", "series", "item");
     * </code>.
     */
    public StandardXYURLGenerator() {
        this(DEFAULT_PREFIX, DEFAULT_SERIES_PARAMETER, DEFAULT_ITEM_PARAMETER);
    }

    /**
     * Creates a new generator with the specified prefix.  This constructor
     * is equivalent to calling
     * <code>StandardXYURLGenerator(prefix, "series", "item");</code>.
     *
     * @param prefix  the prefix to the URL (<code>null</code> not permitted).
     */
    public StandardXYURLGenerator(String prefix) {
        this(prefix, DEFAULT_SERIES_PARAMETER, DEFAULT_ITEM_PARAMETER);
    }

    /**
     * Constructor that overrides all the defaults
     *
     * @param prefix  the prefix to the URL (<code>null</code> not permitted).
     * @param seriesParameterName  the name of the series parameter to go in
     *                             each URL (<code>null</code> not permitted).
     * @param itemParameterName  the name of the item parameter to go in each
     *                           URL (<code>null</code> not permitted).
     */
    public StandardXYURLGenerator(String prefix,
                                  String seriesParameterName,
                                  String itemParameterName) {
        if (prefix == null) {
            throw new IllegalArgumentException("Null 'prefix' argument.");
        }
        if (seriesParameterName == null) {
            throw new IllegalArgumentException(
                    "Null 'seriesParameterName' argument.");
        }
        if (itemParameterName == null) {
            throw new IllegalArgumentException(
                    "Null 'itemParameterName' argument.");
        }
        this.prefix = prefix;
        this.seriesParameterName = seriesParameterName;
        this.itemParameterName = itemParameterName;
    }

    /**
     * Generates a URL for a particular item within a series.
     *
     * @param dataset  the dataset.
     * @param series  the series number (zero-based index).
     * @param item  the item number (zero-based index).
     *
     * @return The generated URL.
     */
    public String generateURL(XYDataset dataset, int series, int item) {
        // TODO: URLEncode?
        String url = this.prefix;
        boolean firstParameter = url.indexOf("?") == -1;
        url += firstParameter ? "?" : "&amp;";
        url += this.seriesParameterName + "=" + series
                + "&amp;" + this.itemParameterName + "=" + item;
        return url;
    }

    /**
     * Tests this generator for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StandardXYURLGenerator)) {
            return false;
        }
        StandardXYURLGenerator that = (StandardXYURLGenerator) obj;
        if (!ObjectUtilities.equal(that.prefix, this.prefix)) {
            return false;
        }
        if (!ObjectUtilities.equal(that.seriesParameterName,
                this.seriesParameterName)) {
            return false;
        }
        if (!ObjectUtilities.equal(that.itemParameterName,
                this.itemParameterName)) {
            return false;
        }
        return true;
    }

}
