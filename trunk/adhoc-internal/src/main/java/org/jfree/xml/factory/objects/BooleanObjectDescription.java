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
 * -----------------------------
 * BooleanObjectDescription.java
 * -----------------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: BooleanObjectDescription.java,v 1.3 2005/11/14 10:59:16 mungady Exp $
 *
 * Changes (from 19-Feb-2003)
 * -------------------------
 * 19-Feb-2003 : Added standard header and Javadocs (DG);
 * 29-Apr-2003 : Distilled from the JFreeReport project and moved into JCommon
 *
 */

package org.jfree.xml.factory.objects;

/**
 * An object-description for a <code>Boolean</code> object.
 *
 * @author Thomas Morgner
 */
public class BooleanObjectDescription extends AbstractObjectDescription {

    /**
     * Creates a new object description.
     */
    public BooleanObjectDescription() {
        super(Boolean.class);
        setParameterDefinition("value", String.class);
    }

    /**
     * Creates a new <code>Boolean</code> based on the settings of this description object.
     *
     * @return A <code>Boolean</code>.
     */
    public Object createObject() {
        final String o = (String) getParameter("value");
        return Boolean.valueOf(o);
    }

    /**
     * Sets the description object parameters to match the supplied object
     * (which should be an instance of <code>Boolean</code>.
     *
     * @param o  the object.
     * @throws ObjectFactoryException if there is a problem while reading the
     * properties of the given object.
     */
    public void setParameterFromObject(final Object o) throws ObjectFactoryException {
        if (!(o instanceof Boolean)) {
            throw new ObjectFactoryException("The given object is no java.lang.Boolean. ");
        }
        setParameter("value", String.valueOf(o));
    }

    /**
     * Tests for equality.
     * 
     * @param o  the object to test.
     * 
     * @return A boolean.
     */
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractObjectDescription)) {
            return false;
        }

        final AbstractObjectDescription abstractObjectDescription = (AbstractObjectDescription) o;

        if (Boolean.TYPE.equals(abstractObjectDescription.getObjectClass())) {
            return true;
        }
        if (Boolean.class.equals(abstractObjectDescription.getObjectClass())) {
            return true;
        }
        return false;
    }

    /**
     * Returns a hash code.
     * 
     * @return A hash code.
     */
    public int hashCode() {
        return getObjectClass().hashCode();
    }

}
