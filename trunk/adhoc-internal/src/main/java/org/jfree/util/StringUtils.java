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
 * ----------------
 * StringUtils.java
 * ----------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: StringUtils.java,v 1.5 2005/10/18 13:24:19 mungady Exp $
 *
 * Changes
 * -------------------------
 * 21.06.2003 : Initial version
 *
 */

package org.jfree.util;

/**
 * String utilities.
 * 
 * @author Thomas Morgner.
 */
public class StringUtils {

    /**
     * Private constructor prevents object creation. 
     */
    private StringUtils() {
    }

    /**
     * Helper functions to query a strings start portion. The comparison is case insensitive.
     *
     * @param base  the base string.
     * @param start  the starting text.
     *
     * @return true, if the string starts with the given starting text.
     */
    public static boolean startsWithIgnoreCase(final String base, final String start) {
        if (base.length() < start.length()) {
            return false;
        }
        return base.regionMatches(true, 0, start, 0, start.length());
    }

    /**
     * Helper functions to query a strings end portion. The comparison is case insensitive.
     *
     * @param base  the base string.
     * @param end  the ending text.
     *
     * @return true, if the string ends with the given ending text.
     */
    public static boolean endsWithIgnoreCase(final String base, final String end) {
        if (base.length() < end.length()) {
            return false;
        }
        return base.regionMatches(true, base.length() - end.length(), end, 0, end.length());
    }

    /**
     * Queries the system properties for the line separator. If access
     * to the System properties is forbidden, the UNIX default is returned.
     *
     * @return the line separator.
     */
    public static String getLineSeparator() {
        try {
            return System.getProperty("line.separator", "\n");
        }
        catch (Exception e) {
            return "\n";
        }
    }


}
