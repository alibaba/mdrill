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

import gnu.trove.set.TByteSet;
import gnu.trove.iterator.TByteIterator;
import gnu.trove.impl.*;
import gnu.trove.impl.hash.*;
import gnu.trove.TByteCollection;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Externalizable;
import java.util.Arrays;
import java.util.Collection;


//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * An open addressed set implementation for byte primitives.
 *
 * @author Eric D. Friedman
 * @author Rob Eden
 * @author Jeff Randall
 */

public class TByteHashSet extends TByteHash implements TByteSet, Externalizable {
	static final long serialVersionUID = 1L;


    /**
     * Creates a new <code>TByteHashSet</code> instance with the default
     * capacity and load factor.
     */
    public TByteHashSet() {
        super();
    }


    /**
     * Creates a new <code>TByteHashSet</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the default load factor.
     *
     * @param initialCapacity an <code>int</code> value
     */
    public TByteHashSet( int initialCapacity ) {
        super( initialCapacity );
    }


    /**
     * Creates a new <code>TIntHash</code> instance with a prime
     * value at or near the specified capacity and load factor.
     *
     * @param initialCapacity used to find a prime capacity for the table.
     * @param load_factor used to calculate the threshold over which
     * rehashing takes place.
     */
    public TByteHashSet( int initialCapacity, float load_factor ) {
        super( initialCapacity, load_factor );
    }


    /**
     * Creates a new <code>TByteHashSet</code> instance with a prime
     * capacity equal to or greater than <tt>initial_capacity</tt> and
     * with the specified load factor.
     *
     * @param initial_capacity an <code>int</code> value
     * @param load_factor a <code>float</code> value
     * @param no_entry_value a <code>byte</code> value that represents null.
     */
    public TByteHashSet( int initial_capacity, float load_factor,
            byte no_entry_value ) {
        super( initial_capacity, load_factor, no_entry_value );
        //noinspection RedundantCast
        if ( no_entry_value != ( byte ) 0 ) {
            Arrays.fill( _set, no_entry_value );
        }
    }


    /**
      * Creates a new <code>TByteHashSet</code> instance that is a copy
      * of the existing Collection.
      *
      * @param collection a <tt>Collection</tt> that will be duplicated.
      */
     public TByteHashSet( Collection<? extends Byte> collection ) {
        this( Math.max( collection.size(), DEFAULT_CAPACITY ) );
        addAll( collection );
     }


    /**
     * Creates a new <code>TByteHashSet</code> instance that is a copy
     * of the existing set.
     *
     * @param collection a <tt>TByteSet</tt> that will be duplicated.
     */
    public TByteHashSet( TByteCollection collection ) {
        this( Math.max( collection.size(), DEFAULT_CAPACITY ) );
        if ( collection instanceof TByteHashSet ) {
            TByteHashSet hashset = ( TByteHashSet ) collection;
            this._loadFactor = hashset._loadFactor;
            this.no_entry_value = hashset.no_entry_value;
            //noinspection RedundantCast
            if ( this.no_entry_value != ( byte ) 0 ) {
                Arrays.fill( _set, this.no_entry_value );
            }
            setUp( (int) Math.ceil( DEFAULT_CAPACITY / _loadFactor ) );
        }
        addAll( collection );
    }


    /**
     * Creates a new <code>TByteHashSet</code> instance containing the
     * elements of <tt>array</tt>.
     *
     * @param array an array of <code>byte</code> primitives
     */
    public TByteHashSet( byte[] array ) {
        this( Math.max( array.length, DEFAULT_CAPACITY ) );
        addAll( array );
    }


    /** {@inheritDoc} */
    public TByteIterator iterator() {
        return new TByteHashIterator( this );
    }


    /** {@inheritDoc} */
    public byte[] toArray() {
        byte[] result = new byte[ size() ];
        byte[] set = _set;
        byte[] states = _states;

        for ( int i = states.length, j = 0; i-- > 0; ) {
            if ( states[i] == FULL ) {
                result[j++] = set[i];
            }
        }
        return result;
    }


