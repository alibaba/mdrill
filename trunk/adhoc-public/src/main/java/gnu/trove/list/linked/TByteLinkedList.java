///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
// Copyright (c) 2009, Rob Eden All Rights Reserved.
// Copyright (c) 2009, Jeff Randall All Rights Reserved.
// Copyright (c) 2011, Johan Parent All Rights Reserved.
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

package gnu.trove.list.linked;

import gnu.trove.function.TByteFunction;
import gnu.trove.list.TByteList;
import gnu.trove.procedure.TByteProcedure;
import gnu.trove.iterator.TByteIterator;
import gnu.trove.TByteCollection;
import gnu.trove.impl.*;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;


//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * A resizable, double linked list of byte primitives.
 */
public class TByteLinkedList implements TByteList, Externalizable {
    byte no_entry_value;
    int size;

    TByteLink head = null;
    TByteLink tail = head;

    public TByteLinkedList() {
    }

    public TByteLinkedList(byte no_entry_value) {
        this.no_entry_value = no_entry_value;
    }

    public TByteLinkedList(TByteList list) {
        no_entry_value = list.getNoEntryValue();
        //
        for (TByteIterator iterator = list.iterator(); iterator.hasNext();) {
            byte next = iterator.next();
            add(next);
        }
    }

    /** {@inheritDoc} */
    public byte getNoEntryValue() {
        return no_entry_value;
    }

    /** {@inheritDoc} */
    public int size() {
        return size;
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
        return size() == 0;
    }

    /** {@inheritDoc} */
    public boolean add(byte val) {
        TByteLink l = new TByteLink(val);
        if (no(head)) {
            head = l;
            tail = l;
        } else {
            l.setPrevious(tail);
            tail.setNext(l);
            //
            tail = l;
        }

        size++;
        return true;
    }

    /** {@inheritDoc} */
    public void add(byte[] vals) {
        for (byte val : vals) {
            add(val);
        }
    }

    /** {@inheritDoc} */
    public void add(byte[] vals, int offset, int length) {
        for (int i = 0; i < length; i++) {
            byte val = vals[offset + i];
            add(val);
        }
    }

    /** {@inheritDoc} */
    public void insert(int offset, byte value) {
        TByteLinkedList tmp = new TByteLinkedList();
        tmp.add(value);
        insert(offset, tmp);
    }

    /** {@inheritDoc} */
    public void insert(int offset, byte[] values) {
        insert(offset, link(values, 0, values.length));
    }

    /** {@inheritDoc} */
    public void insert(int offset, byte[] values, int valOffset, int len) {
        insert(offset, link(values, valOffset, len));
    }

    void insert(int offset, TByteLinkedList tmp) {
        TByteLink l = getLinkAt(offset);

        size = size + tmp.size;
        //
        if (l == head) {
            // Add in front
            tmp.tail.setNext(head);
            head.setPrevious(tmp.tail);
            head = tmp.head;

            return;
        }

        if (no(l)) {
            if (size == 0) {
                // New empty list
                head = tmp.head;
                tail = tmp.tail;
            } else {
                // append
                tail.setNext(tmp.head);
                tmp.head.setPrevious(tail);
                tail = tmp.tail;
            }
        } else {
            TByteLink prev = l.getPrevious();
            l.getPrevious().setNext(tmp.head);

            // Link by behind tmp
            tmp.tail.setNext(l);
            l.setPrevious(tmp.tail);

            tmp.head.setPrevious(prev);
        }
    }

    static TByteLinkedList link(byte[] values, int valOffset, int len) {
        TByteLinkedList ret = new TByteLinkedList();

        for (int i = 0; i < len; i++) {
            ret.add(values[valOffset + i]);
        }

        return ret;
    }

    /** {@inheritDoc} */
    public byte get(int offset) {
        if (offset > size)
            throw new IndexOutOfBoundsException("index " + offset + " exceeds size " + size);

        TByteLink l = getLinkAt(offset);
        //
        if (no(l))
            return no_entry_value;

        return l.getValue();
    }

   /**
     * Returns the link at the given offset.
     * <p/>
     * A simple bisection criteria is used to keep the worst case complexity equal to
     * O(n/2) where n = size(). Simply start from head of list or tail depending on offset
     * and list size.
     *
     * @param offset of the link
     * @return link or null if non-existent
     */
    public TByteLink getLinkAt(int offset) {
        if (offset >= size())
            return null;

        if (offset <= (size() >>> 1))
            return getLink(head, 0, offset, true);
        else
            return getLink(tail, size() - 1, offset, false);
    }

