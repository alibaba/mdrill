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
 * ----------------
 * ObjectTable.java
 * ----------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: ObjectTable.java,v 1.10 2008/09/10 09:22:04 mungady Exp $
 *
 * Changes
 * -------
 * 29-Apr-2003 : Version 1, based on PaintTable class (DG);
 * 21-May-2003 : Copied the array based implementation of StrokeTable and
 *               fixed the serialisation behaviour (TM).
 */

package org.jfree.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * A lookup table for objects. This implementation is not synchronized, it is up
 * to the caller to synchronize it properly.
 *
 * @author Thomas Morgner
 */
public class ObjectTable implements Serializable
{

  /**
   * For serialization.
   */
  private static final long serialVersionUID = -3968322452944912066L;

  /**
   * The number of rows.
   */
  private int rows;

  /**
   * The number of columns.
   */
  private int columns;

  /**
   * An array of objects.  The array may contain <code>null</code> values.
   */
  private transient Object[][] data;

  /**
   * Defines how many object-slots get reserved each time we run out of
   * space.
   */
  private int rowIncrement;

  /**
   * Defines how many object-slots get reserved each time we run out of
   * space.
   */
  private int columnIncrement;

  /**
   * Creates a new table.
   */
  public ObjectTable()
  {
    this(5, 5);
  }

  /**
   * Creates a new table.
   *
   * @param increment the row and column size increment.
   */
  public ObjectTable(final int increment)
  {
    this(increment, increment);
  }

  /**
   * Creates a new table.
   *
   * @param rowIncrement the row size increment.
   * @param colIncrement the column size increment.
   */
  public ObjectTable(final int rowIncrement, final int colIncrement)
  {
    if (rowIncrement < 1)
    {
      throw new IllegalArgumentException("Increment must be positive.");
    }

    if (colIncrement < 1)
    {
      throw new IllegalArgumentException("Increment must be positive.");
    }

    this.rows = 0;
    this.columns = 0;
    this.rowIncrement = rowIncrement;
    this.columnIncrement = colIncrement;

    this.data = new Object[rowIncrement][];
  }

  /**
   * Returns the column size increment.
   *
   * @return the increment.
   */
  public int getColumnIncrement()
  {
    return this.columnIncrement;
  }

  /**
   * Returns the row size increment.
   *
   * @return the increment.
   */
  public int getRowIncrement()
  {
    return this.rowIncrement;
  }

  /**
   * Checks that there is storage capacity for the specified row and resizes
   * if necessary.
   *
   * @param row the row index.
   */
  protected void ensureRowCapacity(final int row)
  {

    // does this increase the number of rows?  if yes, create new storage
    if (row >= this.data.length)
    {

      final Object[][] enlarged = new Object[row + this.rowIncrement][];
      System.arraycopy(this.data, 0, enlarged, 0, this.data.length);
      // do not create empty arrays - this is more expensive than checking
      // for null-values.
      this.data = enlarged;
    }
  }

  /**
   * Ensures that there is storage capacity for the specified item.
   *
   * @param row    the row index.
   * @param column the column index.
   */
  public void ensureCapacity(final int row, final int column)
  {

    if (row < 0)
    {
      throw new IndexOutOfBoundsException("Row is invalid. " + row);
    }
    if (column < 0)
    {
      throw new IndexOutOfBoundsException("Column is invalid. " + column);
    }

    ensureRowCapacity(row);

    final Object[] current = this.data[row];
    if (current == null)
    {
      final Object[] enlarged
          = new Object[Math.max(column + 1, this.columnIncrement)];
      this.data[row] = enlarged;
    }
    else if (column >= current.length)
    {
      final Object[] enlarged = new Object[column + this.columnIncrement];
      System.arraycopy(current, 0, enlarged, 0, current.length);
      this.data[row] = enlarged;
    }
  }

  /**
   * Returns the number of rows in the table.
   *
   * @return The row count.
   */
  public int getRowCount()
  {
    return this.rows;
  }

  /**
   * Returns the number of columns in the table.
   *
   * @return The column count.
   */
  public int getColumnCount()
  {
    return this.columns;
  }

  /**
   * Returns the object from a particular cell in the table. Returns null, if
   * there is no object at the given position.
   * <p/>
   * Note: throws IndexOutOfBoundsException if row or column is negative.
   *
   * @param row    the row index (zero-based).
   * @param column the column index (zero-based).
   * @return The object.
   */
  protected Object getObject(final int row, final int column)
  {

    if (row < this.data.length)
    {
      final Object[] current = this.data[row];
      if (current == null)
      {
        return null;
      }
      if (column < current.length)
      {
        return current[column];
      }
    }
    return null;

  }

  /**
   * Sets the object for a cell in the table.  The table is expanded if
   * necessary.
   *
   * @param row    the row index (zero-based).
   * @param column the column index (zero-based).
   * @param object the object.
   */
  protected void setObject(final int row, final int column,
                           final Object object)
  {

    ensureCapacity(row, column);

    this.data[row][column] = object;
    this.rows = Math.max(this.rows, row + 1);
    this.columns = Math.max(this.columns, column + 1);
  }

