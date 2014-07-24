package com.alimama.mdrill.jdbc;


import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class MdrillDriver  implements Driver{
	static {
	    try {
	      java.sql.DriverManager.registerDriver(new MdrillDriver());
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	  }
	
	  private static final String URL_PREFIX = "jdbc:mdrill://";
	  private static final String URL_PREFIX_OLD = "jdbc:higo://";
	
	
	public Connection connect(String url, Properties info) throws SQLException {
		return new MdrillConnection(url, null);
	}

	 public boolean acceptsURL(String url) throws SQLException {
		    return Pattern.matches(URL_PREFIX + ".*", url)||Pattern.matches(URL_PREFIX_OLD + ".*", url);
	}

	
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		 DriverPropertyInfo infoProp = new DriverPropertyInfo("url",url);
	    DriverPropertyInfo[] dpi = new DriverPropertyInfo[1];
	    dpi[0]=infoProp;
		return dpi;
	}

	
	public int getMajorVersion() {
		return 1;
	}

	
	public int getMinorVersion() {
		return 1;
	}

	
	public boolean jdbcCompliant() {
		return false;
	}

	
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

}
