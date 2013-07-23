package com.alimama.mdrill.jdbc;



import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class MdrillDatabaseMetaData implements java.sql.DatabaseMetaData {
	private String strurl;

	public MdrillDatabaseMetaData(String strurl) {
		this.strurl = strurl;
	}

	  private static final String CATALOG_SEPARATOR = ".";

	  private static final char SEARCH_STRING_ESCAPE = '\\';

	  //  The maximum column length = MFieldSchema.FNAME in metastore/src/model/package.jdo
	  private static final int maxColumnNameLength = 128;



	  public boolean allProceduresAreCallable() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean allTablesAreSelectable() throws SQLException {
	    return true;
	  }

	  public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean deletesAreDetected(int type) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public ResultSet getAttributes(String catalog, String schemaPattern,
	      String typeNamePattern, String attributeNamePattern) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public ResultSet getBestRowIdentifier(String catalog, String schema,
	      String table, int scope, boolean nullable) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public String getCatalogSeparator() throws SQLException {
	    return CATALOG_SEPARATOR;
	  }

	  public String getCatalogTerm() throws SQLException {
	    return "database";
	  }

	  public ResultSet getCatalogs() throws SQLException {
	    try {
	      // TODO a client call to get the schema's after HIVE-675 is implemented
	      final List<String> catalogs = new ArrayList<String>();
	      catalogs.add("default");
	      return new MdrillMetaDataResultSet<String>(Arrays.asList("TABLE_CAT")
	              , Arrays.asList("STRING")
	              , catalogs) {
	        private int cnt = 0;

	        public boolean next() throws SQLException {
	          if (cnt<data.size()) {
	            List<Object> a = new ArrayList<Object>(1);
	            a.add(data.get(cnt)); // TABLE_CAT String => table catalog (may be null)
	            row = a;
	            cnt++;
	            return true;
	          } else {
	            return false;
	          }
	        }
	      };
	    } catch (Exception e) {
	      throw new SQLException(e);
	    }
	  }

	  public ResultSet getClientInfoProperties() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public ResultSet getColumnPrivileges(String catalog, String schema,
	      String table, String columnNamePattern) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  /**
	   * Convert a pattern containing JDBC catalog search wildcards into
	   * Java regex patterns.
	   *
	   * @param pattern input which may contain '%' or '_' wildcard characters, or
	   * these characters escaped using {@link #getSearchStringEscape()}.
	   * @return replace %/_ with regex search characters, also handle escaped
	   * characters.
	   */
	  private String convertPattern(final String pattern) {
	    if (pattern==null) {
	      return ".*";
	    } else {
	      StringBuilder result = new StringBuilder(pattern.length());

	      boolean escaped = false;
	      for (int i = 0, len = pattern.length(); i < len; i++) {
	        char c = pattern.charAt(i);
	        if (escaped) {
	          if (c != SEARCH_STRING_ESCAPE) {
	            escaped = false;
	          }
	          result.append(c);
	        } else {
	          if (c == SEARCH_STRING_ESCAPE) {
	            escaped = true;
	            continue;
	          } else if (c == '%') {
	            result.append(".*");
	          } else if (c == '_') {
	            result.append('.');
	          } else {
	            result.append(c);
	          }
	        }
	      }

	      return result.toString();
	    }
	  }

	  public ResultSet getColumns(String catalog, final String schemaPattern
	          , final String tableNamePattern
	          , final String columnNamePattern) throws SQLException {
		  throw new SQLException("Method not supported");
//		  
//	    List<JdbcColumn> columns = new ArrayList<JdbcColumn>();
//	    try {
//	      if (catalog==null) {
//	        catalog = "default";
//	      }
//
//	      String regtableNamePattern = convertPattern(tableNamePattern);
//	      String regcolumnNamePattern = convertPattern(columnNamePattern);
//
//	      List<String> tables = client.get_tables(catalog, "*");
//	      for (String table: tables) {
//	        if (table.matches(regtableNamePattern)) {
//	          List<FieldSchema> fields = client.get_schema(catalog, table);
//	          int ordinalPos = 1;
//	          for (FieldSchema field: fields) {
//	            if (field.getName().matches(regcolumnNamePattern)) {
//	              columns.add(new JdbcColumn(field.getName(), table, catalog
//	                      , field.getType(), field.getComment(), ordinalPos));
//	              ordinalPos++;
//	            }
//	          }
//	        }
//	      }
//	      Collections.sort(columns, new GetColumnsComparator());
//
//	      return new HigoMetaDataResultSet<JdbcColumn>(
//	              Arrays.asList("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "DATA_TYPE"
//	                      , "TYPE_NAME", "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS"
//	                      , "NUM_PREC_RADIX", "NULLABLE", "REMARKS", "COLUMN_DEF", "SQL_DATA_TYPE"
//	                      , "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION"
//	                      , "IS_NULLABLE", "SCOPE_CATLOG", "SCOPE_SCHEMA", "SCOPE_TABLE"
//	                      , "SOURCE_DATA_TYPE")
//	              , Arrays.asList("STRING", "STRING", "STRING", "STRING", "INT", "STRING"
//	                , "INT", "INT", "INT", "INT", "INT", "STRING"
//	                , "STRING", "INT", "INT", "INT", "INT"
//	                , "STRING", "STRING", "STRING", "STRING", "INT")
//	              , columns) {
//
//	        private int cnt = 0;
//
//	        public boolean next() throws SQLException {
//	          if (cnt<data.size()) {
//	            List<Object> a = new ArrayList<Object>(20);
//	            JdbcColumn column = data.get(cnt);
//	            a.add(column.getTableCatalog()); // TABLE_CAT String => table catalog (may be null)
//	            a.add(null); // TABLE_SCHEM String => table schema (may be null)
//	            a.add(column.getTableName()); // TABLE_NAME String => table name
//	            a.add(column.getColumnName()); // COLUMN_NAME String => column name
//	            a.add(column.getSqlType()); // DATA_TYPE short => SQL type from java.sql.Types
//	            a.add(column.getType()); // TYPE_NAME String => Data source dependent type name.
//	            a.add(column.getColumnSize()); // COLUMN_SIZE int => column size.
//	            a.add(null); // BUFFER_LENGTH is not used.
//	            a.add(column.getDecimalDigits()); // DECIMAL_DIGITS int => number of fractional digits
//	            a.add(column.getNumPrecRadix()); // NUM_PREC_RADIX int => typically either 10 or 2
//	            a.add(DatabaseMetaData.columnNullable); // NULLABLE int => is NULL allowed?
//	            a.add(column.getComment()); // REMARKS String => comment describing column (may be null)
//	            a.add(null); // COLUMN_DEF String => default value (may be null)
//	            a.add(null); // SQL_DATA_TYPE int => unused
//	            a.add(null); // SQL_DATETIME_SUB int => unused
//	            a.add(null); // CHAR_OCTET_LENGTH int
//	            a.add(column.getOrdinalPos()); // ORDINAL_POSITION int
//	            a.add("YES"); // IS_NULLABLE String
//	            a.add(null); // SCOPE_CATLOG String
//	            a.add(null); // SCOPE_SCHEMA String
//	            a.add(null); // SCOPE_TABLE String
//	            a.add(null); // SOURCE_DATA_TYPE short
//	            row = a;
//	            cnt++;
//	            return true;
//	          } else {
//	            return false;
//	          }
//	        }
//	      };
//	    } catch (Exception e) {
//	      throw new SQLException(e);
//	    }
	  }

	  /**
	   * We sort the output of getColumns to guarantee jdbc compliance.
	   * First check by table name then by ordinal position
	   */
	  private class GetColumnsComparator implements Comparator<JdbcColumn> {

	    public int compare(JdbcColumn o1, JdbcColumn o2) {
	      int compareName = o1.getTableName().compareTo(o2.getTableName());
	      if (compareName==0) {
	        if (o1.getOrdinalPos() > o2.getOrdinalPos()) {
	          return 1;
	        } else if (o1.getOrdinalPos() < o2.getOrdinalPos()) {
	          return -1;
	        }
	        return 0;
	      } else {
	        return compareName;
	      }
	    }
	  }

	  public Connection getConnection() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public ResultSet getCrossReference(String primaryCatalog,
	      String primarySchema, String primaryTable, String foreignCatalog,
	      String foreignSchema, String foreignTable) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getDatabaseMajorVersion() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getDatabaseMinorVersion() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public String getDatabaseProductName() throws SQLException {
	    return "Hive";
	  }

	  public String getDatabaseProductVersion() throws SQLException {
	    return "1.0";
	  }

	  public int getDefaultTransactionIsolation() throws SQLException {
	    return Connection.TRANSACTION_NONE;
	  }

	  public int getDriverMajorVersion() {
	    return 1;
	  }

	  public int getDriverMinorVersion() {
		  return 1;
	  }

	  public String getDriverName() throws SQLException {
	    return "higo.jdbc";
	  }

	  public String getDriverVersion() throws SQLException {
	    return "1.0";
	  }

	  public ResultSet getExportedKeys(String catalog, String schema, String table)
	      throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public String getExtraNameCharacters() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public ResultSet getFunctionColumns(String arg0, String arg1, String arg2,
	      String arg3) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public ResultSet getFunctions(String arg0, String arg1, String arg2)
	      throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public String getIdentifierQuoteString() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public ResultSet getImportedKeys(String catalog, String schema, String table)
	      throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public ResultSet getIndexInfo(String catalog, String schema, String table,
	      boolean unique, boolean approximate) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getJDBCMajorVersion() throws SQLException {
	    return 3;
	  }

	  public int getJDBCMinorVersion() throws SQLException {
	    return 0;
	  }

	  public int getMaxBinaryLiteralLength() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxCatalogNameLength() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxCharLiteralLength() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  /**
	   *  Returns the value of maxColumnNameLength.
	   *
	   *  @param int
	   */
	  public int getMaxColumnNameLength() throws SQLException {
	    return maxColumnNameLength;
	  }

	  public int getMaxColumnsInGroupBy() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxColumnsInIndex() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxColumnsInOrderBy() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxColumnsInSelect() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxColumnsInTable() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxConnections() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxCursorNameLength() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxIndexLength() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxProcedureNameLength() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxRowSize() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxSchemaNameLength() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxStatementLength() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxStatements() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxTableNameLength() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxTablesInSelect() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getMaxUserNameLength() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public String getNumericFunctions() throws SQLException {
	    return "";
	  }

	  public ResultSet getPrimaryKeys(String catalog, String schema, String table)
	      throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public ResultSet getProcedureColumns(String catalog, String schemaPattern,
	      String procedureNamePattern, String columnNamePattern)
	      throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public String getProcedureTerm() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public ResultSet getProcedures(String catalog, String schemaPattern,
	      String procedureNamePattern) throws SQLException {
	    return null;
	  }

	  public int getResultSetHoldability() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public RowIdLifetime getRowIdLifetime() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public String getSQLKeywords() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public int getSQLStateType() throws SQLException {
	    return DatabaseMetaData.sqlStateSQL99;
	  }

	  public String getSchemaTerm() throws SQLException {
	    return "";
	  }

	  public ResultSet getSchemas() throws SQLException {
	    return getSchemas(null, null);
	  }

	  public ResultSet getSchemas(String catalog, String schemaPattern)
	      throws SQLException {
	    return new MdrillMetaDataResultSet(Arrays.asList("TABLE_SCHEM", "TABLE_CATALOG")
	            , Arrays.asList("STRING", "STRING"), null) {

	      public boolean next() throws SQLException {
	        return false;
	      }
	    };

	  }

	  public String getSearchStringEscape() throws SQLException {
	    return String.valueOf(SEARCH_STRING_ESCAPE);
	  }

	  public String getStringFunctions() throws SQLException {
	    return "";
	  }

	  public ResultSet getSuperTables(String catalog, String schemaPattern,
	      String tableNamePattern) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public ResultSet getSuperTypes(String catalog, String schemaPattern,
	      String typeNamePattern) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public String getSystemFunctions() throws SQLException {
	    return "";
	  }

	  public ResultSet getTablePrivileges(String catalog, String schemaPattern,
	      String tableNamePattern) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public ResultSet getTableTypes() throws SQLException {
	    final TableType[] tt = TableType.values();
	    ResultSet result = new MdrillMetaDataResultSet<TableType>(
	            Arrays.asList("TABLE_TYPE")
	            , Arrays.asList("STRING"), new ArrayList<TableType>(Arrays.asList(tt))) {
	      private int cnt = 0;

	      public boolean next() throws SQLException {
	        if (cnt<data.size()) {
	          List<Object> a = new ArrayList<Object>(1);
	          a.add(toJdbcTableType(data.get(cnt).name()));
	          row = a;
	          cnt++;
	          return true;
	        } else {
	          return false;
	        }
	      }
	    };
	    return result;
	  }

	  public ResultSet getTables(String catalog, String schemaPattern,
	                             String tableNamePattern, String[] types) throws SQLException {
		  throw new SQLException("Method not supported");
	  }

	  /**
	   * We sort the output of getTables to guarantee jdbc compliance.
	   * First check by table type then by table name
	   */
	  private class GetTablesComparator implements Comparator<JdbcTable> {

	    public int compare(JdbcTable o1, JdbcTable o2) {
	      int compareType = o1.getType().compareTo(o2.getType());
	      if (compareType==0) {
	        return o1.getTableName().compareTo(o2.getTableName());
	      } else {
	        return compareType;
	      }
	    }
	  }

	  /**
	   * Translate hive table types into jdbc table types.
	   * @param hivetabletype
	   * @return
	   */
	  public static String toJdbcTableType(String hivetabletype) {
	    if (hivetabletype==null) {
	      return null;
	    } else if (hivetabletype.equals(TableType.MANAGED_TABLE.toString())) {
	      return "TABLE";
	    } else if (hivetabletype.equals(TableType.VIRTUAL_VIEW.toString())) {
	      return "VIEW";
	    } else if (hivetabletype.equals(TableType.EXTERNAL_TABLE.toString())) {
	      return "EXTERNAL TABLE";
	    } else {
	      return hivetabletype;
	    }
	  }

	  public String getTimeDateFunctions() throws SQLException {
	    return "";
	  }

	  public ResultSet getTypeInfo() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public ResultSet getUDTs(String catalog, String schemaPattern,
	      String typeNamePattern, int[] types) throws SQLException {

	    return new MdrillMetaDataResultSet(
	            Arrays.asList("TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "CLASS_NAME", "DATA_TYPE"
	                    , "REMARKS", "BASE_TYPE")
	            , Arrays.asList("STRING", "STRING", "STRING", "STRING", "INT", "STRING", "INT")
	            , null) {

	      public boolean next() throws SQLException {
	        return false;
	      }
	    };
	  }

	  public String getURL() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public String getUserName() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public ResultSet getVersionColumns(String catalog, String schema, String table)
	      throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean insertsAreDetected(int type) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean isCatalogAtStart() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean isReadOnly() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean locatorsUpdateCopy() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean nullPlusNonNullIsNull() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean nullsAreSortedAtEnd() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean nullsAreSortedAtStart() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean nullsAreSortedHigh() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean nullsAreSortedLow() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean othersDeletesAreVisible(int type) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean othersInsertsAreVisible(int type) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean othersUpdatesAreVisible(int type) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean ownDeletesAreVisible(int type) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean ownInsertsAreVisible(int type) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean ownUpdatesAreVisible(int type) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean storesLowerCaseIdentifiers() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean storesMixedCaseIdentifiers() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean storesUpperCaseIdentifiers() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsANSI92EntryLevelSQL() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsANSI92FullSQL() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsANSI92IntermediateSQL() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsAlterTableWithAddColumn() throws SQLException {
	    return true;
	  }

	  public boolean supportsAlterTableWithDropColumn() throws SQLException {
	    return false;
	  }

	  public boolean supportsBatchUpdates() throws SQLException {
	    return false;
	  }

	  public boolean supportsCatalogsInDataManipulation() throws SQLException {
	    return false;
	  }

	  public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
	    return false;
	  }

	  public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
	    return false;
	  }

	  public boolean supportsCatalogsInProcedureCalls() throws SQLException {
	    return false;
	  }

	  public boolean supportsCatalogsInTableDefinitions() throws SQLException {
	    return false;
	  }

	  public boolean supportsColumnAliasing() throws SQLException {
	    return true;
	  }

	  public boolean supportsConvert() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsConvert(int fromType, int toType) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsCoreSQLGrammar() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsCorrelatedSubqueries() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsDataDefinitionAndDataManipulationTransactions()
	      throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsDifferentTableCorrelationNames() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsExpressionsInOrderBy() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsExtendedSQLGrammar() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsFullOuterJoins() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsGetGeneratedKeys() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsGroupBy() throws SQLException {
	    return true;
	  }

	  public boolean supportsGroupByBeyondSelect() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsGroupByUnrelated() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsIntegrityEnhancementFacility() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsLikeEscapeClause() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsLimitedOuterJoins() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsMinimumSQLGrammar() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsMixedCaseIdentifiers() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsMultipleOpenResults() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsMultipleResultSets() throws SQLException {
	    return false;
	  }

	  public boolean supportsMultipleTransactions() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsNamedParameters() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsNonNullableColumns() throws SQLException {
	    return false;
	  }

	  public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsOrderByUnrelated() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsOuterJoins() throws SQLException {
	    return true;
	  }

	  public boolean supportsPositionedDelete() throws SQLException {
	    return false;
	  }

	  public boolean supportsPositionedUpdate() throws SQLException {
	    return false;
	  }

	  public boolean supportsResultSetConcurrency(int type, int concurrency)
	      throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsResultSetHoldability(int holdability)
	      throws SQLException {
	    return false;
	  }

	  public boolean supportsResultSetType(int type) throws SQLException {
	    return true;
	  }

	  public boolean supportsSavepoints() throws SQLException {
	    return false;
	  }

	  public boolean supportsSchemasInDataManipulation() throws SQLException {
	    return false;
	  }

	  public boolean supportsSchemasInIndexDefinitions() throws SQLException {
	    return false;
	  }

	  public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
	    return false;
	  }

	  public boolean supportsSchemasInProcedureCalls() throws SQLException {
	    return false;
	  }

	  public boolean supportsSchemasInTableDefinitions() throws SQLException {
	    return false;
	  }

	  public boolean supportsSelectForUpdate() throws SQLException {
	    return false;
	  }

	  public boolean supportsStatementPooling() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsStoredProcedures() throws SQLException {
	    return false;
	  }

	  public boolean supportsSubqueriesInComparisons() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsSubqueriesInExists() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsSubqueriesInIns() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsSubqueriesInQuantifieds() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsTableCorrelationNames() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsTransactionIsolationLevel(int level)
	      throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsTransactions() throws SQLException {
	    return false;
	  }

	  public boolean supportsUnion() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean supportsUnionAll() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean updatesAreDetected(int type) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean usesLocalFilePerTable() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean usesLocalFiles() throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public boolean isWrapperFor(Class<?> iface) throws SQLException {
	    throw new SQLException("Method not supported");
	  }

	  public <T> T unwrap(Class<T> iface) throws SQLException {
	    throw new SQLException("Method not supported");
	  }




}
