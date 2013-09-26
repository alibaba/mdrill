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
 * ArffTableModel.java
 * Copyright (C) 2005 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.arffviewer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Undoable;
import weka.core.Utils;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Reorder;
import weka.gui.ComponentHelper;

/**
 * The model for the Arff-Viewer.
 *
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 7555 $ 
 */
public class ArffTableModel 
  extends DefaultTableModel
  implements Undoable {
  
  /** for serialization. */
  private static final long serialVersionUID = 3411795562305994946L;
  
  /** the listeners */
  protected HashSet m_Listeners;
  
  /** the data */
  protected Instances m_Data;
  
  /** whether notfication is enabled */
  protected boolean m_NotificationEnabled;
  
  /** whether undo is active */
  protected boolean m_UndoEnabled;
  
  /** whether to ignore changes, i.e. not adding to undo history */
  protected boolean m_IgnoreChanges;
  
  /** the undo list (contains temp. filenames) */
  protected Vector m_UndoList;
  
  /** whether the table is read-only */
  protected boolean m_ReadOnly;
  
  /** whether to display the attribute index in the table header. */
  protected boolean m_ShowAttributeIndex;
  
  /** for caching long relational and string values that get processed for display. */
  protected Hashtable<String,String> m_Cache;
  
  /**
   * performs some initialization
   */
  private ArffTableModel() {
    super();
    
    m_Listeners           = new HashSet();
    m_Data                = null;
    m_NotificationEnabled = true;
    m_UndoList            = new Vector();
    m_IgnoreChanges       = false;
    m_UndoEnabled         = true;
    m_ReadOnly            = false;
    m_ShowAttributeIndex  = false;
    m_Cache               = new Hashtable<String,String>();
  }
  
  /**
   * initializes the object and loads the given file
   * 
   * @param filename	the file to load
   */
  public ArffTableModel(String filename) {
    this();
    
    if ( (filename != null) && (!filename.equals("")) )
      loadFile(filename);
  }
  
  /**
   * initializes the model with the given data
   * 
   * @param data 	the data to use
   */
  public ArffTableModel(Instances data) {
    this();
    
    this.m_Data = data;
  }

  /**
   * returns whether the notification of changes is enabled
   * 
   * @return 		true if notification of changes is enabled
   */
  public boolean isNotificationEnabled() {
    return m_NotificationEnabled;
  }
  
  /**
   * sets whether the notification of changes is enabled
   * 
   * @param enabled	enables/disables the notification
   */
  public void setNotificationEnabled(boolean enabled) {
    m_NotificationEnabled = enabled;
  }

  /**
   * returns whether undo support is enabled
   * 
   * @return 		true if undo support is enabled
   */
  public boolean isUndoEnabled() {
    return m_UndoEnabled;
  }
  
  /**
   * sets whether undo support is enabled
   * 
   * @param enabled	whether to enable/disable undo support
   */
  public void setUndoEnabled(boolean enabled) {
    m_UndoEnabled = enabled;
  }

  /**
   * returns whether the model is read-only
   * 
   * @return 		true if model is read-only
   */
  public boolean isReadOnly() {
    return m_ReadOnly;
  }
  
  /**
   * sets whether the model is read-only
   * 
   * @param value	if true the model is set to read-only
   */
  public void setReadOnly(boolean value) {
    m_ReadOnly = value;
  }
  
  /**
   * loads the specified ARFF file
   * 
   * @param filename	the file to load
   */
  protected void loadFile(String filename) {
    AbstractFileLoader          loader;
    
    loader = ConverterUtils.getLoaderForFile(filename);
    
    if (loader != null) {
      try {
        loader.setFile(new File(filename));
        setInstances(loader.getDataSet());
      }
      catch (Exception e) {
        ComponentHelper.showMessageBox(
            null, 
            "Error loading file...", 
            e.toString(), 
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE );
        System.out.println(e);
        setInstances(null);
      }
    }
  }
  
  /**
   * sets the data
   * 
   * @param data	the data to use
   */
  public void setInstances(Instances data) {
    m_Data = data;
    m_Cache.clear();
    fireTableDataChanged();
  }
  
  /**
   * returns the data
   * 
   * @return		the current data
   */
  public Instances getInstances() {
    return m_Data;
  }
  
  /**
   * returns the attribute at the given index, can be NULL if not an attribute
   * column
   * 
   * @param columnIndex		the index of the column
   * @return			the attribute at the position
   */
  public Attribute getAttributeAt(int columnIndex) {
    if ( (columnIndex > 0) && (columnIndex < getColumnCount()) )
      return m_Data.attribute(columnIndex - 1);
    else
      return null;
  }
  
  /**
   * returns the TYPE of the attribute at the given position
   * 
   * @param columnIndex		the index of the column
   * @return			the attribute type
   */
  public int getType(int columnIndex) {
    return getType(0, columnIndex);
  }
  
  /**
   * returns the TYPE of the attribute at the given position
   * 
   * @param rowIndex		the index of the row
   * @param columnIndex		the index of the column
   * @return			the attribute type
   */
  public int getType(int rowIndex, int columnIndex) {
    int            result;
    
    result = Attribute.STRING;
    
    if (    (rowIndex >= 0) && (rowIndex < getRowCount())
         && (columnIndex > 0) && (columnIndex < getColumnCount()) )
      result = m_Data.instance(rowIndex).attribute(columnIndex - 1).type();
    
    return result;
  }
  
  /**
   * deletes the attribute at the given col index. notifies the listeners.
   * 
   * @param columnIndex     the index of the attribute to delete
   */
  public void deleteAttributeAt(int columnIndex) {
    deleteAttributeAt(columnIndex, true);
  }
  
  /**
   * deletes the attribute at the given col index
   * 
   * @param columnIndex     the index of the attribute to delete
   * @param notify          whether to notify the listeners
   */
  public void deleteAttributeAt(int columnIndex, boolean notify) {
    if ( (columnIndex > 0) && (columnIndex < getColumnCount()) ) {
      if (!m_IgnoreChanges)
        addUndoPoint();
      m_Data.deleteAttributeAt(columnIndex - 1);
      if (notify) 
        notifyListener(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }
  }
  
  /**
   * deletes the attributes at the given indices
   * 
   * @param columnIndices	the column indices
   */
  public void deleteAttributes(int[] columnIndices) {
    int            i;
    
    Arrays.sort(columnIndices);
    
    addUndoPoint();

    m_IgnoreChanges = true;
    for (i = columnIndices.length - 1; i >= 0; i--)
      deleteAttributeAt(columnIndices[i], false);
    m_IgnoreChanges = false;

    notifyListener(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
  }
  
  /**
   * renames the attribute at the given col index
   * 
   * @param columnIndex		the index of the column
   * @param newName		the new name of the attribute
   */
  public void renameAttributeAt(int columnIndex, String newName) {
    if ( (columnIndex > 0) && (columnIndex < getColumnCount()) ) {
      addUndoPoint();
      m_Data.renameAttribute(columnIndex - 1, newName);
      notifyListener(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }
  }
  
  /**
   * sets the attribute at the given col index as the new class attribute, i.e.
   * it moves it to the end of the attributes
   * 
   * @param columnIndex		the index of the column
   */
  public void attributeAsClassAt(int columnIndex) {
    Reorder     reorder;
    String      order;
    int         i;
    
    if ( (columnIndex > 0) && (columnIndex < getColumnCount()) ) {
      addUndoPoint();
      
      try {
        // build order string (1-based!)
        order = "";
        for (i = 1; i < m_Data.numAttributes() + 1; i++) {
          // skip new class
          if (i == columnIndex)
            continue;
          
          if (!order.equals(""))
            order += ",";
          order += Integer.toString(i);
        }
        if (!order.equals(""))
          order += ",";
        order += Integer.toString(columnIndex);
        
        // process data
        reorder = new Reorder();
        reorder.setAttributeIndices(order);
        reorder.setInputFormat(m_Data);
        m_Data = Filter.useFilter(m_Data, reorder);
        
        // set class index
        m_Data.setClassIndex(m_Data.numAttributes() - 1);
      }
      catch (Exception e) {
        e.printStackTrace();
        undo();
      }
      
      notifyListener(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }
  }
  
  /**
   * deletes the instance at the given index
   * 
   * @param rowIndex		the index of the row
   */
  public void deleteInstanceAt(int rowIndex) {
    deleteInstanceAt(rowIndex, true);
  }
  
  /**
   * deletes the instance at the given index
   * 
   * @param rowIndex		the index of the row
   * @param notify		whether to notify the listeners
   */
  public void deleteInstanceAt(int rowIndex, boolean notify) {
    if ( (rowIndex >= 0) && (rowIndex < getRowCount()) ) {
      if (!m_IgnoreChanges)
        addUndoPoint();
      m_Data.delete(rowIndex);
      if (notify)
        notifyListener(
            new TableModelEvent(
                this, rowIndex, rowIndex, 
                TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
    }
  }
  
  /**
   * deletes the instances at the given positions
   * 
   * @param rowIndices		the indices to delete
   */
  public void deleteInstances(int[] rowIndices) {
    int               i;
    
    Arrays.sort(rowIndices);
    
    addUndoPoint();
    
    m_IgnoreChanges = true;
    for (i = rowIndices.length - 1; i >= 0; i--)
      deleteInstanceAt(rowIndices[i], false);
    m_IgnoreChanges = false;

    notifyListener(
        new TableModelEvent(
            this, rowIndices[0], rowIndices[rowIndices.length - 1], 
            TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
  }
  
  /**
   * sorts the instances via the given attribute
   * 
   * @param columnIndex		the index of the column
   */
  public void sortInstances(int columnIndex) {
    if ( (columnIndex > 0) && (columnIndex < getColumnCount()) ) {
      addUndoPoint();
      m_Data.sort(columnIndex - 1);
      notifyListener(new TableModelEvent(this));
    }
  }
  
  /**
   * returns the column of the given attribute name, -1 if not found
   * 
   * @param name		the name of the attribute
   * @return			the column index or -1 if not found
   */
  public int getAttributeColumn(String name) {
    int            i;
    int            result;
    
    result = -1;
    
    for (i = 0; i < m_Data.numAttributes(); i++) {
      if (m_Data.attribute(i).name().equals(name)) {
        result = i + 1;
        break;
      }
    }
    
    return result;
  }
  
  /**
   * returns the most specific superclass for all the cell values in the 
   * column (always String)
   * 
   * @param columnIndex		the column index
   * @return			the class of the column
   */
  public Class getColumnClass(int columnIndex) {
    Class       result;
    
    result = null;
    
    if ( (columnIndex >= 0) && (columnIndex < getColumnCount()) ) {
      if (columnIndex == 0)
        result = Integer.class;
      else if (getType(columnIndex) == Attribute.NUMERIC)
        result = Double.class;
      else
        result = String.class;   // otherwise no input of "?"!!!
    }
    
    return result;
  }
  
  /**
   * returns the number of columns in the model
   * 
   * @return		the number of columns
   */
  public int getColumnCount() {
    int         result;
    
    result = 1;
    if (m_Data != null)
      result += m_Data.numAttributes();
    
    return result;
  }
  
  /**
   * checks whether the column represents the class or not
   * 
   * @param columnIndex		the index of the column
   * @return			true if the column is the class attribute
   */
  protected boolean isClassIndex(int columnIndex) {
    boolean        result;
    int            index;
    
    index  = m_Data.classIndex();
    result =    ((index == - 1) && (m_Data.numAttributes() == columnIndex))
             || (index == columnIndex - 1);
    
    return result;
  }
  
  /**
   * returns the name of the column at columnIndex
   * 
   * @param columnIndex		the index of the column
   * @return			the name of the column
   */
  public String getColumnName(int columnIndex) {
    String      result;
    
    result = "";
    
    if ( (columnIndex >= 0) && (columnIndex < getColumnCount()) ) {
      if (columnIndex == 0) {
        result = "<html><center>No.<br><font size=\"-2\">&nbsp;</font></center></html>";
      }
      else {
        if (m_Data != null) {
          if ( (columnIndex - 1 < m_Data.numAttributes()) ) {
            result = "<html><center>";

            // index
            if (m_ShowAttributeIndex)
              result += columnIndex + ": ";
            
            // name
            if (isClassIndex(columnIndex))
              result +=   "<b>" 
                + m_Data.attribute(columnIndex - 1).name() 
                + "</b>";
            else
              result += m_Data.attribute(columnIndex - 1).name();
            
            // attribute type
            switch (getType(columnIndex)) {
              case Attribute.DATE: 
                result += "<br><font size=\"-2\">Date</font>";
                break;
              case Attribute.NOMINAL:
                result += "<br><font size=\"-2\">Nominal</font>";
                break;
              case Attribute.STRING:
                result += "<br><font size=\"-2\">String</font>";
                break;
              case Attribute.NUMERIC:
                result += "<br><font size=\"-2\">Numeric</font>";
                break;
              case Attribute.RELATIONAL:
                result += "<br><font size=\"-2\">Relational</font>";
                break;
              default:
                result += "<br><font size=\"-2\">???</font>";
            }
            
            result += "</center></html>";
          }
        }
      }
    }
    
    return result;
  }
  
  /**
   * returns the number of rows in the model
   * 
   * @return		the number of rows
   */
  public int getRowCount() {
    if (m_Data == null)
      return 0;
    else
      return m_Data.numInstances(); 
  }
  
  /**
   * checks whether the value at the given position is missing
   * 
   * @param rowIndex		the row index
   * @param columnIndex		the column index
   * @return			true if the value at the position is missing
   */
  public boolean isMissingAt(int rowIndex, int columnIndex) {
    boolean           result;
    
    result = false;
    
    if (    (rowIndex >= 0) && (rowIndex < getRowCount())
         && (columnIndex > 0) && (columnIndex < getColumnCount()) )
      result = (m_Data.instance(rowIndex).isMissing(columnIndex - 1));
    
    return result;
  }
  
  /**
   * returns the double value of the underlying Instances object at the
   * given position, -1 if out of bounds
   * 
   * @param rowIndex		the row index
   * @param columnIndex		the column index
   * @return			the underlying value in the Instances object
   */
  public double getInstancesValueAt(int rowIndex, int columnIndex) {
    double	result;
    
    result = -1;
    
    if (    (rowIndex >= 0) && (rowIndex < getRowCount())
         && (columnIndex > 0) && (columnIndex < getColumnCount()) )
      result = m_Data.instance(rowIndex).value(columnIndex - 1);
    
    return result;
  }
  
  /**
   * returns the value for the cell at columnindex and rowIndex
   * 
   * @param rowIndex		the row index
   * @param columnIndex		the column index
   * @return 			the value at the position
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    Object	result;
    String	tmp;
    String	key;
    boolean	modified;
    
    result = null;
    key    = rowIndex + "-" + columnIndex;
    
    if (    (rowIndex >= 0) && (rowIndex < getRowCount())
        && (columnIndex >= 0) && (columnIndex < getColumnCount()) ) {
      if (columnIndex == 0) {
        result = new Integer(rowIndex + 1);
      }
      else {
        if (isMissingAt(rowIndex, columnIndex)) {
          result = null;
        }
        else {
          if (m_Cache.containsKey(key)) {
            result = m_Cache.get(key);
          }
          else {
            switch (getType(columnIndex)) {
              case Attribute.DATE: 
              case Attribute.NOMINAL:
              case Attribute.STRING:
              case Attribute.RELATIONAL:
        	result = m_Data.instance(rowIndex).stringValue(columnIndex - 1);
        	break;
              case Attribute.NUMERIC:
        	result = new Double(m_Data.instance(rowIndex).value(columnIndex - 1));
        	break;
              default:
        	result = "-can't display-";
            }
            
            if (getType(columnIndex) != Attribute.NUMERIC) {
              if (result != null) {
                tmp      = result.toString();
                modified = false;
                // fix html tags, otherwise Java parser hangs
                if ((tmp.indexOf('<') > -1) || (tmp.indexOf('>') > -1)) {
                  tmp = tmp.replace("<", "(");
                  tmp = tmp.replace(">", ")");
                  modified = true;
                }
                // does it contain "\n" or "\r"? -> replace with red html tag
                if ( (tmp.indexOf("\n") > -1) || (tmp.indexOf("\r") > -1) ) {
                  tmp = tmp.replaceAll("\\r\\n", "<font color=\"red\"><b>\\\\r\\\\n</b></font>");
                  tmp = tmp.replaceAll("\\r", "<font color=\"red\"><b>\\\\r</b></font>");
                  tmp = tmp.replaceAll("\\n", "<font color=\"red\"><b>\\\\n</b></font>");
                  tmp = "<html>" + tmp + "</html>";
                  modified = true;
                }
                result = tmp;
                if (modified)
                  m_Cache.put(key, tmp);
              }
            }
          }
        }
      }
    }
    
    return result;
  }
  
  /**
   * returns true if the cell at rowindex and columnindexis editable
   * 
   * @param rowIndex		the index of the row
   * @param columnIndex		the index of the column
   * @return			true if the cell is editable
   */
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return (columnIndex > 0) && !isReadOnly();
  }
  
  /**
   * sets the value in the cell at columnIndex and rowIndex to aValue.
   * but only the value and the value can be changed
   * 
   * @param aValue		the new value
   * @param rowIndex		the row index
   * @param columnIndex		the column index
   */
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    setValueAt(aValue, rowIndex, columnIndex, true);
  }
  
  /**
   * sets the value in the cell at columnIndex and rowIndex to aValue.
   * but only the value and the value can be changed
   * 
   * @param aValue		the new value
   * @param rowIndex		the row index
   * @param columnIndex		the column index
   * @param notify		whether to notify the listeners
   */
  public void setValueAt(Object aValue, int rowIndex, int columnIndex, boolean notify) {
    int            type;
    int            index;
    String         tmp;
    Instance       inst;
    Attribute      att;
    Object         oldValue;
    
    if (!m_IgnoreChanges)
      addUndoPoint();
    
    oldValue = getValueAt(rowIndex, columnIndex);
    type     = getType(rowIndex, columnIndex);
    index    = columnIndex - 1;
    inst     = m_Data.instance(rowIndex);
    att      = inst.attribute(index);
    
    // missing?
    if (aValue == null) {
      inst.setValue(index, Utils.missingValue());
    }
    else {
      tmp = aValue.toString();
      
      switch (type) {
        case Attribute.DATE:
          try {
            att.parseDate(tmp);
            inst.setValue(index, att.parseDate(tmp));
          }
          catch (Exception e) {
            // ignore
          }
          break;
      
        case Attribute.NOMINAL:
          if (att.indexOfValue(tmp) > -1)
            inst.setValue(index, att.indexOfValue(tmp));
          break;
      
        case Attribute.STRING:
          inst.setValue(index, tmp);
          break;
      
        case Attribute.NUMERIC:
          try {
            Double.parseDouble(tmp);
            inst.setValue(index, Double.parseDouble(tmp));
          }
          catch (Exception e) {
            // ignore
          }
          break;
          
        case Attribute.RELATIONAL:
          try {
            inst.setValue(index, inst.attribute(index).addRelation((Instances) aValue));
          }
          catch (Exception e) {
            // ignore
          }
          break;
          
        default:
          throw new IllegalArgumentException("Unsupported Attribute type: " + type + "!");
      }
    }
    
    // notify only if the value has changed!
    if (notify && (!("" + oldValue).equals("" + aValue)) )
        notifyListener(new TableModelEvent(this, rowIndex, columnIndex));
  }
  
  /**
   * adds a listener to the list that is notified each time a change to data 
   * model occurs
   * 
   * @param l		the listener to add
   */
  public void addTableModelListener(TableModelListener l) {
    m_Listeners.add(l);
  }
  
  /**
   * removes a listener from the list that is notified each time a change to
   * the data model occurs
   * 
   * @param l		the listener to remove
   */
  public void removeTableModelListener(TableModelListener l) {
    m_Listeners.remove(l);
  }
  
  /**
   * notfies all listener of the change of the model
   * 
   * @param e		the event to send to the listeners
   */
  public void notifyListener(TableModelEvent e) {
    Iterator                iter;
    TableModelListener      l;

    // is notification enabled?
    if (!isNotificationEnabled())
      return;
    
    iter = m_Listeners.iterator();
    while (iter.hasNext()) {
      l = (TableModelListener) iter.next();
      l.tableChanged(e);
    }
  }

  /**
   * removes the undo history
   */
  public void clearUndo() {
    m_UndoList = new Vector();
  }
  
  /**
   * returns whether an undo is possible, i.e. whether there are any undo points
   * saved so far
   * 
   * @return returns TRUE if there is an undo possible 
   */
  public boolean canUndo() {
    return !m_UndoList.isEmpty();
  }
  
  /**
   * undoes the last action
   */
  public void undo() {
    File                  tempFile;
    Instances             inst;
    ObjectInputStream     ooi;
    
    if (canUndo()) {
      // load file
      tempFile = (File) m_UndoList.get(m_UndoList.size() - 1);
      try {
        // read serialized data
        ooi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
        inst = (Instances) ooi.readObject();
        ooi.close();
        
        // set instances
        setInstances(inst);
        notifyListener(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
        notifyListener(new TableModelEvent(this));
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      tempFile.delete();
      
      // remove from undo
      m_UndoList.remove(m_UndoList.size() - 1);
    }
  }
  
  /**
   * adds an undo point to the undo history, if the undo support is enabled
   * @see #isUndoEnabled()
   * @see #setUndoEnabled(boolean)
   */
  public void addUndoPoint() {
    File                  tempFile;
    ObjectOutputStream    oos;

    // undo support currently on?
    if (!isUndoEnabled())
      return;
    
    if (getInstances() != null) {
      try {
        // temp. filename
        tempFile = File.createTempFile("arffviewer", null);
        tempFile.deleteOnExit();
        
        // serialize instances
        oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
        oos.writeObject(getInstances());
        oos.flush();
        oos.close();
        
        // add to undo list
        m_UndoList.add(tempFile);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Sets whether to display the attribute index in the header.
   * 
   * @param value	if true then the attribute indices are displayed in the
   * 			table header
   */
  public void setShowAttributeIndex(boolean value) {
    m_ShowAttributeIndex = value;
    fireTableStructureChanged();
  }
  
  /**
   * Returns whether to display the attribute index in the header.
   * 
   * @return		true if the attribute indices are displayed in the
   * 			table header
   */
  public boolean getShowAttributeIndex() {
    return m_ShowAttributeIndex;
  }
}
