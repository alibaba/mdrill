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
 * --------------------------
 * CategorySeriesHandler.java
 * --------------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 23-Jan-2003 : Version 1 (DG);
 *
 */

package org.jfree.data.xml;

import java.util.Iterator;

import org.jfree.data.DefaultKeyedValues;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A handler for reading a series for a category dataset.
 */
public class CategorySeriesHandler extends DefaultHandler
        implements DatasetTags {

    /** The root handler. */
    private RootHandler root;

    /** The series key. */
    private Comparable seriesKey;

    /** The values. */
    private DefaultKeyedValues values;

    /**
     * Creates a new item handler.
     *
     * @param root  the root handler.
     */
    public CategorySeriesHandler(RootHandler root) {
        this.root = root;
        this.values = new DefaultKeyedValues();
    }

    /**
     * Sets the series key.
     *
     * @param key  the key.
     */
    public void setSeriesKey(Comparable key) {
        this.seriesKey = key;
    }

    /**
     * Adds an item to the temporary storage for the series.
     *
     * @param key  the key.
     * @param value  the value.
     */
    public void addItem(Comparable key, final Number value) {
        this.values.addValue(key, value);
    }

    /**
     * The start of an element.
     *
     * @param namespaceURI  the namespace.
     * @param localName  the element name.
     * @param qName  the element name.
     * @param atts  the attributes.
     *
     * @throws SAXException for errors.
     */
    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts) throws SAXException {

        if (qName.equals(SERIES_TAG)) {
            setSeriesKey(atts.getValue("name"));
            ItemHandler subhandler = new ItemHandler(this.root, this);
            this.root.pushSubHandler(subhandler);
        }
        else if (qName.equals(ITEM_TAG)) {
            ItemHandler subhandler = new ItemHandler(this.root, this);
            this.root.pushSubHandler(subhandler);
            subhandler.startElement(namespaceURI, localName, qName, atts);
        }

        else {
            throw new SAXException(
                "Expecting <Series> or <Item> tag...found " + qName
            );
        }
    }

    /**
     * The end of an element.
     *
     * @param namespaceURI  the namespace.
     * @param localName  the element name.
     * @param qName  the element name.
     */
    public void endElement(String namespaceURI,
                           String localName,
                           String qName) {

        if (this.root instanceof CategoryDatasetHandler) {
            CategoryDatasetHandler handler = (CategoryDatasetHandler) this.root;

            Iterator iterator = this.values.getKeys().iterator();
            while (iterator.hasNext()) {
                Comparable key = (Comparable) iterator.next();
                Number value = this.values.getValue(key);
                handler.addItem(this.seriesKey, key, value);
            }

            this.root.popSubHandler();
        }

    }

}
