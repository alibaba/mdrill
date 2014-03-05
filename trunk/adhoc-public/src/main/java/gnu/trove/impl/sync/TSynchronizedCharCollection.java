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


public class TSynchronizedCharCollection implements TCharCollection, Serializable {
	private static final long serialVersionUID = 3053995032091335093L;

	final TCharCollection c;  // Backing Collection
	final Object mutex;     // Object on which to synchronize

	public TSynchronizedCharCollection( TCharCollection c ) {
		if ( c == null )
				throw new NullPointerException();
		this.c = c;
			mutex = this;
	}
	public TSynchronizedCharCollection( TCharCollection c, Object mutex ) {
		this.c = c;
		this.mutex = mutex;
	}

	public int size() {
		synchronized( mutex ) { return c.size(); }
	}
	public boolean isEmpty() {
		synchronized( mutex ) { return c.isEmpty(); }
	}
	public boolean contains( char o ) {
		synchronized( mutex ) { return c.contains( o ); }
	}
	public char[] toArray() {
		synchronized( mutex ) { return c.toArray(); }
	}
	public char[] toArray( char[] a ) {
		synchronized( mutex ) { return c.toArray( a ); }
	}

	public TCharIterator iterator() {
		return c.iterator(); // Must be manually synched by user!
	}

	public boolean add( char e ) {
		synchronized (mutex ) { return c.add( e ); }
	}
	public boolean remove( char o ) {
		synchronized( mutex ) { return c.remove( o ); }
	}

	public boolean containsAll( Collection<?> coll ) {
		synchronized( mutex ) { return c.containsAll( coll );}
	}
	public boolean containsAll( TCharCollection coll ) {
		synchronized( mutex ) { return c.containsAll( coll );}
	}
	public boolean containsAll( char[] array ) {
		synchronized( mutex ) { return c.containsAll( array );}
	}

	public boolean addAll( Collection<? extends Character> coll ) {
		synchronized( mutex ) { return c.addAll( coll ); }
	}
	public boolean addAll( TCharCollection coll ) {
		synchronized( mutex ) { return c.addAll( coll ); }
	}
	public boolean addAll( char[] array ) {
		synchronized( mutex ) { return c.addAll( array ); }
	}

	public boolean removeAll( Collection<?> coll ) {
		synchronized( mutex ) { return c.removeAll( coll ); }
	}
	public boolean removeAll( TCharCollection coll ) {
		synchronized( mutex ) { return c.removeAll( coll ); }
	}
	public boolean removeAll( char[] array ) {
		synchronized( mutex ) { return c.removeAll( array ); }
	}

	public boolean retainAll( Collection<?> coll ) {
		synchronized( mutex ) { return c.retainAll( coll ); }
	}
	public boolean retainAll( TCharCollection coll ) {
		synchronized( mutex ) { return c.retainAll( coll ); }
	}
	public boolean retainAll( char[] array ) {
		synchronized( mutex ) { return c.retainAll( array ); }
	}

	public char getNoEntryValue() { return c.getNoEntryValue(); }
	public boolean forEach( TCharProcedure procedure ) {
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
