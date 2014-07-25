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
 * -----------------
 * ClassFactory.java
 * -----------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ClassFactory.java,v 1.3 2005/11/14 10:59:58 mungady Exp $
 *
 * Changes (from 19-Feb-2003)
 * -------------------------
 * 19-Feb-2003 : Added standard header and Javadocs (DG);
 * 29-Apr-2003 : Distilled from the JFreeReport project and moved into JCommon
 *
 */

package org.jfree.xml.factory.objects;

import java.io.Serializable;
import java.util.Iterator;

import org.jfree.util.Configuration;

/**
 * A class factory.
 *
 * @author Thomas Morgner
 */
public interface ClassFactory extends Serializable {

    /**
     * Returns an object description for a class.
     *
     * @param c  the class.
     *
     * @return The object description.
     */
    public ObjectDescription getDescriptionForClass(Class c);

    /**
     * Returns an object description for the super class of a class.
     *
     * @param d  the class.
     * @param knownSuperClass the last known super class or null.
     *
     * @return The object description.
     */
    public ObjectDescription getSuperClassObjectDescription
        (Class d, ObjectDescription knownSuperClass);

    /**
     * Returns an iterator for the registered classes. This returns a list
     * of pre-registered classes known to this ClassFactory. A class may be able
     * to handle more than the registered classes.
     * <p>
     * This method exists to support query tools for UI design, do not rely on it
     * for day to day work.
     *
     * @return The iterator.
     */
    public Iterator getRegisteredClasses();

    /**
     * Configures this factory. The configuration contains several keys and
     * their defined values. The given reference to the configuration object
     * will remain valid until the report parsing or writing ends.
     * <p>
     * The configuration contents may change during the reporting.
     *
     * @param config the configuration, never null
     */
    public void configure(Configuration config);


    /**
     * Compares whether two object factories are equal. This method must be
     * implemented!
     *
     * @param o the other object.
     * @return true, if both object factories describe the same objects, false otherwise.
     */
    public boolean equals (Object o);

    /**
     * Computes the hashCode for this ClassFactory. As equals() must be implemented,
     * a corresponding hashCode() should be implemented as well.
     *
     * @return the hashcode.
     */
    public int hashCode();
}

