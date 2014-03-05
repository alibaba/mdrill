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


public class TUnmodifiableDoubleCollection implements TDoubleCollection, Serializable {
	private static final long serialVersionUID = 1820017752578914078L;

	final TDoubleCollection c;

	public TUnmodifiableDoubleCollection( TDoubleCollection c ) {
		if ( c == null )
			throw new NullPointerException();
		this.c = c;
	}

	public int size()                   { return c.size(); }
	public boolean isEmpty() 	        { return c.isEmpty(); }
	public boolean contains( double o )    { return c.contains( o ); }
	public double[] toArray()              { return c.toArray(); }
	public double[] toArray( double[] a )     { return c.toArray( a ); }
	public String toString()            { return c.toString(); }
	public double getNoEntryValue()        { return c.getNoEntryValue(); }
	public boolean forEach( TDoubleProcedure procedure ) { return c.forEach( procedure ); }

	public TDoubleIterator iterator() {
		return new TDoubleIterator() {
			TDoubleIterator i = c.iterator();

			public boolean hasNext()    { return i.hasNext(); }
			public double next()           { return i.next(); }
			public void remove()        { throw new UnsupportedOperationException(); }
		};
	}

	public boolean add( double e ) { throw new UnsupportedOperationException(); }
	public boolean remove( double o ) { throw new UnsupportedOperationException(); }

	public boolean containsAll( Collection<?> coll ) { return c.containsAll( coll ); }
	public boolean containsAll( TDoubleCollection coll ) { return c.containsAll( coll ); }
	public boolean containsAll( double[] array ) { return c.containsAll( array ); }

	public boolean addAll( TDoubleCollection coll ) { throw new UnsupportedOperationException(); }
	public boolean addAll( Collection<? extends Double> coll ) { throw new UnsupportedOperationException(); }
	public boolean addAll( double[] array ) { throw new UnsupportedOperationException(); }

	public boolean removeAll( Collection<?> coll ) { throw new UnsupportedOperationException(); }
	public boolean removeAll( TDoubleCollection coll ) { throw new UnsupportedOperationException(); }
	public boolean removeAll( double[] array ) { throw new UnsupportedOperationException(); }

	public boolean retainAll( Collection<?> coll ) { throw new UnsupportedOperationException(); }
	public boolean retainAll( TDoubleCollection coll ) { throw new UnsupportedOperationException(); }
	public boolean retainAll( double[] array ) { throw new UnsupportedOperationException(); }

	public void clear() { throw new UnsupportedOperationException(); }
}
