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
 * ------------------------
 * ExtensionFileFilter.java
 * ------------------------
 * (C) Copyright 2000-2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: ExtensionFileFilter.java,v 1.5 2007/11/02 17:50:36 taqua Exp $
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.* (DG);
 * 26-Jun-2002 : Updated imports (DG);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */
package org.jfree.ui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * A filter for JFileChooser that filters files by extension.
 *
 * @author David Gilbert
 */
public class ExtensionFileFilter extends FileFilter {

    /** A description for the file type. */
    private String description;

    /** The extension (for example, "png" for *.png files). */
    private String extension;

    /**
     * Standard constructor.
     *
     * @param description  a description of the file type;
     * @param extension  the file extension;
     */
    public ExtensionFileFilter(final String description, final String extension) {
        this.description = description;
        this.extension = extension;
    }

    /**
     * Returns true if the file ends with the specified extension.
     *
     * @param file  the file to test.
     *
     * @return A boolean that indicates whether or not the file is accepted by the filter.
     */
    public boolean accept(final File file) {

        if (file.isDirectory()) {
            return true;
        }

        final String name = file.getName().toLowerCase();
        if (name.endsWith(this.extension)) {
            return true;
        }
        else {
            return false;
        }

    }

    /**
     * Returns the description of the filter.
     *
     * @return a description of the filter.
     */
    public String getDescription() {
        return this.description;
    }

}
