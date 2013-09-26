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
 * -----------------
 * FormatLayout.java
 * -----------------
 * (C) Copyright 2000-2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: FormatLayout.java,v 1.4 2005/10/18 13:16:50 mungady Exp $
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.layout.* (DG);
 * 26-Jun-2002 : Removed redundant code (DG);
 * 10-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.Serializable;

/**
 * A layout manager that spaces components over six columns in seven different 
 * formats.
 *
 * @author David Gilbert
 */
public class FormatLayout implements LayoutManager, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 2866692886323930722L;
    
    /** A useful constant representing layout format 1. */
    public static final int C = 1;

    /** A useful constant representing layout format 2. */
    public static final int LC = 2;

    /** A useful constant representing layout format 3. */
    public static final int LCB = 3;

    /** A useful constant representing layout format 4. */
    public static final int LCLC = 4;

    /** A useful constant representing layout format 5. */
    public static final int LCLCB = 5;

    /** A useful constant representing layout format 6. */
    public static final int LCBLC = 6;

    /** A useful constant representing layout format 7. */
    public static final int LCBLCB = 7;

    /** The layout format for each row. */
    private int[] rowFormats;

    /** The gap between the rows. */
    private int rowGap;

    /** 
     * The gaps between the columns (gap[0] is the gap following column zero). 
     */
    private int[] columnGaps;

    /** Working array for recording the height of each row. */
    private int[] rowHeights;

    /** The total height of the layout. */
    private int totalHeight;

    /** Working array for recording the width of each column. */
    private int[] columnWidths;

    /** The total width of the layout. */
    private int totalWidth;

    /** Combined width of columns 1 and 2. */
    private int columns1and2Width;

    /** Combined width of columns 4 and 5. */
    private int columns4and5Width;

    /** Combined width of columns 1 to 4. */
    private int columns1to4Width;

    /** Combined width of columns 1 to 5. */
    private int columns1to5Width;

    /** Combined width of columns 0 to 5. */
    private int columns0to5Width;

    /**
     * Constructs a new layout manager that can be used to create input forms.  
     * The layout manager works by arranging components in rows using six 
     * columns (some components will use more than one column).
     * <P>
     * Any component can be added, but I think of them in terms of Labels,
     * Components, and Buttons.
     * The formats available are:  C, LC, LCB, LCLC, LCLCB, LCBLC or LCBLCB.
     * <table>
     * <tr>
     * <td>C</td>
     * <td>1 component in this row (spread across all six columns).</td>
     * </tr>
     * <tr>
     * <td>LC</td>
     * <td>2 components, a label in the 1st column, and a component using the
     *      remaining 5 columns).</td>
     * </tr>
     * <tr>
     * <td>LCB</td>
     * <td>3 components, a label in the 1st column, a component spread across
     *      the next 4, and a button in the last column.</td>
     * </tr>
     * <tr>
     * <td>LCLC</td>
     * <td>4 components, a label in column 1, a component in 2-3, a label in
     *       4 and a component in 5-6.</td>
     * </tr>
     * <tr>
     * <td>LCLCB</td>
     * <td>5 components, a label in column 1, a component in 2-3, a label
     *      in 4, a component in 5 and a button in 6.</td>
     * </tr>
     * <tr>
     * <td>LCBLC</td>
     * <td>5 components, a label in column 1, a component in 2, a button in 3,
     *  a label in 4, a component in 5-6.</td>
     * </tr>
     * <tr>
     * <td>LCBLCB</td>
     * <td>6 components, one in each column.</td>
     * </tr>
     * </table>
     * <P>
     * Columns 1 and 4 expand to accommodate the widest label, and 3 and 6 to
     * accommodate the widest button.
     * <P>
     * Each row will contain the number of components indicated by the format.  
     * Be sure to specify enough row formats to cover all the components you 
     * add to the layout.
     *
     * @param rowCount  the number of rows.
     * @param rowFormats  the row formats.
     */
    public FormatLayout(final int rowCount, final int[] rowFormats) {

        this.rowFormats = rowFormats;
        this.rowGap = 2;
        this.columnGaps = new int[5];
        this.columnGaps[0] = 10;
        this.columnGaps[1] = 5;
        this.columnGaps[2] = 5;
        this.columnGaps[3] = 10;
        this.columnGaps[4] = 5;

        // working structures...
        this.rowHeights = new int[rowCount];
        this.columnWidths = new int[6];
    }

    /**
     * Returns the preferred size of the component using this layout manager.
     *
     * @param parent  the parent.
     *
     * @return the preferred size of the component.
     */
    public Dimension preferredLayoutSize(final Container parent) {

        Component c0, c1, c2, c3, c4, c5;

        synchronized (parent.getTreeLock()) {
            final Insets insets = parent.getInsets();
            int componentIndex = 0;
            final int rowCount = this.rowHeights.length;
            for (int i = 0; i < this.columnWidths.length; i++) {
                this.columnWidths[i] = 0;
            }
            this.columns1and2Width = 0;
            this.columns4and5Width = 0;
            this.columns1to4Width = 0;
            this.columns1to5Width = 0;
            this.columns0to5Width = 0;

            this.totalHeight = 0;
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            final int format 
                = this.rowFormats[rowIndex % this.rowFormats.length];
                switch (format) {
                    case FormatLayout.C:
                        c0 = parent.getComponent(componentIndex);
                        updateC(rowIndex, c0.getPreferredSize());
                        componentIndex = componentIndex + 1;
                        break;
                    case FormatLayout.LC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        updateLC(rowIndex, c0.getPreferredSize(), 
                                c1.getPreferredSize());
                        componentIndex = componentIndex + 2;
                        break;
                    case FormatLayout.LCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        updateLCB(rowIndex,
                                  c0.getPreferredSize(),
                                  c1.getPreferredSize(),
                                  c2.getPreferredSize());
                        componentIndex = componentIndex + 3;
                        break;
                    case FormatLayout.LCLC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        c3 = parent.getComponent(componentIndex + 3);
                        updateLCLC(rowIndex,
                                   c0.getPreferredSize(),
                                   c1.getPreferredSize(),
                                   c2.getPreferredSize(),
                                   c3.getPreferredSize());
                        componentIndex = componentIndex + 4;
                        break;
                    case FormatLayout.LCBLC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        c3 = parent.getComponent(componentIndex + 3);
                        c4 = parent.getComponent(componentIndex + 4);
                        updateLCBLC(rowIndex,
                                    c0.getPreferredSize(),
                                    c1.getPreferredSize(),
                                    c2.getPreferredSize(),
                                    c3.getPreferredSize(),
                                    c4.getPreferredSize());
                        componentIndex = componentIndex + 5;
                        break;
                    case FormatLayout.LCLCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        c3 = parent.getComponent(componentIndex + 3);
                        c4 = parent.getComponent(componentIndex + 4);
                        updateLCLCB(rowIndex,
                                    c0.getPreferredSize(),
                                    c1.getPreferredSize(),
                                    c2.getPreferredSize(),
                                    c3.getPreferredSize(),
                                    c4.getPreferredSize());
                        componentIndex = componentIndex + 5;
                        break;
                    case FormatLayout.LCBLCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        c3 = parent.getComponent(componentIndex + 3);
                        c4 = parent.getComponent(componentIndex + 4);
                        c5 = parent.getComponent(componentIndex + 5);
                        updateLCBLCB(rowIndex,
                                     c0.getPreferredSize(),
                                     c1.getPreferredSize(),
                                     c2.getPreferredSize(),
                                     c3.getPreferredSize(),
                                     c4.getPreferredSize(),
                                     c5.getPreferredSize());
                        componentIndex = componentIndex + 6;
                        break;
                }
            }
            complete();
            return new Dimension(this.totalWidth + insets.left + insets.right,
                    this.totalHeight + (rowCount - 1) * this.rowGap
                                 + insets.top + insets.bottom);
        }
    }

    /**
     * Returns the minimum size of the component using this layout manager.
     *
     * @param parent  the parent.
     *
     * @return the minimum size of the component
     */
    public Dimension minimumLayoutSize(final Container parent) {

        Component c0, c1, c2, c3, c4, c5;

        synchronized (parent.getTreeLock()) {
            final Insets insets = parent.getInsets();
            int componentIndex = 0;
            final int rowCount = this.rowHeights.length;
            for (int i = 0; i < this.columnWidths.length; i++) {
                this.columnWidths[i] = 0;
            }
            this.columns1and2Width = 0;
            this.columns4and5Width = 0;
            this.columns1to4Width = 0;
            this.columns1to5Width = 0;
            this.columns0to5Width = 0;
            final int totalHeight = 0;
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {

                final int format 
                    = this.rowFormats[rowIndex % this.rowFormats.length];

                switch (format) {
                    case FormatLayout.C:
                        c0 = parent.getComponent(componentIndex);
                        this.columns0to5Width = Math.max(
                            this.columns0to5Width, c0.getMinimumSize().width
                        );
                        componentIndex = componentIndex + 1;
                        break;
                    case FormatLayout.LC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        updateLC(rowIndex,
                                 c0.getMinimumSize(),
                                 c1.getMinimumSize());
                        componentIndex = componentIndex + 2;
                        break;
                    case FormatLayout.LCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        updateLCB(rowIndex,
                                  c0.getMinimumSize(),
                                  c1.getMinimumSize(),
                                  c2.getMinimumSize());
                        componentIndex = componentIndex + 3;
                        break;
                    case FormatLayout.LCLC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        c3 = parent.getComponent(componentIndex + 3);
                        updateLCLC(rowIndex,
                                   c0.getMinimumSize(),
                                   c1.getMinimumSize(),
                                   c2.getMinimumSize(),
                                   c3.getMinimumSize());
                        componentIndex = componentIndex + 3;
                        break;
                    case FormatLayout.LCBLC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        c3 = parent.getComponent(componentIndex + 3);
                        c4 = parent.getComponent(componentIndex + 4);
                        updateLCBLC(rowIndex,
                                    c0.getMinimumSize(),
                                    c1.getMinimumSize(),
                                    c2.getMinimumSize(),
                                    c3.getMinimumSize(),
                                    c4.getMinimumSize());
                        componentIndex = componentIndex + 4;
                        break;
                    case FormatLayout.LCLCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        c3 = parent.getComponent(componentIndex + 3);
                        c4 = parent.getComponent(componentIndex + 4);
                        updateLCLCB(rowIndex,
                                    c0.getMinimumSize(),
                                    c1.getMinimumSize(),
                                    c2.getMinimumSize(),
                                    c3.getMinimumSize(),
                                    c4.getMinimumSize());
                        componentIndex = componentIndex + 4;
                        break;
                    case FormatLayout.LCBLCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        c3 = parent.getComponent(componentIndex + 3);
                        c4 = parent.getComponent(componentIndex + 4);
                        c5 = parent.getComponent(componentIndex + 5);
                        updateLCBLCB(rowIndex,
                                     c0.getMinimumSize(),
                                     c1.getMinimumSize(),
                                     c2.getMinimumSize(),
                                     c3.getMinimumSize(),
                                     c4.getMinimumSize(),
                                     c5.getMinimumSize());
                        componentIndex = componentIndex + 5;
                        break;
                }
            }
            complete();
            return new Dimension(this.totalWidth + insets.left + insets.right,
                                 totalHeight + (rowCount - 1) * this.rowGap
                                 + insets.top + insets.bottom);
        }
    }

    /**
     * Performs the layout of the container.
     *
     * @param parent  the parent.
     */
    public void layoutContainer(final Container parent) {
        Component c0, c1, c2, c3, c4, c5;

        synchronized (parent.getTreeLock()) {
            final Insets insets = parent.getInsets();
            int componentIndex = 0;
            final int rowCount = this.rowHeights.length;
            for (int i = 0; i < this.columnWidths.length; i++) {
                this.columnWidths[i] = 0;
            }
            this.columns1and2Width = 0;
            this.columns4and5Width = 0;
            this.columns1to4Width = 0;
            this.columns1to5Width = 0;
            this.columns0to5Width 
                = parent.getBounds().width - insets.left - insets.right;

            this.totalHeight = 0;
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                final int format 
                    = this.rowFormats[rowIndex % this.rowFormats.length];
                switch (format) {
                    case FormatLayout.C:
                        c0 = parent.getComponent(componentIndex);
                        updateC(rowIndex, c0.getPreferredSize());
                        componentIndex = componentIndex + 1;
                        break;
                    case FormatLayout.LC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        updateLC(rowIndex, c0.getPreferredSize(), 
                                c1.getPreferredSize());
                        componentIndex = componentIndex + 2;
                        break;
                    case FormatLayout.LCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        updateLCB(rowIndex,
                                  c0.getPreferredSize(),
                                  c1.getPreferredSize(),
                                  c2.getPreferredSize());
                        componentIndex = componentIndex + 3;
                        break;
                    case FormatLayout.LCLC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        c3 = parent.getComponent(componentIndex + 3);
                        updateLCLC(rowIndex,
                                   c0.getPreferredSize(),
                                   c1.getPreferredSize(),
                                   c2.getPreferredSize(),
                                   c3.getPreferredSize());
                        componentIndex = componentIndex + 4;
                        break;
                    case FormatLayout.LCBLC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        c3 = parent.getComponent(componentIndex + 3);
                        c4 = parent.getComponent(componentIndex + 4);
                        updateLCBLC(rowIndex,
                                    c0.getPreferredSize(),
                                    c1.getPreferredSize(),
                                    c2.getPreferredSize(),
                                    c3.getPreferredSize(),
                                    c4.getPreferredSize());
                        componentIndex = componentIndex + 5;
                        break;
                    case FormatLayout.LCLCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        c3 = parent.getComponent(componentIndex + 3);
                        c4 = parent.getComponent(componentIndex + 4);
                        updateLCLCB(rowIndex,
                                    c0.getPreferredSize(),
                                    c1.getPreferredSize(),
                                    c2.getPreferredSize(),
                                    c3.getPreferredSize(),
                                    c4.getPreferredSize());
                        componentIndex = componentIndex + 5;
                        break;
                    case FormatLayout.LCBLCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex + 1);
                        c2 = parent.getComponent(componentIndex + 2);
                        c3 = parent.getComponent(componentIndex + 3);
                        c4 = parent.getComponent(componentIndex + 4);
                        c5 = parent.getComponent(componentIndex + 5);
                        updateLCBLCB(rowIndex,
                                     c0.getPreferredSize(),
                                     c1.getPreferredSize(),
                                     c2.getPreferredSize(),
                                     c3.getPreferredSize(),
                                     c4.getPreferredSize(),
                                     c5.getPreferredSize());
                        componentIndex = componentIndex + 6;
                        break;
                }
            }
            complete();

            componentIndex = 0;
            int rowY = insets.top;
            final int[] rowX = new int[6];
            rowX[0] = insets.left;
            rowX[1] = rowX[0] + this.columnWidths[0] + this.columnGaps[0];
            rowX[2] = rowX[1] + this.columnWidths[1] + this.columnGaps[1];
            rowX[3] = rowX[2] + this.columnWidths[2] + this.columnGaps[2];
            rowX[4] = rowX[3] + this.columnWidths[3] + this.columnGaps[3];
            rowX[5] = rowX[4] + this.columnWidths[4] + this.columnGaps[4];
            final int w1to2 = this.columnWidths[1] + this.columnGaps[1] 
                              + this.columnWidths[2];
            final int w4to5 = this.columnWidths[4] + this.columnGaps[4] 
                              + this.columnWidths[5];
            final int w1to4 = w1to2 + this.columnGaps[2] + this.columnWidths[3]
                        + this.columnGaps[3] + this.columnWidths[4];
            final int w1to5 = w1to4 + this.columnGaps[4] + this.columnWidths[5];
            final int w0to5 = w1to5 + this.columnWidths[0] + this.columnGaps[0];
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                final int format 
                    = this.rowFormats[rowIndex % this.rowFormats.length];

                switch (format) {
                case FormatLayout.C:
                    c0 = parent.getComponent(componentIndex);
                    c0.setBounds(rowX[0], rowY, w0to5, 
                            c0.getPreferredSize().height);
                    componentIndex = componentIndex + 1;
                    break;
                case FormatLayout.LC:
                    c0 = parent.getComponent(componentIndex);
                    c0.setBounds(
                        rowX[0],
                        rowY + (this.rowHeights[rowIndex] 
                                     - c0.getPreferredSize().height) / 2,
                        this.columnWidths[0], c0.getPreferredSize().height
                    );
                    c1 = parent.getComponent(componentIndex + 1);
                    c1.setBounds(
                        rowX[1],
                        rowY + (this.rowHeights[rowIndex] 
                                     - c1.getPreferredSize().height) / 2,
                        w1to5, c1.getPreferredSize().height
                    );
                    componentIndex = componentIndex + 2;
                    break;
                case FormatLayout.LCB:
                    c0 = parent.getComponent(componentIndex);
                    c0.setBounds(
                        rowX[0],
                        rowY + (this.rowHeights[rowIndex] 
                                     - c0.getPreferredSize().height) / 2,
                        this.columnWidths[0], c0.getPreferredSize().height
                    );
                    c1 = parent.getComponent(componentIndex + 1);
                    c1.setBounds(
                        rowX[1],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c1.getPreferredSize().height) / 2,
                        w1to4, c1.getPreferredSize().height
                    );
                    c2 = parent.getComponent(componentIndex + 2);
                    c2.setBounds(
                        rowX[5],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c2.getPreferredSize().height) / 2,
                        this.columnWidths[5], c2.getPreferredSize().height
                    );
                    componentIndex = componentIndex + 3;
                    break;
                case FormatLayout.LCLC:
                    c0 = parent.getComponent(componentIndex);
                    c0.setBounds(
                        rowX[0],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c0.getPreferredSize().height) / 2,
                        this.columnWidths[0], c0.getPreferredSize().height
                    );
                    c1 = parent.getComponent(componentIndex + 1);
                    c1.setBounds(
                        rowX[1],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c1.getPreferredSize().height) / 2,
                        w1to2, c1.getPreferredSize().height
                    );
                    c2 = parent.getComponent(componentIndex + 2);
                    c2.setBounds(
                        rowX[3],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c2.getPreferredSize().height) / 2,
                        this.columnWidths[3], c2.getPreferredSize().height
                    );
                    c3 = parent.getComponent(componentIndex + 3);
                    c3.setBounds(
                        rowX[4],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c3.getPreferredSize().height) / 2,
                        w4to5, c3.getPreferredSize().height
                    );
                    componentIndex = componentIndex + 4;
                    break;
                case FormatLayout.LCBLC:
                    c0 = parent.getComponent(componentIndex);
                    c0.setBounds(
                        rowX[0],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c0.getPreferredSize().height) / 2,
                        this.columnWidths[0], c0.getPreferredSize().height
                    );
                    c1 = parent.getComponent(componentIndex + 1);
                    c1.setBounds(
                        rowX[1],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c1.getPreferredSize().height) / 2,
                        this.columnWidths[1], c1.getPreferredSize().height
                    );
                    c2 = parent.getComponent(componentIndex + 2);
                    c2.setBounds(
                        rowX[2],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c2.getPreferredSize().height) / 2,
                        this.columnWidths[2], c2.getPreferredSize().height
                    );
                    c3 = parent.getComponent(componentIndex + 3);
                    c3.setBounds(
                        rowX[3],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c3.getPreferredSize().height) / 2,
                        this.columnWidths[3], c3.getPreferredSize().height
                    );
                    c4 = parent.getComponent(componentIndex + 4);
                    c4.setBounds(
                        rowX[4],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c4.getPreferredSize().height) / 2,
                        w4to5, c4.getPreferredSize().height
                    );
                    componentIndex = componentIndex + 5;
                    break;
                case FormatLayout.LCLCB:
                    c0 = parent.getComponent(componentIndex);
                    c0.setBounds(
                        rowX[0],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c0.getPreferredSize().height) / 2,
                        this.columnWidths[0], c0.getPreferredSize().height
                    );
                    c1 = parent.getComponent(componentIndex + 1);
                    c1.setBounds(
                        rowX[1],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c1.getPreferredSize().height) / 2,
                        w1to2, c1.getPreferredSize().height
                    );
                    c2 = parent.getComponent(componentIndex + 2);
                    c2.setBounds(
                        rowX[3],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c2.getPreferredSize().height) / 2,
                        this.columnWidths[3], c2.getPreferredSize().height
                    );
                    c3 = parent.getComponent(componentIndex + 3);
                    c3.setBounds(
                        rowX[4],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c3.getPreferredSize().height) / 2,
                        this.columnWidths[4], c3.getPreferredSize().height
                    );
                    c4 = parent.getComponent(componentIndex + 4);
                    c4.setBounds(
                        rowX[5],
                        rowY + (this.rowHeights[rowIndex] 
                                    - c4.getPreferredSize().height) / 2,
                        this.columnWidths[5], c4.getPreferredSize().height
                    );
                    componentIndex = componentIndex + 5;
                    break;
                case FormatLayout.LCBLCB:
                    c0 = parent.getComponent(componentIndex);
                    c0.setBounds(
                        rowX[0],
                        rowY + (this.rowHeights[rowIndex] 
                                     - c0.getPreferredSize().height) / 2,
                        this.columnWidths[0], c0.getPreferredSize().height
                    );
                    c1 = parent.getComponent(componentIndex + 1);
                    c1.setBounds(
                        rowX[1],
                        rowY + (this.rowHeights[rowIndex] 
                                     - c1.getPreferredSize().height) / 2,
                        this.columnWidths[1], c1.getPreferredSize().height
                    );
                    c2 = parent.getComponent(componentIndex + 2);
                    c2.setBounds(
                        rowX[2],
                        rowY + (this.rowHeights[rowIndex] 
                                     - c2.getPreferredSize().height) / 2,
                        this.columnWidths[2], c2.getPreferredSize().height
                    );
                    c3 = parent.getComponent(componentIndex + 3);
                    c3.setBounds(
                        rowX[3],
                        rowY + (this.rowHeights[rowIndex] 
                                     - c3.getPreferredSize().height) / 2,
                        this.columnWidths[3], c3.getPreferredSize().height
                    );
                    c4 = parent.getComponent(componentIndex + 4);
                    c4.setBounds(
                        rowX[4],
                        rowY + (this.rowHeights[rowIndex] 
                                     - c4.getPreferredSize().height) / 2,
                        this.columnWidths[4], c4.getPreferredSize().height
                    );
                    c5 = parent.getComponent(componentIndex + 5);
                    c5.setBounds(
                        rowX[5],
                        rowY + (this.rowHeights[rowIndex] 
                                     - c5.getPreferredSize().height) / 2,
                        this.columnWidths[5], c5.getPreferredSize().height
                    );
                    componentIndex = componentIndex + 6;
                    break;
                    }
                rowY = rowY + this.rowHeights[rowIndex] + this.rowGap;
            }
        }
    }

    /**
     * Processes a row in 'C' format.
     *
     * @param rowIndex  the row index.
     * @param d0  dimension 0.
     */
    protected void updateC(final int rowIndex, final Dimension d0) {
        this.rowHeights[rowIndex] = d0.height;
        this.totalHeight = this.totalHeight + this.rowHeights[rowIndex];
        this.columns0to5Width = Math.max(this.columns0to5Width, d0.width);
    }

    /**
     * Processes a row in 'LC' format.
     *
     * @param rowIndex  the row index.
     * @param d0  dimension 0.
     * @param d1  dimension 1.
     */
    protected void updateLC(final int rowIndex, final Dimension d0, 
                            final Dimension d1) {

        this.rowHeights[rowIndex] = Math.max(d0.height, d1.height);
        this.totalHeight = this.totalHeight + this.rowHeights[rowIndex];
        this.columnWidths[0] = Math.max(this.columnWidths[0], d0.width);
        this.columns1to5Width = Math.max(this.columns1to5Width, d1.width);

    }

    /**
     * Processes a row in 'LCB' format.
     *
     * @param rowIndex  the row index.
     * @param d0  dimension 0.
     * @param d1  dimension 1.
     * @param d2  dimension 2.
     */
    protected void updateLCB(final int rowIndex,
                             final Dimension d0, final Dimension d1, 
                             final Dimension d2) {

        this.rowHeights[rowIndex] 
               = Math.max(d0.height, Math.max(d1.height, d2.height));
        this.totalHeight = this.totalHeight + this.rowHeights[rowIndex];
        this.columnWidths[0] = Math.max(this.columnWidths[0], d0.width);
        this.columns1to4Width = Math.max(this.columns1to4Width, d1.width);
        this.columnWidths[5] = Math.max(this.columnWidths[5], d2.width);

    }

    /**
     * Processes a row in 'LCLC' format.
     *
     * @param rowIndex  the row index.
     * @param d0  dimension 0.
     * @param d1  dimension 1.
     * @param d2  dimension 2.
     * @param d3  dimension 3.
     */
    protected void updateLCLC(final int rowIndex, final Dimension d0, 
                              final Dimension d1, final Dimension d2, 
                              final Dimension d3) {

        this.rowHeights[rowIndex] = Math.max(Math.max(d0.height, d1.height),
                                        Math.max(d2.height, d3.height));
        this.totalHeight = this.totalHeight + this.rowHeights[rowIndex];
        this.columnWidths[0] = Math.max(this.columnWidths[0], d0.width);
        this.columns1and2Width = Math.max(this.columns1and2Width, d1.width);
        this.columnWidths[3] = Math.max(this.columnWidths[3], d2.width);
        this.columns4and5Width = Math.max(this.columns4and5Width, d3.width);
    }

    /**
     * Processes a row in 'LCBLC' format.
     *
     * @param rowIndex  the row index.
     * @param d0  dimension 0.
     * @param d1  dimension 1.
     * @param d2  dimension 2.
     * @param d3  dimension 3.
     * @param d4  dimension 4.
     */
    protected void updateLCBLC(final int rowIndex, final Dimension d0, 
                               final Dimension d1, final Dimension d2, 
                               final Dimension d3, final Dimension d4) {

        this.rowHeights[rowIndex] = (Math.max(
            d0.height,
            Math.max(Math.max(d1.height, d2.height),
            Math.max(d3.height, d4.height)))
        );
        this.totalHeight = this.totalHeight + this.rowHeights[rowIndex];
        this.columnWidths[0] = Math.max(this.columnWidths[0], d0.width);
        this.columnWidths[1] = Math.max(this.columnWidths[1], d1.width);
        this.columnWidths[2] = Math.max(this.columnWidths[2], d2.width);
        this.columnWidths[3] = Math.max(this.columnWidths[3], d3.width);
        this.columns4and5Width = Math.max(this.columns4and5Width, d4.width);

    }

    /**
     * Processes a row in 'LCLCB' format.
     *
     * @param rowIndex  the row index.
     * @param d0  dimension 0.
     * @param d1  dimension 1.
     * @param d2  dimension 2.
     * @param d3  dimension 3.
     * @param d4  dimension 4.
     */
    protected void updateLCLCB(final int rowIndex, final Dimension d0, 
                               final Dimension d1, final Dimension d2,
                               final Dimension d3, final Dimension d4) {

        this.rowHeights[rowIndex] = (Math.max(d0.height,
                                     Math.max(Math.max(d1.height, d2.height),
                                              Math.max(d3.height, d4.height))));
        this.totalHeight = this.totalHeight + this.rowHeights[rowIndex];
        this.columnWidths[0] = Math.max(this.columnWidths[0], d0.width);
        this.columns1and2Width = Math.max(this.columns1and2Width, d1.width);
        this.columnWidths[3] = Math.max(this.columnWidths[3], d2.width);
        this.columnWidths[4] = Math.max(this.columnWidths[4], d3.width);
        this.columnWidths[5] = Math.max(this.columnWidths[5], d4.width);

    }

    /**
     * Processes a row in 'LCBLCB' format.
     *
     * @param rowIndex  the row index.
     * @param d0  dimension 0.
     * @param d1  dimension 1.
     * @param d2  dimension 2.
     * @param d3  dimension 3.
     * @param d4  dimension 4.
     * @param d5  dimension 5.
     */
    protected void updateLCBLCB(final int rowIndex,
                                final Dimension d0, final Dimension d1, 
                                final Dimension d2,
                                final Dimension d3, final Dimension d4, 
                                final Dimension d5) {

        this.rowHeights[rowIndex] = Math.max(
            Math.max(d0.height, d1.height),
            Math.max(Math.max(d2.height, d3.height),
                     Math.max(d4.height, d5.height))
        );
        this.totalHeight = this.totalHeight + this.rowHeights[rowIndex];
        this.columnWidths[0] = Math.max(this.columnWidths[0], d0.width);
        this.columnWidths[1] = Math.max(this.columnWidths[1], d1.width);
        this.columnWidths[2] = Math.max(this.columnWidths[2], d2.width);
        this.columnWidths[3] = Math.max(this.columnWidths[3], d3.width);
        this.columnWidths[4] = Math.max(this.columnWidths[4], d4.width);
        this.columnWidths[5] = Math.max(this.columnWidths[5], d5.width);

    }

    /**
     * Finishes of the processing.
     */
    public void complete() {

        this.columnWidths[1] = Math.max(
             this.columnWidths[1],
             this.columns1and2Width - this.columnGaps[1] - this.columnWidths[2]
        );

        this.columnWidths[4] = Math.max(
            this.columnWidths[4],
            Math.max(
                this.columns4and5Width - this.columnGaps[4] 
                - this.columnWidths[5],
                Math.max(
                    this.columns1to4Width - this.columnGaps[1] 
                        - this.columnGaps[2] - this.columnGaps[3] 
                        - this.columnWidths[1] - this.columnWidths[2] 
                        - this.columnWidths[3],
                    this.columns1to5Width - this.columnGaps[1] 
                        - this.columnGaps[2] - this.columnGaps[3] 
                        - this.columnWidths[1] - this.columnWidths[2] 
                        - this.columnWidths[3] - this.columnGaps[4]
                )
            )
        );

        int leftWidth = this.columnWidths[0] + this.columnGaps[0]
                      + this.columnWidths[1] + this.columnGaps[1] 
                      + this.columnWidths[2];

        int rightWidth = this.columnWidths[3] + this.columnGaps[3]
                       + this.columnWidths[4] + this.columnGaps[4] 
                       + this.columnWidths[5];

        if (splitLayout()) {
            if (leftWidth > rightWidth) {
                final int mismatch = leftWidth - rightWidth;
                this.columnWidths[4] = this.columnWidths[4] + mismatch;
                rightWidth = rightWidth + mismatch;
            }
            else {
                final int mismatch = rightWidth - leftWidth;
                this.columnWidths[1] = this.columnWidths[1] + mismatch;
                leftWidth = leftWidth + mismatch;
            }
        }

        this.totalWidth = leftWidth + this.columnGaps[2] + rightWidth;

        if (this.columns0to5Width > this.totalWidth) {
            final int spaceToAdd = (this.columns0to5Width - this.totalWidth);
            if (splitLayout()) {
                final int halfSpaceToAdd = spaceToAdd / 2;
                this.columnWidths[1] = this.columnWidths[1] + halfSpaceToAdd;
                this.columnWidths[4] = this.columnWidths[4] + spaceToAdd 
                    - halfSpaceToAdd;
                this.totalWidth = this.totalWidth + spaceToAdd;
            }
            else {
                this.columnWidths[1] = this.columnWidths[1] + spaceToAdd;
                this.totalWidth = this.totalWidth + spaceToAdd;
            }
        }

    }

    /**
     * Returns true if this layout involves a split into two sections.
     *
     * @return <code>true</code> if this layout involves a split into two 
     *         sections.
     */
    private boolean splitLayout() {
        for (int i = 0; i < this.rowFormats.length; i++) {
            if (this.rowFormats[i] > FormatLayout.LCB) {
                return true;
            }
        }
        return false;
    }

    /**
     * Not used.
     *
     * @param comp  the component.
     */
    public void addLayoutComponent(final Component comp) {
        // not used
    }

    /**
     * Not used.
     *
     * @param comp  the component.
     */
    public void removeLayoutComponent(final Component comp) {
        // not used
    }

    /**
     * Not used.
     *
     * @param name  the component name.
     * @param comp  the component.
     */
    public void addLayoutComponent(final String name, final Component comp) {
        // not used
    }

    /**
     * Not used.
     *
     * @param name  the component name.
     * @param comp  the component.
     */
    public void removeLayoutComponent(final String name, final Component comp) {
        // not used
    }

}
