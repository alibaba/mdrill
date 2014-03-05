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


package gnu.trove.list.linked;

import gnu.trove.list.TLinkable;
import gnu.trove.procedure.TObjectProcedure;

import java.io.*;
import java.util.AbstractSequentialList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.lang.reflect.Array;



/**
 * <p>A LinkedList implementation which holds instances of type
 * <tt>TLinkable</tt>.
 * <p/>
 * Using this implementation allows you to get java.util.LinkedList
 * behavior (a doubly linked list, with Iterators that support insert
 * and delete operations) without incurring the overhead of creating
 * <tt>Node</tt> wrapper objects for every element in your list.
 * <p/>
 * The requirement to achieve this time/space gain is that the
 * Objects stored in the List implement the <tt>TLinkable</tt>
 * interface.
 * <p/>
 * The limitations are: <ul>
 * <li>the same object cannot be put into more than one list at the same time.
 * <li>the same object cannot be put into the same list more than once at the same time.
 * <li>objects must only be removed from list they are in.  That is,
 * if you have an object A and lists l1 and l2, you must ensure that
 * you invoke List.remove(A) on the correct list.
 * <li> It is also forbidden to invoke List.remove() with an unaffiliated
 * TLinkable (one that belongs to no list): this will destroy the list
 * you invoke it on.
 * </ul>
 *
 * @author Eric D. Friedman
 * @author Rob Eden
 * @author Jeff Randall
 * @version $Id: TLinkedList.java,v 1.1.2.3 2010/09/27 17:23:07 robeden Exp $
 * @see gnu.trove.list.TLinkable
 */


