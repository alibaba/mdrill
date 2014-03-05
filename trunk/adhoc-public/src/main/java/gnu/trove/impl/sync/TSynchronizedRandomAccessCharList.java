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


public  class TSynchronizedRandomAccessCharList extends TSynchronizedCharList
	implements RandomAccess {

	static final long serialVersionUID = 1530674583602358482L;

	public TSynchronizedRandomAccessCharList( TCharList list ) {
		super( list );
	}

	public TSynchronizedRandomAccessCharList( TCharList list, Object mutex ) {
		super( list, mutex );
	}

	public TCharList subList( int fromIndex, int toIndex ) {
		synchronized( mutex ) {
			return new TSynchronizedRandomAccessCharList(
				list.subList( fromIndex, toIndex ), mutex );
		}
	}

	/**
	 * Allows instances to be deserialized in pre-1.4 JREs (which do
	 * not have SynchronizedRandomAccessList).  SynchronizedList has
	 * a readResolve method that inverts this transformation upon
	 * deserialization.
	 */
	private Object writeReplace() {
		return new TSynchronizedCharList( list );
	}
}
