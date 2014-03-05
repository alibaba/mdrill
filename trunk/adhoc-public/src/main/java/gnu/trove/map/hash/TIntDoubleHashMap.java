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

package gnu.trove.map.hash;


//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.function.TDoubleFunction;
import gnu.trove.procedure.*;
import gnu.trove.set.*;
import gnu.trove.iterator.*;
import gnu.trove.iterator.hash.*;
import gnu.trove.impl.hash.*;
import gnu.trove.impl.HashFunctions;
import gnu.trove.*;

import java.io.*;
import java.util.*;

/**
 * An open addressed Map implementation for int keys and double values.
 *
 * @author Eric D. Friedman
 * @author Rob Eden
 * @author Jeff Randall
 * @version $Id: _K__V_HashMap.template,v 1.1.2.16 2010/03/02 04:09:50 robeden Exp $
 */
public class TIntDoubleHashMap extends TIntDoubleHash implements TIntDoubleMap, Externalizable {
    static final long serialVersionUID = 1L;

    /** the values of the map */
    protected transient double[] _values;


    /**
     * Creates a new <code>TIntDoubleHashMap</code> instance with the default
     * capacity and load factor.
     */
    public TIntDoubleHashMap() {
        super();
    }


    /**
     * Creates a new <code>TIntDoubleHashMap</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the default load factor.
     *
     * @param initialCapacity an <code>int</code> value
     */
    public TIntDoubleHashMap( int initialCapacity ) {
        super( initialCapacity );
    }


    /**
     * Creates a new <code>TIntDoubleHashMap</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the specified load factor.
     *
     * @param initialCapacity an <code>int</code> value
     * @param loadFactor a <code>float</code> value
     */
    public TIntDoubleHashMap( int initialCapacity, float loadFactor ) {
        super( initialCapacity, loadFactor );
    }


    /**
     * Creates a new <code>TIntDoubleHashMap</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the specified load factor.
     *
     * @param initialCapacity an <code>int</code> value
     * @param loadFactor a <code>float</code> value
     * @param noEntryKey a <code>int</code> value that represents
     *                   <tt>null</tt> for the Key set.
     * @param noEntryValue a <code>double</code> value that represents
     *                   <tt>null</tt> for the Value set.
     */
    public TIntDoubleHashMap( int initialCapacity, float loadFactor,
        int noEntryKey, double noEntryValue ) {
        super( initialCapacity, loadFactor, noEntryKey, noEntryValue );
    }


    /**
     * Creates a new <code>TIntDoubleHashMap</code> instance containing
     * all of the entries in the map passed in.
     *
     * @param keys a <tt>int</tt> array containing the keys for the matching values.
     * @param values a <tt>double</tt> array containing the values.
     */
    public TIntDoubleHashMap( int[] keys, double[] values ) {
        super( Math.max( keys.length, values.length ) );

        int size = Math.min( keys.length, values.length );
        for ( int i = 0; i < size; i++ ) {
            this.put( keys[i], values[i] );
        }
    }


