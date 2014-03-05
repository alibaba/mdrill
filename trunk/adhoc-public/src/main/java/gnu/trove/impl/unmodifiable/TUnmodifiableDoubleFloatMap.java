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


public class TUnmodifiableDoubleFloatMap implements TDoubleFloatMap, Serializable {
	private static final long serialVersionUID = -1034234728574286014L;

	private final TDoubleFloatMap m;

	public TUnmodifiableDoubleFloatMap( TDoubleFloatMap m ) {
		if ( m == null )
			throw new NullPointerException();
		this.m = m;
	}

	public int size()                       { return m.size(); }
	public boolean isEmpty()                { return m.isEmpty(); }
	public boolean containsKey( double key )   { return m.containsKey( key ); }
	public boolean containsValue( float val ) { return m.containsValue( val ); }
	public float get( double key)                { return m.get( key ); }

	public float put( double key, float value ) { throw new UnsupportedOperationException(); }
	public float remove( double key ) { throw new UnsupportedOperationException(); }
	public void putAll( TDoubleFloatMap m ) { throw new UnsupportedOperationException(); }
	public void putAll( Map<? extends Double, ? extends Float> map ) { throw new UnsupportedOperationException(); }
	public void clear() { throw new UnsupportedOperationException(); }

	private transient TDoubleSet keySet = null;
	private transient TFloatCollection values = null;

	public TDoubleSet keySet() {
		if ( keySet == null )
			keySet = TCollections.unmodifiableSet( m.keySet() );
		return keySet;
	}
	public double[] keys() { return m.keys(); }
	public double[] keys( double[] array ) { return m.keys( array ); }

	public TFloatCollection valueCollection() {
		if ( values == null )
			values = TCollections.unmodifiableCollection( m.valueCollection() );
		return values;
	}
	public float[] values() { return m.values(); }
	public float[] values( float[] array ) { return m.values( array ); }

	public boolean equals(Object o) { return o == this || m.equals(o); }
	public int hashCode()           { return m.hashCode(); }
	public String toString()        { return m.toString(); }
	public double getNoEntryKey()      { return m.getNoEntryKey(); }
	public float getNoEntryValue()    { return m.getNoEntryValue(); }

	public boolean forEachKey( TDoubleProcedure procedure ) {
		return m.forEachKey( procedure );
	}
	public boolean forEachValue( TFloatProcedure procedure ) {
		return m.forEachValue( procedure );
	}
	public boolean forEachEntry( TDoubleFloatProcedure procedure ) {
		return m.forEachEntry( procedure );
	}

	public TDoubleFloatIterator iterator() {
		return new TDoubleFloatIterator() {
			TDoubleFloatIterator iter = m.iterator();

			public double key() { return iter.key(); }
			public float value() { return iter.value(); }
			public void advance() { iter.advance(); }
			public boolean hasNext() { return iter.hasNext(); }
			public float setValue( float val ) { throw new UnsupportedOperationException(); }
			public void remove() { throw new UnsupportedOperationException(); }
		};
	}

	public float putIfAbsent( double key, float value ) { throw new UnsupportedOperationException(); }
	public void transformValues( TFloatFunction function ) { throw new UnsupportedOperationException(); }
	public boolean retainEntries( TDoubleFloatProcedure procedure ) { throw new UnsupportedOperationException(); }
	public boolean increment( double key ) { throw new UnsupportedOperationException(); }
	public boolean adjustValue( double key, float amount ) { throw new UnsupportedOperationException(); }
	public float adjustOrPutValue( double key, float adjust_amount, float put_amount ) { throw new UnsupportedOperationException(); }
}
