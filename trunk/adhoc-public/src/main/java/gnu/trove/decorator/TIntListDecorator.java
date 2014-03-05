///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009, Robert D. Eden All Rights Reserved.
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

package gnu.trove.decorator;

import gnu.trove.list.TIntList;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractList;
import java.util.List;


//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * Wrapper class to make a TIntList conform to the <tt>java.util.List</tt> API.
 * This class simply decorates an underlying TIntList and translates the Object-based
 * APIs into their Trove primitive analogs.
 * <p/>
 * Note that wrapping and unwrapping primitive values is extremely inefficient.  If
 * possible, users of this class should override the appropriate methods in this class
 * and use a table of canonical values.
 * <p/>
 *
 * @author Robert D. Eden
 */
public class TIntListDecorator extends AbstractList<Integer>
	implements List<Integer>, Externalizable, Cloneable {

	static final long serialVersionUID = 1L;

    /** the wrapped primitive list */
    protected TIntList list;


    /**
     * FOR EXTERNALIZATION ONLY!!
     */
    public TIntListDecorator() {}


    /**
     * Creates a wrapper that decorates the specified primitive map.
     *
     * @param list the <tt>TIntList</tt> to wrap.
     */
    public TIntListDecorator( TIntList list ) {
        super();
        this.list = list;
    }


    /**
     * Returns a reference to the list wrapped by this decorator.
     *
     * @return the wrapped <tt>TIntList</tt> instance.
     */
    public TIntList getList() {
        return list;
    }


	@Override
	public int size() {
		return list.size();
	}


	@Override
	public Integer get( int index ) {
		int value = list.get( index );
		if ( value == list.getNoEntryValue() ) return null;
		else return Integer.valueOf( value );
	}


	@Override
	public Integer set( int index, Integer value ) {
		int previous_value = list.set( index, value );
		if ( previous_value == list.getNoEntryValue() ) return null;
		else return Integer.valueOf( previous_value );
	}


	@Override
	public void add( int index, Integer value ) {
		list.insert( index, value.intValue() );
	}


	@Override
	public Integer remove( int index ) {
		int previous_value = list.removeAt( index );
		if ( previous_value == list.getNoEntryValue() ) return null;
		else return Integer.valueOf( previous_value );
	}


	// Implements Externalizable
	public void readExternal( ObjectInput in )
        throws IOException, ClassNotFoundException {

        // VERSION
        in.readByte();

        // LIST
        list = ( TIntList ) in.readObject();
    }


    // Implements Externalizable
    public void writeExternal( ObjectOutput out ) throws IOException {
        // VERSION
        out.writeByte(0);

        // LIST
        out.writeObject( list );
    }

}
