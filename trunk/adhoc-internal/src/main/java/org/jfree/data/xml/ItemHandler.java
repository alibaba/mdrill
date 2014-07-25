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
 * ----------------
 * ItemHandler.java
 * ----------------
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A handler for reading key-value items.
 */
public class ItemHandler extends DefaultHandler implements DatasetTags {

    /** The root handler. */
    private RootHandler root;

    /** The parent handler (can be the same as root, but not always). */
    private DefaultHandler parent;

    /** The key. */
    private Comparable key;

    /** The value. */
    private Number value;

    /**
     * Creates a new item handler.
     *
     * @param root  the root handler.
     * @param parent  the parent handler.
     */
    public ItemHandler(RootHandler root, DefaultHandler parent) {
        this.root = root;
        this.parent = parent;
        this.key = null;
        this.value = null;
    }

    /**
     * Returns the key that has been read by the handler, or <code>null</code>.
     *
     * @return The key.
     */
    public Comparable getKey() {
        return this.key;
    }

    /**
     * Sets the key.
     *
     * @param key  the key.
     */
    public void setKey(Comparable key) {
        this.key = key;
    }

    /**
     * Returns the key that has been read by the handler, or <code>null</code>.
     *
     * @return The value.
     */
    public Number getValue() {
        return this.value;
    }

    /**
     * Sets the value.
     *
     * @param value  the value.
     */
    public void setValue(Number value) {
        this.value = value;
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

        if (qName.equals(ITEM_TAG)) {
            KeyHandler subhandler = new KeyHandler(this.root, this);
            this.root.pushSubHandler(subhandler);
        }
        else if (qName.equals(VALUE_TAG)) {
            ValueHandler subhandler = new ValueHandler(this.root, this);
            this.root.pushSubHandler(subhandler);
        }
        else {
            throw new SAXException(
                "Expected <Item> or <Value>...found " + qName
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

        if (this.parent instanceof PieDatasetHandler) {
            PieDatasetHandler handler = (PieDatasetHandler) this.parent;
            handler.addItem(this.key, this.value);
            this.root.popSubHandler();
        }
        else if (this.parent instanceof CategorySeriesHandler) {
            CategorySeriesHandler handler = (CategorySeriesHandler) this.parent;
            handler.addItem(this.key, this.value);
            this.root.popSubHandler();
        }

    }

}
