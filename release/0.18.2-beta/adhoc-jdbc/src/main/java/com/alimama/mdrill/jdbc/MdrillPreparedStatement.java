package com.alimama.mdrill.jdbc;


import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;



public class MdrillPreparedStatement implements PreparedStatement{

	private final String sql;
	private String httpurl;

	public MdrillPreparedStatement(String client, String sql) {
		this.httpurl = client;
		this.sql = sql;
	}


	  private final HashMap<Integer, String> parameters=new HashMap<Integer, String>();


	  private ResultSet resultSet = null;

	  private  int maxRows = 0;


	  private  SQLWarning warningChain = null;

	  private boolean isClosed = false;

	  private final int updateCount=0;

	  public void addBatch() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public void clearParameters() throws SQLException {
	    this.parameters.clear();
	  }

	  public boolean execute() throws SQLException {
	    ResultSet rs = executeImmediate(sql);
	    return rs != null;
	  }

	  public ResultSet executeQuery() throws SQLException {
	    return executeImmediate(sql);
	  }


	  public int executeUpdate() throws SQLException {
	    executeImmediate(sql);
	    return updateCount;
	  }

	protected ResultSet executeImmediate(String sql) throws SQLException {
		if (isClosed) {
			throw new SQLException(
					"Can't execute after statement has been closed");
		}

		SqlParser parse = new SqlParser();
		Long total = 0l;
		List<List<Object>> results = new ArrayList<List<Object>>();
		try {
			clearWarnings();
			if (sql.contains("?")) {
				sql = updateSql(sql, parameters);
			}
			resultSet = null;
			parse.parse(sql);
			MdrillRequest request = new MdrillRequest(parse, this.httpurl);
			total = request.request(results);
			if (total < 0) {
				throw new Exception("server exception");
			}
		} catch (Exception ex) {
			throw new SQLException(ex.toString(), "08S01");
		}
		resultSet = new MdrillQueryResultSet(parse, results, total);
		return resultSet;

	}

	  /**
	   * update the SQL string with parameters set by setXXX methods of {@link PreparedStatement}
	   *
	   * @param sql
	   * @param parameters
	   * @return updated SQL string
	   */
	  private String updateSql(final String sql, HashMap<Integer, String> parameters) {

	    StringBuffer newSql = new StringBuffer(sql);

	    int paramLoc = 1;
	    while (getCharIndexFromSqlByParamLocation(sql, '?', paramLoc) > 0) {
	      // check the user has set the needs parameters
	      if (parameters.containsKey(paramLoc)) {
	        int tt = getCharIndexFromSqlByParamLocation(newSql.toString(), '?', 1);
	        newSql.deleteCharAt(tt);
	        newSql.insert(tt, parameters.get(paramLoc));
	      }
	      paramLoc++;
	    }

	    return newSql.toString();

	  }

	  /**
	   * Get the index of given char from the SQL string by parameter location
	   * </br> The -1 will be return, if nothing found
	   *
	   * @param sql
	   * @param cchar
	   * @param paramLoc
	   * @return
	   */
	  private int getCharIndexFromSqlByParamLocation(final String sql, final char cchar, final int paramLoc) {
	    int signalCount = 0;
	    int charIndex = -1;
	    int num = 0;
	    for (int i = 0; i < sql.length(); i++) {
	      char c = sql.charAt(i);
	      if (c == '\'' || c == '\\')// record the count of char "'" and char "\"
	      {
	        signalCount++;
	      } else if (c == cchar && signalCount % 2 == 0) {// check if the ? is really the parameter
	        num++;
	        if (num == paramLoc) {
	          charIndex = i;
	          break;
	        }
	      }
	    }
	    return charIndex;
	  }



	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#getMetaData()
	   */

	  public ResultSetMetaData getMetaData() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#getParameterMetaData()
	   */

	  public ParameterMetaData getParameterMetaData() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
	   */

