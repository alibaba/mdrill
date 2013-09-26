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
 * -----------------------------------
 * DecimalFormatObjectDescription.java
 * -----------------------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: DecimalFormatObjectDescription.java,v 1.5 2005/11/14 11:01:44 mungady Exp $
 *
 * Changes (from 19-Feb-2003)
 * -------------------------
 * 19-Feb-2003 : Added standard header and Javadocs (DG);
 * 29-Apr-2003 : Distilled from the JFreeReport project and moved into JCommon
 *
 */

package org.jfree.xml.factory.objects;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * An object-description for a <code>DecimalFormat</code> object.
 *
 * @author Thomas Morgner
 */
public class DecimalFormatObjectDescription extends BeanObjectDescription {

    /**
     * Creates a new object description.
     */
    public DecimalFormatObjectDescription() {
        this(DecimalFormat.class);
    }

    /**
     * Creates a new object description.
     *
     * @param className  the class.
     */
    public DecimalFormatObjectDescription(final Class className) {
        super(className, false);
        setParameterDefinition("localizedPattern", String.class);
        setParameterDefinition("pattern", String.class);
        setParameterDefinition("decimalFormatSymbols", DecimalFormatSymbols.class);
        setParameterDefinition("decimalSeparatorAlwaysShown", Boolean.TYPE);
        setParameterDefinition("groupingSize", Integer.TYPE);
        setParameterDefinition("groupingUsed", Boolean.TYPE);
        setParameterDefinition("maximumFractionDigits", Integer.TYPE);
        setParameterDefinition("maximumIntegerDigits", Integer.TYPE);
        setParameterDefinition("minimumFractionDigits", Integer.TYPE);
        setParameterDefinition("minimumIntegerDigits", Integer.TYPE);
        setParameterDefinition("multiplier", Integer.TYPE);
        setParameterDefinition("negativePrefix", String.class);
        setParameterDefinition("negativeSuffix", String.class);
//        setParameterDefinition("parseBigDecimal", Boolean.TYPE);
        setParameterDefinition("parseIntegerOnly", Boolean.TYPE);
        setParameterDefinition("positivePrefix", String.class);
        setParameterDefinition("positiveSuffix", String.class);
        ignoreParameter("localizedPattern");
        ignoreParameter("pattern");
    }

    /**
     * Creates a new object description.
     *
     * @param className  the class.
     * @param init  initialise
     * @deprecated should no longer be used...
     */
    public DecimalFormatObjectDescription(final Class className, 
            final boolean init) {
        this(className);
    }

    /**
     * Sets the parameters of this description object to match the supplied object.
     *
     * @param o  the object (should be an instance of <code>DecimalFormat</code>).
     *
     * @throws ObjectFactoryException if there is a problem while reading the
     * properties of the given object.
     */
    public void setParameterFromObject(final Object o)
        throws ObjectFactoryException {
        super.setParameterFromObject(o);
        final DecimalFormat format = (DecimalFormat) o;
        setParameter("localizedPattern", format.toLocalizedPattern());
        setParameter("pattern", format.toPattern());
    }

    /**
     * Creates an object (<code>DecimalFormat</code>) based on this description.
     *
     * @return The object.
     */
    public Object createObject() {
        final DecimalFormat format = (DecimalFormat) super.createObject();
        if (getParameter("pattern") != null) {
            format.applyPattern((String) getParameter("pattern"));
        }
        if (getParameter("localizedPattern") != null) {
            format.applyLocalizedPattern((String) getParameter("localizedPattern"));
        }
        return format;
    }
}