    /** {@inheritDoc} */
    public byte[] toArray( byte[] dest ) {
        byte[] set = _set;
        byte[] states = _states;

        for ( int i = states.length, j = 0; i-- > 0; ) {
            if ( states[i] == FULL ) {
                dest[j++] = set[i];
            }
        }

        if ( dest.length > _size ) {
            dest[_size] = no_entry_value;
        }
        return dest;
    }


    /** {@inheritDoc} */
    public boolean add( byte val ) {
        int index = insertKey(val);

        if ( index < 0 ) {
            return false;       // already present in set, nothing to add
        }

        postInsertHook( consumeFreeSlot );

        return true;            // yes, we added something
    }


    /** {@inheritDoc} */
    public boolean remove( byte val ) {
        int index = index(val);
        if ( index >= 0 ) {
            removeAt( index );
            return true;
        }
        return false;
    }


    /** {@inheritDoc} */
    public boolean containsAll( Collection<?> collection ) {
        for ( Object element : collection ) {
            if ( element instanceof Byte ) {
                byte c = ( ( Byte ) element ).byteValue();
                if ( ! contains( c ) ) {
                    return false;
                }
            } else {
                return false;
            }

        }
        return true;
    }


    /** {@inheritDoc} */
    public boolean containsAll( TByteCollection collection ) {
        TByteIterator iter = collection.iterator();
        while ( iter.hasNext() ) {
            byte element = iter.next();
            if ( ! contains( element ) ) {
                return false;
            }
        }
        return true;
    }


    /** {@inheritDoc} */
    public boolean containsAll( byte[] array ) {
        for ( int i = array.length; i-- > 0; ) {
            if ( ! contains( array[i] ) ) {
                return false;
            }
        }
        return true;
    }


    /** {@inheritDoc} */
    public boolean addAll( Collection<? extends Byte> collection ) {
        boolean changed = false;
        for ( Byte element : collection ) {
            byte e = element.byteValue();
            if ( add( e ) ) {
                changed = true;
            }
        }
        return changed;
    }


    /** {@inheritDoc} */
    public boolean addAll( TByteCollection collection ) {
        boolean changed = false;
        TByteIterator iter = collection.iterator();
        while ( iter.hasNext() ) {
            byte element = iter.next();
            if ( add( element ) ) {
                changed = true;
            }
        }
        return changed;
    }


    /** {@inheritDoc} */
    public boolean addAll( byte[] array ) {
        boolean changed = false;
        for ( int i = array.length; i-- > 0; ) {
            if ( add( array[i] ) ) {
                changed = true;
            }
        }
        return changed;
    }


    /** {@inheritDoc} */
    @SuppressWarnings({"SuspiciousMethodCalls"})
    public boolean retainAll( Collection<?> collection ) {
        boolean modified = false;
	    TByteIterator iter = iterator();
	    while ( iter.hasNext() ) {
	        if ( ! collection.contains( Byte.valueOf ( iter.next() ) ) ) {
		        iter.remove();
		        modified = true;
	        }
	    }
	    return modified;
    }


    /** {@inheritDoc} */
    public boolean retainAll( TByteCollection collection ) {
        if ( this == collection ) {
            return false;
        }
        boolean modified = false;
	    TByteIterator iter = iterator();
	    while ( iter.hasNext() ) {
	        if ( ! collection.contains( iter.next() ) ) {
		        iter.remove();
		        modified = true;
	        }
	    }
	    return modified;
    }


    /** {@inheritDoc} */
    public boolean retainAll( byte[] array ) {
        boolean changed = false;
        Arrays.sort( array );
        byte[] set = _set;
        byte[] states = _states;

        _autoCompactTemporaryDisable = true;
        for ( int i = set.length; i-- > 0; ) {
            if ( states[i] == FULL && ( Arrays.binarySearch( array, set[i] ) < 0) ) {
                removeAt( i );
                changed = true;
            }
        }
        _autoCompactTemporaryDisable = false;

        return changed;
    }


    /** {@inheritDoc} */
    public boolean removeAll( Collection<?> collection ) {
        boolean changed = false;
        for ( Object element : collection ) {
            if ( element instanceof Byte ) {
                byte c = ( ( Byte ) element ).byteValue();
                if ( remove( c ) ) {
                    changed = true;
                }
            }
        }
        return changed;
    }


