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
 * -----------------------------
 * StandardEntityCollection.java
 * -----------------------------
 * (C) Copyright 2001-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 23-May-2002 : Version 1 (DG);
 * 26-Jun-2002 : Added iterator() method (DG);
 * 03-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 19-May-2004 : Implemented Serializable (DG);
 * 29-Sep-2004 : Renamed addEntity() --> add() and addEntities()
 *               --> addAll() (DG);
 * 19-Jan-2005 : Changed storage from Collection --> List (DG);
 * 20-May-2005 : Fixed bug 1113521 - inefficiency in getEntity() method (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 01-Dec-2006 : Implemented PublicCloneable and fixed clone() method (DG);
 *
 */

package org.jfree.chart.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A standard implementation of the {@link EntityCollection} interface.
 */
public class StandardEntityCollection implements EntityCollection,
        Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 5384773031184897047L;

    /** Storage for the entities. */
    private List entities;

    /**
     * Constructs a new entity collection (initially empty).
     */
    public StandardEntityCollection() {
        this.entities = new java.util.ArrayList();
    }

    /**
     * Returns the number of entities in the collection.
     *
     * @return The entity count.
     */
    public int getEntityCount() {
        return this.entities.size();
    }

    /**
     * Returns a chart entity from the collection.
     *
     * @param index  the entity index.
     *
     * @return The entity.
     *
     * @see #add(ChartEntity)
     */
    public ChartEntity getEntity(int index) {
        return (ChartEntity) this.entities.get(index);
    }

    /**
     * Clears all the entities from the collection.
     */
    public void clear() {
        this.entities.clear();
    }

    /**
     * Adds an entity to the collection.
     *
     * @param entity  the entity (<code>null</code> not permitted).
     */
    public void add(ChartEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Null 'entity' argument.");
        }
        this.entities.add(entity);
    }

    /**
     * Adds all the entities from the specified collection.
     *
     * @param collection  the collection of entities (<code>null</code> not
     *     permitted).
     */
    public void addAll(EntityCollection collection) {
        this.entities.addAll(collection.getEntities());
    }

    /**
     * Returns the last entity in the list with an area that encloses the
     * specified coordinates, or <code>null</code> if there is no such entity.
     *
     * @param x  the x coordinate.
     * @param y  the y coordinate.
     *
     * @return The entity (possibly <code>null</code>).
     */
    public ChartEntity getEntity(double x, double y) {
        int entityCount = this.entities.size();
        for (int i = entityCount - 1; i >= 0; i--) {
            ChartEntity entity = (ChartEntity) this.entities.get(i);
            if (entity.getArea().contains(x, y)) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Returns the entities in an unmodifiable collection.
     *
     * @return The entities.
     */
    public Collection getEntities() {
        return Collections.unmodifiableCollection(this.entities);
    }

    /**
     * Returns an iterator for the entities in the collection.
     *
     * @return An iterator.
     */
    public Iterator iterator() {
        return this.entities.iterator();
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
        if (obj instanceof StandardEntityCollection) {
            StandardEntityCollection that = (StandardEntityCollection) obj;
            return ObjectUtilities.equal(this.entities, that.entities);
        }
        return false;
    }

    /**
     * Returns a clone of this entity collection.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if the object cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        StandardEntityCollection clone
                = (StandardEntityCollection) super.clone();
        clone.entities = new java.util.ArrayList(this.entities.size());
        for (int i = 0; i < this.entities.size(); i++) {
            ChartEntity entity = (ChartEntity) this.entities.get(i);
            clone.entities.add(entity.clone());
        }
        return clone;
    }

}
