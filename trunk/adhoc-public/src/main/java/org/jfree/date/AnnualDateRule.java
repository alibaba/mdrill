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
 * -------------------
 * AnnualDateRule.java
 * -------------------
 * (C) Copyright 2000-2003, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: AnnualDateRule.java,v 1.4 2005/11/16 15:58:40 taqua Exp $
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.date.* (DG);
 * 12-Nov-2001 : Javadoc comments updated (DG);
 * 03-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.date;

/**
 * The base class for all 'annual' date rules: that is, rules for generating
 * one date for any given year.
 * <P>
 * One example is Easter Sunday (which can be calculated using published algorithms).
 *
 * @author David Gilbert
 */
public abstract class AnnualDateRule implements Cloneable {

    /**
     * Default constructor.
     */
    protected AnnualDateRule() {
    }

    /**
     * Returns the date for this rule, given the year.
     *
     * @param year  the year (1900 &lt;= year &lt;= 9999).
     *
     * @return the date for this rule, given the year.
     */
    public abstract SerialDate getDate(int year);

    /**
     * Returns a clone of the rule.
     * <P>
     * You should refer to the documentation of the clone() method in each
     * subclass for exact details.
     *
     * @return a clone of the rule.
     *
     * @throws CloneNotSupportedException if the rule is not clonable.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
