/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
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
 * -----------------
 * ChartDeleter.java
 * -----------------
  * (C) Copyright 2002-2008, by Richard Atkinson and Contributors.
 *
 * Original Author:  Richard Atkinson;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 19-Aug-2002 : Version 1;
 * 17-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags all over JFreeChart sources (DG);
 *
 */

package org.jfree.chart.servlet;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

/**
 * Used for deleting charts from the temporary directory when the users session
 * expires.
 */
public class ChartDeleter implements HttpSessionBindingListener, Serializable {

    /** The chart names. */
    private List chartNames = new java.util.ArrayList();

    /**
     * Blank constructor.
     */
    public ChartDeleter() {
        super();
    }

    /**
     * Add a chart to be deleted when the session expires
     *
     * @param filename  the name of the chart in the temporary directory to be
     *                  deleted.
     */
    public void addChart(String filename) {
        this.chartNames.add(filename);
    }

    /**
     * Checks to see if a chart is in the list of charts to be deleted
     *
     * @param filename  the name of the chart in the temporary directory.
     *
     * @return A boolean value indicating whether the chart is present in the
     *         list.
     */
    public boolean isChartAvailable(String filename) {
        return (this.chartNames.contains(filename));
    }

    /**
     * Binding this object to the session has no additional effects.
     *
     * @param event  the session bind event.
     */
    public void valueBound(HttpSessionBindingEvent event) {
        return;
    }

    /**
     * When this object is unbound from the session (including upon session
     * expiry) the files that have been added to the ArrayList are iterated
     * and deleted.
     *
     * @param event  the session unbind event.
     */
    public void valueUnbound(HttpSessionBindingEvent event) {

        Iterator iter = this.chartNames.listIterator();
        while (iter.hasNext()) {
            String filename = (String) iter.next();
            File file = new File(
                System.getProperty("java.io.tmpdir"), filename
            );
            if (file.exists()) {
                file.delete();
            }
        }
        return;

    }

}
