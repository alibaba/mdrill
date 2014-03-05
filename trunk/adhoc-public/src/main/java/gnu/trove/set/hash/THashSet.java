///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
// Copyright (c) 2009, Rob Eden All Rights Reserved.
// Copyright (c) 2009, Jeff Randall All Rights Reserved.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////

package gnu.trove.set.hash;

import gnu.trove.impl.hash.TObjectHash;
import gnu.trove.impl.HashFunctions;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.procedure.array.ToObjectArrayProceedure;
import gnu.trove.iterator.hash.TObjectHashIterator;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Arrays;
import java.lang.reflect.Array;


/**
 * An implementation of the <tt>Set</tt> interface that uses an
 * open-addressed hash table to store its contents.
 *
 * @author Eric D. Friedman
 * @author Rob Eden
 * @author Jeff Randall
 * @version $Id: THashSet.java,v 1.1.2.8 2010/03/02 04:09:50 robeden Exp $
 */

public class THashSet<E> extends TObjectHash<E>
        implements Set<E>, Iterable<E>, Externalizable {

    static final long serialVersionUID = 1L;


    /**
     * Creates a new <code>THashSet</code> instance with the default
     * capacity and load factor.
     */
    public THashSet() {
        super();
    }


    /**
     * Creates a new <code>THashSet</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the default load factor.
     *
     * @param initialCapacity an <code>int</code> value
     */
    public THashSet(int initialCapacity) {
        super(initialCapacity);
    }


    /**
     * Creates a new <code>THashSet</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the specified load factor.
     *
     * @param initialCapacity an <code>int</code> value
     * @param loadFactor      a <code>float</code> value
     */
    public THashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }


    /**
     * Creates a new <code>THashSet</code> instance containing the
     * elements of <tt>collection</tt>.
     *
     * @param collection a <code>Collection</code> value
     */
    public THashSet(Collection<? extends E> collection) {
        this(collection.size());
        addAll(collection);
    }


    /**
     * Inserts a value into the set.
     *
     * @param obj an <code>Object</code> value
     * @return true if the set was modified by the add operation
     */
    public boolean add(E obj) {
        int index = insertKey(obj);

        if (index < 0) {
            return false;       // already present in set, nothing to add
        }

        postInsertHook(consumeFreeSlot);
        return true;            // yes, we added something
    }


    @SuppressWarnings({"SimplifiableIfStatement"})
    public boolean equals(Object other) {
        if (!(other instanceof Set)) {
            return false;
        }
        Set that = (Set) other;
        if (that.size() != this.size()) {
            return false;
        }
        return containsAll(that);
    }


    public int hashCode() {
        HashProcedure p = new HashProcedure();
        forEach(p);
        return p.getHashCode();
    }


    private final class HashProcedure implements TObjectProcedure<E> {
        private int h = 0;

        public int getHashCode() {
            return h;
        }

        public final boolean execute(E key) {
            h += HashFunctions.hash(key);
            return true;
        }
    }


    /**
     * Expands the set to accommodate new values.
     *
     * @param newCapacity an <code>int</code> value
     */
    @SuppressWarnings({"unchecked"})
    protected void rehash(int newCapacity) {
        int oldCapacity = _set.length;

        int oldSize = size();
        Object oldSet[] = _set;

        _set = new Object[newCapacity];
        Arrays.fill(_set, FREE);

        int count = 0;
        for (int i = oldCapacity; i-- > 0;) {
            E o = (E) oldSet[i];
            if (o != FREE && o != REMOVED) {
                int index = insertKey(o);
                if (index < 0) { // everyone pays for this because some people can't RTFM
                    throwObjectContractViolation(_set[(-index - 1)], o, size(), oldSize, oldSet);
                }
                //
                count++;
            }
        }
        // Last check: size before and after should be the same
        reportPotentialConcurrentMod(size(), oldSize);
    }

    /**
     * Returns a new array containing the objects in the set.
     *
     * @return an <code>Object[]</code> value
     */
    @SuppressWarnings({"unchecked"})
    public Object[] toArray() {
        Object[] result = new Object[size()];
        forEach(new ToObjectArrayProceedure(result));
        return result;
    }


    /**
     * Returns a typed array of the objects in the set.
     *
     * @param a an <code>Object[]</code> value
     * @return an <code>Object[]</code> value
     */
    @SuppressWarnings({"unchecked"})
    public <T> T[] toArray(T[] a) {
        int size = size();
        if (a.length < size) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        }

        forEach(new ToObjectArrayProceedure(a));

        // If this collection fits in the specified array with room to
        // spare (i.e., the array has more elements than this
        // collection), the element in the array immediately following
        // the end of the collection is set to null. This is useful in
        // determining the length of this collection only if the
        // caller knows that this collection does not contain any null
        // elements.)

        if (a.length > size) {
            a[size] = null;
        }

        return a;
    }


    /**
     * Empties the set.
     */
    public void clear() {
        super.clear();

        Arrays.fill(_set, 0, _set.length, FREE);
    }


    /**
     * Removes <tt>obj</tt> from the set.
     *
     * @param obj an <code>Object</code> value
     * @return true if the set was modified by the remove operation.
     */
    @SuppressWarnings({"unchecked"})
    public boolean remove(Object obj) {
        int index = index(obj);
        if (index >= 0) {
            removeAt(index);
            return true;
        }
        return false;
    }


    /**
     * Creates an iterator over the values of the set.  The iterator
     * supports element deletion.
     *
     * @return an <code>Iterator</code> value
     */
    @SuppressWarnings({"unchecked"})
    public TObjectHashIterator<E> iterator() {
        return new TObjectHashIterator<E>(this);
    }


    /**
     * Tests the set to determine if all of the elements in
     * <tt>collection</tt> are present.
     *
     * @param collection a <code>Collection</code> value
     * @return true if all elements were present in the set.
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public boolean containsAll(Collection<?> collection) {
        for (Iterator i = collection.iterator(); i.hasNext();) {
            if (!contains(i.next())) {
                return false;
            }
        }
        return true;
    }


    /**
     * Adds all of the elements in <tt>collection</tt> to the set.
     *
     * @param collection a <code>Collection</code> value
     * @return true if the set was modified by the add all operation.
     */
    public boolean addAll(Collection<? extends E> collection) {
        boolean changed = false;
        int size = collection.size();

        ensureCapacity(size);
        Iterator<? extends E> it = collection.iterator();
        while (size-- > 0) {
            if (add(it.next())) {
                changed = true;
            }
        }
        return changed;
    }


    /**
     * Removes all of the elements in <tt>collection</tt> from the set.
     *
     * @param collection a <code>Collection</code> value
     * @return true if the set was modified by the remove all operation.
     */
    public boolean removeAll(Collection<?> collection) {
        boolean changed = false;
        int size = collection.size();
        Iterator it;

        it = collection.iterator();
        while (size-- > 0) {
            if (remove(it.next())) {
                changed = true;
            }
        }
        return changed;
    }


    /**
     * Removes any values in the set which are not contained in
     * <tt>collection</tt>.
     *
     * @param collection a <code>Collection</code> value
     * @return true if the set was modified by the retain all operation
     */
    @SuppressWarnings({"SuspiciousMethodCalls"})
    public boolean retainAll(Collection<?> collection) {
        boolean changed = false;
        int size = size();
        Iterator<E> it = iterator();
        while (size-- > 0) {
            if (!collection.contains(it.next())) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }


    public String toString() {
        final StringBuilder buf = new StringBuilder("{");
        forEach(new TObjectProcedure<E>() {
            private boolean first = true;


            public boolean execute(Object value) {
                if (first) {
                    first = false;
                } else {
                    buf.append(", ");
                }

                buf.append(value);
                return true;
            }
        });
        buf.append("}");
        return buf.toString();
    }


    public void writeExternal(ObjectOutput out) throws IOException {
        // VERSION
        out.writeByte(1);

        // NOTE: Super was not written in version 0
        super.writeExternal(out);

        // NUMBER OF ENTRIES
        out.writeInt(_size);

        // ENTRIES
        writeEntries(out);
    }

    protected void writeEntries(ObjectOutput out) throws IOException {
        for (int i = _set.length; i-- > 0;) {
            if (_set[i] != REMOVED && _set[i] != FREE) {
                out.writeObject(_set[i]);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {

        // VERSION
        byte version = in.readByte();

        // NOTE: super was not written in version 0
        if (version != 0) {
            super.readExternal(in);
        }

        // NUMBER OF ENTRIES
        int size = in.readInt();
        setUp(size);

        // ENTRIES
        while (size-- > 0) {
            E val = (E) in.readObject();
            add(val);
        }

    }
} // THashSet
