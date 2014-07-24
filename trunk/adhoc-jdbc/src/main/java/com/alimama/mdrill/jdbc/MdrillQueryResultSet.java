package com.alimama.mdrill.jdbc;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class MdrillQueryResultSet extends MdrillBaseResultSet{

	  public static final Log LOG = LogFactory.getLog(MdrillQueryResultSet.class);
	  private int fetchSize = 50;
	  private Iterator<List<Object>> fetchedRowsItr;
	  private SqlParser parser;
	  Long total;
	  public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public MdrillQueryResultSet(SqlParser parser,List<List<Object>> results,Long total) throws SQLException {
		this.setTotal(total);
	    this.parser = parser;
	    this.fetchedRowsItr=results.iterator();
	    init();
	    row = Arrays.asList(new Object[parser.colsAliasNames.length]);
	  }

	  Properties props= new Properties();
	  private void init() throws SQLException {
	    try {
	      columnNames = new ArrayList<String>();
	      columnTypes = new ArrayList<String>();

	      for(int i=0;i<parser.colsAliasNames.length;i++)
	      {
	    	  String aliasName=parser.colsAliasNames[i];
	    	  String type="STRING";
	    	  columnNames.add(aliasName);
	    	  columnTypes.add(type);
	      }

	    } catch (Exception ex) {
	      ex.printStackTrace();
	      throw new SQLException("Could not create ResultSet: " + ex.getMessage(), ex);
	    }
	  }

	  
	  public void close() throws SQLException {
	    
	  }

	  public boolean next() throws SQLException {
	    if(this.fetchedRowsItr.hasNext())
	    {
	    	this.row=this.fetchedRowsItr.next();
	    	return true;
	    }

	    return false;
	  }

	  
	  public void setFetchSize(int rows) throws SQLException {
	    fetchSize = rows;
	  }

	  
	  public int getFetchSize() throws SQLException {
	    return fetchSize;
	  }

	
	public <T> T getObject(int arg0, Class<T> arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public <T> T getObject(String arg0, Class<T> arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	  
}
