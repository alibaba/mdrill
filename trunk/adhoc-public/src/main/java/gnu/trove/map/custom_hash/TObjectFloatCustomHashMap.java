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

package gnu.trove.map.custom_hash;

import gnu.trove.TFloatCollection;
import gnu.trove.function.TFloatFunction;
import gnu.trove.impl.Constants;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.TCustomObjectHash;
import gnu.trove.impl.hash.THash;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.procedure.TFloatProcedure;
import gnu.trove.procedure.TObjectFloatProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.strategy.HashingStrategy;

import java.io.*;
import java.util.*;



//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * An open addressed Map implementation for Object keys and float values.
 *
 * @author Rob Eden
 */
public class TObjectFloatCustomHashMap<K> extends TCustomObjectHash<K>
    implements TObjectFloatMap<K>, Externalizable {

    static final long serialVersionUID = 1L;

    private final TObjectFloatProcedure<K> PUT_ALL_PROC = new TObjectFloatProcedure<K>() {
        public boolean execute(K key, float value) {
            put(key, value);
            return true;
        }
    };

    /** the values of the map */
    protected transient float[] _values;

    /** the value that represents null */
    protected float no_entry_value;


	/** FOR EXTERNALIZATION ONLY!!! */
	public TObjectFloatCustomHashMap() {}


    /**
     * Creates a new <code>TObjectFloatHashMap</code> instance with the default
     * capacity and load factor.
     */
    public TObjectFloatCustomHashMap( HashingStrategy<? super K> strategy ) {
        super( strategy );
        no_entry_value = Constants.DEFAULT_FLOAT_NO_ENTRY_VALUE;
    }


    /**
     * Creates a new <code>TObjectFloatHashMap</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the default load factor.
     *
     * @param initialCapacity an <code>int</code> value
     */
    public TObjectFloatCustomHashMap( HashingStrategy<? super K> strategy,
		int initialCapacity ) {

        super( strategy, initialCapacity );

        no_entry_value = Constants.DEFAULT_FLOAT_NO_ENTRY_VALUE;
    }


    /**
     * Creates a new <code>TObjectFloatHashMap</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the specified load factor.
     *
     * @param initialCapacity an <code>int</code> value
     * @param loadFactor a <code>float</code> value
     */
    public TObjectFloatCustomHashMap( HashingStrategy<? super K> strategy,
		int initialCapacity, float loadFactor ) {

        super( strategy, initialCapacity, loadFactor );

        no_entry_value = Constants.DEFAULT_FLOAT_NO_ENTRY_VALUE;
    }


    /**
     * Creates a new <code>TObjectFloatHashMap</code> instance with a prime
     * value at or near the specified capacity and load factor.
     *
     * @param initialCapacity used to find a prime capacity for the table.
     * @param loadFactor used to calculate the threshold over which
     * rehashing takes place.
     * @param noEntryValue the value used to represent null.
     */
    public TObjectFloatCustomHashMap( HashingStrategy<? super K> strategy,
		int initialCapacity, float loadFactor, float noEntryValue ) {

        super( strategy, initialCapacity, loadFactor );

        no_entry_value = noEntryValue;
        //noinspection RedundantCast
        if ( no_entry_value != ( float ) 0 ) {
            Arrays.fill( _values, no_entry_value );
        }
    }


    /**
     * Creates a new <code>TObjectFloatCustomHashMap</code> that contains the entries
     * in the map passed to it.
     *
     * @param map the <tt>TObjectFloatMap</tt> to be copied.
     */
    public TObjectFloatCustomHashMap( HashingStrategy<? super K> strategy,
		TObjectFloatMap<? extends K> map ) {
	
        this( strategy, map.size(), 0.5f, map.getNoEntryValue() );

        if ( map instanceof TObjectFloatCustomHashMap ) {
            TObjectFloatCustomHashMap hashmap = ( TObjectFloatCustomHashMap ) map;
            this._loadFactor = hashmap._loadFactor;
            this.no_entry_value = hashmap.no_entry_value;
            this.strategy = hashmap.strategy;
            //noinspection RedundantCast
            if ( this.no_entry_value != ( float ) 0 ) {
                Arrays.fill( _values, this.no_entry_value );
            }
            setUp( (int) Math.ceil( DEFAULT_CAPACITY / _loadFactor ) );
        }
        putAll( map );
    }


    /**
     * initializes the hashtable to a prime capacity which is at least
     * <tt>initialCapacity + 1</tt>.
     *
     * @param initialCapacity an <code>int</code> value
     * @return the actual capacity chosen
     */
    public int setUp( int initialCapacity ) {
        int capacity;

        capacity = super.setUp( initialCapacity );
        _values = new float[capacity];
        return capacity;
    }


    /**
     * rehashes the map to the new capacity.
     *
     * @param newCapacity an <code>int</code> value
     */
    protected void rehash( int newCapacity ) {
        int oldCapacity = _set.length;

        //noinspection unchecked
        K oldKeys[] = ( K[] ) _set;
        float oldVals[] = _values;

        _set = new Object[newCapacity];
        Arrays.fill( _set, FREE );
        _values = new float[newCapacity];
        Arrays.fill( _values, no_entry_value );

        for ( int i = oldCapacity; i-- > 0; ) {
          K o = oldKeys[i];
          if( o != FREE && o != REMOVED ) {
                int index = insertKey(o);
                if ( index < 0 ) {
                    throwObjectContractViolation( _set[ (-index -1) ], o);
                }
                _values[index] = oldVals[i];
            }
        }
    }


    // Query Operations

    /** {@inheritDoc} */
    public float getNoEntryValue() {
        return no_entry_value;
    }


    /** {@inheritDoc} */
    public boolean containsKey( Object key ) {
        return contains( key );
    }


    /** {@inheritDoc} */
    public boolean containsValue( float val ) {
        Object[] keys = _set;
        float[] vals = _values;

        for ( int i = vals.length; i-- > 0; ) {
            if ( keys[i] != FREE && keys[i] != REMOVED && val == vals[i] ) {
                return true;
            }
        }
        return false;
    }


    /** {@inheritDoc} */
    public float get( Object key ) {
        int index = index( key );
        return index < 0 ? no_entry_value : _values[index];
    }


    // Modification Operations

    /** {@inheritDoc} */
    public float put( K key, float value ) {
        int index = insertKey( key );
        return doPut( value, index );
    }


    /** {@inheritDoc} */
    public float putIfAbsent( K key, float value ) {
        int index = insertKey(key);
        if ( index < 0 )
            return _values[-index - 1];
        return doPut( value, index );
    }


    private float doPut( float value, int index ) {
        float previous = no_entry_value;
        boolean isNewMapping = true;
        if ( index < 0 ) {
            index = -index -1;
            previous = _values[index];
            isNewMapping = false;
        }
        //noinspection unchecked
        _values[index] = value;

        if ( isNewMapping ) {
            postInsertHook( consumeFreeSlot );
        }
        return previous;
    }


    /** {@inheritDoc} */
    public float remove( Object key ) {
        float prev = no_entry_value;
        int index = index(key);
        if ( index >= 0 ) {
            prev = _values[index];
            removeAt( index );    // clear key,state; adjust size
        }
        return prev;
    }


    /**
     * Removes the mapping at <tt>index</tt> from the map.
     * This method is used internally and public mainly because
     * of packaging reasons.  Caveat Programmer.
     *
     * @param index an <code>int</code> value
     */
    protected void removeAt( int index ) {
        _values[index] = no_entry_value;
        super.removeAt( index );  // clear key, state; adjust size
    }


    // Bulk Operations

    /** {@inheritDoc} */
    public void putAll( Map<? extends K, ? extends Float> map ) {
        Set<? extends Map.Entry<? extends K,? extends Float>> set = map.entrySet();
        for ( Map.Entry<? extends K,? extends Float> entry : set ) {
            put( entry.getKey(), entry.getValue() );
        }
    }
    

    /** {@inheritDoc} */
    public void putAll( TObjectFloatMap<? extends K> map ){
        map.forEachEntry( PUT_ALL_PROC );
    }


    /** {@inheritDoc} */
    public void clear() {
        super.clear();
        Arrays.fill( _set, 0, _set.length, FREE );
        Arrays.fill( _values, 0, _values.length, no_entry_value );
    }


    // Views

    /** {@inheritDoc} */
    public Set<K> keySet() {
        return new KeyView();
    }


    /** {@inheritDoc} */
    public Object[] keys() {
        //noinspection unchecked
        K[] keys = ( K[] ) new Object[size()];
        Object[] k = _set;

        for ( int i = k.length, j = 0; i-- > 0; ) {
            if ( k[i] != FREE && k[i] != REMOVED ) {
                //noinspection unchecked
                keys[j++] = ( K ) k[i];
            }
        }
        return keys;
    }


    /** {@inheritDoc} */
    public K[] keys( K[] a ) {
        int size = size();
        if ( a.length < size ) {
            //noinspection unchecked
            a = ( K[] ) java.lang.reflect.Array.newInstance(
                          a.getClass().getComponentType(), size );
        }

        Object[] k = _set;

        for ( int i = k.length, j = 0; i-- > 0; ) {
            if ( k[i] != FREE && k[i] != REMOVED ) {
                //noinspection unchecked
                a[j++] = ( K ) k[i];
            }
        }
        return a;
    }


    /** {@inheritDoc} */
    public TFloatCollection valueCollection() {
        return new TFloatValueCollection();
    }


    /** {@inheritDoc} */
    public float[] values() {
        float[] vals = new float[size()];
        float[] v = _values;
        Object[] keys = _set;

        for ( int i = v.length, j = 0; i-- > 0; ) {
            if ( keys[i] != FREE && keys[i] != REMOVED ) {
                vals[j++] = v[i];
            }
        }
        return vals;
    }


    /** {@inheritDoc} */
    public float[] values( float[] array ) {
        int size = size();
        if ( array.length < size ) {
            array = new float[size];
        }

        float[] v = _values;
        Object[] keys = _set;

        for ( int i = v.length, j = 0; i-- > 0; ) {
            if ( keys[i] != FREE && keys[i] != REMOVED ) {
                array[j++] = v[i];
            }
        }
        if ( array.length > size ) {
            array[size] = no_entry_value;
        }
        return array;
    }


    /**
     * @return an iterator over the entries in this map
     */
    public TObjectFloatIterator<K> iterator() {
        return new TObjectFloatHashIterator<K>( this );
    }


    /** {@inheritDoc} */
    @SuppressWarnings({"RedundantCast"})
    public boolean increment( K key ) {
        //noinspection RedundantCast
        return adjustValue( key, (float)1 );
    }


    /** {@inheritDoc} */
    public boolean adjustValue( K key, float amount ) {
        int index = index(key);
        if ( index < 0 ) {
            return false;
        } else {
            _values[index] += amount;
            return true;
        }
    }


    /** {@inheritDoc} */
    public float adjustOrPutValue( final K key, final float adjust_amount,
		final float put_amount ) {

        int index = insertKey( key );
        final boolean isNewMapping;
        final float newValue;
        if ( index < 0 ) {
            index = -index -1;
            newValue = ( _values[index] += adjust_amount );
            isNewMapping = false;
        } else {
            newValue = ( _values[index] = put_amount );
            isNewMapping = true;
        }

        //noinspection unchecked
        if ( isNewMapping ) {
            postInsertHook( consumeFreeSlot );
        }

        return newValue;
    }


    /**
     * Executes <tt>procedure</tt> for each key in the map.
     *
     * @param procedure a <code>TObjectProcedure</code> value
     * @return false if the loop over the keys terminated because
     * the procedure returned false for some key.
     */
    public boolean forEachKey( TObjectProcedure<? super K> procedure ) {
        return forEach( procedure );
    }


    /**
     * Executes <tt>procedure</tt> for each value in the map.
     *
     * @param procedure a <code>TFloatProcedure</code> value
     * @return false if the loop over the values terminated because
     * the procedure returned false for some value.
     */
    public boolean forEachValue( TFloatProcedure procedure ) {
        Object[] keys = _set;
        float[] values = _values;
        for ( int i = values.length; i-- > 0; ) {
            if ( keys[i] != FREE && keys[i] != REMOVED
                && ! procedure.execute( values[i] ) ) {
                return false;
            }
        }
        return true;
    }


    /**
     * Executes <tt>procedure</tt> for each key/value entry in the
     * map.
     *
     * @param procedure a <code>TOObjectFloatProcedure</code> value
     * @return false if the loop over the entries terminated because
     * the procedure returned false for some entry.
     */
    @SuppressWarnings({"unchecked"})
    public boolean forEachEntry( TObjectFloatProcedure<? super K> procedure ) {
        Object[] keys = _set;
        float[] values = _values;
        for ( int i = keys.length; i-- > 0; ) {
            if ( keys[i] != FREE
                && keys[i] != REMOVED
                && ! procedure.execute( ( K ) keys[i], values[i] ) ) {
                return false;
            }
        }
        return true;
    }


    /**
     * Retains only those entries in the map for which the procedure
     * returns a true value.
     *
     * @param procedure determines which entries to keep
     * @return true if the map was modified.
     */
    public boolean retainEntries( TObjectFloatProcedure<? super K> procedure ) {
        boolean modified = false;
        //noinspection unchecked
        K[] keys = ( K[] ) _set;
        float[] values = _values;

        // Temporarily disable compaction. This is a fix for bug #1738760
        tempDisableAutoCompaction();
        try {
            for ( int i = keys.length; i-- > 0; ) {
                if ( keys[i] != FREE
                    && keys[i] != REMOVED
                    && ! procedure.execute( keys[i], values[i] ) ) {
                    removeAt(i);
                    modified = true;
                }
            }
        }
        finally {
            reenableAutoCompaction( true );
        }

        return modified;
    }


    /**
     * Transform the values in this map using <tt>function</tt>.
     *
     * @param function a <code>TFloatFunction</code> value
     */
    public void transformValues( TFloatFunction function ) {
        Object[] keys = _set;
        float[] values = _values;
        for ( int i = values.length; i-- > 0; ) {
            if ( keys[i] != null && keys[i] != REMOVED ) {
                values[i] = function.execute( values[i] );
            }
        }
    }


    // Comparison and hashing

    /**
     * Compares this map with another map for equality of their stored
     * entries.
     *
     * @param other an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean equals( Object other ) {
        if ( ! ( other instanceof TObjectFloatMap ) ) {
            return false;
        }
        TObjectFloatMap that = ( TObjectFloatMap ) other;
        if ( that.size() != this.size() ) {
            return false;
        }
        try {
            TObjectFloatIterator iter = this.iterator();
            while ( iter.hasNext() ) {
                iter.advance();
                Object key = iter.key();
                float value = iter.value();
                if ( value == no_entry_value ) {
                    if ( !( that.get( key ) == that.getNoEntryValue() &&
	                    that.containsKey( key ) ) ) {

                        return false;
                    }
                } else {
                    if ( value != that.get( key ) ) {
                        return false;
                    }
                }
            }
        } catch ( ClassCastException ex ) {
            // unused.
        }
        return true;
    }


    /** {@inheritDoc} */
    public int hashCode() {
        int hashcode = 0;
        Object[] keys = _set;
        float[] values = _values;
        for ( int i = values.length; i-- > 0; ) {
            if ( keys[i] != FREE && keys[i] != REMOVED ) {
                hashcode += HashFunctions.hash( values[i] ) ^
                            ( keys[i] == null ? 0 : keys[i].hashCode() );
            }
        }
        return hashcode;
    }


    /** a view onto the keys of the map. */
    protected class KeyView extends MapBackedView<K> {

        @SuppressWarnings({"unchecked"})
        public Iterator<K> iterator() {
            return new TObjectHashIterator( TObjectFloatCustomHashMap.this );
        }

        public boolean removeElement( K key ) {
            return no_entry_value != TObjectFloatCustomHashMap.this.remove( key );
        }

        public boolean containsElement( K key ) {
            return TObjectFloatCustomHashMap.this.contains( key );
        }
    }


    private abstract class MapBackedView<E> extends AbstractSet<E>
        implements Set<E>, Iterable<E> {

        public abstract boolean removeElement( E key );

        public abstract boolean containsElement( E key );

        @SuppressWarnings({"unchecked"})
        public boolean contains( Object key ) {
            return containsElement( (E) key );
        }

        @SuppressWarnings({"unchecked"})
        public boolean remove( Object o ) {
            return removeElement( (E) o );
        }

        public void clear() {
            TObjectFloatCustomHashMap.this.clear();
        }

        public boolean add( E obj ) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return TObjectFloatCustomHashMap.this.size();
        }

        public Object[] toArray() {
            Object[] result = new Object[size()];
            Iterator<E> e = iterator();
            for ( int i = 0; e.hasNext(); i++ ) {
                result[i] = e.next();
            }
            return result;
        }

        public <T> T[] toArray( T[] a ) {
            int size = size();
            if ( a.length < size ) {
                //noinspection unchecked
                a = (T[]) java.lang.reflect.Array.newInstance(
	                a.getClass().getComponentType(), size );
            }

            Iterator<E> it = iterator();
            Object[] result = a;
            for ( int i = 0; i < size; i++ ) {
                result[i] = it.next();
            }

            if ( a.length > size ) {
                a[size] = null;
            }

            return a;
        }

        public boolean isEmpty() {
            return TObjectFloatCustomHashMap.this.isEmpty();
        }

        public boolean addAll( Collection<? extends E> collection ) {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings({"SuspiciousMethodCalls"})
        public boolean retainAll( Collection<?> collection ) {
            boolean changed = false;
            Iterator<E> i = iterator();
            while ( i.hasNext() ) {
                if ( !collection.contains( i.next() ) ) {
                    i.remove();
                    changed = true;
                }
            }
            return changed;
        }
    }


    class TFloatValueCollection implements TFloatCollection {

        /** {@inheritDoc} */
        public TFloatIterator iterator() {
            return new TObjectFloatValueHashIterator();
        }

        /** {@inheritDoc} */
        public float getNoEntryValue() {
            return no_entry_value;
        }

        /** {@inheritDoc} */
        public int size() {
            return _size;
        }

        /** {@inheritDoc} */
        public boolean isEmpty() {
            return 0 == _size;
        }

        /** {@inheritDoc} */
        public boolean contains( float entry ) {
            return TObjectFloatCustomHashMap.this.containsValue( entry );
        }

        /** {@inheritDoc} */
        public float[] toArray() {
            return TObjectFloatCustomHashMap.this.values();
        }

        /** {@inheritDoc} */
        public float[] toArray( float[] dest ) {
            return TObjectFloatCustomHashMap.this.values( dest );
        }

        public boolean add( float entry ) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        public boolean remove( float entry ) {
            float[] values = _values;
            Object[] set = _set;

            for ( int i = values.length; i-- > 0; ) {
                if ( ( set[i] != FREE && set[i] != REMOVED ) && entry == values[i] ) {
                    removeAt( i );
                    return true;
                }
            }
            return false;
        }

        /** {@inheritDoc} */
        public boolean containsAll( Collection<?> collection ) {
            for ( Object element : collection ) {
                if ( element instanceof Float ) {
                    float ele = ( ( Float ) element ).floatValue();
                    if ( ! TObjectFloatCustomHashMap.this.containsValue( ele ) ) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        }

        /** {@inheritDoc} */
        public boolean containsAll( TFloatCollection collection ) {
            TFloatIterator iter = collection.iterator();
            while ( iter.hasNext() ) {
                if ( ! TObjectFloatCustomHashMap.this.containsValue( iter.next() ) ) {
                    return false;
                }
            }
            return true;
        }

        /** {@inheritDoc} */
        public boolean containsAll( float[] array ) {
            for ( float element : array ) {
                if ( ! TObjectFloatCustomHashMap.this.containsValue( element ) ) {
                    return false;
                }
            }
            return true;
        }

        /** {@inheritDoc} */
        public boolean addAll( Collection<? extends Float> collection ) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        public boolean addAll( TFloatCollection collection ) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        public boolean addAll( float[] array ) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @SuppressWarnings({"SuspiciousMethodCalls"})
        public boolean retainAll( Collection<?> collection ) {
            boolean modified = false;
            TFloatIterator iter = iterator();
            while ( iter.hasNext() ) {
                if ( ! collection.contains( Float.valueOf ( iter.next() ) ) ) {
                    iter.remove();
                    modified = true;
                }
            }
            return modified;
        }

        /** {@inheritDoc} */
        public boolean retainAll( TFloatCollection collection ) {
            if ( this == collection ) {
                return false;
            }
            boolean modified = false;
            TFloatIterator iter = iterator();
            while ( iter.hasNext() ) {
                if ( ! collection.contains( iter.next() ) ) {
                    iter.remove();
                    modified = true;
                }
            }
            return modified;
        }

        /** {@inheritDoc} */
        public boolean retainAll( float[] array ) {
            boolean changed = false;
            Arrays.sort( array );
            float[] values = _values;

            Object[] set = _set;
            for ( int i = set.length; i-- > 0; ) {
                if ( set[i] != FREE
                     && set[i] != REMOVED
                     && ( Arrays.binarySearch( array, values[i] ) < 0) ) {
                    removeAt( i );
                    changed = true;
                }
            }
            return changed;
        }

        /** {@inheritDoc} */
        public boolean removeAll( Collection<?> collection ) {
            boolean changed = false;
            for ( Object element : collection ) {
                if ( element instanceof Float ) {
                    float c = ( ( Float ) element ).floatValue();
                    if ( remove( c ) ) {
                        changed = true;
                    }
                }
            }
            return changed;
        }

        /** {@inheritDoc} */
        public boolean removeAll( TFloatCollection collection ) {
            if ( this == collection ) {
                clear();
                return true;
            }
            boolean changed = false;
            TFloatIterator iter = collection.iterator();
            while ( iter.hasNext() ) {
                float element = iter.next();
                if ( remove( element ) ) {
                    changed = true;
                }
            }
            return changed;
        }

        /** {@inheritDoc} */
        public boolean removeAll( float[] array ) {
            boolean changed = false;
            for ( int i = array.length; i-- > 0; ) {
                if ( remove( array[i] ) ) {
                    changed = true;
                }
            }
            return changed;
        }

        /** {@inheritDoc} */
        public void clear() {
            TObjectFloatCustomHashMap.this.clear();
        }

        /** {@inheritDoc} */
        public boolean forEach( TFloatProcedure procedure ) {
            return TObjectFloatCustomHashMap.this.forEachValue( procedure );
        }


        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder( "{" );
            forEachValue( new TFloatProcedure() {
                private boolean first = true;

                public boolean execute( float value ) {
                    if ( first ) {
                        first = false;
                    } else {
                        buf.append( ", " );
                    }

                    buf.append( value );
                    return true;
                }
            } );
            buf.append( "}" );
            return buf.toString();
        }


        class TObjectFloatValueHashIterator implements TFloatIterator {

            protected THash _hash = TObjectFloatCustomHashMap.this;

            /**
             * the number of elements this iterator believes are in the
             * data structure it accesses.
             */
            protected int _expectedSize;

            /** the index used for iteration. */
            protected int _index;

            /** Creates an iterator over the specified map */
            TObjectFloatValueHashIterator() {
                _expectedSize = _hash.size();
                _index = _hash.capacity();
            }

            /** {@inheritDoc} */
            public boolean hasNext() {
                return nextIndex() >= 0;
            }

            /** {@inheritDoc} */
            public float next() {
                moveToNextIndex();
                return _values[_index];
            }

            /** @{inheritDoc} */
            public void remove() {
                if ( _expectedSize != _hash.size() ) {
                    throw new ConcurrentModificationException();
                }

                // Disable auto compaction during the remove. This is a workaround for
                // bug 1642768.
                try {
                    _hash.tempDisableAutoCompaction();
                    TObjectFloatCustomHashMap.this.removeAt( _index );
                }
                finally {
                    _hash.reenableAutoCompaction( false );
                }

                _expectedSize--;
            }

            /**
             * Sets the internal <tt>index</tt> so that the `next' object
             * can be returned.
             */
            protected final void moveToNextIndex() {
                // doing the assignment && < 0 in one line shaves
                // 3 opcodes...
                if ( ( _index = nextIndex() ) < 0 ) {
                    throw new NoSuchElementException();
                }
            }

            /**
             * Returns the index of the next value in the data structure
             * or a negative value if the iterator is exhausted.
             *
             * @return an <code>int</code> value
             * @throws java.util.ConcurrentModificationException
             *          if the underlying
             *          collection's size has been modified since the iterator was
             *          created.
             */
            protected final int nextIndex() {
                if ( _expectedSize != _hash.size() ) {
                    throw new ConcurrentModificationException();
                }

                Object[] set = TObjectFloatCustomHashMap.this._set;
                int i = _index;
                while ( i-- > 0 && ( set[i] == TCustomObjectHash.FREE ||
					set[i] == TCustomObjectHash.REMOVED ) ) {
                    ;
                }
                return i;
            }
        }
    }


    class TObjectFloatHashIterator<K> extends TObjectHashIterator<K>
        implements TObjectFloatIterator<K> {

        /** the collection being iterated over */
        private final TObjectFloatCustomHashMap<K> _map;

        public TObjectFloatHashIterator( TObjectFloatCustomHashMap<K> map ) {
            super( map );
            this._map = map;
        }

        /** {@inheritDoc} */
        public void advance() {
            moveToNextIndex();
        }

        /** {@inheritDoc} */
        @SuppressWarnings({"unchecked"})
        public K key() {
            return ( K ) _map._set[_index];
        }

        /** {@inheritDoc} */
        public float value() {
            return _map._values[_index];
        }

        /** {@inheritDoc} */
        public float setValue( float val ) {
            float old = value();
            _map._values[_index] = val;
            return old;
        }
    }


    // Externalization

    public void writeExternal( ObjectOutput out ) throws IOException {
        // VERSION
        out.writeByte( 0 );

        // SUPER
        super.writeExternal( out );

		// STRATEGY
		out.writeObject( strategy );

        // NO_ENTRY_VALUE
        out.writeFloat( no_entry_value );

        // NUMBER OF ENTRIES
        out.writeInt( _size );

        // ENTRIES
        for ( int i = _set.length; i-- > 0; ) {
            if ( _set[i] != REMOVED && _set[i] != FREE ) {
                out.writeObject( _set[i] );
                out.writeFloat( _values[i] );
            }
        }
    }


    public void readExternal( ObjectInput in )
        throws IOException, ClassNotFoundException {

        // VERSION
        in.readByte();

        // SUPER
        super.readExternal( in );

		// STRATEGY
		strategy = ( HashingStrategy<K> ) in.readObject();

        // NO_ENTRY_VALUE
        no_entry_value = in.readFloat();

        // NUMBER OF ENTRIES
        int size = in.readInt();
        setUp( size );

        // ENTRIES
        while (size-- > 0) {
            //noinspection unchecked
            K key = ( K ) in.readObject();
            float val = in.readFloat();
            put(key, val);
        }
    }


    /** {@inheritDoc} */
    public String toString() {
        final StringBuilder buf = new StringBuilder("{");
        forEachEntry( new TObjectFloatProcedure<K>() {
            private boolean first = true;
            public boolean execute( K key, float value ) {
                if ( first ) first = false;
                else buf.append( "," );

                buf.append( key ).append( "=" ).append( value );
                return true;
            }
        });
        buf.append( "}" );
        return buf.toString();
    }
}
