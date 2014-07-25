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
 * ----------------
 * BlockResult.java
 * ----------------
 * (C) Copyright 2005-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 19-Apr-2005 : Version 1 (DG);
 *
 */

package org.jfree.chart.block;

import org.jfree.chart.entity.EntityCollection;

/**
 * Used to return results from the draw() method in the {@link Block}
 * class.
 */
public class BlockResult implements EntityBlockResult {

    /** The entities from the block. */
    private EntityCollection entities;

    /**
     * Creates a new result instance.
     */
    public BlockResult() {
        this.entities = null;
    }

    /**
     * Returns the collection of entities from the block.
     *
     * @return The entities.
     */
    public EntityCollection getEntityCollection() {
        return this.entities;
    }

    /**
     * Sets the entities for the block.
     *
     * @param entities  the entities.
     */
    public void setEntityCollection(EntityCollection entities) {
        this.entities = entities;
    }

}
