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

package gnu.trove.impl.sync;


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


public class TSynchronizedShortObjectMap<V>
	implements TShortObjectMap<V>, Serializable {
	// use serialVersionUID from JDK 1.2.2 for interoperability
	private static final long serialVersionUID = 1978198479659022715L;

	private final TShortObjectMap<V> m;     // Backing Map
	final Object      mutex;	// Object on which to synchronize

	public TSynchronizedShortObjectMap( TShortObjectMap<V> m ) {
		if ( m == null )
			throw new NullPointerException();
		this.m = m;
		mutex = this;
	}

	public TSynchronizedShortObjectMap( TShortObjectMap<V> m, Object mutex ) {
		this.m = m;
		this.mutex = mutex;
	}

	public int size() {
		synchronized( mutex ) { return m.size(); }
	}
	public boolean isEmpty(){
		synchronized( mutex ) { return m.isEmpty(); }
	}
	public boolean containsKey( short key ) {
		synchronized( mutex ) { return m.containsKey( key ); }
	}
	public boolean containsValue( Object value ){
		synchronized( mutex ) { return m.containsValue( value ); }
	}
	public V get( short key ) {
		synchronized( mutex ) { return m.get( key ); }
	}

	public V put( short key, V value ) {
		synchronized( mutex ) { return m.put( key, value ); }
	}
	public V remove( short key ) {
		synchronized( mutex ) { return m.remove( key ); }
	}
	public void putAll( Map<? extends Short, ? extends V> map ) {
		synchronized( mutex ) { m.putAll( map ); }
	}
	public void putAll( TShortObjectMap<? extends V> map ) {
		synchronized( mutex ) { m.putAll( map ); }
	}
	public void clear() {
		synchronized( mutex ) { m.clear(); }
	}

	private transient TShortSet keySet = null;
	private transient Collection<V> values = null;

	public TShortSet keySet() {
		synchronized( mutex ) {
			if ( keySet == null )
				keySet = new TSynchronizedShortSet( m.keySet(), mutex );
			return keySet;
		}
	}
	public short[] keys() {
		synchronized( mutex ) { return m.keys(); }
	}
	public short[] keys( short[] array ) {
		synchronized( mutex ) { return m.keys( array ); }
	}

	public Collection<V> valueCollection() {
		synchronized( mutex ) {
			if ( values == null ) {
				values = new SynchronizedCollection<V>( m.valueCollection(), mutex );
			}
			return values;
		}
	}
	public Object[] values() {
		synchronized( mutex ) { return m.values(); }
	}
	public V[] values( V[] array ) {
		synchronized( mutex ) { return m.values( array ); }
	}

	public TShortObjectIterator<V> iterator() {
		return m.iterator(); // Must be manually synched by user!
	}

	// unchanging over the life of the map, no need to lock
	public short getNoEntryKey() { return m.getNoEntryKey(); }

	public V putIfAbsent( short key, V value ) {
		synchronized( mutex ) { return m.putIfAbsent( key, value ); }
	}
	public boolean forEachKey( TShortProcedure procedure ) {
		synchronized( mutex ) { return m.forEachKey( procedure ); }
	}
	public boolean forEachValue( TObjectProcedure<? super V> procedure ) {
		synchronized( mutex ) { return m.forEachValue( procedure ); }
	}
	public boolean forEachEntry( TShortObjectProcedure<? super V> procedure ) {
		synchronized( mutex ) { return m.forEachEntry( procedure ); }
	}
	public void transformValues( TObjectFunction<V,V> function ) {
		synchronized( mutex ) { m.transformValues( function ); }
	}
	public boolean retainEntries( TShortObjectProcedure<? super V> procedure ) {
		synchronized( mutex ) { return m.retainEntries( procedure ); }
	}

	public boolean equals( Object o ) {
		synchronized( mutex ) { return m.equals( o ); }
	}
	public int hashCode() {
		synchronized( mutex ) { return m.hashCode(); }
	}
	public String toString() {
		synchronized( mutex ) { return m.toString(); }
	}
	private void writeObject( ObjectOutputStream s ) throws IOException {
		synchronized( mutex ) { s.defaultWriteObject(); }
	}
}
