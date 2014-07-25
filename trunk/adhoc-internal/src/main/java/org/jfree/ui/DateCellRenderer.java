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
 * ---------------------
 * DateCellRenderer.java
 * ---------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: DateCellRenderer.java,v 1.7 2007/11/02 17:50:36 taqua Exp $
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 24-Jul-2003 : Version 1 (DG);
 *
 */

package org.jfree.ui;

import java.awt.Component;
import java.text.DateFormat;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A table cell renderer that formats dates.
 *
 * @author David Gilbert
 */
public class DateCellRenderer extends DefaultTableCellRenderer {

    /** The formatter. */
    private DateFormat formatter;
    
    /**
     * Default constructor.
     */
    public DateCellRenderer() {
        this(DateFormat.getDateTimeInstance());
    }
    
    /**
     * Creates a new renderer.
     * 
     * @param formatter  the formatter.
     */
    public DateCellRenderer(final DateFormat formatter) {
        super();
        this.formatter = formatter;
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Returns itself as the renderer. Supports the TableCellRenderer interface.
     *
     * @param table  the table.
     * @param value  the data to be rendered.
     * @param isSelected  a boolean that indicates whether or not the cell is 
     *                    selected.
     * @param hasFocus  a boolean that indicates whether or not the cell has 
     *                  the focus.
     * @param row  the (zero-based) row index.
     * @param column  the (zero-based) column index.
     *
     * @return the component that can render the contents of the cell.
     */
    public Component getTableCellRendererComponent(final JTable table, 
            final Object value, final boolean isSelected, 
            final boolean hasFocus, final int row, final int column) {

        setFont(null);
        if (value != null) {
          setText(this.formatter.format(value));
        }
        else {
          setText("");
        }
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        }
        else {
            setBackground(null);
        }
        return this;
    }

}
