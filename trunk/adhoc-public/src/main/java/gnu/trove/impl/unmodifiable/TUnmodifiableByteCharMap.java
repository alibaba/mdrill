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


public class TUnmodifiableByteCharMap implements TByteCharMap, Serializable {
	private static final long serialVersionUID = -1034234728574286014L;

	private final TByteCharMap m;

	public TUnmodifiableByteCharMap( TByteCharMap m ) {
		if ( m == null )
			throw new NullPointerException();
		this.m = m;
	}

	public int size()                       { return m.size(); }
	public boolean isEmpty()                { return m.isEmpty(); }
	public boolean containsKey( byte key )   { return m.containsKey( key ); }
	public boolean containsValue( char val ) { return m.containsValue( val ); }
	public char get( byte key)                { return m.get( key ); }

	public char put( byte key, char value ) { throw new UnsupportedOperationException(); }
	public char remove( byte key ) { throw new UnsupportedOperationException(); }
	public void putAll( TByteCharMap m ) { throw new UnsupportedOperationException(); }
	public void putAll( Map<? extends Byte, ? extends Character> map ) { throw new UnsupportedOperationException(); }
	public void clear() { throw new UnsupportedOperationException(); }

	private transient TByteSet keySet = null;
	private transient TCharCollection values = null;

	public TByteSet keySet() {
		if ( keySet == null )
			keySet = TCollections.unmodifiableSet( m.keySet() );
		return keySet;
	}
	public byte[] keys() { return m.keys(); }
	public byte[] keys( byte[] array ) { return m.keys( array ); }

	public TCharCollection valueCollection() {
		if ( values == null )
			values = TCollections.unmodifiableCollection( m.valueCollection() );
		return values;
	}
	public char[] values() { return m.values(); }
	public char[] values( char[] array ) { return m.values( array ); }

	public boolean equals(Object o) { return o == this || m.equals(o); }
	public int hashCode()           { return m.hashCode(); }
	public String toString()        { return m.toString(); }
	public byte getNoEntryKey()      { return m.getNoEntryKey(); }
	public char getNoEntryValue()    { return m.getNoEntryValue(); }

	public boolean forEachKey( TByteProcedure procedure ) {
		return m.forEachKey( procedure );
	}
	public boolean forEachValue( TCharProcedure procedure ) {
		return m.forEachValue( procedure );
	}
	public boolean forEachEntry( TByteCharProcedure procedure ) {
		return m.forEachEntry( procedure );
	}

	public TByteCharIterator iterator() {
		return new TByteCharIterator() {
			TByteCharIterator iter = m.iterator();

			public byte key() { return iter.key(); }
			public char value() { return iter.value(); }
			public void advance() { iter.advance(); }
			public boolean hasNext() { return iter.hasNext(); }
			public char setValue( char val ) { throw new UnsupportedOperationException(); }
			public void remove() { throw new UnsupportedOperationException(); }
		};
	}

	public char putIfAbsent( byte key, char value ) { throw new UnsupportedOperationException(); }
	public void transformValues( TCharFunction function ) { throw new UnsupportedOperationException(); }
	public boolean retainEntries( TByteCharProcedure procedure ) { throw new UnsupportedOperationException(); }
	public boolean increment( byte key ) { throw new UnsupportedOperationException(); }
	public boolean adjustValue( byte key, char amount ) { throw new UnsupportedOperationException(); }
	public char adjustOrPutValue( byte key, char adjust_amount, char put_amount ) { throw new UnsupportedOperationException(); }
}