public class TLinkedList<T extends TLinkable<T>> extends AbstractSequentialList<T>
        implements Externalizable {

    static final long serialVersionUID = 1L;


    /** the head of the list */
    protected T _head;
    /** the tail of the list */
    protected T _tail;
    /** the number of elements in the list */
    protected int _size = 0;


    /** Creates a new <code>TLinkedList</code> instance. */
    public TLinkedList() {
        super();
    }


    /**
     * Returns an iterator positioned at <tt>index</tt>.  Assuming
     * that the list has a value at that index, calling next() will
     * retrieve and advance the iterator.  Assuming that there is a
     * value before <tt>index</tt> in the list, calling previous()
     * will retrieve it (the value at index - 1) and move the iterator
     * to that position.  So, iterating from front to back starts at
     * 0; iterating from back to front starts at <tt>size()</tt>.
     *
     * @param index an <code>int</code> value
     * @return a <code>ListIterator</code> value
     */
    public ListIterator<T> listIterator( int index ) {
        return new IteratorImpl( index );
    }


    /**
     * Returns the number of elements in the list.
     *
     * @return an <code>int</code> value
     */
    public int size() {
        return _size;
    }


    /**
     * Inserts <tt>linkable</tt> at index <tt>index</tt> in the list.
     * All values > index are shifted over one position to accommodate
     * the new addition.
     *
     * @param index    an <code>int</code> value
     * @param linkable an object of type TLinkable
     */
    public void add( int index, T linkable ) {
        if ( index < 0 || index > size() ) {
            throw new IndexOutOfBoundsException( "index:" + index );
        }
        insert( index, linkable );
    }


    /**
     * Appends <tt>linkable</tt> to the end of the list.
     *
     * @param linkable an object of type TLinkable
     * @return always true
     */
    public boolean add( T linkable ) {
        insert( _size, linkable );
        return true;
    }


    /**
     * Inserts <tt>linkable</tt> at the head of the list.
     *
     * @param linkable an object of type TLinkable
     */
    public void addFirst( T linkable ) {
        insert( 0, linkable );
    }


    /**
     * Adds <tt>linkable</tt> to the end of the list.
     *
     * @param linkable an object of type TLinkable
     */
    public void addLast( T linkable ) {
        insert( size(), linkable );
    }


    /** Empties the list. */
    public void clear() {
        if ( null != _head ) {
            for ( TLinkable<T> link = _head.getNext();
                  link != null;
                  link = link.getNext() ) {
                TLinkable<T> prev = link.getPrevious();
                prev.setNext( null );
                link.setPrevious( null );
            }
            _head = _tail = null;
        }
        _size = 0;
    }


    /**
     * Copies the list's contents into a native array.  This will be a
     * shallow copy: the Tlinkable instances in the Object[] array
     * have links to one another: changing those will put this list
     * into an unpredictable state.  Holding a reference to one
     * element in the list will prevent the others from being garbage
     * collected unless you clear the next/previous links.  <b>Caveat
     * programmer!</b>
     *
     * @return an <code>Object[]</code> value
     */
    public Object[] toArray() {
        Object[] o = new Object[_size];
        int i = 0;
        for ( TLinkable link = _head; link != null; link = link.getNext() ) {
            o[i++] = link;
        }
        return o;
    }


    /**
     * Copies the list to a native array, destroying the next/previous
     * links as the copy is made.  This list will be emptied after the
     * copy (as if clear() had been invoked).  The Object[] array
     * returned will contain TLinkables that do <b>not</b> hold
     * references to one another and so are less likely to be the
     * cause of memory leaks.
     *
     * @return an <code>Object[]</code> value
     */
    public Object[] toUnlinkedArray() {
        Object[] o = new Object[_size];
        int i = 0;
        for ( TLinkable<T> link = _head, tmp; link != null; i++ ) {
            o[i] = link;
            tmp = link;
            link = link.getNext();
            tmp.setNext( null ); // clear the links
            tmp.setPrevious( null );
        }
        _size = 0;              // clear the list
        _head = _tail = null;
        return o;
    }


    /**
     * Returns a typed array of the objects in the set.
     *
     * @param a an <code>Object[]</code> value
     * @return an <code>Object[]</code> value
     */
    @SuppressWarnings({"unchecked"})
    public T[] toUnlinkedArray( T[] a ) {
        int size = size();
        if ( a.length < size ) {
            a = (T[]) Array.newInstance( a.getClass().getComponentType(), size );
        }

        int i = 0;
        for ( T link = _head, tmp; link != null; i++ ) {
            a[i] = link;
            tmp = link;
            link = link.getNext();
            tmp.setNext( null ); // clear the links
            tmp.setPrevious( null );
        }
        _size = 0;              // clear the list
        _head = _tail = null;
        return a;
    }


    /**
     * A linear search for <tt>o</tt> in the list.
     *
     * @param o an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean contains( Object o ) {
        for ( TLinkable<T> link = _head; link != null; link = link.getNext() ) {
            if ( o.equals( link ) ) {
                return true;
            }
        }
        return false;
    }


    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"unchecked"})
    public T get( int index ) {
        // Blow out for bogus values
        if ( index < 0 || index >= _size ) {
            throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + _size );
        }

        // Determine if it's better to get there from the front or the back
        if ( index > ( _size >> 1 ) ) {
            int position = _size - 1;
            T node = _tail;

            while ( position > index ) {
                node = node.getPrevious();
                position--;
            }

            return node;
        } else {
            int position = 0;
            T node = _head;

            while ( position < index ) {
                node = node.getNext();
                position++;
            }

            return node;
        }
    }


    /**
     * Returns the head of the list
     *
     * @return an <code>Object</code> value
     */
    public T getFirst() {
        return _head;
    }


    /**
     * Returns the tail of the list.
     *
     * @return an <code>Object</code> value
     */
    public T getLast() {
        return _tail;
    }


    /**
     * Return the node following the given node. This method exists for two reasons:
     * <ol>
     * <li>It's really not recommended that the methods implemented by TLinkable be
     * called directly since they're used internally by this class.</li>
     * <li>This solves problems arising from generics when working with the linked
     * objects directly.</li>
     * </ol>
     * <p/>
     * NOTE: this should only be used with nodes contained in the list. The results are
     * undefined with anything else.
     *
     * @param current The current node
     * @return the node after the current node
     */
    @SuppressWarnings({"unchecked"})
    public T getNext( T current ) {
        return current.getNext();
    }


    /**
     * Return the node preceding the given node. This method exists for two reasons:
     * <ol>
     * <li>It's really not recommended that the methods implemented by TLinkable be
     * called directly since they're used internally by this class.</li>
     * <li>This solves problems arising from generics when working with the linked
     * objects directly.</li>
     * </ol>
     * <p/>
     * NOTE: this should only be used with nodes contained in the list. The results are
     * undefined with anything else.
     *
     * @param current The current node
     * @return the node after the current node
     */
    @SuppressWarnings({"unchecked"})
    public T getPrevious( T current ) {
        return current.getPrevious();
    }


    /**
     * Remove and return the first element in the list.
     *
     * @return an <code>Object</code> value
     */
    @SuppressWarnings({"unchecked"})
    public T removeFirst() {
        T o = _head;

        if ( o == null ) {
            return null;
        }

        T n = o.getNext();
        o.setNext( null );

        if ( null != n ) {
            n.setPrevious( null );
        }

        _head = n;
        if ( --_size == 0 ) {
            _tail = null;
        }
        return o;
    }


    /**
     * Remove and return the last element in the list.
     *
     * @return an <code>Object</code> value
     */
    @SuppressWarnings({"unchecked"})
    public T removeLast() {
        T o = _tail;

        if ( o == null ) {
            return null;
        }

        T prev = o.getPrevious();
        o.setPrevious( null );

        if ( null != prev ) {
            prev.setNext( null );
        }
        _tail = prev;
        if ( --_size == 0 ) {
            _head = null;
        }
        return o;
    }


    /**
     * Implementation of index-based list insertions.
     *
     * @param index    an <code>int</code> value
     * @param linkable an object of type TLinkable
     */
    @SuppressWarnings({"unchecked"})
    protected void insert( int index, T linkable ) {

        if ( _size == 0 ) {
            _head = _tail = linkable; // first insertion
        } else if ( index == 0 ) {
            linkable.setNext( _head ); // insert at front
            _head.setPrevious( linkable );
            _head = linkable;
        } else if ( index == _size ) { // insert at back
            _tail.setNext( linkable );
            linkable.setPrevious( _tail );
            _tail = linkable;
        } else {
            T node = get( index );

            T before = node.getPrevious();
            if ( before != null ) {
                before.setNext( linkable );
            }

            linkable.setPrevious( before );
            linkable.setNext( node );
            node.setPrevious( linkable );
        }
        _size++;
    }


    /**
     * Removes the specified element from the list.  Note that
     * it is the caller's responsibility to ensure that the
     * element does, in fact, belong to this list and not another
     * instance of TLinkedList.
     *
     * @param o a TLinkable element already inserted in this list.
     * @return true if the element was a TLinkable and removed
     */
    @SuppressWarnings({"unchecked"})
    public boolean remove( Object o ) {
        if ( o instanceof TLinkable ) {
            T p, n;
            TLinkable<T> link = (TLinkable<T>) o;

            p = link.getPrevious();
            n = link.getNext();

            if ( n == null && p == null ) { // emptying the list
                // It's possible this object is not something that's in the list. So,
                // make sure it's the head if it doesn't point to anything. This solves
                // problems caused by removing something multiple times.
                if ( o != _head ) {
                    return false;
                }

                _head = _tail = null;
            } else if ( n == null ) { // this is the tail
                // make previous the new tail
                link.setPrevious( null );
                p.setNext( null );
                _tail = p;
            } else if ( p == null ) { // this is the head
                // make next the new head
                link.setNext( null );
                n.setPrevious( null );
                _head = n;
            } else {            // somewhere in the middle
                p.setNext( n );
                n.setPrevious( p );
                link.setNext( null );
                link.setPrevious( null );
            }

            _size--;            // reduce size of list
            return true;
        } else {
            return false;
        }
    }


    /**
     * Inserts newElement into the list immediately before current.
     * All elements to the right of and including current are shifted
     * over.
     *
     * @param current    a <code>TLinkable</code> value currently in the list.
     * @param newElement a <code>TLinkable</code> value to be added to
     *                   the list.
     */
    public void addBefore( T current, T newElement ) {
        if ( current == _head ) {
            addFirst( newElement );
        } else if ( current == null ) {
            addLast( newElement );
        } else {
            T p = current.getPrevious();
            newElement.setNext( current );
            p.setNext( newElement );
            newElement.setPrevious( p );
            current.setPrevious( newElement );
            _size++;
        }
    }


    /**
     * Inserts newElement into the list immediately after current.
     * All elements to the left of and including current are shifted
     * over.
     *
     * @param current    a <code>TLinkable</code> value currently in the list.
     * @param newElement a <code>TLinkable</code> value to be added to
     *                   the list.
     */
    public void addAfter( T current, T newElement ) {
        if ( current == _tail ) {
            addLast( newElement );
        } else if ( current == null ) {
            addFirst( newElement );
        } else {
            T n = current.getNext();
            newElement.setPrevious( current );
            newElement.setNext( n );
            current.setNext( newElement );
            n.setPrevious( newElement );
            _size++;
        }
    }


    /**
     * Executes <tt>procedure</tt> for each entry in the list.
     *
     * @param procedure a <code>TObjectProcedure</code> value
     * @return false if the loop over the values terminated because
     *         the procedure returned false for some value.
     */
    @SuppressWarnings({"unchecked"})
    public boolean forEachValue( TObjectProcedure<T> procedure ) {
        T node = _head;
        while ( node != null ) {
            boolean keep_going = procedure.execute( node );
            if ( !keep_going ) {
                return false;
            }

            node = node.getNext();
        }

        return true;
    }


    public void writeExternal( ObjectOutput out ) throws IOException {
        // VERSION
        out.writeByte( 0 );

        // NUMBER OF ENTRIES
        out.writeInt( _size );

        // HEAD
        out.writeObject( _head );

        // TAIL
        out.writeObject( _tail );
    }


    @SuppressWarnings({"unchecked"})
    public void readExternal( ObjectInput in )
            throws IOException, ClassNotFoundException {

        // VERSION
        in.readByte();

        // NUMBER OF ENTRIED
        _size = in.readInt();

        // HEAD
        _head = (T) in.readObject();

        // TAIL
        _tail = (T) in.readObject();
    }


    /** A ListIterator that supports additions and deletions. */
    protected final class IteratorImpl implements ListIterator<T> {

        private int _nextIndex = 0;
        private T _next;
        private T _lastReturned;


        /**
         * Creates a new <code>Iterator</code> instance positioned at
         * <tt>index</tt>.
         *
         * @param position an <code>int</code> value
         */
        @SuppressWarnings({"unchecked"})
        IteratorImpl( int position ) {
            if ( position < 0 || position > _size ) {
                throw new IndexOutOfBoundsException();
            }

            _nextIndex = position;
            if ( position == 0 ) {
                _next = _head;
            } else if ( position == _size ) {
                _next = null;
            } else if ( position < ( _size >> 1 ) ) {
                int pos = 0;
                for ( _next = _head; pos < position; pos++ ) {
                    _next = _next.getNext();
                }
            } else {
                int pos = _size - 1;
                for ( _next = _tail; pos > position; pos-- ) {
                    _next = _next.getPrevious();
                }
            }
        }


        /**
         * Insert <tt>linkable</tt> at the current position of the iterator.
         * Calling next() after add() will return the added object.
         *
         * @param linkable an object of type TLinkable
         */
        public final void add( T linkable ) {
            _lastReturned = null;
            _nextIndex++;

            if ( _size == 0 ) {
                TLinkedList.this.add( linkable );
            } else {
                TLinkedList.this.addBefore( _next, linkable );
            }
        }


        /**
         * True if a call to next() will return an object.
         *
         * @return a <code>boolean</code> value
         */
        public final boolean hasNext() {
            return _nextIndex != _size;
        }


        /**
         * True if a call to previous() will return a value.
         *
         * @return a <code>boolean</code> value
         */
        public final boolean hasPrevious() {
            return _nextIndex != 0;
        }


        /**
         * Returns the value at the Iterator's index and advances the
         * iterator.
         *
         * @return an <code>Object</code> value
         * @throws NoSuchElementException if there is no next element
         */
        @SuppressWarnings({"unchecked"})
        public final T next() {
            if ( _nextIndex == _size ) {
                throw new NoSuchElementException();
            }

            _lastReturned = _next;
            _next = _next.getNext();
            _nextIndex++;
            return _lastReturned;
        }


        /**
         * returns the index of the next node in the list (the
         * one that would be returned by a call to next()).
         *
         * @return an <code>int</code> value
         */
        public final int nextIndex() {
            return _nextIndex;
        }


        /**
         * Returns the value before the Iterator's index and moves the
         * iterator back one index.
         *
         * @return an <code>Object</code> value
         * @throws NoSuchElementException if there is no previous element.
         */
        @SuppressWarnings({"unchecked"})
        public final T previous() {
            if ( _nextIndex == 0 ) {
                throw new NoSuchElementException();
            }

            if ( _nextIndex == _size ) {
                _lastReturned = _next = _tail;
            } else {
                _lastReturned = _next = _next.getPrevious();
            }

            _nextIndex--;
            return _lastReturned;
        }


        /**
         * Returns the previous element's index.
         *
         * @return an <code>int</code> value
         */
        public final int previousIndex() {
            return _nextIndex - 1;
        }


        /**
         * Removes the current element in the list and shrinks its
         * size accordingly.
         *
         * @throws IllegalStateException neither next nor previous
         *                               have been invoked, or remove or add have been invoked after
         *                               the last invocation of next or previous.
         */
        @SuppressWarnings({"unchecked"})
        public final void remove() {
            if ( _lastReturned == null ) {
                throw new IllegalStateException( "must invoke next or previous before invoking remove" );
            }

            if ( _lastReturned != _next ) {
                _nextIndex--;
            }
            _next = _lastReturned.getNext();
            TLinkedList.this.remove( _lastReturned );
            _lastReturned = null;
        }


        /**
         * Replaces the current element in the list with
         * <tt>linkable</tt>
         *
         * @param linkable an object of type TLinkable
         */
        public final void set( T linkable ) {
            if ( _lastReturned == null ) {
                throw new IllegalStateException();
            }

            swap( _lastReturned, linkable );
            _lastReturned = linkable;
        }


        /**
         * Replace from with to in the list.
         *
         * @param from a <code>TLinkable</code> value
         * @param to   a <code>TLinkable</code> value
         */
        private void swap( T from, T to ) {
	        T from_p = from.getPrevious();
	        T from_n = from.getNext();

	        T to_p = to.getPrevious();
	        T to_n = to.getNext();

	        // NOTE: 'to' cannot be null at this point
	        if ( from_n == to ) {
		        if ( from_p != null ) from_p.setNext( to );
		        to.setPrevious( from_p );
		        to.setNext( from );
		        from.setPrevious( to );
		        from.setNext( to_n );
		        if ( to_n != null ) to_n.setPrevious( from );
	        }
	        // NOTE: 'from' cannot be null at this point
	        else if ( to_n == from ) {
		        if ( to_p != null ) to_p.setNext( to );
		        to.setPrevious( from );
		        to.setNext( from_n );
		        from.setPrevious( to_p );
		        from.setNext( to );
		        if ( from_n != null ) from_n.setPrevious( to );
	        }
	        else {
				from.setNext( to_n );
				from.setPrevious( to_p );
				if ( to_p != null ) to_p.setNext( from );
				if ( to_n != null ) to_n.setPrevious( from );

				to.setNext( from_n );
				to.setPrevious( from_p );
				if ( from_p != null ) from_p.setNext( to );
				if ( from_n != null ) from_n.setPrevious( to );
	        }

	        if ( _head == from ) _head = to;
	        else if ( _head == to ) _head = from;

	        if ( _tail == from ) _tail = to;
	        else if ( _tail == to ) _tail = from;

	        if ( _lastReturned == from ) _lastReturned = to;
	        else if ( _lastReturned == to ) _lastReturned = from;

	        if ( _next == from ) _next = to;
	        else if ( _next == to ) _next = from;
        }
    }
} // TLinkedList
