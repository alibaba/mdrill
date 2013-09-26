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
 * ------------------
 * JDBCXYDataset.java
 * ------------------
 * (C) Copyright 2002-2008, by Bryan Scott and Contributors.
 *
 * Original Author:  Bryan Scott;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Eric Alexander;
 *
 *
 * Changes
 * -------
 * 14-Mar-2002 : Version 1 contributed by Bryan Scott (DG);
 * 19-Apr-2002 : Updated executeQuery, to close cursors and to improve support
 *               for types.
 * 26-Apr-2002 : Renamed JdbcXYDataset to better fit in with the existing data
 *               source conventions.
 * 26-Apr-2002 : Changed to extend AbstractDataset.
 * 13-Aug-2002 : Updated Javadoc comments and imports (DG);
 * 18-Sep-2002 : Updated to support BIGINT (BS);
 * 21-Jan-2003 : Renamed JdbcXYDataset --> JDBCXYDataset (DG);
 * 01-Jul-2003 : Added support to query whether a timeseries (BS);
 * 30-Jul-2003 : Added empty contructor and executeQuery(connection,string)
 *               method (BS);
 * 24-Sep-2003 : Added a check to ensure at least two valid columns are
 *               returned by the query in executeQuery as suggest in online
 *               forum by anonymous (BS);
 * 02-Dec-2003 : Throwing exceptions allows to handle errors, removed default
 *               constructor, as without a connection, a query can never be
 *               executed.
 * 16-Mar-2004 : Added check for null values (EA);
 * 05-May-2004 : Now extends AbstractXYDataset (DG);
 * 21-May-2004 : Implemented TableXYDataset, added support for SMALLINT and
 *               fixed bug in code that determines the min and max values (see
 *               bug id 938138) (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 18-Nov-2004 : Updated for changes in RangeInfo interface (DG);
 * 11-Jan-2005 : Removed deprecated code in preparation for the 1.0.0
 *               release (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 17-Oct-2006 : Deprecated unused methods - see bug 1578293 (DG);
 *
 */

package org.jfree.data.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;

import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.Log;

/**
 * This class provides an {@link XYDataset} implementation over a database
 * JDBC result set.  The dataset is populated via a call to executeQuery with
 * the string sql query.  The sql query must return at least two columns.
 * The first column will be the x-axis and remaining columns y-axis values.
 * executeQuery can be called a number of times.
 *
 * The database connection is read-only and no write back facility exists.
 */
public class JDBCXYDataset extends AbstractXYDataset
        implements XYDataset, TableXYDataset, RangeInfo {

    /** The database connection. */
    private transient Connection connection;

    /** Column names. */
    private String[] columnNames = {};

    /** Rows. */
    private ArrayList rows;

    /** The maximum y value of the returned result set */
    private double maxValue = 0.0;

    /** The minimum y value of the returned result set */
    private double minValue = 0.0;

    /** Is this dataset a timeseries ? */
    private boolean isTimeSeries = false;

    /**
     * Creates a new JDBCXYDataset (initially empty) with no database
     * connection.
     */
    private JDBCXYDataset() {
        this.rows = new ArrayList();
    }

    /**
     * Creates a new dataset (initially empty) and establishes a new database
     * connection.
     *
     * @param  url  URL of the database connection.
     * @param  driverName  the database driver class name.
     * @param  user  the database user.
     * @param  password  the database user's password.
     *
     * @throws ClassNotFoundException if the driver cannot be found.
     * @throws SQLException if there is a problem connecting to the database.
     */
    public JDBCXYDataset(String url,
                         String driverName,
                         String user,
                         String password)
        throws SQLException, ClassNotFoundException {

        this();
        Class.forName(driverName);
        this.connection = DriverManager.getConnection(url, user, password);
    }

    /**
     * Creates a new dataset (initially empty) using the specified database
     * connection.
     *
     * @param  con  the database connection.
     *
     * @throws SQLException if there is a problem connecting to the database.
     */
    public JDBCXYDataset(Connection con) throws SQLException {
        this();
        this.connection = con;
    }

    /**
     * Creates a new dataset using the specified database connection, and
     * populates it using data obtained with the supplied query.
     *
     * @param con  the connection.
     * @param query  the SQL query.
     *
     * @throws SQLException if there is a problem executing the query.
     */
    public JDBCXYDataset(Connection con, String query) throws SQLException {
        this(con);
        executeQuery(query);
    }

    /**
     * Returns <code>true</code> if the dataset represents time series data,
     * and <code>false</code> otherwise.
     *
     * @return A boolean.
     */
    public boolean isTimeSeries() {
        return this.isTimeSeries;
    }

    /**
     * Sets a flag that indicates whether or not the data represents a time
     * series.
     *
     * @param timeSeries  the new value of the flag.
     */
    public void setTimeSeries(boolean timeSeries) {
        this.isTimeSeries = timeSeries;
    }

    /**
     * ExecuteQuery will attempt execute the query passed to it against the
     * existing database connection.  If no connection exists then no action
     * is taken.
     *
     * The results from the query are extracted and cached locally, thus
     * applying an upper limit on how many rows can be retrieved successfully.
     *
     * @param  query  the query to be executed.
     *
     * @throws SQLException if there is a problem executing the query.
     */
    public void executeQuery(String query) throws SQLException {
        executeQuery(this.connection, query);
    }

    /**
     * ExecuteQuery will attempt execute the query passed to it against the
     * provided database connection.  If connection is null then no action is
     * taken.
     *
     * The results from the query are extracted and cached locally, thus
     * applying an upper limit on how many rows can be retrieved successfully.
     *
     * @param  query  the query to be executed.
     * @param  con  the connection the query is to be executed against.
     *
     * @throws SQLException if there is a problem executing the query.
     */
    public void executeQuery(Connection con, String query)
        throws SQLException {

        if (con == null) {
            throw new SQLException(
                "There is no database to execute the query."
            );
        }

        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = con.createStatement();
            resultSet = statement.executeQuery(query);
            ResultSetMetaData metaData = resultSet.getMetaData();

            int numberOfColumns = metaData.getColumnCount();
            int numberOfValidColumns = 0;
            int [] columnTypes = new int[numberOfColumns];
            for (int column = 0; column < numberOfColumns; column++) {
                try {
                    int type = metaData.getColumnType(column + 1);
                    switch (type) {

                        case Types.NUMERIC:
                        case Types.REAL:
                        case Types.INTEGER:
                        case Types.DOUBLE:
                        case Types.FLOAT:
                        case Types.DECIMAL:
                        case Types.BIT:
                        case Types.DATE:
                        case Types.TIME:
                        case Types.TIMESTAMP:
                        case Types.BIGINT:
                        case Types.SMALLINT:
                            ++numberOfValidColumns;
                            columnTypes[column] = type;
                            break;
                        default:
                            Log.warn(
                                "Unable to load column "
                                + column + " (" + type + ","
                                + metaData.getColumnClassName(column + 1)
                                + ")"
                            );
                            columnTypes[column] = Types.NULL;
                            break;
                    }
                }
                catch (SQLException e) {
                    columnTypes[column] = Types.NULL;
                    throw e;
                }
            }


            if (numberOfValidColumns <= 1) {
                throw new SQLException(
                    "Not enough valid columns where generated by query."
                );
            }

            /// First column is X data
            this.columnNames = new String[numberOfValidColumns - 1];
            /// Get the column names and cache them.
            int currentColumn = 0;
            for (int column = 1; column < numberOfColumns; column++) {
                if (columnTypes[column] != Types.NULL) {
                    this.columnNames[currentColumn]
                        = metaData.getColumnLabel(column + 1);
                    ++currentColumn;
                }
            }

            // Might need to add, to free memory from any previous result sets
            if (this.rows != null) {
                for (int column = 0; column < this.rows.size(); column++) {
                    ArrayList row = (ArrayList) this.rows.get(column);
                    row.clear();
                }
                this.rows.clear();
            }

            // Are we working with a time series.
            switch (columnTypes[0]) {
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                    this.isTimeSeries = true;
                    break;
                default :
                    this.isTimeSeries = false;
                    break;
            }

            // Get all rows.
            // rows = new ArrayList();
            while (resultSet.next()) {
                ArrayList newRow = new ArrayList();
                for (int column = 0; column < numberOfColumns; column++) {
                    Object xObject = resultSet.getObject(column + 1);
                    switch (columnTypes[column]) {
                        case Types.NUMERIC:
                        case Types.REAL:
                        case Types.INTEGER:
                        case Types.DOUBLE:
                        case Types.FLOAT:
                        case Types.DECIMAL:
                        case Types.BIGINT:
                        case Types.SMALLINT:
                            newRow.add(xObject);
                            break;

                        case Types.DATE:
                        case Types.TIME:
                        case Types.TIMESTAMP:
                            newRow.add(new Long(((Date) xObject).getTime()));
                            break;
                        case Types.NULL:
                            break;
                        default:
                            System.err.println("Unknown data");
                            columnTypes[column] = Types.NULL;
                            break;
                    }
                }
                this.rows.add(newRow);
            }

            /// a kludge to make everything work when no rows returned
            if (this.rows.size() == 0) {
                ArrayList newRow = new ArrayList();
                for (int column = 0; column < numberOfColumns; column++) {
                    if (columnTypes[column] != Types.NULL) {
                        newRow.add(new Integer(0));
                    }
                }
                this.rows.add(newRow);
            }

            /// Determine max and min values.
            if (this.rows.size() < 1) {
                this.maxValue = 0.0;
                this.minValue = 0.0;
            }
            else {
                ArrayList row = (ArrayList) this.rows.get(0);
                this.maxValue = Double.NEGATIVE_INFINITY;
                this.minValue = Double.POSITIVE_INFINITY;
                for (int rowNum = 0; rowNum < this.rows.size(); ++rowNum) {
                    row = (ArrayList) this.rows.get(rowNum);
                    for (int column = 1; column < numberOfColumns; column++) {
                        Object testValue = row.get(column);
                        if (testValue != null) {
                            double test = ((Number) testValue).doubleValue();

                            if (test < this.minValue) {
                                this.minValue = test;
                            }
                            if (test > this.maxValue) {
                                this.maxValue = test;
                            }
                        }
                    }
                }
            }

            fireDatasetChanged(); // Tell the listeners a new table has arrived.
        }
        finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                }
                catch (Exception e) {
                    // TODO: is this a good idea?
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (Exception e) {
                    // TODO: is this a good idea?
                }
            }
        }

    }

    /**
     * Returns the x-value for the specified series and item.  The
     * implementation is responsible for ensuring that the x-values are
     * presented in ascending order.
     *
     * @param  seriesIndex  the series (zero-based index).
     * @param  itemIndex  the item (zero-based index).
     *
     * @return The x-value
     *
     * @see XYDataset
     */
    public Number getX(int seriesIndex, int itemIndex) {
        ArrayList row = (ArrayList) this.rows.get(itemIndex);
        return (Number) row.get(0);
    }

    /**
     * Returns the y-value for the specified series and item.
     *
     * @param  seriesIndex  the series (zero-based index).
     * @param  itemIndex  the item (zero-based index).
     *
     * @return The yValue value
     *
     * @see XYDataset
     */
    public Number getY(int seriesIndex, int itemIndex) {
        ArrayList row = (ArrayList) this.rows.get(itemIndex);
        return (Number) row.get(seriesIndex + 1);
    }

    /**
     * Returns the number of items in the specified series.
     *
     * @param  seriesIndex  the series (zero-based index).
     *
     * @return The itemCount value
     *
     * @see XYDataset
     */
    public int getItemCount(int seriesIndex) {
        return this.rows.size();
    }

    /**
     * Returns the number of items in all series.  This method is defined by
     * the {@link TableXYDataset} interface.
     *
     * @return The item count.
     */
    public int getItemCount() {
        return getItemCount(0);
    }

    /**
     * Returns the number of series in the dataset.
     *
     * @return The seriesCount value
     *
     * @see XYDataset
     * @see Dataset
     */
    public int getSeriesCount() {
        return this.columnNames.length;
    }

    /**
     * Returns the key for the specified series.
     *
     * @param seriesIndex  the series (zero-based index).
     *
     * @return The seriesName value
     *
     * @see XYDataset
     * @see Dataset
     */
    public Comparable getSeriesKey(int seriesIndex) {

        if ((seriesIndex < this.columnNames.length)
                && (this.columnNames[seriesIndex] != null)) {
            return this.columnNames[seriesIndex];
        }
        else {
            return "";
        }

    }

    /**
     * Returns the number of items that should be displayed in the legend.
     *
     * @return The legendItemCount value
     *
     * @deprecated This method is not used in JFreeChart 1.0.x (it was left in
     *     the API by mistake and is officially deprecated from version 1.0.3
     *     onwards).
     */
    public int getLegendItemCount() {
        return getSeriesCount();
    }

    /**
     * Returns the legend item labels.
     *
     * @return The legend item labels.
     *
     * @deprecated This method is not used in JFreeChart 1.0.x (it was left in
     *     the API by mistake and is officially deprecated from version 1.0.3
     *     onwards).
     */
    public String[] getLegendItemLabels() {
        return this.columnNames;
    }

    /**
     * Close the database connection
     */
    public void close() {

        try {
            this.connection.close();
        }
        catch (Exception e) {
            System.err.println("JdbcXYDataset: swallowing exception.");
        }

    }

    /**
     * Returns the minimum y-value in the dataset.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         y-interval is taken into account.
     *
     * @return The minimum value.
     */
    public double getRangeLowerBound(boolean includeInterval) {
        return this.minValue;
    }

    /**
     * Returns the maximum y-value in the dataset.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         y-interval is taken into account.
     *
     * @return The maximum value.
     */
    public double getRangeUpperBound(boolean includeInterval) {
        return this.maxValue;
    }

    /**
     * Returns the range of the values in this dataset's range.
     *
     * @param includeInterval  a flag that determines whether or not the
     *                         y-interval is taken into account.
     *
     * @return The range.
     */
    public Range getRangeBounds(boolean includeInterval) {
        return new Range(this.minValue, this.maxValue);
    }

}
