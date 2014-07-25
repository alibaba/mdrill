/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
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
 * -----------------
 * URLUtilities.java
 * -----------------
 * (C) Copyright 2007, 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributors:     -;
 *
 * Changes:
 * --------
 * 17-Apr-2007 : Version 1 (DG);
 *
 */

package org.jfree.chart.urls;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;

/**
 * General utility methods for URLs.
 *
 * @since 1.0.6
 */
public class URLUtilities {

    /** Constant used by {@link #encode(String, String)}. */
    private static final Class[] STRING_ARGS_2 = new Class[] {String.class,
            String.class};

    /**
     * Calls <code>java.net.URLEncoder.encode(String, String)</code> via
     * reflection, if we are running on JRE 1.4 or later, otherwise reverts to
     * the deprecated <code>URLEncoder.encode(String)</code> method.
     *
     * @param s  the string to encode.
     * @param encoding  the encoding.
     *
     * @return The encoded string.
     *
     * @since 1.0.6
     */
    public static String encode(String s, String encoding) {
        Class c = URLEncoder.class;
        String result = null;
        try {
            Method m = c.getDeclaredMethod("encode", STRING_ARGS_2);
            try {
                result = (String) m.invoke(null, new Object[] {s, encoding});
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        catch (NoSuchMethodException e) {
            // we're running on JRE 1.3.1 so this is the best we have...
            result = URLEncoder.encode(s);
        }
        return result;
    }

}
