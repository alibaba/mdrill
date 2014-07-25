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
 * ---------------------------------
 * Dimension2DObjectDescription.java
 * ---------------------------------
 * (C)opyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: Dimension2DObjectDescription.java,v 1.2 2005/10/18 13:31:58 mungady Exp $
 *
 * Changes 
 * -------
 * 13-Jan-2004 : Initial version
 *  
 */

package org.jfree.xml.factory.objects;

import java.awt.geom.Dimension2D;

import org.jfree.ui.FloatDimension;

/**
 * An object-description for a <code>Dimension2D</code> object.
 *
 * @author Thomas Morgner
 */
public class Dimension2DObjectDescription extends AbstractObjectDescription {

    /**
     * Creates a new object description.
     */
    public Dimension2DObjectDescription() {
        super(Dimension2D.class);
        setParameterDefinition("width", Float.class);
        setParameterDefinition("height", Float.class);
    }

    /**
     * Creates an object based on the description.
     *
     * @return The object.
     */
    public Object createObject() {
        final Dimension2D dim = new FloatDimension();

        final float width = getFloatParameter("width");
        final float height = getFloatParameter("height");
        dim.setSize(width, height);
        return dim;
    }

    /**
     * Returns a parameter value as a float.
     *
     * @param param  the parameter name.
     *
     * @return The float value.
     */
    private float getFloatParameter(final String param) {
        final Float p = (Float) getParameter(param);
        if (p == null) {
            return 0;
        }
        return p.floatValue();
    }


    /**
     * Sets the parameters of this description object to match the supplied object.
     *
     * @param o  the object (should be an instance of <code>Dimension2D</code>).
     *
     * @throws ObjectFactoryException if the object is not an instance of <code>Point2D</code>.
     */
    public void setParameterFromObject(final Object o) throws ObjectFactoryException {
        if (!(o instanceof Dimension2D)) {
            throw new ObjectFactoryException("The given object is no java.awt.geom.Dimension2D.");
        }

        final Dimension2D dim = (Dimension2D) o;
        final float width = (float) dim.getWidth();
        final float height = (float) dim.getHeight();

        setParameter("width", new Float(width));
        setParameter("height", new Float(height));
    }
}
