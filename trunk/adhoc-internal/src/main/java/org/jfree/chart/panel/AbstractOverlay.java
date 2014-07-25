/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
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
 * --------------------
 * AbstractOverlay.java
 * --------------------
 * (C) Copyright 2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 09-Apr-2009 : Version 1 (DG);
 *
 */

package org.jfree.chart.panel;

import javax.swing.event.EventListenerList;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.OverlayChangeEvent;
import org.jfree.chart.event.OverlayChangeListener;

/**
 * A base class for implementing overlays for a {@link ChartPanel}.
 *
 * @since 1.0.13
 */
public class AbstractOverlay {

    /** Storage for registered change listeners. */
    private transient EventListenerList changeListeners;

    /**
     * Default constructor.
     */
    public AbstractOverlay() {
        this.changeListeners = new EventListenerList();
    }

    /**
     * Registers an object for notification of changes to the overlay.
     *
     * @param listener  the listener (<code>null</code> not permitted).
     *
     * @see #removeChangeListener(OverlayChangeListener)
     */
    public void addChangeListener(OverlayChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Null 'listener' argument.");
        }
        this.changeListeners.add(OverlayChangeListener.class, listener);
    }

    /**
     * Deregisters an object for notification of changes to the overlay.
     *
     * @param listener  the listener (<code>null</code> not permitted)
     *
     * @see #addChangeListener(OverlayChangeListener)
     */
    public void removeChangeListener(OverlayChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Null 'listener' argument.");
        }
        this.changeListeners.remove(OverlayChangeListener.class, listener);
    }

    /**
     * Sends a default {@link ChartChangeEvent} to all registered listeners.
     * <P>
     * This method is for convenience only.
     */
    public void fireOverlayChanged() {
        OverlayChangeEvent event = new OverlayChangeEvent(this);
        notifyListeners(event);
    }

    /**
     * Sends a {@link ChartChangeEvent} to all registered listeners.
     *
     * @param event  information about the event that triggered the
     *               notification.
     */
    protected void notifyListeners(OverlayChangeEvent event) {
       Object[] listeners = this.changeListeners.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == OverlayChangeListener.class) {
                ((OverlayChangeListener) listeners[i + 1]).overlayChanged(
                        event);
            }
        }
    }

}

