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
 * --------------------------
 * DateObjectDescription.java
 * --------------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: DateObjectDescription.java,v 1.4 2005/11/14 11:01:31 mungady Exp $
 *
 * Changes (from 19-Feb-2003)
 * -------------------------
 * 19-Feb-2003 : Added standard header and Javadocs (DG);
 * 29-Apr-2003 : Distilled from the JFreeReport project and moved into JCommon
 *
 */

package org.jfree.xml.factory.objects;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * An object-description for a <code>Date</code> object.
 *
 * @author Thomas Morgner
 */
public class DateObjectDescription extends AbstractObjectDescription {

    /**
     * Creates a new object description.
     */
    public DateObjectDescription() {
        super(Date.class);
        setParameterDefinition("year", Integer.class);
        setParameterDefinition("month", Integer.class);
        setParameterDefinition("day", Integer.class);
    }

    /**
     * Creates an object based on this description.
     *
     * @return The object.
     */
    public Object createObject() {
        final int y = getIntParameter("year");
        final int m = getIntParameter("month");
        final int d = getIntParameter("day");

        return new GregorianCalendar(y, m, d).getTime();
    }

    /**
     * Returns a parameter value as an <code>int</code>.
     *
     * @param param  the parameter name.
     *
     * @return The parameter value.
     */
    private int getIntParameter(final String param) {
        final Integer p = (Integer) getParameter(param);
        if (p == null) {
            return 0;
        }
        return p.intValue();
    }

    /**
     * Sets the parameters of this description object to match the supplied object.
     *
     * @param o  the object (should be an instance of <code>Date</code>).
     *
     * @throws ObjectFactoryException if the object is not an instance of <code>Date</code>.
     */
    public void setParameterFromObject(final Object o) throws ObjectFactoryException {
        if (o instanceof Date) {
            final GregorianCalendar gc = new GregorianCalendar();
            gc.setTime((Date) o);
            final int year = gc.get(Calendar.YEAR);
            final int month = gc.get(Calendar.MONTH);
            final int day = gc.get(Calendar.DAY_OF_MONTH);

            setParameter("year", new Integer(year));
            setParameter("month", new Integer(month));
            setParameter("day", new Integer(day));
        }
        else {
            throw new ObjectFactoryException("Is no instance of java.util.Date");
        }

    }
}
