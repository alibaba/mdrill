///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
// Copyright (c) 2009, Rob Eden All Rights Reserved.
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


package gnu.trove.stack;

//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////

import java.io.Serializable;


/**
 * A stack of long primitives.
 */
public interface TLongStack {

    /**
     * Returns the value that is used to represent null. The default
     * value is generally zero, but can be changed during construction
     * of the collection.
     *
     * @return the value that represents null
     */
    public long getNoEntryValue();


    /**
     * Pushes the value onto the top of the stack.
     *
     * @param val an <code>long</code> value
     */
    public void push( long val );


    /**
     * Removes and returns the value at the top of the stack.
     *
     * @return an <code>long</code> value
     */
    public long pop();


    /**
     * Returns the value at the top of the stack.
     *
     * @return an <code>long</code> value
     */
    public long peek();


    /**
     * Returns the current depth of the stack.
     */
    public int size();


    /**
     * Clears the stack.
     */
    public void clear();


    /**
     * Copies the contents of the stack into a native array. Note that this will NOT
     * pop them out of the stack.
     *
     * @return an <code>long[]</code> value
     */
    public long[] toArray();


    /**
     * Copies a slice of the list into a native array. Note that this will NOT
     * pop them out of the stack.
     *
     * @param dest the array to copy into.
     */
    public void toArray( long[] dest );
}