        /**
         * Returns the link at absolute offset starting from given the initial link 'l' at index 'idx'
         *
         * @param l
         * @param idx
         * @param offset
         * @return
         */
        private static TByteLink getLink(TByteLink l, int idx, int offset) {
            return getLink(l, idx, offset, true);
        }

        /**
         * Returns link at given absolute offset starting from link 'l' at index 'idx'
         * @param l
         * @param idx
         * @param offset
         * @param next
         * @return
         */
        private static TByteLink getLink(TByteLink l, int idx, int offset, boolean next) {
            int i = idx;
            //
            while (got(l)) {
                if (i == offset) {
                    return l;
                }

                i = i + (next ? 1 : -1);
                l = next ? l.getNext() : l.getPrevious();
            }

            return null;
        }


    /** {@inheritDoc} */
    public byte set(int offset, byte val) {
        if (offset > size)
            throw new IndexOutOfBoundsException("index " + offset + " exceeds size " + size);

        TByteLink l = getLinkAt(offset);
        //
        if (no(l))
            throw new IndexOutOfBoundsException("at offset " + offset);

        byte prev = l.getValue();
        l.setValue(val);
        return prev;
    }

    /** {@inheritDoc} */
    public void set(int offset, byte[] values) {
        set(offset, values, 0, values.length);
    }

    /** {@inheritDoc} */
    public void set(int offset, byte[] values, int valOffset, int length) {
        for (int i = 0; i < length; i++) {
            byte value = values[valOffset + i];
            set(offset + i, value);
        }
    }

    /** {@inheritDoc} */
    public byte replace(int offset, byte val) {
        return set(offset, val);
    }

    /** {@inheritDoc} */
    public void clear() {
        size = 0;
        //
        head = null;
        tail = null;
    }

    /** {@inheritDoc} */
    public boolean remove(byte value) {
        boolean changed = false;
        for (TByteLink l = head; got(l); l = l.getNext()) {
            //
            if (l.getValue() == value) {
                changed = true;
                //
                removeLink(l);
            }
        }

        return changed;
    }

    /**
     * unlinks the give TByteLink from the list
     *
     * @param l
     */
    private void removeLink(TByteLink l) {
        if (no(l))
            return;

        size--;

        TByteLink prev = l.getPrevious();
        TByteLink next = l.getNext();

        if (got(prev)) {
            prev.setNext(next);
        } else {
            // No previous we must be head
            head = next;
        }

        if (got(next)) {
            next.setPrevious(prev);
        } else {
            // No next so me must be tail
            tail = prev;
        }
        // Unlink
        l.setNext(null);
        l.setPrevious(null);
    }

