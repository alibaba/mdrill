/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
 * 
 * Project Info:  http://www.jfree.org/jcommon/index.html
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
 * ------------------
 * AttributeList.java
 * ------------------
 * (C)opyright 2003-2005, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: AttributeList.java,v 1.3 2005/10/18 13:35:06 mungady Exp $
 *
 * Changes
 * -------
 * 25-Sep-2003 : Initial version (TM);
 * 26-Nov-2003 : Javadoc updates (DG);
 *
 */

package org.jfree.xml.writer;

import java.util.Iterator;
import java.util.List;

/**
 * The attribute list is used by a writer to specify the attributes
 * of an XML element in a certain order.
 *
 * @author Thomas Morgner
 */
public class AttributeList {

    /**
     * A name/value pair of the attribute list.
     */
    private static class AttributeEntry {
        
        /** The name of the attribute entry. */
        private String name;
        
        /** The value of the attribute entry. */
        private String value;

        /**
         * Creates a new attribute entry for the given name and value.
         *
         * @param name  the attribute name (<code>null</code> not permitted).
         * @param value the attribute value (<code>null</code> not permitted).
         */
        public AttributeEntry(final String name, final String value) {
            if (name == null) {
                throw new NullPointerException("Name must not be null. [" 
                                               + name + ", " + value + "]");
            }
            if (value == null) {
                throw new NullPointerException("Value must not be null. [" 
                                               + name + ", " + value + "]");
            }
            this.name = name;
            this.value = value;
        }

        /**
         * Returns the attribute name.
         * 
         * @return the name.
         */
        public String getName() {
            return this.name;
        }

        /**
         * Returns the value of this attribute entry.
         * 
         * @return the value of the entry.
         */
        public String getValue() {
            return this.value;
        }

        /**
         * Checks whether the given object is an attribute entry with the same name.
         * 
         * @param o  the suspected other attribute entry.
         * 
         * @return <code>true</code> if the given object is equal, <code>false</code> otherwise.
         */
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AttributeEntry)) {
                return false;
            }

            final AttributeEntry attributeEntry = (AttributeEntry) o;
            if (!this.name.equals(attributeEntry.name)) {
                return false;
            }
            return true;
        }

        /**
         * Computes an hashcode for this entry.
         * 
         * @return the hashcode.
         */
        public int hashCode() {
            return this.name.hashCode();
        }
    }

    /**
     * An iterator over the attribute names of this list.
     */
    private static class AttributeIterator implements Iterator {
        
        /** The backend is an iterator over the attribute entries. */
        private Iterator backend;

        /**
         * Creates a new attribute iterator using the given iterator as backend.
         * 
         * @param backend  an iterator over the attribute entries (<code>null</code> not permitted).
         */
        public AttributeIterator(final Iterator backend) {
            if (backend == null) {
                throw new NullPointerException();
            }
            this.backend = backend;
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements. (In other
         * words, returns <tt>true</tt> if <tt>next</tt> would return an element
         * rather than throwing an exception.)
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext() {
            return this.backend.hasNext();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         */
        public Object next() {
            final AttributeEntry entry = (AttributeEntry) this.backend.next();
            if (entry != null) {
                return entry.getName();
            }
            return entry;
        }

        /**
         *
         * Removes from the underlying collection the last element returned by the
         * iterator (optional operation).  This method can be called only once per
         * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
         * the underlying collection is modified while the iteration is in
         * progress in any way other than by calling this method.
         */
        public void remove() {
            this.backend.remove();
        }
    }

    /** The storage for all entries of this list. */
    private List entryList;

    /**
     * Creates an empty attribute list with no default values.
     */
    public AttributeList() {
        this.entryList = new java.util.ArrayList();
    }

    /**
     * Returns an iterator over all attribute names. The names are returned
     * in their oder of addition to the list. The iterator contains strings.
     *
     * @return the iterator over all attribute names.
     */
    public Iterator keys() {
        return new AttributeIterator(this.entryList.iterator());
    }

    /**
     * Defines an attribute.
     * 
     * @param name the name of the attribute to be defined
     * @param value the value of the attribute.
     */
    public synchronized void setAttribute(final String name, final String value) {
        final AttributeEntry entry = new AttributeEntry(name, value);
        final int pos = this.entryList.indexOf(entry);
        if (pos != -1) {
            this.entryList.remove(pos);
        }
        this.entryList.add(entry);
    }

    /**
     * Returns the attribute value for the given attribute name or null,
     * if the attribute is not defined in this list.
     *
     * @param name the name of the attribute
     * @return the attribute value or null.
     */
    public synchronized String getAttribute(final String name) {
        return getAttribute(name, null);
    }

    /**
     * Returns the attribute value for the given attribute name or the given
     * defaultvalue, if the attribute is not defined in this list.
     *
     * @param name the name of the attribute.
     * @param defaultValue  the default value.
     * 
     * @return the attribute value or the defaultValue.
     */
    public synchronized String getAttribute(final String name, final String defaultValue) {
        for (int i = 0; i < this.entryList.size(); i++) {
            final AttributeEntry ae = (AttributeEntry) this.entryList.get(i);
            if (ae.getName().equals(name)) {
                return ae.getValue();
            }
        }
        return defaultValue;
    }

    /**
     * Removes the attribute with the given name from the list.
     *
     * @param name the name of the attribute which should be removed..
     */
    public synchronized void removeAttribute(final String name) {
        for (int i = 0; i < this.entryList.size(); i++) {
            final AttributeEntry ae = (AttributeEntry) this.entryList.get(i);
            if (ae.getName().equals(name)) {
                this.entryList.remove(ae);
                return;
            }
        }
    }
}
