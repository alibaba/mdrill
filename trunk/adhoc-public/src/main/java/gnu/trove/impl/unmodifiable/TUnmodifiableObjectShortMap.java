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

import java.util.*;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.IOException;


public class TUnmodifiableObjectShortMap<K> implements TObjectShortMap<K>, Serializable {
	private static final long serialVersionUID = -1034234728574286014L;

	private final TObjectShortMap<K> m;

	public TUnmodifiableObjectShortMap( TObjectShortMap<K> m ) {
		if ( m == null )
			throw new NullPointerException();
		this.m = m;
	}

	public int size()                       { return m.size(); }
	public boolean isEmpty()                { return m.isEmpty(); }
	public boolean containsKey( Object key ){ return m.containsKey( key ); }
	public boolean containsValue( short val ) { return m.containsValue( val ); }
	public short get( Object key )            { return m.get( key ); }

	public short put( K key, short value ) { throw new UnsupportedOperationException(); }
	public short remove( Object key ) { throw new UnsupportedOperationException(); }
	public void putAll( TObjectShortMap<? extends K> m ) { throw new UnsupportedOperationException(); }
	public void putAll( Map<? extends K, ? extends Short> map ) { throw new UnsupportedOperationException(); }
	public void clear() { throw new UnsupportedOperationException(); }

	private transient Set<K> keySet = null;
	private transient TShortCollection values = null;

	public Set<K> keySet() {
		if ( keySet == null )
			keySet = Collections.unmodifiableSet( m.keySet() );
		return keySet;
	}
	public Object[] keys() { return m.keys(); }
	public K[] keys( K[] array ) { return m.keys( array ); }

	public TShortCollection valueCollection() {
		if ( values == null )
			values = TCollections.unmodifiableCollection( m.valueCollection() );
		return values;
	}
	public short[] values() { return m.values(); }
	public short[] values( short[] array ) { return m.values( array ); }

	public boolean equals(Object o) { return o == this || m.equals(o); }
	public int hashCode()           { return m.hashCode(); }
	public String toString()        { return m.toString(); }
	public short getNoEntryValue()    { return m.getNoEntryValue(); }

	public boolean forEachKey( TObjectProcedure<? super K> procedure ) {
		return m.forEachKey( procedure );
	}
	public boolean forEachValue( TShortProcedure procedure ) {
		return m.forEachValue( procedure );
	}
	public boolean forEachEntry( TObjectShortProcedure<? super K> procedure ) {
		return m.forEachEntry( procedure );
	}

	public TObjectShortIterator<K> iterator() {
		return new TObjectShortIterator<K>() {
			TObjectShortIterator<K> iter = m.iterator();

			public K key() { return iter.key(); }
			public short value() { return iter.value(); }
			public void advance() { iter.advance(); }
			public boolean hasNext() { return iter.hasNext(); }
			public short setValue( short val ) { throw new UnsupportedOperationException(); }
			public void remove() { throw new UnsupportedOperationException(); }
		};
	}

	public short putIfAbsent( K key, short value ) { throw new UnsupportedOperationException(); }
	public void transformValues( TShortFunction function ) { throw new UnsupportedOperationException(); }
	public boolean retainEntries( TObjectShortProcedure<? super K> procedure ) { throw new UnsupportedOperationException(); }
	public boolean increment( K key ) { throw new UnsupportedOperationException(); }
	public boolean adjustValue( K key, short amount ) { throw new UnsupportedOperationException(); }
	public short adjustOrPutValue( K key, short adjust_amount, short put_amount ) { throw new UnsupportedOperationException(); }
}
