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


import java.util.*;

import gnu.trove.list.*;
import gnu.trove.map.*;
import gnu.trove.set.*;
import gnu.trove.decorator.*;



/**
 * This is a static utility class that provides functions for simplifying creation of
 * decorators.
 *
 * @author  Robert D. Eden
 * @author  Jeff Randall
 * @since   Trove 2.1
 */
public class TDecorators {
    // Hide the constructor
    private TDecorators() {}


    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TDoubleDoubleObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Double,Double> wrap( TDoubleDoubleMap map ) {
        return new TDoubleDoubleMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TDoubleFloatObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Double,Float> wrap( TDoubleFloatMap map ) {
        return new TDoubleFloatMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TDoubleIntObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Double,Integer> wrap( TDoubleIntMap map ) {
        return new TDoubleIntMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TDoubleLongObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Double,Long> wrap( TDoubleLongMap map ) {
        return new TDoubleLongMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TDoubleByteObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Double,Byte> wrap( TDoubleByteMap map ) {
        return new TDoubleByteMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TDoubleShortObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Double,Short> wrap( TDoubleShortMap map ) {
        return new TDoubleShortMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TDoubleCharObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Double,Character> wrap( TDoubleCharMap map ) {
        return new TDoubleCharMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TFloatDoubleObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Float,Double> wrap( TFloatDoubleMap map ) {
        return new TFloatDoubleMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TFloatFloatObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Float,Float> wrap( TFloatFloatMap map ) {
        return new TFloatFloatMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TFloatIntObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Float,Integer> wrap( TFloatIntMap map ) {
        return new TFloatIntMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TFloatLongObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Float,Long> wrap( TFloatLongMap map ) {
        return new TFloatLongMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TFloatByteObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Float,Byte> wrap( TFloatByteMap map ) {
        return new TFloatByteMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TFloatShortObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Float,Short> wrap( TFloatShortMap map ) {
        return new TFloatShortMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TFloatCharObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Float,Character> wrap( TFloatCharMap map ) {
        return new TFloatCharMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TIntDoubleObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Integer,Double> wrap( TIntDoubleMap map ) {
        return new TIntDoubleMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TIntFloatObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Integer,Float> wrap( TIntFloatMap map ) {
        return new TIntFloatMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TIntIntObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Integer,Integer> wrap( TIntIntMap map ) {
        return new TIntIntMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TIntLongObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Integer,Long> wrap( TIntLongMap map ) {
        return new TIntLongMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TIntByteObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Integer,Byte> wrap( TIntByteMap map ) {
        return new TIntByteMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TIntShortObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Integer,Short> wrap( TIntShortMap map ) {
        return new TIntShortMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TIntCharObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Integer,Character> wrap( TIntCharMap map ) {
        return new TIntCharMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TLongDoubleObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Long,Double> wrap( TLongDoubleMap map ) {
        return new TLongDoubleMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TLongFloatObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Long,Float> wrap( TLongFloatMap map ) {
        return new TLongFloatMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TLongIntObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Long,Integer> wrap( TLongIntMap map ) {
        return new TLongIntMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TLongLongObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Long,Long> wrap( TLongLongMap map ) {
        return new TLongLongMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TLongByteObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Long,Byte> wrap( TLongByteMap map ) {
        return new TLongByteMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TLongShortObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Long,Short> wrap( TLongShortMap map ) {
        return new TLongShortMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TLongCharObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Long,Character> wrap( TLongCharMap map ) {
        return new TLongCharMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TByteDoubleObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Byte,Double> wrap( TByteDoubleMap map ) {
        return new TByteDoubleMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TByteFloatObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Byte,Float> wrap( TByteFloatMap map ) {
        return new TByteFloatMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TByteIntObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Byte,Integer> wrap( TByteIntMap map ) {
        return new TByteIntMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TByteLongObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Byte,Long> wrap( TByteLongMap map ) {
        return new TByteLongMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TByteByteObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Byte,Byte> wrap( TByteByteMap map ) {
        return new TByteByteMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TByteShortObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Byte,Short> wrap( TByteShortMap map ) {
        return new TByteShortMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TByteCharObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Byte,Character> wrap( TByteCharMap map ) {
        return new TByteCharMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TShortDoubleObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Short,Double> wrap( TShortDoubleMap map ) {
        return new TShortDoubleMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TShortFloatObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Short,Float> wrap( TShortFloatMap map ) {
        return new TShortFloatMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TShortIntObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Short,Integer> wrap( TShortIntMap map ) {
        return new TShortIntMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TShortLongObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Short,Long> wrap( TShortLongMap map ) {
        return new TShortLongMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TShortByteObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Short,Byte> wrap( TShortByteMap map ) {
        return new TShortByteMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TShortShortObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Short,Short> wrap( TShortShortMap map ) {
        return new TShortShortMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TShortCharObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Short,Character> wrap( TShortCharMap map ) {
        return new TShortCharMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TCharDoubleObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Character,Double> wrap( TCharDoubleMap map ) {
        return new TCharDoubleMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TCharFloatObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Character,Float> wrap( TCharFloatMap map ) {
        return new TCharFloatMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TCharIntObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Character,Integer> wrap( TCharIntMap map ) {
        return new TCharIntMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TCharLongObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Character,Long> wrap( TCharLongMap map ) {
        return new TCharLongMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TCharByteObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Character,Byte> wrap( TCharByteMap map ) {
        return new TCharByteMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TCharShortObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Character,Short> wrap( TCharShortMap map ) {
        return new TCharShortMapDecorator( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TCharCharObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static Map<Character,Character> wrap( TCharCharMap map ) {
        return new TCharCharMapDecorator( map );
    }


    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TObjectDoubleMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static <T> Map<T,Double> wrap( TObjectDoubleMap<T> map ) {
        return new TObjectDoubleMapDecorator<T>( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TObjectFloatMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static <T> Map<T,Float> wrap( TObjectFloatMap<T> map ) {
        return new TObjectFloatMapDecorator<T>( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TObjectIntMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static <T> Map<T,Integer> wrap( TObjectIntMap<T> map ) {
        return new TObjectIntMapDecorator<T>( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TObjectLongMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static <T> Map<T,Long> wrap( TObjectLongMap<T> map ) {
        return new TObjectLongMapDecorator<T>( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TObjectByteMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static <T> Map<T,Byte> wrap( TObjectByteMap<T> map ) {
        return new TObjectByteMapDecorator<T>( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TObjectShortMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static <T> Map<T,Short> wrap( TObjectShortMap<T> map ) {
        return new TObjectShortMapDecorator<T>( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TObjectCharMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static <T> Map<T,Character> wrap( TObjectCharMap<T> map ) {
        return new TObjectCharMapDecorator<T>( map );
    }


    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TDoubleObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static <T> Map<Double,T> wrap( TDoubleObjectMap<T> map ) {
        return new TDoubleObjectMapDecorator<T>( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TFloatObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static <T> Map<Float,T> wrap( TFloatObjectMap<T> map ) {
        return new TFloatObjectMapDecorator<T>( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TIntObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static <T> Map<Integer,T> wrap( TIntObjectMap<T> map ) {
        return new TIntObjectMapDecorator<T>( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TLongObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static <T> Map<Long,T> wrap( TLongObjectMap<T> map ) {
        return new TLongObjectMapDecorator<T>( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TByteObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static <T> Map<Byte,T> wrap( TByteObjectMap<T> map ) {
        return new TByteObjectMapDecorator<T>( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TShortObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static <T> Map<Short,T> wrap( TShortObjectMap<T> map ) {
        return new TShortObjectMapDecorator<T>( map );
    }

    /**
     * Wrap the given map in a decorator that uses the standard {@link java.util.Map Map}
     * interface.
     *
     * @param map the <tt>TCharObjectMap</tt> to be wrapped
     * @return the wrapped map.
     */
    public static <T> Map<Character,T> wrap( TCharObjectMap<T> map ) {
        return new TCharObjectMapDecorator<T>( map );
    }


    /**
     * Wrap the given set in a decorator that uses the standard {@link java.util.Set Set}
     * interface.
     *
     * @param set the <tt>TDoubleSet</tt> to be wrapped
     * @return the wrapped set.
     */
    public static Set<Double> wrap( TDoubleSet set ) {
        return new TDoubleSetDecorator( set );
    }

    /**
     * Wrap the given set in a decorator that uses the standard {@link java.util.Set Set}
     * interface.
     *
     * @param set the <tt>TFloatSet</tt> to be wrapped
     * @return the wrapped set.
     */
    public static Set<Float> wrap( TFloatSet set ) {
        return new TFloatSetDecorator( set );
    }

    /**
     * Wrap the given set in a decorator that uses the standard {@link java.util.Set Set}
     * interface.
     *
     * @param set the <tt>TIntSet</tt> to be wrapped
     * @return the wrapped set.
     */
    public static Set<Integer> wrap( TIntSet set ) {
        return new TIntSetDecorator( set );
    }

    /**
     * Wrap the given set in a decorator that uses the standard {@link java.util.Set Set}
     * interface.
     *
     * @param set the <tt>TLongSet</tt> to be wrapped
     * @return the wrapped set.
     */
    public static Set<Long> wrap( TLongSet set ) {
        return new TLongSetDecorator( set );
    }

    /**
     * Wrap the given set in a decorator that uses the standard {@link java.util.Set Set}
     * interface.
     *
     * @param set the <tt>TByteSet</tt> to be wrapped
     * @return the wrapped set.
     */
    public static Set<Byte> wrap( TByteSet set ) {
        return new TByteSetDecorator( set );
    }

    /**
     * Wrap the given set in a decorator that uses the standard {@link java.util.Set Set}
     * interface.
     *
     * @param set the <tt>TShortSet</tt> to be wrapped
     * @return the wrapped set.
     */
    public static Set<Short> wrap( TShortSet set ) {
        return new TShortSetDecorator( set );
    }

    /**
     * Wrap the given set in a decorator that uses the standard {@link java.util.Set Set}
     * interface.
     *
     * @param set the <tt>TCharSet</tt> to be wrapped
     * @return the wrapped set.
     */
    public static Set<Character> wrap( TCharSet set ) {
        return new TCharSetDecorator( set );
    }


    /**
     * Wrap the given list in a decorator that uses the standard {@link java.util.List List}
     * interface.
     *
     * @param list	the <tt>TDoubleList</tt> to be wrapped
     * @return the wrapped list.
     */
    public static List<Double> wrap( TDoubleList list ) {
        return new TDoubleListDecorator( list );
    }

    /**
     * Wrap the given list in a decorator that uses the standard {@link java.util.List List}
     * interface.
     *
     * @param list	the <tt>TFloatList</tt> to be wrapped
     * @return the wrapped list.
     */
    public static List<Float> wrap( TFloatList list ) {
        return new TFloatListDecorator( list );
    }

    /**
     * Wrap the given list in a decorator that uses the standard {@link java.util.List List}
     * interface.
     *
     * @param list	the <tt>TIntList</tt> to be wrapped
     * @return the wrapped list.
     */
    public static List<Integer> wrap( TIntList list ) {
        return new TIntListDecorator( list );
    }

    /**
     * Wrap the given list in a decorator that uses the standard {@link java.util.List List}
     * interface.
     *
     * @param list	the <tt>TLongList</tt> to be wrapped
     * @return the wrapped list.
     */
    public static List<Long> wrap( TLongList list ) {
        return new TLongListDecorator( list );
    }

    /**
     * Wrap the given list in a decorator that uses the standard {@link java.util.List List}
     * interface.
     *
     * @param list	the <tt>TByteList</tt> to be wrapped
     * @return the wrapped list.
     */
    public static List<Byte> wrap( TByteList list ) {
        return new TByteListDecorator( list );
    }

    /**
     * Wrap the given list in a decorator that uses the standard {@link java.util.List List}
     * interface.
     *
     * @param list	the <tt>TShortList</tt> to be wrapped
     * @return the wrapped list.
     */
    public static List<Short> wrap( TShortList list ) {
        return new TShortListDecorator( list );
    }

    /**
     * Wrap the given list in a decorator that uses the standard {@link java.util.List List}
     * interface.
     *
     * @param list	the <tt>TCharList</tt> to be wrapped
     * @return the wrapped list.
     */
    public static List<Character> wrap( TCharList list ) {
        return new TCharListDecorator( list );
    }
}