    /** {@inheritDoc} */
    public boolean containsAll(Collection<?> collection) {
        if (isEmpty())
            return false;

        for (Object o : collection) {
            if (o instanceof Byte) {
                Byte i = (Byte) o;
                if (!(contains(i)))
                    return false;
            } else {
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    public boolean containsAll(TByteCollection collection) {
        if (isEmpty())
            return false;

        for (TByteIterator it = collection.iterator(); it.hasNext();) {
            byte i = it.next();
            if (!(contains(i)))
                return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    public boolean containsAll(byte[] array) {
        if (isEmpty())
            return false;

        for (byte i : array) {
            if (!contains(i))
                return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    public boolean addAll(Collection<? extends Byte> collection) {
        boolean ret = false;
        for (Byte v : collection) {
            if (add(v.byteValue()))
                ret = true;
        }

        return ret;
    }

    /** {@inheritDoc} */
    public boolean addAll(TByteCollection collection) {
        boolean ret = false;
        for (TByteIterator it = collection.iterator(); it.hasNext();) {
            byte i = it.next();
            if (add(i))
                ret = true;
        }

        return ret;
    }

    /** {@inheritDoc} */
    public boolean addAll(byte[] array) {
        boolean ret = false;
        for (byte i : array) {
            if (add(i))
                ret = true;
        }

        return ret;
    }

    /** {@inheritDoc} */
    public boolean retainAll(Collection<?> collection) {
        boolean modified = false;
        TByteIterator iter = iterator();
        while (iter.hasNext()) {
            if (!collection.contains(Byte.valueOf(iter.next()))) {
                iter.remove();
                modified = true;
            }
        }
        return modified;
    }

    /** {@inheritDoc} */
    public boolean retainAll(TByteCollection collection) {
        boolean modified = false;
        TByteIterator iter = iterator();
        while (iter.hasNext()) {
            if (!collection.contains(iter.next())) {
                iter.remove();
                modified = true;
            }
        }
        return modified;
    }

    /** {@inheritDoc} */
    public boolean retainAll(byte[] array) {
        Arrays.sort(array);

        boolean modified = false;
        TByteIterator iter = iterator();
        while (iter.hasNext()) {
            if (Arrays.binarySearch(array, iter.next()) < 0) {
                iter.remove();
                modified = true;
            }
        }
        return modified;
    }

    /** {@inheritDoc} */
    public boolean removeAll(Collection<?> collection) {
        boolean modified = false;
        TByteIterator iter = iterator();
        while (iter.hasNext()) {
            if (collection.contains(Byte.valueOf(iter.next()))) {
                iter.remove();
                modified = true;
            }
        }
        return modified;
    }

    /** {@inheritDoc} */
    public boolean removeAll(TByteCollection collection) {
        boolean modified = false;
        TByteIterator iter = iterator();
        while (iter.hasNext()) {
            if (collection.contains(iter.next())) {
                iter.remove();
                modified = true;
            }
        }
        return modified;
    }

    /** {@inheritDoc} */
    public boolean removeAll(byte[] array) {
        Arrays.sort(array);

        boolean modified = false;
        TByteIterator iter = iterator();
        while (iter.hasNext()) {
            if (Arrays.binarySearch(array, iter.next()) >= 0) {
                iter.remove();
                modified = true;
            }
        }
        return modified;
    }

    /** {@inheritDoc} */
    public byte removeAt(int offset) {
        TByteLink l = getLinkAt(offset);
        if (no(l))
            throw new ArrayIndexOutOfBoundsException("no elemenet at " + offset);

        byte prev = l.getValue();
        removeLink(l);
        return prev;
    }

    /** {@inheritDoc} */
    public void remove(int offset, int length) {
        for (int i = 0; i < length; i++) {
            removeAt(offset); // since the list shrinks we don't need to use offset+i to get the next entry ;)
        }
    }

    /** {@inheritDoc} */
    public void transformValues(TByteFunction function) {
        for (TByteLink l = head; got(l);) {
            //
            l.setValue(function.execute(l.getValue()));
            //
            l = l.getNext();
        }
    }

    /** {@inheritDoc} */
    public void reverse() {
        TByteLink h = head;
        TByteLink t = tail;
        TByteLink prev, next, tmp;

        //
        TByteLink l = head;
        while (got(l)) {
            next = l.getNext();
            prev = l.getPrevious();
            //
            tmp = l;
            l = l.getNext();
            //
            tmp.setNext(prev);
            tmp.setPrevious(next);
        }

        //
        head = t;
        tail = h;
    }

    /** {@inheritDoc} */
    public void reverse(int from, int to) {
        if (from > to)
            throw new IllegalArgumentException("from > to : " + from + ">" + to);

        TByteLink start = getLinkAt(from);
        TByteLink stop = getLinkAt(to);
        TByteLink prev, next;
        TByteLink tmp = null;

        TByteLink tmpHead = start.getPrevious();

        //
        TByteLink l = start;
        while (l != stop) {
            next = l.getNext();
            prev = l.getPrevious();
            //
            tmp = l;
            l = l.getNext();
            //
            tmp.setNext(prev);
            tmp.setPrevious(next);
        }

        // At this point l == stop and tmp is the but last element {
        if (got(tmp)) {
            tmpHead.setNext(tmp);
            stop.setPrevious(tmpHead);
        }
        start.setNext(stop);
        stop.setPrevious(start);
    }

    /** {@inheritDoc} */
    public void shuffle(Random rand) {
        for (int i = 0; i < size; i++) {
            TByteLink l = getLinkAt(rand.nextInt(size()));
            removeLink(l);
            add(l.getValue());
        }
    }

    /** {@inheritDoc} */
    public TByteList subList(int begin, int end) {
        if (end < begin) {
            throw new IllegalArgumentException("begin index " + begin +
                    " greater than end index " + end);
        }
        if (size < begin) {
            throw new IllegalArgumentException("begin index " + begin +
                    " greater than last index " + size);
        }
        if (begin < 0) {
            throw new IndexOutOfBoundsException("begin index can not be < 0");
        }
        if (end > size) {
            throw new IndexOutOfBoundsException("end index < " + size);
        }

        TByteLinkedList ret = new TByteLinkedList();
        TByteLink tmp = getLinkAt(begin);
        for (int i = begin; i < end; i++) {
            ret.add(tmp.getValue()); // copy
            tmp = tmp.getNext();
        }

        return ret;
    }

    /** {@inheritDoc} */
    public byte[] toArray() {
        return toArray(new byte[size], 0, size);
    }

    /** {@inheritDoc} */
    public byte[] toArray(int offset, int len) {
        return toArray(new byte[len], offset, 0, len);
    }

    /** {@inheritDoc} */
    public byte[] toArray(byte[] dest) {
        return toArray(dest, 0, size);
    }

    /** {@inheritDoc} */
    public byte[] toArray(byte[] dest, int offset, int len) {
        return toArray(dest, offset, 0, len);
    }

    /** {@inheritDoc} */
    public byte[] toArray(byte[] dest, int source_pos, int dest_pos, int len) {
        if (len == 0) {
            return dest;             // nothing to copy
        }
        if (source_pos < 0 || source_pos >= size()) {
            throw new ArrayIndexOutOfBoundsException(source_pos);
        }

        TByteLink tmp = getLinkAt(source_pos);
        for (int i = 0; i < len; i++) {
            dest[dest_pos + i] = tmp.getValue(); // copy
            tmp = tmp.getNext();
        }

        return dest;
    }

    /** {@inheritDoc} */
    public boolean forEach(TByteProcedure procedure) {
        for (TByteLink l = head; got(l); l = l.getNext()) {
            if (!procedure.execute(l.getValue()))
                return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    public boolean forEachDescending(TByteProcedure procedure) {
        for (TByteLink l = tail; got(l); l = l.getPrevious()) {
            if (!procedure.execute(l.getValue()))
                return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    public void sort() {
        sort(0, size);
    }

    /** {@inheritDoc} */
    public void sort(int fromIndex, int toIndex) {
        TByteList tmp = subList(fromIndex, toIndex);
        byte[] vals = tmp.toArray();
        Arrays.sort(vals);
        set(fromIndex, vals);
    }

    /** {@inheritDoc} */
    public void fill(byte val) {
        fill(0, size, val);
    }

    /** {@inheritDoc} */
    public void fill(int fromIndex, int toIndex, byte val) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("begin index can not be < 0");
        }


        TByteLink l = getLinkAt(fromIndex);
        if (toIndex > size) {
            for (int i = fromIndex; i < size; i++) {
                l.setValue(val);
                l = l.getNext();
            }
            for (int i = size; i < toIndex; i++) {
                add(val);
            }
        } else {
            for (int i = fromIndex; i < toIndex; i++) {
                l.setValue(val);
                l = l.getNext();
            }
        }

    }

    /** {@inheritDoc} */
    public int binarySearch(byte value) {
        return binarySearch(value, 0, size());
    }

    /** {@inheritDoc} */
    public int binarySearch(byte value, int fromIndex, int toIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("begin index can not be < 0");
        }

        if (toIndex > size) {
            throw new IndexOutOfBoundsException("end index > size: " + toIndex + " > " + size);
        }


        if (toIndex < fromIndex) {
            return -(fromIndex+1);
        }

        TByteLink middle;
        int mid;
        int from = fromIndex;
        TByteLink fromLink = getLinkAt(fromIndex);
        int to = toIndex;

        while (from < to) {
            mid = (from + to) >>> 1;
            middle = getLink(fromLink, from, mid);
            if (middle.getValue() == value)
                return mid;

            if (middle.getValue() < value) {
                from = mid + 1;
                fromLink = middle.next;
            } else {
                to = mid - 1;
            }
        }

        return -(from + 1);
    }

    /** {@inheritDoc} */
    public int indexOf(byte value) {
        return indexOf(0, value);
    }

    /** {@inheritDoc} */
    public int indexOf(int offset, byte value) {
        int count = offset;
        for (TByteLink l = getLinkAt(offset); got(l.getNext()); l = l.getNext()) {
            if (l.getValue() == value)
                return count;

            count++;
        }

        return -1;
    }

    /** {@inheritDoc} */
    public int lastIndexOf(byte value) {
        return lastIndexOf(0, value);
    }

    /** {@inheritDoc} */
    public int lastIndexOf(int offset, byte value) {
        if (isEmpty())
            return -1;

        int last = -1;
        int count = offset;
        for (TByteLink l = getLinkAt(offset); got(l.getNext()); l = l.getNext()) {
            if (l.getValue() == value)
                last = count;

            count++;
        }

        return last;
    }

    /** {@inheritDoc} */
    public boolean contains(byte value) {
        if (isEmpty())
            return false;

        for (TByteLink l = head; got(l); l = l.getNext()) {
            if (l.getValue() == value)
                return true;
        }
        return false;

    }

    /** {@inheritDoc} */
    public TByteIterator iterator() {
        return new TByteIterator() {
            TByteLink l = head;
            TByteLink current;

            public byte next() {
                if (no(l))
                    throw new NoSuchElementException();

                byte ret = l.getValue();
                current = l;
                l = l.getNext();

                return ret;
            }

            public boolean hasNext() {
                return got(l);
            }

            public void remove() {
                if (current == null)
                    throw new IllegalStateException();

                removeLink(current);
                current = null;
            }
        };
    }

    /** {@inheritDoc} */
    public TByteList grep(TByteProcedure condition) {
        TByteList ret = new TByteLinkedList();
        for (TByteLink l = head; got(l); l = l.getNext()) {
            if (condition.execute(l.getValue()))
                ret.add(l.getValue());
        }
        return ret;
    }

    /** {@inheritDoc} */
    public TByteList inverseGrep(TByteProcedure condition) {
        TByteList ret = new TByteLinkedList();
        for (TByteLink l = head; got(l); l = l.getNext()) {
            if (!condition.execute(l.getValue()))
                ret.add(l.getValue());
        }
        return ret;
    }

    /** {@inheritDoc} */
    public byte max() {
        byte ret = Byte.MIN_VALUE;

        if (isEmpty())
            throw new IllegalStateException();

        for (TByteLink l = head; got(l); l = l.getNext()) {
            if (ret < l.getValue())
                ret = l.getValue();
        }

        return ret;
    }

    /** {@inheritDoc} */
    public byte min() {
        byte ret = Byte.MAX_VALUE;

        if (isEmpty())
            throw new IllegalStateException();

        for (TByteLink l = head; got(l); l = l.getNext()) {
            if (ret > l.getValue())
                ret = l.getValue();
        }

        return ret;
    }

    /** {@inheritDoc} */
    public byte sum() {
        byte sum = 0;

        for (TByteLink l = head; got(l); l = l.getNext()) {
			sum += l.getValue();
        }

        return sum;
    }

    //
    //
    //
    static class TByteLink {
        byte value;
        TByteLink previous;
        TByteLink next;

        TByteLink(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }

        public void setValue(byte value) {
            this.value = value;
        }

        public TByteLink getPrevious() {
            return previous;
        }

        public void setPrevious(TByteLink previous) {
            this.previous = previous;
        }

        public TByteLink getNext() {
            return next;
        }

        public void setNext(TByteLink next) {
            this.next = next;
        }
    }

    class RemoveProcedure implements TByteProcedure {
        boolean changed = false;

        /**
         * Executes this procedure. A false return value indicates that
         * the application executing this procedure should not invoke this
         * procedure again.
         *
         * @param value a value of type <code>int</code>
         * @return true if additional invocations of the procedure are
         *         allowed.
         */
        public boolean execute(byte value) {
            if (remove(value))
                changed = true;

            return true;
        }

        public boolean isChanged() {
            return changed;
        }
    }

    /** {@inheritDoc} */
    public void writeExternal(ObjectOutput out) throws IOException {
        // VERSION
        out.writeByte(0);

        // NO_ENTRY_VALUE
        out.writeByte(no_entry_value);

        // ENTRIES
        out.writeInt(size);
        for (TByteIterator iterator = iterator(); iterator.hasNext();) {
            byte next = iterator.next();
            out.writeByte(next);
        }
    }


    /** {@inheritDoc} */
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {

        // VERSION
        in.readByte();

        // NO_ENTRY_VALUE
        no_entry_value = in.readByte();

        // ENTRIES
        int len = in.readInt();
        for (int i = 0; i < len; i++) {
            add(in.readByte());
        }
    }

    static boolean got(Object ref) {
        return ref != null;
    }

    static boolean no(Object ref) {
        return ref == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TByteLinkedList that = (TByteLinkedList) o;

        if (no_entry_value != that.no_entry_value) return false;
        if (size != that.size) return false;

        TByteIterator iterator = iterator();
        TByteIterator thatIterator = that.iterator();
        while (iterator.hasNext()) {
            if (!thatIterator.hasNext())
                return false;

            if (iterator.next() != thatIterator.next())
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = HashFunctions.hash(no_entry_value);
        result = 31 * result + size;
        for (TByteIterator iterator = iterator(); iterator.hasNext();) {
            result = 31 * result + HashFunctions.hash(iterator.next());
        }

        return result;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("{");
        TByteIterator it = iterator();
        while (it.hasNext()) {
            byte next = it.next();
            buf.append(next);
            if (it.hasNext())
                buf.append(", ");
        }
        buf.append("}");
        return buf.toString();

    }
} // TByteLinkedList