  /**
   * Tests this paint table for equality with another object (typically also
   * an <code>ObjectTable</code>).
   *
   * @param o the other object.
   * @return A boolean.
   */
  public boolean equals(final Object o)
  {

    if (o == null)
    {
      return false;
    }

    if (this == o)
    {
      return true;
    }

    if ((o instanceof ObjectTable) == false)
    {
      return false;
    }

    final ObjectTable ot = (ObjectTable) o;
    if (getRowCount() != ot.getRowCount())
    {
      return false;
    }

    if (getColumnCount() != ot.getColumnCount())
    {
      return false;
    }

    for (int r = 0; r < getRowCount(); r++)
    {
      for (int c = 0; c < getColumnCount(); c++)
      {
        if (ObjectUtilities.equal(getObject(r, c),
            ot.getObject(r, c)) == false)
        {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Returns a hash code value for the object.
   *
   * @return the hashcode
   */
  public int hashCode()
  {
    int result;
    result = this.rows;
    result = 29 * result + this.columns;
    return result;
  }

  /**
   * Handles serialization.
   *
   * @param stream the output stream.
   * @throws java.io.IOException if there is an I/O problem.
   */
  private void writeObject(final ObjectOutputStream stream)
      throws IOException
  {
    stream.defaultWriteObject();
    final int rowCount = this.data.length;
    stream.writeInt(rowCount);
    for (int r = 0; r < rowCount; r++)
    {
      final Object[] column = this.data[r];
      stream.writeBoolean(column != null);
      if (column != null)
      {
        final int columnCount = column.length;
        stream.writeInt(columnCount);
        for (int c = 0; c < columnCount; c++)
        {
          writeSerializedData(stream, column[c]);
        }
      }
    }
  }

  /**
   * Handles the serialization of an single element of this table.
   *
   * @param stream the stream which should write the object
   * @param o      the object that should be serialized
   * @throws IOException if an IO error occured
   */
  protected void writeSerializedData(final ObjectOutputStream stream,
                                     final Object o)
      throws IOException
  {
    stream.writeObject(o);
  }

  /**
   * Restores a serialized object.
   *
   * @param stream the input stream.
   * @throws java.io.IOException    if there is an I/O problem.
   * @throws ClassNotFoundException if a class cannot be found.
   */
  private void readObject(final ObjectInputStream stream)
      throws IOException, ClassNotFoundException
  {
    stream.defaultReadObject();
    final int rowCount = stream.readInt();
    this.data = new Object[rowCount][];
    for (int r = 0; r < rowCount; r++)
    {
      final boolean isNotNull = stream.readBoolean();
      if (isNotNull)
      {
        final int columnCount = stream.readInt();
        final Object[] column = new Object[columnCount];
        this.data[r] = column;
        for (int c = 0; c < columnCount; c++)
        {
          column[c] = readSerializedData(stream);
        }
      }
    }
  }

  /**
   * Handles the deserialization of a single element of the table.
   *
   * @param stream the object input stream from which to read the object.
   * @return the deserialized object
   * @throws ClassNotFoundException if a class cannot be found.
   * @throws IOException            Any of the usual Input/Output related
   *                                exceptions.
   */
  protected Object readSerializedData(final ObjectInputStream stream)
      throws ClassNotFoundException, IOException
  {
    return stream.readObject();
  }

  /**
   * Clears the table.
   */
  public void clear()
  {
    this.rows = 0;
    this.columns = 0;
    for (int i = 0; i < this.data.length; i++)
    {
      if (this.data[i] != null)
      {
        Arrays.fill(this.data[i], null);
      }
    }
  }

  /**
   * Copys the contents of the old column to the new column.
   *
   * @param oldColumn the index of the old (source) column
   * @param newColumn the index of the new column
   */
  protected void copyColumn(final int oldColumn, final int newColumn)
  {
    for (int i = 0; i < getRowCount(); i++)
    {
      setObject(i, newColumn, getObject(i, oldColumn));
    }
  }

  /**
   * Copys the contents of the old row to the new row. This uses raw access to
   * the data and is remarkably faster than manual copying.
   *
   * @param oldRow the index of the old row
   * @param newRow the index of the new row
   */
  protected void copyRow(final int oldRow, final int newRow)
  {
    this.ensureCapacity(newRow, getColumnCount());
    final Object[] oldRowStorage = this.data[oldRow];
    if (oldRowStorage == null)
    {
      final Object[] newRowStorage = this.data[newRow];
      if (newRowStorage != null)
      {
        Arrays.fill(newRowStorage, null);
      }
    }
    else
    {
      this.data[newRow] = (Object[]) oldRowStorage.clone();
    }
  }

  /**
   * Sets the table data.
   *
   * @param data  the data.
   * @param colCount  the number of columns.
   */
  protected void setData(final Object[][] data, final int colCount)
  {
    if (data == null) {
      throw new NullPointerException();
    }
    if (colCount < 0) {
      throw new IndexOutOfBoundsException();
    }

    this.data = data;
    this.rows = data.length;
    this.columns = colCount;
  }

  /**
   * Returns the table data.
   *
   * @return The table data.
   */
  protected Object[][] getData()
  {
    return this.data;
  }
}

