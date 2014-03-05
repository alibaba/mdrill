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


public class TUnmodifiableLongByteMap implements TLongByteMap, Serializable {
	private static final long serialVersionUID = -1034234728574286014L;

	private final TLongByteMap m;

	public TUnmodifiableLongByteMap( TLongByteMap m ) {
		if ( m == null )
			throw new NullPointerException();
		this.m = m;
	}

	public int size()                       { return m.size(); }
	public boolean isEmpty()                { return m.isEmpty(); }
	public boolean containsKey( long key )   { return m.containsKey( key ); }
	public boolean containsValue( byte val ) { return m.containsValue( val ); }
	public byte get( long key)                { return m.get( key ); }

	public byte put( long key, byte value ) { throw new UnsupportedOperationException(); }
	public byte remove( long key ) { throw new UnsupportedOperationException(); }
	public void putAll( TLongByteMap m ) { throw new UnsupportedOperationException(); }
	public void putAll( Map<? extends Long, ? extends Byte> map ) { throw new UnsupportedOperationException(); }
	public void clear() { throw new UnsupportedOperationException(); }

	private transient TLongSet keySet = null;
	private transient TByteCollection values = null;

	public TLongSet keySet() {
		if ( keySet == null )
			keySet = TCollections.unmodifiableSet( m.keySet() );
		return keySet;
	}
	public long[] keys() { return m.keys(); }
	public long[] keys( long[] array ) { return m.keys( array ); }

	public TByteCollection valueCollection() {
		if ( values == null )
			values = TCollections.unmodifiableCollection( m.valueCollection() );
		return values;
	}
	public byte[] values() { return m.values(); }
	public byte[] values( byte[] array ) { return m.values( array ); }

	public boolean equals(Object o) { return o == this || m.equals(o); }
	public int hashCode()           { return m.hashCode(); }
	public String toString()        { return m.toString(); }
	public long getNoEntryKey()      { return m.getNoEntryKey(); }
	public byte getNoEntryValue()    { return m.getNoEntryValue(); }

	public boolean forEachKey( TLongProcedure procedure ) {
		return m.forEachKey( procedure );
	}
	public boolean forEachValue( TByteProcedure procedure ) {
		return m.forEachValue( procedure );
	}
	public boolean forEachEntry( TLongByteProcedure procedure ) {
		return m.forEachEntry( procedure );
	}

	public TLongByteIterator iterator() {
		return new TLongByteIterator() {
			TLongByteIterator iter = m.iterator();

			public long key() { return iter.key(); }
			public byte value() { return iter.value(); }
			public void advance() { iter.advance(); }
			public boolean hasNext() { return iter.hasNext(); }
			public byte setValue( byte val ) { throw new UnsupportedOperationException(); }
			public void remove() { throw new UnsupportedOperationException(); }
		};
	}

	public byte putIfAbsent( long key, byte value ) { throw new UnsupportedOperationException(); }
	public void transformValues( TByteFunction function ) { throw new UnsupportedOperationException(); }
	public boolean retainEntries( TLongByteProcedure procedure ) { throw new UnsupportedOperationException(); }
	public boolean increment( long key ) { throw new UnsupportedOperationException(); }
	public boolean adjustValue( long key, byte amount ) { throw new UnsupportedOperationException(); }
	public byte adjustOrPutValue( long key, byte adjust_amount, byte put_amount ) { throw new UnsupportedOperationException(); }
}
