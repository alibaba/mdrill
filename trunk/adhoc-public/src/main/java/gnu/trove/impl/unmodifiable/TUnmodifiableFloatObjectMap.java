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


public class TUnmodifiableFloatObjectMap<V> implements TFloatObjectMap<V>, Serializable {
	private static final long serialVersionUID = -1034234728574286014L;

	private final TFloatObjectMap<V> m;

	public TUnmodifiableFloatObjectMap( TFloatObjectMap<V> m ) {
		if ( m == null )
			throw new NullPointerException();
		this.m = m;
	}

	public int size()                       { return m.size(); }
	public boolean isEmpty()                { return m.isEmpty(); }
	public boolean containsKey( float key )   { return m.containsKey( key ); }
	public boolean containsValue( Object val ) { return m.containsValue( val ); }
	public V get( float key)                  { return m.get( key ); }

	public V put( float key, V value ) { throw new UnsupportedOperationException(); }
	public V remove( float key ) { throw new UnsupportedOperationException(); }
	public void putAll( TFloatObjectMap<? extends V> m ) { throw new UnsupportedOperationException(); }
	public void putAll( Map<? extends Float, ? extends V> map ) { throw new UnsupportedOperationException(); }
	public void clear() { throw new UnsupportedOperationException(); }

	private transient TFloatSet keySet = null;
	private transient Collection<V> values = null;

	public TFloatSet keySet() {
		if ( keySet == null )
			keySet = TCollections.unmodifiableSet( m.keySet() );
		return keySet;
	}
	public float[] keys() { return m.keys(); }
	public float[] keys( float[] array ) { return m.keys( array ); }

	public Collection<V> valueCollection() {
		if ( values == null )
			values = Collections.unmodifiableCollection( m.valueCollection() );
		return values;
	}
	public Object[] values() { return m.values(); }
	public V[] values( V[] array ) { return m.values( array ); }

	public boolean equals(Object o) { return o == this || m.equals(o); }
	public int hashCode()           { return m.hashCode(); }
	public String toString()        { return m.toString(); }
	public float getNoEntryKey()      { return m.getNoEntryKey(); }

	public boolean forEachKey( TFloatProcedure procedure ) {
		return m.forEachKey( procedure );
	}
	public boolean forEachValue( TObjectProcedure<? super V> procedure ) {
		return m.forEachValue( procedure );
	}
	public boolean forEachEntry( TFloatObjectProcedure<? super V> procedure ) {
		return m.forEachEntry( procedure );
	}

	public TFloatObjectIterator<V> iterator() {
		return new TFloatObjectIterator<V>() {
			TFloatObjectIterator<V> iter = m.iterator();

			public float key() { return iter.key(); }
			public V value() { return iter.value(); }
			public void advance() { iter.advance(); }
			public boolean hasNext() { return iter.hasNext(); }
			public V setValue( V val ) { throw new UnsupportedOperationException(); }
			public void remove() { throw new UnsupportedOperationException(); }
		};
	}

	public V putIfAbsent( float key, V value ) { throw new UnsupportedOperationException(); }
	public void transformValues( TObjectFunction<V,V> function ) { throw new UnsupportedOperationException(); }
	public boolean retainEntries( TFloatObjectProcedure<? super V> procedure ) { throw new UnsupportedOperationException(); }
}
