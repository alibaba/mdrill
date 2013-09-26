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
 * -----------------------
 * AbstractObjectList.java
 * -----------------------
 * (C)opyright 2003, 2004, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Bill Kelemen; 
 *                   Nicolas Brodu
 *
 * $Id: AbstractObjectList.java,v 1.5 2005/10/18 13:24:19 mungady Exp $
 *
 * Changes
 * -------
 * 13-Aug-2003 : Version 1, based on ObjectList (DG);
 * 24-Aug-2003 : Fixed size (BK);
 * 15-Sep-2003 : Fix serialization for subclasses (ShapeList, PaintList) (NB);
 */

package org.jfree.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * A list of objects that can grow as required.
 *
 * @author David Gilbert
 */
public class AbstractObjectList implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 7789833772597351595L;
    
    /** The default initial capacity of the list. */
    public static final int DEFAULT_INITIAL_CAPACITY = 8;

    /** Storage for the objects. */
    private transient Object[] objects;

    /** The current list size. */
    private int size = 0;

    /** The default increment. */
    private int increment = DEFAULT_INITIAL_CAPACITY;

    /**
     * Creates a new list with the default initial capacity.
     */
    protected AbstractObjectList() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Creates a new list.
     *
     * @param initialCapacity  the initial capacity.
     */
    protected AbstractObjectList(final int initialCapacity) {
        this (initialCapacity, initialCapacity);
    }

    /**
     * Creates a new list.
     * 
     * @param initialCapacity  the initial capacity.
     * @param increment  the increment.
     */
    protected AbstractObjectList(final int initialCapacity, 
                                 final int increment) {
        this.objects = new Object[initialCapacity];
        this.increment = increment;
    }

    /**
     * Returns the object at the specified index, if there is one, or 
     * <code>null</code>.
     *
     * @param index  the object index.
     *
     * @return The object or <code>null</code>.
     */
    protected Object get(final int index) {
        Object result = null;
        if (index >= 0 && index < this.size) {
            result = this.objects[index];
        }
        return result;
    }

    /**
     * Sets an object reference (overwriting any existing object).
     *
     * @param index  the object index.
     * @param object  the object (<code>null</code> permitted).
     */
    protected void set(final int index, final Object object) {
        if (index < 0) {
            throw new IllegalArgumentException("Requires index >= 0.");
        }
        if (index >= this.objects.length) {
            final Object[] enlarged = new Object[index + this.increment];
            System.arraycopy(this.objects, 0, enlarged, 0, this.objects.length);
            this.objects = enlarged;
        }
        this.objects[index] = object;
        this.size = Math.max(this.size, index + 1);
    }

    /**
     * Clears the list.
     */
    public void clear() {
        Arrays.fill(this.objects, null);
        this.size = 0;
    }

    /**
     * Returns the size of the list.
     *
     * @return The size of the list.
     */
    public int size() {
        return this.size;
    }

    /**
     * Returns the index of the specified object, or -1 if the object is not in
     * the list.
     *
     * @param object  the object.
     *
     * @return The index or -1.
     */
    protected int indexOf(final Object object) {
        for (int index = 0; index < this.size; index++) {
            if (this.objects[index] == object) {
                return (index);
            }
        }
        return -1;
    }

    /**
     * Tests this list for equality with another object.
     *
     * @param obj  the object to test.
     * 
     * @return A boolean.
     */
    public boolean equals(final Object obj) {

        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof AbstractObjectList)) {
            return false;
        }

        final AbstractObjectList other = (AbstractObjectList) obj;
        final int listSize = size();
        for (int i = 0; i < listSize; i++) {
           if (!ObjectUtilities.equal(get(i), other.get(i))) {
               return false;
           }
        }
        return true;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return the hashcode
     */
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Clones the list of objects.  The objects in the list are not cloned, so 
     * this is method makes a 'shallow' copy of the list.
     *
     * @return A clone.
     * 
     * @throws CloneNotSupportedException if an item in the list does not 
     *         support cloning.
     */
    public Object clone() throws CloneNotSupportedException {

        final AbstractObjectList clone = (AbstractObjectList) super.clone();
        if (this.objects != null) {
            clone.objects = new Object[this.objects.length];
            System.arraycopy(
                this.objects, 0, clone.objects, 0, this.objects.length
            );
        }
        return clone;

    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(final ObjectOutputStream stream) 
        throws IOException {

        stream.defaultWriteObject();
        final int count = size();
        stream.writeInt(count);
        for (int i = 0; i < count; i++) {
            final Object object = get(i);
            if (object != null && object instanceof Serializable) {
                stream.writeInt(i);
                stream.writeObject(object);
            }
            else {
                stream.writeInt(-1);
            }
        }

    }
    
    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(final ObjectInputStream stream) 
        throws IOException, ClassNotFoundException {

        stream.defaultReadObject();
        this.objects = new Object[this.size];
        final int count = stream.readInt();
        for (int i = 0; i < count; i++) {
            final int index = stream.readInt();
            if (index != -1) {
                set(index, stream.readObject());
            }
        }
        
    }
  
}
