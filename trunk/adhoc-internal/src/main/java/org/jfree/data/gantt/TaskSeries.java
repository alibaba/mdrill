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
 * ---------------
 * TaskSeries.java
 * ---------------
 * (C) Copyright 2002-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 06-Jun-2002 : Version 1 (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 24-Oct-2002 : Added methods to get TimeAllocation by task index (DG);
 * 10-Jan-2003 : Renamed GanttSeries --> TaskSeries (DG);
 * 30-Jul-2004 : Added equals() method (DG);
 * 09-May-2008 : Fixed cloning bug (DG);
 *
 */

package org.jfree.data.gantt;

import java.util.Collections;
import java.util.List;

import org.jfree.data.general.Series;
import org.jfree.util.ObjectUtilities;

/**
 * A series that contains zero, one or many {@link Task} objects.
 * <P>
 * This class is used as a building block for the {@link TaskSeriesCollection}
 * class that can be used to construct basic Gantt charts.
 */
public class TaskSeries extends Series {

    /** Storage for the tasks in the series. */
    private List tasks;

    /**
     * Constructs a new series with the specified name.
     *
     * @param name  the series name (<code>null</code> not permitted).
     */
    public TaskSeries(String name) {
        super(name);
        this.tasks = new java.util.ArrayList();
    }

    /**
     * Adds a task to the series and sends a
     * {@link org.jfree.data.general.SeriesChangeEvent} to all registered
     * listeners.
     *
     * @param task  the task (<code>null</code> not permitted).
     */
    public void add(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Null 'task' argument.");
        }
        this.tasks.add(task);
        fireSeriesChanged();
    }

    /**
     * Removes a task from the series and sends
     * a {@link org.jfree.data.general.SeriesChangeEvent}
     * to all registered listeners.
     *
     * @param task  the task.
     */
    public void remove(Task task) {
        this.tasks.remove(task);
        fireSeriesChanged();
    }

    /**
     * Removes all tasks from the series and sends
     * a {@link org.jfree.data.general.SeriesChangeEvent}
     * to all registered listeners.
     */
    public void removeAll() {
        this.tasks.clear();
        fireSeriesChanged();
    }

    /**
     * Returns the number of items in the series.
     *
     * @return The item count.
     */
    public int getItemCount() {
        return this.tasks.size();
    }

    /**
     * Returns a task from the series.
     *
     * @param index  the task index (zero-based).
     *
     * @return The task.
     */
    public Task get(int index) {
        return (Task) this.tasks.get(index);
    }

    /**
     * Returns the task in the series that has the specified description.
     *
     * @param description  the name (<code>null</code> not permitted).
     *
     * @return The task (possibly <code>null</code>).
     */
    public Task get(String description) {
        Task result = null;
        int count = this.tasks.size();
        for (int i = 0; i < count; i++) {
            Task t = (Task) this.tasks.get(i);
            if (t.getDescription().equals(description)) {
                result = t;
                break;
            }
        }
        return result;
    }

    /**
     * Returns an unmodifialble list of the tasks in the series.
     *
     * @return The tasks.
     */
    public List getTasks() {
        return Collections.unmodifiableList(this.tasks);
    }

    /**
     * Tests this object for equality with an arbitrary object.
     *
     * @param obj  the object to test against (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TaskSeries)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        TaskSeries that = (TaskSeries) obj;
        if (!this.tasks.equals(that.tasks)) {
            return false;
        }
        return true;
    }

    /**
     * Returns an independent copy of this series.
     *
     * @return A clone of the series.
     *
     * @throws CloneNotSupportedException if there is some problem cloning
     *     the dataset.
     */
    public Object clone() throws CloneNotSupportedException {
        TaskSeries clone = (TaskSeries) super.clone();
        clone.tasks = (List) ObjectUtilities.deepClone(this.tasks);
        return clone;
    }

}
