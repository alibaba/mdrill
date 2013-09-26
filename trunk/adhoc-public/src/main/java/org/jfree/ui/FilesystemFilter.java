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
 * $Id: FilesystemFilter.java,v 1.6 2008/09/10 09:26:11 mungady Exp $
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 01-Jun-2005 : Updated javadoc.
 */
package org.jfree.ui;

import java.io.File;
import java.io.FilenameFilter;
import javax.swing.filechooser.FileFilter;

/**
 * A filesystem filter.
 *
 * @author David Gilbert
 */
public class FilesystemFilter extends FileFilter implements FilenameFilter {

    /** The file extension, which should be accepted. */
    private String[] fileext;
    /** The filter description. */
    private String descr;
    /** A flag indicating whether to accept directories. */
    private boolean accDirs;

    /**
     * Creates a new filter.
     *
     * @param fileext the file extension.
     * @param descr   the description.
     */
    public FilesystemFilter(final String fileext, final String descr) {
        this(fileext, descr, true);
    }

    /**
     * Creates a new filter.
     *
     * @param fileext the file extension.
     * @param descr   the description.
     * @param accDirs accept directories?
     */
    public FilesystemFilter(final String fileext, final String descr,
                            final boolean accDirs) {
        this(new String[]{fileext}, descr, accDirs);
    }

    /**
     * Creates a new filter.
     *
     * @param fileext the file extension.
     * @param descr   the description.
     * @param accDirs accept directories?
     * @throws NullPointerException if the file extensions are null.
     */
    public FilesystemFilter(final String[] fileext, final String descr,
                            final boolean accDirs) {
        this.fileext = (String[]) fileext.clone();
        this.descr = descr;
        this.accDirs = accDirs;
    }


    /**
     * Returns <code>true</code> if the file is accepted, and <code>false</code> otherwise.
     *
     * @param dir  the directory.
     * @param name the file name.
     * @return A boolean.
     */
    public boolean accept(final File dir, final String name) {
        final File f = new File(dir, name);
        if (f.isDirectory() && acceptsDirectories()) {
            return true;
        }

        for (int i = 0; i < this.fileext.length; i++) {
            if (name.endsWith(this.fileext[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the specified file matches the requirements of this
     * filter, and <code>false</code> otherwise.
     *
     * @param dir the file or directory.
     * @return A boolean.
     */
    public boolean accept(final File dir) {
        if (dir.isDirectory() && acceptsDirectories()) {
            return true;
        }

        for (int i = 0; i < this.fileext.length; i++) {
            if (dir.getName().endsWith(this.fileext[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the filter description.
     *
     * @return The filter description.
     */
    public String getDescription() {
        return this.descr;
    }

    /**
     * Sets the flag that controls whether or not the filter accepts directories.
     *
     * @param b a boolean.
     */
    public void acceptDirectories(final boolean b) {
        this.accDirs = b;
    }

    /**
     * Returns the flag that indicates whether or not the filter accepts directories.
     *
     * @return A boolean.
     */
    public boolean acceptsDirectories() {
        return this.accDirs;
    }

}
