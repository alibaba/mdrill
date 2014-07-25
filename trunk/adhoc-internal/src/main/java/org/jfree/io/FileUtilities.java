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
 * ------------------
 * FileUtilities.java
 * ------------------
 * (C) Copyright 2000-2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: FileUtilities.java,v 1.5 2005/10/18 13:16:02 mungady Exp $
 *
 * Changes (from 5-Nov-2001)
 * -------------------------
 * 05-Nov-2001 : Changed package to com.jrefinery.io.* (DG);
 * 04-Mar-2002 : Renamed Files.java --> FileUtilities.java (DG);
 * 10-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.io;

import java.io.File;
import java.util.StringTokenizer;

/**
 * A class containing useful utility methods relating to files.
 *
 * @author David Gilbert
 */
public class FileUtilities {

    /**
     * To prevent unnecessary instantiation.
     */
    private FileUtilities() {
    }

    /**
     * Returns a reference to a file with the specified name that is located
     * somewhere on the classpath.  The code for this method is an adaptation
     * of code supplied by Dave Postill.
     *
     * @param name  the filename.
     *
     * @return a reference to a file or <code>null</code> if no file could be found.
     */
    public static File findFileOnClassPath(final String name) {

        final String classpath = System.getProperty("java.class.path");
        final String pathSeparator = System.getProperty("path.separator");

        final StringTokenizer tokenizer = new StringTokenizer(classpath, pathSeparator);

        while (tokenizer.hasMoreTokens()) {
            final String pathElement = tokenizer.nextToken();

            final File directoryOrJar = new File(pathElement);
            final File absoluteDirectoryOrJar = directoryOrJar.getAbsoluteFile();

            if (absoluteDirectoryOrJar.isFile()) {
                final File target = new File(absoluteDirectoryOrJar.getParent(), name);
                if (target.exists()) {
                    return target;
                }
            }
            else {
                final File target = new File(directoryOrJar, name);
                if (target.exists()) {
                    return target;
                }
            }

        }
        return null;

    }

}
