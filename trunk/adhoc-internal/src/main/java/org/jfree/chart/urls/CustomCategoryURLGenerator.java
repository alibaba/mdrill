/* ======================================
 * JFreeChart : a free Java chart library
 * ======================================
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
 * -------------------------------
 * CustomCategoryURLGenerator.java
 * -------------------------------
 * (C) Copyright 2008, by Diego Pierangeli and Contributors.
 *
 * Original Author:  Diego Pierangeli;
 * Contributors:     David Gilbert (for Object Refinery Limited);
 *
 * Changes:
 * --------
 * 23-Apr-2008 : Version 1, contributed by Diego Pierangeli based on
 *               CustomXYURLGenerator by Richard Atkinson, with some
 *               modifications by David Gilbert(DG);
 *
 */
package org.jfree.chart.urls;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jfree.data.category.CategoryDataset;
import org.jfree.util.PublicCloneable;

/**
 * A custom URL generator.
 */
public class CustomCategoryURLGenerator implements CategoryURLGenerator,
        Cloneable, PublicCloneable, Serializable {

    /** Storage for the URLs. */
    private ArrayList urlSeries = new ArrayList();

    /**
     * Default constructor.
     */
    public CustomCategoryURLGenerator() {
        super();
    }

    /**
     * Returns the number of URL lists stored by the renderer.
     *
     * @return The list count.
     */
    public int getListCount() {
        return this.urlSeries.size();
    }

    /**
     * Returns the number of URLs in a given list.
     *
     * @param list  the list index (zero based).
     *
     * @return The URL count.
     */
    public int getURLCount(int list) {
        int result = 0;
        List urls = (List) this.urlSeries.get(list);
        if (urls != null) {
            result = urls.size();
        }
        return result;
    }

    /**
     * Returns the URL for an item.
     *
     * @param series  the series index.
     * @param item  the item index.
     *
     * @return The URL (possibly <code>null</code>).
     */
    public String getURL(int series, int item) {
        String result = null;
        if (series < getListCount()) {
            List urls = (List) this.urlSeries.get(series);
            if (urls != null) {
                if (item < urls.size()) {
                    result = (String) urls.get(item);
                }
            }
        }
        return result;
    }

    /**
     * Generates a URL.
     *
     * @param dataset  the dataset (ignored in this implementation).
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return A string containing the URL (possibly <code>null</code>).
     */
    public String generateURL(CategoryDataset dataset, int series, int item) {
        return getURL(series, item);
    }

    /**
     * Adds a list of URLs.
     *
     * @param urls  the list of URLs (<code>null</code> permitted).
     */
    public void addURLSeries(List urls) {
        List listToAdd = null;
        if (urls != null) {
            listToAdd = new java.util.ArrayList(urls);
        }
        this.urlSeries.add(listToAdd);
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
        if (!(obj instanceof CustomCategoryURLGenerator)) {
            return false;
        }
        CustomCategoryURLGenerator generator = (CustomCategoryURLGenerator) obj;
        int listCount = getListCount();
        if (listCount != generator.getListCount()) {
            return false;
        }

        for (int series = 0; series < listCount; series++) {
            int urlCount = getURLCount(series);
            if (urlCount != generator.getURLCount(series)) {
                return false;
            }

            for (int item = 0; item < urlCount; item++) {
                String u1 = getURL(series, item);
                String u2 = generator.getURL(series, item);
                if (u1 != null) {
                    if (!u1.equals(u2)) {
                        return false;
                    }
                } else {
                    if (u2 != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns a new generator that is a copy of, and independent from, this
     * generator.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if there is a problem with cloning.
     */
    public Object clone() throws CloneNotSupportedException {
        CustomCategoryURLGenerator clone
                = (CustomCategoryURLGenerator) super.clone();
        clone.urlSeries = new java.util.ArrayList(this.urlSeries);
        return clone;
    }

}
