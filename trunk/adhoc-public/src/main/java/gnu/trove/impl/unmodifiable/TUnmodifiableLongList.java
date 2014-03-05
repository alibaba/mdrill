///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2008, Robert D. Eden All Rights Reserved.
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

package gnu.trove.impl.unmodifiable;


//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////

////////////////////////////////////////////////////////////
// THIS IS AN IMPLEMENTATION CLASS. DO NOT USE DIRECTLY!  //
// Access to these methods should be through TCollections //
////////////////////////////////////////////////////////////


import gnu.trove.iterator.*;
import gnu.trove.procedure.*;
import gnu.trove.set.*;
import gnu.trove.list.*;
import gnu.trove.function.*;
import gnu.trove.map.*;
import gnu.trove.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Random;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.IOException;


public class TUnmodifiableLongList extends TUnmodifiableLongCollection implements TLongList {
	static final long serialVersionUID = -283967356065247728L;

	final TLongList list;

	public TUnmodifiableLongList( TLongList list ) {
		super( list );
		this.list = list;
	}

	public boolean equals( Object o )   { return o == this || list.equals( o ); }
	public int hashCode()               { return list.hashCode(); }

	public long get( int index )         { return list.get( index ); }
	public int indexOf( long o )         { return list.indexOf( o ); }
	public int lastIndexOf( long o )     { return list.lastIndexOf( o ); }

	public long[] toArray( int offset, int len ) {
		return list.toArray( offset, len );
	}
	public long[] toArray( long[] dest, int offset, int len ) {
		return list.toArray( dest, offset, len );
	}
	public long[] toArray( long[] dest, int source_pos, int dest_pos, int len ) {
		return list.toArray( dest, source_pos, dest_pos, len );
	}

	public boolean forEachDescending( TLongProcedure procedure ) {
		return list.forEachDescending( procedure );
	}

	public int binarySearch( long value ) { return list.binarySearch( value ); }
	public int binarySearch( long value, int fromIndex, int toIndex ) {
		return list.binarySearch( value, fromIndex, toIndex );
	}

	public int indexOf( int offset, long value )     { return list.indexOf( offset, value ); }
	public int lastIndexOf( int offset, long value ) { return list.lastIndexOf( offset, value ); }
	public TLongList grep( TLongProcedure condition ) { return list.grep( condition ); }
	public TLongList inverseGrep( TLongProcedure condition ) { return list.inverseGrep( condition ); }

	public long max()    { return list.max(); }
	public long min()    { return list.min(); }
	public long sum()    { return list.sum(); }

	public TLongList subList( int fromIndex, int toIndex ) {
		return new TUnmodifiableLongList( list.subList( fromIndex, toIndex ) );
	}

	// TODO: Do we want to fullt implement ListIterator?
//        public TIntListIterator listIterator() 	{return listIterator(0);}
//
//        public ListIterator<E> listIterator(final int index) {
//            return new ListIterator<E>() {
//                ListIterator<? extends E> i = list.listIterator(index);
//
//                public boolean hasNext()     {return i.hasNext();}
//                public E next()		     {return i.next();}
//                public boolean hasPrevious() {return i.hasPrevious();}
//                public E previous()	     {return i.previous();}
//                public int nextIndex()       {return i.nextIndex();}
//                public int previousIndex()   {return i.previousIndex();}
//
//                public void remove() {
//                    throw new UnsupportedOperationException();
//                }
//                public void set(E e) {
//                    throw new UnsupportedOperationException();
//                }
//                public void add(E e) {
//                    throw new UnsupportedOperationException();
//                }
//            };
//        }

	/**
	 * UnmodifiableRandomAccessList instances are serialized as
	 * UnmodifiableList instances to allow them to be deserialized
	 * in pre-1.4 JREs (which do not have UnmodifiableRandomAccessList).
	 * This method inverts the transformation.  As a beneficial
	 * side-effect, it also grafts the RandomAccess marker onto
	 * UnmodifiableList instances that were serialized in pre-1.4 JREs.
	 *
	 * Note: Unfortunately, UnmodifiableRandomAccessList instances
	 * serialized in 1.4.1 and deserialized in 1.4 will become
	 * UnmodifiableList instances, as this method was missing in 1.4.
	 */
	private Object readResolve() {
		return ( list instanceof RandomAccess
		? new TUnmodifiableRandomAccessLongList( list )
		: this);
	}

	public void add( long[] vals ) { throw new UnsupportedOperationException(); }
	public void add( long[] vals, int offset, int length ) { throw new UnsupportedOperationException(); }

	public long removeAt( int offset ) { throw new UnsupportedOperationException(); }
	public void remove( int offset, int length ) { throw new UnsupportedOperationException(); }

	public void insert( int offset, long value ) { throw new UnsupportedOperationException(); }
	public void insert( int offset, long[] values ) { throw new UnsupportedOperationException(); }
	public void insert( int offset, long[] values, int valOffset, int len ) { throw new UnsupportedOperationException(); }

	public long set( int offset, long val ) { throw new UnsupportedOperationException(); }
	public void set( int offset, long[] values ) { throw new UnsupportedOperationException(); }
	public void set( int offset, long[] values, int valOffset, int length ) { throw new UnsupportedOperationException(); }

	public long replace( int offset, long val ) { throw new UnsupportedOperationException(); }

	public void transformValues( TLongFunction function ) { throw new UnsupportedOperationException(); }

	public void reverse() { throw new UnsupportedOperationException(); }
	public void reverse( int from, int to ) { throw new UnsupportedOperationException(); }
	public void shuffle( Random rand ) { throw new UnsupportedOperationException(); }

	public void sort() { throw new UnsupportedOperationException(); }
	public void sort( int fromIndex, int toIndex ) { throw new UnsupportedOperationException(); }
	public void fill( long val ) { throw new UnsupportedOperationException(); }
	public void fill( int fromIndex, int toIndex, long val ) { throw new UnsupportedOperationException(); }
}
