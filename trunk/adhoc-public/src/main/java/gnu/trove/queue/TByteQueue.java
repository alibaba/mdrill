///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009, Rob Eden All Rights Reserved.
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

package gnu.trove.queue;


import gnu.trove.TByteCollection;

import java.io.Serializable;

/**
 * Interface for Trove queue implementations.
 *
 * @see java.util.Queue
 */
public interface TByteQueue extends TByteCollection {
	/**
	 * Retrieves and removes the head of this queue. This method differs from
	 * {@link #poll} only in that it throws an exception if this queue is empty.
	 */
	public byte element();


	/**
	 * Inserts the specified element into this queue if it is possible to do so
	 * immediately without violating capacity restrictions. When using a
	 * capacity-restricted queue, this method is generally preferable to
	 * {@link #add}, which can fail to insert an element only by throwing an exception.
	 *
	 * @param e		The element to add.
	 *
	 * @return	<tt>true</tt> if the element was added to this queue, else <tt>false</tt>
	 */
	public boolean offer( byte e );


	/**
	 * Retrieves, but does not remove, the head of this queue, or returns
	 * {@link #getNoEntryValue} if this queue is empty.
	 *
	 * @return	the head of this queue, or {@link #getNoEntryValue} if this queue is empty 
	 */
	public byte peek();


	/**
	 * Retrieves and removes the head of this queue, or returns {@link #getNoEntryValue}
	 * if this queue is empty.
	 *
	 * @return	the head of this queue, or {@link #getNoEntryValue} if this queue is empty
	 */
	public byte poll();
}