	  public void setArray(int i, Array x) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)
	   */

	  public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream,
	   * int)
	   */

	  public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream,
	   * long)
	   */

	  public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
	   */

	  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)
	   */

	  public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream,
	   * int)
	   */

	  public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream,
	   * long)
	   */

	  public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
	   */

	  public void setBlob(int i, Blob x) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)
	   */

	  public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)
	   */

	  public void setBlob(int parameterIndex, InputStream inputStream, long length)
	          throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setBoolean(int, boolean)
	   */

	  public void setBoolean(int parameterIndex, boolean x) throws SQLException {
	    this.parameters.put(parameterIndex, ""+x);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setByte(int, byte)
	   */

	  public void setByte(int parameterIndex, byte x) throws SQLException {
	    this.parameters.put(parameterIndex, ""+x);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setBytes(int, byte[])
	   */

	  public void setBytes(int parameterIndex, byte[] x) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)
	   */

	  public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader,
	   * int)
	   */

	  public void setCharacterStream(int parameterIndex, Reader reader, int length)
	      throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader,
	   * long)
	   */

	  public void setCharacterStream(int parameterIndex, Reader reader, long length)
	      throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
	   */

	  public void setClob(int i, Clob x) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)
	   */

	  public void setClob(int parameterIndex, Reader reader) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
	   */

	  public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
	   */

	  public void setDate(int parameterIndex, Date x) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setDate(int, java.sql.Date,
	   * java.util.Calendar)
	   */

	  public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setDouble(int, double)
	   */

	  public void setDouble(int parameterIndex, double x) throws SQLException {
	    this.parameters.put(parameterIndex,""+x);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setFloat(int, float)
	   */

	  public void setFloat(int parameterIndex, float x) throws SQLException {
	    this.parameters.put(parameterIndex,""+x);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setInt(int, int)
	   */

	  public void setInt(int parameterIndex, int x) throws SQLException {
	    this.parameters.put(parameterIndex,""+x);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setLong(int, long)
	   */

	  public void setLong(int parameterIndex, long x) throws SQLException {
	    this.parameters.put(parameterIndex,""+x);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)
	   */

	  public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader,
	   * long)
	   */

	  public void setNCharacterStream(int parameterIndex, Reader value, long length)
	      throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
	   */

	  public void setNClob(int parameterIndex, NClob value) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)
	   */

	  public void setNClob(int parameterIndex, Reader reader) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)
	   */

	  public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setNString(int, java.lang.String)
	   */

	  public void setNString(int parameterIndex, String value) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setNull(int, int)
	   */

	  public void setNull(int parameterIndex, int sqlType) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
	   */

	  public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
	   */

	  public void setObject(int parameterIndex, Object x) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
	   */

	  public void setObject(int parameterIndex, Object x, int targetSqlType)
	      throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)
	   */

	  public void setObject(int parameterIndex, Object x, int targetSqlType, int scale)
	      throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
	   */

	  public void setRef(int i, Ref x) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)
	   */

	  public void setRowId(int parameterIndex, RowId x) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
	   */

	  public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setShort(int, short)
	   */

	  public void setShort(int parameterIndex, short x) throws SQLException {
	    this.parameters.put(parameterIndex,""+x);
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setString(int, java.lang.String)
	   */

	  public void setString(int parameterIndex, String x) throws SQLException {
	     x=x.replace("'", "\\'");
	     this.parameters.put(parameterIndex,"'"+x+"'");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
	   */

	  public void setTime(int parameterIndex, Time x) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setTime(int, java.sql.Time,
	   * java.util.Calendar)
	   */

	  public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
	   */

	  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp,
	   * java.util.Calendar)
	   */

	  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
	      throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
	   */

	  public void setURL(int parameterIndex, URL x) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.PreparedStatement#setUnicodeStream(int, java.io.InputStream,
	   * int)
	   */

	  public void setUnicodeStream(int parameterIndex, InputStream x, int length)
	      throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#addBatch(java.lang.String)
	   */

	  public void addBatch(String sql) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#cancel()
	   */

	  public void cancel() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#clearBatch()
	   */

	  public void clearBatch() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#clearWarnings()
	   */

	  public void clearWarnings() throws SQLException {
	     warningChain=null;
	  }

	  /**
	   *  Closes the prepared statement.
	   *
	   *  @throws
	   */

	  public void close() throws SQLException {
	    if (resultSet!=null) {
	      resultSet.close();
	      resultSet = null;
	    }
	    isClosed = true;
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#execute(java.lang.String)
	   */

	  public boolean execute(String sql) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#execute(java.lang.String, int)
	   */

	  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#execute(java.lang.String, int[])
	   */

	  public boolean execute(String sql, int[] columnIndexes) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
	   */

	  public boolean execute(String sql, String[] columnNames) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#executeBatch()
	   */

	  public int[] executeBatch() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#executeQuery(java.lang.String)
	   */

	  public ResultSet executeQuery(String sql) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#executeUpdate(java.lang.String)
	   */

	  public int executeUpdate(String sql) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#executeUpdate(java.lang.String, int)
	   */

	  public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
	   */

	  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
	   */

	  public int executeUpdate(String sql, String[] columnNames) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getConnection()
	   */

	  public Connection getConnection() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getFetchDirection()
	   */

	  public int getFetchDirection() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getFetchSize()
	   */

	  public int getFetchSize() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getGeneratedKeys()
	   */

	  public ResultSet getGeneratedKeys() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getMaxFieldSize()
	   */

	  public int getMaxFieldSize() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getMaxRows()
	   */

	  public int getMaxRows() throws SQLException {
	    return this.maxRows;
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getMoreResults()
	   */

	  public boolean getMoreResults() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getMoreResults(int)
	   */

	  public boolean getMoreResults(int current) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getQueryTimeout()
	   */

	  public int getQueryTimeout() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getResultSet()
	   */

	  public ResultSet getResultSet() throws SQLException {
	    return this.resultSet;
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getResultSetConcurrency()
	   */

	  public int getResultSetConcurrency() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getResultSetHoldability()
	   */

	  public int getResultSetHoldability() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getResultSetType()
	   */

	  public int getResultSetType() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getUpdateCount()
	   */

	  public int getUpdateCount() throws SQLException {
	    return updateCount;
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#getWarnings()
	   */

	  public SQLWarning getWarnings() throws SQLException {
	    return warningChain;
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#isClosed()
	   */

	  public boolean isClosed() throws SQLException {
	    return isClosed;
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#isPoolable()
	   */

	  public boolean isPoolable() throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#setCursorName(java.lang.String)
	   */

	  public void setCursorName(String name) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#setEscapeProcessing(boolean)
	   */

	  public void setEscapeProcessing(boolean enable) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#setFetchDirection(int)
	   */

	  public void setFetchDirection(int direction) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#setFetchSize(int)
	   */

	  public void setFetchSize(int rows) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#setMaxFieldSize(int)
	   */

	  public void setMaxFieldSize(int max) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#setMaxRows(int)
	   */

	  public void setMaxRows(int max) throws SQLException {
	    if (max < 0) {
	      throw new SQLException("max must be >= 0");
	    }
	    this.maxRows = max;
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#setPoolable(boolean)
	   */

	  public void setPoolable(boolean poolable) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Statement#setQueryTimeout(int)
	   */

	  public void setQueryTimeout(int seconds) throws SQLException {
	    // TODO Auto-generated method stub
	    // throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	   */

	  public boolean isWrapperFor(Class<?> iface) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }

	  /*
	   * (non-Javadoc)
	   * 
	   * @see java.sql.Wrapper#unwrap(java.lang.Class)
	   */

	  public <T> T unwrap(Class<T> iface) throws SQLException {
	    // TODO Auto-generated method stub
	    throw new SQLException("Method not supported");
	  }



}
