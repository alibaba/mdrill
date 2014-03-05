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


public class TSynchronizedLongCollection implements TLongCollection, Serializable {
	private static final long serialVersionUID = 3053995032091335093L;

	final TLongCollection c;  // Backing Collection
	final Object mutex;     // Object on which to synchronize

	public TSynchronizedLongCollection( TLongCollection c ) {
		if ( c == null )
				throw new NullPointerException();
		this.c = c;
			mutex = this;
	}
	public TSynchronizedLongCollection( TLongCollection c, Object mutex ) {
		this.c = c;
		this.mutex = mutex;
	}

	public int size() {
		synchronized( mutex ) { return c.size(); }
	}
	public boolean isEmpty() {
		synchronized( mutex ) { return c.isEmpty(); }
	}
	public boolean contains( long o ) {
		synchronized( mutex ) { return c.contains( o ); }
	}
	public long[] toArray() {
		synchronized( mutex ) { return c.toArray(); }
	}
	public long[] toArray( long[] a ) {
		synchronized( mutex ) { return c.toArray( a ); }
	}

	public TLongIterator iterator() {
		return c.iterator(); // Must be manually synched by user!
	}

	public boolean add( long e ) {
		synchronized (mutex ) { return c.add( e ); }
	}
	public boolean remove( long o ) {
		synchronized( mutex ) { return c.remove( o ); }
	}

	public boolean containsAll( Collection<?> coll ) {
		synchronized( mutex ) { return c.containsAll( coll );}
	}
	public boolean containsAll( TLongCollection coll ) {
		synchronized( mutex ) { return c.containsAll( coll );}
	}
	public boolean containsAll( long[] array ) {
		synchronized( mutex ) { return c.containsAll( array );}
	}

	public boolean addAll( Collection<? extends Long> coll ) {
		synchronized( mutex ) { return c.addAll( coll ); }
	}
	public boolean addAll( TLongCollection coll ) {
		synchronized( mutex ) { return c.addAll( coll ); }
	}
	public boolean addAll( long[] array ) {
		synchronized( mutex ) { return c.addAll( array ); }
	}

	public boolean removeAll( Collection<?> coll ) {
		synchronized( mutex ) { return c.removeAll( coll ); }
	}
	public boolean removeAll( TLongCollection coll ) {
		synchronized( mutex ) { return c.removeAll( coll ); }
	}
	public boolean removeAll( long[] array ) {
		synchronized( mutex ) { return c.removeAll( array ); }
	}

	public boolean retainAll( Collection<?> coll ) {
		synchronized( mutex ) { return c.retainAll( coll ); }
	}
	public boolean retainAll( TLongCollection coll ) {
		synchronized( mutex ) { return c.retainAll( coll ); }
	}
	public boolean retainAll( long[] array ) {
		synchronized( mutex ) { return c.retainAll( array ); }
	}

	public long getNoEntryValue() { return c.getNoEntryValue(); }
	public boolean forEach( TLongProcedure procedure ) {
		synchronized( mutex ) { return c.forEach( procedure ); }
	}

	public void clear() {
		synchronized( mutex ) { c.clear(); }
	}
	public String toString() {
		synchronized( mutex ) { return c.toString(); }
	}
	private void writeObject( ObjectOutputStream s ) throws IOException {
		synchronized( mutex ) { s.defaultWriteObject(); }
	}
}
