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
 * ----------------------------
 * StandardPieURLGenerator.java
 * ----------------------------
 * (C) Copyright 2002-2008, by Richard Atkinson and Contributors.
 *
 * Original Author:  Richard Atkinson;
 * Contributors:     David Gilbert (for Object Refinery Limited);
 *
 * Changes:
 * --------
 * 05-Aug-2002 : Version 1, contributed by Richard Atkinson;
 * 09-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 07-Mar-2003 : Modified to use KeyedValuesDataset and added pieIndex
 *               parameter (DG);
 * 21-Mar-2003 : Implemented Serializable (DG);
 * 24-Apr-2003 : Switched around PieDataset and KeyedValuesDataset (DG);
 * 31-Mar-2004 : Added an optional 'pieIndex' parameter (DG);
 * 13-Jan-2005 : Fixed for compliance with XHTML 1.0 (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 24-Nov-2006 : Fixed equals() method and added argument checks (DG);
 * 17-Apr-2007 : Encode section key in generateURL() (DG);
 *
 */

package org.jfree.chart.urls;

import java.io.Serializable;

import org.jfree.data.general.PieDataset;
import org.jfree.util.ObjectUtilities;

/**
 * A URL generator for pie charts.  Instances of this class are immutable.
 */
public class StandardPieURLGenerator implements PieURLGenerator, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 1626966402065883419L;

    /** The prefix. */
    private String prefix = "index.html";

    /** The category parameter name. */
    private String categoryParameterName = "category";

    /** The pie index parameter name. */
    private String indexParameterName = "pieIndex";

    /**
     * Default constructor.
     */
    public StandardPieURLGenerator() {
        this("index.html");
    }

    /**
     * Creates a new generator.
     *
     * @param prefix  the prefix (<code>null</code> not permitted).
     */
    public StandardPieURLGenerator(String prefix) {
        this(prefix, "category");
    }

    /**
     * Creates a new generator.
     *
     * @param prefix  the prefix (<code>null</code> not permitted).
     * @param categoryParameterName  the category parameter name
     *     (<code>null</code> not permitted).
     */
    public StandardPieURLGenerator(String prefix,
                                   String categoryParameterName) {
        this(prefix, categoryParameterName, "pieIndex");
    }

    /**
     * Creates a new generator.
     *
     * @param prefix  the prefix (<code>null</code> not permitted).
     * @param categoryParameterName  the category parameter name
     *     (<code>null</code> not permitted).
     * @param indexParameterName  the index parameter name (<code>null</code>
     *     permitted).
     */
    public StandardPieURLGenerator(String prefix,
                                   String categoryParameterName,
                                   String indexParameterName) {
        if (prefix == null) {
            throw new IllegalArgumentException("Null 'prefix' argument.");
        }
        if (categoryParameterName == null) {
            throw new IllegalArgumentException(
                    "Null 'categoryParameterName' argument.");
        }
        this.prefix = prefix;
        this.categoryParameterName = categoryParameterName;
        this.indexParameterName = indexParameterName;
    }

    /**
     * Generates a URL.
     *
     * @param dataset  the dataset (ignored).
     * @param key  the item key (<code>null</code> not permitted).
     * @param pieIndex  the pie index.
     *
     * @return A string containing the generated URL.
     */
    public String generateURL(PieDataset dataset, Comparable key,
            int pieIndex) {
        String url = this.prefix;
        if (url.indexOf("?") > -1) {
            url += "&amp;" + this.categoryParameterName + "="
                    + URLUtilities.encode(key.toString(), "UTF-8");
        }
        else {
            url += "?" + this.categoryParameterName + "="
                    + URLUtilities.encode(key.toString(), "UTF-8");
        }
        if (this.indexParameterName != null) {
            url += "&amp;" + this.indexParameterName + "="
                   + String.valueOf(pieIndex);
        }
        return url;
    }

    /**
     * Tests if this object is equal to another.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StandardPieURLGenerator)) {
            return false;
        }
        StandardPieURLGenerator that = (StandardPieURLGenerator) obj;
        if (!this.prefix.equals(that.prefix)) {
            return false;
        }
        if (!this.categoryParameterName.equals(that.categoryParameterName)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.indexParameterName,
                that.indexParameterName)) {
            return false;
        }
        return true;
    }
}
