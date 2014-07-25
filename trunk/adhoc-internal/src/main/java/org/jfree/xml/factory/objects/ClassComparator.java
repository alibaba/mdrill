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
 * --------------------
 * ClassComparator.java
 * --------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner (taquera@sherito.org);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: ClassComparator.java,v 1.5 2005/11/08 14:22:04 mungady Exp $
 *
 * Changes
 * -------
 * 02-May-2003 : Initial version
 */
package org.jfree.xml.factory.objects;

import java.io.Serializable;
import java.util.Comparator;

/**
 * The class comparator can be used to compare and sort classes and their
 * superclasses. The comparator is not able to compare classes which have
 * no relation...
 *
 * @author Thomas Morgner
 * @deprecated Moved to org.jfree.util
 */
public class ClassComparator implements Comparator, Serializable {

    /**
     * Defaultconstructor.
     */
    public ClassComparator() {
        super();
    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     * <P>
     * Note: throws ClassCastException if the arguments' types prevent them from
     * being compared by this Comparator.
     * And IllegalArgumentException if the classes share no relation.
     *
     * The implementor must ensure that <tt>sgn(compare(x, y)) ==
     * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>compare(x, y)</tt> must throw an exception if and only
     * if <tt>compare(y, x)</tt> throws an exception.)<p>
     *
     * The implementor must also ensure that the relation is transitive:
     * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
     * <tt>compare(x, z)&gt;0</tt>.<p>
     *
     * Finally, the implementer must ensure that <tt>compare(x, y)==0</tt>
     * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
     * <tt>z</tt>.<p>
     *
     * It is generally the case, but <i>not</i> strictly required that
     * <tt>(compare(x, y)==0) == (x.equals(y))</tt>.  Generally speaking,
     * any comparator that violates this condition should clearly indicate
     * this fact.  The recommended language is "Note: this comparator
     * imposes orderings that are inconsistent with equals."
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
     */
    public int compare(final Object o1, final Object o2) {
        final Class c1 = (Class) o1;
        final Class c2 = (Class) o2;
        if (c1.equals(o2)) {
            return 0;
        }
        if (c1.isAssignableFrom(c2)) {
            return -1;
        }
        else {
            if (!c2.isAssignableFrom(c2)) {
                throw new IllegalArgumentException("The classes share no relation");
            }
            return 1;
        }
    }

    /**
     * Checks, whether the given classes are comparable. This method will
     * return true, if one of the classes is assignable from the other class.
     *
     * @param c1 the first class to compare
     * @param c2 the second class to compare
     * @return true, if the classes share a direct relation, false otherwise.
     */
    public boolean isComparable(final Class c1, final Class c2) {
        return (c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1));
    }
}
