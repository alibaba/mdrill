/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
 * 
 * Project Info:  http://www.jfree.org/jcommon/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 * 
 * ---------------------
 * ReaderWriterLock.java
 * ---------------------
 *
 * $Id: ReaderWriterLock.java,v 1.3 2005/10/18 13:18:34 mungady Exp $
 *
 * Changes
 * -------
 * 29-Jan-2003 : Added standard header (DG);
 *
 */

package org.jfree.threads;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A reader-writer lock from "Java Threads" by Scott Oak and Henry Wong.
 *
 * @author Scott Oak and Henry Wong
 */
public class ReaderWriterLock {

    /**
     * A node for the waiting list.
     *
     * @author Scott Oak and Henry Wong
     */
    private static class ReaderWriterNode {

        /** A reader. */
        protected static final int READER = 0;

        /** A writer. */
        protected static final int WRITER = 1;

        /** The thread. */
        protected Thread t;

        /** The state. */
        protected int state;

        /** The number of acquires.*/
        protected int nAcquires;

        /**
         * Creates a new node.
         *
         * @param t  the thread.
         * @param state  the state.
         */
        private ReaderWriterNode(final Thread t, final int state) {
            this.t = t;
            this.state = state;
            this.nAcquires = 0;
        }

    }

    /** The waiting threads. */
    private ArrayList waiters;

    /**
     * Default constructor.
     */
    public ReaderWriterLock() {
        this.waiters = new ArrayList();
    }

    /**
     * Grab the read lock.
     */
    public synchronized void lockRead() {
        final ReaderWriterNode node;
        final Thread me = Thread.currentThread();
        final int index = getIndex(me);
        if (index == -1) {
            node = new ReaderWriterNode(me, ReaderWriterNode.READER);
            this.waiters.add(node);
        }
        else {
            node = (ReaderWriterNode) this.waiters.get(index);
        }
        while (getIndex(me) > firstWriter()) {
            try {
                wait();
            }
            catch (Exception e) {
                System.err.println("ReaderWriterLock.lockRead(): exception.");
                System.err.print(e.getMessage());
            }
        }
        node.nAcquires++;
    }

    /**
     * Grab the write lock.
     */
    public synchronized void lockWrite() {
        final ReaderWriterNode node;
        final Thread me = Thread.currentThread();
        final int index = getIndex(me);
        if (index == -1) {
            node = new ReaderWriterNode(me, ReaderWriterNode.WRITER);
            this.waiters.add(node);
        }
        else {
            node = (ReaderWriterNode) this.waiters.get(index);
            if (node.state == ReaderWriterNode.READER) {
                throw new IllegalArgumentException("Upgrade lock");
            }
            node.state = ReaderWriterNode.WRITER;
        }
        while (getIndex(me) != 0) {
            try {
                wait();
            }
            catch (Exception e) {
                System.err.println("ReaderWriterLock.lockWrite(): exception.");
                System.err.print(e.getMessage());
            }
        }
        node.nAcquires++;
    }

    /**
     * Unlock.
     */
    public synchronized void unlock() {

        final ReaderWriterNode node;
        final Thread me = Thread.currentThread();
        final int index = getIndex(me);
        if (index > firstWriter()) {
            throw new IllegalArgumentException("Lock not held");
        }
        node = (ReaderWriterNode) this.waiters.get(index);
        node.nAcquires--;
        if (node.nAcquires == 0) {
            this.waiters.remove(index);
        }
        notifyAll();
    }

    /**
     * Returns the index of the first waiting writer.
     *
     * @return The index.
     */
    private int firstWriter() {
        final Iterator e = this.waiters.iterator();
        int index = 0;
        while (e.hasNext()) {
            final ReaderWriterNode node = (ReaderWriterNode) e.next();
            if (node.state == ReaderWriterNode.WRITER) {
                return index;
            }
            index += 1;
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Returns the index of a thread.
     *
     * @param t  the thread.
     *
     * @return The index.
     */
    private int getIndex(final Thread t) {
        final Iterator e = this.waiters.iterator();
        int index = 0;
        while (e.hasNext()) {
            final ReaderWriterNode node = (ReaderWriterNode) e.next();
            if (node.t == t) {
                return index;
            }
            index += 1;
        }
        return -1;
    }

}