    /**
     * Creates a new <code>TIntDoubleHashMap</code> instance containing
     * all of the entries in the map passed in.
     *
     * @param map a <tt>TIntDoubleMap</tt> that will be duplicated.
     */
    public TIntDoubleHashMap( TIntDoubleMap map ) {
        super( map.size() );
        if ( map instanceof TIntDoubleHashMap ) {
            TIntDoubleHashMap hashmap = ( TIntDoubleHashMap ) map;
            this._loadFactor = hashmap._loadFactor;
            this.no_entry_key = hashmap.no_entry_key;
            this.no_entry_value = hashmap.no_entry_value;
            //noinspection RedundantCast
            if ( this.no_entry_key != ( int ) 0 ) {
                Arrays.fill( _set, this.no_entry_key );
            }
            //noinspection RedundantCast
            if ( this.no_entry_value != ( double ) 0 ) {
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
    protected int setUp( int initialCapacity ) {
        int capacity;

        capacity = super.setUp( initialCapacity );
        _values = new double[capacity];
        return capacity;
    }


    /**
     * rehashes the map to the new capacity.
     *
     * @param newCapacity an <code>int</code> value
     */
     /** {@inheritDoc} */
    protected void rehash( int newCapacity ) {
        int oldCapacity = _set.length;
        
        int oldKeys[] = _set;
        double oldVals[] = _values;
        byte oldStates[] = _states;

        _set = new int[newCapacity];
        _values = new double[newCapacity];
        _states = new byte[newCapacity];

        for ( int i = oldCapacity; i-- > 0; ) {
            if( oldStates[i] == FULL ) {
                int o = oldKeys[i];
                int index = insertKey( o );
                _values[index] = oldVals[i];
            }
        }
    }


    /** {@inheritDoc} */
    public double put( int key, double value ) {
        int index = insertKey( key );
        return doPut( key, value, index );
    }


    /** {@inheritDoc} */
    public double putIfAbsent( int key, double value ) {
        int index = insertKey( key );
        if (index < 0)
            return _values[-index - 1];
        return doPut( key, value, index );
    }


    private double doPut( int key, double value, int index ) {
        double previous = no_entry_value;
        boolean isNewMapping = true;
        if ( index < 0 ) {
            index = -index -1;
            previous = _values[index];
            isNewMapping = false;
        }
        _values[index] = value;

        if (isNewMapping) {
            postInsertHook( consumeFreeSlot );
        }

        return previous;
    }


    /** {@inheritDoc} */
    public void putAll( Map<? extends Integer, ? extends Double> map ) {
        ensureCapacity( map.size() );
        // could optimize this for cases when map instanceof THashMap
        for ( Map.Entry<? extends Integer, ? extends Double> entry : map.entrySet() ) {
            this.put( entry.getKey().intValue(), entry.getValue().doubleValue() );
        }
    }
    

    /** {@inheritDoc} */
    public void putAll( TIntDoubleMap map ) {
        ensureCapacity( map.size() );
        TIntDoubleIterator iter = map.iterator();
        while ( iter.hasNext() ) {
            iter.advance();
            this.put( iter.key(), iter.value() );
        }
    }


    /** {@inheritDoc} */
    public double get( int key ) {
        int index = index( key );
        return index < 0 ? no_entry_value : _values[index];
    }


    /** {@inheritDoc} */
    public void clear() {
        super.clear();
        Arrays.fill( _set, 0, _set.length, no_entry_key );
        Arrays.fill( _values, 0, _values.length, no_entry_value );
        Arrays.fill( _states, 0, _states.length, FREE );
    }


    /** {@inheritDoc} */
    public boolean isEmpty() {
        return 0 == _size;
    }


    /** {@inheritDoc} */
    public double remove( int key ) {
        double prev = no_entry_value;
        int index = index( key );
        if ( index >= 0 ) {
            prev = _values[index];
            removeAt( index );    // clear key,state; adjust size
        }
        return prev;
    }


    /** {@inheritDoc} */
    protected void removeAt( int index ) {
        _values[index] = no_entry_value;
        super.removeAt( index );  // clear key, state; adjust size
    }


    /** {@inheritDoc} */
    public TIntSet keySet() {
        return new TKeyView();
    }


    /** {@inheritDoc} */
    public int[] keys() {
        int[] keys = new int[size()];
        int[] k = _set;
        byte[] states = _states;

        for ( int i = k.length, j = 0; i-- > 0; ) {
          if ( states[i] == FULL ) {
            keys[j++] = k[i];
          }
        }
        return keys;
    }


    /** {@inheritDoc} */
    public int[] keys( int[] array ) {
        int size = size();
        if ( array.length < size ) {
            array = new int[size];
        }

        int[] keys = _set;
        byte[] states = _states;

        for ( int i = keys.length, j = 0; i-- > 0; ) {
          if ( states[i] == FULL ) {
            array[j++] = keys[i];
          }
        }
        return array;
    }


    /** {@inheritDoc} */
    public TDoubleCollection valueCollection() {
        return new TValueView();
    }


    /** {@inheritDoc} */
    public double[] values() {
        double[] vals = new double[size()];
        double[] v = _values;
        byte[] states = _states;

        for ( int i = v.length, j = 0; i-- > 0; ) {
          if ( states[i] == FULL ) {
            vals[j++] = v[i];
          }
        }
        return vals;
    }


    /** {@inheritDoc} */
    public double[] values( double[] array ) {
        int size = size();
        if ( array.length < size ) {
            array = new double[size];
        }

        double[] v = _values;
        byte[] states = _states;

        for ( int i = v.length, j = 0; i-- > 0; ) {
          if ( states[i] == FULL ) {
            array[j++] = v[i];
          }
        }
        return array;
    }


    /** {@inheritDoc} */
    public boolean containsValue( double val ) {
        byte[] states = _states;
        double[] vals = _values;

        for ( int i = vals.length; i-- > 0; ) {
            if ( states[i] == FULL && val == vals[i] ) {
                return true;
            }
        }
        return false;
    }


    /** {@inheritDoc} */
    public boolean containsKey( int key ) {
        return contains( key );
    }


    /** {@inheritDoc} */
    public TIntDoubleIterator iterator() {
        return new TIntDoubleHashIterator( this );
    }


    /** {@inheritDoc} */
    public boolean forEachKey( TIntProcedure procedure ) {
        return forEach( procedure );
    }


    /** {@inheritDoc} */
    public boolean forEachValue( TDoubleProcedure procedure ) {
        byte[] states = _states;
        double[] values = _values;
        for ( int i = values.length; i-- > 0; ) {
            if ( states[i] == FULL && ! procedure.execute( values[i] ) ) {
                return false;
            }
        }
        return true;
    }


    /** {@inheritDoc} */
    public boolean forEachEntry( TIntDoubleProcedure procedure ) {
        byte[] states = _states;
        int[] keys = _set;
        double[] values = _values;
        for ( int i = keys.length; i-- > 0; ) {
            if ( states[i] == FULL && ! procedure.execute( keys[i], values[i] ) ) {
                return false;
            }
        }
        return true;
    }


    /** {@inheritDoc} */
    public void transformValues( TDoubleFunction function ) {
        byte[] states = _states;
        double[] values = _values;
        for ( int i = values.length; i-- > 0; ) {
            if ( states[i] == FULL ) {
                values[i] = function.execute( values[i] );
            }
        }
    }


    /** {@inheritDoc} */
    public boolean retainEntries( TIntDoubleProcedure procedure ) {
        boolean modified = false;
        byte[] states = _states;
        int[] keys = _set;
        double[] values = _values;


        // Temporarily disable compaction. This is a fix for bug #1738760
        tempDisableAutoCompaction();
        try {
            for ( int i = keys.length; i-- > 0; ) {
                if ( states[i] == FULL && ! procedure.execute( keys[i], values[i] ) ) {
                    removeAt( i );
                    modified = true;
                }
            }
        }
        finally {
            reenableAutoCompaction( true );
        }

        return modified;
    }


    /** {@inheritDoc} */
    public boolean increment( int key ) {
        return adjustValue( key, ( double ) 1 );
    }


    /** {@inheritDoc} */
    public boolean adjustValue( int key, double amount ) {
        int index = index( key );
        if (index < 0) {
            return false;
        } else {
            _values[index] += amount;
            return true;
        }
    }


    /** {@inheritDoc} */
    public double adjustOrPutValue( int key, double adjust_amount, double put_amount ) {
        int index = insertKey( key );
        final boolean isNewMapping;
        final double newValue;
        if ( index < 0 ) {
            index = -index -1;
            newValue = ( _values[index] += adjust_amount );
            isNewMapping = false;
        } else {
            newValue = ( _values[index] = put_amount );
            isNewMapping = true;
        }

        byte previousState = _states[index];

        if ( isNewMapping ) {
            postInsertHook(consumeFreeSlot);
        }

        return newValue;
    }


    /** a view onto the keys of the map. */
    protected class TKeyView implements TIntSet {

        /** {@inheritDoc} */
        public TIntIterator iterator() {
            return new TIntDoubleKeyHashIterator( TIntDoubleHashMap.this );
        }


        /** {@inheritDoc} */
        public int getNoEntryValue() {
            return no_entry_key;
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
        public boolean contains( int entry ) {
            return TIntDoubleHashMap.this.contains( entry );
        }


        /** {@inheritDoc} */
        public int[] toArray() {
            return TIntDoubleHashMap.this.keys();
        }


        /** {@inheritDoc} */
        public int[] toArray( int[] dest ) {
            return TIntDoubleHashMap.this.keys( dest );
        }


        /**
         * Unsupported when operating upon a Key Set view of a TIntDoubleMap
         * <p/>
         * {@inheritDoc}
         */
        public boolean add( int entry ) {
            throw new UnsupportedOperationException();
        }


        /** {@inheritDoc} */
        public boolean remove( int entry ) {
            return no_entry_value != TIntDoubleHashMap.this.remove( entry );
        }


        /** {@inheritDoc} */
        public boolean containsAll( Collection<?> collection ) {
            for ( Object element : collection ) {
                if ( element instanceof Integer ) {
                    int ele = ( ( Integer ) element ).intValue();
                    if ( ! TIntDoubleHashMap.this.containsKey( ele ) ) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        }


        /** {@inheritDoc} */
        public boolean containsAll( TIntCollection collection ) {
            TIntIterator iter = collection.iterator();
            while ( iter.hasNext() ) {
                if ( ! TIntDoubleHashMap.this.containsKey( iter.next() ) ) {
                    return false;
                }
            }
            return true;
        }


        /** {@inheritDoc} */
        public boolean containsAll( int[] array ) {
            for ( int element : array ) {
                if ( ! TIntDoubleHashMap.this.contains( element ) ) {
                    return false;
                }
            }
            return true;
        }


        /**
         * Unsupported when operating upon a Key Set view of a TIntDoubleMap
         * <p/>
         * {@inheritDoc}
         */
        public boolean addAll( Collection<? extends Integer> collection ) {
            throw new UnsupportedOperationException();
        }


        /**
         * Unsupported when operating upon a Key Set view of a TIntDoubleMap
         * <p/>
         * {@inheritDoc}
         */
        public boolean addAll( TIntCollection collection ) {
            throw new UnsupportedOperationException();
        }


        /**
         * Unsupported when operating upon a Key Set view of a TIntDoubleMap
         * <p/>
         * {@inheritDoc}
         */
        public boolean addAll( int[] array ) {
            throw new UnsupportedOperationException();
        }


        /** {@inheritDoc} */
        @SuppressWarnings({"SuspiciousMethodCalls"})
        public boolean retainAll( Collection<?> collection ) {
            boolean modified = false;
            TIntIterator iter = iterator();
            while ( iter.hasNext() ) {
                if ( ! collection.contains( Integer.valueOf ( iter.next() ) ) ) {
                    iter.remove();
                    modified = true;
                }
            }
            return modified;
        }


        /** {@inheritDoc} */
        public boolean retainAll( TIntCollection collection ) {
            if ( this == collection ) {
                return false;
            }
            boolean modified = false;
            TIntIterator iter = iterator();
            while ( iter.hasNext() ) {
                if ( ! collection.contains( iter.next() ) ) {
                    iter.remove();
                    modified = true;
                }
            }
            return modified;
        }


        /** {@inheritDoc} */
        public boolean retainAll( int[] array ) {
            boolean changed = false;
            Arrays.sort( array );
            int[] set = _set;
            byte[] states = _states;

            for ( int i = set.length; i-- > 0; ) {
                if ( states[i] == FULL && ( Arrays.binarySearch( array, set[i] ) < 0) ) {
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
                if ( element instanceof Integer ) {
                    int c = ( ( Integer ) element ).intValue();
                    if ( remove( c ) ) {
                        changed = true;
                    }
                }
            }
            return changed;
        }


        /** {@inheritDoc} */
        public boolean removeAll( TIntCollection collection ) {
            if ( this == collection ) {
                clear();
                return true;
            }
            boolean changed = false;
            TIntIterator iter = collection.iterator();
            while ( iter.hasNext() ) {
                int element = iter.next();
                if ( remove( element ) ) {
                    changed = true;
                }
            }
            return changed;
        }


        /** {@inheritDoc} */
        public boolean removeAll( int[] array ) {
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
            TIntDoubleHashMap.this.clear();
        }


        /** {@inheritDoc} */
        public boolean forEach( TIntProcedure procedure ) {
            return TIntDoubleHashMap.this.forEachKey( procedure );
        }


        @Override
        public boolean equals( Object other ) {
            if (! (other instanceof TIntSet)) {
                return false;
            }
            final TIntSet that = ( TIntSet ) other;
            if ( that.size() != this.size() ) {
                return false;
            }
            for ( int i = _states.length; i-- > 0; ) {
                if ( _states[i] == FULL ) {
                    if ( ! that.contains( _set[i] ) ) {
                        return false;
                    }
                }
            }
            return true;
        }


        @Override
        public int hashCode() {
            int hashcode = 0;
            for ( int i = _states.length; i-- > 0; ) {
                if ( _states[i] == FULL ) {
                    hashcode += HashFunctions.hash( _set[i] );
                }
            }
            return hashcode;
        }


        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder( "{" );
            forEachKey( new TIntProcedure() {
                private boolean first = true;


                public boolean execute( int key ) {
                    if ( first ) {
                        first = false;
                    } else {
                        buf.append( ", " );
                    }

                    buf.append( key );
                    return true;
                }
            } );
            buf.append( "}" );
            return buf.toString();
        }
    }


    /** a view onto the values of the map. */
    protected class TValueView implements TDoubleCollection {

        /** {@inheritDoc} */
        public TDoubleIterator iterator() {
            return new TIntDoubleValueHashIterator( TIntDoubleHashMap.this );
        }


        /** {@inheritDoc} */
        public double getNoEntryValue() {
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
        public boolean contains( double entry ) {
            return TIntDoubleHashMap.this.containsValue( entry );
        }


        /** {@inheritDoc} */
        public double[] toArray() {
            return TIntDoubleHashMap.this.values();
        }


        /** {@inheritDoc} */
        public double[] toArray( double[] dest ) {
            return TIntDoubleHashMap.this.values( dest );
        }



        public boolean add( double entry ) {
            throw new UnsupportedOperationException();
        }


        /** {@inheritDoc} */
        public boolean remove( double entry ) {
            double[] values = _values;
            int[] set = _set;

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
                if ( element instanceof Double ) {
                    double ele = ( ( Double ) element ).doubleValue();
                    if ( ! TIntDoubleHashMap.this.containsValue( ele ) ) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        }


        /** {@inheritDoc} */
        public boolean containsAll( TDoubleCollection collection ) {
            TDoubleIterator iter = collection.iterator();
            while ( iter.hasNext() ) {
                if ( ! TIntDoubleHashMap.this.containsValue( iter.next() ) ) {
                    return false;
                }
            }
            return true;
        }


        /** {@inheritDoc} */
        public boolean containsAll( double[] array ) {
            for ( double element : array ) {
                if ( ! TIntDoubleHashMap.this.containsValue( element ) ) {
                    return false;
                }
            }
            return true;
        }


        /** {@inheritDoc} */
        public boolean addAll( Collection<? extends Double> collection ) {
            throw new UnsupportedOperationException();
        }


        /** {@inheritDoc} */
        public boolean addAll( TDoubleCollection collection ) {
            throw new UnsupportedOperationException();
        }


        /** {@inheritDoc} */
        public boolean addAll( double[] array ) {
            throw new UnsupportedOperationException();
        }


        /** {@inheritDoc} */
        @SuppressWarnings({"SuspiciousMethodCalls"})
        public boolean retainAll( Collection<?> collection ) {
            boolean modified = false;
            TDoubleIterator iter = iterator();
            while ( iter.hasNext() ) {
                if ( ! collection.contains( Double.valueOf ( iter.next() ) ) ) {
                    iter.remove();
                    modified = true;
                }
            }
            return modified;
        }


        /** {@inheritDoc} */
        public boolean retainAll( TDoubleCollection collection ) {
            if ( this == collection ) {
                return false;
            }
            boolean modified = false;
            TDoubleIterator iter = iterator();
            while ( iter.hasNext() ) {
                if ( ! collection.contains( iter.next() ) ) {
                    iter.remove();
                    modified = true;
                }
            }
            return modified;
        }


        /** {@inheritDoc} */
        public boolean retainAll( double[] array ) {
            boolean changed = false;
            Arrays.sort( array );
            double[] values = _values;
            byte[] states = _states;

            for ( int i = values.length; i-- > 0; ) {
                if ( states[i] == FULL && ( Arrays.binarySearch( array, values[i] ) < 0) ) {
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
                if ( element instanceof Double ) {
                    double c = ( ( Double ) element ).doubleValue();
                    if ( remove( c ) ) {
                        changed = true;
                    }
                }
            }
            return changed;
        }


        /** {@inheritDoc} */
        public boolean removeAll( TDoubleCollection collection ) {
            if ( this == collection ) {
                clear();
                return true;
            }
            boolean changed = false;
            TDoubleIterator iter = collection.iterator();
            while ( iter.hasNext() ) {
                double element = iter.next();
                if ( remove( element ) ) {
                    changed = true;
                }
            }
            return changed;
        }


        /** {@inheritDoc} */
        public boolean removeAll( double[] array ) {
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
            TIntDoubleHashMap.this.clear();
        }


        /** {@inheritDoc} */
        public boolean forEach( TDoubleProcedure procedure ) {
            return TIntDoubleHashMap.this.forEachValue( procedure );
        }


        /** {@inheritDoc} */
        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder( "{" );
            forEachValue( new TDoubleProcedure() {
                private boolean first = true;

                public boolean execute( double value ) {
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
    }


    class TIntDoubleKeyHashIterator extends THashPrimitiveIterator implements TIntIterator {

        /**
         * Creates an iterator over the specified map
         *
         * @param hash the <tt>TPrimitiveHash</tt> we will be iterating over.
         */
        TIntDoubleKeyHashIterator( TPrimitiveHash hash ) {
            super( hash );
        }

        /** {@inheritDoc} */
        public int next() {
            moveToNextIndex();
            return _set[_index];
        }

        /** @{inheritDoc} */
        public void remove() {
            if ( _expectedSize != _hash.size() ) {
                throw new ConcurrentModificationException();
            }

            // Disable auto compaction during the remove. This is a workaround for bug 1642768.
            try {
                _hash.tempDisableAutoCompaction();
                TIntDoubleHashMap.this.removeAt( _index );
            }
            finally {
                _hash.reenableAutoCompaction( false );
            }

            _expectedSize--;
        }
    }


   
    class TIntDoubleValueHashIterator extends THashPrimitiveIterator implements TDoubleIterator {

        /**
         * Creates an iterator over the specified map
         *
         * @param hash the <tt>TPrimitiveHash</tt> we will be iterating over.
         */
        TIntDoubleValueHashIterator( TPrimitiveHash hash ) {
            super( hash );
        }

        /** {@inheritDoc} */
        public double next() {
            moveToNextIndex();
            return _values[_index];
        }

        /** @{inheritDoc} */
        public void remove() {
            if ( _expectedSize != _hash.size() ) {
                throw new ConcurrentModificationException();
            }

            // Disable auto compaction during the remove. This is a workaround for bug 1642768.
            try {
                _hash.tempDisableAutoCompaction();
                TIntDoubleHashMap.this.removeAt( _index );
            }
            finally {
                _hash.reenableAutoCompaction( false );
            }

            _expectedSize--;
        }
    }


    class TIntDoubleHashIterator extends THashPrimitiveIterator implements TIntDoubleIterator {

        /**
         * Creates an iterator over the specified map
         *
         * @param map the <tt>TIntDoubleHashMap</tt> we will be iterating over.
         */
        TIntDoubleHashIterator( TIntDoubleHashMap map ) {
            super( map );
        }

        /** {@inheritDoc} */
        public void advance() {
            moveToNextIndex();
        }

        /** {@inheritDoc} */
        public int key() {
            return _set[_index];
        }

        /** {@inheritDoc} */
        public double value() {
            return _values[_index];
        }

        /** {@inheritDoc} */
        public double setValue( double val ) {
            double old = value();
            _values[_index] = val;
            return old;
        }

        /** @{inheritDoc} */
        public void remove() {
            if ( _expectedSize != _hash.size() ) {
                throw new ConcurrentModificationException();
            }
            // Disable auto compaction during the remove. This is a workaround for bug 1642768.
            try {
                _hash.tempDisableAutoCompaction();
                TIntDoubleHashMap.this.removeAt( _index );
            }
            finally {
                _hash.reenableAutoCompaction( false );
            }
            _expectedSize--;
        }
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals( Object other ) {
        if ( ! ( other instanceof TIntDoubleMap ) ) {
            return false;
        }
        TIntDoubleMap that = ( TIntDoubleMap ) other;
        if ( that.size() != this.size() ) {
            return false;
        }
        double[] values = _values;
        byte[] states = _states;
        double this_no_entry_value = getNoEntryValue();
        double that_no_entry_value = that.getNoEntryValue();
        for ( int i = values.length; i-- > 0; ) {
            if ( states[i] == FULL ) {
                int key = _set[i];
                double that_value = that.get( key );
                double this_value = values[i];
                if ( ( this_value != that_value ) &&
                     ( this_value != this_no_entry_value ) &&
                     ( that_value != that_no_entry_value ) ) {
                    return false;
                }
            }
        }
        return true;
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hashcode = 0;
        byte[] states = _states;
        for ( int i = _values.length; i-- > 0; ) {
            if ( states[i] == FULL ) {
                hashcode += HashFunctions.hash( _set[i] ) ^
                            HashFunctions.hash( _values[i] );
            }
        }
        return hashcode;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder( "{" );
        forEachEntry( new TIntDoubleProcedure() {
            private boolean first = true;
            public boolean execute( int key, double value ) {
                if ( first ) first = false;
                else buf.append( ", " );

                buf.append(key);
                buf.append("=");
                buf.append(value);
                return true;
            }
        });
        buf.append( "}" );
        return buf.toString();
    }


    /** {@inheritDoc} */
    public void writeExternal(ObjectOutput out) throws IOException {
        // VERSION
    	out.writeByte( 0 );

        // SUPER
    	super.writeExternal( out );

    	// NUMBER OF ENTRIES
    	out.writeInt( _size );

    	// ENTRIES
    	for ( int i = _states.length; i-- > 0; ) {
            if ( _states[i] == FULL ) {
                out.writeInt( _set[i] );
                out.writeDouble( _values[i] );
            }
        }
    }


    /** {@inheritDoc} */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // VERSION
    	in.readByte();

        // SUPER
    	super.readExternal( in );

    	// NUMBER OF ENTRIES
    	int size = in.readInt();
    	setUp( size );

    	// ENTRIES
        while (size-- > 0) {
            int key = in.readInt();
            double val = in.readDouble();
            put(key, val);
        }
    }
} // TIntDoubleHashMap
