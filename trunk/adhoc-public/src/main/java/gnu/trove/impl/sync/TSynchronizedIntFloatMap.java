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


public class TSynchronizedIntFloatMap implements TIntFloatMap, Serializable {
	private static final long serialVersionUID = 1978198479659022715L;

	private final TIntFloatMap m;     // Backing Map
	final Object      mutex;	// Object on which to synchronize

	public TSynchronizedIntFloatMap( TIntFloatMap m ) {
		if ( m == null )
			throw new NullPointerException();
		this.m = m;
		mutex = this;
	}

	public TSynchronizedIntFloatMap( TIntFloatMap m, Object mutex ) {
		this.m = m;
		this.mutex = mutex;
	}

	public int size() {
		synchronized( mutex ) { return m.size(); }
	}
	public boolean isEmpty(){
		synchronized( mutex ) { return m.isEmpty(); }
	}
	public boolean containsKey( int key ) {
		synchronized( mutex ) { return m.containsKey( key ); }
	}
	public boolean containsValue( float value ){
		synchronized( mutex ) { return m.containsValue( value ); }
	}
	public float get( int key ) {
		synchronized( mutex ) { return m.get( key ); }
	}

	public float put( int key, float value ) {
		synchronized( mutex ) { return m.put( key, value ); }
	}
	public float remove( int key ) {
		synchronized( mutex ) { return m.remove( key ); }
	}
	public void putAll( Map<? extends Integer, ? extends Float> map ) {
		synchronized( mutex ) { m.putAll( map ); }
	}
	public void putAll( TIntFloatMap map ) {
		synchronized( mutex ) { m.putAll( map ); }
	}
	public void clear() {
		synchronized( mutex ) { m.clear(); }
	}

	private transient TIntSet keySet = null;
	private transient TFloatCollection values = null;

	public TIntSet keySet() {
		synchronized( mutex ) {
			if ( keySet == null )
				keySet = new TSynchronizedIntSet( m.keySet(), mutex );
			return keySet;
		}
	}
	public int[] keys() {
		synchronized( mutex ) { return m.keys(); }
	}
	public int[] keys( int[] array ) {
		synchronized( mutex ) { return m.keys( array ); }
	}

	public TFloatCollection valueCollection() {
		synchronized( mutex ) {
			if ( values == null )
				values = new TSynchronizedFloatCollection( m.valueCollection(), mutex );
			return values;
		}
	}
	public float[] values() {
		synchronized( mutex ) { return m.values(); }
	}
	public float[] values( float[] array ) {
		synchronized( mutex ) { return m.values( array ); }
	}

	public TIntFloatIterator iterator() {
		return m.iterator(); // Must be manually synched by user!
	}

	// these are unchanging over the life of the map, no need to lock
	public int getNoEntryKey() { return m.getNoEntryKey(); }
	public float getNoEntryValue() { return m.getNoEntryValue(); }

	public float putIfAbsent( int key, float value ) {
		synchronized( mutex ) { return m.putIfAbsent( key, value ); }
	}
	public boolean forEachKey( TIntProcedure procedure ) {
		synchronized( mutex ) { return m.forEachKey( procedure ); }
	}
	public boolean forEachValue( TFloatProcedure procedure ) {
		synchronized( mutex ) { return m.forEachValue( procedure ); }
	}
	public boolean forEachEntry( TIntFloatProcedure procedure ) {
		synchronized( mutex ) { return m.forEachEntry( procedure ); }
	}
	public void transformValues( TFloatFunction function ) {
		synchronized( mutex ) { m.transformValues( function ); }
	}
	public boolean retainEntries( TIntFloatProcedure procedure ) {
		synchronized( mutex ) { return m.retainEntries( procedure ); }
	}
	public boolean increment( int key ) {
		synchronized( mutex ) { return m.increment( key ); }
	}
	public boolean adjustValue( int key, float amount ) {
		synchronized( mutex ) { return m.adjustValue( key, amount ); }
	}
	public float adjustOrPutValue( int key, float adjust_amount, float put_amount ) {
		synchronized( mutex ) { return m.adjustOrPutValue( key, adjust_amount, put_amount ); }
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
