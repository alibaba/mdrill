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
 * KeyDescription.java
 * -------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: KeyDescription.java,v 1.2 2005/10/18 13:32:37 mungady Exp $
 *
 * Changes 
 * -------------------------
 * 21.06.2003 : Initial version
 *  
 */

package org.jfree.xml.generator.model;

/**
 * A key description.
 */
public class KeyDescription {
    
    /** The parameters. */
    private TypeInfo[] parameters;
    
    /** The comments. */
    private Comments comments;

    /**
     * Creates a new key description instance.
     * 
     * @param parameters  the parameters.
     */
    public KeyDescription(final TypeInfo[] parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns the parameters.
     * 
     * @return The parameters.
     */
    public TypeInfo[] getParameters() {
        return this.parameters;
    }

    /**
     * Returns the comments.
     * 
     * @return The comments.
     */
    public Comments getComments() {
        return this.comments;
    }

    /**
     * Sets the comments.
     * 
     * @param comments  the comments.
     */
    public void setComments(final Comments comments) {
        this.comments = comments;
    }
}
