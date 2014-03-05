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


public class TUnmodifiableObjectDoubleMap<K> implements TObjectDoubleMap<K>, Serializable {
	private static final long serialVersionUID = -1034234728574286014L;

	private final TObjectDoubleMap<K> m;

	public TUnmodifiableObjectDoubleMap( TObjectDoubleMap<K> m ) {
		if ( m == null )
			throw new NullPointerException();
		this.m = m;
	}

	public int size()                       { return m.size(); }
	public boolean isEmpty()                { return m.isEmpty(); }
	public boolean containsKey( Object key ){ return m.containsKey( key ); }
	public boolean containsValue( double val ) { return m.containsValue( val ); }
	public double get( Object key )            { return m.get( key ); }

	public double put( K key, double value ) { throw new UnsupportedOperationException(); }
	public double remove( Object key ) { throw new UnsupportedOperationException(); }
	public void putAll( TObjectDoubleMap<? extends K> m ) { throw new UnsupportedOperationException(); }
	public void putAll( Map<? extends K, ? extends Double> map ) { throw new UnsupportedOperationException(); }
	public void clear() { throw new UnsupportedOperationException(); }

	private transient Set<K> keySet = null;
	private transient TDoubleCollection values = null;

	public Set<K> keySet() {
		if ( keySet == null )
			keySet = Collections.unmodifiableSet( m.keySet() );
		return keySet;
	}
	public Object[] keys() { return m.keys(); }
	public K[] keys( K[] array ) { return m.keys( array ); }

	public TDoubleCollection valueCollection() {
		if ( values == null )
			values = TCollections.unmodifiableCollection( m.valueCollection() );
		return values;
	}
	public double[] values() { return m.values(); }
	public double[] values( double[] array ) { return m.values( array ); }

	public boolean equals(Object o) { return o == this || m.equals(o); }
	public int hashCode()           { return m.hashCode(); }
	public String toString()        { return m.toString(); }
	public double getNoEntryValue()    { return m.getNoEntryValue(); }

	public boolean forEachKey( TObjectProcedure<? super K> procedure ) {
		return m.forEachKey( procedure );
	}
	public boolean forEachValue( TDoubleProcedure procedure ) {
		return m.forEachValue( procedure );
	}
	public boolean forEachEntry( TObjectDoubleProcedure<? super K> procedure ) {
		return m.forEachEntry( procedure );
	}

	public TObjectDoubleIterator<K> iterator() {
		return new TObjectDoubleIterator<K>() {
			TObjectDoubleIterator<K> iter = m.iterator();

			public K key() { return iter.key(); }
			public double value() { return iter.value(); }
			public void advance() { iter.advance(); }
			public boolean hasNext() { return iter.hasNext(); }
			public double setValue( double val ) { throw new UnsupportedOperationException(); }
			public void remove() { throw new UnsupportedOperationException(); }
		};
	}

	public double putIfAbsent( K key, double value ) { throw new UnsupportedOperationException(); }
	public void transformValues( TDoubleFunction function ) { throw new UnsupportedOperationException(); }
	public boolean retainEntries( TObjectDoubleProcedure<? super K> procedure ) { throw new UnsupportedOperationException(); }
	public boolean increment( K key ) { throw new UnsupportedOperationException(); }
	public boolean adjustValue( K key, double amount ) { throw new UnsupportedOperationException(); }
	public double adjustOrPutValue( K key, double adjust_amount, double put_amount ) { throw new UnsupportedOperationException(); }
}
