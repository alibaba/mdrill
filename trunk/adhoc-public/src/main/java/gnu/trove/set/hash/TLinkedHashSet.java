package gnu.trove.set.hash;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;

import java.io.IOException;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Johan
 * Date: 15/03/11
 * Time: 18:15
 * To change this template use File | Settings | File Templates.
 */
public class TLinkedHashSet<E> extends THashSet<E> {
    TIntList order;

    /**
     * Creates a new <code>THashSet</code> instance with the default
     * capacity and load factor.
     */
    public TLinkedHashSet() {
    }

    /**
     * Creates a new <code>THashSet</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the default load factor.
     *
     * @param initialCapacity an <code>int</code> value
     */
    public TLinkedHashSet(int initialCapacity) {
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
    public TLinkedHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Creates a new <code>THashSet</code> instance containing the
     * elements of <tt>collection</tt>.
     *
     * @param es a <code>Collection</code> value
     */
    public TLinkedHashSet(Collection<? extends E> es) {
        super(es);
    }

    /**
     * initializes the Object set of this hash table.
     *
     * @param initialCapacity an <code>int</code> value
     * @return an <code>int</code> value
     */
    @Override
    public int setUp(int initialCapacity) {
        order = new TIntArrayList(initialCapacity) {
            /**
             * Grow the internal array as needed to accommodate the specified number of elements.
             * The size of the array bytes on each resize unless capacity requires more than twice
             * the current capacity.
             */
            @Override
            public void ensureCapacity(int capacity) {
                if (capacity > _data.length) {
                    int newCap = Math.max(_set.length, capacity);
                    int[] tmp = new int[newCap];
                    System.arraycopy(_data, 0, tmp, 0, _data.length);
                    _data = tmp;
                }
            }
        };
        return super.setUp(initialCapacity);    //To change body of overridden methods use File | Settings | File Templates.
    }


    /**
     * Empties the set.
     */
    @Override
    public void clear() {
        super.clear();
        order.clear();
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("{");
        boolean first = true;

        for (Iterator<E> it = iterator(); it.hasNext();) {
            if (first) {
                first = false;
            } else {
                buf.append(", ");
            }

            buf.append(it.next());
        }

        buf.append("}");
        return buf.toString();
    }

    /**
     * Inserts a value into the set.
     *
     * @param obj an <code>Object</code> value
     * @return true if the set was modified by the add operation
     */
    @Override
    public boolean add(E obj) {
        int index = insertKey(obj);

        if (index < 0) {
            return false;       // already present in set, nothing to add
        }

        if (!order.add(index))
            throw new IllegalStateException("Order not changed after insert");

        postInsertHook(consumeFreeSlot);
        return true;            // yes, we added something
    }

    @Override
    protected void removeAt(int index) {
        // Remove from order first since super.removeAt can trigger compaction
        // making the index invalid afterwards
        order.remove(index);
        super.removeAt(index);
    }


    /**
     * Expands the set to accommodate new values.
     *
     * @param newCapacity an <code>int</code> value
     */
    @Override
    protected void rehash(int newCapacity) {
        TIntLinkedList oldOrder = new TIntLinkedList(order);
        int oldSize = size();

        Object oldSet[] = _set;

        order.clear();
        _set = new Object[newCapacity];
        Arrays.fill(_set, FREE);

        for (TIntIterator iterator = oldOrder.iterator(); iterator.hasNext();) {
            int i = iterator.next();
            E o = (E) oldSet[i];
            if (o == FREE || o == REMOVED) {
                throw new IllegalStateException("Iterating over empty location while rehashing");
            }

            if (o != FREE && o != REMOVED) {
                int index = insertKey(o);
                if (index < 0) { // everyone pays for this because some people can't RTFM
                    throwObjectContractViolation(_set[(-index - 1)], o, size(), oldSize, oldSet);
                }

                if (!order.add(index))
                    throw new IllegalStateException("Order not changed after insert");
            }
        }

    }

    class WriteProcedure implements TIntProcedure {
        final ObjectOutput output;
        IOException ioException;

        WriteProcedure(ObjectOutput output) {
            this.output = output;
        }

        public IOException getIoException() {
            return ioException;
        }

        public boolean execute(int value) {
            try {
                output.writeObject(_set[value]);
            } catch (IOException e) {
                ioException = e;
                return false;
            }
            return true;
        }

    }

    @Override
    protected void writeEntries(ObjectOutput out) throws IOException {
        // ENTRIES
        WriteProcedure writeProcedure = new WriteProcedure(out);
        if (!order.forEach(writeProcedure))
            throw writeProcedure.getIoException();
    }

    /**
     * Creates an iterator over the values of the set.  The iterator
     * supports element deletion.
     *
     * @return an <code>Iterator</code> value
     */
    @Override
    public TObjectHashIterator<E> iterator() {
        return new TObjectHashIterator<E>(this) {
            TIntIterator localIterator = order.iterator();
            int lastIndex;

            /**
             * Moves the iterator to the next Object and returns it.
             *
             * @return an <code>Object</code> value
             * @throws java.util.ConcurrentModificationException
             *          if the structure
             *          was changed using a method that isn't on this iterator.
             * @throws java.util.NoSuchElementException
             *          if this is called on an
             *          exhausted iterator.
             */
            @Override
            public E next() {
                lastIndex = localIterator.next();
                return objectAtIndex(lastIndex);
            }

            /**
             * Returns true if the iterator can be advanced past its current
             * location.
             *
             * @return a <code>boolean</code> value
             */
            @Override
            public boolean hasNext() {
                return localIterator.hasNext();    //To change body of overridden methods use File | Settings | File Templates.
            }

            /**
             * Removes the last entry returned by the iterator.
             * Invoking this method more than once for a single entry
             * will leave the underlying data structure in a confused
             * state.
             */
            @Override
            public void remove() {
                // Remove for iterator first
                localIterator.remove();
                // the removal within removeAt() will not change the collection
                // but the localIterator will remain valid
                try {
                    _hash.tempDisableAutoCompaction();
                    TLinkedHashSet.this.removeAt(lastIndex);
                } finally {
                    _hash.reenableAutoCompaction(false);
                }
            }
        };    //To change body of overridden methods use File | Settings | File Templates.
    }

    class ForEachProcedure implements TIntProcedure {
        boolean changed = false;
        final Object[] set;
        final TObjectProcedure<? super E> procedure;

        public ForEachProcedure(Object[] set, TObjectProcedure<? super E> procedure) {
            this.set = set;
            this.procedure = procedure;
        }

        /**
         * Executes this procedure. A false return value indicates that
         * the application executing this procedure should not invoke this
         * procedure again.
         *
         * @param value a value of type <code>int</code>
         * @return true if additional invocations of the procedure are
         *         allowed.
         */
        public boolean execute(int value) {
            return procedure.execute((E) set[value]);
        }
    }

    /**
     * Executes <tt>procedure</tt> for each element in the set.
     *
     * @param procedure a <code>TObjectProcedure</code> value
     * @return false if the loop over the set terminated because
     *         the procedure returned false for some value.
     */
    @Override
    public boolean forEach(TObjectProcedure<? super E> procedure) {
        ForEachProcedure forEachProcedure = new ForEachProcedure(_set, procedure);
        return order.forEach(forEachProcedure);
    }
}
