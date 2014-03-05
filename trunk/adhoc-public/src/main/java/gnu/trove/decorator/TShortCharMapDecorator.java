///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2002, Eric D. Friedman All Rights Reserved.
// Copyright (c) 2009, Robert D. Eden All Rights Reserved.
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

package gnu.trove.decorator;

import gnu.trove.map.TShortCharMap;
import gnu.trove.iterator.TShortCharIterator;

import java.io.*;
import java.util.*;


//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * Wrapper class to make a TShortCharMap conform to the <tt>java.util.Map</tt> API.
 * This class simply decorates an underlying TShortCharMap and translates the Object-based
 * APIs into their Trove primitive analogs.
 * <p/>
 * Note that wrapping and unwrapping primitive values is extremely inefficient.  If
 * possible, users of this class should override the appropriate methods in this class
 * and use a table of canonical values.
 * <p/>
 * Created: Mon Sep 23 22:07:40 PDT 2002
 *
 * @author Eric D. Friedman
 * @author Robert D. Eden
 * @author Jeff Randall
 */
public class TShortCharMapDecorator extends AbstractMap<Short, Character>
	implements Map<Short, Character>, Externalizable, Cloneable {

	static final long serialVersionUID = 1L;

    /** the wrapped primitive map */
    protected TShortCharMap _map;


    /**
     * FOR EXTERNALIZATION ONLY!!
     */
    public TShortCharMapDecorator() {}


    /**
     * Creates a wrapper that decorates the specified primitive map.
     *
     * @param map the <tt>TShortCharMap</tt> to wrap.
     */
    public TShortCharMapDecorator( TShortCharMap map ) {
        super();
        this._map = map;
    }


    /**
     * Returns a reference to the map wrapped by this decorator.
     *
     * @return the wrapped <tt>TShortCharMap</tt> instance.
     */
    public TShortCharMap getMap() {
        return _map;
    }


    /**
     * Inserts a key/value pair into the map.
     *
     * @param key   an <code>Object</code> value
     * @param value an <code>Object</code> value
     * @return the previous value associated with <tt>key</tt>,
     *         or Character(0) if none was found.
     */
    public Character put( Short key, Character value ) {
        short k;
        char v;
        if ( key == null ) {
            k = _map.getNoEntryKey();
        } else {
            k = unwrapKey( key );
        }
        if ( value == null ) {
            v = _map.getNoEntryValue();
        } else {
            v = unwrapValue( value );
        }
        char retval = _map.put( k, v );
        if ( retval == _map.getNoEntryValue() ) {
            return null;
        }
        return wrapValue( retval );
    }


    /**
     * Retrieves the value for <tt>key</tt>
     *
     * @param key an <code>Object</code> value
     * @return the value of <tt>key</tt> or null if no such mapping exists.
     */
    public Character get( Object key ) {
        short k;
        if ( key != null ) {
            if ( key instanceof Short ) {
                k = unwrapKey( key );
            } else {
                return null;
            }
        } else {
            k = _map.getNoEntryKey();
        }
        char v = _map.get( k );
        // There may be a false positive since primitive maps
        // cannot return null, so we have to do an extra
        // check here.
        if ( v == _map.getNoEntryValue() ) {
            return null;
        } else {
            return wrapValue( v );
        }
    }


    /**
     * Empties the map.
     */
    public void clear() {
        this._map.clear();
    }


    /**
     * Deletes a key/value pair from the map.
     *
     * @param key an <code>Object</code> value
     * @return the removed value, or null if it was not found in the map
     */
    public Character remove( Object key ) {
        short k;
        if ( key != null ) {
            if ( key instanceof Short ) {
                k = unwrapKey( key );
            } else {
                return null;
            }
        } else {
            k = _map.getNoEntryKey();
        }
        char v = _map.remove( k );
        // There may be a false positive since primitive maps
        // cannot return null, so we have to do an extra
        // check here.
        if ( v == _map.getNoEntryValue() ) {
            return null;
        } else {
            return wrapValue( v );
        }
    }


    /**
     * Returns a Set view on the entries of the map.
     *
     * @return a <code>Set</code> value
     */
    public Set<Map.Entry<Short,Character>> entrySet() {
        return new AbstractSet<Map.Entry<Short,Character>>() {
            public int size() {
                return _map.size();
            }

            public boolean isEmpty() {
                return TShortCharMapDecorator.this.isEmpty();
            }

            public boolean contains( Object o ) {
                if (o instanceof Map.Entry) {
                    Object k = ( ( Map.Entry ) o ).getKey();
                    Object v = ( ( Map.Entry ) o ).getValue();
                    return TShortCharMapDecorator.this.containsKey(k)
                            && TShortCharMapDecorator.this.get(k).equals(v);
                } else {
                    return false;
                }
            }

            public Iterator<Map.Entry<Short,Character>> iterator() {
                return new Iterator<Map.Entry<Short,Character>>() {
                    private final TShortCharIterator it = _map.iterator();

                    public Map.Entry<Short,Character> next() {
                        it.advance();
                        short ik = it.key();
                        final Short key = (ik == _map.getNoEntryKey()) ? null : wrapKey( ik );
                        char iv = it.value();
                        final Character v = (iv == _map.getNoEntryValue()) ? null : wrapValue( iv );
                        return new Map.Entry<Short,Character>() {
                            private Character val = v;

                            public boolean equals( Object o ) {
                                return o instanceof Map.Entry
                                        && ( ( Map.Entry ) o ).getKey().equals(key)
                                        && ( ( Map.Entry ) o ).getValue().equals(val);
                            }

                            public Short getKey() {
                                return key;
                            }

                            public Character getValue() {
                                return val;
                            }

                            public int hashCode() {
                                return key.hashCode() + val.hashCode();
                            }

                            public Character setValue( Character value ) {
                                val = value;
                                return put( key, value );
                            }
                        };
                    }

                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    public void remove() {
                        it.remove();
                    }
                };
            }

            public boolean add( Map.Entry<Short,Character> o ) {
                throw new UnsupportedOperationException();
            }

            public boolean remove( Object o ) {
                boolean modified = false;
                if ( contains( o ) ) {
                    //noinspection unchecked
                    Short key = ( ( Map.Entry<Short,Character> ) o ).getKey();
                    _map.remove( unwrapKey( key ) );
                    modified = true;
                }
                return modified;
            }

            public boolean addAll( Collection<? extends Map.Entry<Short, Character>> c ) {
                throw new UnsupportedOperationException();
            }

            public void clear() {
                TShortCharMapDecorator.this.clear();
            }
        };
    }


    /**
     * Checks for the presence of <tt>val</tt> in the values of the map.
     *
     * @param val an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean containsValue( Object val ) {
        return val instanceof Character && _map.containsValue( unwrapValue( val ) );
    }


    /**
     * Checks for the present of <tt>key</tt> in the keys of the map.
     *
     * @param key an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean containsKey( Object key ) {
        if ( key == null ) return _map.containsKey( _map.getNoEntryKey() );
        return key instanceof Short && _map.containsKey( unwrapKey( key ) );
    }


    /**
     * Returns the number of entries in the map.
     *
     * @return the map's size.
     */
    public int size() {
        return this._map.size();
    }


    /**
     * Indicates whether map has any entries.
     *
     * @return true if the map is empty
     */
    public boolean isEmpty() {
        return size() == 0;
    }


    /**
     * Copies the key/value mappings in <tt>map</tt> into this map.
     * Note that this will be a <b>deep</b> copy, as storage is by
     * primitive value.
     *
     * @param map a <code>Map</code> value
     */
    public void putAll( Map<? extends Short, ? extends Character> map ) {
        Iterator<? extends Entry<? extends Short,? extends Character>> it =
            map.entrySet().iterator();
        for ( int i = map.size(); i-- > 0; ) {
            Entry<? extends Short,? extends Character> e = it.next();
            this.put( e.getKey(), e.getValue() );
        }
    }


    /**
     * Wraps a key
     *
     * @param k key in the underlying map
     * @return an Object representation of the key
     */
    protected Short wrapKey( short k ) {
        return Short.valueOf( k );
    }


    /**
     * Unwraps a key
     *
     * @param key wrapped key
     * @return an unwrapped representation of the key
     */
    protected short unwrapKey( Object key ) {
        return ( ( Short ) key ).shortValue();
    }


    /**
     * Wraps a value
     *
     * @param k value in the underlying map
     * @return an Object representation of the value
     */
    protected Character wrapValue( char k ) {
        return Character.valueOf( k );
    }


    /**
     * Unwraps a value
     *
     * @param value wrapped value
     * @return an unwrapped representation of the value
     */
    protected char unwrapValue( Object value ) {
        return ( ( Character ) value ).charValue();
    }


    // Implements Externalizable
    public void readExternal( ObjectInput in )
        throws IOException, ClassNotFoundException {

        // VERSION
        in.readByte();

        // MAP
        _map = ( TShortCharMap ) in.readObject();
    }


    // Implements Externalizable
    public void writeExternal( ObjectOutput out ) throws IOException {
        // VERSION
        out.writeByte(0);

        // MAP
        out.writeObject( _map );
    }

} // TShortCharHashMapDecorator
