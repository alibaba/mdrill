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

package gnu.trove;


//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


import gnu.trove.set.*;
import gnu.trove.list.*;
import gnu.trove.map.*;
import gnu.trove.impl.unmodifiable.*;
import gnu.trove.impl.sync.*;

import java.util.RandomAccess;


/**
 * Trove equivalent of the {@link java.util.Collections} class.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class TCollections {

    // Disallow creation of instances of this class
    private TCollections() { }


///////////////////////////
// TUnmodifiableCollections

    /**
     * Returns an unmodifiable view of the specified Trove primitive collection.  This method
     * allows modules to provide users with "read-only" access to internal
     * collections.  Query operations on the returned collection "read through"
     * to the specified collection, and attempts to modify the returned
     * collection, whether direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned collection does <i>not</i> pass the hashCode and equals
     * operations through to the backing collection, but relies on
     * <tt>Object</tt>'s <tt>equals</tt> and <tt>hashCode</tt> methods.  This
     * is necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  c the collection for which an unmodifiable view is to be
     *	       returned.
     * @return an unmodifiable view of the specified Trove primitive collection.
     */
    public static TDoubleCollection unmodifiableCollection( TDoubleCollection c ) {
	    return new TUnmodifiableDoubleCollection( c );
    }


    /**
     * Returns an unmodifiable view of the specified Trove primitive collection.  This method
     * allows modules to provide users with "read-only" access to internal
     * collections.  Query operations on the returned collection "read through"
     * to the specified collection, and attempts to modify the returned
     * collection, whether direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned collection does <i>not</i> pass the hashCode and equals
     * operations through to the backing collection, but relies on
     * <tt>Object</tt>'s <tt>equals</tt> and <tt>hashCode</tt> methods.  This
     * is necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  c the collection for which an unmodifiable view is to be
     *	       returned.
     * @return an unmodifiable view of the specified Trove primitive collection.
     */
    public static TFloatCollection unmodifiableCollection( TFloatCollection c ) {
	    return new TUnmodifiableFloatCollection( c );
    }


    /**
     * Returns an unmodifiable view of the specified Trove primitive collection.  This method
     * allows modules to provide users with "read-only" access to internal
     * collections.  Query operations on the returned collection "read through"
     * to the specified collection, and attempts to modify the returned
     * collection, whether direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned collection does <i>not</i> pass the hashCode and equals
     * operations through to the backing collection, but relies on
     * <tt>Object</tt>'s <tt>equals</tt> and <tt>hashCode</tt> methods.  This
     * is necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  c the collection for which an unmodifiable view is to be
     *	       returned.
     * @return an unmodifiable view of the specified Trove primitive collection.
     */
    public static TIntCollection unmodifiableCollection( TIntCollection c ) {
	    return new TUnmodifiableIntCollection( c );
    }


    /**
     * Returns an unmodifiable view of the specified Trove primitive collection.  This method
     * allows modules to provide users with "read-only" access to internal
     * collections.  Query operations on the returned collection "read through"
     * to the specified collection, and attempts to modify the returned
     * collection, whether direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned collection does <i>not</i> pass the hashCode and equals
     * operations through to the backing collection, but relies on
     * <tt>Object</tt>'s <tt>equals</tt> and <tt>hashCode</tt> methods.  This
     * is necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  c the collection for which an unmodifiable view is to be
     *	       returned.
     * @return an unmodifiable view of the specified Trove primitive collection.
     */
    public static TLongCollection unmodifiableCollection( TLongCollection c ) {
	    return new TUnmodifiableLongCollection( c );
    }


    /**
     * Returns an unmodifiable view of the specified Trove primitive collection.  This method
     * allows modules to provide users with "read-only" access to internal
     * collections.  Query operations on the returned collection "read through"
     * to the specified collection, and attempts to modify the returned
     * collection, whether direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned collection does <i>not</i> pass the hashCode and equals
     * operations through to the backing collection, but relies on
     * <tt>Object</tt>'s <tt>equals</tt> and <tt>hashCode</tt> methods.  This
     * is necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  c the collection for which an unmodifiable view is to be
     *	       returned.
     * @return an unmodifiable view of the specified Trove primitive collection.
     */
    public static TByteCollection unmodifiableCollection( TByteCollection c ) {
	    return new TUnmodifiableByteCollection( c );
    }


    /**
     * Returns an unmodifiable view of the specified Trove primitive collection.  This method
     * allows modules to provide users with "read-only" access to internal
     * collections.  Query operations on the returned collection "read through"
     * to the specified collection, and attempts to modify the returned
     * collection, whether direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned collection does <i>not</i> pass the hashCode and equals
     * operations through to the backing collection, but relies on
     * <tt>Object</tt>'s <tt>equals</tt> and <tt>hashCode</tt> methods.  This
     * is necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  c the collection for which an unmodifiable view is to be
     *	       returned.
     * @return an unmodifiable view of the specified Trove primitive collection.
     */
    public static TShortCollection unmodifiableCollection( TShortCollection c ) {
	    return new TUnmodifiableShortCollection( c );
    }


    /**
     * Returns an unmodifiable view of the specified Trove primitive collection.  This method
     * allows modules to provide users with "read-only" access to internal
     * collections.  Query operations on the returned collection "read through"
     * to the specified collection, and attempts to modify the returned
     * collection, whether direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned collection does <i>not</i> pass the hashCode and equals
     * operations through to the backing collection, but relies on
     * <tt>Object</tt>'s <tt>equals</tt> and <tt>hashCode</tt> methods.  This
     * is necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  c the collection for which an unmodifiable view is to be
     *	       returned.
     * @return an unmodifiable view of the specified Trove primitive collection.
     */
    public static TCharCollection unmodifiableCollection( TCharCollection c ) {
	    return new TUnmodifiableCharCollection( c );
    }



    /**
     * Returns an unmodifiable view of the specified Trove primitive set.  This method allows
     * modules to provide users with "read-only" access to internal sets.
     * Query operations on the returned set "read through" to the specified
     * set, and attempts to modify the returned set, whether direct or via its
     * iterator, result in an <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned set will be serializable if the specified set
     * is serializable.
     *
     * @param  s the set for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive set.
     */
    public static TDoubleSet unmodifiableSet( TDoubleSet s ) {
	    return new TUnmodifiableDoubleSet( s );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive set.  This method allows
     * modules to provide users with "read-only" access to internal sets.
     * Query operations on the returned set "read through" to the specified
     * set, and attempts to modify the returned set, whether direct or via its
     * iterator, result in an <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned set will be serializable if the specified set
     * is serializable.
     *
     * @param  s the set for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive set.
     */
    public static TFloatSet unmodifiableSet( TFloatSet s ) {
	    return new TUnmodifiableFloatSet( s );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive set.  This method allows
     * modules to provide users with "read-only" access to internal sets.
     * Query operations on the returned set "read through" to the specified
     * set, and attempts to modify the returned set, whether direct or via its
     * iterator, result in an <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned set will be serializable if the specified set
     * is serializable.
     *
     * @param  s the set for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive set.
     */
    public static TIntSet unmodifiableSet( TIntSet s ) {
	    return new TUnmodifiableIntSet( s );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive set.  This method allows
     * modules to provide users with "read-only" access to internal sets.
     * Query operations on the returned set "read through" to the specified
     * set, and attempts to modify the returned set, whether direct or via its
     * iterator, result in an <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned set will be serializable if the specified set
     * is serializable.
     *
     * @param  s the set for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive set.
     */
    public static TLongSet unmodifiableSet( TLongSet s ) {
	    return new TUnmodifiableLongSet( s );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive set.  This method allows
     * modules to provide users with "read-only" access to internal sets.
     * Query operations on the returned set "read through" to the specified
     * set, and attempts to modify the returned set, whether direct or via its
     * iterator, result in an <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned set will be serializable if the specified set
     * is serializable.
     *
     * @param  s the set for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive set.
     */
    public static TByteSet unmodifiableSet( TByteSet s ) {
	    return new TUnmodifiableByteSet( s );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive set.  This method allows
     * modules to provide users with "read-only" access to internal sets.
     * Query operations on the returned set "read through" to the specified
     * set, and attempts to modify the returned set, whether direct or via its
     * iterator, result in an <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned set will be serializable if the specified set
     * is serializable.
     *
     * @param  s the set for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive set.
     */
    public static TShortSet unmodifiableSet( TShortSet s ) {
	    return new TUnmodifiableShortSet( s );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive set.  This method allows
     * modules to provide users with "read-only" access to internal sets.
     * Query operations on the returned set "read through" to the specified
     * set, and attempts to modify the returned set, whether direct or via its
     * iterator, result in an <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned set will be serializable if the specified set
     * is serializable.
     *
     * @param  s the set for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive set.
     */
    public static TCharSet unmodifiableSet( TCharSet s ) {
	    return new TUnmodifiableCharSet( s );
    }


    /**
     * Returns an unmodifiable view of the specified Trove primitive list.  This method allows
     * modules to provide users with "read-only" access to internal
     * lists.  Query operations on the returned list "read through" to the
     * specified list, and attempts to modify the returned list, whether
     * direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned list will be serializable if the specified list
     * is serializable. Similarly, the returned list will implement
     * {@link RandomAccess} if the specified list does.
     *
     * @param  list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive list.
     */
    public static TDoubleList unmodifiableList( TDoubleList list) {
	return ( list instanceof RandomAccess ?
                new TUnmodifiableRandomAccessDoubleList( list ) :
                new TUnmodifiableDoubleList( list ) );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive list.  This method allows
     * modules to provide users with "read-only" access to internal
     * lists.  Query operations on the returned list "read through" to the
     * specified list, and attempts to modify the returned list, whether
     * direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned list will be serializable if the specified list
     * is serializable. Similarly, the returned list will implement
     * {@link RandomAccess} if the specified list does.
     *
     * @param  list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive list.
     */
    public static TFloatList unmodifiableList( TFloatList list) {
	return ( list instanceof RandomAccess ?
                new TUnmodifiableRandomAccessFloatList( list ) :
                new TUnmodifiableFloatList( list ) );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive list.  This method allows
     * modules to provide users with "read-only" access to internal
     * lists.  Query operations on the returned list "read through" to the
     * specified list, and attempts to modify the returned list, whether
     * direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned list will be serializable if the specified list
     * is serializable. Similarly, the returned list will implement
     * {@link RandomAccess} if the specified list does.
     *
     * @param  list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive list.
     */
    public static TIntList unmodifiableList( TIntList list) {
	return ( list instanceof RandomAccess ?
                new TUnmodifiableRandomAccessIntList( list ) :
                new TUnmodifiableIntList( list ) );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive list.  This method allows
     * modules to provide users with "read-only" access to internal
     * lists.  Query operations on the returned list "read through" to the
     * specified list, and attempts to modify the returned list, whether
     * direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned list will be serializable if the specified list
     * is serializable. Similarly, the returned list will implement
     * {@link RandomAccess} if the specified list does.
     *
     * @param  list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive list.
     */
    public static TLongList unmodifiableList( TLongList list) {
	return ( list instanceof RandomAccess ?
                new TUnmodifiableRandomAccessLongList( list ) :
                new TUnmodifiableLongList( list ) );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive list.  This method allows
     * modules to provide users with "read-only" access to internal
     * lists.  Query operations on the returned list "read through" to the
     * specified list, and attempts to modify the returned list, whether
     * direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned list will be serializable if the specified list
     * is serializable. Similarly, the returned list will implement
     * {@link RandomAccess} if the specified list does.
     *
     * @param  list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive list.
     */
    public static TByteList unmodifiableList( TByteList list) {
	return ( list instanceof RandomAccess ?
                new TUnmodifiableRandomAccessByteList( list ) :
                new TUnmodifiableByteList( list ) );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive list.  This method allows
     * modules to provide users with "read-only" access to internal
     * lists.  Query operations on the returned list "read through" to the
     * specified list, and attempts to modify the returned list, whether
     * direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned list will be serializable if the specified list
     * is serializable. Similarly, the returned list will implement
     * {@link RandomAccess} if the specified list does.
     *
     * @param  list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive list.
     */
    public static TShortList unmodifiableList( TShortList list) {
	return ( list instanceof RandomAccess ?
                new TUnmodifiableRandomAccessShortList( list ) :
                new TUnmodifiableShortList( list ) );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive list.  This method allows
     * modules to provide users with "read-only" access to internal
     * lists.  Query operations on the returned list "read through" to the
     * specified list, and attempts to modify the returned list, whether
     * direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned list will be serializable if the specified list
     * is serializable. Similarly, the returned list will implement
     * {@link RandomAccess} if the specified list does.
     *
     * @param  list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive list.
     */
    public static TCharList unmodifiableList( TCharList list) {
	return ( list instanceof RandomAccess ?
                new TUnmodifiableRandomAccessCharList( list ) :
                new TUnmodifiableCharList( list ) );
    }


    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TDoubleDoubleMap unmodifiableMap( TDoubleDoubleMap m ) {
	    return new TUnmodifiableDoubleDoubleMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TDoubleFloatMap unmodifiableMap( TDoubleFloatMap m ) {
	    return new TUnmodifiableDoubleFloatMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TDoubleIntMap unmodifiableMap( TDoubleIntMap m ) {
	    return new TUnmodifiableDoubleIntMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TDoubleLongMap unmodifiableMap( TDoubleLongMap m ) {
	    return new TUnmodifiableDoubleLongMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TDoubleByteMap unmodifiableMap( TDoubleByteMap m ) {
	    return new TUnmodifiableDoubleByteMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TDoubleShortMap unmodifiableMap( TDoubleShortMap m ) {
	    return new TUnmodifiableDoubleShortMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TDoubleCharMap unmodifiableMap( TDoubleCharMap m ) {
	    return new TUnmodifiableDoubleCharMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TFloatDoubleMap unmodifiableMap( TFloatDoubleMap m ) {
	    return new TUnmodifiableFloatDoubleMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TFloatFloatMap unmodifiableMap( TFloatFloatMap m ) {
	    return new TUnmodifiableFloatFloatMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TFloatIntMap unmodifiableMap( TFloatIntMap m ) {
	    return new TUnmodifiableFloatIntMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TFloatLongMap unmodifiableMap( TFloatLongMap m ) {
	    return new TUnmodifiableFloatLongMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TFloatByteMap unmodifiableMap( TFloatByteMap m ) {
	    return new TUnmodifiableFloatByteMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TFloatShortMap unmodifiableMap( TFloatShortMap m ) {
	    return new TUnmodifiableFloatShortMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TFloatCharMap unmodifiableMap( TFloatCharMap m ) {
	    return new TUnmodifiableFloatCharMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TIntDoubleMap unmodifiableMap( TIntDoubleMap m ) {
	    return new TUnmodifiableIntDoubleMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TIntFloatMap unmodifiableMap( TIntFloatMap m ) {
	    return new TUnmodifiableIntFloatMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TIntIntMap unmodifiableMap( TIntIntMap m ) {
	    return new TUnmodifiableIntIntMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TIntLongMap unmodifiableMap( TIntLongMap m ) {
	    return new TUnmodifiableIntLongMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TIntByteMap unmodifiableMap( TIntByteMap m ) {
	    return new TUnmodifiableIntByteMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TIntShortMap unmodifiableMap( TIntShortMap m ) {
	    return new TUnmodifiableIntShortMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TIntCharMap unmodifiableMap( TIntCharMap m ) {
	    return new TUnmodifiableIntCharMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TLongDoubleMap unmodifiableMap( TLongDoubleMap m ) {
	    return new TUnmodifiableLongDoubleMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TLongFloatMap unmodifiableMap( TLongFloatMap m ) {
	    return new TUnmodifiableLongFloatMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TLongIntMap unmodifiableMap( TLongIntMap m ) {
	    return new TUnmodifiableLongIntMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TLongLongMap unmodifiableMap( TLongLongMap m ) {
	    return new TUnmodifiableLongLongMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TLongByteMap unmodifiableMap( TLongByteMap m ) {
	    return new TUnmodifiableLongByteMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TLongShortMap unmodifiableMap( TLongShortMap m ) {
	    return new TUnmodifiableLongShortMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TLongCharMap unmodifiableMap( TLongCharMap m ) {
	    return new TUnmodifiableLongCharMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TByteDoubleMap unmodifiableMap( TByteDoubleMap m ) {
	    return new TUnmodifiableByteDoubleMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TByteFloatMap unmodifiableMap( TByteFloatMap m ) {
	    return new TUnmodifiableByteFloatMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TByteIntMap unmodifiableMap( TByteIntMap m ) {
	    return new TUnmodifiableByteIntMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TByteLongMap unmodifiableMap( TByteLongMap m ) {
	    return new TUnmodifiableByteLongMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TByteByteMap unmodifiableMap( TByteByteMap m ) {
	    return new TUnmodifiableByteByteMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TByteShortMap unmodifiableMap( TByteShortMap m ) {
	    return new TUnmodifiableByteShortMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TByteCharMap unmodifiableMap( TByteCharMap m ) {
	    return new TUnmodifiableByteCharMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TShortDoubleMap unmodifiableMap( TShortDoubleMap m ) {
	    return new TUnmodifiableShortDoubleMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TShortFloatMap unmodifiableMap( TShortFloatMap m ) {
	    return new TUnmodifiableShortFloatMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TShortIntMap unmodifiableMap( TShortIntMap m ) {
	    return new TUnmodifiableShortIntMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TShortLongMap unmodifiableMap( TShortLongMap m ) {
	    return new TUnmodifiableShortLongMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TShortByteMap unmodifiableMap( TShortByteMap m ) {
	    return new TUnmodifiableShortByteMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TShortShortMap unmodifiableMap( TShortShortMap m ) {
	    return new TUnmodifiableShortShortMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TShortCharMap unmodifiableMap( TShortCharMap m ) {
	    return new TUnmodifiableShortCharMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TCharDoubleMap unmodifiableMap( TCharDoubleMap m ) {
	    return new TUnmodifiableCharDoubleMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TCharFloatMap unmodifiableMap( TCharFloatMap m ) {
	    return new TUnmodifiableCharFloatMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TCharIntMap unmodifiableMap( TCharIntMap m ) {
	    return new TUnmodifiableCharIntMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TCharLongMap unmodifiableMap( TCharLongMap m ) {
	    return new TUnmodifiableCharLongMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TCharByteMap unmodifiableMap( TCharByteMap m ) {
	    return new TUnmodifiableCharByteMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TCharShortMap unmodifiableMap( TCharShortMap m ) {
	    return new TUnmodifiableCharShortMap( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static TCharCharMap unmodifiableMap( TCharCharMap m ) {
	    return new TUnmodifiableCharCharMap( m );
    }


    /**
     * Returns an unmodifiable view of the specified Trove primitive/Object map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static <V> TDoubleObjectMap<V> unmodifiableMap( TDoubleObjectMap<V> m ) {
	    return new TUnmodifiableDoubleObjectMap<V>( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/Object map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static <V> TFloatObjectMap<V> unmodifiableMap( TFloatObjectMap<V> m ) {
	    return new TUnmodifiableFloatObjectMap<V>( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/Object map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static <V> TIntObjectMap<V> unmodifiableMap( TIntObjectMap<V> m ) {
	    return new TUnmodifiableIntObjectMap<V>( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/Object map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static <V> TLongObjectMap<V> unmodifiableMap( TLongObjectMap<V> m ) {
	    return new TUnmodifiableLongObjectMap<V>( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/Object map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static <V> TByteObjectMap<V> unmodifiableMap( TByteObjectMap<V> m ) {
	    return new TUnmodifiableByteObjectMap<V>( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/Object map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static <V> TShortObjectMap<V> unmodifiableMap( TShortObjectMap<V> m ) {
	    return new TUnmodifiableShortObjectMap<V>( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove primitive/Object map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static <V> TCharObjectMap<V> unmodifiableMap( TCharObjectMap<V> m ) {
	    return new TUnmodifiableCharObjectMap<V>( m );
    }


    /**
     * Returns an unmodifiable view of the specified Trove Object/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static <K> TObjectDoubleMap<K> unmodifiableMap( TObjectDoubleMap<K> m ) {
	    return new TUnmodifiableObjectDoubleMap<K>( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove Object/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static <K> TObjectFloatMap<K> unmodifiableMap( TObjectFloatMap<K> m ) {
	    return new TUnmodifiableObjectFloatMap<K>( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove Object/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static <K> TObjectIntMap<K> unmodifiableMap( TObjectIntMap<K> m ) {
	    return new TUnmodifiableObjectIntMap<K>( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove Object/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static <K> TObjectLongMap<K> unmodifiableMap( TObjectLongMap<K> m ) {
	    return new TUnmodifiableObjectLongMap<K>( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove Object/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static <K> TObjectByteMap<K> unmodifiableMap( TObjectByteMap<K> m ) {
	    return new TUnmodifiableObjectByteMap<K>( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove Object/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static <K> TObjectShortMap<K> unmodifiableMap( TObjectShortMap<K> m ) {
	    return new TUnmodifiableObjectShortMap<K>( m );
    }

    /**
     * Returns an unmodifiable view of the specified Trove Object/primitive map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified Trove primitive/primitive map.
     */
    public static <K> TObjectCharMap<K> unmodifiableMap( TObjectCharMap<K> m ) {
	    return new TUnmodifiableObjectCharMap<K>( m );
    }



///////////////////////////
// TSynchronizedCollections

    /**
     * Returns a synchronized (thread-safe) Trove collection backed by the specified
     * Trove collection.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing collection is accomplished
     * through the returned collection.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * collection when iterating over it:
     * <pre>
     *  TDoubleCollection c = TCollections.synchronizedCollection( myCollection );
     *     ...
     *  synchronized( c ) {
     *      TDoubleIterator i = c.iterator(); // Must be in the synchronized block
     *      while ( i.hasNext() )
     *         foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned collection does <i>not</i> pass the <tt>hashCode</tt>
     * and <tt>equals</tt> operations through to the backing collection, but
     * relies on <tt>Object</tt>'s equals and hashCode methods.  This is
     * necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  c the collection to be "wrapped" in a synchronized collection.
     * @return a synchronized view of the specified collection.
     */
    public static TDoubleCollection synchronizedCollection( TDoubleCollection c ) {
	    return new TSynchronizedDoubleCollection(c);
    }

    static TDoubleCollection synchronizedCollection( TDoubleCollection c, Object mutex ) {
	    return new TSynchronizedDoubleCollection( c, mutex );
    }

    /**
     * Returns a synchronized (thread-safe) Trove collection backed by the specified
     * Trove collection.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing collection is accomplished
     * through the returned collection.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * collection when iterating over it:
     * <pre>
     *  TFloatCollection c = TCollections.synchronizedCollection( myCollection );
     *     ...
     *  synchronized( c ) {
     *      TFloatIterator i = c.iterator(); // Must be in the synchronized block
     *      while ( i.hasNext() )
     *         foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned collection does <i>not</i> pass the <tt>hashCode</tt>
     * and <tt>equals</tt> operations through to the backing collection, but
     * relies on <tt>Object</tt>'s equals and hashCode methods.  This is
     * necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  c the collection to be "wrapped" in a synchronized collection.
     * @return a synchronized view of the specified collection.
     */
    public static TFloatCollection synchronizedCollection( TFloatCollection c ) {
	    return new TSynchronizedFloatCollection(c);
    }

    static TFloatCollection synchronizedCollection( TFloatCollection c, Object mutex ) {
	    return new TSynchronizedFloatCollection( c, mutex );
    }

    /**
     * Returns a synchronized (thread-safe) Trove collection backed by the specified
     * Trove collection.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing collection is accomplished
     * through the returned collection.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * collection when iterating over it:
     * <pre>
     *  TIntCollection c = TCollections.synchronizedCollection( myCollection );
     *     ...
     *  synchronized( c ) {
     *      TIntIterator i = c.iterator(); // Must be in the synchronized block
     *      while ( i.hasNext() )
     *         foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned collection does <i>not</i> pass the <tt>hashCode</tt>
     * and <tt>equals</tt> operations through to the backing collection, but
     * relies on <tt>Object</tt>'s equals and hashCode methods.  This is
     * necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  c the collection to be "wrapped" in a synchronized collection.
     * @return a synchronized view of the specified collection.
     */
    public static TIntCollection synchronizedCollection( TIntCollection c ) {
	    return new TSynchronizedIntCollection(c);
    }

    static TIntCollection synchronizedCollection( TIntCollection c, Object mutex ) {
	    return new TSynchronizedIntCollection( c, mutex );
    }

    /**
     * Returns a synchronized (thread-safe) Trove collection backed by the specified
     * Trove collection.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing collection is accomplished
     * through the returned collection.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * collection when iterating over it:
     * <pre>
     *  TLongCollection c = TCollections.synchronizedCollection( myCollection );
     *     ...
     *  synchronized( c ) {
     *      TLongIterator i = c.iterator(); // Must be in the synchronized block
     *      while ( i.hasNext() )
     *         foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned collection does <i>not</i> pass the <tt>hashCode</tt>
     * and <tt>equals</tt> operations through to the backing collection, but
     * relies on <tt>Object</tt>'s equals and hashCode methods.  This is
     * necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  c the collection to be "wrapped" in a synchronized collection.
     * @return a synchronized view of the specified collection.
     */
    public static TLongCollection synchronizedCollection( TLongCollection c ) {
	    return new TSynchronizedLongCollection(c);
    }

    static TLongCollection synchronizedCollection( TLongCollection c, Object mutex ) {
	    return new TSynchronizedLongCollection( c, mutex );
    }

    /**
     * Returns a synchronized (thread-safe) Trove collection backed by the specified
     * Trove collection.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing collection is accomplished
     * through the returned collection.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * collection when iterating over it:
     * <pre>
     *  TByteCollection c = TCollections.synchronizedCollection( myCollection );
     *     ...
     *  synchronized( c ) {
     *      TByteIterator i = c.iterator(); // Must be in the synchronized block
     *      while ( i.hasNext() )
     *         foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned collection does <i>not</i> pass the <tt>hashCode</tt>
     * and <tt>equals</tt> operations through to the backing collection, but
     * relies on <tt>Object</tt>'s equals and hashCode methods.  This is
     * necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  c the collection to be "wrapped" in a synchronized collection.
     * @return a synchronized view of the specified collection.
     */
    public static TByteCollection synchronizedCollection( TByteCollection c ) {
	    return new TSynchronizedByteCollection(c);
    }

    static TByteCollection synchronizedCollection( TByteCollection c, Object mutex ) {
	    return new TSynchronizedByteCollection( c, mutex );
    }

    /**
     * Returns a synchronized (thread-safe) Trove collection backed by the specified
     * Trove collection.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing collection is accomplished
     * through the returned collection.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * collection when iterating over it:
     * <pre>
     *  TShortCollection c = TCollections.synchronizedCollection( myCollection );
     *     ...
     *  synchronized( c ) {
     *      TShortIterator i = c.iterator(); // Must be in the synchronized block
     *      while ( i.hasNext() )
     *         foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned collection does <i>not</i> pass the <tt>hashCode</tt>
     * and <tt>equals</tt> operations through to the backing collection, but
     * relies on <tt>Object</tt>'s equals and hashCode methods.  This is
     * necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  c the collection to be "wrapped" in a synchronized collection.
     * @return a synchronized view of the specified collection.
     */
    public static TShortCollection synchronizedCollection( TShortCollection c ) {
	    return new TSynchronizedShortCollection(c);
    }

    static TShortCollection synchronizedCollection( TShortCollection c, Object mutex ) {
	    return new TSynchronizedShortCollection( c, mutex );
    }

    /**
     * Returns a synchronized (thread-safe) Trove collection backed by the specified
     * Trove collection.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing collection is accomplished
     * through the returned collection.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * collection when iterating over it:
     * <pre>
     *  TCharCollection c = TCollections.synchronizedCollection( myCollection );
     *     ...
     *  synchronized( c ) {
     *      TCharIterator i = c.iterator(); // Must be in the synchronized block
     *      while ( i.hasNext() )
     *         foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned collection does <i>not</i> pass the <tt>hashCode</tt>
     * and <tt>equals</tt> operations through to the backing collection, but
     * relies on <tt>Object</tt>'s equals and hashCode methods.  This is
     * necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable.
     *
     * @param  c the collection to be "wrapped" in a synchronized collection.
     * @return a synchronized view of the specified collection.
     */
    public static TCharCollection synchronizedCollection( TCharCollection c ) {
	    return new TSynchronizedCharCollection(c);
    }

    static TCharCollection synchronizedCollection( TCharCollection c, Object mutex ) {
	    return new TSynchronizedCharCollection( c, mutex );
    }


    /**
     * Returns a synchronized (thread-safe) Trove set backed by the specified
     * set.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing set is accomplished
     * through the returned set.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * set when iterating over it:
     * <pre>
     *  TDoubleSet s = TCollections.synchronizedSet( new TDoubleHashSet() );
     *      ...
     *  synchronized(s) {
     *      TDoubleIterator i = s.iterator(); // Must be in the synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned set will be serializable if the specified set is
     * serializable.
     *
     * @param  s the set to be "wrapped" in a synchronized set.
     * @return a synchronized view of the specified set.
     */
    public static TDoubleSet synchronizedSet( TDoubleSet s ) {
	    return new TSynchronizedDoubleSet( s );
    }

    static TDoubleSet synchronizedSet( TDoubleSet s, Object mutex ) {
	    return new TSynchronizedDoubleSet( s, mutex );
    }

    /**
     * Returns a synchronized (thread-safe) Trove set backed by the specified
     * set.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing set is accomplished
     * through the returned set.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * set when iterating over it:
     * <pre>
     *  TFloatSet s = TCollections.synchronizedSet( new TFloatHashSet() );
     *      ...
     *  synchronized(s) {
     *      TFloatIterator i = s.iterator(); // Must be in the synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned set will be serializable if the specified set is
     * serializable.
     *
     * @param  s the set to be "wrapped" in a synchronized set.
     * @return a synchronized view of the specified set.
     */
    public static TFloatSet synchronizedSet( TFloatSet s ) {
	    return new TSynchronizedFloatSet( s );
    }

    static TFloatSet synchronizedSet( TFloatSet s, Object mutex ) {
	    return new TSynchronizedFloatSet( s, mutex );
    }

    /**
     * Returns a synchronized (thread-safe) Trove set backed by the specified
     * set.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing set is accomplished
     * through the returned set.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * set when iterating over it:
     * <pre>
     *  TIntSet s = TCollections.synchronizedSet( new TIntHashSet() );
     *      ...
     *  synchronized(s) {
     *      TIntIterator i = s.iterator(); // Must be in the synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned set will be serializable if the specified set is
     * serializable.
     *
     * @param  s the set to be "wrapped" in a synchronized set.
     * @return a synchronized view of the specified set.
     */
    public static TIntSet synchronizedSet( TIntSet s ) {
	    return new TSynchronizedIntSet( s );
    }

    static TIntSet synchronizedSet( TIntSet s, Object mutex ) {
	    return new TSynchronizedIntSet( s, mutex );
    }

    /**
     * Returns a synchronized (thread-safe) Trove set backed by the specified
     * set.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing set is accomplished
     * through the returned set.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * set when iterating over it:
     * <pre>
     *  TLongSet s = TCollections.synchronizedSet( new TLongHashSet() );
     *      ...
     *  synchronized(s) {
     *      TLongIterator i = s.iterator(); // Must be in the synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned set will be serializable if the specified set is
     * serializable.
     *
     * @param  s the set to be "wrapped" in a synchronized set.
     * @return a synchronized view of the specified set.
     */
    public static TLongSet synchronizedSet( TLongSet s ) {
	    return new TSynchronizedLongSet( s );
    }

    static TLongSet synchronizedSet( TLongSet s, Object mutex ) {
	    return new TSynchronizedLongSet( s, mutex );
    }

    /**
     * Returns a synchronized (thread-safe) Trove set backed by the specified
     * set.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing set is accomplished
     * through the returned set.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * set when iterating over it:
     * <pre>
     *  TByteSet s = TCollections.synchronizedSet( new TByteHashSet() );
     *      ...
     *  synchronized(s) {
     *      TByteIterator i = s.iterator(); // Must be in the synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned set will be serializable if the specified set is
     * serializable.
     *
     * @param  s the set to be "wrapped" in a synchronized set.
     * @return a synchronized view of the specified set.
     */
    public static TByteSet synchronizedSet( TByteSet s ) {
	    return new TSynchronizedByteSet( s );
    }

    static TByteSet synchronizedSet( TByteSet s, Object mutex ) {
	    return new TSynchronizedByteSet( s, mutex );
    }

    /**
     * Returns a synchronized (thread-safe) Trove set backed by the specified
     * set.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing set is accomplished
     * through the returned set.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * set when iterating over it:
     * <pre>
     *  TShortSet s = TCollections.synchronizedSet( new TShortHashSet() );
     *      ...
     *  synchronized(s) {
     *      TShortIterator i = s.iterator(); // Must be in the synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned set will be serializable if the specified set is
     * serializable.
     *
     * @param  s the set to be "wrapped" in a synchronized set.
     * @return a synchronized view of the specified set.
     */
    public static TShortSet synchronizedSet( TShortSet s ) {
	    return new TSynchronizedShortSet( s );
    }

    static TShortSet synchronizedSet( TShortSet s, Object mutex ) {
	    return new TSynchronizedShortSet( s, mutex );
    }

    /**
     * Returns a synchronized (thread-safe) Trove set backed by the specified
     * set.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing set is accomplished
     * through the returned set.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * set when iterating over it:
     * <pre>
     *  TCharSet s = TCollections.synchronizedSet( new TCharHashSet() );
     *      ...
     *  synchronized(s) {
     *      TCharIterator i = s.iterator(); // Must be in the synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned set will be serializable if the specified set is
     * serializable.
     *
     * @param  s the set to be "wrapped" in a synchronized set.
     * @return a synchronized view of the specified set.
     */
    public static TCharSet synchronizedSet( TCharSet s ) {
	    return new TSynchronizedCharSet( s );
    }

    static TCharSet synchronizedSet( TCharSet s, Object mutex ) {
	    return new TSynchronizedCharSet( s, mutex );
    }


    /**
     * Returns a synchronized (thread-safe) Trove list backed by the specified
     * list.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing list is accomplished
     * through the returned list.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * list when iterating over it:
     * <pre>
     *  TDoubleList list = TCollections.synchronizedList( new TDoubleArrayList() );
     *      ...
     *  synchronized( list ) {
     *      TDoubleIterator i = list.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned list will be serializable if the specified list is
     * serializable.
     *
     * @param  list the list to be "wrapped" in a synchronized list.
     * @return a synchronized view of the specified list.
     */
    public static TDoubleList synchronizedList( TDoubleList list ) {
	    return ( list instanceof RandomAccess ?
                new TSynchronizedRandomAccessDoubleList( list ) :
                new TSynchronizedDoubleList( list ) );
    }

    static TDoubleList synchronizedList( TDoubleList list, Object mutex ) {
	    return ( list instanceof RandomAccess ?
                new TSynchronizedRandomAccessDoubleList( list, mutex ) :
                new TSynchronizedDoubleList( list, mutex ) );
    }

    /**
     * Returns a synchronized (thread-safe) Trove list backed by the specified
     * list.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing list is accomplished
     * through the returned list.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * list when iterating over it:
     * <pre>
     *  TFloatList list = TCollections.synchronizedList( new TFloatArrayList() );
     *      ...
     *  synchronized( list ) {
     *      TFloatIterator i = list.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned list will be serializable if the specified list is
     * serializable.
     *
     * @param  list the list to be "wrapped" in a synchronized list.
     * @return a synchronized view of the specified list.
     */
    public static TFloatList synchronizedList( TFloatList list ) {
	    return ( list instanceof RandomAccess ?
                new TSynchronizedRandomAccessFloatList( list ) :
                new TSynchronizedFloatList( list ) );
    }

    static TFloatList synchronizedList( TFloatList list, Object mutex ) {
	    return ( list instanceof RandomAccess ?
                new TSynchronizedRandomAccessFloatList( list, mutex ) :
                new TSynchronizedFloatList( list, mutex ) );
    }

    /**
     * Returns a synchronized (thread-safe) Trove list backed by the specified
     * list.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing list is accomplished
     * through the returned list.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * list when iterating over it:
     * <pre>
     *  TIntList list = TCollections.synchronizedList( new TIntArrayList() );
     *      ...
     *  synchronized( list ) {
     *      TIntIterator i = list.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned list will be serializable if the specified list is
     * serializable.
     *
     * @param  list the list to be "wrapped" in a synchronized list.
     * @return a synchronized view of the specified list.
     */
    public static TIntList synchronizedList( TIntList list ) {
	    return ( list instanceof RandomAccess ?
                new TSynchronizedRandomAccessIntList( list ) :
                new TSynchronizedIntList( list ) );
    }

    static TIntList synchronizedList( TIntList list, Object mutex ) {
	    return ( list instanceof RandomAccess ?
                new TSynchronizedRandomAccessIntList( list, mutex ) :
                new TSynchronizedIntList( list, mutex ) );
    }

    /**
     * Returns a synchronized (thread-safe) Trove list backed by the specified
     * list.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing list is accomplished
     * through the returned list.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * list when iterating over it:
     * <pre>
     *  TLongList list = TCollections.synchronizedList( new TLongArrayList() );
     *      ...
     *  synchronized( list ) {
     *      TLongIterator i = list.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned list will be serializable if the specified list is
     * serializable.
     *
     * @param  list the list to be "wrapped" in a synchronized list.
     * @return a synchronized view of the specified list.
     */
    public static TLongList synchronizedList( TLongList list ) {
	    return ( list instanceof RandomAccess ?
                new TSynchronizedRandomAccessLongList( list ) :
                new TSynchronizedLongList( list ) );
    }

    static TLongList synchronizedList( TLongList list, Object mutex ) {
	    return ( list instanceof RandomAccess ?
                new TSynchronizedRandomAccessLongList( list, mutex ) :
                new TSynchronizedLongList( list, mutex ) );
    }

    /**
     * Returns a synchronized (thread-safe) Trove list backed by the specified
     * list.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing list is accomplished
     * through the returned list.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * list when iterating over it:
     * <pre>
     *  TByteList list = TCollections.synchronizedList( new TByteArrayList() );
     *      ...
     *  synchronized( list ) {
     *      TByteIterator i = list.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned list will be serializable if the specified list is
     * serializable.
     *
     * @param  list the list to be "wrapped" in a synchronized list.
     * @return a synchronized view of the specified list.
     */
    public static TByteList synchronizedList( TByteList list ) {
	    return ( list instanceof RandomAccess ?
                new TSynchronizedRandomAccessByteList( list ) :
                new TSynchronizedByteList( list ) );
    }

    static TByteList synchronizedList( TByteList list, Object mutex ) {
	    return ( list instanceof RandomAccess ?
                new TSynchronizedRandomAccessByteList( list, mutex ) :
                new TSynchronizedByteList( list, mutex ) );
    }

    /**
     * Returns a synchronized (thread-safe) Trove list backed by the specified
     * list.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing list is accomplished
     * through the returned list.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * list when iterating over it:
     * <pre>
     *  TShortList list = TCollections.synchronizedList( new TShortArrayList() );
     *      ...
     *  synchronized( list ) {
     *      TShortIterator i = list.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned list will be serializable if the specified list is
     * serializable.
     *
     * @param  list the list to be "wrapped" in a synchronized list.
     * @return a synchronized view of the specified list.
     */
    public static TShortList synchronizedList( TShortList list ) {
	    return ( list instanceof RandomAccess ?
                new TSynchronizedRandomAccessShortList( list ) :
                new TSynchronizedShortList( list ) );
    }

    static TShortList synchronizedList( TShortList list, Object mutex ) {
	    return ( list instanceof RandomAccess ?
                new TSynchronizedRandomAccessShortList( list, mutex ) :
                new TSynchronizedShortList( list, mutex ) );
    }

    /**
     * Returns a synchronized (thread-safe) Trove list backed by the specified
     * list.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing list is accomplished
     * through the returned list.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * list when iterating over it:
     * <pre>
     *  TCharList list = TCollections.synchronizedList( new TCharArrayList() );
     *      ...
     *  synchronized( list ) {
     *      TCharIterator i = list.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned list will be serializable if the specified list is
     * serializable.
     *
     * @param  list the list to be "wrapped" in a synchronized list.
     * @return a synchronized view of the specified list.
     */
    public static TCharList synchronizedList( TCharList list ) {
	    return ( list instanceof RandomAccess ?
                new TSynchronizedRandomAccessCharList( list ) :
                new TSynchronizedCharList( list ) );
    }

    static TCharList synchronizedList( TCharList list, Object mutex ) {
	    return ( list instanceof RandomAccess ?
                new TSynchronizedRandomAccessCharList( list, mutex ) :
                new TSynchronizedCharList( list, mutex ) );
    }


    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TDoubleDoubleMap m = TCollections.synchronizedMap( new TDoubleDoubleHashMap() );
     *      ...
     *  TDoubleSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TDoubleIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TDoubleDoubleMap synchronizedMap( TDoubleDoubleMap m ) {
	    return new TSynchronizedDoubleDoubleMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TDoubleFloatMap m = TCollections.synchronizedMap( new TDoubleFloatHashMap() );
     *      ...
     *  TDoubleSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TDoubleIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TDoubleFloatMap synchronizedMap( TDoubleFloatMap m ) {
	    return new TSynchronizedDoubleFloatMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TDoubleIntMap m = TCollections.synchronizedMap( new TDoubleIntHashMap() );
     *      ...
     *  TDoubleSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TDoubleIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TDoubleIntMap synchronizedMap( TDoubleIntMap m ) {
	    return new TSynchronizedDoubleIntMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TDoubleLongMap m = TCollections.synchronizedMap( new TDoubleLongHashMap() );
     *      ...
     *  TDoubleSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TDoubleIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TDoubleLongMap synchronizedMap( TDoubleLongMap m ) {
	    return new TSynchronizedDoubleLongMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TDoubleByteMap m = TCollections.synchronizedMap( new TDoubleByteHashMap() );
     *      ...
     *  TDoubleSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TDoubleIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TDoubleByteMap synchronizedMap( TDoubleByteMap m ) {
	    return new TSynchronizedDoubleByteMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TDoubleShortMap m = TCollections.synchronizedMap( new TDoubleShortHashMap() );
     *      ...
     *  TDoubleSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TDoubleIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TDoubleShortMap synchronizedMap( TDoubleShortMap m ) {
	    return new TSynchronizedDoubleShortMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TDoubleCharMap m = TCollections.synchronizedMap( new TDoubleCharHashMap() );
     *      ...
     *  TDoubleSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TDoubleIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TDoubleCharMap synchronizedMap( TDoubleCharMap m ) {
	    return new TSynchronizedDoubleCharMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TFloatDoubleMap m = TCollections.synchronizedMap( new TFloatDoubleHashMap() );
     *      ...
     *  TFloatSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TFloatIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TFloatDoubleMap synchronizedMap( TFloatDoubleMap m ) {
	    return new TSynchronizedFloatDoubleMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TFloatFloatMap m = TCollections.synchronizedMap( new TFloatFloatHashMap() );
     *      ...
     *  TFloatSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TFloatIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TFloatFloatMap synchronizedMap( TFloatFloatMap m ) {
	    return new TSynchronizedFloatFloatMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TFloatIntMap m = TCollections.synchronizedMap( new TFloatIntHashMap() );
     *      ...
     *  TFloatSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TFloatIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TFloatIntMap synchronizedMap( TFloatIntMap m ) {
	    return new TSynchronizedFloatIntMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TFloatLongMap m = TCollections.synchronizedMap( new TFloatLongHashMap() );
     *      ...
     *  TFloatSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TFloatIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TFloatLongMap synchronizedMap( TFloatLongMap m ) {
	    return new TSynchronizedFloatLongMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TFloatByteMap m = TCollections.synchronizedMap( new TFloatByteHashMap() );
     *      ...
     *  TFloatSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TFloatIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TFloatByteMap synchronizedMap( TFloatByteMap m ) {
	    return new TSynchronizedFloatByteMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TFloatShortMap m = TCollections.synchronizedMap( new TFloatShortHashMap() );
     *      ...
     *  TFloatSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TFloatIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TFloatShortMap synchronizedMap( TFloatShortMap m ) {
	    return new TSynchronizedFloatShortMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TFloatCharMap m = TCollections.synchronizedMap( new TFloatCharHashMap() );
     *      ...
     *  TFloatSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TFloatIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TFloatCharMap synchronizedMap( TFloatCharMap m ) {
	    return new TSynchronizedFloatCharMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TIntDoubleMap m = TCollections.synchronizedMap( new TIntDoubleHashMap() );
     *      ...
     *  TIntSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TIntIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TIntDoubleMap synchronizedMap( TIntDoubleMap m ) {
	    return new TSynchronizedIntDoubleMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TIntFloatMap m = TCollections.synchronizedMap( new TIntFloatHashMap() );
     *      ...
     *  TIntSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TIntIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TIntFloatMap synchronizedMap( TIntFloatMap m ) {
	    return new TSynchronizedIntFloatMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TIntIntMap m = TCollections.synchronizedMap( new TIntIntHashMap() );
     *      ...
     *  TIntSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TIntIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TIntIntMap synchronizedMap( TIntIntMap m ) {
	    return new TSynchronizedIntIntMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TIntLongMap m = TCollections.synchronizedMap( new TIntLongHashMap() );
     *      ...
     *  TIntSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TIntIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TIntLongMap synchronizedMap( TIntLongMap m ) {
	    return new TSynchronizedIntLongMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TIntByteMap m = TCollections.synchronizedMap( new TIntByteHashMap() );
     *      ...
     *  TIntSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TIntIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TIntByteMap synchronizedMap( TIntByteMap m ) {
	    return new TSynchronizedIntByteMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TIntShortMap m = TCollections.synchronizedMap( new TIntShortHashMap() );
     *      ...
     *  TIntSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TIntIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TIntShortMap synchronizedMap( TIntShortMap m ) {
	    return new TSynchronizedIntShortMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TIntCharMap m = TCollections.synchronizedMap( new TIntCharHashMap() );
     *      ...
     *  TIntSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TIntIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TIntCharMap synchronizedMap( TIntCharMap m ) {
	    return new TSynchronizedIntCharMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TLongDoubleMap m = TCollections.synchronizedMap( new TLongDoubleHashMap() );
     *      ...
     *  TLongSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TLongIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TLongDoubleMap synchronizedMap( TLongDoubleMap m ) {
	    return new TSynchronizedLongDoubleMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TLongFloatMap m = TCollections.synchronizedMap( new TLongFloatHashMap() );
     *      ...
     *  TLongSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TLongIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TLongFloatMap synchronizedMap( TLongFloatMap m ) {
	    return new TSynchronizedLongFloatMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TLongIntMap m = TCollections.synchronizedMap( new TLongIntHashMap() );
     *      ...
     *  TLongSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TLongIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TLongIntMap synchronizedMap( TLongIntMap m ) {
	    return new TSynchronizedLongIntMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TLongLongMap m = TCollections.synchronizedMap( new TLongLongHashMap() );
     *      ...
     *  TLongSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TLongIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TLongLongMap synchronizedMap( TLongLongMap m ) {
	    return new TSynchronizedLongLongMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TLongByteMap m = TCollections.synchronizedMap( new TLongByteHashMap() );
     *      ...
     *  TLongSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TLongIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TLongByteMap synchronizedMap( TLongByteMap m ) {
	    return new TSynchronizedLongByteMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TLongShortMap m = TCollections.synchronizedMap( new TLongShortHashMap() );
     *      ...
     *  TLongSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TLongIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TLongShortMap synchronizedMap( TLongShortMap m ) {
	    return new TSynchronizedLongShortMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TLongCharMap m = TCollections.synchronizedMap( new TLongCharHashMap() );
     *      ...
     *  TLongSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TLongIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TLongCharMap synchronizedMap( TLongCharMap m ) {
	    return new TSynchronizedLongCharMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TByteDoubleMap m = TCollections.synchronizedMap( new TByteDoubleHashMap() );
     *      ...
     *  TByteSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TByteIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TByteDoubleMap synchronizedMap( TByteDoubleMap m ) {
	    return new TSynchronizedByteDoubleMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TByteFloatMap m = TCollections.synchronizedMap( new TByteFloatHashMap() );
     *      ...
     *  TByteSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TByteIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TByteFloatMap synchronizedMap( TByteFloatMap m ) {
	    return new TSynchronizedByteFloatMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TByteIntMap m = TCollections.synchronizedMap( new TByteIntHashMap() );
     *      ...
     *  TByteSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TByteIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TByteIntMap synchronizedMap( TByteIntMap m ) {
	    return new TSynchronizedByteIntMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TByteLongMap m = TCollections.synchronizedMap( new TByteLongHashMap() );
     *      ...
     *  TByteSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TByteIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TByteLongMap synchronizedMap( TByteLongMap m ) {
	    return new TSynchronizedByteLongMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TByteByteMap m = TCollections.synchronizedMap( new TByteByteHashMap() );
     *      ...
     *  TByteSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TByteIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TByteByteMap synchronizedMap( TByteByteMap m ) {
	    return new TSynchronizedByteByteMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TByteShortMap m = TCollections.synchronizedMap( new TByteShortHashMap() );
     *      ...
     *  TByteSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TByteIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TByteShortMap synchronizedMap( TByteShortMap m ) {
	    return new TSynchronizedByteShortMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TByteCharMap m = TCollections.synchronizedMap( new TByteCharHashMap() );
     *      ...
     *  TByteSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TByteIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TByteCharMap synchronizedMap( TByteCharMap m ) {
	    return new TSynchronizedByteCharMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TShortDoubleMap m = TCollections.synchronizedMap( new TShortDoubleHashMap() );
     *      ...
     *  TShortSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TShortIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TShortDoubleMap synchronizedMap( TShortDoubleMap m ) {
	    return new TSynchronizedShortDoubleMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TShortFloatMap m = TCollections.synchronizedMap( new TShortFloatHashMap() );
     *      ...
     *  TShortSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TShortIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TShortFloatMap synchronizedMap( TShortFloatMap m ) {
	    return new TSynchronizedShortFloatMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TShortIntMap m = TCollections.synchronizedMap( new TShortIntHashMap() );
     *      ...
     *  TShortSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TShortIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TShortIntMap synchronizedMap( TShortIntMap m ) {
	    return new TSynchronizedShortIntMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TShortLongMap m = TCollections.synchronizedMap( new TShortLongHashMap() );
     *      ...
     *  TShortSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TShortIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TShortLongMap synchronizedMap( TShortLongMap m ) {
	    return new TSynchronizedShortLongMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TShortByteMap m = TCollections.synchronizedMap( new TShortByteHashMap() );
     *      ...
     *  TShortSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TShortIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TShortByteMap synchronizedMap( TShortByteMap m ) {
	    return new TSynchronizedShortByteMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TShortShortMap m = TCollections.synchronizedMap( new TShortShortHashMap() );
     *      ...
     *  TShortSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TShortIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TShortShortMap synchronizedMap( TShortShortMap m ) {
	    return new TSynchronizedShortShortMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TShortCharMap m = TCollections.synchronizedMap( new TShortCharHashMap() );
     *      ...
     *  TShortSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TShortIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TShortCharMap synchronizedMap( TShortCharMap m ) {
	    return new TSynchronizedShortCharMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TCharDoubleMap m = TCollections.synchronizedMap( new TCharDoubleHashMap() );
     *      ...
     *  TCharSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TCharIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TCharDoubleMap synchronizedMap( TCharDoubleMap m ) {
	    return new TSynchronizedCharDoubleMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TCharFloatMap m = TCollections.synchronizedMap( new TCharFloatHashMap() );
     *      ...
     *  TCharSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TCharIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TCharFloatMap synchronizedMap( TCharFloatMap m ) {
	    return new TSynchronizedCharFloatMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TCharIntMap m = TCollections.synchronizedMap( new TCharIntHashMap() );
     *      ...
     *  TCharSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TCharIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TCharIntMap synchronizedMap( TCharIntMap m ) {
	    return new TSynchronizedCharIntMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TCharLongMap m = TCollections.synchronizedMap( new TCharLongHashMap() );
     *      ...
     *  TCharSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TCharIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TCharLongMap synchronizedMap( TCharLongMap m ) {
	    return new TSynchronizedCharLongMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TCharByteMap m = TCollections.synchronizedMap( new TCharByteHashMap() );
     *      ...
     *  TCharSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TCharIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TCharByteMap synchronizedMap( TCharByteMap m ) {
	    return new TSynchronizedCharByteMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TCharShortMap m = TCollections.synchronizedMap( new TCharShortHashMap() );
     *      ...
     *  TCharSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TCharIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TCharShortMap synchronizedMap( TCharShortMap m ) {
	    return new TSynchronizedCharShortMap( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TCharCharMap m = TCollections.synchronizedMap( new TCharCharHashMap() );
     *      ...
     *  TCharSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TCharIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static TCharCharMap synchronizedMap( TCharCharMap m ) {
	    return new TSynchronizedCharCharMap( m );
    }


    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TDoubleObjectMap m = TCollections.synchronizedMap( new TDoubleObjectHashMap() );
     *      ...
     *  TDoubleSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TDoubleIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <V> TDoubleObjectMap<V> synchronizedMap( TDoubleObjectMap<V> m ) {
	    return new TSynchronizedDoubleObjectMap<V>( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TFloatObjectMap m = TCollections.synchronizedMap( new TFloatObjectHashMap() );
     *      ...
     *  TFloatSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TFloatIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <V> TFloatObjectMap<V> synchronizedMap( TFloatObjectMap<V> m ) {
	    return new TSynchronizedFloatObjectMap<V>( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TIntObjectMap m = TCollections.synchronizedMap( new TIntObjectHashMap() );
     *      ...
     *  TIntSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TIntIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <V> TIntObjectMap<V> synchronizedMap( TIntObjectMap<V> m ) {
	    return new TSynchronizedIntObjectMap<V>( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TLongObjectMap m = TCollections.synchronizedMap( new TLongObjectHashMap() );
     *      ...
     *  TLongSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TLongIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <V> TLongObjectMap<V> synchronizedMap( TLongObjectMap<V> m ) {
	    return new TSynchronizedLongObjectMap<V>( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TByteObjectMap m = TCollections.synchronizedMap( new TByteObjectHashMap() );
     *      ...
     *  TByteSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TByteIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <V> TByteObjectMap<V> synchronizedMap( TByteObjectMap<V> m ) {
	    return new TSynchronizedByteObjectMap<V>( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TShortObjectMap m = TCollections.synchronizedMap( new TShortObjectHashMap() );
     *      ...
     *  TShortSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TShortIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <V> TShortObjectMap<V> synchronizedMap( TShortObjectMap<V> m ) {
	    return new TSynchronizedShortObjectMap<V>( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TCharObjectMap m = TCollections.synchronizedMap( new TCharObjectHashMap() );
     *      ...
     *  TCharSet s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      TCharIterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <V> TCharObjectMap<V> synchronizedMap( TCharObjectMap<V> m ) {
	    return new TSynchronizedCharObjectMap<V>( m );
    }


    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TObjectDoubleMap m = TCollections.synchronizedMap( new TObjectDoubleHashMap() );
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <K> TObjectDoubleMap<K> synchronizedMap( TObjectDoubleMap<K> m ) {
	    return new TSynchronizedObjectDoubleMap<K>( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TObjectFloatMap m = TCollections.synchronizedMap( new TObjectFloatHashMap() );
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <K> TObjectFloatMap<K> synchronizedMap( TObjectFloatMap<K> m ) {
	    return new TSynchronizedObjectFloatMap<K>( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TObjectIntMap m = TCollections.synchronizedMap( new TObjectIntHashMap() );
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <K> TObjectIntMap<K> synchronizedMap( TObjectIntMap<K> m ) {
	    return new TSynchronizedObjectIntMap<K>( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TObjectLongMap m = TCollections.synchronizedMap( new TObjectLongHashMap() );
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <K> TObjectLongMap<K> synchronizedMap( TObjectLongMap<K> m ) {
	    return new TSynchronizedObjectLongMap<K>( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TObjectByteMap m = TCollections.synchronizedMap( new TObjectByteHashMap() );
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <K> TObjectByteMap<K> synchronizedMap( TObjectByteMap<K> m ) {
	    return new TSynchronizedObjectByteMap<K>( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TObjectShortMap m = TCollections.synchronizedMap( new TObjectShortHashMap() );
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <K> TObjectShortMap<K> synchronizedMap( TObjectShortMap<K> m ) {
	    return new TSynchronizedObjectShortMap<K>( m );
    }

    /**
     * Returns a synchronized (thread-safe) Trove map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  TObjectCharMap m = TCollections.synchronizedMap( new TObjectCharHashMap() );
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized( m ) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while ( i.hasNext() )
     *          foo( i.next() );
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static <K> TObjectCharMap<K> synchronizedMap( TObjectCharMap<K> m ) {
	    return new TSynchronizedObjectCharMap<K>( m );
    }
}