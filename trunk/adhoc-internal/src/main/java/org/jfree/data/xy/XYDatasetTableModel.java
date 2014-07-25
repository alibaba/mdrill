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
 * ------------------------
 * XYDatasetTableModel.java
 * ------------------------
 * (C)opyright 2003-2008, by Bryan Scott and Contributors.
 *
 * Original Author:  Bryan Scott ;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 01-Jul-2003 : Version 1 contributed by Bryan Scott (DG);
 * 27-Apr-2005 : Change XYDataset --> TableXYDataset because the table model
 *               assumes all series share the same x-values, and this is not
 *               enforced by XYDataset.  Also fixed bug 1191046, a problem
 *               in the getValueAt() method (DG);
 *
 */

package org.jfree.data.xy;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;

/**
 * A READ-ONLY wrapper around a {@link TableXYDataset} to convert it to a
 * table model for use in a JTable.  The first column of the table shows the
 * x-values, the remaining columns show the y-values for each series (series 0
 * appears in column 1, series 1 appears in column 2, etc).
 * <P>
 * TO DO:
 * <ul>
 * <li>implement proper naming for x axis (getColumnName)</li>
 * <li>implement setValueAt to remove READ-ONLY constraint (not sure how)</li>
 * </ul>
 */
public class XYDatasetTableModel extends AbstractTableModel
        implements TableModel, DatasetChangeListener  {

    /** The dataset. */
    TableXYDataset model = null;

    /**
     * Default constructor.
     */
    public XYDatasetTableModel() {
        super();
    }

    /**
     * Creates a new table model based on the specified dataset.
     *
     * @param dataset  the dataset.
     */
    public XYDatasetTableModel(TableXYDataset dataset) {
        this();
        this.model = dataset;
        this.model.addChangeListener(this);
    }

    /**
     * Sets the model (dataset).
     *
     * @param dataset  the dataset.
     */
    public void setModel(TableXYDataset dataset) {
        this.model = dataset;
        this.model.addChangeListener(this);
        fireTableDataChanged();
    }

    /**
     * Returns the number of rows.
     *
     * @return The row count.
     */
    public int getRowCount() {
        if (this.model == null) {
            return 0;
        }
        return this.model.getItemCount();
    }

    /**
     * Gets the number of columns in the model.
     *
     * @return The number of columns in the model.
     */
    public int getColumnCount() {
        if (this.model == null) {
            return 0;
        }
        return this.model.getSeriesCount() + 1;
    }

    /**
     * Returns the column name.
     *
     * @param column  the column index.
     *
     * @return The column name.
     */
    public String getColumnName(int column) {
        if (this.model == null) {
            return super.getColumnName(column);
        }
        if (column < 1) {
            return "X Value";
        }
        else {
            return this.model.getSeriesKey(column - 1).toString();
        }
    }

    /**
     * Returns a value of the specified cell.
     * Column 0 is the X axis, Columns 1 and over are the Y axis
     *
     * @param row  the row number.
     * @param column  the column number.
     *
     * @return The value of the specified cell.
     */
    public Object getValueAt(int row, int column) {
        if (this.model == null) {
            return null;
        }
        if (column < 1) {
            return this.model.getX(0, row);
        }
        else {
            return this.model.getY(column - 1, row);
        }
    }

    /**
     * Receives notification that the underlying dataset has changed.
    *
     * @param event  the event
     *
     * @see DatasetChangeListener
     */
    public void datasetChanged(DatasetChangeEvent event) {
        fireTableDataChanged();
    }

    /**
     * Returns a flag indicating whether or not the specified cell is editable.
     *
     * @param row  the row number.
     * @param column  the column number.
     *
     * @return <code>true</code> if the specified cell is editable.
     */
    public boolean isCellEditable(int row, int column) {
        return false;
   }

    /**
     * Updates the {@link XYDataset} if allowed.
     *
     * @param value  the new value.
     * @param row  the row.
     * @param column  the column.
     */
    public void setValueAt(Object value, int row, int column) {
        if (isCellEditable(row, column)) {
            // XYDataset only provides methods for reading a dataset...
        }
    }

//    /**
//     * Run a demonstration of the table model interface.
//     *
//     * @param args  ignored.
//     *
//     * @throws Exception when an error occurs.
//     */
//    public static void main(String args[]) throws Exception {
//        JFrame frame = new JFrame();
//        JPanel panel = new JPanel();
//        panel.setLayout(new BorderLayout());
//
//        XYSeries s1 = new XYSeries("Series 1", true, false);
//        for (int i = 0; i < 10; i++) {
//            s1.add(i, Math.random());
//        }
//        XYSeries s2 = new XYSeries("Series 2", true, false);
//        for (int i = 0; i < 15; i++) {
//            s2.add(i, Math.random());
//        }
//        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
//        dataset.addSeries(s1);
//        dataset.addSeries(s2);
//        XYDatasetTableModel tablemodel = new XYDatasetTableModel();
//
//        tablemodel.setModel(dataset);
//
//        JTable dataTable = new JTable(tablemodel);
//        JScrollPane scroll = new JScrollPane(dataTable);
//        scroll.setPreferredSize(new Dimension(600, 150));
//
//        JFreeChart chart = ChartFactory.createXYLineChart(
//            "XY Series Demo",
//            "X", "Y", dataset, PlotOrientation.VERTICAL,
//            true,
//            true,
//            false
//        );
//
//        ChartPanel chartPanel = new ChartPanel(chart);
//
//        panel.add(chartPanel, BorderLayout.CENTER);
//        panel.add(scroll, BorderLayout.SOUTH);
//
//        frame.setContentPane(panel);
//        frame.setSize(600, 500);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.show();
//        RefineryUtilities.centerFrameOnScreen(frame);
//    }

}