    /** {@inheritDoc} */
    public boolean removeAll( TByteCollection collection ) {
        boolean changed = false;
        TByteIterator iter = collection.iterator();
        while ( iter.hasNext() ) {
            byte element = iter.next();
            if ( remove( element ) ) {
                changed = true;
            }
        }
        return changed;
    }


    /** {@inheritDoc} */
    public boolean removeAll( byte[] array ) {
        boolean changed = false;
        for ( int i = array.length; i-- > 0; ) {
            if ( remove(array[i]) ) {
                changed = true;
            }
        }
        return changed;
    }


    /** {@inheritDoc} */
    public void clear() {
        super.clear();
        byte[] set = _set;
        byte[] states = _states;

        for ( int i = set.length; i-- > 0; ) {
            set[i] = no_entry_value;
            states[i] = FREE;
        }
    }


    /** {@inheritDoc} */
    protected void rehash( int newCapacity ) {
        int oldCapacity = _set.length;
        
        byte oldSet[] = _set;
        byte oldStates[] = _states;

        _set = new byte[newCapacity];
        _states = new byte[newCapacity];

        for ( int i = oldCapacity; i-- > 0; ) {
            if( oldStates[i] == FULL ) {
                byte o = oldSet[i];
                int index = insertKey(o);
            }
        }
    }


    /** {@inheritDoc} */
    public boolean equals( Object other ) {
        if ( ! ( other instanceof TByteSet ) ) {
            return false;
        }
        TByteSet that = ( TByteSet ) other;
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


    /** {@inheritDoc} */
    public int hashCode() {
        int hashcode = 0;
        for ( int i = _states.length; i-- > 0; ) {
            if ( _states[i] == FULL ) {
                hashcode += HashFunctions.hash( _set[i] );
            }
        }
        return hashcode;
    }


    /** {@inheritDoc} */
    public String toString() {
        StringBuilder buffy = new StringBuilder( _size * 2 + 2 );
        buffy.append("{");
        for ( int i = _states.length, j = 1; i-- > 0; ) {
            if ( _states[i] == FULL ) {
                buffy.append( _set[i] );
                if ( j++ < _size ) {
                    buffy.append( "," );
                }
            }
        }
        buffy.append("}");
        return buffy.toString();
    }


    class TByteHashIterator extends THashPrimitiveIterator implements TByteIterator {

        /** the collection on which the iterator operates */
        private final TByteHash _hash;

        /** {@inheritDoc} */
        public TByteHashIterator( TByteHash hash ) {
            super( hash );
            this._hash = hash;
        }

        /** {@inheritDoc} */
        public byte next() {
            moveToNextIndex();
            return _hash._set[_index];
        }
    }


    /** {@inheritDoc} */
    public void writeExternal( ObjectOutput out ) throws IOException {

    	// VERSION
    	out.writeByte( 1 );

    	// SUPER
    	super.writeExternal( out );

    	// NUMBER OF ENTRIES
    	out.writeInt( _size );

        // LOAD FACTOR -- Added version 1
        out.writeFloat( _loadFactor );

        // NO ENTRY VALUE -- Added version 1
        out.writeByte( no_entry_value );

    	// ENTRIES
        for ( int i = _states.length; i-- > 0; ) {
            if ( _states[i] == FULL ) {
                out.writeByte( _set[i] );
            }
        }
    }


    /** {@inheritDoc} */
    public void readExternal( ObjectInput in )
    	throws IOException, ClassNotFoundException {

    	// VERSION
    	int version = in.readByte();

        // SUPER
    	super.readExternal( in );

    	// NUMBER OF ENTRIES
        int size = in.readInt();

        if ( version >= 1 ) {
            // LOAD FACTOR
            _loadFactor = in.readFloat();

            // NO ENTRY VALUE
            no_entry_value = in.readByte();
            //noinspection RedundantCast
            if ( no_entry_value != ( byte ) 0 ) {
                Arrays.fill( _set, no_entry_value );
            }
        }

    	// ENTRIES
        setUp( size );
        while ( size-- > 0 ) {
            byte val = in.readByte();
            add( val );
        }
    }
} // TIntHashSet
