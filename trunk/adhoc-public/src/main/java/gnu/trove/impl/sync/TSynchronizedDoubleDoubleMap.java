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


public class TSynchronizedDoubleDoubleMap implements TDoubleDoubleMap, Serializable {
	private static final long serialVersionUID = 1978198479659022715L;

	private final TDoubleDoubleMap m;     // Backing Map
	final Object      mutex;	// Object on which to synchronize

	public TSynchronizedDoubleDoubleMap( TDoubleDoubleMap m ) {
		if ( m == null )
			throw new NullPointerException();
		this.m = m;
		mutex = this;
	}

	public TSynchronizedDoubleDoubleMap( TDoubleDoubleMap m, Object mutex ) {
		this.m = m;
		this.mutex = mutex;
	}

	public int size() {
		synchronized( mutex ) { return m.size(); }
	}
	public boolean isEmpty(){
		synchronized( mutex ) { return m.isEmpty(); }
	}
	public boolean containsKey( double key ) {
		synchronized( mutex ) { return m.containsKey( key ); }
	}
	public boolean containsValue( double value ){
		synchronized( mutex ) { return m.containsValue( value ); }
	}
	public double get( double key ) {
		synchronized( mutex ) { return m.get( key ); }
	}

	public double put( double key, double value ) {
		synchronized( mutex ) { return m.put( key, value ); }
	}
	public double remove( double key ) {
		synchronized( mutex ) { return m.remove( key ); }
	}
	public void putAll( Map<? extends Double, ? extends Double> map ) {
		synchronized( mutex ) { m.putAll( map ); }
	}
	public void putAll( TDoubleDoubleMap map ) {
		synchronized( mutex ) { m.putAll( map ); }
	}
	public void clear() {
		synchronized( mutex ) { m.clear(); }
	}

	private transient TDoubleSet keySet = null;
	private transient TDoubleCollection values = null;

	public TDoubleSet keySet() {
		synchronized( mutex ) {
			if ( keySet == null )
				keySet = new TSynchronizedDoubleSet( m.keySet(), mutex );
			return keySet;
		}
	}
	public double[] keys() {
		synchronized( mutex ) { return m.keys(); }
	}
	public double[] keys( double[] array ) {
		synchronized( mutex ) { return m.keys( array ); }
	}

	public TDoubleCollection valueCollection() {
		synchronized( mutex ) {
			if ( values == null )
				values = new TSynchronizedDoubleCollection( m.valueCollection(), mutex );
			return values;
		}
	}
	public double[] values() {
		synchronized( mutex ) { return m.values(); }
	}
	public double[] values( double[] array ) {
		synchronized( mutex ) { return m.values( array ); }
	}

	public TDoubleDoubleIterator iterator() {
		return m.iterator(); // Must be manually synched by user!
	}

	// these are unchanging over the life of the map, no need to lock
	public double getNoEntryKey() { return m.getNoEntryKey(); }
	public double getNoEntryValue() { return m.getNoEntryValue(); }

	public double putIfAbsent( double key, double value ) {
		synchronized( mutex ) { return m.putIfAbsent( key, value ); }
	}
	public boolean forEachKey( TDoubleProcedure procedure ) {
		synchronized( mutex ) { return m.forEachKey( procedure ); }
	}
	public boolean forEachValue( TDoubleProcedure procedure ) {
		synchronized( mutex ) { return m.forEachValue( procedure ); }
	}
	public boolean forEachEntry( TDoubleDoubleProcedure procedure ) {
		synchronized( mutex ) { return m.forEachEntry( procedure ); }
	}
	public void transformValues( TDoubleFunction function ) {
		synchronized( mutex ) { m.transformValues( function ); }
	}
	public boolean retainEntries( TDoubleDoubleProcedure procedure ) {
		synchronized( mutex ) { return m.retainEntries( procedure ); }
	}
	public boolean increment( double key ) {
		synchronized( mutex ) { return m.increment( key ); }
	}
	public boolean adjustValue( double key, double amount ) {
		synchronized( mutex ) { return m.adjustValue( key, amount ); }
	}
	public double adjustOrPutValue( double key, double adjust_amount, double put_amount ) {
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
