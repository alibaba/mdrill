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

package gnu.trove.map;


//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////

import gnu.trove.function.*;
import gnu.trove.iterator.*;
import gnu.trove.procedure.*;
import gnu.trove.set.*;
import gnu.trove.TCharCollection;

import java.util.Map;
import java.io.Serializable;


/**
 * Interface for a primitive map of double keys and char values.
 */
public interface TDoubleCharMap {
    /**
     * Returns the value that will be returned from {@link #get} or {@link #put} if no
     * entry exists for a given key. The default value is generally zero, but can be
     * changed during construction of the collection.
     *
     * @return the value that represents a null key in this collection.
     */
    public double getNoEntryKey();


    /**
     * Returns the value that will be returned from {@link #get} or {@link #put} if no
     * entry exists for a given key. The default value is generally zero, but can be
     * changed during construction of the collection.
     *
     * @return the value that represents a null value in this collection.
     */
    public char getNoEntryValue();


    /**
     * Inserts a key/value pair into the map.
     *
     * @param key an <code>double</code> value
     * @param value an <code>char</code> value
     *
     * @return the previous value associated with <tt>key</tt>, or the "no entry" value
     *         if none was found (see {@link #getNoEntryValue}).
     */
    public char put( double key, char value );


    /**
     * Inserts a key/value pair into the map if the specified key is not already
     * associated with a value.
     *
     * @param key an <code>double</code> value
     * @param value an <code>char</code> value
     *
     * @return the previous value associated with <tt>key</tt>, or the "no entry" value
     *         if none was found (see {@link #getNoEntryValue}).
     */
    public char putIfAbsent( double key, char value );


    /**
     * Put all the entries from the given Map into this map.
     *
     * @param map The Map from which entries will be obtained to put into this map.
     */
    public void putAll( Map<? extends Double, ? extends Character> map );


    /**
     * Put all the entries from the given map into this map.
     *
     * @param map   The map from which entries will be obtained to put into this map.
     */
    public void putAll( TDoubleCharMap map );


    /**
     * Retrieves the value for <tt>key</tt>
     *
     * @param key an <code>double</code> value
     *
     * @return the previous value associated with <tt>key</tt>, or the "no entry" value
     *         if none was found (see {@link #getNoEntryValue}).
     */
    public char get( double key );


    /**
     * Empties the map.
     */
    public void clear();


    /**
      * Returns <tt>true</tt> if this map contains no key-value mappings.
      *
      * @return <tt>true</tt> if this map contains no key-value mappings
      */
     public boolean isEmpty();


    /**
     * Deletes a key/value pair from the map.
     *
     * @param key an <code>double</code> value
     *
     * @return the previous value associated with <tt>key</tt>, or the "no entry" value
     *         if none was found (see {@link #getNoEntryValue}).
     */
    public char remove( double key );


    /**
     * Returns an <tt>int</tt> value that is the number of elements in the map.
     *
     * @return an <tt>int</tt> value that is the number of elements in the map.
     */
    public int size();


    /**
     * Returns the keys of the map as a <tt>TDoubleSet</tt>
     *
     * @return the keys of the map as a <tt>TDoubleSet</tt>
     */
    public TDoubleSet keySet();


    /**
     * Returns the keys of the map as an array of <tt>double</tt> values.
     *
     * @return the keys of the map as an array of <tt>double</tt> values.
     */
    public double[] keys();


    /**
     * Returns the keys of the map.
     *
     * @param array   the array into which the elements of the list are to be stored,
     *                if it is big enough; otherwise, a new array of the same type is
     *                allocated for this purpose.
     * @return the keys of the map as an array.
     */
    public double[] keys( double[] array );


    /**
     * Returns the values of the map as a <tt>TCharCollection</tt>
     *
     * @return the values of the map as a <tt>TCharCollection</tt>
     */
    public TCharCollection valueCollection();


    /**
     * Returns the values of the map as an array of <tt>#e#</tt> values.
     *
     * @return the values of the map as an array of <tt>#e#</tt> values.
     */
    public char[] values();


    /**
     * Returns the values of the map using an existing array.
     *
     * @param array   the array into which the elements of the list are to be stored,
     *                if it is big enough; otherwise, a new array of the same type is
     *                allocated for this purpose.
     * @return the values of the map as an array of <tt>#e#</tt> values.
     */
    public char[] values( char[] array );


    /**
     * Checks for the presence of <tt>val</tt> in the values of the map.
     *
     * @param val an <code>char</code> value
     * @return a <code>boolean</code> value
     */
    public boolean containsValue( char val );


    /**
     * Checks for the present of <tt>key</tt> in the keys of the map.
     *
     * @param key an <code>double</code> value
     * @return a <code>boolean</code> value
     */
    public boolean containsKey( double key );


    /**
     * @return a TDoubleCharIterator with access to this map's keys and values
     */
    public TDoubleCharIterator iterator();


    /**
     * Executes <tt>procedure</tt> for each key in the map.
     *
     * @param procedure a <code>TDoubleProcedure</code> value
     * @return false if the loop over the keys terminated because
     *               the procedure returned false for some key.
     */
    public boolean forEachKey( TDoubleProcedure procedure );


    /**
     * Executes <tt>procedure</tt> for each value in the map.
     *
     * @param procedure a <code>T#F#Procedure</code> value
     * @return false if the loop over the values terminated because
     *               the procedure returned false for some value.
     */
    public boolean forEachValue( TCharProcedure procedure );


    /**
     * Executes <tt>procedure</tt> for each key/value entry in the
     * map.
     *
     * @param procedure a <code>TODoubleCharProcedure</code> value
     * @return false if the loop over the entries terminated because
     *               the procedure returned false for some entry.
     */
    public boolean forEachEntry( TDoubleCharProcedure procedure );


    /**
     * Transform the values in this map using <tt>function</tt>.
     *
     * @param function a <code>TCharFunction</code> value
     */
    public void transformValues( TCharFunction function );


    /**
     * Retains only those entries in the map for which the procedure
     * returns a true value.
     *
     * @param procedure determines which entries to keep
     * @return true if the map was modified.
     */
    public boolean retainEntries( TDoubleCharProcedure procedure );


    /**
     * Increments the primitive value mapped to key by 1
     *
     * @param key the key of the value to increment
     * @return true if a mapping was found and modified.
     */
    public boolean increment( double key );


    /**
     * Adjusts the primitive value mapped to key.
     *
     * @param key the key of the value to increment
     * @param amount the amount to adjust the value by.
     * @return true if a mapping was found and modified.
     */
    public boolean adjustValue( double key, char amount );


    /**
     * Adjusts the primitive value mapped to the key if the key is present in the map.
     * Otherwise, the <tt>initial_value</tt> is put in the map.
     *
     * @param key the key of the value to increment
     * @param adjust_amount the amount to adjust the value by
     * @param put_amount the value put into the map if the key is not initial present
     *
     * @return the value present in the map after the adjustment or put operation
     */
    public char adjustOrPutValue( double key, char adjust_amount, char put_amount );
}
