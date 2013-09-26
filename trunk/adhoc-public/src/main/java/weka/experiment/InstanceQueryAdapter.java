/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    InstanceQueryAdapter.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.experiment;

/**
 * An interface implemented by InstanceQuery and any user class that is
 * to be passed as the first argument to
 * InstanceQuery.retrieveInstances(InstanceQueryAdapter, ResultSet).
 *
 * @author Wes Munsil (wes_munsil@cytoanalytics.com)
 * @version $Revision: 7522 $
 */
public interface InstanceQueryAdapter
{
  /**
   * returns key column headings in their original case. Used for
   * those databases that create uppercase column names.
   *
   * @param columnName    the column to retrieve the original case for
   * @return        the original case
   */
  public String attributeCaseFix(String columnName);

  /**
   * Gets whether there should be printed some debugging output to stderr or not.
   *
   * @return         true if output should be printed
   */
  public boolean getDebug();

  /**
   * Gets whether data is to be returned as a set of sparse instances
   * @return true if data is to be encoded as sparse instances
   */
  public boolean getSparseData();

  /**
   * translates the column data type string to an integer value that indicates
   * which data type / get()-Method to use in order to retrieve values from the
   * database (see DatabaseUtils.Properties, InstanceQuery()). Blanks in the type
   * are replaced with underscores "_", since Java property names can't contain blanks.
   *
   * @param type     the column type as retrieved with
   *             java.sql.MetaData.getColumnTypeName(int)
   * @return         an integer value that indicates
   *             which data type / get()-Method to use in order to
   *             retrieve values from the
   */
  public int translateDBColumnType(String type);
}
