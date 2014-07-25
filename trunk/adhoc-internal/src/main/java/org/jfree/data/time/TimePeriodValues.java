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
 * ---------------------
 * TimePeriodValues.java
 * ---------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 22-Apr-2003 : Version 1 (DG);
 * 30-Jul-2003 : Added clone and equals methods while testing (DG);
 * 11-Mar-2005 : Fixed bug in bounds recalculation - see bug report 
 *               1161329 (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 03-Oct-2006 : Fixed NullPointerException in equals(), fire change event in 
 *               add() method, updated API docs (DG);
 * 07-Apr-2008 : Fixed bug with maxMiddleIndex in updateBounds() (DG);
 *
 */

package org.jfree.data.time;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jfree.data.general.Series;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesException;
import org.jfree.util.ObjectUtilities;

/**
 * A structure containing zero, one or many {@link TimePeriodValue} instances.  
 * The time periods can overlap, and are maintained in the order that they are 
 * added to the collection.
 * <p>
 * This is similar to the {@link TimeSeries} class, except that the time 
 * periods can have irregular lengths.
 */
public class TimePeriodValues extends Series implements Serializable {

    /** For serialization. */
    static final long serialVersionUID = -2210593619794989709L;
    
    /** Default value for the domain description. */
    protected static final String DEFAULT_DOMAIN_DESCRIPTION = "Time";

    /** Default value for the range description. */
    protected static final String DEFAULT_RANGE_DESCRIPTION = "Value";

    /** A description of the domain. */
    private String domain;

    /** A description of the range. */
    private String range;

    /** The list of data pairs in the series. */
    private List data;

    /** Index of the time period with the minimum start milliseconds. */
    private int minStartIndex = -1;
    
    /** Index of the time period with the maximum start milliseconds. */
    private int maxStartIndex = -1;
    
    /** Index of the time period with the minimum middle milliseconds. */
    private int minMiddleIndex = -1;
    
    /** Index of the time period with the maximum middle milliseconds. */
    private int maxMiddleIndex = -1;
    
    /** Index of the time period with the minimum end milliseconds. */
    private int minEndIndex = -1;
    
    /** Index of the time period with the maximum end milliseconds. */
    private int maxEndIndex = -1;

    /**
     * Creates a new (empty) collection of time period values.
     *
     * @param name  the name of the series (<code>null</code> not permitted).
     */
    public TimePeriodValues(String name) {
        this(name, DEFAULT_DOMAIN_DESCRIPTION, DEFAULT_RANGE_DESCRIPTION);
    }

    /**
     * Creates a new time series that contains no data.
     * <P>
     * Descriptions can be specified for the domain and range.  One situation
     * where this is helpful is when generating a chart for the time series -
     * axis labels can be taken from the domain and range description.
     *
     * @param name  the name of the series (<code>null</code> not permitted).
     * @param domain  the domain description.
     * @param range  the range description.
     */
    public TimePeriodValues(String name, String domain, String range) {
        super(name);
        this.domain = domain;
        this.range = range;
        this.data = new ArrayList();
    }

    /**
     * Returns the domain description.
     *
     * @return The domain description (possibly <code>null</code>).
     * 
     * @see #getRangeDescription()
     * @see #setDomainDescription(String)
     */
    public String getDomainDescription() {
        return this.domain;
    }

    /**
     * Sets the domain description and fires a property change event (with the
     * property name <code>Domain</code> if the description changes).
     *
     * @param description  the new description (<code>null</code> permitted).
     * 
     * @see #getDomainDescription()
     */
    public void setDomainDescription(String description) {
        String old = this.domain;
        this.domain = description;
        firePropertyChange("Domain", old, description);
    }

    /**
     * Returns the range description.
     *
     * @return The range description (possibly <code>null</code>).
     * 
     * @see #getDomainDescription()
     * @see #setRangeDescription(String)
     */
    public String getRangeDescription() {
        return this.range;
    }

    /**
     * Sets the range description and fires a property change event with the
     * name <code>Range</code>.
     *
     * @param description  the new description (<code>null</code> permitted).
     * 
     * @see #getRangeDescription()
     */
    public void setRangeDescription(String description) {
        String old = this.range;
        this.range = description;
        firePropertyChange("Range", old, description);
    }

    /**
     * Returns the number of items in the series.
     *
     * @return The item count.
     */
    public int getItemCount() {
        return this.data.size();
    }

    /**
     * Returns one data item for the series.
     *
     * @param index  the item index (in the range <code>0</code> to 
     *     <code>getItemCount() - 1</code>).
     *
     * @return One data item for the series.
     */
    public TimePeriodValue getDataItem(int index) {
        return (TimePeriodValue) this.data.get(index);
    }

    /**
     * Returns the time period at the specified index.
     *
     * @param index  the item index (in the range <code>0</code> to 
     *     <code>getItemCount() - 1</code>).
     *
     * @return The time period at the specified index.
     * 
     * @see #getDataItem(int)
     */
    public TimePeriod getTimePeriod(int index) {
        return getDataItem(index).getPeriod();
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index  the item index (in the range <code>0</code> to 
     *     <code>getItemCount() - 1</code>).
     *
     * @return The value at the specified index (possibly <code>null</code>).
     * 
     * @see #getDataItem(int)
     */
    public Number getValue(int index) {
        return getDataItem(index).getValue();
    }

    /**
     * Adds a data item to the series and sends a {@link SeriesChangeEvent} to
     * all registered listeners.
     *
     * @param item  the item (<code>null</code> not permitted).
     */
    public void add(TimePeriodValue item) {
        if (item == null) {
            throw new IllegalArgumentException("Null item not allowed.");
        }
        this.data.add(item);
        updateBounds(item.getPeriod(), this.data.size() - 1);
        fireSeriesChanged();
    }
    
    /**
     * Update the index values for the maximum and minimum bounds.
     * 
     * @param period  the time period.
     * @param index  the index of the time period.
     */
    private void updateBounds(TimePeriod period, int index) {
        
        long start = period.getStart().getTime();
        long end = period.getEnd().getTime();
        long middle = start + ((end - start) / 2);

        if (this.minStartIndex >= 0) {
            long minStart = getDataItem(this.minStartIndex).getPeriod()
                .getStart().getTime();
            if (start < minStart) {
                this.minStartIndex = index;           
            }
        }
        else {
            this.minStartIndex = index;
        }
        
        if (this.maxStartIndex >= 0) {
            long maxStart = getDataItem(this.maxStartIndex).getPeriod()
                .getStart().getTime();
            if (start > maxStart) {
                this.maxStartIndex = index;           
            }
        }
        else {
            this.maxStartIndex = index;
        }
        
        if (this.minMiddleIndex >= 0) {
            long s = getDataItem(this.minMiddleIndex).getPeriod().getStart()
                .getTime();
            long e = getDataItem(this.minMiddleIndex).getPeriod().getEnd()
                .getTime();
            long minMiddle = s + (e - s) / 2;
            if (middle < minMiddle) {
                this.minMiddleIndex = index;           
            }
        }
        else {
            this.minMiddleIndex = index;
        }
        
        if (this.maxMiddleIndex >= 0) {
            long s = getDataItem(this.maxMiddleIndex).getPeriod().getStart()
                .getTime();
            long e = getDataItem(this.maxMiddleIndex).getPeriod().getEnd()
                .getTime();
            long maxMiddle = s + (e - s) / 2;
            if (middle > maxMiddle) {
                this.maxMiddleIndex = index;           
            }
        }
        else {
            this.maxMiddleIndex = index;
        }
        
        if (this.minEndIndex >= 0) {
            long minEnd = getDataItem(this.minEndIndex).getPeriod().getEnd()
                .getTime();
            if (end < minEnd) {
                this.minEndIndex = index;           
            }
        }
        else {
            this.minEndIndex = index;
        }
       
        if (this.maxEndIndex >= 0) {
            long maxEnd = getDataItem(this.maxEndIndex).getPeriod().getEnd()
                .getTime();
            if (end > maxEnd) {
                this.maxEndIndex = index;           
            }
        }
        else {
            this.maxEndIndex = index;
        }
        
    }
    
    /**
     * Recalculates the bounds for the collection of items.
     */
    private void recalculateBounds() {
        this.minStartIndex = -1;
        this.minMiddleIndex = -1;
        this.minEndIndex = -1;
        this.maxStartIndex = -1;
        this.maxMiddleIndex = -1;
        this.maxEndIndex = -1;
        for (int i = 0; i < this.data.size(); i++) {
            TimePeriodValue tpv = (TimePeriodValue) this.data.get(i);
            updateBounds(tpv.getPeriod(), i);
        }
    }

    /**
     * Adds a new data item to the series and sends a {@link SeriesChangeEvent}
     * to all registered listeners.
     *
     * @param period  the time period (<code>null</code> not permitted).
     * @param value  the value.
     * 
     * @see #add(TimePeriod, Number)
     */
    public void add(TimePeriod period, double value) {
        TimePeriodValue item = new TimePeriodValue(period, value);
        add(item);
    }

    /**
     * Adds a new data item to the series and sends a {@link SeriesChangeEvent}
     * to all registered listeners.
     *
     * @param period  the time period (<code>null</code> not permitted).
     * @param value  the value (<code>null</code> permitted).
     */
    public void add(TimePeriod period, Number value) {
        TimePeriodValue item = new TimePeriodValue(period, value);
        add(item);
    }

    /**
     * Updates (changes) the value of a data item and sends a 
     * {@link SeriesChangeEvent} to all registered listeners.
     *
     * @param index  the index of the data item to update.
     * @param value  the new value (<code>null</code> not permitted).
     */
    public void update(int index, Number value) {
        TimePeriodValue item = getDataItem(index);
        item.setValue(value);
        fireSeriesChanged();
    }

    /**
     * Deletes data from start until end index (end inclusive) and sends a
     * {@link SeriesChangeEvent} to all registered listeners.
     *
     * @param start  the index of the first period to delete.
     * @param end  the index of the last period to delete.
     */
    public void delete(int start, int end) {
        for (int i = 0; i <= (end - start); i++) {
            this.data.remove(start);
        }
        recalculateBounds();
        fireSeriesChanged();
    }
    
    /**
     * Tests the series for equality with another object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TimePeriodValues)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        TimePeriodValues that = (TimePeriodValues) obj;
        if (!ObjectUtilities.equal(this.getDomainDescription(), 
                that.getDomainDescription())) {
            return false;
        }
        if (!ObjectUtilities.equal(this.getRangeDescription(), 
                that.getRangeDescription())) {
            return false;
        }
        int count = getItemCount();
        if (count != that.getItemCount()) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            if (!getDataItem(i).equals(that.getDataItem(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return The hashcode
     */
    public int hashCode() {
        int result;
        result = (this.domain != null ? this.domain.hashCode() : 0);
        result = 29 * result + (this.range != null ? this.range.hashCode() : 0);
        result = 29 * result + this.data.hashCode();
        result = 29 * result + this.minStartIndex;
        result = 29 * result + this.maxStartIndex;
        result = 29 * result + this.minMiddleIndex;
        result = 29 * result + this.maxMiddleIndex;
        result = 29 * result + this.minEndIndex;
        result = 29 * result + this.maxEndIndex;
        return result;
    }

    /**
     * Returns a clone of the collection.
     * <P>
     * Notes:
     * <ul>
     *   <li>no need to clone the domain and range descriptions, since String 
     *       object is immutable;</li>
     *   <li>we pass over to the more general method createCopy(start, end).
     *   </li>
     * </ul>
     *
     * @return A clone of the time series.
     * 
     * @throws CloneNotSupportedException if there is a cloning problem.
     */
    public Object clone() throws CloneNotSupportedException {
        Object clone = createCopy(0, getItemCount() - 1);
        return clone;
    }

    /**
     * Creates a new instance by copying a subset of the data in this 
     * collection.
     *
     * @param start  the index of the first item to copy.
     * @param end  the index of the last item to copy.
     *
     * @return A copy of a subset of the items.
     * 
     * @throws CloneNotSupportedException if there is a cloning problem.
     */
    public TimePeriodValues createCopy(int start, int end) 
        throws CloneNotSupportedException {

        TimePeriodValues copy = (TimePeriodValues) super.clone();

        copy.data = new ArrayList();
        if (this.data.size() > 0) {
            for (int index = start; index <= end; index++) {
                TimePeriodValue item = (TimePeriodValue) this.data.get(index);
                TimePeriodValue clone = (TimePeriodValue) item.clone();
                try {
                    copy.add(clone);
                }
                catch (SeriesException e) {
                    System.err.println("Failed to add cloned item.");
                }
            }
        }
        return copy;

    }
    
    /**
     * Returns the index of the time period with the minimum start milliseconds.
     * 
     * @return The index.
     */
    public int getMinStartIndex() {
        return this.minStartIndex;
    }
    
    /**
     * Returns the index of the time period with the maximum start milliseconds.
     * 
     * @return The index.
     */
    public int getMaxStartIndex() {
        return this.maxStartIndex;
    }

    /**
     * Returns the index of the time period with the minimum middle 
     * milliseconds.
     * 
     * @return The index.
     */
    public int getMinMiddleIndex() {
        return this.minMiddleIndex;
    }
    
    /**
     * Returns the index of the time period with the maximum middle 
     * milliseconds.
     * 
     * @return The index.
     */
    public int getMaxMiddleIndex() {
        return this.maxMiddleIndex;
    }

    /**
     * Returns the index of the time period with the minimum end milliseconds.
     * 
     * @return The index.
     */
    public int getMinEndIndex() {
        return this.minEndIndex;
    }
    
    /**
     * Returns the index of the time period with the maximum end milliseconds.
     * 
     * @return The index.
     */
    public int getMaxEndIndex() {
        return this.maxEndIndex;
    }

}
