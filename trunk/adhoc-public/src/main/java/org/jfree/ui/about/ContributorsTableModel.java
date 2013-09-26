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
 * ---------------------------
 * ContributorsTableModel.java
 * ---------------------------
 * (C) Copyright 2001-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: ContributorsTableModel.java,v 1.6 2008/12/18 09:57:32 mungady Exp $
 *
 * Changes
 * -------
 * 10-Dec-2001 : Version 1 (DG);
 * 28-Feb-2002 : Moved into package com.jrefinery.ui.about.  Changed import
 *               statements and updated Javadoc comments (DG);
 * 08-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see JFreeChart patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.ui.about;

import java.util.List;
import java.util.ResourceBundle;

import javax.swing.table.AbstractTableModel;

import org.jfree.util.ResourceBundleWrapper;

/**
 * A table model containing a list of contributors to a project.
 * <P>
 * Used in the ContributorsPanel class.
 *
 * @author David Gilbert
 */
public class ContributorsTableModel extends AbstractTableModel {

    /** Storage for the contributors. */
    private List contributors;

    /** Localised version of the name column label. */
    private String nameColumnLabel;

    /** Localised version of the contact column label. */
    private String contactColumnLabel;

    /**
     * Constructs a ContributorsTableModel.
     *
     * @param contributors  the contributors.
     */
    public ContributorsTableModel(final List contributors) {

        this.contributors = contributors;

        final String baseName = "org.jfree.ui.about.resources.AboutResources";
        final ResourceBundle resources = ResourceBundleWrapper.getBundle(
                baseName);
        this.nameColumnLabel = resources.getString(
                "contributors-table.column.name");
        this.contactColumnLabel = resources.getString(
                "contributors-table.column.contact");

    }

    /**
     * Returns the number of rows in the table model.
     *
     * @return The number of rows.
     */
    public int getRowCount() {
        return this.contributors.size();
    }

    /**
     * Returns the number of columns in the table model.  In this case, there
     * are always two columns (name and e-mail address).
     *
     * @return The number of columns in the table model.
     */
    public int getColumnCount() {
        return 2;
    }

    /**
     * Returns the name of a column in the table model.
     *
     * @param column  the column index (zero-based).
     *
     * @return  the name of the specified column.
     */
    public String getColumnName(final int column) {

        String result = null;

        switch (column) {

            case 0:  result = this.nameColumnLabel;
                     break;

            case 1:  result = this.contactColumnLabel;
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
        final Contributor contributor
                = (Contributor) this.contributors.get(row);

        if (column == 0) {
            result = contributor.getName();
        }
        else if (column == 1) {
            result = contributor.getEmail();
        }
        return result;

    }

}
