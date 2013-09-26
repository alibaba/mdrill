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
 * LongObjectDescription.java
 * --------------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: SimpleDateFormatObjectDescription.java,v 1.3 2005/11/14 11:04:12 mungady Exp $
 *
 * Changes (from 19-Feb-2003)
 * -------------------------
 * 19-Feb-2003 : Added standard header and Javadocs (DG);
 * 29-Apr-2003 : Distilled from the JFreeReport project and moved into JCommon
 *
 */

package org.jfree.xml.factory.objects;

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * An object-description for a <code>SimpleDateFormat</code> object.
 *
 * @author Thomas Morgner
 */
public class SimpleDateFormatObjectDescription extends BeanObjectDescription {

    /**
     * Creates a new object description.
     */
    public SimpleDateFormatObjectDescription() {
        this(SimpleDateFormat.class);
    }

    /**
     * Creates a new object description.
     *
     * @param className  the class.
     */
    public SimpleDateFormatObjectDescription(final Class className) {
        this(className, true);
    }

    /**
     * Creates a new object description.
     *
     * @param className  the class.
     * @param init  initialise?
     */
    public SimpleDateFormatObjectDescription(final Class className, final boolean init) {
        super(className, false);
        setParameterDefinition("2DigitYearStart", Date.class);
        setParameterDefinition("calendar", Calendar.class);
        setParameterDefinition("dateFormatSymbols", DateFormatSymbols.class);
        setParameterDefinition("lenient", Boolean.TYPE);
        setParameterDefinition("numberFormat", NumberFormat.class);
//        setParameterDefinition("timeZone", TimeZone.class);
        setParameterDefinition("localizedPattern", String.class);
        setParameterDefinition("pattern", String.class);
        ignoreParameter("localizedPattern");
        ignoreParameter("pattern");
    }

    /**
     * Sets the parameters of this description object to match the supplied object.
     *
     * @param o  the object.
     *
     * @throws ObjectFactoryException if there is a problem while reading the
     * properties of the given object.
     */
    public void setParameterFromObject(final Object o)
        throws ObjectFactoryException {
        super.setParameterFromObject(o);
        final SimpleDateFormat format = (SimpleDateFormat) o;
        setParameter("localizedPattern", format.toLocalizedPattern());
        setParameter("pattern", format.toPattern());
    }

    /**
     * Creates an object based on this description.
     *
     * @return The object.
     */
    public Object createObject() {
        final SimpleDateFormat format = (SimpleDateFormat) super.createObject();
        if (getParameter("pattern") != null) {
            format.applyPattern((String) getParameter("pattern"));
        }
        if (getParameter("localizedPattern") != null) {
            format.applyLocalizedPattern((String) getParameter("localizedPattern"));
        }
        return format;
    }

}
