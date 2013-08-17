package com.alimama.mdrill.jdbc;

import java.sql.SQLException;
import java.sql.Types;

public class Utils {

	  /**
	   * Convert hive types to sql types.
	   * @param type
	   * @return Integer java.sql.Types values
	   * @throws SQLException
	   */
	  public static int hiveTypeToSqlType(String type) throws SQLException {
	    if ("string".equalsIgnoreCase(type)) {
	      return Types.VARCHAR;
	    } else if ("float".equalsIgnoreCase(type)) {
	      return Types.FLOAT;
	    } else if ("double".equalsIgnoreCase(type)) {
	      return Types.DOUBLE;
	    } else if ("boolean".equalsIgnoreCase(type)) {
	      return Types.BOOLEAN;
	    } else if ("tinyint".equalsIgnoreCase(type)) {
	      return Types.TINYINT;
	    } else if ("smallint".equalsIgnoreCase(type)) {
	      return Types.SMALLINT;
	    } else if ("int".equalsIgnoreCase(type)) {
	      return Types.INTEGER;
	    } else if ("bigint".equalsIgnoreCase(type)) {
	      return Types.BIGINT;
	    } else if (type.startsWith("map<")) {
	      return Types.VARCHAR;
	    } else if (type.startsWith("array<")) {
	      return Types.VARCHAR;
	    } else if (type.startsWith("struct<")) {
	      return Types.VARCHAR;
	    }
	    throw new SQLException("Unrecognized column type: " + type);
	  }

}
