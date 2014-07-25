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
 * ---------------
 * TextAnchor.java
 * ---------------
 * (C) Copyright 2003-2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: TextAnchor.java,v 1.5 2005/10/18 13:18:34 mungady Exp $
 *
 * Changes:
 * --------
 * 10-Jun-2003 : Version 1 (DG);
 * 11-Jan-2005 : Removed deprecated code (DG);
 * 
 */

package org.jfree.ui;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Used to indicate the position of an anchor point for a text string.  This is
 * frequently used to align a string to a fixed point in some coordinate space.
 *
 * @author David Gilbert
 */
public final class TextAnchor implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 8219158940496719660L;
    
    /** Top/left. */
    public static final TextAnchor TOP_LEFT 
        = new TextAnchor("TextAnchor.TOP_LEFT");

    /** Top/center. */
    public static final TextAnchor TOP_CENTER 
        = new TextAnchor("TextAnchor.TOP_CENTER");

    /** Top/right. */
    public static final TextAnchor TOP_RIGHT 
        = new TextAnchor("TextAnchor.TOP_RIGHT");

    /** Half-ascent/left. */
    public static final TextAnchor HALF_ASCENT_LEFT 
        = new TextAnchor("TextAnchor.HALF_ASCENT_LEFT");

    /** Half-ascent/center. */
    public static final TextAnchor HALF_ASCENT_CENTER 
        = new TextAnchor("TextAnchor.HALF_ASCENT_CENTER");

    /** Half-ascent/right. */
    public static final TextAnchor HALF_ASCENT_RIGHT 
        = new TextAnchor("TextAnchor.HALF_ASCENT_RIGHT");

    /** Middle/left. */
    public static final TextAnchor CENTER_LEFT 
        = new TextAnchor("TextAnchor.CENTER_LEFT");

    /** Middle/center. */
    public static final TextAnchor CENTER = new TextAnchor("TextAnchor.CENTER");

    /** Middle/right. */
    public static final TextAnchor CENTER_RIGHT 
        = new TextAnchor("TextAnchor.CENTER_RIGHT");

    /** Baseline/left. */
    public static final TextAnchor BASELINE_LEFT 
        = new TextAnchor("TextAnchor.BASELINE_LEFT");

    /** Baseline/center. */
    public static final TextAnchor BASELINE_CENTER 
        = new TextAnchor("TextAnchor.BASELINE_CENTER");

    /** Baseline/right. */
    public static final TextAnchor BASELINE_RIGHT 
        = new TextAnchor("TextAnchor.BASELINE_RIGHT");

    /** Bottom/left. */
    public static final TextAnchor BOTTOM_LEFT 
        = new TextAnchor("TextAnchor.BOTTOM_LEFT");

    /** Bottom/center. */
    public static final TextAnchor BOTTOM_CENTER 
        = new TextAnchor("TextAnchor.BOTTOM_CENTER");

    /** Bottom/right. */
    public static final TextAnchor BOTTOM_RIGHT 
        = new TextAnchor("TextAnchor.BOTTOM_RIGHT");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private TextAnchor(final String name) {
        this.name = name;
    }

    /**
     * Returns a string representing the object.
     *
     * @return The string.
     */
    public String toString() {
        return this.name;
    }

    /**
     * Returns <code>true</code> if this object is equal to the specified 
     * object, and <code>false</code> otherwise.
     *
     * @param o  the other object.
     *
     * @return A boolean.
     */
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }
        if (!(o instanceof TextAnchor)) {
            return false;
        }

        final TextAnchor order = (TextAnchor) o;
        if (!this.name.equals(order.name)) {
            return false;
        }

        return true;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return The hashcode
     */
    public int hashCode() {
        return this.name.hashCode();
    }

    /**
     * Ensures that serialization returns the unique instances.
     * 
     * @return The object.
     * 
     * @throws ObjectStreamException if there is a problem.
     */
    private Object readResolve() throws ObjectStreamException {
        TextAnchor result = null;
        if (this.equals(TextAnchor.TOP_LEFT)) {
            result = TextAnchor.TOP_LEFT;
        }
        else if (this.equals(TextAnchor.TOP_CENTER)) {
            result = TextAnchor.TOP_CENTER;
        }
        else if (this.equals(TextAnchor.TOP_RIGHT)) {
            result = TextAnchor.TOP_RIGHT;
        }
        else if (this.equals(TextAnchor.BOTTOM_LEFT)) {
            result = TextAnchor.BOTTOM_LEFT;
        }
        else if (this.equals(TextAnchor.BOTTOM_CENTER)) {
            result = TextAnchor.BOTTOM_CENTER;
        }
        else if (this.equals(TextAnchor.BOTTOM_RIGHT)) {
            result = TextAnchor.BOTTOM_RIGHT;
        }
        else if (this.equals(TextAnchor.BASELINE_LEFT)) {
            result = TextAnchor.BASELINE_LEFT;
        }
        else if (this.equals(TextAnchor.BASELINE_CENTER)) {
            result = TextAnchor.BASELINE_CENTER;
        }
        else if (this.equals(TextAnchor.BASELINE_RIGHT)) {
            result = TextAnchor.BASELINE_RIGHT;
        }
        else if (this.equals(TextAnchor.CENTER_LEFT)) {
            result = TextAnchor.CENTER_LEFT;
        }
        else if (this.equals(TextAnchor.CENTER)) {
            result = TextAnchor.CENTER;
        }
        else if (this.equals(TextAnchor.CENTER_RIGHT)) {
            result = TextAnchor.CENTER_RIGHT;
        }
        else if (this.equals(TextAnchor.HALF_ASCENT_LEFT)) {
            result = TextAnchor.HALF_ASCENT_LEFT;
        }
        else if (this.equals(TextAnchor.HALF_ASCENT_CENTER)) {
            result = TextAnchor.HALF_ASCENT_CENTER;
        }
        else if (this.equals(TextAnchor.HALF_ASCENT_RIGHT)) {
            result = TextAnchor.HALF_ASCENT_RIGHT;
        }
        return result;
    }

}
