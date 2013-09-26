/**
 * ========================================
 * JCommon : a free Java report library
 * ========================================
 *
 * Project Info:  http://www.jfree.org/jcommon/
 *
 * (C) Copyright 2000-2006, by Object Refinery Limited and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * ------------
 * $Id: FastStack.java,v 1.3 2008/09/10 09:22:05 mungady Exp $
 * ------------
 * (C) Copyright 2002-2006, by Object Refinery Limited.
 */

package org.jfree.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EmptyStackException;

/**
 * A very simple unsynchronized stack. This one is faster than the
 * java.util-Version.
 *
 * @author Thomas Morgner
 */
public final class FastStack implements Serializable, Cloneable {
    private Object[] contents;
    private int size;
    private int initialSize;

    /**
     * Creates a new empty stack.
     */
    public FastStack() {
        this.initialSize = 10;
    }

    /**
     * Creates a new empty stack with the specified initial storage size.
     *
     * @param size  the initial storage elements.
     */
    public FastStack(int size) {
        this.initialSize = Math.max(1, size);
    }

    /**
     * Returns <code>true</code> if the stack is empty, and <code>false</code>
     * otherwise.
     *
     * @return A boolean.
     */
    public boolean isEmpty() {
        return this.size == 0;
    }

    /**
     * Returns the number of elements in the stack.
     *
     * @return The element count.
     */
    public int size() {
        return this.size;
    }

    /**
     * Pushes an object onto the stack.
     *
     * @param o  the object.
     */
    public void push(Object o) {
        if (this.contents == null) {
            this.contents = new Object[this.initialSize];
            this.contents[0] = o;
            this.size = 1;
            return;
        }

        final int oldSize = this.size;
        this.size += 1;
        if (this.contents.length == this.size) {
            // grow ..
            final Object[] newContents = new Object[this.size
                    + this.initialSize];
            System.arraycopy(this.contents, 0, newContents, 0, this.size);
            this.contents = newContents;
        }
        this.contents[oldSize] = o;
    }

    /**
     * Returns the object at the top of the stack without removing it.
     *
     * @return The object at the top of the stack.
     */
    public Object peek() {
        if (this.size == 0) {
            throw new EmptyStackException();
        }
        return this.contents[this.size - 1];
    }

    /**
     * Removes and returns the object from the top of the stack.
     *
     * @return The object.
     */
    public Object pop() {
        if (this.size == 0) {
            throw new EmptyStackException();
        }
        this.size -= 1;
        final Object retval = this.contents[this.size];
        this.contents[this.size] = null;
        return retval;
    }

    /**
     * Returns a clone of the stack.
     *
     * @return A clone.
     */
    public Object clone() {
        try {
            FastStack stack = (FastStack) super.clone();
            if (this.contents != null) {
                stack.contents = (Object[]) this.contents.clone();
            }
            return stack;
        }
        catch (CloneNotSupportedException cne) {
            throw new IllegalStateException("Clone not supported? Why?");
        }
    }

    /**
     * Clears the stack.
     */
    public void clear() {
        this.size = 0;
        if (this.contents != null) {
            Arrays.fill(this.contents, null);
        }
    }

    /**
     * Returns the item at the specified slot in the stack.
     *
     * @param index  the index.
     *
     * @return The item.
     */
    public Object get(final int index) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException();
        }
        return this.contents[index];
    }
}
