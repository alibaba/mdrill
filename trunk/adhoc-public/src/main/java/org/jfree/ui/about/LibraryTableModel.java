/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
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
 * ----------------------
 * LibraryTableModel.java
 * ----------------------
 * (C) Copyright 2002-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: LibraryTableModel.java,v 1.8 2008/12/18 09:57:32 mungady Exp $
 *
 * Changes
 * -------
 * 28-Feb-2002 : Version 1 (DG);
 * 15-Mar-2002 : Modified to use ResourceBundle for elements that require
 *               localisation (DG);
 * 08-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see JFreeChart patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.ui.about;

import java.util.List;
import java.util.ResourceBundle;

import javax.swing.table.AbstractTableModel;

import org.jfree.base.Library;
import org.jfree.util.ResourceBundleWrapper;

/**
 * A table model containing a list of libraries used in a project.
 * <P>
 * Used in the LibraryPanel class.
 *
 * @author David Gilbert
 */
public class LibraryTableModel extends AbstractTableModel {

    /** Storage for the libraries. */
    private Library[] libraries;

    /** Localised name column label. */
    private String nameColumnLabel;

    /** Localised version column label. */
    private String versionColumnLabel;

    /** Localised licence column label. */
    private String licenceColumnLabel;

    /** Localised info column label. */
    private String infoColumnLabel;

    /**
     * Constructs a LibraryTableModel.
     *
     * @param libraries  the libraries.
     */
    public LibraryTableModel(final List libraries) {

        this.libraries = (Library[])
                libraries.toArray(new Library[libraries.size()]);

        final String baseName = "org.jfree.ui.about.resources.AboutResources";
        final ResourceBundle resources = ResourceBundleWrapper.getBundle(
                baseName);

        this.nameColumnLabel = resources.getString(
                "libraries-table.column.name");
        this.versionColumnLabel = resources.getString(
                "libraries-table.column.version");
        this.licenceColumnLabel = resources.getString(
                "libraries-table.column.licence");
        this.infoColumnLabel = resources.getString(
                "libraries-table.column.info");

    }

    /**
     * Returns the number of rows in the table model.
     *
     * @return the number of rows.
     */
    public int getRowCount() {
        return this.libraries.length;
    }

    /**
     * Returns the number of columns in the table model.  In this case, there
     * are always four columns (name, version, licence and other info).
     *
     * @return the number of columns in the table model.
     */
    public int getColumnCount() {
        return 4;
    }

    /**
     * Returns the name of a column in the table model.
     *
     * @param column  the column index (zero-based).
     *
     * @return the name of the specified column.
     */
    public String getColumnName(final int column) {

        String result = null;

        switch (column) {

            case 0:  result = this.nameColumnLabel;
                     break;

            case 1:  result = this.versionColumnLabel;
                     break;

            case 2:  result = this.licenceColumnLabel;
                     break;

            case 3:  result = this.infoColumnLabel;
                     break;

        }

        return result;

    }

    /**
     * Returns the value for a cell in the table model.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return the value.
     */
    public Object getValueAt(final int row, final int column) {

        Object result = null;
        final Library library = this.libraries[row];

        if (column == 0) {
            result = library.getName();
        }
        else if (column == 1) {
            result = library.getVersion();
        }
        else if (column == 2) {
            result = library.getLicenceName();
        }
        else if (column == 3) {
            result = library.getInfo();
        }
        return result;

    }

    /**
     * Returns an array of the libraries in the table.
     *
     * @return An array of libraries.
     */
    public Library[] getLibraries() {
      return (Library[]) this.libraries.clone();
    }
}
